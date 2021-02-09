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

package com.webank.webase.node.mgr.alert.log.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for table tb_alert_log in db
 */
@Data
@NoArgsConstructor
public class AlertLog {
    private Integer logId;
    /**
     * 1-节点, 2-审计, 3-证书
     */
    private Integer alertType;
    /**
     * 1-high, 2-middle, 3-low
     */
    private Integer alertLevel;
    private String alertContent;
    private String description;
    /**
     * 0-未处理，1-已处理
     */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}
