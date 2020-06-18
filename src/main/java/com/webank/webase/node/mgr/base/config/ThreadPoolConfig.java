package com.webank.webase.node.mgr.base.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.webank.webase.node.mgr.base.properties.ExecutorProperties;
import com.webank.webase.node.mgr.base.properties.SchedulerProperties;

import lombok.extern.log4j.Log4j2;

/**
 * 定时任务并行执行
 **/
@Configuration
@EnableScheduling
@Log4j2
public class ThreadPoolConfig {

    @Autowired
    private ExecutorProperties executorProperties;
    @Autowired
    private SchedulerProperties schedulerProperties;

    /**
     * pull block and trans from chain async
     * @return ThreadPoolTaskExecutor
     */
    @Bean
    public ThreadPoolTaskExecutor mgrAsyncExecutor() {
        log.info("start mgrAsyncExecutor init..");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(executorProperties.getCorePoolSize());
        executor.setMaxPoolSize(executorProperties.getMaxPoolSize());
        executor.setQueueCapacity(executorProperties.getQueueSize());
        executor.setThreadNamePrefix(executorProperties.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // init executor
        executor.initialize();
        return executor;
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


    @Bean
    public ThreadPoolTaskScheduler deployAsyncScheduler() {
        log.info("start deployAsyncScheduler init...");
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.afterPropertiesSet();
        scheduler.setThreadNamePrefix("ThreadPoolTaskScheduler-async-deploy:");
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return scheduler;
    }
}
