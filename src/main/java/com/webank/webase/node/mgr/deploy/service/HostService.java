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

import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.node.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.node.mgr.base.enums.HostStatusEnum;
import com.webank.webase.node.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.chain.ChainService;
import com.webank.webase.node.mgr.deploy.entity.TbAgency;
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
 *
 */

@Log4j2
@Component
public class HostService {

    @Autowired private TbHostMapper tbHostMapper;
    @Autowired private TbChainMapper tbChainMapper;
    @Autowired private TbAgencyMapper tbAgencyMapper;
    @Autowired private FrontMapper frontMapper;

    @Autowired private ConstantProperties constant;
    @Autowired private ChainService chainService;
    @Autowired private DockerClientService dockerClientService;
    @Autowired private FrontService frontService;
    @Autowired private AgencyService agencyService;
    @Autowired private PathService pathService;
    @Autowired private ConfigService configService;
    @Autowired private DeployShellService deployShellService;
    @Qualifier(value = "deployAsyncExecutor")
    @Autowired private ThreadPoolTaskExecutor executor;

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateStatus(int hostId, HostStatusEnum newStatus) throws NodeMgrException {
        TbHost newHost = new TbHost();
        newHost.setId(hostId);
        newHost.setStatus(newStatus.getId());
        newHost.setModifyTime(new Date());
        return tbHostMapper.updateByPrimaryKeySelective(newHost) == 1;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public TbHost insert(int agencyId,
                         String agencyName,
                         String ip,
                         String rootDir) throws NodeMgrException {

        // fix call transaction in the same class
        return ((HostService) AopContext.currentProxy())
                .insert(agencyId, agencyName, ip, SSH_DEFAULT_USER, SSH_DEFAULT_PORT, rootDir, HostStatusEnum.ADDED);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public TbHost insert(int agencyId,
                         String agencyName,
                         String ip,
                         String sshUser,
                         int sshPort,
                         String rootDir,
                         HostStatusEnum hostStatusEnum) throws NodeMgrException {

        // TODO. params check

        TbHost host = TbHost.init(agencyId, agencyName, ip, sshUser, sshPort, rootDir, hostStatusEnum);

        if ( tbHostMapper.insertSelective(host) != 1 || host.getId() <= 0) {
            throw new NodeMgrException(ConstantCode.INSERT_HOST_ERROR);
        }
        return host;
    }

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

        // select all hosts by all agencies
        List<TbAgency> tbAgencyList = tbAgencyMapper.selectByChainId(tbChain.getId());
        if (CollectionUtils.isEmpty(tbAgencyList)) {
            log.error("Chain:[{}:{}] has no agency.", tbChain.getId(), tbChain.getChainName());
            return;
        }
        // select all host
        List<TbHost> tbHostList = tbAgencyList.stream()
            .map((agency) -> tbHostMapper.selectByAgencyId(agency.getId()))
            .filter((host) -> host != null)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tbHostList)) {
            log.error("Chain:[{}:{}] has no host.", tbChain.getId(), tbChain.getChainName());
            return;
        }

        // init host
        // 1. install docker and docker-compose,
        // 2. send node config to remote host
        // 3. docker pull image
        boolean isInitSuccess = this.initHosts(tbChain, tbHostList);
        if (!isInitSuccess) {
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
                this.updateStatus(tbHost.getId(), HostStatusEnum.INITIATING);
            }
            executor.submit(() -> {
                try {
                    // TODO. optimize code
                    // exec host init shell script
                    ExecuteResult result = deployShellService.execHostOperate(tbHost.getIp(), tbHost.getSshPort(), tbHost.getSshUser(),
                        PathService.getChainRootOnHost(tbHost.getRootDir(), tbChain.getChainName()));
                    if (!result.success()) {
                        log.error("Init host:[{}] error:[{}:{}] ", tbHost.getIp(), result.getExitCode(), result.getExecuteOut());
                        this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED);
                        return;
                    }

                    // TODO. check docker daemon, if check failed ,then return;

                    // init success
                    log.info("Init host:[{}], exec shell script success:[{}].", tbHost.getIp(), result.getExecuteOut());

                    // scp config files from
                    String src = String.format("%s/*", pathService.getHost(tbChain.getChainName(), tbHost.getIp()).toString());
                    String dst = PathService.getChainRootOnHost(tbHost.getRootDir(), tbChain.getChainName());

                    log.info("Send files from:[{}] to:[{}@{}#{}:{}].", src, tbHost.getSshUser(), tbHost.getIp(), tbHost.getSshPort(), dst);
                    ExecuteResult executeResult = deployShellService.scp(ScpTypeEnum.UP, tbHost.getSshUser(), tbHost.getIp(), tbHost.getSshPort(), src, dst);
                    log.info("Send files to host:[{}] result:[{}]", tbHost.getIp(), executeResult.getExecuteOut());
                    if (!executeResult.success()) {
                        log.error("Send files from [{}] to [{}:{}] filed.", src, tbHost.getIp(), dst);
                        this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED);
                    }
                    log.info("Send files from [{}] to [{}:{}] success.", src, tbHost.getIp(), dst);

                    // docker pull image
                    if (this.dockerClientService.pullImage(tbHost.getIp(), tbHost.getDockerPort(), tbChain.getVersion())) {
                        log.info("Host:[{}] docker pull image:[{}] success.", tbHost.getIp(), tbChain.getVersion());
                        // init success
                        if (this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_SUCCESS)) {
                            // init host success
                            log.info("Host:[{}] init, scp , docker pull success.", tbHost.getIp());
                            hostCount.decrementAndGet();
                        }
                    } else {
                        log.error("Host:[{}] docker pull image:[{}] failed.", tbHost.getIp(), tbChain.getVersion());
                        this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED);
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
     * destroy host when delete last node of host
     */
    public void destroyHost() {

    }

    /**
     *
     * @param chainId
     * @return
     */
    public List<TbHost> selectHostListByChainId(int chainId){
        // select all agencies by chainId
        List<TbAgency> tbAgencyList = this.agencyService.selectAgencyListByChainId(chainId);

        // select all hosts by all agencies
        List<TbHost> tbHostList = tbAgencyList.stream()
                .map((agency) -> tbHostMapper.selectByAgencyId(agency.getId()))
                .filter((host) -> host != null)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tbHostList)) {
            log.error("Chain:[{}] has no host.", chainId);
            return Collections.emptyList();
        }
        return tbHostList;
    }

    /**
     * Init a host, generate sdk files(crt files and node.[key,crt]) and insert into db.
     *
     *
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public TbHost initHost(
            byte encryptType,
            String chainName,
            String rootDirOnHost,
            String ip,
            int agencyId,
            String agencyName) throws NodeMgrException {
        // new host, generate sdk dir first
        Path sdkPath = this.pathService.getSdk(chainName, ip);

        // call shell to generate new node config(private key and crt)
        ExecuteResult executeResult = this.deployShellService.execGenNode(
                encryptType, chainName, agencyName, sdkPath.toAbsolutePath().toString());
        if (executeResult.failed()) {
             throw new NodeMgrException(ConstantCode.EXEC_GEN_SDK_ERROR);
        }

        // init sdk dir
        this.configService.initSdkDir(encryptType, sdkPath);

        // scp sdk to remote
        String src = String.format("%s", sdkPath.toAbsolutePath().toString());
        String dst = PathService.getChainRootOnHost(rootDirOnHost, chainName);

        log.info("Send files from:[{}] to:[{}@{}#{}:{}].", src, SSH_DEFAULT_USER, ip, SSH_DEFAULT_PORT, dst);
        executeResult = this.deployShellService.scp(ScpTypeEnum.UP, ip, src, dst);
        if (executeResult.failed()) {
            throw new NodeMgrException(ConstantCode.SEND_SDK_FILES_ERROR);
        }

        // insert host into db
        return ((HostService) AopContext.currentProxy()).insert(agencyId, agencyName, ip, chainName);
    }

}