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

import static com.webank.webase.node.mgr.base.code.ConstantCode.AGENCY_NAME_CONFIG_ERROR;
import static com.webank.webase.node.mgr.base.code.ConstantCode.INSERT_AGENCY_ERROR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.ValidateUtil;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.deploy.entity.TbAgency;
import com.webank.webase.node.mgr.deploy.entity.TbHost;
import com.webank.webase.node.mgr.deploy.mapper.TbAgencyMapper;
import com.webank.webase.node.mgr.deploy.mapper.TbHostMapper;
import com.webank.webase.node.mgr.front.FrontService;

import lombok.extern.log4j.Log4j2;

/**
 *
 */

@Log4j2
@Component
public class AgencyService {

    @Autowired private TbAgencyMapper tbAgencyMapper;
    @Autowired private TbHostMapper tbHostMapper;

    @Autowired private DeployShellService deployShellService;
    @Autowired private PathService pathService;
    @Autowired private HostService hostService;
    @Autowired private FrontService frontService;

    /**
     * @param chainId
     * @param agencyName
     * @return
     */
    public boolean exists(int chainId, String agencyName) {
        return this.tbAgencyMapper.getByChainIdAndAgencyName(chainId, agencyName) != null;
    }

    /**
     * Init an agency, generate config files(private key and crt files) and insert into db.
     *
     * @param agencyName
     * @param chainId
     * @param chainName
     * @return
     * @throws NodeMgrException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public TbAgency initAgencyIfNew(
            String agencyName,
            int chainId,
            String chainName,
            byte encryptType) throws NodeMgrException  {
        TbAgency agency = this.tbAgencyMapper.getByChainIdAndAgencyName(chainId, agencyName);
        // check agency name is new
        if (agency != null) {
            return agency;
        }

        if(! ValidateUtil.validateAgencyName(agencyName)){
            throw new NodeMgrException(AGENCY_NAME_CONFIG_ERROR);
        }
        // TODO use mv replace mv
        // delete agency config
        Path agencyRoot = this.pathService.getAgencyRoot(chainName, agencyName);
        if(Files.exists(agencyRoot)){
            log.warn("Exists agency:[{}:{}] config, delete first.",agencyName,agencyRoot.toAbsolutePath().toString());
            try {
                FileUtils.deleteDirectory(agencyRoot.toFile());
            } catch (IOException e) {
                throw new NodeMgrException(ConstantCode.DELETE_OLD_AGENCY_DIR_ERROR);
            }
        }

        // generate new agency config(private key and crt)
        ExecuteResult executeResult = this.deployShellService.execGenAgency(encryptType, chainName, agencyName);
        if (executeResult.failed()) {
            throw new NodeMgrException(ConstantCode.EXEC_GEN_AGENCY_ERROR);
        }

        // fix call transaction in the same class
        // insert agency to db
        return ((AgencyService) AopContext.currentProxy()).insert(agencyName, agencyName, chainId, chainName);
    }

    /**
     * Init an agency, generate config files(private key and crt files) and insert into db.
     *
     * @param agencyName
     * @param chainId
     * @param chainName
     * @return
     * @throws NodeMgrException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public TbAgency insertIfNew(
            String agencyName,
            int chainId,
            String chainName) throws NodeMgrException  {
        TbAgency agency = this.tbAgencyMapper.getByChainIdAndAgencyName(chainId, agencyName);
        // check agency name is new
        if (agency != null) {
            return agency;
        }

        // fix call transaction in the same class
        // insert agency to db
        return ((AgencyService) AopContext.currentProxy()).insert(agencyName, agencyName, chainId, chainName);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public TbAgency insert(String agencyName,
                           String agencyDesc,
                           int chainId,
                           String chainName) throws NodeMgrException {
        // TODO. params check
        TbAgency agency = TbAgency.init(agencyName, agencyDesc, chainId, chainName);

        if (tbAgencyMapper.insertSelective(agency) != 1 || agency.getId() <= 0) {
            throw new NodeMgrException(INSERT_AGENCY_ERROR);
        }
        return agency;
    }

    /**
     * @param chainId
     * @return
     */
    public List<TbAgency> selectAgencyListByChainId(int chainId) {
        // select all agencies by chainId
        List<TbAgency> tbAgencyList = tbAgencyMapper.selectByChainId(chainId);
        if (CollectionUtils.isEmpty(tbAgencyList)) {
            log.error("Chain:[{}] has no agency.", chainId);
            return Collections.emptyList();
        }
        return tbAgencyList;
    }

    /**
     *
     * @param deleteAgency
     * @param agencyId
     */
    @Transactional
    public void deleteAgencyWithNoNode(boolean deleteAgency, int agencyId){
        TbAgency agency = this.tbAgencyMapper.selectByPrimaryKey(agencyId);
        if (agency == null){
            log.warn("Agency:[{}] not exists.", agencyId);
            return;
        }

        List<TbHost> hostList = this.tbHostMapper.selectByAgencyId(agencyId);
        if (CollectionUtils.isEmpty(hostList)
                && deleteAgency) {
            this.tbAgencyMapper.deleteByPrimaryKey(agencyId);
            log.warn("Delete agency:[{}].", agencyId);
        }
    }

    /**
     *
     */
    @Transactional
    public void deleteByChainId(int chainId){
        log.info("Delete agency data by chain id:[{}].", chainId);
        // select agency list
        List<TbAgency> tbAgencyList = tbAgencyMapper.selectByChainId(chainId);

        if (CollectionUtils.isEmpty(tbAgencyList)) {
            log.warn("No agency in chain:[{}]", chainId);
            return;
        }

        for (TbAgency agency : tbAgencyList) {
            // delete front
            this.frontService.deleteFrontByAgencyId(agency.getId());

            // delete host
            this.hostService.deleteHostByAgencyId(agency.getChainName(),agency.getId());
        }

        this.tbAgencyMapper.deleteByChainId(chainId);
    }
}