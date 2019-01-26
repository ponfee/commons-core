package code.ponfee.commons.jce.implementation;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Null Cryptor that do nothing
 * 
 * @author Ponfee
 */
public class NullCryptor extends Cryptor {

    public @Override byte[] encrypt(byte[] input, int length, Key ek) {
        return input;
    }

    public @Override byte[] decrypt(byte[] cipher, Key dk) {
        return cipher;
    }

    public @Override Key generateKey() {
        return new NullKey();
    }

    public @Override String toString() {
        return NullCryptor.class.getSimpleName();
    }

    private static final class NullKey implements Key {
        @Override
        public Key readKey(InputStream in) {
            return null;
        }

        @Override
        public void writeKey(OutputStream out) {}

        @Override
        public Key getPublic() {
            return null;
        }

        @Override
        public boolean isPublic() {
            return false;
        }
    }
}
