package test.jce.ecc0;

import java.util.Arrays;

import org.junit.Test;

import code.ponfee.commons.jce.ECParameters;
import code.ponfee.commons.jce.implementation.Cryptor;
import code.ponfee.commons.jce.implementation.Key;
import code.ponfee.commons.jce.implementation.NullCryptor;
import code.ponfee.commons.jce.implementation.ecc.ECCryptor;
import code.ponfee.commons.jce.implementation.ecc.EllipticCurve;
import code.ponfee.commons.util.IdcardResolver;
import code.ponfee.commons.util.MavenProjects;

public class ECCryptorTest {

    private static byte[] origin = MavenProjects.getMainJavaFileAsByteArray(IdcardResolver.class);

    @Test
    public void testECCryptor() {
        Cryptor cs = new ECCryptor(new EllipticCurve(ECParameters.secp112r1));
        Key dk = cs.generateKey();
        Key ek = dk.getPublic();
        System.out.println(dk + "\n" + ek);

        byte[] encrypted = cs.encrypt(origin, ek);
        byte[] decrypted = cs.decrypt(encrypted, dk);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
            System.out.println("=====ECCryptor Decrypted text is: \n" + new String(decrypted));
        } else {
            System.out.println("=====ECCryptor Decrypted text is: \n" + new String(decrypted));
        }
    }

    @Test
    public void testNullCryptor() {
        Cryptor cs = new NullCryptor();
        Key dk = cs.generateKey();
        Key ek = dk.getPublic();

        byte[] encrypted = cs.encrypt(origin, ek);
        byte[] decrypted = cs.decrypt(encrypted, dk);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            System.out.println("\n\n=====NullCryptor Decrypted text is: \n" + new String(decrypted));
        }
    }

}
