package com.webank.webase.node.mgr.alert.task;

import com.webank.webase.node.mgr.alert.mail.MailService;
import com.webank.webase.node.mgr.base.enums.AlertRuleType;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.cert.CertService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sun.security.x509.X509CertImpl;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.List;


/**
 * cert validity time within 7 days triggers alert mail
 */
@Log4j2
@Component
public class CertMonitorTask {

    @Autowired
    private CertService certService;
    @Autowired
    private ConstantProperties cProperties;
    @Autowired
    private MailService alertMailService;

    /**
     * set scheduler's interval
     */
    @Scheduled(fixedDelayString = "${constant.certAlertMailInterval}")
    public void certAlertTaskStart() {
        checkCertValidityForAlert();
    }
    /**
     * scheduled task for cert validity time in alert mail
     */
    public synchronized void checkCertValidityForAlert() {
        Instant startTime = Instant.now();
        log.info("start checkCertValidityForAlert startTime:{}", startTime.toEpochMilli());
        List<X509CertImpl> certList = certService.loadAllX509Certs();
        certList.stream()
            .forEach(cert -> {
                Date certNotAfter = cert.getNotAfter();
                if(checkWithin7days(certNotAfter)){
                    log.warn("cert validity alert. certNotAfter:{}",
                            certNotAfter);
                    SimpleDateFormat formatTool=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    alertMailService.sendMailByRule(AlertRuleType.CERT_ALERT.getValue(),
                            formatTool.format(certNotAfter));
                }
            });

        log.info("end checkCertValidityForAlert useTime:{} ",
                Duration.between(startTime, Instant.now()).toMillis());
    }

    private boolean checkWithin7days(Date when) {
        // unit: ms
        long sevenDays = 1000 * 60 * 60 * 24 * 7;
        long now = Instant.now().getLong(ChronoField.MILLI_OF_SECOND);

        long interval = when.getTime() - now;
        if(interval >= sevenDays) {
            // beyond
            return false;
        } else if(interval >= 0){
            // within
            return true;
        } else {
            // beyond
            return false;
        }
    }
}

