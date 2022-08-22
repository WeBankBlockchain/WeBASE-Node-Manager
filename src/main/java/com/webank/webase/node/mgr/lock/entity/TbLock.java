package com.webank.webase.node.mgr.lock.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author mawla
 * @describe
 * @date 2022/8/22 10:18 上午
 */
@Data
@Builder
public class TbLock {
    private String lockKey;
    private String requestId;
    private Integer lockCount;
    private Long timeOut;
    private Integer version;
}
