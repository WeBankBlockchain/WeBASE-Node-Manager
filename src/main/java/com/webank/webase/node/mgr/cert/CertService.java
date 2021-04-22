/**
 * Copyright 2014-2021 the original author or authors.
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
import com.webank.webase.node.mgr.base.tools.CertTools;
import com.webank.webase.node.mgr.base.tools.CleanPathUtil;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import com.webank.webase.node.mgr.base.tools.ZipUtils;
import com.webank.webase.node.mgr.cert.entity.CertParam;
import com.webank.webase.node.mgr.cert.entity.FileContentHandle;
import com.webank.webase.node.mgr.cert.entity.TbCert;
import com.webank.webase.node.mgr.front.FrontService;
import com.webank.webase.node.mgr.front.entity.FrontParam;
import com.webank.webase.node.mgr.front.entity.TbFront;
import com.webank.webase.node.mgr.frontinterface.FrontInterfaceService;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class CertService {

    @Autowired
    CertMapper certMapper;
    @Autowired
    private FrontInterfaceService frontInterfaceService;
    @Autowired
    private FrontService frontService;
    @Autowired
    private CryptoSuite cryptoSuite;
    private final static String TEMP_SDK_DIR = "sdk";
    private final static String TEMP_ZIP_DIR = "tempZip";
    private final static String TEMP_ZIP_FILE_NAME = "conf.zip";
    private final static String TEMP_ZIP_FILE_PATH = TEMP_ZIP_DIR + File.separator + TEMP_ZIP_FILE_NAME;
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
        List<X509Certificate> certs = loadCertListFromCrtContent(content);
        // 单个crt的原文list
        List<String> certContentList = CertTools.getCrtContentList(content);
        // 用于保存的实体list
        List<TbCert> tbCertList = new ArrayList<>();
        // 记录保存到第几个cert时报错
        int count = 0;
        log.debug("saveCerts start save TbCert in db. cert list size:{}", certs.size());
        for (int i = 0; i < certs.size(); i++) {
            TbCert tbCert = new TbCert();
            X509Certificate certImpl = certs.get(i);
            // 用SHA-1计算得出指纹
            String fingerPrint = NodeMgrTools.getCertFingerPrint(certImpl.getEncoded());
            String certName = CertTools.getCertName(certImpl.getSubjectDN());
            String certType = CertTools.getCertType(certImpl.getSubjectDN());
            // 获取crt的原文,并加上头和尾
            String certContent = CertTools.addCertHeadAndTail(certContentList.get(i));

            // 判断证书类型后给pub和父子证书赋值
            String publicKeyString = "";
            String address = "";
            String fatherCertContent = "";
            // node cert has PublicKey and Address:
            // standard: type=node;  guomi: type = node || type=encrypt_node || type=sdk&&name=sdk
            if (CertTools.TYPE_NODE.equals(certType) || CertTools.TYPE_ENCRYPT_NODE.equals(certType) ||
                    ("sdk".equals(certType) && "sdk".equals(certName))) {
                // ECC 才有符合的public key, pub => address
                publicKeyString = CertTools.getPublicKeyString(certImpl.getPublicKey());
                address = cryptoSuite.getCryptoKeyPair().getAddress(publicKeyString);
                fatherCertContent = findFatherCert(certImpl);
            }else if (CertTools.TYPE_AGENCY.equals(certType)) {
                fatherCertContent = findFatherCert(certImpl);
                setSonCert(certImpl);
            }else if (CertTools.TYPE_CHAIN.equals(certType)) {
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
                log.error("saveCerts exception:[]", e);
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
    public String findFatherCert(X509Certificate sonCert) throws CertificateEncodingException {
        log.debug("start findFatherCert. son cert: {}", NodeMgrTools.getCertFingerPrint(sonCert.getEncoded()));
        List<X509Certificate> x509CertList = loadAllX509Certs();
        String result = "";
        for(int i = 0; i < x509CertList.size(); i++) {
            X509Certificate temp = x509CertList.get(i);
            try{
                sonCert.verify(temp.getPublicKey());
            }catch (Exception e) {
                // 签名不匹配则继续
                continue;
            }
            // 返回指纹
            result = NodeMgrTools.getCertFingerPrint(temp.getEncoded());
        }
        log.debug("end findFatherCert. find one FatherCert's finerPrint:{}", result);
        return result;
    }

    /**
     * 找到父证书所有的子证书，将子证书的father设为他自己
     * @param fatherCert
     */
    public void setSonCert(X509Certificate fatherCert) throws CertificateEncodingException {
        log.debug("start setSonCert. Father FingerPrint:{}", NodeMgrTools.getCertFingerPrint(fatherCert.getEncoded()));
        List<X509Certificate> x509CertList = new ArrayList<>();
        String fatherType = CertTools.getCertType(fatherCert.getSubjectDN());
        if(CertTools.TYPE_CHAIN.equals(fatherType)){
            x509CertList = loadAllX509CertsByType(CertTools.TYPE_AGENCY);
        }else if(CertTools.TYPE_AGENCY.equals(fatherType)){
            x509CertList = loadAllX509CertsByType(CertTools.TYPE_NODE);
        }

        for(int i = 0; i < x509CertList.size(); i++) {
            X509Certificate temp = x509CertList.get(i);
            try{
                // 找子证书
                temp.verify(fatherCert.getPublicKey());
            }catch (Exception e) {
                // 签名不匹配则继续
                continue;
            }
            String sonFingerPrint = NodeMgrTools.getCertFingerPrint(temp.getEncoded());
            updateCertFather(sonFingerPrint, NodeMgrTools.getCertFingerPrint(fatherCert.getEncoded()));
            log.debug("end setSonCert. Father FingerPrint:{}, SonFingerPrint:{}",
                    NodeMgrTools.getCertFingerPrint(fatherCert.getEncoded()), sonFingerPrint);
        }
    }

    /**
     * 获取数据库所有的cert，并转换成X509实例返回
     */
    public List<X509Certificate> loadAllX509Certs() {
        log.debug("start loadAllX509Certs.");
        // 空参数
        CertParam param = new CertParam();
        List<TbCert> tbCertList = getAllCertsListFromDB();

        List<X509Certificate> x509CertList = new ArrayList<>();
        for(TbCert tbCert: tbCertList) {
            X509Certificate temp = loadSingleCertFromCrtContent(tbCert.getContent());
            x509CertList.add(temp);
        }
        log.debug("end loadAllX509Certs. ");
        return x509CertList;
    }

    /**
     * 获取数据库所有符合certType的证书cert，并转换成X509实例返回
     */
    public List<X509Certificate> loadAllX509CertsByType(String certType) {
        log.debug("start loadAllX509CertsByType.certType:{}", certType);
        // 空参数
        CertParam param = new CertParam();
        param.setCertType(certType);
        List<TbCert> tbCertList = getCertListByCertType(param);

        List<X509Certificate> x509CertList = new ArrayList<>();
        for(TbCert tbCert: tbCertList) {
            X509Certificate temp = loadSingleCertFromCrtContent(tbCert.getContent());
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
            String frontIp = tbFront.getFrontIp();
            Integer frontPort = tbFront.getFrontPort();
            log.debug("start getCertMapFromSpecificFront. frontIp:{} , frontPort: {} ", frontIp, frontPort);
            Map<String, String> certs = frontInterfaceService.getCertMapFromSpecificFront(frontIp, frontPort);
            log.debug("end getCertMapFromSpecificFront certs size:{}", certs.size());
            try{
                saveFrontCert(certs);
                count++;
            }catch (Exception e) {
                log.error("pulling Front's Node Certs. exception:[] ", e);
                throw new NodeMgrException(ConstantCode.SAVING_FRONT_CERT_ERROR);
            }
        }
        log.debug("end pulling Front's Node Certs.. front count:{}", count);
        return count;
    }

    private void saveFrontCert(Map<String, String> certContents) throws CertificateException {
        log.debug("start saveFrontCert. certContents:{} ", certContents);
        String chainCertContent = certContents.get(CertTools.TYPE_CHAIN);
        String agencyCertContent = certContents.get(CertTools.TYPE_AGENCY);
        String nodeCertContent = certContents.get(CertTools.TYPE_NODE);
        // guomi encrypt node cert
        String encryptNodeCertContent = certContents.get(CertTools.TYPE_ENCRYPT_NODE);
        String sdkChainCertContent = certContents.get(CertTools.TYPE_SDK_CHAIN);
        String sdkAgencyCertContent = certContents.get(CertTools.TYPE_SDK_AGENCY);
        String sdkNodeCertContent = certContents.get(CertTools.TYPE_SDK_NODE);
        // fisco's cert
        handleSaveFrontCertStr(chainCertContent);
        handleSaveFrontCertStr(agencyCertContent);
        handleSaveFrontCertStr(nodeCertContent);
        handleSaveFrontCertStr(encryptNodeCertContent);
        //sdk's cert
        handleSaveFrontCertStr(sdkChainCertContent);
        handleSaveFrontCertStr(sdkAgencyCertContent);
        handleSaveFrontCertStr(sdkNodeCertContent);
        log.debug("end saveFrontCert. certContents. ");
    }

    /**
     * handle saving Cert bare string
     * @param certStr
     * @throws CertificateException
     */
    public void handleSaveFrontCertStr(String certStr) throws CertificateException {
        if(StringUtils.isNotEmpty(certStr)) {
            certStr = CertTools.addCertHeadAndTail(certStr);
            log.debug("start handleSaveFrontCertStr:{} ", certStr);
            saveCerts(certStr);
            log.debug("end handleSaveFrontCertStr:{} ", certStr);
        }
    }

    /**
     * check Cert fingerPrint.
     */
    public boolean certFingerPrintExists(String fingerPrint) throws NodeMgrException {
        log.debug("start check certFingerPrintExists. fingerPrint:{} ", fingerPrint);
        if(StringUtils.isBlank(fingerPrint)) {
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
     */
    public List<X509Certificate> loadCertListFromCrtContent(String crtContent) {
        log.debug("loadCertListFromCrtContent content:{}", crtContent);
        List<X509Certificate> certs = new ArrayList<>();
        try(InputStream is = new ByteArrayInputStream(crtContent.getBytes())) {

            org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory factory =
                    new org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory();
            factory.engineGenerateCertificates(is).stream()
                    .forEach(c-> certs.add((X509Certificate)c));
//            CertificateFactory cf = CertificateFactory.getInstance("X.509");
//            certs = (List<X509Certificate>) cf.generateCertificates(is);
        }catch (CertificateException | IOException e) {
            log.error("loadCertListFromCrtContent exception:[]", e);
            throw new NodeMgrException(ConstantCode.CERT_ERROR.getCode(), e.getMessage());
        }
        log.debug("end loadCertListFromCrtContent. certs:{}", certs);
        return certs;
    }

    public X509Certificate loadSingleCertFromCrtContent(String crtContent) {
        log.debug("start loadSingleCertFromCrtContent. content:{}", crtContent);
        X509Certificate cert;
        try(InputStream is = new ByteArrayInputStream(crtContent.getBytes())) {
            org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory factory =
                    new org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory();
            cert = (X509Certificate) factory.engineGenerateCertificate(is);
        }catch (CertificateException | IOException e) {
            log.error("get loadSingleCertFromCrtContent. Exception:[]", e);
            throw new NodeMgrException(ConstantCode.CERT_ERROR.getCode(), e.getMessage());
        }
        log.debug("end loadSingleCertFromCrtContent. cert:{}", cert);
        return (X509Certificate)cert;
    }

    public int deleteAll() {
        log.info("delete all certs");
        return this.certMapper.deleteAll();
    }

    public Map<String, String> getFrontSdkContent(int frontId) {
        TbFront front = frontService.getById(frontId);
        if (front == null) {
            throw new NodeMgrException(ConstantCode.INVALID_FRONT_ID);
        }
        return frontInterfaceService.getSdkFilesFromSpecificFront(front.getFrontIp(), front.getFrontPort());
    }


    /**
     * get sdk cert key files' zip
     * @return
     */
    public synchronized FileContentHandle getFrontSdkZipFile(int frontId) {
        // get sdk cert content
        Map<String, String> sdkContentMap = this.getFrontSdkContent(frontId);
        if (sdkContentMap.isEmpty()) {
            throw new NodeMgrException(ConstantCode.SDK_CRT_KEY_FILE_NOT_FOUND);
        }
        // get if guomi sdk
        String key = sdkContentMap.keySet().iterator().next();
        boolean useGm = key.contains("gm");

        // if guomi, create conf/gm, else create conf/
        File sdkDir;
        if (useGm) {
            sdkDir = new File(TEMP_SDK_DIR + File.separator + "gm");
        } else {
            sdkDir = new File(TEMP_SDK_DIR);
        }
        log.info("writeSdkAsFile sdkDir:{}", sdkDir);
        // create dir and zip
        writeSdkFilesAndZip(sdkContentMap, sdkDir, useGm);
        try {
            // FileInputStream would be closed by web
            return new FileContentHandle(TEMP_ZIP_FILE_NAME, new FileInputStream(TEMP_ZIP_FILE_PATH));
        } catch (IOException e) {
            log.error("getFrontSdkFiles fail:[]", e);
            throw new NodeMgrException(ConstantCode.WRITE_SDK_CRT_KEY_FILE_FAIL);
        }
    }

    private void writeSdkFilesAndZip(Map<String, String> sdkContentMap, File sdkDir, boolean useGm) {
        this.writeSdkAsFile(sdkContentMap, sdkDir);
        // zip the directory of conf(guomi: conf/gm)
        String gmDirInZip = useGm ? "gm" : "";
        try {
            ZipUtils.generateZipFile(sdkDir.getPath(), TEMP_ZIP_DIR, gmDirInZip, TEMP_ZIP_FILE_NAME);
        } catch (Exception e) {
            log.error("writeSdkAsFile generateZipFile fail:[]", e);
            throw new NodeMgrException(ConstantCode.WRITE_SDK_CRT_KEY_FILE_FAIL);
        }

        // rm conf dir
        boolean resultDel = sdkDir.delete();
        log.info("delete for temp sdk file, result:{}", resultDel);
    }

    /**
     * write sdk file of ca.crt, sdk.crt, sdk.key
     * @param sdkContentMap
     * @param sdkDir sdk file output directory
     * @return
     */
    public void writeSdkAsFile(Map<String, String> sdkContentMap, File sdkDir) {

        // create sdk dir
        if (sdkDir.exists()) {
            boolean result = sdkDir.delete();
            log.info("delete existed gm dir, result:{}", result);
        }
        boolean result = sdkDir.mkdirs();
        log.info("mkdir for temp sdk file, result:{}", result);

        // write each content to each file in conf/ or conf/gm/
        // gm: gmca.crt, gmsdk.crt, gmsdk.key
        // else: ca.crt, sdk.crt, sdk.key
        for (String fileName : sdkContentMap.keySet()) {
            Path sdkFilePath = Paths
                .get(CleanPathUtil.cleanString(sdkDir.getPath() + File.separator + fileName));
            String fileContent = sdkContentMap.get(fileName);
            log.info("writeSdkAsFile sdkPath:{}, content:{}", sdkFilePath, fileContent);
            try (BufferedWriter writer = Files
                .newBufferedWriter(sdkFilePath, StandardCharsets.UTF_8)) {
                // write to relative path
                writer.write(fileContent);
            } catch (IOException e) {
                log.error("writeSdkAsFile fail:[]", e);
                throw new NodeMgrException(ConstantCode.WRITE_SDK_CRT_KEY_FILE_FAIL);
            }
        }
    }

//
//    public synchronized FileContentHandle getFrontSdkFiles(int frontId) {
//        Map<String, String> sdkContentMap = this.getFrontSdkContent(frontId);
//        if (sdkContentMap.isEmpty()) {
//            throw new NodeMgrException(ConstantCode.SDK_CRT_KEY_FILE_NOT_FOUND);
//        }
//        // get if guomi sdk
//        String key = sdkContentMap.keySet().iterator().next();
//        boolean useGm = key.contains("gm");
//        // create dir and zip
//        writeSdkAsFile(sdkContentMap, useGm);
//        try {
//            return new FileContentHandle(TEMP_ZIP_FILE_NAME, new FileInputStream(TEMP_ZIP_FILE_PATH));
//        } catch (IOException e) {
//            log.error("getFrontSdkFiles fail:[]", e);
//            throw new NodeMgrException(ConstantCode.WRITE_SDK_CRT_KEY_FILE_FAIL);
//        }
//    }
//
//    private void writeSdkAsFile(Map<String, String> sdkContentMap, boolean useGm) {
//        // if guomi, create conf/gm, else create conf/
//        File sdkDir;
//        if (useGm) {
//            sdkDir = new File(TEMP_SDK_DIR + File.separator + "gm");
//        } else {
//            sdkDir = new File(TEMP_SDK_DIR);
//        }
//        log.info("writeSdkAsFile sdkDir:{}", sdkDir);
//
//        // create sdk dir
//        if (sdkDir.exists()) {
//            boolean result = sdkDir.delete();
//            log.info("delete existed gm dir, result:{}", result);
//        }
//        boolean result = sdkDir.mkdirs();
//        log.info("mkdir for temp sdk file, result:{}", result);
//
//        // write each content to each file in conf/ or conf/gm/
//        // gm: gmca.crt, gmsdk.crt, gmsdk.key
//        // else: ca.crt, sdk.crt, sdk.key
//        for (String fileName : sdkContentMap.keySet()) {
//            Path sdkFilePath = Paths.get(sdkDir.getPath() + File.separator + CleanPathUtil.cleanString(fileName));
//            String fileContent = sdkContentMap.get(fileName);
//            log.info("writeSdkAsFile sdkPath:{}, content:{}", sdkFilePath, fileContent);
//            try (BufferedWriter writer = Files.newBufferedWriter(sdkFilePath, StandardCharsets.UTF_8)) {
//                // write to relative path
//                writer.write(fileContent);
//            } catch (IOException e) {
//                log.error("writeSdkAsFile fail:[]", e);
//                throw new NodeMgrException(ConstantCode.WRITE_SDK_CRT_KEY_FILE_FAIL);
//            }
//        }
//        // zip the directory of conf(guomi: conf/gm)
//        try {
//            generateZipFile(sdkDir.getPath(), TEMP_ZIP_DIR, useGm);
//        } catch (Exception e) {
//            log.error("writeSdkAsFile generateZipFile fail:[]", e);
//            throw new NodeMgrException(ConstantCode.WRITE_SDK_CRT_KEY_FILE_FAIL);
//        }
//
//        // rm conf dir
//        boolean resultDel = sdkDir.delete();
//        log.info("delete for temp sdk file, result:{}", resultDel);
//    }
//
//    /**
//     * @param path   要压缩的文件路径
//     * @param outputDir zip包的生成目录，默认为tempZip
//     * @param useGm if use gm, there is gm dir in zip
//     */
//    public static void generateZipFile(String path, String outputDir, boolean useGm) throws Exception {
//
//        File file2Zip = new File(CleanPathUtil.cleanString(path));
//        // 压缩文件的路径不存在
//        if (!file2Zip.exists()) {
//            log.error("file not exist:{}", path);
//            throw new Exception("file not exist: " + path);
//        }
//        // 用于存放压缩文件的文件夹
//        File compress = new File(outputDir);
//        // 如果文件夹不存在，进行创建
//        if (!compress.exists() ){
//            compress.mkdirs();
//        }
//        // 目的压缩文件，已存在则先删除
//        // tempZip/conf.zip
//        String generateFileName = compress.getAbsolutePath() + File.separator + CleanPathUtil.cleanString(TEMP_ZIP_FILE_NAME);
//        File confZip = new File(generateFileName);
//        if (confZip.exists() ) {
//            log.info("confZip exist, now delete:{}", confZip);
//            confZip.delete();
//        }
//        // 输出流
//        FileOutputStream outputStream = new FileOutputStream(CleanPathUtil.cleanString(generateFileName));
//        // 压缩输出流
//        ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(outputStream));
//        // 传入输出流，传入需要压缩的file路径
//        String gmDir = useGm ? "gm" : "";
//        generateFile(zipOutputStream, file2Zip, gmDir);
//
//        log.info("file2Zip:{} and outputFile:{}" ,file2Zip.getAbsolutePath(), generateFileName);
//        // 关闭 输出流
//        zipOutputStream.close();
//        outputStream.close();
//    }
//
//
//    /**
//     * @param out  输出流
//     * @param file 目标文件
//     * @param dir  在压缩包中的文件夹
//     * @throws Exception
//     */
//    private static void generateFile(ZipOutputStream out, File file, String dir) throws Exception {
//
//        // 当前的是文件夹，则进行一步处理
//        if (file.isDirectory()) {
//            //得到文件列表信息
//            File[] files = file.listFiles();
//
//            //将文件夹添加到下一级打包目录
//            out.putNextEntry(new ZipEntry(dir + "/"));
//
//            dir = dir.length() == 0 ? "" : dir + "/";
//
//            //循环将文件夹中的文件打包
//            for (int i = 0; i < files.length; i++) {
//                generateFile(out, files[i], dir + files[i].getName());
//            }
//
//        } else { // 当前是文件
//            FileInputStream inputStream = null;
//            try {
//                inputStream = new FileInputStream(file);
//                // 标记要打包的条目
//                out.putNextEntry(new ZipEntry(dir));
//                // 进行写操作
//                int len = 0;
//                byte[] bytes = new byte[1024];
//                while ((len = inputStream.read(bytes)) > 0) {
//                    out.write(bytes, 0, len);
//                }
//            } catch (IOException e) {
//                log.error("base64ToFile IOException:[{}]", e.toString());
//            } finally {
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//            }
//        }
//
//    }

}
