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


import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.FrontParam;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.TbNode;
import com.webank.webase.node.mgr.node.entity.PeerInfo;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class ResetGroupListTask {

    @Autowired
    private FrontService frontService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private FrontGroupMapService frontGroupMapService;
    @Autowired
    private FrontInterfaceService frontInterfacee;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;

    @Async(value = "mgrAsyncExecutor")
    public void asyncResetGroupList() {
        resetGroupList();
    }


    public void resetGroupList() {
        Instant startTime = Instant.now();
        log.info("start resetGroupList. startTime:{}", startTime.toEpochMilli());

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
                List<String> groupPeerList = frontInterfacee.getGroupPeers(gId);
                //save groupId
                groupService.saveGroupId(gId, groupPeerList.size());
                frontGroupMapService.newFrontGroup(front.getFrontId(), gId);
                //save new peers
                savePeerList(gId, groupPeerList);
                //check node status
                nodeService.checkNodeStatus(gId);
            }
        }

        //reset frontGroupMapList cache.
        frontGroupMapCache.resetMapList();

        log.info("end resetGroupList. useTime:{} ",
            Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * save new peers.
     */
    private void savePeerList(int groupId, List<String> groupPeerList) {
        //get all local nodes
        List<TbNode> localNodeList = nodeService.queryByGroupId(groupId);
        //get peers on chain
        PeerInfo[] peerArr = frontInterfacee.getPeers(groupId);
        List<PeerInfo> peerList = Arrays.asList(peerArr);
        //save new nodes
        for (String nodeId : groupPeerList) {
            long count = localNodeList.stream().filter(
                ln -> groupId == ln.getGroupId() && nodeId.equals(ln.getNodeId())).count();
            if (count == 0) {
                PeerInfo newPeer = peerList.stream().filter(peer -> nodeId.equals(peer.getNodeId()))
                    .findFirst().orElseGet(() -> new PeerInfo(nodeId));
                nodeService.addNodeInfo(groupId, newPeer);
            }
        }
    }

}