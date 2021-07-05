/**
 * Copyright 2014-2021 the original author or authors.
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

package com.webank.webase.node.mgr.pro.alert.rule;

import com.webank.webase.node.mgr.pro.alert.rule.entity.ReqAlertRuleParam;
import com.webank.webase.node.mgr.pro.alert.rule.entity.TbAlertRule;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.webank.webase.node.mgr.lite.base.code.ConstantCode;
import com.webank.webase.node.mgr.lite.base.exception.NodeMgrException;

import lombok.extern.log4j.Log4j2;

/**
 * Alert Type Configuration Service
 */
@Log4j2
@Service
public class AlertRuleService {

    @Autowired
    AlertRuleMapper alertRuleMapper;

    public void saveAlertRule(ReqAlertRuleParam newRuleParam) {
        log.debug("start saveAlertRule AlertRuleParam newRuleParam:{}", newRuleParam);
        checkAndInitAlertRule(newRuleParam);
        TbAlertRule tbAlertRule = new TbAlertRule();
        try{
            BeanUtils.copyProperties(newRuleParam, tbAlertRule);
            alertRuleMapper.add(tbAlertRule);
            log.debug("end saveAlertRule. ");
        }catch (Exception e) {
            log.error("saveAlertRule error: exception:[]", e);
            throw new NodeMgrException(ConstantCode.ALERT_RULE_ERROR.getCode(),
                    e.getMessage());
        }
    }

    public List<TbAlertRule> getAllAlertRules() {
        log.debug("start getAllAlertRules ");
        try {
            List<TbAlertRule> resList = alertRuleMapper.listOfAlertRules();
            log.debug("end getAllAlertRules resList:{}", resList);
            return resList;
        }catch (Exception e) {
            log.error("getAllAlertRules error: exception:{}", e);
            throw new NodeMgrException(ConstantCode.ALERT_RULE_ERROR.getCode(),
                    e.getMessage());
        }
    }

    public TbAlertRule queryByRuleId(int ruleId) {
        log.debug("start queryByRuleId ruleId:{}", ruleId);
        TbAlertRule resAlertRule = alertRuleMapper.queryByRuleId(ruleId);
        log.debug("end resAlertRule:{}", resAlertRule);
        return resAlertRule;
    }

    public void updateAlertRule(ReqAlertRuleParam updateRuleParam) {
        log.debug("start updateAlertRule updateRuleParam:{}", updateRuleParam);
        TbAlertRule tbAlertRule = new TbAlertRule();
        try{
            BeanUtils.copyProperties(updateRuleParam, tbAlertRule);
            log.debug("updateAlertRule tbAlertRule:{}", tbAlertRule);
            alertRuleMapper.update(tbAlertRule);
            log.debug("end updateAlertRule. ");
        }catch (Exception e) {
            log.error("updateAlertRule error: exception:[]", e);
            throw new NodeMgrException(ConstantCode.ALERT_RULE_ERROR.getCode(),
                    e.getMessage());
        }
    }

    public void deleteByRuleId(int ruleId) {
        log.debug("start deleteByRuleId ruleId:{}", ruleId);
        try{
            alertRuleMapper.deleteByRuleId(ruleId);
            log.debug("end deleteByRuleId. ");
        }catch (Exception e) {
            log.error("deleteByRuleId error: exception:[]", e);
            throw new NodeMgrException(ConstantCode.ALERT_RULE_ERROR.getCode(),
                    e.getMessage());
        }
    }

    private void checkAndInitAlertRule(ReqAlertRuleParam inputParam) {
        if(inputParam.getEnable() == null) {
            inputParam.setEnable(0);
        }
        if(inputParam.getIsAllUser() == null) {
            inputParam.setIsAllUser(0);
        }
    }

    /**
     * check alert rule's user list whether empty
     * @param ruleId
     * @return
     */
    public boolean checkUserListIsEmptyByRuleId(int ruleId) {
        TbAlertRule tbAlertRule = alertRuleMapper.queryByRuleId(ruleId);
        return StringUtils.isEmpty(tbAlertRule.getUserList());
    }
}
