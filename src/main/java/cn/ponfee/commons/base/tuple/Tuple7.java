/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.base.tuple;

import java.util.Objects;

/**
 * Tuple7 consisting of seven elements.
 *
 * @author Ponfee
 */
public final class Tuple7<A, B, C, D, E, F, G> extends Tuple {
    private static final long serialVersionUID = 4235194450172178770L;

    public A a;
    public B b;
    public C c;
    public D d;
    public E e;
    public F f;
    public G g;

    public Tuple7(A a, B b, C c, D d, E e, F f, G g) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        this.g = g;
    }

    public static <A, B, C, D, E, F, G> Tuple7<A, B, C, D, E, F, G> of(A a, B b, C c, D d, E e, F f, G g) {
        return new Tuple7<>(a, b, c, d, e, f, g);
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Tuple7)) {
            return false;
        }

        Tuple7<?, ?, ?, ?, ?, ?, ?> o = (Tuple7<?, ?, ?, ?, ?, ?, ?>) obj;
        return eq(o.a, o.b, o.c, o.d, o.e, o.f, o.g);
    }

    public boolean eq(Object a, Object b, Object c, Object d, Object e, Object f, Object g) {
        return Objects.equals(this.a, a)
            && Objects.equals(this.b, b)
            && Objects.equals(this.c, c)
            && Objects.equals(this.d, d)
            && Objects.equals(this.e, e)
            && Objects.equals(this.f, f)
            && Objects.equals(this.g, g);
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
        return result;
    }

    @Override
    public int length() {
        return 7;
    }

    @Override
    public Tuple7<A, B, C, D, E, F, G> copy() {
        return new Tuple7<>(a, b, c, d, e, f, g);
    }

}
