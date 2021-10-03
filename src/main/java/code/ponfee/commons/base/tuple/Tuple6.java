package code.ponfee.commons.base.tuple;

import java.util.Objects;

/**
 * Tuple6 consisting of six elements.
 *
 * @param <A> the type A
 * @param <B> the type B
 * @param <C> the type C
 * @param <D> the type D
 * @param <E> the type E
 * @param <F> the type F
 * @author Ponfee
 */
public class Tuple6<A, B, C, D, E, F> extends Tuple5<A, B, C, D, E> {
    private static final long serialVersionUID = -5055304940772830843L;

    public F f;

    public Tuple6(A a, B b, C c, D d, E e, F f) {
        super(a, b, c, d, e);
        this.f = f;
    }

    public static <A, B, C, D, E, F> Tuple6<A, B, C, D, E, F> of(A a, B b, C c, D d, E e, F f) {
        return new Tuple6<>(a, b, c, d, e, f);
    }

    @Override
    public Object get(int index) {
        switch (index) {
            case  0: return a;
            case  1: return b;
            case  2: return c;
            case  3: return d;
            case  4: return e;
            case  5: return f;
            default: throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public Object[] toArray() {
        return new Object[]{a, b, c, d, e, f};
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ", " + c + ", " + d + ", " + e + ", " + f + ")";
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && Objects.equals(f, ((Tuple6<?, ?, ?, ?, ?, ?>) o).f);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c, d, e, f);
    }
}
