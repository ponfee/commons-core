package code.ponfee.commons.exception;

import code.ponfee.commons.util.ImageUtils;
import org.junit.Test;

import java.io.FileInputStream;

/**
 * @author Ponfee
 */
public class ThrowablesTest {

    @Test
    public void test() {
        Throwables.caught(ThrowablesTest::get0);
        System.out.println("---------------\n");
        
        Throwables.caught(ThrowablesTest::get1);
        System.out.println("---------------\n");

        String caught = Throwables.caught(ThrowablesTest::get2);
        System.out.println("---------------\n");

        Throwables.caught(ThrowablesTest::get3, "xxx");
        System.out.println("---------------\n");

        String yyy = Throwables.caught(ThrowablesTest::get4, "yyy");
        System.out.println("---------------\n");
    }

    public static void get0() {
        System.out.println("get0");
        int i = 1/0;
    }
    
    public static void get1() throws Throwable {
        System.out.println("get1");
        ImageUtils.getImageType(new FileInputStream(""));
    }

    public static String get2() throws Throwable {
        System.out.println("get2");
        ImageUtils.getImageType(new FileInputStream(""));
        return "";
    }

    public static void get3(String a) throws Throwable {
        System.out.println("get3:" + a);
        ImageUtils.getImageType(new FileInputStream(""));
    }

    public static String get4(String a) throws Throwable {
        System.out.println("get4:" + a);
        ImageUtils.getImageType(new FileInputStream(""));
        return a;
    }

}
