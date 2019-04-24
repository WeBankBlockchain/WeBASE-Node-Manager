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
package com.webank.webase.node.mgr.chain;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping(value = "chain")
public class ChainController {

    @Autowired
    private ChainService chainService;

    @GetMapping(value = "/mointorInfo/{nodeId}")
    public BaseResponse getChainMoinntorInfo(@PathVariable("nodeId") Integer nodeId,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime beginDate,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime contrastBeginDate,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime contrastEndDate,
        @RequestParam(required = false, defaultValue = "1") int gap)
        throws NodeMgrException {
        Instant startTime = Instant.now();
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        log.info(
            "start getChainInfo. startTime:{} nodeId:{} beginDate:{} endDate:{} "
                + "contrastBeginDate:{} contrastEndDate:{} gap:{}", startTime.toEpochMilli(),
            nodeId, beginDate, endDate, contrastBeginDate, contrastEndDate, gap);
        Object rspObj = chainService
            .getChainMonitorInfo(nodeId, beginDate, endDate, contrastBeginDate, contrastEndDate,
                gap);

        response.setData(rspObj);
        log.info("end getChainInfo. endTime:{} response:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(response));

        return response;
    }
}
