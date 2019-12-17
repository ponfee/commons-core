/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.tree;

import java.io.Serializable;
import java.util.List;

/**
 * The trait for Tree node
 * 
 * @author Ponfee
 */
public interface TreeTrait<T extends Serializable & Comparable<? super T>, A extends Serializable>
    extends Serializable {

    <E extends TreeTrait<T, A>> void setChildren(List<E> children);

}
