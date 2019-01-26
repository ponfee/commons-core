package code.ponfee.commons.compile.exception;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

/**
 * 编译异常
 * @author fupf
 */
public class JdkCompileException extends Exception {

    private static final long serialVersionUID = 1L;

    private Set<String> classNames;
    private transient DiagnosticCollector<JavaFileObject> diagnostics;

    private String source;

    public JdkCompileException(String message, Set<String> qualifiedClassNames, Throwable cause,
                               DiagnosticCollector<JavaFileObject> diagnostics) {
        super(message, cause);
        setClassNames(qualifiedClassNames);
        setDiagnostics(diagnostics);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public JdkCompileException(String message, Set<String> qualifiedClassNames,
                               DiagnosticCollector<JavaFileObject> diagnostics) {
        super(message);
        setClassNames(qualifiedClassNames);
        setDiagnostics(diagnostics);
    }

    public JdkCompileException(Set<String> qualifiedClassNames, Throwable cause,
                               DiagnosticCollector<JavaFileObject> diagnostics) {
        super(cause);
        setClassNames(qualifiedClassNames);
        setDiagnostics(diagnostics);
    }

    private void setClassNames(Set<String> qualifiedClassNames) {
        // create a new HashSet because the set passed in may not
        // be Serializable. For example, Map.keySet() returns 
        // a non-Serializable set.
        classNames = new HashSet<>(qualifiedClassNames);
    }

    private void setDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics) {
        this.diagnostics = diagnostics;
    }

    /**
     * Gets the diagnostics collected by this exception.
     * 
     * @return this exception's diagnostics
     */
    public DiagnosticCollector<JavaFileObject> getDiagnostics() {
        return diagnostics;
    }

    /**
     * @return The name of the classes whose compilation caused the compile exception
     */
    public Collection<String> getClassNames() {
        return Collections.unmodifiableSet(classNames);
    }
}
