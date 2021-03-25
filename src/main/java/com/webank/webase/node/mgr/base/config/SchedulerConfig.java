package com.webank.webase.node.mgr.base.config;

import com.webank.webase.node.mgr.base.properties.SchedulerProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
@Log4j2

public class SchedulerConfig implements SchedulingConfigurer {

    @Autowired
    private SchedulerProperties schedulerProperties;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(mgrTaskScheduler());
    }

    /**
     * thread pool for scheduler parallel task (not async):
     * pull block, trans monitor, statistic trans, delete info, reset groupList
     * @return ThreadPoolTaskScheduler
     */
    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler mgrTaskScheduler() {
        log.info("start mgrTaskScheduler init..");
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(schedulerProperties.getPoolSize());
        scheduler.setThreadNamePrefix(schedulerProperties.getThreadNamePrefix());
        scheduler.setAwaitTerminationSeconds(schedulerProperties.getAwaitTerminationSeconds());
        scheduler.setWaitForTasksToCompleteOnShutdown(
                schedulerProperties.getWaitForTasksToCompleteOnShutdown());
        return scheduler;
    }

}
