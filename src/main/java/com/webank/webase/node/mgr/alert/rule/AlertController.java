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
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.web3j.abi.datatypes.Int;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("alert")
public class AlertController {


    @Autowired
    AlertService alertService;

    @GetMapping("/{ruleId}")
    public Object getAlertRuleByRuleId(@PathVariable("ruleId") Integer ruleId) {

        TbAlertRule res = alertService.queryByRuleId(ruleId);
        return new BaseResponse(ConstantCode.SUCCESS, res);
    }

    @GetMapping("/list")
    public Object listAlertRules() {
        try{
            List<TbAlertRule> resList = alertService.getAllAlertRules();
            return new BaseResponse(ConstantCode.SUCCESS, resList);
        }catch (NodeMgrException e) {
            return new BaseResponse(ConstantCode.ALERT_RULE_ERROR, e.getMessage());
        }
    }

    @PostMapping("")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object saveAlertRule(@RequestBody @Valid AlertRuleParam param) {
        Instant startTime = Instant.now();
        log.debug("start saveAlertRule. startTime:{} AlertRuleParam:{}",
                startTime.toEpochMilli(), JSON.toJSONString(param));
        // check param
        // 保证参数，用catch方式
        if(param.getUserList().isEmpty()) {
            return new BaseResponse(ConstantCode.ALERT_RULE_ERROR);
        }
        alertService.saveAlertRule(param);
        log.debug("end saveAlertRule. useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    @PutMapping("")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object updateAlertRule(@RequestBody AlertRuleParam param) {
        if(param.getRuleId() == null) {
            return new BaseResponse(ConstantCode.ALERT_RULE_PARAM_EMPTY);
        }
        try{
            alertService.updateAlertRule(param);
        }catch (NodeMgrException e) {
            return new BaseResponse(ConstantCode.ALERT_RULE_ERROR, e.getMessage());
        }
        TbAlertRule res = alertService.queryByRuleId(param.getRuleId());
        return new BaseResponse(ConstantCode.SUCCESS, res);

    }


    @PutMapping("/toggle")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object toggleAlertRule(@RequestBody AlertRuleParam param) {
        if(param.getRuleId() == null || param.getEnable() == null) {
            return new BaseResponse(ConstantCode.ALERT_RULE_PARAM_EMPTY);
        }
        try{
            alertService.updateAlertRule(param);
        }catch (NodeMgrException e) {
            return new BaseResponse(ConstantCode.ALERT_RULE_ERROR, e.getMessage());
        }
        TbAlertRule res = alertService.queryByRuleId(param.getRuleId());
        return new BaseResponse(ConstantCode.SUCCESS, res);

    }

    @DeleteMapping("/{ruleId}")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object deleteAlertRuleByRuleId(@PathVariable("ruleId") Integer ruleId) {
        try {
            alertService.deleteByRuleId(ruleId);
            return new BaseResponse(ConstantCode.SUCCESS);
        }catch (NodeMgrException e) {
            return new BaseResponse(ConstantCode.ALERT_RULE_ERROR, e.getMessage());
        }
    }
}
