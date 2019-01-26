package code.ponfee.commons.compile.model;

import java.io.Serializable;

/**
 * java源代码类
 * @author fupf
 */
public abstract class JavaSource implements Serializable {

    private static final long serialVersionUID = 5643697448853377651L;

    protected final String sourceCode; // 源码
    protected String packageName; // 包名
    protected String publicClass; // public修饰的类名

    public JavaSource(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getPublicClass() {
        return publicClass;
    }

    /**
     * 获取类的全限定名
     * @return class canonical name of packageName + "." + className.
     *         the same to {@code Class#getCanonicalName()}
     * @see {@link Class#getCanonicalName()}
     */
    public String getFullyQualifiedName() {
        return (packageName == null || packageName.isEmpty())
               ? publicClass
               : packageName + "." + publicClass;
    }

}
