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

import com.webank.webase.node.mgr.alert.rule.entity.AlertRuleParam;
import com.webank.webase.node.mgr.alert.rule.entity.TbAlertRule;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AlertService {

    @Autowired
    AlertRuleMapper alertRuleMapper;

    public void saveAlertRule(AlertRuleParam newRuleParam) {
        TbAlertRule tbAlertRule = new TbAlertRule();
        try{
            BeanUtils.copyProperties(newRuleParam, tbAlertRule);
            alertRuleMapper.add(tbAlertRule);
        }catch (Exception e) {
            log.error("saveAlertRule error: exception:{}", e);
            throw new NodeMgrException(ConstantCode.ALERT_RULE_ERROR.getCode(),
                    e.getMessage());
        }
    }

    public List<TbAlertRule> getAllAlertRules() {
        try {
            List<TbAlertRule> resList = alertRuleMapper.listOfAlertRules();
            return resList;
        }catch (Exception e) {
            log.error("getAllAlertRules error: exception:{}", e);
            throw new NodeMgrException(ConstantCode.ALERT_RULE_ERROR.getCode(),
                    e.getMessage());
        }
    }

    public TbAlertRule queryByRuleId(int ruleId) {
        TbAlertRule resAlertRule = alertRuleMapper.queryByRuleId(ruleId);
        return resAlertRule;
    }

    public void updateAlertRule(AlertRuleParam updateRuleParam) {
        TbAlertRule tbAlertRule = new TbAlertRule();
       try{
           BeanUtils.copyProperties(updateRuleParam, tbAlertRule);
            alertRuleMapper.update(tbAlertRule);
        }catch (Exception e) {
            log.error("updateAlertRule error: exception:{}", e);
            throw new NodeMgrException(ConstantCode.ALERT_RULE_ERROR.getCode(),
                    e.getMessage());
        }
    }

    public void deleteByRuleId(int ruleId) {
        try{
            alertRuleMapper.deleteByRuleId(ruleId);
        }catch (Exception e) {
            log.error("deleteByRuleId error: exception:{}", e);
            throw new NodeMgrException(ConstantCode.ALERT_RULE_ERROR.getCode(),
                    e.getMessage());
        }
    }

}
