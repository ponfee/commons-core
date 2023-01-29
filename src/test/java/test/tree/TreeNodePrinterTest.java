package test.tree;

import cn.ponfee.commons.collect.Collects;
import cn.ponfee.commons.export.Thead;
import cn.ponfee.commons.io.Files;
import cn.ponfee.commons.json.Jsons;
import cn.ponfee.commons.tree.*;
import cn.ponfee.commons.tree.print.BinaryTreePrinter;
import cn.ponfee.commons.tree.print.BinaryTreePrinterBuilder;
import cn.ponfee.commons.util.MavenProjects;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 
 * @author Ponfee
 */
public class TreeNodePrinterTest {

    @Test
    public void test1() throws IOException {
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
        TreeNode<String, String> root = TreeNodeBuilder.<String, String> newBuilder(TreeNode.DEFAULT_ROOT_ID).build();
        System.out.println("unmount: "+Jsons.toJson(root));

        // do mount fouth
        root.mount(list); // mount
        System.out.println("mounted: "+Jsons.toJson(root));
        System.out.println("dfs: "+Jsons.toJson(root.flatDFS()));
        System.out.println("cfs: "+Jsons.toJson(root.flatCFS()));
        System.out.println("bfs: "+Jsons.toJson(root.flatBFS()));
        System.out.println("convert-true: "  + Jsons.toJson(root.convert(this::convert, true)));
        System.out.println("convert-false: " + Jsons.toJson(root.convert(this::convert, false)));

        System.out.println("\n\n-----------------\n\n");
        System.out.println(root.print(TreeNode::getNid));
    }

    @Test
    public void test2() throws IOException {
        List<BaseNode<Integer, Thead>> list = new ArrayList<>();

        list.add(new PlainNode<>(1, 0, new Thead("区域")));
        list.add(new PlainNode<>(2, 0, new Thead("分公司")));

        list.add(new PlainNode<>(3, 0, new Thead("昨天")));
        list.add(new PlainNode<>(4, 3, new Thead("项目数")));
        list.add(new PlainNode<>(5, 3, new Thead("项目应收(元)")));
        list.add(new PlainNode<>(6, 3, new Thead("成交套数")));
        list.add(new PlainNode<>(7, 3, new Thead("套均收入(元)")));
        list.add(new PlainNode<>(8, 3, new Thead("团购项目数")));
        list.add(new PlainNode<>(9, 3, new Thead("导客项目数")));
        list.add(new PlainNode<>(10, 3, new Thead("代收项目数")));
        list.add(new PlainNode<>(11, 3, new Thead("线上项目数")));
        list.add(new PlainNode<>(12, 0, new Thead("本月")));
        list.add(new PlainNode<>(13, 12,new Thead("应收(万)")));
        list.add(new PlainNode<>(14, 12,new Thead("实收(万)")));
        list.add(new PlainNode<>(15, 12,new Thead("成交套数")));
        list.add(new PlainNode<>(16, 12,new Thead("套均收入(元)")));
        list.add(new PlainNode<>(17, 12,new Thead("团购项目应收(万)")));
        list.add(new PlainNode<>(18, 12,new Thead("团购项目成交套数")));
        list.add(new PlainNode<>(19, 12,new Thead("团购项目经服成交套数")));
        list.add(new PlainNode<>(20, 12,new Thead("团购项目套均收入(元)")));
        list.add(new PlainNode<>(21, 12,new Thead("团购项目经服成交应收(万)")));
        list.add(new PlainNode<>(22, 12,new Thead("团购项目中介应付外佣(万)")));
        list.add(new PlainNode<>(23, 12,new Thead("团购项目经服成交套数占比")));
        list.add(new PlainNode<>(24, 12,new Thead("团购项目中介分佣比例")));
        list.add(new PlainNode<>(25, 12,new Thead("导客项目应收(万)")));
        list.add(new PlainNode<>(26, 12,new Thead("导客项目成交套数")));
        list.add(new PlainNode<>(27, 12,new Thead("导客项目套均收入(元)")));
        list.add(new PlainNode<>(28, 12,new Thead("导客项目中介应付外佣(万)")));
        list.add(new PlainNode<>(29, 12,new Thead("导客项目中介分佣比例")));
        list.add(new PlainNode<>(30, 12,new Thead("代收项目应收(万)")));
        list.add(new PlainNode<>(31, 12,new Thead("代收项目成交套数")));
        list.add(new PlainNode<>(32, 12,new Thead("线上项目应收(万)")));
        list.add(new PlainNode<>(33, 12,new Thead("线上项目成交套数")));
        list.add(new PlainNode<>(34, 12,new Thead("月指标(万)")));
        list.add(new PlainNode<>(35, 12,new Thead("指标完成率")));

        System.out.println(TreeNodeBuilder.<Integer, Thead>newBuilder(0).build().mount(list).print(e -> Optional.ofNullable(e.getAttach()).map(Thead::getName).orElse(null)));
    }

    @Test
    public void test3() throws IOException {
        System.out.println("\n\n\n");
        System.out.println(
            Files.listFiles(MavenProjects.getProjectBaseDir())
                 .print(e -> e.getSiblingOrdinal() + ":" + e.getChildrenCount() + ":" + e.getAttach().getName())
        );
    }

    @Test
    public void test4() throws IOException {
        System.out.println("\n\n\n");
        TreeNode<Integer, File> files = Files.listFiles(MavenProjects.getProjectBaseDir());
        BinaryTreePrinter printer = new BinaryTreePrinterBuilder<TreeNode<Integer, File>>(
            e -> e.getAttach().getName(),
            e -> CollectionUtils.isEmpty(e.getChildren()) ? null : Collects.getFirst(e.getChildren()),
            e -> CollectionUtils.isEmpty(e.getChildren()) ? null :Collects.getLast(e.getChildren())
        )
        //.branch(BinaryTreePrinter.Branch.TRIANGLE)
        .directed(false)
        .build();

        printer.print(files.getChildren(), 1000);
        //printer.print(files.getChildren().get(5));
    }

    @Test
    public void test5() throws IOException {
        System.out.println(Files.tree(MavenProjects.getMainJavaPath("")));
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

}
