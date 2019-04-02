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
package com.webank.webase.node.mgr.web3;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.block.entity.BlockInfoInChain;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.transhash.entity.TransactionInfo;
import java.math.BigInteger;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * service of web3.
 */
@Log4j2
@Service
public class Web3Service {

    @Autowired
    private FrontService frontService;
    @Qualifier(value = "genericRestTemplate")
    @Autowired
    private RestTemplate genericRestTemplate;

    /**
     * get contract code.
     */
    public BaseResponse getContractCode(Integer networkId, String address, Integer blockNumber)
        throws NodeMgrException {
        log.debug("start getContractCode networkId:{} address:{} blockNumber:{}", networkId,
            address, blockNumber);
        String uri = String.format(FrontService.FRONT_CODE_URI, address, blockNumber);
        BaseResponse frontRsp = frontService.getFromNodeFront(networkId, uri);
        log.debug("end getContractCode frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     * get transaction receipt.
     */
    public BaseResponse getTransReceipt(Integer networkId, String transHash)
        throws NodeMgrException {
        log.debug("start getTransReceipt networkId:{} transhash:{}", networkId, transHash);
        String uri = String.format(FrontService.FRONT_TRANS_RECEIPT_BY_HASH_URI, transHash);
        BaseResponse frontRsp = frontService.getFromNodeFront(networkId, uri);
        log.debug("end getTransReceipt frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     * get block by number.
     */
    public BaseResponse getBlockByNumber(Integer networkId, BigInteger blockNumber)
        throws NodeMgrException {
        log.debug("start getBlockByNumber networkId:{} blockNumber:{}", networkId, blockNumber);
        String uri = String.format(FrontService.FRONT_BLOCK_BY_NUMBER_URI, blockNumber);
        BaseResponse frontRsp = frontService.getFromNodeFront(networkId, uri);
        log.debug("end getBlockByNumber frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     * request for node heartBeat.
     */
    public BaseResponse nodeHeartBeat(String nodeIp, Integer frontPort) throws NodeMgrException {
        log.debug("start nodeHeartBeat. frontPort:{} FrontPort:{}", nodeIp, frontPort);
        String url = String
            .format(FrontService.FRONT_URL, nodeIp, frontPort, FrontService.FRONT_NODE_HEARTBEAT);
        BaseResponse frontRsp = genericRestTemplate.getForObject(url, BaseResponse.class);
        log.debug("end nodeHeartBeat. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     * get transaction by hash.
     */
    public TransactionInfo getTransaction(Integer networkId, String transHash)
        throws NodeMgrException {
        log.debug("start getTransaction networkId:{} transhash:{}", networkId, transHash);
        if (StringUtils.isBlank(transHash)) {
            return null;
        }
        String uri = String.format(FrontService.FRONT_TRANS_BY_HASH_URI, transHash);
        TransactionInfo transInfo = frontService.getFrontForEntity(networkId, uri, TransactionInfo.class);
        log.debug("end getTransaction");
        return transInfo;
    }

    /**
     * get transaction hash by block number
     */
    public List<TransactionInfo> getTransByBlockNumber(Integer networkId, BigInteger blockNumber) {
        log.debug("start getTransByBlockNumber. networkId:{} blockNumber:{}", networkId,
            blockNumber);
        BaseResponse frontRsp = getBlockByNumber(networkId, blockNumber);
        BlockInfoInChain blockInfoInChain = NodeMgrTools.object2JavaBean(frontRsp.getData(),
            BlockInfoInChain.class);
        List<TransactionInfo> transInBLock = blockInfoInChain.getTransactions();
        log.debug("end getTransByBlockNumber. transInBLock:{}", JSON.toJSONString(transInBLock));
        return transInBLock;
    }
}
