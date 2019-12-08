/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.io;

import static code.ponfee.commons.io.CharacterEncodingDetector.DETECT_COUNT;
import static code.ponfee.commons.io.CharacterEncodingDetector.detect;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.bouncycastle.util.Arrays;

/**
 * BOM(byte-order mark) Encoding: 
 *   EF BB BF       UTF-8
 *   FF FE          UTF-16 (little-endian)
 *   FE FF          UTF-16 (big-endian)
 *   FF FE 00 00    UTF-32 (little-endian)
 *   00 00 FE FF    UTF-32 (big-endian)
 * 
 * @author Ponfee
 */
public enum ByteOrderMarks {

    UTF_8   (StandardCharsets.UTF_8     , (byte) 0xEF, (byte) 0xBB, (byte) 0xBF             ), //

    UTF_16LE(StandardCharsets.UTF_16LE  , (byte) 0xFF, (byte) 0xFE                          ), //

    UTF_16BE(StandardCharsets.UTF_16BE  , (byte) 0xFE, (byte) 0xFF                          ), //

    UTF_32LE(Charset.forName("UTF-32LE"), (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00), //

    UTF_32BE(Charset.forName("UTF-32BE"), (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF), //

    ;

    private final Charset charset;
    private final byte[]  bytes;

    ByteOrderMarks(Charset charset, byte... bytes) {
        this.charset = charset;
        this.bytes = bytes;
        Hide.MAPPING.put(charset, this);
    }

    // ------------------------------------------------------------------------of bom without charset
    public static ByteOrderMarks of(String path) throws IOException {
        return of(new File(path));
    }

    public static ByteOrderMarks of(File file) throws IOException {
        return of(Files.readByteArray(file, DETECT_COUNT));
    }

    public static ByteOrderMarks of(InputStream input) throws IOException {
        return of(Files.readByteArray(input, DETECT_COUNT));
    }

    public static ByteOrderMarks of(byte[] bytes) {
        return of(detect(bytes, DETECT_COUNT), bytes);
    }

    // ------------------------------------------------------------------------of bom specified charset
    public static ByteOrderMarks of(Charset charset, String path) throws IOException {
        return of(charset, new File(path));
    }

    public static ByteOrderMarks of(Charset charset, File file) throws IOException {
        ByteOrderMarks bom = Hide.MAPPING.get(charset);
        if (bom == null) {
            return null; // no bom charset
        }
        return bom.match(Files.readByteArray(file, bom.length())) ? bom : null;
    }

    public static ByteOrderMarks of(Charset charset, InputStream input) throws IOException {
        ByteOrderMarks bom = Hide.MAPPING.get(charset);
        if (bom == null) {
            return null; // no bom charset
        }
        return bom.match(Files.readByteArray(input, bom.length())) ? bom : null;
    }

    public static ByteOrderMarks of(Charset charset, byte[] bytes) {
        ByteOrderMarks bom = Hide.MAPPING.get(charset);
        if (bom == null) {
            return null; // no bom charset
        }

        return bom.match(bytes) ? bom : null;
    }

    // ------------------------------------------------------------------------has bom without charset
    public static boolean has(String path) throws IOException {
        return has(new File(path));
    }

    public static boolean has(File file) throws IOException {
        return has(Files.readByteArray(file, DETECT_COUNT));
    }

    public static boolean has(InputStream input) throws IOException {
        return has(Files.readByteArray(input, DETECT_COUNT));
    }

    public static boolean has(byte[] bytes) {
        return has(detect(bytes, DETECT_COUNT), bytes);
    }

    // ------------------------------------------------------------------------has bom specified charset
    public static boolean has(Charset charset, String path) throws IOException {
        return has(charset, new File(path));
    }

    public static boolean has(Charset charset, File file) throws IOException {
        return of(charset, file) != null;
    }

    public static boolean has(Charset charset, InputStream input) throws IOException {
        return of(charset, input) != null;
    }

    public static boolean has(Charset charset, byte[] bytes) {
        return of(charset, bytes) != null;
    }

    // ------------------------------------------------------------------------add bom
    public static ByteOrderMarks add(String path) throws IOException {
        return add(null, new File(path));
    }

    public static ByteOrderMarks add(File file) throws IOException {
        return add(null, file);
    }

    public static ByteOrderMarks add(Charset charset, String path) throws IOException {
        return add(charset, new File(path));
    }

    public static ByteOrderMarks add(Charset charset, File file) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            byte[] headBytes;
            ByteOrderMarks bom;
            int count;
            if (charset == null) {
                headBytes = new byte[DETECT_COUNT];
                count = raf.read(headBytes);
                charset = detect(headBytes, count);
                if ((bom = Hide.MAPPING.get(charset)) == null) {
                    return null; // not bom charset
                }
            } else {
                if ((bom = Hide.MAPPING.get(charset)) == null) {
                    return null; // not bom charset
                }
                headBytes = new byte[bom.length()];
                count = raf.read(headBytes);
            }
            if (count >= bom.length() && bom.match(headBytes)) {
                return bom; // already has bom
            }

            ByteArrayOutputStream tailBytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[Files.BUFF_SIZE];
            for (int n; (n = raf.read(buffer)) != Files.EOF;) {
                tailBytes.write(buffer, 0, n);
            }
            raf.seek(0); // 将指针移动到文件首部
            raf.write(bom.bytes);
            raf.write(headBytes, 0, count);
            raf.write(tailBytes.toByteArray());
            tailBytes.close();
            return bom;
        }
    }

    // ------------------------------------------------------------------------remove bom
    public static ByteOrderMarks remove(String path) throws IOException {
        return remove(null, new File(path));
    }

    public static ByteOrderMarks remove(File file) throws IOException {
        return remove(null, file);
    }

    public static ByteOrderMarks remove(Charset charset, String path) throws IOException {
        return remove(charset, new File(path));
    }

    public static ByteOrderMarks remove(Charset charset, File file) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long length = raf.length();
            byte[] headBytes;
            ByteOrderMarks bom;
            int count;
            if (charset == null) {
                headBytes = new byte[DETECT_COUNT];
                count = raf.read(headBytes);
                charset = detect(headBytes, count);
                if ((bom = Hide.MAPPING.get(charset)) == null) {
                    return null; // not bom charset
                }
            } else {
                if ((bom = Hide.MAPPING.get(charset)) == null) {
                    return null; // not bom charset
                }
                headBytes = new byte[bom.length()];
                count = raf.read(headBytes);
            }
            if (count < bom.length() || !bom.match(headBytes)) {
                return bom; // not has bom
            }

            ByteArrayOutputStream tailBytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[Files.BUFF_SIZE];
            for (int n; (n = raf.read(buffer)) != Files.EOF;) {
                tailBytes.write(buffer, 0, n);
            }
            raf.seek(0); // 将指针移动到文件首部
            raf.write(headBytes, bom.length(), count - bom.length());
            raf.write(tailBytes.toByteArray());
            raf.setLength(length - bom.length());
            tailBytes.close();
            return bom;
        }
    }

    public static byte[] get(Charset charset) {
        ByteOrderMarks bom = Hide.MAPPING.get(charset);
        return bom == null ? null : bom.bytes();
    }

    // ------------------------------------------------------------------------public methods
    public Charset charset() {
        return this.charset;
    }

    public byte[] bytes() {
        return Arrays.copyOf(this.bytes, this.bytes.length);
    }

    public int length() {
        return this.bytes.length;
    }

    // ------------------------------------------------------------------------private methods
    private boolean match(byte[] array) {
        if (array.length < this.length()) {
            return false;
        }

        for (int i = 0; i < this.length(); i++) {
            if (array[i] != this.bytes[i]) {
                return false;
            }
        }
        return true;
    }

    private static class Hide {
        private static final Map<Charset, ByteOrderMarks> MAPPING = new HashMap<>();
    }

}
