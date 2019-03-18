/*
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
package com.webank.webase.node.mgr.base.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseQueryParam {

    private Integer networkId;
    private Integer start;
    private Integer pageSize;
    private String flagSortedByTime;

    public BaseQueryParam(Integer networkId) {
        super();
        this.networkId = networkId;
    }

    /**
     * init BaseQueryParam by networkId、start、pageSize.
     */
    public BaseQueryParam(Integer networkId, Integer start, Integer pageSize) {
        super();
        this.networkId = networkId;
        this.start = start;
        this.pageSize = pageSize;
    }

    /**
     * init BaseQueryParam by start、pageSize.
     */
    public BaseQueryParam(Integer start, Integer pageSize) {
        super();
        this.start = start;
        this.pageSize = pageSize;
    }

}
