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

package com.webank.webase.node.mgr.lite.scheduler.alert;

import com.webank.webase.node.mgr.pro.alert.mail.MailService;
import com.webank.webase.node.mgr.pro.alert.rule.AlertRuleService;
import com.webank.webase.node.mgr.pro.alert.rule.entity.TbAlertRule;
import com.webank.webase.node.mgr.lite.base.enums.AlertRuleType;
import com.webank.webase.node.mgr.lite.base.enums.DataStatus;
import com.webank.webase.node.mgr.lite.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.lite.base.tools.AlertRuleTools;
import com.webank.webase.node.mgr.lite.group.GroupService;
import com.webank.webase.node.mgr.lite.group.entity.TbGroup;
import com.webank.webase.node.mgr.pro.monitor.MonitorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * user/contract audit triggers alert mail
 */
@Log4j2
@Component
public class AuditMonitorTask {

    @Autowired
    private MonitorService monitorService;
    @Autowired
    private ConstantProperties cProperties;
    @Autowired
    private GroupService groupService;
    @Autowired
    private MailService alertMailService;
    @Autowired
    private AlertRuleService alertRuleService;

    /**
     * set scheduler's interval
     */
    //@Scheduled(fixedDelayString = "${constant.auditMonitorTaskFixedDelay}")
    public void auditAlertTaskStart() {
        checkUserAndContractForAlert();
    }

    /**
     * scheduled task for user audit in alert mail
     */
    public synchronized void checkUserAndContractForAlert() {
        Instant startTime = Instant.now();
        log.info("start checkUserAndContractForAlert startTime:{}", startTime.toEpochMilli());
        //check last alert time, if within interval, not send
        TbAlertRule alertRule = alertRuleService.queryByRuleId(AlertRuleType.AUDIT_ALERT.getValue());
        if(AlertRuleTools.isWithinAlertIntervalByNow(alertRule)) {
            log.debug("end checkUserAndContractForAlert non-sending mail" +
                    " for beyond alert interval:{}", alertRule);
            return;
        }
        List<TbGroup> groupList = groupService.getGroupList(DataStatus.NORMAL.getValue());
        if (groupList == null || groupList.size() == 0) {
            log.warn("checkUserAndContractForAlert jump over: not found any group");
            return;
        }
        groupList.stream()
                .forEach(group -> checkUserAndContractByGroup(group.getGroupId()));

        log.info("end checkUserAndContractForAlert useTime:{} ",
                Duration.between(startTime, Instant.now()).toMillis());
    }

    private void checkUserAndContractByGroup(int groupId) {
        log.debug("start checkUserAndContractByGroup groupId:{}", groupId);
        int unusualUserCount = monitorService.countOfUnusualUser(groupId, null);
        int unusualContractCount = monitorService.countOfUnusualContract(groupId, null);
        int unusualMaxCount = cProperties.getMonitorUnusualMaxCount();
        // 异常用户数、异常合约数超出
        List<String> alertContentList = new ArrayList<>();
        if (unusualUserCount >= unusualMaxCount
                && unusualContractCount >= unusualMaxCount) {
            log.warn("audit alert. unusualUserCount:{},unusualMaxCount:{}, monitorUnusualMaxCount:{}",
                    unusualUserCount, unusualMaxCount, unusualMaxCount);
            String alertContent = "群组group " + groupId + "的异常用户数/异常合约数超出最大值："
                    + unusualContractCount + "/" + unusualContractCount;
            String alertContentEn = "group " + groupId + "'s number of abnormal users/contracts exceeds: "
                    + unusualContractCount + "/" + unusualContractCount;
            alertContentList.add(alertContent);
            alertContentList.add(alertContentEn);
            alertMailService.sendMailByRule(AlertRuleType.AUDIT_ALERT.getValue(), alertContentList);
        }else if (unusualUserCount >= unusualMaxCount) {
            log.warn("audit alert. unusualUserCount:{} monitorUnusualMaxCount:{}",
                    unusualUserCount, unusualMaxCount);
            String alertContent = "群组group " + groupId + "的异常用户数超出最大值：" + unusualContractCount;
            String alertContentEn = "group " + groupId + "'s number of abnormal users exceeds: " + unusualContractCount;
            alertContentList.add(alertContent);
            alertContentList.add(alertContentEn);
            alertMailService.sendMailByRule(AlertRuleType.AUDIT_ALERT.getValue(), alertContentList);
        }else if (unusualContractCount >= unusualMaxCount) {
            log.warn("audit alert. unusualContractCount:{} monitorUnusualMaxCount:{}",
                    unusualContractCount, unusualMaxCount);
            String alertContent = "群组group " + groupId + "的异常合约数超出最大值：" + unusualContractCount;
            String alertContentEn = "group " + groupId + "'s number of abnormal contracts exceeds: " + unusualContractCount;
            alertContentList.add(alertContent);
            alertContentList.add(alertContentEn);
            alertMailService.sendMailByRule(AlertRuleType.AUDIT_ALERT.getValue(), alertContentList);
        }
        log.debug("end checkUserAndContractByGroup");

    }

}
