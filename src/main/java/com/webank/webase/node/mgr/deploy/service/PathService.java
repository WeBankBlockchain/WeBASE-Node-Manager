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
     * @return              NODES_ROOT/[chainName]_ipconf, a file, not a directory.
     */
    public Path getIpConfig(String chainName) {
        return Paths.get(constant.getNodesRootDir(), String.format("%s_ipconf", chainName));
    }

    /**
     * Root dir of the nodes config.
     *
     * @param chainName
     * @return              NODES_ROOT/[chainName]/ as a {@link String}, a directory.
     */
    public String getChainRootString(String chainName) {
        return this.getChainRoot(chainName).toString();
    }

    /**
     * Root dir of the nodes config.
     *
     * @param chainName
     * @return              NODES_ROOT/[chainName]/ as a {@link Path}, a directory.
     */
    public Path getChainRoot(String chainName) {
        return Paths.get(constant.getNodesRootDir(), String.format("%s_nodes", chainName));
    }



    /**
     * Return NODES_ROOT_TMP/.
     *
     * @return              NODES_ROOT_TMP/, a directory.
     */
    public Path getLocalDeleteRoot() throws IOException {
        Path path = Paths.get(String.format("%s/", constant.getNodesRootTmpDir()));
        if (! Files.exists(path)){
            Files.createDirectories(path);
        }
        return path;
    }

    /**
     *  Move node directory to another when delete.
     * @param chainName
     * @return              NODES_ROOT_TMP/[chainName]-yyyyMMdd_HHmmss, a directory.
     */
    public Path getChainDeletedRoot(String chainName) throws IOException {
        return Paths.get(String.format("%s/%s-%s",
                this.getLocalDeleteRoot(), chainName, DateUtil.formatNow(YYYYMMDD_HHMMSS)));
    }

    /**
     *
     * @param chainDeletedRoot
     * @return
     */
    public Path getIpConfDeleted(String chainName,Path chainDeletedRoot) {
        return chainDeletedRoot.resolve(String.format("%s_ipconf", chainName));
    }

    /**
     *  Move node directory to another when delete.
     * @param chainName
     * @return              NODES_ROOT_TMP/[chainName]-yyyyMMdd_HHmmss/[ip]/[nodeid], a directory.
     */
    public Path getNodeDeletedRoot( String chainName, String ip, String nodeId) throws IOException {
        return Paths.get(String.format("%s/%s/%s ", this.getChainDeletedRoot(chainName), ip, nodeId));
    }

    /**
     *
     * @return              NODES_ROOT_TMP/[chainName]-yyyyMMdd_HHmmss/[agencyName], a directory.
     *
     * @param chainName
     * @param agencyName
     * @return
     */
    public Path getAgencyDeleteRoot( String chainName, String agencyName) throws IOException {
        return Paths.get(String.format("%s/%s/%s ", this.getChainDeletedRoot(chainName), agencyName));
    }


    /**
     * Get host path.
     *
     * @param chainName
     * @param ip
     * @return              NODES_ROOT/[chainName]/[ip] as a {@link Path}, a directory.
     */
    public Path getHost(String chainName, String ip) {
        return Paths.get(this.getChainRootString(chainName), ip);
    }

    /**
     * Get sdk path.
     *
     * @param chainName
     * @param ip
     * @return              NODES_ROOT/[chainName]/[ip]/sdk as a {@link Path}, a directory.
     */
    public Path getSdk(String chainName, String ip) {
        return this.getHost(chainName, ip).resolve("sdk");
    }

    /**
     * Get cert root directory of chain.
     *
     * @param chainName
     * @return              NODES_ROOT/[chainName]/cert as a {@link Path}, a directory.
     */
    public Path getCertRoot(String chainName) {
        return this.getChainRoot(chainName).resolve("cert");
    }

    /**
     * Get agency root directory of chain.
     *
     * @param chainName
     * @return              NODES_ROOT/[chainName]/cert/[agencyName] as a {@link Path}, a directory.
     */
    public Path getAgencyRoot(String chainName, String agencyName) {
        return this.getCertRoot(chainName).resolve(agencyName);
    }

    /**
     * Get guomi cert root directory of chain.
     *
     * @param chainName
     * @return              NODES_ROOT/[chainName]/gmcert as a {@link Path}, a directory.
     */
    public Path getGmCertRoot(String chainName) {
        return this.getChainRoot(chainName).resolve("gmcert");
    }

    /**
     * Get guomi agency root directory of chain.
     * Agency cert of guomi has two types, agencyName and agencyName-gm.
     *
     * @param chainName
     * @param agencyName
     * @return              NODES_ROOT/[chainName]/gmcert/[agencyName][-gm] as a {@link Path}, a directory.
     */
    public Path getGmAgencyRoot(String chainName, String agencyName) {
        Path gmCertRoot = this.getGmCertRoot(chainName).resolve(agencyName);

        if (!Files.exists(gmCertRoot)) {
            gmCertRoot = this.getGmCertRoot(chainName).resolve(String.format("%s-gm", agencyName));
        }
        return gmCertRoot;
    }

    /**
     * Get all node[x] paths of a host.
     *
     * @param chainName
     * @param ip
     * @return              {@link List}<{@link Path}> of NODES_ROOT/[chainName]/[ip]/node[x], a directory list.
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
     * @return              NODES_ROOT/[chainName]/[ip]/node[index] as a {@link Path}, a directory.
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
     * @return              NODES_ROOT/[chainName]/[ip]/node[index]/config.ini as a {@link Path}, a file.
     */
    public static Path getConfigIniPath( Path nodePath ) {
        return nodePath.resolve("config.ini");
    }
    /**
     * @param rootDirOnHost
     * @param chainName
     * @return              /opt/fisco/[chainName] as a {@link String}, a directory.
     */
    public static String getChainRootOnHost(
            String rootDirOnHost,
            String chainName) {
        return String.format("%s/%s", rootDirOnHost, chainName);
    }

    /**
     *
     * @param rootDirOnHost
     * @return              /opt/fisco/deleted-tmp as a {@link String}, a directory.
     */
    public static String getDeletedRootOnHost(
            String rootDirOnHost) {
        return String.format("%s/deleted-tmp", rootDirOnHost);
    }

    /**
     *  Move node directory to another when delete.
     * @param rootDirOnHost
     * @param chainName
     * @return              /opt/fisco/deleted-tmp/[chainName]-yyyyMMdd_HHmmss/ as a {@link String}, a directory.
     */
    public static String getChainDeletedRootOnHost(
            String rootDirOnHost,
            String chainName) {
        return String.format("%s/%s-%s",
                getDeletedRootOnHost(rootDirOnHost), chainName, DateUtil.formatNow(YYYYMMDD_HHMMSS));
    }

    /**
     *
     * @param chainDeleteRootOnHost
     * @param nodeId
     * @return
     * @return             /opt/fisco/deleted-tmp/[chainName]-yyyyMMdd_HHmmss/[nodeid(128)] as a {@link String}, a directory.
     */
    public static String getNodeDeletedRootOnHost(
            String chainDeleteRootOnHost,
            String nodeId) {
        return String.format("%s/%s", chainDeleteRootOnHost,nodeId);
    }

    /**
     * @param chainRoot
     * @param index
     * @return             /opt/fisco/[chainName]/node[index] as a {@link String}, a directory.
     */
    public static String getNodeRootOnHost(
            String chainRoot,
            int index) {
        return String.format("%s/node%s", chainRoot, index);
    }


    /**
     * Get nodeId from a node, trim first non-blank line and return from node.nodeId file.
     *
     * Read node.nodeid from file: NODES_ROOT/[chainName]/[ip]/node[x]/conf/node.nodeid.
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
     * Read group id list from files: NODES_ROOT/[chainName]/[ip]/node[x]/conf/group.[groupId].genesis
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
     *  Copy single file from src to dst.
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
     *  Copy multiply files from src to dst.
     *
     * @param filePair
     */
    public static void copyFile(Pair<Path, Path> ... filePair) throws IOException {
        if (ArrayUtils.isNotEmpty(filePair)) {
            for (Pair<Path, Path> pair : filePair) {
                copyFile(pair.getLeft(), pair.getRight());
            }
        }
    }

    /**
     * Move all chain's config to tmp dir: NODES_ROOT_TMP/[chainName]/ ;
     * a
     * @param chainName
     * @return
     */
    public void deleteChain(String chainName) throws IOException {
        // mv NODES_ROOT/[chainName]/ to NODES_ROOT_TMP/[chainName]-yyyyMMdd_HHmmss
        Path src_chainRoot = this.getChainRoot(chainName);
        Path dst_deleteChainRoot = this.getChainDeletedRoot(chainName);
        move(src_chainRoot,dst_deleteChainRoot);

        // mv  NODES_ROOT/[chainName]_ipconf to NODES_ROOT_TMP/[chainName]-yyyyMMdd_HHmmss/[chainName]_ipconf
        Path src_ipConf = this.getIpConfig(chainName);
        Path dst_ipConf = this.getIpConfDeleted(chainName,dst_deleteChainRoot);
        move(src_ipConf,dst_ipConf);
    }

    /**
     *  Move node to tmp dir.
     *
     * @param chainName
     * @return
     */
    public void deleteNode(String chainName,String ip, int hostIndex, String nodeId ) throws IOException {
        // mv NODES_ROOT/[chainName]/[ip]/node[hostIndex] to NODES_ROOT_TMP/[chainName]-yyyyMMdd_HHmmss/ip/[nodeId]
        Path src_nodeRoot = this.getNodeRoot(chainName,ip,hostIndex);
        Path dst_nodeDeleteRoot = this.getNodeDeletedRoot(chainName,ip, nodeId);
        move(src_nodeRoot,dst_nodeDeleteRoot);
    }

    /**
     * @param chainName
     * @param agencyName
     * @throws IOException
     */
    public void deleteAgency(String chainName, String agencyName) throws IOException {
        // mv NODES_ROOT/[chainName]/[ip]/node[hostIndex] to NODES_ROOT_TMP/[chainName]-yyyyMMdd_HHmmss/ip/[nodeId]
        Path src_agencyRoot = this.getAgencyRoot(chainName,agencyName);
        Path dst_agencyDeleteRoot = this.getAgencyDeleteRoot(chainName, agencyName);
        move(src_agencyRoot,dst_agencyDeleteRoot);
    }

    /**
     *
     * @param src
     * @param dst
     * @throws IOException
     */
    private void move(Path src, Path dst) throws IOException {
        if (Files.exists(src)){
            if (Files.isDirectory(src)){
                FileUtils.moveDirectoryToDirectory(src.toFile(),dst.toFile(),true);
            }else{
                FileUtils.moveFile(src.toFile(),dst.toFile());
            }
        }
    }
}