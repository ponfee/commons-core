package code.ponfee.commons.constrain;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.reflect.Fields;
import code.ponfee.commons.reflect.GenericUtils;

/**
 * <pre>
 * 方法参数校验：拦截方法中包含@Constraints注解的方法
 * e.g.：
 *    1.开启spring切面特性：<aop:aspectj-autoproxy />
 *    2.编写子类：
 *        ＠Component ＠Aspect
 *        public class TestMethodValidator extends MethodValidator {
 *
 *            ＠Around(
 *                value = "execution(public * code.ponfee.xxx.service.impl..*Impl..*(..)) && ＠annotation(cst)", 
 *                argNames = "pjp,cst"
 *            )
 *            ＠Override
 *            public Object constrain(ProceedingJoinPoint pjp, Constraints cst) throws Throwable {
 *                return super.constrain(pjp, cst);
 *            }
 *
 *            ＠Override
 *            protected Object handleFailure(Class<?> returnType, String errMsg) {
 *                if (BaseResult.class.isAssignableFrom(returnType)) {
 *                    return BaseResult.failure(errMsg);
 *                }
 *                return super.handleFailure(returnType, errMsg);
 *            }
 *
 *        }
 * </pre>
 * 
 * 参数校验
 * 
 * @author Ponfee
 */
public abstract class MethodValidator extends FieldValidator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodValidator.class);

    /**
     * @param pjp
     * @param validator
     * @return
     * @throws Throwable
     */
    @SuppressWarnings("unchecked")
    public Object constrain(ProceedingJoinPoint pjp, Constraints validator) throws Throwable {
        Object[] args = pjp.getArgs();
        if (args == null || args.length == 0) {
            return pjp.proceed();
        }

        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        String[] argsName = METHOD_ARGSNAME.getIfPresent(method);
        if (argsName == null) {
            // 要用到asm字节码操作，消耗性能，所以缓存
            argsName = ClassUtils.getMethodParamNames(method);
            METHOD_ARGSNAME.put(method, argsName);
        }

        // 校验开始
        StringBuilder builder = new StringBuilder();
        Class<?>[] paramTypes = method.getParameterTypes();
        Constraint cst;
        String fieldName;
        Object fieldVal;
        Class<?> fieldType;
        Constraint[] csts = validator.value();
        try {
            boolean[] argsNullable = argsNullable(args, csts);
            for (int len = csts.length, i = 0; i < len; i++) {
                cst = csts[i];
                fieldVal = args[cst.index()]; // 参数对象校验
                fieldType = paramTypes[cst.index()];
                if (argsNullable[cst.index()] && fieldVal == null) {
                    continue; // 参数可为空，则跳过校验
                } else if (StringUtils.isEmpty(cst.field())) {
                    // 验证参数对象
                    fieldName = argsName[cst.index()];
                    builder.append(constrain(method, fieldName, fieldVal, cst, fieldType));
                } else if (fieldVal == null) {
                    // 不可为空，则抛出异常
                    String msg;
                    if (args.length == 1) {
                        msg = "参数不能为空;";
                    } else {
                        msg = "参数{" + argsName[cst.index()] + "}不能为空;";
                    }
                    throw new IllegalArgumentException(msg);
                } else if (fieldVal instanceof Map) {
                    /*Method get = fieldVal.getClass().getMethod("get", Object.class);
                    get.setAccessible(true); // ImmutableMap must be set accessible true
                    fieldVal = get.invoke(fieldVal, cst.field());*/
                    fieldVal = ((Map<String, Object>) fieldVal).get(cst.field());
                    fieldType = fieldVal == null ? null : fieldVal.getClass();
                    fieldName = argsName[cst.index()] + "[" + cst.field() + "]";
                    builder.append(constrain(fieldName, fieldVal, cst, fieldType)); // cannot cache
                } else if (fieldVal instanceof Dictionary) {
                    fieldVal = ((Dictionary<String, Object>) fieldVal).get(cst.field());
                    fieldType = fieldVal == null ? null : fieldVal.getClass();
                    fieldName = argsName[cst.index()] + "[" + cst.field() + "]";
                    builder.append(constrain(fieldName, fieldVal, cst, fieldType)); // cannot cache
                } else {
                    // 验证java bean
                    String[] ognl = cst.field().split("\\.");
                    Field field;
                    for (String s : ognl) {
                        field = ClassUtils.getField(fieldType, s);
                        fieldType = GenericUtils.getFieldActualType(fieldType, field);
                        if (fieldVal != null) {
                            fieldVal = Fields.get(fieldVal, field);
                        }
                    }
                    fieldName = argsName[cst.index()] + "." + cst.field();
                    builder.append(constrain(method, fieldName, fieldVal, cst, fieldType));
                }

                if (builder.length() > MAX_MSG_SIZE) {
                    break;
                }
            }
        } catch (UnsupportedOperationException | IllegalArgumentException e) {
            builder.append(e.getMessage());
        } catch (Exception e) {
            LOG.error("参数约束校验异常", e);
            builder.append("参数约束校验异常：").append(e.getMessage());
        }

        return builder.length() == 0 ? pjp.proceed() : processError(builder, method, args);
    }

    // -------------------------------------------------------------------------private methods
    private boolean[] argsNullable(Object[] args, Constraint[] csts) {
        Set<String> set = new HashSet<>(csts.length);
        boolean[] isArgsNullable = new boolean[args.length];
        Arrays.fill(isArgsNullable, false);
        for (Constraint cst : csts) {
            String key = "index=" + cst.index() + ", field=\"" + cst.field() + "\"";
            if (!set.add(key)) {
                throw new RuntimeException("配置错误，重复校验[" + key + "]");
            }

            if (cst.index() > args.length - 1) {
                throw new RuntimeException("配置错误，下标超出[index=" + cst.index() + "]");
            }

            if (StringUtils.isEmpty(cst.field()) && !cst.notNull()) {
                isArgsNullable[cst.index()] = true; // 该参数可为空
            }
        }
        return isArgsNullable;
    }

}
