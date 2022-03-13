package code.ponfee.commons.jce.symmetric;

import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.util.Base64UrlSafe;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;

/**
 * AES
 * http://blog.csdn.net/qq_28205153/article/details/55798628
 * http://blog.csdn.net/lrwwll/article/details/78069013
 * https://coolshell.cn//wp-content/uploads/2010/10/rijndael_ingles2004.swf
 * 对称加密
 *  加密：C = E(K, P)
 *  解密：P = D(K, C)
 *  
 * AES         密钥长度（32位比特字)    分组长度(32位比特字)    加密轮数
 * AES-128          4                         4                  10
 * AES-192          6                         4                  12
 * AES-256          8                         4                  14
 * 
 * 1、明文按16字节（4个32位比特字）分组，P[0],P[1],...,P[15]，不足则填充
 * 2、16字节（4个32位比特字）密钥进行分组，分成44组每组1个比特字（W[0],W[1],...,W[43]），
 *   前4组为原始密钥用于初始密钥加：W[0]=K[0] K[1] K[2] K[3],...,W[3]=K[12] K[13] K[14] K[15]。
 *   后面40个字分为10组，每组4个字（128比特）分别用于10轮加密运算中的轮密钥加
 * 3、W[4]~W[43]通过轮密钥加来生成，将128位轮密钥Ki同状态矩阵中的数据进行逐位异或操作，
 *   密钥Ki中每个字W[4i],W[4i+1],W[4i+2],W[4i+3]为32位比特字，包含4个字节
 * 4、先将明文和原始密钥进行一次异或加密操作
 * 5、加密的第1轮到第9轮的轮函数一样，包括4个操作：字节代换、行位移、列混合和轮密钥加。最后一轮迭代不执行列混合。
 * 6、字节代换：把该字节的高4位作为行值，低4位作为列值，取出S盒或者逆S盒中对应的行的元素作为输出
 * 
 * @author Ponfee
 */
public class SymmetricCryptor {

    /** 
     * 分组对称加密模式时padding不能为null 
     */
    private final Mode mode;

    /** 
     * 1、RC2、RC4分组对称加密模式时padding必须为NoPadding
     * 2、无分组模式时padding必须为null
     * 3、其它算法无限制 
     */
    private final Padding padding;

    /** 
     * 1、ECB模式时iv必须为null
     * 2、无分组对称加密模式时iv必须为null
     * 3、有分组对称加密模式时必须要有iv
     * 4、iv must be 16 bytes long
     */
    protected final AlgorithmParameterSpec parameter;

    /** 加密提供方 */
    private final Provider provider;

    /** 密钥 */
    private final SecretKey secretKey;

    /** the cipher transformation */
    private final String transformation;

    protected SymmetricCryptor(SecretKey secretKey, Mode mode, Padding padding,
                               AlgorithmParameterSpec parameter, Provider provider) {
        this.secretKey = secretKey;
        this.mode = mode;
        this.padding = padding;
        this.parameter = parameter;
        this.provider = provider;
        if (mode != null) {
            this.transformation = new StringBuilder(getAlgorithm())
                                      .append("/").append(mode.name())
                                      .append("/").append(padding.padding())
                                      .toString();
        } else {
            this.transformation = getAlgorithm();
        }
    }

    public final byte[] encrypt(byte[] data) {
        return this.docrypt(data, Cipher.ENCRYPT_MODE);
    }

    public final byte[] decrypt(byte[] encrypted) {
        return this.docrypt(encrypted, Cipher.DECRYPT_MODE);
    }

    /**
     * 加密解
     * @param bytes     待加密/密文数据
     * @param cryptMode 密码模式：1加密；2解密；
     * @return
     */
    private byte[] docrypt(byte[] bytes, int cryptMode) {
        try {
            Cipher cipher = Providers.getCipher(transformation, provider);
            cipher.init(cryptMode, secretKey, parameter);
            return cipher.doFinal(bytes);
        } catch (GeneralSecurityException e) {
            throw new SecurityException(e);
        }
    }

    // ----------------------------------getter
    /**
     * Returns encrypt algorithm string
     * 
     * @return algorithm string
     */
    public final String getAlgorithm() {
        return secretKey.getAlgorithm();
    }

    /**
     * Returns key byte array data
     * 
     * @return key byte array data
     */
    public final byte[] getKey() {
        return secretKey.getEncoded();
    }

    public final String getKeyAsBase64() {
        return Base64UrlSafe.encode(getKey());
    }

    /**
     * Returns iv parameter byte array data
     * 
     * @return iv parameter byte array data
     */
    public byte[] getParameterAsBytes() {
        return ((IvParameterSpec) parameter).getIV();
    }

    public final String getParameterAsBase64() {
        return Base64UrlSafe.encode(getParameterAsBytes());
    }

    public final Mode getMode() {
        return mode;
    }

    public final Padding getPadding() {
        return padding;
    }

    public final Provider getProvider() {
        return provider;
    }

}
