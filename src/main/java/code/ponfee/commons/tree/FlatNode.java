/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.tree;

import java.io.Serializable;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;

/**
 * 节点扁平结构
 * 
 * @author Ponfee
 */
public final class FlatNode<T extends Serializable & Comparable<? super T>, A extends Serializable>
    extends BaseNode<T, A> {

    private static final long serialVersionUID = 5191371614061952661L;

    private final boolean leaf; // 是否叶子节点

    FlatNode(TreeNode<T, A> nt) {
        super(nt.nid, nt.pid, nt.enabled, nt.available, nt.attach);

        super.level = nt.level;
        super.path  = nt.path;

        super.treeNodeCount  = nt.treeNodeCount;
        super.childLeafCount = nt.childLeafCount;
        super.leftLeafCount  = nt.leftLeafCount;
        super.treeMaxDepth   = nt.treeMaxDepth;

        this.leaf = CollectionUtils.isEmpty(nt.getChildren());
    }

    public <R> R convert(Function<FlatNode<T, A>, R> convertor) {
        return convertor.apply(this);
    }

    // ----------------------------------------------getter/setter
    public boolean isLeaf() {
        return leaf;
    }

}
