///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package node.mgr.test.cert;
//
//import java.io.IOException;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//import java.security.NoSuchProviderException;
//import java.security.PublicKey;
//import java.security.SignatureException;
//import java.security.cert.Certificate;
//import java.security.cert.CertificateException;
//import java.security.cert.X509Certificate;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.HttpsURLConnection;
//import javax.net.ssl.SSLHandshakeException;
//import javax.net.ssl.SSLPeerUnverifiedException;
//import org.bouncycastle.asn1.x509.CRLDistPoint;
//import org.bouncycastle.asn1.x509.DistributionPoint;
//import org.bouncycastle.asn1.x509.GeneralName;
//import org.bouncycastle.asn1.x509.GeneralNames;
//import org.bouncycastle.asn1.x509.X509Extensions;
//import org.bouncycastle.x509.extension.X509ExtensionUtil;
//import sun.security.x509.X509CertImpl;
//
///**
// *
// * @author phoenix
// */
//public class X509 {
//
//  /**
//   * @param args the command line arguments
//   */
//  public static void main(String[] args) {
//    // TODO code application logic here
//    X509 x509 = new X509();
//    try {
//      x509.connectToNetConnect();
//    } catch (SSLPeerUnverifiedException ex) {
//      Logger.getLogger(X509.class.getName()).log(Level.SEVERE, null, ex);
//    }
//  }
//
//  public void connectToNetConnect() throws SSLPeerUnverifiedException {
//    X509Certificate xCertificate;
//    HttpsURLConnection secured;
//    HostnameVerifier hostNameVerifier;
//    URL url;
//
//    try {
//
//      url = new URL(this.SERVER_URL);
//      HttpURLConnection con = (HttpURLConnection) url.openConnection();
//      secured = (HttpsURLConnection) con;
//
//      secured.connect();
//
//      // now get servercertificates and check some stuff
//
//      Certificate[] certs = secured.getServerCertificates();
//      Certificate certifi = new X509CertImpl();
//
//
//
//    } catch (MalformedURLException e) {
//      System.out.println("Malformed URL");
//
//    } catch (SSLHandshakeException e) {
//      System.out.println("Handshake exceptionn");
//      e.printStackTrace();
//    } catch (Exception e) {
//      System.out.println("Plain ole exception");
//      e.printStackTrace();
//    }
//  }
//  String SERVER_URL = "https://www.facebook.com";
//
//  /**
//   *
//   * Checks whether given X.509 certificate is self-signed.
//   *
//   */
//  public static boolean isSelfSigned(X509Certificate cert)
//          throws CertificateException, NoSuchAlgorithmException,
//          NoSuchProviderException {
//    try {
//      // Try to verify certificate signature with its own public key
//      PublicKey key = cert.getPublicKey();
//      cert.verify(key);
//      return true;
//    } catch (SignatureException sigEx) {
//      // Invalid signature --> not self-signed
//      return false;
//
//    } catch (InvalidKeyException keyEx) {
//      // Invalid key --> not self-signed
//      return false;
//    }
//  }
//
//  /**
//   * Gets a list of URLs from the specified certificate.
//   *
//   * @param cert The certificate to find the URLs in.
//   * @return A list of CRL URLs in the certificate
//   */
//  public static List<URL> getURLs(X509Certificate cert) {
//    List<URL> urls = new LinkedList<URL>();
//    // Retrieves the raw ASN1 data of the CRL Dist Points X509 extension
//    byte[] cdp = cert.getExtensionValue(X509Extensions.CRLDistributionPoints.getId());
////    byte[] cdp = cert.getExtensionValue(X509Extension.cRLDistributionPoints);
//    if (cdp != null) {
//      try {
//        // Wraps the raw data in a container class
//        CRLDistPoint crldp = CRLDistPoint.getInstance(X509ExtensionUtil.fromExtensionValue(cdp));
//
//        DistributionPoint[] distPoints = crldp.getDistributionPoints();
//
//        for (DistributionPoint dp : distPoints) {
//          // Only use the "General name" data in the distribution point entry.
//          GeneralNames gns = (GeneralNames) dp.getDistributionPoint().getName();
//
//          for (GeneralName name : gns.getNames()) {
//            // Only retrieve URLs
//            if (name.getTagNo() == GeneralName.uniformResourceIdentifier) {
//              System.out.println("name:" + name.getName());
//              urls.add(new URL(name.getName().toString()));
////              DERString s = (DERString) name.getName();
////              urls.add(new URL(s.getString()));
//            }
//          }
//        }
//      } catch (IOException e) {
//        // Could not retrieve the CRLDistPoint object. Just return empty url list.
//      }
//    }
//
//    return urls;
//  }
//}
