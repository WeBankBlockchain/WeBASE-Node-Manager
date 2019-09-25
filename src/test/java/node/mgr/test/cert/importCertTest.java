package node.mgr.test.cert;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.crypto.Cipher;

import com.webank.webase.node.mgr.base.tools.Web3Tools;
import org.fisco.bcos.web3j.crypto.Keys;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import sun.security.rsa.RSAPublicKeyImpl;
//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;

public class importCertTest {

    @Test
    public void testImport() throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException {
        InputStream sdkCa = new ClassPathResource("old/ca.crt").getInputStream();
        InputStream sdkNode = new ClassPathResource("old/node.crt").getInputStream();

        // read from file is
        InputStream pem = new ClassPathResource("agency/node.crt").getInputStream();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        // agency's crt
        List<X509Certificate> certs = (List<X509Certificate>) cf.generateCertificates(pem);
        // sdk crt
        List<X509Certificate> sdkCerts = (List<X509Certificate>) cf.generateCertificates(sdkNode);
        X509Certificate sdkNodeCrt = sdkCerts.get(0);
        X509Certificate sdkAgencyCrt = sdkCerts.get(1);
        X509Certificate sdkCaCert = (X509Certificate) cf.generateCertificate(sdkCa);


        for(X509Certificate c: certs) {
            System.out.println(c.getSubjectDN());
        }
        X509Certificate nodeCert = certs.get(0);
        X509Certificate agencyCert = certs.get(1);
        X509Certificate caCert = certs.get(2);
        //只能验证上一级
        agencyCert.verify(caCert.getPublicKey());
        nodeCert.verify(agencyCert.getPublicKey());
        // TODO 这里的publick key是包含非公钥文字的，待提取公钥
        String address = Web3Tools.getAddressByPublicKey(agencyCert.getPublicKey().toString());
//        RSAPublicKeyImpl agencyCertImpl = (RSAPublicKeyImpl) agencyCert.getPublicKey();
//        String address = Keys.getAddress(agencyCertImpl.getModulus().);
        // 和sdk的对比
        // 证书类型，证书名字
        System.out.println(nodeCert.getSubjectDN().toString().split(",")[0].split("=")[1]);
        System.out.println(nodeCert.getSubjectDN().toString().split(",")[2].split("=")[1]);
        System.out.println(address);
        System.out.println(agencyCert.getPublicKey().toString());
        System.out.println(sdkNodeCrt.getSubjectDN());
        System.out.println(sdkAgencyCrt.getSubjectDN());
        System.out.println(sdkCaCert.getSubjectDN());
        sdkNodeCrt.verify(sdkAgencyCrt.getPublicKey());
        // 不同的链，verify不通过
//        sdkNodeCrt.verify(agencyCert.getPublicKey());

        // 验签
        System.out.println();
        Signature signature = Signature.getInstance(sdkNodeCrt.getSigAlgName());
        signature.initVerify(sdkAgencyCrt.getPublicKey());
        // 此处应该传入子证书
        // 验签： 【原文】子证书.info.encoded (update) + 父证书's pub (initVerify) ==> 与子证书的signature比对
        // 签发： 【原文】xx data + 父证书's pri  ==> 子证书's signature
        signature.update(sdkNodeCrt.getEncoded());

        boolean tResult = signature.verify(sdkCaCert.getSignature());
        boolean tResult2 = signature.verify(agencyCert.getSignature());
        boolean tResult3 = signature.verify(sdkNodeCrt.getSignature());

        System.out.println(tResult);
        System.out.println(tResult2);
        System.out.println(tResult3);


//        Certificate certificate = cf.generateCertificate(pem);
//        X509Certificate oCert = (X509Certificate) certificate;
//        //主体部分
//        System.out.println("Version:"+oCert.getVersion());
//        System.out.println("SerialNumber:"+ oCert.getSerialNumber().toString(16));
//        System.out.println("【SigALgName】："+oCert.getSigAlgName() + "(" + oCert.getSigAlgOID() + ")");
//        System.out.println("【getSubjectX500Principal】："+oCert.getSubjectX500Principal());
//        System.out.println("【getIssuerX500Principal】：" + oCert.getIssuerX500Principal());
//        System.out.println("【getNotBefore】："+oCert.getNotBefore());
//        System.out.println("【getNotAfter】："+oCert.getNotAfter());
//        System.out.println("【getPublicKey】：\n  "+ oCert.getPublicKey());
//        System.out.println("【getAlgorithm】："+ oCert.getPublicKey().getAlgorithm());
//        System.out.println("【getFormat】："+oCert.getPublicKey().getFormat());
//
//        PublicKey publicKey = oCert.getPublicKey();
//        BASE64Encoder base64Encoder=new BASE64Encoder();
//        String publicKeyString = base64Encoder.encode(publicKey.getEncoded());
//        System.out.println("-----------------getPublicKey-after-base64-encode--------------------");
//        System.out.println(publicKeyString);
//        System.out.println("-----------------getPublicKey--------------------");
    }

//    public static String getHash(String data) throws NoSuchAlgorithmException, UnsupportedEncodingException {
//        MessageDigest md = MessageDigest.getInstance("SHA1");
//        md.update(data.getBytes("UTF-8"));
//
//        byte byteData[] = md.digest();
//
//        BASE64Encoder encoder = new BASE64Encoder();
//        String base64CheckSum = encoder.encode(byteData);
//        //System.out.println("base64:" + base64CheckSum);
//        return base64CheckSum;
//    }

    public static boolean isExpired2(X509Certificate cert) {
        try {
            cert.checkValidity();
            return false;
        } catch (CertificateExpiredException e) {
            System.out.println("Certificate Expired");
            return true;
        } catch (CertificateNotYetValidException e) {
            System.out.println("Certificate Not Yet Valid");
            return true;
        } catch (Exception e) {
            System.out.println("Error checking Certificate Validity.  See admin.");
            return true;
        }
    }
}
