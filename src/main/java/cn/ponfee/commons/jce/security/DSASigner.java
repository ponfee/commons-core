/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.security;

import cn.ponfee.commons.jce.Providers;
import cn.ponfee.commons.util.ObjectUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.security.*;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * 基于整数有限域离散对数难题
 *
 * DSA签名/验签（只用于数字签名）
 * 
 * @author Ponfee
 */
public final class DSASigner {

    private static final String ALGORITHM = "DSA";

    /**
     * 默认生成密钥
     * @return 密钥对象
     */
    public static Pair<DSAPublicKey, DSAPrivateKey> initKey() {
        return initKey(ObjectUtils.uuid32(), 1024);
    }

    /**
     * 生成密钥
     * @param seed 种子
     * @param keySize   must be a multiple of 64, r
     *                  anging from 512 to 1024 (inclusive).
     * @return 密钥对象
     */
    public static Pair<DSAPublicKey, DSAPrivateKey> initKey(String seed, int keySize) {
        KeyPairGenerator keygen = Providers.getKeyPairGenerator(ALGORITHM);
        // 初始化随机产生器
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.setSeed(seed.getBytes());
        keygen.initialize(keySize, secureRandom);
        KeyPair pair = keygen.genKeyPair();
        return ImmutablePair.of(
            (DSAPublicKey) pair.getPublic(), (DSAPrivateKey) pair.getPrivate()
        );
    }

    public static DSAPrivateKey decodePrivateKey(byte[] privateKey) {
        KeyFactory keyFactory = Providers.getKeyFactory(ALGORITHM);
        try {
            return (DSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        } catch (InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    public static DSAPublicKey decodePublicKey(byte[] publicKey) {
        KeyFactory keyFactory = Providers.getKeyFactory(ALGORITHM);
        try {
            return (DSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
        } catch (InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 用私钥对信息生成数字签名
     * @param data 原文数据
     * @param privateKey 私钥
     * @return  签名结果
     */
    public static byte[] sign(byte[] data, byte[] privateKey) {
        return sign(data, decodePrivateKey(privateKey));
    }

    public static byte[] sign(byte[] data, DSAPrivateKey privateKey) {
        Signature signature = Providers.getSignature(privateKey.getAlgorithm());
        try {
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (InvalidKeyException | SignatureException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 校验数字签名
     * @param origin 原文数据
     * @param publicKey 公钥
     * @param signed 签名数据
     * @return 校验成功返回true 失败返回false
     * 
     */
    public static boolean verify(byte[] origin, byte[] publicKey, byte[] signed) {
        return verify(origin, decodePublicKey(publicKey), signed);
    }

    public static boolean verify(byte[] origin, DSAPublicKey publicKey, byte[] signed) {
        Signature signature = Providers.getSignature(publicKey.getAlgorithm());
        try {
            signature.initVerify(publicKey);
            signature.update(origin);
            return signature.verify(signed);
        } catch (InvalidKeyException | SignatureException e) {
            throw new SecurityException(e);
        }
    }

}
