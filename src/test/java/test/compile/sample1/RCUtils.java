package test.compile.sample1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RCUtils {
    private static Pattern classNamePattern = Pattern.compile("\\s+class\\s+([a-zA-Z_$]+[0-9a-zA-Z_$]+)[\\s{]");
    private static Pattern packagePattern = Pattern.compile("\\s*package\\s+([a-z\\.]+)[\\s;]");
    
    public static String getClassName(String str) {
        Matcher matcher = classNamePattern.matcher(str);
        if (!matcher.find()) {
            throw new RuntimeException("Can't find class name in content: \n" + str);
        }
        return matcher.group(1);
    }

    public static String getFullClassName(String str) {
        String className = getClassName(str);
        
        Matcher matcher = packagePattern.matcher(str);
        if (!matcher.find()) {
            return className;
        }
        String packageName = matcher.group(1);
        
        return packageName + "." + className;
    }
}
