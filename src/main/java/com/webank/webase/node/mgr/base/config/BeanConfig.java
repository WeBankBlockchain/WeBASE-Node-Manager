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
package com.webank.webase.node.mgr.base.config;

import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.properties.ExecutorProperties;
import com.webank.webase.node.mgr.executor.VisiableThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

/**
 * config about bean.
 */
@Log4j2
@Configuration
public class BeanConfig {

    @Autowired
    private ExecutorProperties executorProperties;
    @Autowired
    private ConstantProperties constantProperties;

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }

    /**
     * resttemplate for generic http request.
     */
    @Bean(name = "genericRestTemplate")
    public RestTemplate getRestTemplate() {
        SimpleClientHttpRequestFactory factory = getHttpFactoryForDeploy();
        factory.setReadTimeout(constantProperties.getHttpTimeOut());// ms
        factory.setConnectTimeout(constantProperties.getHttpTimeOut());// ms
        return new RestTemplate(factory);
    }

    /**
     * resttemplate for deploy contract.
     */
    @Bean(name = "deployRestTemplate")
    public RestTemplate getDeployRestTemplate() {
        SimpleClientHttpRequestFactory factory = getHttpFactoryForDeploy();
        factory.setReadTimeout(constantProperties.getContractDeployTimeOut());// ms
        factory.setConnectTimeout(constantProperties.getContractDeployTimeOut());// ms
        return new RestTemplate(factory);
    }

    /**
     * factory for deploy.
     */
    @Bean()
    @Scope("prototype")
    public SimpleClientHttpRequestFactory getHttpFactoryForDeploy() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        return factory;
    }

    /**
     * init thread pool.
     */
    @Bean
    public Executor asyncServiceExecutor() {
        log.info("start asyncServiceExecutor");
        ThreadPoolTaskExecutor executor = new VisiableThreadPoolTaskExecutor();
        executor.setCorePoolSize(executorProperties.getCorePoolSize());
        executor.setMaxPoolSize(executorProperties.getMaxPoolSize());
        executor.setQueueCapacity(executorProperties.getQueueSize());
        executor.setThreadNamePrefix(executorProperties.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // init executor
        executor.initialize();
        return executor;
    }
}
