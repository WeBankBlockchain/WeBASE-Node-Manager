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

import static com.webank.webase.node.mgr.base.properties.ConstantProperties.SSH_DEFAULT_PORT;
import static com.webank.webase.node.mgr.base.properties.ConstantProperties.SSH_DEFAULT_USER;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.node.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.node.mgr.base.enums.HostStatusEnum;
import com.webank.webase.node.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.ValidateUtil;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.base.tools.cmd.JavaCommandExecutor;
import com.webank.webase.node.mgr.chain.ChainService;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbAgencyMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbChainMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbHostMapper;
import com.webank.webase.node.mgr.front.FrontMapper;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.TbFront;

import lombok.extern.log4j.Log4j2;

/**
 * Java call shell script and system command.
 */
@Log4j2
@Component
public class DeployShellService {

    @Autowired private PathService pathService;
    @Autowired private ConstantProperties constant;
    @Autowired private TbChainMapper tbChainMapper;
    @Autowired private TbHostMapper tbHostMapper;
    @Autowired private TbAgencyMapper tbAgencyMapper;
    @Autowired private HostService hostService;
    @Autowired private FrontMapper frontMapper;
    @Autowired private FrontService frontService;
    @Autowired private ChainService chainService;
    @Autowired private DockerClientService dockerClientService;

    @Qualifier(value = "deployAsyncExecutor")
    @Autowired private ThreadPoolTaskExecutor executor;

    /**
     * TODO:  1. change to status machine; 2. update synchronized object
     *
     * @param chainName
     */
    @Async("deployAsyncExecutor")
    public void initHostList(String chainName) {
        if (StringUtils.isBlank(chainName)) {
            log.error("Chain name:[{}] is blank, deploy error.", chainName);
            return;
        }

        // check chain status
        TbChain tbChain = null;
        synchronized (DeployShellService.class) {
            tbChain = this.tbChainMapper.getByChainName(chainName);
            // chain not exists
            if (tbChain == null) {
                log.error("Chain:[{}] does not exist, deploy error.", chainName);
                return;
            }

            // check chain status
            // TODO. deploying but no thread is deploying, still need to deploy
            if (ChainStatusEnum.successOrDeploying(tbChain.getChainStatus())) {
                log.error("Chain:[{}] is already deployed success or deploying:[{}].", chainName, tbChain.getChainStatus());
                return;
            }

            // update chain status
            log.info("Start to deploy chain:[{}:{}] from status:[{}]",
                    tbChain.getId(),
                    tbChain.getChainName(),
                    tbChain.getChainStatus());
            if (!chainService.updateStatus(tbChain.getId(), ChainStatusEnum.DEPLOYING)) {
                log.error("Start to deploy chain:[{}:{}], but update status to deploying failed.", tbChain.getId(), tbChain.getChainName());
                return;
            }
        }

        // select host list of chain
        List<TbHost> tbHostList = this.hostService.selectHostListByChainId(tbChain.getId());

        if (CollectionUtils.isEmpty(tbHostList)) {
            log.error("Chain:[{}:{}] has no host.", tbChain.getId(), tbChain.getChainName());
            return;
        }

        // init host
        // 1. install docker and docker-compose,
        // 2. send node config to remote host
        // 3. docker pull image
        if (!this.initHosts(tbChain, tbHostList)) {
            // init host failed
            log.error("Chain:[{}:{}] has no host.", tbChain.getId(), tbChain.getChainName());
            return;
        }

        log.info("Start docker containers.");
        final CountDownLatch dockerStartLatch = new CountDownLatch(CollectionUtils.size(tbHostList));
        AtomicInteger startCount = new AtomicInteger(0);
        for (TbHost tbHost : CollectionUtils.emptyIfNull(tbHostList)) {
            log.info("try to start bcos-front on host:[{}]", tbHost.getIp());
            executor.submit(() -> {
                try {
                    List<TbFront> tbFrontList = frontMapper.selectByHostId(tbHost.getId());
                    startCount.addAndGet(CollectionUtils.size(tbFrontList));
                    for (TbFront tbFront : CollectionUtils.emptyIfNull(tbFrontList)) {
                        try {
                            // TODO. check front status

                            log.info("try to create and start bcos-front:[{}:{}] container",
                                    tbFront.getFrontIp(), tbFront.getHostIndex());
                            boolean startResult = dockerClientService.createAndStart(tbHost.getIp(),
                                    tbHost.getDockerPort(),
                                    tbFront.getImageTag(),
                                    tbFront.getContainerName(),
                                    PathService.getChainRootOnHost(tbHost.getRootDir(), chainName),
                                    tbFront.getHostIndex());

                            log.info("Start docker container:[{}:{}] result:[{}] on host:[{}]",
                                    tbFront.getContainerName(), tbFront.getHostIndex(), startResult, tbHost.getIp());
                            if (startResult) {
                                if (this.frontService.updateStatus(tbFront.getFrontId(), FrontStatusEnum.RUNNING)) {
                                    log.info("Start docker container:[{}:{}] success on host:[{}]",
                                            tbFront.getContainerName(), tbFront.getHostIndex(), tbHost.getIp());
                                    startCount.decrementAndGet();
                                }
                            } else {
                                log.info("Start docker container:[{}:{}] failed on host:[{}]",
                                        tbFront.getContainerName(), tbFront.getHostIndex(), tbHost.getIp());
                            }
                        } catch (Exception e) {
                            log.error("Start bcos-front error:[{}:{}]", tbFront.getFrontIp(), tbFront.getContainerName(), e);
                        }
                    }
                } catch (Exception e) {
                    log.error("Start bcos-front on host:[{}] error.", tbHost.getIp(), e);
                } finally {
                    dockerStartLatch.countDown();
                }
            });
        }

        // start success
        try {
            dockerStartLatch.await(20, TimeUnit.MINUTES);
            // check if all host init success
            if (startCount.get() == 0) {
                log.info("All bcos-front of chain:[{}] start success.", startCount.get(), tbChain.getChainName());
                chainService.updateStatus(tbChain.getId(), ChainStatusEnum.DEPLOY_SUCCESS);
            } else {
                log.error("[{}] bcos-front of chain:[{}] start failed.", startCount.get(), tbChain.getChainName());
                chainService.updateStatus(tbChain.getId(), ChainStatusEnum.DEPLOY_FAILED);
            }
        } catch (InterruptedException e) {
            chainService.updateStatus(tbChain.getId(), ChainStatusEnum.DEPLOY_FAILED);
            log.error("CountDownLatch wait for all bcos-front:[{}] of chain:[{}] start timeout.", dockerStartLatch.getCount(), chainName);
        }
    }

    /**
     * Init hosts:
     * 1. Install docker and docker-compose;
     * 2. Send node config to remote hosts;
     * 3. docker pull image;
     *
     * @param tbChain
     * @param tbHostList
     * @return
     */
    public boolean initHosts(TbChain tbChain, List<TbHost> tbHostList) {
        if (tbChain == null) {
            log.info("Init chain hosts error, chain null.");
            return false;
        }
        if (CollectionUtils.isEmpty(tbHostList)) {
            log.info("Init chain:[{}:{}] hosts error, empty host list.", tbChain.getId(), tbChain.getChainName());
            return false;
        }

        log.info("Start init chain:[{}:{}] hosts.", tbChain.getId(), tbChain.getChainName());

        final CountDownLatch initHostLatch = new CountDownLatch(CollectionUtils.size(tbHostList));
        // check success count
        AtomicInteger hostCount = new AtomicInteger(CollectionUtils.size(tbHostList));
        for (final TbHost tbHost : tbHostList) {
            log.info("Init host:[{}] by exec shell script:[{}]", tbHost.getIp(), constant.getNodeOperateShell());

            // check host status
            synchronized (DeployShellService.class) {
                TbHost currentTbHost = tbHostMapper.selectByPrimaryKey(tbHost.getId());
                // check host status
                if (HostStatusEnum.successOrInitiating(currentTbHost.getStatus())) {
                    log.error("Host:[{}] is already init success or is initiating.", tbHost, tbChain.getChainStatus());
                    continue;
                }
                hostService.updateStatus(tbHost.getId(), HostStatusEnum.INITIATING);
            }
            executor.submit(() -> {
                try {
                    // TODO. optimize code
                    // exec host init shell script
                    ExecuteResult result = this.execHostOperate(tbHost.getIp(), tbHost.getSshPort(), tbHost.getSshUser(),
                            PathService.getChainRootOnHost(tbHost.getRootDir(), tbChain.getChainName()));
                    if (!result.success()) {
                        log.error("Init host:[{}] error:[{}:{}] ", tbHost.getIp(), result.getExitCode(), result.getExecuteOut());
                        this.hostService.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED);
                        return;
                    }

                    // TODO. check docker daemon, if check failed ,then return;

                    // init success
                    log.info("Init host:[{}], exec shell script success:[{}].", tbHost.getIp(), result.getExecuteOut());

                    // scp config files
                    String src = String.format("%s/*", pathService.getHost(tbChain.getChainName(), tbHost.getIp()).toString());
                    String dst = PathService.getChainRootOnHost(tbHost.getRootDir(), tbChain.getChainName());

                    log.info("Send files from:[{}] to:[{}@{}#{}:{}].", src, tbHost.getSshUser(), tbHost.getIp(), tbHost.getSshPort(), dst);
                    ExecuteResult executeResult = this.scp(ScpTypeEnum.UP, tbHost.getSshUser(), tbHost.getIp(), tbHost.getSshPort(), src, dst);
                    log.info("Send files to host:[{}] result:[{}]", tbHost.getIp(), executeResult.getExecuteOut());
                    if (!executeResult.success()) {
                        log.error("Send files from [{}] to [{}:{}] filed.", src, tbHost.getIp(), dst);
                        this.hostService.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED);
                    }
                    log.info("Send files from [{}] to [{}:{}] success.", src, tbHost.getIp(), dst);

                    // docker pull image
                    if (this.dockerClientService.pullImage(tbHost.getIp(), tbHost.getDockerPort(), tbChain.getVersion())) {
                        log.info("Host:[{}] docker pull image:[{}] success.", tbHost.getIp(), tbChain.getVersion());
                        // init success
                        if (this.hostService.updateStatus(tbHost.getId(), HostStatusEnum.INIT_SUCCESS)) {
                            // init host success
                            log.info("Host:[{}] init, scp , docker pull success.", tbHost.getIp());
                            hostCount.decrementAndGet();
                        }
                    } else {
                        log.error("Host:[{}] docker pull image:[{}] failed.", tbHost.getIp(), tbChain.getVersion());
                        this.hostService.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED);
                    }

                } catch (Exception e) {
                    log.error("Init host:[{}] error", tbHost.getIp(), e);
                } finally {
                    initHostLatch.countDown();
                }
            });
        }

        // waite for all host init finish
        try {
            initHostLatch.await(constant.getExecHostInitTimeout(), TimeUnit.MILLISECONDS);
            // check if all host init success
            if (hostCount.get() == 0) {
                log.info("All hosts of chain:[{}] init success.", tbChain.getChainName());
                return true;
            } else {
                log.error("[{}] hosts of chain:[{}] init failed .", hostCount.get(), tbChain.getChainName());
            }
        } catch (InterruptedException e) {
            chainService.updateStatus(tbChain.getId(), ChainStatusEnum.DEPLOY_FAILED);
            log.error("CountDownLatch wait for all hosts:[{}] init timeout.", initHostLatch.getCount());
        }
        return false;
    }

    /**
     *
     * @param typeEnum
     * @param ip
     * @param src
     * @param dst
     * @return
     */
    public ExecuteResult scp(ScpTypeEnum typeEnum, String ip,  String src, String dst) {
        String command = String.format("bash -x -e %s -t %s -i %s -u %s -p %s -s %s -d %s",
                constant.getScpShell(), typeEnum.getValue(), ip, SSH_DEFAULT_USER, SSH_DEFAULT_PORT, src, dst);
        log.info("exec file send command: [{}]", command);
        return JavaCommandExecutor.executeCommand(command, constant.getExecHostInitTimeout());
    }

    /**
     * @param typeEnum
     * @param user
     * @param ip
     * @param port
     * @param src
     * @param dst
     * @return
     */
    public ExecuteResult scp(ScpTypeEnum typeEnum, String user, String ip, int port, String src, String dst) {
        String command = String.format("bash -x -e %s -t %s -i %s -u %s -p %s -s %s -d %s",
                constant.getScpShell(), typeEnum.getValue(), ip, user, port, src, dst);
        log.info("exec file send command: [{}]", command);
        return JavaCommandExecutor.executeCommand(command, constant.getExecHostInitTimeout());
    }


    /**
     * @param ip        Required.
     * @param port      Default 22.
     * @param user      Default root.
     * @param chainRoot chain root on host, default is /opt/fisco/{chain_name}.
     * @return
     */
    public ExecuteResult execHostOperate(String ip, int port, String user, String chainRoot) {
        return this.execHostOperate(ip, port, user, "", chainRoot);
    }

    /**
     * @param ip        Required.
     * @param port      Default 22.
     * @param user      Default root.
     * @param pwd       Not required.
     * @param chainRoot chain root on host, default is /opt/fisco/{chain_name}.
     * @return
     */
    public ExecuteResult execHostOperate(String ip, int port, String user, String pwd, String chainRoot) {
        log.info("Exec execHostOperate method for [{}@{}:{}#{}]", user, ip, port, pwd);
        if (!ValidateUtil.ipv4Valid(ip)) {
            log.error("Exec execHostOperate method ERROR: IP:[{}] error", ip);
            return null;
        }

        int newport = port <= 0 || port > 65535 ? SSH_DEFAULT_PORT : port;
        String newuser = StringUtils.isBlank(user) ? SSH_DEFAULT_USER : user;

        String command = String.format("bash -x -e %s -H %s -P %s -u %s %s %s", constant.getNodeOperateShell(),
                ip, newport, newuser,
                StringUtils.isBlank(pwd) ? "" : String.format(" -p %s ", pwd),
                StringUtils.isBlank(chainRoot) ? "" : String.format(" -n %s ", chainRoot)
        );

        return JavaCommandExecutor.executeCommand(command, constant.getExecHostInitTimeout());
    }

    /**
     * TODO. if nodes config dir already exists, delete or backup first ?
     * TODO. separate two steps: save config files local, and exec build_chain
     *
     * @param encryptType
     * @param ipLines
     * @return
     */
    public ExecuteResult execBuildChain(byte encryptType,
                                        String[] ipLines,
                                        String chainName) {
        Path ipConf = pathService.getIpConfig(chainName);
        log.info("Exec execBuildChain method for [{}], chainName:[{}], ipConfig:[{}]",
                JSON.toJSONString(ipLines), chainName, ipConf.toString());
        try {
            if (!Files.exists(ipConf.getParent())) {
                Files.createDirectories(ipConf.getParent());
            }
            Files.write(ipConf, Arrays.asList(ipLines), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new NodeMgrException(ConstantCode.SAVE_IP_CONFIG_FILE_ERROR);
        }

        if (Files.notExists(ipConf)) {
            // file not exists
            log.error("File: [{}] not exists in directory:[{}] ", ipConf, Paths.get(".").toAbsolutePath().toString());
            throw new NodeMgrException(ConstantCode.NO_CONFIG_FILE_ERROR);
        }
        // build_chain.sh only support docker on linux
        String command = String.format("bash -e %s -f %s -o %s %s %s %s",
                // build_chain.sh shell script
                constant.getBuildChainShell(),
                // ipconf file path
                ipConf.toString(),
                // output path
                pathService.getChainRootString(chainName),
                // guomi or standard
                encryptType == EncryptType.SM2_TYPE ? "-g" : "",
                // only linux supports docker model
                SystemUtils.IS_OS_LINUX ? " -d " : "",
                // use binary local
                StringUtils.isBlank(constant.getFiscoBcosBinary()) ? "" :
                        String.format(" -e %s ", constant.getFiscoBcosBinary())
        );

        return JavaCommandExecutor.executeCommand(command, constant.getExecBuildChainTimeout());
    }

    /**
     * TODO. check agency cert dir, put to temp directory first
     *
     * @param encryptType
     * @param chainName
     * @param newAgencyName
     * @return
     */
    public ExecuteResult execGenAgency(byte encryptType,
                                       String chainName,
                                       String newAgencyName) {
        log.info("Exec execGenAgency method for chainName:[{}], newAgencyName:[{}:{}]", chainName, newAgencyName, encryptType);

        Path certRoot = this.pathService.getCertRoot(chainName);

        if (Files.notExists(certRoot)) {
            // file not exists
            log.error("Chain cert : [{}] not exists in directory:[{}] ", chainName, Paths.get(".").toAbsolutePath().toString());
            throw new NodeMgrException(ConstantCode.CHAIN_CERT_NOT_EXISTS_ERROR);
        }

        // build_chain.sh only support docker on linux
        String command = String.format("bash -e %s -c %s -a %s %s",
                // gen_agency_cert.sh shell script
                constant.getGenAgencyShell(),
                // chain cert dir
                certRoot.toAbsolutePath().toString(),
                // new agency name
                newAgencyName,
                encryptType == EncryptType.SM2_TYPE ?
                        String.format(" -g %s", pathService.getGmCertRoot(chainName).toAbsolutePath().toString())
                        : ""
        );

        return JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
    }


    /**
     * TODO. check agency cert dir, put to temp directory first
     *
     * @param encryptType
     * @param chainName
     * @param agencyName
     * @param newNodeRoot
     * @return
     */
    public ExecuteResult execGenNode(byte encryptType,
                                     String chainName,
                                     String agencyName,
                                     String newNodeRoot) {
        log.info("Exec execGenNode method for chainName:[{}], " +
                "node:[{}:{}:{}]", chainName, encryptType, agencyName, newNodeRoot);

        Path agencyRoot = this.pathService.getAgencyRoot(chainName,agencyName);

        // build_chain.sh only support docker on linux
        String command = String.format("bash -e %s -c %s -o %s %s",
                // gen_node_cert.sh shell script
                constant.getGenNodeShell(),
                // agency cert root
                agencyRoot.toAbsolutePath().toString(),
                // new node dir
                newNodeRoot,
                encryptType == EncryptType.SM2_TYPE ?
                        String.format(" -g %s", pathService.getGmAgencyRoot(chainName,agencyName).toAbsolutePath().toString()) : ""
        );

        return JavaCommandExecutor.executeCommand(command, constant.getExecShellTimeout());
    }
}