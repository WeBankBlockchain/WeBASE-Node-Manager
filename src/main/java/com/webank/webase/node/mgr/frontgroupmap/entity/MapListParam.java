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
package com.webank.webase.node.mgr.frontgroupmap.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MapListParam {
    private Integer mapId;
    private Integer frontId;
    private Integer groupId;
    private Integer status;
    private Integer type;

    public MapListParam(Integer frontId, Integer groupId) {
        this.frontId = frontId;
        this.groupId = groupId;
    }

    public MapListParam(Integer frontId, Integer groupId, Integer status, Integer type) {
        this.frontId = frontId;
        this.groupId = groupId;
        this.status = status;
        this.type = type;
    }
}
