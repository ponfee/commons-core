package cn.ponfee.commons.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FibonacciTest {

    public static int fibonacci(int i) {
        return (i == 1 || i == 2) ? 1 : fibonacci(i - 1) + fibonacci(i - 2);
    }

    public static void main(String[] args) {
        BigDecimal previous = new BigDecimal(1), following = new BigDecimal(1), temp;
        for (int i = 1; i < 1000; i++) {
            System.out.println(previous.divide(following, 1000, RoundingMode.HALF_UP).toString());
            temp = following;
            following = following.add(previous);
            previous = temp;
        }
    }

}
