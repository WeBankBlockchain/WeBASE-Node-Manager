package com.webank.webase.node.mgr.token;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TbToken {
    private String token;
    private String value;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}