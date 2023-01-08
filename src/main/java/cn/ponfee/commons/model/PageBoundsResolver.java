/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页解析器
 * 
 * @author Ponfee
 */
public final class PageBoundsResolver {

    private PageBoundsResolver() {}

    /**
     * 多个数据源查询结果分页
     * 
     * @param pageNum 页号
     * @param pageSize 页大小
     * @param subTotalCounts 各数据源查询结果集行总计
     * @return a list of page bounds
     */
    public static List<PageBounds> resolve(int pageNum, int pageSize, long... subTotalCounts) {
        if (subTotalCounts == null || subTotalCounts.length == 0) {
            return null;
        }

        // 总记录数
        long totalCounts = 0;
        for (long subTotalCount : subTotalCounts) {
            totalCounts += subTotalCount;
        }
        if (totalCounts < 1) {
            return null;
        }

        // pageSize小于1时表示查询全部
        if (pageSize < 1) {
            List<PageBounds> bounds = new ArrayList<>();
            for (int i = 0; i < subTotalCounts.length; i++) {
                // index, subTotalCounts, offset=0, limit=subTotalCounts
                bounds.add(new PageBounds(i, subTotalCounts[i], 0, (int) subTotalCounts[i]));
            }
            return bounds;
        }

        // normalize pageNum, offset value
        if (pageNum < 1) {
            pageNum = 1;
        }
        long offset = PageHandler.computeOffset(pageNum, pageSize);
        if (offset >= totalCounts) { // 超出总记录数，则取最后一页
            pageNum = PageHandler.computeTotalPages(totalCounts, pageSize);
            offset = PageHandler.computeOffset(pageNum, pageSize);
        }

        // 分页计算
        List<PageBounds> bounds = new ArrayList<>();
        long start = offset, end = start + pageSize, cursor = 0;
        for (int limit, i = 0; i < subTotalCounts.length; cursor += subTotalCounts[i], i++) {
            if (start >= cursor + subTotalCounts[i]) {
                continue;
            }

            offset = start - cursor;
            if (end > cursor + subTotalCounts[i]) {
                limit = (int) (cursor + subTotalCounts[i] - start);
                bounds.add(new PageBounds(i, subTotalCounts[i], offset, limit));
                start = cursor + subTotalCounts[i];
            } else {
                limit = (int) (end - start);
                bounds.add(new PageBounds(i, subTotalCounts[i], offset, limit));
                break;
            }
        }
        return bounds;
    }

    /**
     * 单个数据源查询结果分页
     * 
     * @param pageNum the page number
     * @param pageSize the page size
     * @param totalCounts the total counts
     * @return a page bounds
     */
    public static PageBounds resolve(int pageNum, int pageSize, long totalCounts) {
        List<PageBounds> list = resolve(pageNum, pageSize, new long[] { totalCounts });

        if (list == null || list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * 分页对象
     */
    public static final class PageBounds {
        private final int index; // 数据源下标（start 0）
        private final long total; // 总记录数
        private final long offset; // 偏移量（start 0）
        private final int limit; // 数据行数

        PageBounds(int index, long total, long offset, int limit) {
            this.index = index;
            this.total = total;
            this.offset = offset;
            this.limit = limit;
        }

        public int getIndex() {
            return index;
        }

        public long getTotal() {
            return total;
        }

        public long getOffset() {
            return offset;
        }

        public int getLimit() {
            return limit;
        }

        @Override
        public String toString() {
            return "PageBounds [index=" + index + ", total=" + total 
                 + ", offset=" + offset + ", limit=" + limit + "]";
        }
    }

}
