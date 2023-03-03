package test.jce.sm;

import java.util.Base64;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import cn.ponfee.commons.jce.ECParameters;
import cn.ponfee.commons.jce.sm.SM2;
import cn.ponfee.commons.util.MavenProjects;

public class SM2Test {
    static ECParameters ecParameter = ECParameters.SM2_BEST;

    @Test
    public void test1() {
        byte[] data = MavenProjects.getMainJavaFileAsString(SM2.class).substring(0, 100).getBytes();
        Map<String, byte[]> keyMap = SM2.generateKeyPair(ecParameter);

        System.out.println(Base64.getUrlEncoder().encodeToString(SM2.getPublicKey(keyMap)));
        System.out.println(Base64.getUrlEncoder().encodeToString(SM2.getPrivateKey(keyMap)));


        byte[] encrypted = SM2.encrypt(ecParameter, SM2.getPublicKey(keyMap), data);
        System.out.println(Base64.getUrlEncoder().encodeToString(encrypted));

        byte[] decrypted = SM2.decrypt(ecParameter, SM2.getPrivateKey(keyMap), encrypted);
        System.out.println(new String(decrypted));
    }

    @Test
    public void test2() throws Exception {
        byte[] privateKey = Hex.decodeHex("19F4279987CACA6780592C2B330E932B7C6C27919ED56D63785E398A7C3B568F");
        byte[] encrypted = Hex.decodeHex("04AD22DF19CC60E231B74E4F510537C9D71EB57D3734F7EC44188E5655374BC742469A3BF8D247769AF712A06A4D2EAC24470F9851AB6A2F106600E3B5D8DADD2DCD81D6C781A26C7DCBACD9F947A461FF555537A75E9A19B92EE6E447373C9B776CF0236BCA769A7515795A32AF00F94E9B5EBCDE9B8774F19129815AF04C7D03122982448AFB586F8895C2A695FFC034B1B77E350E925E0BC04A683759C763AC396FC3EFDD9EF4A09E53EFFCCA80C29B42B11AB82A7CEACBBF2E748E028035E4D4E71693");
        byte[] decrypted = SM2.decrypt(ecParameter, privateKey, encrypted);
        System.out.println(new String(decrypted));
    }

    @Test
    public void test3() {
        //ECParameters ecParameter = ECParameters.secp256r1;
        //ECParameters ecParameter = ECParameters.EC_PARAMETERS.get("secp256r1");
        //ECParameters ecParameter = ECParameters.EC_PARAMETERS.get("secp256k1");
        ECParameters ecParameter = ECParameters.SM2_BEST;

        //byte[] publicKey1 = Base64.getDecoder().decode("BDky9CNygRTBGh7eGdzdbxP5eGRozk4wQfFmREncwEKnYHhNy1OoBvh0wY/RMH/3ikfYejClHxlI1T+jRa0m2wU=");
        byte[] privateKey1 = Base64.getDecoder().decode("AJaz6VNm1Wl9ba1YCEkYi36+m+8DDL4LDuLM172tg5Ao");
        byte[] encrypted1 = Base64.getDecoder().decode("BDrcVKWOJ2J/x0YRmX99ksvwZJ5DgbgxPpwDNf3u94FH65JHuxG6U6Xp7ST7G7dsTlg6QFiSIANKyb42DvmpLpby1CU5kw0q/C4t1eH08VTAtpZrg2M6+Qz4pdOpp0iGpUp1w5ympuezCxgsEUfhDYihG5MjerLz+8Ss8qTRz3/ZpNvRJaCDk9k=");
        System.out.println(new String(SM2.decrypt(privateKey1, encrypted1)));

        for (int i = 0; i < 5; i++) {
            byte[] data = MavenProjects.getMainJavaFileAsString(SM2.class).getBytes();
            Map<String, byte[]> keyMap = SM2.generateKeyPair(ecParameter);

            System.out.println("\n=============================加密/解密============================");
            byte[] encrypted = SM2.encrypt(ecParameter, keyMap.get(SM2.PUBLIC_KEY), data);
            System.out.println(Base64.getEncoder().encodeToString(SM2.getPublicKey(keyMap)));
            System.out.println(Base64.getEncoder().encodeToString(SM2.getPrivateKey(keyMap)));
            System.out.println(Base64.getEncoder().encodeToString(encrypted));
            byte[] decrypted = SM2.decrypt(ecParameter, keyMap.get(SM2.PRIVATE_KEY), encrypted);
            System.out.println(new String(decrypted));

            System.out.println("\n=============================签名/验签============================");
            byte[] signed = SM2.sign(ecParameter, data, "IDA".getBytes(), SM2.getPublicKey(keyMap), SM2.getPrivateKey(keyMap));
            System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(signed));
            System.out.println(SM2.verify(ecParameter, data, "IDA".getBytes(), signed, SM2.getPublicKey(keyMap)));
        }

        byte[] data = MavenProjects.getMainJavaFileAsString(SM2.class).substring(0, 100).getBytes();
        Map<String, byte[]> keyMap = SM2.generateKeyPair(ecParameter);
        System.out.println("\ncheckPublicKey: "+SM2.checkPublicKey(ecParameter, SM2.getPublicKey(keyMap)));
        for (int i = 0; i < 5; i++) {
            System.out.println("\n=============================加密/解密============================");
            byte[] encrypted = SM2.encrypt(ecParameter, keyMap.get(SM2.PUBLIC_KEY), data);
            byte[] decrypted = SM2.decrypt(ecParameter, keyMap.get(SM2.PRIVATE_KEY), encrypted);
            System.out.println(new String(decrypted));

            System.out.println("\n=============================签名/验签============================");
            byte[] signed = SM2.sign(ecParameter, data, "IDA".getBytes(), SM2.getPublicKey(keyMap), SM2.getPrivateKey(keyMap));
            System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(signed));
            System.out.println(SM2.verify(ecParameter, data, "IDA".getBytes(), signed, SM2.getPublicKey(keyMap)));
        }
    }

    @Test
    public void test4() {
        byte[] publicKey1 = Base64.getDecoder().decode("BDky9CNygRTBGh7eGdzdbxP5eGRozk4wQfFmREncwEKnYHhNy1OoBvh0wY/RMH/3ikfYejClHxlI1T+jRa0m2wU=");
        byte[] privateKey1 = Base64.getDecoder().decode("AJaz6VNm1Wl9ba1YCEkYi36+m+8DDL4LDuLM172tg5Ao");
        byte[] encrypted1 = Base64.getDecoder().decode("BBdOqZ+7WMaYzyyEZui0b5tdfJqquXH0pMxyoSv04BzYCqKIaeXPcc/vIek8tcK4kCkp022OCZ2TW2bLgVgGvFxeMOzGUgliTQP521HmqbQaawS4FFHxGa1vy+lk9UrkOIaTiEqjIRFSB2SW4cOi3u1mwvqK8EuaYFb143K539HIuqu84Bo3RD2zIpssO+yZzduG4KDcn4iqOJ+NJ1RB5wlI1xEyyiKit6c0mVBfSnXbUkuAyml50dM=");
        System.out.println(new String(SM2.decrypt(privateKey1, encrypted1)));


        byte[]   encrypted = SM2.encrypt(publicKey1, MavenProjects.getTestJavaFileAsBytes(SM2Test.class));
        System.out.println(Base64.getEncoder().encodeToString(encrypted));
    }
}
