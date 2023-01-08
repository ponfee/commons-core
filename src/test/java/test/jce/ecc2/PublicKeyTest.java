package test.jce.ecc2;

import org.bouncycastle.crypto.digests.GeneralDigest;
import org.bouncycastle.crypto.digests.SHA224Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.junit.Assert;
import org.junit.Test;

import static test.jce.ecc2.BaseConvert.baseEncodedStringToByteArray;
import static test.jce.ecc2.BaseConvert.byteArrayToBaseEncodedString;
import static test.jce.ecc2.CurveParameters.secp256k1;
import static test.jce.ecc2.CurveParameters.secp256r1;
import static test.jce.ecc2.PrivateKey.getDefaultSignatureConfig;

import java.math.BigInteger;
import java.security.MessageDigest;

public class PublicKeyTest {

    @Test
    public void testToString() throws Exception {
        Assert.assertEquals(PublicKey.fromString(secp256k1,
                "0200bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1",
                16).toString(),
                "0200bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1");
        Assert.assertEquals(PublicKey.fromString(secp256k1,
                "0200bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1",
                16).toString(16),
                "0200bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1");
        Assert.assertEquals(PublicKey.fromString(secp256k1,
                "0200bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1",
                16).toString(16, true),
                "0200bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1");
        Assert.assertEquals(PublicKey.fromString(secp256k1,
                "0200bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1",
                16).toString(16, false),
                "0400bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1be9e001b7ece071fb3986b5e96699fe28dbdeec8956682da78a5f6a115b9f14c");
        Assert.assertEquals(PublicKey.fromString(secp256k1,
                "0400bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1be9e001b7ece071fb3986b5e96699fe28dbdeec8956682da78a5f6a115b9f14c",
                16).toString(16, false),
                "0400bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1be9e001b7ece071fb3986b5e96699fe28dbdeec8956682da78a5f6a115b9f14c");
        Assert.assertEquals(PublicKey.fromString(secp256k1,
                "0400bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1be9e001b7ece071fb3986b5e96699fe28dbdeec8956682da78a5f6a115b9f14c",
                16).toString(16, true),
                "0200bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1");
        Assert.assertEquals(
                "AgC/Dji4Yyn4TqkJcuD5AdXqAUXx66yMUP3td3ltenDh",
                PublicKey.fromString(secp256k1,
                        "0400bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1be9e001b7ece071fb3986b5e96699fe28dbdeec8956682da78a5f6a115b9f14c",
                        16).toString(64, true));
        Assert.assertEquals(
                "0400bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1be9e001b7ece071fb3986b5e96699fe28dbdeec8956682da78a5f6a115b9f14c",
                PublicKey.fromString(secp256k1,
                        "AgC/Dji4Yyn4TqkJcuD5AdXqAUXx66yMUP3td3ltenDh",
                        64).toString(16, false));
    }

    @Test
    public void testToByteArray() throws Exception {
        Assert.assertEquals("0400bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1be9e001b7ece071fb3986b5e96699fe28dbdeec8956682da78a5f6a115b9f14c",
                PublicKey.fromByteArray(secp256k1, PublicKey.fromString(secp256k1,
                        "0400bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1be9e001b7ece071fb3986b5e96699fe28dbdeec8956682da78a5f6a115b9f14c",
                        16).toByteArray()).toString(16, false));
        Assert.assertEquals("0400bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1be9e001b7ece071fb3986b5e96699fe28dbdeec8956682da78a5f6a115b9f14c",
                PublicKey.fromUncompressedByteArray(secp256k1, PublicKey.fromString(secp256k1,
                        "0400bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1be9e001b7ece071fb3986b5e96699fe28dbdeec8956682da78a5f6a115b9f14c",
                        16).toByteArray(false)).toString(16, false));
        Assert.assertEquals(
                "0400bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1be9e001b7ece071fb3986b5e96699fe28dbdeec8956682da78a5f6a115b9f14c",
                PublicKey.fromByteArray(secp256k1, PublicKey.fromString(secp256k1,
                        "0400bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1be9e001b7ece071fb3986b5e96699fe28dbdeec8956682da78a5f6a115b9f14c",
                        16).toByteArray(true)).toString(16, false));
        Assert.assertEquals(
                "0400bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1be9e001b7ece071fb3986b5e96699fe28dbdeec8956682da78a5f6a115b9f14c",
                PublicKey.fromByteArray(secp256k1, PublicKey.fromString(secp256k1,
                        "0400bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1be9e001b7ece071fb3986b5e96699fe28dbdeec8956682da78a5f6a115b9f14c",
                        16).toByteArray(false)).toString(16, false));
        Assert.assertEquals(
                "0200bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1",
                PublicKey.fromByteArray(secp256k1, PublicKey.fromString(secp256k1,
                        "0400bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1be9e001b7ece071fb3986b5e96699fe28dbdeec8956682da78a5f6a115b9f14c",
                        16).toByteArray(false)).toString(16, true));
    }

    private static PublicKey examplePublicKey;

    static {
        try {
            examplePublicKey = PublicKey.fromString(secp256k1,
                    "AgC/Dji4Yyn4TqkJcuD5AdXqAUXx66yMUP3td3ltenDh",
                    64);
        } catch (UnsupportedBaseException ignored) {
        }
    }

    @Test(expected = UnsupportedBaseException.class)
    public void testToStringUnsupportedBaseException() throws Exception {
        String out = examplePublicKey.toString(12345);
        throw new RuntimeException(out);
    }

    @Test(expected = UnsupportedBaseException.class)
    public void testFromStringSadPathUnsupportedBaseException() throws Exception {
        PublicKey.fromString(secp256k1,
                "AgC/Dji4Yyn4TqkJcuD5AdXqAUXx66yMUP3td3ltenDh",
                12345);
    }

    @Test
    public void testVerifySignedUTF8String() throws Exception {
        PrivateKey privateKey = PrivateKey.fromString(
                secp256k1,
                "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                16);
        PublicKey publicKey = privateKey.getPublicKey();

        PrivateKey.SignatureConfig noRecoverNotCanonicalConfig = new PrivateKey.SignatureConfigBuilder()
                .setRecover(false)
                .setCanonical(false)
                .build();

        PrivateKey.SignatureConfig recoverButNoTimeStampsAndNonce = new PrivateKey.SignatureConfigBuilder()
                .setRecover(false)
                .setTimeStampAndNonce(false)
                .build();

        Assert.assertTrue(publicKey.verifySignedUTF8String("foo",
                privateKey.signUTF8String("foo")));
        Assert.assertTrue(publicKey.verifySignedUTF8String("foo",
                privateKey.sign("foo".getBytes("UTF-8"))));
        Assert.assertTrue(publicKey.verifySignedUTF8String("foo",
                privateKey.signUTF8String("foo", noRecoverNotCanonicalConfig)));
        Assert.assertTrue(publicKey.verifySignedUTF8String("foo",
                privateKey.sign("foo".getBytes("UTF-8"), noRecoverNotCanonicalConfig)));
        Assert.assertTrue(publicKey.verifySignedUTF8String("foo",
                privateKey.sign("foo".getBytes("UTF-8"), recoverButNoTimeStampsAndNonce)));
        Assert.assertArrayEquals(
                privateKey.sign("foo".getBytes("UTF-8"), noRecoverNotCanonicalConfig),
                privateKey.signUTF8String("foo", noRecoverNotCanonicalConfig));
        PrivateKey.SignatureConfig defaultConfig = getDefaultSignatureConfig();
        Assert.assertArrayEquals(
                privateKey.sign("foo".getBytes("UTF-8"), defaultConfig),
                privateKey.signUTF8String("foo", defaultConfig));
        Assert.assertTrue(publicKey.verifySignature("foo".getBytes("UTF-8"),
                privateKey.sign("foo".getBytes("UTF-8"), noRecoverNotCanonicalConfig)));
        Assert.assertTrue(publicKey.verifySignature("foo".getBytes("UTF-8"),
                privateKey.sign("foo".getBytes("UTF-8"), noRecoverNotCanonicalConfig),
                new SHA256Digest()));
    }

    @Test
    public void testVerifySignedUTF8StringWithSecp256r1() throws Exception {
        PrivateKey privateKey = PrivateKey.fromString(
                secp256r1,
                "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                16);
        PublicKey publicKey = privateKey.getPublicKey();
        PrivateKey.SignatureConfig noRecoverButCanonicalConfig = new PrivateKey.SignatureConfigBuilder()
                .setRecover(false)
                .setTimeStampAndNonce(false)
                .build();
        PrivateKey.SignatureConfig noRecoverNotCanonicalConfig = new PrivateKey.SignatureConfigBuilder()
                .setRecover(false)
                .setCanonical(false)
                .setTimeStampAndNonce(false)
                .build();
        PrivateKey.SignatureConfig recoverNotCanonicalConfig = new PrivateKey.SignatureConfigBuilder()
                .setRecover(true)
                .setCanonical(false)
                .setTimeStampAndNonce(false)
                .build();
        GeneralDigest sha256digest = new SHA256Digest();
        PrivateKey.SignatureConfig recoverNotCanonicalExplicitMessageDigestConfig =
                new PrivateKey.SignatureConfigBuilder()
                        .setRecover(true)
                        .setCanonical(false)
                        .setTimeStampAndNonce(false)
                        .setMessageDigest(sha256digest)
                        .build();
        PrivateKey.SignatureConfig recoverNotCanonicalExplicitRFC6979DigestConfig =
                new PrivateKey.SignatureConfigBuilder()
                        .setRecover(true)
                        .setCanonical(false)
                        .setTimeStampAndNonce(false)
                        .setRfc6979Digest(sha256digest)
                        .build();
        Assert.assertTrue(publicKey.verifySignedUTF8String("foo",
                privateKey.signUTF8String("foo")));
        Assert.assertTrue(publicKey.verifySignedUTF8String("foo",
                privateKey.sign("foo".getBytes("UTF-8"))));
        Assert.assertTrue(publicKey.verifySignedUTF8String("bar",
                privateKey.signUTF8String("bar", noRecoverButCanonicalConfig)));
        Assert.assertTrue(publicKey.verifySignedUTF8String("bar",
                privateKey.signUTF8String("bar", noRecoverButCanonicalConfig), new SHA256Digest()));
        Assert.assertTrue(publicKey.verifySignedUTF8String("baz",
                privateKey.sign("baz".getBytes("UTF-8"), noRecoverButCanonicalConfig)));
        Assert.assertTrue(publicKey.verifySignature("قفقاز".getBytes("UTF-8"),
                privateKey.sign("قفقاز".getBytes("UTF-8"), noRecoverNotCanonicalConfig)));
        Assert.assertTrue(publicKey.verifySignedUTF8String("कॉकेशस",
                privateKey.signUTF8String("कॉकेशस", recoverNotCanonicalConfig)));
        Assert.assertTrue(publicKey.verifySignedUTF8String("კავკაცია",
                privateKey.sign("კავკაცია".getBytes("UTF-8"), recoverNotCanonicalConfig)));
        Assert.assertTrue(publicKey.verifySignature("കൊക്കേഷ്യ".getBytes("UTF-8"),
                privateKey.sign("കൊക്കേഷ്യ".getBytes("UTF-8"), noRecoverNotCanonicalConfig)));
        Assert.assertTrue(publicKey.verifySignature("കൊക്കേഷ്യ".getBytes("UTF-8"),
                privateKey.sign("കൊക്കേഷ്യ".getBytes("UTF-8"), recoverNotCanonicalExplicitMessageDigestConfig)));
        Assert.assertTrue(publicKey.verifySignature("കൊക്കേഷ്യ".getBytes("UTF-8"),
                privateKey.sign("കൊക്കേഷ്യ".getBytes("UTF-8"), recoverNotCanonicalExplicitRFC6979DigestConfig)));
    }

    @Test
    public void testRecoverPublicKey() throws Exception {
        final PrivateKey privateKey = PrivateKey.fromString(
                secp256k1,
                "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                16);
        final PublicKey publicKey = privateKey.getPublicKey();
        Assert.assertEquals(
                "0200bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1",
                publicKey.toString(16));
        final String message = "foo";
        final byte[] bareSignature = privateKey.signUTF8String(message,
                new PrivateKey.SignatureConfigBuilder()
                        .setRecover(false)
                        .build());
        Assert.assertEquals(
                "304402203dece00b786bb9d49ce00b87323e98afdd3c7ff67f45f56502dc281e98fae20102206efbfc836f990775edc60f50e8a74f913968288e30ae94703813b09db3f4f3dd",
                byteArrayToBaseEncodedString(bareSignature, 16));
        final byte[] signature = privateKey.signUTF8String(message,
                new PrivateKey.SignatureConfigBuilder()
                        .setRecover(true)
                        .setTimeStampAndNonce(false)
                        .build());
        final PublicKey recoveredPublicKey = PublicKey.recoverPublicKey(
                secp256k1,
                message.getBytes("UTF-8"),
                signature);
        Assert.assertEquals(publicKey.toString(16), recoveredPublicKey.toString(16));
        final byte[] hash = MessageDigest.getInstance("SHA-256").digest(message.getBytes("UTF-8"));
        final PublicKey recoveredPublicKeyFromHash = PublicKey.recoverPublicKeyWithHash(
                secp256k1,
                hash,
                signature);
        Assert.assertEquals(publicKey.toString(16), recoveredPublicKeyFromHash.toString(16));
        final byte[] signatureWithTimeStampAndNonce = privateKey.signUTF8String(message,
                new PrivateKey.SignatureConfigBuilder()
                        .setRecover(true)
                        .setTimeStampAndNonce(true)
                        .build());
        Assert.assertEquals(publicKey.toString(16), PublicKey.recoverPublicKey(
                secp256k1,
                message.getBytes("UTF-8"),
                signatureWithTimeStampAndNonce).toString(16));
        Assert.assertEquals(publicKey.toString(16), PublicKey.recoverPublicKey(
                secp256k1,
                message.getBytes("UTF-8"),
                signatureWithTimeStampAndNonce,
                new SHA256Digest()).toString(16));
    }

    @Test
    public void testRecoverPublicKeySHA224() throws Exception {
        final PrivateKey privateKey = PrivateKey.fromString(
                secp256k1,
                "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                16);
        final PublicKey publicKey = privateKey.getPublicKey();
        Assert.assertEquals(
                "0200bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1",
                publicKey.toString(16));
        final String message = "foo";
        final byte[] bareSignature = privateKey.signUTF8String(message,
                new PrivateKey.SignatureConfigBuilder()
                        .setRecover(false)
                        .setMessageDigest(new SHA224Digest())
                        .build());
        Assert.assertEquals(
                "3044022060d071aa96053205693a3c78ee4e1dd62676c4f73bc70f98fc44792d7b6a6c9902206d1b2532cee810d7f264d2cb77341b0e1197dc37a84858cdcdcbe82173ffdd38",
                byteArrayToBaseEncodedString(bareSignature, 16));
        final byte[] signature = privateKey.signUTF8String(message,
                new PrivateKey.SignatureConfigBuilder()
                        .setRecover(true)
                        .setMessageDigest(new SHA224Digest())
                        .setTimeStampAndNonce(false)
                        .build());
        final PublicKey recoveredPublicKey = PublicKey.recoverPublicKey(
                secp256k1,
                message.getBytes("UTF-8"),
                signature,
                new SHA224Digest());
        Assert.assertEquals(publicKey.toString(16), recoveredPublicKey.toString(16));
        final byte[] hash = MessageDigest.getInstance("SHA-224").digest(message.getBytes("UTF-8"));
        final PublicKey recoveredPublicKeyFromHash = PublicKey.recoverPublicKeyWithHash(
                secp256k1,
                hash,
                signature);
        Assert.assertEquals(publicKey.toString(16), recoveredPublicKeyFromHash.toString(16));
        final byte[] signatureWithTimeStampAndNonce = privateKey.signUTF8String(message,
                new PrivateKey.SignatureConfigBuilder()
                        .setRecover(true)
                        .setMessageDigest(new SHA224Digest())
                        .setTimeStampAndNonce(true)
                        .build());
        Assert.assertEquals(publicKey.toString(16), PublicKey.recoverPublicKey(
                secp256k1,
                message.getBytes("UTF-8"),
                signatureWithTimeStampAndNonce,
                new SHA224Digest()).toString(16));
    }

    @Test
    public void testHashCodeTest() throws Exception {
        Assert.assertEquals(
                PrivateKey.fromString(
                        secp256k1,
                        "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16).getPublicKey().hashCode(),
                PublicKey.fromString(secp256k1,
                        "0200bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1",
                        16).hashCode());
        Assert.assertEquals(
                PrivateKey.fromString(
                        CurveParameters.getCurveParametersByName("secp256k1"),
                        "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16).getPublicKey().hashCode(),
                PublicKey.fromString(secp256k1,
                        "0200bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1",
                        16).hashCode());
    }

    @Test(expected = SecurityException.class)
    public void testPublicKeyConstructorPointOnWrongCurveSadPath() {
        BigInteger d = new BigInteger("c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c", 16);
        new PublicKey(secp256k1, secp256r1.getG().multiply(d));
    }

    @Test(expected = SecurityException.class)
    public void testPublicKeyConstructorInvalidPointSadPath() {
        BigInteger d = new BigInteger("c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c", 16);
        // FYI, you need to normalize points before trying to make them PublicKeys!
        new PublicKey(secp256k1, secp256k1.getG().multiply(d));
    }

    @Test
    public void testRecoverPublicKeyFromSignedUTF8String() throws Exception {
        byte[] signature = baseEncodedStringToByteArray("305a022100d55f884dd948b035a78c088af461f65dac80b35940891e914bbd9ba185778771022067eef94e9ae8217b24927a4d4016e9f6d2ac27e077ad11d02f2496d83ecdc9b502011c020601573f8f27af02086662fe5c7db4b76b", 16);
        PrivateKey privateKey2 = PrivateKey.fromString(
                secp256r1,
                "0ffffffffffffffffffffffffffffffffff9252c7f55610b8d0859d8752235a9",
                16);
        String message = "Moloch!";
        Assert.assertEquals(privateKey2.getPublicKey(),
                PublicKey.recoverPublicKeyFromSignedUTF8String(secp256r1, message, signature));
        Assert.assertEquals(privateKey2.getPublicKey(),
                PublicKey.recoverPublicKeyFromSignedUTF8String(secp256r1, message, signature, new SHA256Digest()));
    }

    @Test(expected = SecurityException.class)
    public void testRecoverPublicKeyFromSignedUTF8StringSadPath() throws Exception {
        byte[] signature = baseEncodedStringToByteArray("305a022100d55f884dd948b035a78c088af461f65dac80b35940891e914bbd9ba185778771022067eef94e9ae8217b24927a4d4016e9f6d2ac27e077ad11d02f2496d83ecdc9b502011c020601573f8f27af02086662fe5c7db4b76b", 16);
        String message = "Moloch!";
        PublicKey.recoverPublicKeyFromSignedUTF8String(secp256k1, message, signature);
    }

    @Test(expected = SecurityException.class)
    public void testRecoverPublicKeyFromSignedUTF8StringSadPathBadDER() throws Exception {
        byte[] signature = baseEncodedStringToByteArray("305c022100d55f884dd948b035a78c088af461f65dac80b35940891e914bbd9ba185778771022067eef94e9ae8217b24927a4d4016e9f6d2ac27e077ad11d02f2496d83ecdc9b502011c020601573f8f27af02086662fe5c7db4b76b", 16);
        String message = "Moloch!";
        PublicKey.recoverPublicKeyFromSignedUTF8String(secp256k1, message, signature);
    }

}
