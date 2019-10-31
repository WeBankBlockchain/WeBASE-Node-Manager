///**
// * Copyright 2014-2019 the original author or authors.
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.webank.webase.node.mgr.alert.mail;
//
//import com.alibaba.fastjson.JSON;
//import com.webank.webase.node.mgr.alert.rule.AlertRuleMapper;
//import com.webank.webase.node.mgr.alert.rule.entity.TbAlertRule;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//import org.thymeleaf.TemplateEngine;
//import org.thymeleaf.context.Context;
//
//import javax.mail.MessagingException;
//import javax.mail.internet.MimeMessage;
//import java.util.ArrayList;
//import java.util.List;
//
//@Log4j2
//@Service
//public class MailService {
//
//    @Autowired
//    JavaMailSender mailSender;
//    @Autowired
//    TemplateEngine templateEngine;
//    @Autowired
//    AlertRuleMapper alertRuleMapper;
//
//    public static final String fromMailAddress = "WeBASENodeManagerAlert@webank.com";
//
//    public String processMailContent(String alertContent, String contentTargetParams,
//                         String replacementText) {
//        List<String> contentParamList = new ArrayList<>();
//        log.debug("processMailContent contentTargetParams:{}", contentTargetParams);
//        try{
//            contentParamList = (List<String>) JSON.parse(contentTargetParams);
//        } catch (Exception e) {
//            log.error("processMailContent parse contentParam to List error contentParams:{}, exception:{}",
//                    contentTargetParams, e);
//        }
//        // 替换content中的需要被替代的paramItem， 用replacementText替代
//        // 如 您的节点nodeId状态异常，将nodeId替换为具体的0xda3213..的节点id
//
//        log.debug("processMailContent alertContent:{}", contentTargetParams);
//        String finalAlertContent = "";
//        for(String paramItem: contentParamList) {
//            finalAlertContent = alertContent.replace(paramItem, replacementText);
//        }
//
//
//        return finalAlertContent;
//    }
//
//    /**
//     *
//     * @param from 发送方邮箱
//     * @param to 发送的目标用户
//     * @param ruleId 用户选择一条rule
//     * @param replacementText 单个参数，用于替代rule中的变量，如替代节点告警的nodeId,
//     */
//    public void sendEmail(String from, String to, int ruleId, String replacementText) {
//        TbAlertRule alertRule = alertRuleMapper.queryByRuleId(ruleId);
//        String emailTitle = getAlertTypeStrFromEnum(alertRule.getAlertType());
//        String defaultEmailContent = alertRule.getAlertContent();
//        String emailContentParam2Replace = alertRule.getContentParamList();
//        // 获取替换后的email content
//        String finalEmailContent = processMailContent(defaultEmailContent,
//                emailContentParam2Replace, replacementText);
//
//        // thymeleaf初始化
//        Context context = new Context();
//        context.setVariable("replaceContent", finalEmailContent);
//        String emailTemplateContent = templateEngine.process("mail", context);
//
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = null;
//        try {
//            helper = new MimeMessageHelper(message, true);
//            helper.setFrom(from);
//            helper.setTo(to);
//            helper.setSubject(emailTitle);
//            helper.setText(emailTemplateContent, true);
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//        mailSender.send(message);
//    }
//
//    public String getAlertTypeStrFromEnum(int enumValue) {
//        switch (enumValue) {
//            case 1:
//                return "节点异常告警";
//            case 2:
//                return "审计异常告警";
//            case 3:
//                return "证书异常告警";
//            default:
//                return "";
//        }
//    }
//
////    public List<String> deserializeString(String text) {
////
////    }
//
//}
