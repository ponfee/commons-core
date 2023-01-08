/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.export;

import java.io.Serializable;

/**
 * 表头
 * 
 * @author Ponfee
 */
public class Thead implements Serializable {

    private static final long serialVersionUID = 1898674740598755648L;

    private final String name;  // 列名
    private final Tmeta tmeta;  // 列配置信息
    private final String field; // 字段（对应类的字段）

    public Thead(String name) {
        this(name, null, null);
    }

    public Thead(String name, Tmeta tmeta, String field) {
        this.name = name;
        this.tmeta = tmeta;
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public Tmeta getTmeta() {
        return tmeta;
    }

    public String getField() {
        return field;
    }

}
