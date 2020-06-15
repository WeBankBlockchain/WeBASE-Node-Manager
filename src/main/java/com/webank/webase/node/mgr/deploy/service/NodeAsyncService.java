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

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.deploy.mapper.TbChainMapper;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.entity.TbNode;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class NodeAsyncService {

    @Autowired private TbChainMapper chainMapper;

    @Autowired private FrontService frontService;
    @Autowired private NodeService nodeService;
    @Autowired private ConstantProperties constant;

    @Async
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

    @Async
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
}

