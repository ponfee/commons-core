/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.schema.json;

import cn.ponfee.commons.schema.DataType;
import cn.ponfee.commons.tree.NodeId;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * The element id of json data structure
 * 
 * @author Ponfee
 */
public class JsonId extends NodeId<JsonId> {

    private static final long serialVersionUID = -6344204521700761391L;

    private final String   name; // 节点名称
    private final DataType type; // 数据类型
    private final int    orders; // 次序

    public JsonId(JsonId parent, @Nonnull String name,
                  DataType type, int orders) {
        super(parent);
        this.name   = Objects.requireNonNull(name);
        this.type   = type;
        this.orders = orders;
    }

    @Override
    protected boolean equals(JsonId another) {
        return this.name.equals(another.name);
    }

    @Override
    protected int compare(JsonId another) {
        int a = this.orders - another.orders;
        return a != 0 ? a : this.name.compareTo(another.name);
    }

    @Override
    protected int hash() {
        return this.name == null ? 0 : this.name.hashCode();
    }

    @Override
    public JsonId clone() {
        return new JsonId(
            this.parent == null ? null : this.parent.clone(), 
            this.name, this.type, this.orders
        );
    }

    // -----------------------------------------------------------------setter
    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    public int getOrders() {
        return orders;
    }

}
