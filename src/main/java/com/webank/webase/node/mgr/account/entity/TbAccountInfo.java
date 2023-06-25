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

import com.webank.webase.node.mgr.base.enums.RoleType;
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
    /**
     * 支持注册
     */
    private String realName;
    private String idCardNumber;
    private String mobile;
    private String contactAddress;
    private String companyName;
    private LocalDateTime expireTime;

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
        // 开发者默认是3年有效期
        if (RoleType.DEVELOPER.getValue().equals(roleId) ||RoleType.VISITOR.getValue().equals(roleId)) {
            this.expireTime = LocalDateTime.now().plusYears(3L);
        }
    }

    public TbAccountInfo(String account, String accountPwd, Integer roleId, String roleName,
        String roleNameZh, Integer accountStatus, String description, String email,
        String realName, String idCardNumber, String mobile, String contactAddress,
        String companyName, LocalDateTime expireTime) {
        this.account = account;
        this.accountPwd = accountPwd;
        this.roleId = roleId;
        this.roleName = roleName;
        this.roleNameZh = roleNameZh;
        this.accountStatus = accountStatus;
        this.description = description;
        this.email = email;
        this.realName = realName;
        this.idCardNumber = idCardNumber;
        this.mobile = mobile;
        this.contactAddress = contactAddress;
        this.companyName = companyName;
        this.expireTime = expireTime;
    }
}
