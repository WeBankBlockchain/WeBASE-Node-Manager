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

    private static byte[] isStore = null;
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

    public static String getCertAddress(String publicKey) {
        return Web3Tools.getAddressByPublicKey(publicKey);
    }



//    public static InputStream getStream(String string) throws IOException {
//        InputStream is = new ByteArrayInputStream(string.getBytes());
//        isStore = saveInputStream(is);
//        return is;
//    }

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


    public static String formatStr(String string) {
        return string.substring(0, string.length() - 1);
    }
}
