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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
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
    public boolean updateStatus(int hostId, HostStatusEnum newStatus,String remark) throws NodeMgrException {
        log.info("Change host status to:[{}:{}:{}]",hostId, newStatus, remark);
        return tbHostMapper.updateHostStatus(hostId,new Date(), newStatus.getId(),remark) == 1;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public TbHost insertIfNew(int agencyId,
                         String agencyName,
                         String ip,
                         String rootDir,
                         String sshUser,int sshPort,int dockerPort ,String remark) throws NodeMgrException {
        TbHost host = this.tbHostMapper.getByAgencyIdAndIp(agencyId,ip);
        if (host != null){
            return host;
        }

        // fix call transaction in the same class
        return ((HostService) AopContext.currentProxy())
                .insert(agencyId, agencyName, ip,sshUser,sshPort,rootDir,HostStatusEnum.ADDED, dockerPort,remark);
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public TbHost insert(int agencyId, String agencyName, String ip, String sshUser, int sshPort,
                         String rootDir, HostStatusEnum hostStatusEnum, int dockerPort,String remark) throws NodeMgrException {

        TbHost host = TbHost.init(agencyId, agencyName, ip, sshUser, sshPort, rootDir, hostStatusEnum,dockerPort,remark);

        if ( tbHostMapper.insertSelective(host) != 1 || host.getId() <= 0) {
            throw new NodeMgrException(ConstantCode.INSERT_HOST_ERROR);
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
        Map<TbHost, Future> taskMap = new HashedMap<>();
        for (final TbHost tbHost : tbHostList) {
            log.info("Init host:[{}], status:[{}]", tbHost.getIp(), tbHost.getStatus());

            Future<?> task = threadPoolTaskScheduler.submit(() -> {
                try {
                    // check if host init shell script executed
                    if (tbHost.getStatus() != HostStatusEnum.INIT_SUCCESS.getId()){
                        boolean success = this.updateStatus(tbHost.getId(), HostStatusEnum.INITIATING, "Initiating...");
                        if (success){
                            log.info("Init host:[{}] by exec shell script:[{}]", tbHost.getIp(), constant.getNodeOperateShell());

                            // exec host init shell script
                            try {
                                deployShellService.execHostOperate(tbHost.getIp(), tbHost.getSshPort(), tbHost.getSshUser(),
                                        PathService.getChainRootOnHost(tbHost.getRootDir(), tbChain.getChainName()));
                            } catch (Exception e) {
                                log.error("Exec host init shell script on host:[{}] failed", tbHost.getIp(), e);
                                this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED,
                                        "Execute host init shell script failed, please check the host's network.");
                                return ;
                            }

                        }
                    }

                    if(scpNodeConfig) {
                        // scp config files from local to remote
                        // local: NODES_ROOT/[chainName]/[ip] TO remote: /opt/fisco/[chainName]
                        String src = String.format("%s/*", pathService.getHost(tbChain.getChainName(), tbHost.getIp()).toString());
                        String dst = PathService.getChainRootOnHost(tbHost.getRootDir(), tbChain.getChainName());
                        try {
                            deployShellService.scp(ScpTypeEnum.UP,tbHost.getSshUser(), tbHost.getIp(),tbHost.getSshPort(), src, dst);
                            log.info("Send files from:[{}] to:[{}@{}#{}:{}] success.",
                                    src, tbHost.getSshUser(), tbHost.getIp(), tbHost.getSshPort(), dst);
                        } catch (Exception e) {
                            log.error("Send file to host :[{}] failed", tbHost.getIp(), e);
                            this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED,
                                    "Scp configuration files to host failed, please check the host's network or disk usage.");
                            return ;
                        }

                    }

                    // docker pull image
                    try {
                        this.dockerOptions.pullImage(tbHost.getIp(), tbHost.getDockerPort(), tbHost.getSshUser(),tbHost.getSshPort(), tbChain.getVersion());
                    } catch (Exception e) {
                        log.error("Docker pull image on host :[{}] failed", tbHost.getIp(), e);
                        this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED,
                                "Docker pull image failed, please check the host's network or configuration of Docker.");
                        return;
                    }

                    // update host status
                    tbHost.setStatus(HostStatusEnum.INIT_SUCCESS.getId());
                    this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_SUCCESS,"") ;
                    initSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Init host:[{}] with unknown error", tbHost.getIp(), e);
                    this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED, "Init host with unknown error, check from log files.");
                } finally {
                    initHostLatch.countDown();
                }
            });
            taskMap.put(tbHost, task);
        }

        initHostLatch.await(constant.getExecHostInitTimeout(), TimeUnit.MILLISECONDS);
        log.error("Init host timeout, cancel unfinished tasks.");
        taskMap.entrySet().forEach((entry)->{
            TbHost tbHost = entry.getKey();
            Future<?> task = entry.getValue();
            if(! task.isDone()){
                log.error("Init host:[{}] timeout, cancel the task.", tbHost.getIp() );
                this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED, "Init host timeout.");
                task.cancel(false);
            }
        });

        boolean hostInitSuccess = initSuccessCount.get() == CollectionUtils.size(tbHostList);
        // check if all host init success
        log.log(hostInitSuccess ? Level.INFO: Level.ERROR,
                "Host of chain:[{}] init result, total:[{}], success:[{}]",
                tbChain.getChainName(), CollectionUtils.size(tbHostList),initSuccessCount.get());

        return hostInitSuccess;
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
                .insert(agencyId, agencyName, ip,sshUser,sshPort, rootDirOnHost,HostStatusEnum.ADDED, dockerPort,"");
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

    /**
     *
     * @param ipSet
     * @param sshUser
     * @param sshPort
     * @param imageTag
     */
    public void checkImageExists(Set<String> ipSet,String sshUser,int sshPort, String imageTag){
        for (String ip : ipSet) {
            boolean exists = this.dockerOptions.checkImageExists(ip, constant.getDockerDaemonPort(), sshUser, sshPort,imageTag);
            if (!exists){
                log.error("Docker image:[{}] not exists on host:[{}].", imageTag, ip);
                throw new NodeMgrException(ConstantCode.IMAGE_NOT_EXISTS_ON_HOST.attach(ip));
            }
        }
    }
}