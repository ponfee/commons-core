package test.pdf;

import java.io.FileOutputStream;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;

import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignature;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfSignatureAppearance.RenderingMode;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.TSAClient;

import cn.ponfee.commons.jce.digest.DigestUtils;
import cn.ponfee.commons.jce.pkcs.PKCS1Signature;
import cn.ponfee.commons.jce.security.KeyStoreResolver;
import cn.ponfee.commons.jce.security.KeyStoreResolver.KeyStoreType;
import cn.ponfee.commons.resource.ResourceLoaderFacade;

public class PdfP7Sign {
    
    private static void sign(String src, String dest)
        throws Exception {
        byte[] img = IOUtils.toByteArray(ResourceLoaderFacade.getResource("2.png").getStream());

        // ------------------------------------------------------------------//
        // 1、用户上传自己的证书到服务器，从服务器上拿取待签名文件的hash数据
        KeyStoreResolver resolver = new KeyStoreResolver(KeyStoreType.PKCS12, ResourceLoaderFacade.getResource("subject.pfx").getStream(), "123456");
        PdfReader reader = new PdfReader(src);
        FileOutputStream fout = new FileOutputStream(dest);
        PdfStamper stp = PdfStamper.createSignature(reader, fout, '\0', null, true);

        PdfSignatureAppearance appearance = stp.getSignatureAppearance();
        appearance.setVisibleSignature(new Rectangle(100, 250, 288, 426), 1, "Signature");
        appearance.setSignDate(Calendar.getInstance()); // 设置签名时间为当前日期
        appearance.setSignatureGraphic(Image.getInstance(img));
        appearance.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
        appearance.setRenderingMode(RenderingMode.GRAPHIC);

        PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE, new PdfName("adbe.pkcs7.detached"));
        dic.setReason(appearance.getReason());
        dic.setLocation(appearance.getLocation());
        dic.setContact(appearance.getContact());
        dic.setDate(new PdfDate(appearance.getSignDate()));
        appearance.setCryptoDictionary(dic);

        int estimatedSize = 8192;
        HashMap<PdfName, Integer> exc = new HashMap<PdfName, Integer>();
        exc.put(PdfName.CONTENTS, new Integer((estimatedSize << 1) + 2));
        appearance.preClose(exc);
        
        byte[] bytes1 = IOUtils.toByteArray(appearance.getRangeStream());
        byte[] hash = DigestUtils.sha1(bytes1);
        // ------------------------------------------------------------------//

        // ------------------------------------------------------------------//
        //2、用户本地签名此HASH数据
        TSAClient tsc = null;
        /*boolean withTS = false;
        if (withTS)
        {
            String tsa_url = properties.getProperty("TSA");
            String tsa_login = properties.getProperty("TSA_LOGIN");
            String tsa_passw = properties.getProperty("TSA_PASSWORD");
            tsc = new TSAClientBouncyCastle(tsa_url, tsa_login, tsa_passw);
        }*/
        byte[] ocsp = null;
        /*boolean withOCSP = false;
        if (withOCSP)
        {
            String url = PdfPKCS7.getOCSPURL((X509Certificate)chain[0]);
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            FileInputStream is = new FileInputStream(properties.getProperty("ROOTCERT"));
            X509Certificate root = (X509Certificate)cf.generateCertificate(is);
            ocsp = new OcspClientBouncyCastle((X509Certificate)chain[0], root, url).getEncoded();
        }*/
        PdfPKCS7 pkcs7 = new PdfPKCS7(resolver.getPrivateKey("123456"), resolver.getX509CertChain(), "SHA1", null, null, false);
        byte[] bytes = pkcs7.getAuthenticatedAttributeBytes(hash, ocsp, null, CryptoStandard.CMS);
        byte[] signed = PKCS1Signature.sign(bytes, resolver.getPrivateKey("123456"), resolver.getX509CertChain()[0]);
        System.out.println("signed：" + Base64.getEncoder().encodeToString(signed));
        pkcs7.setExternalDigest(signed, null, "RSA");

        //sgn.update(sh, 0, sh.length);
        byte[] encodedSig = pkcs7.getEncodedPKCS7(hash, tsc, ocsp, null, CryptoStandard.CMS);
        byte[] paddedSig = new byte[encodedSig.length];
        System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.length);
        PdfDictionary dic2 = new PdfDictionary();
        dic2.put(PdfName.CONTENTS, new PdfString(paddedSig).setHexWriting(true));
        appearance.close(dic2);
    }

    
    public static void main(String[] args)
        throws Exception {
        String src = "D:\\test\\123.pdf";
        String dest = "D:\\test\\result.pdf";
        sign(src, dest);
    }
}
