/**
 * Copyright 2014-2019 the original author or authors.
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

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.alert.log.entity.AlertLog;
import com.webank.webase.node.mgr.alert.log.entity.ReqAlertLogParam;
import com.webank.webase.node.mgr.alert.mail.server.config.entity.ReqMailServerConfigParam;
import com.webank.webase.node.mgr.alert.mail.server.config.entity.TbMailServerConfig;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.EnableStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("log")
public class AlertLogController {
    @Autowired
    AlertLogService alertLogService;

    @GetMapping("/list")
    public Object listAlertLog() {
        Instant startTime = Instant.now();
        log.info("start listAlertLog. startTime:{}",
                startTime.toEpochMilli());
        try{
            List<AlertLog> resList = alertLogService.getAllAlertLog();

            log.info("end listAlertLog. useTime:{}, resList:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), resList);
            return new BaseResponse(ConstantCode.SUCCESS, resList);
        }catch (NodeMgrException e) {
            log.debug("listAlertLog, error, exception:[] ", e);
            return new BaseResponse(ConstantCode.ALERT_LOG_ERROR, e.getMessage());
        }
    }

    /**
     * update alert log @status between 0-not handled, 1-already handled
     * @param param
     * @return
     */
    @PutMapping("")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object updateAlertLog(@RequestBody ReqAlertLogParam param) {
        Instant startTime = Instant.now();
        log.info("start updateAlertLog. startTime:{} ReqAlertLogParam:{}",
                startTime.toEpochMilli(), JSON.toJSONString(param));
        if(param.getStatus() == null || param.getLogId() == null) {
            log.debug("updateAlertLog, error:{} ",
                    ConstantCode.ALERT_LOG_PARAM_EMPTY);
            return new BaseResponse(ConstantCode.ALERT_LOG_PARAM_EMPTY);
        }
        try{
            alertLogService.updateAlertLog(param);
        }catch (NodeMgrException e) {
            log.debug("updateAlertLog, error, exception:[] ", e);
            return new BaseResponse(ConstantCode.ALERT_LOG_ERROR, e.getMessage());
        }
        AlertLog res = alertLogService.queryByLogId(param.getLogId());
        log.info("end updateAlertLog. useTime:{}, res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), res);
        return new BaseResponse(ConstantCode.SUCCESS, res);
    }

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

    @PostMapping("")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object saveAlertLog(@RequestBody ReqAlertLogParam param) {
        Instant startTime = Instant.now();
        log.info("start saveAlertLog. startTime:{} ReqAlertLogParam:{}",
                startTime.toEpochMilli(), JSON.toJSONString(param));
        if(StringUtils.isEmpty(param.getAlertContent()) ||
                StringUtils.isEmpty(param.getAlertLevel()) ||
                StringUtils.isEmpty(param.getAlertType())) {
            log.debug("saveAlertLog, error:{} ",
                    ConstantCode.ALERT_LOG_PARAM_EMPTY);
            return new BaseResponse(ConstantCode.ALERT_LOG_PARAM_EMPTY);
        }
        try{
            alertLogService.saveAlertLog(param);
        }catch (NodeMgrException e) {
            log.debug("saveAlertLog, error, exception:[] ", e);
            return new BaseResponse(ConstantCode.ALERT_LOG_ERROR, e.getMessage());
        }
        log.info("end saveAlertLog. useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return new BaseResponse(ConstantCode.SUCCESS);
    }

}
