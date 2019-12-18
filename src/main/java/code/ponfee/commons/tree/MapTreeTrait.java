/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.tree;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The map for Tree node
 * 
 * @author Ponfee
 * @param <T> the node id type
 * @param <A> the attachment biz object type
 */
public class MapTreeTrait<T extends Serializable & Comparable<? super T>, A extends Serializable>
    extends LinkedHashMap<String, Object> implements TreeTrait<T, A, MapTreeTrait<T, A>> {

    private static final long serialVersionUID = -5799393887664198242L;

    public static final String DEFAULT_CHILDREN_KEY = "children";

    private final String childrenKey;

    public MapTreeTrait() {
        this(DEFAULT_CHILDREN_KEY);
    }

    public MapTreeTrait(String childrenKey) {
        this.childrenKey = childrenKey;
    }

    @Override
    public void setChildren(List<MapTreeTrait<T, A>> children) {
        this.put(this.childrenKey, children);
    }

}
