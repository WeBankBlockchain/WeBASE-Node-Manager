/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.deploy.service;

import static com.webank.webase.node.mgr.base.code.ConstantCode.INSERT_AGENCY_ERROR;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.deploy.entity.TbAgency;
import com.webank.webase.node.mgr.deploy.mapper.TbAgencyMapper;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
@Component
public class AgencyService {

    @Autowired private TbAgencyMapper tbAgencyMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    public TbAgency insert(String agencyName,
                           String agencyDesc,
                           int chainId,
                           String chainName) throws NodeMgrException {
        // TODO. params check
        TbAgency agency = TbAgency.init( agencyName, agencyDesc, chainId, chainName);

        if (tbAgencyMapper.insertSelective(agency) != 1 || agency.getId() <= 0) {
            throw new NodeMgrException(INSERT_AGENCY_ERROR);
        }
        return agency;
    }
}