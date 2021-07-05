package com.webank.webase.node.mgr.lite.configapi.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ServerInfo {
    private String ip;
    private Integer port;

    public ServerInfo(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }
}
