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

package com.webank.webase.node.mgr.base.tools;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.webank.webase.node.mgr.alert.rule.entity.TbAlertRule;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class AlertRuleTools {

    // Mail alert content
    public static final String ABNORMAL_USER_EXCEED_ALERT = "";
    public static final String ABNORMAL_CONTRACT_EXCEED_ALERT = "";
    public static final String ABNORMAL_USER_AND_CONTRACT_EXCEED_ALERT = "";

    // alert mail title
    public static final String ALERT_TITLE_PREFIX = "WeBASE-Node-Manager ";

    public static final String ALERT_TITLE_NODE = "节点异常告警";
    public static final String ALERT_TITLE_AUDIT = "审计异常告警";
    public static final String ALERT_TITLE_CERT = "证书异常告警";
    public static final String ALERT_TITLE_OTHERS = "其他告警";
    // en
    public static final String ALERT_TITLE_NODE_EN = " (Node Exception Alert)";
    public static final String ALERT_TITLE_AUDIT_EN = " (Audit Exception Alert)";
    public static final String ALERT_TITLE_CERT_EN = " (Cert Exception Alert)";
    public static final String ALERT_TITLE_OTHERS_EN = " (Other Exception Alert)";

    public static String getAlertTypeStrFromEnum(int enumValue) {
        switch (enumValue) {
            case 1:
                return ALERT_TITLE_PREFIX + ALERT_TITLE_NODE + ALERT_TITLE_NODE_EN;
            case 2:
                return ALERT_TITLE_PREFIX + ALERT_TITLE_AUDIT + ALERT_TITLE_AUDIT_EN;
            case 3:
                return ALERT_TITLE_PREFIX + ALERT_TITLE_CERT + ALERT_TITLE_CERT_EN;
            default:
                return ALERT_TITLE_PREFIX + ALERT_TITLE_OTHERS + ALERT_TITLE_OTHERS_EN;
        }
    }

    /**
     * replace param in mail content
     * @param alertContent
     * @param contentTargetParams
     * @param replacementText
     * @return
     */
    public static String processMailContent(String alertContent, String contentTargetParams,
                                     String replacementText) {
        if(StringUtils.isEmpty(alertContent)) {
            return "";
        }
        if(StringUtils.isEmpty(replacementText)) {
            return alertContent;
        }
        List<String> contentParamList = new ArrayList<>();
        log.debug("processMailContent contentTargetParams:{}", contentTargetParams);
        try{
            contentParamList = JsonTools.toJavaObjectList(contentTargetParams, String.class);
            if (contentParamList == null) {
                log.error("parse json error");
            }
        } catch (Exception e) {
            log.error("processMailContent parse contentParam to List error contentParams:{}, exception:{}",
                    contentTargetParams, e);
        }
        // 替换content中的需要被替代的paramItem， 用replacementText替代
        // 如 您的节点nodeId状态异常，将nodeId替换为具体的0xda3213..的节点id

        log.debug("processMailContent alertContent:{}", contentTargetParams);
        String emailContentAfterReplace = "";
        for(String paramItem: contentParamList) {
            emailContentAfterReplace = alertContent.replace(paramItem, replacementText);
        }

        return emailContentAfterReplace;
    }

    /**
     * String array to replace param in mail content
     * @param alertContent
     * @param contentTargetParams
     * @param replacementTextList
     * @return
     */
    public static String processMailContent(String alertContent, String contentTargetParams,
                                            List<String> replacementTextList) {
        List<String> contentParamList = new ArrayList<>();
        log.debug("processMailContent contentTargetParams:{}", contentTargetParams);
        try{
            contentParamList = JsonTools.toJavaObjectList(contentTargetParams, String.class);
            if (contentParamList == null) {
                log.error("parse json error");
            }
        } catch (Exception e) {
            log.error("processMailContent parse contentParam to List error contentParams:{}, exception:{}",
                    contentTargetParams, e);
        }
        // 替换content中的需要被替代的paramItem， 用replacementText替代
        // 如 您的节点nodeId状态异常，将nodeId替换为具体的0xda3213..的节点id

        log.debug("processMailContent alertContent:{}", contentTargetParams);
        int paramSize = contentParamList.size();
        if(contentParamList.size() != replacementTextList.size()) {
            log.error("processMailContent error for paramList size not match with replacementTextList:{},{}",
                    contentTargetParams, replacementTextList);
            paramSize = Math.min(contentParamList.size(), replacementTextList.size());
        }


        for(int i = 0; i < paramSize; i++) {
            String paramItem = contentParamList.get(i);
            String replacementItem = replacementTextList.get(i);
            alertContent = alertContent.replace(paramItem, replacementItem);
        }
        String emailContentAfterReplace = alertContent;
        return emailContentAfterReplace;
    }

    /**
     * calculate interval time form lastAlertTime
     */
    public static boolean isWithinAlertIntervalByNow(TbAlertRule tbAlertRule) {
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

        return actualInterval < alertInterval;

    }

}
