/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.node.mgr.config;


import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * guomi configuration in web3sdk
 * sdk switch ecdsa to sm2, sha to sm3
 */
@Data
@Configuration
public class EncryptTypeConfig {

    @Bean
    public Map<Integer, CryptoSuite> getCryptoSuite() {
        Map<Integer, CryptoSuite> cryptoSuiteMap = new HashMap<>();
        cryptoSuiteMap.put(CryptoType.ECDSA_TYPE, new CryptoSuite(CryptoType.ECDSA_TYPE));
        cryptoSuiteMap.put(CryptoType.SM_TYPE, new CryptoSuite(CryptoType.SM_TYPE));
        return cryptoSuiteMap;
    }
}
