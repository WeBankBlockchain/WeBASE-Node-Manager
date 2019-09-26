package com.webank.webase.node.mgr.cert;


import com.webank.webase.node.mgr.base.tools.Web3Tools;
import org.springframework.core.io.ClassPathResource;
import sun.security.ec.ECPublicKeyImpl;
import sun.security.x509.X509CertImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CertTools {
    private final static String flag = "-----" ;
    private final static String head = "-----BEGIN CERTIFICATE-----\n" ;
    private final static String tail = "-----END CERTIFICATE-----\n" ;


    /**
     * 获取证书类型 和 名字
     * @return
     * @throws IOException
     */
    public static String  getCertType(Principal subjectDN) {
        return subjectDN.toString().split(",")[0].split("=")[1];
    }
    public static String  getCertName(Principal subjectDN) {
        return subjectDN.toString().split(",")[2].split("=")[1];
    }

    /**
     * getPublicKey
     * 获取了byte[]之后 转换成base64编码 == address?
     * @param key
     * @return String
     */
    public static String getPublicKeyString(PublicKey key) {
        ECPublicKeyImpl pub = (ECPublicKeyImpl) key;
        byte[] pubBytes = pub.getEncodedPublicValue();
        //Base64 Encoded
        String encoded = Base64.getEncoder().encodeToString(pubBytes);
        return encoded;
    }
    // TODO get cert's public key
    public static String getCertAddress(String publicKey) {
        return Web3Tools.getAddressByPublicKey(publicKey);
    }
    /**
     * 解析is获取证书list
     * @return
     * @throws IOException
     */
    public static List<X509CertImpl> getCerts(String crtContent) throws IOException, CertificateException {
        InputStream is = getStream(crtContent);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        List<X509CertImpl> certs = (List<X509CertImpl>) cf.generateCertificates(is);
        return certs;
    }
    public static X509CertImpl getCert(String crtContent) throws IOException, CertificateException {
        InputStream is = getStream(crtContent);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509CertImpl cert = (X509CertImpl) cf.generateCertificate(is);
        return cert;
    }


    public static String getString(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[0];
        bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        String str = new String(bytes);
        return str;
    }

    public static InputStream getStream(String string) throws IOException {
        InputStream is = new ByteArrayInputStream(string.getBytes());
        return is;
    }

    // 0 is node ca, 1 is agency ca, 2 is chain
    public static List<String> getSingleCrtContent(String certContent) throws IOException {
        List<String> list = new ArrayList<>();
        String[] nodeCrtStrArray = certContent.split(head);
        for(int i = 0; i < nodeCrtStrArray.length; i++) {
            String[] nodeCrtStrArray2 = nodeCrtStrArray[i].split(tail);
            for(int j = 0; j < nodeCrtStrArray2.length; j++) {
                String ca = nodeCrtStrArray2[j];
                if(ca.length() != 0) {
                    list.add(formatStr(ca));
                }
            }
        }
        return list;
    }

    public String getChainCrt() throws IOException {
        InputStream caInput = new ClassPathResource("ca.crt").getInputStream();
        String caStr = getString(caInput);
        String ca = "";
        String[] caStrArray = caStr.split(head); // 一个是空，一个是去除了head的string
        for(int i = 0; i < caStrArray.length; i++) { //i=0时为空，跳过，i=1时进入第二次spilt，去除tail
            String[] caStrArray2 = caStrArray[i].split(tail); // i=1时，j=0是string, 因为\n去除了换行符，不包含j=1的情况
            for(int j = 0; j < caStrArray2.length; j++) {
                ca = caStrArray2[j];
                if(ca.length() != 0) {
                    ca = formatStr(ca);
                }
            }
        }
        return ca;
    }

    public String getFirstCrt(String string) {
        String[] strArray = string.split(flag);
        return strArray[2];
    }

    public String getSecondCrt(String string) {
        String[] strArray = string.split(flag);
        return strArray[6];
    }

    public static String formatStr(String string) {
        return string.substring(0, string.length() - 1);
    }
}
