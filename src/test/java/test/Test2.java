/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.objenesis.ObjenesisHelper;

import com.google.common.base.Stopwatch;

import code.ponfee.commons.collect.ValueSortedMap;
import code.ponfee.commons.io.HumanReadables;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.math.Maths;
import code.ponfee.commons.model.Page;
import code.ponfee.commons.reflect.CglibUtils;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.Dates;
import code.ponfee.commons.util.IdWorker;

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

    @SuppressWarnings("rawtypes")
    @Test
    public void test2() {
        Page<Map<String, Object>> source = new Page<>();
        Page<Map> target = new Page<>();
        CglibUtils.copyProperties(source, target);
        System.out.println(source.copy());
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

    @Test @Ignore
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
    public void test10() throws IOException {
        Map<String, Object> map = new HashMap<>();
        System.out.println(map.replace("xx", 1));
        System.out.println(map);
        System.out.println(map.put("a", 1));
        System.out.println(map);
        System.out.println(map.put("a", 2));
        System.out.println(map);
    }

    @Test @Ignore
    public void test11() throws IOException {
        File source = new File("D:\\test\\test1\\CentOS-6.6-x86_64-bin-DVD1.iso");
        File target = new File("D:\\test\\test1\\CentOS-6.6-x86_64-bin-DVD2.iso");

        Stopwatch watch = Stopwatch.createStarted();
        Files.move(source.toPath(), target.toPath());
        System.out.println("move cost: " + watch.stop());

        watch.reset().start();
        source = target;
        target = new File("D:\\test\\test1\\CentOS-6.6-x86_64-bin-DVD3.iso");
        boolean f = source.renameTo(target);
        System.out.println(f);
        System.out.println("move cost: " + watch.stop());
    }
    
    @Test
    public void test13() throws IOException {
        System.out.println(HumanReadables.BINARY.parse("-1,023.56 GiB"));
        System.out.println(HumanReadables.BINARY.human(-1099039181373L).length());
        long size = 1, max = 0;
        for (int i = 0; i < 100; i++) {
            String s = HumanReadables.BINARY.human(size);
            System.out.println(s);
            max = Math.max(max, s.length());
            size *= 7;
            //Assert.assertEquals(s, HumanReadables.BINARY.human(size).replace("i", ""));
            //System.out.println(HumanReadables.BINARY.parse(s, false));
        }
        System.out.println("======="+max);
    }

    @Test
    public void test14() throws IOException {
        System.out.println(HumanReadables.BINARY.human(1047552));
        System.out.println(HumanReadables.BINARY.human(123456789123456L));
        System.out.println(HumanReadables.BINARY.human(999_949_999_999_999_999L));
        System.out.println(HumanReadables.BINARY.human(-12345678));
        System.out.println(HumanReadables.BINARY.human(0));
        System.out.println(HumanReadables.BINARY.human(-0));
        System.out.println(HumanReadables.BINARY.human(5));
        System.out.println(HumanReadables.BINARY.human(-5));
        System.out.println(HumanReadables.BINARY.human(98745612));

        System.out.println("\n===============================");
        System.out.println(HumanReadables.BINARY.parse("1,023KB"));
        System.out.println(HumanReadables.BINARY.parse("1047552"));
        System.out.println(HumanReadables.BINARY.parse("112.28TB"));
        System.out.println(HumanReadables.BINARY.parse("888.13PB"));
        System.out.println(HumanReadables.BINARY.parse("-11.77MB"));
        System.out.println(HumanReadables.BINARY.parse("0B"));
        System.out.println(HumanReadables.BINARY.parse("5B"));
        System.out.println(HumanReadables.BINARY.parse("-5B"));
        System.out.println(HumanReadables.BINARY.parse("94.17MB"));
        System.out.println(HumanReadables.BINARY.parse("-1KB"));
        System.out.println(HumanReadables.BINARY.parse("0B"));
        System.out.println(HumanReadables.BINARY.parse("-0B"));
        System.out.println(HumanReadables.BINARY.parse("123B"));
        System.out.println(HumanReadables.BINARY.parse("-123B"));
        System.out.println(HumanReadables.BINARY.parse("6MB"));
    }

    @Test
    public void test15() throws IOException {
        System.out.println(HumanReadables.BINARY.parse("888.13   PiB"));
        System.out.println(HumanReadables.BINARY.parse("888.13PiB", true));

        System.out.println(HumanReadables.BINARY.parse("888.13PB"));
        //System.out.println(HumanReadables.BINARY.parse("888.13PB", true));
    }
    
    @Test
    public void test16() throws IOException {
        System.out.println(HumanReadables.BINARY.parse("888.13PiB", true));
        //System.out.println(HumanReadables.BINARY.parse("888.13PB", true));
    }

    @Test
    public void test18() throws IOException {
        System.out.println(Long.MAX_VALUE);
        System.out.println(HumanReadables.SI.human(Long.MAX_VALUE));
        System.out.println(HumanReadables.BINARY.human(Long.MAX_VALUE));

        System.out.println(HumanReadables.SI.parse("9.223372036854776 EB", true));
        System.out.println(HumanReadables.BINARY.parse("8EiB", true));
    }

    @Test
    public void test19() throws IOException {
        System.out.println(HumanReadables.SI.human(Long.MIN_VALUE));
        System.out.println(HumanReadables.BINARY.human(Long.MIN_VALUE));

        System.out.println(HumanReadables.SI.parse("-9.22EB", true));
        System.out.println(HumanReadables.BINARY.parse("-8EiB", true));
        System.out.println(Long.MIN_VALUE);
    }

    @Test
    public void test20() throws IOException {
        System.out.println(HumanReadables.SI.parse("888.13P", false));
        System.out.println(HumanReadables.SI.parse("888.13P", true));

        //System.out.println(HumanReadables.BINARY.parse("888.13Pi", false));
        //System.out.println(HumanReadables.BINARY.parse("888.13Pi", true));

        System.out.println(HumanReadables.BINARY.parse("888.13P", false));
        //System.out.println(HumanReadables.BINARY.parse("888.13P", true));
    }
    
    @Test
    public void test21() throws IOException {
        System.out.println(HumanReadables.SI.parse("888.13", false));
        System.out.println(HumanReadables.SI.parse("888.13", true));

        System.out.println(HumanReadables.SI.parse("888.13B", false));
        System.out.println(HumanReadables.SI.parse("888.13B", true));

        System.out.println(HumanReadables.SI.parse("888.13PB", false));
        System.out.println(HumanReadables.SI.parse("888.13PB", true));

        //System.out.println(HumanReadables.SI.parse("888.13PiB", false));
        //System.out.println(HumanReadables.SI.parse("888.13PiB", true));

        System.out.println(HumanReadables.SI.parse("888.13P", false));
        System.out.println(HumanReadables.SI.parse("888.13P", true));
    }
    
    @Test
    public void test22() throws IOException {
        System.out.println(HumanReadables.BINARY.parse("888.13", false));
        System.out.println(HumanReadables.BINARY.parse("888.13", true));
        
        System.out.println(HumanReadables.BINARY.parse("888.13B", false));
        System.out.println(HumanReadables.BINARY.parse("888.13B", true));

        System.out.println(HumanReadables.BINARY.parse("888.13PB", false));
        //System.out.println(HumanReadables.BINARY.parse("888.13PB", true));

        System.out.println(HumanReadables.BINARY.parse("888.13PiB", false));
        System.out.println(HumanReadables.BINARY.parse("888.13PiB", true));

        System.out.println(HumanReadables.BINARY.parse("888.13P", false));
        //System.out.println(HumanReadables.BINARY.parse("888.13P", true));
    }
    
    @Test
    public void test23() throws IOException {
        //System.out.println(HumanReadables.BINARY.parse("888.13AB", false));
        //System.out.println(HumanReadables.BINARY.parse("888.13BA", true));

        //System.out.println(HumanReadables.SI.parse("888.13AB", false));
        //System.out.println(HumanReadables.SI.parse("888.13BA", true));

    }
    
    @Test
    public void test24() throws IOException {
        System.out.println(FileUtils.byteCountToDisplaySize(1047552));
        System.out.println(FileUtils.byteCountToDisplaySize(123456789123456L));
        System.out.println(FileUtils.byteCountToDisplaySize(999_949_999_999_999_999L));
        System.out.println(FileUtils.byteCountToDisplaySize(-12345678));
        System.out.println(FileUtils.byteCountToDisplaySize(0));
        System.out.println(FileUtils.byteCountToDisplaySize(-0));
        System.out.println(FileUtils.byteCountToDisplaySize(5));
        System.out.println(FileUtils.byteCountToDisplaySize(-5));
        System.out.println(FileUtils.byteCountToDisplaySize(98745612));
    }
    
    @Test
    public void test25() throws IOException {
        System.out.println(HumanReadables.BINARY.human(1099382778757L));
        System.out.println(HumanReadables.BINARY.parse("-1,023.88GiB ", false));
        System.out.println(HumanReadables.BINARY.parse("-1,023.88   GiB  ", false));
        System.out.println(HumanReadables.BINARY.parse("-1,023.88    B", false));
        //System.out.println(HumanReadables.BINARY.parse("-1,023.88  Gi B", false));
        //System.out.println(HumanReadables.BINARY.parse("-1,023.88   G iB", false));
        //System.out.println(HumanReadables.BINARY.parse("-B", false));
        //System.out.println(HumanReadables.BINARY.parse(".B", false));
        //System.out.println(HumanReadables.BINARY.parse("TB", false));
        //System.out.println(HumanReadables.BINARY.parse("xTB", false));
    }

    @Test
    public void test26() throws IOException {
        String format = "yyyy-MM-dd HH:mm:ss.SSS";
        System.out.println(Long.toBinaryString(Long.MAX_VALUE));
        System.out.println(Long.toBinaryString(System.currentTimeMillis()));
        System.out.println(Bytes.toBinary(Bytes.toBytes(Long.MAX_VALUE)));
        System.out.println(Dates.format(new Date(0B0000000000000000000000011111111111111111111111111111111111111111L), format));
        System.out.println(Dates.format(new Date(0B0000000000000000000011111111111111111111111111111111111111111111L), format));
        System.out.println(Long.toBinaryString(Long.MAX_VALUE).length());
        System.out.println(Maths.bitsMask(12));

        System.out.println(Dates.format(new Date(17592186044415L), format));
        new IdWorker(10);

        int bits = 41;
        System.out.println((1L << bits) - 1);
        Assert.assertEquals((1L << bits) - 1, -1L ^ (-1L << bits));
        Assert.assertEquals((1L << bits) - 1, Long.MAX_VALUE >>> (63 - bits));

        System.out.println(Long.toBinaryString((1L << bits) - 1));
        System.out.println(Long.toBinaryString(-1L ^ (-1L << bits)));
        System.out.println(Long.toBinaryString(Long.MAX_VALUE >>> (63 - 41)));
    }

}
