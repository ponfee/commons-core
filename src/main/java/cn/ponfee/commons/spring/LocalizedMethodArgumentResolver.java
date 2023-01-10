/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.spring;

import cn.ponfee.commons.collect.Collects;
import cn.ponfee.commons.json.Jsons;
import cn.ponfee.commons.util.ObjectUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Localized method parameter for spring web {@code org.springframework.stereotype.Controller} methods.
 * <p>Can defined multiple object arguments for {@code org.springframework.web.bind.annotation.RequestMapping} method.
 *
 * @author Ponfee
 */
public class LocalizedMethodArgumentResolver implements HandlerMethodArgumentResolver {

    //private final WeakHashMap<NativeWebRequest, Map<String, Object>> resolvedCache = new WeakHashMap<>();

    private static final Set<String> QUERY_PARAMS = ImmutableSet.of(
        GET.name(), DELETE.name(), HEAD.name(), OPTIONS.name()
    );

    private static final String CACHE_ATTRIBUTE_KEY = "LOCALIZED_METHOD_ARGUMENTS";
    private static final Class<? extends Annotation> MARKED_ANNOTATION_TYPE = LocalizedMethodArguments.class;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (!(parameter.getExecutable() instanceof Method)) {
            return false;
        }

        return isAnnotationPresent(parameter.getMethod(), MARKED_ANNOTATION_TYPE)
            || isAnnotationPresent(parameter.getDeclaringClass(), MARKED_ANNOTATION_TYPE);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws IOException {
        Method method = parameter.getMethod();
        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        int parameterIndex = parameter.getParameterIndex();
        Object[] arguments;
        if (parameterIndex == 0) {
            arguments = resolveMethodParameters(method, httpServletRequest);
            if (method.getParameterCount() > 1) {
                // CACHE_KEY_PREFIX + method.toString()
                httpServletRequest.setAttribute(CACHE_ATTRIBUTE_KEY, arguments);
            }
        } else {
            arguments = (Object[]) httpServletRequest.getAttribute(CACHE_ATTRIBUTE_KEY);
        }

        return Collects.get(arguments, parameterIndex);
    }

    private static Object[] resolveMethodParameters(Method method, HttpServletRequest request) throws IOException {
        if (QUERY_PARAMS.contains(request.getMethod())) {
            return resolveQueryString(method, request.getParameterMap());
        } else {
            String body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
            if (StringUtils.isEmpty(body)) {
                return resolveQueryString(method, request.getParameterMap());
            } else {
                return resolveRequestBody(method, body);
            }
        }
    }

    private static Object[] resolveQueryString(Method method, Map<String, String[]> parameterMap) {
        int parameterCount = method.getParameterCount();
        Object[] arguments = new Object[parameterCount];
        for (int i = 0; i < parameterCount; i++) {
            String argName = "args[" + i + "]";
            String[] array = parameterMap.get(argName);
            Assert.isTrue(
                array == null || array.length <= 1,
                "Argument cannot be multiple value, name: " + argName + ", value: " + Jsons.toJson(array)
            );
            String argValue = Collects.get(array, 0);
            Type argType = method.getGenericParameterTypes()[i];
            if (argValue == null) {
                // if basic type then set default value
                arguments[i] = (argType instanceof Class<?>) ? ObjectUtils.cast(null, (Class<?>) argType) : null;
            } else {
                arguments[i] = Jsons.fromJson(argValue, argType);
            }
        }
        return arguments;
    }

    private static Object[] resolveRequestBody(Method method, String body) {
        return JSON.parseArray(body, method.getGenericParameterTypes()).toArray();

        /*
        JsonNode jsonNode = Jsons.NORMAL.objectMapper().readTree(body);
        Assert.isTrue(jsonNode.isArray(), "Request parameter must be json array.");
        ArrayNode arrayNode = (ArrayNode) jsonNode;
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Assert.isTrue(arrayNode.size() == genericParameterTypes.length, "Request parameter size inconsistent.");
        int length = genericParameterTypes.length;
        Object[] arguments = new Object[length];
        for (int i = 0; i < length; i++) {
            arguments[i] = Jsons.NORMAL.parse(arrayNode.get(i).toString(), genericParameterTypes[i]);
        }
        return arguments;
        */
    }

    private static boolean isAnnotationPresent(Method method, Class<? extends Annotation> annotationType) {
        return AnnotationUtils.findAnnotation(method, annotationType) != null;
    }

    private static boolean isAnnotationPresent(Class<?> clazz, Class<? extends Annotation> annotationType) {
        return AnnotationUtils.findAnnotation(clazz, annotationType) != null;
    }

}
