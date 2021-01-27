/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.node.mgr.contract.entity;

import com.webank.webase.node.mgr.base.entity.BaseQueryParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class QueryCnsParam extends BaseQueryParam {
    private Integer groupId;
    private String contractPath;
    private String contractName;
    private String contractAddress;
    private String cnsName;
    private String version;

    public QueryCnsParam(int groupId, String contractAddress) {
        super();
        this.groupId = groupId;
        this.contractAddress = contractAddress;
    }
    
    public QueryCnsParam(int groupId, String cnsName, String version) {
        super();
        this.groupId = groupId;
        this.cnsName = cnsName;
        this.version = version;
    }
}
