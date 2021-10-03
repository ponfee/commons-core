package code.ponfee.commons.base.tuple;

import java.util.Objects;

/**
 * Tuple3 consisting of three elements.
 *
 * @param <A> the type A
 * @param <B> the type B
 * @param <C> the type C
 * @author Ponfee
 */
public class Tuple3<A, B, C> extends Tuple2<A, B> {
    private static final long serialVersionUID = 2520989044037893824L;

    public C c;

    public Tuple3(A a, B b, C c) {
        super(a, b);
        this.c = c;
    }

    public static <A, B, C> Tuple3<A, B, C> of(A a, B b, C c) {
        return new Tuple3<>(a, b, c);
    }

    @Override
    public Object get(int index) {
        switch (index) {
            case  0: return a;
            case  1: return b;
            case  2: return c;
            default: throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public Object[] toArray() {
        return new Object[]{a, b, c};
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ", " + c + ")";
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && Objects.equals(c, ((Tuple3<?, ?, ?>) o).c);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c);
    }
}
