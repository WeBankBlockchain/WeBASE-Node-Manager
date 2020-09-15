package com.webank.webase.node.mgr.precompiled.entity;

import lombok.Data;

@Data
public class ChainGovernanceHandle {
    private Integer groupId;
    private String fromAddress;
    private String address;
    private Integer weight;
    private Integer threshold;
    // use to replace fromAddress when request front
    private String signUserId;
}