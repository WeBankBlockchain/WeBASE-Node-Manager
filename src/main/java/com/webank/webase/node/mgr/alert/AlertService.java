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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AlertService {

    @Autowired
    AlertRuleMapper alertRuleMapper;

    public void saveAlertRule(AlertRuleParam newRuleParam) {
        TbAlertRule tbAlertRule = new TbAlertRule();
        BeanUtils.copyProperties(newRuleParam, tbAlertRule);
        alertRuleMapper.add(tbAlertRule);
    }

    public List<TbAlertRule> getAllAlertRules() {
        List<TbAlertRule> resList = alertRuleMapper.listOfAlertRules();
        return resList;
    }

    public TbAlertRule queryByRuleId(int ruleId) {
        TbAlertRule resAlertRule = alertRuleMapper.queryByRuleId(ruleId);
        return resAlertRule;
    }

    public void updateAlertRule(AlertRuleParam updateRuleParam) {
        TbAlertRule tbAlertRule = new TbAlertRule();
        BeanUtils.copyProperties(updateRuleParam, tbAlertRule);
        alertRuleMapper.update(tbAlertRule);
    }

    public void deleteByRuleId(int ruleId) {
        alertRuleMapper.deleteByRuleId(ruleId);
    }

}
