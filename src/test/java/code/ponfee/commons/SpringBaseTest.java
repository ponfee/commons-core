package code.ponfee.commons;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.reflect.GenericUtils;
import code.ponfee.commons.util.SpringContextHolder;

/**
 * 测试基类
 * @author Ponfee
 * @param <T>
 */
@RunWith(SpringRunner.class) // SpringJUnit4ClassRunner.class
@ContextConfiguration(locations = { "classpath:spring-context.xml" })
public abstract class SpringBaseTest<T> {

    private T bean;
    private final String beanName;

    public SpringBaseTest() {
        this(null);
    }

    public SpringBaseTest(String beanName) {
        this.beanName = beanName;
    }

    protected final T getBean() {
        return bean;
    }

    @Before
    public final void setUp() {
        Class<T> type = GenericUtils.getActualTypeArgument(this.getClass());
        if (Object.class != type) {
            if (beanName != null && beanName.length() > 0) {
                bean = SpringContextHolder.getBean(beanName, type);
            } else {
                bean = SpringContextHolder.getBean(type);
            }
        }
        initialize();
    }

    @After
    public final void tearDown() {
        destroy();
    }

    protected void initialize() {
        // do no thing
    }

    protected void destroy() {
        // do no thing
    }

    public static void consoleJson(Object obj) {
        try {
            Thread.sleep(100);
            System.err.println(Jsons.toJson(obj));
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void console(Object obj) {
        try {
            Thread.sleep(100);
            System.err.println(obj);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
