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
package node.mgr.test.cert;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import io.jsonwebtoken.lang.Assert;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.utils.Numeric;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import sun.security.ec.ECPublicKeyImpl;

import static com.webank.webase.node.mgr.base.tools.CertTools.byteToHex;

/**
 * test load non-guomi cert and guomi cert
 * using java.security.cert.CertificateFactory getInstance("X.509");
 * 2019/12
 * replace java.security.cert.CertificateFactory
 * with org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory
 */
public class ImportCertTest {
    private final static String head = "-----BEGIN CERTIFICATE-----\n" ;
    private final static String tail = "-----END CERTIFICATE-----\n" ;


    @Test
    public void testPubAddress() throws IOException, CertificateException, IllegalAccessException, InstantiationException {
        /**
         * @param: nodeCert
         * 只有节点证书才是ECC椭圆曲线，获取pub的方法和区块链的一致
         * 其余的agency chain 的crt都是rsa方法，使用大素数方法计算，不一样
         */
        // need crt file
        InputStream node = new ClassPathResource("node.crt").getInputStream();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate nodeCert = (X509Certificate) cf.generateCertificate(node);
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
        // 用byte[]穿进去获取公钥，就会可能多出一位0
        byte[] pubBytes = key.getPublicKey().toByteArray();
        System.out.println("=============原生的==============");
        System.out.println(key.getPublicKey()); //64bytes BigInteger
        System.out.println(Keys.getAddress(key.getPublicKey()));

        System.out.println("===========通过转成hex后获取地址============");
        System.out.println(Numeric.toHexStringNoPrefix(key.getPublicKey())); //Hex后显示
        System.out.println(Keys.getAddress(Numeric.toHexStringNoPrefix(key.getPublicKey())));

        System.out.println("===========通过byte[]============");
        System.out.println(Numeric.toHexStringNoPrefix(pubBytes)); // BigInteget=> byte[] => hex 多一位
        System.out.println(Keys.getAddress(Numeric.toHexStringNoPrefix(pubBytes)));
        System.out.println("===============");
//        System.out.println(Keys.getAddress(pubBytes));
    }

    @Test
    public void testImport() throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException {
        // read from file is
        InputStream pem = new ClassPathResource("node.crt").getInputStream();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        // agency's crt
        List<X509Certificate> certs = (List<X509Certificate>) cf.generateCertificates(pem);


        for(X509Certificate c: certs) {
            System.out.println(c.getSubjectDN());
        }
        X509Certificate nodeCert = certs.get(0);
//        X509Certificate agencyCert = certs.get(1);
//        X509Certificate caCert = certs.get(2);
//        //只能验证上一级
//        agencyCert.verify(caCert.getPublicKey());
//        nodeCert.verify(agencyCert.getPublicKey());

        /**
         *  根据subjectDN 获取证书类型，证书名字
         */

    }

    @Test
    public void getSingleCrtContent() throws IOException {
        // read from file is
        InputStream pem = new ClassPathResource("node.crt").getInputStream();
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

    public String getFingerPrint(X509Certificate cert)throws Exception {
        // 指纹
        /**
         * Microsoft's presentation of certificates is a bit misleading
         * because it presents the fingerprint as if it was contained in the certificate
         * but actually Microsoft has to calculate the fingerprint, too.
         * This is especially misleading because a certificate actually has many fingerprints,
         * and Microsoft only displays the fingerprint it seems to use internally, i.e. the SHA-1 fingerprint.
         */
        String finger = NodeMgrTools.getCertFingerPrint(cert.getEncoded());
        System.out.println("指纹");
        System.out.println(finger);
        return finger;
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

    /**
     * import guomi node cert list
     * @throws CertificateEncodingException
     */
    @Test
    public void testLoadCertList() throws CertificateException, IOException {
        // need gmnode.crt file
        InputStream nodes = new ClassPathResource("sdk_node.crt").getInputStream();
        org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory factory =
                new org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory();
        List<X509Certificate> certs = (List<X509Certificate>) factory.engineGenerateCertificates(nodes);
        Assert.notNull(certs);
        certs.stream().forEach(c -> {
            System.out.println(c.getSubjectDN());
            try {
                System.out.println(NodeMgrTools.getCertFingerPrint(c.getEncoded()));
            } catch (CertificateEncodingException e) {
                e.printStackTrace();
            }
        });
    }
}
