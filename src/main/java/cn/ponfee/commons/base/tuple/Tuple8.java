/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.base.tuple;

/**
 * Tuple8 consisting of eight elements.
 *
 * @author Ponfee
 */
public final class Tuple8<A, B, C, D, E, F, G, H> extends Tuple {
    private static final long serialVersionUID = 3607273779775623549L;

    public A a;
    public B b;
    public C c;
    public D d;
    public E e;
    public F f;
    public G g;
    public H h;

    public Tuple8(A a, B b, C c, D d, E e, F f, G g, H h) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        this.g = g;
        this.h = h;
    }

    public static <A, B, C, D, E, F, G, H> Tuple8<A, B, C, D, E, F, G, H> of(A a, B b, C c, D d, E e, F f, G g, H h) {
        return new Tuple8<>(a, b, c, d, e, f, g, h);
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
            default: throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public int length() {
        return 8;
    }

    @Override
    public Tuple8<A, B, C, D, E, F, G, H> copy() {
        return new Tuple8<>(a, b, c, d, e, f, g, h);
    }

}
