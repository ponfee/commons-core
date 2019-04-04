package test.utils;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.springframework.util.Base64Utils;

import code.ponfee.commons.collect.Collects;
import code.ponfee.commons.util.Base64UrlSafe;
import code.ponfee.commons.util.SecureRandoms;

/**
 * @author Ponfee
 */
public class Test2 {
    private static final byte[] data = SecureRandoms.nextBytes(10001);

    @Test
    public void test1() {
        for (int i = 0; i < 999999; i++) {
            java.util.Base64.getEncoder().encodeToString(data);
        }
    }
    
    @Test
    public void test2() {
        for (int i = 0; i < 999999; i++) {
            Base64Utils.encodeToString(data);
        }
    }

    @Test
    public void test3() {
        String s = Base64UrlSafe.encode(data);
        System.out.println(Base64UrlSafe.decode(s));
    }
    
    @Test
    public void test4() {
        String[] s1 = {"1","2"};
        String[] s2 = {"2","3"};
        String[] s3 = Collects.intersect(s1, s2);
        System.out.println(Arrays.toString(s3));
    }
    
    @Test
    public void test5() {
        System.out.println(Stream.of(1,2,3,4).flatMap(i-> Stream.of(i*i, i*i*i)).collect(Collectors.toList()));
        
        System.out.println(Arrays.asList("word", "count").stream().map(w -> w.split("")).collect(Collectors.toList()));
        System.out.println(Arrays.asList("word", "count").stream().flatMap(w -> Stream.of(w.split(""))).collect(Collectors.toList()));
    }
    
    
    public static int leastFire(int num, int shotDegrade, int remDegrade, int health) {
        assert shotDegrade >= remDegrade;

        int[] healths = new int[num];
        Arrays.fill(healths, health);
        return fire(healths, shotDegrade, remDegrade);
    }

    private static int fire(int[] healths, int shotDegrade, int remDegrade) {
        Arrays.sort(healths);
        if (healths[healths.length - 1] == 0) {
            return 0;
        }

        for (int i = 0; i < healths.length; i++) {
            if (i == healths.length - 1) {
                healths[i] = Math.max(0, healths[i] - shotDegrade);
            } else {
                healths[i] = Math.max(0, healths[i] - remDegrade);
            }
        }
        return 1 + fire(healths, shotDegrade, remDegrade);
    }

}
