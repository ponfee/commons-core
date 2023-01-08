package cn.ponfee.commons;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import cn.ponfee.commons.json.Jsons;
import cn.ponfee.commons.reflect.GenericUtils;
import cn.ponfee.commons.spring.SpringContextHolder;

/**
 * 测试基类
 * @author Ponfee
 * @param <T>
 */
@RunWith(SpringRunner.class) // SpringJUnit4ClassRunner.class
@ContextConfiguration(locations = { "classpath:spring-context.xml" })
public abstract class SpringBaseTest<T> {
    private static final Class<?>[] EXCLUDE_CLASSES = {Void.class, Object.class};

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
        Class<T> type = GenericUtils.getActualTypeArgument(getClass(), 0);
        if (!ArrayUtils.contains(EXCLUDE_CLASSES, type)) {
            bean = StringUtils.isBlank(beanName)
                    ? SpringContextHolder.getBean(type)
                    : SpringContextHolder.getBean(beanName, type);
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
