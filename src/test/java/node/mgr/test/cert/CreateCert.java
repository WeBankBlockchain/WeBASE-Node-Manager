package node.mgr.test.cert;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import org.bouncycastle.asn1.*;
//import org.bouncycastle.asn1.DEREncodable;
//import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.GeneralSubtree;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.NameConstraints;
import org.bouncycastle.asn1.x509.PolicyMappings;
import org.bouncycastle.asn1.x509.PrivateKeyUsagePeriod;
import org.bouncycastle.asn1.x509.SubjectDirectoryAttributes;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509ExtensionsGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.util.Strings;
//import org.bouncycastle.x509.X509Util;

public class CreateCert {

    public BigInteger genCertSerial() {
        // BigInteger bigInteger = new BigInteger(val);
        byte[] b = new byte[32];
        Random random = new Random(new Date().getTime());

        for (int i = 0; i < 32; i++) {
            byte[] tmp = new byte[10];
            random.nextBytes(tmp);
            b[i] = tmp[random.nextInt(tmp.length - 1)];
        }
        return new BigInteger(b);
    }

//
//    public static void main(String args[]) throws Exception {
//        Security.addProvider(new BouncyCastleProvider());
//        X500Name issuer = new X500Name("O=IBM,OU=CSC,CN=dev");
//        X500Name subject = new X500Name("O=IBM,OU=CSC,CN=ligson");
//        CreateCert cert = new CreateCert();
//        BigInteger serail = cert.genCertSerial();
//        RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(
//                new BigInteger(
//                        "b4a7e46170574f16a97082b22be58b6a2a629798419be12872a4bdba626cfae9900f76abfb12139dce5de56564fab2b6543165a040c606887420e33d91ed7ed7",
//                        16), new BigInteger("11", 16));
//        RSAPrivateCrtKeySpec privKeySpec = new RSAPrivateCrtKeySpec(
//                new BigInteger(
//                        "b4a7e46170574f16a97082b22be58b6a2a629798419be12872a4bdba626cfae9900f76abfb12139dce5de56564fab2b6543165a040c606887420e33d91ed7ed7",
//                        16),
//                new BigInteger("11", 16),
//                new BigInteger(
//                        "9f66f6b05410cd503b2709e88115d55daced94d1a34d4e32bf824d0dde6028ae79c5f07b580f5dce240d7111f7ddb130a7945cd7d957d1920994da389f490c89",
//                        16),
//                new BigInteger(
//                        "c0a0758cdf14256f78d4708c86becdead1b50ad4ad6c5c703e2168fbf37884cb",
//                        16),
//                new BigInteger(
//                        "f01734d7960ea60070f1b06f2bb81bfac48ff192ae18451d5e56c734a5aab8a5",
//                        16),
//                new BigInteger(
//                        "b54bb9edff22051d9ee60f9351a48591b6500a319429c069a3e335a1d6171391",
//                        16),
//                new BigInteger(
//                        "d3d83daf2a0cecd3367ae6f8ae1aeb82e9ac2f816c6fc483533d8297dd7884cd",
//                        16),
//                new BigInteger(
//                        "b8f52fc6f38593dabb661d3f50f8897f8106eee68b1bce78a95b132b4e5b5d19",
//                        16));
//        KeyFactory fact = KeyFactory.getInstance("RSA", "BC");
//        PublicKey pkKey = fact.generatePublic(pubKeySpec);
//        PrivateKey privateKey = fact.generatePrivate(privKeySpec);
//        System.out.println(pkKey);
//        AlgorithmIdentifier algorithmIdentifier = AlgorithmIdentifier
//                .getInstance(X509Util.getAlgorithmOID("SHA1WITHRSA"));
//        SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(
//                algorithmIdentifier, pkKey.getEncoded());
//        X509Certificate certificate = cert.createAcIssuerCert(issuer, serail,
//                new Date(), new Date(new Date().getTime() + 10000000), subject,
//                subjectPublicKeyInfo, privateKey);
//
//        // certificate.getEncoded();
//        FileOutputStream fileOutputStream = new FileOutputStream(new File(
//                "E:/code/itrusca/SecPKI/cert/2.cer"));
//        fileOutputStream.write(certificate.getEncoded());
//        fileOutputStream.close();
//
//    }

//    // getCertGen 加入各个限制，create证书
//    public X509Certificate createAcIssuerCert(X500Name issuer,
//                                              BigInteger serial, Date notBefore, Date notAfter, X500Name subject,
//                                              final SubjectPublicKeyInfo publicKeyInfo, PrivateKey privKey)
//            throws Exception {
//        V3TBSCertificateGenerator certificateGenerator = new V3TBSCertificateGenerator();
////        certificateGenerator.setExtensions(getCertGen());
//        certificateGenerator.setSignature(publicKeyInfo.getAlgorithmId());
//
//        certificateGenerator.setIssuer(issuer);
//        certificateGenerator.setSubject(subject);
//        certificateGenerator.setSerialNumber(new DERInteger(serial));
//        certificateGenerator.setStartDate(new DERUTCTime(notBefore));
//        certificateGenerator.setEndDate(new DERUTCTime(notAfter));
//        certificateGenerator.setSubjectPublicKeyInfo(publicKeyInfo);
//
//
//        System.out.println(certificateGenerator.generateTBSCertificate()
//                .getEncoded().length);
//
//        ASN1EncodableVector asn1encodablevector = new ASN1EncodableVector();
//        asn1encodablevector.add(certificateGenerator.generateTBSCertificate());
//        asn1encodablevector.add(publicKeyInfo.getAlgorithmId());
//
//        byte[] pubData = new byte[65];
//        pubData[0] = 0;
//
//        for (byte i = 1; i < pubData.length; i++) {
//            pubData[i] = i;
//        }
//
//        byte[] signInfo = new byte[69];//.....
//        for (byte i = 1; i < pubData.length; i++) {
//            pubData[i] = i;
//        }
//
//        asn1encodablevector.add(new DERBitString(signInfo));
//        X509CertificateObject cert = new X509CertificateObject(new X509CertificateStructure(new DERSequence(asn1encodablevector)));
//        return cert;
//    }

//    @SuppressWarnings("deprecation")
//    static X509Extensions getCertGen() {
//        // 添加扩展
//        X509ExtensionsGenerator certGen = new X509ExtensionsGenerator();
//
//        // 基本限制
//        certGen.addExtension(X509Extensions.BasicConstraints, false,
//                new DEREncodable() {
//
//                    @Override
//                    public DERObject getDERObject() {
//                        // TODO Auto-generated method stub
//                        ASN1EncodableVector bConstraints = new ASN1EncodableVector();
//                        // 是否是CA证书
//                        boolean bCA = false;
//                        bConstraints.add(new DERBoolean(bCA));
//                        // 证书路径长度限制
//                        int pathLenConstraint = 3;
//                        if ((pathLenConstraint >= 0) && (bCA))
//                            bConstraints.add(new DERInteger(pathLenConstraint));
//                        return new DERSequence(bConstraints);
//                    }
//
//                });
//        // 密钥用法
//        certGen.addExtension(X509Extensions.KeyUsage, false,
//                new DEREncodable() {
//                    @SuppressWarnings("unused")
//                    public int keyUsage;
//
//                    public static final int digitalSignature = (1 << 7);
//                    public static final int nonRepudiation = (1 << 6);
//                    public static final int keyEncipherment = (1 << 5);
//                    public static final int dataEncipherment = (1 << 4);
//                    public static final int keyAgreement = (1 << 3);
//                    public static final int keyCertSign = (1 << 2);
//                    public static final int cRLSign = (1 << 1);
//                    public static final int encipherOnly = (1 << 0);
//                    public static final int decipherOnly = (1 << 15);
//
//                    @Override
//                    public DERObject getDERObject() {
//                        // TODO Auto-generated method stub
//                        return new KeyUsage(digitalSignature | nonRepudiation
//                                | keyEncipherment | dataEncipherment
//                                | keyAgreement | keyCertSign | cRLSign
//                                | encipherOnly | decipherOnly);
//                    }
//
//                });
//        // 扩展密钥用法
//        certGen.addExtension(X509Extensions.ExtendedKeyUsage, false,
//                new DEREncodable() {
//                    private static final String id_kp = "1.3.6.1.5.5.7.3";
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId anyExtendedKeyUsage = new KeyPurposeId(
//                            X509Extensions.ExtendedKeyUsage.getId() + ".0");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_serverAuth = new KeyPurposeId(
//                            id_kp + ".1");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_clientAuth = new KeyPurposeId(
//                            id_kp + ".2");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_codeSigning = new KeyPurposeId(
//                            id_kp + ".3");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_emailProtection = new KeyPurposeId(
//                            id_kp + ".4");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_ipsecEndSystem = new KeyPurposeId(
//                            id_kp + ".5");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_ipsecTunnel = new KeyPurposeId(
//                            id_kp + ".6");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_ipsecUser = new KeyPurposeId(
//                            id_kp + ".7");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_timeStamping = new KeyPurposeId(
//                            id_kp + ".8");
//                    public final KeyPurposeId id_kp_OCSPSigning = new KeyPurposeId(
//                            id_kp + ".9");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_dvcs = new KeyPurposeId(
//                            id_kp + ".10");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_sbgpCertAAServerAuth = new KeyPurposeId(
//                            id_kp + ".11");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_scvp_responder = new KeyPurposeId(
//                            id_kp + ".12");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_eapOverPPP = new KeyPurposeId(
//                            id_kp + ".13");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_eapOverLAN = new KeyPurposeId(
//                            id_kp + ".14");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_scvpServer = new KeyPurposeId(
//                            id_kp + ".15");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_scvpClient = new KeyPurposeId(
//                            id_kp + ".16");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_ipsecIKE = new KeyPurposeId(
//                            id_kp + ".17");
//                    public final KeyPurposeId id_kp_capwapAC = new KeyPurposeId(
//                            id_kp + ".18");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_capwapWTP = new KeyPurposeId(
//                            id_kp + ".19");
//                    @SuppressWarnings("unused")
//                    public final KeyPurposeId id_kp_smartcardlogon = new KeyPurposeId(
//                            " .2.2");
//                    ASN1EncodableVector extKeyUsage = new ASN1EncodableVector();
//
//                    @Override
//                    public DERObject getDERObject() {
//                        // TODO Auto-generated method stub
//                        extKeyUsage.add(id_kp_OCSPSigning);
//                        extKeyUsage.add(id_kp_capwapAC);
//                        return new DERSequence(extKeyUsage);
//                    }
//
//                });
//        // 主题备用名称
//        certGen.addExtension(X509Extensions.SubjectAlternativeName, false,
//                new DEREncodable() {
//                    @SuppressWarnings("unused")
//                    public static final int otherName = 0;
//                    @SuppressWarnings("unused")
//                    public static final int rfc822Name = 1;
//                    @SuppressWarnings("unused")
//                    public static final int dNSName = 2;
//                    @SuppressWarnings("unused")
//                    public static final int x400Address = 3;
//                    @SuppressWarnings("unused")
//                    public static final int directoryName = 4;
//                    @SuppressWarnings("unused")
//                    public static final int ediPartyName = 5;
//                    @SuppressWarnings("unused")
//                    public static final int uniformResourceIdentifier = 6;
//                    public static final int iPAddress = 7;
//                    @SuppressWarnings("unused")
//                    public static final int registeredID = 8;
//
//                    @Override
//                    public DERObject getDERObject() {
//                        // TODO Auto-generated method stub
//
//                        ASN1EncodableVector nameVector = new ASN1EncodableVector();
//
//                        nameVector.add(new GeneralName(iPAddress, "127.0.0.1"));
//                        return new GeneralNames(new DERSequence(nameVector))
//                                .getDERObject();
//                    }
//
//                });
//        // 颁发者备用别�?
//        certGen.addExtension(X509Extensions.IssuerAlternativeName, false,
//                new DEREncodable() {
//                    @SuppressWarnings("unused")
//                    public static final int otherName = 0;
//                    @SuppressWarnings("unused")
//                    public static final int rfc822Name = 1;
//                    @SuppressWarnings("unused")
//                    public static final int dNSName = 2;
//                    @SuppressWarnings("unused")
//                    public static final int x400Address = 3;
//                    @SuppressWarnings("unused")
//                    public static final int directoryName = 4;
//                    @SuppressWarnings("unused")
//                    public static final int ediPartyName = 5;
//                    @SuppressWarnings("unused")
//                    public static final int uniformResourceIdentifier = 6;
//                    public static final int iPAddress = 7;
//                    @SuppressWarnings("unused")
//                    public static final int registeredID = 8;
//
//                    @Override
//                    public DERObject getDERObject() {
//                        // TODO Auto-generated method stub
//                        ASN1EncodableVector nameVector = new ASN1EncodableVector();
//
//                        nameVector.add(new GeneralName(iPAddress, "127.0.0.1"));
//                        return new GeneralNames(new DERSequence(nameVector))
//                                .getDERObject();
//                    }
//
//                });
//        // 秘钥有效�?
//        certGen.addExtension(X509Extensions.PrivateKeyUsagePeriod, false,
//                new DEREncodable() {
//
//                    @Override
//                    public DERObject getDERObject() {
//                        // TODO Auto-generated method stub
//                        Date notBefore = new Date();
//                        Date notAfter = new Date(notBefore.getTime() * 2);
//                        DERGeneralizedTime keyNotBefore = new DERGeneralizedTime(
//                                notBefore);
//                        DERGeneralizedTime keyNotAfter = new DERGeneralizedTime(
//                                notAfter);
//                        DERTaggedObject atokeyNotBefore = new DERTaggedObject(
//                                false, 0, keyNotBefore);
//                        DERTaggedObject atokeyNotAfter = new DERTaggedObject(
//                                false, 1, keyNotAfter);
//                        ASN1EncodableVector periodVector = new ASN1EncodableVector();
//                        periodVector.add(atokeyNotBefore);
//                        periodVector.add(atokeyNotAfter);
//                        return PrivateKeyUsagePeriod.getInstance(
//                                new DERSequence(periodVector)).getDERObject();
//                    }
//
//                });
//        // 策略限制
//        certGen.addExtension(X509Extensions.PolicyConstraints, false,
//                new DEREncodable() {
//                    int requireExplicitPolicy = -1;
//                    int inhibitPolicyMapping = -1;
//
//                    @Override
//                    public DERObject getDERObject() {
//                        // TODO Auto-generated method stub
//                        ASN1EncodableVector pConstraints = new ASN1EncodableVector();
//                        if (requireExplicitPolicy >= 0)
//                            pConstraints.add(new DERTaggedObject(false, 0,
//                                    new DERInteger(requireExplicitPolicy)));
//                        if (inhibitPolicyMapping >= 0)
//                            pConstraints.add(new DERTaggedObject(false, 1,
//                                    new DERInteger(inhibitPolicyMapping)));
//                        return new DERSequence(pConstraints);
//                    }
//
//                });
//        // 禁止任意策略
//        certGen.addExtension(X509Extensions.InhibitAnyPolicy, false,
//                new DEREncodable() {
//                    public int InhibitAnyPolicy;
//
//                    @Override
//                    public DERObject getDERObject() {
//                        // TODO Auto-generated method stub
//                        if (InhibitAnyPolicy >= 0)
//                            return new DERInteger(InhibitAnyPolicy);
//                        else
//                            return null;
//                    }
//
//                });
//        // 证书策略
//        certGen.addExtension(X509Extensions.CertificatePolicies, false,
//                new CertificatePoliciesInfo());
//        // 策略映射
//        certGen.addExtension(X509Extensions.PolicyMappings, false,
//                new DEREncodable() {
//                    public Hashtable<String, String> policyMappings = new Hashtable<String, String>();
//
//                    @Override
//                    public DERObject getDERObject() {
//                        return new PolicyMappings(policyMappings)
//                                .getDERObject();
//                    }
//
//                    @SuppressWarnings("unused")
//                    public void add(String policyOID, String mappingPolicyOID) {
//                        policyMappings.put(policyOID, mappingPolicyOID);
//
//                    }
//                });
//        // 主题密钥标识�?
//        /*
//         * certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new
//         * DEREncodable() { //TODO public�?要设�? public PublicKey keyIdentifier;
//         *
//         * @Override public DERObject getDERObject() { // TODO Auto-generated
//         * method stub return new
//         * SubjectKeyIdentifierStructure(keyIdentifier).getDERObject(); }
//         *
//         * });
//         */
//        // 权威密钥标识�?
//        // TODO 请参考RFC3093实现
//        /*
//         * certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false,
//         * new DEREncodable() {
//         *
//         * public PublicKey keyIdentifier; //public ExtensionGeneralName
//         * authorityCertIssuer; public BigInteger authorityCertSerialNumber;
//         *
//         * @Override public DERObject getDERObject() { // TODO Auto-generated
//         * method stub ASN1EncodableVector apkInfo = new ASN1EncodableVector();
//         * SubjectPublicKeyInfo apki; try { if (keyIdentifier != null) { apki =
//         * new SubjectPublicKeyInfo( (ASN1Sequence) new ASN1InputStream( new
//         * ByteArrayInputStream( keyIdentifier .getEncoded())) .readObject());
//         * Digest digest = new SHA1Digest(); byte[] resBuf = new
//         * byte[digest.getDigestSize()]; byte[] bytes = apki.getPublicKeyData()
//         * .getBytes(); digest.update(bytes, 0, bytes.length);
//         * digest.doFinal(resBuf, 0); apkInfo.add(new DERTaggedObject(false, 0,
//         * new DEROctetString(resBuf))); } if (authorityCertIssuer != null)
//         * apkInfo.add(new DERTaggedObject(false, 1, new GeneralNames(new
//         * GeneralName( authorityCertIssuer.nameType,
//         * authorityCertIssuer.value)))); if (authorityCertSerialNumber != null)
//         * apkInfo.add(new DERTaggedObject(false, 2, new DERInteger(
//         * authorityCertSerialNumber))); return new DERSequence(apkInfo); }
//         * catch (IOException e) { // TODO Auto-generated catch block
//         * e.printStackTrace(); }
//         *
//         * return null; }
//         *
//         * });
//         */
//        // 主体目录属�??
//        certGen.addExtension(X509Extensions.SubjectDirectoryAttributes, false,
//                new DEREncodable() {
//                    public String gender;
//                    public String dateOfBirth;
//                    public String streetAddress;
//                    public String telephoneNumber;
//                    public String mobileTelephoneNumber;
//
//                    @Override
//                    public DERObject getDERObject() {
//                        String genderOid = "1.3.6.1.5.5.7.9.4";
//                        String dateOfBirthOid = "1.3.6.1.5.5.7.9.1";
//                        String streetAddressOid = "2.5.4.9";
//                        String telephoneNumberOid = "2.5.4.20";
//                        String mobileTelephoneNumberOid = "0.9.2342.19200300.100.1.41";
//
//                        Vector<Attribute> attributes = new Vector<Attribute>();
//
//                        try {
//                            if (gender != null)
//                                attributes
//                                        .add(makeAttribute(genderOid, gender));
//                            if (dateOfBirth != null)
//                                attributes.add(makeAttribute(dateOfBirthOid,
//                                        dateOfBirth));
//                            if (streetAddress != null)
//                                attributes.add(makeAttribute(streetAddressOid,
//                                        streetAddress));
//                            if (telephoneNumber != null)
//                                attributes.add(makeAttribute(
//                                        telephoneNumberOid, telephoneNumber));
//                            if (mobileTelephoneNumber != null)
//                                attributes.add(makeAttribute(
//                                        mobileTelephoneNumberOid,
//                                        mobileTelephoneNumber));
//                            return new SubjectDirectoryAttributes(attributes)
//                                    .getDERObject();
//                        } catch (UnsupportedEncodingException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//
//                        return null;
//                    }
//
//                    private Attribute makeAttribute(String oid, String value)
//                            throws UnsupportedEncodingException {
//                        DERSet valueSet = new DERSet(new DERPrintableString(
//                                value.getBytes("UTF-8")));
//                        return new Attribute(new DERObjectIdentifier(oid),
//                                valueSet);
//                    }
//
//                });
//        // 名称限制
//        certGen.addExtension(X509Extensions.NameConstraints, false,
//                new DEREncodable() {
//                    private Vector<GeneralSubtree> permittedSubtrees = new Vector<GeneralSubtree>();
//                    private Vector<GeneralSubtree> excludedSubtrees = new Vector<GeneralSubtree>();
//
//                    @Override
//                    public DERObject getDERObject() {
//                        // TODO Auto-generated method stub
//                        return new NameConstraints(permittedSubtrees,
//                                excludedSubtrees).getDERObject();
//                    }
//
//                    @SuppressWarnings("unused")
//                    public void addPermitted(
//                            ExtensionGeneralName permittedName, int minimum,
//                            int maximum) {
//                        permittedSubtrees.add(new GeneralSubtree(
//                                new GeneralName(permittedName.nameType,
//                                        permittedName.value), BigInteger
//                                .valueOf(minimum), BigInteger
//                                .valueOf(maximum)));
//                    }
//
//                    @SuppressWarnings("unused")
//                    public void addExcluded(ExtensionGeneralName excludedName,
//                                            int minimum, int maximum) {
//                        excludedSubtrees.add(new GeneralSubtree(
//                                new GeneralName(excludedName.nameType,
//                                        excludedName.value), BigInteger
//                                .valueOf(minimum), BigInteger
//                                .valueOf(maximum)));
//                    }
//
//                });
//        // CRL分布�?
//        certGen.addExtension(X509Extensions.CRLDistributionPoints, false,
//                new DEREncodable() {
//                    private Vector<ExtensionGeneralName> crlDistPoints = new Vector<ExtensionGeneralName>();
//
//                    @Override
//                    public DERObject getDERObject() {
//                        // TODO Auto-generated method stub
//                        int iCount = crlDistPoints.size();
//                        assert (iCount > 0);
//                        DistributionPoint[] dp = new DistributionPoint[iCount];
//
//                        for (int i = 0; i < iCount; ++i) {
//                            DistributionPointName dpn = new DistributionPointName(
//                                    new GeneralNames(
//                                            new GeneralName(
//                                                    crlDistPoints.elementAt(i).nameType,
//                                                    crlDistPoints.elementAt(i).value)));
//                            dp[i] = new DistributionPoint(dpn, null, null);
//                        }
//
//                        return new CRLDistPoint(dp).getDERObject();
//                    }
//
//                    @SuppressWarnings("unused")
//                    public void add(ExtensionGeneralName info) {
//                        crlDistPoints.add(info);
//                    }
//
//                });
//        // �?�?/增量CRL分布�?
//        certGen.addExtension(X509Extensions.FreshestCRL, false,
//                new DEREncodable() {
//                    private Vector<ExtensionGeneralName> crlDistPoints = new Vector<ExtensionGeneralName>();
//
//                    @Override
//                    public DERObject getDERObject() {
//                        // TODO Auto-generated method stub
//                        int iCount = crlDistPoints.size();
//                        assert (iCount > 0);
//                        DistributionPoint[] dp = new DistributionPoint[iCount];
//
//                        for (int i = 0; i < iCount; ++i) {
//                            DistributionPointName dpn = new DistributionPointName(
//                                    new GeneralNames(
//                                            new GeneralName(
//                                                    crlDistPoints.elementAt(i).nameType,
//                                                    crlDistPoints.elementAt(i).value)));
//                            dp[i] = new DistributionPoint(dpn, null, null);
//                        }
//
//                        return new CRLDistPoint(dp).getDERObject();
//                    }
//
//                    @SuppressWarnings("unused")
//                    public void add(ExtensionGeneralName info) {
//                        crlDistPoints.add(info);
//                    }
//
//                });
//        // 机构信息访问
//        certGen.addExtension(X509Extensions.AuthorityInfoAccess, false,
//                new DEREncodable() {
//                    public final DERObjectIdentifier id_ad_caIssuers = new DERObjectIdentifier(
//                            "1.3.6.1.5.5.7.48.2");
//                    public final DERObjectIdentifier id_ad_ocsp = new DERObjectIdentifier(
//                            "1.3.6.1.5.5.7.48.1");
//                    private ASN1EncodableVector authorityInfoAccessVec = new ASN1EncodableVector();
//
//                    @Override
//                    public DERObject getDERObject() {
//                        // TODO Auto-generated method stub
//                        return new DERSequence(authorityInfoAccessVec);
//                    }
//
//                    @SuppressWarnings("unused")
//                    public void add(DERObjectIdentifier accessMethod,
//                                    ExtensionGeneralName accessLocation) {
//                        authorityInfoAccessVec.add(new AccessDescription(
//                                accessMethod, new GeneralName(
//                                accessLocation.nameType,
//                                accessLocation.value)));
//                    }
//
//                    @SuppressWarnings("unused")
//                    public void add(String accessMethod,
//                                    ExtensionGeneralName accessLocation) {
//                        DERObjectIdentifier am = null;
//                        if (accessMethod.equalsIgnoreCase("caIssuers"))
//                            am = id_ad_caIssuers;
//                        else if (accessMethod.equalsIgnoreCase("ocsp"))
//                            am = id_ad_ocsp;
//                        else {
//                            System.out
//                                    .println("InfoAccessInfo:no supported type!");
//                            assert (false);
//                        }
//                        authorityInfoAccessVec.add(new AccessDescription(am,
//                                new GeneralName(accessLocation.nameType,
//                                        accessLocation.value)));
//                    }
//                });
//        // 主题信息访问
//        /*
//         * certGen.addExtension(X509Extensions.AuthorityInfoAccess, false, new
//         * DEREncodable() { public final DERObjectIdentifier id_ad_caIssuers =
//         * new DERObjectIdentifier( "1.3.6.1.5.5.7.48.2"); public final
//         * DERObjectIdentifier id_ad_ocsp = new DERObjectIdentifier(
//         * "1.3.6.1.5.5.7.48.1"); private ASN1EncodableVector
//         * authorityInfoAccessVec = new ASN1EncodableVector();
//         *
//         * @Override public DERObject getDERObject() { // TODO Auto-generated
//         * method stub return new DERSequence(authorityInfoAccessVec); }
//         *
//         * @SuppressWarnings("unused") public void add(DERObjectIdentifier
//         * accessMethod, ExtensionGeneralName accessLocation) {
//         * authorityInfoAccessVec.add(new AccessDescription( accessMethod, new
//         * GeneralName( accessLocation.nameType, accessLocation.value))); }
//         *
//         * @SuppressWarnings("unused") public void add(String accessMethod,
//         * ExtensionGeneralName accessLocation) { DERObjectIdentifier am = null;
//         * if (accessMethod.equalsIgnoreCase("caIssuers")) am = id_ad_caIssuers;
//         * else if (accessMethod.equalsIgnoreCase("ocsp")) am = id_ad_ocsp; else
//         * { System.out .println("InfoAccessInfo:no supported type!"); assert
//         * (false); } authorityInfoAccessVec.add(new AccessDescription(am, new
//         * GeneralName(accessLocation.nameType, accessLocation.value))); } });
//         */
//        return certGen.generate();
//    }


}