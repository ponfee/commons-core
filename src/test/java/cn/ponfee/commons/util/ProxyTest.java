package cn.ponfee.commons.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.util.Arrays;

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


    //---------------------------------------------------------------------------------------

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
        System.out.println("targetBean: " + System.identityHashCode(targetBean) + ", " + targetBean.getClass());
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetBean.getClass());
        enhancer.setUseCache(true);
        enhancer.setInterceptDuringConstruction(false);
        enhancer.setCallback((MethodInterceptor) (proxy, method, args, methodProxy) -> {
            System.out.println("proxy: " + System.identityHashCode(proxy) + ", " + proxy.getClass());
            System.out.println("method: " + method.toGenericString());
            System.out.println("args: " + Arrays.toString(args));
            System.out.println("methodProxy: " + System.identityHashCode(methodProxy) + ", " + methodProxy.getClass());
            System.err.println("代理前");
            //System.out.println("method.invoke(bean1, args): " + method.invoke(bean1, args)); // bean1为代理类，此处会死循环
            //System.out.println("method.invoke(targetBean, args): " + method.invoke(targetBean, args));
            Object result = methodProxy.invokeSuper(proxy, args); // 调用父类方法
            System.err.println("代理后");
            return result;
        });
        Object targetProxyBean = enhancer.create();
        return targetProxyBean;
    }

    public static class TargetBean {
        public TargetBean() {

        }

        public void say() {
            System.err.println("Hello");
        }
    }

}
