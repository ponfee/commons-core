/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.serial;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import code.ponfee.commons.collect.ByteArrayWrapper;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.serial.Serializations.Serializers;
import code.ponfee.commons.util.SecureRandoms;

/**
 * 
 * 
 * @author Ponfee
 */
public class SerialTest {

    @Test
    @Ignore
    public void test0() {
        Assert.assertNotNull(ClassUtils.newInstance(SerialTest.class, "abc"));
    }

    @Test
    public void test1() {
        Assert.assertNotNull(ClassUtils.newInstance(SerialTest.class));
        Assert.assertNotNull(ClassUtils.newInstance(ByteArrayWrapper.class, SecureRandoms.nextBytes(10)));
        Assert.assertNotNull(ClassUtils.newInstance(Result.class, new Class[] { int.class, String.class }, new Object[] { 1, "a" }));
    }
    
    @Test
    public void test2() {
        //Assert.assertNotNull(Serializations.deserialize(Serializations.serialize(true), boolean.class));
        //Assert.assertNotNull(Serializations.deserialize(Serializations.serialize(1), int.class));
        //Assert.assertNotNull(Serializations.deserialize(Serializations.serialize(ByteArrayWrapper.of(SecureRandoms.nextBytes(10))), ByteArrayWrapper.class));
        //Assert.assertNotNull(Serializations.deserialize(Serializations.serialize(new Date()), Date.class));
        //Assert.assertNotNull(Serializations.deserialize(Serializations.serialize("abc"), String.class));
        Assert.assertNotNull(Serializations.deserialize(Serializations.serialize(Serializers.BOOLEAN), Serializers.class));
        //Assert.assertNotNull(Serializations.deserialize(Serializations.serialize(Result.SUCCESS), Result.class));
    }
}
