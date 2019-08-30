package com.webank.webase.node.mgr.precompiled.permission;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;

@Log4j2
@RestController
@RequestMapping("permission")
public class PermissionManageController extends BaseController {
    @Autowired
    PermissionManageService permissionManageService;

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
            @RequestParam(defaultValue = "1") int pageNumber) throws Exception, NodeMgrException {

        Instant startTime = Instant.now();
        log.info("start listPermissionManager startTime:{}", startTime.toEpochMilli());

        Object result = permissionManageService.listPermission(groupId, permissionType, tableName, pageSize, pageNumber);

        log.info("end listPermissionManager useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(result));
        return result;
    }

    /**
     * get permission manager full list
     * 透传front的BaseResponse
     */
    @GetMapping("full")
    public Object listFullPermissionManager(
            @RequestParam(defaultValue = "1") int groupId,
            @RequestParam String permissionType,
            @RequestParam(defaultValue = "", required = false) String tableName) throws Exception, NodeMgrException {

        Instant startTime = Instant.now();
        log.info("start listFullPermissionManager startTime:{}", startTime.toEpochMilli());

        Object result = permissionManageService.getPermissionFullList(groupId, permissionType, tableName);

        log.info("end listFullPermissionManager useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(result));
        return result;
    }

    /**
     * grant permission.
     */
    @PostMapping(value = "")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object grantPermission(@RequestBody @Valid PermissionParam permissionParam,
                                       BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start grantPermission startTime:{} permissionParam:{}", startTime.toEpochMilli(),
                JSON.toJSONString(permissionParam));

        Object res = permissionManageService.grantPermission(permissionParam);

        log.info("end grantPermission useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(res));

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
                JSON.toJSONString(permissionParam));

        Object res = permissionManageService.revokePermission(permissionParam);

        log.info("end revokePermission useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(res));

        return res;
    }
}
