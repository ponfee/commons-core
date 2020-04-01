package code.ponfee.commons.export;

import java.io.Closeable;

/**
 * {@link Closeable#close()} 要求幂等
 * {@link AutoCloseable#close()} 不要求幂等
 * 
 * 数据导出
 * 
 * @author Ponfee
 */
public interface DataExporter<T> extends Closeable {

    /** 提示无结果 */
    String NO_RESULT_TIP = "No results found";

    /**
     * 构建表格
     */
    <E> void build(Table<E> table);

    /**
     * 获取表格
     */
    T export();

    /**
     * 判断是否为空
     */
    boolean isEmpty();

    /**
     * 关闭资源
     * not throw Exception
     */
    @Override
    void close();
}
