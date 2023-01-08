/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.implementation.digest;

/**
 * The RipeMD160 digest implementation
 * 
 * @author Ponfee
 */
public class RipeMD160Digest {

    private static final int[][] ARG_ARRAY = {
        { 11, 14, 15, 12, 5, 8, 7, 9, 11, 13, 14, 15, 6, 7, 9, 8,
          7, 6, 8, 13, 11, 9, 7, 15, 7, 12, 15, 9, 11, 7, 13, 12,
          11, 13, 6, 7, 14, 9, 13, 15, 14, 8, 13, 6, 5, 12, 7, 5,
          11, 12, 14, 15, 14, 15, 9, 8, 9, 14, 5, 6, 8, 6, 5, 12,
          9, 15, 5, 11, 6, 8, 13, 12, 5, 12, 13, 14, 11, 8, 5, 6 
        },
        { 8, 9, 9, 11, 13, 15, 15, 5, 7, 7, 8, 11, 14, 14, 12, 6,
          9, 13, 15, 7, 12, 8, 9, 11, 7, 7, 12, 7, 6, 15, 13, 11,
          9, 7, 15, 11, 8, 6, 6, 14, 12, 13, 5, 14, 13, 13, 7, 5,
          15, 5, 8, 11, 14, 14, 6, 14, 6, 9, 12, 9, 12, 5, 15, 8,
          8, 5, 12, 9, 12, 5, 14, 6, 8, 13, 6, 5, 15, 13, 11, 11 
        } 
    };

    private static final int[][] IDX_ARRAY = {
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
          7, 4, 13, 1, 10, 6, 15, 3, 12, 0, 9, 5, 2, 14, 11, 8,
          3, 10, 14, 4, 9, 15, 8, 1, 2, 7, 0, 6, 13, 11, 5, 12,
          1, 9, 11, 10, 0, 8, 12, 4, 13, 3, 7, 15, 14, 5, 6, 2,
          4, 0, 5, 9, 7, 12, 2, 10, 14, 1, 3, 8, 11, 6, 15, 13 
        },
        { 5, 14, 7, 0, 9, 2, 11, 4, 13, 6, 15, 8, 1, 10, 3, 12,
          6, 11, 3, 7, 0, 13, 5, 10, 14, 15, 8, 12, 4, 9, 1, 2,
          15, 5, 1, 3, 7, 14, 6, 9, 11, 8, 12, 2, 10, 0, 4, 13,
          8, 6, 4, 1, 3, 11, 15, 0, 5, 12, 2, 13, 9, 7, 10, 14,
          12, 15, 10, 4, 1, 5, 8, 7, 6, 2, 13, 14, 0, 3, 9, 11 
        } 
    };

    /** 链变量 */
    private static final int[] CHAIN_VAR = {
        0x67452301, 0xefcdab89,
        0x98badcfe, 0x10325476,
        0xc3d2e1f0
    };

    /** 分组中每块的大小 */
    private static final int BLOCK_SIZE = 64;

    /** 摘要byte大小 */
    private static final int DIGEST_SIZE = 20;

    private final int[] digest = new int[CHAIN_VAR.length];
    private int[] working;
    private int wOffset;
    private int byteCount;

    private RipeMD160Digest() {
        reset();
    }

    private RipeMD160Digest(RipeMD160Digest d) {
        System.arraycopy(d.digest, 0, this.digest, 0, d.digest.length);
        System.arraycopy(d.working, 0, this.working, 0, d.working.length);
        this.wOffset = d.wOffset;
        this.byteCount = d.byteCount;
    }

    public static RipeMD160Digest getInstance() {
        return new RipeMD160Digest();
    }

    public static RipeMD160Digest getInstance(RipeMD160Digest d) {
        return new RipeMD160Digest(d);
    }

    public void reset() {
        System.arraycopy(CHAIN_VAR, 0, digest, 0, CHAIN_VAR.length);
        working = new int[16];
        wOffset = 0;
        byteCount = 0;
    }

    public void update(byte input) {
        working[wOffset >> 2] ^= ((int) input) << ((wOffset & 3) << 3);
        wOffset++;
        if (wOffset == BLOCK_SIZE) {
            digestBlock(working);
            for (int j = 0; j < 16; j++) {
                working[j] = 0;
            }
            wOffset = 0;
        }
        byteCount++;
    }

    public void update(byte[] input) {
        this.update(input, 0, input.length);
    }

    public void update(byte[] input, int offset, int len) {
        len = Math.min(len, input.length - offset);
        for (int i = offset; i < offset + len; i++) {
            this.update(input[i]);
        }
    }

    public void update(String s) {
        byte[] array = new byte[s.length()];
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) s.charAt(i);
        }
        update(array);
    }

    public byte[] doFinal() {
        finish(working, byteCount, 0);
        byte[] result = new byte[digest.length << 2];

        for (int i = 0; i < DIGEST_SIZE; i++) {
            result[i] = (byte) (digest[i >> 2] >>> ((i & 3) << 3));
        }

        reset();

        return result;
    }

    public byte[] doFinal(byte[] input) {
        this.update(input, 0, input.length);
        return doFinal();
    }

    public byte[] doFinal(byte[] input, int offset, int len) {
        this.update(input, offset, len);
        return doFinal();
    }

    // --------------------------------------------------private methods
    private void digestBlock(int[] X) {
        int a, b, c, d, e;
        int A, B, C, D, E;
        int i = 0, temp, s;

        A = a = digest[0];
        B = b = digest[1];
        C = c = digest[2];
        D = d = digest[3];
        E = e = digest[4];

        for (; i < 16; i++) {
            // The 16 FF functions - round 1 */
            temp = a + (b ^ c ^ d) + X[IDX_ARRAY[0][i]];
            a = e;
            e = d;
            d = (c << 10) | (c >>> 22);
            c = b;
            s = ARG_ARRAY[0][i];
            b = ((temp << s) | (temp >>> (32 - s))) + a;

            // The 16 JJJ functions - parallel round 1 */
            temp = A + (B ^ (C | ~D)) + X[IDX_ARRAY[1][i]] + 0x50a28be6;
            A = E;
            E = D;
            D = (C << 10) | (C >>> 22);
            C = B;
            s = ARG_ARRAY[1][i];
            B = ((temp << s) | (temp >>> (32 - s))) + A;
        }

        for (; i < 32; i++) {
            // The 16 GG functions - round 2 */
            temp = a + ((b & c) | (~b & d)) + X[IDX_ARRAY[0][i]] + 0x5a827999;
            a = e;
            e = d;
            d = (c << 10) | (c >>> 22);
            c = b;
            s = ARG_ARRAY[0][i];
            b = ((temp << s) | (temp >>> (32 - s))) + a;

            // The 16 III functions - parallel round 2 */
            temp = A + ((B & D) | (C & ~D)) + X[IDX_ARRAY[1][i]] + 0x5c4dd124;
            A = E;
            E = D;
            D = (C << 10) | (C >>> 22);
            C = B;
            s = ARG_ARRAY[1][i];
            B = ((temp << s) | (temp >>> (32 - s))) + A;
        }

        for (; i < 48; i++) {
            // The 16 HH functions - round 3 */
            temp = a + ((b | ~c) ^ d) + X[IDX_ARRAY[0][i]] + 0x6ed9eba1;
            a = e;
            e = d;
            d = (c << 10) | (c >>> 22);
            c = b;
            s = ARG_ARRAY[0][i];
            b = ((temp << s) | (temp >>> (32 - s))) + a;

            // The 16 HHH functions - parallel round 3 */
            temp = A + ((B | ~C) ^ D) + X[IDX_ARRAY[1][i]] + 0x6d703ef3;
            A = E;
            E = D;
            D = (C << 10) | (C >>> 22);
            C = B;
            s = ARG_ARRAY[1][i];
            B = ((temp << s) | (temp >>> (32 - s))) + A;
        }

        for (; i < 64; i++) {
            // The 16 II functions - round 4 */
            temp = a + ((b & d) | (c & ~d)) + X[IDX_ARRAY[0][i]] + 0x8f1bbcdc;
            a = e;
            e = d;
            d = (c << 10) | (c >>> 22);
            c = b;
            s = ARG_ARRAY[0][i];
            b = ((temp << s) | (temp >>> (32 - s))) + a;

            // The 16 GGG functions - parallel round 4 */
            temp = A + ((B & C) | (~B & D)) + X[IDX_ARRAY[1][i]] + 0x7a6d76e9;
            A = E;
            E = D;
            D = (C << 10) | (C >>> 22);
            C = B;
            s = ARG_ARRAY[1][i];
            B = ((temp << s) | (temp >>> (32 - s))) + A;
        }

        for (; i < 80; i++) {
            // The 16 JJ functions - round 5 */
            temp = a + (b ^ (c | ~d)) + X[IDX_ARRAY[0][i]] + 0xa953fd4e;
            a = e;
            e = d;
            d = (c << 10) | (c >>> 22);
            c = b;
            s = ARG_ARRAY[0][i];
            b = ((temp << s) | (temp >>> (32 - s))) + a;

            // The 16 FFF functions - parallel round 5 */
            temp = A + (B ^ C ^ D) + X[IDX_ARRAY[1][i]];
            A = E;
            E = D;
            D = (C << 10) | (C >>> 22);
            C = B;
            s = ARG_ARRAY[1][i];
            B = ((temp << s) | (temp >>> (32 - s))) + A;
        }

        /* combine results */
        D += c + digest[1]; /* final result for MDbuf[0] */
        digest[1] = digest[2] + d + E;
        digest[2] = digest[3] + e + A;
        digest[3] = digest[4] + a + B;
        digest[4] = digest[0] + b + C;
        digest[0] = D;
    }

    private void finish(int[] array, int lswlen, int mswlen) {
        /* append the bit m_n == 1 */
        array[(lswlen >> 2) & 15] ^= 1 << (((lswlen & 3) << 3) + 7);

        if ((lswlen & 63) > 55) {
            /* length goes to next block */
            digestBlock(array);
            for (int i = 0; i < 14; i++) {
                array[i] = 0;
            }
        }

        /* append length in bits*/
        array[14] = lswlen << 3;
        array[15] = (lswlen >> 29) | (mswlen << 3);
        digestBlock(array);
    }

    public static int getDigestSize() {
        return DIGEST_SIZE;
    }

}
