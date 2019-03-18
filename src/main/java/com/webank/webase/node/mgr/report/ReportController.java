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
package com.webank.webase.node.mgr.report;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Receive report blockchain information Controller.
 */
@Log4j2
@RestController
@RequestMapping("report")
class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * receive all report info.
     */
    @PostMapping(value = "/blockChainInfo")
    public BaseResponse reportBlockChainInfo(@RequestBody BaseReportInfo reportInfo)
        throws NodeMgrException {
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.debug("start reportBlockChainInfo startTime:{} reportInfo:{}", startTime.toEpochMilli(),
            JSON.toJSONString(reportInfo));

        reportService.handlerBlockInfo(reportInfo);

        log.debug("end reportBlockChainInfo useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * query blockChainInfo.
     */
    @GetMapping("/blockChainInfo/{attr}/{nodeIp}/{nodeP2PPort}")
    public BaseResponse getBlockChainInfo(@PathVariable("attr") String attr,
        @PathVariable("nodeIp") String nodeIp,
        @PathVariable("nodeP2PPort") Integer nodeP2PPort) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.debug("start getLatestReportNumber startTime:{} attr:{} nodeIp:{} nodeP2PPort:{}",
            startTime.toEpochMilli(), attr, nodeIp, nodeP2PPort);

        BaseResponse baseResponse = reportService.getBlockChainInfo(attr, nodeIp, nodeP2PPort);

        log.debug("end getLatestReportNumber useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JSON.toJSONString(baseResponse));
        return baseResponse;
    }

}
