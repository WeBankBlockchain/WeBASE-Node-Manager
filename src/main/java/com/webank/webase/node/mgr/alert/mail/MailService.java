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
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Log4j2
@Service
public class MailService {

    @Autowired
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
        mailSender.setHost(latestMailServerConfig.getHost());
        if(latestMailServerConfig.getPort() != null && !"".equals(latestMailServerConfig.getPort())) {
            mailSender.setPort(Integer.valueOf(latestMailServerConfig.getPort()));
        }
        mailSender.setUsername(latestMailServerConfig.getUsername());
        mailSender.setPassword(latestMailServerConfig.getPassword());
        mailSender.setProtocol(latestMailServerConfig.getProtocol());
        // TODO 编码默认UTF-8，是否可以去除该字段
        mailSender.setDefaultEncoding(latestMailServerConfig.getDefaultEncoding());
    }

    /**
     * @param ruleId 用户选择一条rule，从db获取
     * @param replacementText 实际值的参数，用于替代emailContent中的变量
     *                        如0x22实际nodeId替代节点告警的nodeId变量,
     */
    public void sendMailByRule(int ruleId, String replacementText) {

        log.debug("start sendMailByRule ruleId:{},replacementText:{}",
                ruleId, replacementText);
        TbAlertRule alertRule = alertRuleMapper.queryByRuleId(ruleId);
        String emailTitle = AlertRuleTools.getAlertTypeStrFromEnum(alertRule.getAlertType());

        String defaultEmailContent = alertRule.getAlertContent();
        String emailContentParam2Replace = alertRule.getContentParamList();
        // 替换变量param后的emailContent
        String emailContentAfterReplace = AlertRuleTools.processMailContent(defaultEmailContent,
                emailContentParam2Replace, replacementText);

        // thymeleaf初始化
        Context context = new Context();
        context.setVariable("replaceContent", emailContentAfterReplace);
        String emailFinalContent = templateEngine.process("AlertEmailTemplate", context);
        // 将告警发到userList，如果是全选用户
        handleAllUserEmail(alertRule, emailTitle, emailFinalContent);
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
        if(alertRule.getIsAllUser()){
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
            List<String> userList = (List<String>) JSON.parse(alertRule.getUserList());
            for(String userMailAddress: userList) {
                sendMailBare(fromMailAddress,
                        userMailAddress, emailTitle, emailFinalContent);
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

}
