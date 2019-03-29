package com.webank.webase.node.mgr.contract.entity;

import lombok.Data;

@Data
public class QueryContractParam {

    private Integer networkId;
    private Integer pageNumber;
    private Integer pageSize;
}