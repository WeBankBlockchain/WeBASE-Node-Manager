package com.webank.webase.node.mgr.statistic.entity;

import lombok.Data;

/**
 * @author zhangyang
 * @version 1.0
 * @project WeBASE-Node-Manager
 * @description 链统计信息
 * @date 2023/10/18 09:35:27
 */

@Data
public class ChainStat {
    private int encryptType;
    private String version;
    private long groupCount;
    private long nodeCount;
    private long contractCount;
    private long transCount;
}
