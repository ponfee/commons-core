/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.pdf.sign;

import cn.ponfee.commons.io.Files;
import cn.ponfee.commons.jce.Providers;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfSignatureAppearance.RenderingMode;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfStream;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import sun.security.x509.AlgorithmId;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.Calendar;

/**
 * pdf签章
 * 
 * @author Ponfee
 */
@SuppressWarnings("restriction")
public class PdfSignature {

    public static final float SIGN_AREA_RATE = 0.8f;

    /**
     * Sign pdf
     * 
     * @param pdfInput  pdf输入
     * @param pdfOutput pdf输出
     * @param stamp     签章图片信息 {@link Stamp}
     * @param signer    签名人信息 {@link Signer}
     */
    public static void sign(InputStream pdfInput, OutputStream pdfOutput, Stamp stamp, Signer signer) {
        PdfReader reader = null;
        PdfStamper stamper = null;
        try {
            reader = new PdfReader(pdfInput);
            stamper = PdfStamper.createSignature(reader, pdfOutput, '\0', null, true); // true：允许对同一文档多次签名
            stamper.getWriter().setCompressionLevel(PdfStream.DEFAULT_COMPRESSION);
            Calendar calendar = Calendar.getInstance();

            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            //appearance.setReason("");appearance.setLocation("");appearance.setContact("");
            appearance.setVisibleSignature(
                calcRectangle(signer.getImage(), stamp), 
                stamp.getPageNo(), 
                String.valueOf(calendar.getTimeInMillis())
            );

            appearance.setSignDate(calendar); // 设置签名时间为当前日期
            appearance.setSignatureGraphic(signer.getImage());
            appearance.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
            appearance.setRenderingMode(RenderingMode.GRAPHIC);

            ExternalSignature signature = new PrivateKeySignature(
                signer.getPriKey(), 
                AlgorithmId.getDigAlgFromSigAlg(((X509Certificate) signer.getCertChain()[0]).getSigAlgName()), // 获取摘要算法
                Providers.BC.getName()
            );

            MakeSignature.signDetached(
                appearance, new BouncyCastleDigest(), signature, signer.getCertChain(), 
                null, null, null, 0, CryptoStandard.CMS
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
            if (stamper != null) {
                try {
                    stamper.close();
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

    /**
     * 多次签章
     * 
     * @param pdf    the pdf byte array data
     * @param stamps the stamp array
     * @param signer the signer
     * @return
     */
    public static byte[] sign(byte[] pdf, Stamp[] stamps, Signer signer) {
        for (Stamp stamp : stamps) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(Files.BUFF_SIZE);
            sign(new ByteArrayInputStream(pdf), baos, stamp, signer);
            pdf = baos.toByteArray();
        }
        return pdf;
    }

    /**
     * 计算签名位置
     * 
     * @param image the image
     * @param stamp the stamp
     * @return a Rectangle
     */
    private static Rectangle calcRectangle(Image image, Stamp stamp) {
        return new Rectangle(
            stamp.getLeft(), 
            stamp.getBottom(), 
            stamp.getLeft() + image.getWidth() * SIGN_AREA_RATE, 
            stamp.getBottom() + image.getTop() * SIGN_AREA_RATE
        );
    }

}
