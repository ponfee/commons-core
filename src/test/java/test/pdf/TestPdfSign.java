package test.pdf;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.google.common.io.Files;

import code.ponfee.commons.jce.security.KeyStoreResolver;
import code.ponfee.commons.jce.security.KeyStoreResolver.KeyStoreType;
import code.ponfee.commons.pdf.sign.PdfSignature;
import code.ponfee.commons.pdf.sign.Signer;
import code.ponfee.commons.pdf.sign.Stamp;
import code.ponfee.commons.resource.ResourceLoaderFacade;

public class TestPdfSign {

    public static void main(String[] args) throws IOException {
        String sign = "subject.pfx";
        String pwd = "123456";
        KeyStoreResolver r = new KeyStoreResolver(KeyStoreType.PKCS12, ResourceLoaderFacade.getResource(sign).getStream(), pwd);
        byte[] img = IOUtils.toByteArray(ResourceLoaderFacade.getResource("2.png").getStream());
        Signer signer = new Signer(r.getPrivateKey(pwd), r.getX509CertChain(), img, true);

        Stamp stamp1 = new Stamp(1, 50, 250);
        Stamp stamp2 = new Stamp(2, 150, 250);
        Stamp stamp3 = new Stamp(3, 300, 250);

        byte[] pdf = IOUtils.toByteArray(ResourceLoaderFacade.getResource("SM3密码杂凑算法.pdf").getStream());

        byte[] result = PdfSignature.sign(pdf, new Stamp[] { stamp1, stamp2, stamp3 }, signer);
        Files.write(result, new File("d:/test/123.pdf"));
    }
}
