/**
 * Copyright 2014-2020  the original author or authors.
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
package com.webank.webase.node.mgr.deploy.entity;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

import lombok.Data;

/**
 *
 */
@Data
public class ReqAdd {

    /**
     * Host runs new node, maybe a new host.
     */
    @NotBlank
    private String ip;

    @Positive
    private int groupId;

    /**
     * If host ip is new one, agency name should not be null.
     */
    private String agencyName="";

    /**
     * Count of new nodes , default is 1.
     */
    @Positive
    @Max(200)
    @Min(1)
    private int num = 1;

    /**
     * If agency name is a new one, chain name should not be null.
     */
    @NotBlank
    private String chainName = "default_chain";

    @NotBlank
    private String rootDirOnHost="/opt/fisco";
}
