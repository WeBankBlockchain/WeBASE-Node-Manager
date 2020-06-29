/**
 * Copyright 2014-2020  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.node.mgr.base.properties;

import static java.io.File.separator;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * constants.
 */
@Log4j2
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

    // alert mail interval
    private Integer auditMonitorTaskFixedDelay;
    private Integer nodeStatusMonitorTaskFixedDelay;
    private Integer certMonitorTaskFixedDelay;

    //******************* Add in v1.4.0 start. *******************
    public static final boolean RETURN_EXECUTE_LOG = true;

    private boolean useDockerSDK = false;
    public int dockerDaemonPort = 3000;
    public String sshDefaultUser = "root";
    public int sshDefaultPort = 22;

    // shell script
    private String nodeOperateShell = "./script/deploy/host_operate.sh";
    private String buildChainShell = "./script/deploy/build_chain.sh";
    private String genAgencyShell = "./script/deploy/gen_agency_cert.sh";
    private String genNodeShell = "./script/deploy/gen_node_cert.sh";
    private String scpShell =        "./script/deploy/file_trans_util.sh";
    private String privateKey = System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "id_rsa";
    private String fiscoBcosBinary =  "";

    // default port
    // TODO. write tbchain's id in db into config.ini
    private int defaultChainId = 1;
    private int defaultJsonrpcPort = 8545;
    private int defaultP2pPort = 30300;
    private int defaultChannelPort = 20200;
    private int defaultFrontPort = 5002;

    // timeout config
    private long execHostInitTimeout = 2 * 60 * 60 * 1000L;
    private long startNodeTimeout = 5 * 60 * 1000L;
    private long execBuildChainTimeout = 10 * 60 * 1000L;
    private long execShellTimeout = 2 * 60 * 1000L;
    private long dockerRestartPeriodTime = 60 * 1000L;
    private int dockerClientConnectTimeout = 10 * 60 * 1000;
    private int dockerPullTimeout = 10 * 60 * 1000;
    private int dockerClientReadTimeout = 10 * 60 * 1000;

    private String[] permitUrlArray = new String[]{"/account/login", "/account/pictureCheckCode", "/login", "/user/privateKey/**", "/encrypt"};
    private String dockerRepository= "fiscoorg/front";
    private String imageTagUpdateUrl = "https://registry.hub.docker.com/v1/repositories/%s/tags";
    private String dockerRegistryMirror = "";
    private String nodesRootDir = "NODES_ROOT";
    private String nodesRootTmpDir = "NODES_ROOT_TMP";

    /**
     * Docker client connect daemon ip with proxy ip.
     */
    private Map<String, MutablePair<String, Integer>> dockerProxyMap = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        nodesRootDir = initDirectory(nodesRootDir, "NODES_ROOT/");
        nodesRootTmpDir = initDirectory(nodesRootTmpDir, "NODES_ROOT_TMP/");

        log.info("Init constant properties, generate nodes root dir:[{}]", nodesRootDir);
        log.info("Init constant properties, generate nodes root temp dir:[{}]", nodesRootTmpDir);


        this.imageTagUpdateUrl = String.format(this.imageTagUpdateUrl,dockerRepository);
        log.info("Init constant properties, imageTagUpdateUrl: [{}]", this.imageTagUpdateUrl);

        log.info("Init constant properties, permitUrlArray: [{}]", StringUtils.join(permitUrlArray,","));

        log.info("Init constant properties, dockerProxyMap: [{}]", dockerProxyMap);

        log.info("Init constant properties, check FISCO-BCOS binary path: [{}]", fiscoBcosBinary);
        if (!Files.exists(Paths.get(fiscoBcosBinary))) {
            log.warn("FISCO-BCOS binary path: [{}] not exists.", fiscoBcosBinary);
            fiscoBcosBinary = "";
        }

        log.info("Init constant properties, private key: [{}]", privateKey);

        log.info("Init constant properties, defaultP2pPort:[{}], defaultChannelPort:[{}], defaultJsonrpcPort:[{}], defaultFrontPort:[{}]",
                defaultP2pPort, defaultChannelPort, defaultJsonrpcPort, defaultFrontPort);
    }

    /**
     *
     * @param injectedValue
     * @param defaultValue
     * @return
     */
    private static String initDirectory(String injectedValue, String defaultValue){
        String newDirectory = injectedValue;

        if (StringUtils.isBlank(newDirectory)) {
            newDirectory = defaultValue;
        }

        if (newDirectory.trim().endsWith(separator)) {
            // ends with separator
            newDirectory = newDirectory.trim();
        } else {
            // append a separator
            newDirectory = String.format("%s%s", newDirectory.trim(), separator);
        }

        if (! newDirectory.startsWith("/")){
            // not an absolute path
            return String.format("%s/%s",new File(".").toPath().toAbsolutePath().toString(), newDirectory);
        }
        return newDirectory;
    }
    //******************* Add in v1.4.0 end. *******************
}