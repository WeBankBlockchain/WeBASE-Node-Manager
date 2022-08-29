/**
 * Copyright 2014-2021 the original author or authors.
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

package com.webank.webase.node.mgr.precntauth.authmanager.base;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@Api(value = "auth/base/", tags = "check auth controller")
@RestController
@RequestMapping("auth/base/")
public class BaseAuthController {
    @Autowired
    private BaseService baseService;

    /**
     * 获取提案总数
     */
    @ApiOperation(value = "query auth available")
    @ApiImplicitParam(name = "groupId", value = "auth available", required = true)
    @GetMapping("available")
    public BaseResponse queryAuthAvailable(String groupId) {

        return new BaseResponse(ConstantCode.SUCCESS,
            !baseService.queryExecEnvIsWasm(groupId) && baseService.queryChainHasAuth(groupId));
    }
}
