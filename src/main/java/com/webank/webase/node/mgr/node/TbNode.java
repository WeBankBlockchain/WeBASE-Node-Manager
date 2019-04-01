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
package com.webank.webase.node.mgr.node;

import java.math.BigInteger;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Entity class of table tb_node.
 */
@Data
public class TbNode {

    private Integer nodeId;
    private String nodeName;
    private Integer groupId;
    private Integer orgId;
    private String orgName;
    private String nodeIp;
    private Integer p2pPort;
    private Integer rpcPort;
    private Integer channelPort;
    private Integer frontPort;
    private Integer chainIndex;
    private Integer nodeType;
    private String description;
    private BigInteger blockNumber;
    private BigInteger pbftView;
    private int nodeActive;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;

    public TbNode() {
        super();
    }

    /**
     * init by groupId、nodeName.
     */
    public TbNode(Integer groupId, String nodeName) {
        super();
        this.nodeName = nodeName;
        this.groupId = groupId;
    }

    /**
     * init by nodeIp、p2pPort、rpcPort.
     */
    public TbNode(String nodeIp, Integer p2pPort, Integer rpcPort) {
        super();
        this.nodeIp = nodeIp;
        this.p2pPort = p2pPort;
        this.rpcPort = rpcPort;
    }

    /**
     * init by nodeName、groupId、orgId、nodeIp、p2pPort、rpcPort.
     */
    public TbNode(String nodeName, Integer groupId, Integer orgId, String nodeIp, Integer p2pPort,
        Integer rpcPort) {
        super();
        this.nodeName = nodeName;
        this.groupId = groupId;
        this.orgId = orgId;
        this.nodeIp = nodeIp;
        this.p2pPort = p2pPort;
        this.rpcPort = rpcPort;
    }

}