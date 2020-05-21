package com.webank.webase.node.mgr.user.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * import pem private key
 */
@Data
@NoArgsConstructor
public class ReqImportPem extends NewUserInputParam {
    @NotBlank
    private String pemContent;
}