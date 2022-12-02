package code.ponfee.commons.io;

import code.ponfee.commons.util.MavenProjects;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FileTypeDetector {

    // -----------------------------------------------------------------file type
    private static final int SUB_PREFIX = 64;
    public static final Map<String, String> FILE_TYPE_MAGIC = ImmutableMap.<String, String> builder()
            .put("jpg", "FFD8FF") // JPEG (jpg)
            .put("png", "89504E47") // PNG (png)
            .put("gif", "47494638") // GIF (gif)
            .put("tif", "49492A00") // TIFF (tif)
            .put("bmp", "424D") // Windows Bitmap (bmp)
            .put("dwg", "41433130") // CAD (dwg)
            .put("html","68746D6C3E") // HTML (html)
            .put("rtf", "7B5C727466") // Rich Text Format (rtf)
            .put("xml", "3C3F786D6C")
            .put("zip", "504B0304")
            .put("rar", "52617221")
            .put("psd", "38425053") // Photoshop (psd)
            .put("eml", "44656C69766572792D646174653A") // Email [thorough only] (eml)
            .put("dbx", "CFAD12FEC5FD746F") // Outlook Express (dbx)
            .put("pst", "2142444E") // Outlook (pst)
            .put("xls", "D0CF11E0") // MS Word
            .put("doc", "D0CF11E0") // MS Excel 注意：word 和 excel的文件头一样
            .put("mdb", "5374616E64617264204A") // MS Access (mdb)
            .put("wpd", "FF575043") // WordPerfect (wpd)
            .put("eps", "252150532D41646F6265")
            .put("ps",  "252150532D41646F6265")
            .put("pdf", "255044462D312E") // Adobe Acrobat (pdf)
            .put("qdf", "AC9EBD8F") // Quicken (qdf)
            .put("pwl", "E3828596") // Windows Password (pwl)
            .put("wav", "57415645") // Wave (wav)
            .put("avi", "41564920")
            .put("ram", "2E7261FD") // Real Audio (ram)
            .put("rm", "2E524D46") // Real Media (rm)
            .put("mpg", "000001BA")
            .put("mov", "6D6F6F76") // Quicktime (mov)
            .put("asf", "3026B2758E66CF11") // Windows Media (asf)
            .put("mid", "4D546864") // MIDI (mid)
            .build();

    /**
     * 探测文件类型
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String detectFileType(File file) throws IOException {
        return detectFileType(Files.readByteArray(file, SUB_PREFIX));
    }

    public static String detectFileType(byte[] array) {
        if (array.length > SUB_PREFIX) {
            array = ArrayUtils.subarray(array, 0, SUB_PREFIX);
        }

        String hex = Hex.encodeHexString(array, false);
        for (Map.Entry<String, String> entry : FILE_TYPE_MAGIC.entrySet()) {
            if (hex.startsWith(entry.getValue())) {
                return entry.getKey();
            }
        }
       return null;
    }

    public static void main(String[] args) throws IOException {
        File file = MavenProjects.getTestJavaFile(FileTypeDetector.class);

        // 1
        Tika tika1 = new Tika();
        String fileType = tika1.detect(file);
        System.out.println(fileType);

        // 2
        AutoDetectParser parser1 = new AutoDetectParser();
        parser1.setParsers(new HashMap<>());
        Metadata metadata1 = new Metadata();
        //metadata.add(TikaMetadataKeys.RESOURCE_NAME_KEY, file.getName());
        try (InputStream stream = new FileInputStream(file)) {
            parser1.parse(stream, new DefaultHandler(), metadata1, new ParseContext());
            System.out.println(metadata1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3
        Tika tika2 = new Tika();
        Metadata metadata2 = new Metadata();
        tika2.parse(new FileInputStream(file), metadata2);
        System.out.println(metadata2);
    }

}
