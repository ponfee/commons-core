package code.ponfee.commons.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.reflect.Fields;

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
        Object bean;
        for (ApplicationContext c : HOLDER) {
            try {
                if ((bean = c.getBean(name)) != null) {
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
        T bean;
        for (ApplicationContext c : HOLDER) {
            try {
                if ((bean = c.getBean(clazz)) != null) {
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
        T bean;
        for (ApplicationContext c : HOLDER) {
            try {
                if ((bean = c.getBean(name, clazz)) != null) {
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
        Class<?> type;
        for (ApplicationContext c : HOLDER) {
            try {
                if ((type = c.getType(name)) != null) {
                    return type;
                }
            } catch (NoSuchBeanDefinitionException e) {
                ex = e;
            }
        }
        return throwOrReturn(ex, null);
    }

    /**
     * 获取bean的别名
     * 
     * @param name
     * @return
     */
    public static String[] getAliases(String name) {
        //assertContextInjected();
        String[] aliases;
        for (ApplicationContext c : HOLDER) {
            if ((aliases = c.getAliases(name)) != null) {
                return aliases;
            }
        }

        return null;
    }

    /**
     * Returns a map that conatain spec annotation beans
     * 
     * @param annotationType the Annotation type
     * @return a map
     */
    public static Map<String, Object> getBeansWithAnnotation(
        Class<? extends Annotation> annotationType) {
        //assertContextInjected();
        BeansException ex = null;
        Map<String, Object> map;
        for (ApplicationContext c : HOLDER) {
            try {
                if ((map = c.getBeansWithAnnotation(annotationType)) != null) {
                    return map;
                }
            } catch (BeansException e) {
                ex = e;
            }
        }
        return throwOrReturn(ex, null);
    }

    /**
     * Auto injects the field from spring container for object
     * 
     * @param object the object
     */
    public static void autoInject(Object object) {
        Assert.state(HOLDER.size() > 0, "Must be defined SpringContextHolder within spring config file.");

        for (Field field : ClassUtils.listFields(object.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            Object fieldBean = null;
            if (AnnotationUtils.getAnnotation(field, Resource.class) != null) {
                Resource resource = AnnotationUtils.getAnnotation(field, Resource.class);
                if (StringUtils.isNotBlank(resource.name())) {
                    fieldBean = getBean(resource.name());
                } else {
                    fieldBean = getBean(field.getName());
                }
                if (fieldBean == null) {
                    fieldBean = getBean(field.getType());
                }
            } else if (field.getType().isAnnotationPresent(Autowired.class)) {
                Qualifier qualifier = AnnotationUtils.getAnnotation(field, Qualifier.class);
                if (qualifier != null && StringUtils.isNotBlank(qualifier.value())) {
                    fieldBean = getBean(qualifier.value());
                } else {
                    fieldBean = getBean(field.getType());
                }
            }

            if (fieldBean != null && field.getType().isInstance(fieldBean)) {
                Fields.put(object, field, fieldBean);
            }
        }
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

    //private static void assertContextInjected() {
    //    Assert.state(HOLDER.size() > 0, "must be defined SpringContextHolder within spring config file.");
    //}
}
