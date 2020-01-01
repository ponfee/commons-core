package code.ponfee.commons.model;

import static code.ponfee.commons.model.PageHandler.DEFAULT_LIMIT;
import static code.ponfee.commons.model.PageHandler.DEFAULT_OFFSET;
import static code.ponfee.commons.model.PageHandler.DEFAULT_PAGE_NUM;
import static code.ponfee.commons.model.PageHandler.DEFAULT_PAGE_SIZE;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;

import code.ponfee.commons.math.Numbers;

/**
 * 分页请求参数封装类（不能继承Map，否则会被ModelMethodProcessor先处理）
 * 
 * @author Ponfee
 */
public class PageRequestParams implements java.io.Serializable {

    private static final long serialVersionUID = 6176654946390797217L;

    public static final List<String> PAGE_PARAMS = ImmutableList.of(
        DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE, DEFAULT_OFFSET, DEFAULT_LIMIT
    );

    public static final String SORT_PARAM = "sort";

    private static final String[] ORDER_DIRECTION = { "ASC", "DESC" };

    private int pageNum = -1;
    private int pageSize = -1;

    private int offset = -1;
    private int limit = -1;

    private String sort = null;

    // 包含pageNum、pageSize、offset、limit、sort
    private final Map<String, Object> params = new LinkedHashMap<>();

    public PageRequestParams() {}

    public PageRequestParams(Map<String, Object> params) {
        this.params.putAll(params);
    }

    public PageRequestParams searchAll() {
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

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
        this.put(DEFAULT_PAGE_NUM, pageNum);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        this.put(DEFAULT_PAGE_SIZE, pageSize);
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
        this.put(DEFAULT_OFFSET, offset);
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
        this.put(DEFAULT_LIMIT, limit);
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
        this.put(SORT_PARAM, sort);
    }

    public Map<String, Object> getParams() {
        return params;
    }

    // ----------------------------------------------map operators
    public void put(String key, Object value) {
        this.params.put(key, value);
    }

    public Object get(String key) {
        return params.get(key);
    }

    public String getString(String key) {
        return Objects.toString(get(key), "");
    }

    public Integer getInt(String key) {
        return Numbers.toWrapInt(get(key));
    }

    public Double getDouble(String key) {
        return Numbers.toWrapDouble(get(key));
    }

    public Long getLong(String key) {
        return Numbers.toWrapLong(get(key));
    }

    public Float getFloat(String key) {
        return Numbers.toWrapFloat(get(key));
    }

    public Boolean getBoolean(String key) {
        return Numbers.toWrapBoolean(get(key));
    }

}
