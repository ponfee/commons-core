package code.ponfee.commons.jce.symmetric;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.text.RandomStringGenerator;

import code.ponfee.commons.jce.symmetric.PBECryptor.PBEAlgorithm;

/**
 * PBE Cryptor builder
 * 
 * @author Ponfee
 */
public class PBECryptorBuilder {

    private static final RandomStringGenerator GENERATOR =
        new RandomStringGenerator.Builder().withinRange('!', '~').build();

    private final SecretKey secretKey; // 密钥
    private Mode mode; // 分组加密模式
    private Padding padding; // 填充
    private AlgorithmParameterSpec parameter; // 填充向量
    private Provider provider;

    private PBECryptorBuilder(PBEAlgorithm algorithm, char[] pass) {
        try {
            // new SecretKeySpec(new String(pass).getBytes(), algName); // 也可用此方法来构造具体的密钥
            this.secretKey = SecretKeyFactory.getInstance(algorithm.name())
                                      .generateSecret(new PBEKeySpec(pass));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
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
        return new PBECryptorBuilder(algorithm, pass);
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

    public PBECryptorBuilder provider(Provider provider) {
        this.provider = provider;
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
