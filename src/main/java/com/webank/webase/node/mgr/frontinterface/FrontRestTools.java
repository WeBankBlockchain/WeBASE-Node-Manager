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
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroup;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

/**
 * about http request for webase-front.
 */
@Log4j2
@Service
public class FrontRestTools {

    public static final String FRONT_URL = "http://%1s:%2d/webase-front/%3d/%4s";
    public static final String FRONT_TRANS_RECEIPT_BY_HASH_URI = "web3/transactionReceipt/%1s";
    public static final String FRONT_BLOCK_BY_NUMBER_URI = "web3/blockByNumber/%1d";
    public static final String FRONT_BLOCK_BY_HASH_URI = "web3/blockByHash/%1s";
    public static final String FRONT_TRANS_TOTAL_URI = "web3/transaction-total";
    public static final String FRONT_TRANS_BY_HASH_URI = "web3/transaction/%1s";
   // public static final String FRONT_NODE_HEARTBEAT = "web3/nodeHeartBeat";
    public static final String FRONT_GROUP_PEERS = "web3/groupPeers";
    public static final String FRONT_GROUP_PLIST = "web3/groupList";
    public static final String FRONT_PEERS = "web3/peers";
    public static final String FRONT_CONSENSUS_STATUS = "web3/consensusStatus";
    public static final String FRONT_CSYNC_STATUS = "web3/syncStatus";
    public static final String FRONT_SYSTEMCONFIG_BY_KEY = "web3/systemConfigByKey/%1s";
    public static final String FRONT_CODE_URI = "web3/code/%1s/%2s";
    public static final String FRONT_NODE_INFO = "web3/nodeInfo";
    public static final String FRONT_BLOCK_NUMBER = "web3/blockNumber";
    public static final String FRONT_PERFORMANCE_RATIO = "performance";
    public static final String FRONT_PERFORMANCE_CONFIG = "performance/config";
    public static final String FRONT_KEY_PAIR_URI = "contract/privateKey";
    public static final String FRONT_CONTRACT_DEPLOY = "contract/deploy";
    public static final String FRONT_SEND_TRANSACTION = "trans/handle";
    public static final String FRONT_CHAIN = "chain";


    @Autowired
    private FrontGroupMapService mapService;
    @Qualifier(value = "genericRestTemplate")
    @Autowired
    private RestTemplate genericRestTemplate;
    @Qualifier(value = "deployRestTemplate")
    @Autowired
    private RestTemplate deployRestTemplate;
    @Autowired
    private ConstantProperties cproperties;

    //description：key:frontIp$frontPort   value:FailInfo
    //example： key:12345475275272$8081  value: {"lastTime":554294068415,"failCount":3}
    private static Map<String, FailInfo> failRequestMap = new HashMap<>();


    /**
     * random request front.
     */
    private <T> T randomRequestFront(Integer groupId, String uri, RequestMethod httpType,
        Object params, Class<T> clazz) throws NodeMgrException {
        log.debug("start randomRequestFront. groupId:{} uri:{} httpType:{} entity:{}", groupId,
            uri, httpType, JSON.toJSONString(params));
        // get map by groupId
        List<FrontGroup> mapList = mapService.listByGroupId(groupId);
        if (mapList == null || mapList.size() == 0) {
            throw new NodeMgrException(ConstantCode.FRONT_NOT_EXISTS);
        }

        int mapSize = mapList.size();
        Random random = new Random();
        List<Integer> indexList = new ArrayList<>(mapSize);// to save the index of nodeList

        T frontRsp = null;
        while (true) {
            if (indexList.size() == mapSize) {
                log.info("all node had used.  return frontRsp:{}", JSON.toJSONString(frontRsp));
                return frontRsp;
            }

            int index = random.nextInt(mapSize);// random index of nodeList
            if (indexList.contains(index)) {
                log.info(
                    "fail requestFront, nodeSize:{} indexList:{} currentIndex:{}."
                        + " try ndex node",
                    mapSize, JSON.toJSONString(indexList), index);
                continue;
            }

            FrontGroup frontGroup = mapList.get(index);
            indexList.add(index);// save the index of nodeList

            String url = String.format(FRONT_URL, frontGroup.getFrontIp(), frontGroup.getFrontPort(), groupId, uri);
            url = url.replaceAll(" ","");
            if (httpType == null) {
                log.info("httpType is empty.use default:get");
                httpType = RequestMethod.GET;
            }
            if (isServiceSleep(url, httpType.toString())) {
                log.info("front url[{}] is sleep,jump over", frontGroup.getFrontIp(), frontGroup.getFrontPort());
                continue;
            }
            log.info("requestFront url: {}", url);

            try {
                // get
                if (httpType.equals(RequestMethod.GET)) {
                    frontRsp = genericRestTemplate.getForObject(url, clazz);
                }
                // post
                if (httpType.equals(RequestMethod.POST)) {
                    if (url.contains(FRONT_CONTRACT_DEPLOY)) {
                        //is deploy contract
                        frontRsp = deployRestTemplate.postForObject(url, params, clazz);
                    } else {
                        frontRsp = genericRestTemplate.postForObject(url, params, clazz);
                    }
                }
            } catch (RuntimeException ex) {
                log.warn("fail randomRequestFront", ex);
                setFailCount(url, httpType.toString());
                if (indexList.size() < mapSize) {
                    log.warn("fail randomRequestFront, nodeSize:{} indexList:{}. "
                        + "try ndex node", mapSize, JSON.toJSONString(indexList));
                    continue;
                }
            }

            log.debug("end randomRequestFront. groupId:{} url:{} frontRsp:{}", groupId, uri,
                JSON.toJSONString(frontRsp));
            return frontRsp;
        }
    }

    /**
     * get information from node front.
     */
    public <T> T getFrontForEntity(Integer groupId, String uri, Class<T> clazz)
        throws NodeMgrException {
        return randomRequestFront(groupId, uri, RequestMethod.GET, null, clazz);
    }

    /**
     * post to node font.
     */
    public <T> T postFrontForEntity(Integer groupId, String uri, Object params, Class<T> clazz)
        throws NodeMgrException {
        return randomRequestFront(groupId, uri, RequestMethod.POST, params, clazz);
    }


    /**
     *
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
        }else if(subTime>cproperties.getSleepWhenHttpMaxFail()){
            //service is sleep
            deleteKeyOfMap(failRequestMap,key);
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
        int newFailCount = failInfo == null ? 1 : failInfo.getFailCount() + 1;

        //reset failInfo
        failInfo.setLatestTime(LocalDateTime.now());
        failInfo.setFailCount(newFailCount);
        failRequestMap.put(key, failInfo);
    }


    /**
     * build key
     * description: frontIp$frontPort
     * example: 2651654951545$8081
     */
    private String buildKey(String url, String methodType) {
        return url.hashCode() + "$" + methodType;
    }


    /**
     * delete key of map
     */
    public static void deleteKeyOfMap(Map<String, FailInfo> map, String rkey) {
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

    }