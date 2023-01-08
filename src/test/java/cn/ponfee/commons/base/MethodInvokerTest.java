package cn.ponfee.commons.base;

import com.alibaba.druid.pool.DruidDataSource;

import cn.ponfee.commons.util.ObjectUtils;

public class MethodInvokerTest {

    public static void main(String[] args) {
        DruidDataSource ds = new DruidDataSource();
        try {
            Releasable.release(ds);
        } catch (Exception e) {
        }
        try {
            Initializable.init(ds);
        } catch (Exception e) {
        }
        
        System.out.println("\n\n\n\n==============================");
        try {
            System.out.println(Releasable.class.getMethod("release"));
        } catch (Exception e) {
        }
        
        try {
            System.out.println(ObjectUtils.class.getMethod("uuid"));
        } catch (Exception e) {
        }
        
        try {
            System.out.println(MethodInvokerTest.class.getMethod("toStr"));
        } catch (Exception e) {
        }
        
        try {
            System.out.println(MethodInvokerTest.class.getMethod("toString"));
        } catch (Exception e) {
        }
    }
    
    protected String toStr() {
        return "";
    }
}
