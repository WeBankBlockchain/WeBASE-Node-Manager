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
package com.webank.webase.node.mgr.frontinterface.entity;

import java.util.List;
import lombok.Data;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;


/**
 * abiMeta interface parameter.
 */
@Data
public class PostAbiInfo {

    private Integer groupId;
    private String contractName;
    private String address;
    private List<AbiDefinition> abiInfo;
    private String contractBin;
}
