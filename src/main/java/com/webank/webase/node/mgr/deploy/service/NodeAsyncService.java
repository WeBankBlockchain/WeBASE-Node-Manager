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

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import com.webank.webase.node.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.node.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.node.mgr.base.enums.OptionType;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.CertTools;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.chain.ChainService;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbChainMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbHostMapper;
import com.webank.webase.node.mgr.front.FrontMapper;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.node.NodeService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class NodeAsyncService {

    @Autowired private FrontMapper frontMapper;
    @Autowired private TbHostMapper tbHostMapper;
    @Autowired private TbChainMapper tbChainMapper;

    @Autowired private FrontService frontService;
    @Autowired private NodeService nodeService;
    @Autowired private ChainService chainService;
    @Autowired private HostService hostService;
    @Autowired private ConstantProperties constant;
    @Autowired private GroupService groupService;

    @Qualifier(value = "deployAsyncScheduler")
    @Autowired private ThreadPoolTaskScheduler threadPoolTaskScheduler;

//    /**
//     * scp local chain config and front config to host
//     * @param chainName
//     */
//    @Async("deployAsyncScheduler")
//    public void asyncConfigChain(String chainName, OptionType optionType) {
//        TbChain chain = this.tbChainMapper.getByChainName(chainName);
//        if (chain == null) {
//            log.error("No chain:[{}] to deploy.", chainName);
//            return;
//        }
//
//        try {
//            // check chain status
//            if (ChainStatusEnum.successOrDeploying(chain.getChainStatus())){
//                log.error("Chain:[{}] is running or deploying", chainName);
//                return;
//            }
//            boolean success = this.chainService.updateStatus(chain.getId(), ChainStatusEnum.CONFIGURING);
//            if (!success) {
//                log.error("Update chain:[{}] status to DEPLOYING failed when deploying.", chainName);
//                return;
//            }
//
//            // select all host to init
//            List<TbHost> tbHostList = this.hostService.selectHostListByChainId(chain.getId());
//            if (CollectionUtils.isEmpty(tbHostList)) {
//                log.error("Chain:[{}:{}] has no host.", chain.getId(), chain.getChainName());
//                return;
//            }
//
////
//        } catch (Exception e) {
//            log.error("Init host list and start chain:[{}] error", chainName, e);
//            chainService.updateStatus(chain.getId(), ChainStatusEnum.CONFIG_FAILED);
//        }
//    }


    /**
     * start chain
     * @param chainId
     */
    @Async("deployAsyncScheduler")
    public void asyncStartChain(int chainId, OptionType optionType, ChainStatusEnum success, ChainStatusEnum failed ,
                                FrontStatusEnum frontBefore, FrontStatusEnum frontSuccess,FrontStatusEnum frontFailed ) {
        final boolean startSuccess = this.restartChain(chainId, optionType, frontBefore, frontSuccess, frontFailed);
        threadPoolTaskScheduler.schedule(() ->
            chainService.updateStatus(chainId, startSuccess ? success : failed),
            Instant.now().plusMillis(1L));
    }


    /**
     * if add new node or delete old node
     * @param chainId
     * @param groupIdSet
     * @param optionType
     */
    @Async("deployAsyncScheduler")
    public void asyncRestartRelatedFront(int chainId, Set<Integer> groupIdSet, OptionType optionType,
                         FrontStatusEnum frontBefore, FrontStatusEnum frontSuccess,FrontStatusEnum frontFailed ) {

        // update chain to updating
        this.chainService.updateStatus(chainId,ChainStatusEnum.RESTARTING);

        this.restartFrontOfGroupSet(chainId, groupIdSet, optionType, frontBefore,frontSuccess, frontFailed);

        // update chain to running
        threadPoolTaskScheduler.schedule(() -> {
            this.chainService.updateStatus(chainId, ChainStatusEnum.RUNNING);

            // set pull cert to false
            CertTools.isPullFrontCertsDone = false;
        }, Instant.now().plusMillis( constant.getDockerRestartPeriodTime()));
    }

    /**
     * 扩容节点
     * @param chain
     * @param host
     * @param group
     * @param optionType
     * @param newFrontList
     */
    @Async("deployAsyncScheduler")
    public void asyncAddNode(TbChain chain, TbHost host, TbGroup group, OptionType optionType, List<TbFront> newFrontList) {
        try {
            int groupId = group.getGroupId();
            boolean initSuccess = this.hostService.initHostList(chain, Arrays.asList(host.getId()), false, false);
            log.info("Init host:[{}], result:[{}]",host.getIp(), initSuccess);
            if (initSuccess) {
                // start front and  related front
                this.asyncRestartRelatedFront(chain.getId(), Collections.singleton(groupId), optionType,
                        FrontStatusEnum.STARTING,FrontStatusEnum.RUNNING,FrontStatusEnum.STOPPED);
            }else{
                newFrontList.forEach((tbFront -> {
                    this.frontService.updateStatus(tbFront.getFrontId(), FrontStatusEnum.ADD_FAILED);
                }));
            }
        } catch (Exception e) {
            log.error("Init host:[{}] list and start chain:[{}] error",host.getIp() ,chain.getChainName(), e);
        }

    }
    /**
     * if add new node or delete old node, update node's p2p list of ip
     * @param chainId
     * @param groupIdSet
     * @param optionType
     */
    private boolean restartFrontOfGroupSet(int chainId, Set<Integer> groupIdSet, OptionType optionType,
                                FrontStatusEnum frontBefore, FrontStatusEnum frontSuccess,FrontStatusEnum frontFailed ){
        List<TbFront> frontList = this.frontService.selectFrontListByGroupIdSet(groupIdSet);
        if (CollectionUtils.isEmpty(frontList)){
            log.info("No front of group id set:[{}]",JsonTools.toJSONString(groupIdSet));
            return false;
        }

        log.info("Restart front of group:[{}]",JsonTools.toJSONString(groupIdSet));

        // group front by host
        Map<Integer, List<TbFront>> hostFrontListMap = frontList.stream().collect(Collectors.groupingBy(TbFront::getHostId));

        // restart front by host
        return this.restartFrontByHost(chainId,optionType,hostFrontListMap, frontBefore,frontSuccess, frontFailed);
    }

    /**
     *
     * @param chainId
     * @param optionType
     * @return
     * @throws InterruptedException
     */
    private boolean restartChain(int chainId, OptionType optionType,
                                 FrontStatusEnum before, FrontStatusEnum success,FrontStatusEnum failed ) {
        // host of chain
        List<TbHost> hostList = this.hostService.selectHostListByChainId(chainId);

        // select all front of host
        List<TbFront> tbFrontList = hostList.stream()
                .map(host -> this.frontMapper.selectByHostId(host.getId())).flatMap(List::stream).collect(Collectors.toList());

        // group front by host
        Map<Integer, List<TbFront>> hostFrontListMap = tbFrontList.stream().collect(Collectors.groupingBy(TbFront::getHostId));

        // restart by host one by one
        return restartFrontByHost(chainId, optionType, hostFrontListMap, before, success, failed);
    }

    /**
     * start front after
     * @param chainId
     * @param optionType
     * @param hostFrontListMap
     * @return
     * @throws InterruptedException
     */
    private boolean restartFrontByHost(int chainId, OptionType optionType, Map<Integer, List<TbFront>> hostFrontListMap,
                                       FrontStatusEnum before, FrontStatusEnum success,FrontStatusEnum failed) {
        final CountDownLatch startLatch = new CountDownLatch(CollectionUtils.size(hostFrontListMap));

        final AtomicInteger totalFrontCount = new AtomicInteger(0);
        final AtomicInteger startSuccessCount = new AtomicInteger(0);

        // set maxWaitTime
        final AtomicLong maxWaitTime = new AtomicLong();

        hostFrontListMap.values().forEach(frontList -> {
            // add to total
            totalFrontCount.addAndGet(CollectionUtils.size(frontList));

            // set max wait time
            long estimateTimeOfHost = CollectionUtils.size(frontList) * constant.getDockerRestartPeriodTime();
            if ( estimateTimeOfHost > maxWaitTime.get()){
                maxWaitTime.set(estimateTimeOfHost);
            }
        });
        maxWaitTime.addAndGet(constant.getDockerRestartPeriodTime());

        for (Integer tbHostId : CollectionUtils.emptyIfNull(hostFrontListMap.keySet())) {
            threadPoolTaskScheduler.submit(() -> {
                List<TbFront> frontListToRestart = hostFrontListMap.get(tbHostId);
                //
                TbHost tbHost = this.tbHostMapper.selectByPrimaryKey(tbHostId);
                try {
                    for (TbFront front : CollectionUtils.emptyIfNull(frontListToRestart)) {
                        log.info("Start front:[{}:{}:{}].", front.getFrontIp(), front.getHostIndex(), front.getNodeId());
                        boolean startResult = this.frontService.restart(front.getNodeId(), optionType, before, success, failed);
                        if (startResult) {
                            log.info("Start front:[{}:{}:{}] success.", front.getFrontIp(), front.getHostIndex(), front.getNodeId());
                            startSuccessCount.incrementAndGet();
                        }
                        Thread.sleep(constant.getDockerRestartPeriodTime());
                    }
                } catch (Exception e) {
                    log.error("Start front on host:[{}] error", tbHost.getIp(), e);
                }finally {
                    startLatch.countDown();
                }
            });
        }
        boolean startSuccess = false;
        try {
            log.info("Wait:[{}] to restart all fronts.", maxWaitTime.get());
            startLatch.await(maxWaitTime.get(), TimeUnit.MILLISECONDS);
            startSuccess = startSuccessCount.get() == totalFrontCount.get();
        } catch (InterruptedException e) {
            log.error("Start front of chain:[{}] error",chainId, e);
        }

        // check if all host init success
        log.log(startSuccess ? Level.INFO: Level.ERROR,
                "Front of chain:[{}] init result, total:[{}], success:[{}]",
                chainId, totalFrontCount.get(), startSuccessCount.get());

        return startSuccess;
    }

}


