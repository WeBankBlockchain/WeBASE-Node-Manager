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

package com.webank.webase.node.mgr.base.tools;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.alert.rule.entity.ReqAlertRuleParam;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class AlertRuleTools {


    public static String getAlertTypeStrFromEnum(int enumValue) {
        switch (enumValue) {
            case 1:
                return "WeBASE-Node-Manager节点异常告警";
            case 2:
                return "WeBASE-Node-Manager审计异常告警";
            case 3:
                return "WeBASE-Node-Manager证书异常告警";
            default:
                return "WeBASE-Node-Manager其他告警";
        }
    }

    public static String processMailContent(String alertContent, String contentTargetParams,
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

        log.debug("processMailContent alertContent:{}", contentTargetParams);
        String emailContentAfterReplace = "";
        for(String paramItem: contentParamList) {
            emailContentAfterReplace = alertContent.replace(paramItem, replacementText);
        }

        return emailContentAfterReplace;
    }

}
