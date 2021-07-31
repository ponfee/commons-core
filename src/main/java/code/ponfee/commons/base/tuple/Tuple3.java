package code.ponfee.commons.base.tuple;

/**
 * Tuple3 consisting of three elements.
 *
 * @param <A>
 * @param <B>
 * @param <C>
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
    public String toString() {
        return "(" + a + ", " + b + ", " + c + ")";
    }
    
}
