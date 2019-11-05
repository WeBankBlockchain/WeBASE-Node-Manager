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

package com.webank.webase.node.mgr.alert.task;

import com.webank.webase.node.mgr.alert.mail.MailService;
import com.webank.webase.node.mgr.base.enums.AlertRuleType;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroup;
import com.webank.webase.node.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.node.mgr.monitor.MonitorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
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
    private FrontGroupMapCache frontGroupMapCache;
    @Autowired
    private MailService alertMailService;

    /**
     * set scheduler's interval
     */
    @Scheduled(fixedDelayString = "${constant.auditAlertMailInterval}")
    public void auditAlertTaskStart() {
        checkUserAndContractForAlert();
    }

    /**
     * scheduled task for user audit in alert mail
     */
    public synchronized void checkUserAndContractForAlert() {
        Instant startTime = Instant.now();
        log.info("start checkUserAndContractForAlert startTime:{}", startTime.toEpochMilli());
        List<FrontGroup> groupList = frontGroupMapCache.getAllMap();
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
        log.debug("start checkUserAndContractByGroup");
        int unusualUserCount = monitorService.countOfUnusualUser(groupId, null);
        int unusualContractCount = monitorService.countOfUnusualContract(groupId, null);
        int unusualMaxCount = cProperties.getMonitorUnusualMaxCount();
        // 异常用户数、异常合约数超出
        if (unusualUserCount >= unusualMaxCount
                && unusualContractCount >= unusualMaxCount) {
            log.warn("audit alert. unusualUserCount:{},unusualMaxCount:{}, monitorUnusualMaxCount:{}",
                    unusualUserCount, unusualMaxCount, unusualMaxCount);
            alertMailService.sendMailByRule(AlertRuleType.AUDIT_ALERT.getValue(),
                    "群组group " + groupId + "的异常用户数/异常合约数超出最大值："
                            + unusualContractCount + "/" + unusualContractCount);
        }else if (unusualUserCount >= unusualMaxCount) {
            log.warn("audit alert. unusualUserCount:{} monitorUnusualMaxCount:{}",
                    unusualUserCount, unusualMaxCount);
            alertMailService.sendMailByRule(AlertRuleType.AUDIT_ALERT.getValue(),
                    "群组group " + groupId + "的异常用户数超出最大值：" + unusualContractCount);
        }else if (unusualContractCount >= unusualMaxCount) {
            log.warn("audit alert. unusualContractCount:{} monitorUnusualMaxCount:{}",
                    unusualContractCount, unusualMaxCount);
            alertMailService.sendMailByRule(AlertRuleType.AUDIT_ALERT.getValue(),
                    "群组group " + groupId + "的异常合约数超出最大值：" + unusualContractCount);
        }
        log.debug("end checkUserAndContractByGroup");

    }

}
