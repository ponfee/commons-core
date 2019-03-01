package code.ponfee.commons.parser;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * EL parser utility.
 * 
 * ELParser.parse("test |${abc}| a |${12}|  |$[ yyyyMM \n]|  xx  |$[start_year( yyyyMMddHHmmss, -1y )]| aa |$[now( timestamp\n, -1y)]|", ImmutableMap.of("abc", 123, "test.1", "xxx"))
 * 
 * @author Ponfee
 */
public class ELParser {

    private static final Pattern PARAMS_PATTERN = Pattern.compile("\\$\\{\\s*([a-zA-Z0-9_\\-\\.]+)\\s*\\}");
    private static final Pattern DATETM_PATTERN = Pattern.compile("\\$\\[\\s*([a-zA-Z0-9_,\\-\\+\\.\\(\\)\\s]+)\\s*\\]");
    private static final Pattern SPEL_PATTERN   = Pattern.compile("\\{\\{(.+)\\}\\}");

    public static String parse(String text) {
        return parseDatetm(text);
    }

    public static String parse(String text, Map<String, ?> params) {
        text = parseParams(text, params);
        text = parseDatetm(text);
        text = parseSpel(text, params);
        return text;
    }

    // ----------------------------------------------------------------------------- private methods
    private static String parseParams(String text, Map<String, ?> params) {
        if (MapUtils.isEmpty(params)) {
            return text;
        }
        String result = text;
        Matcher matcher = PARAMS_PATTERN.matcher(text);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (params.containsKey(name)) {
                // matcher.group() -> matcher.group(0)
                result = StringUtils.replace(result, matcher.group(), String.valueOf(params.get(name)));
            }
        }
        return result;
    }

    private static String parseDatetm(String text) {
        Matcher matcher = DATETM_PATTERN.matcher(text);
        String result = text;
        while (matcher.find()) {
            String val = translate(matcher.group(1));
            if (val != null) {
                result = StringUtils.replace(result, matcher.group(), val);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static String parseSpel(String text, Map<String, ?> params) {
        if (MapUtils.isEmpty(params)) {
            return text;
        }

        StandardEvaluationContext context = new StandardEvaluationContext(params);
        context.setVariables((Map<String, Object>) params); // #key
        //context.setVariables("name", (Map<String, Object>) params); // #name[key]
        //context.setRootObject(params);
        ExpressionParser parser = new SpelExpressionParser();
        String result = text;
        Matcher matcher = SPEL_PATTERN.matcher(text);
        while (matcher.find()) {
            result = StringUtils.replace(
                result, matcher.group(), 
                String.valueOf(parser.parseExpression(matcher.group(1)).getValue(context))
            );
        }
        return result;
    }

    private static String translate(String text) {
        try {
            text = text.trim();
            int argsStart = text.indexOf('(');
            if (argsStart > 0) {
                if (!text.endsWith(")")) {
                    return null;
                }
                String methodName = LOWER_UNDERSCORE.to(
                    LOWER_CAMEL, text.substring(0, argsStart).trim().toLowerCase()
                );
                String[] params = text.substring(argsStart + 1, text.lastIndexOf(')')).split(",");
                Method method;
                if (params.length == 1) {
                    if (StringUtils.isBlank(params[0])) {
                        method = DateFuncs.class.getMethod(methodName);
                        return (String) method.invoke(null);
                    } else {
                        method = DateFuncs.class.getMethod(methodName, String.class);
                        return (String) method.invoke(null, params[0].trim());
                    }
                } else if (params.length == 2) {
                    method = DateFuncs.class.getMethod(methodName, String.class, String.class);
                    return (String) method.invoke(null, params[0].trim(), params[1].trim());
                } else {
                    throw new UnsupportedOperationException();
                }
            } else {
                return DateFuncs.now(text);
            }
        } catch (Exception e) {
            return null;
        }
    }

}
