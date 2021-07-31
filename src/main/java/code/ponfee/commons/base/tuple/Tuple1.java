package code.ponfee.commons.base.tuple;

import java.io.Serializable;

/**
 * Tuple1 consisting of one elements.
 *
 * @param <A>
 * @author Ponfee
 */
public class Tuple1<A> implements Serializable {
    private static final long serialVersionUID = -3627925720098458172L;

    public A a;

    public Tuple1(A a) {
        this.a = a;
    }

    public static <A, B> Tuple1<A> of(A a) {
        return new Tuple1<>(a);
    }

    @Override
    public String toString() {
        return "(" + a + ")";
    }

}
