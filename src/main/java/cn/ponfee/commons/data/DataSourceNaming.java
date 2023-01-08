/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.data;

import java.lang.annotation.*;

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
     * Spring EL expression
     */
    String value() default "";
}
