/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.constrain;

import cn.ponfee.commons.model.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.stream.Collectors;

import static cn.ponfee.commons.model.ResultCode.BAD_REQUEST;

/**
 * 基于JSR303的Web端参数校验统一处理
 * 
 * Controller的方法中有BindingResult参数，则spring框架会进入Controller的方法内
 * public Result<Void> testValidate(@Valid Article article, BindingResult result) {}
 * 
 * @author Ponfee
 */
//@ControllerAdvice
//@Aspect
//@Order(Ordered.HIGHEST_PRECEDENCE)
public abstract class Jsr303Validator {

    //@Around("execution(public * cn.ponfee.xxx.controller..*Controller..*(..)) && args(..,bindingResult)")
    public Object verify(ProceedingJoinPoint pjp, BindingResult bindingResult) throws Throwable {
        if (bindingResult.hasErrors()) {
            return handleFailure(
                ((MethodSignature) pjp.getSignature()).getMethod().getReturnType(), bindingResult
            );
        }
        return pjp.proceed();
    }

    protected Object handleFailure(Class<?> returnType, BindingResult bindingResult) {
        String errorMsg = bindingResult.getAllErrors()
                                       .stream()
                                       .map(ObjectError::getDefaultMessage)
                                       .collect(Collectors.joining(",", "[", "]"));
        if (returnType == Result.class) {
            return Result.failure(BAD_REQUEST.getCode(), BAD_REQUEST.getMsg() + ": " + errorMsg);
        } else {
            throw new IllegalArgumentException(errorMsg);
        }
    }

}
