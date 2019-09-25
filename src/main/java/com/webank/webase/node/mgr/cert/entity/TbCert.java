package com.webank.webase.node.mgr.cert.entity;

import lombok.Data;

import java.sql.Date;

@Data
public class TbCert {
    // Primary Key
    private String address;
    private String certName;
    private String type;
    private String value;
    private String nodeId;
    // 父证书地址
    private String father;
    private Date validityFrom;
    private Date validityTo;
    private Date createTime;
}
