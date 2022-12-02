/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.tree.print;

import code.ponfee.commons.base.tuple.Tuple4;
import code.ponfee.commons.collect.Collects;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

/**
 * Print multiway tree
 *
 * @author Ponfee
 */
public final class MultiwayTreePrinter<T> {

    private final Appendable output;
    private final Function<T, CharSequence> nodeLabel;
    private final Function<T, List<T>> nodeChildren;

    public MultiwayTreePrinter(Appendable output,
                               Function<T, CharSequence> nodeLabel,
                               Function<T, List<T>> nodeChildren) {
        this.output = output;
        this.nodeLabel = nodeLabel;
        this.nodeChildren = nodeChildren;
    }

    /*// DFS递归方式
    public void print(T root) throws IOException {
        print("", "", "", root);
    }

    private void print(String prefix, String middle, String suffix, T node) throws IOException {
        output.append(prefix).append(suffix).append(nodeLabel.apply(node)).append('\n');

        // print children
        List<T> children = nodeChildren.apply(node);
        if (children == null || children.isEmpty()) {
            return;
        }

        if (middle.length() > 0) {
            prefix += middle;
        }

        int index = children.size();
        for (T child : children) {
            if (--index > 0) {
                print(prefix, "│   ", "├── ", child);
            } else {
                // last child of parent, space: (char) 0xa0
                print(prefix, "    ", "└── ", child);
            }
        }
    }
    */

    public void print(T root) throws IOException {
        Deque<Tuple4<String, String, String, T>> stack = Collects.newLinkedList(Tuple4.of("", "", "", root));
        while (!stack.isEmpty()) {
            Tuple4<String, String, String, T> tuple = stack.pop();
            output.append(tuple.a).append(tuple.c).append(nodeLabel.apply(tuple.d)).append('\n');

            List<T> children = nodeChildren.apply(tuple.d);
            if (children != null && !children.isEmpty()) {
                String a = tuple.b.length() > 0 ? tuple.a + tuple.b : tuple.a;
                int index = 0;
                for (T child : Lists.reverse(children)) {
                    if (index++ == 0) {
                        // last child of parent, space: (char) 0xa0
                        stack.push(Tuple4.of(a, "    ", "└── ", child));
                    } else {
                        stack.push(Tuple4.of(a, "│   ", "├── ", child));
                    }
                }
            }
        }
    }

}
