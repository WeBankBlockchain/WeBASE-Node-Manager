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
package com.webank.webase.node.mgr.lite.front;

import com.webank.webase.node.mgr.lite.base.code.ConstantCode;
import com.webank.webase.node.mgr.lite.base.enums.DataStatus;
import com.webank.webase.node.mgr.lite.base.enums.FrontStatusEnum;
import com.webank.webase.node.mgr.lite.base.enums.GroupStatus;
import com.webank.webase.node.mgr.lite.base.enums.GroupType;
import com.webank.webase.node.mgr.lite.base.enums.RunTypeEnum;
import com.webank.webase.node.mgr.lite.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.lite.base.tools.CertTools;
import com.webank.webase.node.mgr.lite.base.tools.JsonTools;
import com.webank.webase.node.mgr.lite.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.lite.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.lite.config.properties.VersionProperties;
import com.webank.webase.node.mgr.lite.front.entity.FrontInfo;
import com.webank.webase.node.mgr.lite.front.entity.FrontNodeConfig;
import com.webank.webase.node.mgr.lite.front.entity.FrontParam;
import com.webank.webase.node.mgr.lite.front.entity.TbFront;
import com.webank.webase.node.mgr.lite.front.frontgroupmap.FrontGroupMapCache;
import com.webank.webase.node.mgr.lite.front.frontgroupmap.FrontGroupMapMapper;
import com.webank.webase.node.mgr.lite.front.frontgroupmap.FrontGroupMapService;
import com.webank.webase.node.mgr.lite.front.frontgroupmap.entity.TbFrontGroupMap;
import com.webank.webase.node.mgr.lite.front.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.lite.group.GroupService;
import com.webank.webase.node.mgr.lite.group.entity.TbGroup;
import com.webank.webase.node.mgr.lite.node.NodeMapper;
import com.webank.webase.node.mgr.lite.node.NodeService;
import com.webank.webase.node.mgr.lite.node.entity.NodeParam;
import com.webank.webase.node.mgr.lite.node.entity.PeerInfo;
import com.webank.webase.node.mgr.lite.scheduler.ResetGroupListTask;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.client.protocol.response.NodeInfo.NodeInformation;
import org.fisco.bcos.sdk.client.protocol.response.SyncStatus.SyncStatusInfo;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.NodeVersion.ClientVersion;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
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
    private NodeMapper nodeMapper;
    @Autowired
    private FrontGroupMapMapper frontGroupMapMapper;
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
    private ConstantProperties constant;
    @Qualifier(value = "deployAsyncScheduler")
    @Autowired private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Autowired private CryptoSuite cryptoSuite;
    // version to check
    @Autowired
    private VersionProperties versionProperties;
    // interval of check front status
    private static final Long CHECK_FRONT_STATUS_WAIT_MIN_MILLIS = 3000L;

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
                List<String> groupIdList;
                groupIdList = frontInterface.getGroupListFromSpecificFront(frontIp, frontPort);
                // get syncStatus
                SyncStatusInfo syncStatus = frontInterface.getSyncStatusFromSpecificFront(frontIp,
                        frontPort, Integer.valueOf(groupIdList.get(0)));
                // get version info
                ClientVersion versionResponse = frontInterface.getClientVersionFromSpecificFront(frontIp,
                        frontPort, Integer.valueOf(groupIdList.get(0)));
                String clientVersion = versionResponse.getVersion();
                String supportVersion = versionResponse.getSupportedVersion();
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
                // get node config(add in 1.5.0)
                // p2p/rpc/channel port etc.
                FrontNodeConfig nodeConfig = frontInterface.getNodeConfigFromSpecificFront(frontIp, frontPort);
                // get agency of node
                NodeInformation nodeInfo = frontInterface.getNodeInfoFromSpecificFront(frontIp, frontPort);
                tbFront.setP2pPort(nodeConfig.getP2pport());
                tbFront.setJsonrpcPort(nodeConfig.getRpcport());
                tbFront.setChannelPort(nodeConfig.getChannelPort());
                // copy attribute
                tbFront.setNodeId(syncStatus.getNodeId());
                tbFront.setClientVersion(clientVersion);
                tbFront.setSupportVersion(supportVersion);
                // set agency from chain
                tbFront.setAgency(nodeInfo.getAgency() == null ? "fisco" : nodeInfo.getAgency());
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
    
    /**
     * add new front, save front, frontGroupMap, check front's groupStatus, refresh nodeList
     */
    @Transactional
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
        // check front's encrypt type same as nodemgr(guomi or standard)
        int encryptType = frontInterface.getEncryptTypeFromSpecificFront(frontIp, frontPort);
        if (encryptType != cryptoSuite.cryptoTypeConfig) {
            log.error("fail newFront, frontIp:{},frontPort:{},front's encryptType:{}," +
                            "local encryptType not match:{}",
                    frontIp, frontPort, encryptType, cryptoSuite.cryptoTypeConfig);
            throw new NodeMgrException(ConstantCode.ENCRYPT_TYPE_NOT_MATCH);
        }
        //check front not exist
        SyncStatusInfo syncStatus = frontInterface.getSyncStatusFromSpecificFront(frontIp,
                frontPort, Integer.valueOf(groupIdList.get(0)));
        FrontParam param = new FrontParam();
        param.setNodeId(syncStatus.getNodeId());
        int count = getFrontCount(param);
        if (count > 0) {
            throw new NodeMgrException(ConstantCode.FRONT_EXISTS);
        }
        ClientVersion versionResponse = frontInterface.getClientVersionFromSpecificFront(frontIp,
                frontPort, Integer.valueOf(groupIdList.get(0)));
        String clientVersion = versionResponse.getVersion();
        String supportVersion = versionResponse.getSupportedVersion();
        // copy attribute
        BeanUtils.copyProperties(frontInfo, tbFront);
        tbFront.setNodeId(syncStatus.getNodeId());
        tbFront.setClientVersion(clientVersion);
        tbFront.setSupportVersion(supportVersion);

        // 1.5.0 add check client version cannot be lower than v2.4.0
        this.validateSupportVersion(supportVersion);
        // get node config(add in 1.5.0)
        FrontNodeConfig nodeConfig = frontInterface.getNodeConfigFromSpecificFront(frontIp, frontPort);
        tbFront.setP2pPort(nodeConfig.getP2pport());
        tbFront.setJsonrpcPort(nodeConfig.getRpcport());
        tbFront.setChannelPort(nodeConfig.getChannelPort());
        // get agency of node
        NodeInformation nodeInfo = frontInterface.getNodeInfoFromSpecificFront(frontIp, frontPort);
        log.info("front's agency is :{}", nodeInfo);
        tbFront.setAgency(nodeInfo.getAgency());
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
        // save group info
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
    @Transactional
    public void saveGroup(List<String> groupIdList, TbFront tbFront){
        String frontIp = tbFront.getFrontIp();
        Integer frontPort = tbFront.getFrontPort();
        for (String groupId : groupIdList) {
            Integer group = Integer.valueOf(groupId);
            //peer in group
            List<String> groupPeerList = frontInterface
                    .getGroupPeersFromSpecificFront(frontIp, frontPort, group);
            //get peers on chain
            PeerInfo[] peerArr = frontInterface
                    .getPeersFromSpecificFront(frontIp, frontPort, group);
            List<PeerInfo> peerList = Arrays.asList(peerArr);
            //add group
            // check group not existed or node count differs
            TbGroup checkGroup = groupService.getGroupById(group);
            if (Objects.isNull(checkGroup) || groupPeerList.size() != checkGroup.getNodeCount()) {
                groupService.saveGroup(group, groupPeerList.size(), "synchronous",
                        GroupType.SYNC, GroupStatus.NORMAL,0,"");
            }
            //save front group map
            frontGroupMapService.newFrontGroup(tbFront, group);
            //save nodes
            for (String nodeId : groupPeerList) {
                PeerInfo newPeer = peerList.stream()
                    .map(p -> NodeMgrTools.object2JavaBean(p, PeerInfo.class))
                    .filter(Objects::nonNull)
                    .filter(peer -> nodeId.equals(peer.getNodeId()))
                    .findFirst().orElseGet(() -> new PeerInfo(nodeId));
                nodeService.addNodeInfo(group, newPeer);
            }
            // add sealer(consensus node) and observer in nodeList
            refreshSealerAndObserverInNodeList(frontIp, frontPort, group);
        }
    }

    /**
     * add sealer(consensus node) and observer in nodeList
     * @param groupId
     */
    public void refreshSealerAndObserverInNodeList(String frontIp, int frontPort, int groupId) {
        log.debug("start refreshSealerAndObserverInNodeList frontIp:{}, frontPort:{}, groupId:{}",
                frontIp, frontPort, groupId);
        List<String> sealerList = frontInterface.getSealerListFromSpecificFront(frontIp, frontPort, groupId);
        List<String> observerList = frontInterface.getObserverListFromSpecificFront(frontIp, frontPort, groupId);
        List<PeerInfo> sealerAndObserverList = new ArrayList<>();
        sealerList.forEach(nodeId -> sealerAndObserverList.add(new PeerInfo(nodeId)));
        observerList.forEach(nodeId -> sealerAndObserverList.add(new PeerInfo(nodeId)));
        log.debug("refreshSealerAndObserverInNodeList sealerList:{},observerList:{}",
                sealerList, observerList);
        sealerAndObserverList.forEach(peerInfo -> {
            NodeParam checkParam = new NodeParam();
            checkParam.setGroupId(groupId);
            checkParam.setNodeId(peerInfo.getNodeId());
            int existedNodeCount = nodeService.countOfNode(checkParam);
            log.debug("addSealerAndObserver peerInfo:{},existedNodeCount:{}",
                    peerInfo, existedNodeCount);
            if(existedNodeCount == 0) {
                nodeService.addNodeInfo(groupId, peerInfo);
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
        SyncStatusInfo syncStatus = frontInterface.getSyncStatusFromSpecificFront(frontIp, frontPort, 1);
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
        if (updateFront.getStatus() != null &&  updateFront.getStatus().equals(status)) {
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
     * get front list from normal front_group_map
     * @param groupId
     * @return
     */
    public List<TbFront> selectFrontListByGroupId(int groupId) {
        log.info("selectFrontListByGroupId groupId:{}", groupId);
        // select all agencies by chainId
        List<TbFrontGroupMap> frontGroupMapList = this.frontGroupMapMapper.selectListByGroupId(groupId);
        if (CollectionUtils.isEmpty(frontGroupMapList)) {
            return Collections.emptyList();
        }

        // select all fronts by all agencies
        List<TbFront> tbFrontList = frontGroupMapList.stream()
                .map((map) -> frontMapper.getById(map.getFrontId()))
                .filter((front) -> front != null)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tbFrontList)) {
            log.error("Group:[{}] has no front.", groupId);
            return Collections.emptyList();
        }
        return tbFrontList;
    }

    /**
     *
     * @param groupIdSet
     * @return
     */
    public List<TbFront> selectFrontListByGroupIdSet(Set<Integer> groupIdSet) {
        // select all fronts of all group id
        List<TbFront> allTbFrontList = groupIdSet.stream()
                .map((groupId) -> this.selectFrontListByGroupId(groupId))
                .filter((front) -> front != null)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(allTbFrontList)) {
            log.error("Group id set:[{}] has no front.", JsonTools.toJSONString(groupIdSet));
            return Collections.emptyList();
        }

        // delete replication
        return allTbFrontList.stream().distinct().collect(Collectors.toList());
    }


    /**
     *
     * @param nodeId
     * @return
     */
    public List<TbFront> selectRelatedFront(String nodeId){
        Set<Integer> frontIdSet = new HashSet<>();
        List<Integer> groupIdList = this.nodeMapper.selectGroupIdListOfNode(nodeId);
        if (CollectionUtils.isEmpty(groupIdList)){
            log.error("Node:[{}] has no group", nodeId);
            Collections.emptyList();
        }
        for (Integer groupIdOfNode : groupIdList) {
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
        return nodeRelatedFrontList;
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

    public FrontNodeConfig getFrontNodeConfig(int frontId) {
        TbFront front = this.getById(frontId);
        if (front == null) {
            log.error("");
            throw new NodeMgrException(ConstantCode.INVALID_FRONT_ID);
        }
        String frontIp = front.getFrontIp();
        int frontPort = front.getFrontPort();
        FrontNodeConfig nodeConfig = frontInterface.getNodeConfigFromSpecificFront(frontIp, frontPort);
        return nodeConfig;
    }
}
