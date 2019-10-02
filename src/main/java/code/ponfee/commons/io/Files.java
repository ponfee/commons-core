package code.ponfee.commons.io;

import code.ponfee.commons.math.Maths;
import code.ponfee.commons.math.Numbers;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    private static final byte[] WINDOWS_BOM = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

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
     * @param file
     * @return
     */
    public static File mkdir(File file) {
        if (!file.exists()) {
            if (file.mkdirs()) {
                file.setLastModified(System.currentTimeMillis());
            }
        } else if (file.isFile()) {
            throw new IllegalStateException(file.getAbsolutePath() + " is a directory.");
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
        if (!file.exists()) {
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
        } else if (file.isDirectory()) {
            throw new IllegalStateException(file.getAbsolutePath() + " is a directory.");
        }

        return file;
    }

    /**
     * read file as string
     * @param file
     * @return
     */
    public static String toString(String file) {
        return toString(new File(file));
    }

    public static String toString(File file) {
        return toString(file, DEFAULT_CHARSET_NAME);
    }

    public static String toString(File file, String charset) {
        try (FileInputStream in = new FileInputStream(file); 
             FileChannel channel = in.getChannel()
        ) {
            ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
            return Charset.forName(charset).decode(buffer).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * File to string spec charset
     *
     * @param file the file
     * @return
     */
    public static String toStringGuessCharset(File file) {
        try {
            return toStringGuessCharset(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * InputStream to string spec charset
     *
     * @param inputStream the input stream
     * @return
     */
    public static String toStringGuessCharset(InputStream inputStream) {
        try (InputStream input = inputStream) {
            return toStringGuessCharset(IOUtils.toByteArray(input));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /*Charset charset = Charset.forName(
            FileTransformer.guessEncoding(Arrays.copyOf(data, 600))
        );

        inputStream = new ByteArrayInputStream(data);
        try (WrappedBufferedReader reader = new WrappedBufferedReader(inputStream, charset)) {
            StringBuilder builder = new StringBuilder(data.length >> 1);
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append(Files.SYSTEM_LINE_SEPARATOR);
            }
            return builder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }

    public static String toStringGuessCharset(byte[] data) {
        if (data == null) {
            return null;
        } else if (data.length == 0) {
            return StringUtils.EMPTY;
        }
        Charset charset = Charset.forName(FileTransformer.guessEncoding(data));
        return new String(data, charset);
    }

    /**
     * read file to byte array
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

    public static List<String> readLines(File file) throws FileNotFoundException {
        return readLines(new FileInputStream(file), null);
    }

    public static List<String> readLines(File file, String charset)
        throws FileNotFoundException {
        return readLines(new FileInputStream(file), charset);
    }

    /**
     * 读取文件全部行数据
     * @param input
     * @return
     */
    public static List<String> readLines(InputStream input) {
        return readLines(input, null);
    }

    /**
     * 读取文件全部行数据
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

    private static final String[] FILE_UNITS = { "B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
    /** 
     * 文件大小可读化（attach unit）：B、KB、MB
     * @param size 文件字节大小 
     * @return
     */
    public static String human(long size) {
        if (size <= 0) {
            return "0";
        }

        int digit = (int) Maths.log(size, 1024); // log1024(size)
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digit)) + FILE_UNITS[digit];
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
            case '+':
                humanSize = humanSize.substring(1);
                break;
            case '-':
                factor = -1L;
                humanSize = humanSize.substring(1);
                break;
            default:
                break;
        }

        int trim = 1;
        // last character isn't a digit
        char c = humanSize.charAt(humanSize.length() - 1);
        if (c == 'B') {
            c = humanSize.charAt(humanSize.length() - 2);
        }
        if (!Character.isDigit(c)) {
            trim++;
            switch (c) {
                case 'K':
                    factor *= KB;
                    break;
                case 'M':
                    factor *= MB;
                    break;
                case 'G':
                    factor *= GB;
                    break;
                case 'T':
                    factor *= TB;
                    break;
                case 'P':
                    factor *= PB;
                    break;
                case 'E':
                    factor *= EB;
                    break;
                //case 'Z':
                //    factor *= ZB;
                //    break;
                //case 'Y':
                //    factor *= YB;
                //    break;
                default:
                    throw new RuntimeException("Invalid unit " + c); // cannot happened
            }
        }
        humanSize = humanSize.substring(0, humanSize.length() - trim);
        try {
            return factor * Numbers.toLong(humanSize);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse \"" + humanSize + "\"", e);
        }
    }

    // -------------------------------------windows file bom head-------------------------------------
    /**
     * add file bom head
     * @param filepath
     */
    public static void addBOM(String filepath) {
        addBOM(new File(filepath));
    }

    public static void addBOM(File file) {
        FileOutputStream output = null;
        BufferedOutputStream bos = null;
        try (FileInputStream input = new FileInputStream(file)) {
            int length = input.available();
            byte[] bytes1, bytes2;
            if (length >= 3) {
                bytes1 = new byte[3];
                input.read(bytes1);
                if (Arrays.equals(WINDOWS_BOM, bytes1)) {
                    return;
                }
                bytes2 = new byte[length - 3];
                input.read(bytes2);
            } else {
                bytes1 = new byte[0];
                bytes2 = new byte[length];
                input.read(bytes2);
            }
            output = new FileOutputStream(file);
            bos = new BufferedOutputStream(output);
            bos.write(WINDOWS_BOM);
            bos.write(bytes1);
            bos.write(bytes2);
            bos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Closeables.console(bos);
            Closeables.console(output);
        }
    }

    /**
     * remove file bom head
     * @param filepath
     */
    public static void removeBOM(String filepath) {
        removeBOM(new File(filepath));
    }

    public static void removeBOM(File file) {
        FileOutputStream output = null;
        BufferedOutputStream bos = null;
        try (FileInputStream input = new FileInputStream(file)) {
            int length = input.available();
            if (length < 3) {
                return;
            }

            byte[] bytes = new byte[3];
            input.read(bytes);
            if (!Arrays.equals(bytes, WINDOWS_BOM)) {
                return;
            }

            bytes = new byte[length - 3];
            input.read(bytes);
            output = new FileOutputStream(file);
            bos = new BufferedOutputStream(output);
            bos.write(bytes);
            bos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Closeables.console(bos);
            Closeables.console(output);
        }
    }

    // ------------------------file type---------------------------------
    private static final int SUB_PREFIX = 32;
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
     * 猜测文件类型
     * @param file
     * @return
     * @throws IOException
     */
    public static String guessFileType(File file) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            int count = input.available();
            count = count < SUB_PREFIX ? count : SUB_PREFIX;
            byte[] array = new byte[count];
            input.read(array);
            return guessFileType(array);
        }
    }

    public static String guessFileType(byte[] array) {
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
