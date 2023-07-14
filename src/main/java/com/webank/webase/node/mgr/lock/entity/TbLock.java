package com.webank.webase.node.mgr.lock.entity;

import java.time.LocalDateTime;
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
    private String threadId;
    private Integer lockCount;
    private Long timeout;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}
