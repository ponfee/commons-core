/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

//package cn.ponfee.commons.web;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.Collections;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.web.servlet.ModelAndView;
//import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
//import org.springframework.web.util.WebUtils;
//
//import cn.ponfee.commons.exception.BasicException;
//import cn.ponfee.commons.exception.Throwables;
//import cn.ponfee.commons.json.Jsons;
//import cn.ponfee.commons.model.Result;
//import cn.ponfee.commons.model.ResultCode;
//
///**
// * Spring mvc 全局异常处理方式
// * 更改自：org.springframework.web.servlet.handler.SimpleMappingExceptionResolver
// * 
// * @author Ponfee
// */
//public class GlobalExceptionResolver extends AbstractHandlerExceptionResolver {
//
//    public static final String ERROR_MSG = "系统异常，请与管理员联系";
//
//    /** The default name of the exception attribute: "exception". */
//    private Properties exceptionMappings;
//
//    private Class<?>[] excludedExceptions;
//
//    private String defaultErrorView;
//
//    private Integer defaultStatusCode;
//
//    private Map<String, Integer> statusCodes = new HashMap<>();
//
//    private String exceptionAttribute = "exception";
//
//    private String stackTraceAttribute = "stackTrace";
//
//    private String resolverType = "modeView";
//
//    //private static Logger logger = LoggerFactory.getLogger(CespHandlerExceptionResolver.class);
//
//    /**
//     * Set the mappings between exception class names and error view names. The exception class name can be a substring, with no wildcard support at present. A
//     * value of "ServletException" would match {@code javax.servlet.ServletException} and subclasses, for example.
//     * <p>
//     * <b>NB:</b> Consider carefully how specific the pattern is, and whether to include package information (which isn't mandatory). For example, "Exception"
//     * will match nearly anything, and will probably hide other rules. "java.lang.Exception" would be correct if "Exception" was meant to define a rule for all
//     * checked exceptions. With more unusual exception names such as "BaseBusinessException" there's no need to use a FQN.
//     * @param mappings exception patterns (can also be fully qualified class names) as keys, and error view names as values
//     */
//    public void setExceptionMappings(Properties mappings) {
//        this.exceptionMappings = mappings;
//    }
//
//    /**
//     * Set one or more exceptions to be excluded from the exception mappings. Excluded exceptions are checked first and if one of them equals the actual
//     * exception, the exception will remain unresolved.
//     * @param excludedExceptions one or more excluded exception types
//     */
//    public void setExcludedExceptions(Class<?>... excludedExceptions) {
//        this.excludedExceptions = excludedExceptions;
//    }
//
//    /**
//     * Set the name of the default error view. This view will be returned if no specific mapping was found.
//     * <p>
//     * Default is none.
//     */
//    public void setDefaultErrorView(String defaultErrorView) {
//        this.defaultErrorView = defaultErrorView;
//    }
//
//    /**
//     * Set the HTTP status code that this exception resolver will apply for a given resolved error view. Keys are view names; values are status codes.
//     * <p>
//     * Note that this error code will only get applied in case of a top-level request. It will not be set for an include request, since the HTTP status cannot
//     * be modified from within an include.
//     * <p>
//     * If not specified, the default status code will be applied.
//     * @see #setDefaultStatusCode(int)
//     */
//    public void setStatusCodes(Properties statusCodes) {
//        for (Enumeration<?> enumeration = statusCodes.propertyNames(); enumeration.hasMoreElements();) {
//            String viewName = (String) enumeration.nextElement();
//            Integer statusCode = new Integer(statusCodes.getProperty(viewName));
//            this.statusCodes.put(viewName, statusCode);
//        }
//    }
//
//    /**
//     * An alternative to {@link #setStatusCodes(Properties)} for use with Java-based configuration.
//     */
//    public void addStatusCode(String viewName, int statusCode) {
//        this.statusCodes.put(viewName, statusCode);
//    }
//
//    /**
//     * Returns the HTTP status codes provided via {@link #setStatusCodes(Properties)}. Keys are view names; values are status codes.
//     */
//    public Map<String, Integer> getStatusCodesAsMap() {
//        return Collections.unmodifiableMap(statusCodes);
//    }
//
//    /**
//     * Set the default HTTP status code that this exception resolver will apply if it resolves an error view and if there is no status code mapping defined.
//     * <p>
//     * Note that this error code will only get applied in case of a top-level request. It will not be set for an include request, since the HTTP status cannot
//     * be modified from within an include.
//     * <p>
//     * If not specified, no status code will be applied, either leaving this to the controller or view, or keeping the servlet engine's default of 200 (OK).
//     * @param defaultStatusCode HTTP status code value, for example 500 ({@link HttpServletResponse#SC_INTERNAL_SERVER_ERROR}) or 404 (
//     *        {@link HttpServletResponse#SC_NOT_FOUND})
//     * @see #setStatusCodes(Properties)
//     */
//    public void setDefaultStatusCode(int defaultStatusCode) {
//        this.defaultStatusCode = defaultStatusCode;
//    }
//
//    /**
//     * Set the name of the model attribute as which the exception should be exposed. Default is "exception".
//     * <p>
//     * This can be either set to a different attribute name or to {@code null} for not exposing an exception attribute at all.
//     */
//    public void setExceptionAttribute(String exceptionAttribute) {
//        this.exceptionAttribute = exceptionAttribute;
//    }
//
//    public void setStackTraceAttribute(String stackTraceAttribute) {
//        this.stackTraceAttribute = stackTraceAttribute;
//    }
//
//    public void setResolverType(String resolverType) {
//        this.resolverType = resolverType;
//    }
//
//    /**
//     * Actually resolve the given exception that got thrown during on handler execution, returning a ModelAndView that represents a specific error page if
//     * appropriate.
//     * <p>
//     * May be overridden in subclasses, in order to apply specific exception checks. Note that this template method will be invoked <i>after</i> checking
//     * whether this resolved applies ("mappedHandlers" etc), so an implementation may simply proceed with its actual exception handling.
//     * @param request current HTTP request
//     * @param response current HTTP response
//     * @param handler the executed handler, or {@code null} if none chosen at the time of the exception (for example, if multipart resolution failed)
//     * @param ex the exception that got thrown during handler execution
//     * @return a corresponding ModelAndView to forward to, or {@code null} for default processing
//     */
//    @Override
//    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
//        if (ex instanceof BasicException || ex instanceof IllegalArgumentException) {
//            logger.info("", ex);
//        } else {
//            logger.error("", ex);
//        }
//
//        switch (resolverType) {
//            case "application/json":
//            case "text/html":
//            case "text/plain":
//                int code = (ex instanceof BasicException) 
//                           ? ((BasicException) ex).getCode() 
//                           : ResultCode.SERVER_ERROR.getCode();
//                String msg = (ex instanceof BasicException) || logger.isInfoEnabled()
//                             ? ex.getMessage() : ERROR_MSG;
//                msg = StringUtils.isBlank(msg) ? ERROR_MSG : msg;
//                response.setContentType(resolverType + ";charset=UTF-8");
//                try (PrintWriter writer = response.getWriter()) {
//                    writer.print(Jsons.toJson(Result.failure(code, msg)));
//                } catch (IOException e) {
//                    logger.error("response write occur error", e);
//                }
//                return null;
//            default:
//                // Expose ModelAndView for chosen error view.
//                String viewName = determineViewName(ex, request);
//                ModelAndView mv = null;
//                if (viewName != null) {
//                    // Apply HTTP status code for error views, if specified.
//                    // Only apply it if we're processing a top-level request.
//                    Integer statusCode = determineStatusCode(request, viewName);
//                    if (statusCode != null) {
//                        applyStatusCodeIfPossible(request, response, statusCode);
//                    }
//
//                    mv = new ModelAndView(viewName);
//                    if (stackTraceAttribute != null) {
//                        mv.addObject(stackTraceAttribute, Throwables.getStackTrace(ex));
//                    }
//                    if (exceptionAttribute != null) {
//                        mv.addObject(exceptionAttribute, ex);
//                    }
//                }
//                return mv;
//        }
//    }
//
//    /**
//     * Determine the view name for the given exception, first checking against the {@link #setExcludedExceptions(Class[]) "excludedExecptions"}, then searching
//     * the {@link #setExceptionMappings "exceptionMappings"}, and finally using the {@link #setDefaultErrorView "defaultErrorView"} as a fallback.
//     * @param ex the exception that got thrown during handler execution
//     * @param request current HTTP request (useful for obtaining metadata)
//     * @return the resolved view name, or {@code null} if excluded or none found
//     */
//    protected String determineViewName(Exception ex, HttpServletRequest request) {
//        String viewName = null;
//        if (this.excludedExceptions != null) {
//            for (Class<?> excludedEx : this.excludedExceptions) {
//                if (excludedEx.equals(ex.getClass())) {
//                    return null;
//                }
//            }
//        }
//        // Check for specific exception mappings.
//        if (this.exceptionMappings != null) {
//            viewName = findMatchingViewName(this.exceptionMappings, ex);
//        }
//        // Return default error view else, if defined.
//        if (viewName == null && this.defaultErrorView != null) {
//            logger.debug("Resolving to default view '" + this.defaultErrorView + "' for exception of type [" + ex.getClass().getName() + "]");
//            viewName = this.defaultErrorView;
//        }
//        return viewName;
//    }
//
//    /**
//     * Find a matching view name in the given exception mappings.
//     * @param exceptionMappings mappings between exception class names and error view names
//     * @param ex the exception that got thrown during handler execution
//     * @return the view name, or {@code null} if none found
//     * @see #setExceptionMappings
//     */
//    protected String findMatchingViewName(Properties exceptionMappings, Exception ex) {
//        String viewName = null;
//        String dominantMapping = null;
//        int deepest = Integer.MAX_VALUE;
//        for (Enumeration<?> names = exceptionMappings.propertyNames(); names.hasMoreElements();) {
//            String exceptionMapping = (String) names.nextElement();
//            int depth = getDepth(exceptionMapping, ex);
//            if (depth >= 0 && (depth < deepest || (depth == deepest && dominantMapping != null && exceptionMapping.length() > dominantMapping.length()))) {
//                deepest = depth;
//                dominantMapping = exceptionMapping;
//                viewName = exceptionMappings.getProperty(exceptionMapping);
//            }
//        }
//        if (viewName != null) {
//            logger.debug("Resolving to view '" + viewName + "' for exception of type [" + ex.getClass().getName() + "], based on exception mapping ["
//                + dominantMapping + "]");
//        }
//        return viewName;
//    }
//
//    /**
//     * Return the depth to the superclass matching.
//     * <p>
//     * 0 means ex matches exactly. Returns -1 if there's no match. Otherwise, returns depth. Lowest depth wins.
//     */
//    protected int getDepth(String exceptionMapping, Exception ex) {
//        return getDepth(exceptionMapping, ex.getClass(), 0);
//    }
//
//    private int getDepth(String exceptionMapping, Class<?> exceptionClass, int depth) {
//        if (exceptionClass.getName().contains(exceptionMapping)) {
//            // Found it!
//            return depth;
//        }
//        // If we've gone as far as we can go and haven't found it...
//        if (exceptionClass.equals(Throwable.class)) {
//            return -1;
//        }
//        return getDepth(exceptionMapping, exceptionClass.getSuperclass(), depth + 1);
//    }
//
//    /**
//     * Determine the HTTP status code to apply for the given error view.
//     * <p>
//     * The default implementation returns the status code for the given view name (specified through the {@link #setStatusCodes(Properties) statusCodes}
//     * property), or falls back to the {@link #setDefaultStatusCode defaultStatusCode} if there is no match.
//     * <p>
//     * Override this in a custom subclass to customize this behavior.
//     * @param request current HTTP request
//     * @param viewName the name of the error view
//     * @return the HTTP status code to use, or {@code null} for the servlet container's default (200 in case of a standard error view)
//     * @see #setDefaultStatusCode
//     * @see #applyStatusCodeIfPossible
//     */
//    protected Integer determineStatusCode(HttpServletRequest request, String viewName) {
//        if (this.statusCodes.containsKey(viewName)) {
//            return this.statusCodes.get(viewName);
//        }
//        return this.defaultStatusCode;
//    }
//
//    /**
//     * Apply the specified HTTP status code to the given response, if possible (that is, if not executing within an include request).
//     * @param request current HTTP request
//     * @param response current HTTP response
//     * @param statusCode the status code to apply
//     * @see #determineStatusCode
//     * @see #setDefaultStatusCode
//     * @see HttpServletResponse#setStatus
//     */
//    protected void applyStatusCodeIfPossible(HttpServletRequest request, HttpServletResponse response, int statusCode) {
//        if (!WebUtils.isIncludeRequest(request)) {
//            logger.debug("Applying HTTP status code " + statusCode);
//            response.setStatus(statusCode);
//            request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, statusCode);
//        }
//    }
//}
