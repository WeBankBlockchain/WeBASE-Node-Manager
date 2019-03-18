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
package com.webank.webase.node.mgr.networkorgmap;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * Entity class of table tb_network_org_map.
 */
@Data
public class TbNetworkOrgMap {

    private Integer mapId;
    private Integer networkId;
    private Integer orgId;
    private Integer mapStatus;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;

    public TbNetworkOrgMap() {
        super();
    }

    /**
     * init by networkId、orgId.
     */
    public TbNetworkOrgMap(Integer networkId, Integer orgId) {
        super();
        this.networkId = networkId;
        this.orgId = orgId;
    }

}
