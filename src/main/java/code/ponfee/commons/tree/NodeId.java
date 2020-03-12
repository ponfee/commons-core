/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.tree;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import code.ponfee.commons.json.Jsons;

/**
 * Base node id
 * 
 * @author Ponfee
 * @param <T> the NodeId implementation sub class
 */
public abstract class NodeId<T extends NodeId<T>> implements Comparable<T>, Serializable, Cloneable {

    private static final long serialVersionUID = -9004940918491918780L;

    protected final T parent;

    public NodeId(T parent) {
        this.parent = parent;
    }

    @Override @SuppressWarnings("unchecked")
    public final boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        T another = (T) obj;
        return equalsParent(this.parent, another.parent) 
            && this.equalsNode(another);
    }

    @Override
    public final int compareTo(T another) {
        if (this.parent == null) {
            return another.parent == null ? this.compareNode(another) : -1;
        }
        if (another.parent == null) {
            return 1;
        }

        int a = this.parent.compareTo(another.parent);
        return a != 0 ? a : this.compareNode(another);
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder()
            .append(this.parent)
            .append(this.hashNode())
            .build();
    }

    @Override
    public String toString() {
        return Jsons.toJson(this);
    }

    protected abstract boolean equalsNode(T another);

    protected abstract int compareNode(T another);

    protected abstract int hashNode();

    @Override
    public abstract T clone();

    public final T getParent() {
        return parent;
    }

    private boolean equalsParent(T a, T b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

}
