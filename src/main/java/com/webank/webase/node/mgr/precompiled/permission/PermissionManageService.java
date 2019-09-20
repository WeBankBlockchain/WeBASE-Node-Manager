package com.webank.webase.node.mgr.precompiled.permission;

import com.alibaba.fastjson.JSON;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.HttpRequestTools;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.node.mgr.frontinterface.FrontRestTools;
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


    /**
     * get sorted paged permission state list
     */
    public Object listPermissionState(int groupId, int pageSize, int pageNumber) {
        log.debug("start listPermissionState. param:{}" + groupId);
        String uri;
        Map<String, String>  map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));
        uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_PERMISSION_SORTED_LIST, map);
        Object frontRsp = frontRestTools.getForEntity(groupId, uri, Object.class);
        log.debug("end listPermissionState. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     * get paged permission list
     */
    public Object listPermission(int groupId, String permissionType, String tableName, int pageSize, int pageNumber) {
        log.debug("start listPermission. param:{}" + groupId + permissionType);
        String uri;
        Map<String, String>  map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("permissionType", permissionType);
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));
        if(Objects.isNull(tableName)) {
//            uri = String.format(FrontRestTools.URI_PERMISSION, permissionType, pageSize, pageNumber);
            uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_PERMISSION, map);
        } else {
//            uri = String.format(FrontRestTools.URI_PERMISSION, permissionType, tableName, pageSize, pageNumber);
            map.put("tableName", tableName);
            uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_PERMISSION, map);
        }

        Object frontRsp = frontRestTools.getForEntity(groupId, uri, Object.class);
        log.debug("end listPermission. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     * get full permission list
     */
    public Object getPermissionFullList(int groupId, String permissionType, String tableName) {
        log.debug("start getPermissionFullList. param:{}" + groupId + permissionType);
        String uri;
        Map<String, String>  map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("permissionType", permissionType);
        if(Objects.isNull(tableName)) {
//            uri = String.format(FrontRestTools.URI_PERMISSION, permissionType, pageSize, pageNumber);
            uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_PERMISSION_FULL_LIST, map);
        } else {
//            uri = String.format(FrontRestTools.URI_PERMISSION, permissionType, tableName, pageSize, pageNumber);
            map.put("tableName", tableName);
            uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_PERMISSION_FULL_LIST, map);
        }

        Object frontRsp = frontRestTools.getForEntity(groupId, uri, Object.class);
        log.debug("end getPermissionFullList. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     * post permission grant
     */
    public Object updatePermissionState(PermissionParam permissionParam) {
        log.debug("start updatePermissionState. param:{}", JSON.toJSONString(permissionParam));
        if (Objects.isNull(permissionParam)) {
            log.info("fail updatePermissionState. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }

        Object frontRsp = frontRestTools.postForEntity(
                permissionParam.getGroupId(), FrontRestTools.URI_PERMISSION_SORTED_LIST,
                permissionParam, Object.class);
        log.debug("end updatePermissionState. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    public Object grantPermission(PermissionParam permissionParam) {
        log.debug("start grantPermission. param:{}", JSON.toJSONString(permissionParam));
        if (Objects.isNull(permissionParam)) {
            log.info("fail grantPermission. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }

        Object frontRsp = frontRestTools.postForEntity(
                permissionParam.getGroupId(), FrontRestTools.URI_PERMISSION,
                permissionParam, Object.class);
        log.debug("end grantPermission. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    public Object revokePermission(PermissionParam permissionParam) {
        log.debug("start revokePermission. param:{}", JSON.toJSONString(permissionParam));
        if (Objects.isNull(permissionParam)) {
            log.info("fail revokePermission. request param is null");
            throw new NodeMgrException(ConstantCode.INVALID_PARAM_INFO);
        }

        Object frontRsp = frontRestTools.deleteForEntity(
                permissionParam.getGroupId(), FrontRestTools.URI_PERMISSION,
                permissionParam, Object.class);
        log.debug("end revokePermission. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }
}
