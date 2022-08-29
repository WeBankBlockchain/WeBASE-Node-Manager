/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.transaction;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.SqlSortType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.transaction.entity.ReqSignMessage;
import com.webank.webase.node.mgr.transaction.entity.TbTransHash;
import com.webank.webase.node.mgr.transaction.entity.TransListParam;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Log4j2
@RestController
@RequestMapping(value = "transaction")
public class TransHashController extends BaseController {

    @Autowired
    private TransHashService transHashService;


    /**
     * query trans list.
     */
    @GetMapping(value = "/transList/{groupId}/{pageNumber}/{pageSize}")
    public BasePageResponse queryTransList(@PathVariable("groupId") String groupId,
        @PathVariable("pageNumber") Integer pageNumber,
        @PathVariable("pageSize") Integer pageSize,
        @RequestParam(value = "transactionHash", required = false) String transHash,
        @RequestParam(value = "blockNumber", required = false) BigInteger blockNumber) {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info(
            "start queryTransList. startTime:{} groupId:{} pageNumber:{} pageSize:{} "
                + "transaction:{}",
            startTime.toEpochMilli(), groupId, pageNumber, pageSize, transHash);
        TransListParam queryParam = new TransListParam(transHash, blockNumber);
        Integer count;
        // if param's empty, getCount by minus between max and min
        if (StringUtils.isEmpty(transHash) && blockNumber == null) {
//            count = transHashService.queryCountOfTranByMinus(groupId);
            // 接口select准确的分页，但是delete task使用minmax的值进行删除
            count = transHashService.queryCountOfTran(groupId, queryParam);
        } else {
            // select count(1) in InnoDb is slow when data gets large, instead use tx_id to record count
            count = transHashService.queryCountOfTran(groupId, queryParam);
        }
        if (count != null && count > 0) {
            Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
                .orElse(null);
            queryParam.setStart(start);
            queryParam.setPageSize(pageSize);
            queryParam.setFlagSortedByBlock(SqlSortType.DESC.getValue());
            List<TbTransHash> transList = transHashService.queryTransList(groupId, queryParam);
            pageResponse.setData(transList);
            // on chain tx count
            pageResponse.setTotalCount(count);
        } else {
            List<TbTransHash> transList = new ArrayList<>();
            transList = transHashService.getTransListFromChain(groupId, transHash, blockNumber);
            //result
            if (transList.size() > 0) {
                pageResponse.setData(transList);
                pageResponse.setTotalCount(transList.size());
            }
        }

        log.info("end queryBlockList useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }

    /**
     * get transaction receipt.
     */
    @GetMapping("/transactionReceipt/{groupId}/{transHash}")
    public BaseResponse getTransReceipt(@PathVariable("groupId") String groupId,
        @PathVariable("transHash") String transHash)
        throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getTransReceipt startTime:{} groupId:{} transaction:{}",
            startTime.toEpochMilli(), groupId, transHash);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        TransactionReceipt transReceipt = transHashService.getTransReceipt(groupId, transHash);
        baseResponse.setData(transReceipt);
        log.info("end getTransReceipt useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get transaction by hash.
     */
    @GetMapping("/transInfo/{groupId}/{transHash}")
    public BaseResponse getTransaction(@PathVariable("groupId") String groupId,
        @PathVariable("transHash") String transHash)
        throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getTransaction startTime:{} groupId:{} transaction:{}",
            startTime.toEpochMilli(), groupId, transHash);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        JsonTransactionResponse transInfo = transHashService.getTransaction(groupId, transHash);
        baseResponse.setData(transInfo);
        log.info("end getTransaction useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * sign transaction hash.
     */
    @PostMapping("/signMessageHash")
    public Object signMessageHash(@RequestBody @Valid ReqSignMessage reqSignMessage,
        BindingResult result)
        throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start getTransaction startTime:{} hash:{} signUserId:{} groupId:{} ",
            startTime.toEpochMilli(), reqSignMessage.getHash(), reqSignMessage.getSignUserId(),
            reqSignMessage.getGroupId());
        Object object = transHashService.getSignMessageHash(reqSignMessage.getGroupId(),
            reqSignMessage.getHash(), reqSignMessage.getSignUserId());
        log.info("end signMessageHash useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(object));
        return object;
    }
}
