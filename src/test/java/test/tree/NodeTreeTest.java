package test.tree;

import cn.ponfee.commons.json.Jsons;
import cn.ponfee.commons.tree.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 
 * @author Ponfee
 */
public class NodeTreeTest {

    @Test
    public void test1() {
        List<BaseNode<String, String>> list = new ArrayList<>();
        list.add(new PlainNode<>("100000", null, true, "nid100000"));
        list.add(new PlainNode<>("100010", "100000", true, "nid100010"));
        list.add(new PlainNode<>("100011", "100010", false, "nid100011"));
        list.add(new PlainNode<>("100012", "100010", true, "nid100012"));
        list.add(new PlainNode<>("100020", "100000", false, "nid100020"));
        list.add(new PlainNode<>("100021", "100020", true, "nid100020"));
        list.add(new PlainNode<>("100022", "100020", false, "nid100022"));

        list.add(new PlainNode<>("200000", null, true, "nid200000"));

        list.add(new PlainNode<>("300000", null, true, "nid300000"));

        list.add(new PlainNode<>("400000", null, true, "nid400000"));

        // do mount first
        TreeNode<String, String> subtree = TreeNodeBuilder.<String, String> newBuilder(
            "400010", Comparator.comparing(node -> ThreadLocalRandom.current().nextInt(10))
        ).pid("400000").enabled(true).build();

        // do mount second
        subtree.mount(Arrays.asList(
            new PlainNode<>("400011", "400010", true, "nid400011"),
            new PlainNode<>("400012", "400010", false, "nid400012")
        ));
        list.add(subtree); // add a tree node

        list.add(new PlainNode<>("500000", null, false, "nid500000"));
        list.add(new PlainNode<>("500010", "500000", true, "nid500010"));
        list.add(new PlainNode<>("500011", "500010", true, "nid500011"));

        // do mount third
        SiblingNodesComparator.comparing(e -> e.getChildren().size(), false, true).thenComparing(TreeNode::getNid, false, true).get();

        TreeNode<String, String> root = TreeNodeBuilder.<String, String>newBuilder(
            TreeNode.DEFAULT_ROOT_ID, 
            SiblingNodesComparator.comparing(e -> e.getChildren().size(),false, false).thenComparing(TreeNode::getNid, false, false).get()
        ).build();

        // do mount fourth
        root.mount(list); // mount
        System.out.println("sorted: " + Jsons.toJson(root));
        System.out.println("dfs: " + Jsons.toJson(root.flatDFS()));
        System.out.println("cfs: " + Jsons.toJson(root.flatCFS()));
        System.out.println("convert-true: " + Jsons.toJson(root.convert(this::convert, true)));
        System.out.println("convert-false: " + Jsons.toJson(root.convert(this::convert, false)));
    }

    private MapTreeTrait<String, String> convert(TreeNode<String, String> node) {
        MapTreeTrait<String, String> map = new MapTreeTrait<>();
        map.put("nid", node.getNid());
        map.put("pid", node.getPid());
        map.put("attach", node.getAttach());
        map.put("path", node.getPath());
        map.put("enabled", node.isEnabled());
        map.put("available", node.isAvailable());
        return map;
    }

    @Test
    public void test2() {
        List<BaseNode<String, String>> list = new ArrayList<>();
        list.add(new PlainNode<>("a", "b", true, "")); // 节点循环依赖
        list.add(new PlainNode<>("b", "a", true, ""));

        TreeNode<String, String> root = TreeNodeBuilder.<String, String> newBuilder(TreeNode.DEFAULT_ROOT_ID).build();
        Assert.assertThrows(RuntimeException.class, ()->root.mount(list)) ;
        System.out.println(Jsons.toJson(root));
    }

    @Test
    public void test3() {
        List<BaseNode<String, String>> list = new ArrayList<>();
        list.add(new PlainNode<>("100001", null, true, "nid100010"));
        Assert.assertThrows(IllegalArgumentException.class, () -> list.add(new PlainNode<>(null, "100001", true, "nid100010")));// 节点编号不能为空
        TreeNode<String, String> root = TreeNodeBuilder.<String, String> newBuilder(TreeNode.DEFAULT_ROOT_ID).build();
        root.mount(list);
        System.out.println(Jsons.toJson(root));
    }

    @Test
    public void test4() {
        List<BaseNode<String, String>> list = new ArrayList<>();
        list.add(new PlainNode<>("100000", "notfound", true, "nid100000")); // 无效的孤儿节点
        list.add(new PlainNode<>("200000", "notfound", true, "nid200000")); // 无效的孤儿节点

        Assert.assertThrows(RuntimeException.class, () -> TreeNodeBuilder.<String, String>newBuilder(TreeNode.DEFAULT_ROOT_ID).build().mount(list));
    }

    @Test
    public void test5() {
        List<BaseNode<String, String>> list = new ArrayList<>();
        list.add(new PlainNode<>("100000", null, true, null));
        list.add(new PlainNode<>("100010", "100000", true, null));
        list.add(new PlainNode<>("100011", "100010", false, random()));
        list.add(new PlainNode<>("100012", "100010", true, random()));
        list.add(new PlainNode<>("100020", "100000", false, random()));
        list.add(new PlainNode<>("100021", "100020", true, random()));
        list.add(new PlainNode<>("100022", "100020", false, random()));

        list.add(new PlainNode<>("200000", null, true, random()));

        list.add(new PlainNode<>("300000", null, true, random()));

        list.add(new PlainNode<>("400000", null, true, random()));

        // do mount first
        TreeNode<String, String> subtree = TreeNodeBuilder.<String, String> newBuilder("400010").pid("400000").build();

        // do mount second
        subtree.mount(Arrays.asList(
            new PlainNode<>("400011", "400010", true, "nid400011"),
            new PlainNode<>("400012", "400010", false, "nid400012")
        ));
        list.add(subtree); // add a tree node

        list.add(new PlainNode<>("500000", null, false, random()));
        list.add(new PlainNode<>("500010", "500000", true, random()));
        list.add(new PlainNode<>("500011", "500010", true, random()));

        // do mount third
        TreeNode<String, String> root = TreeNodeBuilder.<String, String>newBuilder(TreeNode.DEFAULT_ROOT_ID).build();
        System.out.println(Jsons.toJson(root));

        // do mount fouth
        root.mount(list); // mount
        System.out.println(Jsons.toJson(root));
        System.out.println(Jsons.toJson(root.flatDFS()));
        System.out.println(Jsons.toJson(root.flatCFS()));
    }
    
    private String random() {
        String[] s = new String[] { "a", null, "b", null, "c", "d", null };
        return s[ThreadLocalRandom.current().nextInt(s.length)];
    }
}
