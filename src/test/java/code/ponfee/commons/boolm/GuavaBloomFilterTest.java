package code.ponfee.commons.boolm;

import java.util.ArrayList;
import java.util.List;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

public class GuavaBloomFilterTest {

    public static void main(String[] args) {
        BloomFilter<Integer> filter = BloomFilter.create(
            //(from, into) -> into.putString(from, Charsets.UTF_8),
            Funnels.integerFunnel(),
            1024 * 1024 * 32, 
            0.000000001d
        );
        
        /*filter.test("234");
        filter.put("abc");
        filter.mightContain("123");
        filter.isCompatible("abc");*/

        int size = 1000000;
        for (int i = 0; i < size; i++) {
            filter.put(i);
        }

        for (int i = 0; i < size; i++) {
            if (!filter.mightContain(i)) {
                System.out.println("有坏人逃脱了");
            }
        }

        List<Integer> list = new ArrayList<>(1000);
        for (int i = size + 10000; i < size + 20000; i++) {
            if (filter.mightContain(i)) {
                list.add(i);
            }
        }
        System.out.println("有误伤的数量：" + list.size());
    }

}
