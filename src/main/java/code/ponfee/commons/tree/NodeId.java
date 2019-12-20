/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.tree;

import java.io.Serializable;

/**
 * Base node id
 * 
 * @author Ponfee
 * @param <T> the NodeId implementation sub class
 */
public abstract class NodeId<T extends NodeId<T>> implements Serializable, Comparable<T>, Cloneable {

    private static final long serialVersionUID = -9004940918491918780L;

    protected final T parent;

    public NodeId(T parent) {
        this.parent = parent;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract T clone();

    public final T getParent() {
        return parent;
    }

}
