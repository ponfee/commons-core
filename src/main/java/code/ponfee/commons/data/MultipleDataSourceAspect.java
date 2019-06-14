package code.ponfee.commons.data;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;

import code.ponfee.commons.exception.CheckedThrowing;

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
        return MultipleDataSourceAdvisor.around(
            ((MethodSignature) pjp.getSignature()).getMethod(), 
            pjp.getArgs(), dsn, 
            CheckedThrowing.callable(() -> pjp.proceed())
        );
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
