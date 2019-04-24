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
package com.webank.webase.node.mgr.network;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.scheduler.StatisticsTransdailyTask;
import com.webank.webase.node.mgr.transdaily.SeventDaysTrans;
import com.webank.webase.node.mgr.transdaily.TransDailyService;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for processing network information.
 */
@Log4j2
@RestController
@RequestMapping("network")
public class NetworkController {

    @Autowired
    private NetworkService networkService;
    @Autowired
    private TransDailyService transDailyService;
    @Autowired
    private StatisticsTransdailyTask statisticsTask;

    /**
     * get network general.
     */
    @GetMapping("/general/{networkId}")
    public BaseResponse getNetworkGeneral(@PathVariable("networkId") Integer networkId)
        throws NodeMgrException {
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start getNetworkGeneral startTime:{} networkId:{}", startTime.toEpochMilli(),
            networkId);
        NetworkGeneral networkGeneral = null;

        int statisticTimes = 0;// if transCount less than blockNumber,statistics again
        while (true) {
            networkGeneral = networkService.queryNetworkGeneral(networkId);
            BigInteger transactionCount = networkGeneral.getTransactionCount();
            BigInteger latestBlock = networkGeneral.getLatestBlock();
            if (transactionCount.compareTo(latestBlock) < 0 && statisticTimes == 0) {
                statisticTimes += 1;
                statisticsTask.updateTransdailyData();
                continue;
            } else {
                break;
            }
        }

        baseResponse.setData(networkGeneral);
        log.info("end getNetworkGeneral useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * query all network.
     */
    @GetMapping("/all")
    public BasePageResponse getAllNetwork() throws NodeMgrException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start getAllNetwork startTime:{}", startTime.toEpochMilli());

        // get all network list
        List<TbNetwork> networkList = networkService.getAllNetwork();
        Integer totalCount = Optional.ofNullable(networkList).map(list -> list.size()).orElse(0);
        pagesponse.setTotalCount(totalCount);
        pagesponse.setData(networkList);

        log.info("end getAllNetwork useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(pagesponse));
        return pagesponse;
    }

    /**
     * get trans daily.
     */
    @GetMapping("/transDaily/{networkId}")
    public BaseResponse getTransDaily(@PathVariable("networkId") Integer networkId)
        throws Exception {
        BaseResponse pagesponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start getTransDaily startTime:{} networkId:{}", startTime.toEpochMilli(),
            networkId);

        // query trans daily
        List<SeventDaysTrans> listTrans = transDailyService.listSeventDayOfTrans(networkId);
        pagesponse.setData(listTrans);

        log.info("end getAllNetwork useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(pagesponse));
        return pagesponse;
    }
}
