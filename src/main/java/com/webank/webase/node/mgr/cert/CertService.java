package com.webank.webase.node.mgr.cert;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.cert.entity.TbCert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CertService {

    @Autowired
    CertMapper certMapper;

    /**
     * 存进数据库中，
     */



    /**
     * check Cert Address.
     */
    public boolean certAddressExists(String address) throws NodeMgrException {
        log.debug("start certAddressExists. address:{} ", address);
        if (address == "") {
            log.info("fail certAddressExists. address is empty ");
            throw new NodeMgrException(ConstantCode.ROLE_ID_EMPTY);
        }
        TbCert certInfo = certMapper.queryCertByAddress(address);
        if (certInfo != null) {
            log.debug("end certAddressExists. ");
            return true;
        }else {
            log.debug("end certAddressExists. ");
            return false;
        }
    }
}
