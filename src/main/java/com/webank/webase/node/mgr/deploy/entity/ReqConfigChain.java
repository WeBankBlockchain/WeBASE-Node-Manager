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

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import lombok.Data;

/**
 * deploy batch node
 */
@Data
public class ReqConfigInit {

    /**
     * init chain config
     */
    @NotNull
    private String[] ipconf;

    @Positive
    private int tagId;

    private String chainName;

    private String webaseSignAddr;
    private String agencyName;

    /**
     * 0, 1
     */
    private int encryptType;
    /**
     * init host info list
     */
    private List<DeployNodeInfo> deployNodeInfoList;
}

