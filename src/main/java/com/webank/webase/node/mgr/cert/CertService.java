package com.webank.webase.node.mgr.cert;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.Web3Tools;
import com.webank.webase.node.mgr.cert.entity.TbCert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.security.ec.ECPublicKeyImpl;
import sun.security.x509.X509CertImpl;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
public class CertService {

    @Autowired
    CertMapper certMapper;

    /**
     * 存进数据库中，
     * 存一个单个证书的内容
     * 包含bare string, 以及tbCert的内容
     */

    public int saveCerts(String content) throws IOException, CertificateException {
        // crt加载list
        List<X509CertImpl> certs = CertTools.getCerts(content);
        // 单个crt的原文list
        List<String> certContentList = CertTools.getSingleCrtContent(content);
        // 用于保存的实体list
        List<TbCert> tbCertList = new ArrayList<>();
        // 记录保存到第几个cert时报错
        int count = 0;
        for(int i = 0; i < certs.size(); i++) {
            TbCert tbCert = new TbCert();
            X509CertImpl certImpl = certs.get(i);
            // TODO publick key not right
            String publicKeyString = CertTools.getPublicKeyString(certImpl.getPublicKey());
            // 用SHA-1计算得出指纹
            String fingerPrint = certImpl.getFingerprint("SHA-1").toLowerCase();
            String certName = CertTools.getCertName(certImpl.getSubjectDN());
            String certType = CertTools.getCertType(certImpl.getSubjectDN());
            // 获取crt的原文
            String certContent = certContentList.get(i);

            tbCert.setContent(certContent);
            if(findFatherCert(certContent) != "") {
                tbCert.setFather(findFatherCert(certContent));
            }
            tbCert.setFingerPrint(fingerPrint);
            tbCert.setPublicKey(publicKeyString);
            tbCert.setCertName(certName);
            tbCert.setCertType(certType);
            tbCert.setValidityFrom(certImpl.getNotBefore());
            tbCert.setValidityTo(certImpl.getNotAfter());
            tbCertList.add(tbCert);
            count++;
        }
        for(TbCert tbCert: tbCertList) {
            try{
                saveCert(tbCert);
            }catch (Exception e) {
                throw new NodeMgrException(202061, "Fail saving the " + count + " crt, please try again");
            }
        }

        return count;
    }

    public void saveCert(TbCert tbCert) throws Exception {
        certMapper.add(tbCert);
    }

    public List<TbCert> getCertsList(CertParam param) {
        List<TbCert> certs = new ArrayList<>();
        certs = certMapper.listOfCert(param);
        return certs;
    }

    public TbCert getCertByFingerPrint(String fingerPrint) {
        TbCert cert = certMapper.queryCertByFingerPrint(fingerPrint);
        return cert;
    }

    public boolean removeCertByFingerPrint(String fingerPrint) {
        try{
            certMapper.deleteByFingerPrint(fingerPrint);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据单个crt的内容，找父证书，
     * @param content
     * @return String crt's address
     */
    public String findFatherCert(String content) throws IOException, CertificateException{
        X509CertImpl sonCert = CertTools.getCert(content);
        List<X509CertImpl> x509CertList = getAllX509Certs();
        String result = "";
        for(int i = 0; i < x509CertList.size(); i++) {
            X509CertImpl temp = x509CertList.get(i);
            try{
                sonCert.verify(temp.getPublicKey());
            }catch (Exception e) {
                // 签名不匹配则继续
                continue;
            }
            //TODO 获取pub key
            result = temp.getPublicKey().toString();
        }
        return result;
    }

    /**
     * 获取数据库所有的cert，并转换成X509实例返回
     */
    public List<X509CertImpl> getAllX509Certs() throws IOException, CertificateException {
        // 空参数
        CertParam param = new CertParam();
        List<TbCert> tbCertList = getCertsList(param);

        List<X509CertImpl> x509CertList = new ArrayList<>();
        for(TbCert tbCert: tbCertList) {
            X509CertImpl temp = CertTools.getCert(tbCert.getContent());
            x509CertList.add(temp);
        }
        return x509CertList;
    }


    /**
     * check Cert Address.
     */
    public boolean certAddressExists(String address) throws NodeMgrException {
        log.debug("start certAddressExists. address:{} ", address);
        if (address == "") {
            log.info("fail certAddressExists. address is empty ");
            throw new NodeMgrException(ConstantCode.ROLE_ID_EMPTY);
        }
        TbCert certInfo = certMapper.queryCertByFingerPrint(address);
        if (certInfo != null) {
            log.debug("end certAddressExists. ");
            return true;
        }else {
            log.debug("end certAddressExists. ");
            return false;
        }
    }
}
