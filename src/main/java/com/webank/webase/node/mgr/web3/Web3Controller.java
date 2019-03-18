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
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
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
    @GetMapping("/code/{networkId}/{address}/{blockNumber}")
    public BaseResponse getContractCode(@PathVariable("networkId") Integer networkId,
        @PathVariable("address") String address,
        @PathVariable("blockNumber") Integer blockNumber) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getContractCode startTime:{} networkId:{} address:{} blockNumber:{}",
            startTime.toEpochMilli(), networkId, address,
            blockNumber);
        BaseResponse baseResponse = web3Service.getContractCode(networkId, address, blockNumber);
        log.info("end getContractCode useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get transaction receipt.
     */
    @GetMapping("/transactionReceipt/{networkId}/{transHash}")
    public BaseResponse getTransReceipt(@PathVariable("networkId") Integer networkId,
        @PathVariable("transHash") String transHash)
        throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getTransReceipt startTime:{} networkId:{} transhash:{}",
            startTime.toEpochMilli(), networkId, transHash);
        BaseResponse baseResponse = web3Service.getTransReceipt(networkId, transHash);
        log.info("end getTransReceipt useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get transaction by hash.
     */
    @GetMapping("/transaction/{networkId}/{transHash}")
    public BaseResponse getTransaction(@PathVariable("networkId") Integer networkId,
        @PathVariable("transHash") String transHash)
        throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getTransaction startTime:{} networkId:{} transhash:{}",
            startTime.toEpochMilli(), networkId, transHash);
        BaseResponse baseResponse = web3Service.getTransaction(networkId, transHash);
        log.info("end getTransaction useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get block by number.
     */
    @GetMapping("/blockByNumber/{networkId}/{blockNumber}")
    public BaseResponse getBlockByNumber(@PathVariable("networkId") Integer networkId,
        @PathVariable("blockNumber") Integer blockNumber)
        throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getBlockByNumber startTime:{} networkId:{} blockNumber:{}",
            startTime.toEpochMilli(), networkId, blockNumber);
        BaseResponse baseResponse = web3Service.getBlockByNumber(networkId, blockNumber);
        log.info("end getBlockByNumber useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }

}
