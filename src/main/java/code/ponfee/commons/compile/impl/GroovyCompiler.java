package code.ponfee.commons.compile.impl;

import code.ponfee.commons.compile.JavaSourceCompiler;
import code.ponfee.commons.compile.model.JavaSource;
import groovy.lang.GroovyClassLoader;

/**
 * Compile java source code by groovy
 * 
 * @author Ponfee
 */
public class GroovyCompiler implements JavaSourceCompiler {

    private static final GroovyClassLoader GROOVY_CLASS_LOADER = new GroovyClassLoader();

    @Override
    public Class<?> compile(String codeSource) {
        Class<?> clazz = GROOVY_CLASS_LOADER.parseClass(codeSource);
        if (clazz == null) {
            throw new RuntimeException("Invalid code source: " + codeSource);
        }
        return clazz;
    }

    @Override
    public Class<?> compile(JavaSource javaSource) {
        return compile(javaSource.getSourceCode());
    }

    @Override
    public Class<?> compileForce(JavaSource javaSource) {
        return compile(javaSource.getSourceCode());
    }

}
