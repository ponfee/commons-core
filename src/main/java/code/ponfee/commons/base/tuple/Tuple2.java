package code.ponfee.commons.base.tuple;

import java.util.Objects;

/**
 * Tuple2 consisting of two elements.
 *
 * @param <A> the type A
 * @param <B> the type B
 * @author Ponfee
 */
public class Tuple2<A, B> extends Tuple1<A> {
    private static final long serialVersionUID = -3627925720098458172L;

    public B b;

    public Tuple2(A a, B b) {
        super(a);
        this.b = b;
    }

    public static <A, B> Tuple2<A, B> of(A a, B b) {
        return new Tuple2<>(a, b);
    }

    @Override
    public Object get(int index) {
        switch (index) {
            case  0: return a;
            case  1: return b;
            default: throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public Object[] toArray() {
        return new Object[]{a, b};
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ")";
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && Objects.equals(b, ((Tuple2<?, ?>) o).b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }
}
