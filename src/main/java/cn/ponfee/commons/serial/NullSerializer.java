/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.serial;

/**
 * The {@code NullSerializer} class is representing unable serializer, 
 * it will be throws NullPointerException
 * 
 * @author Ponfee
 */
public final class NullSerializer extends Serializer {

    public static final NullSerializer SINGLETON = new NullSerializer();

    private NullSerializer() {}

    @Override
    protected <T> byte[] serialize0(T obj, boolean compress) {
        throw new NullPointerException();
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> clazz, boolean compress) {
        throw new NullPointerException();
    }
}
