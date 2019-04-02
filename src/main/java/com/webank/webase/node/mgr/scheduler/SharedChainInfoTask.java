/*
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.scheduler;

import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.enums.ContractStatus;
import com.webank.webase.node.mgr.base.enums.HasPk;
import com.webank.webase.node.mgr.base.enums.ShareType;
import com.webank.webase.node.mgr.base.enums.UserType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.Web3Tools;
import com.webank.webase.node.mgr.contract.ContractMapper;
import com.webank.webase.node.mgr.contract.entity.ContractParam;
import com.webank.webase.node.mgr.contract.ContractService;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.TransactionParam;
import com.webank.webase.node.mgr.monitor.MonitorService;
import com.webank.webase.node.mgr.network.NetworkService;
import com.webank.webase.node.mgr.network.TbNetwork;
import com.webank.webase.node.mgr.user.TbUser;
import com.webank.webase.node.mgr.user.UserMapper;
import com.webank.webase.node.mgr.user.UserService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class SharedChainInfoTask {

    @Autowired
    private UserService userService;
    @Autowired
    private ContractMapper contractMapper;
    @Autowired
    private FrontService frontService;
    @Autowired
    private NetworkService networkService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ContractService contractService;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private ConstantProperties cp;
    /*@Autowired
    private NodeMapper nodeMapper;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private OrganizationService organizationService;
*/

    /**
     * task start.
     */
    public void shareAllNetworkInfo() {
        Instant startTime = Instant.now();
        log.info("start shareAllNetworkInfo. startTime:{}", startTime.toEpochMilli());
        List<TbNetwork> networkList = null;
        try {
            networkList = networkService.getAllNetwork();
            if (networkList == null || networkList.size() == 0) {
                log.error("jump over shareAllNetworkInfo. did not find any networks");
                return;
            }
        } catch (Exception ex) {
            log.error("fail shareAllNetworkInfo.", ex);
        }

        // request share methods
        for (TbNetwork tbNetwork : networkList) {
            // share contract
            sharedContractFromChain(tbNetwork.getNetworkId());
            // share user
            sharedUserFromChain(tbNetwork.getNetworkId());
            // share node
            //sharedNodeFromChain(tbNetwork.getNetworkId());
        }

        log.info("end shareAllNetworkInfo. useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * share contract form chain.
     */
    @SuppressWarnings("unchecked")
    public void sharedContractFromChain(Integer networkId) {
        log.debug("start sharedContractFromChain. networkId:{}", networkId);

        try {
            // query system contract:contractDetail
            TbContract systemContract = contractService
                .querySystemContract(networkId, cp.getSysContractContractdetailName());
            if (systemContract == null) {
                log.error("not found contract:contractdetail");
                new NodeMgrException(ConstantCode.NOT_FOUND_CONTRACTDETAIL);
            }
            String systemContractV = systemContract.getContractVersion();

            Integer systemUserId = userService.queryIdOfSystemUser(networkId);
            Integer currentDbMaxIndex = contractMapper.queryMaxChainIndex(networkId);
            Integer countContractOnChain = getCountContractFromChain(networkId, systemUserId,
                systemContractV);
            Integer currentDbMaxIndexVal = currentDbMaxIndex != null ? currentDbMaxIndex : -1;

            if (countContractOnChain == null || countContractOnChain == 0
                || currentDbMaxIndexVal == countContractOnChain) {
                log.info(
                    "sharedContractFromChain jump over. networkId:{}"
                        + " countContractOnChain:{} currentDbMaxIndexVal:{}",
                    networkId, countContractOnChain, currentDbMaxIndexVal);
                return;
            }

            for (int chainIndex = 0; chainIndex < countContractOnChain; chainIndex++) {
                // get contract from chain
                TransactionParam postParam = new TransactionParam(systemUserId, systemContractV,
                    cp.getSysContractContractdetailName(),
                    "getContractAtIndex", Arrays.asList(chainIndex));
                List<Object> contractList = frontService
                    .sendTransactionForEntity(networkId, postParam, List.class);

                if (contractList == null) {
                    log.error("invalid contract index. chainIndex:{}", chainIndex);
                    throw new NodeMgrException(ConstantCode.INVALID_CONTRACT_INDEX);
                }
                String contractNameInChain = (String) contractList.get(0);
                String[] infoArr = contractNameInChain.split("\\" + ConstantProperties.NAME_SPRIT);
                String contractName = infoArr[0];
                String contractVersion = infoArr[1];

                // query contract
                ContractParam param = new ContractParam();
                param.setNetworkId(networkId);
                param.setContractName(contractName);
                param.setContractVersion(contractVersion);
                TbContract dbContract = contractService.queryContract(param);

                if (dbContract != null && dbContract.getChainIndex() == null) {
                    log.info(
                        "update chain index. networkId:{} contractName:{}"
                            + " contractVersion:{} newIndex:{}",
                        networkId, contractName, contractVersion, chainIndex);
                    dbContract.setChainIndex(chainIndex);
                    contractMapper.updateContract(dbContract);
                    continue;
                } else if (dbContract != null) {
                    log.info(
                        "jump over chain index. networkId:{} contractName:{} "
                            + "contractVersion:{} newIndex:{}", networkId, contractName,
                        contractVersion, chainIndex);
                    continue;
                }

                String deployTimeStr = (String) contractList.get(4);
                List<String> dtl = Arrays.asList(
                    deployTimeStr.replace("[", "").replace("]", "")
                        .replaceAll(" ", "").split(","));
                LocalDateTime deployTime = LocalDateTime
                    .of(parseInt(dtl.get(0)), parseInt(dtl.get(1)), parseInt(dtl.get(2)),
                        parseInt(dtl.get(3)),
                        parseInt(dtl.get(4)), parseInt(dtl.get(5)), parseInt(dtl.get(6)));

                // add contract info to db
                TbContract tbContract = new TbContract();
                tbContract.setContractName(contractName);
                tbContract.setContractVersion(contractVersion);
                tbContract.setContractAbi((String) contractList.get(1));
                tbContract.setContractBin((String) contractList.get(2));
                tbContract.setContractSource((String) contractList.get(5));
                tbContract.setContractStatus(ContractStatus.DEPLOYED.getValue());
                tbContract.setDeployTime(deployTime);
                tbContract.setNetworkId(networkId);
                tbContract.setChainIndex(chainIndex);
                tbContract.setDescription("shared from chain");

                Integer affectRow = contractMapper.addContractRow(tbContract);
                if (affectRow == 0) {
                    log.warn("affect 0 rows of tb_contract");
                    throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
                }
                // update monitor unusual contract's info
                monitorService.updateUnusualContract(networkId, tbContract.getContractName(),
                    tbContract.getContractBin());
            }
        } catch (Exception ex) {
            log.error("share contract fail.", ex);
        }

        log.debug("end sharedContractFromChain. networkId:{}", networkId);
    }

    /**
     * get count of contract from chain.
     */
    @SuppressWarnings("unchecked")
    private Integer getCountContractFromChain(Integer networkId, Integer systemUserId,
        String systemContractV) throws NodeMgrException {
        log.debug(
            "start getCountContractFromChain. networkId:{} systemUserId:{}  systemContractV:{}",
            networkId, systemUserId, systemContractV);

        // request node front
        TransactionParam postParam = new TransactionParam(systemUserId, systemContractV,
            cp.getSysContractContractdetailName(), "getContractCount",
            new ArrayList<>());
        List<Integer> list = frontService
            .sendTransactionForEntity(networkId, postParam, List.class);
        Integer contractCountOnChain = list.get(0);

        log.debug("end getCountContractFromChain. networkId:{} contractCountOnChain:{}", networkId,
            contractCountOnChain);
        return contractCountOnChain;
    }

    /*@SuppressWarnings("unchecked")
    public void sharedNodeFromChain(Integer networkId) {
        log.debug("start sharedNodeFromChain. networkId:{}", networkId);

        try {
            // query system contract:node
            TbContract systemContract = contractService
                .querySystemContract(networkId, cp.getSysContractNodeName());
            if (systemContract == null) {
                log.error("not found contract:node");
                new NodeMgrException(ConstantCode.NOT_FOUND_NODECONTRACT);
            }
            String systemContractV = systemContract.getContractVersion();

            Integer systemUserId = userService.queryIdOfSystemUser(networkId);
            Integer currentDbMaxIndex = nodeMapper.queryMaxChainIndex(networkId);
            Integer countNodeOnChain = getCountNodeFromChain(networkId, systemUserId,
                systemContractV);
            Integer currentDbMaxIndexVal = currentDbMaxIndex != null ? currentDbMaxIndex : -1;

            if (countNodeOnChain == null || countNodeOnChain == 0
                || currentDbMaxIndexVal == countNodeOnChain) {
                log.info(
                    "sharedNodeFromChain jump over. networkId:{} countNodeOnChain:{} "
                        + "currentDbMaxIndexVal:{}", networkId, countNodeOnChain,
                    currentDbMaxIndexVal);
                return;
            }

            for (int chainIndex = 0; chainIndex < countNodeOnChain; chainIndex++) {
                // get node from chain
                TransactionParam postParam = new TransactionParam(systemUserId, systemContractV,
                    cp.getSysContractNodeName(), "getNodeAtIndex",
                    Arrays.asList(new Integer[]{chainIndex}));
                List<Object> userList = frontService
                    .sendTransactionForEntity(networkId, postParam, List.class);

                if (userList == null) {
                    log.error("invalid node index. chainIndex:{}", chainIndex);
                    throw new NodeMgrException(ConstantCode.INVALID_NODE_INDEX);
                }

                String nodeIpInChain = (String) userList.get(3);
                // param
                NodeParam queryParam = new NodeParam();
                queryParam.setNetworkId(networkId);
                queryParam.setNodeIp(nodeIpInChain);

                // query node
                TbNode dbNode = nodeService.queryNodeInfo(queryParam);

                if (dbNode != null && dbNode.getChainIndex() == null) {
                    log.info("update chain index. networkId:{} nodeIp:{}", networkId,
                        nodeIpInChain);
                    dbNode.setChainIndex(chainIndex);
                    nodeMapper.updateNodeInfo(dbNode);
                    continue;
                } else if (dbNode != null) {
                    log.info("jump over chain index. networkId:{} nodeIp:{} newIndex:{}", networkId,
                        nodeIpInChain, chainIndex);
                    continue;
                }

                // add organization info
                TbOrganization organization = new TbOrganization();
                String orgNameInChain = (String) userList.get(2);
                organization.setNetworkId(networkId);
                organization.setOrgName(orgNameInChain);
                organization.setOrgType(OrgType.OTHER.getValue());
                organization.setDescription("shared from chain");
                Integer orgId = organizationService.addOrganizationInfo(organization);

                // add node info to db
                String nodeNameInChain = (String) userList.get(0);
                String[] infoArr = nodeNameInChain.split("\\" + ConstantProperties.NAME_SPRIT);
                String nodeName = infoArr[0];
                Integer rpcPort = parseInt(infoArr[1]);
                Integer p2pPort = parseInt(infoArr[2]);
                Integer channelPort = parseInt(infoArr[3]);

                TbNode nodeRow = new TbNode();
                nodeRow.setNetworkId(networkId);
                nodeRow.setNodeIp(nodeIpInChain);
                nodeRow.setP2pPort(p2pPort);
                nodeRow.setRpcPort(rpcPort);
                nodeRow.setChannelPort(channelPort);
                nodeRow.setNodeName(nodeName);
                nodeRow.setOrgId(orgId);
                nodeRow.setNodeType(NodeType.OTHER.getValue());
                nodeRow.setChainIndex(chainIndex);
                nodeRow.setDescription("share from chain");

                Integer affectRow = nodeMapper.addNodeRow(nodeRow);
                if (affectRow == 0) {
                    log.warn("affect 0 rows of tb_node");
                    throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
                }
            }
        } catch (Exception ex) {
            log.error("share node fail.", ex);
        }
        log.debug("end sharedNodeFromChain. networkId:{}", networkId);
    }*/


    /*@SuppressWarnings("unchecked")
    private Integer getCountNodeFromChain(Integer networkId, Integer systemUserId,
        String systemContractV) throws NodeMgrException {
        log.debug("start getCountNodeFromChain. networkId:{} systemUserId:{} sysssstemContractV:{}",
            networkId, systemUserId, systemContractV);

        // request node front
        TransactionParam postParam = new TransactionParam(systemUserId, systemContractV,
            cp.getSysContractNodeName(), "getNodeCount", new ArrayList<>());

        List<Integer> list = frontService
            .sendTransactionForEntity(networkId, postParam, List.class);
        Integer nodeCountOnChain = list.get(0);
        // Integer nodeCountOnChain = new Integer(countStr);

        log.debug("end getCountUserFromChain. networkId:{} nodeCountOnChain:{}", networkId,
            nodeCountOnChain);
        return nodeCountOnChain;
    }*/

    /**
     * share user info from chain.
     */
    @SuppressWarnings("unchecked")
    public void sharedUserFromChain(Integer networkId) {
        log.debug("start sharedUserFromChain. networkId:{}", networkId);

        try {
            // query system contract:user
            TbContract systemContract = contractService
                .querySystemContract(networkId, cp.getSysContractUserName());
            if (systemContract == null) {
                log.error("not found contract:user");
                new NodeMgrException(ConstantCode.NOT_FOUND_USERCONTRACT);
            }
            String systemContractV = systemContract.getContractVersion();

            Integer systemUserId = userService.queryIdOfSystemUser(networkId);
            Integer currentDbMaxIndex = userMapper.queryMaxChainIndex(networkId);
            Integer countUserOnChain = getCountUserFromChain(networkId, systemUserId,
                systemContractV);
            Integer currentDbMaxIndexVal = currentDbMaxIndex != null ? currentDbMaxIndex : -1;

            if (countUserOnChain == null || countUserOnChain == 0
                || currentDbMaxIndexVal == countUserOnChain) {
                log.info(
                    "sharedUserFromChain jump over. networkId:{} countUserOnChain:{} "
                        + "currentDbMaxIndexVal:{}",
                    networkId, countUserOnChain, currentDbMaxIndexVal);
                return;
            }

            for (int chainIndex = 0; chainIndex < countUserOnChain; chainIndex++) {
                // get user from chain
                TransactionParam postParam = new TransactionParam(systemUserId, systemContractV,
                    cp.getSysContractUserName(), "getUserAtIndex",
                    Arrays.asList(chainIndex));
                List<Object> userList = frontService
                    .sendTransactionForEntity(networkId, postParam, List.class);

                if (userList == null) {
                    log.error("invalid user index. chainIndex:{}", chainIndex);
                    throw new NodeMgrException(ConstantCode.INVALID_USER_INDEX);
                }
                String userNameInChain = (String) userList.get(0);
                String orgIdInChain = (String) userList.get(2);
                String publicKeyInChain = (String) userList.get(3);
                Integer orgId = parseInt(orgIdInChain);

                // query user
                TbUser dbUser = userService.queryUser(networkId, userNameInChain, orgId);

                if (dbUser != null && dbUser.getChainIndex() == null) {
                    log.info("update chain index. networkId:{} orgId:{} userName:{} newIndex:{}",
                        networkId, orgId, userNameInChain, chainIndex);
                    dbUser.setChainIndex(chainIndex);
                    userMapper.updateUser(dbUser);
                    continue;
                } else if (dbUser != null) {
                    log.info("jump over chain index. networkId:{} orgId:{} userName:{} newIndex:{}",
                        networkId, orgId, userNameInChain, chainIndex);
                    continue;
                }

                String address = Web3Tools.getAddressByPublicKey(publicKeyInChain);

                // add user info to db
                TbUser userRow = new TbUser(chainIndex, HasPk.NONE.getValue(),
                    UserType.GENERALUSER.getValue(), userNameInChain, networkId, orgId, address,
                    publicKeyInChain, "shared from chain");
                Integer affectRow = userMapper.addUserRow(userRow);
                if (affectRow == 0) {
                    log.warn("affect 0 rows of tb_user");
                    throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
                }

                // update monitor unusual user's info
                monitorService.updateUnusualUser(networkId, userNameInChain, address);
            }
        } catch (Exception ex) {
            log.error("share user fail.", ex);
        }
        log.debug("end sharedUserFromChain. networkId:{}", networkId);
    }

    /**
     * get count of user from chain.
     */
    @SuppressWarnings("unchecked")
    private Integer getCountUserFromChain(Integer networkId, Integer systemUserId,
        String systemContractV) throws NodeMgrException {
        log.debug("start getCountUserFromChain. networkId:{} systemUserId:{} systemContractV:{}",
            networkId, systemUserId, systemContractV);

        // request node front
        TransactionParam postParam = new TransactionParam(systemUserId, systemContractV,
            cp.getSysContractUserName(), "getUserCount", new ArrayList<>());

        List<Integer> list = frontService
            .sendTransactionForEntity(networkId, postParam, List.class);
        Integer userCountOnChain = list.get(0);
        log.debug("end getCountUserFromChain. networkId:{} userCountOnChain:{}", networkId,
            userCountOnChain);
        return userCountOnChain;
    }

    /**
     * share info from chain async.
     */
    @Async("asyncServiceExecutor")
    public void asyncShareFromChain(Integer networkId, ShareType shareType) {
        log.debug("start asyncShareFromChain. networkId:{} shareType:{}", networkId, shareType);

        switch (shareType) {
            /* case NODE:
                sharedNodeFromChain(networkId);
                break;*/
            case USER:
                sharedUserFromChain(networkId);
                break;
            case CONTRACT:
                sharedContractFromChain(networkId);
                break;
            default:
                break;
        }
    }

    private int parseInt(Object str) {
        return Integer.parseInt((String) str);
    }
}
