/**
 * Copyright 2014-2019 the original author or authors.
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

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.HttpRequestTools;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.FrontRestTools;
import com.webank.webase.node.mgr.user.UserService;
import com.webank.webase.node.mgr.user.entity.TbUser;
import com.webank.webase.node.mgr.user.entity.UserParam;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.web3j.precompile.permission.PermissionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Log4j2
@Service
public class PermissionManageService {

    @Autowired
    private FrontRestTools frontRestTools;
    @Autowired
    private FrontInterfaceService frontInterfaceService;
    @Autowired
    private UserService userService;

    /**
     * get sorted paged permission state list
     */
    // 加上无权限的address(user)
    public Map<String, PermissionState> listPermissionState(int groupId) {
        log.info("start listPermissionState. groupId:{}", groupId);
        Map<String, PermissionState> resultMap = new HashMap<>();
        resultMap = listPermissionStateFull(groupId);
        // 获取当前group全部user address
        UserParam param = new UserParam();
        param.setGroupId(groupId);
        List<TbUser> userList = userService.qureyUserList(param);
        log.info("in listPermissionState. adding all user into resultMap userList:{}", userList);
        // 将未加到Map中的user address加进去
        PermissionState emptyState = getDefaultPermissionState();
        for(TbUser user: userList) {
            String address = user.getAddress();
            if(!resultMap.containsKey(address)) {
                resultMap.put(address, emptyState);
            }
        }
        log.info("end listPermissionState. frontRsp:{}", JSON.toJSONString(resultMap));
        return resultMap;
    }

    // 获取初始PermissionState
    public PermissionState getDefaultPermissionState() {
        // 默认全部权限为0
        PermissionState initState = new PermissionState();
        initState.setDeployAndCreate(0);
        initState.setCns(0);
        initState.setNode(0);
        initState.setSysConfig(0);
        return initState;
    }

    // 不分页的list 返回的list只包含有权限的address
    public Map<String, PermissionState> listPermissionStateFull(int groupId) {
        log.info("start listPermissionStateFull. groupId:{}" , groupId);
        String uri;
        Map<String, String>  map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_PERMISSION_SORTED_FULL_LIST, map);
        Map<String, Object> frontRsp = (Map<String, Object>) frontRestTools.getForEntity(groupId, uri, Object.class);
        log.info("end listPermissionStateFull. frontRsp:{}", JSON.toJSONString(frontRsp));
        Map<String, PermissionState> result = (Map<String, PermissionState>) frontRsp.get("data");
        return result;
    }

    /**
     * get paged permission list
     */
    public Object listPermission(int groupId, String permissionType, String tableName, int pageSize, int pageNumber) {
        log.info("start listPermission. groupId:{}, permissionType:{}" , groupId , permissionType);
        String uri;
        Map<String, String>  map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("permissionType", permissionType);
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));
        if(Objects.isNull(tableName)) {
            uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_PERMISSION, map);
        } else {
            map.put("tableName", tableName);
            uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_PERMISSION, map);
        }

        Object frontRsp = frontRestTools.getForEntity(groupId, uri, Object.class);
        log.info("end listPermission. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     * get full permission list
     */
    public Object getPermissionFullList(int groupId, String permissionType, String tableName) {
        log.info("start getPermissionFullList. groupId:{}, permissionType:{}" , groupId , permissionType);
        String uri;
        Map<String, String>  map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("permissionType", permissionType);
        if(Objects.isNull(tableName)) {
            uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_PERMISSION_FULL_LIST, map);
        } else {
            map.put("tableName", tableName);
            uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_PERMISSION_FULL_LIST, map);
        }

        Object frontRsp = frontRestTools.getForEntity(groupId, uri, Object.class);
        log.info("end getPermissionFullList. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     * post permission grant
     */
    public Object updatePermissionState(PermissionParam permissionParam) {
        log.info("start updatePermissionState. permissionParam:{}", JSON.toJSONString(permissionParam));
        if (Objects.isNull(permissionParam)) {
            log.info("fail updatePermissionState. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }

        Object frontRsp = frontRestTools.postForEntity(
                permissionParam.getGroupId(), FrontRestTools.URI_PERMISSION_SORTED_LIST,
                permissionParam, Object.class);
        log.info("end updatePermissionState. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    public Object grantPermission(PermissionParam permissionParam) {
        log.info("start grantPermission. permissionParam:{}", JSON.toJSONString(permissionParam));
        if (Objects.isNull(permissionParam)) {
            log.info("fail grantPermission. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }

        Object frontRsp = frontRestTools.postForEntity(
                permissionParam.getGroupId(), FrontRestTools.URI_PERMISSION,
                permissionParam, Object.class);
        log.info("end grantPermission. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    public Object revokePermission(PermissionParam permissionParam) {
        log.info("start revokePermission. permissionParam:{}", JSON.toJSONString(permissionParam));
        if (Objects.isNull(permissionParam)) {
            log.info("fail revokePermission. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }

        Object frontRsp = frontRestTools.deleteForEntity(
                permissionParam.getGroupId(), FrontRestTools.URI_PERMISSION,
                permissionParam, Object.class);
        log.info("end revokePermission. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }
}
