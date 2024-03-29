//package cn.ponfee.commons;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import cn.ponfee.commons.json.Jsons;
//import cn.ponfee.commons.reflect.GenericUtils;
//import cn.ponfee.commons.spring.SpringContextHolder;
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
//    private static final Class<?>[] EXCLUDE_CLASSES = {Void.class, Object.class};
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
//        Class<T> type = GenericUtils.getActualTypeArgument(getClass(), 0);
//        if (!ArrayUtils.contains(EXCLUDE_CLASSES, type)) {
//            bean = StringUtils.isBlank(beanName)
//                 ? SpringContextHolder.getBean(type)
//                 : SpringContextHolder.getBean(beanName, type);
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
//    public static void consoleJson(Object obj) {
//        try {
//            Thread.sleep(100);
//            System.err.println(Jsons.toJson(obj));
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void console(Object obj) {
//        try {
//            Thread.sleep(100);
//            System.err.println(obj);
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}
