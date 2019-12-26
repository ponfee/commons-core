package code.ponfee.commons.jce.sm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.util.Arrays;

import com.google.common.base.Preconditions;

/**
 * SM4 symmetric cryptor implementation
 * 
 * reference the internet code and refactor optimization
 * 
 * @author Ponfee
 */
public final class SM4 {
    private SM4(){}

    private static final int ENCRYPT_MODE = 1;
    private static final int DECRYPT_MODE = 2;

    // S盒
    private static final byte[] SBOX_TABLE = {
        (byte) 0xd6, (byte) 0x90, (byte) 0xe9, (byte) 0xfe, (byte) 0xcc, (byte) 0xe1,
        (byte) 0x3d, (byte) 0xb7, (byte) 0x16, (byte) 0xb6, (byte) 0x14, (byte) 0xc2,
        (byte) 0x28, (byte) 0xfb, (byte) 0x2c, (byte) 0x05, (byte) 0x2b, (byte) 0x67,
        (byte) 0x9a, (byte) 0x76, (byte) 0x2a, (byte) 0xbe, (byte) 0x04, (byte) 0xc3,
        (byte) 0xaa, (byte) 0x44, (byte) 0x13, (byte) 0x26, (byte) 0x49, (byte) 0x86,
        (byte) 0x06, (byte) 0x99, (byte) 0x9c, (byte) 0x42, (byte) 0x50, (byte) 0xf4,
        (byte) 0x91, (byte) 0xef, (byte) 0x98, (byte) 0x7a, (byte) 0x33, (byte) 0x54,
        (byte) 0x0b, (byte) 0x43, (byte) 0xed, (byte) 0xcf, (byte) 0xac, (byte) 0x62,
        (byte) 0xe4, (byte) 0xb3, (byte) 0x1c, (byte) 0xa9, (byte) 0xc9, (byte) 0x08,
        (byte) 0xe8, (byte) 0x95, (byte) 0x80, (byte) 0xdf, (byte) 0x94, (byte) 0xfa,
        (byte) 0x75, (byte) 0x8f, (byte) 0x3f, (byte) 0xa6, (byte) 0x47, (byte) 0x07,
        (byte) 0xa7, (byte) 0xfc, (byte) 0xf3, (byte) 0x73, (byte) 0x17, (byte) 0xba,
        (byte) 0x83, (byte) 0x59, (byte) 0x3c, (byte) 0x19, (byte) 0xe6, (byte) 0x85,
        (byte) 0x4f, (byte) 0xa8, (byte) 0x68, (byte) 0x6b, (byte) 0x81, (byte) 0xb2,
        (byte) 0x71, (byte) 0x64, (byte) 0xda, (byte) 0x8b, (byte) 0xf8, (byte) 0xeb,
        (byte) 0x0f, (byte) 0x4b, (byte) 0x70, (byte) 0x56, (byte) 0x9d, (byte) 0x35,
        (byte) 0x1e, (byte) 0x24, (byte) 0x0e, (byte) 0x5e, (byte) 0x63, (byte) 0x58,
        (byte) 0xd1, (byte) 0xa2, (byte) 0x25, (byte) 0x22, (byte) 0x7c, (byte) 0x3b,
        (byte) 0x01, (byte) 0x21, (byte) 0x78, (byte) 0x87, (byte) 0xd4, (byte) 0x00,
        (byte) 0x46, (byte) 0x57, (byte) 0x9f, (byte) 0xd3, (byte) 0x27, (byte) 0x52,
        (byte) 0x4c, (byte) 0x36, (byte) 0x02, (byte) 0xe7, (byte) 0xa0, (byte) 0xc4,
        (byte) 0xc8, (byte) 0x9e, (byte) 0xea, (byte) 0xbf, (byte) 0x8a, (byte) 0xd2,
        (byte) 0x40, (byte) 0xc7, (byte) 0x38, (byte) 0xb5, (byte) 0xa3, (byte) 0xf7,
        (byte) 0xf2, (byte) 0xce, (byte) 0xf9, (byte) 0x61, (byte) 0x15, (byte) 0xa1,
        (byte) 0xe0, (byte) 0xae, (byte) 0x5d, (byte) 0xa4, (byte) 0x9b, (byte) 0x34,
        (byte) 0x1a, (byte) 0x55, (byte) 0xad, (byte) 0x93, (byte) 0x32, (byte) 0x30,
        (byte) 0xf5, (byte) 0x8c, (byte) 0xb1, (byte) 0xe3, (byte) 0x1d, (byte) 0xf6,
        (byte) 0xe2, (byte) 0x2e, (byte) 0x82, (byte) 0x66, (byte) 0xca, (byte) 0x60,
        (byte) 0xc0, (byte) 0x29, (byte) 0x23, (byte) 0xab, (byte) 0x0d, (byte) 0x53,
        (byte) 0x4e, (byte) 0x6f, (byte) 0xd5, (byte) 0xdb, (byte) 0x37, (byte) 0x45,
        (byte) 0xde, (byte) 0xfd, (byte) 0x8e, (byte) 0x2f, (byte) 0x03, (byte) 0xff,
        (byte) 0x6a, (byte) 0x72, (byte) 0x6d, (byte) 0x6c, (byte) 0x5b, (byte) 0x51,
        (byte) 0x8d, (byte) 0x1b, (byte) 0xaf, (byte) 0x92, (byte) 0xbb, (byte) 0xdd,
        (byte) 0xbc, (byte) 0x7f, (byte) 0x11, (byte) 0xd9, (byte) 0x5c, (byte) 0x41,
        (byte) 0x1f, (byte) 0x10, (byte) 0x5a, (byte) 0xd8, (byte) 0x0a, (byte) 0xc1,
        (byte) 0x31, (byte) 0x88, (byte) 0xa5, (byte) 0xcd, (byte) 0x7b, (byte) 0xbd,
        (byte) 0x2d, (byte) 0x74, (byte) 0xd0, (byte) 0x12, (byte) 0xb8, (byte) 0xe5,
        (byte) 0xb4, (byte) 0xb0, (byte) 0x89, (byte) 0x69, (byte) 0x97, (byte) 0x4a,
        (byte) 0x0c, (byte) 0x96, (byte) 0x77, (byte) 0x7e, (byte) 0x65, (byte) 0xb9,
        (byte) 0xf1, (byte) 0x09, (byte) 0xc5, (byte) 0x6e, (byte) 0xc6, (byte) 0x84,
        (byte) 0x18, (byte) 0xf0, (byte) 0x7d, (byte) 0xec, (byte) 0x3a, (byte) 0xdc,
        (byte) 0x4d, (byte) 0x20, (byte) 0x79, (byte) 0xee, (byte) 0x5f, (byte) 0x3e,
        (byte) 0xd7, (byte) 0xcb, (byte) 0x39, (byte) 0x48
    };

    private static final int[] FK = {
        0xa3b1bac6, 0x56aa3350, 0x677d9197, 0xb27022dc
    };

    private static final int[] CK = {
        0x00070e15, 0x1c232a31, 0x383f464d, 0x545b6269,
        0x70777e85, 0x8c939aa1, 0xa8afb6bd, 0xc4cbd2d9,
        0xe0e7eef5, 0xfc030a11, 0x181f262d, 0x343b4249,
        0x50575e65, 0x6c737a81, 0x888f969d, 0xa4abb2b9,
        0xc0c7ced5, 0xdce3eaf1, 0xf8ff060d, 0x141b2229,
        0x30373e45, 0x4c535a61, 0x686f767d, 0x848b9299,
        0xa0a7aeb5, 0xbcc3cad1, 0xd8dfe6ed, 0xf4fb0209,
        0x10171e25, 0x2c333a41, 0x484f565d, 0x646b7279 
    };

    public static byte[] encrypt(byte[] key, byte[] input) {
        return crypt(ENCRYPT_MODE, true, key, input);
    }

    public static byte[] encrypt(boolean isPadding, 
                                 byte[] key, byte[] input) {
        return crypt(ENCRYPT_MODE, isPadding, key, input);
    }

    public static byte[] decrypt(byte[] key, byte[] input) {
        return crypt(DECRYPT_MODE, true, key, input);
    }

    public static byte[] decrypt(boolean isPadding, 
                                 byte[] key, byte[] input) {
        return crypt(DECRYPT_MODE, isPadding, key, input);
    }

    public static byte[] encrypt(byte[] key, byte[] iv, byte[] input) {
        return crypt(ENCRYPT_MODE, true, key, iv, input);
    }

    public static byte[] encrypt(boolean isPadding, byte[] key, 
                                 byte[] iv, byte[] input) {
        return crypt(ENCRYPT_MODE, isPadding, key, iv, input);
    }

    public static byte[] decrypt(byte[] key, byte[] iv, byte[] input) {
        return crypt(DECRYPT_MODE, true, key, iv, input);
    }

    public static byte[] decrypt(boolean isPadding, byte[] key, 
                                 byte[] iv, byte[] input) {
        return crypt(DECRYPT_MODE, isPadding, key, iv, input);
    }

    /**
     * 不带向量的加密（ECB模式）
     *
     * @param mode
     * @param isPadding
     * @param key
     * @param input
     * @return
     */
    private static byte[] crypt(int mode, boolean isPadding, 
                                byte[] key, byte[] input) {
        Preconditions.checkArgument(ArrayUtils.isNotEmpty(input), 
                                    "input cannot not null.");

        long[] sk = setKey(mode, key);

        if (isPadding && (mode == ENCRYPT_MODE)) {
            input = padding(input, ENCRYPT_MODE);
        }
        ByteArrayInputStream bins = new ByteArrayInputStream(input);
        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        for (int length = input.length; length > 0; length -= 16) {
            byte[] in = new byte[16];
            byte[] out = new byte[16];
            bins.read(in, 0, in.length);
            oneRound(sk, in, out);
            bous.write(out, 0, out.length);
        }

        byte[] output = bous.toByteArray();
        if (isPadding && mode == DECRYPT_MODE) {
            output = padding(output, DECRYPT_MODE);
        }
        return output;
    }

    /**
     * 带向量的加密（CBC模式）
     *
     * @param mode
     * @param isPadding
     * @param key
     * @param iv
     * @param input
     * @return
     */
    private static byte[] crypt(int mode, boolean isPadding, byte[] key, 
                                @Nonnull byte[] iv, byte[] input) {
        if (ArrayUtils.isEmpty(input)) {
            throw new IllegalArgumentException("Input cannot not null.");
        }
        if (iv == null || iv.length != 16) {
            throw new IllegalArgumentException("Iv must be 16 byte array.");
        }

        long[] sk = setKey(mode, key);
        iv = Arrays.copyOf(iv, iv.length);
        if (isPadding && mode == ENCRYPT_MODE) {
            input = padding(input, ENCRYPT_MODE);
        }

        ByteArrayInputStream bins  = new ByteArrayInputStream(input);
        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        int i, length = input.length;
        if (mode == ENCRYPT_MODE) {
            for (; length > 0; length -= 16) {
                byte[] in = new byte[16];
                byte[] out = new byte[16];
                byte[] out1 = new byte[16];

                bins.read(in, 0, in.length);
                for (i = 0; i < 16; i++) {
                    out[i] = ((byte) (in[i] ^ iv[i]));
                }
                oneRound(sk, out, out1);
                System.arraycopy(out1, 0, iv, 0, 16);
                bous.write(out1, 0, out1.length);
            }
        } else {
            byte[] temp = new byte[16];
            for (; length > 0; length -= 16) {
                byte[] in = new byte[16];
                byte[] out = new byte[16];
                byte[] out1 = new byte[16];

                bins.read(in, 0, in.length);
                System.arraycopy(in, 0, temp, 0, 16);
                oneRound(sk, in, out);
                for (i = 0; i < 16; i++) {
                    out1[i] = ((byte) (out[i] ^ iv[i]));
                }
                System.arraycopy(temp, 0, iv, 0, 16);
                bous.write(out1, 0, out1.length);
            }
        }

        byte[] output = bous.toByteArray();
        if (isPadding && mode == DECRYPT_MODE) {
            output = padding(output, DECRYPT_MODE);
        }
        return output;
    }

    private static long toLong(byte[] bytes, int offset) {
        return ((long) (bytes[  offset] & 0xFF) << 24)
             | ((long) (bytes[++offset] & 0xFF) << 16)
             | ((long) (bytes[++offset] & 0xFF) <<  8)
             | ((long) (bytes[++offset] & 0xFF)      )
             & (             0xFFFFFFFFL             );
    }

    private static void toByteArray(long n, byte[] bytes, int offset) {
        bytes[  offset] = (byte) (n >>> 24);
        bytes[++offset] = (byte) (n >>> 16);
        bytes[++offset] = (byte) (n >>>  8);
        bytes[++offset] = (byte) (n       );
    }

    /**
     * shift left round
     * @param x
     * @param n
     * @return
     */
    private static long rotateLeft(long x, int n) {
        // ((x & 0xFFFFFFFF) << n) | (x >>> (32 - n));
        return (x << n) | (x >>> (32 - n));
    }

    private static void swap(long[] sk, int i) {
        int j = 31 - i;
        long t = sk[i];
         sk[i] = sk[j];
         sk[j] = t;
    }

    private static byte sm4Sbox(byte inch) {
        return SBOX_TABLE[inch & 0xFF];
    }

    private static long sm4Lt(long ka) {
        byte[] a = new byte[4];
        toByteArray(ka, a, 0);
        byte[] b = {
            sm4Sbox(a[0]), sm4Sbox(a[1]),
            sm4Sbox(a[2]), sm4Sbox(a[3]),
        };
        long x = toLong(b, 0);
        return x 
             ^ rotateLeft(x, 2) 
             ^ rotateLeft(x, 10) 
             ^ rotateLeft(x, 18) 
             ^ rotateLeft(x, 24);
    }

    private static long sm4F(long x0, long x1, long x2, long x3, long rk) {
        return x0 ^ sm4Lt(x1 ^ x2 ^ x3 ^ rk);
    }

    private static long sm4CalciRK(long ka) {
        byte[] a = new byte[4];
        toByteArray(ka, a, 0);

        byte[] b = {
            sm4Sbox(a[0]), sm4Sbox(a[1]),
            sm4Sbox(a[2]), sm4Sbox(a[3])
        };
        long x = toLong(b, 0);
        return x ^ rotateLeft(x, 13) ^ rotateLeft(x, 23);
    }

    private static long[] setKey(int mode, @Nonnull byte[] key) {
        if (key == null || key.length != 16) {
            throw new IllegalArgumentException("Key must be 16 byte array.");
        }

        key = Arrays.copyOf(key, key.length);
        long[] MK = {
            toLong(key, 0), toLong(key, 4),
            toLong(key, 8), toLong(key, 12)
        };
        long[] k = new long[36];
        k[0] = MK[0] ^ (long) FK[0];
        k[1] = MK[1] ^ (long) FK[1];
        k[2] = MK[2] ^ (long) FK[2];
        k[3] = MK[3] ^ (long) FK[3];
        long[] SK = new long[32];
        int i = 0;
        for (; i < 32; i++) {
            k[(i + 4)] = k[i] 
                       ^ sm4CalciRK(k[(i + 1)] 
                       ^ k[(i + 2)] 
                       ^ k[(i + 3)] 
                       ^ (long) CK[i]);
            SK[i] = k[(i + 4)];
        }

        if (mode == DECRYPT_MODE) {
            for (i = 0; i < 16; i++) {
                swap(SK, i);
            }
        }
        return SK;
    }

    private static void oneRound(long[] sk, byte[] input, byte[] output) {
        long[] ulbuf = new long[36];
        ulbuf[0] = toLong(input, 0);
        ulbuf[1] = toLong(input, 4);
        ulbuf[2] = toLong(input, 8);
        ulbuf[3] = toLong(input, 12);
        for (int i = 0; i < 32; i++) {
            ulbuf[(i + 4)] = sm4F(ulbuf[i], 
                                  ulbuf[(i + 1)], 
                                  ulbuf[(i + 2)], 
                                  ulbuf[(i + 3)], 
                                  sk[i]);
        }
        toByteArray(ulbuf[35], output, 0);
        toByteArray(ulbuf[34], output, 4);
        toByteArray(ulbuf[33], output, 8);
        toByteArray(ulbuf[32], output, 12);
    }

    private static byte[] padding(byte[] input, int mode) {
        if (input == null) {
            return null;
        }

        byte[] result;
        if (mode == ENCRYPT_MODE) {
            int p = 16 - input.length & 0xF; // % 16
            result = new byte[input.length + p];
            System.arraycopy(input, 0, result, 0, input.length);
            for (int i = 0; i < p; i++) {
                result[input.length + i] = (byte) p;
            }
        } else {
            int p = input[input.length - 1];
            result = new byte[input.length - p];
            System.arraycopy(input, 0, result, 0, input.length - p);
        }
        return result;
    }

}
