package code.ponfee.commons.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.CaseFormat;

import code.ponfee.commons.math.Numbers;

/**
 * 字符串工具类
 * @author fupf
 */
public class Strings {

    public static final char BLANK_CHAR = ' ';
    public static final String FOLDER_SEPARATOR = "/";
    public static final String WINDOWS_FOLDER_SEPARATOR = "\\";
    public static final String TOP_PATH = "..";
    public static final String CURRENT_PATH = ".";

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
     * 字符串分片
     * slice("abcdefghijklmn", 5)  ->  ["abc","def","ghi","jkl","mn"]
     * @param str
     * @param segment
     * @return
     */
    public static String[] slice(String str, int segment) {
        int[] array = Numbers.average(str.length(), segment);
        String[] result = new String[array.length];
        for (int j = 0, i = 0; i < array.length; i++) {
            result[i] = str.substring(j, (j += array[i]));
        }
        return result;
    }

    /**
     * 删除指定字符串中任意的字符<p>
     * deleteAny("hello world", "eo")  ->  hll wrld
     * @param inString       原字符串
     * @param charsToDelete  指定要删除的字符
     * @return               删除后的字符
     */
    public static String deleteAny(String inString, String charsToDelete) {
        if (StringUtils.isEmpty(inString) || StringUtils.isEmpty(charsToDelete)) {
            return inString;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < inString.length(); i++) {
            char c = inString.charAt(i);
            if (charsToDelete.indexOf(c) == -1) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 以分隔符拆分字符串为字符串数组
     * @param str         待拆分字符串
     * @param delimiter   分隔符
     * @return
     */
    public static String[] split(String str, String delimiter) {
        return split(str, delimiter, null);
    }

    /**
     * 以分隔符拆分字符串为字符串数组<p>
     * split("hello world", "l", "eo")  ->  [h,, wr,d]
     * @param str            待拆分字符串
     * @param delimiter      分隔符
     * @param charsToDelete  待删除的字符串
     * @return
     */
    public static String[] split(String str, String delimiter, String charsToDelete) {
        if (str == null) {
            return new String[0];
        }
        if (delimiter == null) {
            return new String[] { str };
        }
        List<String> result = new ArrayList<>();
        if ("".equals(delimiter)) {
            for (int i = 0; i < str.length(); i++) {
                result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
            }
        } else {
            int pos = 0;
            int delPos;
            while ((delPos = str.indexOf(delimiter, pos)) != -1) {
                result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
                pos = delPos + delimiter.length();
            }
            if (str.length() > 0 && pos <= str.length()) {
                // Add rest of String, but not in case of empty input.
                result.add(deleteAny(str.substring(pos), charsToDelete));
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
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
     * @param s characters
     * @param p pattern
     * @return match result: true|false
     */
    public static boolean isMatch(String s, String p) {
        int idxs = 0, idxp = 0, idxstar = -1, idxmatch = 0;
        while (idxs < s.length()) {
            // 当两个指针指向完全相同的字符时，或者p中遇到的是?时
            if (idxp < p.length() && (s.charAt(idxs) == p.charAt(idxp) || p.charAt(idxp) == '?')) {
                idxp++;
                idxs++;
                // 如果字符不同也没有?，但在p中遇到是*时，我们记录下*的位置，但不改变s的指针
            } else if (idxp < p.length() && p.charAt(idxp) == '*') {
                idxstar = idxp;
                idxp++;
                //遇到*后，我们用idxmatch来记录*匹配到的s字符串的位置，和不用*匹配到的s字符串位置相区分
                idxmatch = idxs;
                // 如果字符不同也没有?，p指向的也不是*，但之前已经遇到*的话，我们可以从idxmatch继续匹配任意字符
            } else if (idxstar != -1) {
                // 用上一个*来匹配，那我们p的指针也应该退回至上一个*的后面
                idxp = idxstar + 1;
                // 用*匹配到的位置递增
                idxmatch++;
                // s的指针退回至用*匹配到位置
                idxs = idxmatch;
            } else {
                return false;
            }
        }
        // 因为1个*能匹配无限序列，如果p末尾有多个*，我们都要跳过
        while (idxp < p.length() && p.charAt(idxp) == '*') {
            idxp++;
        }
        // 如果p匹配完了，说明匹配成功
        return idxp == p.length();
    }

    /**
     * 文件路径规范化，如“path/..”内部的点号
     * 注意：windows的文件分隔符“\”会替换为“/”
     * @param path 文件路径
     * @return 规范的文件路径 
     */
    public static String cleanPath(String path) {
        if (path == null) {
            return null;
        }

        String pathToUse = replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);

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
        if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
            prefix = prefix + FOLDER_SEPARATOR;
            pathToUse = pathToUse.substring(1);
        }

        String[] pathArray = split(pathToUse, FOLDER_SEPARATOR);
        List<String> pathElements = new LinkedList<>();
        int tops = 0;

        for (int i = pathArray.length - 1; i >= 0; i--) {
            String element = pathArray[i];
            if (CURRENT_PATH.equals(element)) {
                // Points to current directory - drop it.
            } else if (TOP_PATH.equals(element)) {
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
            pathElements.add(0, TOP_PATH);
        }

        return prefix + join(pathElements, FOLDER_SEPARATOR);
    }

    /**
     * 集合拼接为字符串<p>
     * join(Lists.newArrayList("a","b","c"), ",", "(", ")") -> (a),(b),(c)
     * @param coll     集合对象
     * @param delim    分隔符
     * @param prefix   每个元素添加的前缀
     * @param suffix   每个元素添加的后缀
     * @return
     */
    public static String join(Collection<?> coll, String delim, String prefix, String suffix) {
        if (coll == null || coll.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<?> it = coll.iterator();
        while (it.hasNext()) {
            sb.append(prefix).append(it.next()).append(suffix);
            if (it.hasNext()) {
                sb.append(delim);
            }
        }
        return sb.toString();
    }

    /**
     * 集合拼接为字符串
     * @param coll    集合对象
     * @param delim   分隔符
     * @return
     */
    public static String join(Collection<?> coll, String delim) {
        return join(coll, delim, "", "");
    }

    /**
     * 字符串替换<p>
     * replace("hello world", "o", "-")  ->  hell- w-rld
     * @param inString
     * @param oldPattern
     * @param newPattern
     * @return
     */
    public static String replace(String inString, String oldPattern, String newPattern) {
        if (StringUtils.isEmpty(inString) || StringUtils.isEmpty(oldPattern) 
            || newPattern == null) {
            return inString;
        }
        StringBuilder sb = new StringBuilder();
        int pos = 0; // our position in the old string
        int index = inString.indexOf(oldPattern);
        // the index of an occurrence we've found, or -1
        int patLen = oldPattern.length();
        while (index >= 0) {
            sb.append(inString, pos, index);
            sb.append(newPattern);
            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }
        sb.append(inString.substring(pos));
        // remember to append any characters to the right of a match
        return sb.toString();
    }


    /** 
     * 驼峰转换为下划线 
     * CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, camelCaseName);
     * @param camelCaseName 驼峰名
     * @return the underscore name
     * @see CaseFormat#to(CaseFormat, String)
     * @deprecated instead of CaseFormat#to(CaseFormat, String)
     */
    @Deprecated
    public static String underscoreName(String camelCaseName) {
        if (StringUtils.isEmpty(camelCaseName)) {
            return camelCaseName;
        }

        StringBuilder result = new StringBuilder(camelCaseName.length() << 1);
        result.append(Character.toLowerCase(camelCaseName.charAt(0)));
        for (int i = 1, len = camelCaseName.length(); i < len; i++) {
            char ch = camelCaseName.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append('_').append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /** 
     * 下划线转换为驼峰 
     * CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, underscoreName);
     * 1  LOWER_HYPHEN       连字符的变量命名规范如lower-hyphen
     * 2  LOWER_UNDERSCORE   c++变量命名规范如lower_underscore
     * 3  LOWER_CAMEL        java变量命名规范如lowerCamel
     * 4  UPPER_CAMEL        java和c++类的命名规范如UpperCamel
     * 5  UPPER_UNDERSCORE   java和c++常量的命名规范如UPPER_UNDERSCORE
     * @param underscoreName 下划线名
     * @return the camel case name
     * @see CaseFormat#to(CaseFormat, String)
     * @deprecated instead of CaseFormat#to(CaseFormat, String)
     */
    @Deprecated
    public static String camelCaseName(String underscoreName) {
        if (StringUtils.isEmpty(underscoreName)) {
            return underscoreName;
        }

        StringBuilder result = new StringBuilder(underscoreName.length());
        for (int i = 0, len = underscoreName.length(); i < len; i++) {
            char ch = underscoreName.charAt(i);
            if ('_' == ch) {
                if (++i < len) {
                    result.append(Character.toUpperCase(underscoreName.charAt(i)));
                }
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * 获取IEME校验码
     * @param str
     * @return  校验码
     */
    public static int iemeCode(String str) {
        int checkSum = 0;
        for (int i = 0, len = str.length(), num; i < len; i++) {
            num = str.charAt(i) - '0'; // ascii to num  
            if ((i & 0x01) == 0) {
                checkSum += num; // 1、将奇数位数字相加（从1开始计数）
            } else {
                num *= 2; // 2、将偶数位数字分别乘以2，分别计算个位数和十位数之和（从1开始计数）
                if (num < 10) {
                    checkSum += num;
                } else {
                    checkSum += num - 9;
                }
            }
        }

        return (10 - checkSum % 10) % 10;
    }

    /**
     * 判断是否为空字符串
     * @param value
     * @return
     */
    public static boolean isEmpty(Object value) {
        return value == null
            || (CharSequence.class.isInstance(value) && StringUtils.isEmpty((CharSequence) value));
    }

    public static boolean isBlank(Object value) {
        return value == null
            || (CharSequence.class.isInstance(value) && StringUtils.isBlank((CharSequence) value));
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

    /**
     * <p>Escapes the characters in a <code>String</code> to be suitable to pass to
     * an SQL query.</p>
     *
     * <p>For example,
     *  statement.executeQuery("SELECT * FROM MOVIES WHERE TITLE='" + 
     *  StringEscapeUtils.escapeSql("McHale's Navy") +  "'");
     * </p>
     *
     * <p>At present, this method only turns single-quotes into doubled single-quotes
     * (<code>"McHale's Navy"</code> => <code>"McHale''s Navy"</code>). It does not
     * handle the cases of percent (%) or underscore (_) for use in LIKE clauses.</p>
     *
     * see http://www.jguru.com/faq/view.jsp?EID=8881
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
    * Parse a CSV string using {@link #csvSplit(List,String, int, int)}
    * use in {@link code.ponfee.commons.web.CrossOriginFilter)
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
        return list.toArray(new String[list.size()]);
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
        }

        return list;
    }

}
