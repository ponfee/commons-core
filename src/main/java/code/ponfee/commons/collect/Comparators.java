package code.ponfee.commons.collect;

import java.util.Comparator;

/**
 * For collection order
 * 
 * @author Ponfee
 */
public final class Comparators {

    public static <T extends Comparable<? super T>> Comparator<T> asc() {
        return Comparator.naturalOrder();
    }

    public static <T extends Comparable<? super T>> Comparator<T> desc() {
        return Comparator.reverseOrder();
    }

    public static <T extends Comparable<? super T>> Comparator<T> order(boolean asc) {
        return asc ? asc() : desc();
    }

}
