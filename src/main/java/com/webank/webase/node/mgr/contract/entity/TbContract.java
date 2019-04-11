/**
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
package com.webank.webase.node.mgr.contract.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class of table tb_contract.
 */
@Data
@NoArgsConstructor
public class TbContract {

    private Integer contractId;
    private String contractName;
    private Integer groupId;
    private Integer chainIndex;
    private Integer contractType;
    private String contractSource;
    private String contractAbi;
    private String contractBin;
    private String bytecodeBin;
    private String contractAddress;
    private LocalDateTime deployTime;
    private String contractVersion;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;

    /**
     * init by contractId、contractName、groupId、contractVersion.
     */
    public TbContract(Integer contractId, String contractName, Integer groupId,
        String contractVersion) {
        super();
        this.contractId = contractId;
        this.contractName = contractName;
        this.groupId = groupId;
        this.contractVersion = contractVersion;
    }
}