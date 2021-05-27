/**
 * Copyright 2014-2021 the original author or authors.
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

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.HostStatusEnum;
import com.webank.webase.node.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.exception.ParamException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.IPUtil;
import com.webank.webase.node.mgr.base.tools.NumberUtil;
import com.webank.webase.node.mgr.base.tools.ProgressTools;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.chain.ChainService;
import com.webank.webase.node.mgr.deploy.entity.DeployNodeInfo;
import com.webank.webase.node.mgr.deploy.entity.IpConfigParse;
import com.webank.webase.node.mgr.deploy.entity.NodeConfig;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbHostMapper;
import com.webank.webase.node.mgr.front.FrontMapper;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.TbFront;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
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

/**
 * added host, check->generate->init+scp
 *  a. check docker and cpu/mem(host_init shell)
 *  b. gene config locally,
 *  c. init host and scp(pull docker and scp config)
 */

@Log4j2
@Component
public class HostService {

    @Autowired private TbHostMapper tbHostMapper;
    @Autowired private FrontMapper frontMapper;

    @Autowired private ConstantProperties constant;
    @Autowired private DockerCommandService dockerOptions;
    @Autowired private PathService pathService;
    @Autowired private DeployShellService deployShellService;
    @Autowired private AnsibleService ansibleService;
    @Autowired private FrontService frontService;
    @Autowired private ChainService chainService;
    @Autowired
    private ConfigService configService;

    @Qualifier(value = "deployAsyncScheduler")
    @Autowired private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateStatus(int hostId, HostStatusEnum newStatus, String remark) throws NodeMgrException {
        log.info("Change host status to:[{}:{}:{}]", hostId, newStatus, remark);
        return tbHostMapper.updateHostStatus(hostId, new Date(), newStatus.getId(), remark) == 1;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public TbHost checkDirAndInsert(String ip, String rootDir, HostStatusEnum hostStatusEnum, String remark)
        throws NodeMgrException {

        TbHost host = this.tbHostMapper.getByIp(ip);
        if (host != null){
            log.error("host already exist ip:{}", ip);
            throw new NodeMgrException(ConstantCode.HOST_ALREADY_EXIST);
        }
        // check before add
        log.info("check host ip accessible:{}", ip);
        ansibleService.execPing(ip);
        // check 127.0.0.1
        this.validateAllLocalhostOrNot(ip);
        log.info("check host root dir accessible:{}", rootDir);
        ExecuteResult execResult = ansibleService.execCreateDir(ip, rootDir);
        if (execResult.failed()) {
            log.error("host create rootDir:{} failed", rootDir);
            throw new NodeMgrException(ConstantCode.HOST_ROOT_DIR_ACCESS_DENIED);
        }

        // fix call transaction in the same class
        return ((HostService) AopContext.currentProxy())
                .insert(ip, rootDir, hostStatusEnum, remark);
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public TbHost insert(String ip, String rootDir, HostStatusEnum hostStatusEnum, String remark) throws NodeMgrException {
        log.info("start checkDirAndInsert ip:{}, rootDir:{}, hostStatusEnum:{}, remark:{}", ip, rootDir, hostStatusEnum, remark);
        TbHost host = TbHost.init(ip, rootDir, hostStatusEnum, remark);

        if ( tbHostMapper.insertSelective(host) != 1 || host.getId() <= 0) {
            throw new NodeMgrException(ConstantCode.INSERT_HOST_ERROR);
        }
        return host;
    }


    /**
     * do host_init(host_opearate) after generate_config and host_check and docker_check
     *
     * C step: Init hosts and scp
     * 1. check chain's port(channel,p2p,rpc)
     * 2. pull image and load image
     * 3. Send node config to remote hosts;
     * @param chainName
     * @param hostIdList
     * @param imagePullType default 2-pull from cdn
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean  initHostAndDocker(String chainName, String imageTag, List<Integer> hostIdList, int imagePullType) {
        List<TbHost> tbHostList = this.selectDistinctHostListById(hostIdList);

        log.info("Start initHostAndDocker chain:[{}] hosts:[{}].", chainName, CollectionUtils.size(tbHostList));
        final CountDownLatch initHostLatch = new CountDownLatch(CollectionUtils.size(tbHostList));
        // check success count
        AtomicInteger initSuccessCount = new AtomicInteger(0);

        ProgressTools.setHostInit();
        for (final TbHost tbHost : tbHostList) {
            log.info("initHostAndDocker host:[{}], status:[{}]", tbHost.getIp(), tbHost.getStatus());

            Future<?> task = threadPoolTaskScheduler.submit(() -> {
                try {
                    // check if host init shell script executed
                    if (!HostStatusEnum.successOrInitiating(tbHost.getStatus())) {

                        boolean success = this.updateStatus(tbHost.getId(), HostStatusEnum.INITIATING, "Initiating...");
                        if (success) {
                            log.info("initHostAndDocker host:[{}] by exec shell script:[{}]",
                                tbHost.getIp(), constant.getHostInitShell());

                            // exec host init shell script
                            try {
                                ansibleService.execHostInit(tbHost.getIp(), PathService.getChainRootOnHost(tbHost.getRootDir(), chainName));
                            }  catch (NodeMgrException e) {
                                log.error("Exec initHostAndDocker on host:[{}] failed",
                                    tbHost.getIp(), e);
                                this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED,
                                    e.getRetCode().getAttachment());
                                return;
                            } catch (Exception e) {
                                log.error("Exec initHostAndDocker on host:[{}] failed", tbHost.getIp(), e);
                                this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED,
                                        "Execute initHostAndDocker failed, please check the host's network.");
                                return ;
                            }

                        }

                        // docker pull image(ansible already check exist before pull)
                        try {
                            log.info("initHostAndDocker pull docker ip:{}, imageTag:{}, imagePullType:{}",
                                tbHost.getIp(), imageTag, imagePullType);
                            ProgressTools.setPullDocker();
                            this.dockerOptions.pullImage(tbHost.getIp(), imageTag, imagePullType, tbHost.getRootDir());
                        } catch (NodeMgrException e) {
                            log.error("Docker pull image on host:[{}] failed",
                                tbHost.getIp(), e);
                            this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED,
                                e.getRetCode().getAttachment());
                            return;
                        } catch (Exception e) {
                            log.error("Docker pull image on host :[{}] failed", tbHost.getIp(), e);
                            this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED,
                                "Docker pull image failed, please check the host's network or configuration of Docker.");
                            return;
                        }
                    }

                    tbHost.setStatus(HostStatusEnum.INIT_SUCCESS.getId());
                    this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_SUCCESS, "");
                    initSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("initHostAndDocker host:[{}] with unknown error", tbHost.getIp(), e);
                    this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED, "initHostAndDocker host with unknown error, check from log files.");
                } finally {
                    initHostLatch.countDown();
                }
            });
        }

        // 去除await，直接异步check
//        initHostLatch.await(constant.getExecHostInitTimeout(), TimeUnit.MILLISECONDS);
//
//        log.info("check initHostAndDocker host timeout");
//        taskMap.entrySet().forEach((entry) -> {
//            int hostId = entry.getKey();
//            Future<?> task = entry.getValue();
//            if(!task.isDone()){
//                log.error("initHostAndDocker host:[{}] timeout, cancel the task.", hostId );
//                this.updateStatus(hostId, HostStatusEnum.INIT_FAILED, "initHostAndDocker host timeout.");
//                task.cancel(false);
//            }
//        });
//
//        boolean hostInitSuccess = initSuccessCount.get() == CollectionUtils.size(tbHostList);
//        // check if all host init success
//        log.log(hostInitSuccess ? Level.INFO: Level.ERROR,
//                "Host of chain:[{}] init result, total:[{}], success:[{}]",
//                chainName, CollectionUtils.size(tbHostList),initSuccessCount.get());
        return true;
    }

    /**
     * check async host init finish or not
     * @param hostIdList
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public List<TbHost> checkInitAndListHost(List<Integer> hostIdList) {
        log.info("start checkInitAndListHost hostIdList:{}", hostIdList);
        List<TbHost> hostList = this.selectDistinctHostListById(hostIdList);
        hostList.stream()
            .filter(host -> host.getStatus() == HostStatusEnum.INITIATING.getId())
            .forEach(host -> {
                Date now = new Date();
                Date modifyTime = host.getModifyTime();
                long gap = now.getTime() - modifyTime.getTime();
                if (gap > constant.getExecHostInitTimeout()) {
                    log.warn("checkInitAndListHost host init failed for time out");
                    this.updateStatus(host.getId(), HostStatusEnum.INIT_FAILED, "Host init failed for time out");
                    host.setStatus(HostStatusEnum.INIT_FAILED.getId());
                    host.setRemark("Host init failed for time out");
                }
            });
        log.info("end checkInitAndListHost hostList:{}", hostList);
        return hostList;
    }

    /**
     * after host_init and generate and init chain db
     * CONFIG SUCCESS
     * @param chainName
     * @param hostIdList
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean scpConfigHostList(String chainName, List<Integer> hostIdList) throws InterruptedException {

        List<TbHost> tbHostList = this.selectDistinctHostListById(hostIdList);

        log.info("Start scpConfigHostList init chain:[{}] hosts:[{}].", chainName, CollectionUtils.size(tbHostList));
        final CountDownLatch configHostLatch = new CountDownLatch(CollectionUtils.size(tbHostList));
        // check success count
        AtomicInteger configSuccessCount = new AtomicInteger(0);
        Map<Integer, Future> taskMap = new HashedMap<>();

        ProgressTools.setScpConfig();
        for (final TbHost tbHost : tbHostList) {
            log.info("scpConfigHostList Init host:[{}], status:[{}]", tbHost.getIp(), tbHost.getStatus());

            Future<?> task = threadPoolTaskScheduler.submit(() -> {
                try {
                    // check if host init shell script executed
                    if (HostStatusEnum.INIT_SUCCESS.getId() == tbHost.getStatus()) {

                        // scp when deploy chain
                        // scp config files from local to remote
                        // local: NODES_ROOT/[chainName]/[ip] TO remote: /opt/fisco/[chainName]
                        // String src = String.format("%s/*", pathService.getHost(chainName, tbHost.getIp()).toString());
                        // ansible not support /*. use {ip}/
                        String src = String.format("%s/", pathService.getHost(chainName, tbHost.getIp()).toString());
                        String dst = PathService.getChainRootOnHost(tbHost.getRootDir(), chainName);
                        try {
                            ansibleService.scp(ScpTypeEnum.UP, tbHost.getIp(), src, dst);
                            log.info("scpConfigHostList Send files from:[{}] to:[{}:{}] success.",
                                src, tbHost.getIp(), dst);
                        } catch (NodeMgrException e) {
                            log.error("scpConfigHostList Send file to host:[{}] failed",
                                tbHost.getIp(), e);
                            this.updateStatus(tbHost.getId(), HostStatusEnum.CONFIG_FAIL,
                                e.getRetCode().getAttachment());
                            return;
                        } catch (Exception e) {
                            log.error("scpConfigHostList Send file to host :[{}] failed", tbHost.getIp(), e);
                            this.updateStatus(tbHost.getId(), HostStatusEnum.CONFIG_FAIL,
                                "Scp configuration files to host failed, please check the host's network or disk usage.");
                            return;
                        }

                    }
                    tbHost.setStatus(HostStatusEnum.CONFIG_SUCCESS.getId());
                    this.updateStatus(tbHost.getId(), HostStatusEnum.CONFIG_SUCCESS, "");
                    configSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Config host:[{}] with unknown error", tbHost.getIp(), e);
                    this.updateStatus(tbHost.getId(), HostStatusEnum.CONFIG_FAIL,
                        "scpConfigHostList Init host with unknown error, check from log files.");
                } finally {
                    configHostLatch.countDown();
                }
            });
            taskMap.put(tbHost.getId(), task);
        }

        configHostLatch.await(constant.getExecHostConfigTimeout(), TimeUnit.MILLISECONDS);
        log.info("Check config host time");
        taskMap.entrySet().forEach((entry) -> {
            int hostId = entry.getKey();
            Future<?> task = entry.getValue();
            if(! task.isDone()){
                log.error("scpConfigHostList Config host:[{}] timeout, cancel the task.", hostId );
                this.updateStatus(hostId, HostStatusEnum.CONFIG_FAIL, "scpConfigHostList Config host timeout.");
                task.cancel(false);
            }
        });

        boolean hostInitSuccess = configSuccessCount.get() == CollectionUtils.size(tbHostList);
        // check if all host init success
        log.log(hostInitSuccess ? Level.INFO: Level.ERROR,
            "scpConfigHostListHost of chain:[{}] init result, total:[{}], success:[{}]",
            chainName, CollectionUtils.size(tbHostList), configSuccessCount.get());

        return hostInitSuccess;


    }


    /**
     * @param chainId
     * @return
     */
    public List<TbHost> selectHostListByChainId(int chainId) {
        log.info("selectHostListByChainId chainId:{}", chainId);
        // select all agencies by chainId
        List<TbFront> frontList = frontService.selectFrontListByChainId(chainId);
        // select all hosts by all agencies
        List<Integer> hostIdList = frontList.stream()
                .map(TbFront::getHostId)
                .collect(Collectors.toList());
        List<TbHost> hostList = this.selectDistinctHostListById(hostIdList);
        log.info("selectHostListByChainId hostList:{}", hostList);

        return hostList;
    }

    /**
     * generate sdk files(crt files and node.[key,crt]) and scp to host
     *
     * when add node
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void generateHostSDKCertAndScp(byte encryptType, String chainName, TbHost host, String agencyName)
        throws NodeMgrException {
        log.info("start generateHostSDKCertAndScp encryptType:{},chainName:{},host:{},agencyName:{}",
            encryptType, chainName, host, agencyName);
        ProgressTools.setGenConfig();
        String ip = host.getIp();
        // new host, generate sdk dir first
        Path sdkPath = this.pathService.getSdk(chainName, ip);

        if (Files.exists(sdkPath)) {
            log.warn("generateHostSDKCertAndScp Exists sdk dir of host:[{}:{}], delete first.", ip,
                sdkPath.toAbsolutePath().toAbsolutePath());
            try {
                FileUtils.deleteDirectory(sdkPath.toFile());
            } catch (IOException e) {
                throw new NodeMgrException(ConstantCode.DELETE_OLD_SDK_DIR_ERROR);
            }
        }
        log.info("generateHostSDKCertAndScp execGenNode");
        // call shell to generate new node config(private key and crt)
        ExecuteResult executeResult = this.deployShellService.execGenNode(
                encryptType, chainName, agencyName, sdkPath.toAbsolutePath().toString());
        if (executeResult.failed()) {
            log.error("exec gen node cert shell error!");
            this.updateStatus(host.getId(), HostStatusEnum.CONFIG_FAIL, executeResult.getExecuteOut());
            throw new NodeMgrException(ConstantCode.EXEC_GEN_SDK_ERROR);
        }

        // init sdk dir
        NodeConfig.initSdkDir(encryptType, sdkPath);

        this.scpHostSdkCert(chainName, host);
        log.info("end generateHostSDK");

    }

    /**
     * separated scp from generate sdk cert
     * @param chainName
     * @param host
     */
    @Transactional(propagation = Propagation.REQUIRED)
    private void scpHostSdkCert(String chainName, TbHost host) {
        log.info("start scpHostSdkCert chainName:{},host:{}", chainName, host);
        String ip = host.getIp();
        // host's sdk path
        Path sdkPath = this.pathService.getSdk(chainName, ip);

        // scp sdk to remote
        String src = String.format("%s", sdkPath.toAbsolutePath().toString());
        String dst = PathService.getChainRootOnHost(host.getRootDir(), chainName);

        log.info("scpHostSdkCert scp: Send files from:[{}] to:[{}:{}].", src, ip, dst);
        ProgressTools.setScpConfig();
        try {
            ansibleService.scp(ScpTypeEnum.UP, ip, src, dst);
            log.info("Send files from:[{}] to:[{}:{}] success.", src, ip, dst);
        } catch (NodeMgrException e) {
            log.error("scpHostSdkCert Send file to host:[{}] failed", ip, e);
            this.updateStatus(host.getId(), HostStatusEnum.CONFIG_FAIL, e.getRetCode().getAttachment());
            return;
        } catch (Exception e) {
            log.error("scpHostSdkCert Send file to host :[{}] failed", ip, e);
            this.updateStatus(host.getId(), HostStatusEnum.CONFIG_FAIL,
                "scpHostSdkCert Scp configuration files to host failed, please check the host's network or disk usage.");
            return;
        }

        this.updateStatus(host.getId(), HostStatusEnum.CONFIG_SUCCESS, "");
        log.info("end scpHostSdkCert");

    }
    
    /**
     * delete host by
     * @param hostId
     */
    @Transactional
    public void deleteHostWithoutNode(int hostId){
        log.info("deleteHostWithoutNode hostId:{}", hostId);
        TbHost host = this.tbHostMapper.selectByPrimaryKey(hostId);
        if (host == null){
            log.warn("Host:[{}] not exists.", hostId);
            throw new NodeMgrException(ConstantCode.HOST_NOT_EXIST);
        }

        List<TbFront> frontList = this.frontMapper.selectByHostId(hostId);
        if (CollectionUtils.isEmpty(frontList)) {
            this.tbHostMapper.deleteByPrimaryKey(hostId);
            log.warn("Delete host:[{}].", hostId);
        } else {
            log.error("host still have node on it");
            throw new NodeMgrException(ConstantCode.DELETE_HOST_FAIL_FOR_STILL_CONTAIN_NODE);
        }
    }


    /**
     * deleteHostChainDir b chain id
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteHostChainDir(TbChain chain) {
        log.info("start deleteHostChainDir chain:{}", chain);
        List<TbFront> frontList = frontService.selectFrontListByChainId(chain.getId());
        List<Integer> hostIdList = frontList.stream().map(TbFront::getHostId).collect(Collectors.toList());
        this.mvHostChainDirByIdList(chain.getChainName(), hostIdList);
    }

    /**
     * mv host's chain dir, but not delete host data in db
     * @param hostIdList
     */
    @Transactional
    public void mvHostChainDirByIdList(String chainName, List<Integer> hostIdList){
        log.info("start deleteHostHostIdList chainName:{},hostIdList:{}", chainName, hostIdList);
        List<TbHost> hostList = this.selectDistinctHostListById(hostIdList);
        if(CollectionUtils.isNotEmpty(hostList)){
            for (TbHost host : hostList) {
                // move chain config files
                chainService.mvChainOnRemote(host.getIp(), host.getRootDir(), chainName);
            }
        }
        // delete host in batch
        log.info("End deleteHostHostIdList");
    }

    /**
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
     * check docker image
     * @param hostIdList
     * @param imageTag
     */
    public void checkImageExists(List<Integer> hostIdList, String imageTag){
        List<TbHost> hostList = this.selectDistinctHostListById(hostIdList);
        Set<String> ipSet = hostList.stream().map(TbHost::getIp).collect(Collectors.toSet());
        this.checkImageExists(ipSet, imageTag);
    }

    /**
     * check docker image
     * @param ipSet
     * @param imageTag
     */
    public void checkImageExists(Set<String> ipSet, String imageTag){
        log.info("checkImageExists ipSet:{}", ipSet);
        for (String ip : ipSet) {
            boolean exists = this.dockerOptions.checkImageExists(ip, imageTag);
            if (!exists) {
                log.error("Docker image:[{}] not exists on host:[{}].", imageTag, ip);
                throw new NodeMgrException(ConstantCode.IMAGE_NOT_EXISTS_ON_HOST.attach(ip));
            }
        }
    }

    /**
     * check host memory/cpu/dependencies(docker, docker hello-world)
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean batchCheckHostList(List<Integer> hostIdList) throws InterruptedException {
        log.info("batchCheckHostList hostIdList:{}", hostIdList);
        // save repeat time in "remark"
        List<TbHost> tbHostList = this.selectDistinctHostListById(hostIdList);
        log.info("batchCheckHostList tbHostList:{}", tbHostList);

        final CountDownLatch checkHostLatch = new CountDownLatch(CollectionUtils.size(tbHostList));
        // check success count
        AtomicInteger checkSuccessCount = new AtomicInteger(0);
        Map<Integer, Future> taskMap = new HashedMap<>();

        ProgressTools.setHostCheck();
        for (final TbHost tbHost : tbHostList) {
            log.info("Check host:[{}], status:[{}], remark:[{}]", tbHost.getIp(), tbHost.getStatus(), tbHost.getRemark());

            // check if host check success
            Future<?> task = threadPoolTaskScheduler.submit(() -> {
                try {
                    // fetch remark's nodeCount
                    int nodeCount = Integer.parseInt(tbHost.getRemark());

                    log.info("Check host memory/cpu:[{}] nodeCount:{}, by exec shell script:[{}]",
                        tbHost.getIp(), nodeCount, constant.getHostCheckShell());
                    // check memory every time
                    try {
                        ansibleService.execHostCheckShell(tbHost.getIp(), nodeCount);
                    } catch (NodeMgrException e) {
                        log.error("Check mem/cpu on host:[{}] failed",
                            tbHost.getIp(), e);
                        this.updateStatus(tbHost.getId(), HostStatusEnum.CHECK_FAILED,
                            e.getRetCode().getAttachment());
                        return;
                    } catch (Exception e) {
                        log.error("Check mem/cpu on host:[{}] failed",
                            tbHost.getIp(), e);
                        this.updateStatus(tbHost.getId(), HostStatusEnum.CHECK_FAILED,
                            e.getMessage());
                        return;
                    }

                    // if already check success once time, no need check again
                    if (!HostStatusEnum.hostCheckSuccess(tbHost.getStatus())) {
                        log.info("Check host:[{}] by exec shell script:[{},{}]", tbHost.getIp(),
                            constant.getHostCheckShell(), constant.getDockerCheckShell());
                        ProgressTools.setDockerCheck();
                        // exec host check shell script
                        try {
                            ansibleService.execPing(tbHost.getIp());
                            // check docker installed, active and no need sudo docker
                            ansibleService.execDockerCheckShell(tbHost.getIp());
                        } catch (NodeMgrException e) {
                            log.error("Exec host check shell script on host:[{}] failed",
                                tbHost.getIp(), e);
                            this.updateStatus(tbHost.getId(), HostStatusEnum.CHECK_FAILED,
                                e.getRetCode().getAttachment());
                            return;
                        } catch (Exception e) {
                            log.error("Exec host check shell script on host:[{}] failed",
                                tbHost.getIp(), e);
                            this.updateStatus(tbHost.getId(), HostStatusEnum.CHECK_FAILED,
                                e.getMessage());
                            return;
                        }
                    }
                    // update as check success
                    tbHost.setStatus(HostStatusEnum.CHECK_SUCCESS.getId());
                    this.updateStatus(tbHost.getId(), HostStatusEnum.CHECK_SUCCESS, "");
                    checkSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Check host:[{}] with unknown error", tbHost.getIp(), e);
                    this.updateStatus(tbHost.getId(), HostStatusEnum.CHECK_FAILED,
                        "Check host with unknown error, check from log files.");
                } finally {
                    checkHostLatch.countDown();
                }
            });
            taskMap.put(tbHost.getId(), task);
        }
        checkHostLatch.await(constant.getExecHostCheckTimeout(), TimeUnit.MILLISECONDS);
        log.info("Verify check_host time");
        taskMap.forEach((key, value) -> {
            int hostId = key;
            Future<?> task = value;
            if (!task.isDone()) {
                log.error("Check host:[{}] timeout, cancel the task.", hostId);
                this.updateStatus(hostId, HostStatusEnum.CHECK_FAILED, "Check host timeout.");
                task.cancel(false);
            }
        });

        boolean hostCheckSuccess = checkSuccessCount.get() == CollectionUtils.size(tbHostList);
        // check if all host init success
        log.log(hostCheckSuccess ? Level.INFO: Level.ERROR,
            "Host check result, total:[{}], success:[{}]",
            CollectionUtils.size(tbHostList), checkSuccessCount.get());

        return hostCheckSuccess;
    }

    /**
     * select host list by id list
     * distinct host and save node count(repeat time) in host's remark
     * @param hostIdList
     * @return
     */
    public List<TbHost> selectDistinctHostListById(List<Integer> hostIdList){
        // distinct repeat hostId
        Map<Integer, Long> distinctHostIdMap = hostIdList.stream().collect(Collectors.groupingBy(h -> h, Collectors.counting()));
        log.info("selectDistinctHostListById distinctHostIdMap:{}", distinctHostIdMap);
        // mark repeat time in Host's remark
        List<TbHost> hostList = new ArrayList<>();
        for (Integer hostId: distinctHostIdMap.keySet()) {
            int count = 0;
            Long repeatTime = distinctHostIdMap.get(hostId);
            count = repeatTime.intValue();

            TbHost host = tbHostMapper.selectByPrimaryKey(hostId);
            if (host == null) {
                throw new NodeMgrException(ConstantCode.HOST_NOT_EXIST);
            }
            // store repeat time as node count
            host.setRemark(String.valueOf(count));
            // add in list
            hostList.add(host);
        }
        return hostList;
    }

    /**
     * check host chain's port before init host, after check host mem/cpu
     * @related: syncCheckPortHostList， 一个host多个端口时，端口占用的异常会被覆盖，因此采用同步非异步方式检测
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean checkPortHostList(List<DeployNodeInfo> deployNodeInfoList) throws InterruptedException {
        log.info("batchCheckHostList deployNodeInfoList:{}", deployNodeInfoList);


        final CountDownLatch checkHostLatch = new CountDownLatch(CollectionUtils.size(deployNodeInfoList));
        // check success count
        AtomicInteger checkSuccessCount = new AtomicInteger(0);
        Map<String, Future> taskMap = new HashedMap<>(); //key is ip+"_"+frontPort

        ProgressTools.setPortCheck();
        for (final DeployNodeInfo nodeInfo : deployNodeInfoList) {
            log.info("Check host port:[{}]", nodeInfo.getIp());

            Future<?> task = threadPoolTaskScheduler.submit(() -> {
                try {
                    // check chain port
                    ExecuteResult checkPortResult = ansibleService.checkPortArrayInUse(nodeInfo.getIp(),
                        nodeInfo.getChannelPort(), nodeInfo.getP2pPort(), nodeInfo.getFrontPort(), nodeInfo.getRpcPort());
                    // not in use is true
                    if (!checkPortResult.success()) {
                        log.error("Port check on host ip:[{}] not passe0d:{}!", nodeInfo.getIp(), checkPortResult.getExecuteOut());
                        this.updateStatus(nodeInfo.getHostId(), HostStatusEnum.CHECK_FAILED, checkPortResult.getExecuteOut());
                        return;
                    }
                    this.updateStatus(nodeInfo.getHostId(), HostStatusEnum.CHECK_SUCCESS, "");
                    checkSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Check host port:[{}] with unknown error", nodeInfo.getIp(), e);
                    this.updateStatus(nodeInfo.getHostId(), HostStatusEnum.CHECK_FAILED, e.getMessage());
                } finally {
                    checkHostLatch.countDown();
                }
            });
            String taskKey = nodeInfo.getIp() + "_" + nodeInfo.getFrontPort();
            taskMap.put(taskKey, task);
        }
        checkHostLatch.await(constant.getExecHostCheckPortTimeout(), TimeUnit.MILLISECONDS);
        log.info("Verify check_port time");
        taskMap.forEach((key, value) -> {
            String frontIpPort = key;
            Future<?> task = value;
            if (!task.isDone()) {
                log.error("Check host port:[{}] timeout, cancel the task.", frontIpPort);
                String ip = frontIpPort.split("_")[0];
                int port = Integer.parseInt(frontIpPort.split("_")[1]);
                TbFront front = frontMapper.getByIpPort(ip, port);
                this.updateStatus(front.getHostId(), HostStatusEnum.CHECK_FAILED, "Check host port timeout.");
                task.cancel(false);
            }
        });

        boolean hostPorkCheckSuccess = checkSuccessCount.get() == CollectionUtils.size(deployNodeInfoList);
        // check if all host init success
        log.log(hostPorkCheckSuccess ? Level.INFO: Level.ERROR,
            "Host port check result, total:[{}], success:[{}]",
            CollectionUtils.size(deployNodeInfoList), checkSuccessCount.get());

        return hostPorkCheckSuccess;
    }

    /**
     * check image before start chain
     * @param ipConf
     * @param imageTag
     */
    public void checkImageExistRemote(String[] ipConf, String imageTag) {
        // parse ipConf config
        log.info("Parse ipConf content....");
        List<IpConfigParse> ipConfigParseList = IpConfigParse.parseIpConf(ipConf);
        configService.checkValueInDb(imageTag);
        // check docker image exists before start
        Set<String> ipSet = ipConfigParseList.stream().map(IpConfigParse::getIp).collect(Collectors.toSet());
        this.checkImageExists(ipSet, imageTag);
    }

    /**
     * if not all, throw error
     * @return void
     * @param hostIdList
     */
    public void checkAllHostInitSuc(List<Integer> hostIdList) {
        log.info("check all hosts to add node hostIdList:{}", hostIdList);
        AtomicBoolean allHostInitSuccess = new AtomicBoolean(true);
        hostIdList.forEach(hId -> {
            TbHost host = tbHostMapper.selectByPrimaryKey(hId);
            if (host == null) {
                throw new NodeMgrException(ConstantCode.HOST_NOT_EXIST);
            }
            if (HostStatusEnum.INIT_SUCCESS.getId() != host.getStatus()) {
                allHostInitSuccess.set(false);
            }
        });
        if (!allHostInitSuccess.get()) {
            log.error("configChainAndScp stop for not all host init success");
            throw new NodeMgrException(ConstantCode.NOT_ALL_HOST_INIT_SUCCESS);
        }
    }

    /**
     * check synchronized
     * check host chain's port before init host, after check host mem/cpu
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean syncCheckPortHostList(List<DeployNodeInfo> deployNodeInfoList) {
        log.info("syncCheckPortHostList deployNodeInfoList:{}", deployNodeInfoList);
        ProgressTools.setPortCheck();
        int successCount = 0;
        for (final DeployNodeInfo nodeInfo : deployNodeInfoList) {
            log.info("Check host port:[{}]", nodeInfo.getIp());
            try {
                // check chain port
                ExecuteResult checkPortResult = ansibleService.checkPortArrayInUse(nodeInfo.getIp(),
                    nodeInfo.getChannelPort(), nodeInfo.getP2pPort(), nodeInfo.getFrontPort(), nodeInfo.getRpcPort());
                // not in use is true
                if (!checkPortResult.success()) {
                    log.error("Port check on host ip:[{}] not passed:{}!", nodeInfo.getIp(), checkPortResult.getExecuteOut());
                    this.updateStatus(nodeInfo.getHostId(), HostStatusEnum.CHECK_FAILED, checkPortResult.getExecuteOut());
                    break;
                }
                this.updateStatus(nodeInfo.getHostId(), HostStatusEnum.CHECK_SUCCESS, "");
                successCount++;
            } catch (Exception e) {
                log.error("Check host:[{}] with unknown error", nodeInfo.getIp(), e);
                this.updateStatus(nodeInfo.getHostId(), HostStatusEnum.CHECK_FAILED, e.getMessage());
                break;
            }

        }

        boolean hostPorkCheckSuccess = successCount == CollectionUtils.size(deployNodeInfoList);
        // check if all host init success
        log.log(hostPorkCheckSuccess ? Level.INFO: Level.ERROR,
            "Host check result, total:[{}], success:[{}]",
            CollectionUtils.size(deployNodeInfoList), successCount);

        return hostPorkCheckSuccess;
    }

    /**
     * if 127.0.0.1 was added first, then check
     * @param ip
     */
    @Deprecated
    private void validateHostLocalIp(String ip) {
        if (IPUtil.isLocal(ip)) {
            // if ip is 127.0.0.1, check all other host ip
            List<TbHost> hostList = tbHostMapper.selectAll();
            // 127.0.0.1 is the first host
            if (hostList == null || hostList.isEmpty()) {
                log.info("host list empty, skip check local");
                return;
            } else {
                List<String> ipList = hostList.stream().map(TbHost::getIp)
                    .collect(Collectors.toList());
                if (!ansibleService.checkLocalIp(ipList)) {
                    log.error("same host of local ip:{}", ip);
                    throw new NodeMgrException(ConstantCode.SAME_HOST_ERROR);
                }
            }
        } else {
            // check whether 127.0.0.1 in tb_host
            TbHost hostOfLocalIp = tbHostMapper.getByIp(IPUtil.LOCAL_IP_127);
            if (hostOfLocalIp == null) {
                log.info("host of 127.0.0.1 not in tb_host, skip check local");
                return;
            }
            // 127.0.0.1 existed in tb_host, check this ip whether same with 127.0.0.1
            if (!ansibleService.checkLocalIp(Collections.singletonList(ip))) {
                log.error("same host of local ip:{}", ip);
                throw new NodeMgrException(ConstantCode.SAME_HOST_ERROR);
            }
        }
    }

    /**
     * @case: if 127.0.0.1 added, cannot add other ip, only 127.0.0.1 support
     * @case: if other ip added, cannot add 127.0.0.1
     */
    private void validateAllLocalhostOrNot(String ip) {
        log.info("check validateAllLocalhostOrNot ip:{}",ip);
        if (IPUtil.isLocal(ip)) {
            // if ip is 127.0.0.1, check all other host ip
            List<TbHost> hostList = tbHostMapper.selectAll();
            // 127.0.0.1 is the first host, pass
            if (hostList == null || hostList.isEmpty()) {
                log.info("host list empty, skip check local");
                return;
            } else {
                // if already exist others ip, not pass
                log.error("validateAllLocalhostOrNot already exist common ip, cannot add 127.0.0.1");
                throw new NodeMgrException(ConstantCode.HOST_ONLY_ALL_LOCALHOST_OR_NOT_LOCALHOST);
            }
        } else {
            // check whether 127.0.0.1 in tb_host
            TbHost hostOfLocalIp = tbHostMapper.getByIp(IPUtil.LOCAL_IP_127);
            if (hostOfLocalIp == null) {
                log.info("host of 127.0.0.1 not in tb_host, skip check local");
                return;
            } else {
                // if already exist local ip, not pass
                log.error("validateAllLocalhostOrNot already exist localhost ip, cannot add common ip");
                throw new NodeMgrException(ConstantCode.HOST_ONLY_ALL_LOCALHOST_OR_NOT_LOCALHOST);
            }

        }
    }
}