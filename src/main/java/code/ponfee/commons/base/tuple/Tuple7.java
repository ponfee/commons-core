package code.ponfee.commons.base.tuple;

import java.util.Objects;

/**
 * Tuple7 consisting of seven elements.
 *
 * @param <A> the type A
 * @param <B> the type B
 * @param <C> the type C
 * @param <D> the type D
 * @param <E> the type E
 * @param <F> the type F
 * @param <G> the type G
 * @author Ponfee
 */
public class Tuple7<A, B, C, D, E, F, G> extends Tuple6<A, B, C, D, E, F> {
    private static final long serialVersionUID = -5055304940772830843L;

    public G g;

    public Tuple7(A a, B b, C c, D d, E e, F f, G g) {
        super(a, b, c, d, e, f);
        this.g = g;
    }

    public static <A, B, C, D, E, F, G> Tuple7<A, B, C, D, E, F, G> of(A a, B b, C c, D d, E e, F f, G g) {
        return new Tuple7<>(a, b, c, d, e, f, g);
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
            case  6: return g;
            default: throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public Object[] toArray() {
        return new Object[]{a, b, c, d, e, f, g};
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ", " + c + ", " + d + ", " + e + ", " + f + ", " + g + ")";
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && Objects.equals(g, ((Tuple7<?, ?, ?, ?, ?, ?, ?>) o).g);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c, d, e, f, g);
    }
}
