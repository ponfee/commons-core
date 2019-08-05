/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.objenesis.ObjenesisHelper;

import com.google.common.base.Stopwatch;

import code.ponfee.commons.collect.ValueSortedMap;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.model.Page;
import code.ponfee.commons.reflect.CglibUtils;
import code.ponfee.commons.util.Dates;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Helper;

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
    
    @Test
    public void test4() {
        System.out.println(Dates.format(Dates.ofSeconds(-2000000000)));
    }

    @Test
    public void test5() throws IOException {
        Files.delete(Paths.get("D:\\test\\framework"));
    }
    
    @Test
    public void test6() throws IOException {
        System.out.println(ObjenesisHelper.newInstance(HashMap.class));
        
        System.out.println(Double.isFinite(Math.PI));
        System.out.println(Double.isInfinite(Double.POSITIVE_INFINITY));
        System.out.println(Double.isNaN(0.0/0.0));
        System.out.println(Double.POSITIVE_INFINITY == Double.POSITIVE_INFINITY);
    }
    
    @Test
    public void test7() throws IOException {
     // Create instance
        Argon2 argon2 = Argon2Factory.create();

        // Read password from user
        char[] password = "passwd".toCharArray();

        try {
            // Hash password
            String hash = argon2.hash(8, 65536, 1, password);
            System.out.println(hash);
            // Verify password
            if (argon2.verify(hash, password)) {
                // Hash matches password
            } else {
                // Hash doesn't match password
            }
        } finally {
            // Wipe confidential data
            argon2.wipeArray(password);
        }
    }
    
    @Test
    public void test8() throws IOException {
        Argon2 argon2 = Argon2Factory.create();
        // 1000 = The hash call must take at most 1000 ms
        // 65536 = Memory cost
        // 1 = parallelism
        int iterations = Argon2Helper.findIterations(argon2, 1000, 65536, 1);
        System.out.println("Optimal number of iterations: " + iterations);
    }
    
    @Test
    public void test9() throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (int i = 0; i < 10; i++) {
            Argon2Factory.create().hash(8, 65536, 1, "findIterations".toCharArray());
        }
        System.out.println(stopwatch.stop().toString());
    }
}
