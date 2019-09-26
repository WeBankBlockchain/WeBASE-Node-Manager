package com.webank.webase.node.mgr.cert.entity;

import com.webank.webase.node.mgr.base.entity.BaseQueryParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Date;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CertParam extends BaseQueryParam {
    private String fingerPrint;
    private String certType;
    private String father;
    private String certName;
    private Date validityFrom;
}
