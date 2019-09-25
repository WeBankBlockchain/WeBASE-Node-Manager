package com.webank.webase.node.mgr.cert;

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
    private String address;
    private String type;
    private String father;
    private Date from;
    private Date to;
}
