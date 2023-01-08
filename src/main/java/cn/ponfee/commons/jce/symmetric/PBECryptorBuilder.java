/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.symmetric;

import cn.ponfee.commons.jce.Providers;
import cn.ponfee.commons.jce.symmetric.PBECryptor.PBEAlgorithm;
import org.apache.commons.text.RandomStringGenerator;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;

/**
 * PBE Cryptor builder
 * 
 * @author Ponfee
 */
public class PBECryptorBuilder {

    private static final RandomStringGenerator GENERATOR =
        new RandomStringGenerator.Builder().withinRange('!', '~').build();

    private final SecretKey secretKey; // 密钥
    private final Provider provider;
    private Mode mode; // 分组加密模式
    private Padding padding; // 填充
    private AlgorithmParameterSpec parameter; // 填充向量

    private PBECryptorBuilder(PBEAlgorithm algorithm, char[] pass, Provider provider) {
        try {
            // new SecretKeySpec(new String(pass).getBytes(), algName); // 也可用此方法来构造具体的密钥
            SecretKeyFactory factory = Providers.getSecretKeyFactory(algorithm.name(), provider);
            this.secretKey = factory.generateSecret(new PBEKeySpec(pass));
            this.provider = provider;
        } catch (InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    public static PBECryptorBuilder newBuilder(PBEAlgorithm algorithm) {
        return newBuilder(algorithm, 24);
    }

    public static PBECryptorBuilder newBuilder(PBEAlgorithm algorithm, int passSize) {
        return newBuilder(algorithm, GENERATOR.generate(passSize).toCharArray());
    }

    public static PBECryptorBuilder newBuilder(PBEAlgorithm algorithm, char[] pass) {
        return newBuilder(algorithm, pass, null);
    }

    public static PBECryptorBuilder newBuilder(PBEAlgorithm algorithm, char[] pass, Provider provider) {
        return new PBECryptorBuilder(algorithm, pass, provider);
    }

    public PBECryptorBuilder mode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public PBECryptorBuilder padding(Padding padding) {
        this.padding = padding;
        return this;
    }

    public PBECryptorBuilder parameter(byte[] salt,
                                       int iterations) {
        this.parameter = new PBEParameterSpec(salt, iterations);
        return this;
    }

    public PBECryptor build() {
        if (mode != null && padding == null) {
            // 设置了mode必须指定padding
            throw new IllegalArgumentException("padding cannot be null within mode crypto.");
        } else if (mode == null && padding != null) {
            // 没有设置mode，不能指定padding
            throw new IllegalArgumentException("padding must be null without mode crypto.");
        }
        return new PBECryptor(secretKey, mode, padding, parameter, provider);
    }
}
