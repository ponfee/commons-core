/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.collect;

/**
 * Represents the class has an byte[] array args constructor and a toByteArray method
 * 
 * For serialize
 * 
 * @author Ponfee
 */
public abstract class ByteArrayTrait {

    public ByteArrayTrait(byte[] array) {}

    /**
     * Returns byte array
     *
     * @return byte array
     */
    public abstract byte[] toByteArray();

}
