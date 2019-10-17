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
import java.time.Instant;
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
     * cert存进数据库中，
     * 证书的格式包含开头---BEGIN---与结尾，包含bare string, 以及tbCert的内容
     * 存储一个证书load证书、存到db、更新db中子证书/父证书
     */

    public int saveCerts(String content) throws CertificateException {
        Instant startTime = Instant.now();
        log.debug("start saveCerts startTime:{} Cert content:{}",
                startTime.toEpochMilli(), content);
        // crt加载list
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        List<X509CertImpl> certs = loadCertListFromCrtContent(content);
        // 单个crt的原文list
        List<String> certContentList = CertTools.getCrtContentList(content);
        // 用于保存的实体list
        List<TbCert> tbCertList = new ArrayList<>();
        // 记录保存到第几个cert时报错
        int count = 0;
        log.debug("saveCerts start save TbCert in db. cert list size:{}", certs.size());
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
            // 非节点证书，无需公钥(RSA's public key)
            if(CertTools.TYPE_NODE.equals(certType)) {
                // ECC 才有符合的public key, pub => address
                publicKeyString = CertTools.getPublicKeyString(certImpl.getPublicKey());
                address = Keys.getAddress(publicKeyString);
                fatherCertContent = findFatherCert(certImpl);
            }else if(CertTools.TYPE_AGENCY.equals(certType)){
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
            log.debug("saveCerts save TbCert in db. index:{},cert entity: {}", i, tbCert);

            count++;
        }
        for(TbCert tbCert: tbCertList) {
            try{
                saveCert(tbCert);
            }catch (Exception e) {
                log.error("saveCerts exception:{}", e);
                throw new NodeMgrException(ConstantCode.CERT_FORMAT_ERROR.getCode(),
                        "Fail saving the " + count + " crt, please try again");
            }
        }
        log.debug("end saveCerts.  Cert count:{}", count);
        return count;
    }

    public void saveCert(TbCert tbCert) {
        log.debug("start addCert to db.");
        certMapper.add(tbCert);
        log.debug("end addCert to db.  TbCert:{}", tbCert);
    }

    /**
     * 先拉取front，后返回数据库的所有证书
     * 先pull一次，然后再返回list
     * @return
     */
    public List<TbCert> getAllCertsListAfterPull() {
        // 首次获取参数时，拉取front的证书
        // 如果已完成拉取
        if(!CertTools.isPullFrontCertsDone) {
            try{
                pullFrontNodeCrt();
                CertTools.isPullFrontCertsDone = true;
            }catch (Exception e) {
                log.error("PullFrontNodeCrt error exception:[], message:{}" + e, e.getMessage());
            }
        }

        // 获取数据库cert list
        return getAllCertsListFromDB();
    }

    /**
     * 获取数据库中所有的certs
     * @return
     */
    public List<TbCert> getAllCertsListFromDB() {
        List<TbCert> certs = new ArrayList<>();
        log.debug("start getAllCertsListService.");
        certs = certMapper.listOfCert();
        log.debug("end getAllCertsListService.  Cert List: {}", certs);
        return certs;
    }

    /**
     * 根据证书类型返回list
     * @return
     */
    public List<TbCert> getCertListByCertType(CertParam param) {
        List<TbCert> certs = new ArrayList<>();
        log.debug("start getCertListByCertType.");
        certs = certMapper.listOfCertByConditions(param);
        log.debug("end getCertListByCertType.  Cert List: {}", certs);
        return certs;
    }

    public TbCert getCertByFingerPrint(String fingerPrint) {
        log.debug("start getCertByFingerPrint. ");
        TbCert cert = certMapper.queryCertByFingerPrint(fingerPrint);
        log.debug("end getCertByFingerPrint.  Cert: {}", cert);
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
        log.debug("start updateCertFather. cert old father: {}", cert.getFather());
        cert.setFather(fatherFingerPrint);
        certMapper.update(cert);
        log.debug("end updateCertFather. cert new father: {}", cert.getFather());
    }

    /**
     * 根据单个crt的内容，找父证书，
     * @param sonCert
     * @return String crt's address
     */
    public String findFatherCert(X509CertImpl sonCert) {
        log.debug("start findFatherCert. son cert: {}", sonCert.getFingerprint("SHA-1"));
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
        log.debug("end findFatherCert. find one FatherCert's finerPrint:{}", result);
        return result;
    }

    /**
     * 找到父证书所有的子证书，将子证书的father设为他自己
     * @param fatherCert
     */
    public void setSonCert(X509CertImpl fatherCert) {
        log.debug("start setSonCert. Father FingerPrint:{}", fatherCert.getFingerprint("SHA-1"));
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
            log.debug("end setSonCert. Father FingerPrint:{}, SonFingerPrint:{}",
                    fatherCert.getFingerprint("SHA-1"), sonFingerPrint);
        }
    }

    /**
     * 获取数据库所有的cert，并转换成X509实例返回
     */
    public List<X509CertImpl> loadAllX509Certs() {
        log.debug("start loadAllX509Certs.");
        // 空参数
        CertParam param = new CertParam();
        List<TbCert> tbCertList = getAllCertsListFromDB();

        List<X509CertImpl> x509CertList = new ArrayList<>();
        for(TbCert tbCert: tbCertList) {
            X509CertImpl temp = loadSingleCertFromCrtContent(tbCert.getContent());
            x509CertList.add(temp);
        }
        log.debug("end loadAllX509Certs. certList:{}", x509CertList);
        return x509CertList;
    }

    /**
     * 获取数据库所有符合certType的证书cert，并转换成X509实例返回
     */
    public List<X509CertImpl> loadAllX509CertsByType(String certType) {
        log.debug("start loadAllX509CertsByType.certType:{}", certType);
        // 空参数
        CertParam param = new CertParam();
        param.setCertType(certType);
        List<TbCert> tbCertList = getCertListByCertType(param);

        List<X509CertImpl> x509CertList = new ArrayList<>();
        for(TbCert tbCert: tbCertList) {
            X509CertImpl temp = loadSingleCertFromCrtContent(tbCert.getContent());
            x509CertList.add(temp);
        }
        log.debug("end loadAllX509CertsByType list:{}", x509CertList);
        return x509CertList;
    }

    /**
     * 删除cert，同时更新证书的父证书为空
     * @return 返回受影响的证书数
     */
    public int removeCertByFingerPrint(String fingerPrint) {
        log.debug("start removeCertByFingerPrint. fingerPrint:{}", fingerPrint);
        int count = 0;
        List<TbCert> list = getAllCertsListFromDB();
        removeCert(fingerPrint);
        for(TbCert tbCert: list) {
            if(fingerPrint.equals(tbCert.getFather())){
                tbCert.setFather("");
                String sonCertFingerPrint = tbCert.getFingerPrint();
                updateCertFather(sonCertFingerPrint, "");
                count++;
            }
        }
        log.debug("end removeCertByFingerPrint. fingerPrint:{}, count:{}", fingerPrint, count);
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
        log.debug("start pulling Front's Node Certs. ");
        List<TbFront> frontList = frontService.getFrontList(new FrontParam());
        for(TbFront tbFront: frontList) {
            Map<String, String> certs = new HashMap<>();
            String frontIp = tbFront.getFrontIp();
            Integer frontPort = tbFront.getFrontPort();
            log.debug("start getCertMapFromSpecificFront. frontIp:{} , frontPort: {} ", frontIp, frontPort);
            certs = frontInterfaceService.getCertMapFromSpecificFront(frontIp, frontPort);
            log.debug("end getCertMapFromSpecificFront. ");
            try{
                saveFrontCert(certs);
                count++;
            }catch (Exception e) {
                log.error("pulling Front's Node Certs. exception:[] ", e);
                throw new NodeMgrException(ConstantCode.SAVING_FRONT_CERT_ERROR);
            }
        }
        log.debug("end pulling Front's Node Certs.. count:{}", count);
        return count;
    }

    private void saveFrontCert(Map<String, String> certContents) throws IOException, CertificateException {
        log.debug("start saveFrontCert. address:{} ", certContents);
        String chainCertContent = certContents.get(CertTools.TYPE_CHAIN);
        String agencyCertContent = certContents.get(CertTools.TYPE_AGENCY);
        String nodeCertContent = certContents.get(CertTools.TYPE_NODE);
        if(!"".equals(chainCertContent)) {
            chainCertContent = CertTools.addHeadAndTail(chainCertContent);
            log.debug("start chainCertContent. :{} ", chainCertContent);
            saveCerts(chainCertContent);
            log.debug("end chainCertContent. :{} ", chainCertContent);

        }
        if(!"".equals(agencyCertContent)) {
            agencyCertContent = CertTools.addHeadAndTail(agencyCertContent);
            log.debug("start agencyCertContent. :{} ", chainCertContent);
            saveCerts(agencyCertContent);
            log.debug("end agencyCertContent. :{} ", chainCertContent);
        }
        if(!"".equals(nodeCertContent)) {
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
        log.debug("start check certFingerPrintExists. fingerPrint:{} ", fingerPrint);
        if (fingerPrint == "") {
            log.debug("fail certAddressExists. fingerPrint is empty ");
            throw new NodeMgrException(ConstantCode.ROLE_ID_EMPTY);
        }
        TbCert certInfo = certMapper.queryCertByFingerPrint(fingerPrint);
        if (certInfo != null) {
            log.debug("end check certFingerPrintExists. exist:{}", certInfo);
            return true;
        }else {
            log.debug("end check certFingerPrintExists. exist:{}", false);
            return false;
        }
    }

    /**
     * 解析is获取证书list
     * @return
     * @throws IOException
     * // TODO X509CertImpl为内部API，可能在将来发行版中删除
     */
    public List<X509CertImpl> loadCertListFromCrtContent(String crtContent) {
        log.debug("loadCertListFromCrtContent content:{}", crtContent);
        List<X509CertImpl> certs;
        try(InputStream is = new ByteArrayInputStream(crtContent.getBytes())) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            certs = (List<X509CertImpl>) cf.generateCertificates(is);
        }catch (CertificateException | IOException e) {
            log.error("loadCertListFromCrtContent exception:[]", e);
            throw new NodeMgrException(ConstantCode.CERT_ERROR.getCode(), e.getMessage());
        }
        return certs;
    }

    public X509CertImpl loadSingleCertFromCrtContent(String crtContent) {
        log.debug("start loadSingleCertFromCrtContent. content:{}", crtContent);
        X509CertImpl cert;
        try(InputStream is = new ByteArrayInputStream(crtContent.getBytes())) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509CertImpl) cf.generateCertificate(is);
        }catch (CertificateException | IOException e) {
            log.error("get loadSingleCertFromCrtContent. Exception:[]", e);
            throw new NodeMgrException(ConstantCode.CERT_ERROR.getCode(), e.getMessage());
        }
        log.debug("end loadSingleCertFromCrtContent. cert:{}", cert);
        return cert;
    }
}
