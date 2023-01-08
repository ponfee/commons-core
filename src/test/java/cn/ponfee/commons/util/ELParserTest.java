package cn.ponfee.commons.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.google.common.collect.ImmutableMap;

import cn.ponfee.commons.json.Jsons;
import cn.ponfee.commons.parser.ELParser;

public class ELParserTest {

    private static final Pattern PARAMS_PATTERN = Pattern.compile("\\$\\{\\s*([a-zA-Z0-9_\\-\\.]+)\\s*\\}");
    private static final Pattern DATETM_PATTERN = Pattern.compile("\\$\\[\\s*([a-zA-Z0-9_,\\-\\+\\.\\(\\)\\s]+)\\s*\\]");
    private static final Pattern SPEL_PATTERN = Pattern.compile("\\{\\{\\s*([^\\{\\}]+)\\s*\\}\\}");

    @Test
    public void test1() {
        String text = "dfsa |${abc}| a |${12}|  |$[ yyyyMM \n]|  xx  |$[start_year(  yyyyMMddHHmmss,  -1y)]| aa |$[now(  timestamp\n,  -1y)]";
        Matcher matcher = PARAMS_PATTERN.matcher(text);
        while (matcher.find()) {
            System.out.println(matcher.group(1));
        }
    }

    @Test
    public void test2() {
        String text = "dfsa |${abc}| a |${12}|  |$[ yyyyMM \n]|  xx  |$[start_year(  yyyyMMddHHmmss,  -1y)]| aa |$[now(  timestamp\n,  -1y)]";
        Matcher matcher = DATETM_PATTERN.matcher(text);
        while (matcher.find()) {
            System.out.println(matcher.group(1));
        }
    }

    @Test
    public void test3() {
        String text = "{{#key1}},{{#key2}}";
        Matcher matcher = SPEL_PATTERN.matcher(text);
        while (matcher.find()) {
            System.out.println(matcher.group(1));
        }
    }

    @Test
    public void test4() {
        StandardEvaluationContext context = new StandardEvaluationContext(new String[] { "a", "b" });
        ExpressionParser parser = new SpelExpressionParser();

        String name = parser.parseExpression("#root[0]").getValue(context, String.class);
        System.out.println(name);

        name = parser.parseExpression("\"asfdsaf\"").getValue(context, String.class);
        System.out.println(name);
    }

    @Test
    public void test5() {
        System.out.println(ELParser.parse("dfsa |${abc}| a |${12}|  |$[ yyyyMM \n]|  xx  |$[start_year(  yyyyMMddHHmmss,  -1y)]| aa |$[now(  timestamp\n,  -1y)]", ImmutableMap.of("abc", 123, "test.1", "xxx")));

        String params =
            "{\"from\":0,\"size\":5000,\"query\":{\"bool\":{\"must\":[{\"range\":{\"statEndTime\":{\"from\":$[start_year(timestamp)],\"to\":$[end_day(timestamp)],\"include_lower\":true,\"include_upper\":true}}},{\"terms\":{\"companyName\":[\"xx\",\"yy\"]}}]}}}";
        System.out.println(ELParser.parse(params));

        System.out.println(Jsons.toJson(ImmutableMap.of("key", "val")));
        System.out.println(ELParser.parse("{{#key1}},{{#key2}}", ImmutableMap.of("key1", "val1", "key2", "val2")));
    }
}
