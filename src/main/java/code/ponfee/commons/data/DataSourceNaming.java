package code.ponfee.commons.data;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多数据源注解，指定要切换的数据源名称，支持Spring SPEL，上下文为方法参数（数组）
 * 
 * @author Ponfee
 */
@Documented
@Inherited
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSourceNaming {

    /**
     * Specifiy string of the dataSource name
     */
    String value() default "";
}
