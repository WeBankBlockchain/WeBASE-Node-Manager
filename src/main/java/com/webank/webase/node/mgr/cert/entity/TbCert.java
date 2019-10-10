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
package com.webank.webase.node.mgr.cert.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TbCert {
    // Primary Key
    private String fingerPrint;
    private String certName;
    private String content;
    private String certType;
    // node id=public key, 节点证书&sdk证书才有
    private String publicKey;
    private String address;
    // 父证书地址
    private String father;
    private Date validityFrom;
    private Date validityTo;
    private Date createTime;
}
