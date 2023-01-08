package cn.ponfee.commons.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ForEachTest {

    public static void main(String[] args) {
        //foreach();
        forEachRemaining();
    }

    private static void foreach() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6);
        list.forEach(System.out::println);
        System.out.println("===================================");
        list.forEach(System.out::println);
    }

    private static void forEachRemaining() {
        Iterator<Integer> iter = Arrays.asList(1, 2, 3, 4, 5, 6).iterator();
        
        iter.forEachRemaining(System.out::println);
        System.out.println("===================================");
        iter.forEachRemaining(System.out::println);
    }
}
