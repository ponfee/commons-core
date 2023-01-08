/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.constrain;

import cn.ponfee.commons.exception.Throwables;
import cn.ponfee.commons.reflect.ClassUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * <pre>
 * 方法参数校验：拦截参数中包含@ConstrainParam注解的方法
 * ＠Component
 * ＠Aspect
 * public class TestParamValidator extends ParamValidator {
 *    ＠Around(value = "execution(public * cn.ponfee.xxx.service.impl.*Impl.*(
 *         ＠cn.ponfee.commons.constrain.ConstrainParam (*)
 *    ))")
 *    public ＠Override Object constrain(ProceedingJoinPoint joinPoint) throws Throwable {
 *      return super.constrain(joinPoint);
 *    }
 * }
 * </pre>
 * 
 * @author Ponfee
 */
public abstract class ParamValidator extends FieldValidator {

    private static final Logger LOG = LoggerFactory.getLogger(ParamValidator.class);

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
        try {
            // 缓存方法参数名
            MethodSignature mSign = (MethodSignature) joinPoint.getSignature();
            method = joinPoint.getTarget().getClass()
                              .getMethod(mSign.getName(), mSign.getParameterTypes());
            argsName = METHOD_ARGSNAME.getIfPresent(method);
            if (argsName == null) {
                argsName = ClassUtils.getMethodParamNames(method);
                METHOD_ARGSNAME.put(method, argsName);
            }

            // 方法参数注解校验
            Annotation[][] anns = method.getParameterAnnotations();

            outer: // this is the label for the outer loop
            for (int i = 0; i < args.length; i++) {
                for (Annotation ann : anns[i]) {
                    if (ann instanceof ConstrainParam) {
                        try {
                            constrain(args[i]);
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
            LOG.error("reflect exception", e);
            builder.append(Throwables.getRootCauseStackTrace(e));
        }

        return builder.length() == 0 ? joinPoint.proceed() : processError(builder, method, args);
    }
}
