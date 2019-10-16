/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.serial;

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
