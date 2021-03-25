/**
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.node.mgr.contract.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class of table tb_contract.
 */
@Data
@NoArgsConstructor
public class TbCns {

    private Integer id;
    private Integer groupId;
    private String contractPath;
    private String contractName;
    private String cnsName;
    private String version;
    private String contractAddress;
    private String contractAbi;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;

    public TbCns(Integer groupId, String contractName, String version, String contractAddress) {
        this.groupId = groupId;
        this.contractName = contractName;
        this.version = version;
        this.contractAddress = contractAddress;
    }

}
