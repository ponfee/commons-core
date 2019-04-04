package test.jce.crypto;

import static code.ponfee.commons.util.SecureRandoms.nextBytes;

import java.security.Provider;

import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.jce.symmetric.Algorithm;
import code.ponfee.commons.jce.symmetric.Mode;
import code.ponfee.commons.jce.symmetric.Padding;
import code.ponfee.commons.jce.symmetric.SymmetricCryptor;
import code.ponfee.commons.jce.symmetric.SymmetricCryptorBuilder;

public class EncryptTester {

    public static void main(String[] args) {
        Provider bc = Providers.BC;
        SymmetricCryptor coder = null;

        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.DESede).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.RC2).key(nextBytes(5)).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.RC2).key(nextBytes(16)).mode(Mode.ECB).padding(Padding.NoPadding).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.AES).padding(Padding.PADDING_ISO10126).mode(Mode.ECB).key(nextBytes(16)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.AES).key(nextBytes(16)).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.AES).key(nextBytes(16)).mode(Mode.ECB).padding(Padding.PKCS5Padding).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.AES).key(nextBytes(16)).mode(Mode.OFB).padding(Padding.NoPadding).parameter(nextBytes(16)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.AES).key(nextBytes(16)).mode(Mode.CBC).padding(Padding.PKCS7Padding).parameter(nextBytes(16)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.DES).key(nextBytes(8)).mode(Mode.CBC).padding(Padding.NoPadding).parameter(nextBytes(8)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.DES).key(nextBytes(8)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.DES).key(nextBytes(8)).mode(Mode.CBC).padding(Padding.PKCS5Padding).parameter(nextBytes(8)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.DESede).key(nextBytes(16)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.SM4).key(nextBytes(16)).mode(Mode.CBC).padding(Padding.X9_23Padding).parameter(nextBytes(16)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.DESede).key(nextBytes(16)).mode(Mode.ECB).padding(Padding.PKCS5Padding).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.DESede).key(nextBytes(16)).mode(Mode.CBC).padding(Padding.PKCS5Padding).parameter(nextBytes(8)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.SEED, 16).mode(Mode.CBC).padding(Padding.PKCS5Padding).parameter(nextBytes(16)).provider(bc).build();

        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.GOST, 32).mode(Mode.CBC).padding(Padding.PKCS5Padding).parameter(nextBytes(8)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.GOST, 32).mode(Mode.CBC).padding(Padding.PKCS7Padding).parameter(nextBytes(8)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.GOST, 32).mode(Mode.CBC).padding(Padding.ISO10126_Padding).parameter(nextBytes(8)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.GOST, 32).mode(Mode.CBC).padding(Padding.ISO10126_2Padding).parameter(nextBytes(8)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.GOST, 32).mode(Mode.CBC).padding(Padding.ISO7816_4Padding).parameter(nextBytes(8)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.GOST, 32).mode(Mode.CBC).padding(Padding.X9_23Padding).parameter(nextBytes(8)).provider(bc).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.GOST, 32).mode(Mode.CBC).padding(Padding.TBCPadding).parameter(nextBytes(8)).provider(bc).build();
        
        coder = SymmetricCryptorBuilder.newBuilder(Algorithm.GOST, 32, bc).mode(Mode.CBC).padding(Padding.CS3Padding).parameter(nextBytes(8)).build();
        //coder = SymmetricCryptorBuilder.newBuilder(Algorithm.GOST, 32, bc).mode(Mode.CBC).padding(Padding.CS2Padding).parameter(nextBytes(8)).build();

        byte[] encrypted = coder.encrypt("12345678".getBytes()); // 加密
        byte[] origin = coder.decrypt(encrypted); // 解密
        System.out.println(new String(origin));
    }
}
