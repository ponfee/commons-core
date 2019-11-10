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
public enum WindowsBOM {

    UTF_8   (StandardCharsets.UTF_8,      (byte) 0xEF, (byte) 0xBB, (byte) 0xBF             ), //
    UTF_16LE(StandardCharsets.UTF_16LE,   (byte) 0xFF, (byte) 0xFE                          ), //
    UTF_16BE(StandardCharsets.UTF_16BE,   (byte) 0xFE, (byte) 0xFF                          ), //
    UTF_32LE(Charset.forName("UTF-32LE"), (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00), //
    UTF_32BE(Charset.forName("UTF-32BE"), (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF), //

    ;

    private final Charset charset;
    private final byte[] bom;

    WindowsBOM(Charset charset, byte... bom) {
        this.charset = charset;
        this.bom = bom;
        Hide.MAPPING.put(charset, this);
    }

    // ------------------------------------------------------------------------of bom without charset
    public static WindowsBOM of(String path) throws IOException {
        return of(new File(path));
    }

    public static WindowsBOM of(File file) throws IOException {
        return of(Files.readByteArray(file, DETECT_COUNT));
    }

    public static WindowsBOM of(InputStream input) throws IOException {
        return of(Files.readByteArray(input, DETECT_COUNT));
    }

    public static WindowsBOM of(byte[] bytes) {
        return of(detect(bytes, DETECT_COUNT), bytes);
    }

    // ------------------------------------------------------------------------of bom specified charset
    public static WindowsBOM of(Charset charset, String path) throws IOException {
        return of(charset, new File(path));
    }

    public static WindowsBOM of(Charset charset, File file) throws IOException {
        WindowsBOM wbom = Hide.MAPPING.get(charset);
        if (wbom == null) {
            return null; // no bom charset
        }
        return match(wbom, Files.readByteArray(file, wbom.length())) ? wbom : null;
    }

    public static WindowsBOM of(Charset charset, InputStream input) throws IOException {
        WindowsBOM wbom = Hide.MAPPING.get(charset);
        if (wbom == null) {
            return null; // no bom charset
        }
        return match(wbom, Files.readByteArray(input, wbom.length())) ? wbom : null;
    }

    public static WindowsBOM of(Charset charset, byte[] bytes) {
        WindowsBOM wbom = Hide.MAPPING.get(charset);
        if (wbom == null) {
            return null; // no bom charset
        }

        return match(wbom, bytes) ? wbom : null;
    }

    // ------------------------------------------------------------------------has bom without charset
    public static boolean hasBOM(String path) throws IOException {
        return hasBOM(new File(path));
    }

    public static boolean hasBOM(File file) throws IOException {
        return hasBOM(Files.readByteArray(file, DETECT_COUNT));
    }

    public static boolean hasBOM(InputStream input) throws IOException {
        return hasBOM(Files.readByteArray(input, DETECT_COUNT));
    }

    public static boolean hasBOM(byte[] bytes) {
        return hasBOM(detect(bytes, DETECT_COUNT), bytes);
    }

    // ------------------------------------------------------------------------has bom specified charset
    public static boolean hasBOM(Charset charset, String path) throws IOException {
        return hasBOM(charset, new File(path));
    }

    public static boolean hasBOM(Charset charset, File file) throws IOException {
        return of(charset, file) != null;
    }

    public static boolean hasBOM(Charset charset, InputStream input) throws IOException {
        return of(charset, input) != null;
    }

    public static boolean hasBOM(Charset charset, byte[] bytes) {
        return of(charset, bytes) != null;
    }

    // ------------------------------------------------------------------------add bom
    public static WindowsBOM addBOM(String path) throws IOException {
        return addBOM(null, new File(path));
    }

    public static WindowsBOM addBOM(File file) throws IOException {
        return addBOM(null, file);
    }

    public static WindowsBOM addBOM(Charset charset, String path) throws IOException {
        return addBOM(charset, new File(path));
    }

    public static WindowsBOM addBOM(Charset charset, File file) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            byte[] headBytes;
            WindowsBOM wbom;
            int count;
            if (charset == null) {
                headBytes = new byte[DETECT_COUNT];
                count = raf.read(headBytes);
                charset = detect(headBytes, count);
                if ((wbom = Hide.MAPPING.get(charset)) == null) {
                    return null; // not bom charset
                }
            } else {
                if ((wbom = Hide.MAPPING.get(charset)) == null) {
                    return null; // not bom charset
                }
                headBytes = new byte[wbom.length()];
                count = raf.read(headBytes);
            }
            if (count >= wbom.length() && match(wbom, headBytes)) {
                return wbom; // already has bom
            }

            ByteArrayOutputStream tailBytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[Files.BUFF_SIZE];
            for (int n; (n = raf.read(buffer)) != Files.EOF;) {
                tailBytes.write(buffer, 0, n);
            }
            raf.seek(0); // 将指针移动到文件首部
            raf.write(wbom.bom);
            raf.write(headBytes, 0, count);
            raf.write(tailBytes.toByteArray());
            tailBytes.close();
            return wbom;
        }
    }

    // ------------------------------------------------------------------------remove bom
    public static WindowsBOM removeBOM(String path) throws IOException {
        return removeBOM(null, new File(path));
    }

    public static WindowsBOM removeBOM(File file) throws IOException {
        return removeBOM(null, file);
    }

    public static WindowsBOM removeBOM(Charset charset, String path) throws IOException {
        return removeBOM(charset, new File(path));
    }

    public static WindowsBOM removeBOM(Charset charset, File file) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long length = raf.length();
            byte[] headBytes;
            WindowsBOM wbom;
            int count;
            if (charset == null) {
                headBytes = new byte[DETECT_COUNT];
                count = raf.read(headBytes);
                charset = detect(headBytes, count);
                if ((wbom = Hide.MAPPING.get(charset)) == null) {
                    return null; // not bom charset
                }
            } else {
                if ((wbom = Hide.MAPPING.get(charset)) == null) {
                    return null; // not bom charset
                }
                headBytes = new byte[wbom.length()];
                count = raf.read(headBytes);
            }
            if (count < wbom.length() || !match(wbom, headBytes)) {
                return wbom; // not has bom
            }

            ByteArrayOutputStream tailBytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[Files.BUFF_SIZE];
            for (int n; (n = raf.read(buffer)) != Files.EOF;) {
                tailBytes.write(buffer, 0, n);
            }
            raf.seek(0); // 将指针移动到文件首部
            raf.write(headBytes, wbom.length(), count - wbom.length());
            raf.write(tailBytes.toByteArray());
            raf.setLength(length - wbom.length());
            tailBytes.close();
            return wbom;
        }
    }

    public static byte[] getBOM(Charset charset) {
        WindowsBOM wbom = Hide.MAPPING.get(charset);
        return wbom == null ? null : wbom.bom();
    }

    // ------------------------------------------------------------------------private methods
    public Charset charset() {
        return this.charset;
    }

    public byte[] bom() {
        byte[] bytes = new byte[this.bom.length];
        System.arraycopy(this.bom, 0, bytes, 0, this.bom.length);
        return bytes;
    }

    public int length() {
        return this.bom.length;
    }

    private static boolean match(WindowsBOM wbom, byte[] bytes) {
        if (bytes.length < wbom.length()) {
            return false;
        }

        for (int i = 0; i < wbom.length(); i++) {
            if (bytes[i] != wbom.bom[i]) {
                return false;
            }
        }
        return true;
    }

    private static class Hide {
        private static final Map<Charset, WindowsBOM> MAPPING = new HashMap<>();
    }

}
