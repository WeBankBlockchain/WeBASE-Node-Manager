/*
 * Copyright 2014-2020 the original author or authors.
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

package com.webank.webase.node.mgr.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.jni.common.JniException;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.config.ConfigOption;
import org.fisco.bcos.sdk.v3.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.v3.config.model.ConfigProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * init web3sdk.
 *
 */
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "sdk")
public class Web3Config {

    private String threadPoolSize;
    private String certPath;
    private String useSmSsl;
    private List<String> peers;


    @Bean
    public ConfigOption getConfigOptionFromFile() throws ConfigException {
        log.info("start init ConfigProperty");
        // cert config, encrypt type
        Map<String, Object> cryptoMaterial = new HashMap<>();
        cryptoMaterial.put("certPath", certPath);
        cryptoMaterial.put("useSMCrypto", useSmSsl);

        // peers, default one node in front
        Map<String, Object> network = new HashMap<>();
        network.put("peers", peers);

        // thread pool config
        log.info("init thread pool property");
        Map<String, Object> threadPool = new HashMap<>();
        threadPool.put("threadPoolSize", threadPoolSize);

        // init property
        ConfigProperty configProperty = new ConfigProperty();
        configProperty.setCryptoMaterial(cryptoMaterial);
        configProperty.setNetwork(network);
        configProperty.setThreadPool(threadPool);
        // init config option
        ConfigOption configOption = new ConfigOption(configProperty);
        log.info("initConfigOptionFromFile init configOption :{}", configOption);
        return configOption;
    }

    @Bean
    public BcosSDK getBcosSDK(ConfigOption configOption) {
        return new BcosSDK(configOption);
    }

    /**
     * only used to get groupList
     * @throws JniException
     */
    @Bean(name = "rpcClient")
    public Client getRpcWeb3j(ConfigOption configOption) throws JniException {

        Client rpcWeb3j = Client.build(configOption);
        // Client rpcWeb3j = bcosSDK.getClient();
        log.info("get rpcWeb3j(only support groupList) client:{}", rpcWeb3j);
        return rpcWeb3j;
    }

}
