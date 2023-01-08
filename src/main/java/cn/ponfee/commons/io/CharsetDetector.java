/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.io;

import cn.ponfee.commons.math.Numbers;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 字符编码检测
 *
 * @author Ponfee
 */
public class CharsetDetector {

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static final int DEFAULT_DETECT_LENGTH = 3600;

    public static Charset detect(String path) {
        return detect(path, DEFAULT_DETECT_LENGTH);
    }

    public static Charset detect(String path, int length) {
        return detect(new File(path), length);
    }

    public static Charset detect(File file) {
        return detect(file, DEFAULT_DETECT_LENGTH);
    }

    public static Charset detect(File file, int length) {
        try (InputStream input = new FileInputStream(file)) {
            return detect(input, (int) Math.min(file.length(), length));
        } catch (IOException e) {
            throw new RuntimeException("Detect file '" + file.getPath() + "' occur error.", e);
        }
    }

    public static Charset detect(URL url) {
        return detect(url, DEFAULT_DETECT_LENGTH);
    }

    public static Charset detect(URL url, int length) {
        // 对于网络输入流的InputStream.available()表示对方发过来可用的数据，并不是整个流的大小
        try (InputStream input = url.openStream()) {
            return detect(input, length);
        } catch (IOException e) {
            throw new RuntimeException("Detect url '" + url.getPath() + "' occur error.", e);
        }
    }

    public static Charset detect(byte[] bytes) {
        return detect(bytes, 0, bytes.length);
    }

    public static Charset detect(byte[] bytes, int length) {
        return detect(bytes, 0, length);
    }

    public static Charset detect(byte[] bytes, int offset, int length) {
        offset = Numbers.bounds(offset, 0, bytes.length);
        length = Numbers.bounds(length, 0, bytes.length - offset);
        try {
            return detect(new ByteArrayInputStream(bytes, offset, length), length);
        } catch (IOException e) {
            throw new RuntimeException("Detect byte array charset occur error.", e);
        }
    }

    public static Charset detect(InputStream input) throws IOException {
        return detect(input, DEFAULT_DETECT_LENGTH);
    }

    public static Charset detect(InputStream input, int length) throws IOException {
        //return cn.ponfee.commons.io.charset.CodepageDetector.detect(input, length);
        //return cn.ponfee.commons.io.charset.JchardetDetector.detect(input, length);
        //return cn.ponfee.commons.io.charset.BytesDetector.detect(input, length);
        return cn.ponfee.commons.io.charset.TikaDetector.detect(input, length);
    }

}
