package code.ponfee.commons.collects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import code.ponfee.commons.collect.ImmutableArrayList;
import code.ponfee.commons.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import code.ponfee.commons.collect.Collects;

/**
 * @author Ponfee
 */
public class ListTest {

    @Test
    public void test1() {
        List<Integer> list1 = new ArrayList<>();
        list1.add(1);
        list1.add(2);
        list1.add(3);

        List<Integer> list2 = new ArrayList<>();
        list2.add(3);
        list2.add(4);
        list2.add(5);

        System.out.println("====求交集===");
        System.out.println(Collects.intersect(list1, list2));

        System.out.println("====求差集===");
        System.out.println(Collects.different(list1, list2));

        System.out.println("====求并集===");
        System.out.println(Collects.union(list1, list2));

        System.out.println();
        System.out.println(list1);
        System.out.println(list2);


        Map<String, Object> map1 = Maps.toMap("a", 1, "b", 2);
        Map<String, Object> map2 = Maps.toMap("c", 3, "b", 2);
        System.out.println(Collects.different(map1, map2));
    }

    @Test
    public void test2() {
        ImmutableArrayList.of(); // Object[0]
        ImmutableArrayList.of((String) null); // Object[] { null }
        //ImmutableList.of((String[]) null); // null

        List<String> list1 = new ArrayList<>();
        list1.add("s");
        System.out.println(list1.toArray().getClass()); // class [Ljava.lang.Object;

        List<String> list2 = ImmutableArrayList.of("s");
        System.out.println(list2.toArray().getClass()); // class [Ljava.lang.String;

        System.out.println(ArrayUtils.addAll(new String[0], "s").getClass()); // class [Ljava.lang.String;
        System.out.println(ArrayUtils.addAll(new Object[0], "s").getClass()); // class [Ljava.lang.Object;
        System.out.println(Arrays.toString(ArrayUtils.addAll(new String[0], "s"))); // [s]

        ImmutableArrayList.of(1, 2, 3);
        ImmutableArrayList.of("a");

    }

    @Test
    public void test3() {
        ImmutableArrayList.of(new String[]{"a"}, "b");
    }
}
