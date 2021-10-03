package code.ponfee.commons.base.tuple;

import java.util.Objects;

/**
 * Tuple5 consisting of five elements.
 *
 * @param <A> the type A
 * @param <B> the type B
 * @param <C> the type C
 * @param <D> the type D
 * @param <E> the type E
 * @author Ponfee
 */
public class Tuple5<A, B, C, D, E> extends Tuple4<A, B, C, D> {
    private static final long serialVersionUID = -5055304940772830843L;

    public E e;

    public Tuple5(A a, B b, C c, D d, E e) {
        super(a, b, c, d);
        this.e = e;
    }

    public static <A, B, C, D, E> Tuple5<A, B, C, D, E> of(A a, B b, C c, D d, E e) {
        return new Tuple5<>(a, b, c, d, e);
    }

    @Override
    public Object get(int index) {
        switch (index) {
            case  0: return a;
            case  1: return b;
            case  2: return c;
            case  3: return d;
            case  4: return e;
            default: throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public Object[] toArray() {
        return new Object[]{a, b, c, d, e};
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ", " + c + ", " + d + ", " + e + ")";
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && Objects.equals(e, ((Tuple5<?, ?, ?, ?, ?>) o).e);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c, d, e);
    }
}
