package code.ponfee.commons.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * pdf加水印
 * 
 * @author Ponfee
 */
public class PdfWaterMark {

    /**
     * 添加水印图片
     * @param pdf
     * @param img
     * @param out
     */
    public static void waterImgMark(InputStream pdf, byte[] img, OutputStream out) {
        PdfStamper stamper = null;
        PdfReader reader = null;
        try {
            reader = new PdfReader(pdf);
            stamper = new PdfStamper(reader, out);
            Image image = Image.getInstance(img);// 水印图片
            Rectangle pageRect;
            for (int n = stamper.getReader().getNumberOfPages() + 1, i = 1; i < n; i++) {
                pageRect = stamper.getReader().getPageSizeWithRotation(i);
                PdfContentByte content = stamper.getUnderContent(i);
                content.saveState();
                image.setAbsolutePosition(pageRect.getWidth() - image.getWidth(), 0); // 水印添加到右下角
                content.addImage(image);
            }
            out.flush();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (stamper != null) {
                try {
                    stamper.close();
                } catch (DocumentException | IOException ignored) {
                    ignored.printStackTrace();
                }
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * 添加水印文字
     * @param pdf
     * @param words
     * @param out
     */
    public static void waterWordMark(InputStream pdf, String words, OutputStream out) {
        PdfStamper stamper = null;
        PdfReader reader = null;
        try {
            reader = new PdfReader(pdf);
            stamper = new PdfStamper(reader, out);
            //stamper.setEncryption("user".getBytes(), "owner".getBytes(), PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_PRINTING, PdfWriter.STANDARD_ENCRYPTION_40);

            // 1、使用iTextAsian.jar中的字体：BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
            // 2、使用Windows系统字体（TrueType）：BaseFont.createFont("C:/WINDOWS/Fonts/SIMYOU.TTF", BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
            // 3、使用资源字体（ClassPath）：BaseFont.createFont("/SIMYOU.TTF", BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
            BaseFont base = BaseFont.createFont("code/ponfee/commons/pdf/aspose/simfang.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            PdfGState pdfGState = new PdfGState();
            pdfGState.setStrokeOpacity(0.2f); // 设置线条透明度为0.2
            pdfGState.setFillOpacity(0.2f); // 设置填充透明度为0.2
            for (int totalPage = stamper.getReader().getNumberOfPages(), i = 1; i <= totalPage; i++) {
                Rectangle pageRect = stamper.getReader().getPageSizeWithRotation(i);
                float x = pageRect.getWidth() / 2; // 计算水印X坐标
                float y = pageRect.getHeight() / 2; // 计算水印Y坐标
                PdfContentByte content = stamper.getUnderContent(i); // content = pdfStamper.getOverContent(i);
                content.saveState();
                content.setGState(pdfGState);
                content.beginText();
                content.setColorFill(BaseColor.LIGHT_GRAY);
                content.setFontAndSize(base, 60);
                content.showTextAligned(Element.ALIGN_CENTER, words, x, y, 45); // 水印文字成45度角倾斜
                content.endText();
            }
            out.flush();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (stamper != null) {
                try {
                    stamper.close();
                } catch (DocumentException | IOException ignored) {
                    ignored.printStackTrace();
                }
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

}
