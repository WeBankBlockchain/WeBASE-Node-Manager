/**
 * Copyright 2014-2019  the original author or authors.
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

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.TableName;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.block.entity.MinMaxBlock;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.transaction.entity.TbTransHash;
import com.webank.webase.node.mgr.transaction.entity.TransListParam;
import com.webank.webase.node.mgr.transaction.entity.TransReceipt;
import com.webank.webase.node.mgr.transaction.entity.TransactionInfo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

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
    public void addTransInfo(int groupId, TbTransHash tbTransHash) throws NodeMgrException {
        log.debug("start addTransInfo groupId:{} tbTransHash:{}", groupId,
                JSON.toJSONString(tbTransHash));
        String tableName = TableName.TRANS.getTableName(groupId);
        transHashMapper.add(tableName, tbTransHash);
        log.debug("end addTransInfo");
    }

    /**
     * query trans list.
     */
    public List<TbTransHash> queryTransList(int groupId, TransListParam param)
            throws NodeMgrException {
        log.debug("start queryTransList. TransListParam:{}", JSON.toJSONString(param));
        String tableName = TableName.TRANS.getTableName(groupId);
        List<TbTransHash> listOfTran = null;
        try {
            listOfTran = transHashMapper.getList(tableName, param);
        } catch (RuntimeException ex) {
            log.error("fail queryBlockList. TransListParam:{} ", JSON.toJSONString(param), ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end queryBlockList. listOfTran:{}", JSON.toJSONString(listOfTran));
        return listOfTran;
    }

    /**
     * query count of trans hash.
     */
    public Integer queryCountOfTran(int groupId, TransListParam queryParam)
            throws NodeMgrException {
        log.debug("start queryCountOfTran. queryParam:{}", JSON.toJSONString(queryParam));
        String tableName = TableName.TRANS.getTableName(groupId);
        try {
            Integer count = transHashMapper.getCount(tableName, queryParam);
            log.info("end queryCountOfTran. queryParam:{} count:{}", JSON.toJSONString(queryParam),
                    count);
            return count;
        } catch (RuntimeException ex) {
            log.error("fail queryCountOfTran. queryParam:{}", JSON.toJSONString(queryParam), ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query count of trans by minus max and min trans_number
     */
    public Integer queryCountOfTranByMinus(int groupId)
            throws NodeMgrException {
        log.debug("start queryCountOfTranByMinus.");
        String tableName = TableName.TRANS.getTableName(groupId);
        try {
            Integer count = transHashMapper.getCountByMinMax(tableName);
            log.info("end queryCountOfTranByMinus. count:{}",  count);
            if (count == null) {
                return 0;
            }
            return count;
        } catch (BadSqlGrammarException ex) {
            // TODO v1.2.2+: if trans_number not exists, use queryCountOfTran() instead
            log.error("fail queryCountOfTranByMinus. ", ex);
            log.info("restart from queryCountOfTranByMinus to queryCountOfTran: []", ex.getCause());
            TransListParam queryParam = new TransListParam(null, null);
            Integer count = queryCountOfTran(groupId, queryParam);
            return count;
        } catch (RuntimeException ex) {
            log.error("fail queryCountOfTranByMinus. ", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * get transaction in highest block
     * @param groupId
     * @return number of block height
     * @throws NodeMgrException
     */
//    public Integer queryLatestTransBlockNum(int groupId, TransListParam param)
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
    public List<MinMaxBlock> queryMinMaxBlock(int groupId) throws NodeMgrException {
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
    public Integer remove(Integer groupId, Integer subTransNum) {
        String tableName = TableName.TRANS.getTableName(groupId);
        Integer affectRow = transHashMapper.remove(tableName, subTransNum, groupId);
        return affectRow;
    }


    /**
     * query un statistics transaction list.
     */
    public List<TbTransHash> qureyUnStatTransHashList(int groupId) {
        List<TbTransHash> list = transHashMapper
                .listOfUnStatTransHash(TableName.TRANS.getTableName(groupId));
        return list;
    }

    /**
     * query un statistic transaction list by job.
     */
    public List<TbTransHash> qureyUnStatTransHashListByJob(int groupId, Integer shardingTotalCount,
                                                           Integer shardingItem) {
        String tableName = TableName.TRANS.getTableName(groupId);
        List<TbTransHash> list = transHashMapper
                .listOfUnStatTransHashByJob(tableName, shardingTotalCount, shardingItem);
        return list;
    }

    /**
     * update trans statistic flag.
     */
    public void updateTransStatFlag(int groupId, String transHash) {
        String tableName = TableName.TRANS.getTableName(groupId);
        transHashMapper.updateTransStatFlag(tableName, transHash);
    }

    /**
     * get tbTransInfo from chain
     */
    public List<TbTransHash> getTransListFromChain(Integer groupId, String transHash,
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
        //find trans by block number
        if (transList.size() == 0 && blockNumber != null) {
            List<TransactionInfo> transInBlock = frontInterface
                    .getTransByBlockNumber(groupId, blockNumber);
            if(transInBlock != null && transInBlock.size() != 0) {
                transInBlock.stream().forEach(tran -> {
                    TbTransHash tbTransHash = new TbTransHash(tran.getHash(), tran.getFrom(),
                            tran.getTo(), tran.getBlockNumber(),
                            null);
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
    public TbTransHash getTbTransFromFrontByHash(Integer groupId, String transHash)
            throws NodeMgrException {
        log.info("start getTransFromFrontByHash. groupId:{}  transaction:{}", groupId,
                transHash);
        TransactionInfo trans = frontInterface.getTransaction(groupId, transHash);
        TbTransHash tbTransHash = null;
        if (trans != null) {
            tbTransHash = new TbTransHash(transHash, trans.getFrom(), trans.getTo(),
                    trans.getBlockNumber(), null);
        }
        log.info("end getTransFromFrontByHash. tbTransHash:{}", JSON.toJSONString(tbTransHash));
        return tbTransHash;
    }

    /**
     * get transaction receipt
     */
    public TransReceipt getTransReceipt(int groupId, String transHash) {
        return frontInterface.getTransReceipt(groupId, transHash);
    }


    /**
     * get transaction info
     */
    public TransactionInfo getTransaction(int groupId, String transHash) {
        return frontInterface.getTransaction(groupId, transHash);
    }
}
