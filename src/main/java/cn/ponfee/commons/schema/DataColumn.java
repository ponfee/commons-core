/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.schema;

import java.io.Serializable;

/**
 * Column for table meta config
 * 
 * @author Ponfee
 */
public class DataColumn implements Serializable {

    private static final long serialVersionUID = 7044462319527084588L;

    private String   name;  // 列名（如Mysql表的列名）
    private DataType type;  // 类型
    private String   alias; // 别名（表头标题）

    public DataColumn() {}

    public DataColumn(String name, DataType type, String alias) {
        this.name = name;
        this.type = type;
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public static DataColumn of(String name, DataType type, String alias) {
        return new DataColumn(name, type, alias);
    }

    public static DataColumn of(String name, DataType type) {
        return new DataColumn(name, type, null);
    }

}
