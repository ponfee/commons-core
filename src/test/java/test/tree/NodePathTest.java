/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package test.tree;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.junit.Assert;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.tree.NodePath;
import code.ponfee.commons.tree.NodePath.NodePathFastjsonDeserializer;

/**
 * 
 * 
 * @author Ponfee
 */
public class NodePathTest {

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
    public void testa() {
        System.out.println("\n\n========================test3");
        List<Integer> list = Lists.newArrayList(1, 2, 3, 4);
        String json = Jsons.toJson(list);
        System.out.println(json);
        System.out.println(Jsons.fromJson(json, List.class).get(0).getClass());
        System.out.println(JSON.parseArray(json).get(0).getClass());
    }

    @Test
    public void testb() {
        System.out.println("\n\n========================test4");
        NodePath<Integer> ids = new NodePath<>(1, 2, 3, 4);
        String json = Jsons.toJson(ids);
        System.out.println(json);
        System.out.println(Jsons.fromJson(json, NodePath.class).get(0).getClass());
        System.out.println(JSON.parseObject("[\"a\",\"b\"]", NodePath.class).get(0).getClass());
    }
    
    @Test
    public void test4() {
        String json = Jsons.toJson(new NodePath<>(1, 2, 3, 4));
        System.out.println(Jsons.fromJson(json, NodePath.class));
    }

    @Test
    public void test5() {
        String json = Jsons.toJson(new NodePath<Integer>(1, 2, 3, 4));
        System.out.println(Jsons.fromJson(json, NodePath.class));
    }

    @Test
    public void test6() {
        String json = JSON.toJSONString(new NodePath<>(1, 2, 3, 4));
        System.out.println(JSON.parseObject(json, NodePath.class));
    }
    @Test
    public void test7() {
        String json = JSON.toJSONString(new NodePath<Integer>(1, 2, 3, 4));
        System.out.println(JSON.parseObject(json, NodePath.class));
    }

    // --------------------------------------------------------------
    private static String DATA = "{\"id\":123,\"path\":[1,2,3,4]}";
    @Test
    public void test91() {
        System.out.println(Jsons.fromJson(DATA, NodePathBean1.class).getPath());
        System.out.println(JSON.parseObject(DATA, NodePathBean1.class).getPath());
    }
    @Test
    public void test92() {
        System.out.println(Jsons.fromJson(DATA, NodePathBean2.class).getPath());
        System.out.println(JSON.parseObject(DATA, NodePathBean2.class).getPath());
    }
    @Test
    public void test93() {
        System.out.println(Jsons.fromJson(DATA, NodePathBean3.class).getPath());
        System.out.println(JSON.parseObject(DATA, NodePathBean3.class).getPath());
    }
    @Test
    public void test94() {
        // ERROR
        System.out.println(Jsons.fromJson(DATA, NodePathBean4.class).getPath());
        System.out.println(JSON.parseObject(DATA, NodePathBean4.class).getPath());
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
        @JSONField(deserializeUsing=NodePathFastjsonDeserializer.class)
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

}
