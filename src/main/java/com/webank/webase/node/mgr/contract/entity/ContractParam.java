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
    private String groupId;
    private Integer contractId;
    private String contractName;
    /**
     * v1.4.2 query by contract path
     */
    private String contractPath;
    private String contractVersion;
    private String account;
    private String contractAddress;
    private Integer contractStatus;
    private Integer contractType;
    private String partOfBytecodeBin;
    private String deployAddress;

    /**
     * init by contractId.
     */
    public ContractParam(int contractId, String groupId) {
        super();
        this.contractId = contractId;
        this.groupId = groupId;
    }

    /**
     * init by contractName、contractPath.
     */
    public ContractParam(String groupId, String contractPath, String contractName, String account) {
        super();
        this.groupId = groupId;
        this.contractName = contractName;
        this.contractPath = contractPath;
        this.account = account;
    }


    /**
     * init by groupId, account, contractPath.
     */
    public ContractParam(String groupId, String account, String contractPath) {
        super();
        this.groupId = groupId;
        this.account = account;
        this.contractPath = contractPath;
    }

}