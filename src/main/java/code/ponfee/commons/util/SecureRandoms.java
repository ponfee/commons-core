package code.ponfee.commons.util;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * 安全随机数生成工具类
 * @author Ponfee
 */
public final class SecureRandoms {

    /** SHA1PRNG */
    private static final SecureRandom SECURE_RANDOM =
        new SecureRandom(new SecureRandom(ObjectUtils.uuid()).generateSeed(24));

    /**
     * random byte[] array by SecureRandom
     * @param numOfByte
     * @return
     */
    public static byte[] nextBytes(int numOfByte) {
        byte[] bytes = new byte[numOfByte];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }

    /**
     * returns a pseudo random int, between 0 and bound
     * @param bound
     * @return
     */
    public static int nextInt(int bound) {
        return SECURE_RANDOM.nextInt(bound);
    }

    public static int nextInt() {
        return SECURE_RANDOM.nextInt();
    }

    public static long nextLong() {
        return SECURE_RANDOM.nextLong();
    }

    public static float nextFloat() {
        return SECURE_RANDOM.nextFloat();
    }

    public static double nextDouble() {
        return SECURE_RANDOM.nextDouble();
    }

    public static boolean nextBoolean() {
        return SECURE_RANDOM.nextBoolean();
    }

    /**
     * Returns a pseudo random BigInteger specified bit length
     *
     * @param bitLen specified bit length
     * @return a pseudo random BigInteger
     */
    public static BigInteger random(int bitLen) {
        BigInteger rnd;
        do {
            rnd = new BigInteger(bitLen, SECURE_RANDOM);
        } while (rnd.bitLength() != bitLen);
        return rnd;
    }

    /**
     * Returns a pseudo random BigInteger, the bit length
     * equals mod's bit length - 1
     *
     * @param mod the modulo of maximum bounds
     * @return a pseudo random BigInteger
     */
    public static BigInteger random(BigInteger mod) {
        return random(mod.bitLength() - 1);
    }

    public static byte[] generateSeed(int numBytes) {
        return SECURE_RANDOM.generateSeed(numBytes);
    }

}
