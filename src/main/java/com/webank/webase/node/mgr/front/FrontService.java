/**
 * Copyright 2014-2021  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.node.mgr.front;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.base.enums.GroupType;
import com.webank.webase.node.mgr.base.enums.HostStatusEnum;
import com.webank.webase.node.mgr.base.enums.OptionType;
import com.webank.webase.node.mgr.base.enums.RunTypeEnum;
import com.webank.webase.node.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.config.properties.VersionProperties;
import com.webank.webase.node.mgr.deploy.chain.ChainService;
import com.webank.webase.node.mgr.deploy.entity.DeployNodeInfo;
import com.webank.webase.node.mgr.deploy.entity.NodeConfig;
import com.webank.webase.node.mgr.deploy.entity.TbAgency;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbChainMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbHostMapper;
import com.webank.webase.node.mgr.deploy.service.AgencyService;
import com.webank.webase.node.mgr.deploy.service.AnsibleService;
import com.webank.webase.node.mgr.deploy.service.DeployShellService;
import com.webank.webase.node.mgr.deploy.service.DockerCommandService;
import com.webank.webase.node.mgr.deploy.service.HostService;
import com.webank.webase.node.mgr.deploy.service.PathService;
import com.webank.webase.node.mgr.front.entity.FrontInfo;
import com.webank.webase.node.mgr.front.entity.FrontNodeConfig;
import com.webank.webase.node.mgr.front.entity.FrontParam;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.front.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapCache;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapMapper;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.node.mgr.frontgroupmap.entity.TbFrontGroupMap;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.node.NodeMapper;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.entity.NodeParam;
import com.webank.webase.node.mgr.node.entity.TbNode;
import com.webank.webase.node.mgr.scheduler.ResetGroupListTask;
import com.webank.webase.node.mgr.tools.CertTools;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.tools.NetUtils;
import com.webank.webase.node.mgr.tools.NodeMgrTools;
import com.webank.webase.node.mgr.tools.NumberUtil;
import com.webank.webase.node.mgr.tools.ProgressTools;
import com.webank.webase.node.mgr.tools.ThymeleafUtil;
import com.webank.webase.node.mgr.tools.cmd.ExecuteResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.fisco.bcos.sdk.client.protocol.response.BcosGroupInfo.GroupInfo;
import org.fisco.bcos.sdk.client.protocol.response.SyncStatus.PeersInfo;
import org.fisco.bcos.sdk.client.protocol.response.SyncStatus.SyncStatusInfo;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.CryptoType;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * service of web3.
 */
@Log4j2
@Service
public class FrontService {

    @Autowired
    private FrontMapper frontMapper;
    @Autowired
    private TbHostMapper tbHostMapper;
    @Autowired
    private NodeMapper nodeMapper;
    @Autowired
    private FrontGroupMapMapper frontGroupMapMapper;
    @Autowired
    private TbChainMapper tbChainMapper;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private FrontGroupMapService frontGroupMapService;
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;
    @Autowired
    private ResetGroupListTask resetGroupListTask;
    @Autowired
    private ConstantProperties constants;
    @Autowired
    private AgencyService agencyService;
    @Autowired
    private PathService pathService;
    @Autowired
    private DeployShellService deployShellService;
    @Autowired
    private ConstantProperties constant;
    @Autowired
    private DockerCommandService dockerOptions;
    @Autowired
    private AnsibleService ansibleService;
    @Autowired
    private HostService hostService;
    @Autowired
    private ChainService chainService;
    @Qualifier(value = "deployAsyncScheduler")
    @Autowired private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Autowired private CryptoSuite cryptoSuite;
    // version to check
    @Autowired
    private VersionProperties versionProperties;
    // interval of check front status
    private static final Long CHECK_FRONT_STATUS_WAIT_MIN_MILLIS = 3000L;


    /**
     * add new front, save front, frontGroupMap, check front's groupStatus, refresh nodeList
     */
    @Transactional(isolation= Isolation.READ_COMMITTED)
    public TbFront newFront(FrontInfo frontInfo) {
        log.debug("start newFront frontInfo:{}", frontInfo);
        TbFront tbFront = new TbFront();
        tbFront.setRunType(RunTypeEnum.COMMAND.getId());
        // set default chainId
        tbFront.setChainId(0);
        tbFront.setChainName("default");
        // default normal front
        tbFront.setStatus(DataStatus.NORMAL.getValue());

        String frontIp = frontInfo.getFrontIp();
        Integer frontPort = frontInfo.getFrontPort();
        //check valid ip
        checkNotSupportIp(frontIp);
        //check front ip and port
        NodeMgrTools.checkServerConnect(frontIp, frontPort);
        //query group list
        List<String> groupIdList = null;
        try {
            groupIdList = frontInterface.getGroupListFromSpecificFront(frontIp, frontPort);
        } catch (Exception e) {
            log.error("fail newFront, frontIp:{},frontPort:{}",frontIp,frontPort);
            throw new NodeMgrException(ConstantCode.REQUEST_FRONT_FAIL);
        }
        //check front not exist todo front根据rpc判断是否
        SyncStatusInfo syncStatus = frontInterface.getSyncStatusFromSpecificFront(frontIp,
            frontPort, groupIdList.get(0));
        FrontParam param = new FrontParam();
        log.info("node id is " + syncStatus.getNodeId());
        param.setNodeId(syncStatus.getNodeId());
        int count = getFrontCount(param);
        if (count > 0) {
            throw new NodeMgrException(ConstantCode.FRONT_EXISTS);
        }
        // copy attribute
        BeanUtils.copyProperties(frontInfo, tbFront);
        tbFront.setNodeId(syncStatus.getNodeId());

        // get front server version and sign server version
        try {
            String frontVersion = frontInterface.getFrontVersionFromSpecificFront(frontIp, frontPort);
            String signVersion = frontInterface.getSignVersionFromSpecificFront(frontIp, frontPort);
            tbFront.setFrontVersion(frontVersion);
            tbFront.setSignVersion(signVersion);
        } catch (Exception e) {
            // catch old version front and sign that not have '/version' api
            log.warn("get version of Front and Sign failed (required front and sign v1.4.0+) exception:[]", e);
        }
        //save front info
        try{
            frontMapper.add(tbFront);
        } catch (Exception e) {
            log.warn("fail newFront, after save, tbFront:{}, exception:{}",
                JsonTools.toJSONString(tbFront), e);
            throw new NodeMgrException(ConstantCode.SAVE_FRONT_FAIL.getCode(), e.getMessage());
        }
        // save group info todo抛出异常后，没有回滚front
        saveGroup(groupIdList, tbFront);
        // pull cert from new front and its node
        CertTools.isPullFrontCertsDone = false;
        // clear cache
        frontGroupMapCache.clearMapList();
        return tbFront;
    }

    /**
     * save group, frontGroupMap, nodeList
     * @param groupIdList
     * @param tbFront
     */
    @Transactional(isolation= Isolation.READ_COMMITTED)
    public void saveGroup(List<String> groupIdList, TbFront tbFront) {
        String frontIp = tbFront.getFrontIp();
        Integer frontPort = tbFront.getFrontPort();
        for (String groupId : groupIdList) {
            // peer in group
            List<String> nodesInGroup = frontInterface.getSealerObserverFromSpecificFront(frontIp, frontPort, groupId);
            // add group
            // check group not existed or node count differs
            TbGroup checkGroup = groupService.getGroupById(groupId);
            if (Objects.isNull(checkGroup) || nodesInGroup.size() != checkGroup.getNodeCount()) {
                Integer encryptType = frontInterface.getEncryptTypeFromSpecificFront(frontIp, frontPort, groupId);
                groupService.saveGroup(groupId, nodesInGroup.size(), "synchronous",
                    GroupType.SYNC, GroupStatus.NORMAL,0,"", encryptType);
            }
            //save front group map
            frontGroupMapService.newFrontGroup(tbFront, groupId);
            //save nodes
            for (String nodeId : nodesInGroup) {
                nodeService.addNodeInfo(groupId, nodeId);
            }
            // add sealer(consensus node) and observer in nodeList
//            refreshSealerAndObserverInNodeList(frontIp, frontPort, groupId);
        }
    }

    /**
     * add sealer(consensus node) and observer in nodeList
     * @param groupId
     */
    public void refreshSealerAndObserverInNodeList(String frontIp, int frontPort, String groupId) {
        log.debug("start refreshSealerAndObserverInNodeList frontIp:{}, frontPort:{}, groupId:{}",
            frontIp, frontPort, groupId);
        List<String> nodeInGroupList = frontInterface.getSealerObserverFromSpecificFront(frontIp, frontPort, groupId);
        log.debug("refreshSealerAndObserverInNodeList nodeInGroupList:{}",
            nodeInGroupList);
        nodeInGroupList.forEach(nodeId -> {
            NodeParam checkParam = new NodeParam();
            checkParam.setGroupId(groupId);
            checkParam.setNodeId(nodeId);
            int existedNodeCount = nodeService.countOfNode(checkParam);
            log.debug("addSealerAndObserver nodeId:{},existedNodeCount:{}",
                nodeId, existedNodeCount);
            if (existedNodeCount == 0) {
                nodeService.addNodeInfo(groupId, nodeId);
            }
        });
        log.debug("end addSealerAndObserver");
    }

    /**
     * check not support ip.
     */
    public void checkNotSupportIp(String ip) {

        String ipConfig = constants.getNotSupportFrontIp();
        if(StringUtils.isBlank(ipConfig)) {
            return;
        }
        List<String> list = Arrays.asList(ipConfig.split(","));
        if (list.contains(ip)) {
            throw new NodeMgrException(ConstantCode.INVALID_FRONT_IP);
        }
    }

    /**
     * check front ip and prot
     *
     * if exist:throw exception
     */
    private void checkFrontNotExist(String frontIp, int frontPort) {
        SyncStatusInfo syncStatus = frontInterface.getSyncStatusFromSpecificFront(frontIp, frontPort, "1");
        FrontParam param = new FrontParam();
        param.setNodeId(syncStatus.getNodeId());
        int count = getFrontCount(param);
        if (count > 0) {
            throw new NodeMgrException(ConstantCode.FRONT_EXISTS);
        }
    }


    /**
     * get front count
     */
    public int getFrontCount(FrontParam param) {
        Integer count = frontMapper.getCount(param);
        return count == null ? 0 : count;
    }

    /**
     * get front list
     */
    public List<TbFront> getFrontList(FrontParam param) {
        return frontMapper.getList(param);
    }

    /**
     * query front by frontId.
     */
    public TbFront getById(int frontId) {
        if (frontId == 0) {
            return null;
        }
        return frontMapper.getById(frontId);
    }

    /**
     * query front by nodeId.
     */
    public TbFront getByNodeId(String nodeId) {
        if (StringUtils.isBlank(nodeId)) {
            return null;
        }
        return frontMapper.getByNodeId(nodeId);
    }

    /**
     * remove front
     */
    @Transactional
    public void removeFront(int frontId) {
        //check frontId
        FrontParam param = new FrontParam();
        param.setFrontId(frontId);
        int count = getFrontCount(param);
        if (count == 0) {
            throw new NodeMgrException(ConstantCode.INVALID_FRONT_ID);
        }

        //remove front
        frontMapper.remove(frontId);
        //remove map
        frontGroupMapService.removeByFrontId(frontId);
        //reset group list => remove groups that only belongs to this front
        resetGroupListTask.asyncResetGroupList();
        //clear cache
        frontGroupMapCache.clearMapList();
    }

    public void updateFront(TbFront updateFront) {
        log.info("updateFrontStatus updateFront:{}", updateFront);
        if (updateFront == null) {
            log.error("updateFrontStatus updateFront is null");
            return;
        }
        frontMapper.update(updateFront);
    }

    public void updateFrontWithInternal(Integer frontId, Integer status) {
        log.debug("updateFrontStatus frontId:{}, status:{}", frontId, status);
        TbFront updateFront = getById(frontId);
        if (updateFront == null) {
            log.error("updateFrontStatus updateFront is null");
            return;
        }
        if (updateFront.getStatus().equals(status)) {
            return;
        }
        LocalDateTime modifyTime = updateFront.getModifyTime();
        LocalDateTime createTime = updateFront.getCreateTime();
        Duration duration = Duration.between(modifyTime, LocalDateTime.now());
        Long subTime = duration.toMillis();
        if (subTime < CHECK_FRONT_STATUS_WAIT_MIN_MILLIS && createTime.isBefore(modifyTime)) {
            log.debug("updateFrontWithInternal jump. subTime:{}, minInternal:{}",
                subTime, CHECK_FRONT_STATUS_WAIT_MIN_MILLIS);
            return;
        }
        updateFront.setStatus(status);
        frontMapper.update(updateFront);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public TbFront insert(TbFront tbFront) throws NodeMgrException {
        if (frontMapper.add(tbFront) != 1 || tbFront.getFrontId() <= 0){
            throw new NodeMgrException(ConstantCode.INSERT_FRONT_ERROR);
        }
        return tbFront;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateStatus(int frontId, FrontStatusEnum newStatus) {
        log.info("Update front:[{}] status to:[{}]", frontId, newStatus.toString());
        return this.frontMapper.updateStatus(frontId, newStatus.getId(), LocalDateTime.now()) == 1;
    }

    /**
     * @param chainId
     * @return
     */
    public List<TbFront> selectFrontListByChainId(int chainId) {
        // select all agencies by chainId
        List<TbAgency> tbAgencyList = this.agencyService.selectAgencyListByChainId(chainId);
        log.info("selectFrontListByChainId tbAgencyList:{}", tbAgencyList);

        // select all fronts by all agencies
        List<TbFront> tbFrontList = tbAgencyList.stream()
            .map((agency) -> frontMapper.selectByAgencyId(agency.getId()))
            .filter((front) -> front != null)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tbFrontList)) {
            log.error("Chain:[{}] has no front.", chainId);
            return new ArrayList<>();
        }
        return tbFrontList;
    }

    /**
     * get front list from normal front_group_map
     * @param groupId
     * @return
     */
    public List<TbFront> selectFrontListByGroupId(String groupId) {
        log.info("selectFrontListByGroupId groupId:{}", groupId);
        // select all agencies by chainId
        List<TbFrontGroupMap> frontGroupMapList = this.frontGroupMapMapper.selectListByGroupId(groupId);
        if (CollectionUtils.isEmpty(frontGroupMapList)) {
            log.error("Group:[{}] has no front.", groupId);
            return new ArrayList<>();
        }

        // select all fronts by all agencies
        List<TbFront> tbFrontList = frontGroupMapList.stream()
            .map((map) -> frontMapper.getById(map.getFrontId()))
            .filter((front) -> front != null)
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tbFrontList)) {
            log.error("Group:[{}] has no front.", groupId);
            return new ArrayList<>();
        }
        log.info("selectFrontListByGroupId size:{}", tbFrontList.size());
        return tbFrontList;
    }

    /**
     *
     * @param groupIdSet
     * @return
     */
    public List<TbFront> selectFrontListByGroupIdSet(Set<String> groupIdSet) {
        // select all fronts of all group id
        List<TbFront> allTbFrontList = groupIdSet.stream()
            .map((groupId) -> this.selectFrontListByGroupId(groupId))
            .filter((front) -> front != null)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(allTbFrontList)) {
            log.error("Group id set:[{}] has no front.", JsonTools.toJSONString(groupIdSet));
            return new ArrayList<>();
        }

        // delete replication
        return allTbFrontList.stream().distinct().collect(Collectors.toList());
    }


    /**
     * gen node cert and gen front's yml, and new front ind db
     * @return
     * @throws NodeMgrException
     * @throws IOException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public List<TbFront> initFrontAndNode(List<DeployNodeInfo> nodeInfoList, TbChain chain, TbHost host, int agencyId,
        String agencyName, String groupId, FrontStatusEnum frontStatusEnum)
        throws NodeMgrException, IOException {
        log.info("start initFrontAndNode nodeInfoList:{}, host:{}", nodeInfoList, host);
        ProgressTools.setInitChainData();

        String chainName = chain.getChainName();
        byte encryptType = chain.getEncryptType();
        // the node dir on remote host
        String rootDirOnHost = host.getRootDir();
        String imageTag = chain.getVersion();
        int hostId = host.getId();
        String ip = host.getIp();

        // if host is a new one, currentIndexOnHost will be null
        Integer maxIndexOnHost = this.frontMapper.getNodeMaxIndex(hostId);

        // get start index on host
        int startIndex = maxIndexOnHost == null ? 0 : maxIndexOnHost + 1;

        List<TbFront> newFrontList = new ArrayList<>();
        // call shell to generate new node config(private key and crt)
        for (int i = 0; i < nodeInfoList.size(); i++) {
            DeployNodeInfo nodeInfo = nodeInfoList.get(i);

            int currentIndex = startIndex + i;
            Path nodeRoot = pathService.getNodeRoot(chainName, ip, currentIndex);

            if(Files.exists(nodeRoot)){
                log.warn("initFrontAndNode Exists node:[{}:{}] config, delete first.", ip, nodeRoot.toAbsolutePath().toString());
                try {
                    FileUtils.deleteDirectory(nodeRoot.toFile());
                } catch (IOException e) {
                    throw new NodeMgrException(ConstantCode.DELETE_OLD_NODE_DIR_ERROR);
                }
            }
            log.info("start initFrontAndNode gen node cert");
            // exec gen_node_cert.sh
            ExecuteResult executeResult = this.deployShellService.execGenNode(encryptType, chainName, agencyName,
                nodeRoot.toAbsolutePath().toString());

            if (executeResult.failed()) {
                log.error("initFrontAndNode Generate node:[{}:{}] key and crt error.", ip, currentIndex);
                throw new NodeMgrException(ConstantCode.EXEC_GEN_NODE_ERROR.attach(executeResult.getExecuteOut()));
            }

            String nodeId = PathService.getNodeId(nodeRoot, encryptType);
            int frontPort = nodeInfo.getFrontPort();
            int channelPort = nodeInfo.getChannelPort();
            int p2pPort = nodeInfo.getP2pPort();
            int jsonrpcPort = nodeInfo.getRpcPort();


            TbFront front = TbFront.init(nodeId, ip, frontPort, agencyId, agencyName, imageTag, RunTypeEnum.DOCKER,
                hostId, currentIndex, imageTag, DockerCommandService
                    .getContainerName(rootDirOnHost, chainName, currentIndex),
                jsonrpcPort, p2pPort, channelPort, chain.getId(), chainName, frontStatusEnum);
            // insert front into db
            ((FrontService) AopContext.currentProxy()).insert(front);

            newFrontList.add(front);

            // insert node into db
            String nodeName = NodeService.getNodeName(groupId, nodeId);
            this.nodeService.insert(nodeId, nodeName, groupId, ip, p2pPort, nodeName, DataStatus.STARTING);

            // insert front group into db
            this.frontGroupMapService.newFrontGroup(front.getFrontId(), groupId, GroupStatus.MAINTAINING);

            // generate front application.yml
            ThymeleafUtil.newFrontConfig(nodeRoot, encryptType, channelPort, frontPort, chain.getWebaseSignAddr());
        }
        return newFrontList;
    }

    /**
     *
     * @param nodeId
     * @return
     */
    public List<TbFront> selectRelatedFront(String nodeId) {
        log.info("start selectRelatedFront nodeId:{}", nodeId);
        Set<Integer> frontIdSet = new HashSet<>();
        List<String> groupIdList = this.nodeMapper.selectGroupIdListOfNode(nodeId);
        if (CollectionUtils.isEmpty(groupIdList)) {
            log.error("Node:[{}] has no group", nodeId);
            return new ArrayList<>();
        }
        for (String groupIdOfNode : groupIdList) {
            List<TbFrontGroupMap> tbFrontGroupMaps = this.frontGroupMapMapper.selectListByGroupId(groupIdOfNode);
            if (CollectionUtils.isNotEmpty(tbFrontGroupMaps)){
                tbFrontGroupMaps.forEach(map->{
                    frontIdSet.add(map.getFrontId());
                });
            }
        }

        List<TbFront> nodeRelatedFrontList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(frontIdSet)){
            nodeRelatedFrontList = frontIdSet.stream().map((frontId)-> this.frontMapper.getById(frontId))
                .filter((front) -> front != null)
                .collect(Collectors.toList());
        }
        log.info("selectRelatedFront size:{}", nodeRelatedFrontList.size());
        return nodeRelatedFrontList;
    }

    /**
     * not generate but update existed node config.ini of existed nodes
     * @param chain
     * @param groupId
     * @throws IOException
     */
    public void updateNodeConfigIniByGroupId(TbChain chain, String groupId) throws IOException {
        int chainId = chain.getId();
        log.info("start updateNodeConfigIniByGroupId chainId:{},groupId:{}", chainId, groupId);
        String chainName = chain.getChainName();
        byte encryptType = chain.getEncryptType();

        List<TbNode> tbNodeListOfGroup = this.nodeService.selectNodeListByChainIdAndGroupId(chainId, groupId);
        log.info("updateNodeConfigIniByGroupId tbNodeListOfGroup:{}", tbNodeListOfGroup);

        // all fronts include old and new
        for (TbNode node : CollectionUtils.emptyIfNull(tbNodeListOfGroup)){
            // select related peers to update node config.ini p2p part
            List<TbFront> nodeRelatedFront = this.selectRelatedFront(node.getNodeId());
            log.info("updateNodeConfigIniByGroupId nodeRelatedFront:{}", nodeRelatedFront);

            TbFront tbFront = this.getByNodeId(node.getNodeId());

            boolean guomi = encryptType == CryptoType.SM_TYPE;
            int chainIdInConfigIni = this.constant.getDefaultChainId();

            // local node root
            Path nodeRoot = this.pathService.getNodeRoot(chainName, tbFront.getFrontIp(), tbFront.getHostIndex());

            // generate config.ini
            // 1.5.0 add chain version from v2.7.2 => 2.7.2
            ThymeleafUtil.newNodeConfigIni(nodeRoot, tbFront.getChannelPort(),
                    tbFront.getP2pPort(), tbFront.getJsonrpcPort(), nodeRelatedFront, guomi, chainIdInConfigIni,
                chain.getVersion());

        }
        log.info("end updateNodeConfigIniByGroupId start batchScpNodeConfigIni");

        // scp to remote
        this.scpNodeConfigIni(chain, groupId);

    }

    /**
     * pass new Front list to generate group.ini
     * @param chain
     * @param groupId
     * @param newFrontList when task exec another transaction, this cannot select new front list in db, so pass it
     * @throws IOException
     */
    public void updateConfigIniByGroupIdAndNewFront(TbChain chain, String groupId, final List<TbFront> newFrontList) throws IOException {
        int chainId = chain.getId();
        log.info("start updateNodeConfigIniByGroupId chainId:{},groupId:{}newFrontList:{}", chainId, groupId, newFrontList);
        String chainName = chain.getChainName();
        byte encryptType = chain.getEncryptType();

        // all existed front's nodeid, include removed node's front
        // 游离的front是否需要选进来。
        List<TbNode> dbNodeListOfGroup = this.nodeService.selectNodeListByChainIdAndGroupId(chainId, groupId);
        log.info("updateNodeConfigIniByGroupId dbNodeListOfGroup:{}", dbNodeListOfGroup);

        // all node id included removed node's front
        List<String> allNodeIdList = dbNodeListOfGroup.stream().map(TbNode::getNodeId).collect(Collectors.toList());
        // add new node in db's node list
        List<String> newNodeIdList = newFrontList.stream().map(TbFront::getNodeId).collect(Collectors.toList());
        allNodeIdList.addAll(newNodeIdList);
        log.info("updateNodeConfigIniByGroupId nodeIdList:{}", allNodeIdList);

        // all map's normal front added
        // <nodeId, List<FrontRelated> map
        Map<String, List<TbFront>> nodeIdRelatedFrontMap = new HashMap<>();

        // all fronts include old and new(exclude removed(游离) node)
        // todo support add removed nodes
        for (String nodeId : CollectionUtils.emptyIfNull(allNodeIdList)) {
            // select related peers to update node config.ini p2p part
            // select from existed in db
            List<TbFront> dbRelatedFrontList = this.selectRelatedFront(nodeId);
            log.info("dbRelatedFrontList size:{}", dbRelatedFrontList.size());
            // add just added nodes' new front
            if (dbRelatedFrontList.isEmpty()) {
                // if exist old front, but removed(游离) node's front, not add
                List<TbFront> oldFrontListDb = this.selectFrontListByGroupId(groupId);
                log.debug("oldFrontListDb :{}", oldFrontListDb);
                dbRelatedFrontList.addAll(oldFrontListDb);
            }
            dbRelatedFrontList.addAll(newFrontList);

            // store
            nodeIdRelatedFrontMap.put(nodeId, dbRelatedFrontList);
            // start generate process
            log.info("updateNodeConfigIniByGroupId nodeRelatedFront:{}", dbRelatedFrontList);
            // find first target
            TbFront tbFront = dbRelatedFrontList.stream().filter(f -> f.getNodeId().equals(nodeId)).findFirst().orElse(null);
            if (tbFront == null) {
                log.error("updateNodeConfigIniByGroupId cannot find front of nodeId:{}", nodeId);
                continue;
            }

            boolean guomi = encryptType == CryptoType.SM_TYPE;
            int chainIdInConfigIni = this.constant.getDefaultChainId();

            // local node root
            Path nodeRoot = this.pathService.getNodeRoot(chainName, tbFront.getFrontIp(), tbFront.getHostIndex());

            // generate config.ini
            ThymeleafUtil.newNodeConfigIni(nodeRoot, tbFront.getChannelPort(),
                tbFront.getP2pPort(), tbFront.getJsonrpcPort(), dbRelatedFrontList, guomi, chainIdInConfigIni, chain.getVersion());

        }
        log.info("end updateNodeConfigIniByGroupId start batchScpNodeConfigIni");

        // scp to remote
        // this.scpNodeConfigIni(chain, groupId);
        try {
            this.batchScpNodeConfigIni(chain, groupId, nodeIdRelatedFrontMap);
        } catch (InterruptedException e) {
            log.error("batchScpNodeConfigIni interrupted:[]", e);
            Thread.currentThread().interrupt();
        }
    }
    /**
     *
     * @param chain
     * @param groupIdList
     */
    public void updateNodeConfigIniByGroupList(TbChain chain,
        Set<String> groupIdList) throws IOException {
        // update config.ini of related nodes
        for (String groupId : CollectionUtils.emptyIfNull(groupIdList)) {
            // update node config.ini in group
            this.updateNodeConfigIniByGroupId(chain, groupId);
        }
    }

    /**
     * sync scp node config init
     * not multi thread
     * @param chain
     * @param groupId
     */
    public void scpNodeConfigIni(TbChain chain, String groupId) {
        List<TbNode> tbNodeList = this.nodeService.selectNodeListByChainIdAndGroupId(chain.getId(), groupId);

        for (TbNode tbNode : CollectionUtils.emptyIfNull(tbNodeList)){
            TbFront front = this.getByNodeId(tbNode.getNodeId());
            int hostIndex = front.getHostIndex();

            TbHost host = this.tbHostMapper.selectByPrimaryKey(front.getHostId());

            // path pattern: /NODES_ROOT/chain_name/[ip]/node[index]/config.ini
            // ex: (node-mgr local) ./NODES_ROOT/chain1/127.0.0.1/node0/config.ini
            Path localNodePath = this.pathService.getNodeRoot(chain.getChainName(),front.getFrontIp(),hostIndex);
            String localScr = PathService.getConfigIniPath(localNodePath).toAbsolutePath().toString();

            // ex: (node-mgr local) /opt/fisco/chain1/node0/config.ini
            String remoteDst = String.format("%s/%s/node%s/config.ini", host.getRootDir(), chain.getChainName(),hostIndex);

            // copy group config files to local node's conf dir
            ansibleService.scp(ScpTypeEnum.UP, host.getIp(), localScr, remoteDst);
        }
    }

    /**
     * multi scp node config init
     *  multi thread
     * @param chain
     * @param groupId
     * @param newNodeRelatedFrontMap nodeId include new Front new node
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void batchScpNodeConfigIni(TbChain chain, String groupId, Map<String, List<TbFront>> newNodeRelatedFrontMap) throws InterruptedException {
        log.info("start batchScpNodeConfigIni chainId:{},groupId:{},newNodeRelatedFrontMap:{}",
            chain.getId(), groupId, newNodeRelatedFrontMap);

        final CountDownLatch checkHostLatch = new CountDownLatch(CollectionUtils.size(newNodeRelatedFrontMap));
        // check success count
        AtomicInteger configSuccessCount = new AtomicInteger(0);
        Map<Integer, Future> taskMap = new HashedMap<>();

        for (final String nodeId : newNodeRelatedFrontMap.keySet()) {
            // find first target
            TbFront front = newNodeRelatedFrontMap.get(nodeId).stream().filter(f -> f.getNodeId().equals(nodeId)).findFirst().orElse(null);
            if (front == null) {
                log.error("batchScpNodeConfigIni cannot find front of nodeId:{}", nodeId);
                continue;
            }
            TbHost host = this.tbHostMapper.selectByPrimaryKey(front.getHostId());

            // scp multi
            Future<?> task = threadPoolTaskScheduler.submit(() -> {
                try {
                    // path pattern: /NODES_ROOT/chain_name/[ip]/node[index]/config.ini
                    // ex: (node-mgr local) ./NODES_ROOT/chain1/127.0.0.1/node0/config.ini
                    int hostIndex = front.getHostIndex();
                    Path localNodePath = this.pathService
                        .getNodeRoot(chain.getChainName(), front.getFrontIp(), hostIndex);
                    String localScr = PathService.getConfigIniPath(localNodePath).toAbsolutePath()
                        .toString();

                    // ex: (node-mgr local) /opt/fisco/chain1/node0/config.ini
                    String remoteDst = String
                        .format("%s/%s/node%s/config.ini", host.getRootDir(), chain.getChainName(),
                            hostIndex);

                    // copy group config files to local node's conf dir
                    ansibleService.scp(ScpTypeEnum.UP, host.getIp(), localScr, remoteDst);
                    configSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("batchScpNodeConfigIni:[{}] with unknown error", host.getIp(), e);
                    this.updateStatus(front.getFrontId(), FrontStatusEnum.ADD_FAILED);
                } finally {
                    checkHostLatch.countDown();
                }
            });
            taskMap.put(host.getId(), task);
        }
        // task to scp
        checkHostLatch.await(constant.getExecScpTimeout(), TimeUnit.MILLISECONDS);
        log.info("Verify batchScpNodeConfigIni timeout");
        taskMap.forEach((key, value) -> {
            int hostId = key;
            Future<?> task = value;
            if (!task.isDone()) {
                log.error("batchScpNodeConfigIni host:[{}] timeout, cancel the task.", hostId);
                hostService.updateStatus(hostId, HostStatusEnum.CONFIG_FAIL, "config host timeout.");
                task.cancel(false);
            }
        });

        boolean hostCheckSuccess = configSuccessCount.get() == CollectionUtils.size(newNodeRelatedFrontMap);
        // check if all host init success
        log.log(hostCheckSuccess ? Level.INFO: Level.ERROR,
            "batchScpNodeConfigIni result, total:[{}], success:[{}]",
            CollectionUtils.size(newNodeRelatedFrontMap), configSuccessCount.get());

    }

    /**
     * start front and node
     * @param nodeId
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public boolean restart(String nodeId, OptionType optionType, FrontStatusEnum before,
        FrontStatusEnum success, FrontStatusEnum failed){
        log.info("Restart node:[{}]", nodeId );
        // get front
        TbFront front = this.getByNodeId(nodeId);
        if (front == null){
            throw new NodeMgrException(ConstantCode.NODE_ID_NOT_EXISTS_ERROR);
        }

        TbChain chain = tbChainMapper.selectByPrimaryKey(front.getChainId());
        byte encryptType = chain.getEncryptType();

        // set front status to stopped to avoid error for time task.
        ((FrontService) AopContext.currentProxy()).updateStatus(front.getFrontId(), before);

        this.frontGroupMapService.updateFrontMapStatus(front.getFrontId(), GroupStatus.MAINTAINING);

        TbHost host = this.tbHostMapper.selectByPrimaryKey(front.getHostId());

        log.info("Docker start container front id:[{}:{}].", front.getFrontId(), front.getContainerName());
        try {
            this.dockerOptions.run(
                front.getFrontIp(), front.getImageTag(), front.getContainerName(),
                PathService.getChainRootOnHost(host.getRootDir(), front.getChainName()), front.getHostIndex());

            threadPoolTaskScheduler.schedule(()-> {
                // add check port is on
                // check chain port
                Pair<Boolean, Integer> notInUse = ansibleService.checkPorts(front.getFrontIp(),
                    front.getP2pPort(), front.getFrontPort());
                // not in use is true, then not start success
                if (notInUse.getKey()) {
                    // update front status as start success
                    log.error("Docker start failed!");
                    this.updateStatus(front.getFrontId(), failed);
                } else {
                    log.info("Docker start Front ip{}:{} is in use, start success!",
                        front.getFrontIp(), notInUse.getValue());
                    this.updateStatus(front.getFrontId(), success);

                    // update front version
                    if (StringUtils.isBlank(front.getFrontVersion())
                        || StringUtils.isBlank(front.getSignVersion())) {
                        // update front version
                        try {
                            String frontVersion = frontInterface
                                .getFrontVersionFromSpecificFront(front.getFrontIp(),
                                    front.getFrontPort());
                            String signVersion = frontInterface
                                .getSignVersionFromSpecificFront(front.getFrontIp(),
                                    front.getFrontPort());

                            this.frontMapper
                                .updateVersion(front.getChainId(), frontVersion, signVersion);
                        } catch (Exception e) {
                            log.error("Request front and sign version from front:[{}:{}] error.",
                                front.getFrontIp(), front.getFrontPort(), e);
                        }
                    }

                    if (optionType == OptionType.DEPLOY_CHAIN) {
                        this.frontGroupMapService.updateFrontMapStatus(front.getFrontId(), GroupStatus.NORMAL);
                    } else if (optionType == OptionType.MODIFY_CHAIN) {
                        // check front is in group
                        Path nodePath = this.pathService
                            .getNodeRoot(front.getChainName(), host.getIp(), front.getHostIndex());
                        Set<String> groupIdSet = NodeConfig.getGroupIdSet(nodePath, encryptType);
                        Optional.of(groupIdSet).ifPresent(idSet -> idSet.forEach(groupId -> {
                            List<String> list = frontInterface.getGroupPeers(groupId);
                            if (CollectionUtils.containsAny(list, front.getNodeId())) {
                                this.frontGroupMapService.updateFrontMapStatus(front.getFrontId(), GroupStatus.NORMAL);
                            }
                        }));
                    }
                }
            }, Instant.now().plusMillis(constant.getDockerRestartPeriodTime()));
            // schedule后，等待
            Thread.sleep(constant.getDockerRestartPeriodTime());

            return true;
        } catch (Exception e) {
            log.error("Start front:[{}:{}] failed.", front.getFrontIp(), front.getHostIndex(),e);
            ((FrontService) AopContext.currentProxy()).updateStatus(front.getFrontId(), failed);
            throw new NodeMgrException(ConstantCode.START_NODE_ERROR);
        }
    }

    /**
     *
     * @param chainId
     * @param newImageTag
     * @return
     */
    @Transactional
    public boolean upgrade(int chainId,String newImageTag) {
        boolean updateResult = this.frontMapper.updateUpgradingByChainId(chainId,
            newImageTag, LocalDateTime.now(), FrontStatusEnum.STARTING.getId()) > 0;
        return updateResult;
    }


    /**
     * @param nodeId
     * @return
     */
    @Transactional
    public void stopNode(String nodeId) {
        // get front
        TbFront front = this.getByNodeId(nodeId);
        if (front == null){
            throw new NodeMgrException(ConstantCode.NODE_ID_NOT_EXISTS_ERROR);
        }

        int chainId = front.getChainId();
        int runningTotal = 0;
        List<TbFront> frontList = this.selectFrontListByChainId(chainId);
        for (TbFront tbFront : frontList) {
            if (FrontStatusEnum.isRunning(tbFront.getStatus())) {
                runningTotal ++;
            }
        }
        if (runningTotal < 2) {
            log.error("Two running nodes at least of chain:[{}]", chainId);
            throw new NodeMgrException(ConstantCode.TWO_NODES_AT_LEAST);
        }

        if (!FrontStatusEnum.isRunning(front.getStatus())) {
            log.warn("Node:[{}:{}] is already stopped.",front.getFrontIp(),front.getHostIndex());
            return ;
        }

        // select node list
        List<TbNode> nodeList = this.nodeMapper.selectByNodeId(nodeId);

        // node is removed and doesn't belong to any group.
        // if observer to removed, this observer would still return groupId(as a observer)
        boolean nodeRemovable = CollectionUtils.isEmpty(nodeList);

        if (!nodeRemovable) {
            // node belongs to some groups, check if it is the last one of each group.
            Set<String> groupIdSet = nodeList.stream().map(TbNode::getGroupId)
                .collect(Collectors.toSet());

            for (String groupId : groupIdSet) {
                int nodeCountOfGroup = CollectionUtils.size(this.nodeMapper.selectByGroupId(groupId));
                if (nodeCountOfGroup != 1){ // group has another node.
                    throw new NodeMgrException(ConstantCode.NODE_NEED_REMOVE_FROM_GROUP_ERROR.attach(groupId));
                }
            }
        }

        log.info("Docker stop and remove container front id:[{}:{}].", front.getFrontId(), front.getContainerName());
        this.dockerOptions.stop( front.getFrontIp(), front.getContainerName());
        try {
            Thread.sleep(constant.getDockerRestartPeriodTime());
        } catch (InterruptedException e) {
            log.warn("Docker stop and remove container sleep Interrupted");
            Thread.currentThread().interrupt();
        }

        // update map
        this.frontGroupMapService.updateFrontMapStatus(front.getFrontId(), GroupStatus.MAINTAINING);
        // update front
        ((FrontService) AopContext.currentProxy()).updateStatus(front.getFrontId(), FrontStatusEnum.STOPPED);
    }

    @Transactional
    public void stopNodeForce(String nodeId) {
        log.info("start stopNodeForce nodeId:{}", nodeId);
        // get front
        TbFront front = this.getByNodeId(nodeId);
        if (front == null){
            throw new NodeMgrException(ConstantCode.NODE_ID_NOT_EXISTS_ERROR);
        }

        int chainId = front.getChainId();
        int runningTotal = 0;
        List<TbFront> frontList = this.selectFrontListByChainId(chainId);
        for (TbFront tbFront : frontList) {
            if (FrontStatusEnum.isRunning(tbFront.getStatus())) {
                runningTotal ++;
            }
        }
        if (runningTotal < 2) {
            log.error("Two running nodes at least of chain:[{}]", chainId);
            throw new NodeMgrException(ConstantCode.TWO_NODES_AT_LEAST);
        }

        if (!FrontStatusEnum.isRunning(front.getStatus())) {
            log.warn("Node:[{}:{}] is already stopped.",front.getFrontIp(),front.getHostIndex());
            return ;
        }

        log.info("Docker stop and remove container front id:[{}:{}].", front.getFrontId(), front.getContainerName());
        this.dockerOptions.stop( front.getFrontIp(), front.getContainerName());
        try {
            Thread.sleep(constant.getDockerRestartPeriodTime());
        } catch (InterruptedException e) {
            log.warn("Docker stop and remove container sleep Interrupted");
            Thread.currentThread().interrupt();
        }

        // update map
        this.frontGroupMapService.updateFrontMapStatus(front.getFrontId(), GroupStatus.MAINTAINING);
        // update front
        ((FrontService) AopContext.currentProxy()).updateStatus(front.getFrontId(), FrontStatusEnum.STOPPED);
        log.info("end stopNodeForce. ");

    }

    @Transactional
    public void deleteNode(TbHost host, String nodeId){
        // get front
        TbFront front = this.getByNodeId(nodeId);
        if (front == null){
            throw new NodeMgrException(ConstantCode.NODE_ID_NOT_EXISTS_ERROR);
        }
        TbHost hostInDb = host != null ? host : this.tbHostMapper.selectByPrimaryKey(front.getHostId());
        log.info("Docker stop and remove container front id:[{}:{}].", front.getFrontId(), front.getContainerName());
        this.dockerOptions.stop( front.getFrontIp(), front.getContainerName());

        this.nodeMapper.deleteByNodeId(nodeId);
    }


    /**
     * delete front
     * @param agencyId
     */
    @Transactional
    public void deleteFrontByAgencyId(int agencyId){
        log.info("Delete front data by agency id:[{}].", agencyId);

        // select host, front, group in agency
        List<TbFront> frontList = frontMapper.selectByAgencyId(agencyId);
        if (CollectionUtils.isEmpty(frontList)) {
            log.warn("No front in agency:[{}]", agencyId);
            return ;
        }

        for (TbFront front : frontList) {
            TbHost host = this.tbHostMapper.selectByPrimaryKey(front.getHostId());
            log.info("rm host container by host ip:{}", host.getIp());
            // remote docker container
            this.dockerOptions.stop(host.getIp(), front.getContainerName());

            // delete in deleteAllGroupData
//            log.info("Delete node data by node id:[{}].", front.getNodeId());
//            this.nodeMapper.deleteByNodeId(front.getNodeId());

//            log.info("Delete front group map data by front id:[{}].", front.getFrontId());
//            this.frontGroupMapMapper.removeByFrontId(front.getFrontId());
        }

        // delete front in batch
        this.frontMapper.deleteByAgencyId(agencyId);
        log.info("end deleteFrontByAgencyId");
    }

    /**
     *
     * @param chainId
     */
    public int frontProgress(int chainId){
        // check host init
        int frontFinishCount = 0;
        List<TbFront> frontList = this.selectFrontListByChainId(chainId);
        if (CollectionUtils.isEmpty(frontList)) {
            return NumberUtil.PERCENTAGE_FINISH;
        }
        for (TbFront front : frontList) {
            if(FrontStatusEnum.isRunning(front.getStatus())){
                frontFinishCount ++;
            }
        }
        // check front init finish ?
        if (frontFinishCount == frontList.size()){
            // init success
            return NumberUtil.PERCENTAGE_FINISH;
        }
        return NumberUtil.percentage(frontFinishCount,frontList.size());
    }

    /**
     * refresh front's status only for front status not front_group_map
     * check if chain already running
     */
    @Transactional
    public void refreshFrontStatus() {
        // get all front
        List<TbFront> frontList = frontMapper.getAllList();
        if (frontList == null || frontList.size() == 0) {
            log.info("refreshFrontStatus jump over, front not found.");
            return;
        }
        if (!chainService.runTask()) {
            log.info("refreshFrontStatus jump over, chain not running yet.");
            return;
        }
        for (TbFront tbFront : frontList) {
            String frontIp = tbFront.getFrontIp();
            Integer frontPort = tbFront.getFrontPort();
            int frontId = tbFront.getFrontId();
            // get front server version and sign server version
            try {
                frontInterface.getFrontVersionFromSpecificFront(frontIp, frontPort);
                log.info("get version of Front success, update front as started");
            } catch (Exception e) {
                // catch old version front and sign that not have '/version' api
                log.warn("get version of Front failed, update front as stopped");
                this.updateStatus(frontId, FrontStatusEnum.STOPPED);
                return;
            }
            // not update front_group_map
            this.updateStatus(frontId, FrontStatusEnum.RUNNING);
        }
    }

    public List<TbFront> selectByFrontIdList(List<Integer> frontIdList){
        log.info("selectByFrontIdList frontIdList:{}", frontIdList);
        List<TbFront> frontList = new ArrayList<>();
        frontIdList.forEach(id -> {
            TbFront front = frontMapper.getById(id);
            if (front == null) {
                throw new NodeMgrException(ConstantCode.INVALID_FRONT_ID);
            }
            frontList.add(front);
        });
        return frontList;
    }

    /**
     * require if webase >= 1.3.1(dynamic group), fisco >= 2.4.1
     * ignore: require if webase <= 1.3.2, fisco < 2.5.0
     * @param supportVersion
     */
    private void validateSupportVersion(String supportVersion) {
        int nodeSupportVerInt = NodeMgrTools.getVersionFromStr(supportVersion);
        String webaseVersion = versionProperties.getVersion();
        int webaseVerInt = NodeMgrTools.getVersionFromStr(webaseVersion);
        // webase v1.3.2 above and fisco v2.4.1 below, error for dynamic group manage
        if ( webaseVerInt >= VersionProperties.WEBASE_LOWEST_VERSION_INT
            && nodeSupportVerInt < VersionProperties.NODE_LOWEST_SUPPORT_VERSION_INT ) {
            throw new NodeMgrException(ConstantCode.WEBASE_VERSION_NOT_MATCH_FISCO_SUPPORT_VERSION);
        }
    }

//    public FrontNodeConfig getFrontNodeConfig(int frontId) {
//        TbFront front = this.getById(frontId);
//        if (front == null) {
//            log.error("");
//            throw new NodeMgrException(ConstantCode.INVALID_FRONT_ID);
//        }
//        String frontIp = front.getFrontIp();
//        int frontPort = front.getFrontPort();
//        FrontNodeConfig nodeConfig = frontInterface.getNodeConfigFromSpecificFront(frontIp, frontPort);
//        return nodeConfig;
//    }

    public GroupInfo getGroupInfo(int frontId, String groupId) {
        TbFront front = this.getById(frontId);
        if (front == null) {
            log.error("");
            throw new NodeMgrException(ConstantCode.INVALID_FRONT_ID);
        }
        String frontIp = front.getFrontIp();
        int frontPort = front.getFrontPort();
        GroupInfo groupInfo = frontInterface.getGroupInfoFromSpecificFront(frontIp, frontPort, groupId);
        return groupInfo;
    }

    /**
     * refresh front, group, frontGroupMap, nodeList
     */
    @Transactional
    public void refreshFront() {
        //get all front
        List<TbFront> frontList = frontMapper.getAllList();
        if (frontList == null || frontList.size() == 0) {
            log.info("refreshFront. not find any front.");
            return;
        }
        for (TbFront tbFront : frontList) {
            try {
                String frontIp = tbFront.getFrontIp();
                Integer frontPort = tbFront.getFrontPort();
                // query group list from chain
                List<String> groupIdList = frontInterface.getGroupListFromSpecificFront(frontIp, frontPort);
                // get syncStatus
                SyncStatusInfo syncStatus = frontInterface.getSyncStatusFromSpecificFront(frontIp,
                    frontPort, groupIdList.get(0));
                // get front server version and sign server version
                try {
                    String frontVersion = frontInterface.getFrontVersionFromSpecificFront(frontIp, frontPort);
                    String signVersion = frontInterface.getSignVersionFromSpecificFront(frontIp, frontPort);
                    tbFront.setFrontVersion(frontVersion);
                    tbFront.setSignVersion(signVersion);
                } catch (Exception e) {
                    // catch old version front and sign that not have '/version' api
                    log.warn("get version of Front and Sign failed (required front and sign v1.4.0+).");
                }
                // copy attribute
                tbFront.setNodeId(syncStatus.getNodeId());
                //update front info
                frontMapper.updateBasicInfo(tbFront);
                // save group info
                saveGroup(groupIdList, tbFront);
            } catch (Exception ex) {
                log.error("refreshFront fail. frontId:{}", tbFront.getFrontId(), ex);
                continue;
            }
        }
        // clear cache
        frontGroupMapCache.clearMapList();
    }

}
