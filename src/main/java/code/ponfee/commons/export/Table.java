package code.ponfee.commons.export;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

import org.springframework.util.CollectionUtils;

import code.ponfee.commons.tree.BaseNode;
import code.ponfee.commons.tree.FlatNode;
import code.ponfee.commons.tree.TreeNode;

/**
 * 表格
 * @author fupf
 */
public class Table<E> implements Serializable {
    private static final long serialVersionUID = 1600567917100486004L;

    private static final int ROOT_PID = 0;

    private final List<FlatNode<Integer, Thead>> thead; // 表头
    private final Function<E, Object[]> converter; // 数据转换
    private String caption; // 标题
    private Object[] tfoot; // 表尾
    private String comment; // 注释说明
    private Map<CellStyleOptions, Object> options; // 其它特殊配置项，如：{HIGHLIGHT:{\"cells\":[[2,15],[2,16]],\"color\":\"#f00\"}}

    private final Queue<E> tbody = new LinkedBlockingQueue<>(); // 表体
    private volatile boolean empty = true;
    private volatile boolean end = false;

    public Table(List<FlatNode<Integer, Thead>> thead, 
                 Function<E, Object[]> converter,
                 String caption, Object[] tfoot, String comment, 
                 Map<CellStyleOptions, Object> options) {
        this.thead = thead;
        this.converter = converter;
        this.caption = caption;
        this.tfoot = tfoot;
        this.comment = comment;
        this.options = options;
    }

    public Table(List<BaseNode<Integer, Thead>> list) {
        this(list, null);
    }

    public Table(List<BaseNode<Integer, Thead>> list, 
                 Function<E, Object[]> converter) {
        this.thead = TreeNode.<Integer, Thead>createRoot(ROOT_PID, null, 0)
                             .mount(list).bfsFlat();
        this.converter = converter;
    }

    public Table(String[] names) {
        this(names, null);
    }

    public Table(String[] names, Function<E, Object[]> converter) {
        List<BaseNode<Integer, Thead>> list = new ArrayList<>(names.length);
        for (int i = 0, j = 1; i < names.length; i++, j++) {
            list.add(new BaseNode<>(j, ROOT_PID, j, true, new Thead(names[i])));
        }
        this.thead = TreeNode.<Integer, Thead>createRoot(ROOT_PID, null, 0)
                             .mount(list).bfsFlat();
        this.converter = converter;
    }

    public Table<E> copyOfWithoutTbody() {
        return new Table<>(thead, converter, caption, tfoot, comment, options);
    }

    public <H> Table<H> copyOfWithoutTbody(Function<H, Object[]> converter) {
        return new Table<>(thead, converter, caption, tfoot, comment, options);
    }

    public List<FlatNode<Integer, Thead>> getThead() {
        return thead;
    }

    public Function<E, Object[]> getConverter() {
        return converter;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Object[] getTfoot() {
        return tfoot;
    }

    public void setTfoot(Object[] tfoot) {
        this.tfoot = tfoot;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Map<CellStyleOptions, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<CellStyleOptions, Object> options) {
        this.options = options;
    }

    // -----------------------------------------------add row data
    public void addRowsAndEnd(List<E> rows) {
        addRows(rows);
        toEnd();
    }

    public void addRows(List<E> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        for (E row : rows) {
            tbody.offer(row);
        }
        empty = false;
    }

    public void addRowAndEnd(E row) {
        addRow(row);
        toEnd();
    }

    public void addRow(E row) {
        if (row == null) {
            return;
        }
        tbody.offer(row);
        empty = false;
    }

    // -----------------------------------------------to end operation
    public synchronized Table<E> toEnd() {
        this.end = true;
        return this;
    }

    // -----------------------------------------------data exporter use
    boolean isEnd() {
        return end && tbody.isEmpty();
    }

    boolean isNotEnd() {
        return !isEnd();
    }

    boolean isEmptyTbody() {
        return empty && isEnd();
    }

    E poll() {
        return tbody.poll();
    }

}
