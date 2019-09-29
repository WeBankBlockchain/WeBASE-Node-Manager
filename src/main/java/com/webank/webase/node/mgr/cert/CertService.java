package com.webank.webase.node.mgr.cert;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.cert.entity.CertParam;
import com.webank.webase.node.mgr.cert.entity.TbCert;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.FrontParam;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.crypto.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
// TODO 内部API，可能在将来发行版中删除
import sun.security.x509.X509CertImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CertService {

    @Autowired
    CertMapper certMapper;
    @Autowired
    private FrontInterfaceService frontInterfaceService;
    @Autowired
    private FrontService frontService;
    /**
     * 存进数据库中，
     * 存一个单个证书的内容
     * 证书的格式包含开头---BEGIN---与结尾
     * 包含bare string, 以及tbCert的内容
     */
    public int saveCerts(String content) throws IOException, CertificateException {

        // crt加载list
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        List<X509CertImpl> certs = getCerts(content);
        // 单个crt的原文list
        List<String> certContentList = CertTools.getSingleCrtContent(content);
        // 用于保存的实体list
        List<TbCert> tbCertList = new ArrayList<>();
        // 记录保存到第几个cert时报错
        int count = 0;
        for(int i = 0; i < certs.size(); i++) {
            TbCert tbCert = new TbCert();
            X509CertImpl certImpl = certs.get(i);
            // 用SHA-1计算得出指纹
            String fingerPrint = certImpl.getFingerprint("SHA-1");
            String certName = CertTools.getCertName(certImpl.getSubjectDN());
            String certType = CertTools.getCertType(certImpl.getSubjectDN());
            // 获取crt的原文,并加上头和尾
            String certContent = CertTools.addHeadAndTail(certContentList.get(i));
            // 判断证书类型后给pub和父子证书赋值
            String publicKeyString = "";
            String address = "";
            String fatherCertContent = "";
            if(certType.equals("node")) {
                // ECC 才有符合的public key, pub => address
                publicKeyString = CertTools.getPublicKeyString(certImpl.getPublicKey());
                address = Keys.getAddress(publicKeyString);
                fatherCertContent = findFatherCert(certImpl);
            }else if(certType.equals("agency")){ // 非节点证书，无需pub(RSA's public key)
                fatherCertContent = findFatherCert(certImpl);
                setSonCert(certImpl);
            }else if(certType.equals("chain")){
                setSonCert(certImpl);
            }
            // 实体赋值
            tbCert.setPublicKey(publicKeyString);
            tbCert.setAddress(address);
            tbCert.setContent(certContent);
            tbCert.setFather(fatherCertContent);
            tbCert.setFingerPrint(fingerPrint);
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

    public List<TbCert> getAllCertsList() {
        List<TbCert> certs = new ArrayList<>();
        // 首次获取参数时，拉取front的证书
        pullFrontNodeCrt();
        certs = certMapper.listOfCert();
        return certs;
    }

    /**
     * 根据证书类型返回list
     * @return
     */
    public List<TbCert> getCertListByCertType(CertParam param) {
        List<TbCert> certs = new ArrayList<>();
        certs = certMapper.listOfCertByConditions(param);
        return certs;
    }

    public TbCert getCertByFingerPrint(String fingerPrint) {
        TbCert cert = certMapper.queryCertByFingerPrint(fingerPrint);
        return cert;
    }


    /**
     * 只会更新father字段
     * @param sonFingerPrint
     * @param fatherFingerPrint
     */
    public void updateCertFather(String sonFingerPrint, String fatherFingerPrint){
        TbCert cert = certMapper.queryCertByFingerPrint(sonFingerPrint);
        // father delete了父证书时可以为空
        cert.setFather(fatherFingerPrint);
        certMapper.update(cert);
    }

    /**
     * 根据单个crt的内容，找父证书，
     * @param sonCert
     * @return String crt's address
     */
    public String findFatherCert(X509CertImpl sonCert) throws IOException, CertificateException{
        List<X509CertImpl> x509CertList = loadAllX509Certs();
        String result = "";
        for(int i = 0; i < x509CertList.size(); i++) {
            X509CertImpl temp = x509CertList.get(i);
            try{
                sonCert.verify(temp.getPublicKey());
            }catch (Exception e) {
                // 签名不匹配则继续
                continue;
            }
            // 返回指纹
            result = temp.getFingerprint("SHA-1");
        }
        return result;
    }

    /**
     * 找到父证书所有的子证书，将子证书的father设为他自己
     * @param fatherCert
     */
    public void setSonCert(X509CertImpl fatherCert) throws IOException, CertificateException {
        List<X509CertImpl> x509CertList = new ArrayList<>();
        String fatherType = CertTools.getCertType(fatherCert.getSubjectDN());
        if(fatherType.equals(CertTools.TYPE_CHAIN)){
            x509CertList = loadAllX509CertsByType(CertTools.TYPE_AGENCY);
        }else if(fatherType.equals(CertTools.TYPE_AGENCY)){
            x509CertList = loadAllX509CertsByType(CertTools.TYPE_NODE);
        }

        for(int i = 0; i < x509CertList.size(); i++) {
            X509CertImpl temp = x509CertList.get(i);
            try{
                // 找儿子
                temp.verify(fatherCert.getPublicKey());
            }catch (Exception e) {
                // 签名不匹配则继续
                continue;
            }
            String sonFingerPrint = temp.getFingerprint("SHA-1");
            updateCertFather(sonFingerPrint, fatherCert.getFingerprint("SHA-1"));
        }
    }

    /**
     * 获取数据库所有的cert，并转换成X509实例返回
     */
    public List<X509CertImpl> loadAllX509Certs() throws IOException, CertificateException {
        // 空参数
        CertParam param = new CertParam();
        List<TbCert> tbCertList = getAllCertsList();

        List<X509CertImpl> x509CertList = new ArrayList<>();
        for(TbCert tbCert: tbCertList) {
            X509CertImpl temp = getCert(tbCert.getContent());
            x509CertList.add(temp);
        }
        return x509CertList;
    }

    /**
     * 获取数据库所有符合certType的证书cert，并转换成X509实例返回
     */
    public List<X509CertImpl> loadAllX509CertsByType(String certType) throws IOException, CertificateException {
        // 空参数
        CertParam param = new CertParam();
        param.setCertType(certType);
        List<TbCert> tbCertList = getCertListByCertType(param);

        List<X509CertImpl> x509CertList = new ArrayList<>();
        for(TbCert tbCert: tbCertList) {
            X509CertImpl temp = getCert(tbCert.getContent());
            x509CertList.add(temp);
        }
        return x509CertList;
    }

    /**
     * 删除cert，同时更新证书的父证书为空
     * @return 返回受影响的证书数
     * TODO 如果update出错，会导致update少了一部分
     */
    public int removeCertByFingerPrint(String fingerPrint) {
        int count = 0;
        List<TbCert> list = getAllCertsList();
        removeCert(fingerPrint);
        for(TbCert tbCert: list) {
            if(tbCert.getFather().equals(fingerPrint)){
                tbCert.setFather("");
                String sonCertFingerPrint = tbCert.getFingerPrint();
                updateCertFather(sonCertFingerPrint, "");
                count++;
            }
        }
        return count;
    }

    public void removeCert(String fingerPrint) {
        certMapper.deleteByFingerPrint(fingerPrint);
    }

    /**
     * pull front's cert
     * 拉去front的节点证书
     * 返回的是一个map，包含(chain,ca.crt), (node, xx), (agency, xx)
     */
    public int pullFrontNodeCrt()  {
        // 如果已完成拉取
        if(CertTools.isPullFrontCertsDone) {
            return 0;
        }
        int count = 0;
        log.debug("start pullFrontNodeCrt. ");
        List<TbFront> frontList = frontService.getFrontList(new FrontParam());
        for(TbFront tbFront: frontList) {
            Map<String, String> certs = new HashMap<>();
            String frontIp = tbFront.getFrontIp();
            Integer frontPort = tbFront.getFrontPort();
            log.debug("start getCertMapFromSpecificFront. frontIp:{} , frontPort: {} ", frontIp, frontPort);
            certs = (Map<String, String>) frontInterfaceService.getCertMapFromSpecificFront(frontIp, frontPort);
            log.debug("end getCertMapFromSpecificFront. ");
            try{
                saveFrontCert(certs);
                count++;
            }catch (Exception e) {
                log.error(e.getMessage());
                throw new NodeMgrException(ConstantCode.SAVING_FRONT_CERT_ERROR);
            }
        }
        CertTools.isPullFrontCertsDone = true;
        return count;
    }

    private void saveFrontCert(Map<String, String> certContents) throws IOException, CertificateException {
        log.debug("start saveFrontCert. address:{} ", certContents);
        String chainCertContent = certContents.get(CertTools.TYPE_CHAIN);
        String agencyCertContent = certContents.get(CertTools.TYPE_AGENCY);
        String nodeCertContent = certContents.get(CertTools.TYPE_NODE);
        if(!chainCertContent.equals("")) {
            chainCertContent = CertTools.addHeadAndTail(chainCertContent);
            log.debug("start chainCertContent. :{} ", chainCertContent);
            saveCerts(chainCertContent);
            log.debug("end chainCertContent. :{} ", chainCertContent);

        }
        if(!agencyCertContent.equals("")) {
            agencyCertContent = CertTools.addHeadAndTail(agencyCertContent);
            log.debug("start agencyCertContent. :{} ", chainCertContent);
            saveCerts(agencyCertContent);
            log.debug("end agencyCertContent. :{} ", chainCertContent);
        }
        if(!nodeCertContent.equals("")) {
            nodeCertContent = CertTools.addHeadAndTail(nodeCertContent);
            log.debug("start nodeCertContent:{} ", nodeCertContent);
            saveCerts(nodeCertContent);
            log.debug("end nodeCertContent:{} ", nodeCertContent);
        }
        log.debug("end saveFrontCert. address:{} ", certContents);
    }
    /**
     * check Cert fingerPrint.
     */
    public boolean certFingerPrintExists(String fingerPrint) throws NodeMgrException {
        log.debug("start certAddressExists. address:{} ", fingerPrint);
        if (fingerPrint == "") {
            log.debug("fail certAddressExists. fingerPrint is empty ");
            throw new NodeMgrException(ConstantCode.ROLE_ID_EMPTY);
        }
        TbCert certInfo = certMapper.queryCertByFingerPrint(fingerPrint);
        if (certInfo != null) {
            log.debug("end certAddressExists. ");
            return true;
        }else {
            log.debug("end certAddressExists. ");
            return false;
        }
    }

    /**
     * 解析is获取证书list
     * @return
     * @throws IOException
     */
    public List<X509CertImpl> getCerts(String crtContent) throws IOException, CertificateException {
        InputStream is = new ByteArrayInputStream(crtContent.getBytes());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        List<X509CertImpl> certs = (List<X509CertImpl>) cf.generateCertificates(is);
        is.close();
        return certs;
    }

    public X509CertImpl getCert(String crtContent) throws IOException, CertificateException {
        InputStream is = new ByteArrayInputStream(crtContent.getBytes());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509CertImpl cert = (X509CertImpl) cf.generateCertificate(is);
        is.close();
        return cert;
    }
}
