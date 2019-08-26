package com.webank.webase.node.mgr.precompiled.permission;


import lombok.Data;

@Data
public class PermissionParam {

    private int groupId;
    private String permissionType;
    private String fromAddress;
    private String address;
    private String tableName;
}