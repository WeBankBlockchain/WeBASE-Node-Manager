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

import java.time.Duration;
import java.time.Instant;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.precompiled.entity.ConsensusHandle;
import com.webank.webase.node.mgr.precompiled.entity.CrudHandle;

import lombok.extern.log4j.Log4j2;

/**
 * Precompiled common controller
 * including management of CNS, node consensus status, CRUD
 */
@Log4j2
@RestController
@RequestMapping("precompiled")
public class PrecompiledController extends BaseController {
    @Autowired
    PrecompiledService precompiledService;

    /**
     * get cns list
     * 透传front的BaseResponse
     */
    @GetMapping("cns/list")
    public Object listCns(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam String contractNameAndVersion,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) {

        Instant startTime = Instant.now();
        log.info("start listCns startTime:{}", startTime.toEpochMilli());
        Object result = precompiledService.listCnsService(groupId, contractNameAndVersion, pageSize, pageNumber);

        log.info("end listCns useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(result));
        return result;
    }

    /**
     * get node list with consensus status.
     */
    @GetMapping("consensus/list")
    public Object getNodeList(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) {

        Instant startTime = Instant.now();
        log.info("start getNodeList startTime:{}", startTime.toEpochMilli());

        Object result = precompiledService.getNodeListService(groupId, pageSize, pageNumber);
        log.info("end getNodeList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(result));
        return result;
    }

    @PostMapping(value = "consensus")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object nodeManage(@RequestBody @Valid ConsensusHandle consensusHandle,
                                  BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start nodeManage startTime:{} consensusHandle:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(consensusHandle));

        Object res = precompiledService.nodeManageService(consensusHandle);

        log.info("end nodeManage useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));

        return res;
    }

    /**
     * crud control.
     */
    @PostMapping(value = "crud")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object crud(@RequestBody @Valid CrudHandle crudHandle,
                                   BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start crud startTime:{} crudHandle:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(crudHandle));

        Object res = precompiledService.crudService(crudHandle);

        log.info("end crud useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));

        return res;
    }
}
