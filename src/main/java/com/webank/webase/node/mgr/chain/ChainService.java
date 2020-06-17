/**
 * Copyright 2014-2020  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.node.mgr.chain;

import static com.webank.webase.node.mgr.frontinterface.FrontRestTools.URI_CHAIN;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.node.mgr.base.enums.RunTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.base.tools.SshTools;
import com.webank.webase.node.mgr.deploy.entity.TbChain;
import com.webank.webase.node.mgr.deploy.mapper.TbChainMapper;
import com.webank.webase.node.mgr.deploy.service.AgencyService;
import com.webank.webase.node.mgr.deploy.service.PathService;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.group.GroupService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ChainService {

    @Autowired private TbChainMapper tbChainMapper;

    @Autowired
    private ConstantProperties cproperties;
    @Autowired
    private FrontService frontService;
    @Autowired
    private AgencyService agencyService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private RestTemplate genericRestTemplate;
    @Autowired
    private PathService pathService;

    /**
     * get chain info.
     */
    public Object getChainMonitorInfo(Integer frontId, LocalDateTime beginDate,
                                      LocalDateTime endDate, LocalDateTime contrastBeginDate,
                                      LocalDateTime contrastEndDate, int gap, int groupId) {
        log.debug(
                "start getChainMonitorInfo.  frontId:{} beginDate:{} endDate:{}"
                        + " contrastBeginDate:{} contrastEndDate:{} gap:{} groupId:{}",
                frontId, beginDate, endDate, contrastBeginDate, contrastEndDate, gap, groupId);

        // request param to str
        List<Object> valueList = Arrays
                .asList(beginDate, endDate, contrastBeginDate, contrastEndDate, gap, groupId);
        List<String> nameList = Arrays
                .asList("beginDate", "endDate", "contrastBeginDate", "contrastEndDate", "gap",
                        "groupId");

        String chainUrlParam = NodeMgrTools.convertUrlParam(nameList, valueList);

        // query by front Id
        TbFront tbFront = frontService.getById(frontId);
        if (tbFront == null) {
            throw new NodeMgrException(ConstantCode.INVALID_FRONT_ID);
        }

        // request url
        String url = String
                .format(cproperties.getFrontUrl(), tbFront.getFrontIp(), tbFront.getFrontPort(),
                        URI_CHAIN);
        url = url + "?" + chainUrlParam;
        log.info("getChainMonitorInfo request url:{}", url);

        Object rspObj = genericRestTemplate.getForObject(url, Object.class);
        log.debug("end getChainMonitorInfo. rspObj:{}", JSON.toJSONString(rspObj));
        return rspObj;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public TbChain insert(String chainName,
                          String chainDesc,
                          String version,
                          byte encryptType,
                          ChainStatusEnum status,
                          String rootDirOnHost,
                          RunTypeEnum runTypeEnum,
                          String webaseSignAddr
    ) throws NodeMgrException {
        // TODO. params check

        TbChain chain = TbChain.init(chainName, chainDesc, version, encryptType, status, rootDirOnHost, runTypeEnum,webaseSignAddr);

        if (tbChainMapper.insertSelective(chain) != 1 || chain.getId() <= 0) {
            throw new NodeMgrException(ConstantCode.INSERT_CHAIN_ERROR);
        }
        return chain;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateStatus(int chainId, ChainStatusEnum newStatus) {
        TbChain newChain = new TbChain();
        newChain.setId(chainId);
        newChain.setChainStatus(newStatus.getId());
        newChain.setModifyTime(new Date());
        return this.tbChainMapper.updateByPrimaryKeySelective(newChain) == 1;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean upgrade(TbChain chain , String newTagVersion ) {
        log.info("Upgrade chain:[{}] from version:[{}] to version:[{}].",
                chain.getChainName(), chain.getVersion(), newTagVersion);

        TbChain newChain = new TbChain();
        newChain.setId(chain.getId());
        newChain.setVersion(newTagVersion);
        newChain.setModifyTime(new Date());
        int count = this.tbChainMapper.updateByPrimaryKeySelective(newChain);

        if(count != 1){
            throw new NodeMgrException(ConstantCode.UPDATE_CHAIN_WITH_NEW_VERSION_ERROR);
        }

        // restart front
        log.info("Upgrade front:[{}] to version:[{}].",chain.getVersion() , newTagVersion);
        return this.frontService.upgrade(chain.getId(),newTagVersion);
    }


    /**
     *
     * @param ip
     * @param rootDirOnHost
     * @param chainName
     */
    public static void mvChainOnRemote(String ip,String rootDirOnHost,String chainName){
        // create /opt/fisco/deleted-tmp/ as a parent dir
        String deleteRootOnHost = PathService.getDeletedRootOnHost(rootDirOnHost);
        SshTools.createDirOnRemote(ip, deleteRootOnHost);

        // like /opt/fisco/default_chain
        String src_chainRootOnHost = PathService.getChainRootOnHost(rootDirOnHost, chainName);
        // move to /opt/fisco/deleted-tmp/default_chain-yyyyMMdd_HHmmss
        String dst_chainDeletedRootOnHost = PathService.getChainDeletedRootOnHost(rootDirOnHost, chainName);

        SshTools.mvDirOnRemote(ip,src_chainRootOnHost,dst_chainDeletedRootOnHost);
    }

    /**
     *
     * @param chainName
     */
    @Transactional
    public void delete(String chainName) throws IOException {
        TbChain chain = tbChainMapper.getByChainName(chainName);
        if (chain == null) {
            throw new NodeMgrException(ConstantCode.CHAIN_NAME_NOT_EXISTS_ERROR);
        }

        // delete agency
        this.agencyService.deleteByChainId(chain.getId());

        // delete group
        this.groupService.deleteGroupByChainId(chain.getId());

        log.info("Delete chain data by chain id:[{}].", chain.getId());
        this.tbChainMapper.deleteByPrimaryKey(chain.getId());

        log.info("Delete chain:[{}] config files", chainName);
        this.pathService.deleteChain(chainName);
    }

    /**
     *
     * @param chainName
     * @param ipConf
     * @param tagId
     * @param rootDirOnHost
     */
    public void init(String chainName,
                     String[] ipConf,
                     int tagId,
                     String rootDirOnHost){


    }
}
