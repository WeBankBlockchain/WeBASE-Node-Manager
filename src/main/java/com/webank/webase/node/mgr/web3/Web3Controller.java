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
package com.webank.webase.node.mgr.web3;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.transhash.entity.TransReceipt;
import com.webank.webase.node.mgr.transhash.entity.TransactionInfo;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * web3api interface.
 */
@Log4j2
@RestController
@RequestMapping("web3")
public class Web3Controller {

    @Autowired
    private Web3Service web3Service;

    /**
     * get contract code.
     */
    @GetMapping("/code/{groupId}/{address}/{blockNumber}")
    public BaseResponse getContractCode(@PathVariable("groupId") Integer groupId,
        @PathVariable("address") String address,
        @PathVariable("blockNumber") BigInteger blockNumber) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getContractCode startTime:{} groupId:{} address:{} blockNumber:{}",
            startTime.toEpochMilli(), groupId, address,blockNumber);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        String contractCode = web3Service.getContractCode(groupId, address, blockNumber);
        baseResponse.setData(contractCode);
        log.info("end getContractCode useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get transaction receipt.
     */
    @GetMapping("/transactionReceipt/{groupId}/{transHash}")
    public BaseResponse getTransReceipt(@PathVariable("groupId") Integer groupId,
        @PathVariable("transHash") String transHash)
        throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getTransReceipt startTime:{} groupId:{} transhash:{}",
            startTime.toEpochMilli(), groupId, transHash);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        TransReceipt transReceipt =  web3Service.getTransReceipt(groupId, transHash);
        baseResponse.setData(transReceipt);
        log.info("end getTransReceipt useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get transaction by hash.
     */
    @GetMapping("/transaction/{groupId}/{transHash}")
    public BaseResponse getTransaction(@PathVariable("groupId") Integer groupId,
        @PathVariable("transHash") String transHash)
        throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getTransaction startTime:{} groupId:{} transhash:{}",
            startTime.toEpochMilli(), groupId, transHash);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        TransactionInfo transInfo = web3Service.getTransaction(groupId, transHash);
        baseResponse.setData(transInfo);
        log.info("end getTransaction useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
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
        Object blockInfo = web3Service.getBlockByNumber(groupId, blockNumber);
        baseResponse.setData(blockInfo);
        log.info("end getBlockByNumber useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }

}
