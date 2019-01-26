package code.ponfee.commons.jce.passwd;

import static code.ponfee.commons.jce.HmacAlgorithms.ALGORITHM_MAPPING;

import java.security.Provider;
import java.util.Arrays;

import javax.crypto.Mac;

import com.google.common.base.Preconditions;

import code.ponfee.commons.jce.HmacAlgorithms;
import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.jce.digest.HmacUtils;
import code.ponfee.commons.util.Base64UrlSafe;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.SecureRandoms;

/**
 * The passwd crypt based hmac
 * 
 * @author Ponfee
 */
public class Crypt {

    private static final char SEPARATOR = '$';

    public static String create(String passwd) {
        return create(HmacAlgorithms.HmacSHA256, passwd, 32, Providers.BC);
    }

    /**
     * create crypt
     * @param alg
     * @param passwd
     * @param rounds    the loop hmac count, between 1 and 255
     * @param provider
     * @return
     */
    public static String create(HmacAlgorithms alg, String passwd, 
                                int rounds, Provider provider) {
        Preconditions.checkArgument(rounds >= 1 && rounds <= 0xFF, 
                                    "iterations must between 1 and 255");

        byte[] salt = SecureRandoms.nextBytes(16);
        int algIdx = ALGORITHM_MAPPING.inverse().get(alg) & 0xF; // maximum is 0xf
        byte[] hashed = crypt(alg, passwd.getBytes(), salt, rounds, provider);

        return new StringBuilder(6 + ((salt.length + hashed.length) << 2) / 3 + 4)
                    .append(SEPARATOR).append(Integer.toString((algIdx << 8) | rounds, 16))
                    .append(SEPARATOR).append(Base64UrlSafe.encode(salt))
                    .append(SEPARATOR).append(Base64UrlSafe.encode(hashed))
                    .toString();
    }

    public static boolean check(String passwd, String hashed) {
        return check(passwd, hashed, null);
    }

    /**
     * check the passwd crypt
     * @param passwd
     * @param hashed
     * @param provider
     * @return {@code true} is success
     */
    public static boolean check(String passwd, String hashed, Provider provider) {
        String[] parts = hashed.split("\\" + SEPARATOR);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid hashed value");
        }

        int params = Integer.parseInt(parts[1], 16);
        HmacAlgorithms alg = ALGORITHM_MAPPING.get(params >> 8 & 0xF);
        byte[] salt = Base64UrlSafe.decode(parts[2]);
        byte[] testHash = crypt(alg, passwd.getBytes(), salt, params & 0xFF, provider);

        // compare
        return Arrays.equals(Base64UrlSafe.decode(parts[3]), testHash);
    }

    /**
     * crypt with hmac
     * @param alg
     * @param password
     * @param salt
     * @param rounds
     * @param provider
     * @return
     */
    private static byte[] crypt(HmacAlgorithms alg, byte[] password, 
                                byte[] salt, int rounds, Provider provider) {
        Mac mac = HmacUtils.getInitializedMac(alg, provider, salt);
        password = mac.doFinal(password);
        for (int i = 1; i < rounds; i++) {
            mac.update(Bytes.fromInt(i));
            password = mac.doFinal(password);
        }
        return password;
    }

}
