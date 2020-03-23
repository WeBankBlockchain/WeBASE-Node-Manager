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

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * receive contract info entity.
 */
@Data
public class DeployInputParam {
    /**
     * user's address, to identify signUserId
     */
    @NotBlank
    private String user;
    @NotNull
    private Integer groupId;
    @NotNull
    private Integer contractId;
    @NotBlank
    private String contractName;
    @NotBlank
    private String contractPath;
    @NotBlank
    private String contractSource;
    @NotBlank
    private String contractAbi;
    @NotBlank
    private String contractBin;
    @NotBlank
    private String bytecodeBin;
    private List<Object> constructorParams;
}

