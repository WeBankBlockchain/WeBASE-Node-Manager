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

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.network.NetworkService;
import com.webank.webase.node.mgr.organization.OrganizationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * services for network_organization_mapping data.
 */
@Log4j2
@Service
public class NetworkOrgMapService {

    @Autowired
    private NetworkService networkService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private NetworkOrgMapMapper networkOrgMapMapper;

    /**
     * add network org map.
     */
    public Integer addNetworkOrgMap(Integer networkId, Integer orgId) throws NodeMgrException {
        log.debug("start addNetworkOrgMap networkId:{} orgId:{}", networkId, orgId);

        // check network id
        networkService.checkNetworkId(networkId);
        // check org id
        organizationService.checkOrganizationId(orgId);

        Integer count = countOfNetworkOrgMap(null, networkId, orgId);

        if (count != null && count > 0) {
            log.info("network_organization_mapping already exists.  networkId:{} orgId:{} count:{}",
                networkId, orgId, count);
            throw new NodeMgrException(ConstantCode.NET_ORG_MAP_EXISTS);
        }

        // add row
        TbNetworkOrgMap dbParam = new TbNetworkOrgMap(networkId, orgId);
        Integer affectRow = networkOrgMapMapper.addNetworkOrgMapRow(dbParam);
        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_network_org_map");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        Integer mapId = dbParam.getMapId();
        log.debug("end addNetworkOrgMap mapId:{}", mapId);
        return mapId;
    }

    /**
     * delete network_organization_mapping info.
     */
    public void deleteByOrgID(Integer orgId) throws NodeMgrException {
        log.debug("start deleteByOrgID orgId:{} ", orgId);
        Integer mapCount = countOfNetworkOrgMap(null, null, orgId);
        if (mapCount == null || mapCount == 0) {
            log.warn("fail deleteByOrgID orgID:{} mapCount:{}", orgId, mapCount);
            throw new NodeMgrException(ConstantCode.NET_ORG_MAP_NOT_EXISTS);
        }

        Integer affectRow = 0;
        try {
            affectRow = networkOrgMapMapper.deleteByOrgID(orgId);
        } catch (RuntimeException ex) {
            log.error("fail deleteByOrgID. orgId:{}", orgId, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_network_org_map");
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end deleteByOrgID");
    }

    /**
     * qurey count of network_organization_mapping.
     */
    public Integer countOfNetworkOrgMap(Integer mapId, Integer networkId, Integer orgId)
        throws NodeMgrException {
        log.debug("start countOfNetworkOrgMap   mapId:{} networkId:{} orgId:{}", mapId, networkId,
            orgId);
        try {
            Integer networkOrgMapCount = networkOrgMapMapper
                .countOfNetworkOrgMap(mapId, networkId, orgId);
            log.debug(
                "end countOfNetworkOrgMap   mapId:{} networkId:{} orgId:{}  networkOrgMapCount:{}",
                mapId, networkId, orgId,
                networkOrgMapCount);
            return networkOrgMapCount;
        } catch (RuntimeException ex) {
            log.error("fail countOfNetworkOrgMap  mapId:{} networkId:{} orgId:{} ", mapId,
                networkId, orgId, ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }
}
