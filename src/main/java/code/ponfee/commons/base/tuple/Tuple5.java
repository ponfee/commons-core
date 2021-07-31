package code.ponfee.commons.base.tuple;

/**
 * Tuple5 consisting of five elements.
 *
 * @param <A>
 * @param <B>
 * @param <C>
 * @param <D>
 * @param <E>
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
    public String toString() {
        return "(" + a + ", " + b + ", " + c + ", " + d + ", " + e + ")";
    }

}
