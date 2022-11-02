/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.node.mgr.account;

import com.webank.webase.node.mgr.alert.mail.server.config.entity.TbMailServerConfig;
import com.webank.webase.node.mgr.base.enums.EnableStatus;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import java.util.Properties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

/**
 * 支持发送邮件
 */
@Log4j2
@Service
public class MessageService {

    @Autowired
    @Qualifier("checkCode")
    private JavaMailSenderImpl mailSender;
    @Autowired
    private ConstantProperties constantProperties;

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
//        Properties sslProperties = initJavaMailProperties(latestMailServerConfig);
//        log.debug("end initJavaMailSenderConfig. sslProperties:{}", sslProperties);
//        mailSender.setJavaMailProperties(sslProperties);
    }


    public void sendMail(String mailAddress) {
        log.info("sendMail of checkCode {}", mailAddress);
        String mailContent = "";

    }
}
