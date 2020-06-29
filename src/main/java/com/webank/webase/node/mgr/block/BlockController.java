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

import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.SqlSortType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.block.entity.BlockInfo;
import com.webank.webase.node.mgr.block.entity.BlockListParam;
import com.webank.webase.node.mgr.block.entity.TbBlock;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping(value = "block")
public class BlockController {

    @Autowired
    private BlockService blockService;

    /**
     * query block list.
     */
    @GetMapping(value = "/blockList/{groupId}/{pageNumber}/{pageSize}")
    public BasePageResponse queryBlockList(@PathVariable("groupId") Integer groupId,
        @PathVariable("pageNumber") Integer pageNumber,
        @PathVariable("pageSize") Integer pageSize,
        @RequestParam(value = "pkHash", required = false) String pkHash,
        @RequestParam(value = "blockNumber", required = false) BigInteger blockNumber)
        throws NodeMgrException, Exception {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info(
            "start queryBlockList startTime:{} groupId:{} pageNumber:{} pageSize:{} "
                + "pkHash:{} blockNumber:{}",
            startTime.toEpochMilli(), groupId,
            pageNumber, pageSize, pkHash, blockNumber);
        int count;
        // if query all block's count
        if(StringUtils.isEmpty(pkHash) && blockNumber == null) {
            count = blockService.queryCountOfBlockByMinus(groupId);
        } else {
            count = blockService.queryCountOfBlock(groupId, pkHash, blockNumber);
        }
        if (count > 0) {
            Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
                .orElse(null);
            BlockListParam queryParam = new BlockListParam(start, pageSize, pkHash,
                blockNumber, SqlSortType.DESC.getValue());
            List<TbBlock> blockList = blockService.queryBlockList(groupId, queryParam);
            pageResponse.setData(blockList);
            pageResponse.setTotalCount(count);
        } else {
            BlockInfo blockInfo = null;
            if (blockNumber != null) {
                log.debug("did not find block, request from front. blockNumber:{} groupId:{}",
                    blockNumber, groupId);
                blockInfo = blockService.getBlockFromFrontByNumber(groupId, blockNumber);
            } else if (StringUtils.isNotBlank(pkHash)) {
                log.debug(
                    "did not find block,request from front. pkHash:{} groupId:{}",
                    pkHash, groupId);
                try {
                    blockInfo = blockService.getblockFromFrontByHash(groupId, pkHash);
                }catch (NodeMgrException e) {
                    log.debug("queryBlockList did not find block from front(chain).e:[]", e);
                    pageResponse.setData(null);
                    pageResponse.setTotalCount(0);
                }
            }
            if (blockInfo != null) {
                TbBlock tbBlock = BlockService.chainBlock2TbBlock(blockInfo);
                pageResponse.setData(new TbBlock[]{tbBlock});
                pageResponse.setTotalCount(1);
            }
        }

        log.info("end queryBlockList useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }


    /**
     * get block by number.
     */
    @GetMapping("/blockByNumber/{groupId}/{blockNumber}")
    public BaseResponse getBlockByNumber(@PathVariable("groupId") Integer groupId,
        @PathVariable("blockNumber") BigInteger blockNumber)
        throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getBlockByNumber startTime:{} groupId:{} blockNumber:{}",
            startTime.toEpochMilli(), groupId, blockNumber);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Object blockInfo = blockService.getBlockFromFrontByNumber(groupId, blockNumber);
        baseResponse.setData(blockInfo);
        log.info("end getBlockByNumber useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }
}
