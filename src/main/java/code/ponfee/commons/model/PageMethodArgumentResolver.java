package code.ponfee.commons.model;

import static code.ponfee.commons.model.PageHandler.DEFAULT_LIMIT;
import static code.ponfee.commons.model.PageHandler.DEFAULT_PAGE_SIZE;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.model.PageRequestParams;
import code.ponfee.commons.reflect.Fields;

/**
 * 分页查询方法参数解析
 * 
 * https://blog.csdn.net/lqzkcx3/article/details/78794636
 * 
 * @see org.springframework.web.method.support.HandlerMethodArgumentResolverComposite
 * @see org.springframework.web.method.annotation.RequestParamMapMethodArgumentResolver
 * @see org.springframework.web.method.annotation.RequestParamMethodArgumentResolver
 * 
 * 被注入到RequestMappingHandlerAdapter中的argumentResolvers字段
 * 
 * @author Ponfee
 */
public class PageMethodArgumentResolver implements HandlerMethodArgumentResolver {

    // pageSize(or limit) has not spec or spec less 1 then use this default value
    private static final int DEFAULT_SIZE = 20;

    private static final List<String> SIZE_PARAMS = Arrays.asList(
        DEFAULT_PAGE_SIZE, DEFAULT_LIMIT
    );

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        //return parameter.hasParameterAnnotation(PageRequestParam.class);
        return PageRequestParams.class == parameter.getParameterType();
    }

    @Override
    public PageRequestParams resolveArgument(
        MethodParameter parameter, ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        PageRequestParams page = new PageRequestParams();
        webRequest.getParameterMap().forEach((key, value) -> {
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

    //@Target(ElementType.PARAMETER)
    //@Retention(RetentionPolicy.RUNTIME)
    //@Documented
    //public static @interface PageRequestParam {}

}
