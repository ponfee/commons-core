package code.ponfee.commons.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.function.Consumer;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.exception.Throwables;
import code.ponfee.commons.math.Maths;

/**
 * 文件工具类
 * @author Ponfee
 */
public final class Files {
    private Files() {}

    public static final int EOF = -1; // end of file read

    public static final int BUFF_SIZE = 8192; // file buffer size

    // ------------------------------------------------------------charset encoding
    public static final Charset DEFAULT_CHARSET = Charset.defaultCharset(); // default charset

    public static final String DEFAULT_CHARSET_NAME = DEFAULT_CHARSET.name(); // default charset name

    public static final String UTF_8 = "UTF-8"; // UTF-8 encoding

    // ------------------------------------------------------------file separator
    public static final String WINDOWS_FILE_SEPARATOR = "\\"; // windows file separator

    public static final String UNIX_FILE_SEPARATOR = "/"; // unix file separator

    public static final String SYSTEM_FILE_SEPARATOR = File.separator; // system file separator

    // ------------------------------------------------------------line separator
    public static final String UNIX_LINE_SEPARATOR = "\n"; // unix file line separator spec \n  LF

    public static final String WINDOWS_LINE_SEPARATOR = "\r\n"; // windows file line separator spec \r\n  CRLF

    public static final String MAC_LINE_SEPARATOR = "\r"; // mac file line separator spec \r  CR

    public static final String SYSTEM_LINE_SEPARATOR; // system file line separator
    static {
        /*String separator = java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("line.separator")
        );
        if (separator == null || separator.length() == 0) {
            separator = System.getProperty("line.separator", "\n");
        }
        SYSTEM_LINE_SEPARATOR = separator;*/
        StringBuilderWriter buffer = new StringBuilderWriter(4);
        PrintWriter out = new PrintWriter(buffer);
        out.println();
        SYSTEM_LINE_SEPARATOR = buffer.toString();
        out.close();
    }

    /**
     * 创建目录
     * @param path
     * @return
     */
    public static File mkdir(String path) {
        return mkdir(new File(path));
    }

    /**
     * 创建目录
     * 
     * @param file
     * @return
     */
    public static File mkdir(File file) {
        if (file.isFile()) {
            throw new IllegalStateException(file.getAbsolutePath() + " is a directory.");
        }

        if (file.exists()) {
            return file;
        }

        if (file.mkdirs()) {
            file.setLastModified(System.currentTimeMillis());
        }
        return file;
    }

    /**
     * 创建文件
     * @param file
     * @return
     */
    public static File touch(String file) {
        return touch(new File(file));
    }

    /**
     * 创建文件
     * @param file
     * @return
     */
    public static File touch(File file) {
        if (file.isDirectory()) {
            throw new IllegalStateException(file.getAbsolutePath() + " is a directory.");
        }

        if (file.exists()) {
            return file;
        }

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try {
            if (file.createNewFile()) {
                file.setLastModified(System.currentTimeMillis());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return file;
    }

    public static void deleteQuietly(File file) {
        if (file == null) {
            return;
        }

        try {
            //org.apache.commons.io.FileUtils.deleteQuietly(file);
            java.nio.file.Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            Throwables.ignore(e);
        }
    }

    // --------------------------------------------------------------------------file to string
    public static String toString(String file) throws IOException {
        return toString(new File(file));
    }

    public static String toString(File file) throws IOException {
        return toString(file, CharacterEncodingDetector.detect(file));
    }

    public static String toString(File file, String charsetName) throws IOException {
        return toString(file, Charset.forName(charsetName));
    }

    public static String toString(File file, Charset charset) throws IOException {
        ByteOrderMarks bom = ByteOrderMarks.of(charset, file);

        try (FileInputStream input = new FileInputStream(file); 
             FileChannel channel = input.getChannel()
        ) {
            long offset = 0, length = channel.size();
            if (bom != null) {
                offset = bom.length();
                length -= offset;
            }
            ByteBuffer buffer = channel.map(MapMode.READ_ONLY, offset, length);
            return charset.decode(buffer).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads file to byte array
     * 
     * @param file
     * @return
     */
    public static byte[] toByteArray(File file) {
        try (FileInputStream in = new FileInputStream(file); 
             FileChannel channel = in.getChannel()
        ) {
            ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes, 0, bytes.length);
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------------------------------------read line
    public static List<String> readLines(File file) throws FileNotFoundException {
        return readLines(new FileInputStream(file), null);
    }

    public static List<String> readLines(File file, String charset)
        throws FileNotFoundException {
        return readLines(new FileInputStream(file), charset);
    }

    public static List<String> readLines(InputStream input) {
        return readLines(input, null);
    }

    /**
     * 读取文件全部行数据
     * 
     * @param input
     * @param charset
     * @return
     */
    public static List<String> readLines(InputStream input, String charset) {
        List<String> list = new ArrayList<>();
        try (Scanner scanner = (charset == null) 
                               ? new Scanner(input) 
                               : new Scanner(input, charset)
        ) {
            while (scanner.hasNextLine()) {
                list.add(scanner.nextLine());
            }
        }
        return list;
    }

    /**
     * 读取文件
     * @param input
     * @param charset
     * @param consumer
     */
    public static void readFile(InputStream input, String charset, 
                                Consumer<String> consumer) {
        try (Scanner scanner = (charset == null)
                               ? new Scanner(input)
                               : new Scanner(input, charset)) {
            while (scanner.hasNextLine()) {
                consumer.accept(scanner.nextLine());
            }
        }
    }

    // ---------------------------------------------------------------file size humanly
    private static final String[] FILE_UNITS = { 
        "B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" 
    };
    /** 
     * 文件大小可读化（attach unit）：B、KB、MB
     * @param size 文件字节大小 
     * @return
     */
    public static String human(long size) {
        if (size == 0) {
            return "0B";
        }
        String signed = "";
        if (size < 0) {
            signed = "-";
            size = -size;
        }

        int digit = (int) Maths.log(size, 1024); // log1024(size)

        return signed 
             + new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digit)) 
             + FILE_UNITS[digit];
    }

    private static final long UNIT = 1024, KB = UNIT;
    private static final long MB = KB * UNIT;
    private static final long GB = MB * UNIT;
    private static final long TB = GB * UNIT;
    private static final long PB = TB * UNIT;
    private static final long EB = PB * UNIT;
    //private static final long ZB = EB * UNIT;
    //private static final long YB = ZB * UNIT;
    public static long parseHuman(String humanSize) {
        long factor = 1L;
        switch (humanSize.charAt(0)) {
            case '+': humanSize = humanSize.substring(1);               break;
            case '-': humanSize = humanSize.substring(1); factor = -1L; break;
        }

        int trim = 0;
        // last character isn't a digit
        char c = humanSize.charAt(humanSize.length() - 1);
        if (c == 'B') {
            trim++;
            c = humanSize.charAt(humanSize.length() - 2);
        }
        if (!Character.isDigit(c)) {
            trim++;
            switch (c) {
                case 'K': factor *= KB; break;
                case 'M': factor *= MB; break;
                case 'G': factor *= GB; break;
                case 'T': factor *= TB; break;
                case 'P': factor *= PB; break;
                case 'E': factor *= EB; break;
                //case 'Z': factor *= ZB; break;
                //case 'Y': factor *= YB; break;
                default: throw new RuntimeException("Invalid unit " + c); // cannot happened
            }
        }
        if (trim > 0) {
            humanSize = humanSize.substring(0, humanSize.length() - trim);
        }
        try {
            return (long) (factor * Double.parseDouble(humanSize));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse \"" + humanSize + "\"", e);
        }
    }

    // -----------------------------------------------------------------readByteArray
    public static byte[] readByteArray(InputStream input, int count) throws IOException {
        byte[] bytes = new byte[count];
        int n, index = 0;
        while (index < count && (n = input.read(bytes, index, count - index)) != EOF) {
            index += n;
        }
 
        return (index == count) ? bytes : Arrays.copyOf(bytes, index);
    }

    public static byte[] readByteArray(File file, int count) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            return readByteArray(input, count);
        }
    }

    public static byte[] readByteArray(String filePath, int count) throws IOException {
        return readByteArray(new File(filePath), count);
    }

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
        return detectFileType(readByteArray(file, SUB_PREFIX));
    }

    public static String detectFileType(byte[] array) {
        if (array.length > SUB_PREFIX) {
            array = ArrayUtils.subarray(array, 0, SUB_PREFIX);
        }

        String hex = Hex.encodeHexString(array, false);
        for (Entry<String, String> entry : FILE_TYPE_MAGIC.entrySet()) {
            if (hex.startsWith(entry.getValue())) {
                return entry.getKey();
            }
        }

        try {
            return net.sf.jmimemagic.Magic.getMagicMatch(array).getExtension();
        } catch (Exception e) {
            return null; // contentType = "text/plain";
        }
    }

}
