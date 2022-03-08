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

package com.webank.webase.node.mgr.precntauth.authmanager.admin;

import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.precntauth.authmanager.admin.entity.ReqAclAuthTypeInfo;
import com.webank.webase.node.mgr.precntauth.authmanager.admin.entity.ReqAclUsrInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * permission manage above FISCO-BCOS v3.0
 */

/***
 * admin as operator
 */
@Log4j2
@Api(value = "precntauth/authmanager/admin/", tags = "precntauth authmanager controller")
@RestController
@RequestMapping("precntauth/authmanager/admin/")
public class AdminController extends BaseController {

    @Autowired
    private AdminService adminService;

    /**
     * 合约接口权限控制(目前只能对写方法进行控制)
     */
    @ApiOperation(value = "set contract func acl type")
    @ApiImplicitParam(name = "reqAclAuthTypeInfo", value = "aclType info", required = true, dataType = "ReqAclAuthTypeInfo")
    @PostMapping("method/auth/type")
    public Object setMethodAuthType(@Valid @RequestBody ReqAclAuthTypeInfo reqAclAuthTypeInfo) {
        Object res = adminService.setMethodAuthType(reqAclAuthTypeInfo);
        return res;
    }

    /**
     * 设置合约函数用户访问控制
     * contractAddress(0xCcEeF68C9b4811b32c75df284a1396C7C5509561) set(string) accountAddress(0x7fb008862ff69353a02ddabbc6cb7dc31683d0f6)
     */
    @ApiOperation(value = "set contract func usr acl")
    @ApiImplicitParam(name = "reqAclUsrInfo", value = "aclUsr info", required = true, dataType = "ReqAclUsrInfo")
    @PostMapping("method/auth/set")
    public Object setMethodAuth(@Valid @RequestBody ReqAclUsrInfo reqAclUsrInfo)
        throws ContractException {
        Object res = adminService.setMethodAuth(reqAclUsrInfo);
        return res;
    }

}
