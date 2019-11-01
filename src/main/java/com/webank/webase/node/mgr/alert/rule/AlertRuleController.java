/**
 * Copyright 2014-2019 the original author or authors.
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


package com.webank.webase.node.mgr.alert.rule;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.alert.rule.entity.AlertRuleParam;
import com.webank.webase.node.mgr.alert.rule.entity.TbAlertRule;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("alert")
public class AlertRuleController {


    @Autowired
    AlertRuleService alertRuleService;

    @GetMapping("/{ruleId}")
    public Object getAlertRuleByRuleId(@PathVariable("ruleId") Integer ruleId) {
        Instant startTime = Instant.now();
        log.info("start getAlertRuleByRuleId startTime:{} ruleId:{}",
                startTime.toEpochMilli(), ruleId);

        TbAlertRule res = alertRuleService.queryByRuleId(ruleId);

        log.info("end getAlertRuleByRuleId useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(res));
        return new BaseResponse(ConstantCode.SUCCESS, res);
    }

    @GetMapping("/list")
    public Object listAlertRules() {
        Instant startTime = Instant.now();
        log.info("start listAlertRules startTime:{}", startTime.toEpochMilli());
        try{
            List<TbAlertRule> resList = alertRuleService.getAllAlertRules();
            log.info("end listAlertRules useTime:{} resList:{}",
                    Duration.between(startTime, Instant.now()).toMillis(), resList);
            return new BaseResponse(ConstantCode.SUCCESS, resList);
        }catch (NodeMgrException e) {
            log.debug("listAlertRules, error, exception:[] ", e);
            return new BaseResponse(ConstantCode.ALERT_RULE_ERROR, e.getMessage());
        }
    }

//    @PostMapping("")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
//    public Object saveAlertRule(@RequestBody @Valid AlertRuleParam param) {
//        Instant startTime = Instant.now();
//        log.info("start saveAlertRule. startTime:{} AlertRuleParam:{}",
//                startTime.toEpochMilli(), JSON.toJSONString(param));
//        // check param
//        // 保证参数，用catch方式
//        if(param.getUserList().isEmpty()) {
//            return new BaseResponse(ConstantCode.ALERT_RULE_ERROR);
//        }
//        alertRuleService.saveAlertRule(param);
//        log.info("end saveAlertRule. useTime:{}",
//                Duration.between(startTime, Instant.now()).toMillis());
//        return new BaseResponse(ConstantCode.SUCCESS);
//    }

    @PutMapping("")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object updateAlertRule(@RequestBody AlertRuleParam param) {
        Instant startTime = Instant.now();
        log.info("start updateAlertRule. startTime:{} AlertRuleParam:{}",
                startTime.toEpochMilli(), JSON.toJSONString(param));
        if(param.getRuleId() == null) {
            return new BaseResponse(ConstantCode.ALERT_RULE_PARAM_EMPTY);
        }
        try{
            alertRuleService.updateAlertRule(param);
        }catch (NodeMgrException e) {
            log.debug("updateAlertRule, error, exception:[] ", e);
            return new BaseResponse(ConstantCode.ALERT_RULE_ERROR, e.getMessage());
        }
        TbAlertRule res = alertRuleService.queryByRuleId(param.getRuleId());
        log.info("end updateAlertRule. useTime:{}, res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), res);
        return new BaseResponse(ConstantCode.SUCCESS, res);

    }


    @PutMapping("/toggle")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object toggleAlertRule(@RequestBody AlertRuleParam param) {
        Instant startTime = Instant.now();
        log.info("start toggleAlertRule. startTime:{} AlertRuleParam:{}",
                startTime.toEpochMilli(), JSON.toJSONString(param));

        if(param.getRuleId() == null || param.getEnable() == null) {
            return new BaseResponse(ConstantCode.ALERT_RULE_PARAM_EMPTY);
        }
        try{
            alertRuleService.updateAlertRule(param);
        }catch (NodeMgrException e) {
            log.debug("toggleAlertRule, error, exception:[] ", e);
            return new BaseResponse(ConstantCode.ALERT_RULE_ERROR, e.getMessage());
        }
        TbAlertRule res = alertRuleService.queryByRuleId(param.getRuleId());
        log.info("end toggleAlertRule. useTime:{}, res:{}",
                Duration.between(startTime, Instant.now()).toMillis(), res);
        return new BaseResponse(ConstantCode.SUCCESS, res);

    }

    /**
     * @Duplicated no need to delete alert_rule for default rules only need updating
     */
//    @DeleteMapping("/{ruleId}")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
//    public Object deleteAlertRuleByRuleId(@PathVariable("ruleId") Integer ruleId) {
//        Instant startTime = Instant.now();
//        log.info("start deleteAlertRuleByRuleId. startTime:{} ruleId:{}",
//                startTime.toEpochMilli(), ruleId);
//        try {
//            alertRuleService.deleteByRuleId(ruleId);
//            log.info("end toggleAlertRule. useTime:{}",
//                    Duration.between(startTime, Instant.now()).toMillis());
//            return new BaseResponse(ConstantCode.SUCCESS);
//        }catch (NodeMgrException e) {
//            log.debug("deleteAlertRuleByRuleId, error, exception:[] ", e);
//            return new BaseResponse(ConstantCode.ALERT_RULE_ERROR, e.getMessage());
//        }
//    }
}
