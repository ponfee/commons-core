package code.ponfee.commons.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.util.Strings;

/**
 * Sql utility
 * 
 * @author Ponfee
 */
public final class SqlUtils {

    public static String trim(String sql) {
        if (StringUtils.isEmpty(sql)) {
            return sql;
        }

        int start = 0, n = sql.length() - 1, end = n;

        for (; start < n; start++) {
            char ch = sql.charAt(start);
            if (isNotBlank(ch)) {
                break;
            }
        }

        if (start >= n) {
            return "";
        }

        for (; end > start; end--) {
            char ch = sql.charAt(end);
            if (isNotBlank(ch) && ch != ';') {
                break;
            }
        }
        return start == end ? "" : (start == 0 && end == n) ? sql : sql.substring(start, end + 1);
    }

    // --------------------------------------------------------------limit mysql
    private static final Pattern LIMIT_MYSQL = Pattern.compile(
        "^(.+)(\\s+(?i)LIMIT\\s+(\\d+)(\\s*,\\s*(\\d+))?\\s*)$"/*, Pattern.CASE_INSENSITIVE*/
    );
    public static String limitMysql(String sql, int limit) {
        String outermostSql = outermostSql(sql);
        Matcher matcher = LIMIT_MYSQL.matcher(outermostSql);
        if (!matcher.matches()) {
            return sql + " LIMIT " + limit;
        }

        // (LIMIT a) OR (LIMIT a, b)
        String a = matcher.group(3), b = matcher.group(5);
        if (Integer.parseInt(Optional.ofNullable(b).orElse(a)) <= limit) {
            return sql;
        }

        String replace = b == null ? Integer.toString(limit) : a + "," + limit;

        return sql.substring(0, sql.length() - outermostSql.length()) 
             + matcher.group(1) + " LIMIT " + replace;
    }

    // --------------------------------------------------------------limit pgsql
    private static final Pattern LIMIT_PGSQL = Pattern.compile(
       "^(.+)(?i)(\\s+LIMIT\\s+(\\d+)(\\s+OFFSET\\s+(\\d+))?\\s*)$"
    );
    public static String limitPgsql(String sql, int limit) {
        String outermostSql = outermostSql(sql);
        Matcher matcher = LIMIT_PGSQL.matcher(outermostSql);
        if (!matcher.matches()) {
            return sql + " LIMIT " + limit;
        }

        // (LIMIT a) OR (LIMIT a OFFSET b)
        String a = matcher.group(3), b = matcher.group(5);
        if (Integer.parseInt(a) <= limit) {
            return sql;
        }

        String limitStr = limit + (b == null ? "" : " OFFSET " + b);

        return sql.substring(0, sql.length() - outermostSql.length()) 
             + matcher.group(1) + " LIMIT " + limitStr;
    }

    // --------------------------------------------------------------limit oracle
    // Oracle不支持ROWNUM>(=)number语法：SELECT * FROM t_table_name WHERE ROWNUM>2 AND ROWNUM<=4
    // 因为第一条数据行号为1，不符合>2的条件所以第一行被去掉，之前的第二行又变为新的第一行，如此下去到最后一条数据也查不出来
    // 所以此处正则表达式不考虑“ROWNUM>(=)number”的情况（因为正常oracle sql不会这么写）
    private static final Pattern LIMIT_ORACLE = Pattern.compile(
        "^(.+)(\\s+(?i)ROWNUM\\s*<=?\\s*(\\d+))(\\s+(?i)AND\\s+.+|\\s*)$"
    );
    public static String limitOracle(String sql, int limit) {
        String outermostSql = outermostSql(sql);
        Matcher matcher = LIMIT_ORACLE.matcher(outermostSql);
        if (!matcher.matches()) {
            return sql + completeWhere(outermostSql) + " ROWNUM<" + limit;
        }

        // ROWNUM<limit
        String a = matcher.group(3);
        if (Integer.parseInt(a) <= limit) {
            return sql;
        }

        String mid = matcher.group(1), tail = matcher.group(4);
        return sql.substring(0, sql.length() - outermostSql.length()) 
             + mid + completeWhere(mid) + " ROWNUM<" + limit + Strings.ifEmpty(tail, "");
    }

    // --------------------------------------------------------------limit microsoft sql(SQL Server)
    private static final Pattern LIMIT_SQLSERVER = Pattern.compile(
        "^(.+?)(\\s+(?i)TOP\\s+(\\d+)\\s+)(.+)$" // “.+?”非贪婪模式
    );
    private static final Pattern SELECT_SQLSERVER = Pattern.compile(
        "^(\\s*(?i)SELECT\\s)(.+)$"
    );
    public static String limitMssql(String sql, int limit) {
        Matcher matcher = LIMIT_SQLSERVER.matcher(sql);
        if (!matcher.matches()) {
            matcher = SELECT_SQLSERVER.matcher(sql);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid select sql: " + sql);
            }
            return matcher.group(1) + "TOP " + limit + " " + matcher.group(2);
        }

        String top = matcher.group(3);
        if (Integer.parseInt(top) <= limit) {
            return sql;
        }

        return matcher.group(1) + " TOP " + limit + " " + matcher.group(4);
    }

    // --------------------------------------------------------------private methods
    private static boolean isNotBlank(char ch) {
        return !isBlank(ch);
    }

    private static boolean isBlank(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';
    }

    private static String outermostSql(String sql) {
        if (StringUtils.isEmpty(sql)) {
            return "";
        }

        int pos = sql.lastIndexOf(')'); // extract outermost sql string(include ")")
        return (pos == -1) ? sql : sql.substring(pos, sql.length());
    }

    private static String completeWhere(String outermost) {
        outermost = outermost.trim().toUpperCase();
        if (outermost.endsWith(" WHERE") || outermost.endsWith(" AND")) {
            return "";
        }

        return outermost.contains(" WHERE ") ? " AND " : " WHERE ";
    }

}
