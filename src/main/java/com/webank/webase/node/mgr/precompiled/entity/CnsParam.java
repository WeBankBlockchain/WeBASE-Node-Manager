package com.webank.webase.node.mgr.precompiled.entity;

import lombok.Data;

@Data
public class CnsParam {

    private int groupId;
    private String fromAddress;
    private String contractName;
    private String contractNameAndVersion;
    private String version;
    // register
    private String contractAddress;
    private String abi;
}
