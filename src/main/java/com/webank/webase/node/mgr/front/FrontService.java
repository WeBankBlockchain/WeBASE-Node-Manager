/**
 * Copyright 2014-2019  the original author or authors.
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

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.CertTools;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.front.entity.FrontInfo;
import com.webank.webase.node.mgr.front.entity.FrontParam;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.node.NodeParam;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.TbNode;
import com.webank.webase.node.mgr.node.entity.PeerInfo;
import com.webank.webase.node.mgr.scheduler.ResetGroupListTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * service of web3.
 */
@Log4j2
@Service
public class FrontService {

    @Autowired
    private GroupService groupService;
    @Autowired
    private FrontMapper frontMapper;
    @Autowired
    private NodeService nodeService;
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

    /**
     * add new front
     */
    @Transactional
    public TbFront newFront(FrontInfo frontInfo) {
        log.debug("start newFront frontInfo:{}", frontInfo);
        TbFront tbFront = new TbFront();
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
        if (encryptType != EncryptType.encryptType) {
            log.error("fail newFront, frontIp:{},frontPort:{},front's encryptType:{}," +
                            "local encryptType not match:{}",
                    frontIp, frontPort, encryptType, EncryptType.encryptType);
            throw new NodeMgrException(ConstantCode.ENCRYPT_TYPE_NOT_MATCH);
        }
        //check front not exist
        SyncStatus syncStatus = frontInterface.getSyncStatusFromSpecificFront(frontIp, 
                frontPort, Integer.valueOf(groupIdList.get(0)));
        FrontParam param = new FrontParam();
        param.setNodeId(syncStatus.getNodeId());
        int count = getFrontCount(param);
        if (count > 0) {
            throw new NodeMgrException(ConstantCode.FRONT_EXISTS);
        }
        String clientVersion = frontInterface.getClientVersion(frontIp,
                frontPort, Integer.valueOf(groupIdList.get(0)));
        //copy attribute
        BeanUtils.copyProperties(frontInfo, tbFront);
        tbFront.setNodeId(syncStatus.getNodeId());
        tbFront.setClientVersion(clientVersion);
        //save front info
        frontMapper.add(tbFront);
        if (tbFront.getFrontId() == null || tbFront.getFrontId() == 0) {
            log.warn("fail newFront, after save, tbFront:{}", JSON.toJSONString(tbFront));
            throw new NodeMgrException(ConstantCode.SAVE_FRONT_FAIL);
        }
        for (String groupId : groupIdList) {
            Integer group = Integer.valueOf(groupId);
            //peer in group
            List<String> groupPeerList = frontInterface
                .getNodeIDListFromSpecificFront(frontIp, frontPort, group);
            //get peers on chain
            PeerInfo[] peerArr = frontInterface
                .getPeersFromSpecificFront(frontIp, frontPort, group);
            List<PeerInfo> peerList = Arrays.asList(peerArr);
            //add groupId
            groupService.saveGroupId(group, groupPeerList.size());
            //save front group map
            frontGroupMapService.newFrontGroup(tbFront.getFrontId(), group);
            //save nodes
            for (String nodeId : groupPeerList) {
                PeerInfo newPeer = peerList.stream().map(p -> NodeMgrTools
                    .object2JavaBean(p, PeerInfo.class))
                    .filter(peer -> nodeId.equals(peer.getNodeId()))
                    .findFirst().orElseGet(() -> new PeerInfo(nodeId));
                nodeService.addNodeInfo(group, newPeer);
            }
            //add sealer(consensus node) and observer in nodeList
             refreshSealerAndObserverInNodeList(frontIp, frontPort, group);
        }
        // pull cert from new front and its node
        CertTools.isPullFrontCertsDone = false;
        //clear cache
        frontGroupMapCache.clearMapList();
        return tbFront;
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
        sealerList.stream().forEach(nodeId -> sealerAndObserverList.add(new PeerInfo(nodeId)));
        observerList.stream().forEach(nodeId -> sealerAndObserverList.add(new PeerInfo(nodeId)));
        log.debug("refreshSealerAndObserverInNodeList sealerList:{},observerList:{}",
                sealerList, observerList);
        sealerAndObserverList.stream()
                .forEach(peerInfo -> {
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
    /**
     * check not support ip.
     */
    private void checkNotSupportIp(String ip) {

        String ipConfig = constants.getNotSupportFrontIp();
        if(StringUtils.isBlank(ipConfig))return;
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
        SyncStatus syncStatus = frontInterface.getSyncStatusFromSpecificFront(frontIp, frontPort, 1);
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
     * remove front
     */
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
        //reset group list
        resetGroupListTask.asyncResetGroupList();
        //clear cache
        frontGroupMapCache.clearMapList();
    }

    public void setFrontEncryptType(List<TbFront> list) {

        list.stream().forEach(front -> {

        });
    }
}
