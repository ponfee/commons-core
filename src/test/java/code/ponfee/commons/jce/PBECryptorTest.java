package code.ponfee.commons.jce;

import code.ponfee.commons.jce.symmetric.Mode;
import code.ponfee.commons.jce.symmetric.PBECryptor;
import code.ponfee.commons.jce.symmetric.PBECryptor.PBEAlgorithm;
import code.ponfee.commons.jce.symmetric.PBECryptorBuilder;
import code.ponfee.commons.jce.symmetric.Padding;

public class PBECryptorTest {

    public static void main(String[] args) {
        //PBEAlgorithm alg = PBEAlgorithm.PBEWithMD5AndTripleDES;
        //PBEAlgorithm alg = PBEAlgorithm.PBEWithMD5AndDES;
        //PBEAlgorithm alg = PBEAlgorithm.PBEWithSHA1AndRC2_40;
        PBEAlgorithm alg = PBEAlgorithm.PBEWithSHA1AndDESede;
        char[] pass = "87654321".toCharArray();
        byte[] salt = "12345678".getBytes();
        int iterations = 100;

        // 加密
        PBECryptor p = PBECryptorBuilder.newBuilder(alg, pass)
                                        .mode(Mode.CBC).padding(Padding.PKCS5Padding)
                                        .parameter(salt, iterations)
                                        .build();
        byte[] encrypted = p.encrypt("abc".getBytes());

        // 解密
        p = PBECryptorBuilder.newBuilder(alg, p.getPass())
            .mode(p.getMode()).padding(p.getPadding())
            .parameter(p.getSalt(), p.getIterations())
            .build();
        
        byte[] decrypted = p.decrypt(encrypted);
        System.out.println(new String(decrypted));
    }
}
