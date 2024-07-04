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
package com.webank.webase.node.mgr.group.entity;

import java.math.BigInteger;
import java.time.LocalDateTime;

import com.webank.webase.node.mgr.base.enums.GroupStatus;
import com.webank.webase.node.mgr.base.enums.GroupType;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class of table tb_group.
 */
@Data
@NoArgsConstructor
public class TbGroup {

    private String groupId;
    private String groupName;
    /**
     * 1-normal, 2-invalid
     * @related groupType sync group default 1, manual default 2
     */
    private Integer groupStatus;
    private Integer nodeCount;
    private BigInteger latestBlock = BigInteger.ZERO;
    private BigInteger transCount = BigInteger.ZERO;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
    private String description;
    private Integer groupType;
    private Integer encryptType;
    /**
     * group.x.genesis timestamp
     */
    private String groupTimestamp;
    /**
     * group peers nodeId
     */
    private String nodeIdList;

    private Integer chainId;
    private String chainName;

    private BigInteger userId = BigInteger.ZERO;
    private BigInteger deptId = BigInteger.ZERO;


    public TbGroup(String groupId, String groupName, Integer nodeCount, String description,
        GroupType groupType, GroupStatus groupStatus, Integer chainId, String chainName, Integer encryptType,
                   BigInteger userId, BigInteger deptId) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.nodeCount = nodeCount;
        this.description = description;
        this.groupType = groupType.getValue();
        this.groupStatus = groupStatus.getValue();
        this.chainId = chainId;
        this.chainName = chainName;
        this.encryptType = encryptType;
        this.userId = userId;
        this.deptId = deptId;
    }

}