package code.ponfee.commons.base.tuple;

import java.util.Objects;

/**
 * Tuple6 consisting of six elements.
 *
 * @author Ponfee
 */
public final class Tuple6<A, B, C, D, E, F> extends Tuple {
    private static final long serialVersionUID = 8697978867751048118L;

    public A a;
    public B b;
    public C c;
    public D d;
    public E e;
    public F f;

    public Tuple6(A a, B b, C c, D d, E e, F f) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }

    public static <A, B, C, D, E, F> Tuple6<A, B, C, D, E, F> of(A a, B b, C c, D d, E e, F f) {
        return new Tuple6<>(a, b, c, d, e, f);
    }

    @Override
    public <T> T get(int index) {
        switch (index) {
            case  0: return (T) a;
            case  1: return (T) b;
            case  2: return (T) c;
            case  3: return (T) d;
            case  4: return (T) e;
            case  5: return (T) f;
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
            case  5: f = (F) value; break;
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Tuple6)) {
            return false;
        }

        Tuple6<?, ?, ?, ?, ?, ?> o = (Tuple6<?, ?, ?, ?, ?, ?>) obj;
        return eq(o.a, o.b, o.c, o.d, o.e, o.f);
    }

    public boolean eq(Object a, Object b, Object c, Object d, Object e, Object f) {
        return Objects.equals(this.a, a)
            && Objects.equals(this.b, b)
            && Objects.equals(this.c, c)
            && Objects.equals(this.d, d)
            && Objects.equals(this.e, e)
            && Objects.equals(this.f, f);
    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = HASH_FACTOR * result + (b != null ? b.hashCode() : 0);
        result = HASH_FACTOR * result + (c != null ? c.hashCode() : 0);
        result = HASH_FACTOR * result + (d != null ? d.hashCode() : 0);
        result = HASH_FACTOR * result + (e != null ? e.hashCode() : 0);
        result = HASH_FACTOR * result + (f != null ? f.hashCode() : 0);
        return result;
    }

    @Override
    public int length() {
        return 6;
    }

    @Override
    public Tuple6<A, B, C, D, E, F> copy() {
        return new Tuple6<>(a, b, c, d, e, f);
    }

}
