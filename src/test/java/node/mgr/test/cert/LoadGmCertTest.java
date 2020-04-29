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
import com.webank.webase.node.mgr.cert.CertService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public class LoadGmCertTest extends TestBase{
    @Autowired
    CertService certService;

    public final static String gmNodeCrt = "MIIBfDCCASKgAwIBAgIJAI98D486PmWFMAoGCCqBHM9VAYN1MDYxDzANBgNVBAMM\nBmFnZW5jeTESMBAGA1UECgwJZmlzY29iY29zMQ8wDQYDVQQLDAZhZ2VuY3kwHhcN\nMTkxMTI1MDM0MTExWhcNMjkxMTIyMDM0MTExWjA1MQ4wDAYDVQQDDAVub2RlMDES\nMBAGA1UECgwJZmlzY29iY29zMQ8wDQYDVQQLDAZhZ2VuY3kwWTATBgcqhkjOPQIB\nBggqgRzPVQGCLQNCAAS0TE9xO0C2ftL52uLvIIgIt/MREu/hqCLeBGYE/gu9/99G\nt8DPHKmqRodS1RSVjLYDZwptNFx4+v9uadgl6FG4oxowGDAJBgNVHRMEAjAAMAsG\nA1UdDwQEAwIGwDAKBggqgRzPVQGDdQNIADBFAiEAqF6J+H31mW0vcbGxFBjAM2Ue\n9GFZNrpS5UjFRDxFAcsCIFuzUNWBo/y1ySxSftxBFPd5I/iHedSFKPybcxQilvqt";
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
     * load guomi cert: need yml's encryptType: 0 -> 1
     * @throws CertificateEncodingException
     */
    @Test
    public void testLoadCert() throws CertificateEncodingException {
        String crtContent = CertTools.addCertHeadAndTail(gmNodeCrt);
        X509Certificate certificate = certService.loadSingleCertFromCrtContent(crtContent);
        Assert.notNull(certificate, "certificate null");
        System.out.println(certificate.getSubjectDN());
        System.out.println(NodeMgrTools.getCertFingerPrint(certificate.getEncoded()));
    }

}
