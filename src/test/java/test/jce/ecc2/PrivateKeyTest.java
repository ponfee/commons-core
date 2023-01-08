package test.jce.ecc2;

import static test.jce.ecc2.BaseConvert.baseEncodedStringToByteArray;
import static test.jce.ecc2.BaseConvert.byteArrayToBaseEncodedString;
import static test.jce.ecc2.CurveParameters.secp256k1;
import static test.jce.ecc2.CurveParameters.secp256r1;
import static test.jce.ecc2.PrivateKey.getDefaultSignatureConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.junit.Assert;
import org.junit.Test;

public class PrivateKeyTest {

    @Test
    public void testFromAndToString() throws Exception {
        Assert.assertEquals(
                "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                PrivateKey.fromString(
                        secp256k1,
                        "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16).toString(16));
        Assert.assertEquals(PrivateKey.fromString(
                secp256k1,
                "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                16).toString(),
                "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c");
        Assert.assertEquals(PrivateKey.fromString(
                secp256k1,
                "01",
                16).toString(),
                "01");
        Assert.assertEquals(PrivateKey.fromString(
                secp256k1,
                "01",
                10).toString(10),
                "1");
    }

    @Test(expected = SecurityException.class)
    public void testFromStringEmptyStringThrows() throws Exception {
        PrivateKey.fromString(secp256k1, "", 16);
    }

    @Test(expected = SecurityException.class)
    public void testFromStringZeroStringThrows() throws Exception {
        PrivateKey.fromString(secp256k1, "00", 16);
    }

    @Test(expected = SecurityException.class)
    public void testFromStringVeryBigInputStringThrows() throws Exception {
        PrivateKey.fromString(secp256k1, "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08cc6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c", 16);
    }

    @Test(expected = UnsupportedBaseException.class)
    public void testFromStringUnsupportedBase() throws Exception {
        PrivateKey.fromString(secp256k1, "01", 1234);
    }

    @Test
    public void testGetPublicKey() throws Exception {
        Assert.assertEquals(
                PrivateKey.fromString(
                        secp256k1,
                        "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16).getPublicKey().toString(16),
                "0200bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1"
        );
        Assert.assertEquals(
                PrivateKey.fromString(
                        secp256k1,
                        "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16).getPublicKey(),
                PublicKey.fromString(secp256k1,
                        "0200bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1",
                        16));
        Assert.assertEquals(
                PrivateKey.fromString(
                        CurveParameters.getCurveParametersByName("secp256k1"),
                        "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16).getPublicKey(),
                PublicKey.fromString(secp256k1,
                        "0200bf0e38b86329f84ea90972e0f901d5ea0145f1ebac8c50fded77796d7a70e1",
                        16));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTimeStampAndNonceSignatureConfigBuilderSadPath() {
        new PrivateKey.SignatureConfigBuilder().setRecover(false).setTimeStampAndNonce(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTimeStampAndNonceSignatureConfigSadPath() {
        //noinspection ConstantConditions
        new PrivateKey.SignatureConfig(false, true, true, new SHA256Digest(), new SHA256Digest());
    }

    private static PrivateKey examplePrivateKey;

    static {
        try {
            examplePrivateKey = PrivateKey.fromString(secp256k1,
                    "AgC/Dji4Yyn4TqkJcuDd3ltenDh",
                    64);
        } catch (UnsupportedBaseException ignored) {
        }
    }

    @Test(expected = UnsupportedBaseException.class)
    public void testToStringUnsupportedBaseException() throws Exception {
        String out = examplePrivateKey.toString(12345);
        throw new RuntimeException(out);
    }

    @Test
    public void testEquality() throws Exception {
        Assert.assertEquals(
                PrivateKey.fromString(
                        secp256k1,
                        "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16),
                PrivateKey.fromString(
                        secp256k1,
                        "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16));
        Assert.assertEquals(
                PrivateKey.fromString(
                        secp256k1,
                        "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16),
                PrivateKey.fromString(
                        secp256k1,
                        "000000c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16));
        Assert.assertEquals(
                PrivateKey.fromString(
                        secp256k1,
                        "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16),
                PrivateKey.fromString(
                        CurveParameters.getCurveParametersByName("secp256k1"),
                        "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16));
    }

    @Test
    public void testDeterministicSignatureNoRecover() throws Exception {
        PrivateKey privateKey = PrivateKey.fromString(
                secp256k1,
                "22c49372a7506d162e6551fca36eb59235a9252c7f55610b8d0859d8752235a9",
                16);
        String input = "コトドリ属（コトドリぞく、学名 Menura）はコトドリ上科コトドリ科 Menuridae に属する鳥の属の一つ。コトドリ科は単型である。";
        byte[] signatureBytes = privateKey.signUTF8String(input,
                new PrivateKey.SignatureConfigBuilder()
                        .setRecover(false)
                        .build());
        Assert.assertTrue(privateKey.getPublicKey().verifySignedUTF8String(input, signatureBytes));
        String signature = byteArrayToBaseEncodedString(signatureBytes, 16);
        Assert.assertEquals("3045022100a28224c02e60f4e0a345cfc1043de9be408301393eec9225ab849d6bed8b794302205d09d76f6ae27094c005883d41e7059bb14afb0d9b61f9c051dea384b5048834", signature);
    }

    @Test
    public void testDeterministicSignatureWithRecover() throws Exception {
        PrivateKey privateKey = PrivateKey.fromString(
                secp256k1,
                "22c49372a7506d162e6551fca36eb59235a9252c7f55610b8d0859d8752235a9",
                16);
        String input = "コトドリ属（コトドリぞく、学名 Menura）はコトドリ上科コトドリ科 Menuridae に属する鳥の属の一つ。コトドリ科は単型である。";
        byte[] signatureBytes = privateKey.signUTF8String(input,
                new PrivateKey.SignatureConfigBuilder()
                        .setRecover(true)
                        .setTimeStampAndNonce(false)
                        .build());
        Assert.assertTrue(privateKey.getPublicKey().verifySignedUTF8String(input, signatureBytes));
        String signature = byteArrayToBaseEncodedString(signatureBytes, 16);
        Assert.assertEquals("3048022100a28224c02e60f4e0a345cfc1043de9be408301393eec9225ab849d6bed8b794302205d09d76f6ae27094c005883d41e7059bb14afb0d9b61f9c051dea384b504883402011b", signature);
    }

    @Test
    public void testDeterministicSignatureWithRecoverTestCanonical() throws Exception {
        PrivateKey privateKey = PrivateKey.fromString(
                secp256k1,
                "22c49372a7506d162e6551fca36eb59235a9252c7f55610b8d0859d8752235a9",
                16);
        String input = "コトドリ属（コトドリぞく、学名 Menura）はコトドリ上科コトドリ科 Menuridae に属する鳥の属の一つ。コトドリ科は単型である。XXX";
        byte[] canonicalSignatureBytes = privateKey.signUTF8String(input,
                new PrivateKey.SignatureConfigBuilder()
                        .setRecover(true)
                        .setTimeStampAndNonce(false)
                        .setCanonical(true)
                        .build());
        Assert.assertTrue(privateKey.getPublicKey().verifySignedUTF8String(input, canonicalSignatureBytes));
        String canonicalSignature = byteArrayToBaseEncodedString(canonicalSignatureBytes, 16);
        byte[] nonCanonicalSignatureBytes = privateKey.signUTF8String(input,
                new PrivateKey.SignatureConfigBuilder()
                        .setRecover(true)
                        .setTimeStampAndNonce(false)
                        .setCanonical(false)
                        .build());
        Assert.assertTrue(privateKey.getPublicKey().verifySignedUTF8String(input, nonCanonicalSignatureBytes));
        String nonCanonicalSignature = byteArrayToBaseEncodedString(nonCanonicalSignatureBytes, 16);
        Assert.assertFalse(canonicalSignature.equals(nonCanonicalSignature));
    }

    @Test
    public void testDeterministicSignatureWithRecoverByteArray() throws Exception {
        PrivateKey privateKey = PrivateKey.fromByteArray(
                secp256k1,
                BaseConvert.baseEncodedStringToByteArray("22c49372a7506d162e6551fca36eb59235a9252c7f55610b8d0859d8752235a9", 16));
        String input = "コトドリ属（コトドリぞく、学名 Menura）はコトドリ上科コトドリ科 Menuridae に属する鳥の属の一つ。コトドリ科は単型である。";
        byte[] signatureBytes = privateKey.signUTF8String(input,
                new PrivateKey.SignatureConfigBuilder()
                        .setRecover(true)
                        .setTimeStampAndNonce(false)
                        .build());
        Assert.assertTrue(privateKey.getPublicKey().verifySignedUTF8String(input, signatureBytes));
        String signature = byteArrayToBaseEncodedString(signatureBytes, 16);
        Assert.assertEquals("3048022100a28224c02e60f4e0a345cfc1043de9be408301393eec9225ab849d6bed8b794302205d09d76f6ae27094c005883d41e7059bb14afb0d9b61f9c051dea384b504883402011b", signature);
    }

    @Test
    public void testDeterministicSignatureWithRecoverByteArrayBackAndForth() throws Exception {
        PrivateKey privateKey =
                PrivateKey.fromByteArray(
                        secp256k1,
                        PrivateKey.fromByteArray(
                                secp256k1,
                                BaseConvert.baseEncodedStringToByteArray("22c49372a7506d162e6551fca36eb59235a9252c7f55610b8d0859d8752235a9", 16))
                                .toByteArray());
        String input = "コトドリ属（コトドリぞく、学名 Menura）はコトドリ上科コトドリ科 Menuridae に属する鳥の属の一つ。コトドリ科は単型である。";
        byte[] signatureBytes = privateKey.signUTF8String(input,
                new PrivateKey.SignatureConfigBuilder()
                        .setRecover(true)
                        .setTimeStampAndNonce(false)
                        .build());
        Assert.assertTrue(privateKey.getPublicKey().verifySignedUTF8String(input, signatureBytes));
        String signature = byteArrayToBaseEncodedString(signatureBytes, 16);
        Assert.assertEquals("3048022100a28224c02e60f4e0a345cfc1043de9be408301393eec9225ab849d6bed8b794302205d09d76f6ae27094c005883d41e7059bb14afb0d9b61f9c051dea384b504883402011b", signature);
    }

    @Test
    public void testDeterministicSignatureWithRecoverConstructor() throws Exception {
        PrivateKey privateKey = new PrivateKey(
                secp256k1,
                new BigInteger(1, BaseConvert.baseEncodedStringToByteArray("22c49372a7506d162e6551fca36eb59235a9252c7f55610b8d0859d8752235a9", 16)));
        String input = "コトドリ属（コトドリぞく、学名 Menura）はコトドリ上科コトドリ科 Menuridae に属する鳥の属の一つ。コトドリ科は単型である。";
        byte[] signatureBytes = privateKey.signUTF8String(input,
                new PrivateKey.SignatureConfigBuilder()
                        .setRecover(true)
                        .setTimeStampAndNonce(false)
                        .build());
        Assert.assertTrue(privateKey.getPublicKey().verifySignedUTF8String(input, signatureBytes));
        String signature = byteArrayToBaseEncodedString(signatureBytes, 16);
        Assert.assertEquals("3048022100a28224c02e60f4e0a345cfc1043de9be408301393eec9225ab849d6bed8b794302205d09d76f6ae27094c005883d41e7059bb14afb0d9b61f9c051dea384b504883402011b", signature);
    }

    @Test
    public void testDeterministicSignatureWithRecoverBigIntegerONE() throws Exception {
        PrivateKey privateKey = new PrivateKey(secp256k1, BigInteger.ONE);
        String input = "コトドリ属（コトドリぞく、学名 Menura）はコトドリ上科コトドリ科 Menuridae に属する鳥の属の一つ。コトドリ科は単型である。";
        byte[] signatureBytes = privateKey.signUTF8String(input,
                new PrivateKey.SignatureConfigBuilder()
                        .setRecover(true)
                        .setTimeStampAndNonce(false)
                        .build());
        Assert.assertTrue(privateKey.getPublicKey().verifySignedUTF8String(input, signatureBytes));
        String signature = byteArrayToBaseEncodedString(signatureBytes, 16);
        Assert.assertEquals("3048022100972b6487837a509cc781ad73fa07c92bbbb65fb8aa35de97a341a0dcdb5244ba022031d27cbe8d4998806862d4df7a75b5b4a102db13aea1b51a080d13f45fda57a402011c", signature);
    }

    @Test
    public void testSignHash() throws Exception {
        PrivateKey privateKey = PrivateKey.fromString(
                secp256k1,
                "22c49372a7506d162e6551fca36eb59235a9252c7f55610b8d0859d8752235a9",
                16);
        Method signHash = PrivateKey.class.getDeclaredMethod("signHash", byte[].class, PrivateKey.SignatureConfig.class);
        signHash.setAccessible(true);
        signHash.invoke(privateKey, new byte[secp256k1.getN().bitLength() / 8], getDefaultSignatureConfig());
    }

    @Test(expected = InvocationTargetException.class)
    public void testSignHashBigHashSadPath() throws Exception {
        PrivateKey privateKey = PrivateKey.fromString(
                secp256k1,
                "22c49372a7506d162e6551fca36eb59235a9252c7f55610b8d0859d8752235a9",
                16);
        Method signHash = PrivateKey.class.getDeclaredMethod("signHash", byte[].class, PrivateKey.SignatureConfig.class);
        signHash.setAccessible(true);
        signHash.invoke(privateKey, new byte[512], getDefaultSignatureConfig());
    }

    @Test
    public void testHashCode() throws Exception {
        Assert.assertEquals(
                PrivateKey.fromString(
                        secp256k1,
                        "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16).hashCode(),
                PrivateKey.fromString(
                        secp256k1,
                        "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16).hashCode());
        Assert.assertEquals(
                PrivateKey.fromString(
                        secp256k1,
                        "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16).hashCode(),
                PrivateKey.fromString(
                        secp256k1,
                        "000000c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16).hashCode());
        Assert.assertEquals(
                PrivateKey.fromString(
                        secp256k1,
                        "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16).hashCode(),
                PrivateKey.fromString(
                        CurveParameters.getCurveParametersByName("secp256k1"),
                        "c6b7f6bfe5bb19b1e390e55ed4ba5df8af6068d0eb89379a33f9c19aacf6c08c",
                        16).hashCode());
    }

    @Test
    public void testDiffieHelman() throws Exception {
        PrivateKey privateKey1 = PrivateKey.fromString(
                secp256k1,
                "22c49372a7506d162e6551fca36eb59235a9252c7f55610b8d0859d8752235a9",
                16);
        PrivateKey privateKey2 = PrivateKey.fromString(
                secp256k1,
                "0ffffffffffffffffffffffffffffffffff9252c7f55610b8d0859d8752235a9",
                16);
        Assert.assertArrayEquals(
                privateKey1.diffieHelmanSharedSecret(privateKey2.getPublicKey()),
                privateKey2.diffieHelmanSharedSecret(privateKey1.getPublicKey()));
    }

    @Test(expected = SecurityException.class)
    public void testDiffieHelmanSadPath() throws Exception {
        PrivateKey privateKey1 = PrivateKey.fromString(
                secp256k1,
                "22c49372a7506d162e6551fca36eb59235a9252c7f55610b8d0859d8752235a9",
                16);
        PrivateKey privateKey2 = PrivateKey.fromString(
                secp256r1,
                "0ffffffffffffffffffffffffffffffffff9252c7f55610b8d0859d8752235a9",
                16);
        privateKey1.diffieHelmanSharedSecret(privateKey2.getPublicKey());
    }

    @Test
    public void testSignatureSadPath() throws Exception {
        PrivateKey privateKey1 = PrivateKey.fromString(
                secp256k1,
                "22c49372a7506d162e6551fca36eb59235a9252c7f55610b8d0859d8752235a9",
                16);
        PrivateKey privateKey2 = PrivateKey.fromString(
                secp256k1,
                "0ffffffffffffffffffffffffffffffffff9252c7f55610b8d0859d8752235a9",
                16);
        String message = "Moloch!";
        String data = byteArrayToBaseEncodedString(privateKey2.signUTF8String(message), 16);
        Assert.assertFalse(
                privateKey1.getPublicKey().verifySignedUTF8String(
                        message,
                        baseEncodedStringToByteArray(data, 16)));
    }

    @Test
    public void testTimeStampChronologicalOrder() throws Exception {
        PrivateKey privateKey1 = PrivateKey.fromString(
                secp256k1,
                "22c49372a7506d162e6551fca36eb59235a9252c7f55610b8d0859d8752235a9",
                16);
        PrivateKey privateKey2 = PrivateKey.fromString(
                secp256r1,
                "22c49372a7506d162e6551fca36eb59235a9252c7f55610b8d0859d8752235a9",
                16);
        String message = "Moloch!";
        Long timeStamp1 = PublicKey.getTimeStampFromSignature(privateKey1.signUTF8String(message));
        Long timeStamp2 = PublicKey.getTimeStampFromSignature(privateKey2.signUTF8String(message));
        Long now = System.currentTimeMillis();
        Assert.assertTrue(timeStamp1 <= timeStamp2);
        Assert.assertTrue(timeStamp2 <= now);
    }

}
