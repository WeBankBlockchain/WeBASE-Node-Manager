package com.webank.webase.node.mgr.cert.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TbCert {
    // Primary Key
    private String fingerPrint;
    private String certName;
    private String content;
    private String certType;
    // nodeid
    private String publicKey;
    // 父证书地址
    private String father;
    private Date validityFrom;
    private Date validityTo;
    private Date createTime;
}
