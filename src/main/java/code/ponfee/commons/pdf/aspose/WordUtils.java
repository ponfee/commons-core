package code.ponfee.commons.pdf.aspose;

import static code.ponfee.commons.resource.ResourceLoaderFacade.getResource;
import static code.ponfee.commons.resource.ResourceLoaderFacade.listResources;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.aspose.words.Document;
import com.aspose.words.FontSettings;
import com.aspose.words.FontSourceBase;
import com.aspose.words.License;
import com.aspose.words.LineStyle;
import com.aspose.words.LoadOptions;
import com.aspose.words.MemoryFontSource;
import com.aspose.words.NodeType;
import com.aspose.words.PdfSaveOptions;
import com.aspose.words.Table;

import code.ponfee.commons.resource.Resource;

/**
 * 依赖
 * <dependency>
 *   <groupId>aspose</groupId>
 *   <artifactId>words-jdk16</artifactId>
 *   <version>1.0.0</version>
 *   <scope>system</scope>
 *   <systemPath>${project.basedir}/lib/aspose.words.jdk16.jar</systemPath>
 * </dependency>
 * word转pdf
 * @author fupf
 */
public final class WordUtils {

    static {
        // 加载字体
        List<FontSourceBase> fonts = new ArrayList<>();
        for (Resource resource : listResources(new String[] { "ttf" }, WordUtils.class)) {
            try (InputStream input = resource.getStream()) {
                fonts.add(new MemoryFontSource(IOUtils.toByteArray(input)));
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
        if (!fonts.isEmpty()) {
            FontSettings.setFontsSources(fonts.toArray(new FontSourceBase[fonts.size()]));
            fonts.clear();
        }

        // 加载license
        try (InputStream input = getResource("license.xml", WordUtils.class).getStream()) {
            new License().setLicense(input);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * 转pdf
     * @param words
     * @param out
     */
    public static void toPdf(InputStream words, OutputStream out) {
        try {
            Document doc = new Document(words);
            Table table = (Table) doc.getChild(NodeType.TABLE, 1, true);
            if (table != null) {
                table.setBorders(LineStyle.SINGLE, 1.0, Color.BLACK);
            }

            PdfSaveOptions pso = new PdfSaveOptions();
            pso.setEmbedFullFonts(false);
            doc.save(out, pso);
            out.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (words != null) try {
                words.close();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
    }

    /**
     * html转word
     * @param html
     * @param out
     */
    public static void fromHtml(String html, OutputStream out) {
        try {
            LoadOptions loadOptions = new LoadOptions();
            loadOptions.setLoadFormat(com.aspose.words.LoadFormat.HTML);
            Document doc = new Document(new ByteArrayInputStream(html.getBytes()), loadOptions);

            PdfSaveOptions pso = new PdfSaveOptions();
            pso.setEmbedFullFonts(false);
            doc.save(out, pso);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
