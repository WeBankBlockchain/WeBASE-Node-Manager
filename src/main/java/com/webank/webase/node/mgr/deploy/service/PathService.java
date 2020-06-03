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
package com.webank.webase.node.mgr.deploy.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.ini4j.Ini;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webank.webase.node.mgr.base.properties.ConstantProperties;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
@Component
public class PathService {

    @Autowired private ConstantProperties constant;

    /**
     * The file to save ipconf.
     *
     * @param chainName
     * @return
     */
    public Path getIpConfig(String chainName) {
        return Paths.get(constant.getNodesRootDir(), String.format("%s_ipconf", chainName));
    }

    /**
     * Root dir of the nodes config.
     *
     * @param chainName
     * @return
     */
    public String getChainRootString(String chainName) {
        return this.getChainRoot(chainName).toString();
    }

    /**
     * Root dir of the nodes config.
     *
     * @param chainName
     * @return
     */
    public Path getChainRoot(String chainName) {
        return Paths.get(constant.getNodesRootDir(), String.format("%s_nodes", chainName));
    }

    /**
     * Delete chain node config while exception occurred during deploy option.
     *
     * @param chainName
     * @return
     */
    public void deleteChain(String chainName) throws IOException {
        // delete nodes config
        Path chainRoot = getChainRoot(chainName);
        if (Files.exists(chainRoot)) {
            FileUtils.deleteDirectory(chainRoot.toFile());
        }

        // delete ipconf
        Path ipConfig = getIpConfig(chainName);
        if (Files.exists(ipConfig)) {
            Files.delete(ipConfig);
        }
    }

    /**
     * Get host path.
     *
     * @param chainName
     * @param ip
     * @return
     */
    public Path getHost(String chainName, String ip) {
        return Paths.get(this.getChainRootString(chainName), ip);
    }

    /**
     * Get sdk path under host.
     *
     * @param chainName
     * @param ip
     * @return
     */
    public Path getHostSdk(String chainName, String ip) {
        return this.getHost(chainName, ip).resolve("sdk");
    }


    /**
     * Get nodeX path under host.
     *
     * @param chainName
     * @param ip
     * @return
     */
    public List<Path> listHostNodesPath(String chainName, String ip) throws IOException {
        Path hostNodes = this.getHost(chainName, ip);
        return Files.walk(hostNodes, 1)
                .filter(path -> path.getFileName().toString().startsWith("node"))
                .collect(Collectors.toList());
    }

    /**
     *
     * @param rootDirOnHost
     * @param chainName
     * @return
     */
    public static String getChainRootOnHost(
            String rootDirOnHost,
            String chainName ) {
        return String.format("%s/%s", rootDirOnHost, chainName );

    }

    /**
     *
     * @param chainRoot
     * @param index
     * @return
     */
    public static String getNodeRootOnHost(
            String chainRoot,
            short index) {
        return String.format("%s/node%s", chainRoot,index);

    }

    /**
     * Get nodeId from a node, trim first non-blank line and return from node.nodeid file.
     *
     * @param nodePath
     * @return
     * @throws IOException
     */
    public static String getNodeId(Path nodePath) throws IOException {
        List<String> lines = Files.readAllLines(nodePath.resolve("conf/node.nodeid"));
        if (CollectionUtils.isEmpty(lines)) {
            return null;
        }
        return lines.stream().filter(StringUtils::isNotBlank)
                .map(StringUtils::trim).findFirst().orElse(null);
    }

    /**
     * Get jsonrpcPort, channelPort, p2pPort from a node.
     *
     * @param nodePath
     * @return order : <jsonrpcPort, channelPort, p2pPort>
     * @throws IOException
     */
    public static Triple<Short, Short, Short> getNodePorts(Path nodePath) {
        try {
            Path configIni = nodePath.resolve("config.ini");
            Ini ini = new Ini(configIni.toFile());
            short channelPort = Short.parseShort(ini.get("rpc", "channel_listen_port"));
            short jsonrpcPort = Short.parseShort(ini.get("rpc", "jsonrpc_listen_port"));
            short p2pPort = Short.parseShort(ini.get("p2p", "listen_port"));
            return Triple.of(jsonrpcPort, channelPort, p2pPort);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get node group id set.
     *
     * @param nodePath
     * @return
     * @throws IOException
     */
    public static Set<Integer> getNodeGroupIdSet(Path nodePath) {
        try {
            return Files.walk(nodePath.resolve("conf"), 1)
                    .filter(path -> path.getFileName().toString().matches("^group\\.\\d+\\.genesis$"))
                    .map((path) -> Integer.parseInt(path.getFileName().toString()
                            .replaceAll("group\\.", "").replaceAll("\\.genesis", "")))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            return null;
        }
    }
}