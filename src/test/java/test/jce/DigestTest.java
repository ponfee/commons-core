package test.jce;

import static code.ponfee.commons.util.SecureRandoms.nextBytes;

import java.security.Provider;

import org.apache.commons.codec.binary.Hex;

import code.ponfee.commons.jce.DigestAlgorithms;
import code.ponfee.commons.jce.HmacAlgorithms;
import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.jce.digest.DigestUtils;
import code.ponfee.commons.jce.digest.HmacUtils;
import code.ponfee.commons.jce.sm.SM3Digest;
import code.ponfee.commons.jce.sm.SM4;
import code.ponfee.commons.jce.symmetric.Algorithm;
import code.ponfee.commons.jce.symmetric.Mode;
import code.ponfee.commons.jce.symmetric.Padding;
import code.ponfee.commons.jce.symmetric.SymmetricCryptor;
import code.ponfee.commons.jce.symmetric.SymmetricCryptorBuilder;
import code.ponfee.commons.util.SecureRandoms;

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
        coder = SymmetricCryptorBuilder.newBuilder(Algorithm.SM4, key).mode(Mode.CBC)
                      .padding(Padding.X9_23Padding).parameter(iv).provider(bc).build();
        
        data = "1234".getBytes();
        byte[] encrypted = coder.encrypt(data);
        System.out.println(new String(coder.decrypt(encrypted)));
        System.out.println(new String(SM4.decrypt(true, key, iv, encrypted)));
    }
}
