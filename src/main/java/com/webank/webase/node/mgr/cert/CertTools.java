package com.webank.webase.node.mgr.cert;


import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
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
    public String  getCertType(String subjectDN) {
        return subjectDN.split(",")[0].split("=")[1];
    }
    public String  getCertName(String subjectDN) {
        return subjectDN.split(",")[2].split("=")[1];
    }

    /**
     * 解析is获取证书list
     * @return
     * @throws IOException
     */
    public List<X509Certificate> getCerts(InputStream is) throws IOException, CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        // agency's crt
        List<X509Certificate> certs = (List<X509Certificate>) cf.generateCertificates(is);
        return certs;
    }


    public String getString(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[0];
        bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        String str = new String(bytes);
        return str;
    }

    public InputStream getStream(String string) throws IOException {
        InputStream is = new ByteArrayInputStream(string.getBytes());
        return is;
    }

    // 0 is node ca, 1 is agency ca
    public List<String> getNodeCrt() throws IOException {
        List<String> list = new ArrayList<>();
        InputStream nodeCrtInput = new ClassPathResource("node.crt").getInputStream();
        String nodeCrtStr = getString(nodeCrtInput);

        String[] nodeCrtStrArray = nodeCrtStr.split(head);
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

    public String formatStr(String string) {
        return string.substring(0, string.length() - 1);
    }
}
