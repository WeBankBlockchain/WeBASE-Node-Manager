package com.webank.webase.node.mgr.cert.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CertHandle {
//    @NotBlank(message = "content cannot be empty")
    private String content;
    private String fingerPrint;
}
