package com.webank.webase.node.mgr.lite.contract.event.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RspContractInfo {
    private String type;
    private String contractAddress;
    private String contractName;
}