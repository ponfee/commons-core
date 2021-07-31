package code.ponfee.commons.model;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The common request parameter resolver for map model
 * 
 * @author Ponfee
 */
public class TypedMapMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return TypedParameter.class == parameter.getParameterType();
    }

    @Override
    public TypedParameter resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Map<String, String[]> params = webRequest.getParameterMap();
        Map<String, Object> map = new LinkedHashMap<>(params.size(), 1);
        params.forEach((key, value) -> map.put(key, value.length == 1 ? value[0] : value));
        return new TypedParameter(map);
    }

}
