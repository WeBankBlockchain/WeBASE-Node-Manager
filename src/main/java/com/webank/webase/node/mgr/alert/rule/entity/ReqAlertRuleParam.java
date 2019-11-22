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

package com.webank.webase.node.mgr.alert.rule.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Handle PUT request from web to update alert type configs' value
 */
@Data
@NoArgsConstructor
public class ReqAlertRuleParam {

    private Integer ruleId;
    private String ruleName;
    private Integer enable;
    private Integer alertType;
    // unit: s
    private Long alertIntervalSeconds;
    private Integer alertLevel;
    // 用html模板组件
    private String alertContent;
    // 序列化
    private String contentParamList;

    private String description;
    private Integer isAllUser;
    // 序列化
    private String userList;

    private String lessThan;
    private String lessAndEqual;
    private String largerThan;
    private String largerAndEqual;
    private String equal;

    private LocalDateTime lastAlertTime;
}
