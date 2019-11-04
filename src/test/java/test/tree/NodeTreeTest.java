/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package test.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.tree.BaseNode;
import code.ponfee.commons.tree.TreeNode;

/**
 * 
 * @author Ponfee
 */
public class NodeTreeTest {

    @Test
    public void test1() {
        List<BaseNode<String, String>> list = new ArrayList<>();
        list.add(new BaseNode<>("100000", null, true, "nid100000"));
        list.add(new BaseNode<>("100010", "100000", true, "nid100010"));
        list.add(new BaseNode<>("100011", "100010", false, "nid100011"));
        list.add(new BaseNode<>("100012", "100010", true, "nid100012"));
        list.add(new BaseNode<>("100020", "100000", false, "nid100020"));
        list.add(new BaseNode<>("100021", "100020", true, "nid100020"));
        list.add(new BaseNode<>("100022", "100020", false, "nid100022"));

        list.add(new BaseNode<>("200000", null, true, "nid200000"));

        list.add(new BaseNode<>("300000", null, true, "nid300000"));

        list.add(new BaseNode<>("400000", null, true, "nid400000"));

        TreeNode<String, String> subtree = TreeNode.of("400010", "400000", true, Comparator.comparing(n -> ThreadLocalRandom.current().nextInt(10)));
        subtree.mount(Arrays.asList(
            new BaseNode<>("400011", "400010", true, "nid400011"),
            new BaseNode<>("400012", "400010", false, "nid400012")
        ));
        list.add(subtree); // add a tree node

        list.add(new BaseNode<>("500000", null, false, "nid500000"));
        list.add(new BaseNode<>("500010", "500000", true, "nid500010"));
        list.add(new BaseNode<>("500011", "500010", true, "nid500011"));

        TreeNode<String, String> root = TreeNode.of(TreeNode.DEFAULT_ROOT_ID, Comparator.comparing(BaseNode::getNid));
        System.out.println(Jsons.toJson(root));

        root.mount(list); // mount
        System.out.println(Jsons.toJson(root));
        System.out.println(Jsons.toJson(root.dfsFlat()));
        System.out.println(Jsons.toJson(root.bfsFlat()));
    }

    @Test
    public void test2() {
        List<BaseNode<String, String>> list = new ArrayList<>();
        list.add(new BaseNode<>("a", "b", true, "")); // 节点循环依赖
        list.add(new BaseNode<>("b", "a", true, ""));

        TreeNode<String, String> root = TreeNode.of(TreeNode.DEFAULT_ROOT_ID);
        root.mount(list);
        System.out.println(Jsons.toJson(root));
    }

    @Test
    public void test3() {
        List<BaseNode<String, String>> list = new ArrayList<>();
        list.add(new BaseNode<>("100001", null, true, "nid100010")); // 节点编号不能为空
        list.add(new BaseNode<>(null, "100001", true, "nid100010"));
        TreeNode<String, String> root = TreeNode.of(TreeNode.DEFAULT_ROOT_ID);
        root.mount(list);
        System.out.println(Jsons.toJson(root));
    }

    @Test
    public void test4() {
        List<BaseNode<String, String>> list = new ArrayList<>();
        list.add(new BaseNode<>("100000", "notfound", true, "nid100000")); // 无效的孤儿节点
        list.add(new BaseNode<>("200000", "notfound", true, "nid200000")); // 无效的孤儿节点

        TreeNode.<String, String> of(TreeNode.DEFAULT_ROOT_ID).mount(list);
    }
}
