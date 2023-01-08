/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.io;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
    public static long compress(InputStream input, OutputStream output) {
        try (GZIPOutputStream gzout = new ExtendedGZIPOutputStream(output)) {
            long size = IOUtils.copyLarge(input, gzout);
            gzout.flush();
            gzout.finish();
            return size;
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
