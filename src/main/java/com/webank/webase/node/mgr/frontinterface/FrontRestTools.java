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
package com.webank.webase.node.mgr.frontinterface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroup;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.node.mgr.frontinterface.entity.FailInfo;
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
 * about http request for webase-front.
 */
@Log4j2
@Service
public class FrontRestTools {

    //public static final String FRONT_URL = "http://%1s:%2d/webase-front/%3s";
    public static final String FRONT_TRANS_RECEIPT_BY_HASH_URI = "web3/transactionReceipt/%1s";
    public static final String URI_BLOCK_BY_NUMBER = "web3/blockByNumber/%1d";
    public static final String URI_BLOCK_BY_HASH = "web3/blockByHash/%1s";
    public static final String URI_TRANS_TOTAL = "web3/transaction-total";
    public static final String URI_TRANS_BY_HASH = "web3/transaction/%1s";
    public static final String URI_GROUP_PEERS = "web3/groupPeers";
    public static final String URI_GROUP_PLIST = "web3/groupList";
    public static final String URI_PEERS = "web3/peers";
    public static final String URI_CONSENSUS_STATUS = "web3/consensusStatus";
    public static final String URI_CSYNC_STATUS = "web3/syncStatus";
    public static final String URI_SYSTEMCONFIG_BY_KEY = "web3/systemConfigByKey/%1s";
    public static final String URI_CODE = "web3/code/%1s/%2s";
    public static final String URI_BLOCK_NUMBER = "web3/blockNumber";
    public static final String URI_GET_SEALER_LIST = "web3/sealerList";
    public static final String FRONT_PERFORMANCE_RATIO = "performance";
    public static final String FRONT_PERFORMANCE_CONFIG = "performance/config";
    public static final String URI_KEY_PAIR = "privateKey?useAes=%1b";
    public static final String URI_CONTRACT_DEPLOY = "contract/deploy";
    public static final String URI_CONTRACT_SENDABI = "contract/abiInfo";
    public static final String URI_SEND_TRANSACTION = "trans/handle";
    public static final String URI_CHAIN = "chain";

    private static final List<String> URI_NOT_CONTAIN_GROUP_ID = Arrays
        .asList(URI_CONTRACT_DEPLOY, URI_SEND_TRANSACTION, URI_KEY_PAIR, URI_CONTRACT_SENDABI);


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

    private static Map<String, FailInfo> failRequestMap = new HashMap<>();


    /**
     * append groupId to uri.
     */
    public static String uriAddGroupId(Integer groupId, String uri) {
        if (groupId == null || StringUtils.isBlank(uri)) {
            return null;
        }

        final String tempUri = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;

        long count = URI_NOT_CONTAIN_GROUP_ID.stream().filter(u -> u.contains(tempUri)).count();
        if (count > 0) {
            return uri;
        }
        return groupId + "/" + uri;
    }

    /**
     * check url status.
     */
    private boolean isServiceSleep(String url, String methType) {
        //get failInfo
        String key = buildKey(url, methType);
        FailInfo failInfo = failRequestMap.get(key);

        //cehck server status
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
        log.info("the latest failInfo:{}", JSON.toJSONString(failRequestMap));
    }


    /**
     * build key description: frontIp$frontPort example: 2651654951545$8081
     */
    private String buildKey(String url, String methodType) {
        return url.hashCode() + "$" + methodType;
    }


    /**
     * delete key of map
     */
    private static void deleteKeyOfMap(Map<String, FailInfo> map, String rkey) {
        log.info("start deleteKeyOfMap. rkey:{} map:{}", rkey, JSON.toJSONString(map));
        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            if (rkey.equals(key)) {
                iter.remove();
            }
        }
        log.info("end deleteKeyOfMap. rkey:{} map:{}", rkey, JSON.toJSONString(map));
    }


    /**
     * build  url of front service.
     */
    private String buildFrontUrl(ArrayList<FrontGroup> list, String uri, HttpMethod httpMethod) {
        Collections.shuffle(list);//random one
        Iterator<FrontGroup> iterator = list.iterator();
        while (iterator.hasNext()) {
            FrontGroup frontGroup = iterator.next();
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
            return url;
        }
        log.info("end buildFrontUrl. url is null");
        return null;
    }

    /**
     * build httpEntity
     */
    public static HttpEntity buildHttpEntity(Object param) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String paramStr = null;
        if (Objects.nonNull(param)) {
            paramStr = JSON.toJSONString(param);
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
        if (uri.contains(URI_CONTRACT_DEPLOY)) {
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
     * restTemplate exchange.
     */
    private <T> T restTemplateExchange(int groupId, String uri, HttpMethod method,
        Object param, Class<T> clazz) {
        List<FrontGroup> frontList = frontGroupMapCache.getMapListByGroupId(groupId);
        if (frontList == null || frontList.size() == 0) {
            log.error("fail restTemplateExchange. frontList is empty");
            throw new NodeMgrException(ConstantCode.FRONT_LIST_NOT_FOUNT);
        }
        ArrayList<FrontGroup> list = new ArrayList<>(frontList);
        RestTemplate restTemplate = caseRestemplate(uri);

        while (list != null && list.size() > 0) {
            String url = buildFrontUrl(list, uri, method);//build url
            try {
                HttpEntity entity = buildHttpEntity(param);// build entity
                if (null == restTemplate) {
                    log.error("fail restTemplateExchange, rest is null. groupId:{} uri:{}", groupId,uri);
                    throw new NodeMgrException(ConstantCode.SYSTEM_EXCEPTION);
                }
                ResponseEntity<T> response = restTemplate.exchange(url, method, entity, clazz);
                return response.getBody();
            } catch (ResourceAccessException ex) {
                log.warn("fail restTemplateExchange", ex);
                setFailCount(url, method.toString());
                if (isServiceSleep(url, method.toString())) {
                    throw ex;
                }
                log.info("continue next front", ex);
                continue;
            } catch (HttpStatusCodeException e) {
                JSONObject error = JSONObject.parseObject(e.getResponseBodyAsString());
                log.error("http request fail. error:{}", JSON.toJSONString(error));
                throw new NodeMgrException(error.getInteger("code"),
                    error.getString("errorMessage"));
            }
        }
        return null;
    }
}