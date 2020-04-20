package code.ponfee.commons.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import code.ponfee.commons.collect.Collects;

/**
 * 消息格式化
 * 
 * @author Ponfee
 */
public final class MessageFormats {
    private MessageFormats() {}

    private static final String PREFIX = "#\\{(\\s|\\t)*";
    private static final String SUFFIX = "(\\s|\\t)*\\}";
    private static final Pattern PATTERN = Pattern.compile(PREFIX + "(\\w+)" + SUFFIX);

    public static String format(String text, Map<String, Object> args) {
        List<String> arguments = new ArrayList<>(args.size());
        int i = 0;
        for (Entry<String, Object> entry : args.entrySet()) {
            text = text.replaceAll(PREFIX + entry.getKey() + SUFFIX, "{" + i++ + "}");
            // toString reason：MessageFormat.format("{0}", 10000) -> 10,000
            arguments.add(Objects.toString(entry.getValue(), ""));
        }
        return MessageFormat.format(text, arguments.toArray());
    }

    public static String format(String text, Object... args) {
        Map<String, Object> map = new HashMap<>(args.length << 1);
        Matcher matcher = PATTERN.matcher(text);
        for (int n = args.length, i = 0; i < n && matcher.find(); i++) {
            map.put(matcher.group(2), args[i]);
        }
        return format(text, map);
    }

    public static String formatPair(String text, Object... args) {
        return format(text, Collects.toMap(args));
    }

}
