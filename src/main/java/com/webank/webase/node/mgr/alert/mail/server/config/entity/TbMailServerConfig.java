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

package com.webank.webase.node.mgr.alert.mail.server.config.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * entity for table tb_mail_server_config
 * 存储mail server的配置内容，存在表中；
 * 表为空时，读取yml中的配置内容；
 * 非空时，不同的alertRule以指定不同的mail server
 */

@Data
@NoArgsConstructor
public class TbMailServerConfig {
    // primary key
    private Long serverId;
    private String serverName;
    // 邮箱服务器地址
    private String host;
    private String port;
    // 邮箱地址
    private String username;
    // 邮箱授权码
    private String password;
    // 邮箱协议：smtp, pop3, imap
    private String protocol;
    // 默认编码
    private String defaultEncoding;

    /**
     * properties: 安全配置项 TODO待丰富
     */
    private boolean authentication;
    private boolean starttlsEnable;
    private boolean starttlsRequired;

    private LocalDateTime createTime;
    private LocalDateTime modifyTime;

}
