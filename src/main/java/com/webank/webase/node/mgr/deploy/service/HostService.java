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

import static com.webank.webase.node.mgr.base.properties.ConstantProperties.SSH_DEFAULT_PORT;
import static com.webank.webase.node.mgr.base.properties.ConstantProperties.SSH_DEFAULT_USER;

import java.util.Date;

import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.HostStatusEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbHostMapper;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
@Component
public class HostService {

    @Autowired private TbHostMapper tbHostMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateStatus(int hostId, HostStatusEnum newStatus) throws NodeMgrException {
        TbHost newHost = new TbHost();
        newHost.setId(hostId);
        newHost.setStatus(newStatus.getId());
        newHost.setModifyTime(new Date());
        return tbHostMapper.updateByPrimaryKeySelective(newHost) == 1;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public TbHost insert(int agencyId,
                         String agencyName,
                         String ip,
                         String rootDir) throws NodeMgrException {

        // fix call transaction in the same class
        return ((HostService) AopContext.currentProxy())
                .insert(agencyId, agencyName, ip, SSH_DEFAULT_USER, SSH_DEFAULT_PORT, rootDir, HostStatusEnum.ADDED);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public TbHost insert(int agencyId,
                         String agencyName,
                         String ip,
                         String sshUser,
                         short sshPort,
                         String rootDir,
                         HostStatusEnum hostStatusEnum) throws NodeMgrException {

        // TODO. params check

        TbHost host = TbHost.init(agencyId, agencyName, ip, sshUser, sshPort, rootDir, hostStatusEnum);

        if ( tbHostMapper.insertSelective(host) != 1 || host.getId() <= 0) {
            throw new NodeMgrException(ConstantCode.INSERT_HOST_ERROR);
        }
        return host;
    }

}