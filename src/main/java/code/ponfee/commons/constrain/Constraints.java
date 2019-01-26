package code.ponfee.commons.constrain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 *  `@Constraints({
 *      `@Constraint(field = "name", notBlank = true, maxLen = 64),
 *      `@Constraint(field = "type", series = { 1, 2 }),
 *  })
 * </pre>
 * 
 * 方法参数校验器
 * @author fupf
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Constraints {
    Constraint[] value() default {};
}
