/**
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
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
 * Entity of alert type configuration
 * related with tb_alert_rule
 */
@Data
@NoArgsConstructor
public class TbAlertRule {

    private Integer ruleId;

    /**
     * 告警规则的名字
     */
    private String ruleName;

    /**
     * 是否启用
     */
    private Integer enable;

    /**
     * 告警的类型：证书有效期、节点异常、审计
     */
    private Integer alertType;
    /**
     * 规则的级别：3-低、2-中、1-高
     */
    private Integer alertLevel;

    /**
     * 出发时发送的频率
     * unit: s
     * needed switch to ms for calculating
     */
    private Long alertIntervalSeconds;

    /**
     * 告警的内容
     * @param 标题
     * @param 内容模板：包含告警时间、告警级别、告警具体信息
     */
    private String alertContent;

    /**
     * 告警内容中的，待填充的字段
     */
    private String contentParamList;
    /**
     * 告警规则描述
     */
    private String description;

    /**
     * 告警的作用时间
     */
//    private LocalDateTime startTime;
//    private LocalDateTime endTime;

    /**
     * 告警的conditions
     */
    private String lessThan;
    private String lessAndEqual;
    private String largerThan;
    private String largerAndEqual;
    private String equal;
//    private String status;

    private LocalDateTime createTime;
    private LocalDateTime modifyTime;


    /**
     * 保存该规则的告警目标邮箱地址
     * @param: allUser 全选
     */
    private Integer isAllUser;
    private String userList;

    /**
     * 上次告警的时间，与alertInterval告警频率共同作用
     */
    private LocalDateTime lastAlertTime;
}
