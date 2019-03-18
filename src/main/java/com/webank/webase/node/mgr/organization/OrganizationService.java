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

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.entity.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.network.NetworkService;
import com.webank.webase.node.mgr.networkorgmap.NetworkOrgMapService;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * services for organization data.
 */
@Log4j2
@Service
public class OrganizationService {

    @Autowired
    private OrganizationMapper organizationMapper;
    @Autowired
    private NetworkService networkService;
    @Autowired
    private NetworkOrgMapService networkOrgMapService;

    /**
     * add new organization data.
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public Integer addOrganizationInfo(TbOrganization org) throws NodeMgrException {
        log.debug("start addOrganizationInfo TbOrganization:{}", JSON.toJSONString(org));

        // check network id
        networkService.checkNetworkId(org.getNetworkId());

        // check name of organization
        TbOrganization dbRow = queryByOrgName(org.getNetworkId(), org.getOrgName());
        if (dbRow != null) {
            log.info(
                "fail addOrganizationInfo:organization name already exists. orgId:{} qureyParam:{}",
                dbRow.getOrgId(), JSON.toJSONString(org));
            return dbRow.getOrgId();
        }

        // add organization row
        Integer affectRow = 0;
        try {
            affectRow = organizationMapper.addOrganizationRow(org);
        } catch (RuntimeException ex) {
            log.error("fail addOrganizationInfo:organization", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_organization");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        Integer orgId = org.getOrgId();

        // add network_organization_mapping
        Integer mapId = 0;
        try {
            mapId = networkOrgMapService.addNetworkOrgMap(org.getNetworkId(), orgId);
        } catch (RuntimeException ex) {
            log.error("fail addNetworkOrgMap", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
        if (mapId == null || mapId == 0) {
            log.debug("fail addNetworkOrgMap fail mapId:{}", mapId);
            throw new NodeMgrException(ConstantCode.SYSTEM_EXCEPTION);
        }
        log.debug("end addOrganizationInfo orgId:{} mapId:{}", orgId, mapId);
        return orgId;
    }

    /**
     * query organization list by page.
     */
    public List<TbOrganization> qureyOrganizationList(Integer networkId, Integer pageNumber,
        Integer pageSize, String orgName)
        throws NodeMgrException {
        log.debug("start qureyOrganizationList networkId:{} pageNumber:{} pageSize:{} orgName:{}",
            networkId, pageNumber, pageSize, orgName);

        try {
            // qurey organization list
            Integer start = Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize)
                .orElse(null);
            List<TbOrganization> listOfOrganization = organizationMapper
                .listOfOrganization(networkId, start, pageSize, orgName);
            log.debug("end qureyOrganizationList listOfOrganization:{}",
                JSON.toJSONString(listOfOrganization));
            return listOfOrganization;
        } catch (RuntimeException ex) {
            log.error(
                "fail qureyOrganizationList. networkId:{} pageNumber:{} pageSize:{} orgName:{}",
                networkId, pageNumber, pageSize, orgName, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * update organization info.
     */
    public void updateOrtanization(Organization organization) throws NodeMgrException {
        log.debug("start updateOrganization Organization:{}", JSON.toJSONString(organization));
        Integer orgId = Optional.ofNullable(organization).map(org -> org.getOrgId())
            .orElseThrow(() -> new NodeMgrException(ConstantCode.INVALID_PARAM_INFO));
        String description = Optional.ofNullable(organization).map(org -> org.getDescription())
            .orElseThrow(() -> new NodeMgrException(ConstantCode.INVALID_PARAM_INFO));

        // check organization id
        checkOrganizationId(orgId);

        TbOrganization updateParam = new TbOrganization();
        updateParam.setOrgId(orgId);
        updateParam.setDescription(description);
        Integer affectRow = 0;
        try {
            affectRow = organizationMapper.updateOrganization(updateParam);
        } catch (RuntimeException ex) {
            log.error("fail updateOrtanization. orgId:{} description:{}", orgId, description, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_organization");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
        log.debug("end updateOrtanization");
    }

    /**
     * qurey count of organization.
     */
    public Integer countOfOrganization(Integer networkId, Integer orgId, String orgName,
        Integer orgType) throws NodeMgrException {
        log.debug("start countOfOrganization networkId:{} orgId:{} orgName:{} orgType:{}",
            networkId, orgId, orgName, orgType);
        try {
            Integer organizationCount = organizationMapper
                .countOfOrganization(networkId, orgId, orgName, orgType);
            log.debug(
                "end countOfOrganization networkId:{} orgId:{} orgName:{} orgType:{}"
                    + " organizationCount:{}",
                networkId, orgId, orgName, orgType,
                organizationCount);
            return organizationCount;
        } catch (RuntimeException ex) {
            log.error("fail countOfOrganization . networkId:{} orgId:{} orgName:{} "
                    + "orgType:{}",
                networkId, orgId, orgName, orgType, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * count by orgName.
     */
    public Integer countOfOrganization(Integer networkId, String orgName) throws NodeMgrException {
        return countOfOrganization(networkId, null, orgName, null);
    }

    /**
     * count by orgType.
     */
    public Integer countOfOrganization(Integer networkId, Integer orgType) throws NodeMgrException {
        return countOfOrganization(networkId, null, null, orgType);
    }

    /**
     * Check the validity of the organization id.
     */
    public void checkOrganizationId(Integer organizationId) throws NodeMgrException {
        log.debug("start checkOrganizationId organizationId:{}", organizationId);

        if (organizationId == null) {
            log.error("fail checkOrganizationId organizationId is null");
            throw new NodeMgrException(ConstantCode.ORG_ID_NULL);
        }

        Integer orgCount = countByOrgId(organizationId);
        log.debug("checkOrganizationId organizationId:{} orgCount:{}", organizationId, orgCount);
        if (orgCount == null || orgCount == 0) {
            throw new NodeMgrException(ConstantCode.INVALID_ORG_ID);
        }
        log.debug("end checkOrganizationId");
    }

    /**
     * qurey organization info.
     */
    public TbOrganization queryOrganization(Integer networkId, Integer orgType, Integer orgId,
        String orgName) throws NodeMgrException {
        log.debug("start queryOrganization networkId:{} orgType:{} orgId:{} orgName:{}",
            networkId, orgType, orgId, orgName);
        try {
            TbOrganization organizationRow = organizationMapper
                .queryOrganization(networkId, orgType, orgId, orgName);
            log.debug(
                "end queryOrganization networkId:{} orgType:{} orgId:{} TbOrganization:{} "
                    + "orgName:{}",networkId, orgType, orgId, orgName,
                JSON.toJSONString(organizationRow));
            return organizationRow;
        } catch (RuntimeException ex) {
            log.error("fail queryOrganization . networkId:{} orgType:{} orgId:{} orgName:{}",
                networkId, orgType, orgId, orgName, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query organization by networkId、orgType.
     */
    public TbOrganization queryOrganization(Integer networkId, Integer orgType)
        throws NodeMgrException {
        return queryOrganization(networkId, orgType, null, null);
    }

    public TbOrganization queryByOrgName(Integer networkId, String orgName)
        throws NodeMgrException {
        return queryOrganization(networkId, null, null, orgName);
    }

    /**
     * query count of organization by organization id.
     */
    public Integer countByOrgId(Integer orgId) throws NodeMgrException {
        log.debug("start countByOrgId orgId:{}", orgId);
        try {
            Integer organizationCount = organizationMapper.countByOrgId(orgId);
            log.debug("end countByOrgId orgId:{} organizationCount:{}", orgId, organizationCount);
            return organizationCount;
        } catch (RuntimeException ex) {
            log.error("fail countByOrgId . orgId:{}", orgId, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

}
