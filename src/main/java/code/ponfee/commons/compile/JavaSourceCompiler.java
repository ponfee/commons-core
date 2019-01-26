package code.ponfee.commons.compile;

import code.ponfee.commons.compile.model.JavaSource;

/**
 * java源代码动态编译
 * @author fupf
 */
public interface JavaSourceCompiler {

    Class<?> compile(String sourceString);

    Class<?> compile(JavaSource javaSource);

    Class<?> compileForce(JavaSource javaSource);
}
