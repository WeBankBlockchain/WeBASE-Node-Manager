/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.account.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * Entity class of table tb_account.
 */
@Data
public class RspDeveloper {

    private String account;
    private Integer roleId;
    private String roleName;
    private String roleNameZh;
    private Integer accountStatus;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
    /**
     * 邮件告警的邮箱
      */
    private String email;
    /**
     * 支持注册
     */
    private String realName;
    private String idCardNumber;
    private Long mobile;
    private String contactAddress;
    private String companyName;
    private LocalDateTime expireTime;

    public RspDeveloper() {
        super();
    }

    public RspDeveloper(String account) {
        super();
        this.account = account;
    }

}
