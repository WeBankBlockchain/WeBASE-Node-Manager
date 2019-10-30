package com.webank.webase.node.mgr.base.enums;

public enum AlertRuleType {
    NODE_ALERT(1), AUDIT_ALERT(2), CERT_ALERT(3);

    private int value;

    AlertRuleType(Integer type) {
        this.value = type;
    }

    public int getValue() {
        return this.value;
    }
}
