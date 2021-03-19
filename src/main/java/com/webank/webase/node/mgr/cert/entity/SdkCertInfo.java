package com.webank.webase.node.mgr.cert.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SdkCertInfo {
    private String name;
    private String content;

    public SdkCertInfo(String name, String content) {
        this.name = name;
        this.content = content;
    }
}
