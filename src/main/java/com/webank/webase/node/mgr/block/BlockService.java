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
package com.webank.webase.node.mgr.block;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.group.GroupService;
import com.webank.webase.node.mgr.node.NodeService;
import com.webank.webase.node.mgr.node.TbNode;
import com.webank.webase.node.mgr.transhash.TransHashService;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
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
    private NodeService nodeService;
    @Autowired
    private BlockMapper blockmapper;
    @Autowired
    private GroupService groupService;
    @Autowired
    private TransHashService transHashService;

    /**
     * save report block info.
     */
    /*@Transactional
    public void saveBLockInfo(BlockInfo blockInfo, Integer groupId) throws NodeMgrException {
        if (blockInfo == null) {
            log.debug("fail saveBlockInfo. blockInfo null");
            return;
        }
        BlockRpcResultInfo brri = blockInfo.getResult();
        Integer number = Integer.parseInt(StringUtils.removeStart(brri.getNumber(), "0x"), 16);
        BigInteger bigIntegerNumber = new BigInteger(number.toString());
        LocalDateTime localDateTime = NodeMgrTools
            .hex2LocalDateTime(StringUtils.removeStart(brri.getTimestamp(), "0x"));

        List<String> transList = brri.getTransactions();

        // save block info
        TbBlock tbBlock = new TbBlock(brri.getHash(), groupId, bigIntegerNumber,
            brri.getMinerNodeId(), localDateTime, transList.size());
        addBlockInfo(tbBlock);

        // update latest block number
        groupService.updateNetworkInfo(groupId, bigIntegerNumber);

        // save trans hashfor
        for (String hashStr : transList) {
            TbTransHash tbTransHash = new TbTransHash(hashStr, groupId, bigIntegerNumber,
                localDateTime);
            transHashService.addTransInfo(tbTransHash);
        }
    }*/


    /**
     * add block info to db.
     */
    @Transactional
    public void addBlockInfo(TbBlock tbBlock) throws NodeMgrException {
        log.debug("start addBlockInfo tbBlock:{}", JSON.toJSONString(tbBlock));
        // check reportBLock == dbMaxBLock +1
        BigInteger dbMaxBLock = blockmapper.queryLatestBlockNumber(tbBlock.getGroupId());

        BigInteger reportBlock = tbBlock.getBlockNumber();
        if (dbMaxBLock != null && !(reportBlock.compareTo(dbMaxBLock.add(numberOne)) == 0)) {
            log.info("fail addBlockInfo.  dbMaxBLock:{} reportBlock:{}", dbMaxBLock, reportBlock);
            throw new NodeMgrException(ConstantCode.NOT_SAVE_BLOCK);
        }

        // add row
        Integer affectRow = blockmapper.addBlockRow(tbBlock);
        if (affectRow == 0) {
            log.info("fail addBlockInfo. block:{} affect 0 rows of tb_block", reportBlock);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query latest block number.
     */
    public BigInteger getLatestBlockNumber(String nodeIp, Integer nodeP2PPort)
        throws NodeMgrException {
        log.debug("start getLatestBlockNumber nodeIp:{} nodeP2PPort:{}", nodeIp, nodeP2PPort);
        TbNode nodeRow = nodeService.queryNodeByIpAndP2pPort(nodeIp, nodeP2PPort);
        Integer groupId = Optional.ofNullable(nodeRow).map(node -> node.getGroupId())
            .orElseThrow(() -> new NodeMgrException(ConstantCode.INVALID_NODE_INFO));

        try {
            BigInteger latestBlock = blockmapper.queryLatestBlockNumber(groupId);
            log.debug("end getLatestBlockNumber nodeIp:{} nodeP2PPort:{} latestBlock:{}", nodeIp,
                nodeP2PPort, latestBlock);
            return latestBlock;
        } catch (RuntimeException ex) {
            log.error("add row exception", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query block info list.
     */
    public List<TbBlock> queryBlockList(BlockListParam queryParam) throws NodeMgrException {
        log.debug("start queryBlockList queryParam:{}", JSON.toJSONString(queryParam));

        List<TbBlock> listOfBlock = null;
        try {
            listOfBlock = blockmapper.listOfBlock(queryParam);
        } catch (RuntimeException ex) {
            log.error("fail queryBlockList. queryParam:{}", JSON.toJSONString(queryParam), ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

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
            Integer count = blockmapper.countOfBlock(groupId, pkHash, blockNumber);
            log.info("end countOfBlock groupId:{} pkHash:{} count:{}", groupId, pkHash, count);
            return count;
        } catch (RuntimeException ex) {
            log.error("fail countOfBlock groupId:{} pkHash:{}", groupId, pkHash, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query the min and max block number of tb_block.
     */
    public List<MinMaxBlock> queryMinMaxBlock() throws NodeMgrException {
        log.debug("start queryMinMaxBlock");
        try {
            List<MinMaxBlock> listMinMaxBlock = blockmapper.queryMinMaxBlock();
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
            affectRow = blockmapper.deleteSomeBlocks(groupId, deleteBlockNumber);
        } catch (RuntimeException ex) {
            log.error("fail deleteSomeBlocks. groupId:{} deleteBlockNumber:{}", groupId,
                deleteBlockNumber, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end deleteSomeBlocks. groupId:{} deleteBlockNumber:{} affectRow:{}", groupId,
            deleteBlockNumber, affectRow);
        return affectRow;
    }

}
