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

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Level;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import com.webank.webase.node.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.chain.ChainService;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.front.FrontMapper;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapMapper;
import com.webank.webase.node.mgr.node.NodeService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class NodeAsyncService {

    @Autowired private FrontMapper frontMapper;
    @Autowired private FrontGroupMapMapper frontGroupMapMapper;

    @Autowired private FrontService frontService;
    @Autowired private NodeService nodeService;
    @Autowired private ChainService chainService;
    @Autowired private HostService hostService;
    @Autowired private ConstantProperties constant;

    @Qualifier(value = "deployAsyncScheduler")
    @Autowired private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    /**
     *
     * @param chainId
     */
    @Async("deployAsyncScheduler")
    public void upgradeStartChain(int chainId)   {
        try {
            final boolean startSuccess = this.startFrontOfChain(chainId);
            final ChainStatusEnum chainStatusEnum = startSuccess ? ChainStatusEnum.RUNNING : ChainStatusEnum.UPGRADING_FAILED;
            threadPoolTaskScheduler.schedule(()->{
                chainService.updateStatus(chainId,chainStatusEnum);
            }, Instant.now().plusMillis( constant.getDockerRestartPeriodTime()));
    } catch (InterruptedException e) {
            log.error("Upgrade start chain:[{}] failed.", chainId, e);
        }
    }


    /**
     *
     * @param chainId
     * @param groupId
     */
    @Async("deployAsyncScheduler")
    public void startFrontOfGroupSet(int chainId, int groupId ) {
        List<TbFront> frontList = this.frontService.selectFrontListByGroupId(groupId);

        for (TbFront tbFront : CollectionUtils.emptyIfNull(frontList)) {
            try {
                // ssh host and start docker container
                this.frontService.restart(tbFront.getNodeId());
                Thread.sleep(constant.getDockerRestartPeriodTime());
            } catch (InterruptedException e) {
                log.error("Start group:[{}:{}], docker restart server:[{}:{}] throws exception when sleep.",
                        chainId, groupId, tbFront.getFrontIp(), tbFront.getNodeId());
            }
        }
    }

    /**
     * @param chainId
     * @param groupIdSet
     */
    @Async("deployAsyncScheduler")
    public void startFrontOfGroupSet(int chainId, Set<Integer> groupIdSet) {
        for (Integer groupId : CollectionUtils.emptyIfNull(groupIdSet)) {
            ((NodeAsyncService) AopContext.currentProxy()).startFrontOfGroupSet(chainId, groupId );
        }
    }

    @Async("deployAsyncScheduler")
    public void initHostAndStart(TbChain chain,TbHost host,int groupId) {
        try {
            boolean initSuccess = this.hostService.initHostList(chain, Arrays.asList(host), false);
            log.info("Init host:[{}], result:[{}]",host.getIp(), initSuccess);
            if (initSuccess) {
                // start node
                this.startFrontOfGroupSet(chain.getId(),groupId);
            }
        } catch (InterruptedException e) {
            log.error("Init host:[{}] list and start chain:[{}] error",host.getIp() ,chain.getChainName(), e);
        }

    }

    /**
     *
     * @param chainName
     */
    @Async("deployAsyncScheduler")
    public void initHostListAndStart(String chainName) {
        TbChain chain = null;
        try {
            // chainge chain status to deploying
            chain = this.chainService.changeChainStatusToDeploying(chainName);
            if (chain == null) {
                log.error("No chain:[{}] to deploy.", chainName);
                return;
            }

            // select all host to init
            List<TbHost> tbHostList = this.hostService.selectHostListByChainId(chain.getId());
            if (CollectionUtils.isEmpty(tbHostList)) {
                log.error("Chain:[{}:{}] has no host.", chain.getId(), chain.getChainName());
                return;
            }

            // init host
            // 1. install docker and docker-compose,
            // 2. send node config to remote host
            // 3. docker pull image
            boolean deploySuccess = this.hostService.initHostList(chain, tbHostList, true);
            if (deploySuccess){
                // start node
                deploySuccess = this.startFrontOfChain(chain.getId());

                final int chainId = chain.getId();
                final ChainStatusEnum chainStatusEnum = deploySuccess ? ChainStatusEnum.RUNNING : ChainStatusEnum.DEPLOY_FAILED;
                threadPoolTaskScheduler.schedule(()->{
                    chainService.updateStatus(chainId,chainStatusEnum);
                }, Instant.now().plusMillis( constant.getDockerRestartPeriodTime()));
            }
        } catch (Exception e) {
            log.error("Init host list and start chain:[{}] error", chainName, e);
            chainService.updateStatus(chain.getId(), ChainStatusEnum.DEPLOY_FAILED);
        }
    }

    /**
     *
     * @param chainId
     * @return
     * @throws InterruptedException
     */
    private boolean startFrontOfChain(int chainId) throws InterruptedException {
        // host of chain
        List<TbHost> hostList = this.hostService.selectHostListByChainId(chainId);

        final CountDownLatch startLatch = new CountDownLatch(CollectionUtils.size(hostList));
        AtomicInteger startSuccessCount = new AtomicInteger(0);
        AtomicInteger totalFrontCount = new AtomicInteger(0);

        for (TbHost tbHost : CollectionUtils.emptyIfNull(hostList)) {
            List<TbFront> tbFrontList = this.frontMapper.selectByHostId(tbHost.getId());
            // add to total
            totalFrontCount.addAndGet(CollectionUtils.size(tbFrontList));

            for (TbFront front : CollectionUtils.emptyIfNull(tbFrontList)) {
                log.info("Start front:[{}:{}:{}].",front.getFrontIp(),front.getHostIndex(),front.getNodeId());
                try {
                    boolean startResult = this.frontService.restart(front.getNodeId());
                    if (startResult){
                        log.info("Start front:[{}:{}:{}] success.",front.getFrontIp(),front.getHostIndex(),front.getNodeId());
                        startSuccessCount.incrementAndGet();
                    }
                    Thread.sleep(constant.getDockerRestartPeriodTime());
                } catch (Exception e) {
                    log.error("Start front:[{}] error",front.getFrontIp(),front.getHostIndex(), e);
                }finally {
                    startLatch.countDown();
                }
            }
        }

        startLatch.await(constant.getStartNodeTimeout(), TimeUnit.MILLISECONDS);

        boolean startSuccess = startSuccessCount.get() == totalFrontCount.get();
        // check if all host init success
        log.log(startSuccess ? Level.INFO: Level.ERROR,
                "Host of chain:[{}] init result, total:[{}], success:[{}]",
                chainId, totalFrontCount.get(), startSuccessCount.get());

        return startSuccess;
    }
}


