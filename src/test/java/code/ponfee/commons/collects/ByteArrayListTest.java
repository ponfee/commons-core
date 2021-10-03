/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.collects;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.collect.ByteArrayList;
import code.ponfee.commons.collect.HashKey;
import code.ponfee.commons.model.Result;

/**
 * 
 * 
 * @author Ponfee
 */
public class ByteArrayListTest {

    @Test
    public void test1() {
        ByteArrayList list = new ByteArrayList(new byte[] {1,2,3,4,5,6,5,8,9}, 3, 7);
        System.out.println(list.get(0));
        System.out.println(list.isEmpty());
        System.out.println(list.size());
        System.out.println();
        list.iterator().forEachRemaining(System.out::print);
        System.out.println();
        list.listIterator().forEachRemaining(System.out::print);
        System.out.println();
        list.listIterator(2).forEachRemaining(System.out::print);
        System.out.println();
        
        System.out.println("============================");
        System.out.println(list.contains((byte)1));
        System.out.println(list.contains((byte)5));
        System.out.println(list.contains("x"));

        System.out.println("============================");
        System.out.println(list.indexOf((byte) 1));
        System.out.println(list.indexOf((byte) 5));
        System.out.println(list.indexOf("x"));
        
        System.out.println("============================");
        System.out.println(list.lastIndexOf((byte) 1));
        System.out.println(list.lastIndexOf((byte) 5));
        System.out.println(list.lastIndexOf("x"));
        
        System.out.println("============================");
        list.set(3, (byte)7);
        System.out.println(list);
        System.out.println(list.subList(0, 2));
        System.out.println(list.equals(new ByteArrayList(new byte[] {4,5,6,7})));
        System.out.println(list.equals(new ByteArrayList(new byte[] {4,5})));
        System.out.println(list.hashCode());

        System.out.println(Arrays.asList(new byte[] {4,5,6,7}));
        System.out.println(new ByteArrayList(new byte[] {4,5,6,7}));
    }
    
    @Test
    public void test2() {
        ByteArrayList list = new ByteArrayList(new byte[] {1,2,3,4,5,6,5,8,9});
        System.out.println(list.containsAll(Arrays.asList((byte) 1, (byte) 2, (byte) 3, (byte) 4)));
        System.out.println(Arrays.toString(list.toArray(new Byte[] {})));
        System.out.println(IntStream.of(new int[] { 1, 2, 3, 4 }).boxed().collect(Collectors.toList()));
    }

    @Test
    public void test3() throws Exception {
        Method method1 = Result.class.getDeclaredMethod("copy", Object.class);
        Field field1 = Result.class.getDeclaredField("code");

        Map<HashKey, Boolean> map = ImmutableMap.of(HashKey.of(method1, field1), true);

        Method method2 = Result.class.getDeclaredMethod("copy", Object.class);
        Field field2 = Result.class.getDeclaredField("code");

        System.out.println(method1 == method2);
        System.out.println(field1 == field2);
        
        System.out.println(map.get(HashKey.of(method2, field2)));
    }
}
