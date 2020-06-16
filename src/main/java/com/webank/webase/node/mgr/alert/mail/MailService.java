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

package com.webank.webase.node.mgr.alert.mail;

import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.account.AccountMapper;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.account.entity.AccountListParam;
import com.webank.webase.node.mgr.account.entity.TbAccountInfo;
import com.webank.webase.node.mgr.alert.log.AlertLogService;
import com.webank.webase.node.mgr.alert.mail.server.config.MailServerConfigService;
import com.webank.webase.node.mgr.alert.mail.server.config.entity.ReqMailServerConfigParam;
import com.webank.webase.node.mgr.alert.mail.server.config.entity.TbMailServerConfig;
import com.webank.webase.node.mgr.alert.rule.AlertRuleMapper;
import com.webank.webase.node.mgr.base.tools.AlertRuleTools;
import com.webank.webase.node.mgr.alert.rule.entity.TbAlertRule;
import com.webank.webase.node.mgr.base.enums.EnableStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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

/**
 * Send mail Service including config JavaMailSender and mail content
 */
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
    @Autowired
    AlertLogService alertLogService;

    /**
     * Java Mail Sender Implement config
     * Server config replace default config in yml
     * use the last one in db's table 'tb_mail_server_config' in @Param latestMailServerConfig
     */
    public void refreshJavaMailSenderConfigFromDB() {
        log.debug("start refreshJavaMailSenderConfigFromDB. ");
        TbMailServerConfig latestMailServerConfig = mailServerConfigService.getLatestMailServerConfig();
        // refresh Java mail sender config
        initJavaMailSenderConfig(latestMailServerConfig);
        log.debug("end refreshJavaMailSenderConfigFromDB latestMailServerConfig:{}", latestMailServerConfig);
    }

    /**
     * Java Mail Sender Implement config
     * Server config replace default config in yml
     * use the configs from web's request body @Param reqMailServerConfigParam
     *
     * web's config has been check in MailController,
     * but @protocol and @sslProperties need check if empty,
     * init it from db's config, see function @initJavaMailProperties and @checkMailServerConfigAndInit
     */
    public void refreshJavaMailSenderConfigFromWeb(ReqMailServerConfigParam reqMailServerConfigParam) {
        log.debug("start refreshJavaMailSenderConfigFromWeb. reqMailServerConfigParam:{}",
                reqMailServerConfigParam);
        // init empty param from db
        ReqMailServerConfigParam reqConfigParamAfterInit = checkMailServerConfigAndInit(reqMailServerConfigParam);

        TbMailServerConfig tempMailServerConfigFromWeb = new TbMailServerConfig();
        BeanUtils.copyProperties(reqConfigParamAfterInit, tempMailServerConfigFromWeb);
        // refresh Java mail sender config
        initJavaMailSenderConfig(tempMailServerConfigFromWeb);
        log.debug("end refreshJavaMailSenderConfigFromWeb tempMailServerConfigFromWeb:{}", tempMailServerConfigFromWeb);
    }

    /**
     * latestMailServerConfig might come from db or web
     * @param latestMailServerConfig
     */
    public void initJavaMailSenderConfig(TbMailServerConfig latestMailServerConfig) {
        log.debug("start initJavaMailSenderConfig. latestMailServerConfig:{}", latestMailServerConfig);
        mailSender.setHost(latestMailServerConfig.getHost());
        mailSender.setPort(latestMailServerConfig.getPort());
        Boolean isAuthEnable = latestMailServerConfig.getAuthentication() == EnableStatus.ON.getValue();
        if(isAuthEnable) {
            mailSender.setUsername(latestMailServerConfig.getUsername());
            mailSender.setPassword(latestMailServerConfig.getPassword());
        }
        mailSender.setDefaultEncoding(latestMailServerConfig.getDefaultEncoding());
        mailSender.setProtocol(latestMailServerConfig.getProtocol());
        // init properties
        Properties sslProperties = initJavaMailProperties(latestMailServerConfig);
        log.debug("end initJavaMailSenderConfig. sslProperties:{}", sslProperties);
        mailSender.setJavaMailProperties(sslProperties);
    }

    /**
     * set properties of JavaMailSender's config
     * set " + protocolName + "/pop3/imap java mailsender configuration
     */
    private Properties initJavaMailProperties(TbMailServerConfig latestMailServerConfig) {

        // set SMTP JavaMailProperties such as ssl configuration
        Properties sslProperties = new Properties();
        Boolean isAuthEnable = latestMailServerConfig.getAuthentication() == EnableStatus.ON.getValue();
        String protocolName = latestMailServerConfig.getProtocol().toLowerCase();
        sslProperties.setProperty("mail." + protocolName + ".auth",
                String.valueOf(isAuthEnable));
        Boolean isSTARTTLSEnable = latestMailServerConfig.getStarttlsEnable() == EnableStatus.ON.getValue();;
        sslProperties.setProperty("mail." + protocolName + ".starttls.enable",
                String.valueOf(isSTARTTLSEnable));

        // 设置读取超时时间，连接超时时间、、写入超时时间
        sslProperties.setProperty("mail." + protocolName + ".timeout",
                String.valueOf(latestMailServerConfig.getTimeout()));
        sslProperties.setProperty("mail." + protocolName + ".connectiontimeout",
                String.valueOf(latestMailServerConfig.getConnectionTimeout()));
        sslProperties.setProperty("mail." + protocolName + ".writetimeout",
                String.valueOf(latestMailServerConfig.getWriteTimeout()));

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
     * alert mail content comes from db's default alert_rule,
     * using @param replace variable in alert_rule,
     * then fill in resources/templates/xx.html's variable
     */
    public void sendMailByRule(int ruleId, String replacementText) {

        log.debug("start sendMailByRule ruleId:{},replacementText:{}",
                ruleId, replacementText);
        TbMailServerConfig latestMailServerConfig = mailServerConfigService.getLatestMailServerConfig();
        // if mail server not turn ON
        if(latestMailServerConfig.getEnable() == EnableStatus.OFF.getValue()) {
            log.warn("end sendMailByRule for server config not enable:{}", latestMailServerConfig);
            return;
        }
        TbAlertRule alertRule = alertRuleMapper.queryByRuleId(ruleId);
        // if alert not activated
        if(alertRule.getEnable() == EnableStatus.OFF.getValue()) {
            log.warn("end sendMailByRule non-sending mail for alertRule not enabled:{}", alertRule);
            return;
        }
        // if userList is empty or default email
        if(StringUtils.isEmpty(alertRule.getUserList())) {
            log.error("end sendMailByRule for no receive mail address:{}", alertRule);
            return;
        }
        String emailTitle = AlertRuleTools.getAlertTypeStrFromEnum(alertRule.getAlertType());
        /* handle email alert content */
        // default content
        String defaultEmailContent = alertRule.getAlertContent();

        // params to be replaced
        String emailContentParam2Replace = alertRule.getContentParamList();
        String emailContentAfterReplace = AlertRuleTools.processMailContent(defaultEmailContent,
                emailContentParam2Replace, replacementText);

        //init thymeleaf email template
        Context context = new Context();
        context.setVariable("replaceContent", emailContentAfterReplace);
        // add date in content
        SimpleDateFormat formatTool=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        context.setVariable("time", formatTool.format(new Date()));
        String emailFinalContent = templateEngine.process("AlertEmailTemplate", context);

        // save alert to log
        alertLogService.saveAlertLogByRuleAndContent(alertRule.getAlertLevel(),
                alertRule.getAlertType(), emailContentAfterReplace);

        // refresh JavaMailSender's config from db
        refreshJavaMailSenderConfigFromDB();
        // 将告警发到userList，如果是全选用户
        handleAllUserEmail(alertRule, emailTitle, emailFinalContent);
        // update alert rule's last alertTime
        alertRule.setLastAlertTime(LocalDateTime.now());
        log.debug("sendMailByRule update alert rule's lastAlertTime updateAlertTime:{}",
                alertRule);
        alertRuleMapper.update(alertRule);
        log.debug("end sendMailByRule. ");
    }

    /**
     * support List of replacement
     * @param ruleId
     * @param replacementTextList
     */
    public void sendMailByRule(int ruleId, List<String> replacementTextList) {

        log.debug("start sendMailByRule ruleId:{},replacementTextList:{}",
                ruleId, replacementTextList);
        TbMailServerConfig latestMailServerConfig = mailServerConfigService.getLatestMailServerConfig();
        // if mail server not turn ON
        if(latestMailServerConfig.getEnable() == EnableStatus.OFF.getValue()) {
            log.warn("end sendMailByRule for server config not enable:{}", latestMailServerConfig);
            return;
        }
        TbAlertRule alertRule = alertRuleMapper.queryByRuleId(ruleId);
        // if alert not activated
        if(alertRule.getEnable() == EnableStatus.OFF.getValue()) {
            log.warn("end sendMailByRule non-sending mail for alertRule not enabled:{}", alertRule);
            return;
        }
        // last time alert by now, if within interval, not send
        // 告警间隔时间的刷新放到遍历group异常的for循环外面
//        if(isWithinAlertIntervalByNow(alertRule)) {
//            log.debug("end sendMailByRule non-sending mail for beyond alert interval:{}", alertRule);
//            return;
//        }
        // if userList is empty or default email
        if(StringUtils.isEmpty(alertRule.getUserList())) {
            log.error("end sendMailByRule for no receive mail address:{}", alertRule);
            return;
        }
        String emailTitle = AlertRuleTools.getAlertTypeStrFromEnum(alertRule.getAlertType());
        /* handle email alert content */
        // default content
        String defaultEmailContent = alertRule.getAlertContent();

        // params to be replaced
        String emailContentParam2Replace = alertRule.getContentParamList();
        String emailContentAfterReplace = AlertRuleTools.processMailContent(defaultEmailContent,
                emailContentParam2Replace, replacementTextList);

        //init thymeleaf email template
        Context context = new Context();
        context.setVariable("replaceContent", emailContentAfterReplace);
        // add date in content
        SimpleDateFormat formatTool=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        context.setVariable("time", formatTool.format(new Date()));
        String emailFinalContent = templateEngine.process("AlertEmailTemplate", context);

        // save alert to log
        alertLogService.saveAlertLogByRuleAndContent(alertRule.getAlertLevel(),
                alertRule.getAlertType(), emailContentAfterReplace);

        // refresh JavaMailSender's config from db
        refreshJavaMailSenderConfigFromDB();
        // 将告警发到userList，如果是全选用户
        handleAllUserEmail(alertRule, emailTitle, emailFinalContent);
        // update alert rule's last alertTime
        alertRule.setLastAlertTime(LocalDateTime.now());
        log.debug("sendMailByRule update alert rule's lastAlertTime updateAlertTime:{}",
                alertRule);
        alertRuleMapper.update(alertRule);
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
                userList = JsonTools.toJavaObjectList(alertRule.getUserList(), String.class);
                if (userList == null) {
                    log.error("parse json error");
                }
            }catch (Exception e) {
                log.error("handleAllUserEmail parse error: e:{}, getUserList{}",
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
//        refreshJavaMailSenderConfigFromDB();
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
     * init empty param from db
     * @param reqMailServerConfigParam
     * @return param after fill up with db's config
     */
    public ReqMailServerConfigParam checkMailServerConfigAndInit(ReqMailServerConfigParam reqMailServerConfigParam) {
        TbMailServerConfig configFromDB = mailServerConfigService.getLatestMailServerConfig();

        if(reqMailServerConfigParam.getProtocol() == null) {
            reqMailServerConfigParam.setProtocol(configFromDB.getProtocol());
        }
        if(reqMailServerConfigParam.getDefaultEncoding() == null) {
            reqMailServerConfigParam.setDefaultEncoding(configFromDB.getDefaultEncoding());
        }
        if(reqMailServerConfigParam.getStarttlsEnable() == null) {
            reqMailServerConfigParam.setStarttlsEnable(configFromDB.getStarttlsEnable());
        }
        if(reqMailServerConfigParam.getConnectionTimeout() == null) {
            reqMailServerConfigParam.setConnectionTimeout(configFromDB.getConnectionTimeout());
        }
        if(reqMailServerConfigParam.getTimeout() == null) {
            reqMailServerConfigParam.setTimeout(configFromDB.getTimeout());
        }
        if(reqMailServerConfigParam.getWriteTimeout() == null) {
            reqMailServerConfigParam.setWriteTimeout(configFromDB.getWriteTimeout());
        }
        if(reqMailServerConfigParam.getStarttlsRequired() == null) {
            reqMailServerConfigParam.setStarttlsRequired(configFromDB.getStarttlsRequired());
        }else if(reqMailServerConfigParam.getStarttlsRequired() == EnableStatus.ON.getValue()) {
            // init socket ssl config from db
            if(reqMailServerConfigParam.getSocketFactoryClass() == null) {
                reqMailServerConfigParam.setSocketFactoryClass(configFromDB.getSocketFactoryClass());
            }
            if(reqMailServerConfigParam.getSocketFactoryFallback() == null) {
                reqMailServerConfigParam.setSocketFactoryFallback(configFromDB.getSocketFactoryFallback());
            }
            if(reqMailServerConfigParam.getSocketFactoryPort() == null) {
                reqMailServerConfigParam.setSocketFactoryPort(configFromDB.getSocketFactoryPort());
            }
        }
        return reqMailServerConfigParam;
    }

}
