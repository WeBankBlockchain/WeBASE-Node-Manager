/**
 * Copyright 2014-2020  the original author or authors.
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
package com.webank.webase.node.mgr.performance;

import com.webank.webase.node.mgr.base.tools.JsonTools;
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

/**
 * get performance info.
 */
@Log4j2
@RestController
@RequestMapping(value = "performance")
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    /**
     * get ratio of performance.
     */
    @GetMapping(value = "/ratio/{frontId}")
    public BaseResponse getPerformanceRatio(@PathVariable("frontId") Integer frontId,
        @RequestParam("beginDate") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime beginDate,
        @RequestParam("endDate") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(value = "contrastBeginDate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime contrastBeginDate,
        @RequestParam(value = "contrastEndDate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime contrastEndDate,
        @RequestParam(value = "gap", required = false, defaultValue = "1") int gap)
        throws NodeMgrException {
        Instant startTime = Instant.now();
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        log.info(
            "start getPerformanceRatio. startTime:{} frontId:{} beginDate:{}"
                + " endDate:{} contrastBeginDate:{} contrastEndDate:{} gap:{}",
            startTime.toEpochMilli(), frontId, beginDate, endDate, contrastBeginDate,
            contrastEndDate, gap);

        Object rspObj = performanceService
            .getPerformanceRatio(frontId, beginDate, endDate, contrastBeginDate, contrastEndDate,
                gap);
        response.setData(rspObj);
        log.info("end getPerformanceRatio. useTime:{} response:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(response));

        return response;
    }

    /**
     * get config of performance.
     */
    @GetMapping(value = "/config/{frontId}")
    public BaseResponse getPerformanceConfig(@PathVariable("frontId") Integer frontId)
        throws NodeMgrException {
        Instant startTime = Instant.now();
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start getPerformanceConfig. startTime:{} frontId:{}", startTime.toEpochMilli(),
            frontId);
        Object frontRsp = performanceService.getPerformanceConfig(frontId);
        response.setData(frontRsp);
        log.info("end getPerformanceConfig. useTime:{} response:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(response));
        return response;
    }
}
