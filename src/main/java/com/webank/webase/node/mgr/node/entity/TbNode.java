/**
 * Copyright 2014-2020  the original author or authors.
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
package com.webank.webase.node.mgr.node.entity;

import java.math.BigInteger;
import java.time.LocalDateTime;

import com.webank.webase.node.mgr.base.enums.DataStatus;

import lombok.Data;

/**
 * Entity class of table tb_node.
 */
@Data
public class TbNode {

    private String nodeId;
    private String nodeName;
    private Integer groupId;
    private String nodeIp;
    private Integer p2pPort;
    private String description;
    private BigInteger blockNumber;
    private BigInteger pbftView;
    private int nodeActive;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;

    public static TbNode init(
            String nodeId,
            String nodeName,
            int groupId ,
            String ip,
            int p2pPort,
            String description,
            DataStatus dataStatus
    ) {
        LocalDateTime now = LocalDateTime.now();
        TbNode node = new TbNode();
        node.setNodeId(nodeId);
        node.setNodeName(nodeName);
        node.setGroupId(groupId);
        node.setNodeIp(ip);
        node.setP2pPort(p2pPort);
        node.setDescription(description);
        node.setBlockNumber(BigInteger.ZERO);
        node.setPbftView(BigInteger.ZERO);
        node.setNodeActive(dataStatus.getValue());
        node.setCreateTime(now);
        node.setModifyTime(now);

        return node;
    }
}