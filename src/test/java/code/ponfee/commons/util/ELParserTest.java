package code.ponfee.commons.util;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.parser.ELParser;

public class ELParserTest {

    public static void main(String[] args) {
        System.out.println(ELParser.parse("dfsa |${abc}| a |${12}|  |$[ yyyyMM \n]|  xx  |$[start_year(  yyyyMMddHHmmss,  -1y)]| aa |$[now(  timestamp\n,  -1y)]|", ImmutableMap.of("abc", 123, "test.1", "xxx")));

        String params = "{\"from\":0,\"size\":5000,\"query\":{\"bool\":{\"must\":[{\"range\":{\"statEndTime\":{\"from\":$[start_year(timestamp)],\"to\":$[end_day(timestamp)],\"include_lower\":true,\"include_upper\":true}}},{\"terms\":{\"companyName\":[\"xx\",\"yy\"]}}]}}}";
        System.out.println(ELParser.parse(params));
    }
}
