/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.constrain;

import java.lang.annotation.*;

/**
 * <pre>
 *  `@Constraints({
 *      `@Constraint(field = "name", notBlank = true, maxLen = 64),
 *      `@Constraint(field = "type", series = { 1, 2 }),
 *  })
 * </pre>
 * 
 * 方法参数校验器
 * 
 * @author Ponfee
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Constraints {
    Constraint[] value() default {};
}
