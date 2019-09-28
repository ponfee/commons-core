package code.ponfee.commons.jce.symmetric;

import java.security.Provider;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.util.SecureRandoms;

/**
 * 对称加密构建类
 * 
 * @author Ponfee
 */
public final class SymmetricCryptorBuilder {

    private final SecretKey secretKey; // 密钥

    /**加密服务提供方 {@link code.ponfee.commons.jce.Providers} */
    private final Provider provider;

    private Mode mode; // 分组加密模式
    private Padding padding; // 填充
    private IvParameterSpec parameter; // 填充向量


    private SymmetricCryptorBuilder(Algorithm alg, byte[] key, Provider provider) {
        if (key == null) {
            KeyGenerator keyGenerator = Providers.getKeyGenerator(alg.name(), provider);
            this.secretKey = keyGenerator.generateKey();
        } else {
            this.secretKey = new SecretKeySpec(key, alg.name());
        }
        this.provider = provider;
    }

    public static SymmetricCryptorBuilder newBuilder(Algorithm algorithm) {
        return newBuilder(algorithm, null, null);
    }

    public static SymmetricCryptorBuilder newBuilder(Algorithm algorithm, Provider provider) {
        return newBuilder(algorithm, null, provider);
    }

    public static SymmetricCryptorBuilder newBuilder(Algorithm algorithm, int keySize) {
        return newBuilder(algorithm, SecureRandoms.nextBytes(keySize), null);
    }

    public static SymmetricCryptorBuilder newBuilder(Algorithm algorithm, int keySize, Provider provider) {
        return newBuilder(algorithm, SecureRandoms.nextBytes(keySize), provider);
    }

    public static SymmetricCryptorBuilder newBuilder(Algorithm algorithm, byte[] key) {
        return newBuilder(algorithm, key, null);
    }

    public static SymmetricCryptorBuilder newBuilder(Algorithm algorithm, byte[] key, Provider provider) {
        return new SymmetricCryptorBuilder(algorithm, key, null);
    }

    public SymmetricCryptorBuilder mode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public SymmetricCryptorBuilder padding(Padding padding) {
        this.padding = padding;
        return this;
    }

    public SymmetricCryptorBuilder parameter(byte[] parameter) {
        this.parameter = new IvParameterSpec(parameter);
        return this;
    }

    public SymmetricCryptor build() {
        if (mode != null && padding == null) {
            // 设置了mode必须指定padding
            throw new IllegalArgumentException("padding cannot be null within mode crypto.");
        } else if (mode == null && padding != null) {
            // 没有设置mode，不能指定padding
            throw new IllegalArgumentException("padding must be null without mode crypto.");
        }

        return new SymmetricCryptor(secretKey, mode, padding, parameter, provider);
    }

}
