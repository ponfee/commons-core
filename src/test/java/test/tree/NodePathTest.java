package test.tree;

import cn.ponfee.commons.base.tuple.Tuple2;
import cn.ponfee.commons.collect.ImmutableArrayList;
import cn.ponfee.commons.json.Jsons;
import cn.ponfee.commons.tree.NodePath;
import cn.ponfee.commons.tree.NodePath.FastjsonDeserializer;
import cn.ponfee.commons.util.Asserts;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ponfee
 */
public class NodePathTest {

    @Test
    public void test0() {
        System.out.println(ImmutableList.of(new Integer[]{1,2}).get(0).getClass());
        System.out.println(ImmutableArrayList.of(new Integer[]{1,2}).get(0).getClass());


        for (Object o : Tuple2.of(1, 2)) {
            System.out.println(o);
        }

        System.out.println();
        System.out.println(new NodePath<>(1, 2, 3, 4));
        System.out.println(new NodePath<>(new Integer[]{1, 2, 3, 4}, 5));

        System.out.println(new NodePath<>(Arrays.asList(1, 2, 3)));
        System.out.println(new NodePath<>(Arrays.asList(1, 2, 3), 4));

        NodePath<Integer> ids = new NodePath<>(1, 2, 3, 4);
        System.out.println(new NodePath<>(ids));
        System.out.println(new NodePath<>(ids, 1));
    }

    @Test
    public void test1() {
        System.out.println(CollectionUtils.isEqualCollection(Arrays.asList(1, 2, 3), Arrays.asList(3, 2, 1)));
        System.out.println(ListUtils.isEqualList(Arrays.asList(1, 2, 3), Arrays.asList(3, 2, 1)));
        System.out.println(ListUtils.isEqualList(Arrays.asList(1, 2, 3), Arrays.asList(1, 2, 3)));
    }

    @Test
    public void test2() {
        NodePath<Integer> p1 = new NodePath<>(1, 2, 3, 4);
        NodePath<Integer> p2 = new NodePath<>(1, 2, 3, 4);
        System.out.println(p1.equals(p2));
        System.out.println(p1.compareTo(p2));

        System.out.println("\n\n=========================");
        p1 = new NodePath<>(1, 2, 3, 4);
        p2 = new NodePath<>(1, 2);
        System.out.println(p1.equals(p2));
        System.out.println(p1.compareTo(p2));

        System.out.println("\n\n=========================");
        p1 = new NodePath<>(1, 2, 3, 4);
        p2 = new NodePath<>(4, 2);
        System.out.println(p1.equals(p2));
        System.out.println(p1.compareTo(p2));
    }

    @Test
    public void test3() {
        Map<NodePath<Integer>, Object> map = new HashMap<>();
        map.put(new NodePath<>(1, 2, 3, 4), "xx");
        Assert.assertNotNull(map.get(new NodePath<>(1, 2, 3, 4)));
        Assert.assertNull(map.get(new NodePath<>(1, 2, 3)));
    }


    @Test
    public void test4() {
        System.out.println("\n\n========================test4");
        List<Integer> list = Lists.newArrayList(1, 2, 3, 4);
        String json = Jsons.toJson(list);
        System.out.println(json);
        System.out.println(Jsons.fromJson(json, List.class).get(0).getClass());
        System.out.println(JSON.parseArray(json).get(0).getClass());
    }

    @Test
    public void test5() {
        System.out.println("\n\n========================test5");
        NodePath<Integer> ids = new NodePath<>(1, 2, 3, 4);
        String json = Jsons.toJson(ids);
        System.out.println(json);
        Asserts.isTrue(json.equals(JSON.toJSONString(ids)));
        System.out.println(Jsons.fromJson(json, NodePath.class).get(0).getClass());
        System.out.println(JSON.parseObject(json, NodePath.class).get(0).getClass());
        System.out.println(JSON.parseObject("[\"a\",\"b\"]", NodePath.class).get(0).getClass());
    }

    // --------------------------------------------------------------
    private static final String DATA = "{\"id\":123,\"path\":[1,2,3,4]}";

    @Test
    public void test6() {
        System.out.println(Jsons.fromJson(DATA, NodePathBean1.class).getPath());
        System.out.println(JSON.parseObject(DATA, NodePathBean1.class).getPath());
    }

    @Test
    public void test7() {
        System.out.println(Jsons.fromJson(DATA, NodePathBean2.class).getPath());
        System.out.println(JSON.parseObject(DATA, NodePathBean2.class).getPath());
    }

    @Test
    public void test8() {
        System.out.println(Jsons.fromJson(DATA, NodePathBean3.class).getPath());
        System.out.println(JSON.parseObject(DATA, NodePathBean3.class).getPath());
    }

    // -----------------------------------------------------------ERROR
    @Test
    public void test10() {
        System.out.println(Jsons.fromJson(DATA, NodePathBean5.class).getPath().getClass()); // class java.util.ArrayList
        System.out.println(JSON.parseObject(DATA, NodePathBean5.class).getPath().getClass()); // class java.util.ArrayList
    }

    @Test
    public void test11() {
        System.out.println(Jsons.fromJson(DATA, NodePathBean6.class).getPath());
        System.out.println(JSON.parseObject(DATA, NodePathBean6.class).getPath());
    }

    @Test @Ignore
    public void test12() {
        System.out.println(Jsons.fromJson(DATA, NodePathBean4.class).getPath());
        System.out.println(JSON.parseObject(DATA, NodePathBean4.class).getPath()); // add method UnsupportedOperationException
    }

    /**
     * add method UnsupportedOperationException
     */
    @Test @Ignore
    public void test13() {
        System.out.println(Jsons.fromJson(DATA, NodePathBean7.class).getPath());
        System.out.println(JSON.parseObject(DATA, NodePathBean7.class).getPath());
    }

    /**
     * ERROR non-concrete Collection
     */
    @Test @Ignore
    public void test14() {
        System.out.println(ImmutableList.of().getClass());
        System.out.println(ImmutableList.of(1).getClass());
        System.out.println(ImmutableList.of(1,2).getClass());
        System.out.println(Jsons.fromJson("[1,2,3,4]", ImmutableList.class));
        System.out.println(JSON.parseObject("[1,2,3,4]", ImmutableList.class));
    }

    @SuppressWarnings("rawtypes")
    public static class NodePathBean1 implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private int id;
        private NodePath path;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public NodePath getPath() {
            return path;
        }

        public void setPath(NodePath path) {
            this.path = path;
        }
    }

    public static class NodePathBean2<T extends Serializable & Comparable<? super T>> implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private int id;
        private NodePath<T> path; // 

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public NodePath<T> getPath() {
            return path;
        }

        public void setPath(NodePath<T> path) {
            this.path = path;
        }
    }

    public static class NodePathBean3 implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private int id;
        @JSONField(deserializeUsing = FastjsonDeserializer.class)
        private NodePath<Integer> path;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public NodePath<Integer> getPath() {
            return path;
        }

        public void setPath(NodePath<Integer> path) {
            this.path = path;
        }
    }

    public static class NodePathBean4 implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private int id;
        // FIXME ERROR
        // 当字段有泛型参数时的类型信息type为ParameterizedType，所以必须用JSONField注解，
        // 否则当成Collection来解析（此字段的类型不是NodePath，而是ParameterizedType）
        private NodePath<Integer> path;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public NodePath<Integer> getPath() {
            return path;
        }

        public void setPath(NodePath<Integer> path) {
            this.path = path;
        }
    }

    public static class NodePathBean5 implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private int id;
        private List<Integer> path;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public List<Integer> getPath() {
            return path;
        }

        public void setPath(List<Integer> path) {
            this.path = path;
        }
    }

    public static class NodePathBean6 implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private int id;
        private ArrayList<Integer> path;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public ArrayList<Integer> getPath() {
            return path;
        }

        public void setPath(ArrayList<Integer> path) {
            this.path = path;
        }
    }

    public static class NodePathBean7 implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private int id;
        private ImmutableArrayList<Integer> path;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public ImmutableArrayList<Integer> getPath() {
            return path;
        }

        public void setPath(ImmutableArrayList<Integer> path) {
            this.path = path;
        }
    }

}
