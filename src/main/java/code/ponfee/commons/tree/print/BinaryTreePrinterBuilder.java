package code.ponfee.commons.tree.print;

import java.util.function.Function;

/**
 * Binary tree printer builder
 *
 * @author Ponfee
 */
public class BinaryTreePrinterBuilder<T> {

    private final Appendable output;
    private final Function<T, String> labelMapper;
    private final Function<T, T> leftMapper;
    private final Function<T, T> rightMapper;

    private boolean squareBranch = true;
    private boolean unrecognizedLR = false;
    private int nodesSpace = 5;
    private int treesSpace = 8;

    public BinaryTreePrinterBuilder(Function<T, String> labelMapper,
                                    Function<T, T> leftMapper, Function<T, T> rightMapper) {
        this(System.out, labelMapper, leftMapper, rightMapper);
    }

    public BinaryTreePrinterBuilder(Appendable output, Function<T, String> labelMapper,
                                    Function<T, T> leftMapper, Function<T, T> rightMapper) {
        this.output = output;
        this.labelMapper = labelMapper;
        this.leftMapper = leftMapper;
        this.rightMapper = rightMapper;
    }

    public BinaryTreePrinterBuilder<T> squareBranch(boolean squareBranch) {
        this.squareBranch = squareBranch;
        return this;
    }

    public BinaryTreePrinterBuilder<T> unrecognizedLR(boolean unrecognizedLR) {
        this.unrecognizedLR = unrecognizedLR;
        return this;
    }

    public BinaryTreePrinterBuilder<T> nodesSpace(int nodesSpace) {
        this.nodesSpace = nodesSpace;
        return this;
    }

    public BinaryTreePrinterBuilder<T> treesSpace(int treesSpace) {
        this.treesSpace = treesSpace;
        return this;
    }

    public BinaryTreePrinter<T> build() {
        return new BinaryTreePrinter<>(
            output, labelMapper, leftMapper, rightMapper,
            squareBranch, unrecognizedLR, nodesSpace, treesSpace
        );
    }

}
