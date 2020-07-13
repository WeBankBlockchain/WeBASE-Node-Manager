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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.HostStatusEnum;
import com.webank.webase.node.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NumberUtil;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.chain.ChainService;
import com.webank.webase.node.mgr.deploy.entity.NodeConfig;
import com.webank.webase.node.mgr.deploy.entity.TbAgency;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbHostMapper;
import com.webank.webase.node.mgr.deploy.service.docker.DockerOptions;
import com.webank.webase.node.mgr.front.FrontMapper;
import com.webank.webase.node.mgr.front.entity.TbFront;

import lombok.extern.log4j.Log4j2;

/**
 *
 */

@Log4j2
@Component
public class HostService {

    @Autowired private TbHostMapper tbHostMapper;
    @Autowired private FrontMapper frontMapper;

    @Autowired private ConstantProperties constant;
    @Autowired private DockerOptions dockerOptions;
    @Autowired private AgencyService agencyService;
    @Autowired private PathService pathService;
    @Autowired private DeployShellService deployShellService;

    @Qualifier(value = "deployAsyncScheduler")
    @Autowired private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateStatus(int hostId, HostStatusEnum newStatus) throws NodeMgrException {
        log.info("Change host status  to:[{}]",hostId, newStatus);
        TbHost newHost = new TbHost();
        newHost.setId(hostId);
        newHost.setStatus(newStatus.getId());
        newHost.setModifyTime(new Date());
        return tbHostMapper.updateByPrimaryKeySelective(newHost) == 1;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public TbHost insertIfNew(int agencyId,
                         String agencyName,
                         String ip,
                         String rootDir,
                         String sshUser,int sshPort,int dockerPort ) throws NodeMgrException {
        TbHost host = this.tbHostMapper.getByAgencyIdAndIp(agencyId,ip);
        if (host != null){
            return host;
        }

        // fix call transaction in the same class
        return ((HostService) AopContext.currentProxy())
                .insert(agencyId, agencyName, ip,sshUser,sshPort,rootDir,HostStatusEnum.ADDED, dockerPort);
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public TbHost insert(int agencyId, String agencyName, String ip, String sshUser, int sshPort,
                         String rootDir, HostStatusEnum hostStatusEnum, int dockerPort ) throws NodeMgrException {

        TbHost host = TbHost.init(agencyId, agencyName, ip, sshUser, sshPort, rootDir, hostStatusEnum,dockerPort);

        if ( tbHostMapper.insertSelective(host) != 1 || host.getId() <= 0) {
            throw new NodeMgrException(ConstantCode.INSERT_HOST_ERROR);
        }
        return host;
    }

    /**
     *
     * @param hostId
     * @return
     */
    @Transactional
    public TbHost changeHostStatusToInitiating(int hostId ){
        // check chain status
        TbHost host = null;
        synchronized (ChainService.class) {
            host = tbHostMapper.selectByPrimaryKey(hostId);
            if (host == null) {
                log.error("Host:[{}] does not exist.", hostId);
                return null;
            }
            // check host status
            if (HostStatusEnum.successOrInitiating(host.getStatus())) {
                log.error("Host:[{}:{}] is already init success or is initiating.", host.getIp(), host.getStatus());
                return null;
            }

            // update chain status
            log.info("Start to init host:[{}:{}] from status:[{}]", host.getId(), host.getIp(), host.getStatus());

            if (!((HostService) AopContext.currentProxy()).updateStatus(host.getId(), HostStatusEnum.INITIATING)) {
                log.error("Start to init host:[{}:{}], but update status to initiating failed.", host.getIp(), host.getStatus());
                return null;
            }
        }
        return host;
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
    public boolean initHostList(TbChain tbChain, List<TbHost> tbHostList, boolean scpNodeConfig) throws InterruptedException {
        log.info("Start init chain:[{}:{}] hosts:[{}].", tbChain.getId(), tbChain.getChainName(), CollectionUtils.size(tbHostList));

        final CountDownLatch initHostLatch = new CountDownLatch(CollectionUtils.size(tbHostList));
        // check success count
        AtomicInteger initSuccessCount = new AtomicInteger(0);
        for (final TbHost tbHost : tbHostList) {
            log.info("Init host:[{}] by exec shell script:[{}]", tbHost.getIp(), constant.getNodeOperateShell());

            // set host status
            ((HostService) AopContext.currentProxy()).changeHostStatusToInitiating(tbHost.getId());

            threadPoolTaskScheduler.submit(() -> {
                try {
                    // exec host init shell script
                    deployShellService.execHostOperate(tbHost.getIp(), tbHost.getSshPort(), tbHost.getSshUser(),
                        PathService.getChainRootOnHost(tbHost.getRootDir(), tbChain.getChainName()));

                    if(scpNodeConfig) {
                        // scp config files from local to remote
                        // local: NODES_ROOT/[chainName]/[ip] TO remote: /opt/fisco/[chainName]
                        String src = String.format("%s/*", pathService.getHost(tbChain.getChainName(), tbHost.getIp()).toString());
                        String dst = PathService.getChainRootOnHost(tbHost.getRootDir(), tbChain.getChainName());
                        deployShellService.scp(ScpTypeEnum.UP,tbHost.getSshUser(), tbHost.getIp(),tbHost.getSshPort(), src, dst);
                        log.info("Send files from:[{}] to:[{}@{}#{}:{}] success.",
                                src, tbHost.getSshUser(), tbHost.getIp(), tbHost.getSshPort(), dst);
                    }

                    // docker pull image
                    this.dockerOptions.pullImage(tbHost.getIp(), tbHost.getDockerPort(), tbHost.getSshUser(),tbHost.getSshPort(), tbChain.getVersion());

                    // update host status
                    this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_SUCCESS) ;
                    initSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Init host:[{}] error", tbHost.getIp(), e);
                    this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED);
                } finally {
                    initHostLatch.countDown();
                }
            });
        }

        initHostLatch.await(constant.getExecHostInitTimeout(), TimeUnit.MILLISECONDS);

        boolean initSuccess = initSuccessCount.get() == CollectionUtils.size(tbHostList);
        // check if all host init success
        log.log(initSuccess ? Level.INFO: Level.ERROR,
                "Host of chain:[{}] init result, total:[{}], success:[{}]",
                tbChain.getChainName(), initSuccessCount.get(),initSuccessCount.get());

        return initSuccess;
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
    public TbHost generateHostSDKAndScp(
            byte encryptType, String chainName, String rootDirOnHost,
            String ip, int agencyId, String agencyName,String sshUser,
            int sshPort, int dockerPort) throws NodeMgrException {
        // new host, generate sdk dir first
        Path sdkPath = this.pathService.getSdk(chainName, ip);

        if (Files.exists(sdkPath)){
            // TODO.
            log.warn("Exists sdk dir of host:[{}:{}], delete first.", ip,sdkPath.toAbsolutePath().toAbsolutePath());
            try {
                FileUtils.deleteDirectory(sdkPath.toFile());
            } catch (IOException e) {
                throw new NodeMgrException(ConstantCode.DELETE_OLD_SDK_DIR_ERROR);
            }
        }

        // call shell to generate new node config(private key and crt)
        ExecuteResult executeResult = this.deployShellService.execGenNode(
                encryptType, chainName, agencyName, sdkPath.toAbsolutePath().toString());
        if (executeResult.failed()) {
             throw new NodeMgrException(ConstantCode.EXEC_GEN_SDK_ERROR);
        }

        // init sdk dir
        NodeConfig.initSdkDir(encryptType, sdkPath);

        // scp sdk to remote
        String src = String.format("%s", sdkPath.toAbsolutePath().toString());
        String dst = PathService.getChainRootOnHost(rootDirOnHost, chainName);

        log.info("Send files from:[{}] to:[{}@{}#{}:{}].", src, sshUser, ip, sshPort, dst);
        this.deployShellService.scp(ScpTypeEnum.UP, sshUser, ip, sshPort, src, dst);

        // insert host into db
        return ((HostService) AopContext.currentProxy())
                .insert(agencyId, agencyName, ip,sshUser,sshPort, rootDirOnHost,HostStatusEnum.ADDED, dockerPort);
    }

    /**
     *
     * @param deleteHost
     * @param hostId
     */
    @Transactional
    public void deleteHostWithNoNode(boolean deleteHost, int hostId){
        TbHost host = this.tbHostMapper.selectByPrimaryKey(hostId);
        if (host == null){
            log.warn("Host:[{}] not exists.",hostId);
            return;
        }

        List<TbFront> frontList = this.frontMapper.selectByHostId(hostId);
        if (CollectionUtils.isEmpty(frontList)
                && deleteHost) {
            this.tbHostMapper.deleteByPrimaryKey(hostId);
            log.warn("Delete host:[{}].", hostId);
        }
    }

    /**
     *
     * @param agencyId
     */
    @Transactional
    public void deleteHostByAgencyId(String chainName, int agencyId){
        List<TbHost> hostList = this.tbHostMapper.selectByAgencyId(agencyId);
        if(CollectionUtils.isNotEmpty(hostList)){
            for (TbHost host : hostList) {
                // move chain config files
                ChainService.mvChainOnRemote(host.getIp(),host.getRootDir(),chainName,host.getSshUser(),
                        host.getSshPort(),constant.getPrivateKey());
            }
        }

        // delete host in batch
        log.info("Delete host data by agency id:[{}].", agencyId);
        this.tbHostMapper.deleteByAgencyId(agencyId);
    }

    /**
     *
     * @param chainId
     */
    public int hostProgress(int chainId){
        // check host init
        int hostFinishCount = 0;
        List<TbHost> hostList = this.selectHostListByChainId(chainId);
        if (CollectionUtils.isEmpty(hostList)) {
            return NumberUtil.PERCENTAGE_FINISH;
        }
        for (TbHost host : hostList) {
            HostStatusEnum hostStatusEnum = HostStatusEnum.getById(host.getStatus());
            switch (hostStatusEnum){
                case INIT_FAILED:
                    return NumberUtil.PERCENTAGE_FAILED;
                case INIT_SUCCESS:
                    hostFinishCount ++;
                    break;
                default:
                    break;
            }
        }
        // check host init finish ?
        if (hostFinishCount == hostList.size()){
            // init success
            return NumberUtil.PERCENTAGE_FINISH;
        }
        return NumberUtil.percentage(hostFinishCount,hostList.size());
    }
}