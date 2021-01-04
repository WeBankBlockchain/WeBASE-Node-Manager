/**
 * Copyright 2014-2020 the original author or authors.
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

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webank.webase.node.mgr.alert.mail.MailService;
import com.webank.webase.node.mgr.alert.rule.AlertRuleService;
import com.webank.webase.node.mgr.alert.rule.entity.TbAlertRule;
import com.webank.webase.node.mgr.base.enums.AlertRuleType;
import com.webank.webase.node.mgr.base.tools.AlertRuleTools;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.cert.CertService;

import lombok.extern.log4j.Log4j2;


/**
 * cert validity time within 7 days triggers alert mail
 */
@Log4j2
@Component
public class CertMonitorTask {

    @Autowired
    private CertService certService;
    @Autowired
    private MailService alertMailService;
    @Autowired
    private AlertRuleService alertRuleService;

    /**
     * set scheduler's interval
     */
    @Scheduled(fixedDelayString = "${constant.certMonitorTaskFixedDelay}")
    public void certAlertTaskStart() {
        checkCertValidityForAlert();
    }

    /**
     * scheduled task for cert validity time in alert mail
     */
    public synchronized void checkCertValidityForAlert() {
        Instant startTime = Instant.now();
        log.info("start checkCertValidityForAlert startTime:{}", startTime.toEpochMilli());
        //check last alert time, if within interval, not send
        TbAlertRule alertRule = alertRuleService.queryByRuleId(AlertRuleType.CERT_ALERT.getValue());
        if(AlertRuleTools.isWithinAlertIntervalByNow(alertRule)) {
            log.debug("end checkCertValidityForAlert non-sending mail" +
                    " for beyond alert interval:{}", alertRule);
            return;
        }
        List<X509Certificate> certList = certService.loadAllX509Certs();
        certList.stream()
            .forEach(cert -> {
                List<String> alertContentList = new ArrayList<>();
                Date certNotAfter = cert.getNotAfter();
                if(checkWithin7days(certNotAfter)){
                    log.warn("cert validity alert. certNotAfter:{}",
                            certNotAfter);
                    SimpleDateFormat formatTool=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String fingerPrint = null;
                    try {
                        fingerPrint = NodeMgrTools.getCertFingerPrint(cert.getEncoded());
                    } catch (CertificateEncodingException e) {
                        e.printStackTrace();
                    }
                    alertContentList.add(formatTool.format(certNotAfter) + "(证书指纹:{" + fingerPrint + "})");
                    alertContentList.add(formatTool.format(certNotAfter) + "(cert fingerprint:{" + fingerPrint + "})");
                    alertMailService.sendMailByRule(AlertRuleType.CERT_ALERT.getValue(), alertContentList);
                }
            });

        log.info("end checkCertValidityForAlert useTime:{} ",
                Duration.between(startTime, Instant.now()).toMillis());
    }

    private boolean checkWithin7days(Date certNotAfter) {
        // unit: ms
        long sevenDays = 1000 * 60 * 60 * 24 * 7;
        long now = Instant.now().toEpochMilli();
        long interval = certNotAfter.getTime() - now;
        log.info("checkWithin7days time distance:{}, sevenDays:{}",
                interval, sevenDays);
        if(interval < sevenDays) {
            // within 7days or already not valid (<0)
            return true;
        } else {
            // beyond 7days
            return false;
        }
    }
}

