package code.ponfee.commons.tree;

import code.ponfee.commons.collect.Comparators;

import java.io.Serializable;
import java.util.Comparator;
import java.util.function.Function;

/**
 * 兄弟节点排序比较器
 *
 * @author Ponfee
 */
public class SiblingNodeComparator {

    // -----------------------------------------------------------comparing by Attach
    public static <T extends Serializable & Comparable<? super T>, A extends Serializable, O extends Serializable & Comparable<? super O>>
        Comparator<? super TreeNode<T, A>> compareAttach(Function<? super A, ? extends O> keyExtractor) {
        return compareAttach(keyExtractor, true);
    }

    public static <T extends Serializable & Comparable<? super T>, A extends Serializable, O extends Serializable & Comparable<? super O>>
        Comparator<? super TreeNode<T, A>> compareAttach(Function<? super A, ? extends O> keyExtractor, boolean asc) {
        /*Comparator.nullsLast( // First nullsLast will handle the cases when the "node" objects are null.
            Comparator.<TreeNode<T, A>, O> comparing(
                // Second nullsLast will handle the cases when the return value of "keyExtractor.apply(node.attach)" is null.
                node -> keyExtractor.apply(node.attach), Comparator.nullsLast(Comparators.order(asc))
            )
        );*/

        // node be null cannot happened
        return Comparator.comparing(node -> keyExtractor.apply(node.attach), Comparator.nullsLast(Comparators.order(asc)));
    }

    // -----------------------------------------------------------comparing by Attach then after with TreeNode.nid
    public static <T extends Serializable & Comparable<? super T>, A extends Serializable, O extends Serializable & Comparable<? super O>>
        Comparator<? super TreeNode<T, A>> compareAttachThenNid(Function<? super A, ? extends O> keyExtractor) {
        return compareAttachThenNid(keyExtractor, true);
    }

    public static <T extends Serializable & Comparable<? super T>, A extends Serializable, O extends Serializable & Comparable<? super O>>
        Comparator<? super TreeNode<T, A>> compareAttachThenNid(Function<? super A, ? extends O> keyExtractor, boolean asc) {
        return Comparator.<TreeNode<T, A>, O>comparing(
            n -> keyExtractor.apply(n.attach), Comparator.nullsLast(Comparators.order(asc))
        ).thenComparing(
            TreeNode::getNid
        );
    }

}
