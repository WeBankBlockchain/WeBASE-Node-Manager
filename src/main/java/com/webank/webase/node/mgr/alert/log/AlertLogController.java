/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.node.mgr.alert.log;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.qctc.common.log.annotation.Log;
import com.qctc.common.log.enums.BusinessType;
import com.webank.webase.node.mgr.tools.JsonTools;
import com.webank.webase.node.mgr.alert.log.entity.AlertLog;
import com.webank.webase.node.mgr.alert.log.entity.ReqLogListParam;
import com.webank.webase.node.mgr.alert.log.entity.ReqLogParam;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.SqlSortType;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * AlertLog Controller for get/update logs
 */
@Tag(name= "告警日志管理")
@Log4j2
@RestController
@RequestMapping("log")
public class AlertLogController {
    @Autowired
    private AlertLogService alertLogService;

    @SaCheckPermission("bcos3:monitor:emailAlarmType")
    @GetMapping("/list/{pageNumber}/{pageSize}")
    public Object listAlertLog(@PathVariable("pageNumber") Integer pageNumber,
                               @PathVariable("pageSize") Integer pageSize) {
        Instant startTime = Instant.now();
        log.info("start listAlertLog. startTime:{},pageNumber:{},pageSize:{}",
                startTime.toEpochMilli(), pageNumber, pageSize);
        try{
            int count = alertLogService.countOfLog();
            Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
                    .orElse(0);
            ReqLogListParam param = new ReqLogListParam(start, pageSize,
                    SqlSortType.DESC.getValue());
            List<AlertLog> resList = alertLogService.getAllAlertLog(param);
            log.info("end listAlertLog. useTime:{}, resList:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), resList);
            return new BasePageResponse(ConstantCode.SUCCESS, resList, count);
        }catch (NodeMgrException e) {
            log.error("listAlertLog, error, exception:[] ", e);
            return new BaseResponse(ConstantCode.ALERT_LOG_ERROR, e.getMessage());
        }
    }

    /**
     * update alert log @status between 0-not handled, 1-already handled
     * @param param
     * @return
     */
    @Log(title = "BCOS3/系统监控/告警管理", businessType = BusinessType.UPDATE)
    @SaCheckPermission("bcos3:monitor:updateAlertLog")
    @PutMapping("")
    public Object updateAlertLog(@RequestBody ReqLogParam param) {
        Instant startTime = Instant.now();
        log.info("start updateAlertLog. startTime:{} ReqAlertLogParam:{}",
                startTime.toEpochMilli(), JsonTools.toJSONString(param));
        if(param.getStatus() == null || param.getLogId() == null) {
            return new BaseResponse(ConstantCode.ALERT_LOG_PARAM_EMPTY);
        }
        try{
            alertLogService.updateAlertLog(param);
        }catch (NodeMgrException e) {
            log.error("updateAlertLog, error, exception:[] ", e);
            return new BaseResponse(ConstantCode.ALERT_LOG_ERROR, e.getMessage());
        }
        AlertLog res = alertLogService.queryByLogId(param.getLogId());
        log.info("end updateAlertLog. useTime:{}, res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), res);
        return new BaseResponse(ConstantCode.SUCCESS, res);
    }

    @SaCheckPermission("bcos3:monitor:getAlertLog")
    @GetMapping("/{logId}")
    public Object getAlertLogById(@PathVariable("logId") Integer logId) {
        Instant startTime = Instant.now();
        log.info("start getAlertLogById. startTime:{} logId:{}",
                startTime.toEpochMilli(), logId);
        AlertLog res = alertLogService.queryByLogId(logId);
        log.info("end getAlertLogById. useTime:{}, res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), res);
        return new BaseResponse(ConstantCode.SUCCESS, res);
    }

}
