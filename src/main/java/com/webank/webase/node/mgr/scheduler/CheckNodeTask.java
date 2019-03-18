/*
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.scheduler;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.enums.NodeType;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.node.NodeParam;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.TbNode;
import com.webank.webase.node.mgr.web3.NodeHeartBeat;
import com.webank.webase.node.mgr.web3.Web3Service;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class CheckNodeTask {

    @Autowired
    private NodeService nodeService;
    @Autowired
    private ConstantProperties constantsProperties;
    @Autowired
    private Web3Service web3Service;

    /**
     * check node status.
     */
    public void checkNodeStatus() {
        Instant startTime = Instant.now();
        log.info("start checkNodeStatus startTime:{}", startTime.toEpochMilli());
        List<TbNode> listOfnode = new ArrayList<>();
        try {
            listOfnode = nodeService.qureyNodeList(new NodeParam());
        } catch (Exception ex) {
            log.error("fail checkNodeStatus", ex);
            return;
        }

        if (null == listOfnode || listOfnode.size() == 0) {
            log.error("fail checkNodeStatus. no node information found. jump over");
            return;
        }

        for (TbNode tbNode : listOfnode) {
            String nodeIp = tbNode.getNodeIp();
            Integer frontPort = tbNode.getFrontPort();
            if (NodeType.CURRENT.getValue() != tbNode.getNodeType()) {
                log.info("The node[{}:{}] is not current node. jump over", nodeIp, frontPort);
                continue;
            }

            try {
                BaseResponse rsp = web3Service.nodeHeartBeat(nodeIp, frontPort);
                if (rsp.getCode() != 0) {
                    log.info("The node[{}:{}]. frontRspCode:{} frontRspMsg:{}. node is invalid",
                        nodeIp, frontPort, rsp.getCode(), rsp.getMessage());
                    tbNode.setNodeActive(DataStatus.INVALID.getValue());
                } else {
                    NodeHeartBeat heartBeat = NodeMgrTools
                        .object2JavaBean(rsp.getData(), NodeHeartBeat.class);
                    tbNode.setNodeActive(DataStatus.NORMAL.getValue());
                    tbNode.setBlockNumber(heartBeat.getBlockNumber());
                    tbNode.setPbftView(heartBeat.getPbftView());
                    log.info("The node[{}:{}]. heartBeat:{}. node is normal", nodeIp, frontPort,
                        JSON.toJSONString(heartBeat));
                }
            } catch (Exception ex) {
                log.error("fail checkNodeStatus. node is invalid", ex);
                tbNode.setNodeActive(DataStatus.INVALID.getValue());
            }

            nodeService.updateNodeInfo(tbNode);
        }
        log.info("end checkNodeStatus useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
    }
}
