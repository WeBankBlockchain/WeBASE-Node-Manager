package com.webank.webase.node.mgr.precompiled.entity;

import java.util.List;
import lombok.Data;

@Data
public class AccountStatusHandle {
    private Integer groupId;
    private List<String> addressList;
}