/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.implementation.rsa;

import cn.ponfee.commons.io.Files;
import cn.ponfee.commons.jce.HmacAlgorithms;
import cn.ponfee.commons.jce.implementation.Key;
import cn.ponfee.commons.util.Bytes;
import cn.ponfee.commons.util.SecureRandoms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;

import static cn.ponfee.commons.jce.Providers.BC;
import static cn.ponfee.commons.jce.digest.HmacUtils.crypt;

/**
 * RSA Cryptor based sha512 xor 
 * @author Ponfee
 */
public class RSAHashCryptor extends AbstractRSACryptor {

    public RSAHashCryptor() {
        super(false);
    }

    private static final HmacAlgorithms HMAC_ALG = HmacAlgorithms.HmacSHA3_512;

    
    /**
     * (origin ⊕ passwd) ⊕ passwd = origin
     * @param input
     * @param length
     * @param ek
     * @return
     */
    @Override
    public byte[] encrypt(byte[] input, int length, Key ek) {
        RSAKey rsaKey = (RSAKey) ek;
        int keyByteLen = rsaKey.n.bitLength() / 8, count = 1;
        BigInteger exponent = getExponent(rsaKey);

        // 生成随机对称密钥
        BigInteger key = SecureRandoms.random(rsaKey.n); // mod是以1XX开头，key是以01X开头

        // 对密钥进行RSA加密，encryptedKey = key^e mod n
        byte[] encryptedKey = key.modPow(exponent, rsaKey.n).toByteArray();

        byte[] result = new byte[keyByteLen + length];
        // mod pow之后可能被去0或加0
        Bytes.tailCopy(encryptedKey, 0, encryptedKey.length, result, 0, keyByteLen);

        // 对密钥进行HASH
        byte[] keyArray = key.toByteArray();
        byte[] hashedKey = crypt(keyArray, Bytes.toBytes(count), HMAC_ALG, BC);
        for (int keyOffset = 0, i = 0; i < length; i++) {
            if (keyOffset == HMAC_ALG.byteSize()) {
                keyOffset = 0;
                hashedKey = crypt(keyArray, Bytes.toBytes(++count), HMAC_ALG, BC);
            }
            result[keyByteLen + i] = (byte) (input[i] ^ hashedKey[keyOffset++]);
        }
        return result;
    }

    @Override
    public byte[] decrypt(byte[] input, Key dk) {
        RSAKey rsaKey = (RSAKey) dk;
        int keyByteLen = rsaKey.n.bitLength() / 8, count = 1;
        BigInteger exponent = getExponent(rsaKey);

        // 获取被加密的对称密钥数据
        byte[] encryptedKey = Arrays.copyOfRange(input, 0, keyByteLen);

        // 解密被加密的密钥数据，key = encryptedKey^d mod n
        BigInteger key = new BigInteger(1, encryptedKey).modPow(exponent, rsaKey.n);

        // 对密钥进行HASH
        byte[] keyArray = key.toByteArray();
        byte[] hashedKey = crypt(keyArray, Bytes.toBytes(count), HMAC_ALG, BC);
        byte[] result = new byte[input.length - keyByteLen];
        for (int keyOffset = 0, rLen = result.length, i = 0; i < rLen; i++) {
            if (keyOffset == HMAC_ALG.byteSize()) {
                keyOffset = 0;
                hashedKey = crypt(keyArray, Bytes.toBytes(++count), HMAC_ALG, BC);
            }
            result[i] = (byte) (input[keyByteLen + i] ^ hashedKey[keyOffset++]);
        }
        return result;
    }

    @Override
    public void encrypt(InputStream input, Key ek, OutputStream output) {
        RSAKey rsaKey = (RSAKey) ek;
        int keyByteLen = rsaKey.n.bitLength() / 8, count = 1;
        BigInteger exponent = getExponent(rsaKey);

        // 生成随机对称密钥
        BigInteger key = SecureRandoms.random(rsaKey.n);

        // 对密钥进行RSA加密，encryptedKey = key^e mod n
        byte[] encryptedKey = key.modPow(exponent, rsaKey.n).toByteArray();

        byte[] encryptedKey0 = new byte[keyByteLen]; // mod pow之后可能被去0或加0
        Bytes.tailCopy(encryptedKey, 0, encryptedKey.length, encryptedKey0, 0, keyByteLen);

        byte[] keyArray = key.toByteArray();
        byte[] hashedKey = crypt(keyArray, Bytes.toBytes(count), HMAC_ALG, BC);
        try {
            output.write(encryptedKey0); // encrypted key
            byte[] buffer = new byte[this.getOriginBlockSize(rsaKey)];
            for (int keyOffset = 0, len, i; (len = input.read(buffer)) != Files.EOF;) {
                for (i = 0; i < len; i++) {
                    if (keyOffset == HMAC_ALG.byteSize()) {
                        keyOffset = 0;
                        hashedKey = crypt(keyArray, Bytes.toBytes(++count), HMAC_ALG, BC);
                    }
                    output.write((byte) (buffer[i] ^ hashedKey[keyOffset++]));
                }
            }
            output.flush();
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    @Override
    public void decrypt(InputStream input, Key dk, OutputStream output) {
        RSAKey rsaKey = (RSAKey) dk;
        int keyByteLen = rsaKey.n.bitLength() / 8, count = 1;
        BigInteger exponent = getExponent(rsaKey);
        try {
            if (input.available() < keyByteLen) {
                throw new IllegalArgumentException("Invalid cipher data");
            }

            // 获取被加密的对称密钥数据
            byte[] encryptedKey = new byte[keyByteLen];
            input.read(encryptedKey);

            // 解密被加密的密钥数据，key = encryptedKey^d mod n
            BigInteger key = new BigInteger(1, encryptedKey).modPow(exponent, rsaKey.n);

            byte[] buffer = new byte[this.getCipherBlockSize(rsaKey)];
            byte[] keyArray = key.toByteArray();
            byte[] hashedKey = crypt(keyArray, Bytes.toBytes(count), HMAC_ALG, BC);
            for (int keyOffset = 0, len, i; (len = input.read(buffer)) != Files.EOF;) {
                for (i = 0; i < len; i++) {
                    if (keyOffset == HMAC_ALG.byteSize()) {
                        keyOffset = 0;
                        hashedKey = crypt(keyArray, Bytes.toBytes(++count), HMAC_ALG, BC);
                    }
                    output.write((byte) (buffer[i] ^ hashedKey[keyOffset++]));
                }
            }
            output.flush();
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    @Override
    public int getOriginBlockSize(RSAKey rsaKey) {
        return 4096;
    }

    @Override
    public int getCipherBlockSize(RSAKey rsaKey) {
        return this.getOriginBlockSize(rsaKey);
    }

}
