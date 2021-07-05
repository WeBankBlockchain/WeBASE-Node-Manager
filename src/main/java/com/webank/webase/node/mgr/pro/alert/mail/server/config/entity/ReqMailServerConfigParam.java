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

package com.webank.webase.node.mgr.pro.alert.mail.server.config.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Handle PUT request from web to mail server's config
 */
@Data
@NoArgsConstructor
public class ReqMailServerConfigParam {

    private Integer serverId;
    private String serverName;
    /**
     * 邮箱服务器地址
     */
    private String host;
    private Integer port;
    /**
     * 邮箱地址
     */
    private String username;
    /**
     * 邮箱授权码
     * v1.4.2 base64混淆后传输
     */
    private String password;
    /**
     * 邮箱协议：smtp, pop3, imap
     */
    private String protocol;
    /**
     * 默认编码
     */
    private String defaultEncoding;

    /**
     * properties: 安全配置项
     * Boolean: authentication starttlsEnable starttlsRequired socketFactoryFallback
     * 0-off, 1-on
     */
    private Integer authentication;
    private Integer starttlsEnable;

    /**
     * STARTTLS 具体配置
     */
    private Integer starttlsRequired;
    private Integer socketFactoryPort;
    private String socketFactoryClass;
    private Integer socketFactoryFallback;

    /**
     * 启用、禁用邮箱服务 0-关闭 1-启用
     */
    private Integer enable;
    /**
     * timeout settings
     */
    private Integer timeout;
    private Integer connectionTimeout;
    private Integer writeTimeout;
}
