/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package test.tree;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.junit.Assert;
import org.junit.Test;

import code.ponfee.commons.tree.NodePath;

/**
 * 
 * 
 * @author Ponfee
 */
public class NodePathTest {

    @Test
    public void test1() {
        System.out.println(CollectionUtils.isEqualCollection(Arrays.asList(1, 2, 3), Arrays.asList(3, 2, 1)));
        System.out.println(ListUtils.isEqualList(Arrays.asList(1, 2, 3), Arrays.asList(3, 2, 1)));
        System.out.println(ListUtils.isEqualList(Arrays.asList(1, 2, 3), Arrays.asList(1, 2, 3)));
    }

    @Test
    public void test2() {
        NodePath<Integer> p1 = new NodePath<>(1, 2, 3, 4);
        NodePath<Integer> p2 = new NodePath<>(1, 2, 3, 4);
        System.out.println(p1.equals(p2));
        System.out.println(p1.compareTo(p2));

        System.out.println("\n\n=========================");
        p1 = new NodePath<>(1, 2, 3, 4);
        p2 = new NodePath<>(1, 2);
        System.out.println(p1.equals(p2));
        System.out.println(p1.compareTo(p2));

        System.out.println("\n\n=========================");
        p1 = new NodePath<>(1, 2, 3, 4);
        p2 = new NodePath<>(4, 2);
        System.out.println(p1.equals(p2));
        System.out.println(p1.compareTo(p2));
    }

    @Test
    public void test3() {
        Map<NodePath<Integer>, Object> map = new HashMap<>();
        map.put(new NodePath<>(1, 2, 3, 4), "xx");
        Assert.assertNotNull(map.get(new NodePath<>(1, 2, 3, 4)));
        Assert.assertNull(map.get(new NodePath<>(1, 2, 3)));
    }
}
