package code.ponfee.commons.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
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
     * 400 - Bad Request
     */
    @ExceptionHandler({
        TypeMismatchException.class, HttpMessageNotReadableException.class,
        MethodArgumentNotValidException.class, ServletRequestBindingException.class,
        IllegalArgumentException.class, IllegalStateException.class, BindException.class
    })
    //@ResponseBody @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(HttpServletRequest req, HttpServletResponse resp, Exception e) {
        LOGGER.debug("Bad request.", e);
        handle(req, resp, serverErrorPage, ResultCode.BAD_REQUEST, e.getMessage());
    }

    /**
     * 405 - Method Not Allowed
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    //@ResponseBody @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public void handle(HttpServletRequest req, HttpServletResponse resp, HttpRequestMethodNotSupportedException e) {
        LOGGER.debug("Request method not supported.", e);
        handle(req, resp, serverErrorPage, ResultCode.NOT_ALLOWED, e.getMessage());
    }

    /**
     * 415 - Unsupported Media Type
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    //@ResponseBody @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public void handle(HttpServletRequest req, HttpServletResponse resp, HttpMediaTypeNotSupportedException e) {
        LOGGER.debug("Media type not supported.", e);
        handle(req, resp, serverErrorPage, ResultCode.UNSUPPORT_MEDIA, e.getMessage());
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
        handle(req, resp, serverErrorPage, ResultCode.SERVER_ERROR, message);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp, 
                        String page, ResultCode code, String message) {
        handle(req, resp, page, code.getCode(), message);
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
