/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.collect.ValueSortedMap;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.model.Page;
import code.ponfee.commons.reflect.CglibUtils;

/**
 * 
 * @author Ponfee
 */
public class Test2 {

    @Test
    public void test0() {
        Map<Object, Object> map = new HashMap<>();
        map.put(1, null);
        map.put("a", "");
        System.out.println(Jsons.toJson(map));
        System.out.println(Jsons.NON_NULL.string(map));
        System.out.println(Jsons.NON_EMPTY.string(map));
    }

    @Test
    public void test1() {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put(RandomStringUtils.randomAlphabetic(1), ThreadLocalRandom.current().nextInt(100));
        }
        map.put("b", 8);
        System.out.println(map);
        TreeMap<String, Integer> tree = ValueSortedMap.nullsFirst(map, Comparator.comparing(v->v));
        System.out.println(tree);
        
        TreeMap<String, Integer> tree2 = new TreeMap<>(Comparator.comparing(k -> map.get(k)));
        tree2.putAll(map);
        System.out.println(tree2);
        
    }
    @Test
    public void test2() {
        Page<Map<String, Object>> source = new Page<>();
        Page<Map> target = new Page<>();
        CglibUtils.copyProperties(source, target);
        Page<Map> p = source.copy();
    }
    
    @Test
    public void test3() {
        ForkJoinPool.commonPool().shutdownNow();
        byte b = 127;
        System.out.println(Integer.toBinaryString( b                ));
        System.out.println(Integer.toBinaryString((b & 0xFF)        ));
        System.out.println(Integer.toBinaryString((b & 0xFF) | 0x100));
        System.out.println(Integer.toBinaryString((b & 0xFF) + 0x100));
    }
}
