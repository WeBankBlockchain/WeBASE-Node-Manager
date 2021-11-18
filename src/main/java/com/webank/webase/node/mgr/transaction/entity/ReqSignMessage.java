package com.webank.webase.node.mgr.transaction.entity;

import lombok.Data;

@Data
public class ReqSignMessage {

    private String user;
    private String hash;

    private String signUserId;
    private String groupId;

}
