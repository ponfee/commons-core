package code.ponfee.commons.data;

import code.ponfee.commons.schema.DataStructure;
import code.ponfee.commons.schema.json.JSONExtractUtils;
import code.ponfee.commons.schema.json.JSONId;
import code.ponfee.commons.schema.json.JSONTree;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.model.Null;
import code.ponfee.commons.tree.TreeNode;
import code.ponfee.commons.util.Asserts;
import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.text.ParseException;

public class JSONExtractUtilsTest {

    @Test
    public void testFormat() {
        System.out.println(String.format("%02d", 1));
        System.out.println(String.format("%01d", 10));
        System.out.println(String.format("%01d", 1));
        System.out.println(String.format("%02d", 10));
        System.out.println(String.format("|% 3d|", 1));
        System.out.println(String.format("|% 3d|", 10));
    }

    @Test
    public void parseJson() {
        System.out.println(JSON.parse("123").getClass()); // class java.lang.Integer
        //System.out.println(JSON.parseObject("123").getClass()); // 【error】

        System.out.println(JSON.parse("[1,2,3]").getClass()); // class com.alibaba.fastjson.JSONArray
        //System.out.println(JSON.parseObject("[1,2,3]").getClass()); // 【error】

        System.out.println(JSON.parse("{}").getClass()); // class com.alibaba.fastjson.JSONObject
        System.out.println(JSON.parseObject("{}").getClass()); // class com.alibaba.fastjson.JSONObject
    }

    // -------------------------------------------------------
    @Test
    public void extractObjectArray() throws ParseException {
        String text = "[{\"name\":\"Alice\",\"age\":18,\"gender\":\"F\"},{\"name\":\"Bob\",\"age\":20,\"gender\":\"M\"},{\"name\":\"Tom\",\"age\":30,\"gender\":\"M\"}]";
        System.out.println("Origin data: " + text);

        TreeNode<JSONId, Null> treeNode = JSONExtractUtils.extractSchema(text);
        JSONTree schemaObj = (treeNode == null) ? null : treeNode.convert(JSONTree::convert);

        String schemaText = JSON.toJSONString(schemaObj);
        System.out.println("Extracted Schema: " + schemaText);

        // 拷贝上面的schemaText进行更改部分checked为true（即选中某些想要的数据列）
        schemaText = "{\"checked\":true,\"children\":[{\"checked\":true,\"children\":[{\"checked\":false,\"name\":\"gender\",\"orders\":2,\"path\":[\"Root\",\"[{}]\",\"gender\"],\"type\":\"STRING\"},{\"checked\":true,\"name\":\"name\",\"orders\":3,\"path\":[\"Root\",\"[{}]\",\"name\"],\"type\":\"STRING\"},{\"checked\":true,\"name\":\"age\",\"orders\":4,\"path\":[\"Root\",\"[{}]\",\"age\"],\"type\":\"INTEGER\"}],\"name\":\"[{}]\",\"orders\":1,\"path\":[\"Root\",\"[{}]\"]}],\"name\":\"Root\",\"orders\":0,\"path\":[\"Root\"]}";
        System.out.println("After selected Some Column: " + schemaText);

        schemaObj = Jsons.fromJson(schemaText, JSONTree.class);
        Asserts.isTrue(schemaObj.equals(JSON.parseObject(schemaText, JSONTree.class)), "two json not equals");
        if (!JSONTree.hasChoose(schemaObj)) {
            throw new IllegalStateException("Not choose");
        }
        DataStructure extractedData = JSONExtractUtils.extractData(text, schemaObj);
        System.out.println("Table data: " + JSON.toJSONString(extractedData));
    }

    @Test
    public void extractDoubleArray() throws ParseException {
        String text = "[[\"a1\",\"b1\",11,21],[\"a2\",\"b2\",12,22],[\"a3\",\"b3\",13,23],[\"a4\",\"b4\",14,24]]";
        System.out.println("Origin data: " + text);

        TreeNode<JSONId, Null> treeNode = JSONExtractUtils.extractSchema(text);
        JSONTree schemaObj = (treeNode == null) ? null : treeNode.convert(JSONTree::convert);

        String schemaText = JSON.toJSONString(schemaObj);
        System.out.println("Extracted Schema: " + schemaText);

        // 拷贝上面的schemaText进行更改部分checked为true（即选中某些想要的数据列）
        schemaText = "{\"checked\":true,\"children\":[{\"checked\":true,\"children\":[{\"checked\":true,\"name\":\"[02]\",\"orders\":2,\"path\":[\"Root\",\"[[]]\",\"[02]\"],\"type\":\"STRING\"},{\"checked\":false,\"name\":\"[03]\",\"orders\":3,\"path\":[\"Root\",\"[[]]\",\"[03]\"],\"type\":\"STRING\"},{\"checked\":true,\"name\":\"[04]\",\"orders\":4,\"path\":[\"Root\",\"[[]]\",\"[04]\"],\"type\":\"INTEGER\"},{\"checked\":true,\"name\":\"[05]\",\"orders\":5,\"path\":[\"Root\",\"[[]]\",\"[05]\"],\"type\":\"INTEGER\"}],\"name\":\"[[]]\",\"orders\":1,\"path\":[\"Root\",\"[[]]\"]}],\"name\":\"Root\",\"orders\":0,\"path\":[\"Root\"]}";
        System.out.println("After selected Some Column: " + schemaText);

        schemaObj = Jsons.fromJson(schemaText, JSONTree.class);
        if (!JSONTree.hasChoose(schemaObj)) {
            throw new IllegalStateException("Not choose");
        }
        DataStructure extractedData = JSONExtractUtils.extractData(text, schemaObj);
        System.out.println("Table data: " + JSON.toJSONString(extractedData));
    }

    @Test
    public void extractBasicArray() throws ParseException {
        String text = "[\"a\",\"b\",1,2]";
        System.out.println("Origin data: " + text);

        TreeNode<JSONId, Null> treeNode = JSONExtractUtils.extractSchema(text);
        JSONTree schemaObj = (treeNode == null) ? null : treeNode.convert(JSONTree::convert);

        String schemaText = JSON.toJSONString(schemaObj);
        System.out.println("Extracted Schema: " + schemaText);

        // 拷贝上面的schemaText进行更改部分checked为true（即选中某些想要的数据列）
        schemaText = "{\"checked\":true,\"children\":[{\"checked\":true,\"children\":[{\"checked\":true,\"name\":\"[02]\",\"orders\":2,\"path\":[\"Root\",\"[()]\",\"[02]\"],\"type\":\"STRING\"},{\"checked\":false,\"name\":\"[03]\",\"orders\":3,\"path\":[\"Root\",\"[()]\",\"[03]\"],\"type\":\"STRING\"},{\"checked\":false,\"name\":\"[04]\",\"orders\":4,\"path\":[\"Root\",\"[()]\",\"[04]\"],\"type\":\"INTEGER\"},{\"checked\":true,\"name\":\"[05]\",\"orders\":5,\"path\":[\"Root\",\"[()]\",\"[05]\"],\"type\":\"INTEGER\"}],\"name\":\"[()]\",\"orders\":1,\"path\":[\"Root\",\"[()]\"]}],\"name\":\"Root\",\"orders\":0,\"path\":[\"Root\"]}";
        System.out.println("After selected Some Column: " + schemaText);

        schemaObj = Jsons.fromJson(schemaText, JSONTree.class);
        if (!JSONTree.hasChoose(schemaObj)) {
            throw new IllegalStateException("Not choose");
        }
        DataStructure extractedData = JSONExtractUtils.extractData(text, schemaObj);
        System.out.println("Table data: " + JSON.toJSONString(extractedData));
    }

    @Test
    public void extractObject() throws ParseException {
        String text = "{\"name\":\"Alice\",\"age\":18,\"gender\":\"F\"}";
        System.out.println("Origin data: " + text);

        TreeNode<JSONId, Null> treeNode = JSONExtractUtils.extractSchema(text);
        JSONTree schemaObj = (treeNode == null) ? null : treeNode.convert(JSONTree::convert);

        String schemaText = JSON.toJSONString(schemaObj);
        System.out.println("Extracted Schema: " + schemaText);

        // 拷贝上面的schemaText进行更改部分checked为true（即选中某些想要的数据列）
        schemaText = "{\"checked\":true,\"children\":[{\"checked\":true,\"name\":\"gender\",\"orders\":1,\"path\":[\"Root\",\"gender\"],\"type\":\"STRING\"},{\"checked\":false,\"name\":\"name\",\"orders\":2,\"path\":[\"Root\",\"name\"],\"type\":\"STRING\"},{\"checked\":true,\"name\":\"age\",\"orders\":3,\"path\":[\"Root\",\"age\"],\"type\":\"INTEGER\"}],\"name\":\"Root\",\"orders\":0,\"path\":[\"Root\"]}";
        System.out.println("After selected Some Column: " + schemaText);

        schemaObj = Jsons.fromJson(schemaText, JSONTree.class);
        if (!JSONTree.hasChoose(schemaObj)) {
            throw new IllegalStateException("Not choose");
        }
        DataStructure extractedData = JSONExtractUtils.extractData(text, schemaObj);
        System.out.println("Table data: " + JSON.toJSONString(extractedData));
    }

    @Test
    public void extractNormalData() throws ParseException {
        String text = "{\"code\":200,\"msg\":\"成功\",\"success\":true,\"data\":[{\"name\":\"Alice\",\"age\":18,\"gender\":\"F\"},{\"name\":\"Bob\",\"age\":20,\"gender\":\"M\"},{\"name\":\"Tom\",\"age\":30,\"gender\":\"M\"}]}";
        System.out.println("Origin data: " + text);

        TreeNode<JSONId, Null> treeNode = JSONExtractUtils.extractSchema(text);
        JSONTree schemaObj = (treeNode == null) ? null : treeNode.convert(JSONTree::convert);

        String schemaText = JSON.toJSONString(schemaObj);
        System.out.println("Extracted Schema: " + schemaText);

        // 拷贝上面的schemaText进行更改部分checked为true（即选中某些想要的数据列）
        schemaText = "{\"checked\":true,\"children\":[{\"checked\":false,\"name\":\"msg\",\"orders\":1,\"path\":[\"Root\",\"msg\"],\"type\":\"STRING\"},{\"checked\":false,\"name\":\"code\",\"orders\":2,\"path\":[\"Root\",\"code\"],\"type\":\"INTEGER\"},{\"checked\":true,\"children\":[{\"checked\":true,\"children\":[{\"checked\":true,\"name\":\"gender\",\"orders\":5,\"path\":[\"Root\",\"data\",\"[{}]\",\"gender\"],\"type\":\"STRING\"},{\"checked\":true,\"name\":\"name\",\"orders\":6,\"path\":[\"Root\",\"data\",\"[{}]\",\"name\"],\"type\":\"STRING\"},{\"checked\":true,\"name\":\"age\",\"orders\":7,\"path\":[\"Root\",\"data\",\"[{}]\",\"age\"],\"type\":\"INTEGER\"}],\"name\":\"[{}]\",\"orders\":4,\"path\":[\"Root\",\"data\",\"[{}]\"]}],\"name\":\"data\",\"orders\":3,\"path\":[\"Root\",\"data\"]},{\"checked\":false,\"name\":\"success\",\"orders\":8,\"path\":[\"Root\",\"success\"],\"type\":\"BOOLEAN\"}],\"name\":\"Root\",\"orders\":0,\"path\":[\"Root\"]}";
        System.out.println("After selected Some Column: " + schemaText);

        schemaObj = Jsons.fromJson(schemaText, JSONTree.class);
        if (!JSONTree.hasChoose(schemaObj)) {
            throw new IllegalStateException("Not choose");
        }
        DataStructure extractedData = JSONExtractUtils.extractData(text, schemaObj);
        System.out.println("Table data: " + JSON.toJSONString(extractedData));
    }

    @Test
    public void extractNormalData2() throws ParseException {
        String text = "{\"code\":200,\"msg\":\"成功\",\"success\":true,\"data\":[{\"name\":\"Alice\",\"age\":18,\"gender\":\"F\"},{\"name\":\"Bob\",\"age\":20,\"gender\":\"M\"},{\"name\":\"Tom\",\"age\":30,\"gender\":\"M\"}]}";
        System.out.println("Origin data: " + text);

        TreeNode<JSONId, Null> treeNode = JSONExtractUtils.extractSchema(text);
        JSONTree schemaObj = (treeNode == null) ? null : treeNode.convert(JSONTree::convert);

        String schemaText = JSON.toJSONString(schemaObj);
        System.out.println("Extracted Schema: " + schemaText);

        // 拷贝上面的schemaText进行更改部分checked为true（即选中某些想要的数据列）
        schemaText = "{\"checked\":true,\"children\":[{\"checked\":true,\"name\":\"msg\",\"orders\":1,\"path\":[\"Root\",\"msg\"],\"type\":\"STRING\"},{\"checked\":true,\"name\":\"code\",\"orders\":2,\"path\":[\"Root\",\"code\"],\"type\":\"INTEGER\"},{\"checked\":true,\"children\":[{\"checked\":true,\"children\":[{\"checked\":true,\"name\":\"gender\",\"orders\":5,\"path\":[\"Root\",\"data\",\"[{}]\",\"gender\"],\"type\":\"STRING\"},{\"checked\":false,\"name\":\"name\",\"orders\":6,\"path\":[\"Root\",\"data\",\"[{}]\",\"name\"],\"type\":\"STRING\"},{\"checked\":true,\"name\":\"age\",\"orders\":7,\"path\":[\"Root\",\"data\",\"[{}]\",\"age\"],\"type\":\"INTEGER\"}],\"name\":\"[{}]\",\"orders\":4,\"path\":[\"Root\",\"data\",\"[{}]\"]}],\"name\":\"data\",\"orders\":3,\"path\":[\"Root\",\"data\"]},{\"checked\":false,\"name\":\"success\",\"orders\":8,\"path\":[\"Root\",\"success\"],\"type\":\"BOOLEAN\"}],\"name\":\"Root\",\"orders\":0,\"path\":[\"Root\"]}";
        System.out.println("After selected Some Column: " + schemaText);

        schemaObj = Jsons.fromJson(schemaText, JSONTree.class);
        if (!JSONTree.hasChoose(schemaObj)) {
            throw new IllegalStateException("Not choose");
        }
        DataStructure extractedData = JSONExtractUtils.extractData(text, schemaObj);
        System.out.println("Table data: " + JSON.toJSONString(extractedData));
    }

    @Test
    public void extractComplexData() throws ParseException {
        String text = "{\"code\":200,\"msg\":\"成功\",\"success\":true,\"data\":{\"friends\":[{\"name\":\"Alice\",\"age\":18,\"gender\":\"F\",\"values\":[\"a\",\"b\",1,2]},{\"name\":\"Bob\",\"age\":20,\"gender\":\"M\",\"values\":[\"c\",\"d\",4]},{\"name\":\"Tom\",\"age\":30,\"gender\":\"M\",\"values\":[\"e\",\"f\"]}],\"darray\":[[\"a1\",\"b1\",11,21],[\"a2\",\"b2\",12,22],[\"a3\",\"b3\",13,23],[\"a4\",\"b4\",14,24]]}}";
        System.out.println("Origin data: " + text);

        TreeNode<JSONId, Null> treeNode = JSONExtractUtils.extractSchema(text);
        JSONTree schemaObj = (treeNode == null) ? null : treeNode.convert(JSONTree::convert);

        String schemaText = JSON.toJSONString(schemaObj);
        System.out.println("Extracted Schema: " + schemaText);

        // 拷贝上面的schemaText进行更改部分checked为true（即选中某些想要的数据列）
        schemaText = "{\"checked\":true,\"children\":[{\"checked\":true,\"name\":\"msg\",\"orders\":1,\"path\":[\"Root\",\"msg\"],\"type\":\"STRING\"},{\"checked\":true,\"name\":\"code\",\"orders\":2,\"path\":[\"Root\",\"code\"],\"type\":\"INTEGER\"},{\"checked\":true,\"children\":[{\"checked\":true,\"children\":[{\"checked\":true,\"children\":[{\"checked\":true,\"name\":\"[06]\",\"orders\":6,\"path\":[\"Root\",\"data\",\"darray\",\"[[]]\",\"[06]\"],\"type\":\"STRING\"},{\"checked\":true,\"name\":\"[07]\",\"orders\":7,\"path\":[\"Root\",\"data\",\"darray\",\"[[]]\",\"[07]\"],\"type\":\"STRING\"},{\"checked\":true,\"name\":\"[08]\",\"orders\":8,\"path\":[\"Root\",\"data\",\"darray\",\"[[]]\",\"[08]\"],\"type\":\"INTEGER\"},{\"checked\":true,\"name\":\"[09]\",\"orders\":9,\"path\":[\"Root\",\"data\",\"darray\",\"[[]]\",\"[09]\"],\"type\":\"INTEGER\"}],\"name\":\"[[]]\",\"orders\":5,\"path\":[\"Root\",\"data\",\"darray\",\"[[]]\"]}],\"name\":\"darray\",\"orders\":4,\"path\":[\"Root\",\"data\",\"darray\"]},{\"checked\":true,\"children\":[{\"checked\":true,\"children\":[{\"checked\":true,\"name\":\"gender\",\"orders\":12,\"path\":[\"Root\",\"data\",\"friends\",\"[{}]\",\"gender\"],\"type\":\"STRING\"},{\"checked\":true,\"children\":[{\"checked\":true,\"children\":[{\"checked\":true,\"name\":\"[15]\",\"orders\":15,\"path\":[\"Root\",\"data\",\"friends\",\"[{}]\",\"values\",\"[()]\",\"[15]\"],\"type\":\"STRING\"},{\"checked\":true,\"name\":\"[16]\",\"orders\":16,\"path\":[\"Root\",\"data\",\"friends\",\"[{}]\",\"values\",\"[()]\",\"[16]\"],\"type\":\"STRING\"},{\"checked\":true,\"name\":\"[17]\",\"orders\":17,\"path\":[\"Root\",\"data\",\"friends\",\"[{}]\",\"values\",\"[()]\",\"[17]\"],\"type\":\"INTEGER\"},{\"checked\":true,\"name\":\"[18]\",\"orders\":18,\"path\":[\"Root\",\"data\",\"friends\",\"[{}]\",\"values\",\"[()]\",\"[18]\"],\"type\":\"INTEGER\"}],\"name\":\"[()]\",\"orders\":14,\"path\":[\"Root\",\"data\",\"friends\",\"[{}]\",\"values\",\"[()]\"]}],\"name\":\"values\",\"orders\":13,\"path\":[\"Root\",\"data\",\"friends\",\"[{}]\",\"values\"]},{\"checked\":true,\"name\":\"name\",\"orders\":19,\"path\":[\"Root\",\"data\",\"friends\",\"[{}]\",\"name\"],\"type\":\"STRING\"},{\"checked\":true,\"name\":\"age\",\"orders\":20,\"path\":[\"Root\",\"data\",\"friends\",\"[{}]\",\"age\"],\"type\":\"INTEGER\"}],\"name\":\"[{}]\",\"orders\":11,\"path\":[\"Root\",\"data\",\"friends\",\"[{}]\"]}],\"name\":\"friends\",\"orders\":10,\"path\":[\"Root\",\"data\",\"friends\"]}],\"name\":\"data\",\"orders\":3,\"path\":[\"Root\",\"data\"]},{\"checked\":true,\"name\":\"success\",\"orders\":21,\"path\":[\"Root\",\"success\"],\"type\":\"BOOLEAN\"}],\"name\":\"Root\",\"orders\":0,\"path\":[\"Root\"]}";
        System.out.println("After selected Some Column: " + schemaText);

        schemaObj = Jsons.fromJson(schemaText, JSONTree.class);
        if (!JSONTree.hasChoose(schemaObj)) {
            throw new IllegalStateException("Not choose");
        }
        DataStructure extractedData = JSONExtractUtils.extractData(text, schemaObj);
        System.out.println("Table data: " + JSON.toJSONString(extractedData));
    }
}
