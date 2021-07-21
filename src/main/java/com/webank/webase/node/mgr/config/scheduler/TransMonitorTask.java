/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.config.scheduler;

import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.group.entity.TbGroup;
import com.webank.webase.node.mgr.monitor.MonitorService;
import com.webank.webase.node.mgr.statistic.StatService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * using in async monitor
 * 交易审计
 */
@Log4j2
@Component
public class TransMonitorTask {

    @Autowired
    private MonitorService monitorService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private StatService statService;

    @Scheduled(fixedRateString = "${constant.transMonitorTaskFixedRate}")
    public void taskStart() {
        monitorStart();
    }

    @Scheduled(fixedRateString = "${constant.statBlockFixedDelay}")
    public void statTaskStart() {
        blockStatStart();
    }

    /**
     * start monitor.
     */
    public void monitorStart() {
        Instant startTime = Instant.now();
        log.debug("=== start monitor. startTime:{}", startTime.toEpochMilli());
        //get group list
        List<TbGroup> groupList = groupService.getGroupList(DataStatus.NORMAL.getValue());
        if (groupList == null || groupList.size() == 0) {
            log.warn("monitor jump over, not found any group");
            return;
        }
        // count down group, make sure all group's transMonitor finished
        CountDownLatch latch = new CountDownLatch(groupList.size());
        groupList
            .forEach(group -> monitorService.transMonitorByGroupId(latch, group.getGroupId()));

        try {
             latch.await();
        } catch (InterruptedException ex) {
            log.error("InterruptedException", ex);
            Thread.currentThread().interrupt();
        }

        log.debug("=== end monitor. useTime:{} ",
            Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * start monitor.
     */
    public void blockStatStart() {
        Instant startTime = Instant.now();
        log.debug("=== start blockStat. startTime:{}", startTime.toEpochMilli());
        //get group list
        List<TbGroup> groupList = groupService.getGroupList(DataStatus.NORMAL.getValue());
        if (groupList == null || groupList.size() == 0) {
            log.warn("blockStat jump over, not found any group");
            return;
        }
        // count down group, make sure all group's transMonitor finished
        CountDownLatch latch = new CountDownLatch(groupList.size());
        groupList
            .forEach(group -> statService.pullBlockStatistic(latch, group.getGroupId()));

        try {
             latch.await();
        } catch (InterruptedException ex) {
            log.error("InterruptedException", ex);
            Thread.currentThread().interrupt();
        }

        log.debug("=== end blockStat. useTime:{} ",
            Duration.between(startTime, Instant.now()).toMillis());
    }
}
