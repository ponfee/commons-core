package code.ponfee.commons.web;

//Copyright (c) 1995-2018 Mort Bay Consulting Pty. Ltd.

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.util.RegexUtils;
import code.ponfee.commons.util.Strings;

/**
 * Implementation of the
 * <a href="http://www.w3.org/TR/cors/">cross-origin resource sharing</a>.
 * <p>
 * A typical example is to use this filter to allow cross-domain
 * <a href="http://cometd.org">cometd</a> communication using the standard
 * long polling transport instead of the JSONP transport (that is less
 * efficient and less reactive to failures).
 * <p>
 * This filter allows the following configuration parameters:
 * <dl>
 * <dt>allowedOrigins</dt>
 * <dd>a comma separated list of origins that are
 * allowed to access the resources. Default value is <b>*</b>, meaning all
 * origins.    Note that using wild cards can result in security problems
 * for requests identifying hosts that do not exist. 
 * <p>
 * If an allowed origin contains one or more * characters (for example
 * http://*.domain.com), then "*" characters are converted to ".*", "."
 * characters are escaped to "\." and the resulting allowed origin
 * interpreted as a regular expression.
 * <p>
 * Allowed origins can therefore be more complex expressions such as
 * https?://*.domain.[a-z]{3} that matches http or https, multiple subdomains
 * and any 3 letter top-level domain (.com, .net, .org, etc.).</dd>
 * 
 * <dt>allowedTimingOrigins</dt>
 * <dd>a comma separated list of origins that are
 * allowed to time the resource. Default value is the empty string, meaning
 * no origins.
 * <p>
 * The check whether the timing header is set, will be performed only if
 * the user gets general access to the resource using the <b>allowedOrigins</b>.
 *
 * <dt>allowedMethods</dt>
 * <dd>a comma separated list of HTTP methods that
 * are allowed to be used when accessing the resources. Default value is
 * <b>GET,POST,HEAD</b></dd>
 * 
 * 
 * <dt>allowedHeaders</dt>
 * <dd>a comma separated list of HTTP headers that
 * are allowed to be specified when accessing the resources. Default value
 * is <b>X-Requested-With,Content-Type,Accept,Origin</b>. If the value is a single "*",
 * this means that any headers will be accepted.</dd>
 * 
 * <dt>preflightMaxAge</dt>
 * <dd>the number of seconds that preflight requests
 * can be cached by the client. Default value is <b>1800</b> seconds, or 30
 * minutes</dd>
 * 
 * <dt>allowCredentials</dt>
 * <dd>a boolean indicating if the resource allows
 * requests with credentials. Default value is <b>true</b></dd>
 * 
 * <dt>exposedHeaders</dt>
 * <dd>a comma separated list of HTTP headers that
 * are allowed to be exposed on the client. Default value is the
 * <b>empty list</b></dd>
 * 
 * <dt>chainPreflight</dt>
 * <dd>if true preflight requests are chained to their
 * target resource for normal handling (as an OPTION request).  Otherwise the
 * filter will response to the preflight. Default is <b>true</b>.</dd>
 * 
 * </dl>
 * A typical configuration could be:
 * 
 * Plan one:
 * <pre>
 *  〈filter>
 *    〈filter-name>cross-origin</filter-name>
 *    〈filter-class>code.ponfee.commons.web.CrossOriginFilter</filter-class>
 *    〈init-param>
 *      <param-name>allowedOrigins</param-name>
 *      〈param-value>*</param-value>  
 *    〈/init-param>
 *    〈init-param>
 *      〈param-name>allowedMethods</param-name>
 *      〈param-value>GET,POST,HEAD</param-value>
 *    〈/init-param>
 *    〈init-param>
 *      〈param-name>allowedHeaders</param-name>
 *      〈param-value>X-Requested-With,Content-Type,Accept,Origin</param-value>
 *    〈/init-param>
 *  〈/filter>
 *  〈filter-mapping>
 *    〈filter-name>cross-origin</filter-name>
 *    〈url-pattern>/*</url-pattern>
 *  〈/filter-mapping>
 * </pre>
 * 
 * Modify from org.eclipse.jetty.servlets.CrossOriginFilter
 * @see org.eclipse.jetty.servlets.CrossOriginFilter
 * 
 * 
 * Plan two: Spring mvc xml config
 *  <mvc:cors>
 *    <mvc:mapping path="/**" 
 *      allowed-origins="${allowed_origins}" 
 *      allowed-methods="GET,POST,PUT,DELETE,HEAD,OPTIONS" 
 *      allowed-headers="X-Requested-With,Content-Type,Accept,Origin,LastModified,Authorization" 
 *      exposed-headers="SetCookie" allow-credentials="true" />
 *    <mvc:mapping path="/api/**" allowed-origins="http://domain1.com" />
 *  </mvc:cors>
 * 
 */
public class CrossOriginFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(CrossOriginFilter.class);

    // Request headers
    private static final String ORIGIN_HEADER = "Origin";
    public static final String ACCESS_CONTROL_REQUEST_METHOD_HEADER = "Access-Control-Request-Method";
    public static final String ACCESS_CONTROL_REQUEST_HEADERS_HEADER = "Access-Control-Request-Headers";
    // Response headers
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    public static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";
    public static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers";
    public static final String ACCESS_CONTROL_MAX_AGE_HEADER = "Access-Control-Max-Age";
    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";
    public static final String ACCESS_CONTROL_EXPOSE_HEADERS_HEADER = "Access-Control-Expose-Headers";
    public static final String TIMING_ALLOW_ORIGIN_HEADER = "Timing-Allow-Origin";
    // Implementation constants
    public static final String ALLOWED_ORIGINS_PARAM = "allowedOrigins";
    public static final String ALLOWED_TIMING_ORIGINS_PARAM = "allowedTimingOrigins";
    public static final String ALLOWED_METHODS_PARAM = "allowedMethods";
    public static final String ALLOWED_HEADERS_PARAM = "allowedHeaders";
    public static final String PREFLIGHT_MAX_AGE_PARAM = "preflightMaxAge";
    public static final String ALLOW_CREDENTIALS_PARAM = "allowCredentials";
    public static final String EXPOSED_HEADERS_PARAM = "exposedHeaders";
    public static final String CHAIN_PREFLIGHT_PARAM = "chainPreflight";
    private static final String ANY_ORIGIN = "*";
    private static final String DEFAULT_ALLOWED_ORIGINS = "*";
    private static final String DEFAULT_ALLOWED_TIMING_ORIGINS = "";
    private static final List<String> SIMPLE_HTTP_METHODS = Arrays.asList("GET", "POST", "HEAD");
    private static final List<String> DEFAULT_ALLOWED_METHODS = Arrays.asList("GET", "POST", "HEAD");
    private static final List<String> DEFAULT_ALLOWED_HEADERS = Arrays.asList("X-Requested-With", "Content-Type", "Accept", "Origin");

    private boolean anyOriginAllowed;
    private boolean anyTimingOriginAllowed;
    private boolean anyHeadersAllowed;
    private List<String> allowedOrigins = new ArrayList<>();
    private List<String> allowedTimingOrigins = new ArrayList<>();
    private List<String> allowedMethods = new ArrayList<>();
    private List<String> allowedHeaders = new ArrayList<>();
    private List<String> exposedHeaders = new ArrayList<>();
    private int preflightMaxAge;
    private boolean allowCredentials;
    private boolean chainPreflight;

    @Override
    public void init(FilterConfig config) {
        String allowedOriginsConfig = config.getInitParameter(ALLOWED_ORIGINS_PARAM);
        String allowedTimingOriginsConfig = config.getInitParameter(ALLOWED_TIMING_ORIGINS_PARAM);

        anyOriginAllowed = generateAllowedOrigins(allowedOrigins, allowedOriginsConfig, DEFAULT_ALLOWED_ORIGINS);
        anyTimingOriginAllowed = generateAllowedOrigins(allowedTimingOrigins, allowedTimingOriginsConfig, DEFAULT_ALLOWED_TIMING_ORIGINS);

        String allowedMethodsConfig = config.getInitParameter(ALLOWED_METHODS_PARAM);
        if (allowedMethodsConfig == null) {
            allowedMethods.addAll(DEFAULT_ALLOWED_METHODS);
        } else {
            allowedMethods.addAll(Arrays.asList(Strings.csvSplit(allowedMethodsConfig)));
        }

        String allowedHeadersConfig = config.getInitParameter(ALLOWED_HEADERS_PARAM);
        if (allowedHeadersConfig == null) {
            allowedHeaders.addAll(DEFAULT_ALLOWED_HEADERS);
        } else if ("*".equals(allowedHeadersConfig)) {
            anyHeadersAllowed = true;
        } else {
            allowedHeaders.addAll(Arrays.asList(Strings.csvSplit(allowedHeadersConfig)));
        }

        String preflightMaxAgeConfig = config.getInitParameter(PREFLIGHT_MAX_AGE_PARAM);
        if (preflightMaxAgeConfig == null) {
            preflightMaxAgeConfig = "1800"; // Default is 30 minutes
        }
        try {
            preflightMaxAge = Integer.parseInt(preflightMaxAgeConfig);
        } catch (NumberFormatException x) {
            logger.info("Cross-origin filter, could not parse '{}' parameter as integer: {}", 
                        PREFLIGHT_MAX_AGE_PARAM, preflightMaxAgeConfig);
        }

        String allowedCredentialsConfig = config.getInitParameter(ALLOW_CREDENTIALS_PARAM);
        if (allowedCredentialsConfig == null) {
            allowedCredentialsConfig = "true";
        }
        allowCredentials = Boolean.parseBoolean(allowedCredentialsConfig);

        String exposedHeadersConfig = config.getInitParameter(EXPOSED_HEADERS_PARAM);
        if (exposedHeadersConfig == null) {
            exposedHeadersConfig = "";
        }
        exposedHeaders.addAll(Arrays.asList(Strings.csvSplit(exposedHeadersConfig)));

        String chainPreflightConfig = config.getInitParameter(CHAIN_PREFLIGHT_PARAM);
        if (chainPreflightConfig == null) {
            chainPreflightConfig = "true";
        }
        chainPreflight = Boolean.parseBoolean(chainPreflightConfig);

        if (logger.isDebugEnabled()) {
            logger.debug("Cross-origin filter configuration: " +
                ALLOWED_ORIGINS_PARAM + " = " + allowedOriginsConfig + ", " +
                ALLOWED_TIMING_ORIGINS_PARAM + " = " + allowedTimingOriginsConfig + ", " +
                ALLOWED_METHODS_PARAM + " = " + allowedMethodsConfig + ", " +
                ALLOWED_HEADERS_PARAM + " = " + allowedHeadersConfig + ", " +
                PREFLIGHT_MAX_AGE_PARAM + " = " + preflightMaxAgeConfig + ", " +
                ALLOW_CREDENTIALS_PARAM + " = " + allowedCredentialsConfig + "," +
                EXPOSED_HEADERS_PARAM + " = " + exposedHeadersConfig + "," +
                CHAIN_PREFLIGHT_PARAM + " = " + chainPreflightConfig);
        }
    }

    private boolean generateAllowedOrigins(List<String> allowedOriginStore, 
                                           String allowedOriginsConfig, String defaultOrigin) {
        if (allowedOriginsConfig == null) {
            allowedOriginsConfig = defaultOrigin;
        }
        String[] allowedOrigins = Strings.csvSplit(allowedOriginsConfig);
        for (String allowedOrigin : allowedOrigins) {
            if (allowedOrigin.length() > 0) {
                if (ANY_ORIGIN.equals(allowedOrigin)) {
                    allowedOriginStore.clear();
                    return true;
                } else {
                    allowedOriginStore.add(allowedOrigin);
                }
            }
        }
        return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
        throws IOException, ServletException {
        handle((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain) 
        throws IOException, ServletException {
        String origin = request.getHeader(ORIGIN_HEADER);
        // Is it a cross origin request ?
        if (origin != null && isEnabled(request)) {
            if (anyOriginAllowed || originMatches(allowedOrigins, origin)) {
                if (isSimpleRequest(request)) {
                    logger.debug("Cross-origin request to {} is a simple cross-origin request", 
                                 request.getRequestURI());
                    handleSimpleResponse(request, response, origin);
                } else if (isPreflightRequest(request)) {
                    logger.debug("Cross-origin request to {} is a preflight cross-origin request", 
                                 request.getRequestURI());
                    handlePreflightResponse(request, response, origin);
                    if (chainPreflight) {
                        logger.debug("Preflight cross-origin request to {} forwarded to application", 
                                     request.getRequestURI());
                    } else {
                        return;
                    }
                } else {
                    logger.debug("Cross-origin request to {} is a non-simple cross-origin request", 
                                 request.getRequestURI());
                    handleSimpleResponse(request, response, origin);
                }

                if (anyTimingOriginAllowed || originMatches(allowedTimingOrigins, origin)) {
                    response.setHeader(TIMING_ALLOW_ORIGIN_HEADER, origin);
                } else {
                    logger.debug("Cross-origin request to {} with origin {} does not match allowed timing origins {}", 
                                 request.getRequestURI(), origin, allowedTimingOrigins);
                }
            } else {
                logger.debug("Cross-origin request to {} with origin {} does not match allowed origins {}", 
                             request.getRequestURI(), origin, allowedOrigins);
            }
        }

        chain.doFilter(request, response);
    }

    protected boolean isEnabled(HttpServletRequest request) {
        // WebSocket clients such as Chrome 5 implement a version of the WebSocket
        // protocol that does not accept extra response headers on the upgrade response
        for (Enumeration<String> elm = request.getHeaders("Connection"); elm.hasMoreElements();) {
            String connection = elm.nextElement();
            if ("Upgrade".equalsIgnoreCase(connection)) {
                for (Enumeration<String> upg = request.getHeaders("Upgrade"); upg.hasMoreElements();) {
                    String upgrade = upg.nextElement();
                    if ("WebSocket".equalsIgnoreCase(upgrade)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean originMatches(List<String> allowedOrigins, String originList) {
        if (originList.trim().length() == 0) {
            return false;
        }

        String[] origins = originList.split(" ");
        for (String origin : origins) {
            if (origin.trim().length() == 0) {
                continue;
            }

            for (String allowedOrigin : allowedOrigins) {
                if (allowedOrigin.contains("*")) {
                    // we want to be greedy here to match multiple subdomains, thus we use .*
                    String regex = allowedOrigin.replace(".", "\\.").replace("*", ".*");
                    if (RegexUtils.matches(origin, regex)) {
                        return true;
                    }
                } else if (allowedOrigin.equals(origin)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSimpleRequest(HttpServletRequest request) {
        String method = request.getMethod();
        if (SIMPLE_HTTP_METHODS.contains(method)) {
            // TODO: implement better detection of simple headers
            // The specification says that for a request to be simple, custom request headers must be simple.
            // Here for simplicity I just check if there is a Access-Control-Request-Method header,
            // which is required for preflight requests
            return request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER) == null;
        }
        return false;
    }

    private boolean isPreflightRequest(HttpServletRequest request) {
        String method = request.getMethod();
        if (!"OPTIONS".equalsIgnoreCase(method)) {
            return false;
        }
        return request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER) != null;
    }

    private void handleSimpleResponse(HttpServletRequest request, HttpServletResponse response, String origin) {
        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
        //W3C CORS spec http://www.w3.org/TR/cors/#resource-implementation
        if (!anyOriginAllowed) {
            response.addHeader("Vary", ORIGIN_HEADER);
        }
        if (allowCredentials) {
            response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        }
        if (!exposedHeaders.isEmpty()) {
            response.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, commify(exposedHeaders));
        }
    }

    private void handlePreflightResponse(HttpServletRequest request, HttpServletResponse response, String origin) {
        boolean methodAllowed = isMethodAllowed(request);

        if (!methodAllowed) {
            return;
        }
        List<String> headersRequested = getAccessControlRequestHeaders(request);
        boolean headersAllowed = areHeadersAllowed(headersRequested);
        if (!headersAllowed) {
            return;
        }
        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
        //W3C CORS spec http://www.w3.org/TR/cors/#resource-implementation
        if (!anyOriginAllowed) {
            response.addHeader("Vary", ORIGIN_HEADER);
        }
        if (allowCredentials) {
            response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        }
        if (preflightMaxAge > 0) {
            response.setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, String.valueOf(preflightMaxAge));
        }
        response.setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, commify(allowedMethods));
        if (anyHeadersAllowed) {
            response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, commify(headersRequested));
        } else {
            response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, commify(allowedHeaders));
        }
    }

    private boolean isMethodAllowed(HttpServletRequest request) {
        String accessControlRequestMethod = request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER);
        logger.debug("{} is {}", ACCESS_CONTROL_REQUEST_METHOD_HEADER, accessControlRequestMethod);
        boolean result = false;
        if (accessControlRequestMethod != null) {
            result = allowedMethods.contains(accessControlRequestMethod);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Method {} is" + (result ? "" : " not") + " among allowed methods {}", 
                         accessControlRequestMethod, allowedMethods);
        }
        return result;
    }

    private List<String> getAccessControlRequestHeaders(HttpServletRequest request) {
        String accessControlRequestHeaders = request.getHeader(ACCESS_CONTROL_REQUEST_HEADERS_HEADER);
        logger.debug("{} is {}", ACCESS_CONTROL_REQUEST_HEADERS_HEADER, accessControlRequestHeaders);
        if (accessControlRequestHeaders == null) {
            return Collections.emptyList();
        }

        List<String> requestedHeaders = new ArrayList<>();
        String[] headers = Strings.csvSplit(accessControlRequestHeaders);
        for (String header : headers) {
            String h = header.trim();
            if (h.length() > 0) {
                requestedHeaders.add(h);
            }
        }
        return requestedHeaders;
    }

    private boolean areHeadersAllowed(List<String> requestedHeaders) {
        if (anyHeadersAllowed) {
            logger.debug("Any header is allowed");
            return true;
        }

        boolean result = true;
        for (String requestedHeader : requestedHeaders) {
            boolean headerAllowed = false;
            for (String allowedHeader : allowedHeaders) {
                if (requestedHeader.equalsIgnoreCase(allowedHeader.trim())) {
                    headerAllowed = true;
                    break;
                }
            }
            if (!headerAllowed) {
                result = false;
                break;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Headers [{}] are" + (result ? "" : " not") + " among allowed headers {}", 
                         requestedHeaders, allowedHeaders);
        }
        return result;
    }

    private String commify(List<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strings.size(); ++i) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(strings.get(i));
        }
        return builder.toString();
    }

    @Override
    public void destroy() {
        anyOriginAllowed = false;
        allowedOrigins.clear();
        allowedMethods.clear();
        allowedHeaders.clear();
        preflightMaxAge = 0;
        allowCredentials = false;
    }
}
