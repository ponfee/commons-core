package cn.ponfee.commons.json;

import cn.ponfee.commons.model.Result;
import com.alibaba.fastjson.JSON;

/**
 * @author Ponfee
 */
public class BooleanPojoTest {
    
    private boolean success1;
    private boolean isSuccess2;
    private Boolean success3;
    private Boolean isSuccess4;

    public boolean isSuccess1() {
        return success1;
    }

    public void setSuccess1(boolean success1) {
        this.success1 = success1;
    }

    public boolean isSuccess2() {
        return isSuccess2;
    }

    public void setSuccess2(boolean success2) {
        isSuccess2 = success2;
    }

    public Boolean getSuccess3() {
        return success3;
    }

    public void setSuccess3(Boolean success3) {
        this.success3 = success3;
    }

    public Boolean getSuccess4() {
        return isSuccess4;
    }

    public void setSuccess4(Boolean success4) {
        isSuccess4 = success4;
    }

    public static void main(String[] args) {
        // 一空
        BooleanPojoTest pojo1 = new BooleanPojoTest();
        pojo1.success1 = true;
        pojo1.success3 = false;
        System.out.println(Jsons.toJson(pojo1));
        System.out.println(JSON.toJSONString(pojo1));

        System.out.println("\n-------------");
        BooleanPojoTest pojo2 = new BooleanPojoTest();
        pojo2.success1 = false;
        pojo2.isSuccess4 = false;
        System.out.println(Jsons.toJson(pojo2));
        System.out.println(JSON.toJSONString(pojo2));

        System.out.println("\n-------------");
        BooleanPojoTest pojo3 = new BooleanPojoTest();
        pojo3.success1 = true;
        System.out.println(Jsons.toJson(pojo3));
        System.out.println(JSON.toJSONString(pojo3));

        System.out.println("\n-------------");
        BooleanPojoTest pojo4 = new BooleanPojoTest();
        pojo4.success1 = true;
        pojo4.success3 = false;
        pojo4.isSuccess4 = false;
        System.out.println(Jsons.toJson(pojo4));
        System.out.println(JSON.toJSONString(pojo4));

        System.out.println("\n-------------");
        System.out.println(Jsons.toJson(Result.success()));
        System.out.println(JSON.toJSONString(Result.success()));
    }
}
