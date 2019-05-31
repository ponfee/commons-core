package code.ponfee.commons.data;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * 多数据源切换，用于Spring XML配置文件形式的切面拦截多数据源切换处理
 * 
 * @author Ponfee
 */
public class MultipleDataSourceAdvisor implements MethodInterceptor {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    /**
     * 基于Spring XML &lt;aop:aspect /&gt;方式的配置
     * 
     * <pre>{@code
     *   <!-- aop:advisor必须在aop:aspect前面 -->
     *   <bean id="dsChangeAdvice" class="code.ponfee.commons.data.MultipleDataSourceAdvisor" />
     *   <aop:config proxy-target-class="true">
     *     <aop:pointcut id="dbTxMgrPointcut" expression="execution(public * cn.ponfee..*.service.impl..*..*(..))" />
     *     <aop:advisor advice-ref="txManageAdvice" pointcut-ref="dbTxMgrPointcut" order="9" />
     *     <aop:aspect ref="dsChangeAdvice" order="0">
     *       <aop:around method="doAround" pointcut-ref="dbTxMgrPointcut" />
     *     </aop:aspect>
     *   </aop:config>
     * 
     * }</pre>
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

        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        String name = getDataSourceName(
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

    /**
     * 基于Spring XML &lt;aop:advisor /&gt;方式的配置
     * <pre>{@code
     * 
     *   <bean id="dsChangeAdvice" class="code.ponfee.commons.data.MultipleDataSourceAdvisor" />
     *   <aop:config proxy-target-class="true">
     *     <aop:pointcut id="dbTxMgrPointcut" 
     *       expression="execution(public * cn.ponfee..*.service.impl..*..*(..))" />
     *     <aop:advisor advice-ref="dsChangeAdvice" pointcut-ref="dbTxMgrPointcut" order="0" />
     *     <aop:advisor advice-ref="txManageAdvice" pointcut-ref="dbTxMgrPointcut" order="9" />
     *   </aop:config>
     * 
     * }</pre>
     * 
     * @param invocation the MethodInvocation
     * @return target method return result
     * @throws Throwable if occur error
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object[] args = invocation.getArguments();
        String name = getDataSourceName(
            method, args, method.getAnnotation(DataSourceNaming.class)
        );

        boolean changed = false;
        try {
            if (StringUtils.isNotBlank(name)) {
                changed = true;
                MultipleDataSourceContext.set(name);
            }
            return method.invoke(invocation.getThis(), args);
        } finally {
            if (changed) {
                MultipleDataSourceContext.clear();
            }
        }
    }

    public static String getDataSourceName(Method method, Object[] args, DataSourceNaming dsn) {
        if (dsn == null || StringUtils.isBlank(dsn.value())) {
            return null;
        }
        return PARSER.parseExpression(
            dsn.value()
        ).getValue(
            new StandardEvaluationContext(args), String.class
        );
    }

}
