/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.tree;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;

import com.google.common.base.Preconditions;

import code.ponfee.commons.util.Strings;

/**
 * 基于树形结构节点的基类
 * 
 * @author Ponfee
 * 
 * @param <T> the node id type
 * @param <A> the attachment biz object type
 */
public class BaseNode<T extends Serializable & Comparable<T>, A>
    implements Serializable, Cloneable {

    private static final long serialVersionUID = -4116799955526185765L;

    protected final T nid; // node id
    protected final T pid; // parent node id
    protected final int orders; // 节点次序（在兄弟节点间的排序中使用到）
    protected final boolean enabled; // 状态（业务相关）：false无效；true有效；
    protected final A attach; // 附加节点（附加信息，与业务相关）

    protected boolean available; // 是否可用（parent.available & this.enabled）
    protected int level; // 节点层级（以根节点为1开始，往下逐级加1）
    protected List<T> path; // 节点路径list<nid>（父节点在前，末尾元素是节点本身的nid）

    protected int childLeafCount; // 子叶子节点数量（若为叶子节点则为1）
    protected int leftLeafCount;  // 左叶子节点数量（在其左边的所有叶子节点数量）
    protected int treeNodeCount;  // 整棵树的节点数量（包括根节点）
    protected int treeMaxDepth;   // 节点树的最大深度（包括自身层级）

    public BaseNode(T nid, T pid, int orders, A attach) {
        this(nid, pid, orders, true, attach);
    }

    public BaseNode(T nid, T pid, int orders, 
                    boolean enabled, A attach) {
        Preconditions.checkArgument(!Strings.isBlank(nid), "节点编号不能为空");
        this.nid = nid;
        this.pid = pid;
        this.orders = orders;
        this.enabled = enabled;
        this.available = enabled;
        this.attach = attach;
    }

    @Override
    public BaseNode<T, A> clone() {
        return SerializationUtils.clone(this);
    }

    // -----------------------------------------------getter/setter
    public T getPid() {
        return pid;
    }

    public T getNid() {
        return nid;
    }

    public int getOrders() {
        return orders;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public A getAttach() {
        return attach;
    }

    public int getLevel() {
        return level;
    }

    public boolean isAvailable() {
        return available;
    }

    public List<T> getPath() {
        return path;
    }

    public int getTreeNodeCount() {
        return treeNodeCount;
    }

    public int getChildLeafCount() {
        return childLeafCount;
    }

    public int getTreeMaxDepth() {
        return treeMaxDepth;
    }

    public int getLeftLeafCount() {
        return leftLeafCount;
    }

}
