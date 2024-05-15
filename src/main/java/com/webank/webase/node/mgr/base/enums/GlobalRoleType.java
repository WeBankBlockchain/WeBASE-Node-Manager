package com.webank.webase.node.mgr.base.enums;

/**
 * @description 集成微服务框架后的通用用户角色类型
 * @return
 * @author 2023/9/18
 * @date zhangyang 11:07:11
 */
public enum GlobalRoleType {
    ADMIN("admin"), COMMON("common"), DEVELOPER("dev");

    private String value;

    private GlobalRoleType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
