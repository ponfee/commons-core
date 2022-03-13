/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.tree.print;

import code.ponfee.commons.io.Files;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

/**
 * Print tree node
 *
 * @author Ponfee
 */
public final class MultiwayTreePrinter<T> {

    private final Appendable output;
    private final Function<T, Collection<T>> childrenMapper;
    private final Function<T, CharSequence> labelMapper;

    public MultiwayTreePrinter(Appendable output,
                               Function<T, Collection<T>> childrenMapper,
                               Function<T, CharSequence> labelMapper) {
        this.output = output;
        this.childrenMapper = childrenMapper;
        this.labelMapper = labelMapper;
    }

    public void print(T root) throws IOException {
        print(root, "", false);
    }

    private void print(T node, String indent, boolean isLastChild) throws IOException {
        output.append(indent)
              .append(labelMapper.apply(node))
              .append(Files.UNIX_LINE_SEPARATOR);

        // print children
        Collection<T> children = childrenMapper.apply(node);
        if (CollectionUtils.isEmpty(children)) {
            return;
        }

        if (indent.length() > 0) {
            // (char) 0xa0
            indent = indent.substring(0, indent.length() - 4) + (isLastChild ? "    " : "│   ");
        }

        int count = children.size();
        String nonLastIndent = null;
        for (T child : children) {
            if (--count == 0) {
                // last child of parent
                print(child, indent + "└── ", true);
            } else {
                if (nonLastIndent == null) {
                    nonLastIndent = indent + "├── ";
                }
                print(child, nonLastIndent, false);
            }
        }
    }

}
