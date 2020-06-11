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

import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.HostStatusEnum;
import com.webank.webase.node.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.deploy.entity.TbAgency;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbAgencyMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbHostMapper;

import lombok.extern.log4j.Log4j2;

/**
 *
 */

@Log4j2
@Component
public class HostService {

    @Autowired private TbHostMapper tbHostMapper;
    @Autowired private TbAgencyMapper tbAgencyMapper;
    @Autowired private AgencyService agencyService;
    @Autowired private PathService pathService;
    @Autowired private ConfigService configService;
    @Autowired private DeployShellService deployShellService;

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
                         int sshPort,
                         String rootDir,
                         HostStatusEnum hostStatusEnum) throws NodeMgrException {

        // TODO. params check

        TbHost host = TbHost.init(agencyId, agencyName, ip, sshUser, sshPort, rootDir, hostStatusEnum);

        if ( tbHostMapper.insertSelective(host) != 1 || host.getId() <= 0) {
            throw new NodeMgrException(ConstantCode.INSERT_HOST_ERROR);
        }
        return host;
    }

    /**
     *
     * @param chainId
     * @return
     */
    public List<TbHost> selectHostListByChainId(int chainId){
        // select all agencies by chainId
        List<TbAgency> tbAgencyList = this.agencyService.selectAgencyListByChainId(chainId);

        // select all hosts by all agencies
        List<TbHost> tbHostList = tbAgencyList.stream()
                .map((agency) -> tbHostMapper.selectByAgencyId(agency.getId()))
                .filter((host) -> host != null)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tbHostList)) {
            log.error("Chain:[{}] has no host.", chainId);
            return Collections.emptyList();
        }
        return tbHostList;
    }

    /**
     * Init a host, generate sdk files(crt files and node.[key,crt]) and insert into db.
     *
     *
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public TbHost initHost(
            byte encryptType,
            String chainName,
            String rootDirOnHost,
            String ip,
            int agencyId,
            String agencyName) throws NodeMgrException {
        // new host, generate sdk dir first
        Path sdkPath = this.pathService.getSdk(chainName, ip);

        // call shell to generate new node config(private key and crt)
        ExecuteResult executeResult = this.deployShellService.execGenNode(
                encryptType, chainName, agencyName, sdkPath.toAbsolutePath().toString());
        if (executeResult.failed()) {
             throw new NodeMgrException(ConstantCode.EXEC_GEN_SDK_ERROR);
        }

        // init sdk dir
        this.configService.initSdkDir(encryptType, sdkPath);

        // scp sdk to remote
        String src = String.format("%s", sdkPath.toAbsolutePath().toString());
        String dst = PathService.getChainRootOnHost(rootDirOnHost, chainName);

        log.info("Send files from:[{}] to:[{}@{}#{}:{}].", src, SSH_DEFAULT_USER, ip, SSH_DEFAULT_PORT, dst);
        executeResult = this.deployShellService.scp(ScpTypeEnum.UP, ip, src, dst);
        if (executeResult.failed()) {
            throw new NodeMgrException(ConstantCode.SEND_SDK_FILES_ERROR);
        }

        // insert host into db
        return ((HostService) AopContext.currentProxy()).insert(agencyId, agencyName, ip, chainName);
    }

}