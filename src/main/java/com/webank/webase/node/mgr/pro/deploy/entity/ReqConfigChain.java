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
package com.webank.webase.node.mgr.pro.deploy.entity;

import java.util.List;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * deploy batch node
 */
@Data
public class ReqConfigChain {

    /**
     * init chain config
     */
    @NotNull
    private String[] ipconf;

    /**
     * ex: v2.7.0, v2.7.1 etc.
     */
    String imageTag;

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

