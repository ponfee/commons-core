/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.implementation;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Null Cryptor that do nothing
 * 
 * @author Ponfee
 */
public final class NoopCryptor extends Cryptor {

    public static final NoopCryptor SINGLETON = new NoopCryptor();

    private NoopCryptor() {}

    @Override
    public byte[] encrypt(byte[] input, int length, Key ek) {
        return input;
    }

    @Override
    public byte[] decrypt(byte[] cipher, Key dk) {
        return cipher;
    }

    @Override
    public Key generateKey() {
        return NoopKey.SINGLETON;
    }

    @Override
    public String toString() {
        return NoopCryptor.class.getSimpleName();
    }

    private static final class NoopKey implements Key {

        private static final NoopKey SINGLETON = new NoopKey();

        private NoopKey() {}

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
