/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.base.tuple;

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
    public int length() {
        return 5;
    }

    @Override
    public Tuple5<A, B, C, D, E> copy() {
        return new Tuple5<>(a, b, c, d, e);
    }

}
