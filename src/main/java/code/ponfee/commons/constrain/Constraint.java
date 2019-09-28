package code.ponfee.commons.constrain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 *  `@Constraints({
 *      `@Constraint(field = "name", notBlank = true, maxLen = 64),
 *      `@Constraint(field = "type", series = { 1, 2 })
 *  })
 *
 *  or
 *
 *  `@Constraint(notBlank = true, maxLen = 64)
 *  private String name;
 * </pre>
 *
 * 参数约束
 * 
 * @author Ponfee
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Constraints.class) // 当只有一个Constraint时要包上Constraints
@Documented
public @interface Constraint {

    /**
     * 参数索引位置（第一个参数为0，第二个参数为1，...，第N个参数为N-1）
     */
    int index() default 0;

    /**
     * 基本数据类型参数不用设值，对象类型参数为字段名
     */
    String field() default ""; // can not use in class filed

    /**
     * 校验失败时的提示信息（不设置时自动拼装为如：orderNo{null}：not allow blank;）
     */
    String msg() default "";

    /**
     * 是否不能为null ， 为true表示不能为空 ， false表示能够为空（对所有类型有效，默认不能为空）
     */
    boolean notNull() default true;

    /**
     * 是否不能为空（只针对CharSequence,Collection,Map,Dictionary,Array）
     */
    boolean notEmpty() default false;

    /**
     * 是否不为空白串（只针对CharSequence）
     */
    boolean notBlank() default false;

    /**
     * 最大长度（只针对CharSequence）
     */
    int maxLen() default -1;

    /**
     * 最小长度（只针对CharSequence）
     */
    int minLen() default -1;

    /**
     * 正则验证（只针对CharSequence）
     */
    String regExp() default "";

    /**
     * 最大值（只针对整数）
     * the max value cannot supported Long.MAX_VALUE
     */
    long max() default Long.MAX_VALUE;

    /**
     * 最小值（只针对整数）
     * the max value cannot supported Long.MIN_VALUE
     */
    long min() default Long.MIN_VALUE;

    /**
     * 日期格式（只针对CharSequence）
     */
    String datePattern() default "";

    /**
     * 时态（只针对CharSequence[datePattern]，Date）
     */
    Tense tense() default Tense.ANY;

    /**
     * 数列
     */
    long[] series() default {};

    /**
     * 最大值（只针对浮点数）
     * the decimalMax value cannot supported Double.POSITIVE_INFINITY
     */
    double decimalMax() default Double.POSITIVE_INFINITY;

    /**
     * 最小值（只针对浮点数）
     * the decimalMin value cannot supported Double.NEGATIVE_INFINITY
     */
    double decimalMin() default Double.NEGATIVE_INFINITY;

    /**
     * 时态（过去或将来）
     */
    enum Tense {
        PAST("过去"), FUTURE("将来"), ANY("任意");

        private final String desc;

        Tense(String desc) {
            this.desc = desc;
        }

        public String desc() {
            return this.desc;
        }
    }

}
