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

import com.webank.webase.node.mgr.alert.log.entity.AlertLog;
import com.webank.webase.node.mgr.alert.log.entity.ReqLogListParam;
import com.webank.webase.node.mgr.alert.log.entity.ReqLogParam;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.EnableStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Alert log related with AlertRule
 * when alerts, log creates while sending alert mail
 */
@Log4j2
@Service
public class AlertLogService {
    @Autowired
    AlertLogMapper alertLogMapper;

    public void saveAlertLog(ReqLogParam inputParam) {
        log.debug("start saveAlertLog alertLog:{}", inputParam);
        if(inputParam.getStatus() == null) {
            inputParam.setStatus(EnableStatus.OFF.getValue());
        }
        AlertLog alertLog = new AlertLog();
        try{
            BeanUtils.copyProperties(inputParam, alertLog);
            alertLogMapper.add(alertLog);
            log.debug("end saveAlertLog. ");
        }catch (Exception e) {
            log.error("saveAlertLog error exception:[]", e);
            throw new NodeMgrException(ConstantCode.ALERT_LOG_ERROR.getCode(),
                    e.getMessage());
        }
    }

    public void saveAlertLogByRuleAndContent(int alertLevel, int alertType, String alertContentInEmail) {
        log.debug("start saveAlertLogByRuleAndContent alertLevel:{},alertType:{} alertContentInEmail:{}",
                alertLevel, alertType, alertContentInEmail);
        AlertLog alertLog = new AlertLog();
        alertLog.setStatus(EnableStatus.OFF.getValue());
        alertLog.setAlertLevel(alertLevel);
        alertLog.setAlertType(alertType);
        alertLog.setAlertContent(alertContentInEmail);
        try{
            alertLogMapper.add(alertLog);
            log.debug("end saveAlertLogByRuleAndContent. ");
        }catch (Exception e) {
            log.error("saveAlertLogByRuleAndContent error exception:[]", e);
            throw new NodeMgrException(ConstantCode.ALERT_LOG_ERROR.getCode(),
                    e.getMessage());
        }
    }

    public AlertLog queryByLogId(int logId) {
        log.debug("start queryByLogId logId:{}", logId);
        AlertLog resAlertLog = alertLogMapper.queryByLogId(logId);
        log.debug("end resAlertLog:{} ", resAlertLog);
        return resAlertLog;
    }

    /**
     * get all logs in
     * @return
     */
    public List<AlertLog> getAllAlertLog(ReqLogListParam pageParam) {
        log.debug("start getAllAlertLog ");
        List<AlertLog> resList = new ArrayList<>();
        try {
            resList = alertLogMapper.listOfAlertLog(pageParam);
            log.debug("end getAllAlertLog resList:{}", resList);
            return resList;
        }catch (Exception e) {
            log.error("getAllAlertLog error exception:[]", e);
            throw new NodeMgrException(ConstantCode.ALERT_LOG_ERROR.getCode(),
                    e.getMessage());
        }
    }

    public int countOfLog() {
        log.debug("start countOfLog ");
        try {
            int count = alertLogMapper.countOfLog();
            log.debug("end countOfLog count:{}", count);
            return count;
        }catch (Exception e) {
            log.error("countOfLog error exception:[]", e);
            throw new NodeMgrException(ConstantCode.ALERT_LOG_ERROR.getCode(),
                    e.getMessage());
        }
    }


    public void updateAlertLog(ReqLogParam inputParam) {
        log.debug("start updateAlertLog ReqAlertLogParam inputParam:{}", inputParam);
        AlertLog alertLog = new AlertLog();
        try{
            // only change status
            alertLog.setStatus(inputParam.getStatus());
            alertLog.setLogId(inputParam.getLogId());
            alertLogMapper.update(alertLog);
            log.debug("end updateAlertLog. ");
        }catch (Exception e) {
            log.error("updateAlertLog error exception:[]", e);
            throw new NodeMgrException(ConstantCode.ALERT_LOG_ERROR.getCode(),
                    e.getMessage());
        }
    }

    public void deleteByLogId(int logId) {
        log.debug("start deleteByLogId logId:{}", logId);
        alertLogMapper.deleteByLogId(logId);
        log.debug("end deleteByLogId. ");

    }

}
