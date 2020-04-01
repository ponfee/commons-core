package code.ponfee.commons.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SqlUtilsTest {

    @Test
    public void trimSql() {
        assertEquals("",                         SqlUtils.trim("         ; ; ; ;;   "));
        assertEquals("select * from t lImit 10", SqlUtils.trim("  select * from t lImit 10"));
        assertEquals("select * from t lImit 10", SqlUtils.trim("select * from t lImit 10    "));
        assertEquals("select * from t lImit 10", SqlUtils.trim("select * from t lImit 10;    "));
        assertEquals("select * from t lImit 10", SqlUtils.trim("select * from t lImit 10 ;"));
        assertEquals("select * from t lImit 10", SqlUtils.trim("select * from t lImit 10    ;; ; ;; ;  "));
    }

    @Test
    public void limitMsql() {
        assertEquals("select * from t lImit 10",       SqlUtils.limitMysql("select * from t lImit 10", 100));
        assertEquals("select * from t lImit 1000,10",  SqlUtils.limitMysql("select * from t lImit 1000,10", 100));
        assertEquals("select * from t LIMIT 100",      SqlUtils.limitMysql("select * from t lImit 118", 100));
        assertEquals("select * from t LIMIT 1951,100", SqlUtils.limitMysql("select * from t liMiT 1951 , 210", 100));
        assertEquals("select * from t  LIMIT 100",     SqlUtils.limitMysql("select * from t ", 100));
    }

    @Test
    public void limitPgsql() {
        assertEquals("select * from t lImit 12 ",            SqlUtils.limitPgsql("select * from t lImit 12 ", 100));
        assertEquals("select * from t LIMIT 11 OFFSET 9999 ",SqlUtils.limitPgsql("select * from t LIMIT 11 OFFSET 9999 ", 100));
        assertEquals("select * from t LIMIT 100",            SqlUtils.limitPgsql("select * from t lImit 118", 100));
        assertEquals("select * from t LIMIT 100 OFFSET 210", SqlUtils.limitPgsql("select * from t liMiT 1951 ofFseT  210", 100));
        assertEquals("select * from t  LIMIT 100",           SqlUtils.limitPgsql("select * from t ", 100));
    }

    @Test
    public void limitOracle() {
        assertEquals("select * from t WHERE  ROWNUM<100",                                   SqlUtils.limitOracle("select * from t", 100));
        assertEquals("select * from where rownum<=10",                                      SqlUtils.limitOracle("select * from where rownum<=10", 100));
        assertEquals("select * from where rownum<=10 and  1=1",                             SqlUtils.limitOracle("select * from where rownum<=10 and  1=1", 100));
        assertEquals("select * from where   1=1 aNd rownum<=10",                            SqlUtils.limitOracle("select * from where   1=1 aNd rownum<=10", 100));
        assertEquals("select * from WHERE  ROWNUM<100",                                     SqlUtils.limitOracle("select * from rownum<1000", 100));
        assertEquals("select * from WHERE  ROWNUM<100 AND 1=1",                             SqlUtils.limitOracle("select * from rownum<=999 AND 1=1", 100));
        assertEquals("select * from where ROWNUM<100",                                      SqlUtils.limitOracle("select * from where rownum<=12346", 100));
        assertEquals("select * from (select * from where 1=1) t WHERE  ROWNUM<100",         SqlUtils.limitOracle("select * from (select * from where 1=1) t", 100));
        assertEquals("select * from (select * from where 1=1) t where 1=1 AND  ROWNUM<100", SqlUtils.limitOracle("select * from (select * from where 1=1) t where 1=1", 100));
        assertEquals("select * from (select * from where 1=1) t where 1=1 AND  ROWNUM<100 AND 2=2", SqlUtils.limitOracle("select * from (select * from where 1=1) t where 1=1 AND  rownuM<1000 AND 2=2", 100));
    }

    @Test
    public void limitSqlServer() {
        assertEquals("select TOP 100 * from t ",                          SqlUtils.limitMssql("select * from t ", 100));
        assertEquals("select TOP 100 * from t ",                          SqlUtils.limitMssql("select TOP 1000 * from t ", 100));
        assertEquals("select * from (select TOP 100 * from a) b",         SqlUtils.limitMssql("select * from (select TOP 1000 * from a) b", 100));
        assertEquals("select TOP 100 * from (select TOP 1000 * from a) b",SqlUtils.limitMssql("select TOP 101 * from (select TOP 1000 * from a) b", 100));
    }

    @Test
    public void limitHive() {
        assertEquals("select * from t lImit 10",                   SqlUtils.limitHive("select * from t lImit 10", 100));
        assertEquals("select * from t LIMIT 100",                  SqlUtils.limitHive("select * from t lImit 118", 100));
        assertEquals("select * from t  LIMIT 100",                 SqlUtils.limitHive("select * from t ", 100));
        assertEquals("select * from t lImit 1000,10 LIMIT 100",    SqlUtils.limitHive("select * from t lImit 1000,10", 100));
        assertEquals("select * from t liMiT 1951 , 210 LIMIT 100", SqlUtils.limitHive("select * from t liMiT 1951 , 210", 100));
    }

}
