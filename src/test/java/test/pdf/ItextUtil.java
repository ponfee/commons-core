package test.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PrivateKeySignature;

import code.ponfee.commons.jce.security.KeyStoreResolver;
import code.ponfee.commons.jce.security.KeyStoreResolver.KeyStoreType;
import code.ponfee.commons.resource.ResourceLoaderFacade;

public class ItextUtil {

    /**
     * 单多次签章通用
     * @param src
     * @param target
     * @param signatureInfos
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws DocumentException
     */
    public void sign(String src, String target, SignatureInfo... signatureInfos){
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try {
            inputStream = new FileInputStream(src);
            for (SignatureInfo signatureInfo : signatureInfos) {
                ByteArrayOutputStream tempArrayOutputStream = new ByteArrayOutputStream();
                PdfReader reader = new PdfReader(inputStream);
                //创建签章工具PdfStamper ，最后一个boolean参数是否允许被追加签名
                PdfStamper stamper = PdfStamper.createSignature(reader, tempArrayOutputStream, '\0', null, true);
                // 获取数字签章属性对象
                PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
                appearance.setReason(signatureInfo.getReason());
                appearance.setLocation(signatureInfo.getLocation());
                //设置签名的签名域名称，多次追加签名的时候，签名域名称不能一样，图片大小受表单域大小影响（过小导致压缩）
                //appearance.setVisibleSignature(signatureInfo.getFieldName());
                appearance.setVisibleSignature(new Rectangle(200, 200, 400, 400), 1, "pdf seal[" + System.nanoTime() + "]");
                //读取图章图片
                Image image = Image.getInstance(signatureInfo.getImagePath());
                appearance.setSignatureGraphic(image);
                appearance.setCertificationLevel(signatureInfo.getCertificationLevel());
                //设置图章的显示方式，如下选择的是只显示图章（还有其他的模式，可以图章和签名描述一同显示）
                appearance.setRenderingMode(signatureInfo.getRenderingMode());
                // 摘要算法
                ExternalDigest digest = new BouncyCastleDigest();
                // 签名算法
                ExternalSignature signature = new PrivateKeySignature(signatureInfo.getPk(), signatureInfo.getDigestAlgorithm(), null);
                // 调用itext签名方法完成pdf签章
                MakeSignature.signDetached(appearance, digest, signature, signatureInfo.getChain(), null, null, null, 0, signatureInfo.getSubfilter());
                //定义输入流为生成的输出流内容，以完成多次签章的过程
                inputStream = new ByteArrayInputStream(tempArrayOutputStream.toByteArray());
                result = tempArrayOutputStream;
            }
            outputStream = new FileOutputStream(new File(target));
            outputStream.write(result.toByteArray());
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(null!=outputStream){
                    outputStream.close();
                }
                if(null!=inputStream){
                    inputStream.close();
                }
                if(null!=result){
                    result.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            ItextUtil app = new ItextUtil();
            //将证书文件放入指定路径，并读取keystore ，获得私钥和证书链
            KeyStoreResolver resolver = new KeyStoreResolver(KeyStoreType.PKCS12, ResourceLoaderFacade.getResource("cas_test.pfx").getStream(), "1234");
            PrivateKey pk = resolver.getPrivateKey("1234");
            Certificate[] chain = resolver.getX509CertChain();
            String src = ResourceLoaderFacade.getResource("ElasticSearch.pdf").getFilePath();  
            //封装签章信息
            SignatureInfo info = new SignatureInfo();
            info.setReason("理由");
            info.setLocation("位置");
            info.setPk(pk);
            info.setChain(chain);
            info.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
            info.setDigestAlgorithm(DigestAlgorithms.SHA1);
            info.setFieldName("sig1");
            info.setImagePath(ResourceLoaderFacade.getResource("2.png").getFilePath());
            info.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);

            SignatureInfo info1 = new SignatureInfo();
            info1.setReason("理由1");
            info1.setLocation("位置1");
            info1.setPk(pk);
            info1.setChain(chain);
            info1.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
            info1.setDigestAlgorithm(DigestAlgorithms.SHA1);
            info1.setFieldName("sig2");
            info1.setImagePath(ResourceLoaderFacade.getResource("2.png").getFilePath());
            info1.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);

            app.sign(src, "D://sign.pdf", info,info1);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static class SignatureInfo {
        private String reason; //理由
        private String location;//位置
        private String digestAlgorithm;//摘要类型
        private String imagePath;//图章路径
        private String fieldName;//表单域名称
        private Certificate[] chain;//证书链
        private PrivateKey pk;//私钥
        private int certificationLevel = 0; //批准签章
        private PdfSignatureAppearance.RenderingMode renderingMode;//表现形式：仅描述，仅图片，图片和描述，签章者和描述
        private MakeSignature.CryptoStandard subfilter;//支持标准，CMS,CADES
        public String getReason() {
            return reason;
        }
        public void setReason(String reason) {
            this.reason = reason;
        }
        public String getLocation() {
            return location;
        }
        public void setLocation(String location) {
            this.location = location;
        }
        public String getDigestAlgorithm() {
            return digestAlgorithm;
        }
        public void setDigestAlgorithm(String digestAlgorithm) {
            this.digestAlgorithm = digestAlgorithm;
        }
        public String getImagePath() {
            return imagePath;
        }
        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }
        public String getFieldName() {
            return fieldName;
        }
        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }
        public Certificate[] getChain() {
            return chain;
        }
        public void setChain(Certificate[] chain) {
            this.chain = chain;
        }
        public PrivateKey getPk() {
            return pk;
        }
        public void setPk(PrivateKey pk) {
            this.pk = pk;
        }
        public int getCertificationLevel() {
            return certificationLevel;
        }
        public void setCertificationLevel(int certificationLevel) {
            this.certificationLevel = certificationLevel;
        }
        public PdfSignatureAppearance.RenderingMode getRenderingMode() {
            return renderingMode;
        }
        public void setRenderingMode(PdfSignatureAppearance.RenderingMode renderingMode) {
            this.renderingMode = renderingMode;
        }
        public MakeSignature.CryptoStandard getSubfilter() {
            return subfilter;
        }
        public void setSubfilter(MakeSignature.CryptoStandard subfilter) {
            this.subfilter = subfilter;
        }
        
        
    }
}