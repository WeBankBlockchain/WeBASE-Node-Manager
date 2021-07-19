/**
 * Copyright 2014-2021  the original author or authors.
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
import com.webank.webase.node.mgr.base.enums.HostStatusEnum;
import com.webank.webase.node.mgr.base.enums.OptionType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.IPUtil;
import com.webank.webase.node.mgr.base.tools.NetUtils;
import com.webank.webase.node.mgr.cert.CertService;
import com.webank.webase.node.mgr.chain.ChainService;
import com.webank.webase.node.mgr.deploy.entity.DeployNodeInfo;
import com.webank.webase.node.mgr.deploy.entity.NodeConfig;
import com.webank.webase.node.mgr.deploy.entity.ReqAddNode;
import com.webank.webase.node.mgr.deploy.entity.TbAgency;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.entity.TbConfig;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Component
public class DeployService {

    @Autowired private TbConfigMapper tbConfigMapper;
    @Autowired private TbChainMapper tbChainMapper;
    @Autowired private FrontMapper frontMapper;
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

    @Qualifier(value = "deployAsyncScheduler")
    @Autowired private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    /**
     * generate chain config and front config in db, scp to remote and async start
     */
    @Transactional(propagation = Propagation.REQUIRED)
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
        // check image
        log.info("configChainAndScp check image on host:{}", imageTag);
        hostService.checkImageExistRemote(ipConf, imageTag);

        log.info("configChainAndScp configChain and init db data");
        // config locally(chain, front, group, front_group_map, agency etc.)
        boolean configSuccess = this.configChain(chainName, deployNodeInfoList, ipConf, imageTag, encrtypType,
            webaseSignAddr, agencyName);
        if (!configSuccess) {
            log.error("configChainAndScp fail to config chain and init db data");
            chainService.updateStatus(chainName, ChainStatusEnum.START_FAIL);
            throw new NodeMgrException(ConstantCode.CONFIG_CHAIN_LOCALLY_FAIL);
        }

        // scp config to host
        log.info("configChainAndScp start scpConfigHostList chainName:{}, hostIdList:{}", chainName, hostIdList);
        boolean configHostSuccess = hostService.scpConfigHostList(chainName, hostIdList);
        if (!configHostSuccess) {
            log.error("configChainAndScp scpConfigHostList fail, cannot start chain!");
            chainService.updateStatus(chainName, ChainStatusEnum.START_FAIL);
            throw new NodeMgrException(ConstantCode.ANSIBLE_INIT_HOST_CDN_SCP_NOT_ALL_SUCCESS);
        }
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

        // make sure sign not 127.0.0.1
        if (IPUtil.isLocal(webaseSignAddr)) {
            throw new NodeMgrException(ConstantCode.WEBASE_SIGN_NOT_LOCALHOST_ERROR);
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
     * Add a node. 扩容节点，并重启链的所有节点
     * include: gen config & update other nodes & restart all node
     * after check host and init host(dependency,port,image)
     */
    @Deprecated
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
        log.info("configChainAndScp check all host init success hostIdList:{}", hostIdList);
        hostService.checkAllHostInitSuc(hostIdList);

        // check image
        log.info("configChainAndScp check image on host:{}", chain.getVersion());
        hostService.checkImageExists(hostIdList, chain.getVersion());

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

        for (Integer hostId : hostIdAndInfoMap.keySet()) {
            Instant startTime = Instant.now();
            log.info("addNodes hostId:{}, startTime:{}", hostId, startTime.toEpochMilli());
            TbHost tbHost = tbHostMapper.selectByPrimaryKey(hostId);
            List<DeployNodeInfo> nodeListOnSameHost = hostIdAndInfoMap.get(hostId);

            // generate new sdk cert and scp to host
            log.info("addNodes generateHostSDKCertAndScp");
            hostService.generateHostSDKCertAndScp(chain.getEncryptType(), chain.getChainName(), tbHost, agency.getAgencyName());
            //hostService.scpHostSdkCert(chainName, tbHost);
            log.info("addNodes after generateHostSDKCertAndScp usedTime:{}", Duration.between(startTime, Instant.now()).toMillis());

            // 1.4.3 deprecated, when add nodes, not support add group id
            // update group node count
            // log.info("addNodes saveOrUpdateNodeCount groupId:{},new node num:{}", groupId, num);
            // groupService.saveOrUpdateNodeCount(groupId, num, chain.getId(), chainName);

            // init front and node (gen node cert & init db)
            try {
                // gen node cert and gen front's yml
                log.info("addNodes initFrontAndNode");
                List<TbFront> newFrontList = frontService.initFrontAndNode(nodeListOnSameHost, chain,
                    tbHost, agency.getId(), agency.getAgencyName(), groupId, FrontStatusEnum.ADDING);
                log.info("addNodes after initFrontAndNode usedTime:{}", Duration.between(startTime, Instant.now()).toMillis());

                // generate(or update existed) related node config files
                // and scp
                log.info("addNodes updateNodeConfigIniByGroupId groupId:{}", groupId);
                frontService.updateNodeConfigIniByGroupId(chain, groupId);
                log.info("addNodes after updateNodeConfigIniByGroupId usedTime:{}", Duration.between(startTime, Instant.now()).toMillis());

                // generate(or update existed) new group(node) config files and scp to remote
                log.info("addNodes generateNewNodesGroupConfigsAndScp chain:{},groupId:{},ip:{},newFrontList:{}",
                    chain, groupId, tbHost.getIp(), newFrontList);
                groupService.generateNewNodesGroupConfigsAndScp(chain, groupId,
                    tbHost.getIp(), newFrontList);
                log.info("addNodes after generateNewNodesGroupConfigsAndScp usedTime:{}", Duration.between(startTime, Instant.now()).toMillis());

                // init host
                // start all front on the host
                // restart related front
                log.info("addNodes asyncReStartNode");
                log.info("addNodes before asyncReStartNode usedTime:{}", Duration.between(startTime, Instant.now()).toMillis());
                List<Integer> frontIdList = newFrontList.stream().map(TbFront::getFrontId).collect(Collectors.toList());
                nodeAsyncService.asyncRestartNode(chain, groupId, OptionType.MODIFY_CHAIN, frontIdList);
                log.info("addNodes hostId:{}, usedTime:{}", hostId, Duration.between(startTime, Instant.now()).toMillis());
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

    public void stopNodeForce(String nodeId) {
        // two nodes running at least
        this.frontService.stopNodeForce(nodeId);
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

        // restart by web manually
        // restart related node
//        this.nodeAsyncService.asyncRestartRelatedFront(chain.getId(), groupIdSet, OptionType.MODIFY_CHAIN,
//                FrontStatusEnum.STARTING, FrontStatusEnum.RUNNING, FrontStatusEnum.STOPPED);

        // if error occur, throw out finally
        if (errorFlag != 0) {
            log.error("Update related group OR delete node's config files error. Check out upper error log");
            throw new NodeMgrException(ConstantCode.DELETE_NODE_DIR_ERROR);
        }
    }

    /**
     * Add a node. 扩容节点
     * include: gen config & update other nodes & restart all node
     * generateHostSDKCertAndScp 并发
     * initFrontAndNode 可以并发
     * updateNodeConfigIniByGroupId 更新group config.ini，在initFront后执行，可以直接select到，需要整合后一次执行，多个node并发scp节点配置；
     * generateNewNodesGroupConfigsAndScp， cp locally and scp 没有新群组，整合一次性复制即可
     * async add （整合后一次执行）
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Pair<RetCode, String> batchAddNode(ReqAddNode addNode)
        throws NodeMgrException, InterruptedException {

        int groupId = addNode.getGroupId();
        String chainName = addNode.getChainName();
        String agencyName = addNode.getAgencyName();

        List<DeployNodeInfo> deployNodeInfoList = addNode.getDeployNodeInfoList();

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
        log.info("batchAddNode start hostIdAndInfoMap:{}", hostIdAndInfoMap);
        List<Integer> hostIdList = new ArrayList<>(hostIdAndInfoMap.keySet());


        log.info("batchAddNode check chain name:[{}] exists...", chainName);
        TbChain chain = tbChainMapper.getByChainName(chainName);
        if (chain == null) {
            throw new NodeMgrException(ConstantCode.CHAIN_NAME_NOT_EXISTS_ERROR);
        }

        log.info("batchAddNode chainName:{},deployNodeInfoList:{},tagId:{},encrtypType:{},"
                + "webaseSignAddr:{},agencyName:{}", chainName, deployNodeInfoList,
            chain.getVersion(), chain.getEncryptType(), chain.getWebaseSignAddr(), agencyName);

        // check all host success (old or new host will set as init success in step init_host)
        log.info("batchAddNode check all host init success hostIdAndInfoMap:{}", hostIdList);
        hostService.checkAllHostInitSuc(hostIdList);

        // check image
        log.info("batchAddNode check image on host:{}", chain.getVersion());
        hostService.checkImageExists(hostIdList, chain.getVersion());

        TbAgency agency = this.agencyService.initAgencyIfNew(
            agencyName, chain.getId(), chainName, chain.getEncryptType());

        // new Front list record
        List<Integer> newFrontIdList = new ArrayList<>();
        List<TbFront> newFrontListStore = new ArrayList<>();
        final CountDownLatch configHostLatch = new CountDownLatch(CollectionUtils.size(hostIdAndInfoMap));
        // check success count
        AtomicInteger configSuccessCount = new AtomicInteger(0);
        Map<Integer, Future> taskMap = new HashedMap<>();
        // mark chain as adding ,to avoid refresh
        this.chainService.updateStatus(chain.getId(), ChainStatusEnum.NODE_ADDING);
        // concurrent add nodes in multi host
        for (final Integer hostId : hostIdAndInfoMap.keySet()) {
            Instant startTime = Instant.now();
            log.info("batchAddNode hostId:{}, startTime:{}", hostId, startTime.toEpochMilli());
            TbHost tbHost = tbHostMapper.selectByPrimaryKey(hostId);
            List<DeployNodeInfo> nodeListOnSameHost = hostIdAndInfoMap.get(hostId);
            Future<?> task = threadPoolTaskScheduler.submit(() -> {
                try {
                    // generate ip/agency/sdk cert and scp
                    log.info("batchAddNode generateHostSDKCertAndScp");
                    hostService.generateHostSDKCertAndScp(chain.getEncryptType(), chainName, tbHost, agencyName);

                    // init front config files and db data, including node's cert
                    log.info("batchAddNode initFrontAndNode");
                    List<TbFront> newFrontResult = frontService.initFrontAndNode(nodeListOnSameHost, chain,
                        tbHost, agency.getId(), agency.getAgencyName(), groupId, FrontStatusEnum.ADDING);
                    newFrontListStore.addAll(newFrontResult);
                    newFrontIdList.addAll(newFrontResult.stream().map(TbFront::getFrontId).collect(Collectors.toList()));
                    log.info("batchAddNode initFrontAndNode newFrontIdList:{}", newFrontIdList);

                    // generate(actual copy same old group of group1)
                    // and scp to target new Front
                    log.info("batchAddNode generateNewNodesGroupConfigsAndScp");
                    groupService.generateNewNodesGroupConfigsAndScp(chain, groupId, tbHost.getIp(), newFrontResult);
                    configSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("batchAddNode Exception:[].", e);
                    newFrontIdList.forEach((id -> frontService.updateStatus(id, FrontStatusEnum.ADD_FAILED)));
                    // update in each config process
                    //hostService.updateStatus(hostId, HostStatusEnum.CONFIG_FAIL, "batchAddNode failed" + e.getMessage());
                } finally {
                    configHostLatch.countDown();
                }
            });
            taskMap.put(hostId, task);
        }
        // await and check time out
        configHostLatch.await(constantProperties.getExecAddNodeTimeout(), TimeUnit.MILLISECONDS);
        log.info("Verify batchAddNode timeout");
        taskMap.forEach((key, value) -> {
            int hostId = key;
            Future<?> task = value;
            if (!task.isDone()) {
                log.error("batchAddNode:[{}] timeout, cancel the task.", hostId);
                newFrontIdList.forEach((id -> frontService.updateStatus(id, FrontStatusEnum.ADD_FAILED)));
                hostService.updateStatus(hostId, HostStatusEnum.CONFIG_FAIL, "batchAddNode failed for timeout");
                task.cancel(false);
            }
        });

        boolean hostConfigSuccess = configSuccessCount.get() == CollectionUtils.size(hostIdList);
        // check if all host init success
        log.log(hostConfigSuccess ? Level.INFO: Level.ERROR,
            "batchAddNode result, total:[{}], success:[{}]",
            CollectionUtils.size(hostIdAndInfoMap.keySet()), configSuccessCount.get());

        // update after all host config finish
        // select all node list into config.ini
        log.info("batchAddNode updateNodeConfigIniByGroupId");
        try {
            frontService.updateConfigIniByGroupIdAndNewFront(chain, groupId, newFrontListStore);
        } catch (IOException e) {
            log.error("batchAddNode updateNodeConfigIniByGroupId io Exception:[].", e);
            newFrontIdList.forEach((id -> frontService.updateStatus(id, FrontStatusEnum.ADD_FAILED)));
        }

        log.info("batchAddNode asyncStartAddedNode");
        // restart one node
        nodeAsyncService.asyncStartAddedNode(chain.getId(), OptionType.MODIFY_CHAIN, newFrontIdList);
        // restart all node to make sure 'nodeIdList' of each node contains removed node
        // nodeAsyncService.asyncRestartNode(chain, groupId, OptionType.MODIFY_CHAIN, newFrontIdList);

        return Pair.of(ConstantCode.SUCCESS, "success");
    }

    public void restartChain(String chainName, Integer groupId) {
        log.info("restartChain chainName:{},groupId:{}", chainName, groupId);
        TbChain chain = tbChainMapper.getByChainName(chainName);
        nodeAsyncService.asyncRestartRelatedFront(chain.getId(), Collections.singleton(groupId), OptionType.MODIFY_CHAIN,
            FrontStatusEnum.STARTING, FrontStatusEnum.RUNNING, FrontStatusEnum.STOPPED);
    }
}

