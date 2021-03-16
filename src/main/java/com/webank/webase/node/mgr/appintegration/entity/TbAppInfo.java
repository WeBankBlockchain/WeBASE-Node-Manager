/**
 * Copyright 2014-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.node.mgr.appintegration.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class of table tb_app_info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TbAppInfo {
    private Integer id;
    private String appName;
    private String appKey;
    private String appSecret;
    /**
     * 1-template, 2-new
     */
    private Integer appType;
    /**
     * 1-active, 2-not active
     */
    private Integer appStatus;
    private String appDocLink;
    private String appLink;
    private String appIp;
    private Integer appPort;
    private String appIcon;
    private String appDesc;
    private String appDetail;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
    
    public TbAppInfo(Integer id, Integer appStatus) {
        this.id = id;
        this.appStatus = appStatus;
    }
}
