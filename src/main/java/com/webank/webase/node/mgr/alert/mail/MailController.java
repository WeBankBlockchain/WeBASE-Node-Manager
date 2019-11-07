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

import com.webank.webase.node.mgr.alert.mail.server.config.MailServerConfigService;
import com.webank.webase.node.mgr.alert.mail.server.config.entity.TbMailServerConfig;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@Log4j2
@RestController
@RequestMapping("/alert/mail")
public class MailController {

    @Autowired
    MailService mailService;
    @Autowired
    MailServerConfigService mailServerConfigService;

    public static final String testTitle = "WeBase-Node-Manager测试邮件，请勿回复";
    public static final String testContent = "这是WeBase-Node-Manager节点管理服务-邮件告警的测试邮件，请勿回复";

    @PostMapping("/test/{toMailAddress}")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object sendDefaultMail(@PathVariable("toMailAddress")String toMailAddress) {
        Instant startTime = Instant.now();
        log.info("start sendDefaultMail. startTime:{} toMailAddress:{}",
                startTime.toEpochMilli(), toMailAddress);
        // get latest mail config
        TbMailServerConfig latestMailServerConfig = mailServerConfigService.getLatestMailServerConfig();
        String fromMailAddress = latestMailServerConfig.getUsername();
        try {
            mailService.sendMailBare(fromMailAddress, toMailAddress,
                    testTitle, testContent);
            log.info("end sendDefaultMail. useTime:{}",
                    Duration.between(startTime, Instant.now()).toMillis());
            return new BaseResponse(ConstantCode.SUCCESS);
        }catch (Exception e) {
            log.error("sendDefaultMail error：[]", e);
            return new BaseResponse(ConstantCode.SEND_MAIL_ERROR, e.getMessage());
        }
    }
}
