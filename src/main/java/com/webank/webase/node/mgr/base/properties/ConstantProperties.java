/**
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
package com.webank.webase.node.mgr.base.properties;

import java.math.BigInteger;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * constants.
 */
@Data
@Component
@ConfigurationProperties(prefix = ConstantProperties.CONSTANT_PREFIX)
public class ConstantProperties {

    // constant
    public static final String CONSTANT_PREFIX = "constant";
    public static final String COOKIE_JSESSIONID = "JSESSIONID"; // cookie key---session
    public static final String COOKIE_MGR_ACCOUNT = "NODE_MGR_ACCOUNT_C"; // cookie key---account
    public static final String SESSION_MGR_ACCOUNT = "NODE_MGR_ACCOUNT_S"; // session key---account
    public static final String CONTRACT_NAME_ZERO = "0x00000000";
    public static final int PUBLICKEY_LENGTH = 130;
    public static final int ADDRESS_LENGTH = 42;

    // scheduler
    private BigInteger blockRetainMax = new BigInteger("10000");
    private BigInteger transRetainMax = new BigInteger("10000");
    private String statisticsTransDailyCron = "0 0/1 * * * ?";// Execute once every minute
    private String deleteInfoCron = "0 0/2 * * * ?";// Execute once every two minute
    private String insertTransMonitorCron = "0 0/10 * * * ?";
    private Long resetGroupListCycle = 600000L; //10 min
    private Long pullBlockSleepTime = 20L; //20 mills
    private Boolean isBlockPullFromZero = false;
    private Integer maxRequestFail = 3;
    private Long sleepWhenHttpMaxFail = 60000L;  //default 1min


    // http
    private Integer contractDeployTimeOut = 30000;
    private Integer httpTimeOut = 5000;

    // AES
    private String aesKey = "a3LdMfg123C56z9r";

    // COOKIE
    private Integer cookieMaxAge = 900; // seconds
}