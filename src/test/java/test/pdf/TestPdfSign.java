package test.pdf;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.google.common.io.Files;

import code.ponfee.commons.jce.security.KeyStoreResolver;
import code.ponfee.commons.jce.security.KeyStoreResolver.KeyStoreType;
import code.ponfee.commons.pdf.PdfSignature;
import code.ponfee.commons.pdf.PdfSignature.Signer;
import code.ponfee.commons.pdf.PdfSignature.Stamp;
import code.ponfee.commons.resource.ResourceLoaderFacade;

public class TestPdfSign {

    public static void main(String[] args) throws IOException {
        /*String sign = "cas_test.pfx";
        String pwd = "1234";*/
        String sign = "subject.pfx";
        String pwd = "123456";
        KeyStoreResolver r = new KeyStoreResolver(KeyStoreType.PKCS12, ResourceLoaderFacade.getResource(sign).getStream(), pwd);
        byte[] img = IOUtils.toByteArray(ResourceLoaderFacade.getResource("2.png").getStream());
        Signer signer = new Signer(r.getPrivateKey(pwd), r.getX509CertChain(), img, true);
        
        Stamp stamp1 = new Stamp(1,100,250);
        Stamp stamp2 = new Stamp(1,300,250);
        
        byte[] pdf = IOUtils.toByteArray(ResourceLoaderFacade.getResource("ElasticSearch.pdf").getStream());
        
        byte[] result = PdfSignature.sign(pdf, new Stamp[]{stamp1, stamp2}, signer);
        Files.write(result, new File("d:/test/123.pdf"));
        
    }
}
