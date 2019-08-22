package com.webank.webase.node.mgr.precompiled.entity;

import lombok.Data;


@Data
public class CrudHandle {
    private int groupId;
    private String fromAddress;
    private String sql;
}
