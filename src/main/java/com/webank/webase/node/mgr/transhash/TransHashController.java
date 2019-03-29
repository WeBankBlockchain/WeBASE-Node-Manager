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
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.enums.SqlSortType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.front.FrontService;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping(value = "transaction")
public class TransHashController {

    @Autowired
    private TransHashService transHashService;
    @Autowired
    private FrontService frontService;

    /**
     * query trans list.
     */
    @GetMapping(value = "/transList/{networkId}/{pageNumber}/{pageSize}")
    public BasePageResponse queryTransList(@PathVariable("networkId") Integer networkId,
        @PathVariable("pageNumber") Integer pageNumber,
        @PathVariable("pageSize") Integer pageSize,
        @RequestParam(value = "transactionHash", required = false) String transHash,
        @RequestParam(value = "blockNumber", required = false) BigInteger blockNumber)
        throws NodeMgrException, Exception {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info(
            "start queryTransList. startTime:{} networkId:{} pageNumber:{} pageSize:{} "
                + "transhash:{}",
            startTime.toEpochMilli(), networkId,pageNumber, pageSize, transHash);

        TransListParam queryParam = new TransListParam(networkId, transHash, blockNumber);

        Integer count = transHashService.queryCountOfTran(queryParam);
        if (count != null && count > 0) {
            Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
                .orElse(null);
            queryParam.setStart(start);
            queryParam.setPageSize(pageSize);
            queryParam.setFlagSortedByBlock(SqlSortType.DESC.getValue());
            List<TbTransHash> transList = transHashService.queryTransList(queryParam);
            pageResponse.setData(transList);
            pageResponse.setTotalCount(count);
        } else {
            List<TbTransHash> transList = transHashService.getTransListFromChain(networkId,transHash,blockNumber);
            //result
            if (transList.size() > 0) {
                pageResponse.setData(transList);
                pageResponse.setTotalCount(transList.size());
            }
        }

        log.info("end queryBlockList useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(pageResponse));
        return pageResponse;
    }
}
