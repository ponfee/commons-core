package code.ponfee.commons.tree;

import java.io.Serializable;
import java.util.Comparator;

import javax.validation.constraints.NotNull;

/**
 * Builds tree node as root node
 * 
 * @author Ponfee
 * @param <T> the node id type
 * @param <A> the attachment biz object type
 */
public final class TreeNodeBuilder<T extends Serializable & Comparable<? super T>, A extends Serializable> {

    private final T nid;
    private final Comparator<? super TreeNode<T, A>> siblingNodeOrders;

    private T       pid       = null;
    private boolean enabled   = true;
    private boolean available = true;
    private A       attach    = null;
    boolean         buildPath = true;

    private TreeNodeBuilder(@NotNull T nid, @NotNull Comparator<? super TreeNode<T, A>> siblingNodeOrders) {
        this.nid = nid;
        this.siblingNodeOrders = siblingNodeOrders;
    }

    public static <T extends Serializable & Comparable<? super T>, A extends Serializable> TreeNodeBuilder<T, A> 
        newBuilder(T nid) {
        return newBuilder(nid, Comparator.comparing(TreeNode::getNid));
    }

    public static <T extends Serializable & Comparable<? super T>, A extends Serializable> TreeNodeBuilder<T, A> 
        newBuilder(T nid, Comparator<? super TreeNode<T, A>> siblingNodeOrders) {
        return new TreeNodeBuilder<>(nid, siblingNodeOrders);
    }

    public TreeNodeBuilder<T, A> pid(T pid) {
        this.pid = pid;
        return this;
    }

    public TreeNodeBuilder<T, A> enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public TreeNodeBuilder<T, A> available(boolean available) {
        this.available = available;
        return this;
    }

    public TreeNodeBuilder<T, A> attach(A attach) {
        this.attach = attach;
        return this;
    }

    public TreeNodeBuilder<T, A> buildPath(boolean buildPath) {
        this.buildPath = buildPath;
        return this;
    }

    public TreeNode<T, A> build() {
        return new TreeNode<>(
            nid, pid, enabled, available, attach, 
            siblingNodeOrders, buildPath, true
        );
    }

}
