/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.base.tuple;

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
    public int length() {
        return 7;
    }

    @Override
    public Tuple7<A, B, C, D, E, F, G> copy() {
        return new Tuple7<>(a, b, c, d, e, f, g);
    }

}
