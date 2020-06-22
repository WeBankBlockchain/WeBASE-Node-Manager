/**
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.node.mgr.precompiled;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.HttpRequestTools;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.precompiled.entity.ConsensusHandle;
import com.webank.webase.node.mgr.precompiled.entity.CrudHandle;
import com.webank.webase.node.mgr.user.UserService;

import lombok.extern.log4j.Log4j2;

/**
 * Precompiled common service
 * including management of CNS, node consensus status, CRUD
 */
@Log4j2
@Service
public class PrecompiledService {

    @Autowired
    private FrontRestTools frontRestTools;
    @Autowired
    private FrontInterfaceService frontInterfaceService;
    @Autowired
    private UserService userService;

    /**
     * get cns list /{groupId}/{pathValue} /a?groupId=xx
     */
    public Object listCnsService(int groupId, String contractNameAndVersion, int pageSize, int pageNumber) {
        log.debug("start listCnsService. groupId:{}, contractNameAndVersion:{}" + groupId + contractNameAndVersion);
        String uri;
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("contractNameAndVersion", contractNameAndVersion);
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));
        uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_CNS_LIST, map);


        Object frontRsp = frontRestTools.getForEntity(groupId, uri, Object.class);
        log.debug("end listCnsService. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     * get node list with consensus status
     */
    public Object getNodeListService(int groupId, int pageSize, int pageNumber) {
        log.debug("start getNodeListService. groupId:{}", groupId);
        String uri;
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));
        uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_CONSENSUS_LIST, map);
        Object frontRsp = null;
        frontRsp = frontRestTools.getForEntity(groupId, uri, Object.class);
        log.debug("end getNodeListService. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }


    /**
     * post node manage consensus status
     */

    public Object nodeManageService(ConsensusHandle consensusHandle) {
        log.debug("start nodeManageService. consensusHandle:{}", JsonTools.toJSONString(consensusHandle));
        if (Objects.isNull(consensusHandle)) {
            log.error("fail nodeManageService. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }
        int groupId = consensusHandle.getGroupId();
        String signUserId = userService.getSignUserIdByAddress(groupId, consensusHandle.getFromAddress());
        consensusHandle.setSignUserId(signUserId);
        Object frontRsp = frontRestTools.postForEntity(
                groupId, FrontRestTools.URI_CONSENSUS,
                consensusHandle, Object.class);
        log.debug("end nodeManageService. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     *  post CRUD opperation
     */

    public Object crudService(CrudHandle crudHandle) {
        log.debug("start crudService. crudHandle:{}", JsonTools.toJSONString(crudHandle));
        if (Objects.isNull(crudHandle)) {
            log.error("fail crudService. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }
        int groupId = crudHandle.getGroupId();
        String signUserId = userService.getSignUserIdByAddress(groupId, crudHandle.getFromAddress());
        crudHandle.setSignUserId(signUserId);
        Object frontRsp = frontRestTools.postForEntity(
                groupId, FrontRestTools.URI_CRUD,
                crudHandle, Object.class);
        log.debug("end crudService. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }
}
