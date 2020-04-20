package code.ponfee.commons.parser;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
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
    private static final Pattern SPEL_PATTERN   = Pattern.compile("\\{\\{\\s*([^\\{\\}]+)\\s*\\}\\}");

    public static String parse(String text) {
        return parseDateUDF(text);
    }

    public static String parse(String text, Map<String, ?> params) {
        text = parseParams(text, params);
        text = parseSpel(text, params);
        text = parseDateUDF(text);
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
                result = StringUtils.replace(result, matcher.group(), Objects.toString(params.get(name), ""));
            }
        }
        return result;
    }

    private static String parseDateUDF(String text) {
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

    /**
     * SPEL Java: parser.parseExpression("T(java.lang.Math).PI").getValue(double.class);
     *            parser.parseExpression("T(java.lang.Math).random()").getValue(double.class)
     *            parser.parseExpression("'Hi,everybody'").getValue(String.class)
     * 
     * SPEL Xml : parser.parseExpression("#{T(java.lang.Math).PI}").getValue(double.class);
     *            parser.parseExpression("#{T(java.lang.Math).random()}").getValue(double.class)
     *            parser.parseExpression("#{'Hi,everybody'}").getValue(String.class)
     * @param text
     * @param params
     * @return
     */
    @SuppressWarnings("unchecked")
    private static String parseSpel(String text, Map<String, ?> params) {
        if (MapUtils.isEmpty(params)) {
            return text;
        }

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables((Map<String, Object>) params); // #key
        //context.setVariables("name", (Map<String, Object>) params); // #name[key]
        //context.setRootObject(params); // #root.field
        ExpressionParser parser = new SpelExpressionParser();
        String result = text;
        Matcher matcher = SPEL_PATTERN.matcher(text);
        while (matcher.find()) {
            result = StringUtils.replace(
                result, matcher.group(), 
                Objects.toString(parser.parseExpression(matcher.group(1)).getValue(context), null)
            );
        }
        return result;
    }

    private static String translate(String text) {
        try {
            text = text.trim();
            int argsStart = text.indexOf('(');

            if (argsStart < 1) {
                // $[yyyyMMdd]
                return DateUDF.now(text);
            }

            if (!text.endsWith(")")) {
                return null;
            }

            String methodName = LOWER_UNDERSCORE.to(LOWER_CAMEL, text.substring(0, argsStart).trim().toLowerCase());
            String[] params = text.substring(argsStart + 1, text.lastIndexOf(')')).split(",");
            if (params.length == 1) {
                // e.g. $[now(timestamp)] or $[start_day(yyyyMMddHHmmss)]
                return (String) getPublicStaticMethod(DateUDF.class, methodName, String.class)
                    .invoke(null, params[0].trim());
            } else if (params.length == 2) {
                // e.g. $[start_year(yyyyMMddHHmmss, -1y)]
                return (String) getPublicStaticMethod(DateUDF.class, methodName, String.class, String.class)
                    .invoke(null, params[0].trim(), params[1].trim());
            } else {
                return null;
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Method getPublicStaticMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = ArrayUtils.isEmpty(parameterTypes) 
                      ? clazz.getDeclaredMethod(methodName) 
                      : clazz.getDeclaredMethod(methodName, parameterTypes);
        int modifiers = method.getModifiers();
        return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) ? method : null;
    }

}
