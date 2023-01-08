package cn.ponfee.commons.collects;

import cn.ponfee.commons.collect.DoubleListViewer;
import com.google.common.base.Joiner;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ponfee
 */
public class DoubleListViewerTest {

    @Test
    public void test1() {
        List<Integer> list1 = new ArrayList<>();
        list1.add(1);
        list1.add(2);
        list1.add(3);

        List<Integer> list2 = new ArrayList<>();
        list2.add(3);
        list2.add(4);
        list2.add(5);

        DoubleListViewer viewer = new DoubleListViewer(Arrays.asList(list1, list2));

        Assert.assertEquals(viewer.get(0), 1);
        Assert.assertEquals(viewer.get(5), 5);
        Assert.assertEquals(viewer.indexOf(1), 0);
        Assert.assertEquals(viewer.indexOf(10), -1);
        Assert.assertEquals("[1, 2, 3, 3, 4, 5]", viewer.toString());
        Assert.assertEquals("1,2,3,3,4,5", Joiner.on(",").join(viewer));
        Assert.assertThrows(IndexOutOfBoundsException.class, () -> viewer.get(6));
    }

}
