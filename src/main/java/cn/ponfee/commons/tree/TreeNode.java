/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.tree;

import cn.ponfee.commons.collect.Collects;
import cn.ponfee.commons.reflect.Fields;
import cn.ponfee.commons.tree.print.MultiwayTreePrinter;
import cn.ponfee.commons.util.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <pre>
 * Tree node structure
 *
 * ┌───────────────────────────┐
 * │              0            │
 * │        ┌─────┴─────┐      │
 * │        1           2      │
 * │    ┌───┴───┐   ┌───┴───┐  │
 * │    3       4   5       6  │
 * │  ┌─┴─┐   ┌─┘              │
 * │  7   8   9                │
 * └───────────────────────────┘
 *
 * 上面这棵二叉树中的遍历方式：
 *   DFS前序遍历：0137849256
 *   DFS中序遍历：7381940526
 *   DFS后序遍历：7839415620
 *   BFS广度优先：0123456789
 *   CFS孩子优先：0123478956         (备注：教科书上没有CFS一说，是我为方便说明描述而取名的)
 * </pre>
 *
 * @param <T> the node id type
 * @param <A> the attachment biz object type
 * @author Ponfee
 */
public final class TreeNode<T extends Serializable & Comparable<? super T>, A extends Serializable>
    extends BaseNode<T, A> {
    private static final long serialVersionUID = -9081626363752680404L;

    public static final String DEFAULT_ROOT_ID = "__ROOT__";

    // 用于比较兄弟节点次序
    private final Comparator<? super TreeNode<T, A>> siblingNodeSort;

    // 子节点列表（空列表则表示为叶子节点）
    private final LinkedList<TreeNode<T, A>> children = new LinkedList<>();

    // 是否构建path
    private final boolean buildPath;

    /**
     * Constructs a tree node
     * 
     * @param nid             the node id
     * @param pid             the parent node id(withhold this pid field value,
     *                        when use if the other root node mount this node as child)
     * @param enabled         the node is enabled
     * @param available       the current node is available(parent.available & this.enabled)
     * @param attach          the attachment for biz object
     * @param siblingNodeSort the comparator for sibling nodes(has the same parent node) by sort
     * @param buildPath       the if whether build path
     * @param doMount         the if whether do mount, if is inner new TreeNode then false else true
     */
    TreeNode(T nid, T pid, boolean enabled, boolean available, A attach, 
             Comparator<? super TreeNode<T, A>> siblingNodeSort,
             boolean buildPath, boolean doMount) {
        super(nid, pid, enabled, available, attach);

        this.siblingNodeSort = Objects.requireNonNull(siblingNodeSort);

        this.buildPath = buildPath;

        if (doMount) {
            this.mount(null); // as root node if new instance at external(TreeNodeBuilder) or of(TreeNode)
        }
    }

    // ------------------------------------------------------creates tree node by a base node
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
        of(BaseNode<T, A> node, Comparator<? super TreeNode<T, A>> siblingNodeSort) {
        return of(node, siblingNodeSort, true);
    }

    public static <T extends Serializable & Comparable<? super T>, A extends Serializable> TreeNode<T, A> 
        of(BaseNode<T, A> node, Comparator<? super TreeNode<T, A>> siblingNodeSort, boolean buildPath) {
        return new TreeNode<>(
            node.nid, node.pid, node.enabled, node.available, 
            node.attach, siblingNodeSort, buildPath, true
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
        List<T> checkDuplicateList = Collects.newLinkedList(super.nid);
        nodes.forEach(n -> checkDuplicateList.add(n.nid));
        List<T> duplicated = Collects.duplicate(checkDuplicateList);
        if (CollectionUtils.isNotEmpty(duplicated)) {
            throw new RuntimeException("Duplicated nodes: " + duplicated);
        }

        // 3、以此节点为根构建节点树
        super.level = 1;         // root node level is 1
        super.path = null;       // reset with null
        super.leftLeafCount = 0; // root node left leaf count is 1
        super.siblingOrder = 1;
        this.mount0(null, nodes, ignoreOrphan, super.nid);

        // 4、检查是否存在孤儿节点
        if (!ignoreOrphan && CollectionUtils.isNotEmpty(nodes)) {
            String nids = nodes.stream().map(e -> String.valueOf(e.getNid())).collect(Collectors.joining(","));
            throw new RuntimeException("Invalid orphan nodes: [" + nids + "]");
        }

        // 5、统计
        count();

        return this;
    }

    // -------------------------------------------------------------DFS
    /**
     * 深度优先搜索DFS(Depth-First Search)：使用前序遍历
     * <p>Should be invoking after {@link #mount(List)}
     * 
     * @return a list nodes for DFS tree node
     */
    public List<FlatNode<T, A>> flatDFS() {
        List<FlatNode<T, A>> collect = Lists.newLinkedList();
        Deque<TreeNode<T, A>> stack = Collects.newLinkedList(this);
        while (!stack.isEmpty()) {
            TreeNode<T, A> node = stack.pop();
            collect.add(new FlatNode<>(node));
            if (node.hasChildren()) {
                // 反向遍历子节点
                for (Iterator<TreeNode<T, A>> iter = node.children.descendingIterator(); iter.hasNext(); ) {
                    stack.push(iter.next());
                }
            }
        }
        return collect;
    }

    /*// 递归方式DFS
    public List<FlatNode<T, A>> flatDFS() {
        List<FlatNode<T, A>> collect = Lists.newLinkedList();
        dfs(collect);
        return collect;
    }
    private void dfs(List<FlatNode<T, A>> collect) {
        collect.add(new FlatNode<>(this));
        forEachChild(child -> child.dfs(collect));
    }
    */

    // -------------------------------------------------------------CFS
    /**
     * 按层级方式展开节点：兄弟节点相邻
     * <p>子节点优先搜索CFS(Children-First Search)
     * <p>Should be invoking after {@link #mount(List)}
     *
     * Note：为了构建复杂表头，保证左侧的叶子节点必须排在右侧叶子节点前面，此处不能用广度优先搜索策略
     *
     * @return a list nodes for CFS tree node
     */
    public List<FlatNode<T, A>> flatCFS() {
        List<FlatNode<T, A>> collect = Collects.newLinkedList(new FlatNode<>(this));
        Deque<TreeNode<T, A>> stack = Collects.newLinkedList(this);
        while (!stack.isEmpty()) {
            TreeNode<T, A> node = stack.pop();
            if (node.hasChildren()) {
                node.forEachChild(child -> collect.add(new FlatNode<>(child)));

                // 反向遍历子节点
                for (Iterator<TreeNode<T, A>> iter = node.children.descendingIterator(); iter.hasNext(); ) {
                    stack.push(iter.next());
                }
            }
        }
        return collect;
    }

    /*// 递归方式CFS
    public List<FlatNode<T, A>> flatCFS() {
        List<FlatNode<T, A>> collect = Collects.newLinkedList(new FlatNode<>(this));
        cfs(collect);
        return collect;
    }
    private void cfs(List<FlatNode<T, A>> collect) {
        forEachChild(child -> collect.add(new FlatNode<>(child)));
        forEachChild(child -> child.cfs(collect));
    }
    */

    // -------------------------------------------------------------BFS

    /**
     * 广度优先遍历BFS(Breath-First Search)
     *
     * @return a list nodes for BFS tree node
     */
    public List<FlatNode<T, A>> flatBFS() {
        List<FlatNode<T, A>> collect = new LinkedList<>();
        Queue<TreeNode<T, A>> queue = Collects.newLinkedList(this);
        while (!queue.isEmpty()) {
            for (int i = queue.size(); i > 0; i--) {
                TreeNode<T, A> node = queue.poll();
                collect.add(new FlatNode<>(node));
                node.forEachChild(queue::offer);
            }
        }
        return collect;
    }

    /*// 递归方式BFS
    public List<FlatNode<T, A>> flatBFS() {
        List<FlatNode<T, A>> collect = new LinkedList<>();
        Queue<TreeNode<T, A>> queue = Collects.newLinkedList(this);
        bfs(queue, collect);
        return collect;
    }
    private void bfs(Queue<TreeNode<T, A>> queue, List<FlatNode<T, A>> collect) {
        int size = queue.size();
        if (size == 0) {
            return;
        }
        while (size-- > 0) {
            TreeNode<T, A> node = queue.poll();
            collect.add(new FlatNode<>(node));
            node.forEachChild(queue::offer);
        }
        bfs(queue, collect);
    }
    */

    /**
     * Traverses the tree
     *
     * @param action the action function
     */
    public void forEach(Consumer<TreeNode<T, A>> action) {
        Deque<TreeNode<T, A>> stack = Collects.newLinkedList(this);
        while (!stack.isEmpty()) {
            TreeNode<T, A> node = stack.pop();
            action.accept(node);
            node.forEachChild(stack::push);
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

    // -----------------------------------------------------------getter/setter
    public LinkedList<TreeNode<T, A>> getChildren() {
        return this.children;
    }

    public String print(Function<TreeNode<T, A>, CharSequence> nodeLabel) throws IOException {
        StringBuilder builder = new StringBuilder();
        new MultiwayTreePrinter<>(builder, nodeLabel, TreeNode::getChildren).print(this);
        return builder.toString();
    }

    @Override
    public String toString() {
        try {
            return print(e -> String.valueOf(e.getNid()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------private methods
    private boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    private void forEachChild(Consumer<TreeNode<T, A>> action) {
        if (hasChildren()) {
            children.forEach(action);
        }
    }

    private <E extends BaseNode<T, A>> List<BaseNode<T, A>> prepare(List<E> nodes) {
        List<BaseNode<T, A>> list = Lists.newLinkedList();

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
        if (hasChildren()) {
            forEachChild(child -> child.forEach(list::add));
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
                    node.attach, this.siblingNodeSort,
                    this.buildPath, false
                );

                child.level = super.level + 1;
                this.children.add(child); // 挂载子节点

                iter.remove(); // remove the found child node
            }
        }

        if (hasChildren()) {
            // sort the children list(sibling nodes sort)
            this.children.sort(this.siblingNodeSort);

            // recursion to mount child tree
            forEachChild(child -> child.mount0(path, nodes, ignoreOrphan, mountPidIfNull));
        }
        super.degree = (this.children == null) ? 0 : this.children.size();
    }

    private void count() {
        if (CollectionUtils.isEmpty(this.children)) {
            // 叶子节点
            super.treeDepth     = 1;
            super.treeMaxDegree = 0;
            super.treeLeafCount = 1;
            super.treeNodeCount = 1;
            super.childrenCount = 0;
            return;
        }

        // 非叶子节点
        int maxChildTreeDepth        = 0, maxChildTreeMaxDegree    = 0,
            sumChildrenTreeLeafCount = 0, sumChildrenTreeNodeCount = 0;
        TreeNode<T, A> child;
        for (int i = 0; i < this.children.size(); i++) {
            child = this.children.get(i);
            child.siblingOrder = i + 1;

            // 1、统计左叶子节点数量
            if (i == 0) {
                // 是最左子节点：左叶子节点个数=父节点的左叶子节点个数
                child.leftLeafCount = super.leftLeafCount;
            } else {
                // 非最左子节点：左叶子节点个数=相邻左兄弟节点的左叶子节点个数+该兄弟节点的子节点个数
                TreeNode<T, A> prevSibling = this.children.get(i - 1);
                child.leftLeafCount = prevSibling.leftLeafCount + prevSibling.treeLeafCount;
            }

            // 2、递归
            child.count();

            // 3、统计子叶子节点数量及整棵树节点的数量
            maxChildTreeDepth         = Math.max(maxChildTreeDepth, child.treeDepth);
            maxChildTreeMaxDegree     = Math.max(maxChildTreeMaxDegree, child.degree);
            sumChildrenTreeLeafCount += child.treeLeafCount;
            sumChildrenTreeNodeCount += child.treeNodeCount;
        }

        super.treeDepth     = maxChildTreeDepth + 1;                         // 加上自身节点的层级
        super.treeMaxDegree = Math.max(maxChildTreeMaxDegree, super.degree); // 树中的最大度数
        super.treeLeafCount = sumChildrenTreeLeafCount;                      // 子节点的叶子节点之和
        super.treeNodeCount = sumChildrenTreeNodeCount + 1;                  // 要包含节点本身
        super.childrenCount = this.children.size();                          // 子节点数量
    }

    /**
     * Returns a immutable list for current node path
     * 
     * @param parentPath the parent node path
     * @param nid        the current node id
     * @return a immutable list appended current node id
     */
    private List<T> buildPath(List<T> parentPath, T nid) {
        if (!this.buildPath) {
            return null;
        }

        // already check duplicated, so cannot happen has circular dependencies state
        /*
        if (IterableUtils.matchesAny(parentPath, nid::equals)) {
            // 节点路径中已经包含了此节点，则视为环状
            throw new RuntimeException("Node circular dependencies: " + parentPath + " -> " + nid);
        }
        */

        int size = parentPath == null ? 1 : parentPath.size() + 1;
        ImmutableList.Builder<T> builder = ImmutableList.builderWithExpectedSize(size);
        // root node un-contains null parent
        if (parentPath != null) {
            builder.addAll(parentPath);
        }
        return builder.add(nid).build();
    }

    private <E extends TreeTrait<T, A, E>> void convert(Function<TreeNode<T, A>, E> convert, 
                                                        E parent, boolean containsUnavailable) {
        if (CollectionUtils.isEmpty(this.children)) {
            parent.setChildren(null);
            return;
        }

        List<E> list = new LinkedList<>();
        for (TreeNode<T, A> child : this.children) {
            if (child.available || containsUnavailable) { // filter unavailable
                E node = convert.apply(child);
                child.convert(convert, node, containsUnavailable);
                list.add(node);
            }
        }
        parent.setChildren(list);
    }

}
