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
package com.webank.webase.node.mgr.precompiled.permission;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.JsonTools;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.base.tools.pagetools.List2Page;
import com.webank.webase.node.mgr.base.tools.pagetools.entity.MapHandle;

import lombok.extern.log4j.Log4j2;

/**
 * Permission contoller
 * grant or revoke administrator and get administrators on chain
 */
@Log4j2
@RestController
@RequestMapping("permission")
public class PermissionManageController extends BaseController {
    @Autowired
    PermissionManageService permissionManageService;

    /**
     * get permission state list
     * 返回user的权限状态，包含cns, sysConfig, deployAndCreate, node
     */
    @GetMapping("sorted")
    public Object listPermissionMgrState(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) {

        Instant startTime = Instant.now();
        log.info("start listPermissionMgrState startTime:{}", startTime.toEpochMilli());

        Map<String, PermissionState> resultMap = permissionManageService.listPermissionState(groupId);
        int totalCount = resultMap.size();
        // 对应Map排序
        List<MapHandle> resultList = NodeMgrTools.sortMap(resultMap);
        // Map分页
        List2Page list2Page = new List2Page(resultList, pageSize, pageNumber);
        List<MapHandle> finalList = list2Page.getPagedList();
        log.info("end listPermissionMgrState useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(finalList));
        return new BasePageResponse(ConstantCode.SUCCESS, finalList, totalCount);
    }

    /**
     * get permission manager paged list
     * 透传front的BaseResponse
     */
    @GetMapping("")
    public Object listPermissionManager(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam String permissionType,
            @RequestParam(defaultValue = "", required = false) String tableName,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) {

        Instant startTime = Instant.now();
        log.info("start listPermissionManager startTime:{}", startTime.toEpochMilli());

        Object result = permissionManageService.listPermissionPaged(groupId, permissionType, tableName, pageSize, pageNumber);

        log.info("end listPermissionManager useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(result));
        return result;
    }

    /**
     * get permission manager full list
     * 根据权限类型返回拥有该权限的全部user address
     * 透传front的BaseResponse
     */
    @GetMapping("full")
    public Object listFullPermissionManager(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam String permissionType,
            @RequestParam(defaultValue = "", required = false) String tableName) {

        Instant startTime = Instant.now();
        log.info("start listFullPermissionManager startTime:{}", startTime.toEpochMilli());

        Object result = permissionManageService.listPermissionFull(groupId, permissionType, tableName);

        log.info("end listFullPermissionManager useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(result));
        return result;
    }

    /**
     * grant permission.
     * 更新用户的权限状态 包含cns, sysConfig, deployAndCreate, node
     */
    @PostMapping(value = "sorted")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object updatePermission(@RequestBody @Valid PermissionParam permissionParam,
                                  BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start updatePermission startTime:{} permissionParam:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(permissionParam));
        Object res = permissionManageService.updatePermissionState(permissionParam);

        log.info("end updatePermission useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));

        return res;
    }

    @PostMapping(value = "")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object grantPermission(@RequestBody @Valid PermissionParam permissionParam,
                                       BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start grantPermission startTime:{} permissionParam:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(permissionParam));

        Object res = permissionManageService.grantPermission(permissionParam);

        log.info("end grantPermission useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));

        return res;
    }

    /**
     * revoke Permission.
     */
    @DeleteMapping(value = "")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object revokePermission(@RequestBody @Valid PermissionParam permissionParam,
                                 BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start revokePermission startTime:{} permissionParam:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(permissionParam));

        Object res = permissionManageService.revokePermission(permissionParam);

        log.info("end revokePermission useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(res));

        return res;
    }
}
