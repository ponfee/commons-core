package test.jce;

import static cn.ponfee.commons.util.SecureRandoms.nextBytes;

import java.security.Provider;

import org.apache.commons.codec.binary.Hex;

import cn.ponfee.commons.jce.DigestAlgorithms;
import cn.ponfee.commons.jce.HmacAlgorithms;
import cn.ponfee.commons.jce.Providers;
import cn.ponfee.commons.jce.digest.DigestUtils;
import cn.ponfee.commons.jce.digest.HmacUtils;
import cn.ponfee.commons.jce.sm.SM3Digest;
import cn.ponfee.commons.jce.sm.SM4;
import cn.ponfee.commons.jce.symmetric.Algorithm;
import cn.ponfee.commons.jce.symmetric.Mode;
import cn.ponfee.commons.jce.symmetric.Padding;
import cn.ponfee.commons.jce.symmetric.SymmetricCryptor;
import cn.ponfee.commons.jce.symmetric.SymmetricCryptorBuilder;
import cn.ponfee.commons.util.SecureRandoms;

public class DigestTest {

    public static void main(String[] args) {
        byte[] data = SecureRandoms.nextBytes(1204);
        byte[] key = SecureRandoms.nextBytes(1204);
        //System.out.println(Hex.encodeHexString(DigestUtils.digest(DigestAlgorithms.SHAKE128, Providers.BC, data)));
        //System.out.println(Hex.encodeHexString(HmacUtils.crypt(key, data, HmacAlgorithms.HmacSHAKE256)));

        System.out.println(Hex.encodeHexString(DigestUtils.digest(DigestAlgorithms.SM3, Providers.BC, data)));
        System.out.println(Hex.encodeHexString(SM3Digest.getInstance().doFinal(data)));
        //System.out.println(Hex.encodeHexString(HmacUtils.crypt(key, data, HmacAlgorithms.HmacSM3)));

        System.out.println(Hex.encodeHexString(DigestUtils.digest(DigestAlgorithms.SKEIN_512_256, Providers.BC, data)));
        System.out.println(Hex.encodeHexString(HmacUtils.crypt(key, data, HmacAlgorithms.HmacSKEIN_512_256)));

        System.out.println(Hex.encodeHexString(DigestUtils.digest(DigestAlgorithms.SHA512, Providers.BC, data)));
        System.out.println(Hex.encodeHexString(DigestUtils.digest(DigestAlgorithms.SKEIN_512_512, Providers.BC, data)));
        System.out.println(Hex.encodeHexString(DigestUtils.digest(DigestAlgorithms.SKEIN_1024_512, Providers.BC, data)));
        System.out.println(Hex.encodeHexString(DigestUtils.digest(DigestAlgorithms.SKEIN_1024_1024, Providers.BC, data)));
        System.out.println(Hex.encodeHexString(HmacUtils.crypt(key, data, HmacAlgorithms.HmacSKEIN_1024_1024)));
        
        System.out.println("========================");
        Provider bc = Providers.BC;
        key = nextBytes(16);
        byte[] iv = nextBytes(16);
        SymmetricCryptor coder = null;
        coder = SymmetricCryptorBuilder.newBuilder(Algorithm.SM4, key, bc).mode(Mode.CBC)
                      .padding(Padding.X9_23Padding).parameter(iv).build();
        
        data = "1234".getBytes();
        byte[] encrypted = coder.encrypt(data);
        System.out.println(new String(coder.decrypt(encrypted)));
        System.out.println(new String(SM4.decrypt(true, key, iv, encrypted)));
    }
}
