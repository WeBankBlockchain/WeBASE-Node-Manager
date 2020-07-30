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

import javax.validation.constraints.NotBlank;

import lombok.Data;

/**
 *
 */
@Data
public class ReqNodeOption {

    /**
     * nodeId.
     */
    @NotBlank
    private String nodeId;

    /**
     *  Whether delete node when trying to delete the last node on a host.
     */
    private boolean deleteHost;

    /**
     *  Whether delete agency when trying to delete the last node on a host.
     *
     */
    private boolean deleteAgency;

}
