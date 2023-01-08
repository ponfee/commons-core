/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.sm;

import cn.ponfee.commons.jce.ECParameters;
import cn.ponfee.commons.util.Base64UrlSafe;
import cn.ponfee.commons.util.Bytes;
import cn.ponfee.commons.util.SecureRandoms;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * 基于椭圆曲线
 *
 * new BigInteger("0") // 0为十进制数字符串表示
 * SM2 asymmetric cipher implementation
 * support encrypt/decrypt
 * and sign/verify signature
 * 
 * reference the internet code and refactor optimization
 * 
 * @author Ponfee
 */
public final class SM2 {

    public static final String PRIVATE_KEY = "SM2PrivateKey";
    public static final String PUBLIC_KEY = "SM2PublicKey";
    private static final int KEY_LENGTH = SM3Digest.getDigestSize();

    private final byte[] key = new byte[KEY_LENGTH];
    private final SM3Digest sm3keybase = SM3Digest.getInstance(); // sm3 keybase
    private final SM3Digest sm3c3 = SM3Digest.getInstance(); // sm3 c3
    private final byte[] x, y;

    private byte keyOffset;
    private int count;

    private SM2(ECPoint publicKey, BigInteger privateKey, BigInteger n) {
        Objects.requireNonNull(publicKey, "public key cannot be null.");
        Objects.requireNonNull(privateKey, "private key cannot be null.");

        ECPoint point = publicKey.multiply(privateKey); // S = [h]point

        byte[] x1 = point.normalize().getXCoord().toBigInteger().toByteArray();
        byte[] y1 = point.normalize().getYCoord().toBigInteger().toByteArray();
        int byteCount = (int) Math.ceil(n.bitLength() / 8.0D);

        this.x = new byte[byteCount];
        this.y = new byte[byteCount];
        Bytes.tailCopy(x1, 0, x1.length, this.x, 0, this.x.length);
        Bytes.tailCopy(y1, 0, y1.length, this.y, 0, this.y.length);

        this.reset();
    }

    private void reset() {
        this.sm3keybase.reset();
        this.sm3keybase.update(this.x);
        this.sm3keybase.update(this.y);

        this.sm3c3.reset();
        this.sm3c3.update(this.x);

        this.count = 1;
        nextKey();
    }

    private void nextKey() {
        SM3Digest sm3keycur = SM3Digest.getInstance(this.sm3keybase);
        sm3keycur.update(Bytes.toBytes(count));
        sm3keycur.doFinal(key, 0); // update key
        this.keyOffset = 0;
        this.count++;
    }

    private void encrypt(byte[] data) {
        this.sm3c3.update(data);

        for (int i = 0, len = data.length; i < len;) {
            if (keyOffset == KEY_LENGTH) {
                nextKey();
            }
            data[i++] ^= key[keyOffset++];
        }
    }

    private void decrypt(byte[] data) {
        for (int i = 0, len = data.length; i < len;) {
            if (keyOffset == KEY_LENGTH) {
                nextKey();
            }
            data[i++] ^= key[keyOffset++];
        }

        this.sm3c3.update(data);
    }

    private byte[] doFinal() {
        this.sm3c3.update(this.y);
        byte[] digest = this.sm3c3.doFinal();
        reset();
        return digest;
    }

    public static Map<String, byte[]> generateKeyPair() {
        return generateKeyPair(ECParameters.SM2_BEST);
    }

    /**
     * generate the SM2 key pair
     * a public key and a private key
     * @param ecParam the ec parameter
     * @return sm2 key store the map
     */
    public static Map<String, byte[]> generateKeyPair(ECParameters ecParam) {
        AsymmetricCipherKeyPair key = ecParam.keyPairGenerator.generateKeyPair();
        ECPrivateKeyParameters ecpriv = (ECPrivateKeyParameters) key.getPrivate();
        ECPublicKeyParameters ecpub = (ECPublicKeyParameters) key.getPublic();

        BigInteger priKey = ecpriv.getD(); // k
        ECPoint pubKey = ecpub.getQ(); // K = [k]POINT_G

        return ImmutableMap.of(PRIVATE_KEY, priKey.toByteArray(), 
                               PUBLIC_KEY, pubKey.getEncoded(false));
    }

    public static byte[] getPublicKey(Map<String, byte[]> keyMap) {
        return keyMap.get(PUBLIC_KEY);
    }

    public static byte[] getPrivateKey(Map<String, byte[]> keyMap) {
        return keyMap.get(PRIVATE_KEY);
    }

    public static ECPoint getPublicKey(byte[] publicKey) {
        return getPublicKey(ECParameters.SM2_BEST, publicKey);
    }

    public static ECPoint getPublicKey(ECParameters ecParam, byte[] publicKey) {
        return ecParam.curve.decodePoint(publicKey);
    }

    public static BigInteger getPrivateKey(byte[] privateKey) {
        return new BigInteger(1, privateKey);
    }

    public static byte[] encrypt(byte[] publicKey, byte[] data) {
        return encrypt(ECParameters.SM2_BEST, publicKey, data);
    }

    /**
     * encrypt data by public key
     * @param ecParam the ec parameter
     * @param publicKey SM2 public key, point K = [k]POINT_G
     * @param data the data to be encrypt
     * @return encrypted byte array
     */
    public static byte[] encrypt(ECParameters ecParam, byte[] publicKey, byte[] data) {
        if (ArrayUtils.isEmpty(publicKey) || ArrayUtils.isEmpty(data)) {
            return null;
        }

        // create C1 point
        AsymmetricCipherKeyPair key = ecParam.keyPairGenerator.generateKeyPair(); // point M
        ECPublicKeyParameters ecPub = (ECPublicKeyParameters) key.getPublic();
        ECPrivateKeyParameters ecPri = (ECPrivateKeyParameters) key.getPrivate();

        SM2 sm2 = new SM2(ecParam.curve.decodePoint(publicKey), ecPri.getD(), ecParam.n);

        byte[] c1 = ecPub.getQ().getEncoded(false); // generate random r, C1=M+rK
        byte[] c2 = Arrays.copyOf(data, data.length); // C2=rG
        sm2.encrypt(c2); // 加密数据

        byte[] c3 = sm2.doFinal(); // 摘要

        // return: C1(65) + C2(data.length) + C3(32)
        // C1 = {0x04, X byte array, Y byte array}
        return Bytes.concat(c1, c2, c3);
    }

    public static byte[] decrypt(byte[] privateKey, byte[] encrypted) {
        return decrypt(ECParameters.SM2_BEST, privateKey, encrypted);
    }

    /**
     * decrypt the encrypted byte array data by private key
     * @param ecParam the ec parameter
     * @param privateKey SM2 private key
     * @param encrypted the encrypted byte array data
     * @return the origin byte array data
     */
    public static byte[] decrypt(ECParameters ecParam, 
                                 byte[] privateKey, byte[] encrypted) {
        if (ArrayUtils.isEmpty(privateKey) || ArrayUtils.isEmpty(encrypted)) {
            return null;
        }

        // 分解加密数据
        // C1公钥 = 1位标志位+64位公钥（共65位）
        // C2数据 = encrypted.length-C1-C3
        // C3摘要 = 32
        int c1Len = 65, c3Len = 32, c2Len = encrypted.length - (c1Len + c3Len);
        byte[] c1 = Arrays.copyOf(encrypted, c1Len);
        byte[] c2 = Arrays.copyOfRange(encrypted, c1Len, c1Len + c2Len);
        byte[] c3 = Arrays.copyOfRange(encrypted, c1Len + c2Len, encrypted.length);

        SM2 sm2 = new SM2(getPublicKey(ecParam, c1), 
                          getPrivateKey(privateKey), ecParam.n);

        sm2.decrypt(c2); // 解密

        if (!Arrays.equals(c3, sm2.doFinal())) {
            throw new SecurityException("Invalid SM3 digest.");
        }

        //返回解密结果
        return c2;
    }

    // ----------------------------------------------------------------signature sign
    public static byte[] sign(byte[] data, byte[] publicKey, 
                              byte[] privateKey) {
        return sign(data, null, publicKey, privateKey);
    }

    public static byte[] sign(byte[] data, byte[] ida, 
                              byte[] publicKey, byte[] privateKey) {
        return sign(ECParameters.SM2_BEST, data, ida, publicKey, privateKey);
    }

    public static byte[] sign(ECParameters ecParam, byte[] data, 
                              byte[] publicKey, byte[] privateKey) {
        return sign(ecParam, data, null, publicKey, privateKey);
    }

    /**
     * sm2 sign
     * @param ecParam the ec parameter
     * @param data 签名信息
     * @param ida  签名方唯一标识，如：Alice@gmail.com
     * @param publicKey 公钥
     * @param privateKey 私钥
     * @return 签名信息
     */
    public static byte[] sign(ECParameters ecParam, byte[] data, byte[] ida, 
                              byte[] publicKey, byte[] privateKey) {
        ECPoint pubKey = getPublicKey(ecParam, publicKey);
        BigInteger priKey = getPrivateKey(privateKey);

        SM3Digest sm3 = SM3Digest.getInstance();
        sm3.update(calcZ(sm3, ecParam, ida, pubKey));
        sm3.update(data);
        BigInteger e = new BigInteger(1, sm3.doFinal()), k ,r;
        do {
            k = SecureRandoms.random(ecParam.n);
            ECPoint p = ecParam.pointG.multiply(k).normalize();
            r = e.add(p.getXCoord().toBigInteger()).mod(ecParam.n);
        } while (r.equals(BigInteger.ZERO) || r.add(k).equals(ecParam.n));

        BigInteger n1 = priKey.add(BigInteger.ONE).modInverse(ecParam.n);
        BigInteger n2 = k.subtract(r.multiply(priKey));
        BigInteger n3 = n1.multiply(n2.mod(ecParam.n));
        BigInteger s = n3.mod(ecParam.n);

        return new Signature(r, s, ecParam.n).toByteArray();
    }

    // ----------------------------------------------------------------signature verify
    public static boolean verify(byte[] data, byte[] signed, byte[] publicKey) {
        return verify(data, null, signed, publicKey);
    }

    public static boolean verify(byte[] data, byte[] ida, 
                                 byte[] signed, byte[] publicKey) {
        return verify(ECParameters.SM2_BEST, data, ida, signed, publicKey);
    }

    public static boolean verify(ECParameters ecParam, byte[] data, 
                                 byte[] signed, byte[] publicKey) {
        return verify(ecParam, data, null, signed, publicKey);
    }

    /**
     * verify signature
     * @param ecParam the ec parameter
     * @param data
     * @param ida
     * @param signed
     * @param publicKey
     * @return
     */
    public static boolean verify(ECParameters ecParam, byte[] data, byte[] ida, 
                                 byte[] signed, byte[] publicKey) {
        Signature signature = new Signature(signed, ecParam.n);
        if (   isNotBetween(signature.r, BigInteger.ONE, ecParam.n)
            || isNotBetween(signature.s, BigInteger.ONE, ecParam.n)) {
            return false;
        }

        ECPoint pubKey = getPublicKey(ecParam, publicKey);

        SM3Digest sm3 = SM3Digest.getInstance();
        sm3.update(calcZ(sm3, ecParam, ida, pubKey));
        sm3.update(data);
        BigInteger e = new BigInteger(1, sm3.doFinal());
        BigInteger t = signature.r.add(signature.s).mod(ecParam.n);

        if (t.equals(BigInteger.ZERO)) {
            return false;
        }

        ECPoint p1 = ecParam.pointG.multiply(signature.s).normalize();
        ECPoint p2 = pubKey.multiply(t).normalize();
        BigInteger x1 = p1.add(p2).normalize().getXCoord().toBigInteger();
        BigInteger r = e.add(x1).mod(ecParam.n);
        return r.equals(signature.r);
    }

    public static boolean checkPublicKey(byte[] publicKey) {
        return checkPublicKey(getPublicKey(publicKey));
    }

    public static boolean checkPublicKey(ECParameters ecParam, byte[] publicKey) {
        return checkPublicKey(ecParam, getPublicKey(ecParam, publicKey));
    }

    public static boolean checkPublicKey(ECPoint publicKey) {
        return checkPublicKey(ECParameters.SM2_BEST, publicKey);
    }

    public static boolean checkPublicKey(ECParameters ecParam, ECPoint publicKey) {
        if (publicKey.isInfinity()) {
            return false;
        }

        BigInteger x = publicKey.getXCoord().toBigInteger();
        BigInteger y = publicKey.getYCoord().toBigInteger();

        if (   isNotBetween(x, BigInteger.ZERO, ecParam.p)
            || isNotBetween(y, BigInteger.ZERO, ecParam.p)) {
            return false;
        }

        BigInteger x1 = x.pow(3).add(ecParam.a.multiply(x))
                                .add(ecParam.b).mod(ecParam.p);
        BigInteger y1 = y.pow(2).mod(ecParam.p);

        return y1.equals(x1) && publicKey.multiply(ecParam.n).isInfinity();
    }

    static byte[] calcZ(SM3Digest sm3, ECParameters ecParam, ECPoint pubKey) {
        return calcZ(sm3, ecParam, null, pubKey);
    }

    /**
     * 取得用户标识字节数组
     * @param ecParam the ec parameter
     * @param ida
     * @param pubKey
     * @return
     */
    static byte[] calcZ(SM3Digest sm3, ECParameters ecParam, byte[] ida, ECPoint pubKey) {
        sm3.reset();
        if (ida != null && ida.length > 0) {
            int idaBitLen = ida.length << 3; // ida.length*8
            sm3.update((byte) (idaBitLen & 0xFF00));
            sm3.update((byte) (idaBitLen & 0x00FF));
            sm3.update(ida);
        }
        sm3.update(ecParam.a.toByteArray());
        sm3.update(ecParam.b.toByteArray());
        sm3.update(ecParam.gx.toByteArray());
        sm3.update(ecParam.gy.toByteArray());
        sm3.update(pubKey.getXCoord().toBigInteger().toByteArray());
        sm3.update(pubKey.getYCoord().toBigInteger().toByteArray());
        return sm3.doFinal();
    }

    // -------------------------------------------------------------------private methods
    /**
     * check the number is not between min(inclusion) and max(exclusion)
     * @param number the value
     * @param min   the minimum number, inclusion
     * @param max   the maximum number, exclusion
     * @return {@code true} is not between
     */
    private static boolean isNotBetween(BigInteger number, BigInteger min, BigInteger max) {
        return number.compareTo(min) < 0 || number.compareTo(max) >= 0;
    }

    /**
     * SM2WithSM3 signature
     */
    private static class Signature implements java.io.Serializable {
        private static final long serialVersionUID = -2732762291362285185L;

        final BigInteger r;
        final BigInteger s;
        final BigInteger n;

        Signature(BigInteger r, BigInteger s, BigInteger n) {
            this.r = r;
            this.s = s;
            this.n = n;
        }

        Signature(byte[] signed, BigInteger n) {
            this.n = n;
            int byteCount = (int) Math.ceil(this.n.bitLength() / 8.0D);
            this.r = new BigInteger(1, Arrays.copyOfRange(signed, 0, byteCount));
            this.s = new BigInteger(1, Arrays.copyOfRange(signed, byteCount, byteCount << 1));
        }

        byte[] toByteArray() {
            int byteCount = (int) Math.ceil(this.n.bitLength() / 8.0D);
            byte[] out = new byte[byteCount << 1];
            byte[] r1 = this.r.toByteArray();
            byte[] s1 = this.s.toByteArray();
            Bytes.tailCopy(r1, 0, r1.length, out, 0, byteCount);
            Bytes.tailCopy(s1, 0, s1.length, out, byteCount, byteCount);
            return out;
        }

        @Override
        public String toString() {
            return Base64UrlSafe.encode(toByteArray());
        }
    }

}
