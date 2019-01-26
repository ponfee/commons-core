//package code.ponfee.commons.web;
//
//import code.ponfee.commons.exception.BasicException;
//import code.ponfee.commons.model.Result;
//import code.ponfee.commons.model.ResultCode;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.converter.HttpMessageNotReadableException;
//import org.springframework.web.HttpMediaTypeNotSupportedException;
//import org.springframework.web.HttpRequestMethodNotSupportedException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.ResponseStatus;
//
///**
// * WebExceptionHandler
// *
// * @author Ponfee
// */
//@ControllerAdvice
//@ResponseBody
//public class WebExceptionHandler {
//
//    private static Logger logger = LoggerFactory.getLogger(WebExceptionHandler.class);
//    public static final String ERROR_MSG = "系统异常，请与管理员联系";
//
//    /**
//     * 400 - Bad Request
//     */
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ExceptionHandler(HttpMessageNotReadableException.class)
//    public Result<Void> handle(HttpMessageNotReadableException e) {
//        logger.info("bad request", e);
//        return Result.failure(ResultCode.BAD_REQUEST, e.getMessage());
//    }
//
//    /**
//     * 405 - Method Not Allowed
//     */
//    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
//    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
//    public Result<Void> handle(HttpRequestMethodNotSupportedException e) {
//        logger.info("method not allowed", e);
//        return Result.failure(ResultCode.NOT_ALLOWED, e.getMessage());
//    }
//
//    /**
//     * 415 - Unsupported Media Type
//     */
//    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
//    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
//    public Result<Void> handle(HttpMediaTypeNotSupportedException e) {
//        logger.info("unsupported media type", e);
//        return Result.failure(ResultCode.UNSUPPORT_MEDIA, e.getMessage());
//    }
//
//    /**
//     * 500 - Internal Server Error
//     */
//    //@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ExceptionHandler(Throwable.class)
//    public Result<Void> handle(Throwable t) {
//        if (t instanceof IllegalArgumentException) {
//            logger.info("", t);
//            return Result.failure(ResultCode.BAD_REQUEST, t.getMessage());
//        } else if (t instanceof BasicException) {
//            logger.info("", t);
//            return Result.failure(((BasicException) t).getCode(), t.getMessage());
//        }
//
//        logger.error("server error", t);
//        String msg = logger.isInfoEnabled() && StringUtils.isNotBlank(t.getMessage())
//                     ? t.getMessage() : ERROR_MSG;
//        return Result.failure(ResultCode.SERVER_ERROR, msg);
//    }
//}