/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.tree;

import code.ponfee.commons.util.Strings;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 基于树形结构节点的基类
 * 
 * @author Ponfee
 * 
 * @param <T> the node id type
 * @param <A> the attachment biz object type
 */
public class BaseNode<T extends Serializable & Comparable<? super T>, A extends Serializable>
    implements Serializable, Cloneable {

    private static final long serialVersionUID = -4116799955526185765L;

    // -------------------------------------------------------------------基础信息
    protected final T             nid; // node id
    protected final T             pid; // parent node id
    protected final boolean   enabled; // 状态（业务相关）：false无效；true有效；
    protected final boolean available; // 是否可用（parent.available & this.enabled）
    protected final A          attach; // 附加信息（与业务相关）

    // -------------------------------------------------------------------以ROOT为根的整棵树
    protected int               level; // 节点层级（以根节点为1开始，往下逐级加1）
    protected List<T>            path; // 节点路径list<nid>（父节点在前，末尾元素是节点本身的nid）
    protected int              degree; // 节点的度数（子节点数量，叶子节点的度为0）

    protected int       leftLeafCount; // 左叶子节点数量（在其左边的所有叶子节点数量：相邻左兄弟节点的左叶子节点个数+该兄弟节点的子节点个数）

    // -------------------------------------------------------------------以当前节点为根的子树
    protected int           treeDepth; // 树的深度（叶子节点的树深度为1）
    protected int       treeNodeCount; // 树的节点数量
    protected int       treeMaxDegree; // 树中最大的度数（树中所有节点数目=所有节点度数之和+1）
    protected int       treeLeafCount; // 树的叶子节点数量（叶子节点为1）

    protected int       childrenCount; // 子节点个数
    protected int        siblingOrder; // 兄弟节点按顺序排行（从1开始）

    public BaseNode(T nid, T pid, A attach) {
        this(nid, pid, true, attach);
    }

    public BaseNode(T nid, T pid, boolean enabled, A attach) {
        this(nid, pid, enabled, enabled, attach);
    }

    public BaseNode(T nid, T pid, boolean enabled, boolean available, A attach) {
        Preconditions.checkArgument(!Strings.isBlank(nid), "Node id cannot be empty.");
        this.nid = nid;
        this.pid = pid;
        this.enabled = enabled;
        this.available = available;
        this.attach = attach;
    }

    @Override
    public BaseNode<T, A> clone() { // deep copy
        return SerializationUtils.clone(this);
    }

    public BaseNode<T, A> copy() { // shadow copy
        BaseNode<T, A> node = new BaseNode<>(
            this.nid, this.pid, this.enabled, this.available, this.attach
        );
        node.level  = this.level;
        node.degree = this.degree;
        node.path   = this.path;

        node.leftLeafCount = this.leftLeafCount;

        node.treeDepth     = this.treeDepth;
        node.treeNodeCount = this.treeNodeCount;
        node.treeMaxDegree = this.treeMaxDegree;
        node.treeLeafCount = this.treeLeafCount;
        return node;
    }

    // -----------------------------------------------final field getter

    public T getNid() {
        return nid;
    }

    public T getPid() {
        return pid;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAvailable() {
        return available;
    }

    public A getAttach() {
        return attach;
    }

    // ----------------------------------------------------others field getter

    public int getLevel() {
        return level;
    }

    public List<T> getPath() {
        return path;
    }

    public int getDegree() {
        return degree;
    }

    public int getLeftLeafCount() {
        return leftLeafCount;
    }

    public int getTreeDepth() {
        return treeDepth;
    }

    public int getTreeNodeCount() {
        return treeNodeCount;
    }

    public int getTreeMaxDegree() {
        return treeMaxDegree;
    }

    public int getTreeLeafCount() {
        return treeLeafCount;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public int getSiblingOrder() {
        return siblingOrder;
    }
}
