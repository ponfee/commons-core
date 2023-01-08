/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.digest;

import cn.ponfee.commons.io.Files;
import cn.ponfee.commons.jce.DigestAlgorithms;
import cn.ponfee.commons.jce.Providers;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

/**
 * digest算法封装
 * 
 * @author Ponfee
 */
public final class DigestUtils {

    private static final int BUFF_SIZE = 4096;

    public static byte[] md5(InputStream input) {
        return digest(DigestAlgorithms.MD5, input);
    }

    public static byte[] md5(byte[] data) {
        return digest(DigestAlgorithms.MD5, data);
    }

    public static String md5Hex(InputStream input) {
        return Hex.encodeHexString(md5(input));
    }

    public static String md5Hex(byte[] data) {
        return Hex.encodeHexString(md5(data));
    }

    public static String md5Hex(String data) {
        return md5Hex(data.getBytes());
    }

    public static String md5Hex(String data, String charset) {
        return md5Hex(data.getBytes(Charset.forName(charset)));
    }

    public static byte[] sha1(InputStream input) {
        return digest(DigestAlgorithms.SHA1, input);
    }

    public static byte[] sha1(String data) {
        return digest(DigestAlgorithms.SHA1, data.getBytes());
    }

    public static byte[] sha1(byte[] data) {
        return digest(DigestAlgorithms.SHA1, data);
    }

    public static String sha1Hex(InputStream input) {
        return Hex.encodeHexString(sha1(input));
    }

    public static String sha1Hex(byte[] data) {
        return Hex.encodeHexString(sha1(data));
    }

    public static String sha1Hex(String data) {
        return sha1Hex(data.getBytes());
    }

    public static String sha1Hex(String data, String charset) {
        return sha1Hex(data.getBytes(Charset.forName(charset)));
    }

    public static byte[] sha224(byte[] data) {
        return digest(DigestAlgorithms.SHA224, data);
    }

    public static String sha224Hex(byte[] data) {
        return Hex.encodeHexString(sha224(data));
    }

    public static byte[] sha256(byte[] data) {
        return digest(DigestAlgorithms.SHA256, data);
    }

    public static String sha256Hex(byte[] data) {
        return Hex.encodeHexString(sha256(data));
    }

    public static String sha256Hex(String data, String charset) {
        return sha256Hex(data.getBytes(Charset.forName(charset)));
    }

    public static byte[] sha384(byte[] data) {
        return digest(DigestAlgorithms.SHA384, data);
    }

    public static String sha384Hex(byte[] data) {
        return Hex.encodeHexString(sha384(data));
    }

    public static byte[] sha512(byte[] input, byte[]... data) {
        return digest(DigestAlgorithms.SHA512, input, data);
    }

    public static String sha512Hex(byte[] data) {
        return Hex.encodeHexString(sha512(data));
    }

    // ---------------------------------------RipeMD
    public static byte[] ripeMD128(byte[] data) {
        return digest(DigestAlgorithms.RipeMD128, Providers.BC, data);
    }

    public static String ripeMD128Hex(byte[] data) {
        return Hex.encodeHexString(ripeMD128(data));
    }

    public static byte[] ripeMD160(byte[] data) {
        return digest(DigestAlgorithms.RipeMD160, Providers.BC, data);
    }

    public static String ripeMD160Hex(byte[] data) {
        return Hex.encodeHexString(ripeMD160(data));
    }

    public static byte[] ripeMD256(byte[] data) {
        return digest(DigestAlgorithms.RipeMD256, Providers.BC, data);
    }

    public static String ripeMD256Hex(byte[] data) {
        return Hex.encodeHexString(ripeMD256(data));
    }

    public static byte[] ripeMD320(byte[] data) {
        return digest(DigestAlgorithms.RipeMD320, Providers.BC, data);
    }

    public static String ripeMD320Hex(byte[] data) {
        return Hex.encodeHexString(ripeMD320(data));
    }

    // ---------------------------------------------digest
    public static MessageDigest getMessageDigest(DigestAlgorithms alg, Provider provider) {
        try {
            return (provider == null)
                   ? MessageDigest.getInstance(alg.algorithm())
                   : MessageDigest.getInstance(alg.algorithm(), provider);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e); // cannot happened
        }
    }

    public static byte[] digest(DigestAlgorithms alg, byte[] input) {
        return digest(alg, null, input);
    }

    public static byte[] digest(DigestAlgorithms alg, Provider provider, byte[] input) {
        MessageDigest digest = getMessageDigest(alg, provider);
        digest.update(input);
        return digest.digest();
    }

    public static byte[] digest(DigestAlgorithms alg, byte[] input, byte[]... data) {
        return digest(alg, null, input, data);
    }

    /**
     * 数据摘要
     * @param alg       digest算法
     * @param provider
     * @param data      digest data of byte array
     * @return
     */
    public static byte[] digest(DigestAlgorithms alg, Provider provider,
                                byte[] input, byte[]... data) {
        MessageDigest digest = getMessageDigest(alg, provider);
        digest.update(input);
        for (byte[] bytes : data) {
            digest.update(bytes);
        }
        return digest.digest();
    }

    public static byte[] digest(DigestAlgorithms alg, InputStream input) {
        return digest(alg, null, input);
    }

    /**
     * 数据摘要
     * @param alg        digest 算法
     * @param provider
     * @param input      digest data of input stream
     * @return
     */
    public static byte[] digest(DigestAlgorithms alg, Provider provider, 
                                InputStream input) {
        byte[] buff = new byte[BUFF_SIZE];
        MessageDigest digest = getMessageDigest(alg, provider);

        try (InputStream in = input) {
            for (int n; (n = in.read(buff, 0, BUFF_SIZE)) != Files.EOF;) {
                digest.update(buff, 0, n);
            }
            return digest.digest();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /*try (InputStream in = input; 
             DigestInputStream dIn = new DigestInputStream(input, digest)
         ) {
            while (dIn.read(buff, 0, buff.length) != Files.EOF) {
                //  do-non
            }
            return digest.digest();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }

}
