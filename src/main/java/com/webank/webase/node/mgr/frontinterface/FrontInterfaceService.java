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
package com.webank.webase.node.mgr.frontinterface;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.block.entity.BlockInfo;
import com.webank.webase.node.mgr.front.entity.TotalTransCountInfo;
import com.webank.webase.node.mgr.monitor.ChainTransInfo;
import com.webank.webase.node.mgr.node.entity.PeerInfo;
import com.webank.webase.node.mgr.transaction.entity.TransReceipt;
import com.webank.webase.node.mgr.transaction.entity.TransactionInfo;
import java.math.BigInteger;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Log4j2
@Service
public class FrontInterfaceService {
    @Autowired
    private FrontRestTools frontRestTools;
    @Qualifier(value = "genericRestTemplate")
    @Autowired
    private RestTemplate genericRestTemplate;


    /**
     * get group list
     */
    public List<String> getGroupList(String nodeIp, Integer frontPort) {
        log.debug("start getGroupList. groupId:{} frontPort:{}", nodeIp, frontPort);
        String url = String
            .format(FrontRestTools.FRONT_URL, nodeIp, frontPort, Integer.MAX_VALUE,
                FrontRestTools.FRONT_GROUP_PLIST);
        log.info("getGroupList. url:{}", url);
        List<String> groupList;
        try {
            groupList =genericRestTemplate.getForObject(url, List.class);
        } catch (Exception e) {
            log.warn("fail getGroupList url:{} errorMsg:{}", url, e.getMessage());
            throw new NodeMgrException(ConstantCode.INVALID_FRONT_INFO);
        }

        log.debug("end getGroupList. groupList:{}", JSON.toJSONString(groupList));
        return groupList;
    }

    /**
     * get peers
     */
    public List<PeerInfo> getPeers(String frontIp, Integer frontPort, Integer groupId) {
        String url = String
            .format(FrontRestTools.FRONT_URL, frontIp, frontPort, groupId, FrontRestTools.FRONT_PEERS);
        log.info("getGroupList. url:{}", url);
        List<PeerInfo> peersList = genericRestTemplate.getForObject(url, List.class);
        return peersList;
    }

    /**
     * get contract code.
     */
    public String getContractCode(Integer groupId, String address, BigInteger blockNumber)
        throws NodeMgrException {
        log.debug("start getContractCode groupId:{} address:{} blockNumber:{}", groupId,
            address, blockNumber);
        String uri = String.format(FrontRestTools.FRONT_CODE_URI, address, blockNumber);
        String contractCode = frontRestTools.getFrontForEntity(groupId, uri, String.class);
        log.debug("end getContractCode. contractCode:{}", contractCode);
        return contractCode;
    }

    /**
     * get transaction receipt.
     */
    public TransReceipt getTransReceipt(Integer groupId, String transHash)
        throws NodeMgrException {
        log.debug("start getTransReceipt groupId:{} transaction:{}", groupId, transHash);
        String uri = String.format(FrontRestTools.FRONT_TRANS_RECEIPT_BY_HASH_URI, transHash);
        TransReceipt transReceipt = frontRestTools.getFrontForEntity(groupId, uri, TransReceipt.class);
        log.debug("end getTransReceipt");
        return transReceipt;
    }

    /**
     * get transaction by hash.
     */
    public TransactionInfo getTransaction(Integer groupId, String transHash)
        throws NodeMgrException {
        log.debug("start getTransaction groupId:{} transaction:{}", groupId, transHash);
        if (StringUtils.isBlank(transHash)) {
            return null;
        }
        String uri = String.format(FrontRestTools.FRONT_TRANS_BY_HASH_URI, transHash);
        TransactionInfo transInfo = frontRestTools
            .getFrontForEntity(groupId, uri, TransactionInfo.class);
        log.debug("end getTransaction");
        return transInfo;
    }

    /**
     * get block by number.
     */
    public BlockInfo getBlockByNumber(Integer groupId, BigInteger blockNumber)
        throws NodeMgrException {
        log.debug("start getBlockByNumber groupId:{} blockNumber:{}", groupId, blockNumber);
        String uri = String.format(FrontRestTools.FRONT_BLOCK_BY_NUMBER_URI, blockNumber);
        BlockInfo blockInfo = frontRestTools.getFrontForEntity(groupId, uri, BlockInfo.class);
        log.debug("end getBlockByNumber");
        return blockInfo;
    }

    /**
     * request for node heartBeat.
     */
/*    public BaseResponse nodeHeartBeat(String nodeIp, Integer frontPort) throws NodeMgrException {
        log.debug("start nodeHeartBeat. frontPort:{} FrontPort:{}", nodeIp, frontPort);
        String url = String
            .format(FrontRestTools.FRONT_URL, nodeIp, frontPort, FrontRestTools.FRONT_NODE_HEARTBEAT);
        BaseResponse frontRsp = genericRestTemplate.getForObject(url, BaseResponse.class);
        log.debug("end nodeHeartBeat. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }*/


    /**
     * request front for block by hash.
     */
    public BlockInfo getblockFromFrontByHash(Integer groupId, String pkHash)
        throws NodeMgrException {
        log.debug("start getblockFromFrontByHash. groupId:{}  pkHash:{}", groupId,
            pkHash);
        String uri = String.format(FrontRestTools.FRONT_BLOCK_BY_HASH_URI, pkHash);
        BlockInfo blockInfo = frontRestTools.getFrontForEntity(groupId, uri, BlockInfo.class);
        log.debug("end getblockFromFrontByHash. blockInfo:{}", JSON.toJSONString(blockInfo));
        return blockInfo;
    }


    /**
     * getTransFromFrontByHash.
     */
    public ChainTransInfo getTransInfoFromFrontByHash(Integer groupId, String hash)
        throws NodeMgrException {
        log.debug("start getTransFromFrontByHash. groupId:{} hash:{}", groupId, hash);
        TransactionInfo transaction = getTransaction(groupId, hash);
        ChainTransInfo chainTransInfo = new ChainTransInfo(transaction.getFrom(),
            transaction.getTo(), transaction.getInput());
        log.debug("end getTransFromFrontByHash:{}", JSON.toJSONString(chainTransInfo));
        return chainTransInfo;
    }

    /**
     * getAddressFromFrontByHash.
     */
    public String getAddressFromFrontByHash(Integer groupId, String transHash)
        throws NodeMgrException {
        log.debug("start getAddressFromFrontByHash. groupId:{} transHash:{}", groupId, transHash);

        TransReceipt transReceipt = getTransReceipt(groupId, transHash);
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
        String uri = String.format(FrontRestTools.FRONT_CODE_URI, address, blockNumber);
        String code = frontRestTools.getFrontForEntity(groupId, uri, String.class);

        log.debug("end getCodeFromFront:{}", code);
        return code;
    }

    /**
     * get total transaction count
     */
    public TotalTransCountInfo getTotalTransactionCount(Integer groupId) {
        log.debug("start getTotalTransactionCount. groupId:{}", groupId);
        TotalTransCountInfo totalCount = frontRestTools
            .getFrontForEntity(groupId, FrontRestTools.FRONT_TRANS_TOTAL_URI, TotalTransCountInfo.class);
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
        List<String> groupPeers = frontRestTools
            .getFrontForEntity(groupId, FrontRestTools.FRONT_GROUP_PEERS, List.class);
        log.debug("end getGroupPeers. groupPeers:{}", JSON.toJSONString(groupPeers));
        return groupPeers;
    }


    /**
     * get consensusStatus
     */
    public String getConsensusStatus(Integer groupId) {
        log.debug("start getConsensusStatus. groupId:{}", groupId);
        String consensusStatus = frontRestTools
            .getFrontForEntity(groupId, FrontRestTools.FRONT_CONSENSUS_STATUS, String.class);
        log.debug("end getConsensusStatus. consensusStatus:{}", consensusStatus);
        return consensusStatus;
    }

    /**
     * get syncStatus
     */
    public String syncStatus(Integer groupId) {
        log.debug("start syncStatus. groupId:{}", groupId);
        String ststus = frontRestTools
            .getFrontForEntity(groupId, FrontRestTools.FRONT_CSYNC_STATUS, String.class);
        log.debug("end syncStatus. ststus:{}", ststus);
        return ststus;
    }

    /**
     * get latest block number
     */
    public BigInteger getLatestBlockNumber(Integer groupId) {
        log.debug("start getLatestBlockNumber. groupId:{}", groupId);
        BigInteger latestBlockNmber = frontRestTools
            .getFrontForEntity(groupId, FrontRestTools.FRONT_BLOCK_NUMBER, BigInteger.class);
        log.debug("end getLatestBlockNumber. latestBlockNmber:{}", latestBlockNmber);
        return latestBlockNmber;
    }


    /**
     * get config by key
     */
    public String getSystemConfigByKey(Integer groupId, String key) {
        log.debug("start getSystemConfigByKey. groupId:{}", groupId);
        String uri = String.format(FrontRestTools.FRONT_SYSTEMCONFIG_BY_KEY, key);
        String config = frontRestTools.getFrontForEntity(groupId, uri, String.class);
        log.debug("end getSystemConfigByKey. config:{}", config);
        return config;
    }
}
