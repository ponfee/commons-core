package code.ponfee.commons.export;

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
