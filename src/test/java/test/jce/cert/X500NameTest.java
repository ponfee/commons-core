package test.jce.cert;


import java.io.IOException;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.X509Principal;

import code.ponfee.commons.jce.cert.X509CertUtils;


public class X500NameTest {

    public static void main(String[] args) throws Exception {
        X509Certificate cert = X509CertUtils.loadX509Cert(Thread.currentThread().getContextClassLoader().getResourceAsStream("sm2-1.cer"));
        System.out.println(new X509Principal(cert.getIssuerX500Principal().getEncoded()).getName());
        System.out.println(cert.getIssuerDN().getName());
        System.out.println(cert.getSubjectDN().getName());
        //System.out.println(X500Name.getInstance(PrincipalUtil.getIssuerX509Principal(cert).getName()));
        System.out.println(new sun.security.x509.X500Name(cert.getIssuerX500Principal().getEncoded()).getName());
        //new X500Name(rDNs)
        //System.out.println(new X500NameBuilder(),.(cert.getIssuerX500Principal().getEncoded()).getName());

        
    }
}
