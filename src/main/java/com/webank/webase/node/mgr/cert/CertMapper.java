package com.webank.webase.node.mgr.cert;

import com.webank.webase.node.mgr.cert.entity.CertParam;
import com.webank.webase.node.mgr.cert.entity.TbCert;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertMapper {

    void add(TbCert tbCert);

    void deleteByFingerPrint(@Param("fingerPrint")String fingerPrint);

    TbCert queryCertByFingerPrint(@Param("fingerPrint")String fingerPrint);

    List<TbCert> listOfCert();

    List<TbCert> listOfCertByConditions(CertParam param);

    void update(TbCert tbCert);
}
