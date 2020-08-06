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
package com.webank.webase.node.mgr.contract.entity;

import com.webank.webase.node.mgr.base.entity.BaseQueryParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ContractParam extends BaseQueryParam {
    private Integer groupId;
    private Integer contractId;
    private String contractName;
    private String contractPath;
    private String contractVersion;
    private String contractAddress;
    private Integer contractStatus;
    private Integer contractType;
    private String partOfBytecodeBin;

    /**
     * init by contractId.
     */
    public ContractParam(int contractId,int groupId) {
        super();
        this.contractId = contractId;
        this.groupId = groupId;
    }

    /**
     * init by contractName、contractPath.
     */
    public ContractParam(int groupId, String contractPath,String contractName) {
        super();
        this.groupId = groupId;
        this.contractName = contractName;
        this.contractPath = contractPath;
    }

}