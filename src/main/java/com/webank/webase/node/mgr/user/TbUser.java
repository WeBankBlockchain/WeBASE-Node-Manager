/*
 * Copyright 2014-2019  the original author or authors.
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
package com.webank.webase.node.mgr.user;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TbUser {

    private Integer userId;
    private String userName;
    private Integer groupId;
    private Integer orgId;
    private String publicKey;
    private Integer userStatus;
    private Integer chainIndex;
    private Integer userType;
    private String address;
    private Integer hasPk;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;

    public TbUser() {
        super();
    }

    /**
     * init TbUser.
     */
    public TbUser(Integer chainIndex, Integer hasPk, Integer userType, String userName,
        Integer groupId, Integer orgId, String address,
        String publicKey, String description) {
        super();
        this.chainIndex = chainIndex;
        this.hasPk = hasPk;
        this.userType = userType;
        this.userName = userName;
        this.groupId = groupId;
        this.orgId = orgId;
        this.publicKey = publicKey;
        this.description = description;
        this.address = address;
    }

    /**
     * init TbUser.
     */
    public TbUser(Integer hasPk, Integer userType, String userName, Integer groupId,
        Integer orgId, String address, String publicKey,
        String description) {
        super();
        this.hasPk = hasPk;
        this.userType = userType;
        this.userName = userName;
        this.groupId = groupId;
        this.orgId = orgId;
        this.publicKey = publicKey;
        this.description = description;
        this.address = address;
    }
}