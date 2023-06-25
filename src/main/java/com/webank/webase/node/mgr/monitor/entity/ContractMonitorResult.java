/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.node.mgr.monitor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * contract monitor result info.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractMonitorResult {
    private String contractName;
    private String contractAddress;
    private String interfaceName;
    private Integer transType;
    private Integer transUnusualType;

    public ContractMonitorResult(String contractName, String contractAddress, Integer transType, Integer transUnusualType) {
        this.contractName = contractName;
        this.contractAddress = contractAddress;
        this.transType = transType;
        this.transUnusualType = transUnusualType;
    }
}
