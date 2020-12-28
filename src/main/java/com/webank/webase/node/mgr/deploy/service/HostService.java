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

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.HostStatusEnum;
import com.webank.webase.node.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NetUtils;
import com.webank.webase.node.mgr.base.tools.NumberUtil;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.chain.ChainService;
import com.webank.webase.node.mgr.deploy.entity.IpConfigParse;
import com.webank.webase.node.mgr.deploy.entity.NodeConfig;
import com.webank.webase.node.mgr.deploy.entity.DeployNodeInfo;
import com.webank.webase.node.mgr.deploy.entity.TbAgency;
import com.webank.webase.node.mgr.deploy.entity.TbConfig;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbChainMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbConfigMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbHostMapper;
import com.webank.webase.node.mgr.deploy.service.docker.DockerOptions;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
    @Autowired private TbChainMapper tbChainMapper;

    @Autowired private ConstantProperties constant;
    @Autowired private DockerOptions dockerOptions;
    @Autowired private AgencyService agencyService;
    @Autowired private PathService pathService;
    @Autowired private DeployShellService deployShellService;
    @Autowired private DeployService deployService;
    @Autowired private AnsibleService ansibleService;
    @Autowired private FrontService frontService;
    @Autowired private TbConfigMapper tbConfigMapper;
    @Autowired private ChainService chainService;

    @Qualifier(value = "deployAsyncScheduler")
    @Autowired private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateStatus(int hostId, HostStatusEnum newStatus,String remark) throws NodeMgrException {
        log.info("Change host status to:[{}:{}:{}]",hostId, newStatus, remark);
        return tbHostMapper.updateHostStatus(hostId,new Date(), newStatus.getId(),remark) == 1;
    }

//    @Transactional(propagation = Propagation.REQUIRED)
//    public TbHost insertIfNew(String ip, String remark) throws NodeMgrException {
//        TbHost host = this.tbHostMapper.getByIp(ip);
//        if (host != null){
//            return host;
//        }
//
//        // fix call transaction in the same class
//        return ((HostService) AopContext.currentProxy())
//                .insert(ip, HostStatusEnum.ADDED, remark);
//    }


    @Transactional(propagation = Propagation.REQUIRED)
    public TbHost insert(String ip, String rootDir, HostStatusEnum hostStatusEnum, String remark) throws NodeMgrException {

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
    public boolean initHostAndDocker(String chainName, String imageTag, List<Integer> hostIdList, int imagePullType)
        throws InterruptedException {
        List<TbHost> tbHostList = this.selectDistinctHostListById(hostIdList);

        log.info("Start initHostAndDocker chain:[{}] hosts:[{}].", chainName, CollectionUtils.size(tbHostList));
        final CountDownLatch initHostLatch = new CountDownLatch(CollectionUtils.size(tbHostList));
        // check success count
        AtomicInteger initSuccessCount = new AtomicInteger(0);
        Map<Integer, Future> taskMap = new HashedMap<>();


        for (final TbHost tbHost : tbHostList) {
            log.info("initHostAndDocker host:[{}], status:[{}]", tbHost.getIp(), tbHost.getStatus());

            Future<?> task = threadPoolTaskScheduler.submit(() -> {
                try {
                    // check if host init shell script executed
                    if (!HostStatusEnum.successOrInitiating(tbHost.getStatus())) {

                        boolean success = this.updateStatus(tbHost.getId(), HostStatusEnum.INITIATING, "Initiating...");
                        if (success) {
                            log.info("initHostAndDocker host:[{}] by exec shell script:[{}]", tbHost.getIp(), constant.getHostInitShell());

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
                            log.info("initHostAndDocker pull docker ip:{}, imageTag:{}, imagePullType:{}", tbHost.getIp(), imageTag, imagePullType);
                            this.dockerOptions.pullImage(tbHost.getIp(), imageTag, imagePullType);
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
            taskMap.put(tbHost.getId(), task);
        }

        initHostLatch.await(constant.getExecHostInitTimeout(), TimeUnit.MILLISECONDS);
        log.error("initHostAndDocker host timeout, cancel unfinished tasks.");
        taskMap.entrySet().forEach((entry)->{
            int hostId = entry.getKey();
            Future<?> task = entry.getValue();
            if(! task.isDone()){
                log.error("initHostAndDocker host:[{}] timeout, cancel the task.", hostId );
                this.updateStatus(hostId, HostStatusEnum.INIT_FAILED, "initHostAndDocker host timeout.");
                task.cancel(false);
            }
        });

        boolean hostInitSuccess = initSuccessCount.get() == CollectionUtils.size(tbHostList);
        // check if all host init success
        log.log(hostInitSuccess ? Level.INFO: Level.ERROR,
                "Host of chain:[{}] init result, total:[{}], success:[{}]",
                chainName, CollectionUtils.size(tbHostList),initSuccessCount.get());

        return hostInitSuccess;
    }



    /**
     * after host_init and generate and init chain db
     * CONFIG SUCCESS
     * @param chainName
     * @param hostIdList
     */
    public boolean scpConfigHostList(String chainName, List<Integer> hostIdList) throws InterruptedException {

        List<TbHost> tbHostList = this.selectDistinctHostListById(hostIdList);

        log.info("Start init chain:[{}] hosts:[{}].", chainName, CollectionUtils.size(tbHostList));
        final CountDownLatch initHostLatch = new CountDownLatch(CollectionUtils.size(tbHostList));
        // check success count
        AtomicInteger initSuccessCount = new AtomicInteger(0);
        Map<Integer, Future> taskMap = new HashedMap<>();

        for (final TbHost tbHost : tbHostList) {
            log.info("Init host:[{}], status:[{}]", tbHost.getIp(), tbHost.getStatus());

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
//                            deployShellService.scp(ScpTypeEnum.UP, tbHost.getSshUser(), tbHost.getIp(), tbHost.getSshPort(), src, dst);
                            ansibleService.scp(ScpTypeEnum.UP, tbHost.getIp(), src, dst);
                            log.info("Send files from:[{}] to:[{}:{}] success.",
                                src, tbHost.getIp(), dst);
                        } catch (Exception e) {
                            log.error("Send file to host :[{}] failed", tbHost.getIp(), e);
                            this.updateStatus(tbHost.getId(), HostStatusEnum.CONFIG_FAIL,
                                "Scp configuration files to host failed, please check the host's network or disk usage.");
                            return ;
                        }

                    }

                    tbHost.setStatus(HostStatusEnum.CONFIG_SUCCESS.getId());
                    this.updateStatus(tbHost.getId(), HostStatusEnum.CONFIG_SUCCESS, "");
                    initSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Init host:[{}] with unknown error", tbHost.getIp(), e);
                    this.updateStatus(tbHost.getId(), HostStatusEnum.CONFIG_FAIL, "Init host with unknown error, check from log files.");
                } finally {
                    initHostLatch.countDown();
                }
            });
            taskMap.put(tbHost.getId(), task);
        }

        initHostLatch.await(constant.getExecHostInitTimeout(), TimeUnit.MILLISECONDS);
        log.error("Check init host time, cancel unfinished tasks.");
        taskMap.entrySet().forEach((entry)->{
            int hostId = entry.getKey();
            Future<?> task = entry.getValue();
            if(! task.isDone()){
                log.error("Init host:[{}] timeout, cancel the task.", hostId );
                this.updateStatus(hostId, HostStatusEnum.CONFIG_FAIL, "Init host timeout.");
                task.cancel(false);
            }
        });

        boolean hostInitSuccess = initSuccessCount.get() == CollectionUtils.size(tbHostList);
        // check if all host init success
        log.log(hostInitSuccess ? Level.INFO: Level.ERROR,
            "Host of chain:[{}] init result, total:[{}], success:[{}]",
            chainName, CollectionUtils.size(tbHostList),initSuccessCount.get());

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
     * Init a host, generate sdk files(crt files and node.[key,crt])
     * and insert into db.
     *
     * when add node
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public TbHost generateHostSDKAndScp(byte encryptType, String chainName, String rootDirOnHost, String ip, String agencyName)
        throws NodeMgrException {
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
        // get host dir by ip
        TbHost host = tbHostMapper.getByIp(ip);

        // init sdk dir
        NodeConfig.initSdkDir(encryptType, sdkPath);

        // scp sdk to remote
        String src = String.format("%s", sdkPath.toAbsolutePath().toString());
        String dst = PathService.getChainRootOnHost(host.getRootDir(), chainName);

        log.info("Send files from:[{}] to:[{}:{}].", src, ip, dst);
        ansibleService.scp(ScpTypeEnum.UP, ip, src, dst);

        // insert host into db
        return ((HostService) AopContext.currentProxy())
                .insert(ip, rootDirOnHost, HostStatusEnum.ADDED,"");
    }

    /**
     * delete host by
     * @param hostId
     */
    @Transactional
    public void deleteHostWithNoNode(int hostId){
        log.info("deleteHostWithNoNode hostId:{}", hostId);
        TbHost host = this.tbHostMapper.selectByPrimaryKey(hostId);
        if (host == null){
            log.warn("Host:[{}] not exists.", hostId);
            return;
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
     * todo add check
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
     * @param ipSet
     * @param imageTag
     */
    public void checkImageExists(Set<String> ipSet, String imageTag){
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
    public boolean batchCheckHostList(List<Integer> hostIdList) throws InterruptedException {
        log.info("batchCheckHostList hostIdList:{}", hostIdList);
        // save repeat time in "remark"
        List<TbHost> tbHostList = this.selectDistinctHostListById(hostIdList);
        log.info("batchCheckHostList tbHostList:{}", tbHostList);


        final CountDownLatch checkHostLatch = new CountDownLatch(CollectionUtils.size(tbHostList));
        // check success count
        AtomicInteger checkSuccessCount = new AtomicInteger(0);
        Map<Integer, Future> taskMap = new HashedMap<>();

        for (final TbHost tbHost : tbHostList) {
            log.info("Check host:[{}], status:[{}]", tbHost.getIp(), tbHost.getStatus());

            Future<?> task = threadPoolTaskScheduler.submit(() -> {
                try {
                    // check if host check success
                    if (!HostStatusEnum.hostCheckSuccess(tbHost.getStatus())) {
                        log.info("Check host:[{}] by exec shell script:[{},{}]", tbHost.getIp(),
                            constant.getHostCheckShell(), constant.getDockerCheckShell());

                        // fetch remark's nodeCount
                        int nodeCount = Integer.parseInt(tbHost.getRemark());
                        // exec host check shell script
                        try {
                            ansibleService.execPing(tbHost.getIp());
                            ansibleService.execHostCheckShell(tbHost.getIp(), nodeCount);
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
        log.error("Verify check_host time, cancel unfinished tasks.");
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
     * get distinct host list by host id list, save repeat time in "remark"
     * @case: if hostId the same, count repeat times in host's remark
     *
     * @param hostIdList
     * @return
     */
    public List<TbHost> selectHostListById(List<Integer> hostIdList){
        // mark repeat time in Host's remark
        List<TbHost> hostList = new ArrayList<>();
        hostIdList.forEach(id -> {
            TbHost host = tbHostMapper.selectByPrimaryKey(id);
            hostList.add(host);
        });

        return hostList;
    }

    public List<TbHost> selectDistinctHostListById(List<Integer> hostIdList){
        // distinct repeat hostId
        Map<Integer, Long> distinctHostIdMap = hostIdList.stream().collect(Collectors.groupingBy(h -> h, Collectors.counting()));
        // mark repeat time in Host's remark
        List<TbHost> hostList = new ArrayList<>();
        for (Integer hostId: distinctHostIdMap.keySet()) {
            Long repeatTime = distinctHostIdMap.get(hostId);
            TbHost host = tbHostMapper.selectByPrimaryKey(hostId);
            host.setRemark(repeatTime.toString());
            // add in list
            hostList.add(host);
        }
        return hostList;
    }

    /**
     * check host chain's port before init host, after check host mem/cpu
     */
    public boolean checkPortHostList(List<DeployNodeInfo> deployNodeInfoList) throws InterruptedException {
        log.info("batchCheckHostList deployNodeInfoList:{}", deployNodeInfoList);


        final CountDownLatch checkHostLatch = new CountDownLatch(CollectionUtils.size(deployNodeInfoList));
        // check success count
        AtomicInteger checkSuccessCount = new AtomicInteger(0);
        Map<String, Future> taskMap = new HashedMap<>(); //key is ip+"_"+frontPort

        for (final DeployNodeInfo nodeInfo : deployNodeInfoList) {
            log.info("Check host:[{}]", nodeInfo.getIp());

            Future<?> task = threadPoolTaskScheduler.submit(() -> {
                try {
                    // check chain port
                    Pair<Boolean, Integer> portReachable = NetUtils
                        .checkPorts(nodeInfo.getIp(), 2000,
                            nodeInfo.getChannelPort(), nodeInfo.getP2pPort(), nodeInfo.getFrontPort(), nodeInfo.getRpcPort());
                    if (portReachable.getKey()) {
                        log.error("Port:[{}] is in use on host :[{}] failed", portReachable.getValue(), nodeInfo.getIp() );
                        this.updateStatus(nodeInfo.getHostId(), HostStatusEnum.CHECK_FAILED, "Port is in use!");
                        return;
                    }
                    this.updateStatus(nodeInfo.getHostId(), HostStatusEnum.CHECK_SUCCESS, "");
                    checkSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Check host:[{}] with unknown error", nodeInfo.getIp(), e);
                    this.updateStatus(nodeInfo.getHostId(), HostStatusEnum.CHECK_FAILED, e.getMessage());
                } finally {
                    checkHostLatch.countDown();
                }
            });
            String taskKey = nodeInfo.getIp() + "_" + nodeInfo.getFrontPort();
            taskMap.put(taskKey, task);
        }
        checkHostLatch.await(constant.getExecHostCheckTimeout(), TimeUnit.MILLISECONDS);
        log.error("Verify check_port time, cancel unfinished tasks.");
        taskMap.forEach((key, value) -> {
            String frontIpPort = key;
            Future<?> task = value;
            if (!task.isDone()) {
                log.error("Check host:[{}] timeout, cancel the task.", frontIpPort);
                String ip = frontIpPort.split("_")[0];
                int port = Integer.parseInt(frontIpPort.split("_")[1]);
                TbFront front = frontMapper.getByIpPort(ip, port);
                this.updateStatus(front.getHostId(), HostStatusEnum.CHECK_FAILED, "Check host timeout.");
                task.cancel(false);
            }
        });

        boolean hostPorkCheckSuccess = checkSuccessCount.get() == CollectionUtils.size(deployNodeInfoList);
        // check if all host init success
        log.log(hostPorkCheckSuccess ? Level.INFO: Level.ERROR,
            "Host check result, total:[{}], success:[{}]",
            CollectionUtils.size(deployNodeInfoList), checkSuccessCount.get());

        return hostPorkCheckSuccess;
    }

    /**
     * check image before start chain
     * @param ipConf
     * @param tagId
     */
    public void checkImageExistRemote(String[] ipConf, int tagId) {
        // parse ipConf config
        log.info("Parse ipConf content....");
        List<IpConfigParse> ipConfigParseList = IpConfigParse.parseIpConf(ipConf);
        TbConfig imageConfig = tbConfigMapper.selectByPrimaryKey(tagId);
        if (imageConfig == null || StringUtils.isBlank(imageConfig.getConfigValue())) {
            throw new NodeMgrException(ConstantCode.TAG_ID_PARAM_ERROR);
        }
        // check docker image exists before start
        Set<String> ipSet = ipConfigParseList.stream().map(IpConfigParse::getIp).collect(Collectors.toSet());
        this.checkImageExists(ipSet, imageConfig.getConfigValue());
    }


}