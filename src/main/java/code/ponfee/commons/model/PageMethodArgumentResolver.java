package code.ponfee.commons.model;

import static code.ponfee.commons.model.PageHandler.DEFAULT_LIMIT;
import static code.ponfee.commons.model.PageHandler.DEFAULT_PAGE_SIZE;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.google.common.collect.ImmutableList;

import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.reflect.Fields;

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
        return PageRequestParams.class == parameter.getParameterType();
    }

    @Override
    public PageRequestParams resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                             NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Map<String, String[]> params = webRequest.getParameterMap();
        PageRequestParams page = new PageRequestParams(params.size());
        params.forEach((key, value) -> {
            if (PageRequestParams.PAGE_PARAMS.contains(key)) {
                int value0 = Numbers.toInt(value[0], 0);
                if (value0 < 1 && SIZE_PARAMS.contains(key)) {
                    value0 = DEFAULT_SIZE;
                }
                Fields.put(page, key, value0);
                page.put(key, value0);
            } else if (PageRequestParams.SORT_PARAM.equalsIgnoreCase(key)) {
                // value：“name ASC, age DESC”
                String value0 = StringUtils.join(value, ',').trim();
                Fields.put(page, PageRequestParams.SORT_PARAM, value0);
                page.put(PageRequestParams.SORT_PARAM, value0);
            } else {
                page.put(key, value.length == 1 ? value[0].trim() : value);
            }
        });

        if (page.getLimit() > 0) { // use limit query
            if (page.getLimit() > PageHandler.MAX_SIZE) {
                page.setLimit(PageHandler.MAX_SIZE);
            }
            if (page.getOffset() < 0) {
                page.setOffset(0); // start with 0
            }
        } else {
            if (page.getPageSize() < 1) {
                page.setPageSize(DEFAULT_SIZE);
            } else if (page.getPageSize() > PageHandler.MAX_SIZE) {
                page.setPageSize(PageHandler.MAX_SIZE);
            }
            if (page.getPageNum() < 1) {
                page.setPageNum(1); // start with 1
            }
        }

        return page;
    }

}
