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
    private Integer serverId;
    private String serverName;
    // 邮箱服务器地址
    private String host;
    private Integer port;
    // 邮箱地址
    private String username;
    // 邮箱授权码
    private String password;
    // 邮箱协议：smtp, pop3, imap
    private String protocol;
    // 默认编码
    private String defaultEncoding;

    private LocalDateTime createTime;
    private LocalDateTime modifyTime;

    /**
     * 以下均为SMTP的properties安全配置项 与 默认值
     * @param authentication 是否需要验证 默认true 0-off, 1-on
     * @param starttlsEnable 支持STARTTLS时则启用 默认true 0-off, 1-on
     * @param starttlsRequired 默认false, 开启则需要配置端口、SSL类等，需保证端口可用
     * @param socketFactory.port 不同的邮箱服务器的TLS端口，发件邮箱465, 收件IMAP为993, POP3为995
     * @param socketFactory.class 默认javax.net.ssl.SSLSocketFactory
     * @param socketFactory.fallback 默认false 0-off, 1-on
     */
    private Integer authentication;
    private Integer starttlsEnable;

    // STARTTLS 具体配置
    private Integer starttlsRequired;
    private Integer socketFactoryPort;
    private String socketFactoryClass;
    private Integer socketFactoryFallback;

    /**
     * 0-off, 1-on
     * if edited and done, status is 1, else is 0
     * if not done, cannot send mails
     */
    private Integer status;

}
