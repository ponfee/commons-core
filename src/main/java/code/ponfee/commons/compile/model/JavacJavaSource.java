package code.ponfee.commons.compile.model;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STRICTFP;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;

/**
 * <pre>
 *   <dependency>
 *     <groupId>com.sun</groupId>
 *     <artifactId>tools</artifactId>
 *     <version>${java.version}</version>
 *     <scope>system</scope>
 *     <systemPath>${JAVA_HOME}/lib/tools.jar</systemPath>
 *   </dependency>
 * </pre>
 * 
 * 基于jdk tools jar的语法/词法分析
 * 
 * @author fupf
 */
public class JavacJavaSource extends JavaSource {
    private static final long serialVersionUID = 8020419352084840057L;

    public JavacJavaSource(String sourceCode) {
        super(sourceCode);

        Context context = new Context();
        JavacFileManager.preRegister(context);
        Parser parser = ParserFactory.instance(context).newParser(sourceCode, true, false, true);
        JCCompilationUnit unit = parser.parseCompilationUnit();

        super.packageName = unit.getPackageName().toString();
        new SourceVisitor().visitCompilationUnit(unit, this);
        if (super.publicClass == null || super.publicClass.isEmpty()) {
            throw new IllegalArgumentException("illegal source code, public class not found.");
        }
    }

    /**
     * 最外围类（class TD）总在最后
     * 
     * <pre>
     * public final class TD {
     *     public void say() {}
     * 
     *     public class TA {
     *         public String hello() {
     *             return "hello";
     *         }
     *     }
     * 
     *     public class TB {
     *         public String hello() {
     *             return "hello";
     *         }
     *     }
     * 
     *     public class TV {
     *     }
     * 
     *     public class TF {
     *         public String hello() {
     *             return "hello";
     *         }
     *     }
     * 
     *     public class TC {
     *     }
     * }
     * 
     * class D {
     * }
     * </pre>
     * 源码访问类
     */
    private static class SourceVisitor extends TreeScanner<Void, JavaSource> {
        private static final List<Modifier> MODIFIERS = Arrays.asList(PUBLIC, FINAL, ABSTRACT, STRICTFP);

        @Override
        public Void visitClass(ClassTree classtree, JavaSource source) {
            super.visitClass(classtree, source);
            Set<Modifier> modifiers = classtree.getModifiers().getFlags();
            if (modifiers.contains(PUBLIC) && modifiers.size() <= 3
                && MODIFIERS.containsAll(modifiers)) {
                source.publicClass = classtree.getSimpleName().toString();
            }
            return null;
        }
    }

}
