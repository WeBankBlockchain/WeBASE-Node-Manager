/*
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
package com.webank.webase.node.mgr.precntauth.precompiled.sysconf;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.webank.common.log.annotation.Log;
import com.webank.common.log.enums.BusinessType;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.precntauth.precompiled.sysconf.entity.ReqSetSysConfigInfo;
import com.webank.webase.node.mgr.tools.JsonTools;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;

/**
 * System config controller
 * manage tx_count and gas_limit
 */
//@Api(value = "precntauth/precompiled/sys", tags = "precntauth precompiled controller")
@Tag(name="配置管理")
@Slf4j
@RestController
@RequestMapping(value = "precntauth/precompiled/sys")
@SaCheckPermission("bcos3:sys:configManagement")
public class SysConfigController extends BaseController {

    @Autowired
    private SysConfigServiceInWebase SysConfigServiceInWebase;

    /**
     * get system config list 透传front的BaseResponse
     */
//    @ApiImplicitParam(name = "groupId", value = "groupId info", required = true)

    @GetMapping("config/list")
    public BaseResponse getSysConfigList(@RequestParam(defaultValue = "group") String groupId) {
        Instant startTime = Instant.now();
        log.info("start getSysConfigList startTime:{}", startTime.toEpochMilli());
        Object result = SysConfigServiceInWebase.querySysConfigByGroupId(groupId);
        log.info("end getSysConfigList useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(result));
        return new BaseResponse(ConstantCode.SUCCESS, result);
    }

    /**
     * set system config by key.
     */
//    @ApiOperation(value = "setSysConfigValueByKey", notes = "set system config value by key")
//    @ApiImplicitParam(name = "reqSetSysConfigInfo", value = "system config info", required = true,
//        dataType = "ReqSetSysConfigInfo")
    @Log(title = "BCOS3/系统管理/配置管理", businessType = BusinessType.UPDATE)
    @PostMapping(value = "config")
    public Object setSysConfigByKeyService(
        @RequestBody @Valid ReqSetSysConfigInfo reqSetSysConfigInfo,
        BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start setSysConfigByKeyService startTime:{} sysConfigParam:{}",
            startTime.toEpochMilli(),
            JsonTools.toJSONString(reqSetSysConfigInfo));
        Object res = SysConfigServiceInWebase.setSysConfigByKeyService(reqSetSysConfigInfo);
        log.info("end setSysConfigByKeyService useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));

//        return new BaseResponse(ConstantCode.SUCCESS, res);
        return res;
    }

}
