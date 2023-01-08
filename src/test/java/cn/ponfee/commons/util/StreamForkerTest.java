package cn.ponfee.commons.util;

import java.util.stream.Stream;

import cn.ponfee.commons.collect.StreamForker;

public class StreamForkerTest {

    public static void main(String[] args) throws Exception {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4, 4, 5, 5);
        StreamForker.Results results = new StreamForker<>(stream)
                .fork(1, s -> s.max(Integer::compareTo)) // 直接聚合
                //.fork(2, s -> s.distinct().collect(Collectors.reducing(Integer::sum))) // 先收集再聚合
                //.fork(2, s -> s.distinct().collect(Collectors.summingInt(Integer::intValue))) // 先收集再聚合
                .fork(2, s -> s.distinct().reduce(0, Integer::sum))
                .getResults();
        System.out.println(results.get(1) + "");
        System.out.println(results.get(2) + "");
    }
}
