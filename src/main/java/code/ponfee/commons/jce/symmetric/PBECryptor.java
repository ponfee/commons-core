package code.ponfee.commons.jce.symmetric;

import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.SecretKey;
import javax.crypto.spec.PBEParameterSpec;

/**
 * <pre>
 *  |---------------------------------------|-------------------|---------------------------|
 *  |               Algorithm               | secret key length | default secret key length |
 *  |---------------------------------------|-------------------|---------------------------|
 *  | PBEWithMD5AndDES                      |        56         |            56             |
 *  |---------------------------------------|-------------------|---------------------------|
 *  | PBEWithMD5AndTripleDES                |      112,168      |            168            |
 *  |---------------------------------------|-------------------|---------------------------|
 *  | PBEWithSHA1AndDESede                  |      112,168      |            168            |
 *  |---------------------------------------|-------------------|---------------------------|
 *  | PBEWithSHA1AndRC2_40                  |     40 to 1024    |            128            |
 *  |---------------------------------------|-------------------|---------------------------|
 * </pre>
 * 
 * String是常量（即创建之后就无法更改），会保存到常量池中，如果有其他进程
 * 可以dump这个进程的内存，那么密码就会随着常量池被dump出去从而泄露。
 * 而char[]可以写入其他的信息从而改变，即是被dump了也会减少泄露密码的风险。
 * <p>
 * 
 * PBE Cryptors
 * 
 * @author Ponfee
 */
public class PBECryptor extends SymmetricCryptor {

    public enum PBEAlgorithm {
        PBEWithMD5AndDES, //
        PBEWithSHA1AndDESede, // best
        PBEWithSHA1AndRC2_40, //
        PBEWithMD5AndTripleDES, //
        ;
    }

    public PBECryptor(SecretKey secretKey, Mode mode, Padding padding, 
                      AlgorithmParameterSpec parameter, Provider provider) {
        // (provider == null) ? Providers.SunJCE : provider
        super(secretKey, mode, padding, parameter, provider);
    }

    // --------------------------getter
    public char[] getPass() {
        return new String(getKey()).toCharArray();
    }

    public byte[] getSalt() {
        return ((PBEParameterSpec) parameter).getSalt();
    }

    public int getIterations() {
        return ((PBEParameterSpec) parameter).getIterationCount();
    }

    @Override
    public byte[] getParameterAsBytes() {
        throw new UnsupportedOperationException();
    }
}
