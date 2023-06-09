/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.base.tuple;

/**
 * Tuple1 consisting of one element.
 *
 * @author Ponfee
 */
public final class Tuple1<A> extends Tuple {
    private static final long serialVersionUID = -3627925720098458172L;

    public A a;

    public Tuple1(A a) {
        this.a = a;
    }

    public static <A> Tuple1<A> of(A a) {
        return new Tuple1<>(a);
    }

    @Override
    public <T> T get(int index) {
        if (index == 0) {
            return (T) a;
        } else {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public <T> void set(T value, int index) {
        if (index == 0) {
            a = (A) value;
        } else {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public Tuple1<A> copy() {
        return new Tuple1<>(a);
    }

}
