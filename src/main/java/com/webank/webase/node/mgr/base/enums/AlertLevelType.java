package com.webank.webase.node.mgr.base.enums;

public enum AlertLevelType {
    HIGH(1), MIDDLE(2), LOW(3);

    private int value;

    AlertLevelType(Integer type) {
        this.value = type;
    }

    public int getValue() {
        return this.value;
    }
}
