package code.ponfee.commons.model;

import java.util.Dictionary;
import java.util.Map;

import javax.annotation.Nonnull;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.reflect.Fields;

/**
 * 分页参数处理类
 * 基于github上的mybatis分页工具
 * https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md
 * 
 * @author Ponfee
 */
public final class PageHandler {

    public static final String DEFAULT_PAGE_NUM = "pageNum";
    public static final String DEFAULT_PAGE_SIZE = "pageSize";
    public static final String DEFAULT_OFFSET = "offset";
    public static final String DEFAULT_LIMIT = "limit";

    public static final Map<String, Object> QUERY_ALL_PARAMS = ImmutableMap.of(
        DEFAULT_PAGE_NUM, 1, DEFAULT_PAGE_SIZE, 0, // start page number is 1
        DEFAULT_OFFSET, 0, DEFAULT_LIMIT, 0 // start offset is 0
    );

    public static final PageHandler NORMAL = new PageHandler(
        DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE, DEFAULT_OFFSET, DEFAULT_LIMIT
    );
    public static final int MAX_SIZE = 500;

    private final String paramPageNum;
    private final String paramPageSize;
    private final String paramOffset;
    private final String paramLimit;

    public PageHandler(String paramPageNum, String paramPageSize, 
                       String paramOffset, String paramLimit) {
        this.paramPageNum = paramPageNum;
        this.paramPageSize = paramPageSize;
        this.paramOffset = paramOffset;
        this.paramLimit = paramLimit;
    }

    /**
     * Handles the page parameters
     *  
     * @param params the params
     */
    public void handle(@Nonnull Object params) {
        Integer pageSize = getInt(params, paramPageSize);
        Integer limit = getInt(params, paramLimit);

        // 默认通过pageSize查询
        if (   (pageSize == null || pageSize < 0) 
            && (limit == null    || limit < 0)
        ) {
            pageSize = 0;
        }

        // 分页处理，pageSizeZero：默认值为false，当该参数设置为true时，如果pageSize=0或
        // RowBounds.limit=0就会查询出全部的结果（相当于没有执行分页查询，但是返回结果仍然是Page类型）
        if (pageSize != null && pageSize > -1) { // first priority use page size
            startPage(
                getInt(params, paramPageNum), Numbers.bounds(pageSize, 0, MAX_SIZE)
            );
        } else {
            offsetPage(
                getInt(params, paramOffset), Numbers.bounds(limit, 0, MAX_SIZE)
            );
        }
    }

    /**
     * Page query with pageNum and pageSize
     * pageSize=0时查询全部数据
     * 
     * @param pageNum the pageNum
     * @param pageSize the pageSize
     */
    public static void startPage(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 0) {
            pageSize = 0;
        }
        PageHelper.startPage(pageNum, pageSize);
    }

    /**
     * Page query with offset and limit
     * RowBounds.limit=0则会查询出全部的结果
     * 
     * @param offset the offset
     * @param limit the limit
     */
    public static void offsetPage(Integer offset, Integer limit) {
        if (offset == null || offset < 0) {
            offset = 0;
        }
        if (limit == null || limit < 0) {
            limit = 0;
        }
        PageHelper.offsetPage(offset, limit);
    }

    public static int computeTotalPages(long totalRecords, int pageSize) {
        return pageSize == 0 ? 0 : (int) ((totalRecords + pageSize - 1) / pageSize);
    }

    public static int computePageNum(long offset, int limit) {
        return limit == 0 ? 0 : (int) (offset / limit + 1);
    }

    public static int computeOffset(long pageNum, int pageSize) {
        return (int) ((pageNum - 1) * pageSize);
    }

    /**
     * Gets page number from java bean or map or dictionary
     * 
     * @param params the params collect object
     * @param name the param name
     * @return a value of name
     */
    private static Integer getInt(Object params, String name) {
        try {
            Object value;
            if (params instanceof Map) {
                /*Method get = params.getClass().getMethod("get", Object.class);
                get.setAccessible(true); // Guava ImmutableMap must be set accessible true
                value = get.invoke(params, name);*/
                value = ((Map<?, ?>) params).get(name);
            } else if (params instanceof Dictionary) {
                value = ((Dictionary<?, ?>) params).get(name);
            } else {
                value = Fields.get(params, name); // as java bean
            }
            return Numbers.toWrapInt(value);
        } catch (Exception e) {
            return null;
        }
    }

}
