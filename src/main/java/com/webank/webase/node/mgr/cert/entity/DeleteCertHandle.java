package com.webank.webase.node.mgr.cert.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DeleteCertHandle {
    @NotBlank(message = "fingerPrint cannot be empty")
    private String fingerPrint;
}
