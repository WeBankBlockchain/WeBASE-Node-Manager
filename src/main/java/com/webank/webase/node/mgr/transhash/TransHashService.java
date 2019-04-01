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
package com.webank.webase.node.mgr.transhash;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.block.MinMaxBlock;
import com.webank.webase.node.mgr.transhash.entity.TransactionInfo;
import com.webank.webase.node.mgr.web3.Web3Service;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
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
    private Web3Service web3Service;

    /**
     * add trans hash info.
     */
    public void addTransInfo(TbTransHash tbTransHash) throws NodeMgrException {
        log.debug("start addTransInfo tbTransHash:{}", JSON.toJSONString(tbTransHash));
        Integer affectRow = transHashMapper.addTransRow(tbTransHash);
        if (affectRow == 0) {
            log.info("fail addTransInfo. hash:{} affect 0 rows of tb_block",
                tbTransHash.getTransHash());
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
        log.debug("end addTransInfo");
    }

    /**
     * query trans list.
     */
    public List<TbTransHash> queryTransList(TransListParam param) throws NodeMgrException {
        log.debug("start queryTransList. TransListParam:{}", JSON.toJSONString(param));

        List<TbTransHash> listOfTran = null;
        try {
            listOfTran = transHashMapper.listOfTransHash(param);
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
    public Integer queryCountOfTran(TransListParam queryParam) throws NodeMgrException {
        log.debug("start queryCountOfTran. queryParam:{}", JSON.toJSONString(queryParam));
        try {
            Integer count = transHashMapper.countOfTransHash(queryParam);
            log.info("end queryCountOfTran. queryParam:{} count:{}", JSON.toJSONString(queryParam),
                count);
            return count;
        } catch (RuntimeException ex) {
            log.error("fail queryCountOfTran. queryParam:{}", JSON.toJSONString(queryParam), ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query min and max block number.
     */
    public List<MinMaxBlock> queryMinMaxBlock() throws NodeMgrException {
        log.debug("start queryMinMaxBlock");
        try {
            List<MinMaxBlock> listMinMaxBlock = transHashMapper.queryMinMaxBlock();
            int listSize = Optional.ofNullable(listMinMaxBlock).map(list -> list.size()).orElse(0);
            log.info("end queryMinMaxBlock listMinMaxBlockSize:{}", listSize);
            return listMinMaxBlock;
        } catch (RuntimeException ex) {
            log.error("fail queryMinMaxBlock", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * Remove some trans info.
     */
    public Integer deleteSomeTrans(Integer groupId, BigInteger deleteBlockNumber)
        throws NodeMgrException {
        log.debug("start deleteSomeTrans. groupId:{} deleteBlockNumber:{}", groupId,
            deleteBlockNumber);

        Integer affectRow = 0;
        try {
            affectRow = transHashMapper.deleteSomeTrans(groupId, deleteBlockNumber);
        } catch (RuntimeException ex) {
            log.error("fail deleteSomeTrans. groupId:{} deleteBlockNumber:{}", groupId,
                deleteBlockNumber, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end deleteSomeTrans. groupId:{} deleteBlockNumber:{} affectRow:{}", groupId,
            deleteBlockNumber, affectRow);
        return affectRow;
    }

    /**
     * query un statistics transhash list.
     */
    public List<TbTransHash> qureyUnStatTransHashList() {
        List<TbTransHash> list = transHashMapper.listOfUnStatTransHash();
        return list;
    }

    /**
     * query un statistic transhash list by job.
     */
    public List<TbTransHash> qureyUnStatTransHashListByJob(Integer shardingTotalCount,
        Integer shardingItem) {
        List<TbTransHash> list = transHashMapper
            .listOfUnStatTransHashByJob(shardingTotalCount, shardingItem);
        return list;
    }

    /**
     * update trans statistic flag.
     */
    public void updateTransStatFlag(String transHash) {
        transHashMapper.updateTransStatFlag(transHash);
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
                tbTransHash.setGroupId(groupId);
                transList.add(tbTransHash);
            }
        }
        //find trans by block number
        if (transList.size() == 0 && blockNumber != null) {
            List<TransactionInfo> transInBlock = web3Service
                .getTransByBlockNumber(groupId, blockNumber);
            transInBlock.stream().forEach(tran -> {
                TbTransHash tbTransHash = new TbTransHash(tran.getHash(), groupId,
                    tran.getBlockNumber(), null);
                transList.add(tbTransHash);
            });
        }
        log.debug("end getTransListFromChain.");
        return transList;
    }


    /**
     * request front for transaction by hash.
     */
    public TbTransHash getTbTransFromFrontByHash(Integer groupId, String transHash)
        throws NodeMgrException {
        log.info("start getTransFromFrontByHash. groupId:{}  transhash:{}", groupId,
            transHash);
        TransactionInfo transactionInfo = web3Service.getTransaction(groupId, transHash);
        TbTransHash tbTransHash = null;
        if (transactionInfo != null) {
            tbTransHash = new TbTransHash(transHash, groupId, transactionInfo.getBlockNumber(),
                null);
        }
        log.info("end getTransFromFrontByHash. tbTransHash:{}", JSON.toJSONString(tbTransHash));
        return tbTransHash;
    }
}
