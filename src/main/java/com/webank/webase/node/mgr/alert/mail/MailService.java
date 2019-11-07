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

package com.webank.webase.node.mgr.alert.mail;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.account.AccountMapper;
import com.webank.webase.node.mgr.account.entity.AccountListParam;
import com.webank.webase.node.mgr.account.entity.TbAccountInfo;
import com.webank.webase.node.mgr.alert.mail.server.config.MailServerConfigService;
import com.webank.webase.node.mgr.alert.mail.server.config.entity.TbMailServerConfig;
import com.webank.webase.node.mgr.alert.rule.AlertRuleMapper;
import com.webank.webase.node.mgr.alert.rule.AlertRuleTools;
import com.webank.webase.node.mgr.alert.rule.entity.TbAlertRule;
import com.webank.webase.node.mgr.base.enums.EnableStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Log4j2
@Service
public class MailService {

    @Autowired()
    @Qualifier("mailSender")
    JavaMailSenderImpl mailSender;

    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    AlertRuleMapper alertRuleMapper;
    @Autowired
    MailServerConfigService mailServerConfigService;
    @Autowired
    AccountMapper accountMapper;

    /**
     * Java Mail Sender Implement config
     *
     * Server config replace default config in yml
     * use the last one in table tb_mail_server_config as @Param latest MailServerConfig
     */
    public void configJavaMailSenderFromDB() {
        log.debug("start configJavaMailSenderFromDB. ");
        TbMailServerConfig latestMailServerConfig = mailServerConfigService.getLatestMailServerConfig();
        // refresh Java mail sender config
        refreshJavaMailSenderConfig(latestMailServerConfig);
        log.debug("end configJavaMailSenderFromDB latestMailServerConfig:{}", latestMailServerConfig);
    }

    public void refreshJavaMailSenderConfig(TbMailServerConfig latestMailServerConfig) {
        log.debug("start refreshJavaMailSenderConfig. latestMailServerConfig:{}", latestMailServerConfig);
        mailSender.setHost(latestMailServerConfig.getHost());
        if(latestMailServerConfig.getPort() != null ) {
            mailSender.setPort(latestMailServerConfig.getPort());
        }

        mailSender.setUsername(latestMailServerConfig.getUsername());
        mailSender.setPassword(latestMailServerConfig.getPassword());
        mailSender.setDefaultEncoding(latestMailServerConfig.getDefaultEncoding());

        mailSender.setProtocol(latestMailServerConfig.getProtocol());

        Properties sslProperties = initJavaMailProperties(latestMailServerConfig);
        log.debug("end refreshJavaMailSenderConfig. sslProperties:{}", sslProperties);
        mailSender.setJavaMailProperties(sslProperties);

    }

    /**
     * set " + protocolName + "/pop3/imap java mailsender configuration
     */
    private Properties initJavaMailProperties(TbMailServerConfig latestMailServerConfig) {
        // set SMTP JavaMailProperties such as ssl configuration
        Properties sslProperties = new Properties();
        Boolean isAuthEnable = latestMailServerConfig.getAuthentication() == EnableStatus.ON.getValue();
        Boolean isSTARTTLSEnable = latestMailServerConfig.getStarttlsEnable() == EnableStatus.ON.getValue();
        String protocolName = latestMailServerConfig.getProtocol().toLowerCase();
        sslProperties.setProperty("mail." + protocolName + ".auth",
                String.valueOf(isAuthEnable));
        sslProperties.setProperty("mail." + protocolName + ".starttls.enable",
                String.valueOf(isSTARTTLSEnable));
        // if required starttls is true, set ssl configuration
        Boolean isSTARTTLSRequired = (latestMailServerConfig.getStarttlsRequired() == EnableStatus.ON.getValue());
        if (isSTARTTLSRequired) {
            sslProperties.setProperty("mail." + protocolName + ".starttls.required",
                    String.valueOf(isSTARTTLSRequired));
            sslProperties.setProperty("mail." + protocolName + ".socketFactory.port",
                    String.valueOf(latestMailServerConfig.getSocketFactoryPort()));
            sslProperties.setProperty("mail." + protocolName + ".socketFactory.class",
                    latestMailServerConfig.getSocketFactoryClass());
            Boolean isUsingFallback = latestMailServerConfig.getSocketFactoryFallback() == EnableStatus.ON.getValue();
            sslProperties.setProperty("mail." + protocolName + ".socketFactory.fallback",
                    String.valueOf(isUsingFallback));
        }
        return sslProperties;
    }
    /**
     * @param ruleId 用户选择一条rule，从db获取
     * @param replacementText 实际值的参数，用于替代emailContent中的变量
     *                        如0x22实际nodeId替代节点告警的nodeId变量,
     */
    public void sendMailByRule(int ruleId, String replacementText) {

        log.debug("start sendMailByRule ruleId:{},replacementText:{}",
                ruleId, replacementText);
        TbMailServerConfig latestMailServerConfig = mailServerConfigService.getLatestMailServerConfig();
        // if mail server not config
        if(latestMailServerConfig.getStatus() == EnableStatus.OFF.getValue()) {
            log.error("end sendMailByRule for server config not done:{}", latestMailServerConfig);
            return;
        }
        TbAlertRule alertRule = alertRuleMapper.queryByRuleId(ruleId);
        // if not activated
        if(alertRule.getEnable() == EnableStatus.OFF.getValue()) {
            log.debug("end sendMailByRule non-sending mail for alertRule not enabled:{}", alertRule);
            return;
        }
        // last time alert by now, if within interval, not send
        if(isWithinAlertIntervalByNow(alertRule)) {
            log.debug("end sendMailByRule non-sending mail for beyond alert interval:{}", alertRule);
            return;
        }
        String emailTitle = AlertRuleTools.getAlertTypeStrFromEnum(alertRule.getAlertType());

        String defaultEmailContent = alertRule.getAlertContent();
        String emailContentParam2Replace = alertRule.getContentParamList();
        // 替换变量param后的emailContent
        String emailContentAfterReplace = AlertRuleTools.processMailContent(defaultEmailContent,
                emailContentParam2Replace, replacementText);

        // thymeleaf初始化
        Context context = new Context();
        context.setVariable("replaceContent", emailContentAfterReplace);
        // add date in content
        SimpleDateFormat formatTool=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        context.setVariable("time", formatTool.format(new Date()));
        String emailFinalContent = templateEngine.process("AlertEmailTemplate", context);
        // 将告警发到userList，如果是全选用户
        handleAllUserEmail(alertRule, emailTitle, emailFinalContent);
        alertRule.setLastAlertTime(LocalDateTime.now());
        log.debug("end sendMailByRule. ");
    }

    /**
     * handle email sending to multiple users or sending to all user
     * @param alertRule
     * @param emailTitle
     * @param emailFinalContent
     */
    public void handleAllUserEmail(TbAlertRule alertRule, String emailTitle, String emailFinalContent) {
        log.debug("start handleAllUserEmail alertRule:{},emailTitle:{},emailFinalContent:{}",
                emailTitle, emailTitle, emailFinalContent);
        // get from address
        TbMailServerConfig latestMailServerConfig = mailServerConfigService.getLatestMailServerConfig();
        String fromMailAddress = latestMailServerConfig.getUsername();
        // 将告警发到userList，如果是全选用户
        if(alertRule.getIsAllUser() == EnableStatus.ON.getValue()){
            AccountListParam accountListParam = new AccountListParam();
            List<TbAccountInfo> allAccountList = accountMapper.listOfAccount(accountListParam);
            for(TbAccountInfo accountInfo: allAccountList) {
                String accountEmailAddress = accountInfo.getEmail();
                if(!"".equals(accountEmailAddress)&& accountEmailAddress != null) {
                    sendMailBare(fromMailAddress, accountEmailAddress,
                            emailTitle, emailFinalContent);
                }
            }
            log.debug("end handleAllUserEmail. ");
        }else {
            List<String> userList = new ArrayList<>();
            try {
                userList = (List<String>) JSON.parse(alertRule.getUserList());
            }catch (Exception e) {
                log.error("handleAllUserEmail parse error: e:[], getUserList{}",
                        e, alertRule.getUserList());
            }
            for(String userMailAddress: userList) {
                try {
                    log.debug("handleAllUserEmail sending email fromMailAddress:{},fromMailAddress:{}," +
                                    "emailTitle:{},emailFinalContent:{}",
                            fromMailAddress, userMailAddress, emailTitle, emailFinalContent);
                    sendMailBare(fromMailAddress,
                            userMailAddress, emailTitle, emailFinalContent);
                }catch (Exception e) {
                    log.error("handleAllUserEmail send email error:[]", e);
                }
            }
            log.debug("end handleAllUserEmail. ");
        }
    }
    /**
     * 发送邮件基类
     * @param from
     * @param to
     * @param emailTitle
     * @param emailFinalContent
     */
    public void sendMailBare(String from, String to, String emailTitle, String emailFinalContent) {
        log.debug("start sendMailBare from:{},to:{},emailTitle:{},emailFinalContent:{}",
                from, to, emailTitle, emailFinalContent);
        // refresh java mail sender config from db, cover yml's config
        configJavaMailSenderFromDB();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(emailTitle);
            helper.setText(emailFinalContent, true);
        } catch (MessagingException e) {
            log.error("sendMailBare error:[]", e);
            e.printStackTrace();
        }
        log.debug("end sendMailBare MimeMessage:{}", message);
        mailSender.send(message);
    }

    /**
     * calculate interval time form lastAlertTime
     */
    public boolean isWithinAlertIntervalByNow(TbAlertRule tbAlertRule) {
        LocalDateTime lastAlertTime = tbAlertRule.getLastAlertTime();
        // first time alert
        if(lastAlertTime == null) {
            return false;
        }
        Long alertInterval = tbAlertRule.getAlertIntervalSeconds();
        // unit s => ms
        alertInterval *= 1000;

        LocalDateTime now = LocalDateTime.now();
        Long actualInterval = Timestamp.valueOf(now).getTime()
                - Timestamp.valueOf(lastAlertTime).getTime();

        if(actualInterval < alertInterval) {
            return true;
        }else {
            return false;
        }
    }

}
