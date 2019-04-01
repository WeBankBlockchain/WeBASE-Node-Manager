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
package com.webank.webase.node.mgr.grouporgmap;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * group_org_map data interface.
 */
@Repository
public interface NetworkOrgMapMapper {

    /**
     * Add new group_org_map data.
     */
    Integer addNetworkOrgMapRow(TbNetworkOrgMap tbNetworkOrgMap);

    /**
     * Query the number of group_org_map according to some conditions.
     */
    Integer countOfNetworkOrgMap(@Param("mapId") Integer mapId,
        @Param("groupId") Integer groupId, @Param("orgId") Integer orgId);

    /**
     * delete group_org_map by organization id.
     */
    Integer deleteByOrgID(@Param("orgId") Integer orgId);
}
