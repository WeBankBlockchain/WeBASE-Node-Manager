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
package com.webank.webase.node.mgr.block;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.enums.TableName;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.block.entity.BlockInfo;
import com.webank.webase.node.mgr.block.entity.BlockListParam;
import com.webank.webase.node.mgr.block.entity.MinMaxBlock;
import com.webank.webase.node.mgr.block.entity.TbBlock;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.transaction.TransHashService;
import com.webank.webase.node.mgr.transaction.entity.TbTransHash;
import com.webank.webase.node.mgr.transaction.entity.TransactionInfo;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * services for block data.
 */
@Log4j2
@Service
public class BlockService {

    BigInteger numberOne = new BigInteger("1");
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private BlockMapper blockmapper;
    @Autowired
    private TransHashService transHashService;

    /**
     * copy chainBlock properties;
     */
    public static TbBlock chainBlock2TbBlock(BlockInfo blockInfo) {
        if (blockInfo == null) {
            return null;
        }
        BigInteger bigIntegerNumber = blockInfo.getNumber();
        LocalDateTime blockTimestamp = LocalDateTime.MIN;
        if (bigIntegerNumber != BigInteger.ZERO) {
            blockTimestamp = NodeMgrTools
                .timestamp2LocalDateTime(Long.valueOf(blockInfo.getTimestamp()));
        }
        int sealerIndex = Integer.parseInt(blockInfo.getSealer().substring(2),16);

        List<TransactionInfo> transList = blockInfo.getTransactions();

        // save block info
        TbBlock tbBlock = new TbBlock(blockInfo.getHash(), bigIntegerNumber, blockTimestamp,
            transList.size(), sealerIndex);
        return tbBlock;
    }

    /**
     * save report block info.
     */
    @Transactional
    public void saveBLockInfo(BlockInfo blockInfo, Integer groupId) throws NodeMgrException {
        List<TransactionInfo> transList = blockInfo.getTransactions();

        // save block info
        TbBlock tbBlock = chainBlock2TbBlock(blockInfo);
        addBlockInfo(tbBlock, groupId);

        // save trans hash
        for (TransactionInfo trans : transList) {
            TbTransHash tbTransHash = new TbTransHash(trans.getHash(), trans.getFrom(),
                trans.getTo(), tbBlock.getBlockNumber(), tbBlock.getBlockTimestamp());
            transHashService.addTransInfo(groupId, tbTransHash);
        }
    }

    /**
     * add block info to db.
     */
    @Transactional
    public void addBlockInfo(TbBlock tbBlock, int groupId) throws NodeMgrException {
        log.debug("start addBlockInfo tbBlock:{}", JSON.toJSONString(tbBlock));
        String tableName = TableName.BLOCK.getTableName(groupId);
        //check newBLock == dbMaxBLock +1
        BigInteger dbMaxBLock = blockmapper.getLatestBlockNumber(tableName);
        BigInteger pullBlockNumber = tbBlock.getBlockNumber();
        if (dbMaxBLock != null && !(pullBlockNumber.compareTo(dbMaxBLock.add(numberOne)) == 0)) {
            log.info("fail addBlockInfo.  dbMaxBLock:{} pullBlockNumber:{}", dbMaxBLock,
                pullBlockNumber);
            return;
        }

        // save block info
        blockmapper.add(tableName, tbBlock);
    }

    /**
     * query block info list.
     */
    public List<TbBlock> queryBlockList(int groupId, BlockListParam queryParam)
        throws NodeMgrException {
        log.debug("start queryBlockList groupId:{},queryParam:{}", groupId,
            JSON.toJSONString(queryParam));

        List<TbBlock>  listOfBlock = blockmapper.getList(TableName.BLOCK.getTableName(groupId), queryParam);
        //check sealer
        listOfBlock.stream().forEach(block -> checkSearlerOfBlock(groupId, block));

        log.debug("end queryBlockList listOfBlockSize:{}", listOfBlock.size());
        return listOfBlock;
    }

    /**
     * query count of block.
     */
    public int queryCountOfBlock(Integer groupId, String pkHash, BigInteger blockNumber)
        throws NodeMgrException {
        log.debug("start countOfBlock groupId:{} pkHash:{} blockNumber:{}", groupId, pkHash,
            blockNumber);
        try {
            int count = blockmapper
                .getCount(TableName.BLOCK.getTableName(groupId), pkHash, blockNumber);
            log.info("end countOfBlock groupId:{} pkHash:{} count:{}", groupId, pkHash, count);
            return count;
        } catch (RuntimeException ex) {
            log.error("fail countOfBlock groupId:{} pkHash:{}", groupId, pkHash, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * get sealer by index.
     */
    public void checkSearlerOfBlock(int groupId, TbBlock tbBlock) {
        if (StringUtils.isNotBlank(tbBlock.getSealer())) {
            return;
        }

        //get sealer from chain.
        List<String> sealerList = frontInterface.getSealerList(groupId);
        String sealer = sealerList.get(tbBlock.getSealerIndex());
        tbBlock.setSealer(sealer);

        //save sealer
        blockmapper.update(TableName.BLOCK.getTableName(groupId), tbBlock);
    }

    /**
     * query the min and max block number of tb_block.
     */
    public List<MinMaxBlock> queryMinMaxBlock(int groupId) throws NodeMgrException {
        log.debug("start queryMinMaxBlock");
        try {
            List<MinMaxBlock> listMinMaxBlock = blockmapper
                .queryMinMaxBlock(TableName.BLOCK.getTableName(groupId));
            int listSize = Optional.ofNullable(listMinMaxBlock).map(list -> list.size()).orElse(0);
            log.info("end queryMinMaxBlock listMinMaxBlockSize:{}", listSize);
            return listMinMaxBlock;
        } catch (RuntimeException ex) {
            log.error("fail queryMinMaxBlock", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }


    /**
     * Remove all block heights less than inputValue.
     */
    public Integer deleteSomeBlocks(Integer groupId, BigInteger deleteBlockNumber)
        throws NodeMgrException {
        log.debug("start deleteSomeBlocks. groupId:{} deleteBlockNumber:{}", groupId,
            deleteBlockNumber);

        Integer affectRow = 0;
        try {
            affectRow = blockmapper
                .remove(TableName.BLOCK.getTableName(groupId), deleteBlockNumber);
        } catch (RuntimeException ex) {
            log.error("fail deleteSomeBlocks. groupId:{} deleteBlockNumber:{}", groupId,
                deleteBlockNumber, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end deleteSomeBlocks. groupId:{} deleteBlockNumber:{} affectRow:{}", groupId,
            deleteBlockNumber, affectRow);
        return affectRow;
    }

    /**
     * get latest block number
     */
    public BigInteger getLatestBlockNumber(int groupId) {
        return blockmapper.getLatestBlockNumber(TableName.BLOCK.getTableName(groupId));
    }

    /**
     * get block by block from front server
     */
    public BlockInfo getBlockFromFrontByNumber(int groupId, BigInteger blockNumber) {
        return frontInterface.getBlockByNumber(groupId, blockNumber);
    }

    /**
     * get block by block from front server
     */
    public BlockInfo getblockFromFrontByHash(int groupId, String pkHash) {
        return frontInterface.getblockByHash(groupId, pkHash);
    }
}
