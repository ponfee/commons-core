/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.graph;

import cn.ponfee.commons.base.tuple.Tuple2;
import cn.ponfee.commons.tree.TreeNode;
import cn.ponfee.commons.tree.print.MultiwayTreePrinter;
import com.google.common.graph.Graph;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isEqualCollection;

/**
 * DAGParserTest
 *
 * @author Ponfee
 */
public class DAGExpressionParserTest {

    private static final MultiwayTreePrinter<TreeNode<DAGExpressionParser.TreeNodeId, Object>> TREE_PRINTER =
        new MultiwayTreePrinter<>(System.out, e -> e.getNid().toString(), TreeNode::getChildren);

    @Test
    public void testSplit() {
        Assert.assertTrue(isEqualCollection(asList("->", "->", "->"), DAGExpressionParser.split("->->->", "->")));
        Assert.assertTrue(isEqualCollection(asList("a", "->", "->", "->"), DAGExpressionParser.split("a->->->", "->")));
        Assert.assertTrue(isEqualCollection(asList("->", "->", "->", "b"), DAGExpressionParser.split("->->->b", "->")));
        Assert.assertTrue(isEqualCollection(asList("a", "->", "b", "->", "c", "->", "d"), DAGExpressionParser.split("a->b->c->d", "->")));
        Assert.assertTrue(isEqualCollection(asList("abcd"), DAGExpressionParser.split("abcd", "->")));
    }

    @Test
    public void testProcess() {
        Assert.assertEquals("((A)->(((B)->(C)->(D)),((A)->(F)))->((G),(H),(X))->(J))", DAGExpressionParser.process("(A->((B->C->D),(A->F))->(G,H,X)->J)"));
        Assert.assertEquals("(A),(B)->((C)->(D)),(E)->(F)", DAGExpressionParser.process("A,B->(C->D),(E)->F"));
        Assert.assertEquals("(A),(B)->((C)->(D)),(E)->(F)", DAGExpressionParser.process("A,B->(C->D),E->F"));
    }

    @Test
    public void testPartition() {
        Assert.assertTrue(isEqualCollection(asList(Tuple2.of(0, 1), Tuple2.of(7, 1)), DAGExpressionParser.group("(A -> B)")));

        Assert.assertTrue(isEqualCollection(
            asList(Tuple2.of(0, 1), Tuple2.of(4, 2), Tuple2.of(12, 2), Tuple2.of(14, 2), Tuple2.of(19, 2), Tuple2.of(22, 2), Tuple2.of(26, 2), Tuple2.of(30, 1)),
            DAGExpressionParser.group("(A->(B->C->D),(E->F)->(G,H)->J)")
        ));
    }

    @Test
    public void testValidate() {
        Assert.assertTrue(DAGExpressionParser.checkParenthesis("(A->(B->C->D),(E->F)->(G,H)->J)"));
        Assert.assertTrue(DAGExpressionParser.checkParenthesis("afdsafd"));
        Assert.assertFalse(DAGExpressionParser.checkParenthesis("((A->(B->C->D),(E->F)->(G,H)->J)"));
        Assert.assertFalse(DAGExpressionParser.checkParenthesis(")A->(B->C->D),(E->F)->)G,H(->J("));
        Assert.assertFalse(DAGExpressionParser.checkParenthesis(")("));
        Assert.assertFalse(DAGExpressionParser.checkParenthesis("()("));
        Assert.assertFalse(DAGExpressionParser.checkParenthesis("())"));
        Assert.assertFalse(DAGExpressionParser.checkParenthesis("(()"));
        Assert.assertFalse(DAGExpressionParser.checkParenthesis(")()"));
    }

    @Test
    public void testBuildTree() throws IOException {
        List<Tuple2<Integer, Integer>> partitions = DAGExpressionParser.group("(A->(B->C->D),(A->F)->(G,H,X)->J)");
        TreeNode<DAGExpressionParser.TreeNodeId, Object> root = DAGExpressionParser.buildTree(partitions);
        Assert.assertEquals(root.getChildrenCount(), 3);
        System.out.println("------------------");
        TREE_PRINTER.print(root);

        partitions = DAGExpressionParser.group("((A->((B->C->D),(E->F))->(G,H)->J))");
        root = DAGExpressionParser.buildTree(partitions);
        Assert.assertEquals(root.getChildrenCount(), 1);
        System.out.println("\n------------------");
        TREE_PRINTER.print(root);

        partitions = DAGExpressionParser.group("(A->((B->C->D),(E->F))->(G,H)->J)");
        root = DAGExpressionParser.buildTree(partitions);
        Assert.assertEquals(root.getChildrenCount(), 2);
        System.out.println("\n------------------");
        TREE_PRINTER.print(root);
    }

    @Test
    public void testSameExpression() {
        String text = "(A->((B->C->D),(A->F))->(G,H,X)->J)";
        assertSameExpression(text, text);
        assertSameExpression("(A->((B->C->D),(A->F))->(G,H,X)->J) ", " A->((B->C->D),(A->F))->(G,H,X)->J");
        assertSameExpression("(A->(B->C->D),(A->F)->(G,H,X)->J)", "A->((B->C->D),(A->F))->(G,H,X)->J");
        assertSameExpression("(A->((B->C->D),(A->F))->G,H,X->J)", " A->((B->C->D),(A->F))->(G,H,X)->J");
        assertSameExpression("(A->(B->C->D),(A->F)->G,H,X->J)", "A->((B->C->D),(A->F))->(G,H,X)->J");
    }

    @Test
    public void testEdgesEquals() {
        assertEdgesEquals(
            "(A)->((B),(C))->(E),(F->G)->(H)",
            "[<0:0:HEAD -> 1:1:A>, <1:1:A -> 1:1:B>, <1:1:A -> 1:1:C>, <1:1:B -> 1:1:E>, <1:1:B -> 1:1:F>, <1:1:E -> 1:1:H>, <1:1:H -> 0:0:TAIL>, <1:1:F -> 1:1:G>, <1:1:G -> 1:1:H>, <1:1:C -> 1:1:E>, <1:1:C -> 1:1:F>]"
        );
        assertEdgesEquals(
            "(A->((B->C->D),(A->F))->(G,H,X)->J);(A->Y)",
            "[<0:0:HEAD -> 1:1:A>, <0:0:HEAD -> 2:3:A>, <1:1:A -> 1:1:B>, <1:1:A -> 1:2:A>, <1:1:B -> 1:1:C>, <1:1:C -> 1:1:D>, <1:1:D -> 1:1:G>, <1:1:D -> 1:1:H>, <1:1:D -> 1:1:X>, <1:1:G -> 1:1:J>, <1:1:J -> 0:0:TAIL>, <1:1:H -> 1:1:J>, <1:1:X -> 1:1:J>, <1:2:A -> 1:1:F>, <1:1:F -> 1:1:G>, <1:1:F -> 1:1:H>, <1:1:F -> 1:1:X>, <2:3:A -> 2:1:Y>, <2:1:Y -> 0:0:TAIL>]"
        );
        assertEdgesEquals(
            "(A,B)->(C->D),(A->E),(B->F)->G",
            "[<0:0:HEAD -> 1:1:A>, <0:0:HEAD -> 1:2:B>, <1:1:A -> 1:1:C>, <1:1:A -> 1:2:A>, <1:1:A -> 1:1:B>, <1:1:C -> 1:1:D>, <1:1:D -> 1:1:G>, <1:1:G -> 0:0:TAIL>, <1:2:A -> 1:1:E>, <1:1:E -> 1:1:G>, <1:1:B -> 1:1:F>, <1:1:F -> 1:1:G>, <1:2:B -> 1:1:C>, <1:2:B -> 1:2:A>, <1:2:B -> 1:1:B>]"
        );
        assertEdgesEquals(
            "A,B->C,D,C",
            "[<0:0:HEAD -> 1:1:A>, <0:0:HEAD -> 1:1:B>, <1:1:A -> 1:1:C>, <1:1:A -> 1:1:D>, <1:1:A -> 1:2:C>, <1:1:C -> 0:0:TAIL>, <1:1:D -> 0:0:TAIL>, <1:2:C -> 0:0:TAIL>, <1:1:B -> 1:1:C>, <1:1:B -> 1:1:D>, <1:1:B -> 1:2:C>]"
        );
        assertEdgesEquals(
            "A,B->(C->D),E->F",
            "[<0:0:HEAD -> 1:1:A>, <0:0:HEAD -> 1:1:B>, <1:1:A -> 1:1:C>, <1:1:A -> 1:1:E>, <1:1:C -> 1:1:D>, <1:1:D -> 1:1:F>, <1:1:F -> 0:0:TAIL>, <1:1:E -> 1:1:F>, <1:1:B -> 1:1:C>, <1:1:B -> 1:1:E>]"
        );
        assertEdgesEquals(
            "A,B->(C->D),(E)->F",
            "[<0:0:HEAD -> 1:1:A>, <0:0:HEAD -> 1:1:B>, <1:1:A -> 1:1:C>, <1:1:A -> 1:1:E>, <1:1:C -> 1:1:D>, <1:1:D -> 1:1:F>, <1:1:F -> 0:0:TAIL>, <1:1:E -> 1:1:F>, <1:1:B -> 1:1:C>, <1:1:B -> 1:1:E>]"
        );
        assertEdgesEquals(
            "A->B;A->B",
            "[<0:0:HEAD -> 1:1:A>, <0:0:HEAD -> 2:2:A>, <1:1:A -> 1:1:B>, <1:1:B -> 0:0:TAIL>, <2:2:A -> 2:2:B>, <2:2:B -> 0:0:TAIL>]"
        );
    }

    @Test
    public void testGraph() {
        String expression = "(A->((B->C->D),(A->F))->(G,H,X)->J);(A->Y)";
        Graph<GraphNodeId> graph = new DAGExpressionParser(expression).parse();
        Assert.assertEquals("[1:1:A, 2:3:A]", graph.successors(GraphNodeId.HEAD).toString());
        Assert.assertTrue(graph.predecessors(GraphNodeId.HEAD).isEmpty());

        Assert.assertEquals("[1:1:J, 2:1:Y]", graph.predecessors(GraphNodeId.TAIL).toString());
        Assert.assertTrue(graph.successors(GraphNodeId.TAIL).isEmpty());

        Assert.assertEquals("[1:1:B, 1:2:A]", graph.successors(GraphNodeId.of(1, 1, "A")).toString());

        //graph.adjacentNodes();
        //graph.incidentEdges();
    }

    // ------------------------------------------------------------------------

    private static void assertSameExpression(String text1, String text2) {
        System.out.println("\n\n------\n\n");
        Assert.assertEquals(new DAGExpressionParser(text1).parse(), new DAGExpressionParser(text2).parse());
    }

    private static void assertEdgesEquals(String expression, String edges) {
        System.out.println("\n\n------\n\n");
        System.out.println(expression);
        Graph<GraphNodeId> graph = new DAGExpressionParser(expression).parse();
        Assert.assertEquals(edges, graph.edges().toString());
        System.out.println(expression + " graph result: " + graph);
    }

}
