package code.ponfee.commons.compile.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则匹配不精确，建议用jdk-tools工具来进行词法分析（JavacJavaSource.java）
 * 只会匹配第一个public class类
 * @author fupf
 */
public class RegexJavaSource extends JavaSource {

    private static final long serialVersionUID = -9205215223349262114L;

    public static final String VALID_NAME = "[a-zA-Z_$][a-zA-Z0-9_$]*";
    public static final String VALID_PKGE = "(" + VALID_NAME + "(\\." + VALID_NAME + ")*)*";
    public static final Pattern QUALIFIER_PATTERN = Pattern.compile("^" + VALID_PKGE + VALID_NAME + "$");

    /** 正则提取：(?m)启用多行 */
    private static final Pattern PACKAGE_NAME = Pattern.compile("(?m)^\\s*package\\s+([^;]+);");
    private static final Pattern PUBLIC_CLASS = Pattern.compile("(?m)^\\s*public(((\\s+strictfp)?(\\s+(final|abstract))?)|((\\s+(final|abstract))?(\\s+strictfp)?))\\s+class\\s+\\b(" + VALID_NAME + ")\\b(\\s+extends\\s+\\b(" + VALID_NAME + ")\\b)?(\\s+implements\\s+\\b(" + VALID_NAME + ")\\b(\\s*,\\s*\\b(" + VALID_NAME + ")\\b\\s*)*)?\\s*\\{");

    public RegexJavaSource(String sourceCode) {
        super(sourceCode);

        /*// 通过嵌入式标志表达式 (?s) 也可以启用 dotall 模式（s 是 "single-line" 模式的助记符，在 Perl中也使用它）
        // X*?  X，零次或多次（懒汉模式）
        String findFirst = RegexUtils.findFirst(sourceString, "package (?s).*?;");
        this.packageName = findFirst.replaceAll("package ", EMPTY).replaceAll(";", EMPTY).trim();

        findFirst = RegexUtils.findFirst(sourceString, "public class (?s).*?{");
        this.publicClass = findFirst.split("extends")[0].split("implements")[0]
                                    .replaceAll("public class ", EMPTY).replace("{", EMPTY).trim();*/

        Matcher matcher = PACKAGE_NAME.matcher(sourceCode);
        if (matcher.find()) {
            super.packageName = matcher.group(1).replaceAll("\\s", "");
        } else {
            super.packageName = null;
        }

        matcher = PUBLIC_CLASS.matcher(sourceCode);
        if (matcher.find()) {
            this.publicClass = matcher.group(10);
        } else {
            throw new IllegalArgumentException("invalid java source code, public class not found.");
        }
    }

}
