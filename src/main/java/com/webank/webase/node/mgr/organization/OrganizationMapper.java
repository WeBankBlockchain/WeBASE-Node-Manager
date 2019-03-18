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
package com.webank.webase.node.mgr.organization;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * mapper for organization data.
 */
@Repository
public interface OrganizationMapper {

    /**
     * Add new organization data.
     */
    Integer addOrganizationRow(TbOrganization tbOrganization);

    /**
     * Query the number of organization according to some conditions.
     */
    Integer countOfOrganization(@Param("networkId") Integer networkId,
        @Param("orgId") Integer orgId, @Param("orgName") String orgName,
        @Param("orgType") Integer orgType);

    /**
     * Query organization list according to some conditions.
     */
    List<TbOrganization> listOfOrganization(@Param("networkId") Integer networkId,
        @Param("start") Integer start, @Param("pageSize") Integer pageSize,
        @Param("orgName") String orgName);

    /**
     * update organization row.
     */
    Integer updateOrganization(TbOrganization tbOrganization);

    /**
     * query organization row.
     */
    TbOrganization queryOrganization(@Param("networkId") Integer networkId,
        @Param("orgType") Integer orgType, @Param("orgId") Integer orgId,
        @Param("orgName") String orgName);

    /**
     * query count of organization by organization id.
     */
    Integer countByOrgId(@Param("orgId") Integer orgId);
}