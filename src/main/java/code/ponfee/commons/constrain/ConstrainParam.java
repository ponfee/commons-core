package code.ponfee.commons.constrain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * 方法参数验证，用于方法参数内，e.g.
 * public void method(@ConstrainParam SomeBean param);
 * </pre>
 * 
 * @author Ponfee
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ConstrainParam {
}
