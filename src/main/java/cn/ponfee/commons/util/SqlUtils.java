/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        //sql = sql.replaceAll("\\s{2,}", " ");
        int start = 0, end = (sql.length() - 1), n = end;

        while (start < n && isBlank(sql.charAt(start))) {
            start++;
        }

        if (start == n) {
            return "";
        }

        while (end > start && isBlankOrSemicolon(sql.charAt(end))) {
            end--;
        }

        return start == end ? "" : sql.substring(start, end + 1);
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
        // .+? / X*?：非贪婪模式/懒汉模式；
        // (?i)：忽略大小写；
        // (?s)：所在位置右侧的表达式开启单行模式；
        // (?m)：所在位置右侧的表达式开启多行模式；
        "^(.+?)(\\s+(?i)TOP\\s+(\\d+)\\s+)(.+)$"
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

    // --------------------------------------------------------------limit hive
    private static final Pattern LIMIT_HIVE = Pattern.compile(
        "^(.+)(\\s+(?i)LIMIT\\s+(\\d+)?\\s*)$"/*, Pattern.CASE_INSENSITIVE*/
    );
    public static String limitHive(String sql, int limit) {
        String outermostSql = outermostSql(sql);
        Matcher matcher = LIMIT_HIVE.matcher(outermostSql);
        if (!matcher.matches()) {
            return sql + " LIMIT " + limit;
        }

        String a = matcher.group(3);
        if (Integer.parseInt(a) <= limit) {
            return sql;
        }


        return sql.substring(0, sql.length() - outermostSql.length()) 
             + matcher.group(1) + " LIMIT " + limit;
    }

    // --------------------------------------------------------------private methods
    private static boolean isBlank(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';
    }

    private static boolean isBlankOrSemicolon(char ch) {
        return isBlank(ch) || ch == ';';
    }

    private static String outermostSql(String sql) {
        if (StringUtils.isEmpty(sql)) {
            return "";
        }

        int pos = sql.lastIndexOf(')'); // extract outermost sql string(include ")")
        return (pos == -1) ? sql : sql.substring(pos);
    }

    private static String completeWhere(String outermost) {
        outermost = outermost.trim().toUpperCase();
        if (outermost.endsWith(" WHERE") || outermost.endsWith(" AND")) {
            return "";
        }

        return outermost.contains(" WHERE ") ? " AND " : " WHERE ";
    }

}
