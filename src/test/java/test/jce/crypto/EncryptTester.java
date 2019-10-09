package test.jce.crypto;

import static code.ponfee.commons.util.SecureRandoms.nextBytes;

import java.security.Provider;

import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.jce.symmetric.Algorithm;
import code.ponfee.commons.jce.symmetric.Mode;
import code.ponfee.commons.jce.symmetric.Padding;
import code.ponfee.commons.jce.symmetric.SymmetricCryptor;
import static code.ponfee.commons.jce.symmetric.SymmetricCryptorBuilder.newBuilder;

public class EncryptTester {

    public static void main(String[] args) {
        Provider bc = Providers.BC;

        test(newBuilder(Algorithm.DESede).build());
        test(newBuilder(Algorithm.RC2, nextBytes(5)).build());
        test(newBuilder(Algorithm.RC2, nextBytes(16), bc).mode(Mode.ECB).padding(Padding.NoPadding).build());
        test(newBuilder(Algorithm.AES, nextBytes(16), bc).padding(Padding.ISO10126_Padding).mode(Mode.ECB).build());
        test(newBuilder(Algorithm.AES, nextBytes(16)).build());
        test(newBuilder(Algorithm.AES, nextBytes(16), bc).mode(Mode.ECB).padding(Padding.PKCS5Padding).build());
        test(newBuilder(Algorithm.AES, nextBytes(16), bc).mode(Mode.OFB).padding(Padding.NoPadding).parameter(nextBytes(16)).build());
        test(newBuilder(Algorithm.AES, nextBytes(16), bc).mode(Mode.CBC).padding(Padding.PKCS7Padding).parameter(nextBytes(16)).build());
        test(newBuilder(Algorithm.DES, nextBytes(8), bc).mode(Mode.CBC).padding(Padding.NoPadding).parameter(nextBytes(8)).build());
        test(newBuilder(Algorithm.DES, nextBytes(8), bc).build());
        test(newBuilder(Algorithm.DES, nextBytes(8), bc).mode(Mode.CBC).padding(Padding.PKCS5Padding).parameter(nextBytes(8)).build());
        test(newBuilder(Algorithm.DESede, nextBytes(16), bc).build());
        test(newBuilder(Algorithm.SM4, nextBytes(16), bc).mode(Mode.CBC).padding(Padding.X9_23Padding).parameter(nextBytes(16)).build());
        test(newBuilder(Algorithm.DESede, nextBytes(16), bc).mode(Mode.ECB).padding(Padding.PKCS5Padding).build());
        test(newBuilder(Algorithm.DESede, nextBytes(16), bc).mode(Mode.CBC).padding(Padding.PKCS5Padding).parameter(nextBytes(8)).build());
        test(newBuilder(Algorithm.SEED, 16, bc).mode(Mode.CBC).padding(Padding.PKCS5Padding).parameter(nextBytes(16)).build());

        test(newBuilder(Algorithm.GOST, 32, bc).mode(Mode.CBC).padding(Padding.PKCS5Padding).parameter(nextBytes(8)).build());
        test(newBuilder(Algorithm.GOST, 32, bc).mode(Mode.CBC).padding(Padding.PKCS7Padding).parameter(nextBytes(8)).build());
        test(newBuilder(Algorithm.GOST, 32, bc).mode(Mode.CBC).padding(Padding.ISO10126_Padding).parameter(nextBytes(8)).build());
        test(newBuilder(Algorithm.GOST, 32, bc).mode(Mode.CBC).padding(Padding.ISO10126_2Padding).parameter(nextBytes(8)).build());
        test(newBuilder(Algorithm.GOST, 32, bc).mode(Mode.CBC).padding(Padding.ISO7816_4Padding).parameter(nextBytes(8)).build());
        test(newBuilder(Algorithm.GOST, 32, bc).mode(Mode.CBC).padding(Padding.X9_23Padding).parameter(nextBytes(8)).build());
        test(newBuilder(Algorithm.GOST, 32, bc).mode(Mode.CBC).padding(Padding.TBCPadding).parameter(nextBytes(8)).build());

        test(newBuilder(Algorithm.GOST, 32, bc).mode(Mode.CBC).padding(Padding.CS3Padding).parameter(nextBytes(8)).build());
        test(newBuilder(Algorithm.SM4, 16, bc).mode(Mode.CBC).padding(Padding.ISO10126_Padding).parameter(nextBytes(16)).build());
        test(newBuilder(Algorithm.SM4, 16, bc).mode(Mode.CBC).padding(Padding.ISO10126_2Padding).parameter(nextBytes(16)).build());

        //test(newBuilder(Algorithm.GOST, 32, bc).mode(Mode.CBC).padding(Padding.CS1Padding).parameter(nextBytes(8)).build()); // Padding CS1Padding unknown.
        //test(newBuilder(Algorithm.GOST, 32, bc).mode(Mode.CBC).padding(Padding.CS2Padding).parameter(nextBytes(8)).build()); // Padding CS2Padding unknown.
    }

    public static void test(SymmetricCryptor cryptor) {
        byte[] encrypted = cryptor.encrypt("12345678".getBytes()); // 加密
        byte[] origin = cryptor.decrypt(encrypted); // 解密
        System.out.println(new String(origin));
    }

}
