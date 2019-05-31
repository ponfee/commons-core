package code.ponfee.commons.data;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;

/**
 * <pre>
 *  1、开启spring切面特性：<aop:aspectj-autoproxy />
 * 
 *  2、编写子类：
 *  {@code 
 *  
 *    `@Component `@Aspect
 *    public class MultipleDataSourceChanger extends MultipleDataSourceAspect {
 *      `@Around(
 *        value = "execution(public * cn.ponfee..*.service.impl..*Impl..*(..)) && `@annotation(dsn)", 
 *        argNames = "pjp,dsn"
 *      )
 *      `@Override
 *      public Object doAround(ProceedingJoinPoint pjp, DataSourceNaming dsn) throws Throwable {
 *        return super.doAround(pjp, dsn);
 *      }
 *    }
 * 
 *  }
 * 
 * 注意：与&lt;tx:advice id="txManageAdvice" transaction-manager="txManager">搭配无效，要结合`@Transactional使用，
 *     &lt;tx:annotation-driven proxy-target-class="true" transaction-manager="txManager" order="9" />
 * </pre>
 * 
 * @author Ponfee
 */
public abstract class MultipleDataSourceAspect implements Ordered {

    public Object doAround(ProceedingJoinPoint pjp, DataSourceNaming dsn) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        String name = MultipleDataSourceAdvisor.getDataSourceName(
            method, pjp.getArgs(), method.getAnnotation(DataSourceNaming.class)
        );
        boolean changed = false;
        try {
            if (StringUtils.isNotBlank(name)) {
                changed = true;
                MultipleDataSourceContext.set(name);
            }
            return pjp.proceed();
        } finally {
            if (changed) {
                MultipleDataSourceContext.clear();
            }
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
