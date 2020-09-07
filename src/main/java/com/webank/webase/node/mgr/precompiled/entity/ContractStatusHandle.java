package com.webank.webase.node.mgr.precompiled.entity;

import lombok.Data;

@Data
public class ContractStatusHandle {
    private int groupId;
    private String contractAddress;
    // 4 types: freeze, unfreeze, getStatus, grantManager, listManager
    private String handleType;
    private String fromAddress;
    private String signUserId;
    private String grantAddress;
}