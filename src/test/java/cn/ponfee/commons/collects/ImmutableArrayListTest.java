package cn.ponfee.commons.collects;

import cn.ponfee.commons.collect.ImmutableArrayList;
import cn.ponfee.commons.json.Jsons;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ImmutableArrayListTest {

    @Test
    public void test1() {
        ImmutableArrayList<Object> list = ImmutableArrayList.of();
        Assert.assertEquals("[]", list.toString());
        Assert.assertEquals("[]", list.subList(0, 0).toString());
    }

    @Test
    public void test2() {
        ImmutableArrayList<Integer> list = ImmutableArrayList.of(1, 2, 3, 4, 5);
        Assert.assertEquals("[1, 2, 3, 4, 5]", list.toString());
        Assert.assertEquals("[]", list.subList(0, 0).toString());
        Assert.assertTrue(CollectionUtils.isEqualCollection(list.subList(2, 4), Arrays.asList(1, 2, 3, 4, 5).subList(2, 4)));

        Assert.assertTrue(CollectionUtils.isEqualCollection(IteratorUtils.toList(list.subList(2, 4).iterator()), IteratorUtils.toList(Arrays.asList(1, 2, 3, 4, 5).subList(2, 4).iterator())));
        Assert.assertTrue(CollectionUtils.isEqualCollection(IteratorUtils.toList(list.subList(2, 4).listIterator()), IteratorUtils.toList(Arrays.asList(1, 2, 3, 4, 5).subList(2, 4).listIterator())));
        Assert.assertTrue(CollectionUtils.isEqualCollection(IteratorUtils.toList(list.subList(2, 4).listIterator(1)), IteratorUtils.toList(Arrays.asList(1, 2, 3, 4, 5).subList(2, 4).listIterator(1))));
        Assert.assertTrue(list.subList(2, 4).contains(3));
        Assert.assertFalse(list.subList(2, 4).contains(9));
        Assert.assertEquals(list.subList(2, 4).indexOf(1), -1);
        Assert.assertEquals(ImmutableArrayList.of(0, 1, 2, 3, 1, 5, 1, 6).subList(1, 6).indexOf(1), 0);
        Assert.assertEquals(ImmutableArrayList.of(0, 1, 2, 3, 1, 5, 1, 6).subList(1, 6).lastIndexOf(1), 3);
        Assert.assertTrue(ImmutableArrayList.of(0, 1, 2, 3, 1, 5, 1, 6).subList(1, 6).containsAll(Arrays.asList(1, 3, 5)));
        Assert.assertFalse(ImmutableArrayList.of(0, 1, 2, 3, 1, 5, 1, 6).subList(1, 6).containsAll(Arrays.asList(1, 3, 5, 6)));
        Assert.assertEquals(Jsons.toJson(Arrays.asList(0, 1, 2, 3, 1, 5, 1, 6).subList(1, 6).toArray(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0})), Jsons.toJson(ImmutableArrayList.of(0, 1, 2, 3, 1, 5, 1, 6).subList(1, 6).toArray(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0})));
        Assert.assertEquals(Jsons.toJson(Arrays.asList(0, 1, 2, 3, 1, 5, 1, 6).subList(1, 6).toArray(new Integer[]{})), Jsons.toJson(ImmutableArrayList.of(0, 1, 2, 3, 1, 5, 1, 6).subList(1, 6).toArray(new Integer[]{})));
        Assert.assertEquals(Jsons.toJson(Arrays.asList(0, 1, 2, 3, 1, 5, 1, 6).subList(1, 6).toArray()), Jsons.toJson(ImmutableArrayList.of(0, 1, 2, 3, 1, 5, 1, 6).subList(1, 6).toArray()));
        Assert.assertEquals(Arrays.asList(0, 1, 2, 3, 1, 5, 1, 6).subList(1, 6).hashCode(), ImmutableArrayList.of(0, 1, 2, 3, 1, 5, 1, 6).subList(1, 6).hashCode());

        Assert.assertTrue(CollectionUtils.isEqualCollection(Arrays.asList(0, 1, 2), ImmutableArrayList.of(0, 1).concat(2)));

        StringBuilder builder = new StringBuilder();
        Arrays.asList(0, 1, 2, 3, 1, 5, 1, 6).subList(1, 6).spliterator().trySplit().forEachRemaining(builder::append);
        Assert.assertEquals("12315", builder.toString());
    }
}
