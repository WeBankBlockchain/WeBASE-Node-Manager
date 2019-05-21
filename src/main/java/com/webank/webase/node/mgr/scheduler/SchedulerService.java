/*
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.scheduler;

import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

/**
 * all timed task.
 */
@Component
@EnableScheduling
public class SchedulerService implements SchedulingConfigurer {

    @Autowired
    private StatisticsTransdailyTask statisticsTask;
    @Autowired
    private CheckNodeTask checkNodeTask;
    @Autowired
    private DeleteBlockTask deleteBlockTask;
    @Autowired
    private DeleteTransHashTask deleteTransHashTask;
    @Autowired
    private TransMonitorTask transMonitorTask;
    @Autowired
    private SharedChainInfoTask sharedChainInfoTask;
    @Autowired
    private DeleteMonitorInfoTask deleteMonitorInfoTask;
    @Autowired
    private ConstantProperties constants;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(() -> statisticsTask.updateTransdailyData(),
            (context) -> new CronTrigger(constants.getStatisticsTransDailyCron())
                .nextExecutionTime(context));

        taskRegistrar.addTriggerTask(() -> checkNodeTask.checkNodeStatus(),
            (context) -> new CronTrigger(constants.getCheckNodeStatusCron())
                .nextExecutionTime(context));

        taskRegistrar.addTriggerTask(() -> deleteBlockTask.deleteBlockInfo(),
            (context) -> new CronTrigger(constants.getDeleteInfoCron())
                .nextExecutionTime(context));

        taskRegistrar.addTriggerTask(() -> deleteTransHashTask.deleteTransHash(),
            (context) -> new CronTrigger(constants.getDeleteInfoCron())
                .nextExecutionTime(context));

        taskRegistrar.addTriggerTask(() -> deleteMonitorInfoTask.deleteMonitorInfo(),
            (context) -> new CronTrigger(constants.getDeleteInfoCron())
                .nextExecutionTime(context));

        taskRegistrar.addTriggerTask(() -> transMonitorTask.monitorInfoHandle(),
            (context) -> new CronTrigger(constants.getInsertTransMonitorCron())
                .nextExecutionTime(context));

        if (constants.getSupportTransaction()) {
            taskRegistrar.addTriggerTask(() -> sharedChainInfoTask.shareAllNetworkInfo(),
                (context) -> new CronTrigger(constants.getSharedChainInfoCron())
                    .nextExecutionTime(context));
        }

    }
}