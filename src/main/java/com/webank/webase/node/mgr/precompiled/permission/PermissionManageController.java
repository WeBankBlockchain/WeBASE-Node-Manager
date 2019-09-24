package com.webank.webase.node.mgr.precompiled.permission;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.controller.BaseController;
import com.webank.webase.node.mgr.base.entity.BasePageResponse;
import com.webank.webase.node.mgr.base.entity.BaseResponse;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.page.Map2PagedList;
import com.webank.webase.node.mgr.base.tools.page.MapHandle;
import com.webank.webase.node.mgr.contract.entity.TbContract;
import com.webank.webase.node.mgr.user.entity.TbUser;
import com.webank.webase.node.mgr.user.entity.UserParam;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            @RequestParam(defaultValue = "1") int pageNumber) throws Exception, NodeMgrException {

        Instant startTime = Instant.now();
        log.info("start listPermissionMgrState startTime:{}", startTime.toEpochMilli());

        Map<String, PermissionState> resultMap = permissionManageService.listPermissionState(groupId);
        // Map分页
        Map2PagedList<MapHandle> list2Page = new Map2PagedList(resultMap, pageSize, pageNumber);
        List<MapHandle> finalList = list2Page.getPagedList();
        log.info("end listPermissionMgrState useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(finalList));
        return new BasePageResponse(ConstantCode.SUCCESS, finalList, finalList.size());
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
     * 根据权限类型返回拥有该权限的全部user address
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
     * 更新用户的权限状态 包含cns, sysConfig, deployAndCreate, node
     */
    @PostMapping(value = "sorted")
    @PreAuthorize(ConstantProperties.HAS_ROLE_ADMIN)
    public Object updatePermission(@RequestBody @Valid PermissionParam permissionParam,
                                  BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start updatePermission startTime:{} permissionParam:{}", startTime.toEpochMilli(),
                JSON.toJSONString(permissionParam));

        Object res = permissionManageService.updatePermissionState(permissionParam);

        log.info("end updatePermission useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(), JSON.toJSONString(res));

        return res;
    }

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
