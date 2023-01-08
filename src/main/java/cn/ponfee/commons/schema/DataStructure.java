/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.schema;

/**
 * 数据格式标记类：结构化的数据
 * 
 * @author Ponfee
 */
public interface DataStructure extends java.io.Serializable {

    default String structure() {
        return DataStructures.ofType(this.getClass()).name();
    }

    NormalStructure toNormal();

    TableStructure toTable();

    PlainStructure toPlain();

}
