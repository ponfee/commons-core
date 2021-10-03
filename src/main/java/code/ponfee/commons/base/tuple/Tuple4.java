package code.ponfee.commons.base.tuple;

import java.util.Objects;

/**
 * Tuple4 consisting of four elements.
 *
 * @param <A> the type A
 * @param <B> the type B
 * @param <C> the type C
 * @param <D> the type D
 * @author Ponfee
 */
public class Tuple4<A, B, C, D> extends Tuple3<A, B, C> {
    private static final long serialVersionUID = 6317175705377921586L;

    public D d;

    public Tuple4(A a, B b, C c, D d) {
        super(a, b, c);
        this.d = d;
    }

    public static <A, B, C, D> Tuple4<A, B, C, D> of(A a, B b, C c, D d) {
        return new Tuple4<>(a, b, c, d);
    }

    @Override
    public Object get(int index) {
        switch (index) {
            case  0: return a;
            case  1: return b;
            case  2: return c;
            case  3: return d;
            default: throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public Object[] toArray() {
        return new Object[]{a, b, c, d};
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ", " + c + ", " + d + ")";
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && Objects.equals(d, ((Tuple4<?, ?, ?, ?>) o).d);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c, d);
    }
}
