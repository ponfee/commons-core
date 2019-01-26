package test.model;

import java.io.Serializable;

public class PageInfo implements Serializable {

    private static final long serialVersionUID = 587754556498974978L;

    // pagesize ，每一页显示多少
    private int showCount = 3;
    // 总页数
    private int totalPage;
    // 总记录数
    private int totalResult;
    // 当前页数
    private int currentPage;
    // 当前显示到的ID, 在mysql limit 中就是第一个参数.
    private int currentResult;
    private String sortField;
    private String order;

    public int getShowCount() {
        return showCount;
    }

    public void setShowCount(int showCount) {
        this.showCount = showCount;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getTotalResult() {
        return totalResult;
    }

    public void setTotalResult(int totalResult) {
        this.totalResult = totalResult;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getCurrentResult() {
        return currentResult;
    }

    public void setCurrentResult(int currentResult) {
        this.currentResult = currentResult;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

}