package code.ponfee.commons.mybatis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import code.ponfee.commons.data.DataSourceNaming;

public class SQLMapperTest {

    @DataSourceNaming("'primary'")
    public void selectScroll() {
        String sql = 
            "<script>"
          + "SELECT row_id id, code_attributes->'$.scanCount' AS cnt "
          + "FROM trace_code "
          + "WHERE "
          + "code_attributes->'$.scanCount' IS NOT NULL "
          + "<if test='id!=null'>AND row_id>#{id}</if> "
          + "ORDER BY row_id ASC "
          + "LIMIT 100"
          + "</script>";
        AtomicInteger count = new AtomicInteger(0);
        new SqlMapper(null).selectScroll(sql, new HashMap<>(), Map.class, (param, list) -> {
            param.put("id", list.get(list.size() - 1).get("id"));
            count.addAndGet(list.size());
            return param;
        });
        System.out.println("================="+count.get());
    }
}
