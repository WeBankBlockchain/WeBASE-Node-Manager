package com.webank.webase.node.mgr.account.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    private String accountPwd;
    @NotNull
    private Integer roleId;
    /**
     * db's email not blank, not support update; else fill in email
     * 管理员直接add的account没有详细信息
     */
    @Length(max = 50)
    private String email;
    private String description;

    private String companyName;
    private String realName;
    private Long mobile;

    /**
     * 身份证号
     */
    private String idCardNumber;
    private String contactAddress;
    /**
     * 延长有效时间，单位：年
     */
    private Integer expandTime;
}
