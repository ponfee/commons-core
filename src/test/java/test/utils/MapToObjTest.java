package test.utils;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.jce.DigestAlgorithms;
import code.ponfee.commons.util.ObjectUtils;

public class MapToObjTest {

    public static class A {
        private int a_b;
        private String str;
        private DigestAlgorithms mode;

        public A() {}

        public A(int a_b, String str) {
            super();
            this.a_b = a_b;
            this.str = str;
        }

        public int getA_b() {
            return a_b;
        }

        public void setA_b(int a_b) {
            this.a_b = a_b;
        }

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }

        public DigestAlgorithms getMode() {
            return mode;
        }

        public void setMode(DigestAlgorithms mode) {
            this.mode = mode;
        }

        @Override
        public String toString() {
            return "A [a_b=" + a_b + ", str=" + str + ", mode=" +( mode == null ? "null" : mode.name()) + "]";
        }

    }

    public static void main(String[] args) {
        System.out.println(CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, "test-data"));//testData
        System.out.println(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, "test_data"));//testData
        System.out.println(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, "test_data"));//TestData

        System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "testdata"));//testdata
        System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "TestData"));//test_data
        System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "testData"));//test_data
        System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, "testData"));//test-data

        A a = new A(1, "aaa");
        //Map<String, Object> map = ObjectUtils.bean2map(a);
        //System.out.println(map);

        //a = ObjectUtils.map2bean(map, A.class);
        //System.out.println(a);

        a = ObjectUtils.map2bean(ImmutableMap.of("aB", 123, "str", "abc", "mode", "RipeMD128"), A.class);
        System.out.println(a);
        System.out.println(ObjectUtils.bean2map(a));
    }

}
