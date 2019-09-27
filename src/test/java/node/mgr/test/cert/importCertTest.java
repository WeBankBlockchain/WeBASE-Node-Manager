package node.mgr.test.cert;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

import com.webank.webase.node.mgr.cert.CertTools;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.utils.Numeric;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import sun.security.ec.ECPublicKeyImpl;
import sun.security.x509.X509CertImpl;

import static com.webank.webase.node.mgr.cert.CertTools.byteToHex;

public class importCertTest {
    private final static String flag = "-----" ;
    private final static String head = "-----BEGIN CERTIFICATE-----\n" ;
    private final static String tail = "-----END CERTIFICATE-----\n" ;

    @Test
    public void testPubAddress() throws IOException, CertificateException {
        /**
         * @param: nodeCert
         * 只有节点证书才是ECC椭圆曲线，获取pub的方法和区块链的一致
         * 其余的agency chain 的crt都是rsa方法，使用大素数方法计算，不一样
         */
        InputStream node = new ClassPathResource("online/node-single.crt").getInputStream();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509CertImpl nodeCert = (X509CertImpl) cf.generateCertificate(node);
        // rsa算法的公钥和ecc的不一样
        ECPublicKeyImpl pub = (ECPublicKeyImpl) nodeCert.getPublicKey();
        byte[] pubBytes = pub.getEncodedPublicValue();
        String publicKey = Numeric.toHexStringNoPrefix(pubBytes);
        String address = Keys.getAddress(publicKey);
        byte[] addByteArray = Keys.getAddress(pubBytes);
        System.out.println("byte[] : pub ");
        System.out.println(pubBytes);
        System.out.println("====================================");
        System.out.println(publicKey); // 04e5e7efc9e8d5bed699313d5a0cd5b024b3c11811d50473b987b9429c2f6379742c88249a7a8ea64ab0e6f2b69fb8bb280454f28471e38621bea8f38be45bc42d
        System.out.println("byte[] to pub to address ");
        System.out.println(address); // f7b2c352e9a872d37a427601c162671202416dbc
        System.out.println("包含开头的04");
        System.out.println(byteToHex(addByteArray));
    }

    /**
     * address到底需不需要传入pub的开头的两位04
     * 答案： 不需要，公钥是128位的
     */
    @Test
    public void testAddress() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        ECKeyPair key = Keys.createEcKeyPair();
        byte[] pubBytes = key.getPublicKey().toByteArray();
        System.out.println("=============原生的==============");
        System.out.println(key.getPublicKey());
        System.out.println(Keys.getAddress(key.getPublicKey()));

        System.out.println("===========通过转成hex后台获取地址============");
        System.out.println(Numeric.toHexStringNoPrefix(key.getPublicKey()));
        System.out.println(Keys.getAddress(Numeric.toHexStringNoPrefix(key.getPublicKey())));

        System.out.println("===========通过byte[]============");
        System.out.println(Numeric.toHexStringNoPrefix(pubBytes));
        System.out.println(Keys.getAddress(Numeric.toHexStringNoPrefix(pubBytes)));
    }

    @Test
    public void testImport() throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException {
//        InputStream sdkCa = new ClassPathResource("old/ca.crt").getInputStream();
//        InputStream sdkNode = new ClassPathResource("old/node.crt").getInputStream();
//        // sdk crt
//        List<X509CertImpl> sdkCerts = (List<X509CertImpl>) cf.generateCertificates(sdkNode);
//        X509CertImpl sdkNodeCrt = sdkCerts.get(0);
//        X509CertImpl sdkAgencyCrt = sdkCerts.get(1);
//        X509CertImpl sdkCaCert = (X509CertImpl) cf.generateCertificate(sdkCa);

        // read from file is
        InputStream pem = new ClassPathResource("agency/node.crt").getInputStream();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        // agency's crt
        List<X509CertImpl> certs = (List<X509CertImpl>) cf.generateCertificates(pem);


        for(X509CertImpl c: certs) {
            System.out.println(c.getSubjectDN());
        }
        X509CertImpl nodeCert = certs.get(0);
        X509CertImpl agencyCert = certs.get(1);
        X509CertImpl caCert = certs.get(2);
        //只能验证上一级
        agencyCert.verify(caCert.getPublicKey());
        nodeCert.verify(agencyCert.getPublicKey());


//        BASE64Encoder base64Encoder=new BASE64Encoder();
//        String publicKeyString = base64Encoder.encode(nodeCert.getPublicKey().getEncoded());
//        System.out.println("-----------------getPublicKey-after-base64-encode--------------------");
//        System.out.println(publicKeyString);
//        System.out.println(Web3Tools.getAddressByPublicKey(publicKeyString));
//        System.out.println(address);
//        System.out.println("-----------------getPublicKey--------------------");

        /**
         *   对比证书getEncoded之后 base64的值,和crt里的内容是否一致
         *   答案: 不一致
         */

//        System.out.println("-----------------getContent-after-base64-encode--------------------");
//        String certString = base64Encoder.encode(nodeCert.getEncoded());
//        System.out.println(certString);
//        System.out.println("-----------------getContent--------------------");
        // 和sdk的对比
        /**
         *  根据subjectDN 获取证书类型，证书名字
         */

//        System.out.println(nodeCert.getSubjectDN().toString().split(",")[0].split("=")[1]);
//        System.out.println(nodeCert.getSubjectDN().toString().split(",")[2].split("=")[1]);
//        System.out.println(address);
//        System.out.println(agencyCert.getPublicKey().toString());
//        System.out.println(sdkNodeCrt.getSubjectDN());
//        System.out.println(sdkAgencyCrt.getSubjectDN());
//        System.out.println(sdkCaCert.getSubjectDN());
//        sdkNodeCrt.verify(sdkAgencyCrt.getPublicKey());
        // 不同的链，verify不通过
//        sdkNodeCrt.verify(agencyCert.getPublicKey());

        /**
         * 验签
          */
//        System.out.println();
//        Signature signature = Signature.getInstance(nodeCert.getSigAlgName());
//        signature.initVerify(nodeCert.getPublicKey());
//        // 此处应该传入子证书
//        // 验签： 【原文】子证书.info.encoded (update) + 父证书's pub (initVerify) ==> 与子证书的signature比对
//        // 签发： 【原文】xx data + 父证书's pri  ==> 子证书's signature
//        signature.update((byte[])nodeCert.get("x509"));
//
//        boolean tResult = signature.verify(caCert.getSignature());
//        boolean tResult2 = signature.verify(agencyCert.getSignature());
//        boolean tResult3 = signature.verify(nodeCert.getSignature());
//
//        System.out.println(tResult);
//        System.out.println(tResult2);
//        System.out.println(tResult3);

    }

    @Test
    public void getSingleCrtContent() throws IOException {
        // read from file is
        InputStream pem = new ClassPathResource("agency/node.crt").getInputStream();
        String certContent = getString(pem);
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
        for(String s: list) {
            System.out.println(s);
            System.out.println();
        }
    }
    public static String formatStr(String string) {
        return string.substring(0, string.length() - 1);
    }
    public static String getString(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[0];
        bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        String str = new String(bytes);
        return str;
    }

    public String getFingerPrint(X509CertImpl cert)throws Exception {
        // 指纹
        /**
         * Microsoft's presentation of certificates is a bit misleading
         * because it presents the fingerprint as if it was contained in the certificate
         * but actually Microsoft has to calculate the fingerprint, too.
         * This is especially misleading because a certificate actually has many fingerprints,
         * and Microsoft only displays the fingerprint it seems to use internally, i.e. the SHA-1 fingerprint.
         */
        String finger = cert.getFingerprint("SHA-1");
        System.out.println("指纹");
        System.out.println(finger);
        return finger;
//        private static String getThumbprint(X509Certificate cert)
//            throws NoSuchAlgorithmException, CertificateEncodingException {
//            MessageDigest md = MessageDigest.getInstance("SHA-1");
//            byte[] der = cert.getEncoded();
//            md.update(der);
//            byte[] digest = md.digest();
//            String digestHex = DatatypeConverter.printHexBinary(digest);
//            return digestHex.toLowerCase();
//        }
    }

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
