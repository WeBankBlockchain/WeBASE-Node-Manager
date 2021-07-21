/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.base.config.scheduler;

import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.block.BlockService;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * get block info and trans info from chain.
 * including tb_block and tb_trans (block contains trans)
 */
@Log4j2
@Component
public class PullBlockTransTask {

    @Autowired
    private BlockService blockService;
    @Autowired
    private GroupService groupService;

    @Scheduled(fixedDelayString = "${constant.pullBlockTaskFixedDelay}")
    public void taskStart() {
        pullBlockStart();
    }

    /**
     * task start
     */
    public synchronized void pullBlockStart() {
        Instant startTime = Instant.now();
        log.debug("start pullBLock startTime:{}", startTime.toEpochMilli());
        List<TbGroup> groupList = groupService.getGroupList(GroupStatus.NORMAL.getValue());
        if (groupList == null || groupList.size() == 0) {
            log.warn("pullBlock jump over: not found any group");
            return;
        }
        // count down group, make sure all group's pullBlock finished
        CountDownLatch latch = new CountDownLatch(groupList.size());
        groupList
                .forEach(group -> blockService.pullBlockByGroupId(latch, group.getGroupId()));

        try {
            latch.await();
        } catch (InterruptedException ex) {
            log.error("InterruptedException", ex);
            Thread.currentThread().interrupt();
        }

        log.debug("end pullBLock useTime:{} ",
                Duration.between(startTime, Instant.now()).toMillis());
    }
}
