package cn.ponfee.commons.model;

import org.junit.Test;

/**
 * 
 * 
 * @author Ponfee
 */
public class ParamsTest {

    @Test
    public void test1() {
        PageParameter params = new PageParameter();
        params.setSort("name,   test   asc");
        params.validateSort("name", "test");
    }
}
