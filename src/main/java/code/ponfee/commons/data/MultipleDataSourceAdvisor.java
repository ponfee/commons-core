package code.ponfee.commons.data;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import code.ponfee.commons.data.lookup.MultipleDataSourceContext;
import code.ponfee.commons.exception.CheckedThrowing.ThrowingCallable;

/**
 * 多数据源切换，用于Spring XML配置文件形式的切面拦截多数据源切换处理
 * 
 * @author Ponfee
 */
public class MultipleDataSourceAdvisor implements MethodInterceptor {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    /**
     * 基于Spring XML &lt;aop:aspect /&gt;的配置方式
     * 
     * <pre>
     * {@code
     *   <!-- aop:advisor必须在aop:aspect前面 -->
     *   <bean id="dsChangeAdvice" class="code.ponfee.commons.data.MultipleDataSourceAdvisor" />
     *   <aop:config proxy-target-class="true">
     *     <aop:pointcut id="dbTxMgrPointcut" expression="execution(public * cn.ponfee..*.service.impl..*..*(..))" />
     *     <aop:advisor advice-ref="txManageAdvice" pointcut-ref="dbTxMgrPointcut" order="9" />
     *     <aop:aspect ref="dsChangeAdvice" order="0">
     *       <aop:around method="doAround" pointcut-ref="dbTxMgrPointcut" />
     *     </aop:aspect>
     *   </aop:config>
     * }
     * 
     * transaction-xml       ：数据源切换正常(doAround执行六次)，事务正常
     * transaction-annotation：数据源切换正常(doAround执行六次)，事务正常
     * </pre>
     * 
     * @param pjp the ProceedingJoinPoint
     * @return target method return result
     * @throws Throwable if occur error
     */
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        /*MethodSignature ms = (MethodSignature) pjp.getSignature();
        // DatabaseQueryServiceImpl.query4page(PageRequestParams)
        System.out.println(ms.getMethod());

        // DatabaseQueryServiceImpl$$EnhancerBySpringCGLIB$$ca6c0b12.query4page(PageRequestParams)
        System.out.println(pjp.getTarget().getClass().getMethod(ms.getName(), ms.getParameterTypes()));*/

        return around(
            ((MethodSignature) pjp.getSignature()).getMethod(), 
            pjp.getArgs(), 
            ThrowingCallable.checked(() -> pjp.proceed())
        );
    }

    /**
     * 基于Spring XML &lt;aop:advisor /&gt;的配置方式
     * <pre>
     * {@code
     *   <bean id="dsChangeAdvice" class="code.ponfee.commons.data.MultipleDataSourceAdvisor" />
     *   <aop:config proxy-target-class="true">
     *     <aop:pointcut id="dbTxMgrPointcut" expression="execution(public * cn.ponfee..*.service.impl..*..*(..))" />
     *     <aop:advisor advice-ref="dsChangeAdvice" pointcut-ref="dbTxMgrPointcut" order="0" />
     *     <aop:advisor advice-ref="txManageAdvice" pointcut-ref="dbTxMgrPointcut" order="9" />
     *   </aop:config>
     * }
     * transaction-xml       ：数据源切换正常(invoke  执行六次)，事务无效
     * transaction-annotation：数据源切换正常(invoke  执行六次)，事务无效
     * </pre>
     * 
     * @param invocation the MethodInvocation
     * @return target method return result
     * @throws Throwable if occur error
     * 
     * @deprecated 此方式事务失效（去掉此数据源切面，则事务正常）
     */
    @Override @Deprecated
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object[] args = invocation.getArguments();
        return around(method, args, () -> method.invoke(invocation.getThis(), args));
    }

    public static Object around(Method method, Object[] args, Callable<Object> call) throws Throwable {
        return around(method, args, method.getAnnotation(DataSourceNaming.class), call);
    }

    public static Object around(Method method, Object[] args, DataSourceNaming dsn, 
                                Callable<Object> call) throws Throwable {
        String name = null;
        if (dsn != null && StringUtils.isNotBlank(dsn.value())) {
            name = PARSER.parseExpression(
                dsn.value()
            ).getValue(
                new StandardEvaluationContext(args), String.class
            );
        }

        boolean changed = false;
        try {
            if (StringUtils.isNotBlank(name)) {
                MultipleDataSourceContext.set(name);
                changed = true;
            }
            return call.call();
        } finally {
            if (changed) {
                MultipleDataSourceContext.clear();
            }
        }
    }

}
