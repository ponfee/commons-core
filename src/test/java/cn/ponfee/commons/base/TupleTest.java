package cn.ponfee.commons.base;

import cn.ponfee.commons.base.tuple.*;
import org.junit.Assert;
import org.junit.Test;

public class TupleTest {

    @Test
    public void test() {
        Assert.assertEquals(new Tuple0(), new Tuple0());
        Assert.assertEquals(Tuple1.of(1), Tuple1.of(1));
        Assert.assertEquals(Tuple2.of(1, 2), Tuple2.of(1, 2));
        Assert.assertTrue(Tuple2.of(1, 2).equals(1, 2));

        StringBuilder builder = new StringBuilder();
        for (Object e : Tuple4.of(1, 2, 3, 4)) {
            builder.append(e);
        }
        Assert.assertEquals(builder.toString(), "1234");

        Assert.assertEquals(Tuple3.of(1, 2, 3).join(", ", String::valueOf, "(", ")"), "(1, 2, 3)");

        Assert.assertEquals("()", Tuple0.of().toString());
        Assert.assertEquals("(1)", Tuple1.of(1).toString());
        Assert.assertEquals("(1, 2)", Tuple2.of(1, 2).toString());
    }
}
