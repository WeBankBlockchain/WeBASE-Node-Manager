package com.webank.webase.node.mgr.cert;


import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.tools.Web3Tools;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.utils.Numeric;
import org.springframework.core.io.ClassPathResource;
import sun.security.ec.ECPublicKeyImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
    private final static String tailForConcat = "\n-----END CERTIFICATE-----\n" ;

    public final static String TYPE_CHAIN = "chain";
    public final static String TYPE_AGENCY = "agency";
    public final static String TYPE_NODE = "node";
    // 首次启动时需要拉取
    public static boolean pullFrontCertsDone = false;
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
     * 给cert的内容加上头和尾
     * begin ...
     * end ...
     */
    public static String addHeadAndTail(String certContent) {
        String headToConcat = head;
        String fullCert = headToConcat.concat(certContent).concat(tailForConcat);
        return fullCert;
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
        String publicKey = Numeric.toHexStringNoPrefix(pubBytes);
        publicKey = publicKey.substring(2); //128位,去除前两位
//        String address = Keys.getAddress(publicKey);
        return publicKey;
    }

    public static String getAddress(PublicKey key) {
        ECPublicKeyImpl pub = (ECPublicKeyImpl) key;
        byte[] pubBytes = pub.getEncodedPublicValue();
        String publicKey = Numeric.toHexStringNoPrefix(pubBytes);
        publicKey = publicKey.substring(2); //128位
        String address = Keys.getAddress(publicKey);
        return address;
    }

    public static String getCertAddress(String publicKey) {
        return Web3Tools.getAddressByPublicKey(publicKey);
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
        if(!certContent.startsWith(head)){
            throw new NodeMgrException(ConstantCode.CERT_FORMAT_ERROR);
        }
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

    /**
     * byte数组转hex
     * @param bytes
     * @return
     */
    public static String byteToHex(byte[] bytes){
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            strHex = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().trim();
    }
}
