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

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.webank.webase.node.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.chain.ChainService;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbAgencyMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbChainMapper;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.entity.TbNode;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class NodeAsyncService {

    @Autowired private TbChainMapper tbChainMapper;
    @Autowired private TbAgencyMapper tbAgencyMapper;

    @Autowired private FrontService frontService;
    @Autowired private NodeService nodeService;
    @Autowired private ChainService chainService;
    @Autowired private HostService hostService;
    @Autowired private ConstantProperties constant;

    @Async("deployAsyncExecutor")
    public void startFrontOfChain(int chainId) {
        List<TbFront> frontList = this.frontService.selectFrontListByChainId(chainId);

        for (TbFront front : CollectionUtils.emptyIfNull(frontList)) {
            // ssh host and start docker container
            this.frontService.start(front.getNodeId());
            try {
                Thread.sleep(constant.getDockerRestartPeriodTime());
            } catch (InterruptedException e) {
                log.error("Start chain:[{}], docker restart server:[{}:{}] throws exception when sleep.",
                        chainId,front.getFrontIp(),front.getContainerName());
            }
        }


    }

    @Async("deployAsyncExecutor")
    public void startFrontOfGroup(int chainId, int groupId) {
        List<TbNode> tbNodeList = this.nodeService.selectNodeListByChainIdAndGroupId(chainId, groupId);

        for (TbNode tbNode : CollectionUtils.emptyIfNull(tbNodeList)) {
            // ssh host and start docker container
            this.frontService.start(tbNode.getNodeId());
            try {
                Thread.sleep(constant.getDockerRestartPeriodTime());
            } catch (InterruptedException e) {
                log.error("Start group:[{}:{}], docker restart server:[{}:{}] throws exception when sleep.",
                        chainId,groupId,tbNode.getNodeIp(),tbNode.getNodeId());
            }
        }
    }

    /**
     *
     * @param chainId
     * @param groupIdSet
     */
    @Async("deployAsyncExecutor")
    public void startFrontOfGroup(int chainId, Set<Integer> groupIdSet) {
        for (Integer groupId: CollectionUtils.emptyIfNull(groupIdSet)){
            ((NodeAsyncService) AopContext.currentProxy()).startFrontOfGroup(chainId,groupId);
        }
    }

    /**
     * TODO:  1. change to status machine; 2. update synchronized object
     *
     * @param chainName
     */
    @Async("deployAsyncExecutor")
    public void initHostListAndStart(String chainName) {
        boolean deploySuccess = true;
        try {
            TbChain chain = this.chainService.startDeploy(chainName);
            if(chain == null){
                log.error("No chain:[{}] to deploy.",chainName);
                deploySuccess = false;
                return;
            }

            // select all host
            List<TbHost> tbHostList = this.hostService.selectHostListByChainId(chain.getId());
            if (CollectionUtils.isEmpty(tbHostList)) {
                log.error("Chain:[{}:{}] has no host.", chain.getId(), chain.getChainName());
                deploySuccess = false;
                return;
            }

        } catch (Exception e) {
            // TODO
            e.printStackTrace();
            deploySuccess = false;

        } finally {
            // TODO

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


                            // TODO. call front service
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
                log.info("All bcos-front of chain:[{}] start success, start failed count:[{}].",  tbChain.getChainName(), startCount.get());
                chainService.updateStatus(tbChain.getId(), ChainStatusEnum.DEPLOY_SUCCESS);
            } else {
                log.error("[{}] bcos-front of chain:[{}] start failed.", startCount.get(), tbChain.getChainName());
                chainService.updateStatus(tbChain.getId(), ChainStatusEnum.DEPLOY_FAILED);
            }
        } catch (InterruptedException e) {
            chainService.updateStatus(tbChain.getId(), ChainStatusEnum.DEPLOY_FAILED);
            log.error("CountDownLatch wait for bcos-front of chain:[{}] start timeout, left count:[{}].",  chainName,dockerStartLatch.getCount());
        }
    }


    public boolean initHost(TbChain chain, List<TbHost> tbHostList) {
        // init host
        // 1. install docker and docker-compose,
        // 2. send node config to remote host
        // 3. docker pull image
        boolean isInitSuccess = this.hostService.initHosts(chain, tbHostList);
        if (!isInitSuccess) {
            // init host failed
            log.error("Chain:[{}:{}] has no host.", chain.getId(), chain.getChainName());
        }
        return isInitSuccess;
    }

    public void startHost(String chainName) {

    }
}


