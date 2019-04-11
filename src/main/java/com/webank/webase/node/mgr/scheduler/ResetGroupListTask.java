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
package com.webank.webase.node.mgr.scheduler;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.front.FrontMapper;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.FrontParam;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.entity.PeerOfConsensusStatus;
import com.webank.webase.node.mgr.frontinterface.entity.PeerOfSyncStatus;
import com.webank.webase.node.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.group.TbGroup;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.TbNode;
import com.webank.webase.node.mgr.node.entity.PeerInfo;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class ResetGroupListTask {

    @Autowired
    private FrontService frontService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private FrontMapper frontMapper;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private FrontGroupMapService frontGroupMapService;
    @Autowired
    private FrontInterfaceService frontInterfacee;

    public void resetGroupList() {
        //get all local groups
        List<TbGroup> localGroupList = groupService.getAllGroup();
        //get all front
        List<TbFront> frontList = frontService.getFrontList(new FrontParam());
        if (frontList == null || frontList.size() == 0) {
            log.info("resetGroupList jump over. front list is null");
            return;
        }
        //get group from chain
        for (TbFront front : frontList) {
            //query group list
            List<String> groupIdList;
            try {
                groupIdList = frontInterfacee
                    .getGroupListFromSpecificFront(front.getFrontIp(), front.getFrontPort());
            } catch (Exception ex) {
                log.error("fail getGroupListFromSpecificFront.", ex);
                continue;
            }
            for (String groupId : groupIdList) {
                Integer gId = Integer.valueOf(groupId);
                //peer in group
                List<PeerInfo> peerList = frontInterfacee.getPeers(gId);
                //save new groupId
                localGroupList.stream().filter(localGroup -> gId != localGroup.getGroupId())
                    .forEach(newGroup -> {
                        groupService.saveGroupId(newGroup.getGroupId(), peerList.size());
                        frontGroupMapService.newFrontGroup(front.getFrontId(), gId);
                    });

                //save new peers
                savePeerList(gId, peerList);
                //check node status
                checkNodeStatus(gId);
            }
        }
    }

    /**
     * save new peers.
     */
    private void savePeerList(int groupId, List<PeerInfo> peerList) {
        //get all local nodes
        List<TbNode> localNodeList = nodeService.queryByGroupId(groupId);
        //save new nodes
        for (Object obj : peerList) {
            PeerInfo peerInfo = NodeMgrTools.object2JavaBean(obj, PeerInfo.class);
            long count = localNodeList.stream().filter(
                ln -> groupId == ln.getGroupId() && peerInfo.getNodeId().equals(ln.getNodeId()))
                .count();
            if (count == 0) {
                nodeService.addNodeInfo(groupId, peerInfo);
            }
        }
    }

    /**
     * check node status
     */
    private void checkNodeStatus(int groupId) {
        //get local node list
        List<TbNode> nodeList = nodeService.queryByGroupId(groupId);
        //get PeerOfSyncStatus
        List<PeerOfSyncStatus> syncList = getPeerOfSyncStatus(groupId);
        //getPeerOfConsensusStatus
        List<PeerOfConsensusStatus> consensusList = getPeerOfConsensusStatus(groupId);
        for (TbNode tbNode : nodeList) {
            String nodeId = tbNode.getNodeId();
            BigInteger localBlockNumber = tbNode.getBlockNumber();
            BigInteger localPbftView = tbNode.getPbftView();

            BigInteger latestNumber = syncList.stream().filter(sl -> nodeId.equals(sl.getNodeId()))
                .map(s -> s.getBlockNumber()).findFirst().orElse(BigInteger.ZERO);//blockNumber
            BigInteger latestView = consensusList.stream()
                .filter(cl -> nodeId.equals(cl.getNodeId())).map(c -> c.getView()).findFirst()
                .orElse(BigInteger.ZERO);//pbftView

            if (localBlockNumber.equals(latestNumber) && localPbftView.equals(latestView)) {
                log.warn("node[{}] is invalid", nodeId);
                tbNode.setNodeActive(DataStatus.INVALID.getValue());
            } else {
                tbNode.setBlockNumber(latestNumber);
                tbNode.setPbftView(latestView);
                tbNode.setNodeActive(DataStatus.NORMAL.getValue());
            }

            //update node
            nodeService.updateNode(tbNode);
        }

    }

    /**
     * get peer Of SyncStatus
     */
    public List<PeerOfSyncStatus> getPeerOfSyncStatus(int groupId) {
        SyncStatus syncStatus = frontInterfacee.getSyncStatus(groupId);
        return syncStatus.getPeers();
    }

    /**
     * get peer of consensusStatus
     */
    private List<PeerOfConsensusStatus> getPeerOfConsensusStatus(int groupId) {
        String consensusStatusJson = frontInterfacee.getConsensusStatus(groupId);
        JSONArray jsonArr = JSONArray.parseArray(consensusStatusJson);
        List<Object> dataIsList = jsonArr.stream().filter(jsonObj -> jsonObj instanceof List)
            .map(arr -> {
                Object obj = JSONArray.parseArray(JSON.toJSONString(arr)).get(0);
                try {
                    return NodeMgrTools.object2JavaBean(obj, PeerOfConsensusStatus.class);
                } catch (Exception e) {
                    return null;
                }
            }).collect(Collectors.toList());

        return JSONArray.parseArray(JSON.toJSONString(dataIsList), PeerOfConsensusStatus.class);
    }
}