package com.webank.webase.node.mgr.precompiled.sysconf;

import lombok.Data;

@Data
public class SysConfigParam {

    private Long id;
    private int groupId;
    private String fromAddress;
    private String configKey;
    private String configValue;
}
