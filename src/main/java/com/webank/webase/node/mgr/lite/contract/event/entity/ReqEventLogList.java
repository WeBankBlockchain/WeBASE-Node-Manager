package com.webank.webase.node.mgr.lite.contract.event.entity;

import java.util.List;
import lombok.Data;

/**
 * sync get event logs of exactly block
 */
@Data
public class ReqEventLogList {
    private Integer groupId;
    private List<Object> contractAbi;
    private Integer fromBlock;
    private Integer toBlock;
    private String contractAddress;
    private EventTopicParam topics;
}