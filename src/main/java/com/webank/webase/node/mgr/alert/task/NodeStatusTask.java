/**
 * Copyright 2014-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.node.mgr.alert.task;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.alert.mail.MailService;
import com.webank.webase.node.mgr.base.enums.AlertRuleType;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroup;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.node.mgr.node.Node;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.TbNode;
import com.webank.webase.node.mgr.precompiled.PrecompiledService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * cannot connect to node triggers alert mail
 */
@Log4j2
@Component
public class NodeStatusTask {

    @Autowired
    private NodeService nodeService;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;
    @Autowired
    private PrecompiledService precompiledService;
    @Autowired
    private MailService alertMailService;

    @Scheduled(fixedDelayString = "${constant.nodeAlertMailInterval}")
    public void nodeAlertTaskStart() {
        checkAllNodeStatusForAlert();
    }

    /**
     * task start
     */
    public synchronized void checkAllNodeStatusForAlert() {
        Instant startTime = Instant.now();
        log.info("start checkAllNodeStatusForAlert startTime:{}", startTime.toEpochMilli());
        List<FrontGroup> groupList = frontGroupMapCache.getAllMap();
        if (groupList == null || groupList.size() == 0) {
            log.warn("checkNodeStatusForAlert jump over: not found any group");
            return;
        }

        CountDownLatch latch = new CountDownLatch(groupList.size());
        groupList.stream()
                .forEach(group -> checkNodeStatusByGroup(group.getGroupId()));

        try {
            latch.await();//5min
        } catch (InterruptedException ex) {
            log.error("InterruptedException", ex);
            Thread.currentThread().interrupt();
        }

        log.info("end checkAllNodeStatusForAlert useTime:{} ",
                Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * get node list by groupId to check node if abnormal
     * @param groupId
     */
    public void checkNodeStatusByGroup(int groupId) {
        log.debug("start checkNodeStatusByGroup groupId:{}", groupId);
        // TODO
        try{
            nodeService.checkAndUpdateNodeStatus(groupId);
        }catch (Exception e) {
            log.error("in checkNodeStatusByGroup error: []", e);
        }
        List<TbNode> nodeList = nodeService.queryByGroupId(groupId);
        List<String> abnormalNodeIdList = new ArrayList<>();
        nodeList.stream()
            .forEach(node -> {
                if(isNodeAbnormalAndAlert(node)) {
                    abnormalNodeIdList.add(node.getNodeId());
                }
            });
        if(!abnormalNodeIdList.isEmpty()) {
            log.warn("start checkNodeStatusByGroup node abnormal groupId:{}, nodeIds:[]",
                    groupId, JSON.toJSONString(abnormalNodeIdList));
            alertMailService.sendMailByRule(AlertRuleType.NODE_ALERT.getValue(),
                    "群组group " + groupId
                            + "的共识/观察节点nodeId：" + JSON.toJSONString(abnormalNodeIdList));
        }
        log.debug("end checkNodeStatusByGroup");
    }

    /**
     * if node is invalid( not active) alert
     * @param node
     * @return true: is abnormal, false: normal
     */
    public boolean isNodeAbnormalAndAlert(TbNode node) {
        log.debug(" isNodeAbnormalAndAlert TbNode:{}", node);
        int groupId = node.getGroupId();
        String nodeId = node.getNodeId();
        // TODO active变成invalid(2)，且校验节点是not remove 则触发告警。如果是remove 不触发
        // if node is invalid(not active)
        if(node.getNodeActive() == DataStatus.INVALID.getValue()) {
          //  if (checkNodeTypeByNodeId(groupId, nodeId) != null) {
            log.warn("isNodeAbnormalAndAlert abnormal node alert . groupId:{}, node:{}",
                    groupId, nodeId);
            return true;
         //   }
          //  return false;
        }else {
            return false;
        }
    }

    /**
     *  TODO 游离节点不报警
     * get nodeList by type(remove) through precompiled api from front
     * node type: [sealer(consensus), observer, remove]
     * @param groupId
     * @param nodeId
     * @return nodeId is unique, change list to single nodeId using list.get(0)
     */
//    public Node checkNodeNotRemove(int groupId, String nodeId) {
//        log.debug("start checkNodeNotRemove groupId:{}, nodeId:{}", groupId, nodeId);
//        Object nodeList = precompiledService.getNodeListService(groupId,
//                100, 1);
//        List<Node> nodeTypeList;
//        try {
//            // response is BaseResponse
//            Map<Object, Object> response = (Map<Object, Object>) nodeList;
//            // cast response from front to List<Node>
//            nodeTypeList = (List<Node>) response.get("data");
//        }catch (Exception e) {
//            log.error("checkNodeNotRemove in getNodeListService error " +
//                    "Object cast to List<Node> error nodeList:{},exception:[]", e, nodeList);
//            return null;
//        }
//        // abnormal node must not be "remove" node（游离节点）
//        List<Node> abnormalNodeList = nodeTypeList.stream()
//                .filter(node -> node.getNodeId().equals(nodeId)
//                        && !"remove".equals(node.getNodeType()))
//                .collect(Collectors.toList());
//        if(abnormalNodeList.isEmpty()) {
//            log.error("handleNodeAlert in getNodeListService error for no such nodeId");
//            return null;
//        }
//        log.debug("end checkNodeNotRemove abnormalNodeList:{}", abnormalNodeList);
//        return abnormalNodeList.get(0);
//    }
}