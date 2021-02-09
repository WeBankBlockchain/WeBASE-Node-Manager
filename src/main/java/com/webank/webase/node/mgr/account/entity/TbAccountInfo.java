/**
 * Copyright 2014-2020  the original author or authors.
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
public class TbAccountInfo {

    private String account;
    private String accountPwd;
    private Integer roleId;
    private String roleName;
    private String roleNameZh;
    private int loginFailTime;
    private Integer accountStatus;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
    /**
     * 邮件告警的邮箱
      */
    private String email;

    public TbAccountInfo() {
        super();
    }

    public TbAccountInfo(String account) {
        super();
        this.account = account;
    }

    /**
     * init by account、accountPwd、roleId、description.
     */
    public TbAccountInfo(String account, String accountPwd, Integer roleId, String description) {
        super();
        this.account = account;
        this.accountPwd = accountPwd;
        this.roleId = roleId;
        this.description = description;
    }

    /**
     * init by account、accountPwd、roleId、description、email.
     */
    public TbAccountInfo(String account, String accountPwd, Integer roleId,
                         String description, String email) {
        super();
        this.account = account;
        this.accountPwd = accountPwd;
        this.roleId = roleId;
        this.description = description;
        this.email = email;
    }
}
