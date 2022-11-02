package com.webank.webase.node.mgr.account.entity;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * modify contact person.
 */
@Data
public class ReqUpdateInfo {

    /**
     * 修改的目标账户（发起修改的通过token确认身份）
     */
    @NotBlank
    @Length(max = 250)
    private String account;

    private String companyName;

    private String realName;
    @Length(max = 50)
    private String email;
    private Long mobile;
    private String address;

    /**
     * 身份证号
     */
    private String idNumber;
    private String contactAddress;
}
