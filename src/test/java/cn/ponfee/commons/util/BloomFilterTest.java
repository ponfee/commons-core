package cn.ponfee.commons.util;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

public class BloomFilterTest {

    static int sizeOfNumberSet = Integer.MAX_VALUE >> 10;

    static Random generator = new Random();

    public static void main(String[] args) {

        int error = 0;
        HashSet<Integer> hashSet = new HashSet<Integer>();
        BloomFilter<Integer> filter = BloomFilter.create(Funnels.integerFunnel(), sizeOfNumberSet, 0.000001);

        for(int i = 0; i < sizeOfNumberSet; i++) {
            int number = generator.nextInt();
            if(filter.mightContain(number) != hashSet.contains(number)) {
                error++;
            }
            filter.put(number);
            hashSet.add(number);
        }

        System.out.println("Error count: " + error + ", error rate = " + String.format("%f", (float)error/(float)sizeOfNumberSet));
    }
}
