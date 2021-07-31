package code.ponfee.commons.schema.json;

import code.ponfee.commons.schema.DataType;
import code.ponfee.commons.tree.NodeId;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * The element id of json data structure
 * 
 * @author Ponfee
 */
public class JSONId extends NodeId<JSONId> {

    private static final long serialVersionUID = -6344204521700761391L;

    private final String   name; // 节点名称
    private final DataType type; // 数据类型
    private final int    orders; // 次序

    public JSONId(JSONId parent, @Nonnull String name,
                  DataType type, int orders) {
        super(parent);
        this.name   = Objects.requireNonNull(name);
        this.type   = type;
        this.orders = orders;
    }

    @Override
    protected boolean equalsNode(JSONId another) {
        return this.name.equals(another.name);
    }

    @Override
    protected int compareNode(JSONId another) {
        int a = this.orders - another.orders;
        return a != 0 ? a : this.name.compareTo(another.name);
    }

    @Override
    protected int hashNode() {
        return this.name == null ? 0 : this.name.hashCode();
    }

    @Override
    public JSONId clone() {
        return new JSONId(
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
