package cn.ponfee.commons.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

/**
 * @author Ponfee
 */
public class ProxyTest {

    @Test
    public void test() {
        User user = LazyLoader.of(User.class, ProxyTest::findById, 1);
        System.out.println("------222");
        System.out.println("bean1: " + System.identityHashCode(user) + ", " + user.getClass());
        System.out.println("---------user.getId()");
        System.out.println(user.getId());
        System.out.println(user.getName());
        System.out.println(user.getSex());
        System.out.println(user.toString());
    }

    public static User findById(Integer id) {
        System.out.println("-------bbb");
        return new User(1, "bob", "男");
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        private Integer id;
        private String name;
        private String sex;
    }
    
    
    
    //-------
    @Test
    public void test2() {
        TargetBean bean2 = (TargetBean) createProxyBean(new TargetBean());
        System.out.println("bean1: " + System.identityHashCode(bean2) + ", " + bean2.getClass());
        bean2.say();
    }

    /**
     * 创建代理类
     *
     * @param targetBean 源Bean
     * @return 代理Bean
     */
    private Object createProxyBean(final Object targetBean) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetBean.getClass());
        enhancer.setUseCache(true);
        enhancer.setInterceptDuringConstruction(false);
        enhancer.setCallback((MethodInterceptor) (bean1, method, args, methodProxy) -> {
            System.out.println("bean1: " + System.identityHashCode(bean1) + ", " + bean1.getClass());
            System.err.println("代理前");
            Object result = methodProxy.invokeSuper(bean1, args);
            System.err.println("代理后");
            return result;
        });
        Object targetProxyBean = enhancer.create();
        return targetProxyBean;
    }
    public static  class TargetBean{
        public TargetBean(){
            
        }
        public void say() {
            System.err.println("Hello");
        }
    }
    
}
