/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.tree;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

/**
 * Builds tree node as root node
 * 
 * @author Ponfee
 * @param <T> the node id type
 * @param <A> the attachment biz object type
 */
public final class TreeNodeBuilder<T extends Serializable & Comparable<? super T>, A extends Serializable> {

    private final T nid;
    private final Comparator<? super TreeNode<T, A>> siblingNodeSort;

    private T       pid       = null;
    private boolean enabled   = true;
    private boolean available = true;
    private A       attach    = null;
    private boolean buildPath = true;

    private TreeNodeBuilder(T nid,Comparator<? super TreeNode<T, A>> siblingNodeSort) {
        this.nid = Objects.requireNonNull(nid);
        this.siblingNodeSort = Objects.requireNonNull(siblingNodeSort);
    }

    public static <T extends Serializable & Comparable<? super T>, A extends Serializable> TreeNodeBuilder<T, A> newBuilder(T nid) {
        return newBuilder(nid, Comparator.comparing(TreeNode::getNid));
    }

    public static <T extends Serializable & Comparable<? super T>, A extends Serializable> TreeNodeBuilder<T, A> newBuilder(T nid, Comparator<? super TreeNode<T, A>> siblingNodeSort) {
        return new TreeNodeBuilder<>(nid, siblingNodeSort);
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
            siblingNodeSort, buildPath, true
        );
    }

}
