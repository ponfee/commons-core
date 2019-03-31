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
public final class TreeNode<T extends Serializable & Comparable<T>, A>
    extends BaseNode<T, A> {

    private static final long serialVersionUID = -9081626363752680404L;
    public static final String DEFAULT_ROOT_ID = "__ROOT__";

    // 子节点列表（空列表则表示为叶子节点）
    private final List<TreeNode<T, A>> children = Lists.newArrayList();

    /**
     * 构造根节点
     * 
     * @param nid
     * @param pid
     * @param orders
     * @param enabled
     */
    private TreeNode(T nid, T pid, int orders, boolean enabled) {
        super(nid, pid, orders, enabled, null);
        this.available = enabled;
    }

    /**
     * 指定一个节点作为根节点
     * 
     * @param node  as a tree root node
     */
    private TreeNode(BaseNode<T, A> node) {
        super(node.getNid(), node.getPid(), node.getOrders(), 
              node.isEnabled(), node.attach);
        super.available = node.isAvailable();
    }

    // ---------------------------------------------------create root node
    public static <T extends Serializable & Comparable<T>, A> TreeNode<T, A> 
        createRoot(T nid) {
        return createRoot(nid, null, 0, true);
    }

    public static <T extends Serializable & Comparable<T>, A> TreeNode<T, A> 
        createRoot(T nid, T pid, int orders) {
        return createRoot(nid, pid, orders, true);
    }

    public static <T extends Serializable & Comparable<T>, A> TreeNode<T, A> 
        createRoot(T nid, T pid, int orders, boolean enabled) {
        return new TreeNode<>(nid, pid, orders, enabled);
    }

    /**
     * Returns a tree root node
     *  
     * @param node   the node for root
     * @return a tree root node
     */
    public static <T extends Serializable & Comparable<T>, A> TreeNode<T, A> 
        createRoot(BaseNode<T, A> node) {
        return new TreeNode<>(node);
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

        Set<T> nodeNids = Sets.newHashSet(this.nid);

        // 1、预处理
        List<BaseNode<T, A>> nodes = before(list);

        // 2、检查是否存在重复节点
        for (BaseNode<T, A> n : nodes) {
            if (!nodeNids.add(n.getNid())) {
                throw new RuntimeException("重复的节点：" + n.getNid());
            }
        }

        // 3、以此节点为根构建节点树
        this.level = 1; // root node level is 1
        this.path = null; // reset with null
        this.leftLeafCount = 0; // root node left leaf count is 1
        this.mount0(nodes, ignoreOrphan, this.nid);

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
     * should be before invoke {@link #mount(List)}
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
     * should be before invoke {@link #mount(List)}
     * 
     * @return a list nodes for bfs tree node
     */
    public List<FlatNode<T, A>> bfsFlat() {
        List<FlatNode<T, A>> collect = Lists.newArrayList(new FlatNode<>(this));
        bfs(collect);
        return collect;
    }

    // -----------------------------------------------------------private methods
    private <E extends BaseNode<T, A>> List<BaseNode<T, A>> before(List<E> nodes) {
        List<BaseNode<T, A>> list = Lists.newArrayListWithCapacity(nodes.size());

        // nodes list
        for (BaseNode<T, A> node : nodes) {
            if (node instanceof TreeNode) {
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

        Set<Integer> uniqueOrders = Sets.newHashSet();
        // find child nodes for the current node
        for (Iterator<E> iter = nodes.iterator(); iter.hasNext();) {
            BaseNode<T, A> node = iter.next();

            if (!ignoreOrphan && !Strings.isBlank(node.getPid())) { // effect condition that pid is null
                // 不忽略孤儿节点且节点的父节点为空，则其父节点视为根节点（将其挂载到根节点下）
                Fields.put(node, "pid", mountPidIfNull); // pid is final modify
            }

            if (this.nid.equals(node.getPid())) {
                // found a child node
                if (CollectionUtils.isNotEmpty(this.path)
                    && this.path.contains(this.nid)) { // 节点路径中已经包含了此节点，则视为环状
                    throw new RuntimeException("节点循环依赖：" + node.getNid());
                }

                if (!uniqueOrders.add(node.getOrders())) {
                    throw new RuntimeException("兄弟节点次序重复：" + node.getNid());
                }

                TreeNode<T, A> child = new TreeNode<>(node);
                child.available = this.available && child.isEnabled();

                // 子节点路径=节点路径+自身节点
                child.path = concat(this.path, this.nid);
                child.level = this.level + 1;
                this.children.add(child); // 挂载子节点

                iter.remove(); // remove the found child node
            }
        }

        if (CollectionUtils.isNotEmpty(this.children)) {
            // sort the children list
            this.children.sort(Comparator.comparing(TreeNode::getOrders));

            // recursion to mount child tree
            for (TreeNode<T, A> nt : this.children) {
                nt.mount0(nodes, ignoreOrphan, mountPidIfNull);
            }
        }

        this.path = concat(this.path, this.nid); // 节点路径追加自身的ID
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
                    child.leftLeafCount = this.leftLeafCount;
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
            this.childLeafCount = sumChildLeafCount;     // 子节点的叶子节点之和
            this.treeMaxDepth   = maxChildTreeDepth + 1; // 加上父（自身）节点层级
            this.treeNodeCount  = sumTreeNodeCount + 1;  // 要包含节点本身
        } else { // 叶子节点
            this.treeNodeCount = 1;
            this.childLeafCount = 1;
            this.treeMaxDepth = 1;
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
