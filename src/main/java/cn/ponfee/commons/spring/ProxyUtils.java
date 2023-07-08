package cn.ponfee.commons.spring;

import cn.ponfee.commons.reflect.Fields;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.support.AopUtils;

/**
 * Spring proxy utils
 *
 * @author Ponfee
 */
public class ProxyUtils {

    /**
     * Returns the proxy target object
     *
     * @param object the object
     * @return target object
     * @throws Exception
     */
    public static Object getTargetObject(Object object) throws Exception {
        if (!AopUtils.isAopProxy(object)) {
            return object;
        }
        if (object instanceof Advised) {
            return ((Advised) object).getTargetSource().getTarget();
        }
        if (AopUtils.isJdkDynamicProxy(object)) {
            return getProxyTargetObject(Fields.get(object, "h"));
        }
        if (AopUtils.isCglibProxy(object)) {
            return getProxyTargetObject(Fields.get(object, "CGLIB$CALLBACK_0"));
        }
        return object;
    }

    private static Object getProxyTargetObject(Object proxy) throws Exception {
        AdvisedSupport advisedSupport = (AdvisedSupport) Fields.get(proxy, "advised");
        return advisedSupport.getTargetSource().getTarget();
    }

}
