package code.ponfee.commons.constrain;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.exception.Throwables;
import code.ponfee.commons.reflect.ClassUtils;

/**
 * <pre>
 * 方法参数校验：拦截参数中包含@ConstrainParam注解的方法
 * ＠Component
 * ＠Aspect
 * public class TestParamValidator extends ParamValidator {
 *    ＠Around(value = "execution(public * code.ponfee.xxx.service.impl.*Impl.*(
 *         ＠code.ponfee.commons.constrain.ConstrainParam (*)
 *    ))")
 *    public ＠Override Object constrain(ProceedingJoinPoint joinPoint) throws Throwable {
 *      return super.constrain(joinPoint);
 *    }
 * }
 * </pre>
 * 
 * @author fupf
 */
public abstract class ParamValidator extends FieldValidator {

    private static Logger logger = LoggerFactory.getLogger(ParamValidator.class);

    /**
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    public Object constrain(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return joinPoint.proceed();
        }

        // 参数校验
        StringBuilder builder = new StringBuilder();
        String[] argsName;
        Method method = null;
        String methodSign = null;
        try {
            // 缓存方法参数名
            MethodSignature mSign = (MethodSignature) joinPoint.getSignature();
            method = joinPoint.getTarget().getClass()
                              .getMethod(mSign.getName(), mSign.getParameterTypes());
            methodSign = method.toGenericString();
            argsName = METHOD_SIGN_CACHE.get(methodSign);
            if (argsName == null) {
                argsName = ClassUtils.getMethodParamNames(method);
                METHOD_SIGN_CACHE.set(methodSign, argsName);
            }

            // 方法参数注解校验
            Annotation[][] anns = method.getParameterAnnotations();

            outer: // this is the label for the outer loop
            for (int i = 0; i < args.length; i++) {
                for (Annotation ann : anns[i]) {
                    if (ann instanceof ConstrainParam) {
                        try {
                            super.constrain(args[i]);
                        } catch (IllegalArgumentException e) {
                            builder.append("[").append(argsName[i]).append("]")
                                   .append(e.getMessage());
                        }
                    }
                    if (builder.length() > MAX_MSG_SIZE) {
                        break outer;
                    }
                }
            }
        } catch (UnsupportedOperationException e) {
            builder.append(e.getMessage());
        } catch (NoSuchMethodException e) {
            logger.error("reflect exception", e);
            builder.append(Throwables.getStackTrace(e));
        }

        return super.process(builder, joinPoint, method, args, methodSign);
    }
}
