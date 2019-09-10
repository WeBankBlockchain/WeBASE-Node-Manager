package com.webank.webase.node.mgr.precompiled.entity;

import lombok.Data;


@Data
public class ConsensusHandle {
    private int groupId;
    private String nodeType;
    private String fromAddress;
    private String nodeId;
}
