package test.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.stream.LongStream;

/**
 * @author ponfee.fu
 */
public class NumbersTest {

    @Test
    public void testSplit() {
        long[] oldBillsPaid = {543L, -560L, 20L, 3200L, 20L, 0L};
        long[] oldBillsAmt = {100L, 3200L, 100L, -10000L};

        long total = LongStream.of(oldBillsPaid).sum();

        long[] split = split(oldBillsAmt, total);
        System.out.println("total: " + total);
        System.out.println("bills: " + Arrays.toString(oldBillsAmt));
        System.out.println("paid: " + Arrays.toString(split));
    }

    public static long[] split(long[] bills, long value) {
        long total = LongStream.of(bills).map(Math::abs).sum();

        long[] result = new long[bills.length];
        if (bills.length == 0 || value == 0) {
            return result;
        }

        double rate;
        int i = 0, n = bills.length - 1;
        for (; i < n; i++) {
            // rate <= 1.0
            rate = value / (double) total;

            // 因为result[i]是ceil后的结果，所以按比率上来算value减得会更多，即rate只会递减，所以不会出现溢出(后面的费用项不够抵扣)的情况
            result[i] = Math.min((int) Math.ceil(bills[i] * rate), value);
            value -= result[i];
            total -= bills[i];

            if (value == 0) {
                break;
            }
        }

        // the last bill item
        if (i == n) {
            result[i] = value;
        }
        return result;
    }

}
