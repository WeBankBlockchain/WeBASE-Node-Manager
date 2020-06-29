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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.ini4j.Ini;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.deploy.service.PathService;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public class NodeConfig {
    private String nodeId;
    private int hostIndex;
    private int jsonrpcPort;
    private int p2pPort;
    private int channelPort;
    private Set<Integer> groupIdSet;

    /**
     * Read config value from node config files.
     *
     * @param nodePath
     * @return
     * @throws IOException
     */
    public static NodeConfig read(Path nodePath)  {
        NodeConfig config = new NodeConfig();
        try {
            config.nodeId= PathService.getNodeId(nodePath);
            try {
                config.hostIndex = Integer.parseInt(nodePath.getFileName().toString().replaceAll("node", ""));
            } catch (Exception e) {
                throw new NodeMgrException(ConstantCode.PARSE_HOST_INDEX_ERROR);
            }

            Triple<Integer, Integer, Integer> nodePorts = NodeConfig.getNodePorts(nodePath);
            config.jsonrpcPort = nodePorts.getLeft();
            config.channelPort = nodePorts.getMiddle();
            config.p2pPort = nodePorts.getRight();

            config.groupIdSet = PathService.getNodeGroupIdSet(nodePath);
        } catch (Exception e) {
            log.error("Read delete node:[{}] config error", nodePath.toAbsolutePath().toString(), e);
            throw new NodeMgrException(ConstantCode.READ_NODE_CONFIG_ERROR, e);
        }

        return config;
    }

    /**
     * Modify sdk dir structure.
     *
     * @param encryptType
     * @param sdk
     */
    public static void initSdkDir(byte encryptType, Path sdk) {
        if (Files.exists(sdk)) {
            Path sdkConfig = sdk.resolve("conf");
            if (encryptType == EncryptType.SM2_TYPE) {
                sdkConfig = sdkConfig.resolve("origin_cert");
            }

            try {
                PathService.copyFile(
                        Pair.of(sdkConfig.resolve("ca.crt"), sdk.resolve("ca.crt")),
                        Pair.of(sdkConfig.resolve("node.crt"), sdk.resolve("node.crt")),
                        Pair.of(sdkConfig.resolve("node.key"), sdk.resolve("node.key")),
                        Pair.of(sdkConfig.resolve("node.crt"), sdk.resolve("sdk.crt")),
                        Pair.of(sdkConfig.resolve("node.key"), sdk.resolve("sdk.key"))
                );

                FileUtils.deleteDirectory(sdk.resolve("conf").toFile());
            } catch (IOException e) {
                throw new NodeMgrException(ConstantCode.COPY_SDK_FILES_ERROR);
            }
        } else {
            log.error("SDK dir:[{}] not exists.", sdk.toAbsolutePath().toString());
        }
    }

    /**
     *
     * @param oldNode
     * @param newNode
     */
    public static void copyGroupConfigFiles(Path oldNode,Path newNode, int groupId) {
        if (Files.exists(oldNode)) {
            String groupGenesisFileName =String.format("conf/group.%s.genesis",groupId);
            String groupIniFileName =String.format("conf/group.%s.ini",groupId);

            try {
                PathService.copyFile(
                        Pair.of(oldNode.resolve(groupGenesisFileName), newNode.resolve(groupGenesisFileName)),
                        Pair.of(oldNode.resolve(groupIniFileName), newNode.resolve(groupIniFileName))
                );
            } catch (IOException e) {
                throw new NodeMgrException(ConstantCode.COPY_GROUP_FILES_ERROR);
            }
        } else {
            log.error("Old node dir:[{}] not exists.", oldNode.toAbsolutePath().toString());
        }
    }

    /**
     * Get jsonrpcPort, channelPort, p2pPort from a node.
     *
     * @param nodePath
     * @return order : <jsonrpcPort, channelPort, p2pPort>
     * @throws IOException
     */
    public static Triple<Integer, Integer, Integer> getNodePorts(Path nodePath) throws IOException {
        Path configIni = PathService.getConfigIniPath(nodePath);
        Ini ini = new Ini(configIni.toFile());
        int channelPort = Integer.parseInt(ini.get("rpc", "channel_listen_port"));
        int jsonrpcPort = Integer.parseInt(ini.get("rpc", "jsonrpc_listen_port"));
        int p2pPort = Integer.parseInt(ini.get("p2p", "listen_port"));
        return Triple.of(jsonrpcPort, channelPort, p2pPort);
    }

    /**
     *
     * @param nodePath
     * @return
     */
    public static Set<Integer> getGroupIdSet(Path nodePath ){
        return NodeConfig.read(nodePath).getGroupIdSet();
    }
}
