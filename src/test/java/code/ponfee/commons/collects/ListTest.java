package code.ponfee.commons.collects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        
        
        Map<String, Object> map1 = Collects.toMap("a", 1, "b", 2);
        Map<String, Object> map2 = Collects.toMap("c", 3, "b", 2);
        System.out.println(Collects.different(map1, map2));
    }
}
