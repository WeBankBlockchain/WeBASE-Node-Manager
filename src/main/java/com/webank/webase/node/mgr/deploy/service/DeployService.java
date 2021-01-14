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

package com.webank.webase.node.mgr.deploy.service;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.code.RetCode;
import com.webank.webase.node.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.node.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.node.mgr.base.enums.OptionType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NetUtils;
import com.webank.webase.node.mgr.base.tools.ProgressTools;
import com.webank.webase.node.mgr.cert.CertService;
import com.webank.webase.node.mgr.chain.ChainService;
import com.webank.webase.node.mgr.deploy.entity.DeployNodeInfo;
import com.webank.webase.node.mgr.deploy.entity.NodeConfig;
import com.webank.webase.node.mgr.deploy.entity.ReqAddNode;
import com.webank.webase.node.mgr.deploy.entity.TbAgency;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.entity.TbConfig;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbAgencyMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbChainMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbConfigMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbHostMapper;
import com.webank.webase.node.mgr.front.FrontMapper;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.node.NodeService;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Component
public class DeployService {

    @Autowired private TbConfigMapper tbConfigMapper;
    @Autowired private TbChainMapper tbChainMapper;
    @Autowired private FrontMapper frontMapper;
    @Autowired private TbAgencyMapper tbAgencyMapper;
    @Autowired private TbHostMapper tbHostMapper;

    @Autowired private AgencyService agencyService;
    @Autowired private HostService hostService;
    @Autowired private GroupService groupService;
    @Autowired private FrontService frontService;
    @Autowired private ChainService chainService;
    @Autowired private NodeAsyncService nodeAsyncService;
    @Autowired private PathService pathService;
    @Autowired private ConstantProperties constantProperties;
    @Autowired private NodeService nodeService;
    @Autowired private CertService certService;

    /**
     * generate chain config and front config in db, scp to remote and async start
     */
    public void configChainAndScp(String chainName, List<DeployNodeInfo> deployNodeInfoList, String[] ipConf,
        String imageTag, int encrtypType, String webaseSignAddr, String agencyName)
        throws InterruptedException {
        // convert to host id list by distinct id
        List<Integer> hostIdList = deployNodeInfoList.stream().map(DeployNodeInfo::getHostId).collect(
            Collectors.toList());
        log.info("configChainAndScp chainName:{},deployNodeInfoList:{},ipConf:{},imageTag:{},encrtypType:{},"
                + "webaseSignAddr:{},agencyName:{}", chainName, deployNodeInfoList, ipConf, imageTag, encrtypType,
            webaseSignAddr, agencyName);

        log.info("configChainAndScp check all host init success");
        // check all host success
        hostService.checkAllHostInitSuc(hostIdList);

        log.info("configChainAndScp configChain and init db data");
        // config locally
        boolean configSuccess = this.configChain(chainName, deployNodeInfoList, ipConf, imageTag, encrtypType,
            webaseSignAddr, agencyName);
        if (!configSuccess) {
            log.error("configChainAndScp fail to config chain and init db data");
            throw new NodeMgrException(ConstantCode.CONFIG_CHAIN_LOCALLY_FAIL);
        }

        // scp config to host
        log.info("configChainAndScp start scpConfigHostList chainName:{}, hostIdList:{}", chainName, hostIdList);
        boolean configHostSuccess = hostService.scpConfigHostList(chainName, hostIdList);
        if (!configHostSuccess) {
            log.error("configChainAndScp config success but image not on remote host, cannot start chain!");
            chainService.updateStatus(chainName, ChainStatusEnum.START_FAIL);
            throw new NodeMgrException(ConstantCode.ANSIBLE_INIT_HOST_CDN_SCP_NOT_ALL_SUCCESS);
        }
        // check image
        hostService.checkImageExistRemote(ipConf, imageTag);
        // start
        log.info("configChainAndScp asyncStartChain chainName:{}", chainName);
        TbChain chain = tbChainMapper.getByChainName(chainName);
        nodeAsyncService.asyncStartChain(chain.getId(), OptionType.DEPLOY_CHAIN, ChainStatusEnum.RUNNING, ChainStatusEnum.START_FAIL,
            FrontStatusEnum.INITIALIZED, FrontStatusEnum.RUNNING, FrontStatusEnum.STOPPED);
    }


    /**
     * Add in v1.4.0 deploy.
     * generate chain config locally and async deploy chain
     * @param chainName
     * @param ipConf
     * @param imageTag
     * @param encryptType
     * @param webaseSignAddr
     * @param agencyName one agency
     * manually or pull from hub or pull cdn
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean configChain(String chainName, List<DeployNodeInfo> deployNodeInfoList, String[] ipConf, String imageTag,
        int encryptType, String webaseSignAddr, String agencyName) throws NodeMgrException {
        log.info("start configChain chainName:{},ipConf:{}", chainName, ipConf);

        if (StringUtils.isBlank(chainName)) {
            throw new NodeMgrException(ConstantCode.PARAM_EXCEPTION);
        }

        // check already init
        TbChain chain = tbChainMapper.getByChainName(chainName);
        // if initChainData fail, transactional revert
        if (chain != null && chain.getChainStatus() == ChainStatusEnum.INITIALIZED.getId()) {
            log.info("chain :{} has already been initialized", chain.getChainName());
            return true;
        }

        // check WeBASE Sign accessible
        if (StringUtils.isBlank(webaseSignAddr)
                || ! NetUtils.checkAddress(webaseSignAddr, 2000) ) {
            throw new NodeMgrException(ConstantCode.WEBASE_SIGN_CONFIG_ERROR);
        }

        // generate config files(chain's config&cert) gen front's yml
        // and insert data to db （chain update as initialized
        boolean genSuccess = chainService.generateConfigLocalAndInitDb(chainName, deployNodeInfoList, ipConf,
            imageTag, encryptType, webaseSignAddr, agencyName);
        return genSuccess;
    }

    /**
     * split config and start chain
     * @param chainName
     * @param optionType
     */
    public void startChain(String chainName, OptionType optionType) {
        log.info("startChain chainName:{},optionType:{}", chainName, optionType);
        TbChain chain = this.tbChainMapper.getByChainName(chainName);
        if (chain == null) {
            log.error("No chain:[{}] to deploy.", chainName);
            return;
        }

        boolean configSuccess = chain.getChainStatus() == ChainStatusEnum.INITIALIZED.getId();
        log.info("startChain configSuccess:{}", configSuccess);

        if (configSuccess) {
            // start chain
            nodeAsyncService.asyncStartChain(chain.getId(), optionType, ChainStatusEnum.RUNNING, ChainStatusEnum.START_FAIL,
                FrontStatusEnum.INITIALIZED, FrontStatusEnum.RUNNING, FrontStatusEnum.STOPPED);
        } else {
            log.error("Init host list not success:[{}]", chainName);
            chainService.updateStatus(chain.getId(), ChainStatusEnum.START_FAIL);
        }

    }

    /**
     * <p>
     * Delete a chain by chain name.
     *
     * @param chainName
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public RetCode deleteChain(String chainName) {
        log.info("Delete chain:[{}] data in db and config files...", chainName);
        if (StringUtils.isBlank(chainName)) {
            throw new NodeMgrException(ConstantCode.PARAM_EXCEPTION);
        }
        this.chainService.delete(chainName);
        return ConstantCode.SUCCESS;
    }

    /**
     * Add a node. 扩容节点
     * include: gen config & update other nodes & restart all node
     * after check host and init host(dependency,port,image)
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Pair<RetCode, String> addNodes(ReqAddNode addNode) throws NodeMgrException {

        int groupId = addNode.getGroupId();
        String chainName = addNode.getChainName();
        String agencyName = addNode.getAgencyName();

        List<DeployNodeInfo> deployNodeInfoList = addNode.getDeployNodeInfoList();
        // convert to host id list by distinct id
        List<Integer> hostIdList = deployNodeInfoList.stream().map(DeployNodeInfo::getHostId).collect(
            Collectors.toList());

        log.info("Add node check chain name:[{}] exists...", chainName);
        TbChain chain = tbChainMapper.getByChainName(chainName);
        if (chain == null) {
            throw new NodeMgrException(ConstantCode.CHAIN_NAME_NOT_EXISTS_ERROR);
        }

        log.info("addNodes chainName:{},deployNodeInfoList:{},tagId:{},encrtypType:{},"
                + "webaseSignAddr:{},agencyName:{}", chainName, deployNodeInfoList,
            chain.getVersion(), chain.getEncryptType(), chain.getWebaseSignAddr(), agencyName);

        // check all host success (old or new host will set as init success in step init_host)
        hostService.checkAllHostInitSuc(hostIdList);

        TbAgency agency = this.agencyService.initAgencyIfNew(
            agencyName, chain.getId(), chainName, chain.getEncryptType());

        // deployNodeInfo group by host id
        Map<Integer, List<DeployNodeInfo>> hostIdAndInfoMap = new HashMap<>();
        for (DeployNodeInfo nodeInfo : deployNodeInfoList) {
            Integer hostId = nodeInfo.getHostId();
            List<DeployNodeInfo> value = hostIdAndInfoMap.get(hostId);
            if (value == null) {
                value = new ArrayList<>();
            }
            value.add(nodeInfo);
            hostIdAndInfoMap.put(hostId, value);
        }
        log.info("addNodes hostIdAndInfoMap:{}", hostIdAndInfoMap);

        // store node count in tbHost's remark
        // List<TbHost> hostList = hostService.selectDistinctHostListById(hostIdList);
        for (Integer hostId : hostIdAndInfoMap.keySet()) {
            TbHost tbHost = tbHostMapper.selectByPrimaryKey(hostId);
            List<DeployNodeInfo> nodeListOnSameHost = hostIdAndInfoMap.get(hostId);

            // node number in one host when adding
            int num = nodeListOnSameHost.size();
            // generate new sdk cert and scp to host
            log.info("addNodes generateHostSDKCertAndScp");
            hostService.generateHostSDKCertAndScp(chain.getEncryptType(), chain.getChainName(), tbHost, agency.getAgencyName());

            // update group node count
            log.info("addNodes saveOrUpdateNodeCount groupId:{},new node num:{}", groupId, num);
            groupService.saveOrUpdateNodeCount(groupId, num, chain.getId(), chainName);

            // init front and node (gen node cert & init db)
            try {
                // gen node cert and gen front's yml
                log.info("addNodes initFrontAndNode");
                List<TbFront> newFrontList = frontService.initFrontAndNode(nodeListOnSameHost, chain,
                    tbHost, agency.getId(), agency.getAgencyName(), groupId, FrontStatusEnum.ADDING);

                // generate(or update existed) related node config files
                log.info("addNodes updateNodeConfigIniByGroupId groupId:{}", groupId);
                frontService.updateNodeConfigIniByGroupId(chain, groupId);

                // generate(or update existed) new group(node) config files and scp to remote
                log.info("addNodes generateNewNodesGroupConfigsAndScp chain:{},groupId:{},ip:{},newFrontList:{}",
                    chain, groupId, tbHost.getIp(), newFrontList);
                groupService.generateNewNodesGroupConfigsAndScp(chain, groupId,
                    tbHost.getIp(), newFrontList);

                // init host
                // start all front on the host
                // restart related front
                log.info("addNodes asyncAddNode");
                nodeAsyncService.asyncAddNode(chain, tbHost, groupId, OptionType.MODIFY_CHAIN, newFrontList);
            } catch (Exception e) {
                log.error("Add node error", e);
                throw new NodeMgrException(ConstantCode.ADD_NODE_WITH_UNKNOWN_EXCEPTION_ERROR, e);
            }

        }

        return Pair.of(ConstantCode.SUCCESS, "success");
    }



    /**
     *
     * @param newTagId
     * @param chainName
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void upgrade(int newTagId, String chainName) {
        // check tagId existed
        TbConfig newTagConfig = tbConfigMapper.selectByPrimaryKey(newTagId);
        if (newTagConfig == null
                || StringUtils.isBlank(newTagConfig.getConfigValue())) {
            throw new NodeMgrException(ConstantCode.TAG_ID_PARAM_ERROR);
        }

        log.info("Upgrade check chain name:[{}] exists...", chainName);
        TbChain chain = tbChainMapper.getByChainName(chainName);
        if (chain == null) {
            throw new NodeMgrException(ConstantCode.CHAIN_NAME_NOT_EXISTS_ERROR);
        }

        boolean sameTagVersion = StringUtils.equalsIgnoreCase(chain.getVersion(),newTagConfig.getConfigValue());
        if (sameTagVersion){
            throw new NodeMgrException(ConstantCode.UPGRADE_WITH_SAME_TAG_ERROR);
        }

        this.chainService.upgrade(chain,newTagConfig.getConfigValue());
    }

    /**
     * Start a node by nodeId.
     *
     * @param nodeId
     * @return
     */
    public void startNode(String nodeId, OptionType optionType, FrontStatusEnum before,
                          FrontStatusEnum success, FrontStatusEnum failed) {
        this.frontService.restart(nodeId, optionType, before, success, failed);
    }

    /**
     * Stop a node by nodeId.
     *
     * @param nodeId
     * @return
     */
    public void stopNode(String nodeId) {
        // two nodes running at least
        this.frontService.stopNode(nodeId);
    }

    /**
     *  @param nodeId
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteNode(String nodeId) {
        log.info("deleteNode nodeId:{}", nodeId);
        int errorFlag = 0;
        // remove front
        TbFront front = this.frontMapper.getByNodeId(nodeId);
        if (front == null) {
            throw new NodeMgrException(ConstantCode.NODE_ID_NOT_EXISTS_ERROR);
        }

        // check front status
        if (FrontStatusEnum.isRunning(front.getStatus())) {
            throw new NodeMgrException(ConstantCode.NODE_RUNNING_ERROR);
        }

        TbChain chain = this.tbChainMapper.selectByPrimaryKey(front.getChainId());
        TbHost host = this.tbHostMapper.selectByPrimaryKey(front.getHostId());
        final byte encryptType = chain.getEncryptType();

        // get delete node's group id list from ./NODES_ROOT/default_chain/ip/node[x]/conf/group.[groupId].genesis
        Path nodePath = this.pathService.getNodeRoot(chain.getChainName(), host.getIp(), front.getHostIndex());
        Set<Integer> groupIdSet = NodeConfig.getGroupIdSet(nodePath, encryptType);
        log.info("deleteNode updateNodeConfigIniByGroupList chain:{}, groupIdSet:{}", chain, groupIdSet);
        // update related node's config.ini file, e.g. p2p
        try {
            log.info("deleteNode updateNodeConfigIniByGroupList chain:{}, groupIdSet:{}", chain, groupIdSet);
            // update related node's config.ini file, e.g. p2p
            this.frontService.updateNodeConfigIniByGroupList(chain, groupIdSet);
        } catch (IOException e) {
            errorFlag++;
            log.error("Delete node, update related group:[{}] node's config error ", groupIdSet, e);
            log.error("Please update related node's group config manually");
        }

        // move node directory to tmp
        try {
            this.pathService.deleteNode(chain.getChainName(), host.getIp(), front.getHostIndex(), front.getNodeId());
        } catch (IOException e) {
            errorFlag++;
            log.error("Delete node's config files:[{}:{}:{}] error.",
                    chain.getChainName(), host.getIp(), front.getHostIndex(), e);
            log.error("Please move/rm node's config files manually");
        }

        // move node of remote host files to temp directory, e.g./opt/fisco/delete-tmp
        nodeService.mvNodeOnRemoteHost(host.getIp(), host.getRootDir(), chain.getChainName(), front.getHostIndex(),
                front.getNodeId());

        // delete front, node in db
        this.frontService.removeFront(front.getFrontId());
        // delete all certs
        // set CertTools.isPullFrontCertsDone to false after asyncRestartRelatedFront finished
        certService.deleteAll();

        // restart related node
        this.nodeAsyncService.asyncRestartRelatedFront(chain.getId(), groupIdSet, OptionType.MODIFY_CHAIN,
                FrontStatusEnum.STARTING, FrontStatusEnum.RUNNING, FrontStatusEnum.STOPPED);

        // if error occur, throw out finally
        if (errorFlag != 0) {
            log.error("Update related group OR delete node's config files error. Check out upper error log");
            throw new NodeMgrException(ConstantCode.DELETE_NODE_DIR_ERROR);
        }
    }


}

