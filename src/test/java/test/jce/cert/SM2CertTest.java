package test.jce.cert;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.junit.Test;

public class SM2CertTest {

    public @Test void test() {
        try {
            genCSR("CN=subject,OU=hackwp,O=wp,L=BJ,S=BJ,C=CN", "RSA1024", "BC");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String genCSR(String subject, String alg, String provider) throws Exception {
        String signalg = "";
        int alglength = 0;
        String keyAlg = "";
        if (alg.toUpperCase().equals("RSA1024")) {
            signalg = "SHA1WithRSA";
            alglength = 1024;
            keyAlg = "RSA";
        } else if (alg.toUpperCase().equals("RSA2048")) {
            signalg = "SHA1WithRSA";
            alglength = 2048;
            keyAlg = "RSA";
        } else if (alg.toUpperCase().equals("SM2")) {
            signalg = "SM3withSM2";
            alglength = 256;
            keyAlg = "SM2";
        }
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyAlg);
        keyGen.initialize(alglength);
        KeyPair kp = keyGen.generateKeyPair();
        PKCS10CertificationRequestBuilder builder = new PKCS10CertificationRequestBuilder(new X500Name(subject), SubjectPublicKeyInfo.getInstance(kp.getPublic().getEncoded()));
        JcaContentSignerBuilder jcaContentSignerBuilder = new JcaContentSignerBuilder(signalg);
        jcaContentSignerBuilder.setProvider(provider);
        ContentSigner contentSigner = jcaContentSignerBuilder.build(kp.getPrivate());
        builder.build(contentSigner);
        return builder.toString();
    }
}
