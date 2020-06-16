/**
 * Copyright 2014-2020  the original author or authors.
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

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.TableName;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.block.entity.BlockInfo;
import com.webank.webase.node.mgr.block.entity.BlockListParam;
import com.webank.webase.node.mgr.block.entity.TbBlock;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.transaction.TransHashService;
import com.webank.webase.node.mgr.transaction.entity.TbTransHash;
import com.webank.webase.node.mgr.transaction.entity.TransactionInfo;
import lombok.extern.log4j.Log4j2;

/**
 * services for block data.
 * including pull block from chain and block service
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
    private static final Long SAVE_TRANS_SLEEP_TIME = 5L;


    /**
     * get block from chain by groupId
     * ThreadPool configuration in /base/config/BeanConfig
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
        }finally {
            // finish one group, count down
            latch.countDown();
        }
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
            BigInteger initBlock = frontInterface.getLatestBlockNumber(groupId);
            if (initBlock.compareTo(cProperties.getPullBlockInitCnts()) > 0) {
                initBlock = initBlock.subtract(cProperties.getPullBlockInitCnts().
                        subtract(BigInteger.valueOf(1)));
            } else {
                initBlock = BigInteger.ZERO;
            }
            log.info("=== getNextBlockNumber init groupId:{} initBlock:{}", groupId, initBlock);
            return initBlock;
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
        LocalDateTime blockTimestamp = NodeMgrTools
                .timestamp2LocalDateTime(Long.valueOf(blockInfo.getTimestamp()));
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
            try {
                Thread.sleep(SAVE_TRANS_SLEEP_TIME);
            } catch (InterruptedException ex) {
                log.error("saveBLockInfo", ex);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * add block info to db.
     */
    @Transactional
    public void addBlockInfo(TbBlock tbBlock, int groupId) throws NodeMgrException {
        log.debug("start addBlockInfo tbBlock:{}", JsonTools.toJSONString(tbBlock));
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
            JsonTools.toJSONString(queryParam));

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
    public Integer queryCountOfBlock(Integer groupId, String pkHash, BigInteger blockNumber)
        throws NodeMgrException {
        log.debug("start countOfBlock groupId:{} pkHash:{} blockNumber:{}", groupId, pkHash,
            blockNumber);
        try {
            Integer count = blockmapper
                .getCount(TableName.BLOCK.getTableName(groupId), pkHash, blockNumber);
            log.info("end countOfBlock groupId:{} pkHash:{} count:{}", groupId, pkHash, count);
            if(count == null) {
                return 0;
            }
            return count;
        } catch (RuntimeException ex) {
            log.error("fail countOfBlock groupId:{} pkHash:{}", groupId, pkHash, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    public Integer queryCountOfBlockByMinus(Integer groupId) {
        log.debug("start queryCountOfBlockByMinus groupId:{}", groupId);
        try {
            Integer count = blockmapper
                    .getBlockCountByMinMax(TableName.BLOCK.getTableName(groupId));
            log.info("end queryCountOfBlockByMinus groupId:{} count:{}", groupId, count);
            if(count == null) {
                return 0;
            }
            return count;
        } catch (RuntimeException ex) {
            log.error("fail queryCountOfBlockByMinus groupId:{},exception:{}", groupId, ex);
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
        String sealer = "0x0";
        if (sealerList != null && sealerList.size() > 0) {
            if (tbBlock.getSealerIndex() < sealerList.size()) {
                sealer = sealerList.get(tbBlock.getSealerIndex());
            } else {
                sealer = sealerList.get(0);
            }
        }
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

    /**
     * get smallest block height in local db
	 *
     */
    public TbBlock getSmallestBlockInfo(int groupId) {
        BigInteger smallestHeight = getSmallestBlockHeight(groupId);
        if (smallestHeight == null) {
        	log.debug("getSmallestBlockInfo groupId:{} has no block local", groupId);
        	return null;
		}
        return getBlockByBlockNumber(groupId, smallestHeight);
    }

    /**
     * get block of smallest height in local db
     * @param groupId
     */
    public BigInteger getSmallestBlockHeight(int groupId) {
        return blockmapper.getSmallestBlockNumber(TableName.BLOCK.getTableName(groupId));
    }

    /**
     * get local tbBlock by blockNumber
     * @param groupId
     * @param blockNumber
     * @return TbBlock
     */
    public TbBlock getBlockByBlockNumber(int groupId, BigInteger blockNumber) {
        return blockmapper.getBlockByBlockNumber(TableName.BLOCK.getTableName(groupId),
                blockNumber);
    }
}
