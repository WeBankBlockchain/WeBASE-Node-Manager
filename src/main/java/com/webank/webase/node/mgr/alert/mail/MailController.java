/**
 * Copyright 2014-2021 the original author or authors.
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

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.qctc.common.log.annotation.Log;
import com.qctc.common.log.enums.BusinessType;
import com.webank.webase.node.mgr.alert.mail.server.config.MailServerConfigService;
import com.webank.webase.node.mgr.alert.mail.server.config.entity.ReqMailServerConfigParam;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.enums.EnableStatus;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import java.util.Base64;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * main function is to test mail server config
 * using mailServerConfigService and mailService
 */
@Tag(name="邮件服务测试")
@Log4j2
@RestController
@RequestMapping("/alert/mail")
public class MailController {

    @Autowired
    MailService mailService;
    @Autowired
    MailServerConfigService mailServerConfigService;
    @Autowired
    TemplateEngine templateEngine;

    public static final String testTitle = "WeBase-Node-Manager测试邮件，请勿回复";


    /**
     * check param empty and send test mail using ReqMailServerConfigParam
     * @param toMailAddress
     * @param reqMailServerConfigParam
     * @return
     */
    @Log(title = "BCOS3/系统监控/告警管理", businessType = BusinessType.INSERT)
    @SaCheckPermission("bcos3:monitor:sendMailTest")
    @PostMapping("/test/{toMailAddress}")
    public Object sendTestMail(@PathVariable("toMailAddress")String toMailAddress,
                                  @RequestBody ReqMailServerConfigParam reqMailServerConfigParam) {
        Instant startTime = Instant.now();
        log.info("start sendDefaultMail. startTime:{} toMailAddress:{}",
                startTime.toEpochMilli(), toMailAddress);
        try{
            checkParamEmpty(reqMailServerConfigParam);
        }catch (NodeMgrException e){
            return new BaseResponse(ConstantCode.MAIL_SERVER_CONFIG_PARAM_EMPTY);
        }
        try {
            String pwdDecoded = new String(Base64.getDecoder().decode(reqMailServerConfigParam.getPassword()));
            reqMailServerConfigParam.setPassword(pwdDecoded);
        } catch (Exception e) {
            log.error("decode pwd error:[]", e);
            return new BaseResponse(ConstantCode.PASSWORD_DECODE_FAIL, e.getMessage());
        }
        // get configuration from web and refresh JavaMailSender
        mailService.refreshJavaMailSenderConfigFromWeb(reqMailServerConfigParam);

        String fromMailAddress = reqMailServerConfigParam.getUsername();
        Context context = new Context();
        // add date in content
        SimpleDateFormat formatTool=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        context.setVariable("time", formatTool.format(new Date()));
        String emailFinalContent = templateEngine.process("AlertEmailForTest", context);

        try {
            mailService.sendMailBare(fromMailAddress, toMailAddress,
                    testTitle, emailFinalContent);
            log.info("end sendDefaultMail. useTime:{}",
                    Duration.between(startTime, Instant.now()).toMillis());
            return new BaseResponse(ConstantCode.SUCCESS);
        }catch (Exception e) {
            log.error("sendDefaultMail error：[]", e);
            return new BaseResponse(ConstantCode.SEND_MAIL_ERROR, e.getMessage());
        }
    }

    public void checkParamEmpty(ReqMailServerConfigParam reqMailServerConfigParam) {
        log.debug("start checkParamEmpty reqMailServerConfigParam:{}", reqMailServerConfigParam);
        if(reqMailServerConfigParam.getServerId() == null || reqMailServerConfigParam.getPort() == null ||
        reqMailServerConfigParam.getAuthentication() == null ||
                StringUtils.isEmpty(reqMailServerConfigParam.getHost())) {
            log.error("error checkParamEmpty reqMailServerConfigParam:{}", reqMailServerConfigParam);
            throw new NodeMgrException(ConstantCode.MAIL_SERVER_CONFIG_PARAM_EMPTY);
        }
        if(reqMailServerConfigParam.getAuthentication() == EnableStatus.ON.getValue()) {
            if(StringUtils.isEmpty(reqMailServerConfigParam.getUsername()) ||
                    StringUtils.isEmpty(reqMailServerConfigParam.getPassword())) {
                log.error("error checkParamEmpty in auth reqMailServerConfigParam:{}", reqMailServerConfigParam);
                throw new NodeMgrException(ConstantCode.MAIL_SERVER_CONFIG_PARAM_EMPTY);
            }
        }
        log.debug("end checkParamEmpty reqMailServerConfigParam:{}", reqMailServerConfigParam);
    }
}
