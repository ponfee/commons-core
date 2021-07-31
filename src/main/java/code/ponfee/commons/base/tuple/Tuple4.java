package code.ponfee.commons.base.tuple;

/**
 * Tuple4 consisting of four elements.
 *
 * @param <A>
 * @param <B>
 * @param <C>
 * @param <D>
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
    public String toString() {
        return "(" + a + ", " + b + ", " + c + ", " + d + ")";
    }

}
