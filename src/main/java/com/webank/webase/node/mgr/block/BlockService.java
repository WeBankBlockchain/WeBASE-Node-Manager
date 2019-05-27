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
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.TableName;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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
    @Autowired
    private ConstantProperties cProperties;


    /**
     * get block from chain by groupId
     */
    @Async(value = "mgrAsyncExecutor")
    public void pullBlockByGroupId(CountDownLatch latch, int groupId) {
        log.debug("start pullBlockByGroupId groupId:{}", groupId);
        try {
            //max block in chain
            BigInteger maxChainBlock = frontInterface.getLatestBlockNumber(groupId);
            //next block
            BigInteger nextBlock = getNextBlockNumber(groupId);

            //pull block
            while (Objects.nonNull(maxChainBlock) && maxChainBlock.compareTo(nextBlock) >= 0) {
                log.debug("continue pull block. maxChainBlock:{} nextBlock:{}", maxChainBlock,
                    nextBlock);
                Thread.sleep(cProperties.getPullBlockSleepTime());
                pullBlockByNumber(groupId, nextBlock);
                nextBlock = getNextBlockNumber(groupId);

                //reset maxChainBlock
                if (maxChainBlock.compareTo(nextBlock) < 0) {
                    maxChainBlock = frontInterface.getLatestBlockNumber(groupId);
                }
            }
        } catch (Exception ex) {
            log.error("fail pullBlockByGroupId. groupId:{} ", groupId, ex);
        }
        latch.countDown();
        log.debug("end pullBlockByGroupId groupId:{}", groupId);
    }


    /**
     * pull block by number.
     */
    private void pullBlockByNumber(int groupId, BigInteger blockNumber) {
        //get block by number
        BlockInfo blockInfo = frontInterface.getBlockByNumber(groupId, blockNumber);
        if (blockInfo == null || blockInfo.getNumber() == null) {
            log.info("pullBlockByNumber jump over. not found new block.");
            return;
        }
        //save block info
        saveBLockInfo(blockInfo, groupId);
    }

    /**
     * get next blockNumber
     */
    private BigInteger getNextBlockNumber(int groupId) {
        //get max blockNumber in table
        BigInteger localMaxBlockNumber = getLatestBlockNumber(groupId);
        if (Objects.nonNull(localMaxBlockNumber)) {
            return localMaxBlockNumber.add(BigInteger.ONE);
        }
        if (cProperties.getIsBlockPullFromZero()) {
            return BigInteger.ZERO;
        } else {
            return frontInterface.getLatestBlockNumber(groupId);
        }
    }


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
        int sealerIndex = Integer.parseInt(blockInfo.getSealer().substring(2), 16);

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

        List<TbBlock> listOfBlock = blockmapper
            .getList(TableName.BLOCK.getTableName(groupId), queryParam);
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
     * remove block into.
     */
    public Integer remove(Integer groupId, BigInteger blockRetainMax)
        throws NodeMgrException {
        String tableName = TableName.BLOCK.getTableName(groupId);
        Integer affectRow = blockmapper.remove(tableName, blockRetainMax);
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
