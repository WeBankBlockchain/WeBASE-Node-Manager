package com.webank.webase.node.mgr.account.entity;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReqFreeze {

    /**
     * 待冻结的目标账户（发起修改的通过token确认身份）
     */
    @NotBlank
    private String account;
    private String description;
}
