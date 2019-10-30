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
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class MailService {

    @Autowired
    JavaMailSender mailSender;
    @Autowired
    TemplateEngine templateEngine;

    public static final String fromMailAddress = "WeBASENodeManagerAlert@webank.com";

    public String processMailContent(String alertContent, String contentTargetParams,
                         String replacementText) {
        List<String> contentParamList = new ArrayList<>();
        log.debug("processMailContent contentTargetParams:{}", contentTargetParams);
        try{
            contentParamList = (List<String>) JSON.parse(contentTargetParams);
        } catch (Exception e) {
            log.error("processMailContent parse contentParam to List error contentParams:{}, exception:{}",
                    contentTargetParams, e);
        }
        // 替换content中的需要被替代的paramItem， 用replacementText替代
        // 如 您的节点nodeId状态异常，将nodeId替换为具体的0xda3213..的节点id
        alertContent = "您的节点nodeId状态异常";

        log.debug("processMailContent alertContent:{}", contentTargetParams);

        for(String paramItem: contentParamList) {
            alertContent.replace(paramItem, replacementText);
        }
        // thymeleaf初始化
        Context context = new Context();
        context.setVariable("replaceContent", alertContent);
        String emailTemplateContent = templateEngine.process("mail", context);

        return emailTemplateContent;
    }

    public void sendEmail(String from, String to, String title, String emailContent) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(title);
            helper.setText(emailContent, true);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        mailSender.send(message);
    }


//    public List<String> deserializeString(String text) {
//
//    }

}
