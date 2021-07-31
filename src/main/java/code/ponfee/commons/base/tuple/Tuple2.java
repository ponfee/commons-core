package code.ponfee.commons.base.tuple;

/**
 * Tuple2 consisting of two elements.
 *
 * @param <A>
 * @param <B>
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
    public String toString() {
        return "(" + a + ", " + b + ")";
    }

}
