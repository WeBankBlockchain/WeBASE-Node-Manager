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


package com.webank.webase.node.mgr.alert;

import com.webank.webase.node.mgr.alert.entity.AlertRuleParam;
import com.webank.webase.node.mgr.alert.entity.TbAlertRule;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("alert")
public class AlertController {


    @Autowired
    AlertService alertService;

    @GetMapping("/{ruleId}")
    public Object getServerConfig(@PathVariable("ruleId") int ruleId) {

        TbAlertRule res = alertService.queryByRuleId(ruleId);
        return res;
    }

    @PostMapping("")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object saveAlertRule(@RequestBody AlertRuleParam param) {
        // check param
        // 保证参数，用catch方式
        if(param.getUserList().isEmpty()) {
            return new BaseResponse(ConstantCode.PARAM_EXCEPTION);
        }
        alertService.saveAlertRule(param);
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    @PutMapping("")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object updateAlertRule(@RequestBody AlertRuleParam param) {

        alertService.updateAlertRule(param);
        return new BaseResponse(ConstantCode.SUCCESS);
    }

    @DeleteMapping("/{ruleId}")
//    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object updateAlertRule(@PathVariable("ruleId") int ruleId) {

        alertService.deleteByRuleId(ruleId);
        return new BaseResponse(ConstantCode.SUCCESS);
    }
}
