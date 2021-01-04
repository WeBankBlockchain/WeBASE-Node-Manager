/**
 * Copyright 2014-2020 the original author or authors.
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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webank.webase.node.mgr.alert.mail.MailService;
import com.webank.webase.node.mgr.alert.rule.AlertRuleService;
import com.webank.webase.node.mgr.alert.rule.entity.TbAlertRule;
import com.webank.webase.node.mgr.base.enums.AlertRuleType;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.tools.AlertRuleTools;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.entity.Node;
import com.webank.webase.node.mgr.node.entity.TbNode;
import com.webank.webase.node.mgr.precompiled.PrecompiledService;

import lombok.extern.log4j.Log4j2;

/**
 * cannot connect to node triggers alert mail
 */
@Log4j2
@Component
public class NodeStatusMonitorTask {

    @Autowired
    private NodeService nodeService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private PrecompiledService precompiledService;
    @Autowired
    private MailService alertMailService;
    @Autowired
    private AlertRuleService alertRuleService;

    @Scheduled(fixedDelayString = "${constant.nodeStatusMonitorTaskFixedDelay}")
    public void nodeAlertTaskStart() {
        checkAllNodeStatusForAlert();
    }

    /**
     * task start
     */
    public synchronized void checkAllNodeStatusForAlert() {
        Instant startTime = Instant.now();
        log.info("start checkAllNodeStatusForAlert startTime:{}", startTime.toEpochMilli());
        //check last alert time, if within interval, not send
        TbAlertRule alertRule = alertRuleService.queryByRuleId(AlertRuleType.NODE_ALERT.getValue());
        if(AlertRuleTools.isWithinAlertIntervalByNow(alertRule)) {
            log.debug("end checkAllNodeStatusForAlert non-sending mail" +
                    " for beyond alert interval:{}", alertRule);
            return;
        }

        List<TbGroup> groupList = groupService.getGroupList(DataStatus.NORMAL.getValue());
        if (groupList == null || groupList.size() == 0) {
            log.warn("checkNodeStatusForAlert jump over: not found any group");
            return;
        }
        groupList.stream()
                .forEach(group -> checkNodeStatusByGroup(group.getGroupId()));

        log.info("end checkAllNodeStatusForAlert useTime:{} ",
                Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * get node list by groupId to check node if abnormal
     * @param groupId
     */
    public void checkNodeStatusByGroup(int groupId) {
        log.debug("start checkNodeStatusByGroup groupId:{}", groupId);
        try{
            nodeService.checkAndUpdateNodeStatus(groupId);
        }catch (Exception e) {
            log.error("in checkNodeStatusByGroup checkAndUpdateNodeStatus error: []", e);
        }
        List<TbNode> nodeList = nodeService.queryByGroupId(groupId);
        List<String> abnormalNodeIdList = new ArrayList<>();
        nodeList.stream()
            .forEach(node -> {
                // check whether node is invalid
                if(isNodeInvalid(node)) {
                    abnormalNodeIdList.add(node.getNodeId());
                }
            });
        if(!abnormalNodeIdList.isEmpty()) {
            log.warn("start  node abnormal mail alert nodeIds:{} in groupId:{}",
                    JsonTools.toJSONString(abnormalNodeIdList), groupId);
            List<String> alertContentList = new ArrayList<>();
            alertContentList.add("群组group " + groupId + "的共识/观察节点nodeId：" + JsonTools.toJSONString(abnormalNodeIdList));
            alertContentList.add("group " + groupId + "'s sealer/observer nodes nodeId: " + JsonTools.toJSONString(abnormalNodeIdList));
            // send node alert mail
            alertMailService.sendMailByRule(AlertRuleType.NODE_ALERT.getValue(), alertContentList);
        }
        log.debug("end checkNodeStatusByGroup");
    }

    /**
     * if node is invalid( not active) alert
     * @param node
     * @return true: is abnormal, false: normal
     */
    public boolean isNodeInvalid(TbNode node) {
        log.debug(" isNodeInvalid TbNode:{}", node);
        int groupId = node.getGroupId();
        String nodeId = node.getNodeId();
        // if node is invalid and nodeType isn't remove
        if(node.getNodeActive() == DataStatus.INVALID.getValue()) {
            if (checkAbnormalNodeIsNotRemove(groupId, nodeId) != null) {
                // abnormal node's nodeType is consensus(sealer) or observer
                log.warn("isNodeInvalid checkAbnormalNodeIsNotRemove. groupId:{}, node:{}",
                        groupId, nodeId);
                return true;
            }
            return false;
        }else {
            return false;
        }
    }

    /**
     * abnormal node's nodeType cannot be "remove"
     * node type: [sealer(consensus), observer, remove]
     * @param groupId
     * @param nodeId
     * @return nodeId is unique, change list to single nodeId using list.get(0)
     */
    public Node checkAbnormalNodeIsNotRemove(int groupId, String nodeId) {
        log.debug("start checkAbnormalNodeIsNotRemove groupId:{}, nodeId:{}", groupId, nodeId);
        for(LinkedHashMap<String, String> entry: getNodeListWithType(groupId)) {
            Node node = new Node();
            node.setNodeId(entry.get("nodeId"));
            node.setNodeType(entry.get("nodeType"));
            // abnormal node's nodeType cannot be "remove"（游离节点）
            if(nodeId.equals(node.getNodeId())
                    && !"remove".equals(node.getNodeType())) {
                log.debug("end checkAbnormalNodeIsNotRemove not 'remove':{}", node);
                return node;
            }
            log.debug("in checkAbnormalNodeIsNotRemove node:{}", node);
        }
        log.debug("end checkAbnormalNodeIsNotRemove abnormal nodes are all 'remove' node");
        return null;
    }

    /**
     * getNodeListWithType from front
     * @param groupId
     * @return [{nodeId=xxx,nodeType=xxx}, {..}]
     */
    public List<LinkedHashMap<String, String>> getNodeListWithType(int groupId) {
        Object responseFromFront = precompiledService.getNodeListService(groupId,
                100, 1);
        try {
            // get data from response
            LinkedHashMap<String, Object> responseMap = (LinkedHashMap<String, Object>) responseFromFront;
            log.debug("end getNodeListWithType result: {}", responseMap.get("data"));
            return (List<LinkedHashMap<String, String>>) responseMap.get("data");
        }catch (Exception e) {
            log.error("checkNodeNotRemove in getNodeListWithType error " +
                            "Object cast to List<Node> error responseFromFront:{},exception:[]",
                    responseFromFront, e);
            return null;
        }
    }
}