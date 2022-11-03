package com.webank.webase.node.mgr.account.entity;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * user registration information of the developer platform.
 */
@Data
public class ReqDeveloperRegister {

    /**
     * 新注册的账户
     */
    @NotBlank
    @Length(max = 250)
    private String account;
    @NotBlank
    private String accountPwd;
    /**
     * developer可注册
     */
    @NotNull
    private Integer roleId;
    /**
     * email需要用来接收注册验证码
     */
    @NotBlank
    @Length(max = 50)
    private String email;

    @Length(max = 250)
    private String realName;
    /**
     * 身份证号
     */
    private String idCardNumber;
    /**
     * 电话
     */
    @DecimalMax(value = "99999999999", message = "mobile value are not match")
    private Long mobile;
    private String contactAddress;
    @Length(max = 250)
    private String companyName;

    /**
     * 有效时间，单位：年
     */
    private Integer expandTime = 1;
}
