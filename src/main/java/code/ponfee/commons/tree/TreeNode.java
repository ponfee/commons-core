/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import code.ponfee.commons.collect.Collects;
import code.ponfee.commons.collect.Comparators;
import code.ponfee.commons.reflect.Fields;
import code.ponfee.commons.util.Strings;

/**
 * Tree node structure
 * 
 * @author Ponfee
 * @param <T> the node id type
 * @param <A> the attachment biz object type
 */
public final class TreeNode<T extends Serializable & Comparable<? super T>, A extends Serializable>
    extends BaseNode<T, A> {

    private static final long serialVersionUID = -9081626363752680404L;
    public static final String DEFAULT_ROOT_ID = "__ROOT__";

    // 用于比较兄弟节点
    private final Comparator<? super TreeNode<T, A>> siblingNodeOrders;

    // 子节点列表（空列表则表示为叶子节点）
    private final List<TreeNode<T, A>> children = new ArrayList<>();

    // 是否构建path
    private final boolean buildPath;

    /**
     * Constructs a tree node
     * 
     * @param nid               the node id
     * @param pid               the parent node id(withhold this pid field value, 
     *                          when use if the other root node mount this node as child)
     * @param enabled           the node is enabled
     * @param available         the current node is available(parent.available & this.enabled)
     * @param attach            the attachment for biz object
     * @param siblingNodeOrders the comparator for sibling nodes(has the same parent node) by sort
     * @param buildPath         the if whether build path
     * @param doMount           whether do mount, if is inner new TreeNode then false else true
     */
    TreeNode(T nid, T pid, boolean enabled, boolean available, A attach, 
             @Nonnull Comparator<? super TreeNode<T, A>> siblingNodeOrders, 
             boolean buildPath, boolean doMount) {
        super(nid, pid, enabled, available, attach);

        Objects.nonNull(siblingNodeOrders);
        this.siblingNodeOrders = siblingNodeOrders; // comparator.thenComparing(TreeNode::getNid);

        this.buildPath = buildPath;

        if (doMount) {
            this.mount(null); // as root node if new instance at external(TreeNodeBuilder) or of(TreeNode)
        }
    }

    // ------------------------------------------------------creates tree node from a base node
    /**
     * Returns a tree node
     *  
     * @param node   the base node
     * @return a tree node
     */
    public static <T extends Serializable & Comparable<? super T>, A extends Serializable> TreeNode<T, A> 
        of(BaseNode<T, A> node) {
        return of(node, Comparator.comparing(TreeNode::getNid));
    }

    public static <T extends Serializable & Comparable<? super T>, A extends Serializable> TreeNode<T, A> 
        of(BaseNode<T, A> node, Comparator<? super TreeNode<T, A>> siblingNodeOrders) {
        return of(node, siblingNodeOrders, true);
    }

    public static <T extends Serializable & Comparable<? super T>, A extends Serializable> TreeNode<T, A> 
        of(BaseNode<T, A> node, Comparator<? super TreeNode<T, A>> siblingNodeOrders, boolean buildPath) {
        return new TreeNode<>(
            node.nid, node.pid, node.enabled, node.available, 
            node.attach, siblingNodeOrders, buildPath, true
        );
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
    public <E extends BaseNode<T, A>> TreeNode<T, A> mount(List<E> list, boolean ignoreOrphan) {
        if (list == null) {
            list = Collections.emptyList();
        }

        // 1、预处理
        List<BaseNode<T, A>> nodes = prepare(list);

        // 2、检查是否存在重复节点
        List<T> checkDuplicateList = Lists.newArrayList(super.nid);
        nodes.stream().forEach(n -> checkDuplicateList.add(n.nid));
        Set<T> duplicated = Collects.duplicate(checkDuplicateList);
        if (CollectionUtils.isNotEmpty(duplicated)) {
            throw new RuntimeException("Duplicated nodes: " + duplicated);
        }

        // 3、以此节点为根构建节点树
        super.level = 1; // root node level is 1
        super.path = null; // reset with null
        super.leftLeafCount = 0; // root node left leaf count is 1
        this.mount0(null, nodes, ignoreOrphan, super.nid);

        // 4、检查是否存在孤儿节点
        if (!ignoreOrphan && CollectionUtils.isNotEmpty(nodes)) {
            List<T> nids = nodes.stream().map(BaseNode::getNid)
                                .collect(Collectors.toList());
            throw new RuntimeException("Invalid orphan nodes: " + nids);
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

    // -----------------------------------------------------------for each
    public void forEach(Consumer<TreeNode<T, A>> accept) {
        accept.accept(this);
        if (CollectionUtils.isNotEmpty(this.children)) {
            this.children.forEach(treeNode -> treeNode.forEach(accept));
        }
    }

    // -----------------------------------------------------------convert to TreeTrait
    public <E extends TreeTrait<T, A, E>> E convert(Function<TreeNode<T, A>, E> convert) {
        return convert(convert, true);
    }

    public <E extends TreeTrait<T, A, E>> E convert(Function<TreeNode<T, A>, E> convert, 
                                                    boolean containsUnavailable) {
        if (!this.available && !containsUnavailable) {
            // not contains unavailable node
            return null;
        }

        E root = convert.apply(this);
        this.convert(convert, root, containsUnavailable);
        return root;
    }

    // -----------------------------------------------------------private methods
    private <E extends BaseNode<T, A>> List<BaseNode<T, A>> prepare(List<E> nodes) {
        List<BaseNode<T, A>> list = Lists.newArrayListWithCapacity(nodes.size());

        // nodes list
        for (BaseNode<T, A> node : nodes) {
            if (node instanceof TreeNode) {
                // if tree node, then add all the tree nodes that includes the node's children(recursive)
                ((TreeNode<T, A>) node).forEach(list::add);
            } else {
                list.add(node); // node.copy(), node.clone()
            }
        }

        // the root node children
        if (CollectionUtils.isNotEmpty(this.children)) {
            this.children.forEach(treeNode -> treeNode.forEach(list::add));
            this.children.clear(); // reset before mount
        }
        return list;
    }

    private <E extends BaseNode<T, A>> void mount0(List<T> parentPath, List<E> nodes, 
                                                   boolean ignoreOrphan, T mountPidIfNull) {
        // current "this" is parent: AbstractNode parent = this;

        // 当前节点路径=父节点路径+当前节点
        // the "super" means defined in super class BaseNode's field, is not parent node
        super.path = buildPath(parentPath, super.nid); 

        // find child nodes for the current node
        for (Iterator<E> iter = nodes.iterator(); iter.hasNext();) {
            BaseNode<T, A> node = iter.next();

            if (!ignoreOrphan && Strings.isBlank(node.pid)) { // effect condition that pid is null
                // 不忽略孤儿节点且节点的父节点为空，则其父节点视为根节点（将其挂载到根节点下）
                Fields.put(node, "pid", mountPidIfNull); // pid is final modify
            }

            if (super.nid.equals(node.pid)) {
                // found a child node
                TreeNode<T, A> child = new TreeNode<>(
                    node.nid, node.pid, node.enabled, 
                    super.available && node.enabled, // recompute the child node is available
                    node.attach, this.siblingNodeOrders, 
                    this.buildPath, false
                );

                child.level = super.level + 1;
                this.children.add(child); // 挂载子节点

                iter.remove(); // remove the found child node
            }
        }

        if (CollectionUtils.isNotEmpty(this.children)) {
            // sort the children list(sibling nodes sort)
            this.children.sort(this.siblingNodeOrders);

            // recursion to mount child tree
            for (TreeNode<T, A> nt : this.children) {
                nt.mount0(super.path, nodes, ignoreOrphan, mountPidIfNull);
            }
        }
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
     * Returns the ImmutableList for current node path
     * 
     * @param parentPath the parent node path
     * @param nid        the current node id
     * @return a new ImmutableList appended current node id
     */
    private List<T> buildPath(List<T> parentPath, T nid) {
        if (!this.buildPath) {
            return null;
        }

        // already check duplicated, so cannot happened has circular dependencies state
        /*if (IterableUtils.matchesAny(parentPath, nid::equals)) {
            // 节点路径中已经包含了此节点，则视为环状
            throw new RuntimeException("Node circular dependencies: " + parentPath + " -> " + nid);
        }*/

        ImmutableList.Builder<T> builder = ImmutableList.builder();
        // root node uncontains null parent
        if (parentPath != null) {
            builder.addAll(parentPath);
        }
        return builder.add(nid).build();
    }

    private <E extends TreeTrait<T, A, E>> void convert(Function<TreeNode<T, A>, E> convert, 
                                                        E parent, boolean containsUnavailable) {
        if (CollectionUtils.isNotEmpty(this.children)) {
            List<E> list = new ArrayList<>(this.children.size());
            for (TreeNode<T, A> child : this.children) {
                if (child.available || containsUnavailable) { // filter unavailable
                    E node = convert.apply(child);
                    child.convert(convert, node, containsUnavailable);
                    list.add(node);
                }
            }
            parent.setChildren(list);
        } else {
            parent.setChildren(null);
        }
    }

    // -----------------------------------------------getter/setter
    public List<TreeNode<T, A>> getChildren() {
        return this.children;
    }

    // -----------------------------------------------------------------------------comparing by Attach
    public static <T extends Serializable & Comparable<? super T>, A extends Serializable, O extends Serializable & Comparable<? super O>> 
        Comparator<? super TreeNode<T, A>> comparing(Function<? super A, ? extends O> keyExtractor) {
        return comparing(keyExtractor, true);
    }

    public static <T extends Serializable & Comparable<? super T>, A extends Serializable, O extends Serializable & Comparable<? super O>> 
        Comparator<? super TreeNode<T, A>> comparing(Function<? super A, ? extends O> keyExtractor, boolean asc) {
        // First nullsLast will handle the cases when the "node" objects are null.
        // Second nullsLast will handle the cases when the return value of "keyExtractor.apply(node.attach)" is null.
        //Comparator.nullsLast(Comparator.<TreeNode<T, A>, O> comparing(node -> keyExtractor.apply(node.attach), Comparator.nullsLast(Comparators.order(asc))));

        return Comparator.comparing(n -> keyExtractor.apply(n.attach), Comparator.nullsLast(Comparators.order(asc)));
    }

    // -----------------------------------------------------------------------------comparing by Attach then after with TreeNode.nid
    public static <T extends Serializable & Comparable<? super T>, A extends Serializable, O extends Serializable & Comparable<? super O>> 
        Comparator<? super TreeNode<T, A>> comparingThenComparingNid(Function<? super A, ? extends O> keyExtractor) {
        return comparingThenComparingNid(keyExtractor, true);
    }

    public static <T extends Serializable & Comparable<? super T>, A extends Serializable, O extends Serializable & Comparable<? super O>> 
        Comparator<? super TreeNode<T, A>> comparingThenComparingNid(Function<? super A, ? extends O> keyExtractor, boolean asc) {
        return Comparator.<TreeNode<T, A>, O> comparing(
            n -> keyExtractor.apply(n.attach), Comparator.nullsLast(Comparators.order(asc))
        ).thenComparing(
            TreeNode::getNid
        );
    }

}
