/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.implementation;

/**
 * This class of the Cryptor base class
 * @author Ponfee
 */
public abstract class Cryptor {

    public final byte[] encrypt(byte[] original, Key ek) {
        return encrypt(original, original.length, ek);
    }

    /**
     * encrypt original data in length byte
     * @param original 
     * @param length the byte length of original
     * @param ek encrypt key
     * @return
     */
    public abstract byte[] encrypt(byte[] original, int length, Key ek);

    /**
     * decrypt the cipher use decrypt key
     * @param cipher
     * @param dk
     * @return
     */
    public abstract byte[] decrypt(byte[] cipher, Key dk);

    /**
     * generate cryptor key
     * @return
     */
    public abstract Key generateKey();

}
