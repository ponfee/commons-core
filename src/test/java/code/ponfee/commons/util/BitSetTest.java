package code.ponfee.commons.util;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class BitSetTest {

    public static void main(String[] args) {
        Random random = new Random();

        List<Integer> list = new ArrayList<>();
        int size = 10000000;
        BitSet bitSet = new BitSet();
        for (int i = 0; i < size; i++) {
            int randomResult = random.nextInt(size);
            list.add(randomResult);
            bitSet.set(randomResult);
        }

        System.out.println("0~1亿不在上述随机数中有" + bitSet.size());
        for (int i = 0; i < size; i++) {
            if (!bitSet.get(i)) {
                System.out.println(i);
            }
        }
    }
}