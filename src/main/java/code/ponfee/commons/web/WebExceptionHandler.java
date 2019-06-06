package code.ponfee.commons.web;

import java.io.IOException;
import java.util.function.Function;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.base.Throwables;

import code.ponfee.commons.exception.BasicException;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.model.ResultCode;
import code.ponfee.commons.web.WebUtils;

/**
 * Global exception handler for web application
 *
 * @author Ponfee
 */
@ControllerAdvice
public class WebExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(WebExceptionHandler.class);
    public static final String ERROR_MSG = "500 Error";

    /**
     * 400 - Bad Request
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public Result<Void> handle(HttpMessageNotReadableException e) {
        logger.debug("Bad request", e);
        return Result.failure(ResultCode.BAD_REQUEST, e.getMessage());
    }

    /**
     * 405 - Method Not Allowed
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public Result<Void> handle(HttpRequestMethodNotSupportedException e) {
        logger.debug("Method not allowed", e);
        return Result.failure(ResultCode.NOT_ALLOWED, e.getMessage());
    }

    /**
     * 415 - Unsupported Media Type
     */
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseBody
    public Result<Void> handle(HttpMediaTypeNotSupportedException e) {
        logger.debug("Unsupported media type", e);
        return Result.failure(ResultCode.UNSUPPORT_MEDIA, e.getMessage());
    }

    /**
     * 500 - Internal Server Error
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Throwable.class)
    public void handle(HttpServletRequest req, HttpServletResponse resp, Throwable t) {
        if (t instanceof IllegalArgumentException) {
            logger.info("", t);
            handleException(req, resp, ResultCode.BAD_REQUEST.getCode(), debug -> t.getMessage());
        } else if (t instanceof BasicException) {
            logger.info("", t);
            handleException(req, resp, ((BasicException) t).getCode(), debug -> t.getMessage());
        } else {
            logger.error("Server error", t);
            handleException(
                req, resp, ResultCode.SERVER_ERROR.getCode(), 
                debug -> debug ? Throwables.getStackTraceAsString(t) : ERROR_MSG
            );
        }
    }

    private void handleException(HttpServletRequest req, HttpServletResponse resp, 
                                 int code, Function<Boolean, String> mapper) {
        boolean debug = logger.isDebugEnabled();
        if (debug || WebUtils.isAjax(req)) {
            WebUtils.respJson(resp, new Result<>(code, mapper.apply(debug)));
        } else {
            try {
                req.getRequestDispatcher("/page/500.html").forward(req, resp);
            } catch (ServletException | IOException e) {
                logger.info("Forward page occur error.", e);
            }
        }
    }

}
