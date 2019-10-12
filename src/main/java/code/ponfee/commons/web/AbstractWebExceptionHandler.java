package code.ponfee.commons.web;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.google.common.base.Throwables;

import code.ponfee.commons.exception.BasicException;
import code.ponfee.commons.exception.UnauthorizedException;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.model.ResultCode;

/**
 * Spring mvc global exception handler for web application
 * 
 * <code>
 *   `@ControllerAdvice
 *   public class WebExceptionHandler extends AbstractWebExceptionHandler {}
 * </code
 * 
 * 有@RequestBody：类型转换出错时，会抛HttpMessageNotReadableException
 * 无@RequestBody：类型转换出错时，会抛BindException（假如方法中有BindingResult参数，则错误信息会收集到此参数中，正常进入到业务方法）
 * 
 * @author Ponfee
 */
public abstract class AbstractWebExceptionHandler {

    public final String unauthorizedPage;
    public final String serverErrorPage;
    public final String defaultErrorMsg;

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebExceptionHandler.class);

    public AbstractWebExceptionHandler() {
        this("/page/401.html", "/page/500.html", "Server error.");
    }

    public AbstractWebExceptionHandler(String unauthorizedPage, String serverErrorPage, String defaultErrorMsg) {
        this.unauthorizedPage = unauthorizedPage;
        this.serverErrorPage = serverErrorPage;
        this.defaultErrorMsg = defaultErrorMsg;
    }

    /**
     * 401 - Unauthorized
     */
    @ExceptionHandler(UnauthorizedException.class)
    //@ResponseBody @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void handle(HttpServletRequest req, HttpServletResponse resp, UnauthorizedException e) {
        LOGGER.debug("Unauthorized.", e);
        int code = e.getCode() == null ? ResultCode.UNAUTHORIZED.getCode() : e.getCode();
        handle(req, resp, unauthorizedPage, code, e.getMessage());
    }

    /**
     * 400 - Bind error: jsr 303
     * 
     * Controller方法中无BindingResult参数
     * public Result testValidate1(@Valid Article article) {}
     * 
     * 注：
     *  1、@org.springframework.validation.annotation.Validated可代替@Valid
     *  2、类型转换失败（如前端传错误的日期格式）也会抛BindException
     */
    @ExceptionHandler(BindException.class)
    //@ResponseBody @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(HttpServletRequest req, HttpServletResponse resp, BindException e) {
        LOGGER.debug("Bind failed.", e);
        handle(req, resp, serverErrorPage, e.getAllErrors());
    }

    /**
     * 400 - Method argument not valid: jsr 303
     * 
     * 含@RequestBody注解（HttpMessageConverter application/json）
     * 
     * Controller方法中无BindingResult参数
     * public Result testValidate2(@RequestBody `@Valid Article article) {}
     * 
     * 注：
     *  1、@org.springframework.validation.annotation.Validated可代替@Valid
     *  2、类型转换失败（如前端传错误的日期格式）会抛HttpMessageNotReadableException，不会抛MethodArgumentNotValidException（即不会进入此方法）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    //@ResponseBody @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleMethod(HttpServletRequest req, HttpServletResponse resp, MethodArgumentNotValidException e) {
        LOGGER.debug("Method argument not valid.", e);
        handle(req, resp, serverErrorPage, e.getBindingResult().getAllErrors());
    }

    /**
     * 400 - Constraint violation: jsr 303
     * 
     * 1、加配置：
     *   <bean id="MethodValidationPostProcessor" class="org.springframework.validation.beanvalidation.MethodValidationPostProcessor">
     *     <property name="validator"><bean class="code.ponfee.commons.constrain.FastFailValidatorFactoryBean" /></property>
     *   </bean>
     * 2、必须在Controller类中注解@org.springframework.validation.annotation.Validated
     * 3、public Result testValidate3(@Range(min = 1, max = 9, message = "年级只能从1-9") @RequestParam(name = "grade", required = true) int grade) {}
     */
    @ExceptionHandler(ConstraintViolationException.class)
    //@ResponseBody @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(HttpServletRequest req, HttpServletResponse resp, ConstraintViolationException e) {
        LOGGER.debug("Constraint violation.", e);
        String message = e.getConstraintViolations().stream()
                                                    .map(ConstraintViolation::getMessage)
                                                    .collect(Collectors.joining(",", "[", "]"));
        handle(req, resp, serverErrorPage, ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 400 - Bad Request
     */
    @ExceptionHandler({
        IllegalArgumentException.class, IllegalStateException.class, 
        TypeMismatchException.class, HttpMessageNotReadableException.class,
        ServletRequestBindingException.class, 
    })
    //@ResponseBody @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(HttpServletRequest req, HttpServletResponse resp, Exception e) {
        LOGGER.debug("Bad request.", e);
        handle(req, resp, serverErrorPage, ResultCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    /**
     * 405 - Method Not Allowed
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    //@ResponseBody @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public void handle(HttpServletRequest req, HttpServletResponse resp, HttpRequestMethodNotSupportedException e) {
        LOGGER.debug("Request method not supported.", e);
        handle(req, resp, serverErrorPage, ResultCode.NOT_ALLOWED.getCode(), e.getMessage());
    }

    /**
     * 415 - Unsupported Media Type
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    //@ResponseBody @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public void handle(HttpServletRequest req, HttpServletResponse resp, HttpMediaTypeNotSupportedException e) {
        LOGGER.debug("Media type not supported.", e);
        handle(req, resp, serverErrorPage, ResultCode.UNSUPPORT_MEDIA.getCode(), e.getMessage());
    }

    /**
     * 500 - Biz operate failure
     */
    @ExceptionHandler(BasicException.class)
    //@ResponseBody @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handle(HttpServletRequest req, HttpServletResponse resp, BasicException e) {
        LOGGER.debug("Biz operate failure.", e);
        Integer code = ((BasicException) e).getCode();
        code = code == null ? ResultCode.SERVER_ERROR.getCode() : code;
        handle(req, resp, serverErrorPage, code, e.getMessage());
    }

    /**
     * 500 - Internal Server Error
     */
    @ExceptionHandler(Throwable.class)
    //@ResponseBody @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handle(HttpServletRequest req, HttpServletResponse resp, Throwable t) {
        LOGGER.error("Server error.", t);
        String message = LOGGER.isDebugEnabled() ? Throwables.getStackTraceAsString(t) : defaultErrorMsg;
        handle(req, resp, serverErrorPage, ResultCode.SERVER_ERROR.getCode(), message);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp,
                        String page, List<ObjectError> errors) {
        String message = errors.stream()
                               .map(ObjectError::getDefaultMessage)
                               .collect(Collectors.joining(",", "[", "]"));
        handle(req, resp, serverErrorPage, ResultCode.BAD_REQUEST.getCode(), message);
    }

    protected void handle(HttpServletRequest req, HttpServletResponse resp,
                          String page, int code, String message) {
        if (LOGGER.isDebugEnabled() || WebUtils.isAjax(req)) {
            WebUtils.respJson(resp, new Result<>(code, message)); // resp.setStatus(code);
        } else {
            try {
                req.getRequestDispatcher(page).forward(req, resp);
                //resp.sendRedirect(resp.encodeRedirectURL(WebUtils.getContextPath(req) + page));
            } catch (IOException | ServletException e) {
                LOGGER.error("Forward page occur error.", e);
            }
        }
    }

}
