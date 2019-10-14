/**
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

        log.info("start saveCerts.  Cert content:{}", content);
        // crt加载list
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        log.info("getCerts(load certs from content). ");
        List<X509CertImpl> certs = getCerts(content);
        // 单个crt的原文list
        List<String> certContentList = CertTools.getSingleCrtContent(content);
        // 用于保存的实体list
        List<TbCert> tbCertList = new ArrayList<>();
        // 记录保存到第几个cert时报错
        int count = 0;
        for(int i = 0; i < certs.size(); i++) {
            log.info("start save TbCert in db. cert list size:{}", certs.size());
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
            if(CertTools.TYPE_NODE.equals(certType)) {
                // ECC 才有符合的public key, pub => address
                publicKeyString = CertTools.getPublicKeyString(certImpl.getPublicKey());
                address = Keys.getAddress(publicKeyString);
                fatherCertContent = findFatherCert(certImpl);
            }else if(CertTools.TYPE_AGENCY.equals(certType)){ // 非节点证书，无需公钥(RSA's public key)
                fatherCertContent = findFatherCert(certImpl);
                setSonCert(certImpl);
            }else if(CertTools.TYPE_CHAIN.equals(certType)){
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
            log.info("save TbCert in db. cert entity: {}", tbCert);

            count++;
        }
        for(TbCert tbCert: tbCertList) {
            try{
                saveCert(tbCert);
            }catch (Exception e) {
                throw new NodeMgrException(ConstantCode.CERT_FORMAT_ERROR.getCode(), "Fail saving the " + count + " crt, please try again");
            }
        }
        log.info("end saveCerts.  Cert content:{}", content);
        return count;
    }

    public void saveCert(TbCert tbCert) {
        log.info(
                "start addCert to db.  TbCert:{}", tbCert);
        certMapper.add(tbCert);
        log.info(
                "end addCert to db.  TbCert:{}", tbCert);}

    /**
     * 先拉取front，后返回数据库的所有帧数
     * 先pull一次，然后再返回list
     * @return
     */
    public List<TbCert> getAllCertsListAndPullFront() {
        // 首次获取参数时，拉取front的证书
        // 如果已完成拉取
        if(!CertTools.isPullFrontCertsDone) {
            try{
                pullFrontNodeCrt();
                CertTools.isPullFrontCertsDone = true;
            }catch (Exception e) {
                log.error("PullFrontNodeCrt error" + e.getMessage());
            }
        }

        // 获取数据库cert list
        return getAllCertsListService();
    }

    /**
     * 获取数据库中所有的certs
     * @return
     */
    public List<TbCert> getAllCertsListService() {
        List<TbCert> certs = new ArrayList<>();
        log.info("start getAllCertsListService.");
        certs = certMapper.listOfCert();
        log.info("end getAllCertsListService.  Cert List: {}", certs);
        return certs;
    }

    /**
     * 根据证书类型返回list
     * @return
     */
    public List<TbCert> getCertListByCertType(CertParam param) {
        List<TbCert> certs = new ArrayList<>();
        log.info("start getCertListByCertType.");
        certs = certMapper.listOfCertByConditions(param);
        log.info("end getCertListByCertType.  Cert List: {}", certs);
        return certs;
    }

    public TbCert getCertByFingerPrint(String fingerPrint) {
        log.info("start getCertByFingerPrint. ");
        TbCert cert = certMapper.queryCertByFingerPrint(fingerPrint);
        log.info("end getCertByFingerPrint.  Cert: {}", cert);
        return cert;
    }


    /**
     * 只会更新father字段
     * @param sonFingerPrint
     * @param fatherFingerPrint
     */
    public void updateCertFather(String sonFingerPrint, String fatherFingerPrint){
        TbCert cert = certMapper.queryCertByFingerPrint(sonFingerPrint);
        // fatherFingerPrint可为空。因父证书被删除后，子证书的father字段为空
        log.info("start updateCertFather. cert old: {}", cert);
        cert.setFather(fatherFingerPrint);
        certMapper.update(cert);
        log.info("end updateCertFather. cert new: {}", cert);
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
        log.info("start setSonCert. Father FingerPrint:{}", fatherCert);
        List<X509CertImpl> x509CertList = new ArrayList<>();
        String fatherType = CertTools.getCertType(fatherCert.getSubjectDN());
        if(CertTools.TYPE_CHAIN.equals(fatherType)){
            x509CertList = loadAllX509CertsByType(CertTools.TYPE_AGENCY);
        }else if(CertTools.TYPE_AGENCY.equals(fatherType)){
            x509CertList = loadAllX509CertsByType(CertTools.TYPE_NODE);
        }

        for(int i = 0; i < x509CertList.size(); i++) {
            X509CertImpl temp = x509CertList.get(i);
            try{
                // 找子证书
                temp.verify(fatherCert.getPublicKey());
            }catch (Exception e) {
                // 签名不匹配则继续
                continue;
            }
            String sonFingerPrint = temp.getFingerprint("SHA-1");
            updateCertFather(sonFingerPrint, fatherCert.getFingerprint("SHA-1"));
            log.info("end setSonCert. Father FingerPrint:{}, SonFingerPrint:{}", fatherCert, sonFingerPrint);
        }
    }

    /**
     * 获取数据库所有的cert，并转换成X509实例返回
     */
    public List<X509CertImpl> loadAllX509Certs() throws IOException, CertificateException {
        log.info("start loadAllX509Certs.");
        // 空参数
        CertParam param = new CertParam();
        List<TbCert> tbCertList = getAllCertsListService();

        List<X509CertImpl> x509CertList = new ArrayList<>();
        for(TbCert tbCert: tbCertList) {
            X509CertImpl temp = getCert(tbCert.getContent());
            x509CertList.add(temp);
        }
        log.info("end loadAllX509Certs. certList:{}", x509CertList);
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
     */
    public int removeCertByFingerPrint(String fingerPrint) {
        log.info("start removeCertByFingerPrint. fingerPrint:{}", fingerPrint);
        int count = 0;
        List<TbCert> list = getAllCertsListService();
        removeCert(fingerPrint);
        for(TbCert tbCert: list) {
            if(fingerPrint.equals(tbCert.getFather())){
                tbCert.setFather("");
                String sonCertFingerPrint = tbCert.getFingerPrint();
                updateCertFather(sonCertFingerPrint, "");
                count++;
            }
        }
        log.info("end removeCertByFingerPrint. fingerPrint:{}, count:{}", fingerPrint, count);
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
        int count = 0;
        log.info("start pullFrontNodeCrt. ");
        List<TbFront> frontList = frontService.getFrontList(new FrontParam());
        for(TbFront tbFront: frontList) {
            Map<String, String> certs = new HashMap<>();
            String frontIp = tbFront.getFrontIp();
            Integer frontPort = tbFront.getFrontPort();
            log.info("start getCertMapFromSpecificFront. frontIp:{} , frontPort: {} ", frontIp, frontPort);
            certs = frontInterfaceService.getCertMapFromSpecificFront(frontIp, frontPort);
            log.info("end getCertMapFromSpecificFront. ");
            try{
                saveFrontCert(certs);
                count++;
            }catch (Exception e) {
                log.error(e.getMessage());
                throw new NodeMgrException(ConstantCode.SAVING_FRONT_CERT_ERROR);
            }
        }
        log.info("start pullFrontNodeCrt. count:{}", count);
        return count;
    }

    private void saveFrontCert(Map<String, String> certContents) throws IOException, CertificateException {
        log.info("start saveFrontCert. address:{} ", certContents);
        String chainCertContent = certContents.get(CertTools.TYPE_CHAIN);
        String agencyCertContent = certContents.get(CertTools.TYPE_AGENCY);
        String nodeCertContent = certContents.get(CertTools.TYPE_NODE);
        if(!"".equals(chainCertContent)) {
            chainCertContent = CertTools.addHeadAndTail(chainCertContent);
            log.info("start chainCertContent. :{} ", chainCertContent);
            saveCerts(chainCertContent);
            log.info("end chainCertContent. :{} ", chainCertContent);

        }
        if(!"".equals(agencyCertContent)) {
            agencyCertContent = CertTools.addHeadAndTail(agencyCertContent);
            log.info("start agencyCertContent. :{} ", chainCertContent);
            saveCerts(agencyCertContent);
            log.info("end agencyCertContent. :{} ", chainCertContent);
        }
        if(!"".equals(nodeCertContent)) {
            nodeCertContent = CertTools.addHeadAndTail(nodeCertContent);
            log.info("start nodeCertContent:{} ", nodeCertContent);
            saveCerts(nodeCertContent);
            log.info("end nodeCertContent:{} ", nodeCertContent);
        }
        log.info("end saveFrontCert. address:{} ", certContents);
    }
    /**
     * check Cert fingerPrint.
     */
    public boolean certFingerPrintExists(String fingerPrint) throws NodeMgrException {
        log.info("start certAddressExists. address:{} ", fingerPrint);
        if (fingerPrint == "") {
            log.info("fail certAddressExists. fingerPrint is empty ");
            throw new NodeMgrException(ConstantCode.ROLE_ID_EMPTY);
        }
        TbCert certInfo = certMapper.queryCertByFingerPrint(fingerPrint);
        if (certInfo != null) {
            log.info("end certAddressExists. ");
            return true;
        }else {
            log.info("end certAddressExists. ");
            return false;
        }
    }

    /**
     * 解析is获取证书list
     * @return
     * @throws IOException
     * // TODO X509CertImpl为内部API，可能在将来发行版中删除
     */
    public List<X509CertImpl> getCerts(String crtContent) throws IOException {
        InputStream is = new ByteArrayInputStream(crtContent.getBytes());
        List<X509CertImpl> certs;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            certs = (List<X509CertImpl>) cf.generateCertificates(is);
        }catch (CertificateException e) {
            is.close();
            throw new NodeMgrException(ConstantCode.CERT_ERROR.getCode(), e.getMessage());
        }
        is.close();
        return certs;
    }

    public X509CertImpl getCert(String crtContent) throws IOException {
        InputStream is = new ByteArrayInputStream(crtContent.getBytes());
        X509CertImpl cert;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509CertImpl) cf.generateCertificate(is);
        }catch (CertificateException e) {
            is.close();
            throw new NodeMgrException(ConstantCode.CERT_ERROR.getCode(), e.getMessage());
        }
        is.close();
        return cert;
    }
}
