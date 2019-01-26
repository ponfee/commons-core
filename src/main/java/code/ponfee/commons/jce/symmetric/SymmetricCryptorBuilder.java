package code.ponfee.commons.jce.symmetric;

import java.security.GeneralSecurityException;
import java.security.Provider;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import code.ponfee.commons.util.SecureRandoms;

/**
 * 对称加密构建类
 * 
 * @author fupf
 */
public final class SymmetricCryptorBuilder {

    private final SecretKey secretKey; // 密钥
    private Mode mode; // 分组加密模式
    private Padding padding; // 填充
    private IvParameterSpec parameter; // 填充向量

    /**加密服务提供方 {@link code.ponfee.commons.jce.Providers} */
    private Provider provider;

    private SymmetricCryptorBuilder(Algorithm alg, byte[] key) {
        if (key == null) {
            try {
                this.secretKey = KeyGenerator.getInstance(alg.name())
                                             .generateKey();
            } catch (GeneralSecurityException e) {
                throw new SecurityException(e);
            }
        } else {
            this.secretKey = new SecretKeySpec(key, alg.name());
        }
    }

    public static SymmetricCryptorBuilder newBuilder(Algorithm algorithm) {
        return new SymmetricCryptorBuilder(algorithm, null);
    }

    public static SymmetricCryptorBuilder newBuilder(Algorithm algorithm, int keySize) {
        return new SymmetricCryptorBuilder(algorithm, SecureRandoms.nextBytes(keySize));
    }

    public static SymmetricCryptorBuilder newBuilder(Algorithm algorithm, byte[] key) {
        return new SymmetricCryptorBuilder(algorithm, key);
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

    public SymmetricCryptorBuilder provider(Provider provider) {
        this.provider = provider;
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
