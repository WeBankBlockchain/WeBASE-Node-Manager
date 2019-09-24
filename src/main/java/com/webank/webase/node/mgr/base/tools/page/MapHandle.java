package com.webank.webase.node.mgr.base.tools.page;

import lombok.Data;

@Data
public class MapHandle{
    private String key;
    private Object data;

    public MapHandle(String key, Object data) {
        this.key = key;
        this.data = data;
    }
}
