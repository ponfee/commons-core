package code.ponfee.commons.data;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * 多数据源切换，切面拦截处理
 * 
 * @author Ponfee
 */
public class MultipleDataSourceChanger implements MethodInterceptor {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object[] args = invocation.getArguments();


        boolean changed = false;
        DataSourceNaming dsn = method.getAnnotation(DataSourceNaming.class);
        try {
            if (dsn != null && StringUtils.isNotBlank(dsn.value())) {
                String name = PARSER.parseExpression(dsn.value()).getValue(
                    new StandardEvaluationContext(args), String.class
                );
                MultipleDataSourceContext.set(name);
                changed = true;
            }
            return method.invoke(invocation.getThis(), args);
        } finally {
            if (changed) {
                MultipleDataSourceContext.remove();
            }
        }
    }

}
