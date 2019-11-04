/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.tree;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import code.ponfee.commons.reflect.Fields;
import code.ponfee.commons.util.Strings;

/**
 * 节点树形结构
 * 
 * @author Ponfee
 */
public final class TreeNode<T extends Serializable & Comparable<T>, A extends Serializable>
    extends BaseNode<T, A> {

    private static final long serialVersionUID = -9081626363752680404L;
    public static final String DEFAULT_ROOT_ID = "__ROOT__";

    // 用于比较兄弟节点
    private final Comparator<BaseNode<T, A>> comparator;

    // 子节点列表（空列表则表示为叶子节点）
    private final List<TreeNode<T, A>> children = Lists.newArrayList();

    /**
     * Constructs a tree node
     * 
     * @param node  the base node
     */
    private TreeNode(BaseNode<T, A> node, @Nonnull Comparator<BaseNode<T, A>> comparator) {
        super(node.getNid(), node.getPid(), node.isEnabled(), node.attach);
        Objects.nonNull(comparator);
        super.available = node.isAvailable();
        this.comparator = comparator;
        //this.comparator.thenComparing(BaseNode::getNid);
    }

    /**
     * Constructs a tree node
     * 
     * @param nid        the node id
     * @param pid        the parent node id
     * @param enabled    the node is enabled
     * @param comparator the comparator
     */
    private TreeNode(T nid, T pid, boolean enabled, @Nonnull Comparator<BaseNode<T, A>> comparator) {
        super(nid, pid, enabled, null);
        Objects.nonNull(comparator);
        super.available = enabled;
        this.comparator = comparator;
        //this.comparator.thenComparing(BaseNode::getNid);
    }

    // ---------------------------------------------------creates tree node without Comparator
    public static <T extends Serializable & Comparable<T>, A extends Serializable> TreeNode<T, A> 
        of(T rootNid) {
        return of(rootNid, null, true);
    }

    public static <T extends Serializable & Comparable<T>, A extends Serializable> TreeNode<T, A> 
        of(T nid, T pid) {
        return of(nid, pid, true);
    }

    public static <T extends Serializable & Comparable<T>, A extends Serializable> TreeNode<T, A> 
        of(T nid, T pid, boolean enabled) {
        return of(nid, pid, enabled, Comparator.comparing(BaseNode::getNid));
    }

    // ---------------------------------------------------creates tree node with Comparator
    public static <T extends Serializable & Comparable<T>, A extends Serializable> TreeNode<T, A> 
        of(T rootNid, Comparator<BaseNode<T, A>> comparator) {
        return of(rootNid, null, true, comparator);
    }

    public static <T extends Serializable & Comparable<T>, A extends Serializable> TreeNode<T, A> 
        of(T nid, T pid, Comparator<BaseNode<T, A>> comparator) {
        return of(nid, pid, true, comparator);
    }

    public static <T extends Serializable & Comparable<T>, A extends Serializable> TreeNode<T, A> 
        of(T nid, T pid, boolean enabled, Comparator<BaseNode<T, A>> comparator) {
        return new TreeNode<>(nid, pid, enabled, comparator);
    }

    // ------------------------------------------------------creates tree node from a base node
    /**
     * Returns a tree node
     *  
     * @param node   the base node
     * @return a tree node
     */
    public static <T extends Serializable & Comparable<T>, A extends Serializable> TreeNode<T, A> 
        of(BaseNode<T, A> node) {
        return of(node, Comparator.comparing(BaseNode::getNid));
    }

    public static <T extends Serializable & Comparable<T>, A extends Serializable> TreeNode<T, A> 
        of(BaseNode<T, A> node, Comparator<BaseNode<T, A>> comparator) {
        return new TreeNode<>(node, comparator);
    }

    // ------------------------------------------------------mount children nodes
    public <E extends BaseNode<T, A>> TreeNode<T, A> mount(List<E> nodes) {
        mount(nodes, false);
        return this;
    }

    /**
     * Mount a tree
     * 
     * @param list         子节点列表
     * @param ignoreOrphan {@code true}忽略孤儿节点
     */
    @SuppressWarnings("unchecked")
    public <E extends BaseNode<T, A>> TreeNode<T, A> mount(@Nonnull List<E> list, 
                                                           boolean ignoreOrphan) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(list));

        Set<T> nodeNids = Sets.newHashSet(super.nid);

        // 1、预处理
        List<BaseNode<T, A>> nodes = prepare(list);

        // 2、检查是否存在重复节点
        for (BaseNode<T, A> n : nodes) {
            if (!nodeNids.add(n.getNid())) {
                throw new RuntimeException("重复的节点：" + n.getNid());
            }
        }

        // 3、以此节点为根构建节点树
        super.level = 1; // root node level is 1
        super.path = null; // reset with null
        super.leftLeafCount = 0; // root node left leaf count is 1
        this.mount0(nodes, ignoreOrphan, super.nid);

        // 4、检查是否存在孤儿节点
        if (!ignoreOrphan && CollectionUtils.isNotEmpty(nodes)) {
            List<T> nids = nodes.stream().map(BaseNode::getNid)
                                .collect(Collectors.toList());
            throw new RuntimeException("无效的孤儿节点：" + nids);
        }

        // 5、统计
        count();

        return this;
    }

    /**
     * 按继承方式展开节点：父子节点相邻 ，Inherit
     * 深度优先搜索DFS：（Depth-First Search）
     * 
     * Should be invoking after {@link #mount(List)}
     * 
     * @return a list nodes for dfs tree node
     */
    public List<FlatNode<T, A>> dfsFlat() {
        List<FlatNode<T, A>> collect = Lists.newArrayList();
        dfs(collect);
        return collect;
    }

    /**
     * 按层级方式展开节点：兄弟节点相邻，Hierarchy
     * 广度优先搜索BFS：（Breadth-First Search）
     * 
     * Should be invoking after {@link #mount(List)}
     * 
     * @return a list nodes for bfs tree node
     */
    public List<FlatNode<T, A>> bfsFlat() {
        List<FlatNode<T, A>> collect = Lists.newArrayList(new FlatNode<>(this));
        bfs(collect);
        return collect;
    }

    // -----------------------------------------------------------private methods
    private <E extends BaseNode<T, A>> List<BaseNode<T, A>> prepare(List<E> nodes) {
        List<BaseNode<T, A>> list = Lists.newArrayListWithCapacity(nodes.size());

        // nodes list
        for (BaseNode<T, A> node : nodes) {
            if (node instanceof TreeNode) { // if tree node, then add all the tree nodes
                list.addAll(((TreeNode<T, A>) node).dfsFlat());
            } else {
                list.add(node); // node.clone()
            }
        }

        // the root node children
        if (CollectionUtils.isNotEmpty(this.children)) {
            List<FlatNode<T, A>> flat = this.dfsFlat();
            list.addAll(flat.subList(1, flat.size()));
            this.children.clear();
        }
        return list;
    }

    private <E extends BaseNode<T, A>> void mount0(
        List<E> nodes, boolean ignoreOrphan, T mountPidIfNull) {
        // current "this" is parent: AbstractNode parent = this;

        // find child nodes for the current node
        for (Iterator<E> iter = nodes.iterator(); iter.hasNext();) {
            BaseNode<T, A> node = iter.next();

            if (!ignoreOrphan && Strings.isBlank(node.getPid())) { // effect condition that pid is null
                // 不忽略孤儿节点且节点的父节点为空，则其父节点视为根节点（将其挂载到根节点下）
                Fields.put(node, "pid", mountPidIfNull); // pid is final modify
            }

            if (super.nid.equals(node.getPid())) {
                // found a child node
                if (CollectionUtils.isNotEmpty(super.path)
                    && super.path.contains(super.nid)) { // 节点路径中已经包含了此节点，则视为环状
                    throw new RuntimeException("节点循环依赖：" + node.getNid());
                }

                TreeNode<T, A> child = new TreeNode<>(node, this.comparator);
                child.available = super.available && child.isEnabled();

                // 子节点路径=节点路径+自身节点
                child.path = concat(super.path, super.nid);
                child.level = super.level + 1;
                this.children.add(child); // 挂载子节点

                iter.remove(); // remove the found child node
            }
        }

        if (CollectionUtils.isNotEmpty(this.children)) {
            // sort the children list(sibling nodes sort)
            //this.children.sort(Comparator.<BaseNode<T, O, A>, O> comparing(BaseNode::getOrders).thenComparing(BaseNode::getNid));
            this.children.sort(this.comparator);

            // recursion to mount child tree
            for (TreeNode<T, A> nt : this.children) {
                nt.mount0(nodes, ignoreOrphan, mountPidIfNull);
            }
        }

        super.path = concat(super.path, super.nid); // 节点路径追加自身的ID
    }

    private void dfs(List<FlatNode<T, A>> collect) {
        collect.add(new FlatNode<>(this));
        if (CollectionUtils.isNotEmpty(this.children)) {
            for (TreeNode<T, A> nt : this.children) {
                nt.dfs(collect);
            }
        }
    }

    private void bfs(List<FlatNode<T, A>> collect) {
        if (CollectionUtils.isNotEmpty(this.children)) {
            for (TreeNode<T, A> nt : this.children) {
                collect.add(new FlatNode<>(nt));
            }
            for (TreeNode<T, A> nt : this.children) {
                nt.bfs(collect);
            }
        }
    }

    private void count() {
        if (CollectionUtils.isNotEmpty(this.children)) { // 非叶子节点
            int maxChildTreeDepth = 0, sumTreeNodeCount = 0, 
                sumChildLeafCount = 0;
            TreeNode<T, A> child;
            for (int i = 0; i < this.children.size(); i++) {
                child = this.children.get(i);

                // 1、统计左叶子节点数量
                if (i == 0) {
                    // 最左子节点：左叶子节点个数=父节点的左叶子节点个数
                    child.leftLeafCount = super.leftLeafCount;
                } else {
                    // 若不是最左子节点，则其左叶子节点个数=
                    // 相邻左兄弟节点的左叶子节点个数+该兄弟节点的子节点个数
                    TreeNode<T, A> prevSibling = this.children.get(i - 1);
                    child.leftLeafCount = prevSibling.leftLeafCount 
                                        + prevSibling.childLeafCount;
                }

                // 2、递归
                child.count();

                // 3、统计子叶子节点数量及整棵树节点的数量
                sumChildLeafCount += child.childLeafCount;
                maxChildTreeDepth = Math.max(maxChildTreeDepth, child.treeMaxDepth);
                sumTreeNodeCount += child.treeNodeCount;
            }
            super.childLeafCount = sumChildLeafCount;     // 子节点的叶子节点之和
            super.treeMaxDepth   = maxChildTreeDepth + 1; // 加上父（自身）节点层级
            super.treeNodeCount  = sumTreeNodeCount + 1;  // 要包含节点本身
        } else { // 叶子节点
            super.treeNodeCount = 1;
            super.childLeafCount = 1;
            super.treeMaxDepth = 1;
        }
    }

    /**
     * Returns the ImmutableList of merged collection and object
     * 
     * @param coll the elements list
     * @param obj the object element
     * @return a new ImmutableList appended an element
     */
    private static <E> List<E> concat(Collection<E> coll, E obj) {
        ImmutableList.Builder<E> builder = ImmutableList.builder();
        if (coll != null) {
            builder.addAll(coll);
        }
        builder.add(obj);
        return builder.build();
    }

    // -----------------------------------------------getter/setter
    public List<TreeNode<T, A>> getChildren() {
        return children;
    }

}
