package test.jce.crypto;

import static code.ponfee.commons.jce.security.RSACryptor.generateKeyPair;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Files;

import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.jce.digest.DigestUtils;
import code.ponfee.commons.jce.security.RSACryptor;
import code.ponfee.commons.jce.security.RSACryptor.RSAKeyPair;
import code.ponfee.commons.jce.security.RSAPrivateKeys;
import code.ponfee.commons.jce.security.RSAPublicKeys;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.MavenProjects;

public class RSACryptoTester {

    @BeforeClass
    public static void beforeClass() {
        Providers.set(Providers.BC);
    }

    @Before
    public void before() {
        Providers.set(Providers.BC);
    }

    @Test
    public void test1() throws Exception {
        RSAKeyPair keyPair = generateKeyPair(4096);
        
        // 签名解密－－－－
        byte [] bytes = "123456".getBytes();
        byte[] signed = RSACryptor.signSha1(bytes, keyPair.getPrivateKey());
        byte[] decrypted = RSACryptor.decrypt(signed, keyPair.getPublicKey());
        System.out.println(Hex.encodeHexString(DigestUtils.sha1(bytes))); // 7c4a8d09ca3762af61e59520943dc26494f8941b
        System.out.println(Hex.encodeHexString(decrypted)); // 3021300906052b0e03021a050004147c4a8d09ca3762af61e59520943dc26494f8941b
        // -------------
        
        System.out.println(keyPair.toPkcs8PrivateKey());
        System.out.println(keyPair.toPkcs8PublicKey());
        test(keyPair.getPrivateKey(), RSAPrivateKeys.extractPublicKey(keyPair.getPrivateKey()));
        
        test(RSAPrivateKeys.fromPkcs1Pem(RSAPrivateKeys.toPkcs1Pem(RSAPrivateKeys.fromPkcs1(keyPair.toPkcs1PrivateKey()))),
             RSAPublicKeys.fromPkcs8Pem(RSAPublicKeys.toPkcs8Pem(RSAPublicKeys.fromPkcs1(keyPair.toPkcs1PublicKey()))));
        
        test(RSAPrivateKeys.fromPkcs1(RSAPrivateKeys.toPkcs1(keyPair.getPrivateKey())),
             RSAPublicKeys.fromPkcs1(keyPair.toPkcs1PublicKey()));

        test(RSAPrivateKeys.fromPkcs8(keyPair.toPkcs8PrivateKey()),
             RSAPublicKeys.fromPkcs8(keyPair.toPkcs8PublicKey()));

        System.out.println(RSAPrivateKeys.fromEncryptedPkcs8Pem(RSAPrivateKeys.toEncryptedPkcs8Pem(keyPair.getPrivateKey(),"123"), "123"));

        System.out.println(RSAPrivateKeys.toPkcs1(keyPair.getPrivateKey()));
        System.out.println(RSAPrivateKeys.toPkcs8(keyPair.getPrivateKey()));
        System.out.println(RSAPrivateKeys.toPkcs1Pem(keyPair.getPrivateKey()));
        System.out.println(RSAPrivateKeys.toEncryptedPkcs8Pem(keyPair.getPrivateKey(), "1234"));
    }

    @Test
    public void test2() throws Exception {
        String privateKeyStr = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAocbCrurZGbC5GArEHKlAfDSZi7gFBnd4yxOt0rwTqKBFzGyhtQLu5PRKjEiOXVa95aeIIBJ6OhC2f8FjqFUpawIDAQABAkAPejKaBYHrwUqUEEOe8lpnB6lBAsQIUFnQI/vXU4MV+MhIzW0BLVZCiarIQqUXeOhThVWXKFt8GxCykrrUsQ6BAiEA4vMVxEHBovz1di3aozzFvSMdsjTcYRRo82hS5Ru2/OECIQC2fAPoXixVTVY7bNMeuxCP4954ZkXp7fEPDINCjcQDywIgcc8XLkkPcs3Jxk7uYofaXaPbg39wuJpEmzPIxi3k0OECIGubmdpOnin3HuCP/bbjbJLNNoUdGiEmFL5hDI4UdwAdAiEAtcAwbm08bKN7pwwvyqaCBC//VnEWaq39DCzxr+Z2EIk=";
        String publicKeyStr = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKHGwq7q2RmwuRgKxBypQHw0mYu4BQZ3eMsTrdK8E6igRcxsobUC7uT0SoxIjl1WveWniCASejoQtn/BY6hVKWsCAwEAAQ==";
        RSAPrivateKey privateKey = RSAPrivateKeys.fromPkcs8(privateKeyStr);
        System.out.println(RSAPublicKeys.toPkcs8(RSAPrivateKeys.extractPublicKey(privateKey))); // publicKeyStr
        test(privateKey, RSAPrivateKeys.extractPublicKey(privateKey));

        RSAPublicKey publicKey = RSAPublicKeys.fromPkcs8(publicKeyStr);
        test(privateKey, publicKey);

        test(RSAPublicKeys.inverse(publicKey), RSAPrivateKeys.inverse(privateKey));
    }

    @Test
    public void test3() throws Exception {
        String privateKeyStr = "MIICXQIBAAKBgQDlOJu6TyygqxfWT7eLtGDwajtNFOb9I5XRb6khyfD1Yt3YiCgQWMNW649887VGJiGr/L5i2osbl8C9+WJTeucF+S76xFxdU6jE0NQ+Z+zEdhUTooNRaY5nZiu5PgDB0ED/ZKBUSLKL7eibMxZtMlUDHjm4gwQco1KRMDSmXSMkDwIDAQABAoGAfY9LpnuWK5Bs50UVep5c93SJdUi82u7yMx4iHFMc/Z2hfenfYEzu+57fI4fvxTQ//5DbzRR/XKb8ulNv6+CHyPF31xk7YOBfkGI8qjLoq06V+FyBfDSwL8KbLyeHm7KUZnLNQbk8yGLzB3iYKkRHlmUanQGaNMIJziWOkN+N9dECQQD0ONYRNZeuM8zd8XJTSdcIX4a3gy3GGCJxOzv16XHxD03GW6UNLmfPwenKu+cdrQeaqEixrCejXdAFz/7+BSMpAkEA8EaSOeP5Xr3ZrbiKzi6TGMwHMvC7HdJxaBJbVRfApFrE0/mPwmP5rN7QwjrMY+0+AbXcm8mRQyQ1+IGEembsdwJBAN6az8Rv7QnD/YBvi52POIlRSSIMV7SwWvSK4WSMnGb1ZBbhgdg57DXaspcwHsFV7hByQ5BvMtIduHcT14ECfcECQATeaTgjFnqE/lQ22Rk0eGaYO80cc643BXVGafNfd9fcvwBMnk0iGX0XRsOozVt5AzilpsLBYuApa66NcVHJpCECQQDTjI2AQhFc1yRnCU/YgDnSpJVm1nASoRUnU8Jfm3Ozuku7JUXcVpt08DFSceCEX9unCuMcT72rAQlLpdZir876";
        String publicKeyStr = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDlOJu6TyygqxfWT7eLtGDwajtNFOb9I5XRb6khyfD1Yt3YiCgQWMNW649887VGJiGr/L5i2osbl8C9+WJTeucF+S76xFxdU6jE0NQ+Z+zEdhUTooNRaY5nZiu5PgDB0ED/ZKBUSLKL7eibMxZtMlUDHjm4gwQco1KRMDSmXSMkDwIDAQAB";
        RSAPrivateKey privateKey = RSAPrivateKeys.fromPkcs1(privateKeyStr);
        System.out.println(RSAPublicKeys.toPkcs8(RSAPrivateKeys.extractPublicKey(privateKey))); // publicKeyStr
        test(privateKey, RSAPrivateKeys.extractPublicKey(privateKey));

        RSAPublicKey publicKey = RSAPublicKeys.fromPkcs8(publicKeyStr);
        test(privateKey, publicKey);

        test(RSAPublicKeys.inverse(publicKey), RSAPrivateKeys.inverse(privateKey));
    }

    @Test
    public void test4() throws Exception {
        String privateKeyStr = "MIICXAIBAAKBgQDBZ86MIZ2ytsFX9jML+nhYTIC2LdlWzXrN9HV9Ba4yK812S1pgeQpgmt0lFkd378eqb4qb2cC7Z+XT7IOEaSJTp9fP+aKjFG/rKEKG4YPvRD0IKTfm6yDEd9A4bf8a1RxO+5wip9KAGCFdNScwT6DlpDH7gmrzHFWOUPpTsPDNPQIDAQABAoGBAIzz7bl9KmQ8Ay7rNIrPUXPw1YFwasxzVsPRHOsv/6N6/vPuuQBEVsbPNsq3sQB9FURmpFsvWOJ8Nyi7X6JZyPRv9Dal0FuzcLMMU0NLSoW7nAJmzjiU5abS3v5Bj3TfTlAGD7QcXRCM4s5wS18Zm9JPl+vFJkK9Tj1gSoqMhuQVAkEA7g45nL5UgpGny0Ua8xV/PCHq6e6q7VVYFPcWetp8ugJw91ZBJuXzmgp0V+FrmkMuGF2Kx0cauoMMiTqKEosU5wJBAM/791qF07auuXdbxEz9a7ofS3n49sTbMuInsiLWB6m5aRtWb7Wawj2oTvfLOEmIYaMcj1GqFlq7PIYC/rOppDsCQHcQ4Fn4jHZd+cnef5MznlbqM//bYtygAhVCXJkH7LhwfiYHm0CkZQoXzoch9VrL3SNMrhvsAX9mCoAcqnCJ5eMCQCz886RBDmqVoMiQsQV2S7cWzdy0Xax3Paptq7qdUUsFMBcZu1AtCZcMsQgojSRau8PsiZPAltVJau4R98YlC8ECQCMdLS8/lZaOASPocm/fvIno4//NoXrsOi7Wph5vt9OQhQEoYVhdCsk6/28kEy51xAywZ2SjD3IiI/Ygt1uUn8E=";
        String publicKeyStr = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDBZ86MIZ2ytsFX9jML+nhYTIC2LdlWzXrN9HV9Ba4yK812S1pgeQpgmt0lFkd378eqb4qb2cC7Z+XT7IOEaSJTp9fP+aKjFG/rKEKG4YPvRD0IKTfm6yDEd9A4bf8a1RxO+5wip9KAGCFdNScwT6DlpDH7gmrzHFWOUPpTsPDNPQIDAQAB";
        RSAPrivateKey privateKey = RSAPrivateKeys.fromPkcs1(privateKeyStr);
        System.out.println(RSAPublicKeys.toPkcs8(RSAPrivateKeys.extractPublicKey(privateKey))); // publicKeyStr
        test(privateKey, RSAPrivateKeys.extractPublicKey(privateKey));

        RSAPublicKey publicKey = RSAPublicKeys.fromPkcs8(publicKeyStr);
        test(privateKey, publicKey);

        test(RSAPublicKeys.inverse(publicKey), RSAPrivateKeys.inverse(privateKey));
    }

    @Test
    public void test5() throws Exception {
        String privateKeyStr = "MIICXQIBAAKBgQDlOJu6TyygqxfWT7eLtGDwajtNFOb9I5XRb6khyfD1Yt3YiCgQWMNW649887VGJiGr/L5i2osbl8C9+WJTeucF+S76xFxdU6jE0NQ+Z+zEdhUTooNRaY5nZiu5PgDB0ED/ZKBUSLKL7eibMxZtMlUDHjm4gwQco1KRMDSmXSMkDwIDAQABAoGAfY9LpnuWK5Bs50UVep5c93SJdUi82u7yMx4iHFMc/Z2hfenfYEzu+57fI4fvxTQ//5DbzRR/XKb8ulNv6+CHyPF31xk7YOBfkGI8qjLoq06V+FyBfDSwL8KbLyeHm7KUZnLNQbk8yGLzB3iYKkRHlmUanQGaNMIJziWOkN+N9dECQQD0ONYRNZeuM8zd8XJTSdcIX4a3gy3GGCJxOzv16XHxD03GW6UNLmfPwenKu+cdrQeaqEixrCejXdAFz/7+BSMpAkEA8EaSOeP5Xr3ZrbiKzi6TGMwHMvC7HdJxaBJbVRfApFrE0/mPwmP5rN7QwjrMY+0+AbXcm8mRQyQ1+IGEembsdwJBAN6az8Rv7QnD/YBvi52POIlRSSIMV7SwWvSK4WSMnGb1ZBbhgdg57DXaspcwHsFV7hByQ5BvMtIduHcT14ECfcECQATeaTgjFnqE/lQ22Rk0eGaYO80cc643BXVGafNfd9fcvwBMnk0iGX0XRsOozVt5AzilpsLBYuApa66NcVHJpCECQQDTjI2AQhFc1yRnCU/YgDnSpJVm1nASoRUnU8Jfm3Ozuku7JUXcVpt08DFSceCEX9unCuMcT72rAQlLpdZir876";
        String publicKeyStr = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDlOJu6TyygqxfWT7eLtGDwajtNFOb9I5XRb6khyfD1Yt3YiCgQWMNW649887VGJiGr/L5i2osbl8C9+WJTeucF+S76xFxdU6jE0NQ+Z+zEdhUTooNRaY5nZiu5PgDB0ED/ZKBUSLKL7eibMxZtMlUDHjm4gwQco1KRMDSmXSMkDwIDAQAB";
        RSAPrivateKey privateKey = RSAPrivateKeys.fromPkcs1(privateKeyStr);

        System.out.println(RSAPrivateKeys.toPkcs1(privateKey));
        System.out.println(RSAPrivateKeys.toPkcs8(privateKey));

        System.out.println(RSAPublicKeys.toPkcs8(RSAPrivateKeys.extractPublicKey(privateKey)));
        System.out.println(RSAPublicKeys.toPkcs8(RSAPublicKeys.fromPkcs8(publicKeyStr)));

        System.out.println(RSAPublicKeys.toPkcs1(RSAPrivateKeys.extractPublicKey(privateKey)));
        System.out.println(RSAPublicKeys.toPkcs1(RSAPublicKeys.fromPkcs8(publicKeyStr)));

        test(privateKey, RSAPublicKeys.fromPkcs1(RSAPublicKeys.toPkcs1(RSAPublicKeys.fromPkcs8(publicKeyStr))));
    }

    @Test
    public void test6() throws Exception {
        String privateKeyStr = "MIIEpgIBAAKCAQEAudN1YfE4lOSBoRTZC4ErZ8H2IedOjnSKOk9T1MhmefBVkVwi4emT+walqsowtLw6pHP1whoOBbkqZcQjhz5EgDiMJQ8fmeFYjV5eb+5jZVYefdUKYw1kPqt1aOLysnLpLZRaIQfR+6yHJRbBRWEKWvXWkJ1/3nZulYs4UHpJlPc/1pFyppOsYji/xZj74bCbQkMtasngcuemXphU5zxB/ll2P/jR9x3zxjM8/6/nrF3c4dSFDzMzh8qTAwumT5L3G6tQTErZ4bKsDL/AU1wBUx6Y49iPNxnf8IWG+k7TBK9rIgPIQ/rQUKVJ+3NnoEEWQyPvSwS2yFPnqoDq9qijgwIDAQABAoIBAQC3qCqndkU5wu3rSjOJj0xa6/RbZcTaPowvPR/ZeYbulX28gJdpN/Wtb9BkkBi7SB2dU45dHGsndO5WThffHseNAlZgeiX9bB6c+dvUPIO4L/lK3De71gxxc/xCgarke3XCOpEpfBUo7EdVfLvf2hzl8XryyvcJ43tACazKvVHkCxir/tRkHECEt8ssOibvG2GeWpP3pNhAWcfH7ienVnYBerzLCn7ojtkQM11Z2CkoAMT3D+OcfHewkHhhkYYDJMgEsK6awiZhHHr8kgabzYOhO7CKlSURFOS4WmWQQjiBd6lc4m4IC4l5db4FHd1fZ2Kf/kGD+AKsb7pW/f+azvCBAoGBAPDB6l0hQxIB3ZxCV7z608huSGs/oMUGrwTlrYMnupQTeAqBEDPpO80IEhc1LzQy84fN70Md5JfhqUlINvbclr4bs9qSCIPIl/Cv0FSX5zjswO7xW17dV7K2QxVCturqHA08732EW7WHRda7EHVO0KM5V3XlPwE/1/aKNxljZP+RAoGBAMWXPU1xRCCDtFHFSPrSbYXwyIXtJz32GhYvhdjTIiNxrvrweT+9XBUCJ7vlQwx64MWTziJCYHjJDVlHMa1kLTwbxb4OrYTVUZLvOwIifCLpvmzyKooLNcx8TGmMj7LX8FSAgFZHjznhKz/8iJAXBVlh2mFj5pkLAxWqnk8hKI/TAoGBALwF1XBx/51ak6XrMfZGtYr8hdYsVPRKafkbHk0lg9MM+VzKusqvxaI0QVyajojnmcVfkRILkHEFLV4r5bEZSSijHez+y2OQDwlLZRoLn+qXC34QRFlr54eMTAuYlJ4Vw16bTjXqXm0Afgxa/1l9+fbfW2yZYoEpSRIjkzBirYfhAoGBAJIuCr9Rbap0ZaIdR5mwtjBia6eRRPf1K2WAcRBxWw9H2sFxyPIcAJTWTFkZCtqfyczCRb1YyBB0BbkoD5uMwl522Xt7VmowezIuZMR2iMo3jZcCLfCEzJ9k0g9AW0tfsECD9O5f8JlMeXfUN6AKN/3hg/OLOh29ZOHRoV8/U8fbAoGBAKLvvaAlcSaB57GC3BWc+ckQjCVItM+sunPePY4WSCytT6zjyB6EQBQzfimjY7O4xktDQYb9b9m0WNKAHTLGf+0Otk2iMRhL8dKLICodwoHNR+4izQTzcuHpouMNndRLXTqrMiBIIO0k0LtvQzDffPi1AYw/PNwruvZRmaFsOLWu";
        String publicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAudN1YfE4lOSBoRTZC4ErZ8H2IedOjnSKOk9T1MhmefBVkVwi4emT+walqsowtLw6pHP1whoOBbkqZcQjhz5EgDiMJQ8fmeFYjV5eb+5jZVYefdUKYw1kPqt1aOLysnLpLZRaIQfR+6yHJRbBRWEKWvXWkJ1/3nZulYs4UHpJlPc/1pFyppOsYji/xZj74bCbQkMtasngcuemXphU5zxB/ll2P/jR9x3zxjM8/6/nrF3c4dSFDzMzh8qTAwumT5L3G6tQTErZ4bKsDL/AU1wBUx6Y49iPNxnf8IWG+k7TBK9rIgPIQ/rQUKVJ+3NnoEEWQyPvSwS2yFPnqoDq9qijgwIDAQAB";
        RSAPrivateKey privateKey = RSAPrivateKeys.fromPkcs1(privateKeyStr);
        System.out.println(RSAPublicKeys.toPkcs8(RSAPrivateKeys.extractPublicKey(privateKey))); // publicKeyStr
        test(privateKey, RSAPrivateKeys.extractPublicKey(privateKey));

        RSAPublicKey publicKey = RSAPublicKeys.fromPkcs8(publicKeyStr);
        test(privateKey, publicKey);

        test(RSAPublicKeys.inverse(publicKey), RSAPrivateKeys.inverse(privateKey));
    }

    @Test
    public void test7() {
        String privateKeyStr =
            "MIIEpgIBAAKCAQEAudN1YfE4lOSBoRTZC4ErZ8H2IedOjnSKOk9T1MhmefBVkVwi4emT+walqsowtLw6pHP1whoOBbkqZcQjhz5EgDiMJQ8fmeFYjV5eb+5jZVYefdUKYw1kPqt1aOLysnLpLZRaIQfR+6yHJRbBRWEKWvXWkJ1/3nZulYs4UHpJlPc/1pFyppOsYji/xZj74bCbQkMtasngcuemXphU5zxB/ll2P/jR9x3zxjM8/6/nrF3c4dSFDzMzh8qTAwumT5L3G6tQTErZ4bKsDL/AU1wBUx6Y49iPNxnf8IWG+k7TBK9rIgPIQ/rQUKVJ+3NnoEEWQyPvSwS2yFPnqoDq9qijgwIDAQABAoIBAQC3qCqndkU5wu3rSjOJj0xa6/RbZcTaPowvPR/ZeYbulX28gJdpN/Wtb9BkkBi7SB2dU45dHGsndO5WThffHseNAlZgeiX9bB6c+dvUPIO4L/lK3De71gxxc/xCgarke3XCOpEpfBUo7EdVfLvf2hzl8XryyvcJ43tACazKvVHkCxir/tRkHECEt8ssOibvG2GeWpP3pNhAWcfH7ienVnYBerzLCn7ojtkQM11Z2CkoAMT3D+OcfHewkHhhkYYDJMgEsK6awiZhHHr8kgabzYOhO7CKlSURFOS4WmWQQjiBd6lc4m4IC4l5db4FHd1fZ2Kf/kGD+AKsb7pW/f+azvCBAoGBAPDB6l0hQxIB3ZxCV7z608huSGs/oMUGrwTlrYMnupQTeAqBEDPpO80IEhc1LzQy84fN70Md5JfhqUlINvbclr4bs9qSCIPIl/Cv0FSX5zjswO7xW17dV7K2QxVCturqHA08732EW7WHRda7EHVO0KM5V3XlPwE/1/aKNxljZP+RAoGBAMWXPU1xRCCDtFHFSPrSbYXwyIXtJz32GhYvhdjTIiNxrvrweT+9XBUCJ7vlQwx64MWTziJCYHjJDVlHMa1kLTwbxb4OrYTVUZLvOwIifCLpvmzyKooLNcx8TGmMj7LX8FSAgFZHjznhKz/8iJAXBVlh2mFj5pkLAxWqnk8hKI/TAoGBALwF1XBx/51ak6XrMfZGtYr8hdYsVPRKafkbHk0lg9MM+VzKusqvxaI0QVyajojnmcVfkRILkHEFLV4r5bEZSSijHez+y2OQDwlLZRoLn+qXC34QRFlr54eMTAuYlJ4Vw16bTjXqXm0Afgxa/1l9+fbfW2yZYoEpSRIjkzBirYfhAoGBAJIuCr9Rbap0ZaIdR5mwtjBia6eRRPf1K2WAcRBxWw9H2sFxyPIcAJTWTFkZCtqfyczCRb1YyBB0BbkoD5uMwl522Xt7VmowezIuZMR2iMo3jZcCLfCEzJ9k0g9AW0tfsECD9O5f8JlMeXfUN6AKN/3hg/OLOh29ZOHRoV8/U8fbAoGBAKLvvaAlcSaB57GC3BWc+ckQjCVItM+sunPePY4WSCytT6zjyB6EQBQzfimjY7O4xktDQYb9b9m0WNKAHTLGf+0Otk2iMRhL8dKLICodwoHNR+4izQTzcuHpouMNndRLXTqrMiBIIO0k0LtvQzDffPi1AYw/PNwruvZRmaFsOLWu";
        String publicKeyStr =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAudN1YfE4lOSBoRTZC4ErZ8H2IedOjnSKOk9T1MhmefBVkVwi4emT+walqsowtLw6pHP1whoOBbkqZcQjhz5EgDiMJQ8fmeFYjV5eb+5jZVYefdUKYw1kPqt1aOLysnLpLZRaIQfR+6yHJRbBRWEKWvXWkJ1/3nZulYs4UHpJlPc/1pFyppOsYji/xZj74bCbQkMtasngcuemXphU5zxB/ll2P/jR9x3zxjM8/6/nrF3c4dSFDzMzh8qTAwumT5L3G6tQTErZ4bKsDL/AU1wBUx6Y49iPNxnf8IWG+k7TBK9rIgPIQ/rQUKVJ+3NnoEEWQyPvSwS2yFPnqoDq9qijgwIDAQAB";
        RSAPrivateKey privateKey = RSAPrivateKeys.fromPkcs1(privateKeyStr);
        RSAPublicKey publicKey = RSAPublicKeys.fromPkcs8(publicKeyStr);

        byte[] data = "1234".getBytes();
        String sha1 = DigestUtils.sha1Hex(data);
        byte[] signature = RSACryptor.signSha1(data, privateKey);
        byte[] array = RSACryptor.decrypt(signature, publicKey);
        System.out.println(sha1);
        System.out.println(Hex.encodeHexString(array));
    }
    
    private static void test(RSAPrivateKey privateKey, RSAPublicKey publicKey) throws IOException {
        byte[] data = Files.toByteArray(MavenProjects.getTestJavaFile(RSACryptoTester.class));
        System.out.println("=============================加密测试==============================");
        long i = System.currentTimeMillis();
        System.out.println("原文：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(data, 0, 100)));
        byte[] encodedData = RSACryptor.encrypt(data, publicKey);
        System.out.println("密文：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(encodedData, 0, 100)));
        System.out.println("解密：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(RSACryptor.decrypt(encodedData, privateKey), 0, 100)));
        
        System.out.println("=============================加密测试==============================");
        i = System.currentTimeMillis();
        System.out.println("原文：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(data, 0, 100)));
        encodedData = RSACryptor.encrypt(data, privateKey);
        System.out.println("密文：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(encodedData, 0, 100)));
        System.out.println("解密：");
        System.out.println(Bytes.hexDump(ArrayUtils.subarray(RSACryptor.decrypt(encodedData, publicKey), 0, 100)));

        System.out.println("===========================签名测试=========================");
        data = Base64.getDecoder().decode("");
        byte[] signed = RSACryptor.signSha1(data, privateKey);
        System.out.println("签名数据：len->" + signed.length + " ， b64->" + Base64.getEncoder().encodeToString(signed));
        System.out.println("验签结果：" + RSACryptor.verifySha1(data, publicKey, signed));

        System.out.println("cost time: " + (System.currentTimeMillis() - i));
    }
}
