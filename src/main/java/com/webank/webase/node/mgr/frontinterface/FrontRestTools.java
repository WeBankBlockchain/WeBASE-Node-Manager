/**
 * Copyright 2014-2020  the original author or authors.
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

package com.webank.webase.node.mgr.frontinterface;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.DataStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapCache;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroup;
import com.webank.webase.node.mgr.frontinterface.entity.FailInfo;
import com.webank.webase.node.mgr.frontinterface.entity.FrontUrlInfo;

import lombok.extern.log4j.Log4j2;

/**
 * about http request for WeBASE-Front.
 */
@Log4j2
@Service
public class FrontRestTools {

    //public static final String FRONT_URL = "http://%1s:%2d/WeBASE-Front/%3s";
    public static final String FRONT_TRANS_RECEIPT_BY_HASH_URI = "web3/transactionReceipt/%1s";
    public static final String URI_BLOCK_BY_NUMBER = "web3/blockByNumber/%1d";
    public static final String URI_BLOCK_BY_HASH = "web3/blockByHash/%1s";
    public static final String URI_TRANS_TOTAL = "web3/transaction-total";
    public static final String URI_TRANS_BY_HASH = "web3/transaction/%1s";
    public static final String URI_GROUP_PEERS = "web3/groupPeers";
    public static final String URI_NODEID_LIST = "web3/nodeIdList";
    public static final String URI_GROUP_PLIST = "web3/groupList";
    public static final String URI_PEERS = "web3/peers";
    public static final String URI_CONSENSUS_STATUS = "web3/consensusStatus";
    public static final String URI_CSYNC_STATUS = "web3/syncStatus";
    public static final String URI_SYSTEMCONFIG_BY_KEY = "web3/systemConfigByKey/%1s";
    public static final String URI_CODE = "web3/code/%1s/%2s";
    public static final String URI_BLOCK_NUMBER = "web3/blockNumber";
    public static final String URI_GET_SEALER_LIST = "web3/sealerList";
    public static final String URI_GET_OBSERVER_LIST = "web3/observerList";
    public static final String URI_GET_CLIENT_VERSION = "web3/clientVersion";
    public static final String URI_GENERATE_GROUP = "web3/generateGroup";
    public static final String URI_OPERATE_GROUP = "web3/operateGroup/%1s";
    public static final String URI_QUERY_GROUP_STATUS = "web3/queryGroupStatus";
    public static final String URI_REFRESH_FRONT = "web3/refresh";
    public static final String FRONT_PERFORMANCE_RATIO = "performance";
    public static final String FRONT_PERFORMANCE_CONFIG = "performance/config";
    public static final String URI_KEY_PAIR = "privateKey?type=2&userName=%s&signUserId=%s&appId=%s";
    public static final String URI_KEY_PAIR_LOCAL_KEYSTORE = "privateKey/localKeyStores";
    public static final String URI_KEY_PAIR_IMPORT_WITH_SIGN = "privateKey/importWithSign";
    public static final String URI_CONTRACT_DEPLOY_WITH_SIGN = "contract/deployWithSign";
    public static final String URI_CONTRACT_SENDABI = "contract/abiInfo";
    public static final String URI_SEND_TRANSACTION_WITH_SIGN = "trans/handleWithSign";
    public static final String URI_CHAIN = "chain";

    public static final String URI_PERMISSION = "permission";
    public static final String URI_PERMISSION_FULL_LIST = "permission/full";
    public static final String URI_PERMISSION_SORTED_LIST = "permission/sorted";
    public static final String URI_PERMISSION_SORTED_FULL_LIST = "permission/sorted/full";
    public static final String URI_SYS_CONFIG_LIST = "sys/config/list";
    public static final String URI_SYS_CONFIG = "sys/config";
    public static final String URI_CNS_LIST = "precompiled/cns/list";
    public static final String URI_CONSENSUS_LIST = "precompiled/consensus/list";
    public static final String URI_CONSENSUS = "precompiled/consensus";
    public static final String URI_CRUD = "precompiled/crud";

    public static final String URI_CERT = "cert";
    public static final String URI_ENCRYPT_TYPE = "encrypt";

    public static final String URI_CONTRACT_EVENT_INFO_LIST = "event/contractEvent/list";
    public static final String URI_NEW_BLOCK_EVENT_INFO_LIST = "event/newBlockEvent/list";

    //不需要在url的前面添加groupId的
    private static final List<String> URI_NOT_PREPEND_GROUP_ID = Arrays
        .asList(URI_CONTRACT_DEPLOY_WITH_SIGN, URI_SEND_TRANSACTION_WITH_SIGN, URI_KEY_PAIR, URI_KEY_PAIR_LOCAL_KEYSTORE,
                URI_CONTRACT_SENDABI, URI_PERMISSION, URI_PERMISSION_FULL_LIST, URI_CNS_LIST, URI_SYS_CONFIG_LIST,
                URI_SYS_CONFIG, URI_CONSENSUS_LIST, URI_CONSENSUS, URI_CRUD, URI_PERMISSION_SORTED_LIST,
                URI_PERMISSION_SORTED_FULL_LIST, URI_CERT, URI_ENCRYPT_TYPE,
                URI_KEY_PAIR_IMPORT_WITH_SIGN);

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

    private static Map<String, FailInfo> failRequestMap = new HashMap<>();


    /**
     * append groupId to uri.
     */
    public static String uriAddGroupId(Integer groupId, String uri) {
        if (groupId == null || StringUtils.isBlank(uri)) {
            return null;
        }

        final String tempUri = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;

        long countNotAppend = URI_NOT_PREPEND_GROUP_ID.stream().filter(u -> u.contains(tempUri)).count();
        long countNotContain = URI_CONTAIN_GROUP_ID.stream().filter(u -> u.contains(tempUri)).count();
        if (countNotAppend > 0 || countNotContain > 0) {
            return uri;
        }
        return groupId + "/" + uri;
    }

    /**
     * check url status.
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
        log.info("start deleteKeyOfMap. rkey:{} map:{}", rkey, JsonTools.toJSONString(map));
        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            // fix null
            if (key.equals(rkey)) {
                iter.remove();
            }
        }
        log.info("end deleteKeyOfMap. rkey:{} map:{}", rkey, JsonTools.toJSONString(map));
    }


    /**
     * build  url of front service.
     */
    private FrontUrlInfo buildFrontUrl(ArrayList<FrontGroup> list, String uri, HttpMethod httpMethod) {
        Collections.shuffle(list);//random one
        log.info("====================map list:{}",JsonTools.toJSONString(list));
        Iterator<FrontGroup> iterator = list.iterator();
        while (iterator.hasNext()) {
            FrontGroup frontGroup = iterator.next();
            log.info("============frontGroup:{}",JsonTools.toJSONString(frontGroup));
            FrontUrlInfo frontUrlInfo = new FrontUrlInfo();
            frontUrlInfo.setFrontId(frontGroup.getFrontId());

            uri = uriAddGroupId(frontGroup.getGroupId(), uri);//append groupId to uri
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
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
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
    public <T> T getForEntity(Integer groupId, String uri, Class<T> clazz) {
        return restTemplateExchange(groupId, uri, HttpMethod.GET, null, clazz);
    }

    /**
     * post from front for entity.
     */
    public <T> T postForEntity(Integer groupId, String uri, Object params, Class<T> clazz) {
        return restTemplateExchange(groupId, uri, HttpMethod.POST, params, clazz);
    }

    /**
     * delete from front for entity.
     */
    public <T> T deleteForEntity(Integer groupId, String uri, Object params, Class<T> clazz) {
        return restTemplateExchange(groupId, uri, HttpMethod.DELETE, params, clazz);
    }

    /**
     * restTemplate exchange.
     */
    private <T> T restTemplateExchange(int groupId, String uri, HttpMethod method,
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
            String url = frontUrlInfo.getUrl();
            // check url available
            if (StringUtils.isBlank(uri)) {
                log.warn("restTemplateExchange buildFrontUrl get null url:{}", list);
                throw new NodeMgrException(ConstantCode.AVAILABLE_FRONT_URL_IS_NULL);
            }
            try {
                HttpEntity entity = buildHttpEntity(param);// build entity
                if (null == restTemplate) {
                    log.error("fail restTemplateExchange, rest is null. groupId:{} uri:{}",
                            groupId,uri);
                    throw new NodeMgrException(ConstantCode.SYSTEM_EXCEPTION);
                }
                ResponseEntity<T> response = restTemplate.exchange(url, method, entity, clazz);
                frontService.updateFrontWithInternal(frontUrlInfo.getFrontId(), DataStatus.NORMAL.getValue());
                return response.getBody();
            } catch (ResourceAccessException ex) {
                log.warn("fail restTemplateExchange", ex);
                setFailCount(url, method.toString());
                if (isServiceSleep(url, method.toString())) {
                    frontService.updateFrontWithInternal(frontUrlInfo.getFrontId(), DataStatus.INVALID.getValue());
                    throw ex;
                }
                log.info("continue next front", ex);
                continue;
            } catch (HttpStatusCodeException ex) {
                JsonNode error = JsonTools.stringToJsonNode(ex.getResponseBodyAsString());
                log.error("http request fail. error:{}", JsonTools.toJSONString(error));
                try {
                    int code = error.get("code").intValue();
                    String errorMessage = error.get("errorMessage").asText();
                    frontService.updateFrontWithInternal(frontUrlInfo.getFrontId(), DataStatus.INVALID.getValue());
                    throw new NodeMgrException(code, errorMessage);
                } catch (NullPointerException e) {
                    throw new NodeMgrException(ConstantCode.REQUEST_FRONT_FAIL);
                }
            }
        }
        return null;
    }
}