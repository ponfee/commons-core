/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.util;

import cn.ponfee.commons.io.Files;
import cn.ponfee.commons.math.Numbers;
import com.google.common.base.CaseFormat;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 字符串工具类
 *
 * @author Ponfee
 */
public class Strings {

    private static final char[] REGEX_SPECIALS = { '\\', '$', '(', ')', '*', '+', '.', '[', ']', '?', '^', '{', '}', '|' };

    public static String join(Collection<?> coll) {
        return join(coll, ",", String::valueOf, "", "");
    }

    public static String join(Collection<?> coll, String delimiter) {
        return join(coll, delimiter, String::valueOf, "", "");
    }

    /**
     * Convert to hexadecimal string array
     *
     * @param text the text
     * @return hexadecimal string array
     */
    public static String[] hexadecimal(String text) {
        // Integer.toString(text.charAt(i), 16)
        return IntStream.range(0, text.length())
                        .mapToObj(i -> "0x" + Integer.toHexString(text.charAt(i)))
                        .toArray(String[]::new);
    }

    /**
     * 集合拼接为字符串<p>
     * join(Arrays.asList("a","b","c"), ",", "(", ")") -> (a),(b),(c)
     *
     * @param coll      集合对象
     * @param delimiter 分隔符
     * @param mapper    对象转String
     * @param open      每个元素添加的前缀
     * @param close     每个元素添加的后缀
     * @return a String with joined
     *
     * @see org.apache.commons.collections4.IteratorUtils#toString(Iterator, org.apache.commons.collections4.Transformer, String, String, String)
     * @see org.apache.commons.collections4.IterableUtils#toString(Iterable, org.apache.commons.collections4.Transformer, String, String, String)
     *
     * @see java.lang.String#join(CharSequence, CharSequence...)
     * @see java.util.stream.Collectors#joining(CharSequence, CharSequence, CharSequence)
     * @see org.apache.commons.lang3.StringUtils#join(List, String, int, int)
     * @see com.google.common.base.Joiner#join(Object, Object, Object...)
     * @see java.util.StringJoiner#StringJoiner(CharSequence, CharSequence, CharSequence)
     */
    public static <T> String join(Collection<T> coll, String delimiter, Function<T, String> mapper, String open, String close) {
        if (coll == null) {
            return null;
        }
        if (coll.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder(128);
        for (T o : coll) {
            builder.append(open).append(mapper.apply(o)).append(close).append(delimiter);
        }
        builder.setLength(builder.length() - delimiter.length());
        return builder.toString();
    }

    /**
     * Parse main method args, such as: [name1=value,name2=value2,...]
     *
     * @param args the args
     * @return a map object params
     */
    public static Map<String, String> fromArgs(String[] args) {
        if (args == null) {
            return null;
        }
        return Arrays.stream(args)
                     .filter(s -> s != null && s.contains("="))
                     .map(s -> s.split("=", 2))
                     .collect(Collectors.toMap(p -> p[0], p -> p[1], (v1, v2) -> v1));
    }

    public static String mask(String text, String regex, String replacement) {
        if (text == null) {
            return null;
        }
        return text.replaceAll(regex, replacement);
    }

    public static String mask(String text, int start, int len) {
        return mask(text, start, len, '*');
    }

    /**
     * 遮掩（如手机号中间4位加*）
     * @param text    需要处理的字符串
     * @param start   开始位置
     * @param len     要处理的字数长度
     * @param maskChar 替换的字符
     * @return
     */
    public static String mask(String text, int start, int len, char maskChar) {
        int length;
        if (len < 1 || StringUtils.isEmpty(text)
            || (length = text.length()) < start) {
            return text;
        }
        if (start < 0) {
            start = 0;
        }
        if (length < start + len) {
            len = length - start;
        }
        int end = length - start - len;
        String regex = "(\\w{" + start + "})\\w{" + len + "}(\\w{" + end + "})";
        return mask(text, regex, "$1" + StringUtils.repeat(maskChar, len) + "$2");
    }

    /**
     * Count str occur on text.
     *
     * @param text the text
     * @param str  the string
     * @return number of occur count
     */
    public static int count(String text, String str) {
        int count = 0;
        for (int len = str.length(), index=-len; (index = text.indexOf(str, index + len)) != -1; ) {
            count++;
        }
        return count;
    }

    /**
     * 字符串分片
     * slice("abcdefghijklmn", 5)  ->  ["abc","def","ghi","jkl","mn"]
     * @param str
     * @param segment
     * @return
     */
    public static String[] slice(String str, int segment) {
        int[] array = Numbers.slice(str.length(), segment);
        String[] result = new String[array.length];
        for (int j = 0, i = 0; i < array.length; i++) {
            result[i] = str.substring(j, (j += array[i]));
        }
        return result;
    }

    /**
     * <pre>
     * '?' Matches any single character.
     * '*' Matches any sequence of characters (including the empty sequence).
     *
     * isMatch("aa","a")       = false
     * isMatch("aa","aa")      = true
     * isMatch("aaa","aa")     = false
     * isMatch("aa", "*")      = true
     * isMatch("aa", "a*")     = true
     * isMatch("ab", "?*")     = true
     * isMatch("aab", "c*a*b") = false
     * </pre>
     *
     * @param s the text
     * @param p the wildcard pattern
     * @return {@code true} if the string match pattern
     */
    public static boolean isMatch(String s, String p) {
        // 状态 dp[i][j] : 表示 s 的前 i 个字符和 p 的前 j 个字符是否匹配 (true 的话表示匹配)
        // 状态转移方程：
        //      1. 当 s[i] == p[j]，或者 p[j] == ? 那么 dp[i][j] = dp[i - 1][j - 1];
        //      2. 当 p[j] == * 那么 dp[i][j] = dp[i][j - 1] || dp[i - 1][j]    其中：
        //      dp[i][j - 1] 表示 * 代表的是空字符，例如 ab, ab*
        //      dp[i - 1][j] 表示 * 代表的是非空字符，例如 abcd, ab*
        // 初始化：
        //      1. dp[0][0] 表示什么都没有，其值为 true
        //      2. 第一行 dp[0][j]，换句话说，s 为空，与 p 匹配，所以只要 p 开始为 * 才为 true
        //      3. 第一列 dp[i][0]，当然全部为 false
        int m = s.length(), n = p.length();
        boolean[][] dp = new boolean[m + 1][n + 1];
        dp[0][0] = true;
        for (int i = 1; i <= n; ++i) {
            if (p.charAt(i - 1) == '*') {
                dp[0][i] = true;
            } else {
                break;
            }
        }
        for (int i = 1; i <= m; ++i) {
            for (int j = 1; j <= n; ++j) {
                if (p.charAt(j - 1) == '*') {
                    dp[i][j] = dp[i][j - 1] || dp[i - 1][j];
                } else if (p.charAt(j - 1) == '?' || s.charAt(i - 1) == p.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                }
            }
        }
        return dp[m][n];
    }

    /**
     * Returns a safe file system path that forbid access parent dir
     *
     * @param path the path
     * @return a safe path
     */
    public static String safePath(String path) {
        if (path == null) {
            return null;
        }
        return cleanPath(path).replace("../", "");
    }

    /**
     * 文件路径规范化，如“path/..”内部的点号
     * 注意：windows的文件分隔符“\”会替换为“/”
     *
     * @param path 文件路径
     * @return 规范的文件路径
     */
    public static String cleanPath(String path) {
        if (path == null) {
            return null;
        }

        String pathToUse = StringUtils.replace(path, Files.WINDOWS_FOLDER_SEPARATOR, Files.UNIX_FOLDER_SEPARATOR);

        // Strip prefix from path to analyze, to not treat it as part of the
        // first path element. This is necessary to correctly parse paths like
        // "file:core/../core/io/Resource.class", where the ".." should just
        // strip the first "core" directory while keeping the "file:" prefix.
        int prefixIndex = pathToUse.indexOf(":");
        String prefix = "";
        if (prefixIndex != -1) {
            prefix = pathToUse.substring(0, prefixIndex + 1);
            if (prefix.contains("/")) {
                prefix = "";
            } else {
                pathToUse = pathToUse.substring(prefixIndex + 1);
            }
        }
        if (pathToUse.startsWith(Files.UNIX_FOLDER_SEPARATOR)) {
            prefix = prefix + Files.UNIX_FOLDER_SEPARATOR;
            pathToUse = pathToUse.substring(1);
        }

        String[] pathArray = StringUtils.split(pathToUse, Files.UNIX_FOLDER_SEPARATOR);
        List<String> pathElements = new LinkedList<>();
        int tops = 0;

        for (int i = pathArray.length - 1; i >= 0; i--) {
            String element = pathArray[i];
            if (Files.CURRENT_PATH.equals(element)) {
                // Points to current directory - drop it.
            } else if (Files.TOP_PATH.equals(element)) {
                // Registering top path found.
                tops++;
            } else {
                if (tops > 0) {
                    // Merging path element with element corresponding to top path.
                    tops--;
                } else {
                    // Normal path element found.
                    pathElements.add(0, element);
                }
            }
        }

        // Remaining top paths need to be retained.
        for (int i = 0; i < tops; i++) {
            pathElements.add(0, Files.TOP_PATH);
        }

        return prefix + String.join(Files.UNIX_FOLDER_SEPARATOR, pathElements);
    }

    /**
     * 驼峰转为带分隔符名字，如驼峰转换为下划线：CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, camelCaseName);
     *
     * @param camelcaseName the camelcase name
     * @param separator     the separator
     * @return with separator name
     * @see CaseFormat#to(CaseFormat, String)
     */
    public static String toSeparatedName(String camelcaseName, char separator) {
        if (StringUtils.isEmpty(camelcaseName)) {
            return camelcaseName;
        }

        StringBuilder result = new StringBuilder(camelcaseName.length() << 1);
        result.append(Character.toLowerCase(camelcaseName.charAt(0)));
        for (int i = 1, len = camelcaseName.length(); i < len; i++) {
            char ch = camelcaseName.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append(separator).append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * 带分隔符名字转驼峰，如下划线转换为驼峰：aseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, underscoreName);
     * 1  LOWER_HYPHEN       连字符的变量命名规范如lower-hyphen
     * 2  LOWER_UNDERSCORE   c++变量命名规范如lower_underscore
     * 3  LOWER_CAMEL        java变量命名规范如lowerCamel
     * 4  UPPER_CAMEL        java和c++类的命名规范如UpperCamel
     * 5  UPPER_UNDERSCORE   java和c++常量的命名规范如UPPER_UNDERSCORE
     *
     * @param separatedName the separated name
     * @param separator     the separator
     * @return camelcase name
     * @see CaseFormat#to(CaseFormat, String)
     */
    public static String toCamelcaseName(String separatedName, char separator) {
        if (StringUtils.isEmpty(separatedName)) {
            return separatedName;
        }

        StringBuilder result = new StringBuilder(separatedName.length());
        for (int i = 0, len = separatedName.length(); i < len; i++) {
            char ch = separatedName.charAt(i);
            if (separator == ch) {
                if (++i < len) {
                    result.append(Character.toUpperCase(separatedName.charAt(i)));
                }
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * 如果为空则设置默认
     *
     * @param str
     * @param defaultStr
     * @return
     */
    public static String ifEmpty(String str, String defaultStr) {
        return StringUtils.isEmpty(str) ? defaultStr : str;
    }

    public static String ifBlank(String str, String defaultStr) {
        return StringUtils.isBlank(str) ? defaultStr : str;
    }

    // ---------------------------------------------------------------------------escape
    /**
     * <pre>
     * Escapes the characters in a <code>String</code> to be suitable to pass to
     * an SQL query.
     *
     * For example,
     *  statement.executeQuery("SELECT * FROM MOVIES WHERE TITLE='" +
     *  StringEscapeUtils.escapeSql("McHale's Navy") +  "'");
     *
     * At present, this method only turns single-quotes into doubled single-quotes
     * (<code>"McHale's Navy"</code> => <code>"McHale''s Navy"</code>). It does not
     * handle the cases of percent (%) or underscore (_) for use in LIKE clauses.
     *
     * see http://www.jguru.com/faq/view.jsp?EID=8881
     * </pre>
     *
     * @param str  the string to escape, may be null
     * @return a new String, escaped for SQL, <code>null</code> if null string input
     */
    public static String escapeSql(String str) {
        if (str == null) {
            return null;
        }
        return StringUtils.replace(str, "'", "''");
    }

    /**
     * Escape the regex characters: $()*+.[]?\^{},|
     *
     * @param text the text string
     * @return a new String, escaped for regex
     */
    public static String escapeRegex(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }

        StringBuilder escaped = new StringBuilder(text.length() + 8);
        char c;
        for (int i = 0, n = text.length(); i < n; i++) {
            c = text.charAt(i);
            if (ArrayUtils.contains(REGEX_SPECIALS, c)) {
                escaped.append('\\');
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    public static boolean containsAny(String str, List<String> searches) {
        if (StringUtils.isEmpty(str) || CollectionUtils.isEmpty(searches)) {
            return false;
        }
        for (String search : searches) {
            if (StringUtils.contains(str, search)) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------------------csv split
    /**
    * Parse a CSV string using {@link #csvSplit(List,String, int, int)}
    * use in {@link cn.ponfee.commons.web.WebContext.WebContextFilter)
    *
    * @param s The string to parse
    * @return An array of parsed values.
    */
    public static String[] csvSplit(String s) {
        if (s == null) {
            return null;
        }
        return csvSplit(s, 0, s.length());
    }

    /**
     * Parse a CSV string using {@link #csvSplit(List, String, int, int)}
     *
     * @param s The string to parse
     * @param off The offset into the string to start parsing
     * @param len The len in characters to parse
     * @return An array of parsed values.
     */
    public static String[] csvSplit(String s, int off, int len) {
        if (s == null) {
            return null;
        }
        if (off < 0 || len < 0 || off > s.length()) {
            throw new IllegalArgumentException();
        }

        List<String> list = new ArrayList<>();
        csvSplit(list, s, off, len);
        return list.toArray(new String[0]);
    }

    private enum CsvSplitState {
        PRE_DATA, QUOTE, SLOSH, DATA, WHITE, POST_DATA
    }

    /** Split a quoted comma separated string to a list
     * <p>Handle <a href="https://www.ietf.org/rfc/rfc4180.txt">rfc4180</a>-like
     * CSV strings, with the exceptions:<ul>
     * <li>quoted values may contain double quotes escaped with back-slash
     * <li>Non-quoted values are trimmed of leading trailing white space
     * <li>trailing commas are ignored
     * <li>double commas result in a empty string value
     * </ul>
     * @param list The Collection to split to (or null to get a new list)
     * @param s The string to parse
     * @param off The offset into the string to start parsing
     * @param len The len in characters to parse
     * @return list containing the parsed list values
     */
    public static List<String> csvSplit(List<String> list, String s, int off, int len) {
        if (list == null) {
            list = new ArrayList<>();
        }
        CsvSplitState state = CsvSplitState.PRE_DATA;
        StringBuilder out = new StringBuilder();
        int last = -1;
        while (len > 0) {
            char ch = s.charAt(off++);
            len--;

            switch (state) {
                case PRE_DATA:
                    if (Character.isWhitespace(ch)) {
                        continue;
                    }

                    if ('"' == ch) {
                        state = CsvSplitState.QUOTE;
                        continue;
                    }

                    if (',' == ch) {
                        list.add("");
                        continue;
                    }

                    state = CsvSplitState.DATA;
                    out.append(ch);
                    continue;

                case DATA:
                    if (Character.isWhitespace(ch)) {
                        last = out.length();
                        out.append(ch);
                        state = CsvSplitState.WHITE;
                        continue;
                    }

                    if (',' == ch) {
                        list.add(out.toString());
                        out.setLength(0);
                        state = CsvSplitState.PRE_DATA;
                        continue;
                    }

                    out.append(ch);
                    continue;

                case WHITE:
                    if (Character.isWhitespace(ch)) {
                        out.append(ch);
                        continue;
                    }

                    if (',' == ch) {
                        out.setLength(last);
                        list.add(out.toString());
                        out.setLength(0);
                        state = CsvSplitState.PRE_DATA;
                        continue;
                    }

                    state = CsvSplitState.DATA;
                    out.append(ch);
                    last = -1;
                    continue;

                case QUOTE:
                    if ('\\' == ch) {
                        state = CsvSplitState.SLOSH;
                        continue;
                    }
                    if ('"' == ch) {
                        list.add(out.toString());
                        out.setLength(0);
                        state = CsvSplitState.POST_DATA;
                        continue;
                    }
                    out.append(ch);
                    continue;

                case SLOSH:
                    out.append(ch);
                    state = CsvSplitState.QUOTE;
                    continue;

                case POST_DATA:
                    if (',' == ch) {
                        state = CsvSplitState.PRE_DATA;
                        continue;
                    }
                    continue;
                default:
                    throw new UnsupportedOperationException("Unsupported state " + state);
            }
        }

        switch (state) {
            case PRE_DATA:
            case POST_DATA:
                break;
            case DATA:
            case QUOTE:
            case SLOSH:
                list.add(out.toString());
                break;
            case WHITE:
                out.setLength(last);
                list.add(out.toString());
                break;
            default:
                throw new UnsupportedOperationException("Unsupported state " + state);
        }

        return list;
    }

}
