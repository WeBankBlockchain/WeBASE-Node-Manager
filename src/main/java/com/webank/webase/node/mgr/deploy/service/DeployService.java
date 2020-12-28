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

import static com.webank.webase.node.mgr.base.code.ConstantCode.SAME_HOST_ERROR;

import com.webank.webase.node.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.node.mgr.base.enums.HostStatusEnum;
import com.webank.webase.node.mgr.deploy.entity.DeployNodeInfo;
import com.webank.webase.node.mgr.deploy.entity.IpConfigParse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.code.RetCode;
import com.webank.webase.node.mgr.base.enums.DockerImageTypeEnum;
import com.webank.webase.node.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.node.mgr.base.enums.OptionType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.IPUtil;
import com.webank.webase.node.mgr.base.tools.NetUtils;
import com.webank.webase.node.mgr.base.tools.SshTools;
import com.webank.webase.node.mgr.base.tools.ValidateUtil;
import com.webank.webase.node.mgr.chain.ChainService;
import com.webank.webase.node.mgr.deploy.entity.NodeConfig;
import com.webank.webase.node.mgr.deploy.entity.ReqAdd;
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
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.node.NodeService;

import lombok.extern.log4j.Log4j2;

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


    /**
     * generate chain config and front config in db, scp to remote and async start
     */
    public void configChainAndScp(String chainName, List<DeployNodeInfo> deployNodeInfoList, String[] ipConf,
        int tagId, int encrtypType, String webaseSignAddr, String agencyName)
        throws InterruptedException {
        // convert to host id list by distinct id
        List<Integer> hostIdList = deployNodeInfoList.stream().map(DeployNodeInfo::getHostId).collect(
            Collectors.toList());
        if (StringUtils.isBlank(agencyName)) {
            agencyName = constantProperties.getDefaultAgencyName();
        }
        log.info("configChainAndScp chainName:{},deployNodeInfoList:{},ipConf:{},tagId:{},encrtypType:{},"
                + "webaseSignAddr:{},agencyName:{}", chainName, deployNodeInfoList, ipConf, tagId, encrtypType,
            webaseSignAddr, agencyName);

        log.info("configChainAndScp check allHostInitSuccess");
        // check all host success
        AtomicBoolean allHostInitSuccess = new AtomicBoolean(true);
        hostIdList.forEach(hId -> {
            TbHost host = tbHostMapper.selectByPrimaryKey(hId);
            if (HostStatusEnum.INIT_SUCCESS.getId() != host.getStatus()) {
                allHostInitSuccess.set(false);
            }
        });
        if (!allHostInitSuccess.get()) {
            log.error("configChainAndScp stop for not all host init success");
            throw new NodeMgrException(ConstantCode.NOT_ALL_HOST_INIT_SUCCESS);
        }

        log.info("configChainAndScp configChain and init db data");
        // config locally
        boolean configSuccess = this.configChain(chainName, deployNodeInfoList, ipConf, tagId, encrtypType,
            webaseSignAddr, agencyName);
        // config success, use ansible to scp config & load image
        if (!configSuccess) {
            log.error("configChainAndScp fail to config chain and init db data");
            throw new NodeMgrException(ConstantCode.CONFIG_CHAIN_LOCALLY_FAIL);
        }
        // scp config to host
        log.info("configChainAndScp start scpConfigHostList chainName:{},hostIdList:{}", chainName, hostIdList);
        boolean configHostSuccess = hostService.scpConfigHostList(chainName, hostIdList);
        if (!configHostSuccess) {
            log.error("configChainAndScp config success but image not on remote host, cannot start chain!");
            throw new NodeMgrException(ConstantCode.ANSIBLE_INIT_HOST_CDN_SCP_NOT_ALL_SUCCESS);
        }
        // check image
        hostService.checkImageExistRemote(ipConf, tagId);
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
     * @param tagId
     * @param encryptType
     * @param webaseSignAddr
     * @param agencyName one agency
     * manually or pull from hub or pull cdn
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean configChain(String chainName,  List<DeployNodeInfo> deployNodeInfoList, String[] ipConf, int tagId, int encryptType,
        String webaseSignAddr, String agencyName) throws NodeMgrException {
        log.info("start configChain chainName:{},ipConf:{}", chainName, ipConf);
//        DockerImageTypeEnum imageTypeEnum = DockerImageTypeEnum.getById(dockerImageType);
//        if (imageTypeEnum == null){
//            throw new NodeMgrException(ConstantCode.UNKNOWN_DOCKER_IMAGE_TYPE);
//        }

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
            tagId, encryptType, webaseSignAddr, agencyName);

        return genSuccess;
        // start node and start front
//        this.nodeAsyncService.asyncConfigChain(chainName, OptionType.DEPLOY_CHAIN);
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

        // todo whether check host init success
        boolean initSuccess = true;
        if (configSuccess && initSuccess) {
            // start chain
            nodeAsyncService.asyncStartChain(chain.getId(), optionType, ChainStatusEnum.RUNNING, ChainStatusEnum.START_FAIL,
                FrontStatusEnum.INITIALIZED, FrontStatusEnum.RUNNING, FrontStatusEnum.STOPPED);
        } else {
            log.error("Init host list not success:[{}]", chainName);
            chainService.updateStatus(chain.getId(), ChainStatusEnum.START_FAIL);
        }

    }

    /**
     *
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

        try {
            this.chainService.delete(chainName);
        } catch (IOException e) {
            log.error("Delete chain:[{}] error.", chainName, e);
            throw new NodeMgrException(ConstantCode.DELETE_CHAIN_ERROR);
        }

        return ConstantCode.SUCCESS;
    }

    /**
     * Add a node. 扩容节点
     *
     * @param add
     * @return
     * @throws NodeMgrException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Pair<RetCode, String> addNodes(ReqAdd add) throws NodeMgrException {
        String chainName = add.getChainName();
        String ip = add.getIp() ;
        int num = add.getNum();
        String agencyName = add.getAgencyName();
        byte dockerImageType = add.getDockerImageType();
        int groupId = add.getGroupId();

        log.info("Add node check chain name:[{}] exists...", chainName);
        TbChain chain = tbChainMapper.getByChainName(chainName);
        if (chain == null) {
            throw new NodeMgrException(ConstantCode.CHAIN_NAME_NOT_EXISTS_ERROR);
        }

        // allow local
//        if (IPUtil.isLocal(ip)){
//            throw new NodeMgrException(SAME_HOST_ERROR);
//        }

        log.info("Add node check ip format:[{}]...", ip);
        if (!ValidateUtil.ipv4Valid(ip)) {
            throw new NodeMgrException(ConstantCode.IP_FORMAT_ERROR);
        }

        // todo check ip connect by ansible
        log.info("Add node check ip reachable:[{}]...", ip);
        if (!SshTools.connect(ip, constantProperties.sshDefaultUser, constantProperties.sshDefaultPort,
            constantProperties.getPrivateKey())) {
            throw new NodeMgrException(ConstantCode.HOST_CONNECT_ERROR);
        }

        // select host list by agency id
        List<TbHost> tbHostList = this.hostService.selectHostListByChainId(chain.getId());

        // check host exists by ip
        TbHost tbHostExists = tbHostList.stream().filter(host ->
            StringUtils.equalsIgnoreCase(ip, host.getIp())).findFirst().orElse(null);

        // init agency cert
        TbAgency agency = null;
        if (tbHostExists == null) {
            log.info("Add node check num:[{}]...", num);
            if (num <= 0 || num > ConstantProperties.MAX_NODE_ON_HOST) {
                throw new NodeMgrException(ConstantCode.NODES_NUM_EXCEED_MAX_ERROR);
            }

            if (StringUtils.isBlank(agencyName)) {
                // agency name cannot be blank when host ip is new
                throw new NodeMgrException(ConstantCode.AGENCY_NAME_EMPTY_ERROR);
            }

            // check docker image exists, default pull cdn
            DockerImageTypeEnum dockerImageTypeEnum = DockerImageTypeEnum.getById(dockerImageType);
            dockerImageTypeEnum = dockerImageTypeEnum == null ? DockerImageTypeEnum.PULL_CDN : dockerImageTypeEnum;
            if (DockerImageTypeEnum.MANUAL == dockerImageTypeEnum){
                this.hostService.checkImageExists(Collections.singleton(ip), chain.getVersion());
            }

            // a new host IP address, check agency name is new
            agency = this.agencyService.initAgencyIfNew(
                    agencyName, chain.getId(), chainName, chain.getEncryptType());

            // generate sdk config files
            tbHostExists = this.hostService.generateHostSDKAndScp(chain.getEncryptType(), chain.getChainName(), add.getRootDirOnHost(),
                ip, agency.getAgencyName());
        } else {
            // exist host
            agency = this.tbAgencyMapper.getByChainIdAndAgencyName(chain.getId(), agencyName);

            int currentNodeNum = this.frontMapper.countByHostId(tbHostExists.getId());
            if (currentNodeNum + num > ConstantProperties.MAX_NODE_ON_HOST){
                throw new NodeMgrException(ConstantCode.NODES_NUM_EXCEED_MAX_ERROR);
            }
        }

        // init group, if group is new, return true
        Pair<TbGroup, Boolean> isNewGroup = this.groupService.saveOrUpdateNodeCount(groupId,
                num, chain.getId(), chainName);
        TbGroup group = isNewGroup.getKey();
        boolean newGroup = isNewGroup.getValue();

        // todo add node split gene config and start
        // init front and node
        try {
            // gen node cert and gen front's yml
            List<TbFront> newFrontList = this.frontService.initFrontAndNode(num, chain,
                    tbHostExists, agency.getId(), agency.getAgencyName(), group, FrontStatusEnum.ADDING);

            // generate(or update existed) related node config files
            this.frontService.updateNodeConfigIniByGroupId(chain, groupId);

            // generate(or update existed) new group(node) config files and scp to remote
            this.groupService.generateNewNodesGroupConfigsAndScp(newGroup, chain, groupId,
                    tbHostExists.getIp(), newFrontList);

            // init host
            // start all front on the host
            // restart related front
            this.nodeAsyncService.asyncAddNode(chain, tbHostExists, group, OptionType.MODIFY_CHAIN, newFrontList);
        } catch (Exception e) {
            log.error("Add node error", e);
            throw new NodeMgrException(ConstantCode.ADD_NODE_WITH_UNKNOWN_EXCEPTION_ERROR, e);
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
     * @param deleteHost default false
     * @param deleteAgency
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteNode(String nodeId,
                           boolean deleteHost,
                           boolean deleteAgency ) {
        log.info("deleteNode nodeId:{},deleteHost:{},deleteAgency:{}", nodeId, deleteHost, deleteAgency);
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
        Set<Integer> groupIdSet = NodeConfig.getGroupIdSet(nodePath,encryptType);
        try {
            // update related node's config.ini file, e.g. p2p
            this.frontService.updateNodeConfigIniByGroupList(chain, groupIdSet);
        } catch (IOException e) {
            log.error("Delete node, update related group:[{}] node's config error ", groupIdSet, e);
            throw new NodeMgrException(ConstantCode.UPDATE_RELATED_NODE_ERROR);
        }
        // move node directory to tmp
        try {
            this.pathService.deleteNode(chain.getChainName(), host.getIp(), front.getHostIndex(), front.getNodeId());
        } catch (IOException e) {
            log.error("Delete node:[{}:{}:{}] config files error.",
                    chain.getChainName(), host.getIp(), front.getHostIndex(), e);
            throw new NodeMgrException(ConstantCode.DELETE_NODE_DIR_ERROR);
        }

        // move node of remote host files to temp directory, e.g./opt/fisco/delete-tmp
        nodeService.mvNodeOnRemoteHost(host.getIp(), host.getRootDir(), chain.getChainName(), front.getHostIndex(),
                front.getNodeId());

        // delete front, node in db
        this.frontService.removeFront(front.getFrontId());

        // delete host, default false
        if (deleteHost) {
            this.hostService.deleteHostWithNoNode(host.getId());
        }

        // delete agency
        this.agencyService.deleteAgencyWithNoNode(deleteAgency,host.getId());

        // restart related node
        this.nodeAsyncService.asyncRestartRelatedFront(chain.getId(), groupIdSet, OptionType.MODIFY_CHAIN,
                FrontStatusEnum.STARTING,FrontStatusEnum.RUNNING,FrontStatusEnum.STOPPED);
    }

    /**
     *
     * @param chainName
     */
    public int progress(String chainName) {

        log.info("Progress check chain name:[{}] exists...", chainName);
        TbChain chain = tbChainMapper.getByChainName(chainName);
        if (chain == null) {
            throw new NodeMgrException(ConstantCode.CHAIN_NAME_NOT_EXISTS_ERROR);
        }

        return this.chainService.progress(chain);
    }


}

