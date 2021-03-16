/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.node.mgr.statistic;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.statistic.mapper.TbStatMapper;
import com.webank.webase.node.mgr.statistic.result.PerformanceData;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("stat")
public class StatController {

    @Autowired
    private StatService statService;
    @Autowired
    private TbStatMapper tbStatMapper;
    
    @GetMapping
    public BaseResponse getBlockStat(
        @RequestParam(required = false) Long beginDate,
        @RequestParam(required = false) Long endDate,
        @RequestParam(required = false) Long contrastBeginDate,
        @RequestParam(required = false) Long contrastEndDate,
        @RequestParam(required = false, defaultValue = "1") int gap,
        @RequestParam(defaultValue = "1") int groupId) {
        Instant startTime = Instant.now();
        log.info("getBlockStat start. groupId:[{}], startTime:{}", groupId,
            startTime.toEpochMilli());

        List<PerformanceData> performanceList = statService.findContrastDataByTime(groupId,
            beginDate, endDate, contrastBeginDate, contrastEndDate, gap);
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS, performanceList);

        log.info("getBlockStat end. useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
        return response;
    }

}
