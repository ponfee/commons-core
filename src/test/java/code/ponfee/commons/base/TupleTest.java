package code.ponfee.commons.base;

import code.ponfee.commons.base.tuple.Tuple2;
import code.ponfee.commons.base.tuple.Tuple4;
import org.junit.Assert;
import org.junit.Test;

public class TupleTest {

    public static void main(String[] args) {
    }

    @Test
    public void test() {
        Assert.assertEquals(Tuple2.of(1, 2), (Tuple2.of(1, 2)));
        Assert.assertTrue(Tuple2.of(1, 2).eq(1, 2));
        StringBuilder builder = new StringBuilder();
        for (Object e : Tuple4.of(1, 2, 3, 4)) {
            builder.append(e);
        }
        Assert.assertEquals(builder.toString(), "1234");
    }
}
