/**
 * Copyright 2014-2020  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.node.mgr.deploy.entity;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.deploy.service.PathService;

import lombok.Data;

@Data
public class NodeConfig {
    private String nodeId;
    private int hostIndex;
    private int jsonrpcPort;
    private int p2pPort;
    private int channelPort;
    private Set<Integer> groupIdList;

    /**
     * Read config value from node config files.
     *
     * @param nodePath
     * @return
     * @throws IOException
     */
    public static NodeConfig read(Path nodePath) throws IOException {
        NodeConfig config = new NodeConfig();
        config.nodeId= PathService.getNodeId(nodePath);
        try {
            config.hostIndex = Integer.parseInt(nodePath.getFileName().toString().replaceAll("node", ""));
        } catch (Exception e) {
            throw new NodeMgrException(ConstantCode.PARSE_HOST_INDEX_ERROR);
        }

        Triple<Integer, Integer, Integer> nodePorts = PathService.getNodePorts(nodePath);
        config.jsonrpcPort = nodePorts.getLeft();
        config.channelPort = nodePorts.getMiddle();
        config.p2pPort = nodePorts.getRight();

        config.groupIdList = PathService.getNodeGroupIdSet(nodePath);

        return config;
    }
}
