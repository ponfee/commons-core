package code.ponfee.commons.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfSignatureAppearance.RenderingMode;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PrivateKeySignature;

import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.util.ImageUtils;
import sun.security.x509.AlgorithmId;

/**
 * pdf签章
 * @author fupf
 */
@SuppressWarnings("restriction")
public class PdfSignature {

    public static final float SIGN_AREA_RATE = 0.8f;

    /**
     * 对pdf签名
     * @param pdf    pdf文档数据流
     * @param stamp  签章图片信息 {@link Stamp}
     * @param signer 签名人信息 {@link Signer}
     * @return byte[]  the signed pdf byte array data
     */
    public static byte[] sign(byte[] pdf, Stamp stamp, Signer signer) {
        PdfReader reader = null;
        PdfStamper stamper = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
            reader = new PdfReader(pdf);
            stamper = PdfStamper.createSignature(reader, out, '\0', null, true); // true：允许对同一文档多次签名

            Calendar calendar = Calendar.getInstance();
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            //appearance.setReason("");appearance.setLocation("");appearance.setContact("");
            String fieldName = String.valueOf(calendar.getTimeInMillis());
            appearance.setVisibleSignature(calcRectangle(signer.getImage(), stamp), stamp.getPageNo(), fieldName);

            appearance.setSignDate(calendar); // 设置签名时间为当前日期
            appearance.setSignatureGraphic(signer.getImage());
            appearance.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
            appearance.setRenderingMode(RenderingMode.GRAPHIC);

            String signAlg = ((X509Certificate) signer.getCertChain()[0]).getSigAlgName();
            String hashAlg = AlgorithmId.getDigAlgFromSigAlg(signAlg); // 获取摘要算法
            ExternalSignature signature = new PrivateKeySignature(signer.getPriKey(), hashAlg, 
                                                                  Providers.BC.getName());

            MakeSignature.signDetached(appearance, new BouncyCastleDigest(), signature, 
                                       signer.getCertChain(), null, null, null, 0, CryptoStandard.CMS);

            PdfWriter writer = stamper.getWriter();
            stamper.close();
            stamper = null;
            writer.setCompressionLevel(5);
            writer.flush();
            out.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            if (stamper != null) try {
                stamper.close();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }

    /**
     * 多次签章
     * @param pdf
     * @param stamps
     * @param signer
     * @return
     */
    public static byte[] sign(byte[] pdf, Stamp[] stamps, Signer signer) {
        byte[] bytes = sign(pdf, stamps[0], signer);
        for (int i = 1; i < stamps.length; i++) {
            if (stamps[i] == null) {
                continue;
            }
            bytes = sign(bytes, stamps[i], signer);
        }
        return bytes;
    }

    /**
     * 计算签名位置
     * @param image
     * @param stamp
     * @return
     */
    private static Rectangle calcRectangle(Image image, Stamp stamp) {
        float urx = stamp.getLeft() + image.getWidth() * SIGN_AREA_RATE;
        float ury = stamp.getBottom() + image.getTop() * SIGN_AREA_RATE;
        return new Rectangle(stamp.getLeft(), stamp.getBottom(), urx, ury);
    }

    /**
     * 印章相关信息
     */
    public static class Stamp implements java.io.Serializable {
        private static final long serialVersionUID = -6348664154098224106L;

        private final int pageNo;
        private final float left;
        private final float bottom;

        public Stamp(int pageNo, float left, float bottom) {
            this.pageNo = pageNo;
            this.left = left;
            this.bottom = bottom;
        }

        public int getPageNo() {
            return pageNo;
        }

        public float getLeft() {
            return left;
        }

        public float getBottom() {
            return bottom;
        }

    }

    /**
     * 签名者
     */
    public static class Signer {

        private final PrivateKey priKey;
        private final Certificate[] certChain;
        private final Image image;

        public Signer(PrivateKey priKey, Certificate[] certChain, 
                      byte[] img, boolean transparent) {
            this.priKey = priKey;
            this.certChain = certChain;
            if (transparent) { // 图片透明化处理
                img = ImageUtils.transparent(img, 250, 235);
            }
            try {
                this.image = Image.getInstance(img);
            } catch (BadElementException | IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public PrivateKey getPriKey() {
            return priKey;
        }

        public Certificate[] getCertChain() {
            return certChain;
        }

        public Image getImage() {
            return image;
        }
    }

}
