/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.schema.json;

import cn.ponfee.commons.collect.Collects;
import cn.ponfee.commons.collect.Maps;
import cn.ponfee.commons.json.Jsons;
import cn.ponfee.commons.model.Null;
import cn.ponfee.commons.schema.*;
import cn.ponfee.commons.tree.NodePath;
import cn.ponfee.commons.tree.PlainNode;
import cn.ponfee.commons.tree.TreeNode;
import cn.ponfee.commons.tree.TreeNodeBuilder;
import cn.ponfee.commons.util.ObjectUtils;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The utility class for extract json schema and data
 *
 * @author Ponfee
 */
public final class JsonExtractUtils {

    static final String ARRAY_EMPTY  = "[--]";
    static final String ARRAY_ARRAY  = "[[]]";
    static final String ARRAY_OBJECT = "[{}]";
    static final String ARRAY_BASIC  = "[()]";
    static final String ARRAY_INDEX  = "[%02d]";

    static final Map<String, Object> NULL_VALUE_MAPPING = Collections.unmodifiableMap(
        Maps.toMap(
            ARRAY_EMPTY,  null,
            ARRAY_ARRAY,  Collections.singletonList(Collections.emptyList()),
            ARRAY_OBJECT, Collections.singletonList(Collections.emptyMap()),
            ARRAY_BASIC,  Collections.emptyList()
        )
    );

    private static final String ROOT = "Root";

    public static TreeNode<JsonId, Null> extractSchema(String text) throws ParseException {
        // com.fasterxml.jackson.databind.ObjectMapper#readTree(String)
        return extractSchema(JSON.parse(text));
    }

    /**
     * Returns a tree data of extracted the json data structure schema
     *
     * @param obj the object of use {@link JSON#parse(String)} parsed
     * @return a tree node of json data schema
     */
    public static TreeNode<JsonId, Null> extractSchema(Object obj) throws ParseException {
        if (!ObjectUtils.isComplexType(obj)) {
            throw new ParseException("The basic type data cannot extract schema: " + obj, 0);
        }

        List<JsonId> ids = new LinkedList<>();
        extractSchema(ids, null, obj, new AtomicInteger(1));

        return ids.isEmpty() ? null : buildTree(ids);
    }

    public static DataStructure extractData(String original, @Nonnull JsonTree tree) {
        Object obj;
        try {
            obj = JSON.parse(original);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return new PlainStructure(original);
        }

        if (!ObjectUtils.isComplexType(obj)) {
            return new PlainStructure(original);
        }

        return extractData(obj, tree);
    }

    /**
     * Returns a DataStructure object by user specified json columns in tree
     *
     * @param object the object of use {@link JSON#parse(String)} parsed
     * @param tree the json tree
     * @return a DataStructure object
     */
    public static TableStructure extractData(@Nonnull Object object, @Nonnull JsonTree tree) {
        List<List<Object>> dataset = new LinkedList<>();
        LinkedHashSet<NodePath<String>> extracted = new LinkedHashSet<>();
        Map<NodePath<String>, JsonTree> config = tree.toFlatMap();
        extractData(dataset, tree.getPath(), object, config, extracted);

        Set<String> duplicate = extracted
                .stream()
                .map(Collects::getLast)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > 1)
                .map(Entry::getKey)
                .collect(Collectors.toSet());

        DataColumn[] columns = new DataColumn[extracted.size()];
        int i = 0;
        for (NodePath<String> path : extracted) {
            JsonTree node = config.get(path);
            String name = node.getName();
            if (duplicate.contains(name)) {
                // prevent repeat name
                name += "-" + String.format("%02d", node.getOrders());
            }
            columns[i++] = new DataColumn(name, node.getType(), null);
        }

        return new TableStructure(
            columns,
            dataset.stream().map(List::toArray).collect(Collectors.toList())
        );
    }

    // -----------------------------------------------------------------------------------private methods
    @SuppressWarnings("unchecked")
    private static void extractSchema(List<JsonId> ids, JsonId parent, Object object, AtomicInteger count) {
        if (object instanceof Map) { // JSONObject
            for (Entry<String, Object> entry : ((Map<String, Object>) object).entrySet()) {
                DataType dataType = detectDataType(entry.getValue());
                JsonId id = new JsonId(parent, entry.getKey(), dataType, count.getAndIncrement());
                ids.add(id);
                if (dataType == null) {
                    // complex json data type
                    extractSchema(ids, id, entry.getValue(), count);
                }
            }
        } else if ((object instanceof List) || object.getClass().isArray()) { // JSONArray
            List<?> list = Collects.toList(object);
            switch (detectArrayType(list)) {
                case EMPTY: // 空的数组
                    ids.add(new JsonId(parent, ARRAY_EMPTY, null, count.getAndIncrement()));
                    break;
                case ARRAY: // 二维数组
                    ids.add(parent = new JsonId(parent, ARRAY_ARRAY, null, count.getAndIncrement()));
                    buildArrayColumns(findFirstArray((List<List<Object>>) list), ids, parent, count);
                    break;
                case OBJECT: // 数组对象
                    ids.add(parent = new JsonId(parent, ARRAY_OBJECT, null, count.getAndIncrement()));
                    extractSchema(ids, parent, findFirstObject((List<Map<String, Object>>) list), count); // 继续下钻解析
                    break;
                case BASIC: // 基本类型（一行）
                    ids.add(parent = new JsonId(parent, ARRAY_BASIC, null, count.getAndIncrement()));
                    buildArrayColumns(list, ids, parent, count);
                    break;
                default:
                    throw new RuntimeException("Unknown data type: " + Jsons.toJson(list)); // cannot happened
            }
        } else {
            // If others json type then to skip
        }
    }

    private static TreeNode<JsonId, Null> buildTree(List<JsonId> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }

        TreeNode<JsonId, Null> root = TreeNodeBuilder.<JsonId, Null> newBuilder(
            new JsonId(null, ROOT, null, 0)
        ).build();

        try {
            root.mount(ids.stream().map(JsonExtractUtils::toNode).collect(Collectors.toList()));
            return root;
        } catch (Exception e) {
            throw new IllegalStateException("Parsed json schema occur error: " + e.getMessage(), e);
        }
    }

    private static PlainNode<JsonId, Null> toNode(JsonId id) {
        return new PlainNode<>(id, id.getParent(), null);
    }

    @SuppressWarnings("unchecked")
    private static void extractData(List<List<Object>> dataset, NodePath<String> parent,
                                    Object object, Map<NodePath<String>, JsonTree> config,
                                    LinkedHashSet<NodePath<String>> extracted) {
        JsonTree tree = config.get(parent);
        if (CollectionUtils.isEmpty(tree.getChildren())) {
            throw new IllegalStateException("Parent \"" + parent + "\" have not children.");
        }
        if (!tree.isChecked()) {
            return; // parent not check
        }

        if (object == null) {
            if (tree.getChildren().size() == 1) {
                String childName = tree.getChildren().get(0).getName();
                // if ARRAY_EMPTY then null, else if single map key then Collections.emptyMap()
                object = NULL_VALUE_MAPPING.getOrDefault(childName, Collections.emptyMap());
            } else {
                object = Collections.emptyMap();
            }
        }

        if (object instanceof Map) { // JSONObject
            Map<String, Object> map = (Map<String, Object>) object;
            List<JsonTree> checkedNodes = new LinkedList<>();
            for (String name : map.keySet()) {
                JsonTree node = config.get(new NodePath<>(parent, name));
                if (node != null && node.isChecked()) {
                    checkedNodes.add(node);
                }
            }
            // adjust orders
            checkedNodes.sort(Comparator.comparing(JsonTree::getOrders));

            List<List<Object>> subset = new LinkedList<>();
            for (JsonTree node : checkedNodes) {
                Object value = map.get(node.getName());
                if (node.getType() != null) { // or CollectionUtils.isEmpty(node.getChildren())
                    // leaf node, extract this value data
                    extracted.add(node.getPath());
                    concat(subset, getValue(value, node.getType()));
                } else {
                    List<List<Object>> dataset0 = new LinkedList<>();
                    extractData(dataset0, node.getPath(), value, config, extracted);
                    concat(subset, dataset0);
                }
            }

            append(dataset, subset);
        } else if ((object instanceof List) || object.getClass().isArray()) { // JSONArray
            List<?> list = Collects.toList(object);
            JsonTree node;
            switch (detectArrayType(list)) {
                case EMPTY: // 空的数组
                    // Nothing to do
                    break;
                case ARRAY: // 二维数组
                    node = config.get(new NodePath<>(parent, ARRAY_ARRAY));
                    if (node != null && node.isChecked()) {
                        List<Pair<Integer, JsonTree>> checkedNodes = getCheckedChildren(node, config, extracted);
                        List<List<Object>> subset = new LinkedList<>();
                        for (List<Object> array : ((List<List<Object>>) list)) {
                            List<Object> row = new LinkedList<>();
                            int size = array.size();
                            for (Pair<Integer, JsonTree> pair : checkedNodes) {
                                int idx = pair.getLeft();
                                row.add(idx >= size ? null : getValue(array.get(idx), pair.getRight().getType()));
                            }
                            subset.add(row);
                        }
                        concat(dataset, subset);
                    }
                    break;
                case OBJECT: // 数组对象
                    node = config.get(new NodePath<>(parent, ARRAY_OBJECT));
                    if (node != null && node.isChecked()) {
                        for (Map<String, Object> map : (List<Map<String, Object>>) list) {
                            if (map == null) {
                                map = Collections.emptyMap();
                            }
                            extractData(dataset, node.getPath(), map, config, extracted);
                        }
                    }
                    break;
                case BASIC: // 基本类型（一行多列）
                    node = config.get(new NodePath<>(parent, ARRAY_BASIC));
                    if (node != null && node.isChecked()) {
                        List<Pair<Integer, JsonTree>> checkedNodes = getCheckedChildren(node, config, extracted);
                        int size = list.size();
                        List<Object> row = new LinkedList<>();
                        for (Pair<Integer, JsonTree> pair : checkedNodes) {
                            int idx = pair.getLeft();

                            // 如果之后数据的长度不够就横向重复
                            //row.add(getValue(list.get(idx >= size ? size - 1 : idx), pair.getRight().getType()));

                            // 如果之后数据的长度不够就为null
                            row.add(idx >= size ? null : getValue(list.get(idx), pair.getRight().getType()));
                        }
                        concat(dataset, Collections.singletonList(row));
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown data type: " + Jsons.toJson(list)); // cannot happened
            }
        } else {
            // If others json type then to skip
        }
    }

    /**
     * Detect the array inner emelent type(the first non null value)
     *
     * @param list the array
     * @return a type
     */
    private static ArrayType detectArrayType(List<?> list) {
        if (list.isEmpty()) {
            return ArrayType.EMPTY;
        }

        for (Object obj : list) {
            if (obj == null) {
                continue;
            }
            if (obj instanceof Map) {
                return ArrayType.OBJECT;
            } else if (obj instanceof List) {
                return ArrayType.ARRAY;
            } else {
                return ArrayType.BASIC;
            }
        }

        // if all elements is null, then determine is basic array type
        return ArrayType.BASIC;
    }

    private enum ArrayType {
        EMPTY, OBJECT, ARRAY, BASIC
    }

    private static Map<String, Object> findFirstObject(List<Map<String, Object>> list) {
        for (Map<String, Object> map : list) {
            if (map != null) {
                return map;
            }
        }
        throw new RuntimeException("Empty [OBJECT]"); // cannot happened
    }

    private static List<Object> findFirstArray(List<List<Object>> list) {
        for (List<Object> array : list) {
            if (array != null) {
                return array;
            }
        }
        throw new RuntimeException("Empty [ARRAY]"); // cannot happened
    }

    private static DataType detectDataType(Object value, DataType defaultType) {
        DataType dataType = detectDataType(value);
        return dataType == null ? defaultType : dataType;
    }

    // 两种特殊类型：数组(array)、对象(object)
    // 四种基础类型：字符串(string)、数字(number)、布尔型(boolean)、NULL值
    private static DataType detectDataType(Object value) {
        if (value == null || value instanceof CharSequence) {
            // cannot detect if value is null, then determine it's a string type
            return DataType.STRING;
        }

        if (value instanceof Boolean) {
            return DataType.BOOLEAN;
        }

        if (value instanceof Integer || value instanceof Long || value instanceof BigInteger) {
            return DataType.INTEGER;
        }

        if (value instanceof BigDecimal || value instanceof Float || value instanceof Double) {
            return DataType.DECIMAL;
        }

        // if not complex type then determine it's a string type
        return ObjectUtils.isComplexType(value) ? null : DataType.STRING;
    }

    private static Object getValue(Object value, DataType dataType) {
        return dataType == DataType.STRING ? Objects.toString(value, null) : value;
    }

    private static void buildArrayColumns(List<?> list, List<JsonId> ids,
                                          JsonId parent, AtomicInteger count) {
        for (Object element : list) {
            int index = count.getAndIncrement();
            // 二维数组和基本数组不再下钻解析，如果为复合类型（二维数组），则直接判决其为STRING
            DataType dataType = detectDataType(element, DataType.STRING);
            ids.add(new JsonId(parent, String.format(ARRAY_INDEX, index), dataType, index));
        }
    }

    private static List<Pair<Integer, JsonTree>> getCheckedChildren(
            JsonTree node, Map<NodePath<String>, JsonTree> config, LinkedHashSet<NodePath<String>> columns) {
        int startIndex = node.getOrders() + 1;
        List<Pair<Integer, JsonTree>> checkedNodes = new LinkedList<>();
        for (int i = 0, n = node.getChildren().size(); i < n; i++) {
            String name = String.format(ARRAY_INDEX, startIndex + i);
            JsonTree child = config.get(new NodePath<>(node.getPath(), name));
            if (child != null && child.isChecked()) {
                checkedNodes.add(Pair.of(i, child));
                columns.add(child.getPath());
            }
        }
        if (checkedNodes.isEmpty()) {
            throw new IllegalStateException(
                "Parent is checked but not checked children: " + node.getPath().toString()
            );
        }
        return checkedNodes;
    }

    // ------------------------------------------------------------append table
    /**
     * Appends the sub dataset after dataset last row, as new row
     *
     * [[1,2], [3,4]]  append  [["a","b"], ["c","d"]]  =>  [[1,2], [3,4], ["a","b"], ["c","d"]]
     *
     * @param dataset the dataset
     * @param subset  the sub dataset
     */
    public static void append(List<List<Object>> dataset, List<List<Object>> subset) {
        if (subset.isEmpty()) {
            return;
        }

        if (dataset.isEmpty()) {
            for (List<Object> row : subset) {
                dataset.add(new LinkedList<>(row));
            }
            return;
        }

        int maxCol = Math.max(dataset.get(0).size(), subset.get(0).size());
        completeCol(dataset, maxCol);
        completeCol(subset, maxCol);

        dataset.addAll(subset);
    }

    private static void completeCol(List<List<Object>> listArray, int maxCol) {
        if (listArray.isEmpty() || listArray.get(0).size() >= maxCol) {
            return;
        }

        for (List<Object> array : listArray) {
            Object lastCol = array.get(array.size() - 1);
            for (int i = maxCol - array.size(); i > 0; i--) {
                array.add(lastCol);
            }
        }
    }

    // ------------------------------------------------------------join table
    /**
     * Concats the sub dataset after dataset last column, as new column
     *
     * [[1,2], [3,4]]  concat  "a"  =>  [[1,2,"a"], [3,4,"a"]]
     *
     * @param dataset the dataset
     * @param value   the new column vlaue
     */
    public static void concat(List<List<Object>> dataset, Object value) {
        if (dataset.isEmpty()) {
            List<Object> row = new LinkedList<>();
            row.add(value);
            dataset.add(row);
        } else {
            for (List<Object> array : dataset) {
                array.add(value);
            }
        }
    }

    /**
     * Concats the sub dataset after dataset last column, as new column
     *
     * [[1,2], [3,4]]  concat  [["a","b"], ["c","d"]]  =>  [[1,2,"a","b"], [3,4,"c","d"]]
     *
     * @param dataset the dataset
     * @param subset  the sub dataset
     */
    public static void concat(List<List<Object>> dataset, List<List<Object>> subset) {
        if (subset.isEmpty()) {
            return;
        }

        if (dataset.isEmpty()) {
            dataset.addAll(subset);
            return;
        }

        int maxRow = Math.max(dataset.size(), subset.size());
        completeRow(dataset, maxRow);
        completeRow(subset, maxRow);

        for (int i = 0; i < maxRow; i++) {
            dataset.get(i).addAll(subset.get(i));
        }
    }

    private static void completeRow(List<List<Object>> listArray, int maxRow) {
        if (listArray.size() >= maxRow) {
            return;
        }

        List<Object> lastRow = listArray.get(listArray.size() - 1);
        for (int i = maxRow - listArray.size(); i > 0; i--) {
            listArray.add(new LinkedList<>(lastRow)); // repeat last row
        }
    }

}
