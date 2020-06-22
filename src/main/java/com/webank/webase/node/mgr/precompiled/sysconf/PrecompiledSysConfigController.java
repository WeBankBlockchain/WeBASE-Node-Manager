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
package com.webank.webase.node.mgr.precompiled.sysconf;

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

import lombok.extern.log4j.Log4j2;

/**
 * System config value controller
 * manage tx_count and gas_limit
 */
@Log4j2
@RestController
@RequestMapping("sys")
public class PrecompiledSysConfigController extends BaseController {

    @Autowired
    PrecompiledSysConfigService precompiledSysConfigService;
    /**
     * get system config list
     * 透传front的BaseResponse
     */
    @GetMapping("config/list")
    public Object getSysConfigList(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) {

        Instant startTime = Instant.now();
        log.info("start getSysConfigList startTime:{}", startTime.toEpochMilli());

        Object result = precompiledSysConfigService.getSysConfigListService(groupId, pageSize, pageNumber);

        log.info("end getSysConfigList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(result));
        return result;
    }

    /**
     * set system config by key.
     */
    @PostMapping(value = "config")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object setSysConfigByKeyService(@RequestBody @Valid SysConfigParam sysConfigParam,
                                  BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start setSysConfigByKeyService startTime:{} sysConfigParam:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(sysConfigParam));

        Object res = precompiledSysConfigService.setSysConfigByKeyService(sysConfigParam);

        log.info("end setSysConfigByKeyService useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));

        return res;
    }
}
