/**
 * Copyright 2014-2019 the original author or authors.
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
import java.util.List;

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
    private Boolean enable;

    /**
     * 告警的类型：证书有效期、节点异常、审计
     */
    private Integer alertType;
    /**
     * 规则的级别：低、中、高、严重
     */
    private String alertLevel;

    /**
     * 出发时发送的频率
     * TODO 间隔时间下限，不能小于10分钟？
     */
    private Long alertInterval;

    /**
     * 告警的内容
     * @param 标题
     * @param 内容模板：包含告警时间、告警级别、告警具体信息
     */
    private String alertContent;

    /**
     * 告警内容中的，待填充的字段
     */
//    private List<String> contentParamList;
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
    private Boolean isAllUser;
//    private List<Integer> userList;
    private String userList;
}
