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
    // node id=public key, 节点证书&sdk证书才有
    private String publicKey;
    private String address;
    // 父证书地址
    private String father;
    private Date validityFrom;
    private Date validityTo;
    private Date createTime;
}
