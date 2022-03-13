package code.ponfee.commons.tree.print;

import code.ponfee.commons.io.Files;
import com.google.common.base.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Print binary tree
 *
 * @author Ponfee
 */
public class BinaryTreePrinter<T> {

    private final Appendable output;
    private final Function<T, String> labelMapper;
    private final Function<T, T> leftMapper;
    private final Function<T, T> rightMapper;
    private final boolean squareBranch;
    private final boolean unrecognizedLR;
    private final int nodesSpace;
    private final int treesSpace;

    BinaryTreePrinter(Appendable output, Function<T, String> labelMapper,
                      Function<T, T> leftMapper, Function<T, T> rightMapper,
                      boolean squareBranch, boolean unrecognizedLR,
                      int nodesSpace, int treesSpace) {
        this.output = output;
        this.labelMapper = labelMapper;
        this.leftMapper = leftMapper;
        this.rightMapper = rightMapper;
        this.squareBranch = squareBranch;
        this.unrecognizedLR = unrecognizedLR;
        this.nodesSpace = nodesSpace;
        this.treesSpace = treesSpace;
    }

    /**
     * Prints ascii representation of binary tree.
     * Parameter nodesSpace is minimum number of spaces between adjacent node labels.
     * Parameter squareBranches, when set to true, results in branches being printed with ASCII box
     * drawing characters.
     */
    public void print(T root) throws IOException {
        printTreeLines(buildTreeLines(root));
    }

    /**
     * Prints ascii representations of multiple trees across page.
     * Parameter nodesSpace is minimum number of spaces between adjacent node labels in a tree.
     * Parameter treesSpace is horizontal distance between trees, as well as number of blank lines
     * between rows of trees.
     * Parameter lineWidth is maximum width of output
     * Parameter squareBranches, when set to true, results in branches being printed with ASCII box
     * drawing characters.
     */
    public void print(List<T> trees, int lineWidth) throws IOException {
        List<List<TreeLine>> allTreeLines = new ArrayList<>(trees.size());
        int[] treeWidths = new int[trees.size()];
        int[] minLeftOffsets = new int[trees.size()];
        int[] maxRightOffsets = new int[trees.size()];
        for (int i = 0; i < trees.size(); i++) {
            List<TreeLine> treeLines = buildTreeLines(trees.get(i));
            allTreeLines.add(treeLines);
            minLeftOffsets[i] = minLeftOffset(treeLines);
            maxRightOffsets[i] = maxRightOffset(treeLines);
            treeWidths[i] = maxRightOffsets[i] - minLeftOffsets[i] + 1;
        }

        int nextTreeIndex = 0;
        while (nextTreeIndex < trees.size()) {
            // print a row of trees starting at nextTreeIndex
            // first figure range of trees we can print for next row
            int sumOfWidths = treeWidths[nextTreeIndex];
            int endTreeIndex = nextTreeIndex + 1;
            while (endTreeIndex < trees.size() && sumOfWidths + treesSpace + treeWidths[endTreeIndex] < lineWidth) {
                sumOfWidths += (treesSpace + treeWidths[endTreeIndex]);
                endTreeIndex++;
            }
            endTreeIndex--;

            // find max number of lines for tallest tree
            int maxLines = allTreeLines.stream().mapToInt(List::size).max().orElse(0);

            // print trees line by line
            for (int i = 0; i < maxLines; i++) {
                for (int j = nextTreeIndex; j <= endTreeIndex; j++) {
                    List<TreeLine> treeLines = allTreeLines.get(j);
                    if (i >= treeLines.size()) {
                        output.append(spaces(treeWidths[j]));
                    } else {
                        int leftSpaces = -(minLeftOffsets[j] - treeLines.get(i).leftOffset);
                        int rightSpaces = maxRightOffsets[j] - treeLines.get(i).rightOffset;
                        output.append(spaces(leftSpaces)).append(treeLines.get(i).line).append(spaces(rightSpaces));
                    }
                    if (j < endTreeIndex) {
                        output.append(spaces(treesSpace));
                    }
                }
                output.append(Files.UNIX_LINE_SEPARATOR);
            }

            nextTreeIndex = endTreeIndex + 1;
        }
    }

    private void printTreeLines(List<TreeLine> treeLines) throws IOException {
        if (treeLines.size() <= 0) {
            return;
        }
        int minLeftOffset = minLeftOffset(treeLines);
        int maxRightOffset = maxRightOffset(treeLines);
        for (TreeLine treeLine : treeLines) {
            output.append(spaces(-(minLeftOffset - treeLine.leftOffset)))
                  .append(treeLine.line)
                  .append(spaces(maxRightOffset - treeLine.rightOffset))
                  .append(Files.UNIX_LINE_SEPARATOR);
        }
    }

    private List<TreeLine> buildTreeLines(T root) {
        if (root == null) {
            return Collections.emptyList();
        }

        String rootLabel = labelMapper.apply(root);
        List<TreeLine> leftTreeLines = buildTreeLines(leftMapper.apply(root));
        List<TreeLine> rightTreeLines = buildTreeLines(rightMapper.apply(root));

        int leftCount = leftTreeLines.size();
        int rightCount = rightTreeLines.size();
        int minCount = Math.min(leftCount, rightCount);
        int maxCount = Math.max(leftCount, rightCount);

        // The left and right subtree print representations have jagged edges, and we essentially we have to
        // figure out how close together we can bring the left and right roots so that the edges just meet on
        // some line.  Then we add hspace, and round up to next odd number.
        int maxRootSpacing = 0;
        for (int i = 0; i < minCount; i++) {
            int spacing = leftTreeLines.get(i).rightOffset - rightTreeLines.get(i).leftOffset;
            if (spacing > maxRootSpacing) {
                maxRootSpacing = spacing;
            }
        }
        int rootSpacing = maxRootSpacing + nodesSpace;
        if ((rootSpacing & 0x01) == 0) {
            rootSpacing++;
        }
        // rootSpacing is now the number of spaces between the roots of the two subtrees

        List<TreeLine> allTreeLines = new ArrayList<>();

        // strip ANSI escape codes to get length of rendered string. Fixes wrong padding when labels use ANSI escapes for colored nodes.
        String renderedRootLabel = rootLabel.replaceAll("\\e\\[[\\d;]*[^\\d;]", "");

        // add the root and the two branches leading to the subtrees
        allTreeLines.add(new TreeLine(rootLabel, -(renderedRootLabel.length() - 1) / 2, renderedRootLabel.length() / 2));

        // also calculate offset adjustments for left and right subtrees
        int leftTreeAdjust = 0;
        int rightTreeAdjust = 0;

        if (leftTreeLines.isEmpty()) {
            if (!rightTreeLines.isEmpty()) {
                // there's a right subtree only
                if (squareBranch) {
                    if (unrecognizedLR) {
                        allTreeLines.add(new TreeLine("\u2502", 0, 0));
                    } else {
                        allTreeLines.add(new TreeLine("\u2514\u2510", 0, 1));
                        rightTreeAdjust = 1;
                    }
                } else {
                    allTreeLines.add(new TreeLine("\\", 1, 1));
                    rightTreeAdjust = 2;
                }
            }
        } else if (rightTreeLines.isEmpty()) {
            // there's a left subtree only
            if (squareBranch) {
                if (unrecognizedLR) {
                    allTreeLines.add(new TreeLine("\u2502", 0, 0));
                } else {
                    allTreeLines.add(new TreeLine("\u250C\u2518", -1, 0));
                    leftTreeAdjust = -1;
                }
            } else {
                allTreeLines.add(new TreeLine("/", -1, -1));
                leftTreeAdjust = -2;
            }
        } else {
            // there's a left and right subtree
            if (squareBranch) {
                int adjust = (rootSpacing / 2) + 1;
                String horizontal = String.join("", Collections.nCopies(rootSpacing / 2, "\u2500"));
                String branch = "\u250C" + horizontal + "\u2534" + horizontal + "\u2510";
                allTreeLines.add(new TreeLine(branch, -adjust, adjust));
                rightTreeAdjust = adjust;
                leftTreeAdjust = -adjust;
            } else {
                if (rootSpacing == 1) {
                    allTreeLines.add(new TreeLine("/ \\", -1, 1));
                    rightTreeAdjust = 2;
                    leftTreeAdjust = -2;
                } else {
                    for (int i = 1; i < rootSpacing; i += 2) {
                        String branches = "/" + spaces(i) + "\\";
                        allTreeLines.add(new TreeLine(branches, -((i + 1) / 2), (i + 1) / 2));
                    }
                    rightTreeAdjust = (rootSpacing / 2) + 1;
                    leftTreeAdjust = -((rootSpacing / 2) + 1);
                }
            }
        }

        // now add joined lines of subtrees, with appropriate number of separating spaces, and adjusting offsets
        for (int i = 0; i < maxCount; i++) {
            TreeLine left, right;
            if (i >= leftTreeLines.size()) {
                // nothing remaining on left subtree
                right = rightTreeLines.get(i);
                right.leftOffset += rightTreeAdjust;
                right.rightOffset += rightTreeAdjust;
                allTreeLines.add(right);
            } else if (i >= rightTreeLines.size()) {
                // nothing remaining on right subtree
                left = leftTreeLines.get(i);
                left.leftOffset += leftTreeAdjust;
                left.rightOffset += leftTreeAdjust;
                allTreeLines.add(left);
            } else {
                left = leftTreeLines.get(i);
                right = rightTreeLines.get(i);
                int adjustedRootSpacing = (rootSpacing == 1 ? (squareBranch ? 1 : 3) : rootSpacing);
                TreeLine combined = new TreeLine(
                    left.line + spaces(adjustedRootSpacing - left.rightOffset + right.leftOffset) + right.line,
                    left.leftOffset + leftTreeAdjust,
                    right.rightOffset + rightTreeAdjust
                );
                allTreeLines.add(combined);
            }
        }
        return allTreeLines;
    }

    private static int minLeftOffset(List<TreeLine> treeLines) {
        return treeLines.stream().mapToInt(e -> e.leftOffset).min().orElse(0);
    }

    private static int maxRightOffset(List<TreeLine> treeLines) {
        return treeLines.stream().mapToInt(e -> e.rightOffset).max().orElse(0);
    }

    private static String spaces(int n) {
        return Strings.repeat(" ", n);
    }

    private static class TreeLine {
        final String line;
        int leftOffset;
        int rightOffset;

        TreeLine(String line, int leftOffset, int rightOffset) {
            this.line = line;
            this.leftOffset = leftOffset;
            this.rightOffset = rightOffset;
        }
    }

}
