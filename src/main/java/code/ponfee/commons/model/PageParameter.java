package code.ponfee.commons.model;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static code.ponfee.commons.model.PageHandler.DEFAULT_LIMIT;
import static code.ponfee.commons.model.PageHandler.DEFAULT_OFFSET;
import static code.ponfee.commons.model.PageHandler.DEFAULT_PAGE_NUM;
import static code.ponfee.commons.model.PageHandler.DEFAULT_PAGE_SIZE;

/**
 * 分页请求参数封装类（不能继承Map，否则会被内置Map解析器优先处理）
 * 
 * @see org.springframework.web.method.annotation.MapMethodProcessor#supportsParameter(org.springframework.core.MethodParameter)
 * @see org.springframework.web.method.annotation.RequestParamMapMethodArgumentResolver#supportsParameter(org.springframework.core.MethodParameter)
 * 
 * @author Ponfee
 */
public class PageParameter extends TypedParameter {

    private static final long serialVersionUID = 6176654946390797217L;

    public static final List<String> PAGE_PARAMS = ImmutableList.of(
        DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE, DEFAULT_OFFSET, DEFAULT_LIMIT
    );

    public static final String SORT_PARAM = "sort";

    private static final String[] ORDER_DIRECTION = { "ASC", "DESC" };

    private int pageNum  = -1;
    private int pageSize = -1;

    private int offset   = -1;
    private int limit    = -1;

    private String sort  = null;

    // -----------------------------------------------------constructors
    public PageParameter() {
        this(new LinkedHashMap<>());
    }

    public PageParameter(int initialCapacity) {
        this(new LinkedHashMap<>(initialCapacity));
    }

    public PageParameter(int initialCapacity, int loadFactor) {
        this(new LinkedHashMap<>(initialCapacity, loadFactor));
    }

    public PageParameter(Map<String, Object> params) {
        super(params);
    }

    // ----------------------------------------------------- methods
    public PageParameter searchAll() {
        this.setPageNum(1);
        this.setPageSize(0);
        this.setLimit(0);
        this.setOffset(0);
        return this;
    }

    // prevent sql inject
    public void validateSort(String... allows) {
        if (ArrayUtils.isEmpty(allows)) {
            return;
        }

        String sort = this.getString(SORT_PARAM);
        if (StringUtils.isBlank(sort)) {
            return;
        }

        String[] orders = sort.split(",");
        for (String order : orders) {
            if (StringUtils.isBlank(order)) {
                continue;
            }

            String[] array = order.trim()/*.replaceAll("\\s{2,}", " ")*/.split(" ", 2);
            if (   !ArrayUtils.contains(allows, array[0].trim())
                || (array.length == 2 && !ArrayUtils.contains(ORDER_DIRECTION, array[1].trim().toUpperCase()))
            ) {
                throw new IllegalArgumentException("Illegal sort param: " + sort);
            }
        }
    }

    // ----------------------------------------------page operators
    public int getPageNum() {
        return this.pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
        this.put(DEFAULT_PAGE_NUM, pageNum);
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        this.put(DEFAULT_PAGE_SIZE, pageSize);
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
        this.put(DEFAULT_OFFSET, offset);
    }

    public int getLimit() {
        return this.limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
        this.put(DEFAULT_LIMIT, limit);
    }

    public String getSort() {
        return this.sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
        this.put(SORT_PARAM, sort);
    }

}
