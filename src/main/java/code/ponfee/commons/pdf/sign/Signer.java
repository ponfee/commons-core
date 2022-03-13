package code.ponfee.commons.pdf.sign;

import code.ponfee.commons.util.ImageUtils;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;

/**
 * 签名人信息
 * 
 * @author Ponfee
 */
public class Signer {

    private final PrivateKey priKey;
    private final Certificate[] certChain;
    private final Image image;

    public Signer(PrivateKey priKey, Certificate[] certChain, 
                  byte[] img, boolean transparent) {
        this.priKey = priKey;
        this.certChain = certChain;
        if (transparent) { // 图片透明化处理
            img = ImageUtils.transparent(new ByteArrayInputStream(img), 250, 235);
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