// Copyright (C) 2011 - Will Glozer. All rights reserved.

package code.ponfee.commons.jce.passwd;

import static code.ponfee.commons.jce.HmacAlgorithms.ALGORITHM_MAPPING;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.System.arraycopy;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.ShortBufferException;

import com.google.common.base.Preconditions;

import code.ponfee.commons.jce.HmacAlgorithms;
import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.jce.digest.HmacUtils;
import code.ponfee.commons.util.Base64UrlSafe;
import code.ponfee.commons.util.SecureRandoms;

/**
 * An implementation of the <a href="http://www.tarsnap.com/scrypt/scrypt.pdf"/>scrypt</a> 
 * key derivation function. This class will attempt to load a native
 * library containing the optimized C implementation from 
 * <a href="http://www.tarsnap.com/scrypt.html">http://www.tarsnap.com/scrypt.html</a> 
 * and fall back to the pure Java version if that fails.
 * 
 * @author Will Glozer
 * 
 * Reference from internet and with optimization
 */
public final class SCrypt {
    private SCrypt() {}

    private static final char SEPARATOR = '$';

    // -------------------------------------------------------------------pbkdf2
    /**
     * Implementation of PBKDF2 (RFC2898).
     * 
     * @param alg HMAC algorithm to use.
     * @param P Password.
     * @param S Salt.
     * @param c Iteration count.
     * @param dkLen Intended length, in octets, of the derived key (hash byte size).
     * @return The derived key.
     */
    public static String createPbkdf2(HmacAlgorithms alg, byte[] P, byte[] S, 
                                      int c, int dkLen) {
        return Base64UrlSafe.encode(pbkdf2(alg, P, S, c, dkLen));
    }

    /**
     * Checks the pbkdf2 hashed value
     * 
     * @param alg  HMAC algorithm to use.
     * @param P    Password.
     * @param S    Salt.
     * @param c    Iteration count.
     * @param hashed  the hashed value to check
     * @return {@code true} is checked success
     */
    public static boolean checkPbkdf2(HmacAlgorithms alg, byte[] P, byte[] S,
                                      int c, String hashed) {
        byte[] actual = Base64UrlSafe.decode(hashed);
        byte[] except = pbkdf2(alg, P, S, c, actual.length);
        return Arrays.equals(actual, except);
    }

    // -------------------------------------------------------------------scrypt
    public static String create(String passwd, int N, int r, int p) {
        return create(HmacAlgorithms.HmacSHA256, passwd, N, r, p, 32);
    }

    /**
     * Hash the supplied plaintext password and generate output in the format
     * described in {@link #scrypt(HmacAlgorithms, byte[], byte[], int, int, int, int)}.
     *
     * @param alg HmacAlgorithm.
     * @param passwd Password.
     * @param N CPU cost parameter.         between 0x01 and 0x0F, 2^15=32768
     * @param r Memory cost parameter.      between 0x01 and 0xFF
     * @param p Parallelization parameter.  between 0x01 and 0xFF
     * @param dkLen Intended length, in octets, of the derived key.
     * @return The hashed password.
     */
    public static String create(HmacAlgorithms alg, String passwd, 
                                int N, int r, int p, int dkLen) {
        Preconditions.checkArgument(N > 0 && N <= 0xF,  "N must between 1 and 15");
        Preconditions.checkArgument(r > 0 && r <= 0xFF, "r must between 1 and 255");
        Preconditions.checkArgument(p > 0 && p <= 0xFF, "p must between 1 and 255");

        int algIdx = ALGORITHM_MAPPING.inverse().get(alg) & 0xF; // maximum is 0xF
        byte[] salt = SecureRandoms.nextBytes(16);
        byte[] derived = scrypt(alg, passwd.getBytes(UTF_8), salt, 1 << N, r, p, dkLen);
        String params = Integer.toString(algIdx << 20 | N << 16 | r << 8 | p, 16);

        return new StringBuilder(12 + ((salt.length + derived.length) << 2) / 3 + 4)
                        .append(SEPARATOR).append("s0").append(SEPARATOR)
                        .append(params).append(SEPARATOR)
                        .append(Base64UrlSafe.encode(salt)).append(SEPARATOR)
                        .append(Base64UrlSafe.encode(derived)).toString();
    }

    /**
     * Compare the supplied plaintext password to a hashed password.
     * @param passwd Plaintext password.
     * @param hashed scrypt hashed password.
     * @return true if passwd matches hashed value.
     */
    public static boolean check(String passwd, String hashed) {
        String[] parts = hashed.split("\\" + SEPARATOR);

        if (parts.length != 5 || !"s0".equals(parts[1])) {
            throw new IllegalArgumentException("Invalid hashed value");
        }

        int params = Integer.parseInt(parts[2], 16);
        byte[] salt = Base64UrlSafe.decode(parts[3]);
        byte[] actual = Base64UrlSafe.decode(parts[4]);

        int algIdx = (params >> 20) & 0xF ,
                 N = (params >> 16) & 0xF ,
                 r = (params >>  8) & 0xFF,
                 p = (params      ) & 0xFF;

        byte[] except = scrypt(ALGORITHM_MAPPING.get(algIdx), 
                               passwd.getBytes(UTF_8), salt, 
                               1 << N, r, p, actual.length);

        return Arrays.equals(actual, except);
    }

    /**
     * Implementation of PBKDF2 (RFC2898).
     * @param alg HmacAlgorithm.
     * @param P password of byte array.
     * @param S Salt.
     * @param c Iteration count.
     * @param dkLen Intended length, in octets, of the derived key.
     * @return the byte array of DK
     */
    private static byte[] pbkdf2(HmacAlgorithms alg, byte[] P, 
                                 byte[] S, int c, int dkLen) {
        Mac mac = HmacUtils.getInitializedMac(alg, Providers.BC, P);
        int hLen = mac.getMacLength();

        // ((long) 1 << 32) - 1 == 4294967295L
        if (dkLen > 4294967295L * hLen) {
            throw new SecurityException("Requested key length too long");
        }

        byte[] U = new byte[hLen];
        byte[] T = new byte[hLen];
        byte[] block = new byte[S.length + 4];

        int n = (int) Math.ceil((double) dkLen / hLen);
        int r = dkLen - (n - 1) * hLen;

        arraycopy(S, 0, block, 0, S.length);

        byte[] DK = new byte[dkLen];
        for (int i = 1; i <= n; i++) {
            block[S.length    ] = (byte) (i >> 24 & 0xff);
            block[S.length + 1] = (byte) (i >> 16 & 0xff);
            block[S.length + 2] = (byte) (i >> 8  & 0xff);
            block[S.length + 3] = (byte) (i       & 0xff);

            mac.update(block);
            try {
                mac.doFinal(U, 0);
                arraycopy(U, 0, T, 0, hLen);
                for (int j = 1, k; j < c; j++) {
                    mac.update(U);
                    mac.doFinal(U, 0);
                    for (k = 0; k < hLen; k++) {
                        T[k] ^= U[k];
                    }
                }
            } catch (ShortBufferException | IllegalStateException e) {
                throw new SecurityException(e);
            }

            arraycopy(T, 0, DK, (i - 1) * hLen, (i == n ? r : hLen));
        }
        return DK;
    }

    /**
     * Pure Java implementation of the 
     * <a href="http://www.tarsnap.com/scrypt/scrypt.pdf"/>scrypt KDF</a>.
     * @param alg HmacAlgorithm.
     * @param P Password.
     * @param S Salt.
     * @param N CPU cost parameter.
     * @param r Memory cost parameter.
     * @param p Parallelization parameter.
     * @param dkLen Intended length of the derived key.
     * @return The derived key.
     */
    public static byte[] scrypt(HmacAlgorithms alg, byte[] P, byte[] S, 
                                int N, int r, int p, int dkLen) {
        if (r > MAX_VALUE / 128 / p) {
            throw new IllegalArgumentException("Parameter r is too large");
        }

        if (N > MAX_VALUE / 128 / r) {
            throw new IllegalArgumentException("Parameter N is too large");
        }

        byte[] B  = pbkdf2(alg, P, S, 1, (p << 7) * r);
        byte[] XY = new byte[r << 8],
               V  = new byte[(r << 7) * N];

        for (int i = 0; i < p; i++) {
            smix(B, (i << 7) * r, r, N, V, XY);
        }

        return pbkdf2(alg, P, B, 1, dkLen);
    }

    private static void smix(byte[] B, int Bi, int r, int N, byte[] V, byte[] XY) {
        int i, Xi = 0, Yi = r << 7;

        arraycopy(B, Bi, XY, Xi, Yi);

        for (i = 0; i < N; i++) {
            arraycopy(XY, Xi, V, i * Yi, Yi);
            blockmix_salsa8(XY, Xi, Yi, r);
        }

        for (i = 0; i < N; i++) {
            int j = integerify(XY, Xi, r) & (N - 1);
            blockxor(V, j * Yi, XY, Xi, Yi);
            blockmix_salsa8(XY, Xi, Yi, r);
        }

        arraycopy(XY, Xi, B, Bi, Yi);
    }

    private static void blockmix_salsa8(byte[] BY, int Bi, int Yi, int r) {
        byte[] X = new byte[64];

        arraycopy(BY, Bi + ((2 * r - 1) << 6), X, 0, 64);

        int i, n, m;
        for (i = 0, n = r << 1; i < n; i++) {
            m = i << 6;
            blockxor(BY, m, X, 0, 64);
            salsa20_8(X);
            arraycopy(X, 0, BY, Yi + m, 64);
        }

        for (i = 0; i < r; i++) {
            arraycopy(BY, Yi + (i << 7), BY, Bi + (i << 6), 64);
        }

        for (i = 0; i < r; i++) {
            arraycopy(BY, Yi + (((i << 1) + 1) << 6), BY, Bi + ((i + r) << 6), 64);
        }
    }

    private static int R(int a, int b) {
        return (a << b) | (a >>> (32 - b));
    }

    private static void salsa20_8(byte[] B) {
        int[] B32 = new int[16], x = new int[16];

        int i;
        for (i = 0; i < 16; i++) {
            B32[i]  = (B[(i << 2)    ] & 0xff)      ;
            B32[i] |= (B[(i << 2) + 1] & 0xff) <<  8;
            B32[i] |= (B[(i << 2) + 2] & 0xff) << 16;
            B32[i] |= (B[(i << 2) + 3] & 0xff) << 24;
        }

        arraycopy(B32, 0, x, 0, 16);

        for (i = 8; i > 0; i -= 2) {
             x[4] ^=  R(x[0] + x[12],  7);
             x[8] ^=  R(x[4] +  x[0],  9);
            x[12] ^=  R(x[8] +  x[4], 13);
             x[0] ^= R(x[12] +  x[8], 18);
             x[9] ^=  R(x[5] +  x[1],  7);
            x[13] ^=  R(x[9] +  x[5],  9);
             x[1] ^= R(x[13] +  x[9], 13);
             x[5] ^=  R(x[1] + x[13], 18);
            x[14] ^= R(x[10] +  x[6],  7);
             x[2] ^= R(x[14] + x[10],  9);
             x[6] ^=  R(x[2] + x[14], 13);
            x[10] ^=  R(x[6] +  x[2], 18);
             x[3] ^= R(x[15] + x[11],  7);
             x[7] ^=  R(x[3] + x[15],  9);
            x[11] ^=  R(x[7] +  x[3], 13);
            x[15] ^= R(x[11] +  x[7], 18);
             x[1] ^=  R(x[0] +  x[3],  7);
             x[2] ^=  R(x[1] +  x[0],  9);
             x[3] ^=  R(x[2] +  x[1], 13);
             x[0] ^=  R(x[3] +  x[2], 18);
             x[6] ^=  R(x[5] +  x[4],  7);
             x[7] ^=  R(x[6] +  x[5],  9);
             x[4] ^=  R(x[7] +  x[6], 13);
             x[5] ^=  R(x[4] +  x[7], 18);
            x[11] ^= R(x[10] +  x[9],  7);
             x[8] ^= R(x[11] + x[10],  9);
             x[9] ^=  R(x[8] + x[11], 13);
            x[10] ^=  R(x[9] +  x[8], 18);
            x[12] ^= R(x[15] + x[14],  7);
            x[13] ^= R(x[12] + x[15],  9);
            x[14] ^= R(x[13] + x[12], 13);
            x[15] ^= R(x[14] + x[13], 18);
        }

        for (i = 0; i < 16; ++i) {
            B32[i] = x[i] + B32[i];
        }

        for (i = 0; i < 16; i++) {
            B[(i << 2)    ] = (byte) (B32[i]       & 0xff);
            B[(i << 2) + 1] = (byte) (B32[i] >> 8  & 0xff);
            B[(i << 2) + 2] = (byte) (B32[i] >> 16 & 0xff);
            B[(i << 2) + 3] = (byte) (B32[i] >> 24 & 0xff);
        }
    }

    private static void blockxor(byte[] S, int Si, byte[] D, int Di, int len) {
        for (int i = 0; i < len; i++) {
            D[Di + i] ^= S[Si + i];
        }
    }

    private static int integerify(byte[] B, int Bi, int r) {
        Bi += ((2 * r - 1) << 6);
        return ((B[Bi    ] & 0xff)      )
             | ((B[Bi + 1] & 0xff) <<  8)
             | ((B[Bi + 2] & 0xff) << 16)
             | ((B[Bi + 3] & 0xff) << 24);
    }

}
