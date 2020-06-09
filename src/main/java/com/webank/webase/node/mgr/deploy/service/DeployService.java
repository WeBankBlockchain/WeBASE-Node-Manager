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

package com.webank.webase.node.mgr.deploy.service;

import static com.webank.webase.node.mgr.base.code.ConstantCode.HOST_CONNECT_ERROR;
import static com.webank.webase.node.mgr.base.code.ConstantCode.IP_NUM_ERROR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.code.RetCode;
import com.webank.webase.node.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.node.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.base.enums.GroupType;
import com.webank.webase.node.mgr.base.enums.NodeStatusEnum;
import com.webank.webase.node.mgr.base.enums.RunTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.SshTools;
import com.webank.webase.node.mgr.base.tools.ValidateUtil;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.chain.ChainService;
import com.webank.webase.node.mgr.deploy.entity.ConfigLine;
import com.webank.webase.node.mgr.deploy.entity.NodeConfig;
import com.webank.webase.node.mgr.deploy.entity.TbAgency;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.entity.TbConfig;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbAgencyMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbChainMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbConfigMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbHostMapper;
import com.webank.webase.node.mgr.front.FrontMapper;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapMapper;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.node.mgr.group.GroupMapper;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.node.NodeMapper;
import com.webank.webase.node.mgr.node.NodeService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DeployService {

    @Autowired private TbConfigMapper tbConfigMapper;
    @Autowired private TbChainMapper tbChainMapper;
    @Autowired private FrontMapper frontMapper;
    @Autowired private NodeMapper nodeMapper;
    @Autowired private FrontGroupMapMapper frontGroupMapMapper;
    @Autowired private TbAgencyMapper tbAgencyMapper;
    @Autowired private GroupMapper groupMapper;
    @Autowired private TbHostMapper tbHostMapper;

    @Autowired private AgencyService agencyService;
    @Autowired private HostService hostService;
    @Autowired private GroupService groupService;
    @Autowired private FrontService frontService;
    @Autowired private FrontGroupMapService frontGroupMapService;
    @Autowired private NodeService nodeService;
    @Autowired private ChainService chainService;
    @Autowired private DeployShellService deployShellService;
    @Autowired private DockerClientService dockerClientService;
    @Autowired private PathService pathService;
    @Autowired private ConstantProperties constant;

    /**
     * Add in v1.4.0 deploy.
     *
     * @param ipConf
     * @param tagId
     * @param rootDirOnHost
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Pair<RetCode, String> deploy(String chainName,
                                        String[] ipConf,
                                        int tagId,
                                        String rootDirOnHost) throws NodeMgrException {
        // TODO. check param
        if (StringUtils.isBlank(chainName)) {
            throw new NodeMgrException(ConstantCode.PARAM_EXCEPTION);
        }

        // check tagId existed
        TbConfig imageConfig = tbConfigMapper.selectByPrimaryKey(tagId);
        if (imageConfig == null
                || StringUtils.isBlank(imageConfig.getConfigValue())) {
            throw new NodeMgrException(ConstantCode.TAG_ID_PARAM_ERROR);
        }

        log.info("Check chainName exists....");
        TbChain chain = tbChainMapper.getByChainName(chainName);
        if (chain != null) {
            throw new NodeMgrException(ConstantCode.CHAIN_NAME_EXISTS_ERROR);
        }

        // validate ipConf config
        log.info("Parse ipConf content....");
        List<ConfigLine> configLineList = this.parseIpConf(ipConf);

        byte encryptType = (byte) (imageConfig.getConfigValue().endsWith("-gm") ?
                EncryptType.SM2_TYPE : EncryptType.ECDSA_TYPE);

        try {
            // generate nodes config
            ExecuteResult buildChainResult = deployShellService.execBuildChain(encryptType, ipConf, chainName);
            if (buildChainResult.failed()) {
                return Pair.of(ConstantCode.EXEC_BUILD_CHAIN_ERROR, buildChainResult.getExecuteOut());
            }

            // insert chain
            final TbChain newChain = this.chainService.insert(chainName, chainName,
                    imageConfig.getConfigValue(), encryptType, ChainStatusEnum.INITIALIZED);
            // TODO 提高代码可读性
            Map<String, Integer> agencyIdMap = new HashMap<>();
            Map<String, Integer> hostIdMap = new HashMap<>();
            Map<String, Set<Integer>> hostGroupListMap = new HashMap<>();
            Map<String, Pair<String, Integer>> hostAgencyMap = new HashMap<>();
            Map<Integer, AtomicInteger> groupCountMap = new HashMap<>();

            configLineList.forEach((config) -> {
                // insert agency
                if (!agencyIdMap.containsKey(config.getAgencyName())) {
                    TbAgency agency = agencyService.insert(config.getAgencyName(), config.getAgencyName(),
                            newChain.getId(), newChain.getChainName());
                    agencyIdMap.put(config.getAgencyName(), agency.getId());
                }
                hostAgencyMap.put(config.getIp(),
                        Pair.of(config.getAgencyName(), agencyIdMap.get(config.getAgencyName())));

                // insert host
                if (!hostIdMap.containsKey(config.getIp())) {
                    TbHost host = hostService.insert(agencyIdMap.get(config.getAgencyName()),
                            config.getAgencyName(), config.getIp(), rootDirOnHost);
                    hostIdMap.put(config.getIp(), host.getId());
                }

                // insert group
                // sum node num in group
                if (hostGroupListMap.containsKey(config.getIp())) {
                    hostGroupListMap.get(config.getIp()).addAll(config.getGroupIdSet());
                } else {
                    hostGroupListMap.put(config.getIp(), config.getGroupIdSet());
                }
                config.getGroupIdSet().forEach((groupId) -> {
                    if (groupCountMap.containsKey(groupId)) {
                        groupCountMap.get(groupId).addAndGet(config.getNum());
                    } else {
                        // TODO. why insert ignore???
                        // save group default status maintaining
                        groupService.saveGroup(groupId, config.getNum(),
                             "deploy", GroupType.DEPLOY, GroupStatus.MAINTAINING,
                            newChain.getId(), newChain.getChainName());
                        groupCountMap.put(groupId, new AtomicInteger(config.getNum()));
                    }
                });
            });

            // insert nodes, there may be multiple nodes on a host.
            for (String ip : hostIdMap.keySet()) {
                List<Path> nodePathList = pathService.listHostNodesPath(newChain.getChainName(), ip);
                Pair<String, Integer> agency = hostAgencyMap.get(ip);
                for (Path nodePath : nodePathList) {
                    // get node properties
                    NodeConfig nodeConfig = NodeConfig.read(nodePath);

                    // frontPort = 5002 + indexOnHost(0,1,2,3...)
                    int frontPort = constant.getDefaultFrontPort() + nodeConfig.getHostIndex();

                    TbFront front = this.frontService.insert(nodeConfig.getNodeId(), ip, frontPort, agency.getKey(),
                            imageConfig.getConfigValue(), RunTypeEnum.DOCKER,
                            agency.getValue(), hostIdMap.get(ip),
                            nodeConfig.getHostIndex(), imageConfig.getConfigValue(),
                            DockerClientService.getContainerName(rootDirOnHost, chainName, nodeConfig.getHostIndex()),
                            nodeConfig.getJsonrpcPort(), nodeConfig.getP2pPort(),
                            nodeConfig.getChannelPort(), FrontStatusEnum.INITIALIZED);

                    hostGroupListMap.get(ip).forEach((groupId) -> {
                        String nodeName = NodeService.getNodeName(groupId, nodeConfig.getNodeId());
                        this.nodeService.insert(nodeConfig.getNodeId(), nodeName,
                                groupId, ip, nodeConfig.getP2pPort(),
                                nodeName, NodeStatusEnum.DEAD);

                        this.frontGroupMapService.newFrontGroup(front.getFrontId(), groupId);
                    });

                    // generate front application.yml
                    // TODO. make constant
                    Files.write(nodePath.resolve("application.yml"), Arrays.asList(
                            new String[]{
                                    "spring:",
                                    "  datasource:",
                                    "    url: jdbc:h2:file:/data/h2/webasefront;DB_CLOSE_ON_EXIT=FALSE",
                                    "sdk:",
                                    String.format("  encryptType: %s  # 0:ecdsa, 1:guomi", encryptType),
                                    String.format("  channelPort: %s", nodeConfig.getChannelPort()),
                                    "server:",
                                    String.format("  port: %s", frontPort)
                            }
                    ), StandardOpenOption.CREATE);
                }
            }

            // update group node count
            groupCountMap.forEach((groupId, nodeCount) -> {
                groupService.updateGroupNodeCount(groupId, nodeCount.get());
            });

            // init host env
            this.deployShellService.initHostList(newChain.getChainName());

            return Pair.of(ConstantCode.SUCCESS, buildChainResult.getExecuteOut());
        } catch (Exception e) {
            try {
                pathService.deleteChain(chainName);
            } catch (IOException ex) {
                log.error("Delete chain:[{}] node config ERROR while exception occurred during deploy option."
                        , chainName, ex);
            }
            throw new NodeMgrException(ConstantCode.DEPLOY_WITH_UNKNOWN_EXCEPTION_ERROR, e);
        }

    }

    /**
     * Validate ipConf.
     *
     * @param ipConf
     * @throws NodeMgrException
     * @return List<ConfigLine> entity of config for build_chain
     */
    public List<ConfigLine> parseIpConf(String[] ipConf) throws NodeMgrException {
        if (ArrayUtils.isEmpty(ipConf)) {
            throw new NodeMgrException(ConstantCode.IP_CONF_PARAM_NULL_ERROR);
        }

        List<ConfigLine> configLineList = new ArrayList<>();
        Map<String, String> hostAgencyMap = new HashMap<>();
        for (String line : ipConf) {
            if (StringUtils.isBlank(line)) {
                continue;
            }

            ConfigLine configLine = ConfigLine.parseLine(line);
            if (configLine == null) {
                continue;
            }

            // A host only belongs to one agency.
            if (hostAgencyMap.get(configLine.getIp()) != null &&
                    !StringUtils.equalsIgnoreCase(hostAgencyMap.get(configLine.getIp()), configLine.getAgencyName())) {
                throw new NodeMgrException(ConstantCode.HOST_ONLY_BELONGS_ONE_AGENCY_ERROR);
            }
            hostAgencyMap.put(configLine.getIp(), configLine.getAgencyName());

//            frontService.checkNotSupportIp(configLine.getIp());

            // SSH to host ip
            if (!SshTools.connect(configLine.getIp())) {
                // cannot SSH to IP
                throw new NodeMgrException(HOST_CONNECT_ERROR.msg(configLine.getIp()));
            }

            // TODO. Get max mem size, check nodes num.
            if (configLine.getNum() <= 0) {
                throw new NodeMgrException(IP_NUM_ERROR.msg(line));
            }

            configLineList.add(configLine);
        }

        if (CollectionUtils.isEmpty(configLineList)) {
            throw new NodeMgrException(ConstantCode.IP_CONF_PARAM_NULL_ERROR);
        }

        return configLineList;
    }


    /**
     * TODO. delete object should call service.delete
     * <p>
     * Delete a chain by chain name.
     *
     * @param chainName
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public RetCode delete(String chainName) {
        if (StringUtils.isBlank(chainName)) {
            throw new NodeMgrException(ConstantCode.PARAM_EXCEPTION);
        }
        log.info("Delete chain:[{}] data in db and config files...", chainName);

        TbChain chain = tbChainMapper.getByChainName(chainName);
        if (chain != null) {
            // select agency list
            List<TbAgency> tbAgencyList = tbAgencyMapper.selectByChainId(chain.getId());
            for (TbAgency agency : CollectionUtils.emptyIfNull(tbAgencyList)) {
                // select host, front, group in agency
                List<TbFront> frontList = frontMapper.selectByAgencyId(agency.getId());

                for (TbFront front : CollectionUtils.emptyIfNull(frontList)) {
                    // TODO. ssh remote server, shutdown, delete node config
                    // delete config files
                    log.info("ssh remote server and shutdown front id:[{}].", front.getFrontId());

                    // optimize code to get host
                    TbHost host = this.tbHostMapper.selectByPrimaryKey(front.getHostId());
                    if (host != null) {
                        log.info("Docker stop and remove container front id:[{}:{}].", front.getFrontId(), front.getContainerName());
                        this.dockerClientService.removeByName(
                                front.getFrontIp(),
                                host.getDockerPort(),
                                front.getContainerName());
                    }

                    // TODO. delete node and frontGroupMap in batch
                    // delete node
                    if (StringUtils.isNotBlank(front.getNodeId())) {
                        log.info("Delete node data by node id:[{}].", front.getNodeId());
                        this.nodeMapper.deleteByNodeId(front.getNodeId());
                        log.info("Delete front group map data by front id:[{}].", front.getFrontId());
                        this.frontGroupMapMapper.removeByFrontId(front.getFrontId());
                    }
                }

                // delete front in batch
                log.info("Delete front data by agency id:[{}].", agency.getId());
                this.frontMapper.deleteByAgencyId(agency.getId());

                // delete host in batch
                log.info("Delete host data by agency id:[{}].", agency.getId());
                this.tbHostMapper.deleteByAgencyId(agency.getId());
            }

            log.info("Delete group data by chain id:[{}].", chain.getId());
            this.groupMapper.deleteByChainId(chain.getId());

            log.info("Delete agency data by chain id:[{}].", chain.getId());
            this.tbAgencyMapper.deleteByChainId(chain.getId());

            log.info("Delete chain data by chain id:[{}].", chain.getId());
            this.tbChainMapper.deleteByPrimaryKey(chain.getId());
        }

        try {
            log.info("Delete chain:[{}] config files", chainName);
            this.pathService.deleteChain(chainName);
        } catch (IOException e) {
            log.error("Delete chain:[{}] config files ERROR", e);
        }

        return ConstantCode.SUCCESS;
    }

    /**
     * Add a node.
     *
     * @param ip
     * @param agencyName
     * @param num
     * @param chainName
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Pair<RetCode, String> add(String chainName, String ip, String agencyName, int num) {
        log.info("Check chainName:[{}] exists...", chainName);
        TbChain chain = tbChainMapper.getByChainName(chainName);
        if (chain == null) {
            throw new NodeMgrException(ConstantCode.CHAIN_NAME_NOT_EXISTS_ERROR);
        }

        log.info("Check ip format:[{}]...", ip);
        if (!ValidateUtil.validateIpv4(ip)) {
            throw new NodeMgrException(ConstantCode.IP_FORMAT_ERROR);
        }

        List<TbAgency> agencyList = this.tbAgencyMapper.selectByChainId(chain.getId());
        if (CollectionUtils.isEmpty(agencyList)) {
            throw new NodeMgrException(ConstantCode.CHAIN_WITH_NO_AGENCY_ERROR);
        }

        // get agency id set
        Set<Integer> agencyIdSet = agencyList.stream().map(agency -> agency.getId()).collect(Collectors.toSet());

        List<TbHost> hostList = this.tbHostMapper.selectByIp(ip);
        if (CollectionUtils.isEmpty(hostList) && StringUtils.isBlank(agencyName)){
            // new ip address, agency name cannot be blank
            throw new NodeMgrException(ConstantCode.AGENCY_NAME_EMPTY_ERROR);
        }

        // get agency id set from host
        Set<Integer> agencyIdSetFromHost = hostList.stream().map(host -> host.getAgencyId()).collect(Collectors.toSet());
        agencyIdSet.retainAll(agencyIdSetFromHost);

        if (CollectionUtils.size(agencyIdSet) != 1
                && StringUtils.isBlank(agencyName)){
            // new ip address, agency name cannot be blank
            throw new NodeMgrException(ConstantCode.AGENCY_NAME_EMPTY_ERROR);
        }

        if(CollectionUtils.isEmpty(agencyIdSet)){
            // new IP and new agency name

            // call shell to generate new agency config(private key and crt)

            // insert node to db
        }

        // generate nodes config by agency config

        // insert host into db

        // ssh host and start docker container

        // try catch, if error occurred, delete generated config files.

        return null;
    }
}

