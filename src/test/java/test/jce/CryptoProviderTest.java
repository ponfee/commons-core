package test.jce;

import java.util.Map;

import cn.ponfee.commons.jce.CryptoProvider;
import cn.ponfee.commons.jce.ECParameters;
import cn.ponfee.commons.jce.Providers;
import cn.ponfee.commons.jce.sm.SM2;
import cn.ponfee.commons.jce.symmetric.Algorithm;
import cn.ponfee.commons.jce.symmetric.Mode;
import cn.ponfee.commons.jce.symmetric.Padding;
import cn.ponfee.commons.jce.symmetric.SymmetricCryptorBuilder;
import cn.ponfee.commons.util.MavenProjects;

public class CryptoProviderTest {

    public static void main(String[] args) {
        System.out.println("\n============================RSA crypt==========================");
        CryptoProvider rsa = CryptoProvider.rsaPrivateKeyProvider("MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEA9pU2mWa+yJwXF1VQb3WL5uk06Rc2jARYPlcV0JK0x4fMXboR9rpMlpJ9cr4B1wbJdBEa8H+kSgbJROFKsmkhFQIDAQABAkAcGiNP1krV+BwVl66EFWRtW5ShH/kiefhImoos7BtYReN5WZyYyxFCAf2yjMJigq2GFm8qdkQK+c+E7Q3lY6zdAiEA/wVfy+wGQcFh3gdFKhaQ12fBYMCtywxZ3Edss0EmxBMCIQD3h4vfENmbIMH+PX5dAPbRfrBFcx77/MxFORMESN0bNwIgL5kJMD51TICTi6U/u4NKtWmgJjbQOT2s5/hMyYg3fBECIEqRc+qUKenYuXg80Dd2VeSQlMunPZtN8b+czQTKaomLAiEA02qUv/p1dT/jc2BDtp9bl8jDiWFg5FNFcH6bBDlwgts=");
        String str = MavenProjects.getMainJavaFileAsString(CryptoProvider.class);
        String data = rsa.encrypt(str);
        System.out.println("加密后：" + data);
        System.out.println("解密后：" + rsa.decrypt(data));

        System.out.println("\n============================RSA sign==========================");
        String signed = rsa.sign(str);
        System.out.println("签名："+signed);
        System.out.println("验签："+rsa.verify(str, signed));

        System.out.println("\n============================AES crypt==========================");
        CryptoProvider aes = CryptoProvider.symmetricKeyProvider(SymmetricCryptorBuilder.newBuilder(Algorithm.AES, "z]_5Fi!X$ed4OY8j".getBytes(), Providers.BC)
                                                                         .mode(Mode.CBC).parameter("SVE<r[)qK`n%zQ'o".getBytes())
                                                                         .padding(Padding.PKCS7Padding)
                                                                         .build());
        data = aes.encrypt(str);
        System.out.println("加密后：" + data);
        System.out.println("解密后：" + aes.decrypt(data));

        System.out.println("\n============================SM2 crypt==========================");
        ECParameters ecParameter = ECParameters.secp256k1;
        Map<String, byte[]> sm2KeyMap = SM2.generateKeyPair(ecParameter);
        CryptoProvider sm2 = CryptoProvider.sm2PrivateKeyProvider(ecParameter, SM2.getPublicKey(sm2KeyMap), SM2.getPrivateKey(sm2KeyMap));
        data = sm2.encrypt(str);
        System.out.println("加密后：" + data);
        System.out.println("解密后：" + sm2.decrypt(data));

        System.out.println("\n============================SM2 sign==========================");
        signed = sm2.sign(str);
        System.out.println("签名："+signed);
        System.out.println("验签："+sm2.verify(str, signed));
    }
}
