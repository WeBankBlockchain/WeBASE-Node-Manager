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
package com.webank.webase.node.mgr.front;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.block.TbBlock;
import com.webank.webase.node.mgr.contract.entity.RspSystemProxy;
import com.webank.webase.node.mgr.monitor.ChainTransInfo;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.TbNode;
import com.webank.webase.node.mgr.transhash.TbTransHash;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
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
public class FrontService {

    public static final String FRONT_URL = "http://%1s:%2d/webase-front/%3s";
    public static final String FRONT_BLOCK_BY_NUMBER_URI = "web3/blockByNumber/%1d";
    public static final String FRONT_TRANS_BY_HASH_URI = "web3/transaction/%1s";
    public static final String FRONT_CODE_URI = "web3/code/%1s/%2s";
    public static final String FRONT_TRANS_RECEIPT_BY_HASH_URI = "web3/transactionReceipt/%1s";
    public static final String FRONT_KEY_PAIR_URI = "contract/privateKey";
    public static final String FRONT_CONTRACT_DEPLOY = "contract/deploy";
    public static final String FRONT_SEND_TRANSACTION = "trans/handle";
    public static final String FRONT_NODE_INFO = "web3/nodeInfo";
    public static final String FRONT_NODE_HEARTBEAT = "/web3/nodeHeartBeat";
    public static final String FRONT_PERFORMANCE_RATIO = "performance";
    public static final String FRONT_PERFORMANCE_CONFIG = "performance/config";
    public static final String FRONT_CHAIN = "chain";
    private static final String FRONT_BLOCK_BY_HASH_URI = "web3/blockByHash/%1s";
    private static final String FRONT_SYSTEM_PROXY_BY_ID_URI = "web3/systemProxy/%s";
    public static final String FRONT_BLOCK_NUMBER_URI = "/web3/blockNumber";
    @Autowired
    private NodeService nodeService;
    @Qualifier(value = "genericRestTemplate")
    @Autowired
    private RestTemplate genericRestTemplate;
    @Qualifier(value = "deployRestTemplate")
    @Autowired
    private RestTemplate deployRestTemplate;
    @Autowired
    private ConstantProperties constantProperties;

    /**
     * random request front.
     */
    private BaseResponse randomRequestFront(Integer networkId, String uri, RequestMethod httpType,
        Object params) throws NodeMgrException {
        log.debug("start randomRequestFront. networkId:{} uri:{} httpType:{} params:{}", networkId,
            uri, httpType, JSON.toJSONString(params));
        BaseResponse frontRsp = new BaseResponse(ConstantCode.SYSTEM_EXCEPTION);

        // query curret node
        List<TbNode> nodeList = nodeService.queryCurrentNodeList(networkId);
        if (nodeList == null || nodeList.size() == 0) {
            throw new NodeMgrException(ConstantCode.CURRENT_NODE_NOT_EXISTS);
        }

        int nodeSize = nodeList.size();
        Random random = new Random();
        List<Integer> indexList = new ArrayList<>(nodeSize);// to save the index of nodeList

        while (true) {
            if (indexList.size() == nodeSize) {
                log.info("all node had used.  return frontRsp:{}", JSON.toJSONString(frontRsp));
                return frontRsp;
            }

            int index = random.nextInt(nodeSize);// random index of nodeList
            if (indexList.contains(index)) {
                log.info(
                    "fail getFromNodeFront, nodeSize:{} indexList:{} currentIndex:{}."
                        + " try ndex node",
                    nodeSize, JSON.toJSONString(indexList), index);
                continue;
            }

            TbNode node = nodeList.get(index);
            indexList.add(index);// save the index of nodeList

            String url = String.format(FRONT_URL, node.getNodeIp(), node.getFrontPort(), uri);
            log.info("requestNodeFront url: {}", url);
            try {
                if (httpType == null) {
                    log.info("httpType is empty.use default:get");
                    httpType = RequestMethod.GET;
                }
                // get
                if (httpType.equals(RequestMethod.GET)) {
                    frontRsp = genericRestTemplate.getForObject(url, BaseResponse.class);
                }
                // post
                if (httpType.equals(RequestMethod.POST)) {
                    if (url.contains(FRONT_CONTRACT_DEPLOY)) {
                        //is deploy contract
                        frontRsp = deployRestTemplate
                            .postForObject(url, params, BaseResponse.class);
                    } else {
                        frontRsp = genericRestTemplate
                            .postForObject(url, params, BaseResponse.class);
                    }

                }

            } catch (RuntimeException ex) {
                log.warn("fail getFromNodeFront", ex);
            } finally {

                if (frontRsp.getCode() != 0 && indexList.size() < nodeSize) {
                    log.warn("fail getFromNodeFront, nodeSize:{} indexList:{}. "
                            + "try ndex node",
                        nodeSize, JSON.toJSONString(indexList));
                    continue;
                }
            }

            log.debug("end getFromNodeFront. networkId:{} url:{} frontRsp:{}", networkId, uri,
                JSON.toJSONString(frontRsp));
            return frontRsp;
        }
    }

    /**
     * get information from node front.
     */
    public BaseResponse getFromNodeFront(Integer networkId, String uri) throws NodeMgrException {
        return randomRequestFront(networkId, uri, RequestMethod.GET, null);
    }

    /**
     * get java bean from front.
     */
    public <T> T getFrontForEntity(Integer networkId, String uri, Class<T> clazz)
        throws NodeMgrException {
        BaseResponse frontRsp = getFromNodeFront(networkId, uri);
        if (frontRsp.getCode() != 0) {
            log.error("fail getFrontForEntity. msg:{}", frontRsp.getMessage());
            throw new NodeMgrException(frontRsp.getCode(), frontRsp.getMessage());
        }
        T t = NodeMgrTools.object2JavaBean(frontRsp.getData(), clazz);
        return t;

    }

    /**
     * post to node font.
     */
    public BaseResponse postNodeFront(Integer networkId, String uri, Object params)
        throws NodeMgrException {
        return randomRequestFront(networkId, uri, RequestMethod.POST, params);
    }

    /**
     * common method: post for object.
     */
    public Object postFrontForObject(Integer networkId, String uri, Object params)
        throws NodeMgrException {
        BaseResponse frontRsp = postNodeFront(networkId, uri, params);
        if (frontRsp.getCode() != 0) {
            log.error("fail postFrontForObject. msg:{}", frontRsp.getMessage());
            throw new NodeMgrException(frontRsp.getCode(), frontRsp.getMessage());
        }
        return frontRsp.getData();
    }

    /**
     * post front and return java bean.
     */
    public <T> T postFrontForEntity(Integer networkId, String uri, Object params, Class<T> clazz)
        throws NodeMgrException {
        Object object = postFrontForObject(networkId, uri, params);
        T t = NodeMgrTools.object2JavaBean(object, clazz);
        return t;

    }

    /**
     * send transactionForObject.
     */
    public void sendTransaction(Integer networkId, Object params) throws NodeMgrException {
        postFrontForObject(networkId, FRONT_SEND_TRANSACTION, params);
    }

    /**
     * send transaction for entity.
     */
    public <C> C sendTransactionForEntity(Integer networkId, Object params, Class<C> clazz)
        throws NodeMgrException {
        Object rspData = postFrontForObject(networkId, FRONT_SEND_TRANSACTION, params);
        C c = NodeMgrTools.object2JavaBean(rspData, clazz);
        return c;
    }

    /**
     * request front for block by number.
     */
    public TbBlock getblockFromFrontByNumber(Integer networkId, BigInteger blockNumber)
        throws NodeMgrException {
        log.info("start getblockFromFrontByNumber. networkId:{}  blockNumber:{}",
            networkId,blockNumber);
        String uri = String.format(FRONT_BLOCK_BY_NUMBER_URI, blockNumber);
        // query node list
        BaseResponse frontRsp = getFromNodeFront(networkId, uri);
        if (frontRsp.getCode() != 0) {
            log.warn("fail getblockFromFrontByNumber networkId:{} blockNumber:{} "
                    + "errorMsg:{}",networkId, blockNumber, frontRsp.getMessage());
            throw new NodeMgrException(frontRsp.getCode(), frontRsp.getMessage());

        }

        TbBlock tbBlock = frontRsp2TbBlock(frontRsp);

        log.info("end getblockFromFrontByNumber. tbBlock:{}", JSON.toJSONString(tbBlock));
        return tbBlock;
    }

    /**
     * request front for transaction by hash.
     */
    public TbTransHash getTransFromFrontByHash(Integer networkId, String transHash)
        throws NodeMgrException {
        log.info("start getTransFromFrontByHash. networkId:{}  transhash:{}", networkId,
            transHash);
        String uri = String.format(FRONT_TRANS_BY_HASH_URI, transHash);
        // query node list
        BaseResponse frontRsp = getFromNodeFront(networkId, uri);
        if (frontRsp.getCode() != 0) {
            log.warn("fail getTransFromFrontByHash.  networkId:{} transhash:{} "
                    + "errorMsg:{}", networkId, transHash, frontRsp.getMessage());
            throw new NodeMgrException(frontRsp.getCode(), frontRsp.getMessage());
        }

        TbTransHash tbTransHash = frontRsp2TbTransHash(frontRsp);

        log.info("end getTransFromFrontByHash. tbTransHash:{}",
            JSON.toJSONString(tbTransHash));
        return tbTransHash;
    }

    /**
     * request front for block by hash.
     */
    public TbBlock getblockFromFrontByHash(Integer networkId, String pkHash)
        throws NodeMgrException {
        log.debug("start getblockFromFrontByHash. networkId:{}  pkHash:{}",
            networkId, pkHash);
        String uri = String.format(FRONT_BLOCK_BY_HASH_URI, pkHash);
        // query node list
        BaseResponse frontRsp = getFromNodeFront(networkId, uri);
        if (frontRsp.getCode() != 0) {
            log.warn("fail getblockFromFrontByHash networkId:{} pkHash:{} errorMsg:{}",
                networkId,
                pkHash, frontRsp.getMessage());
            throw new NodeMgrException(frontRsp.getCode(), frontRsp.getMessage());
        }

        TbBlock tbBlock = frontRsp2TbBlock(frontRsp);

        log.debug("end getblockFromFrontByNumber. tbBlock:{}", JSON.toJSONString(tbBlock));
        return tbBlock;
    }

    /**
     * getTransFromFrontByHash.
     */
    public ChainTransInfo getTransInfoFromFrontByHash(Integer networkId, String hash)
        throws NodeMgrException {
        log.debug("start getTransFromFrontByHash. networkId:{} hash:{}", networkId, hash);
        String uri = String.format(FRONT_TRANS_BY_HASH_URI, hash);
        // query node list
        BaseResponse frontRsp = getFromNodeFront(networkId, uri);
        if (frontRsp.getCode() != 0) {
            log.warn("fail getTransFromFrontByHash networkId:{} hash:{} errorMsg:{}",
                networkId,hash, frontRsp.getMessage());
            throw new NodeMgrException(frontRsp.getCode(), frontRsp.getMessage());
        }

        ChainTransInfo chainTransInfo = frontRsp2TransInfo(frontRsp);

        log.debug("end getTransFromFrontByHash:{}", JSON.toJSONString(chainTransInfo));
        return chainTransInfo;
    }

    /**
     * getAddressFromFrontByHash.
     */
    public String getAddressFromFrontByHash(Integer networkId, String hash)
        throws NodeMgrException {
        log.debug("start getAddressFromFrontByHash. networkId:{} hash:{}", networkId, hash);
        String uri = String.format(FRONT_TRANS_RECEIPT_BY_HASH_URI, hash);
        // query node list
        BaseResponse frontRsp = getFromNodeFront(networkId, uri);
        if (frontRsp.getCode() != 0) {
            log.warn("fail getAddressFromFrontByHash networkId:{} hash:{} errorMsg:{}",
                networkId, hash, frontRsp.getMessage());
            throw new NodeMgrException(frontRsp.getCode(), frontRsp.getMessage());
        }

        String address = frontRsp2Address(frontRsp);

        log.debug("end getTransFromFrontByHash:{}", address);
        return address;
    }

    /**
     * get code from front.
     */
    public String getCodeFromFront(Integer networkId, String address, BigInteger blockNumber)
        throws NodeMgrException {
        log.debug("start getCodeFromFront. networkId:{} address:{} blockNumber:{}",
            networkId,address, blockNumber);
        String uri = String.format(FRONT_CODE_URI, address, blockNumber);
        // query node list
        BaseResponse frontRsp = getFromNodeFront(networkId, uri);
        if (frontRsp.getCode() != 0) {
            throw new NodeMgrException(frontRsp.getCode(), frontRsp.getMessage());
        }

        String code = frontRsp2Code(frontRsp);

        log.debug("end getCodeFromFront:{}", code);
        return code;
    }

    /**
     * get system proxy.
     */
    public List<RspSystemProxy> getSystemProxy(Integer networkId, Integer userId)
        throws NodeMgrException {
        log.debug("start getSystemProxy. networkId:{}", networkId);
        // query node list
        String uri = String.format(FRONT_SYSTEM_PROXY_BY_ID_URI, userId);
        BaseResponse frontRsp = getFromNodeFront(networkId, uri);
        if (frontRsp.getCode() != 0) {
            return null;
        }

        String jsonStr = Optional.ofNullable(frontRsp).map(rsp -> rsp.getData())
            .map(data -> JSON.toJSONString(data)).orElse(null);
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        List<RspSystemProxy> rspList = JSONArray.parseArray(jsonStr, RspSystemProxy.class);

        log.debug("end getSystemProxy.");
        return rspList;
    }

    /**
     * convert response of front to entity TbBLock.
     */
    private TbBlock frontRsp2TbBlock(BaseResponse frontRsp) {
        String jsonStr = Optional.ofNullable(frontRsp).map(rsp -> rsp.getData())
            .map(data -> JSON.toJSONString(data)).orElse(null);
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        JSONObject json = JSONObject.parseObject(jsonStr);

        TbBlock tbBlock = new TbBlock();
        tbBlock.setPkHash(json.getString("hash"));
        tbBlock.setBlockNumber(json.getBigInteger("number"));
        tbBlock.setMiner(json.getString("miner"));

        int transCount = Optional.ofNullable(json).map(j -> j.getJSONArray("transactions"))
            .map(arr -> arr.size()).orElse(0);

        tbBlock.setTransCount(transCount);
        LocalDateTime blockTimestamp = NodeMgrTools
            .timestamp2LocalDateTime(json.getLongValue("timestamp"));
        tbBlock.setBlockTimestamp(blockTimestamp);

        return tbBlock;
    }

    /**
     * convert response of front to entity TbTransHash.
     */
    private TbTransHash frontRsp2TbTransHash(BaseResponse frontRsp) {
        String jsonStr = Optional.ofNullable(frontRsp).map(rsp -> rsp.getData())
            .map(data -> JSON.toJSONString(data)).orElse(null);
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        JSONObject json = JSONObject.parseObject(jsonStr);

        TbTransHash tbTransHash = new TbTransHash();
        tbTransHash.setTransHash(json.getString("hash"));
        tbTransHash.setBlockNumber(new BigInteger(json.getString("blockNumber")));

        return tbTransHash;
    }

    /**
     * convert response of front to entity ChainTransInfo.
     */
    private ChainTransInfo frontRsp2TransInfo(BaseResponse frontRsp) {
        String jsonStr = Optional.ofNullable(frontRsp).map(rsp -> rsp.getData())
            .map(data -> JSON.toJSONString(data)).orElse(null);
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        JSONObject json = JSONObject.parseObject(jsonStr);

        ChainTransInfo chainTransInfo = new ChainTransInfo();
        chainTransInfo.setFrom(json.getString("from"));
        chainTransInfo.setTo(json.getString("to"));
        chainTransInfo.setInput(json.getString("input"));

        return chainTransInfo;
    }

    private String frontRsp2Address(BaseResponse frontRsp) {
        String jsonStr = Optional.ofNullable(frontRsp).map(rsp -> rsp.getData())
            .map(data -> JSON.toJSONString(data)).orElse(null);
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        JSONObject json = JSONObject.parseObject(jsonStr);

        return json.getString("contractAddress");
    }

    private String frontRsp2Code(BaseResponse frontRsp) {
        String jsonStr = Optional.ofNullable(frontRsp).map(rsp -> rsp.getData())
            .map(data -> JSON.toJSONString(data)).orElse(null);
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        JSONObject json = JSONObject.parseObject(jsonStr);

        return json.getString("code").substring(2);
    }

}