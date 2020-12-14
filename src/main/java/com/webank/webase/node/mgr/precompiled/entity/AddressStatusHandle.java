package com.webank.webase.node.mgr.precompiled.entity;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddressStatusHandle {
    @NotNull
    private Integer groupId;
    /**
     * account address or contract address
     */
    @NotNull
    private List<String> addressList;
}