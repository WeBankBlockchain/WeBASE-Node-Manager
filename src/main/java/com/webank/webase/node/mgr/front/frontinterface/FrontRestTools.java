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

package com.webank.webase.node.mgr.front.frontinterface;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.frontinterface.entity.FailInfo;
import com.webank.webase.node.mgr.front.frontinterface.entity.FrontUrlInfo;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapCache;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroup;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.tools.JsonTools;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * about http request for WeBASE-Front.
 */
@Log4j2
@Service
public class FrontRestTools {

    //public static final String FRONT_URL = "http://%1s:%2d/WeBASE-Front/%3s";
    public static final String FRONT_TRANS_RECEIPT_BY_HASH_URI = "web3/transactionReceipt/%1s";
    public static final String URI_BLOCK_BY_NUMBER = "web3/blockByNumber/%1d";
    public static final String URI_BLOCK_TRANS_COUNT_BY_NUMBER = "web3/blockTransCnt/%1d";
    public static final String URI_BLOCK_STAT_BY_NUMBER = "web3/blockStat/%1d";
    public static final String URI_BLOCK_BY_HASH = "web3/blockByHash/%1s";
    public static final String URI_TRANS_TOTAL = "web3/transaction-total";
    public static final String URI_TRANS_BY_HASH = "web3/transaction/%1s";
    public static final String URI_GROUP_PEERS = "web3/groupPeers";
    public static final String URI_GROUP_PLIST = "web3/groupList";
    public static final String URI_PEERS = "web3/peers";
    public static final String URI_CONSENSUS_STATUS = "web3/consensusStatus";
    public static final String URI_SYNC_STATUS = "web3/syncStatus";
    public static final String URI_SYSTEM_CONFIG = "web3/systemConfigByKey/%1s";
    public static final String URI_CODE = "web3/code/%1s/%2s";
    public static final String URI_CODE_V2 = "web3/code";
    public static final String URI_BLOCK_NUMBER = "web3/blockNumber";
    public static final String URI_GET_SEALER_LIST = "web3/sealerList";
    public static final String URI_GET_SEALER_LIST_WEIGHT = "web3/sealerList/weight";
    public static final String URI_GET_OBSERVER_LIST = "web3/observerList";
    public static final String URI_GET_CLIENT_VERSION = "web3/clientVersion";
    public static final String URI_REFRESH_FRONT = "web3/refresh";
    public static final String URI_SEARCH_BLOCK_OR_TX = "web3/search";
    public static final String URI_NODE_CONFIG = "web3/peersConfig";
    public static final String URI_GROUP_INFO = "web3/groupInfo";
    public static final String URI_GROUP_NODE_INFO = "web3/groupNodeInfo";
    public static final String URI_BINARY_VERSION = "web3/binaryVersion";
    public static final String URI_NODE_STATUS_LIST = "web3/nodeStatusList";
    public static final String URI_ENCRYPT_TYPE = "web3/encrypt";
    public static final String URI_IS_WASM = "web3/isWasm";
    public static final String URI_USE_SM_SSL = "web3/useSmSsl";

    // config sdk
    public static final String URI_CONFIG_SDK = "config/bcosSDK";

    public static final String URI_KEY_PAIR = "privateKey";
    public static final String URI_KEY_PAIR_LOCAL_KEYSTORE = "privateKey/localKeyStores";
    public static final String URI_KEY_PAIR_IMPORT_WITH_SIGN = "privateKey/importWithSign";
    public static final String URI_KEY_PAIR_EXPORT_PEM_WITH_SIGN = "privateKey/exportPem";
    public static final String URI_KEY_PAIR_EXPORT_P12_WITH_SIGN = "privateKey/exportP12";
    public static final String URI_KEY_PAIR_USERINFO_WITH_SIGN = "privateKey/userInfoWithSign";
    public static final String URI_CONTRACT_DEPLOY_WITH_SIGN = "contract/deployWithSign";
    public static final String URI_CONTRACT_REGISTER_CNS = "contract/registerCns";
    public static final String URI_CONTRACT_SENDABI = "contract/abiInfo";
    // liquid contract
    public static final String URI_CONTRACT_LIQUID_CHECK = "contract/liquid/check";
    public static final String URI_CONTRACT_LIQUID_COMPILE = "contract/liquid/compile";
    public static final String URI_CONTRACT_LIQUID_COMPILE_CHECK = "contract/liquid/compile/check";

    public static final String URI_SEND_TRANSACTION_WITH_SIGN = "trans/handleWithSign";
    public static final String URI_CHAIN = "chain";

    /**
     * rpc precompiled
     */
    // 1)sys config
    public static final String RPC_PRECOM_SYS_CONFIG_LIST = "precntauth/precompiled/sys/config/list";
    public static final String RPC_PRECOM_SYS_CONFIG = "precntauth/precompiled/sys/config";
    // 2)CNS
    public static final String RPC_PRECOM_CNS_ADDRESS_BY_NAME_VERSION = "precntauth/precompiled/cns/reqAddressInfoByNameVersion";
    public static final String RPC_PRECOM_CNS_REGISTER = "precntauth/precompiled/cns/register";
    public static final String RPC_PRECOM_CNS_CNSINFO_BY_NAME_VERSION = "precntauth/precompiled/cns/queryCnsByNameVersion";
    public static final String RPC_PRECOM_CNS_CNSINFO_BY_NAME = "precntauth/precompiled/cns/queryCnsByName";
    // 3)conseensus
    public static final String RPC_PRECOM_CONSENSUS_LIST = "precntauth/precompiled/consensus/list";
    public static final String RPC_PRECOM_CONSENSUS_MGR = "precntauth/precompiled/consensus/manage";
    // 4)CRUD
    public static final String RPC_PRECOM_CRUD_SET = "precntauth/precompiled/kvtable/reqSetTable";
    public static final String RPC_PRECOM_CRUD_GET = "precntauth/precompiled/kvtable/reqGetTable";
    public static final String RPC_PRECOM_CRUD_CREATE = "precntauth/precompiled/kvtable/reqCreateTable";
    // 5)bfs
    public static final String RPC_PRECOM_BFS_QUERY = "precntauth/precompiled/bfs/query";
    public static final String RPC_PRECOM_BFS_CREATE = "precntauth/precompiled/bfs/create";

    /**
     * rpc authmanager
     */
    public static final String RPC_AUTHMANAGER_BASE_ENV = "precntauth/authmanager/base/queryExecEnvIsWasm";
    public static final String RPC_AUTHMANAGER_BASE_AUTH = "precntauth/authmanager/base/queryChainHasAuth";
    // 1) everyone
    public static final String RPC_AUTHMANAGER_EVERYONE_CMTINFO = "precntauth/authmanager/everyone/cmtInfo";
    public static final String RPC_AUTHMANAGER_EVERYONE_PROINFO = "precntauth/authmanager/everyone/proposalInfo";
    public static final String RPC_AUTHMANAGER_EVERYONE_PROINFOLIST = "precntauth/authmanager/everyone/proposalInfoList";
    public static final String RPC_AUTHMANAGER_EVERYONE_PROINFOCOUNT = "precntauth/authmanager/everyone/proposalInfoCount";
    public static final String RPC_AUTHMANAGER_EVERYONE_DEPLOY_TYPE = "precntauth/authmanager/everyone/deploy/type";
    public static final String RPC_AUTHMANAGER_EVERYONE_USR_DEPLOY = "precntauth/authmanager/everyone/usr/deploy";
    public static final String RPC_AUTHMANAGER_EVERYONE_CNT_ADMIN = "precntauth/authmanager/everyone/contract/admin";
    public static final String RPC_AUTHMANAGER_EVERYONE_CNT_METHOD_AUTH = "precntauth/authmanager/everyone/contract/method/auth";
    public static final String RPC_AUTHMANAGER_EVERYONE_CNT_STATUS_GET = "precntauth/authmanager/everyone/contract/status";
    public static final String RPC_AUTHMANAGER_EVERYONE_CNT_STATUS_GET_LIST = "precntauth/authmanager/everyone/contract/status/list";
    // 2) committee
    public static final String RPC_AUTHMANAGER_COMMITTEE_GOVERNOR = "precntauth/authmanager/committee/governor";
    public static final String RPC_AUTHMANAGER_COMMITTEE_RATE = "precntauth/authmanager/committee/rate";
    public static final String RPC_AUTHMANAGER_COMMITTEE_PRO_VOTE = "precntauth/authmanager/committee/proposal/vote";
    public static final String RPC_AUTHMANAGER_COMMITTEE_PRO_REVOKE = "precntauth/authmanager/committee/proposal/revoke";
    public static final String RPC_AUTHMANAGER_COMMITTEE_DEPLOY_TYPE = "precntauth/authmanager/committee/deploy/type";
    public static final String RPC_AUTHMANAGER_COMMITTEE_CNT_ADMIN = "precntauth/authmanager/committee/contract/admin";
    public static final String RPC_AUTHMANAGER_COMMITTEE_USR_DEPLOY = "precntauth/authmanager/committee/usr/deploy";
    public static final String RPC_AUTHMANAGER_COMMITTEE_CONSENSUS = "precntauth/authmanager/committee/proposal/consensus";
    // 3) admin
    public static final String RPC_AUTHMANAGER_ADMIN_METHOD_AUTH_SET = "precntauth/authmanager/admin/method/auth/set";
    public static final String RPC_AUTHMANAGER_ADMIN_METHOD_AUTH_TYPE = "precntauth/authmanager/admin/method/auth/type";
    public static final String RPC_AUTHMANAGER_ADMIN_CONTRACT_STATUS_SET = "precntauth/authmanager/admin/contract/status/set";


    public static final String URI_CERT_SDK_FILES = "cert/sdk";

    public static final String URI_CONTRACT_EVENT_INFO_LIST = "event/contractEvent/list";
    public static final String URI_NEW_BLOCK_EVENT_INFO_LIST = "event/newBlockEvent/list";

    // server version
    public static final String URI_FRONT_VERSION = "version";
    public static final String URI_SIGN_VERSION = "version/sign";
    // event log list api
    public static final String URI_EVENT_LOG_LIST = "event/eventLogs/list";

    public static final String URI_SIGN_MESSAGE = "trans/signMessageHashExternal";

    //不需要在url的前面添加groupId的
    private static final List<String> URI_NOT_PREPEND_GROUP_ID = Arrays
        .asList(URI_CONTRACT_DEPLOY_WITH_SIGN, URI_SEND_TRANSACTION_WITH_SIGN, URI_KEY_PAIR,
            URI_KEY_PAIR_LOCAL_KEYSTORE,
            URI_CONTRACT_SENDABI, URI_CERT_SDK_FILES,
            URI_KEY_PAIR_IMPORT_WITH_SIGN, URI_KEY_PAIR_USERINFO_WITH_SIGN,
            URI_CONTRACT_REGISTER_CNS,
            URI_FRONT_VERSION, URI_SIGN_VERSION, URI_KEY_PAIR_EXPORT_PEM_WITH_SIGN,
            URI_KEY_PAIR_EXPORT_P12_WITH_SIGN,
            URI_EVENT_LOG_LIST, URI_SIGN_MESSAGE, URI_CONFIG_SDK,
            RPC_AUTHMANAGER_BASE_ENV,   RPC_AUTHMANAGER_BASE_AUTH,
                RPC_AUTHMANAGER_EVERYONE_CMTINFO,
            RPC_AUTHMANAGER_EVERYONE_PROINFO, RPC_AUTHMANAGER_EVERYONE_PROINFOLIST,
            RPC_AUTHMANAGER_EVERYONE_PROINFOCOUNT,
            RPC_AUTHMANAGER_EVERYONE_USR_DEPLOY, RPC_AUTHMANAGER_EVERYONE_CNT_ADMIN,
            RPC_AUTHMANAGER_EVERYONE_DEPLOY_TYPE,
            RPC_AUTHMANAGER_EVERYONE_CNT_METHOD_AUTH, RPC_AUTHMANAGER_COMMITTEE_GOVERNOR,
            RPC_AUTHMANAGER_EVERYONE_CNT_STATUS_GET, RPC_AUTHMANAGER_EVERYONE_CNT_STATUS_GET_LIST,
            RPC_AUTHMANAGER_COMMITTEE_RATE,
            RPC_AUTHMANAGER_COMMITTEE_PRO_VOTE, RPC_AUTHMANAGER_COMMITTEE_PRO_REVOKE,
            RPC_AUTHMANAGER_COMMITTEE_DEPLOY_TYPE,
            RPC_AUTHMANAGER_COMMITTEE_CNT_ADMIN, RPC_AUTHMANAGER_COMMITTEE_USR_DEPLOY, RPC_AUTHMANAGER_COMMITTEE_CONSENSUS,
            RPC_AUTHMANAGER_ADMIN_METHOD_AUTH_SET, RPC_AUTHMANAGER_ADMIN_CONTRACT_STATUS_SET,
            RPC_AUTHMANAGER_ADMIN_METHOD_AUTH_TYPE, RPC_PRECOM_SYS_CONFIG_LIST,
            RPC_PRECOM_SYS_CONFIG,
            RPC_PRECOM_CNS_ADDRESS_BY_NAME_VERSION, RPC_PRECOM_CNS_REGISTER,
            RPC_PRECOM_CNS_CNSINFO_BY_NAME_VERSION,
            RPC_PRECOM_CONSENSUS_LIST, RPC_PRECOM_CONSENSUS_MGR, RPC_PRECOM_CRUD_SET
            , RPC_PRECOM_CRUD_GET, RPC_PRECOM_CRUD_CREATE, RPC_PRECOM_BFS_QUERY,
            RPC_PRECOM_BFS_CREATE,
            URI_CONTRACT_LIQUID_CHECK, URI_CONTRACT_LIQUID_COMPILE, URI_CONTRACT_LIQUID_COMPILE_CHECK);

    public static List<String> URI_CONTAIN_GROUP_ID = new ArrayList<>();

    @Qualifier(value = "genericRestTemplate")
    @Autowired
    private RestTemplate genericRestTemplate;
    @Qualifier(value = "deployRestTemplate")
    @Autowired
    private RestTemplate deployRestTemplate;
    @Autowired
    private ConstantProperties cproperties;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;
    /**
     * update front status
     */
    @Autowired
    private FrontService frontService;
    /**
     * update node status
     */
    @Autowired
    private NodeService nodeService;
    private static final int NODE_IS_DOWN = -1;

    private static Map<String, FailInfo> failRequestMap = new HashMap<>();

    // sa-token
    private static String tokenKey;
    private static String tokenValue;


    /**
     * append groupId to uri.
     */
    public static String uriAddGroupId(String groupId, String uri) {
        if (groupId == null || StringUtils.isBlank(uri)) {
            return null;
        }

        final String tempUri = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;

        long countNotAppend = URI_NOT_PREPEND_GROUP_ID.stream().filter(u -> u.contains(tempUri))
            .count();
        long countNotContain = URI_CONTAIN_GROUP_ID.stream().filter(u -> u.contains(tempUri))
            .count();
        if (countNotAppend > 0 || countNotContain > 0) {
            return uri;
        }
        return groupId + "/" + uri;
    }

    /**
     * check url status.
     *
     * @return if sleeping, true
     */
    private boolean isServiceSleep(String url, String methType) {
        //get failInfo
        String key = buildKey(url, methType);
        FailInfo failInfo = failRequestMap.get(key);

        //check server status
        if (failInfo == null) {
            return false;
        }
        int failCount = failInfo.getFailCount();
        Long subTime = Duration.between(failInfo.getLatestTime(), Instant.now()).toMillis();
        if (failCount > cproperties.getMaxRequestFail() && subTime < cproperties
            .getSleepWhenHttpMaxFail()) {
            return true;
        } else if (subTime > cproperties.getSleepWhenHttpMaxFail()) {
            //service is sleep
            deleteKeyOfMap(failRequestMap, key);
        }
        return false;

    }

    /**
     * set request fail times.
     */
    private void setFailCount(String url, String methodType) {
        //get failInfo
        String key = buildKey(url, methodType);
        FailInfo failInfo = failRequestMap.get(key);
        if (failInfo == null) {
            failInfo = new FailInfo();
            failInfo.setFailUrl(url);
        }

        //reset failInfo
        failInfo.setLatestTime(Instant.now());
        failInfo.setFailCount(failInfo.getFailCount() + 1);
        failRequestMap.put(key, failInfo);
        log.info("the latest failInfo:{}", JsonTools.toJSONString(failRequestMap));
    }


    /**
     * build key description: frontIp$frontPort example: 2651654951545$8081.
     */
    private String buildKey(String url, String methodType) {
        return url.hashCode() + "$" + methodType;
    }


    /**
     * delete key of map.
     */
    private static void deleteKeyOfMap(Map<String, FailInfo> map, String rkey) {
        log.info("start deleteKeyOfMap. rkey:{} map:{}", rkey, map);
        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            // fix null
            if (key.equals(rkey)) {
                iter.remove();
            }
        }
        log.info("end deleteKeyOfMap. rkey:{} map:{}", rkey, map);
    }


    /**
     * build  url of front service.
     *
     * @remind v1.4.1 rm random
     */
    private FrontUrlInfo buildFrontUrl(ArrayList<FrontGroup> list, String uri,
        HttpMethod httpMethod) {
        // v1.4.2 recover random one
        Collections.shuffle(list);
        log.debug("====================map list:{}", JsonTools.toJSONString(list));
        Iterator<FrontGroup> iterator = list.iterator();
        String uriTemp = uri;
        while (iterator.hasNext()) {
            FrontGroup frontGroup = iterator.next();
            log.debug("============frontGroup:{}", JsonTools.toJSONString(frontGroup));
            FrontUrlInfo frontUrlInfo = new FrontUrlInfo();
            frontUrlInfo.setFrontId(frontGroup.getFrontId());

            uri = uriAddGroupId(frontGroup.getGroupId(), uriTemp);//append groupId to uri
            String url = String
                .format(cproperties.getFrontUrl(), frontGroup.getFrontIp(),
                    frontGroup.getFrontPort(), uri)
                .replaceAll(" ", "");
            iterator.remove();

            if (isServiceSleep(url, httpMethod.toString())) {
                log.warn("front url[{}] is sleep,jump over", url);
                continue;
            }
            frontUrlInfo.setUrl(url);
            return frontUrlInfo;
        }
        log.info("end buildFrontUrl. url is null");
        return null;
    }

    /**
     * build httpEntity.
     */
    public static HttpEntity buildHttpEntity(Object param) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            // sa-token的token鉴权字段，用于front调用sign
            String tokenKey = StpUtil.getTokenName();
            String tokenValue = StpUtil.getTokenValueNotCut();

            if (!StringUtils.isEmpty(tokenValue)) {
                FrontRestTools.tokenKey = tokenKey;
                FrontRestTools.tokenValue = tokenValue;
            }
        } catch (Exception e) {
            log.info("get token key or value err, ignore, err msg:{}", e.getMessage());
        }

        if (!StringUtils.isEmpty(FrontRestTools.tokenValue)) {
            headers.add("tokenKey", FrontRestTools.tokenKey);
            headers.add("tokenValue", FrontRestTools.tokenValue);
        }

        String paramStr = null;
        if (Objects.nonNull(param)) {
            paramStr = JsonTools.toJSONString(param);
        }
        HttpEntity requestEntity = new HttpEntity(paramStr, headers);
        return requestEntity;
    }

    /**
     * case restTemplate by uri.
     */
    private RestTemplate caseRestemplate(String uri) {
        if (StringUtils.isBlank(uri)) {
            return null;
        }
        if (uri.contains(URI_CONTRACT_DEPLOY_WITH_SIGN)) {
            return deployRestTemplate;
        }
        return genericRestTemplate;
    }


    /**
     * get from front for entity.
     */
    public <T> T getForEntity(String groupId, String uri, Class<T> clazz) {
        T response = restTemplateExchange(groupId, uri, HttpMethod.GET, null, clazz);
        if (response == null) {
            log.error("getForEntity response is null!");
            throw new NodeMgrException(
                ConstantCode.REQUEST_FRONT_FAIL.attach("getForEntity response is null"));
        }
        return response;
    }

    /**
     * post from front for entity.
     */
    public <T> T postForEntity(String groupId, String uri, Object params, Class<T> clazz) {
        T response = restTemplateExchange(groupId, uri, HttpMethod.POST, params, clazz);
        if (response == null) {
            log.error("postForEntity response is null!");
            throw new NodeMgrException(
                ConstantCode.REQUEST_FRONT_FAIL.attach("postForEntity response is null"));
        }
        return response;
    }

    /**
     * delete from front for entity.
     */
    public <T> T deleteForEntity(String groupId, String uri, Object params, Class<T> clazz) {
        T response = restTemplateExchange(groupId, uri, HttpMethod.DELETE, params, clazz);
        if (response == null) {
            log.error("deleteForEntity response is null!");
            throw new NodeMgrException(
                ConstantCode.REQUEST_FRONT_FAIL.attach("deleteForEntity response is null"));
        }
        return response;
    }

    /**
     * restTemplate exchange.
     */
    private <T> T restTemplateExchange(String groupId, String uri, HttpMethod method,
        Object param, Class<T> clazz) {
        List<FrontGroup> frontList = frontGroupMapCache.getMapListByGroupId(groupId);
        if (frontList == null || frontList.size() == 0) {
            log.error("fail restTemplateExchange. frontList is empty groupId:{}", groupId);
            throw new NodeMgrException(ConstantCode.FRONT_LIST_NOT_FOUNT.getCode(),
                "all front of group: " + groupId + " is stopped");
        }
        ArrayList<FrontGroup> list = new ArrayList<>(frontList);
        RestTemplate restTemplate = caseRestemplate(uri);

        while (list.size() > 0) {
            // build by frontGroupList, if build one, remove one;
            // build until find success url and return
            // while loop use the same list, try again until get response
            FrontUrlInfo frontUrlInfo = buildFrontUrl(list, uri, method);//build url
            // check url available
            if (frontUrlInfo == null) {
                log.warn("restTemplateExchange buildFrontUrl frontUrlInfo is null.");
                throw new NodeMgrException(ConstantCode.AVAILABLE_FRONT_URL_IS_NULL);
            }
            String url = frontUrlInfo.getUrl();
            if (StringUtils.isBlank(uri)) {
                log.warn("restTemplateExchange buildFrontUrl get null url:{}", list);
                throw new NodeMgrException(ConstantCode.AVAILABLE_FRONT_URL_IS_NULL);
            }
            try {
                log.info("FrontRestTools call front:[{}]", url);

                HttpEntity entity = buildHttpEntity(param);// build entity
                if (null == restTemplate) {
                    log.error("fail restTemplateExchange, rest is null. groupId:{} uri:{}",
                        groupId, uri);
                    throw new NodeMgrException(
                        ConstantCode.SYSTEM_EXCEPTION.attach("restTemplate is null"));
                }
                ResponseEntity<T> response = restTemplate.exchange(url, method, entity, clazz);
                frontService.updateFrontWithInternal(frontUrlInfo.getFrontId(),
                    DataStatus.NORMAL.getValue());
                return response.getBody();
            } catch (ResourceAccessException ex) {
                // case1: request front failed
                log.warn("fail restTemplateExchange", ex);
                setFailCount(url, method.toString());
                if (isServiceSleep(url, method.toString())) {
                    frontService.updateFrontWithInternal(frontUrlInfo.getFrontId(),
                        DataStatus.INVALID.getValue());
                    throw new NodeMgrException(ConstantCode.REQUEST_FRONT_FAIL, ex);
                }
                log.info("continue next front");
                continue;
            } catch (HttpStatusCodeException ex) {
                // case2: request front success but return fail
                JsonNode error = JsonTools.stringToJsonNode(ex.getResponseBodyAsString());
                log.error("http request:[{}] fail. error:{}", url, JsonTools.toJSONString(error),
                    ex);
                try {
                    int code = error.get("code").intValue();
                    String errorMessage = error.get("errorMessage").asText();
                    frontService.updateFrontWithInternal(frontUrlInfo.getFrontId(),
                        DataStatus.INVALID.getValue());
                    // v1.4.3 if node is down but front normal, return -1
                    if (code == NODE_IS_DOWN) {
                        nodeService.updateNodeActiveStatus(frontUrlInfo.getFrontId(),
                            DataStatus.DOWN.getValue());
                    }
                    throw new NodeMgrException(code, errorMessage);
                } catch (NullPointerException e) {
                    throw new NodeMgrException(ConstantCode.REQUEST_FRONT_FAIL);
                }
            }
        }
        return null;
    }
}