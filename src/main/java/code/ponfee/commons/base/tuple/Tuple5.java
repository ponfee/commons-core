package code.ponfee.commons.base.tuple;

import java.util.Objects;

/**
 * Tuple5 consisting of five elements.
 *
 * @author Ponfee
 */
public final class Tuple5<A, B, C, D, E> extends Tuple {
    private static final long serialVersionUID = -528096819207260665L;

    public A a;
    public B b;
    public C c;
    public D d;
    public E e;

    public Tuple5(A a, B b, C c, D d, E e) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
    }

    public static <A, B, C, D, E> Tuple5<A, B, C, D, E> of(A a, B b, C c, D d, E e) {
        return new Tuple5<>(a, b, c, d, e);
    }

    @Override
    public <T> T get(int index) {
        switch (index) {
            case  0: return (T) a;
            case  1: return (T) b;
            case  2: return (T) c;
            case  3: return (T) d;
            case  4: return (T) e;
            default: throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public <T> void set(T value, int index) {
        switch (index) {
            case  0: a = (A) value; break;
            case  1: b = (B) value; break;
            case  2: c = (C) value; break;
            case  3: d = (D) value; break;
            case  4: e = (E) value; break;
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Tuple5)) {
            return false;
        }

        Tuple5<?, ?, ?, ?, ?> o = (Tuple5<?, ?, ?, ?, ?>) obj;
        return eq(o.a, o.b, o.c, o.d, o.e);
    }

    public boolean eq(Object a, Object b, Object c, Object d, Object e) {
        return Objects.equals(this.a, a)
            && Objects.equals(this.b, b)
            && Objects.equals(this.c, c)
            && Objects.equals(this.d, d)
            && Objects.equals(this.e, e);
    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = HASH_FACTOR * result + (b != null ? b.hashCode() : 0);
        result = HASH_FACTOR * result + (c != null ? c.hashCode() : 0);
        result = HASH_FACTOR * result + (d != null ? d.hashCode() : 0);
        result = HASH_FACTOR * result + (e != null ? e.hashCode() : 0);
        return result;
    }

    @Override
    public int length() {
        return 5;
    }

    @Override
    public Tuple5<A, B, C, D, E> copy() {
        return new Tuple5<>(a, b, c, d, e);
    }

}
