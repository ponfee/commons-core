/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package test.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.Test;

import code.ponfee.commons.cache.Cache;
import code.ponfee.commons.cache.CacheBuilder;
import code.ponfee.commons.collect.ByteArrayWrapper;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.ClassUtils;

/**
 * 
 * 
 * @author Ponfee
 */
public class ClassUtilsTest {

    static int n = 100000000;

    @Test
    public void test1() {
        for (int i = 0; i < n; i++) {
            ClassUtils.getConstructor(ByteArrayWrapper.class, byte[].class);
        }
    }

    @Test
    public void test12() {
        for (int i = 0; i < n; i++) {
            ClassUtils.getConstructor(ClassUtilsTest.class);
        }
    }

    @Test
    public void test2() throws Exception {
        for (int i = 0; i < n; i++) {
            ByteArrayWrapper.class.getConstructor(byte[].class);
        }
    }

    @Test
    public void test3() throws Exception {
        int n = 100000;
        Method method = ClassUtils.class.getMethod("newInstance", Class.class, Class.class, Object.class);
        System.out.println(Arrays.toString(ClassUtils.getMethodParamNames(method)));
        for (int i = 0; i < n; i++) {
            ClassUtils.getMethodParamNames(method);
        }
    }

    @Test
    public void test4() throws Exception {
        int n = 1000000000;
        Cache<Method, String[]> METHOD_ARGSNAME = CacheBuilder.<Method, String[]> newBuilder().build();
        Method method = ClassUtils.class.getMethod("newInstance", Class.class, Class.class, Object.class);
        for (int i = 0; i < n; i++) {
            String[] argsName = METHOD_ARGSNAME.get(method);
            if (argsName == null) {
                argsName = ClassUtils.getMethodParamNames(method);
                METHOD_ARGSNAME.set(method, argsName);
                System.out.println(Arrays.toString(argsName));
            }
        }
    }
    
    @Test
    public void test5() throws Exception {
        System.out.println(ClassUtils.class.getMethod("newInstance", Class.class, Class.class, Object.class).equals(ClassUtils.class.getMethod("newInstance", Class.class, Class.class, Object.class)));
        System.out.println(Result.class.getDeclaredField("code").equals(Result.class.getDeclaredField("code")));
    }
}
