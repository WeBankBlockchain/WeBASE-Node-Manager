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

import static com.webank.webase.node.mgr.base.tools.DateUtil.YYYYMMDD_HHMMSS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.DateUtil;

import lombok.extern.log4j.Log4j2;

/**
 * TODO. check path exists
 */

@Log4j2
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
     * @param chainName
     * @param agencyName
     * @throws IOException
     */
    public void deleteAgency(String chainName, String agencyName) throws IOException {
        // delete agency root
        Path agencyRoot = this.getAgencyRoot(chainName, agencyName);
        if (Files.exists(agencyRoot)) {
            FileUtils.deleteDirectory(agencyRoot.toFile());
        }

        // delete guomi agency root
        Path gmAgencyRoot = this.getGmAgencyRoot(chainName, agencyName);
        if (Files.exists(gmAgencyRoot)) {
            Files.delete(gmAgencyRoot);
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
     * Get sdk path.
     *
     * @param chainName
     * @param ip
     * @return
     */
    public Path getSdk(String chainName, String ip) {
        return this.getHost(chainName, ip).resolve("sdk");
    }

    /**
     * Get cert root directory of chain.
     *
     * @param chainName
     * @return
     */
    public Path getCertRoot(String chainName) {
        return this.getChainRoot(chainName).resolve("cert");
    }

    /**
     * Get agency root directory of chain.
     *
     * @param chainName
     * @return
     */
    public Path getAgencyRoot(String chainName, String agencyName) {
        return this.getCertRoot(chainName).resolve(agencyName);
    }

    /**
     * Get guomi cert root directory of chain.
     *
     * @param chainName
     * @return
     */
    public Path getGmCertRoot(String chainName) {
        return this.getChainRoot(chainName).resolve("gmcert");
    }

    /**
     * TODO. optimize.
     * <p>
     * Get guomi agency root directory of chain.
     *
     * @param chainName
     * @param agencyName
     * @return
     */
    public Path getGmAgencyRoot(String chainName, String agencyName) {
        Path gmCertRoot = this.getGmCertRoot(chainName).resolve(agencyName);

        if (!Files.exists(gmCertRoot)) {
            gmCertRoot = this.getGmCertRoot(chainName).resolve(String.format("%s-gm", agencyName));
        }
        if (!Files.exists(gmCertRoot)) {
            return null;
        }
        return gmCertRoot;
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
     * @param chainName
     * @param ip
     * @param index
     * @return
     */
    public Path getNodeRoot(
            String chainName,
            String ip,
            int index) {
        return this.getHost(chainName, ip).resolve(String.format("node%s", index));
    }

    /**
     *
     * @param nodePath
     * @return
     */
    public static Path getConfigIniPath( Path nodePath ) {
        return nodePath.resolve("config.ini");
    }
    /**
     * @param rootDirOnHost
     * @param chainName
     * @return
     */
    public static String getChainRootOnHost(
            String rootDirOnHost,
            String chainName) {
        return String.format("%s/%s", rootDirOnHost, chainName);
    }

    /**
     * Return /opt/fisco/deleted-tmp.
     *
     * @param rootDirOnHost
     * @return
     */
    public static String getDeletedRootOnHost(
            String rootDirOnHost) {
        return String.format("%s/deleted-tmp", rootDirOnHost);
    }

    /**
     *  Move node directory to another when delete.
     * @param rootDirOnHost
     * @param chainName
     * @return
     */
    public static String getChainDeletedRootOnHost(
            String rootDirOnHost,
            String chainName) {
        return String.format("%s/%s-%s",
                getDeletedRootOnHost(rootDirOnHost), chainName, DateUtil.formatNow(YYYYMMDD_HHMMSS));
    }

    /**
     *
     * @param rootDirOnHost
     * @param chainName
     * @param nodeId
     * @return
     */
    public static String getNodeDeletedRootOnHost(
            String rootDirOnHost,
            String chainName,
            String nodeId) {
        return String.format("%s/delete-%s-%s/%s",
                getDeletedRootOnHost(rootDirOnHost), chainName, DateUtil.formatNow(YYYYMMDD_HHMMSS),nodeId);
    }

    /**
     * @param chainRoot
     * @param index
     * @return
     */
    public static String getNodeRootOnHost(
            String chainRoot,
            int index) {
        return String.format("%s/node%s", chainRoot, index);
    }


    /**
     * Get nodeId from a node, trim first non-blank line and return from node.nodeId file.
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

    /**
     * TODO. check params
     *
     * @param src
     * @param des
     * @return
     */
    public static void copyFile(Path src, Path des) throws IOException {
        try {
            FileUtils.copyFile(src.toFile(), des.toFile());
        } catch (IOException e) {
            log.error("Copy file from:[{}] to:[{}] error.", src, des, e);
            throw e;
        }
    }

    /**
     * TODO. check params
     *
     * @param filePair
     */
    public static void copyFile(Pair<Path, Path>... filePair) throws IOException {
        if (ArrayUtils.isNotEmpty(filePair)) {
            for (Pair<Path, Path> pair : filePair) {
                copyFile(pair.getLeft(), pair.getRight());
            }
        }
    }


}