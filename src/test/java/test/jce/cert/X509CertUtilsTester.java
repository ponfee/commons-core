package test.jce.cert;

import java.io.IOException;
import java.security.cert.X509Certificate;

import org.apache.commons.lang3.StringUtils;

import cn.ponfee.commons.jce.cert.X509CertInfo;
import cn.ponfee.commons.jce.cert.X509CertUtils;
import cn.ponfee.commons.resource.ResourceLoaderFacade;

public class X509CertUtilsTester {

    public static void main(String[] args) throws IOException {
        //X509Certificate cert = X509CertUtils.loadX509Cert(ResourceLoaderFacade.getResource("cacert.pem").getStream());
        X509Certificate cert = X509CertUtils.loadX509Cert(ResourceLoaderFacade.getResource("sm2-1.cer").getStream());
        System.out.println(cert);
        for (X509CertInfo c : X509CertInfo.values())
            System.out.println(StringUtils.rightPad(c.name(), 15) + X509CertUtils.getCertInfo(cert, c));
    }
}
