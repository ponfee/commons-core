package code.ponfee.commons.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

/**
 * gzip压缩/解压缩处理
 * @author Ponfee
 */
public final class GzipProcessor {

    static final int BYTE_SIZE = 512;

    /**
     * gzip压缩
     * @param data
     * @return
     */
    public static byte[] compress(byte[] data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(BYTE_SIZE);
        compress(data, baos);
        return baos.toByteArray();
    }

    /**
     * gzip压缩
     * @param data
     * @param output
     */
    public static void compress(byte[] data, OutputStream output) {
        try (GZIPOutputStream gzout = new ExtendedGZIPOutputStream(output)) {
            gzout.write(data);
            gzout.flush();
            gzout.finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * gzip压缩
     * @param input
     * @param output
     */
    public static void compress(InputStream input, OutputStream output) {
        try (GZIPOutputStream gzout = new ExtendedGZIPOutputStream(output)) {
            IOUtils.copyLarge(input, gzout);
            gzout.flush();
            gzout.finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * gzip解压缩
     * @param data
     * @return
     */
    public static byte[] decompress(byte[] data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(BYTE_SIZE);
        decompress(new ByteArrayInputStream(data), baos);
        return baos.toByteArray();
    }

    public static void decompress(byte[] data, OutputStream output) {
        decompress(new ByteArrayInputStream(data), output);
    }

    /**
     * gzip解压缩
     * @param input
     * @param output
     */
    public static void decompress(InputStream input, OutputStream output) {
        try (GZIPInputStream gzin = new GZIPInputStream(input)) {
            IOUtils.copyLarge(gzin, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
