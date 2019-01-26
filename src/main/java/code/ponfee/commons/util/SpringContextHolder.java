package code.ponfee.commons.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <pre>
 * ContextLoaderListener的beanfactory是DispatcherServlet的parent
 * spring上下文无法访问spring mvc上下文，但spring mvc上下文却能访问spring上下文
 *   解决方案1：在DispatcherServlet配置bean aware，如<bean id="beanId" class="xx.BeanImpl"/>
 *   解决方案2：Set<ApplicationContext>
 * </pre>
 * 
 * spring上下文持有类
 * @author fupf
 */
public class SpringContextHolder implements ApplicationContextAware/*, BeanFactoryAware*/, DisposableBean {

    private static final List<ApplicationContext> HOLDER = new ArrayList<>();
    //private static final List<BeanFactory> BEAN_FACTORY_HOLDER = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext cxt) throws BeansException {
        synchronized (SpringContextHolder.class) {
            if (!HOLDER.contains(cxt)) {
                HOLDER.add(cxt);
            }
        }
    }

    /*@Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        synchronized (SpringContextHolder.class) {
            if (!BEAN_FACTORY_HOLDER.contains(beanFactory)) {
                BEAN_FACTORY_HOLDER.add(beanFactory);
            }
        }
    }*/

    /**
     * 通过名称获取bean
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        //assertContextInjected();
        BeansException ex = null;
        for (ApplicationContext c : HOLDER) {
            try {
                Object bean = c.getBean(name);
                if (bean != null) {
                    return bean;
                }
            } catch (BeansException e) {
                ex = e;
            }
        }
        return throwOrReturn(ex, null);
    }

    /**
     * 通过类获取bean
     * @param clazz
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        //assertContextInjected();
        BeansException ex = null;
        for (ApplicationContext c : HOLDER) {
            try {
                T bean = c.getBean(clazz);
                if (bean != null) {
                    return bean;
                }
            } catch (BeansException e) {
                ex = e;
            }
        }
        return throwOrReturn(ex, null);
    }

    /**
     * @param name
     * @param clazz
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        //assertContextInjected();
        BeansException ex = null;
        for (ApplicationContext c : HOLDER) {
            try {
                T bean = c.getBean(name, clazz);
                if (bean != null) {
                    return bean;
                }
            } catch (BeansException e) {
                ex = e;
            }
        }
        return throwOrReturn(ex, null);
    }

    /**
     * 判断是否含有该名称的Bean
     * @param name
     * @return
     */
    public static boolean containsBean(String name) {
        //assertContextInjected();
        for (ApplicationContext c : HOLDER) {
            if (c.containsBean(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断Bean是否单例
     * @param name
     * @return
     */
    public static boolean isSingleton(String name) {
        //assertContextInjected();
        NoSuchBeanDefinitionException ex = null;
        for (ApplicationContext c : HOLDER) {
            try {
                if (c.isSingleton(name)) {
                    return true;
                }
            } catch (NoSuchBeanDefinitionException e) {
                ex = e;
            }
        }
        return throwOrReturn(ex, false);
    }

    /**
     * 获取Bean的类型
     * @param name
     * @return
     */
    public static Class<?> getType(String name) {
        //assertContextInjected();
        NoSuchBeanDefinitionException ex = null;
        for (ApplicationContext c : HOLDER) {
            try {
                if (c.getType(name) != null) {
                    return c.getType(name);
                }
            } catch (NoSuchBeanDefinitionException e) {
                ex = e;
            }
        }
        return throwOrReturn(ex, null);
    }

    /**
     * 获取bean的别名
     * @param name
     * @return
     */
    public static String[] getAliases(String name) {
        //assertContextInjected();
        for (ApplicationContext c : HOLDER) {
            String[] aliases = c.getAliases(name);
            if (aliases != null) {
                return aliases;
            }
        }

        return null;
    }

    @Override
    public void destroy() {
        synchronized (SpringContextHolder.class) {
            HOLDER.clear();
        }
    }

    private static <T> T throwOrReturn(BeansException ex, T t) {
        if (ex == null) {
            return t;
        } else {
            throw ex;
        }
    }

    /**
     * 检查ApplicationContext不为空.
     */
    //private static void assertContextInjected() {
    //    Assert.state(HOLDER.size() > 0, "must be defined SpringContextHolder within spring config file.");
    //}
}
