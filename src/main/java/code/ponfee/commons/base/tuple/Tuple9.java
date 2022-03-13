package code.ponfee.commons.base.tuple;

import java.util.Objects;

/**
 * Tuple9 consisting of nine elements.
 *
 * @author Ponfee
 */
public final class Tuple9<A, B, C, D, E, F, G, H, I> extends Tuple {
    private static final long serialVersionUID = 3462929449266307061L;

    public A a;
    public B b;
    public C c;
    public D d;
    public E e;
    public F f;
    public G g;
    public H h;
    public I i;

    public Tuple9(A a, B b, C c, D d, E e, F f, G g, H h, I i) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        this.g = g;
        this.h = h;
        this.i = i;
    }

    public static <A, B, C, D, E, F, G, H, I> Tuple9<A, B, C, D, E, F, G, H, I> of(A a, B b, C c, D d, E e, F f, G g, H h, I i) {
        return new Tuple9<>(a, b, c, d, e, f, g, h, i);
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
            case  6: return (T) g;
            case  7: return (T) h;
            case  8: return (T) i;
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
            case  6: g = (G) value; break;
            case  7: h = (H) value; break;
            case  8: i = (I) value; break;
            default: throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public Object[] toArray() {
        return new Object[]{a, b, c, d, e, f, g, h, i};
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ", " + c + ", " + d + ", " + e + ", " + f + ", " + g + ", " + h + ", " + i + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Tuple9)) {
            return false;
        }

        Tuple9<?, ?, ?, ?, ?, ?, ?, ?, ?> o = (Tuple9<?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
        return eq(o.a, o.b, o.c, o.d, o.e, o.f, o.g, o.h, o.i);
    }

    public boolean eq(Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i) {
        return Objects.equals(this.a, a)
            && Objects.equals(this.b, b)
            && Objects.equals(this.c, c)
            && Objects.equals(this.d, d)
            && Objects.equals(this.e, e)
            && Objects.equals(this.f, f)
            && Objects.equals(this.g, g)
            && Objects.equals(this.h, h)
            && Objects.equals(this.i, i);
    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = HASH_FACTOR * result + (b != null ? b.hashCode() : 0);
        result = HASH_FACTOR * result + (c != null ? c.hashCode() : 0);
        result = HASH_FACTOR * result + (d != null ? d.hashCode() : 0);
        result = HASH_FACTOR * result + (e != null ? e.hashCode() : 0);
        result = HASH_FACTOR * result + (f != null ? f.hashCode() : 0);
        result = HASH_FACTOR * result + (g != null ? g.hashCode() : 0);
        result = HASH_FACTOR * result + (h != null ? h.hashCode() : 0);
        result = HASH_FACTOR * result + (i != null ? i.hashCode() : 0);
        return result;
    }

    @Override
    public int length() {
        return 9;
    }

    @Override
    public Tuple9<A, B, C, D, E, F, G, H, I> copy() {
        return new Tuple9<>(a, b, c, d, e, f, g, h, i);
    }

}
