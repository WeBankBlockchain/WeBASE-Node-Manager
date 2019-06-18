/**
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.node.mgr.scheduler;

import com.webank.webase.node.mgr.block.BlockService;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroup;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroupMapCache;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * get block info from chain.
 */
@Log4j2
@Component
public class PullBlockInfoTask {

    @Autowired
    private BlockService blockService;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;
    private CountDownLatch latch;

    /**
     * task start
     */
    public synchronized void pullBlockStart() {
        Instant startTime = Instant.now();
        log.info("start pullBLock startTime:{}", startTime.toEpochMilli());
        List<FrontGroup> groupList = frontGroupMapCache.getAllMap();
        if (groupList == null || groupList.size() == 0) {
            log.info("pullBlock jump over .not found any group");
            return;
        }

        latch = new CountDownLatch(groupList.size());
        groupList.stream()
            .forEach(group -> blockService.pullBlockByGroupId(latch, group.getGroupId()));

        try {
            boolean result = latch.await(5, TimeUnit.MINUTES);//5min
            log.debug("pull block latch result:{}", result);
        } catch (InterruptedException ex) {
            log.error("InterruptedException", ex);
            Thread.currentThread().interrupt();
        }

        log.info("end pullBLock useTime:{} ",
            Duration.between(startTime, Instant.now()).toMillis());
    }
}
