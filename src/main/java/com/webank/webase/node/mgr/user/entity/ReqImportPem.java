package com.webank.webase.node.mgr.user.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * import pem private key
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ReqImportPem extends NewUserInputParam {
    @NotBlank
    private String pemContent;
}