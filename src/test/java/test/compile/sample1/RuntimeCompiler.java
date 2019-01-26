package test.compile.sample1;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

public class RuntimeCompiler {

    private List<String> options = null;
    private JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
    private StringBuffer traceMsg = new StringBuffer();

    public RuntimeCompiler(String... options) {
        // inital compile params
        if (options != null && options.length > 0) {
            this.options = Arrays.asList(options);
        }
    }

    public boolean compile(String className, String code) {
        JavaFileObject sourceFile = new StringJavaFileObject(className, code);
        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(sourceFile);
        CompilationTask task = compiler.getTask(null, null, diagnostics, options, null, compilationUnits);
        boolean result = task.call();

        // Record compile error messages
        for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
            traceMsg.append(diagnostic.getMessage(null)).append("\n");
            traceMsg.append(String.format("Error on line %d in %s%n", diagnostic.getLineNumber(),
                    ((FileObject) diagnostic.getSource()).toUri()));
        }

        return result;
    }

    public String getTraceMsg() {
        return traceMsg.toString();
    }
}

class StringJavaFileObject extends SimpleJavaFileObject {

    final String code;

    StringJavaFileObject(String className, String code) {
        super(URI.create(className + Kind.SOURCE.extension), Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }
}
