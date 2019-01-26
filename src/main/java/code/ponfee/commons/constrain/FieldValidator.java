package code.ponfee.commons.constrain;

import static code.ponfee.commons.model.ResultCode.BAD_REQUEST;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.cache.Cache;
import code.ponfee.commons.cache.CacheBuilder;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.reflect.Fields;
import code.ponfee.commons.util.ObjectUtils;
import code.ponfee.commons.util.RegexUtils;
import code.ponfee.commons.util.Strings;

/**
 * <pre>
 *   校验bean实体中含@Constraint注解的属性
 *   e.g.：FieldValidator.newInstance().constrain(bean);
 * </pre>
 * 
 * 字段校验
 * @author fupf
 */
public class FieldValidator {

    private static Logger logger = LoggerFactory.getLogger(FieldValidator.class);

    static final int MAX_MSG_SIZE = 500;
    private static final String CFG_ERR = "约束配置错误[";
    private static final String EMPTY = "";
    private static final Lock LOCK = new ReentrantLock();
    static final Cache<String[]> METHOD_SIGN_CACHE = CacheBuilder.newBuilder().build();
    private static final Cache<CacheResult> META_CFG_CACHE = CacheBuilder.newBuilder().build();

    protected FieldValidator() {}

    public static FieldValidator newInstance() {
        return new FieldValidator();
    }

    /**
     * 约束验证
     * @param bean
     */
    public final void constrain(Object bean) {
        Class<?> clazz = bean.getClass();
        if (clazz.isInterface()) {
            throw new UnsupportedOperationException("unsupported interface constrain.");
        }

        StringBuilder builder = new StringBuilder();
        while (!clazz.equals(Object.class)) {
            for (Field field : clazz.getDeclaredFields()) {
                Constraint cst = field.getAnnotation(Constraint.class);
                if (cst == null || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                String name = ClassUtils.getClassName(clazz);
                Object value = Fields.get(bean, field);
                String error = constrain(name, field.getName(), value, cst, field.getType());
                builder.append(error);
            }
            clazz = clazz.getSuperclass();
        }

        if (builder.length() > 0) {
            throw new IllegalArgumentException(builder.toString());
        }
    }

    protected Object process(StringBuilder builder, ProceedingJoinPoint pjp,
        Method method, Object[] args, String methodSign) throws Throwable {
        if (builder.length() == 0) {
            return pjp.proceed(); // 校验成功，调用方法
        }

        // 校验失败，不调用方法，进入失败处理
        if (builder.length() > MAX_MSG_SIZE) {
            builder.setLength(MAX_MSG_SIZE - 3);
            builder.append("...");
        }
        String errMsg = builder.toString();
        if (logger.isInfoEnabled()) {
            logger.info(
                "[args check not pass]-[{}]-{}-[{}]", 
                methodSign, ObjectUtils.toString(args), errMsg
            );
        }

        return handleFailure(method.getReturnType(), errMsg);
    }

    protected Object handleFailure(Class<?> returnType, String errMsg) {
        if (returnType == Result.class) {
            return Result.failure(BAD_REQUEST, BAD_REQUEST.getMsg() + ": " + errMsg);
        } else {
            throw new IllegalArgumentException(errMsg);
        }
    }

    protected final String constrain(String name, String field, Object value, 
                                     Constraint cst, Class<?> type) {
        name = new StringBuilder(name).append('@').append(field).toString();
        CacheResult result = META_CFG_CACHE.get(name);
        if (result == null) {
            LOCK.lock();
            try {
                if ((result = META_CFG_CACHE.get(name)) == null) {
                    try {
                        verifyMeta(field, cst, type);
                        result = new CacheResult(true);
                        META_CFG_CACHE.set(name, result);
                    } catch (Exception e) {
                        result = new CacheResult(false, e.getMessage());
                        META_CFG_CACHE.set(name, result);
                        throw e;
                    }
                }
            } finally {
                LOCK.unlock();
            }
        }

        // 配置验证
        if (!result.flag) {
            throw new UnsupportedOperationException(result.msg);
        }

        // 参数验证
        return verifyValue(field, value, cst);
    }

    /**
     * Constrain without cache,
     * only use in method parameter is Map or Dictionary type
     *
     * @param name
     * @param value
     * @param cst
     * @param type
     * @return the constrain result, if empty string means success
     */
    protected final String constrain(String name, Object value, 
                                     Constraint cst, Class<?> type) {
        // 配置验证
        verifyMeta(name, cst, type);

        // 参数验证
        return verifyValue(name, value, cst);
    }

    /**
     * 配置元数据验证
     * @param name
     * @param c
     * @param type
     */
    private void verifyMeta(String name, Constraint c, Class<?> type) {
        if (type == null) {
            return;
        }

        // 基本类型转包装类型（如果是）
        type = org.apache.commons.lang3.ClassUtils.primitiveToWrapper(type);

        // 字串类型验证
        if (   (isNotBlank(c.regExp()) || isNotBlank(c.datePattern()) || c.notBlank() || c.maxLen() > -1 || c.minLen() > -1)
            && !CharSequence.class.isAssignableFrom(type)
        ) {
            throw new UnsupportedOperationException(CFG_ERR + name + "]：非字符类型不支持字符规则验证");
        }

        // 整数类型验证
        if (   !(c.max() == Long.MAX_VALUE && c.min() == Long.MIN_VALUE) // meet not all the conditions
            && !(Long.class.isAssignableFrom(type) || Integer.class.isAssignableFrom(type))
        ) {
            throw new UnsupportedOperationException(CFG_ERR + name + "]：非整数类型不支持整数数值验证");
        }

        // 数列类型验证
        if (   (c.series() != null && c.series().length > 0)
            && !(Long.class.isAssignableFrom(type) || Integer.class.isAssignableFrom(type))
        ) {
            throw new UnsupportedOperationException(CFG_ERR + name + "]：非整数类型不支持数列验证");
        }

        // 小数类型验证
        if (   !(c.decimalMax() == Double.POSITIVE_INFINITY && c.decimalMin() == Double.NEGATIVE_INFINITY)
            && !(Double.class.isAssignableFrom(type) || Float.class.isAssignableFrom(type))
        ) {
            throw new UnsupportedOperationException(CFG_ERR + name + "]：非浮点数类型不支持浮点数值验证");
        }

        // 时间类型验证
        if (   (c.tense() != Constraint.Tense.ANY && isBlank(c.datePattern()))
            && !Date.class.isAssignableFrom(type)
        ) {
            throw new UnsupportedOperationException(CFG_ERR + name + "]：非日期类型不支持时态验证");
        }

        // 字串、集合类型验证
        if (c.notEmpty() && !isEmptiable(type)) {
            throw new UnsupportedOperationException(CFG_ERR + name + "非集合/字符类型不支持非空验证");
        }
    }

    /**
     * Returns the type instance value can with empty verifyValue
     *
     * @param type  the class type
     * @return {@code true} means should with empty verifyValue
     */
    private boolean isEmptiable(Class<?> type) {
        return CharSequence.class.isAssignableFrom(type)
            || Collection.class.isAssignableFrom(type)
            || type.isArray()
            || Map.class.isAssignableFrom(type)
            || Dictionary.class.isAssignableFrom(type);
    }

    private String verifyValue(String str, Object value, Constraint cst) {
        String error = verify(str, value, cst);
        if (isNotBlank(error)) { // verify result is not pass
            return isNotBlank(cst.msg()) ? cst.msg() + ";" : error;
        } else {
            return EMPTY;
        }
    }

    /**
     * 参数验证
     * @param n
     * @param v
     * @param c
     * @return
     */
    private String verify(String n, Object v, Constraint c) {
        // 可以为null且值为null，则跳过验证
        if (!c.notNull() && v == null) {
            return EMPTY;
        }

        // 是否不能为blank
        if (c.notBlank() && isBlank((CharSequence) v)) {
            return n + "{" + v + "}：不能为空串;";
        }

        // 是否不能为empty
        if (c.notEmpty() && ObjectUtils.isEmpty(v)) {
            return n + "{" + v + "}：不能为empty;";
        }

        // 是否不能为null
        if (c.notNull() && v == null) {
            return n + "{null}：不能为null;";
        }

        // 正则校验
        if (!(!c.notNull() && v == null) && isNotBlank(c.regExp())
            && (v == null || !RegexUtils.matches(v.toString(), c.regExp()))) {
            return n + "{" + v + "}：格式不匹配" + c.regExp() + ";";
        }

        // 最大字符长度
        if (c.maxLen() > -1 && v != null && ((CharSequence) v).length() > c.maxLen()) {
            return n + "{" + v + "}：不能大于" + c.maxLen() + "个字符;";
        }

        // 最小字符长度
        if (c.minLen() > -1 && (v == null || ((CharSequence) v).length() < c.minLen())) {
            return n + "{" + v + "}：不能小于" + c.minLen() + "个字符;";
        }

        // 整数值限制
        if (c.max() != Long.MAX_VALUE && v != null && Long.parseLong(v.toString()) > c.max()) {
            return n + "{" + v + "}：不能大于" + c.max() + ";";
        }
        if (c.min() != Long.MIN_VALUE && (v == null || Long.parseLong(v.toString()) < c.min())) {
            return n + "{" + v + "}：不能小于" + c.min() + ";";
        }

        // 数列验证
        long[] seqs = c.series();
        if ((seqs != null && seqs.length > 0)
            && (v == null || !ArrayUtils.contains(seqs, Long.parseLong(v.toString())))) {
            return n + "{" + v + "}：不属于数列" + ObjectUtils.toString(seqs) + ";";
        }

        // 浮点数限制
        if (c.decimalMax() != Double.POSITIVE_INFINITY && v != null
            && Double.parseDouble(v.toString()) > c.decimalMax()) {
            return n + "{" + v + "}：不能大于" + c.decimalMax() + ";";
        }
        if (c.decimalMin() != Double.NEGATIVE_INFINITY
            && (v == null || Double.parseDouble(v.toString()) < c.decimalMin())) {
            return n + "{" + v + "}：不能小于" + c.decimalMin() + ";";
        }

        // 时间格式 
        Date date = null;
        if (isNotBlank(c.datePattern()) && !(!c.notNull() && (v == null||Strings.isEmpty(v)))) {
            try {
                date = DateUtils.parseDateStrictly((String) v, c.datePattern());
                //date = FastDateFormat.getInstance(c.datePattern()).parse((String) v);
            } catch (ParseException e) {
                return n + "{" + v + "}：日期格式不匹配" + c.datePattern() + ";";
            }
        }

        // 时态校验
        if (c.tense() != Constraint.Tense.ANY) {
            if (date == null) {
                date = (Date) v;
            }

            String pattern = c.datePattern();
            if (isBlank(pattern)) {
                pattern = "yyyy-MM-dd HH:mm:ss";
            }
            if (date == null) {
                return n + "{null}：日期不能为空;";
            } else if (c.tense() == Constraint.Tense.FUTURE && date.before(new Date())) {
                return n + "{" + DateFormatUtils.format(date, pattern) + "}：不为将来时间;";
            } else if (c.tense() == Constraint.Tense.PAST && date.after(new Date())) {
                return n + "{" + DateFormatUtils.format(date, pattern) + "}：不为过去时间;";
            }
        }

        return EMPTY;
    }

    private static final class CacheResult {
        final boolean flag;
        final String msg;

        CacheResult(boolean flag, String msg) {
            this.flag = flag;
            this.msg = msg;
        }

        CacheResult(boolean flag) {
            this(flag, null);
        }
    }

}
