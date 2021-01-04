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

package node.mgr.test.alert.mail;

import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.Application;
import com.webank.webase.node.mgr.alert.mail.MailService;
import com.webank.webase.node.mgr.alert.rule.AlertRuleMapper;
import com.webank.webase.node.mgr.alert.rule.AlertRuleService;
import com.webank.webase.node.mgr.alert.task.AuditMonitorTask;
import com.webank.webase.node.mgr.base.tools.AlertRuleTools;
import com.webank.webase.node.mgr.alert.rule.entity.TbAlertRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SendMailTest {

    @Autowired
    TemplateEngine templateEngine;

    @Autowired
    AlertRuleMapper alertRuleMapper;

    @Autowired
    AlertRuleService alertRuleService;

    @Autowired
    MailService mailService;

    @Autowired
    AuditMonitorTask auditMonitorTask;

    public static final String testTitle = "WeBase-Node-Manager测试邮件，请勿回复";
    public static final String fromMailAddress = "yourmail@163.com";
    public static final String toMailAddress = "yourmail@163.com";
    public static final String testContent = "【这是节点管理的测试邮件，请勿回复】";

    /**
     * test alert_rule
     * INSERT INTO `tb_alert_rule`(`rule_name`,`enable`,`alert_type`,`alert_level`,`alert_interval`,`alert_content`,`content_param_list`,`description`,`is_all_user`,`user_list`,`create_time`,`modify_time`,`less_than`,`less_and_equal`,`larger_than`,`larger_and_equal`,`equal`)VALUES ('测试告警', 0, 2, 'low', 3600, '这是测试邮件，来自from', '["from"]', '', 0, '["yourmail@163.com"]', '2019-10-29 20:02:30', '2019-10-29 20:02:30', '','','','','');
     * test mail_server_config
     * INSERT INTO `tb_mail_server_config`(`server_name`,`host`,`username`,`password`,`protocol`,`default_encoding`,`create_time`,`modify_time`,`authentication`,`starttls_enable`,`starttls_required`,`socket_factory_port`,`socket_factory_class`,`socket_factory_fallback`) VALUES ('Default config', 'smtp.163.com', 'yourmail@163.com', '','smtp', 'UTF-8','2019-10-29 20:02:30', '2019-10-29 20:02:30', 1, 1, 0, 465, 'javax.net.ssl.SSLSocketFactory', 0);     *
     */
    @Test
    public void testSendingByRule() {
        // make sure mail server config is enabled, userList is not empty
        mailService.sendMailByRule(3, "WeBASE-Node-Manager in Test");
    }

    /**
     * testSendingMailByRule with ReplacementList
     */
    @Test
    public void testSendingMailByRuleAndReplacementList() {
        String zh = "TEST 2019-12-19" + "(证书指纹:{" + "8D222" + "})";
        String en = "TEST 2019-12-19" + "(cert fingerprint:{" + "8D222" + "})";
        List<String> list = new ArrayList<>();
        list.add(zh);
        list.add(en);
        // make sure mail server config is enabled
        mailService.sendMailByRule(1, list);
        mailService.sendMailByRule(2, list);
        mailService.sendMailByRule(3, list);
    }
    /**
     * set fromMailAddress, toMailAddress, testTitle, using db's mail server config
     */
    @Test
    public void testSending() {
        // add date in content
        SimpleDateFormat formatTool=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = formatTool.format(new Date());
        mailService.refreshJavaMailSenderConfigFromDB();
        mailService.sendMailBare(fromMailAddress, toMailAddress,
                testTitle, testContent + "\n " + date);
    }


    /**
     * test replace content & thyme leaf's html template
     */
    @Test
    public void testFinalEmailContent() {
        TbAlertRule alertRule = alertRuleService.queryByRuleId(1);
        System.out.println("=========alertRule=========");
        System.out.println(JsonTools.toJSONString(alertRule));

        String emailTitle = AlertRuleTools.getAlertTypeStrFromEnum(alertRule.getAlertType());
        System.out.println("=========emailTitle=========");
        System.out.println(emailTitle);

        String defaultEmailContent = alertRule.getAlertContent();
        String emailContentParam2Replace = alertRule.getContentParamList();
        // 获取替换后的email content
        String finalEmailContent = AlertRuleTools.processMailContent(defaultEmailContent,
                emailContentParam2Replace, testContent);
        System.out.println("=========finalEmailContent=========");
        System.out.println(finalEmailContent);

        // thymeleaf初始化
        Context context = new Context();
        context.setVariable("replaceContent", finalEmailContent);
        String emailTemplateContent = templateEngine.process("AlertEmailTemplate", context);
        System.out.println("=========emailTemplateContent=========");
        System.out.println(emailTemplateContent);
    }

    /**
     * test process alertContent and contentParamList
     * use replaceText to replace params in alertContent
     */
    @Test
    public void testProcessEmailContent() {
        // 初始化参数
        // 假设只有一个参数时
        List<String> testParam = new ArrayList<>();
        testParam.add("nodeId");
        String afterTestParam = JsonTools.toJSONString(testParam);
        System.out.println("=======afterTestParam======");
        System.out.println(afterTestParam);

        // 转回去，可能出错
        List<String> finalParamList = JsonTools.toJavaObjectList(afterTestParam, String.class);

        // 待处理的string
        String alertContent = "您的节点nodeId状态异常";
        String nodeId = "0x000124412312321ABCEF";
        String result = "";
        for(String paramItem: finalParamList) {
            result = alertContent.replace(paramItem, nodeId);
        }
        System.out.println("=======alertContent======");
        System.out.println(alertContent);
        System.out.println("=======result======");
        System.out.println(result);
    }

    @Test
    public void parseString2List() {
//        List<String> testList = new ArrayList<>();
//        testList.add("targetmail@163.com");
//        testList.add("yourmail@163.com");
//        System.out.println(JSON.toJSON(testList));
        String listStr = "[\"targetmail@163.com\",\"yourmail@163.com\"]";
        List<String> list = JsonTools.toJavaObjectList(listStr, String.class);
        System.out.println(list);
    }

    @Test
    public void testParseDate2Str() {
        // add date in content
        SimpleDateFormat formatTool=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String test = "";
        test += formatTool.format(new Date());
        System.out.println(test);
        String time = formatTool.format(LocalDateTime.now());
        System.out.println(time);

    }

    /**
     * task triggers sending alert mail by rule
     */
    @Test
    public void testSendingByRuleInTask() {
        // make sure mail server config is enabled
        auditMonitorTask.auditAlertTaskStart();
    }
}
