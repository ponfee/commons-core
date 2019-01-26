package test.jce.demo;
//package test.jce.cert;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.math.BigInteger;
//import java.security.PrivateKey;
//import java.security.PublicKey;
//import java.security.cert.Certificate;
//import java.security.cert.CertificateFactory;
//import java.security.cert.Extension;
//import java.security.cert.X509Certificate;
//import java.util.Date;
//import java.util.List;
//
//import javax.security.cert.CertificateException;
//
//import org.bouncycastle.asn1.ASN1Encodable;
//import org.bouncycastle.asn1.ASN1ObjectIdentifier;
//import org.bouncycastle.asn1.x500.X500Name;
//import org.bouncycastle.cert.X509CertificateHolder;
//import org.bouncycastle.cert.X509v3CertificateBuilder;
//import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
//import org.bouncycastle.operator.ContentSigner;
//import org.bouncycastle.operator.OperatorCreationException;
//import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
//
//public class CreateCert {
//
//    public static Certificate generateV3(String issuer, String subject,
//        BigInteger serial, Date notBefore, Date notAfter,
//        PublicKey publicKey, PrivateKey privKey, List<Extension> extensions)
//        throws OperatorCreationException, CertificateException, IOException {
//
//        X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(new X500Name(issuer), serial, notBefore, notAfter, new X500Name(subject), publicKey);
//        ContentSigner sigGen = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(privKey);
//        //privKey:使用自己的私钥进行签名，CA证书  
//        if (extensions != null) {
//            for (Extension ext : extensions) {
//                builder.addExtension(new ASN1ObjectIdentifier(ext.getId()), ext.isCritical(), ASN1Encodable.fromByteArray(ext.getValue()));
//            }
//        }
//        X509CertificateHolder holder = builder.build(sigGen);
//        CertificateFactory cf = CertificateFactory.getInstance("X.509");
//        InputStream is1 = new ByteArrayInputStream(holder.toASN1Structure().getEncoded());
//        X509Certificate theCert = (X509Certificate) cf.generateCertificate(is1);
//        is1.close();
//        return theCert;
//    }
//}
