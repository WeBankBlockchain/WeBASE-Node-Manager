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
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.enums.SqlSortType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.block.entity.BlockInfo;
import com.webank.webase.node.mgr.web3.Web3Service;
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
    @Autowired
    private Web3Service web3Service;

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

        Integer count = blockService.queryCountOfBlock(groupId, pkHash, blockNumber);
        if (count != null && count > 0) {
            Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
                .orElse(null);
            BlockListParam queryParam = new BlockListParam(groupId, start, pageSize, pkHash,
                blockNumber, SqlSortType.DESC.getValue());
            List<TbBlock> blockList = blockService.queryBlockList(queryParam);
            pageResponse.setData(blockList);
            pageResponse.setTotalCount(count);
        } else {
            BlockInfo blockInfo = null;
            if (blockNumber != null) {
                log.debug("did not find block, request from front. blockNumber:{} groupId:{}",
                    blockNumber, groupId);
                 blockInfo = web3Service.getBlockByNumber(groupId, blockNumber);
            } else if (StringUtils.isNotBlank(pkHash)) {
                log.debug(
                    "did not find block,request from front. pkHash:{} groupId:{}",
                    pkHash, groupId);
                blockInfo = web3Service.getblockFromFrontByHash(groupId, pkHash);
            }
            if (blockInfo != null) {
                TbBlock tbBlock = NodeMgrTools.object2JavaBean(blockInfo,TbBlock.class);
                tbBlock.setGroupId(groupId);
                pageResponse.setData(new TbBlock[]{tbBlock});
                pageResponse.setTotalCount(1);
            }
        }

        log.info("end queryBlockList useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(pageResponse));
        return pageResponse;
    }
}
