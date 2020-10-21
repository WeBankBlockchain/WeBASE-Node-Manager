package com.webank.webase.node.mgr.contract.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 *
 */
@Data
public class RspContractPath {
    private Integer groupId;
    private String contractPath;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}
