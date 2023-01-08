/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.export;

import java.io.Closeable;

/**
 * {@link Closeable#close()} 要求幂等
 * {@link AutoCloseable#close()} 不要求幂等
 * 
 * <p>数据导出
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

}
