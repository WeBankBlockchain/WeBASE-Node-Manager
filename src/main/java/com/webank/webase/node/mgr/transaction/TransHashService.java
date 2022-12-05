/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.transaction;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.TableName;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.block.entity.MinMaxBlock;
import com.webank.webase.node.mgr.front.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.tools.NodeMgrTools;
import com.webank.webase.node.mgr.transaction.entity.TbTransHash;
import com.webank.webase.node.mgr.transaction.entity.TransListParam;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * services for block data.
 */
@Log4j2
@Service
public class TransHashService {

    @Autowired
    private TransHashMapper transHashMapper;
    @Autowired
    private FrontInterfaceService frontInterface;

    /**
     * add trans hash info.
     */
    public void addTransInfo(String groupId, TbTransHash tbTransHash) throws NodeMgrException {
        log.debug("start addTransInfo groupId:{} tbTransHash:{}", groupId,
                JsonTools.toJSONString(tbTransHash));
        String tableName = TableName.TRANS.getTableName(groupId);
        transHashMapper.add(tableName, tbTransHash);
        log.debug("end addTransInfo");
    }

    /**
     * query trans list.
     */
    public List<TbTransHash> queryTransList(String groupId, TransListParam param)
            throws NodeMgrException {
        log.debug("start queryTransList. TransListParam:{}", JsonTools.toJSONString(param));
        String tableName = TableName.TRANS.getTableName(groupId);
        List<TbTransHash> listOfTran = null;
        try {
            listOfTran = transHashMapper.getList(tableName, param);
        } catch (RuntimeException ex) {
            log.error("fail queryBlockList. TransListParam:{} ", JsonTools.toJSONString(param), ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end queryBlockList. listOfTran:{}", JsonTools.toJSONString(listOfTran));
        return listOfTran;
    }

    /**
     * query count of trans hash.
     */
    public Integer queryCountOfTran(String groupId, TransListParam queryParam)
            throws NodeMgrException {
        log.debug("start queryCountOfTran. queryParam:{}", JsonTools.toJSONString(queryParam));
        String tableName = TableName.TRANS.getTableName(groupId);
        try {
            Integer count = transHashMapper.getCount(tableName, queryParam);
            log.info("end queryCountOfTran. queryParam:{} count:{}", JsonTools.toJSONString(queryParam),
                    count);
            return count;
        } catch (RuntimeException ex) {
            log.error("fail queryCountOfTran. queryParam:{}", JsonTools.toJSONString(queryParam), ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }


    /**
     * get transaction in highest block
     * @param groupId
     * @return number of block height
     * @throws NodeMgrException
     */
//    public Integer queryLatestTransBlockNum(String groupId, TransListParam param)
//            throws NodeMgrException {
//        log.debug("start queryApproximateCount. groupId:{}", groupId);
//        String tableName = TableName.TRANS.getTableName(groupId);
//        try {
//            List<TbTransHash> latestBlockTrans = transHashMapper.getLatestBlockTrans(tableName, param);
//            if(latestBlockTrans.size() == 0) {
//                return 0;
//            }
//            Integer highestBlockNum = latestBlockTrans.get(0).getBlockNumber().intValue();
//            log.info("end queryApproximateCount. groupId:{} latestBlockTrans:{}",
//                    groupId, latestBlockTrans);
//            return highestBlockNum;
//        } catch (RuntimeException ex) {
//            log.error("fail queryApproximateCount. groupId:{}", groupId, ex);
//            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
//        }
//    }

    /**
     * query min and max block number.
     */
    public List<MinMaxBlock> queryMinMaxBlock(String groupId) throws NodeMgrException {
        log.debug("start queryMinMaxBlock");
        String tableName = TableName.TRANS.getTableName(groupId);
        try {
            List<MinMaxBlock> listMinMaxBlock = transHashMapper.queryMinMaxBlock(tableName);
            int listSize = Optional.ofNullable(listMinMaxBlock).map(list -> list.size()).orElse(0);
            log.info("end queryMinMaxBlock listMinMaxBlockSize:{}", listSize);
            return listMinMaxBlock;
        } catch (RuntimeException ex) {
            log.error("fail queryMinMaxBlock", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * Remove trans info.
     */
    public Integer remove(String groupId, Integer subTransNum) {
        String tableName = TableName.TRANS.getTableName(groupId);
        Integer affectRow = transHashMapper.remove(tableName, subTransNum, groupId);
        return affectRow;
    }


    /**
     * query un statistics transaction list.
     */
    public List<TbTransHash> queryUnStatTransHashList(String groupId) {
        List<TbTransHash> list = transHashMapper
                .listOfUnStatTransHash(TableName.TRANS.getTableName(groupId));
        return list;
    }

    /**
     * query un statistic transaction list by job.
     */
    public List<TbTransHash> queryUnStatTransHashListByJob(String groupId, Integer shardingTotalCount,
                                                           Integer shardingItem) {
        String tableName = TableName.TRANS.getTableName(groupId);
        List<TbTransHash> list = transHashMapper
                .listOfUnStatTransHashByJob(tableName, shardingTotalCount, shardingItem);
        return list;
    }

    /**
     * update trans statistic flag.
     */
    public void updateTransStatFlag(String groupId, String transHash) {
        String tableName = TableName.TRANS.getTableName(groupId);
        transHashMapper.updateTransStatFlag(tableName, transHash);
    }

    /**
     * get tbTransInfo from chain
     */
    public List<TbTransHash> getTransListFromChain(String groupId, String transHash,
                                                   BigInteger blockNumber) {
        log.debug("start getTransListFromChain.");
        List<TbTransHash> transList = new ArrayList<>();
        //find by transHash
        if (transHash != null) {
            TbTransHash tbTransHash = getTbTransFromFrontByHash(groupId, transHash);
            if (tbTransHash != null) {
                transList.add(tbTransHash);
            }
        }
        // find trans by block number
        if (transList.size() == 0 && blockNumber != null) {
            List<JsonTransactionResponse> transInBlock = frontInterface
                .getTransByBlockNumber(groupId, blockNumber);
            BcosBlock.Block blockOnChain = frontInterface.getBlockByNumber(groupId, blockNumber);//todo fix blockLimit
            if (transInBlock != null && transInBlock.size() != 0) {
                transInBlock.forEach(tran -> {
                    TbTransHash tbTransHash = new TbTransHash(tran.getHash(), tran.getFrom(),
                            tran.getTo(), blockNumber,
                        NodeMgrTools.timestamp2LocalDateTime(blockOnChain.getTimestamp()));
                    transList.add(tbTransHash);
                });
            }
        }
        log.debug("end getTransListFromChain.");
        return transList;
    }


    /**
     * request front for transaction by hash.
     */
    public TbTransHash getTbTransFromFrontByHash(String groupId, String transHash)
            throws NodeMgrException {
        log.info("start getTransFromFrontByHash. groupId:{}  transaction:{}", groupId,
                transHash);
        TransactionReceipt trans = frontInterface.getTransReceipt(groupId, transHash);
        BcosBlock.Block block = frontInterface.getBlockByNumber(groupId, new BigInteger(trans.getBlockNumber()));
        TbTransHash tbTransHash = null;
        if (trans != null) {
            tbTransHash = new TbTransHash(transHash, trans.getFrom(), trans.getTo(),
                new BigInteger(trans.getBlockNumber()),
                NodeMgrTools.timestamp2LocalDateTime(block.getTimestamp()));
        }
        log.info("end getTransFromFrontByHash. tbTransHash:{}", JsonTools.toJSONString(tbTransHash));
        return tbTransHash;
    }

    /**
     * get transaction receipt
     */
    public TransactionReceipt getTransReceipt(String groupId, String transHash) {
        return frontInterface.getTransReceipt(groupId, transHash);
    }


    /**
     * get transaction info
     */
    public JsonTransactionResponse getTransaction(String groupId, String transHash) {
        return frontInterface.getTransaction(groupId, transHash);
    }


    public Object getSignMessageHash(String groupId,String hash, String signUserId) {
        return frontInterface.getSignMessageHash(groupId,hash,signUserId);
    }
}
