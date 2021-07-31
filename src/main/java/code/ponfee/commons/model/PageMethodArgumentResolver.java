package code.ponfee.commons.model;

import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.reflect.Fields;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.Map;

import static code.ponfee.commons.model.PageHandler.DEFAULT_LIMIT;
import static code.ponfee.commons.model.PageHandler.DEFAULT_PAGE_SIZE;

/**
 * 分页查询方法参数解析，在spring-mvc配置文件中做如下配置
 *  <mvc:annotation-driven>
 *    <mvc:argument-resolvers>
 *      <bean class="code.ponfee.commons.model.PageMethodArgumentResolver" />
 *    </mvc:argument-resolvers>
 *  </mvc:annotation-driven>
 * 
 * 配置完之后PageMethodArgumentResolver这个spring bean会被注入到RequestMappingHandlerAdapter.argumentResolvers中
 * 
 * https://blog.csdn.net/lqzkcx3/article/details/78794636
 * 
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
 * @see org.springframework.web.method.support.HandlerMethodArgumentResolverComposite
 * @see org.springframework.web.method.annotation.RequestParamMapMethodArgumentResolver
 * @see org.springframework.web.method.annotation.RequestParamMethodArgumentResolver
 * 
 * @author Ponfee
 */
public class PageMethodArgumentResolver implements HandlerMethodArgumentResolver {

    // pageSize(or limit) has not spec or spec less 1 then use this default value
    private static final int DEFAULT_SIZE = 20;

    private static final List<String> SIZE_PARAMS = ImmutableList.of(
        DEFAULT_PAGE_SIZE, DEFAULT_LIMIT
    );

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        //return parameter.hasParameterAnnotation(PageRequestParam.class);
        return PageParameter.class == parameter.getParameterType();
    }

    @Override
    public PageParameter resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                         NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Map<String, String[]> params = webRequest.getParameterMap();
        PageParameter pp = new PageParameter(params.size(), 1);
        params.forEach((key, value) -> {
            if (PageParameter.PAGE_PARAMS.contains(key)) {
                int value0 = Numbers.toInt(value[0], 0);
                if (value0 < 1 && SIZE_PARAMS.contains(key)) {
                    value0 = DEFAULT_SIZE;
                }
                Fields.put(pp, key, value0);
                pp.put(key, value0);
            } else if (PageParameter.SORT_PARAM.equalsIgnoreCase(key)) {
                // value：“name ASC, age DESC”
                String value0 = StringUtils.join(value, ',').trim();
                Fields.put(pp, PageParameter.SORT_PARAM, value0);
                pp.put(PageParameter.SORT_PARAM, value0);
            } else {
                pp.put(key, value.length == 1 ? value[0].trim() : value);
            }
        });

        if (pp.getLimit() > 0) { // use limit query
            if (pp.getLimit() > PageHandler.MAX_SIZE) {
                pp.setLimit(PageHandler.MAX_SIZE);
            }
            if (pp.getOffset() < 0) {
                pp.setOffset(0); // start with 0
            }
        } else {
            if (pp.getPageSize() < 1) {
                pp.setPageSize(DEFAULT_SIZE);
            } else if (pp.getPageSize() > PageHandler.MAX_SIZE) {
                pp.setPageSize(PageHandler.MAX_SIZE);
            }
            if (pp.getPageNum() < 1) {
                pp.setPageNum(1); // start with 1
            }
        }

        return pp;
    }

}
