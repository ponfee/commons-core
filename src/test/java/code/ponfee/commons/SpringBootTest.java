//package code.ponfee.commons;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import code.ponfee.commons.json.Jsons;
//import code.ponfee.commons.reflect.GenericUtils;
//import code.ponfee.commons.util.SpringContextHolder;
//
///**
// * 测试基类
// *
// * @param <T>
// * @author Ponfee
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public abstract class SpringBootBaseTest<T> {
//
//    private T bean;
//    private final String beanName;
//
//    public SpringBootBaseTest() {
//        this(null);
//    }
//
//    public SpringBootBaseTest(String beanName) {
//        this.beanName = beanName;
//    }
//
//    protected final T getBean() {
//        return bean;
//    }
//
//    @Before
//    public final void setUp() {
//        Class<T> type = GenericUtils.getActualTypeArgument(this.getClass());
//        if (Object.class != type) {
//            if (beanName != null && beanName.length() > 0) {
//                bean = SpringContextHolder.getBean(beanName, type);
//            } else {
//                bean = SpringContextHolder.getBean(type);
//            }
//        }
//        initiate();
//    }
//
//    @After
//    public final void tearDown() {
//        destory();
//    }
//
//    protected void initiate() {
//        // do no thing
//    }
//
//    protected void destory() {
//        // do no thing
//    }
//
//    public static void printJson(Object obj) {
//        try {
//            Thread.sleep(100);
//            System.err.println(Jsons.toJson(obj));
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void print(Object obj) {
//        try {
//            Thread.sleep(100);
//            System.err.println(obj);
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}
