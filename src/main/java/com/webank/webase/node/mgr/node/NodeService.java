/**
 * Copyright 2014-2020  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.node.mgr.node;

import static com.webank.webase.node.mgr.base.code.ConstantCode.HOST_CONNECT_ERROR;
import static com.webank.webase.node.mgr.base.code.ConstantCode.IP_NUM_ERROR;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.code.RetCode;
import com.webank.webase.node.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.base.tools.SshTools;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.deploy.entity.ConfigLine;
import com.webank.webase.node.mgr.deploy.entity.TbAgency;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.entity.TbConfig;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbChainMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbConfigMapper;
import com.webank.webase.node.mgr.deploy.service.AgencyService;
import com.webank.webase.node.mgr.deploy.service.ChainService;
import com.webank.webase.node.mgr.deploy.service.HostService;
import com.webank.webase.node.mgr.deploy.service.PathService;
import com.webank.webase.node.mgr.deploy.service.ShellService;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.entity.PeerOfConsensusStatus;
import com.webank.webase.node.mgr.frontinterface.entity.PeerOfSyncStatus;
import com.webank.webase.node.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.node.entity.PeerInfo;

import lombok.extern.log4j.Log4j2;

/**
 * services for node data.
 */
@Log4j2
@Service
public class NodeService {

    @Autowired
    private NodeMapper nodeMapper;
    @Autowired
    private FrontInterfaceService frontInterface;

    @Autowired private TbConfigMapper tbConfigMapper;
    @Autowired private TbChainMapper tbChainMapper;
    @Autowired private AgencyService agencyService;
    @Autowired private HostService hostService;
    @Autowired private ChainService chainService;
    @Autowired private ShellService shellService;
    @Autowired private GroupService groupService;
    @Autowired private PathService pathService;

    // interval of check node status
    private static final Long CHECK_NODE_WAIT_MIN_MILLIS = 7500L;

    /**
     * add new node data.
     */
    public void addNodeInfo(Integer groupId, PeerInfo peerInfo) throws NodeMgrException {
        String nodeIp = null;
        Integer nodeP2PPort = null;

        if (StringUtils.isNotBlank(peerInfo.getIPAndPort())) {
            String[] ipPort = peerInfo.getIPAndPort().split(":");
            nodeIp = ipPort[0];
            nodeP2PPort = Integer.valueOf(ipPort[1]);
        }
        String nodeName = groupId + "_" + peerInfo.getNodeId();

        // add row
        TbNode tbNode = new TbNode();
        tbNode.setNodeId(peerInfo.getNodeId());
        tbNode.setGroupId(groupId);
        tbNode.setNodeIp(nodeIp);
        tbNode.setNodeName(nodeName);
        tbNode.setP2pPort(nodeP2PPort);
        nodeMapper.add(tbNode);
    }

    /**
     * query count of node.
     */
    public Integer countOfNode(NodeParam queryParam) throws NodeMgrException {
        log.debug("start countOfNode queryParam:{}", JSON.toJSONString(queryParam));
        try {
            Integer nodeCount = nodeMapper.getCount(queryParam);
            log.debug("end countOfNode nodeCount:{} queryParam:{}", nodeCount,
                    JSON.toJSONString(queryParam));
            return nodeCount;
        } catch (RuntimeException ex) {
            log.error("fail countOfNode . queryParam:{}", queryParam, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }


    /**
     * query node list by page.
     */
    public List<TbNode> qureyNodeList(NodeParam queryParam) throws NodeMgrException {
        log.debug("start qureyNodeList queryParam:{}", JSON.toJSONString(queryParam));

        // query node list
        List<TbNode> listOfNode = nodeMapper.getList(queryParam);

        log.debug("end qureyNodeList listOfNode:{}", JSON.toJSONString(listOfNode));
        return listOfNode;
    }

    /**
     * query node by groupId
     */
    public List<TbNode> queryByGroupId(int groupId) {
        NodeParam nodeParam = new NodeParam();
        nodeParam.setGroupId(groupId);
        return qureyNodeList(nodeParam);
    }

    /**
     * query all node list
     */
    public List<TbNode> getAll() {
        return qureyNodeList(new NodeParam());
    }

    /**
     * query node info.
     */
    public TbNode queryByNodeId(String nodeId) throws NodeMgrException {
        log.debug("start queryNode nodeId:{}", nodeId);
        try {
            TbNode nodeRow = nodeMapper.queryByNodeId(nodeId);
            log.debug("end queryNode nodeId:{} TbNode:{}", nodeId, JSON.toJSONString(nodeRow));
            return nodeRow;
        } catch (RuntimeException ex) {
            log.error("fail queryNode . nodeId:{}", nodeId, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }


    /**
     * update node info.
     */
    public void updateNode(TbNode tbNode) throws NodeMgrException {
        log.debug("start updateNodeInfo  param:{}", JSON.toJSONString(tbNode));
        Integer affectRow = 0;
        try {

            affectRow = nodeMapper.update(tbNode);
        } catch (RuntimeException ex) {
            log.error("updateNodeInfo exception", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_node");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
        log.debug("end updateNodeInfo");
    }

    /**
     * query node info.
     */
    public TbNode queryNodeInfo(NodeParam nodeParam) {
        log.debug("start queryNodeInfo nodeParam:{}", JSON.toJSONString(nodeParam));
        TbNode tbNode = nodeMapper.queryNodeInfo(nodeParam);
        log.debug("end queryNodeInfo result:{}", tbNode);
        return tbNode;
    }

    /**
     * delete by node and group.
     */
    public void deleteByNodeAndGroupId(String nodeId, int groupId) throws NodeMgrException {
        log.debug("start deleteByNodeAndGroupId nodeId:{} groupId:{}", nodeId, groupId);
        nodeMapper.deleteByNodeAndGroup(nodeId, groupId);
        log.debug("end deleteByNodeAndGroupId");
    }

    /**
     * delete by groupId.
     */
    public void deleteByGroupId(int groupId) {
        if (groupId == 0) {
            return;
        }
        nodeMapper.deleteByGroupId(groupId);
    }


    /**
     * check node status
     */
    public void checkAndUpdateNodeStatus(int groupId) {
        //get local node list
        List<TbNode> nodeList = queryByGroupId(groupId);

        //getPeerOfConsensusStatus
        List<PeerOfConsensusStatus> consensusList = getPeerOfConsensusStatus(groupId);
        if (Objects.isNull(consensusList)) {
            log.error("fail checkNodeStatus, consensusList is null");
            return;
        }

        // getObserverList
        List<String> observerList = frontInterface.getObserverList(groupId);

        for (TbNode tbNode : nodeList) {
            String nodeId = tbNode.getNodeId();
            BigInteger localBlockNumber = tbNode.getBlockNumber();
            BigInteger localPbftView = tbNode.getPbftView();
            LocalDateTime modifyTime = tbNode.getModifyTime();
            LocalDateTime createTime = tbNode.getCreateTime();

            Duration duration = Duration.between(modifyTime, LocalDateTime.now());
            Long subTime = duration.toMillis();
            if (subTime < CHECK_NODE_WAIT_MIN_MILLIS && createTime.isBefore(modifyTime)) {
                log.info("checkNodeStatus jump over. subTime:{}", subTime);
                return;
            }

            int nodeType = 0;   //0-consensus;1-observer
            if (observerList != null) {
                nodeType = observerList.stream()
                        .filter(observer -> observer.equals(tbNode.getNodeId())).map(c -> 1).findFirst()
                        .orElse(0);
            }

            BigInteger latestNumber = getBlockNumberOfNodeOnChain(groupId, nodeId);//blockNumber
            BigInteger latestView = consensusList.stream()
                    .filter(cl -> nodeId.equals(cl.getNodeId())).map(c -> c.getView()).findFirst()
                    .orElse(BigInteger.ZERO);//pbftView

            if (nodeType == 0) {    //0-consensus;1-observer
                // if local block number and pbftView equals chain's, invalid
                if (localBlockNumber.equals(latestNumber) && localPbftView.equals(latestView)) {
                    log.warn("node[{}] is invalid. localNumber:{} chainNumber:{} localView:{} chainView:{}",
                            nodeId, localBlockNumber, latestNumber, localPbftView, latestView);
                    tbNode.setNodeActive(DataStatus.INVALID.getValue());
                } else {
                    tbNode.setBlockNumber(latestNumber);
                    tbNode.setPbftView(latestView);
                    tbNode.setNodeActive(DataStatus.NORMAL.getValue());
                }
            } else { //observer
                // if latest block number not equal, invalid
                if (!latestNumber.equals(frontInterface.getLatestBlockNumber(groupId))) {
                    log.warn("node[{}] is invalid. localNumber:{} chainNumber:{} localView:{} chainView:{}",
                            nodeId, localBlockNumber, latestNumber, localPbftView, latestView);
                    tbNode.setNodeActive(DataStatus.INVALID.getValue());
                } else {
                    tbNode.setBlockNumber(latestNumber);
                    tbNode.setPbftView(latestView);
                    tbNode.setNodeActive(DataStatus.NORMAL.getValue());
                }
            }
            tbNode.setModifyTime(LocalDateTime.now());
            //update node
            updateNode(tbNode);
        }

    }


    /**
     * get latest number of peer on chain.
     */
    private BigInteger getBlockNumberOfNodeOnChain(int groupId, String nodeId) {
        SyncStatus syncStatus = frontInterface.getSyncStatus(groupId);
        if (nodeId.equals(syncStatus.getNodeId())) {
            return syncStatus.getBlockNumber();
        }
        List<PeerOfSyncStatus> peerList = syncStatus.getPeers();
        BigInteger latestNumber = peerList.stream().filter(peer -> nodeId.equals(peer.getNodeId()))
                .map(s -> s.getBlockNumber()).findFirst().orElse(BigInteger.ZERO);//blockNumber
        return latestNumber;
    }


    /**
     * get peer of consensusStatus
     */
    private List<PeerOfConsensusStatus> getPeerOfConsensusStatus(int groupId) {
        String consensusStatusJson = frontInterface.getConsensusStatus(groupId);
        if (StringUtils.isBlank(consensusStatusJson)) {
            log.debug("getPeerOfConsensusStatus is null: {}", consensusStatusJson);
            return null;
        }
        JSONArray jsonArr = JSONArray.parseArray(consensusStatusJson);
        List<Object> dataIsList = jsonArr.stream().filter(jsonObj -> jsonObj instanceof List)
                .map(arr -> {
                    Object obj = JSONArray.parseArray(JSON.toJSONString(arr)).get(0);
                    try {
                        NodeMgrTools.object2JavaBean(obj, PeerOfConsensusStatus.class);
                    } catch (Exception e) {
                        return null;
                    }
                    return arr;
                }).collect(Collectors.toList());
        return JSONArray
                .parseArray(JSON.toJSONString(dataIsList.get(0)), PeerOfConsensusStatus.class);
    }

    /**
     * add sealer and observer in NodeList
     * return: List<String> nodeIdList
     */
    public List<PeerInfo> getSealerAndObserverList(int groupId) {
        log.debug("start getSealerAndObserverList groupId:{}", groupId);
        List<String> sealerList = frontInterface.getSealerList(groupId);
        List<String> observerList = frontInterface.getObserverList(groupId);
        List<PeerInfo> resList = new ArrayList<>();
        sealerList.stream().forEach(nodeId -> resList.add(new PeerInfo(nodeId)));
        observerList.stream().forEach(nodeId -> resList.add(new PeerInfo(nodeId)));
        log.debug("end getSealerAndObserverList resList:{}", resList);
        return resList;
    }


    /**
     * Add in v1.4.0 deploy.
     *
     * @param ipConf
     * @param tagId
     * @param rootDirOnHost
     * @return
     */
    @Transactional
    public Pair<RetCode, String> deploy(String chainName,
                                        String[] ipConf,
                                        int tagId,
                                        String rootDirOnHost) throws NodeMgrException {
        // verify tagId if exists
        TbConfig imageTag = tbConfigMapper.selectByPrimaryKey(tagId);
        if (imageTag == null
                || StringUtils.isBlank(imageTag.getConfigValue())) {
            throw new NodeMgrException(ConstantCode.TAG_ID_PARAM_ERROR);
        }

        // validate ipConf config
        List<ConfigLine> configLineList = this.validateIpConf(ipConf);

        TbChain chain = tbChainMapper.selectByChainName(chainName);
        if (chain != null) {
            throw new NodeMgrException(ConstantCode.CHAIN_NAME_EXISTS_ERROR);
        }

        byte encryptType = (byte) (imageTag.getConfigValue().endsWith("-gm") ?
                EncryptType.SM2_TYPE : EncryptType.ECDSA_TYPE);

        try {
            // generate nodes config
            ExecuteResult buildChainResult = shellService.execBuildChain(encryptType, ipConf, chainName);
            if (buildChainResult.failed()) {
                return Pair.of(ConstantCode.EXEC_BUILD_CHAIN_ERROR, buildChainResult.getExecuteOut());
            }

            // insert chain
            final TbChain newChain = this.chainService.insert(chainName, chainName,
                    imageTag.getConfigValue(), encryptType, ChainStatusEnum.INITIALIZED);

            Map<String, Integer> agencyIdMap = new HashMap<>();
            Map<String, Integer> hostIdMap = new HashMap<>();
            Map<Integer, AtomicInteger> groupCountMap = new HashMap<>();

            configLineList.forEach((config) -> {
                // insert agency
                if (!agencyIdMap.containsKey(config.getAgencyName())) {
                    TbAgency agency = agencyService.insert(config.getAgencyName(), config.getAgencyName(),
                            newChain.getId(), newChain.getChainName());
                    agencyIdMap.put(config.getAgencyName(), agency.getId());
                }

                // insert host
                if (!hostIdMap.containsKey(config.getIp())) {
                    TbHost host = hostService.insert(agencyIdMap.get(config.getAgencyName()),
                            config.getAgencyName(), config.getIp(), rootDirOnHost);
                    hostIdMap.put(config.getIp(), host.getId());
                }

                // insert group
                // sum node num in group
                config.getGroupIdSet().forEach((groupId) -> {
                    if (groupCountMap.containsKey(groupId)) {
                        groupCountMap.get(groupId).addAndGet(config.getNum());
                    } else {
                        groupService.saveGroupId(groupId,config.getNum(),
                                newChain.getId(),newChain.getChainName(),"");
                        groupCountMap.put(groupId, new AtomicInteger(config.getNum()));
                    }
                });

                // TODO. save node

                // TODO. save front

                // TODO. save front_group

            });

            // update group node count
            groupCountMap.forEach((groupId,nodeCount) -> {
                // TODO. upate node count
//                groupService.updateGroupStatus();

            });



        } catch (
                Exception e) {
            // TODO. delete ipConf file and nodes config
        } finally {

        }

        return null;
    }

    /**
     * Validate ipConf.
     *
     * @param ipConf
     * @throws NodeMgrException
     */
    public List<ConfigLine> validateIpConf(String[] ipConf) throws NodeMgrException {
        if (ArrayUtils.isEmpty(ipConf)) {
            throw new NodeMgrException(ConstantCode.IP_CONF_PARAM_NULL_ERROR);
        }

        List<ConfigLine> configLineList = new ArrayList<>();
        for (String line : ipConf) {
            if (StringUtils.isBlank(line)) {
                continue;
            }

            ConfigLine configLine = ConfigLine.parseLine(line);
            if (configLine == null) {
                continue;
            }

            // SSH to host ip
            if (!SshTools.iSConnectable(configLine.getIp())) { // cannot SSH to IP
                throw new NodeMgrException(HOST_CONNECT_ERROR.msg(configLine.getIp()));
            }

            // TODO. Get max mem size, check nodes num.
            if (configLine.getNum() <= 0) {
                throw new NodeMgrException(IP_NUM_ERROR.msg(line));
            }

            configLineList.add(configLine);
        }

        if (CollectionUtils.isEmpty(configLineList)) {
            throw new NodeMgrException(ConstantCode.IP_CONF_PARAM_NULL_ERROR);
        }

        return configLineList;
    }

    /**
     * Insert values from ipConf.
     *
     * @param ipConf
     * @throws NodeMgrException
     */
    public void insertIpConf(String[] ipConf) throws NodeMgrException {
        // validate ip address
        if (ArrayUtils.isEmpty(ipConf)) {
            throw new NodeMgrException(ConstantCode.IP_CONF_PARAM_NULL_ERROR);
        }

        // TODO.
        for (String conf : ipConf) {
            if (StringUtils.isBlank(conf)) {
                continue;
            }
            conf.split(" ");


        }
    }
}
