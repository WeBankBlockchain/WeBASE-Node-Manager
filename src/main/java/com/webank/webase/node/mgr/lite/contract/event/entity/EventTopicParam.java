package com.webank.webase.node.mgr.lite.contract.event.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventTopicParam {

    private String eventName;
    private IndexedParamType indexed1;
    private IndexedParamType indexed2;
    private IndexedParamType indexed3;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IndexedParamType {
        private String type;
        // indexed value
        private String value;
    }
}