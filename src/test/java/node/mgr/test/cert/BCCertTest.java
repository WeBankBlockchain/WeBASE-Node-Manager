/**
 * Copyright 2014-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package node.mgr.test.cert;

import com.webank.webase.node.mgr.base.tools.CertTools;
import com.webank.webase.node.mgr.base.tools.NodeMgrTools;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.fisco.bcos.web3j.utils.Numeric;
import org.junit.Test;
import org.springframework.util.Assert;
import sun.security.ec.ECPublicKeyImpl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import java.util.List;



/**
 * factory: org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory
 * instance: java.security.cert.X509Certificate
 * replace java.security.X509.X509CertImpl which is deprecated
 */
public class BCCertTest {

    public final static String gmNodeCrt = "MIIBfDCCASKgAwIBAgIJAI98D486PmWFMAoGCCqBHM9VAYN1MDYxDzANBgNVBAMM\nBmFnZW5jeTESMBAGA1UECgwJZmlzY29iY29zMQ8wDQYDVQQLDAZhZ2VuY3kwHhcN\nMTkxMTI1MDM0MTExWhcNMjkxMTIyMDM0MTExWjA1MQ4wDAYDVQQDDAVub2RlMDES\nMBAGA1UECgwJZmlzY29iY29zMQ8wDQYDVQQLDAZhZ2VuY3kwWTATBgcqhkjOPQIB\nBggqgRzPVQGCLQNCAAS0TE9xO0C2ftL52uLvIIgIt/MREu/hqCLeBGYE/gu9/99G\nt8DPHKmqRodS1RSVjLYDZwptNFx4+v9uadgl6FG4oxowGDAJBgNVHRMEAjAAMAsG\nA1UdDwQEAwIGwDAKBggqgRzPVQGDdQNIADBFAiEAqF6J+H31mW0vcbGxFBjAM2Ue\n9GFZNrpS5UjFRDxFAcsCIFuzUNWBo/y1ySxSftxBFPd5I/iHedSFKPybcxQilvqt";
    public final static String standardNodeCrt = "MIICOTCCASGgAwIBAgIJAKHsAYI3TsAOMA0GCSqGSIb3DQEBCwUAMDgxEDAOBgNV\nBAMMB2FnZW5jeUExEzARBgNVBAoMCmZpc2NvLWJjb3MxDzANBgNVBAsMBmFnZW5j\neTAeFw0xOTA3MTIwMjA2MTZaFw0yOTA3MDkwMjA2MTZaMDIxDDAKBgNVBAMMA3Nk\nazETMBEGA1UECgwKZmlzY28tYmNvczENMAsGA1UECwwEbm9kZTBWMBAGByqGSM49\nAgEGBSuBBAAKA0IABJ79rSKIb97xZwByW58xH6tzoNKNLaKG7J5wxAEgAb03O2h4\nMkEMLtf/LB7tELOiyCiIEhLScprb1LjvDDt2RDGjGjAYMAkGA1UdEwQCMAAwCwYD\nVR0PBAQDAgXgMA0GCSqGSIb3DQEBCwUAA4IBAQC0u2lfclRmCszBTi2rtvMibZec\noalRC0sQPBPRb7UQhGCodxmsAT3dBUf+s4wLLrmN/cnNhq5HVObbWxzfu7gn3+IN\nyQEeqdbGdzlu1EDcaMgAz6p2W3+FG/tmx/yrNza29cYekWRL44OT5LOUPEKrJ4bJ\neOBRY4QlwZPFmM0QgP7DoKxHXldRopkmvqT4pbW51hWvPgj7KrdqwbVWzuWQuI3i\n3j3O96XZJsaDZ0+IGa5093+TsTNPfWUZzp5Kg+EyNR6Ea1evuMDNq9NAqqcd5bX9\nO9kgkb8+llO8I5ZhdnN0BuhGvv9wpsa9hW8BImOLzUBwfSVYouGCkoqlVq9X";
    public final static String standardAgencyCrt = "MIIDADCCAeigAwIBAgIJAJUF2Dp1a9U6MA0GCSqGSIb3DQEBCwUAMDUxDjAMBgNV\nBAMMBWNoYWluMRMwEQYDVQQKDApmaXNjby1iY29zMQ4wDAYDVQQLDAVjaGFpbjAe\nFw0xOTA3MTIwMjA2MTZaFw0yOTA3MDkwMjA2MTZaMDgxEDAOBgNVBAMMB2FnZW5j\neUExEzARBgNVBAoMCmZpc2NvLWJjb3MxDzANBgNVBAsMBmFnZW5jeTCCASIwDQYJ\nKoZIhvcNAQEBBQADggEPADCCAQoCggEBANBT4CTciIYdSeEabgJzif+CFB0y3GzG\ny+XQYtWK+TtdJWduXqhnnZiYAZs7OPGEu79Yx/bEpjEXsu2cXH0D6BHZk+wvuxG6\nezXWq5MYjCw3fQiSRWkDYoxzWgulkRyYROF1xoZeNGQssReFmCgP+pcQwRxjcq8z\nIA9iT61YxyW5nrS7xnra9uZq/EE3tsJ0ae3ax6zixCT66aV49S27cMcisS+XKP/q\nEVPxhO7SUjnzZY69MgZzNSFxCzIbapnlmYAOS26vIT0taSkoKXmIsYssga45XPwI\n7YBVCc/34kHzW9xrNjyyThMWOgDsuBqZN9xvapGSQ82Lsh7ObN0dZVUCAwEAAaMQ\nMA4wDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAQEAu3aHxJnCZnICpHbQ\nv1Lc5tiXtAYE9aEP5cxb/cO14xY8dS+t0wiLIvyrE2aTcgImzr4BYNBm1XDt5suc\nMpzha1oJytGv79M9/WnI/BKmgUqTaaXOV2Ux2yPX9SadNcsD9/IbrV0b/hlsPd6M\nK8w7ndowvBgopei+A1NQY6jTDUKif4RxD4u5HZFWUu7pByNLFaydU4qBKVkucXOq\nxmWoupL5XrDk5o490kiz/Zgufqtb4w6oUr3lrQASAbFB3lID/P1ipi0DwX7kZwVX\nECDLYvr+eX6GbTClzn0JGuzqV4OoRo1rrRv+0tp1aLZKpCYn0Lhf6s1iw/kCeM2O\nnP9l2Q==";
    public final static String standardChainCrt = "MIIDPTCCAiWgAwIBAgIJAMfvnu4d5fHdMA0GCSqGSIb3DQEBCwUAMDUxDjAMBgNV\nBAMMBWNoYWluMRMwEQYDVQQKDApmaXNjby1iY29zMQ4wDAYDVQQLDAVjaGFpbjAe\nFw0xOTA3MTIwMjA2MTZaFw0yOTA3MDkwMjA2MTZaMDUxDjAMBgNVBAMMBWNoYWlu\nMRMwEQYDVQQKDApmaXNjby1iY29zMQ4wDAYDVQQLDAVjaGFpbjCCASIwDQYJKoZI\nhvcNAQEBBQADggEPADCCAQoCggEBAMGsKT/S60cxvFS4tBLyfT0QKPLW1g3ZgMND\n03hrWp1FAnvE9htsDEgqvNLD5hKWaYcUhjQMq0WttiP/vPxkwwJkZhzWhXpdSxMR\nqKVX4BppnkT0ICm84jYSyJdNFjKvfWlBIptIfFuTUDMT+XqF/Ct756JksiUwKZRW\neRAVcYzFM4u4ZuKeaept/8Bv8Z/RlJzGI57qj5BELeA0meUagq2WoCgJrPyvbO0b\nLwogFWS4kEjv20IIdj3fTqeJlooEXtPnuegunSMQB6aIh2im74FfJ3sHuOjQDFuC\nb5ZUiyUHG6IOGCqs+Grk+/VYI16Mx+8OoGBD5koTpK8B+/aedsUCAwEAAaNQME4w\nHQYDVR0OBBYEFLTg2FsUFekx9XjIi01BrDpo0aPIMB8GA1UdIwQYMBaAFLTg2FsU\nFekx9XjIi01BrDpo0aPIMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEB\nAJmuMLhWSld8G6i3Vw21TN/d2rSRg3hNqOyycPYtdVK1YXEj4Xm91qgL8An3Kui8\njSq1S9+PstGvyh14YUw43Y1VtEPGpNVTvDtkxQ/8rs1sGHbqUxshgFMbqruxp7WH\ns0fxgn5COHEnRC4jQn02wZAk8pIjFVZLkhqdIYBtC35buHr17mXNL0S4H5cJxzPN\nk3XtKBqXedkTrEsDhR/bZ6qDDq0BcduhtKiYVPiVw9z3moLuwDb0QDM59zCexpcz\nb/w7K4lIxWqpD5tbpKSmj/3v5TCqez0Mim8/j4q29bP913KQrngnVCdCezOsPWIH\nDDoihgeRQHuz1VuGGZ259Hc=";

    //guomi's sdk cert is not guomi
    public final static String gmSdkCrt = "MIICNzCCAR+gAwIBAgIJAOCh/7JoVFs3MA0GCSqGSIb3DQEBCwUAMDcxDzANBgNV\n" +
            "BAMMBmFnZW5jeTETMBEGA1UECgwKZmlzY28tYmNvczEPMA0GA1UECwwGYWdlbmN5\n" +
            "MB4XDTE5MTEyNTAzNDExMVoXDTI5MTEyMjAzNDExMVowMTEMMAoGA1UEAwwDc2Rr\n" +
            "MRMwEQYDVQQKDApmaXNjby1iY29zMQwwCgYDVQQLDANzZGswVjAQBgcqhkjOPQIB\n" +
            "BgUrgQQACgNCAAR4U4lvTksIkcWVTVs/dzJeOXIHGOyOGlYI5Oh3S92w3N1jqY2t\n" +
            "bCdmAxc2dMR38iaeersee4sem5yFLCetfAgUoxowGDAJBgNVHRMEAjAAMAsGA1Ud\n" +
            "DwQEAwIF4DANBgkqhkiG9w0BAQsFAAOCAQEAr6Frxn/pLNnG3aRV+aGulXXS7kYr\n" +
            "Tc6CIZ8O+2m758+A3rWN6GHuKEZ0kYy/h2vYjpgpezVXRElmuON/LbyX+hvBXtxY\n" +
            "aAaTFR/OLiCJ6UiRISSnRRbvl6nClmHeZjf4gddvEqAxicKGGWxmek2FXDzxvA11\n" +
            "h4vfNEnxatLqtlM5hSZkNIYrYCiSaXFE0w2UB4uuIoLrcfE+YR4nYxISNdSKpYjh\n" +
            "BXwVuJgMRaQXI4njL4Rd6X+f7wA6lBJBMPmj+YWHdWTQCpUOe5yYxj7o7yEPtmJr\n" +
            "MqZAO+R9xoTWsifsjHyD3AuUxd3bQBqojih4C89tvtww+4FDzmnC3R/4pg==";

    /**
     * 加载国密证书，并打印指纹
     * @throws CertificateException
     */
    @Test
    public void testCertFingerprint() throws CertificateException {
        String cert = CertTools.addCertHeadAndTail(gmNodeCrt);
        // print fingerprint
        loadCert(new ByteArrayInputStream(cert.getBytes()));
    }

    /**
     * get guomi cert, use bouncy castle
     * @param is
     * @throws CertificateException
     */
    private void loadCert(InputStream is) throws CertificateException {
        CertificateFactory factory = new CertificateFactory();
        X509Certificate cert = (X509Certificate) factory.engineGenerateCertificate(is);
        System.out.println(cert.getType());
        System.out.println(cert.getPublicKey());
        System.out.println(cert.getSubjectDN());
        System.out.println("guomi node fingerPrint");
        String fingerPrint = NodeMgrTools.getCertFingerPrint(cert.getEncoded());
        System.out.println(fingerPrint);
        Assert.notNull(fingerPrint, "finer print null");
    }

    @Test
    public void getCertContent() {
        String certCa = CertTools.addCertHeadAndTail(standardChainCrt);
        String certAgency = CertTools.addCertHeadAndTail(standardAgencyCrt);
        String certNode = CertTools.addCertHeadAndTail(standardNodeCrt);
        System.out.println("===getCertContent");
        System.out.println(certCa);
        System.out.println(certAgency);
        System.out.println(certNode);
    }

    /**
     * use sun.security 获取指纹 限定 非国密 cert
     */
    @Test
    public void getCertFingerPrint() throws CertificateException {
        java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
        String cert = CertTools.addCertHeadAndTail(standardNodeCrt);
        System.out.println(cert);
        // guomi's EC curve not match X509CertImpl
        List<X509Certificate> certs = (List<X509Certificate>) cf.generateCertificates(new ByteArrayInputStream(cert.getBytes()));
        Assert.notNull(certs, "certs are null");

        if (certs.size() != 0) {
            System.out.println("===standard cert");
            System.out.println("SHA-1 fingerPrint");
            System.out.println(NodeMgrTools.getCertFingerPrint(certs.get(0).getEncoded()));

        }
    }

    @Test
    public void testGmCertPublicKey() throws CertificateException {
        String crtContent = CertTools.addCertHeadAndTail(standardNodeCrt);
        InputStream is2 = new ByteArrayInputStream(crtContent.getBytes());
        CertificateFactory factory = new CertificateFactory();
        X509Certificate certificate = (X509Certificate)factory.engineGenerateCertificate(is2);
        BCECPublicKey bcecPublicKey = (BCECPublicKey) certificate.getPublicKey();
        System.out.println(bcecPublicKey.getEncoded());
        byte[] bcecPubBytes = bcecPublicKey.getEncoded();
        String publicKeyBC = Numeric.toHexStringNoPrefix(bcecPubBytes);
        publicKeyBC = publicKeyBC.substring(publicKeyBC.length() - 128); //证书byte[]为130位，只取128位，去除开头的04标记位
        System.out.println(publicKeyBC); // 3056301006072a8648ce3d020106052b8104000a034200047853896f4e4b0891c5954d5b3f77325e39720718ec8e1a5608e4e8774bddb0dcdd63a98dad6c276603173674c477f2269e7abb1e7b8b1e9b9c852c27ad7c0814

        //========== import java.security.cert.CertificateFactory;
        /** @Deprecated */
        InputStream is = new ByteArrayInputStream(crtContent.getBytes());
        java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
        X509Certificate cert2 = (X509Certificate) cf.generateCertificate(is);
        PublicKey publicKeyObject = cert2.getPublicKey();
        // this.key = ECUtil.encodePoint(var1, var2.getCurve());
        ECPublicKeyImpl pub = (ECPublicKeyImpl) publicKeyObject;
        byte[] pubBytes = pub.getEncodedPublicValue();
        String publicKey = Numeric.toHexStringNoPrefix(pubBytes);
        publicKey = publicKey.substring(publicKey.length() - 128); //证书byte[]为130位，只取128位，去除开头的04标记位
        System.out.println(publicKey);// 7853896f4e4b0891c5954d5b3f77325e39720718ec8e1a5608e4e8774bddb0dcdd63a98dad6c276603173674c477f2269e7abb1e7b8b1e9b9c852c27ad7c0814

        Assert.isTrue(publicKeyBC.equals(publicKey), "public key not equal");
        Assert.isTrue(publicKeyBC.equals(CertTools.getPublicKeyString(bcecPublicKey)), "public key not equal");
    }

}
