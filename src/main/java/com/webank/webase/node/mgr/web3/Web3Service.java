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
import com.webank.webase.node.mgr.block.entity.BlockInfo;
import com.webank.webase.node.mgr.contract.RspSystemProxy;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.monitor.ChainTransInfo;
import com.webank.webase.node.mgr.transhash.TbTransHash;
import com.webank.webase.node.mgr.transhash.entity.TransReceipt;
import com.webank.webase.node.mgr.transhash.entity.TransactionInfo;
import com.webank.webase.node.mgr.web3.entity.TotalTransCountInfo;
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
    public String getContractCode(Integer groupId, String address, BigInteger blockNumber)
        throws NodeMgrException {
        log.debug("start getContractCode groupId:{} address:{} blockNumber:{}", groupId,
            address, blockNumber);
        String uri = String.format(FrontService.FRONT_CODE_URI, address, blockNumber);
        String contractCode = frontService.getFrontForEntity(groupId, uri, String.class);
        log.debug("end getContractCode. contractCode:{}", contractCode);
        return contractCode;
    }

    /**
     * get transaction receipt.
     */
    public TransReceipt getTransReceipt(Integer groupId, String transHash)
        throws NodeMgrException {
        log.debug("start getTransReceipt groupId:{} transhash:{}", groupId, transHash);
        String uri = String.format(FrontService.FRONT_TRANS_RECEIPT_BY_HASH_URI, transHash);
        TransReceipt transReceipt = frontService.getFrontForEntity(groupId, uri, TransReceipt.class);
        log.debug("end getTransReceipt");
        return transReceipt;
    }

    /**
     * get transaction by hash.
     */
    public TransactionInfo getTransaction(Integer groupId, String transHash)
        throws NodeMgrException {
        log.debug("start getTransaction groupId:{} transhash:{}", groupId, transHash);
        if (StringUtils.isBlank(transHash)) {
            return null;
        }
        String uri = String.format(FrontService.FRONT_TRANS_BY_HASH_URI, transHash);
        TransactionInfo transInfo = frontService.getFrontForEntity(groupId, uri, TransactionInfo.class);
        log.debug("end getTransaction");
        return transInfo;
    }

    /**
     * get block by number.
     */
    public BlockInfo getBlockByNumber(Integer groupId, BigInteger blockNumber)
        throws NodeMgrException {
        log.debug("start getBlockByNumber groupId:{} blockNumber:{}", groupId, blockNumber);
        String uri = String.format(FrontService.FRONT_BLOCK_BY_NUMBER_URI, blockNumber);
        BlockInfo blockInfo = frontService.getFrontForEntity(groupId, uri, BlockInfo.class);
        log.debug("end getBlockByNumber");
        return blockInfo;
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
     * request front for block by hash.
     */
    public BlockInfo getblockFromFrontByHash(Integer groupId, String pkHash)
        throws NodeMgrException {
        log.debug("start getblockFromFrontByHash. groupId:{}  pkHash:{}", groupId,
            pkHash);
        String uri = String.format(FrontService.FRONT_BLOCK_BY_HASH_URI, pkHash);
        BlockInfo blockInfo = frontService.getFrontForEntity(groupId, uri, BlockInfo.class);
        log.debug("end getblockFromFrontByHash. blockInfo:{}", JSON.toJSONString(blockInfo));
        return blockInfo;
    }




    /**
     * getTransFromFrontByHash.
     */
    public ChainTransInfo getTransInfoFromFrontByHash(Integer groupId, String hash)
        throws NodeMgrException {
        log.debug("start getTransFromFrontByHash. groupId:{} hash:{}", groupId, hash);
        TransactionInfo transaction = getTransaction(groupId,hash);
        ChainTransInfo chainTransInfo = new ChainTransInfo(transaction.getFrom(),transaction.getTo(),transaction.getInput());
        log.debug("end getTransFromFrontByHash:{}", JSON.toJSONString(chainTransInfo));
        return chainTransInfo;
    }

    /**
     * getAddressFromFrontByHash.
     */
    public String getAddressFromFrontByHash(Integer groupId, String transHash)
        throws NodeMgrException {
        log.debug("start getAddressFromFrontByHash. groupId:{} transHash:{}", groupId, transHash);

        TransReceipt transReceipt = getTransReceipt( groupId,  transHash);
        String contractAddress = transReceipt.getContractAddress();
        log.debug("end getTransFromFrontByHash. contractAddress{}", contractAddress);
        return contractAddress;
    }


    /**
     * get code from front.
     */
    public String getCodeFromFront(Integer groupId, String address, BigInteger blockNumber)
        throws NodeMgrException {
        log.debug("start getCodeFromFront. groupId:{} address:{} blockNumber:{}",
            groupId, address, blockNumber);
        String uri = String.format(FrontService.FRONT_CODE_URI, address, blockNumber);
        String code = frontService.getFrontForEntity(groupId, uri, String.class);

        log.debug("end getCodeFromFront:{}", code);
        return code;
    }

    /**
     * get total transaction count
     */
    public TotalTransCountInfo getTotalTransactionCount(Integer groupId) {
        log.debug("start getTotalTransactionCount. groupId:{}", groupId);
        TotalTransCountInfo totalCount = frontService
            .getFrontForEntity(groupId, FrontService.FRONT_TRANS_TOTAL_URI, TotalTransCountInfo.class);
        log.debug("end getTotalTransactionCount:{}", totalCount);
        return totalCount;
    }

    /**
     * get transaction hash by block number
     */
    public List<TransactionInfo> getTransByBlockNumber(Integer groupId, BigInteger blockNumber) {
        log.debug("start getTransByBlockNumber. groupId:{} blockNumber:{}", groupId,
            blockNumber);
        BlockInfo blockInfo = getBlockByNumber(groupId, blockNumber);
        if (blockInfo == null) {
            return null;
        }
        List<TransactionInfo> transInBLock = blockInfo.getTransactions();
        log.debug("end getTransByBlockNumber. transInBLock:{}", JSON.toJSONString(transInBLock));
        return transInBLock;
    }

    /**
     * get group peers
     */
    public List<String> getGroupPeers(Integer groupId) {
        log.debug("start getGroupPeers. groupId:{}", groupId);
        List<String> groupPeers = frontService
            .getFrontForEntity(groupId, FrontService.FRONT_GROUP_PEERS, List.class);
        log.debug("end getGroupPeers. groupPeers:{}", JSON.toJSONString(groupPeers));
        return groupPeers;
    }

    /**
     * get group list
     */
    public List<String> getGroupList(Integer groupId) {
        log.debug("start getGroupList. groupId:{}", groupId);
        List<String> groupList = frontService
            .getFrontForEntity(groupId, FrontService.FRONT_GROUP_PLIST, List.class);
        log.debug("end getGroupList. groupPeers:{}", JSON.toJSONString(groupList));
        return groupList;
    }

    /**
     * get peers
     */
    public List<String> getPeers(Integer groupId) {
        log.debug("start getPeers. groupId:{}", groupId);
        List<String> peers = frontService
            .getFrontForEntity(groupId, FrontService.FRONT_PEERS, List.class);
        log.debug("end getPeers. getPeers:{}", JSON.toJSONString(peers));
        return peers;
    }

    /**
     * get consensusStatus
     */
    public String getConsensusStatus(Integer groupId) {
        log.debug("start getConsensusStatus. groupId:{}", groupId);
        String consensusStatus = frontService
            .getFrontForEntity(groupId, FrontService.FRONT_CONSENSUS_STATUS, String.class);
        log.debug("end getConsensusStatus. consensusStatus:{}", consensusStatus);
        return consensusStatus;
    }

    /**
     * get syncStatus
     */
    public String syncStatus(Integer groupId) {
        log.debug("start syncStatus. groupId:{}", groupId);
        String ststus = frontService
            .getFrontForEntity(groupId, FrontService.FRONT_CSYNC_STATUS, String.class);
        log.debug("end syncStatus. ststus:{}", ststus);
        return ststus;
    }

    /**

     * get config by key
     */
    public String getSystemConfigByKey(Integer groupId, String key) {
        log.debug("start getSystemConfigByKey. groupId:{}", groupId);
        String uri = String.format(FrontService.FRONT_SYSTEMCONFIG_BY_KEY, key);
        String config = frontService.getFrontForEntity(groupId, uri, String.class);
        log.debug("end getSystemConfigByKey. config:{}", config);
        return config;
    }
}
