package com.webank.webase.node.mgr.precompiled.permission;

import lombok.Data;

@Data
public class PermissionState {
    private int deployAndCreate;
    private int cns;
    private int sysConfig;
    private int node;
}
