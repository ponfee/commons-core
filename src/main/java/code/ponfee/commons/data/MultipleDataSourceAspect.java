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
 *  1、transaction-xml：<aop:config proxy-target-class="true">
 *    MultipleDataSourceAspect.doAround ：数据源切换无效，事务正常×
 *
 *  2、transaction-xml：<aop:config proxy-target-class="false">
 *    MultipleDataSourceAspect.doAround ：数据源切换正常，事务正常√
 *
 *  3、<tx:annotation-driven proxy-target-class="true">
 *    MultipleDataSourceAspect.doAround ：数据源切换正常，事务正常√
 *
 *  4、<tx:annotation-driven proxy-target-class="false">
 *    MultipleDataSourceAspect.doAround ：数据源切换正常，事务正常√
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
