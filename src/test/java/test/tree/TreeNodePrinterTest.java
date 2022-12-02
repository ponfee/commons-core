/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package test.tree;

import code.ponfee.commons.collect.Collects;
import code.ponfee.commons.export.Thead;
import code.ponfee.commons.io.Files;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.tree.BaseNode;
import code.ponfee.commons.tree.MapTreeTrait;
import code.ponfee.commons.tree.TreeNode;
import code.ponfee.commons.tree.TreeNodeBuilder;
import code.ponfee.commons.tree.print.BinaryTreePrinter;
import code.ponfee.commons.tree.print.BinaryTreePrinterBuilder;
import code.ponfee.commons.util.MavenProjects;
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

        // do mount first
        TreeNode<String, String> subtree = TreeNodeBuilder.<String, String> newBuilder(
            "400010", Comparator.comparing(node -> ThreadLocalRandom.current().nextInt(10))
        ).pid("400000").enabled(true).build();

        // do mount second
        subtree.mount(Arrays.asList(
            new BaseNode<>("400011", "400010", true, "nid400011"),
            new BaseNode<>("400012", "400010", false, "nid400012")
        ));
        list.add(subtree); // add a tree node

        list.add(new BaseNode<>("500000", null, false, "nid500000"));
        list.add(new BaseNode<>("500010", "500000", true, "nid500010"));
        list.add(new BaseNode<>("500011", "500010", true, "nid500011"));

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

        list.add(new BaseNode<>(1, 0, new Thead("区域")));
        list.add(new BaseNode<>(2, 0, new Thead("分公司")));

        list.add(new BaseNode<>(3, 0, new Thead("昨天")));
        list.add(new BaseNode<>(4, 3, new Thead("项目数")));
        list.add(new BaseNode<>(5, 3, new Thead("项目应收(元)")));
        list.add(new BaseNode<>(6, 3, new Thead("成交套数")));
        list.add(new BaseNode<>(7, 3, new Thead("套均收入(元)")));
        list.add(new BaseNode<>(8, 3, new Thead("团购项目数")));
        list.add(new BaseNode<>(9, 3, new Thead("导客项目数")));
        list.add(new BaseNode<>(10, 3, new Thead("代收项目数")));
        list.add(new BaseNode<>(11, 3, new Thead("线上项目数")));
        list.add(new BaseNode<>(12, 0, new Thead("本月")));
        list.add(new BaseNode<>(13, 12,new Thead("应收(万)")));
        list.add(new BaseNode<>(14, 12,new Thead("实收(万)")));
        list.add(new BaseNode<>(15, 12,new Thead("成交套数")));
        list.add(new BaseNode<>(16, 12,new Thead("套均收入(元)")));
        list.add(new BaseNode<>(17, 12,new Thead("团购项目应收(万)")));
        list.add(new BaseNode<>(18, 12,new Thead("团购项目成交套数")));
        list.add(new BaseNode<>(19, 12,new Thead("团购项目经服成交套数")));
        list.add(new BaseNode<>(20, 12,new Thead("团购项目套均收入(元)")));
        list.add(new BaseNode<>(21, 12,new Thead("团购项目经服成交应收(万)")));
        list.add(new BaseNode<>(22, 12,new Thead("团购项目中介应付外佣(万)")));
        list.add(new BaseNode<>(23, 12,new Thead("团购项目经服成交套数占比")));
        list.add(new BaseNode<>(24, 12,new Thead("团购项目中介分佣比例")));
        list.add(new BaseNode<>(25, 12,new Thead("导客项目应收(万)")));
        list.add(new BaseNode<>(26, 12,new Thead("导客项目成交套数")));
        list.add(new BaseNode<>(27, 12,new Thead("导客项目套均收入(元)")));
        list.add(new BaseNode<>(28, 12,new Thead("导客项目中介应付外佣(万)")));
        list.add(new BaseNode<>(29, 12,new Thead("导客项目中介分佣比例")));
        list.add(new BaseNode<>(30, 12,new Thead("代收项目应收(万)")));
        list.add(new BaseNode<>(31, 12,new Thead("代收项目成交套数")));
        list.add(new BaseNode<>(32, 12,new Thead("线上项目应收(万)")));
        list.add(new BaseNode<>(33, 12,new Thead("线上项目成交套数")));
        list.add(new BaseNode<>(34, 12,new Thead("月指标(万)")));
        list.add(new BaseNode<>(35, 12,new Thead("指标完成率")));

        System.out.println(TreeNodeBuilder.<Integer, Thead>newBuilder(0).build().mount(list).print(e -> Optional.ofNullable(e.getAttach()).map(Thead::getName).orElse(null)));
    }

    @Test
    public void test3() throws IOException {
        System.out.println("\n\n\n");
        System.out.println(
            Files.listFiles(MavenProjects.getProjectBaseDir())
                 .print(e -> e.getSiblingOrder() + ":" + e.getChildrenCount() + ":" + e.getAttach().getName())
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
