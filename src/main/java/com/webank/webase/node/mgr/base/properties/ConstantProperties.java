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
package com.webank.webase.node.mgr.base.properties;

import java.math.BigInteger;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * constants.
 */
@Data
@Component
@ConfigurationProperties(prefix = ConstantProperties.CONSTANT_PREFIX)
public class ConstantProperties {

    @Autowired
    private ConstantProperties constants;

    // constant
    public static final String CONSTANT_PREFIX = "constant";
    public static final String COOKIE_JSESSIONID = "JSESSIONID"; // cookie key---session
    public static final String COOKIE_MGR_ACCOUNT = "NODE_MGR_ACCOUNT_C"; // cookie key---account
    public static final String SESSION_MGR_ACCOUNT = "NODE_MGR_ACCOUNT_S"; // session key---account
    public static final String CONTRACT_NAME_ZERO = "0x00000000";
    public static final String ADDRESS_DEPLOY = "0x0000000000000000000000000000000000000000";
    public static final String LOGIN_CHECKCODE_SESSION_KEY = "NODE_MGR_CHECK_CODE_S";
    public static final int PUBLICKEY_LENGTH = 130;
    public static final int ADDRESS_LENGTH = 42;
    public static final String HAS_ROLE_ADMIN = "hasRole('admin')";


    private BigInteger transRetainMax = new BigInteger("10000");
    private String groupInvalidGrayscaleValue;  //y:year, M:month, d:day of month, h:hour, m:minute, n:forever valid
    private String notSupportFrontIp;

    //block into
    private BigInteger blockRetainMax = new BigInteger("10000");
    private BigInteger pullBlockInitCnts = new BigInteger("100");
    private Long pullBlockSleepTime = 20L; //20 mills
    private Boolean isBlockPullFromZero = false;

    //receive http request
    private Integer verificationCodeMaxAge = 300; // seconds
    private Integer authTokenMaxAge = 900; // seconds
    private Boolean isUseSecurity = true;
    private String ignoreCheckFront = null;

    //front http request
    private String frontUrl;
    private Integer contractDeployTimeOut = 30000;
    private Integer httpTimeOut = 5000;
    private Boolean isPrivateKeyEncrypt = true;
    private Integer maxRequestFail = 3;
    private Long sleepWhenHttpMaxFail = 60000L;  //default 1min

    //transaction monitor
    private Long transMonitorTaskFixedRate = 60000L; //second
    private Integer monitorInfoRetainMax;
    private Long analysisSleepTime = 200L;
    private Boolean isMonitorIgnoreUser = false;
    private Boolean isMonitorIgnoreContract = false;
    private Integer monitorUnusualMaxCount;

}