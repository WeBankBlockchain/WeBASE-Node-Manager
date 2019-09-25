package com.webank.webase.node.mgr.cert;

import com.webank.webase.node.mgr.cert.entity.TbCert;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertMapper {

    void add(TbCert tbCert);

    void delete(@Param("certId") String certId);

    TbCert queryCertByAddress(@Param("address") String address);

    List<TbCert> listOfCert(@Param("param") CertParam param);

}
