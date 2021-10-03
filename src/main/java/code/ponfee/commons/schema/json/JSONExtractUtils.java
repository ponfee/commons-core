package code.ponfee.commons.schema.json;

import code.ponfee.commons.collect.Collects;
import code.ponfee.commons.exception.Throwables;
import code.ponfee.commons.json.JsonUtils;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.model.Null;
import code.ponfee.commons.schema.DataColumn;
import code.ponfee.commons.schema.DataStructure;
import code.ponfee.commons.schema.DataType;
import code.ponfee.commons.schema.PlainStructure;
import code.ponfee.commons.schema.TableStructure;
import code.ponfee.commons.tree.BaseNode;
import code.ponfee.commons.tree.NodePath;
import code.ponfee.commons.tree.TreeNode;
import code.ponfee.commons.tree.TreeNodeBuilder;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The utility class for extract json schema and data
 * 
 * @author Ponfee
 */
public final class JSONExtractUtils {

    static final String ARRAY_EMPTY  = "[--]";
    static final String ARRAY_ARRAY  = "[[]]";
    static final String ARRAY_OBJECT = "[{}]";
    static final String ARRAY_BASIC  = "[()]";
    static final String ARRAY_INDEX  = "[%02d]";

    static final Map<String, Object> NULL_VALUE_MAPPING = Collections.unmodifiableMap(
        Collects.toMap(
            ARRAY_EMPTY,  null,
            ARRAY_ARRAY,  Collections.singletonList(Collections.emptyList()),
            ARRAY_OBJECT, Collections.singletonList(Collections.emptyMap()),
            ARRAY_BASIC,  Collections.emptyList()
        )
    );

    private static final String ROOT = "Root";

    public static TreeNode<JSONId, Null> extractSchema(String text) throws ParseException {
        return extractSchema(JSON.parse(text));
    }

    /**
     * Returns a tree data of extracted the json data structure schema
     * 
     * @param obj the object of use {@link JSON#parse(String)} parsed
     * @return a tree node of json data schema
     */
    public static TreeNode<JSONId, Null> extractSchema(Object obj) throws ParseException {
        if (!JsonUtils.isComplexType(obj)) {
            throw new ParseException("The basic type data cannot extract schema: " + obj, 0);
        }

        List<JSONId> ids = new LinkedList<>();
        extractSchema(ids, null, obj, new AtomicInteger(1));

        return ids.isEmpty() ? null : buildTree(ids);
    }

    public static DataStructure extractData(String original, @Nonnull JSONTree tree) {
        Object obj;
        try {
            obj = JSON.parse(original);
        } catch (Exception ignored) {
            Throwables.ignore(ignored);
            return new PlainStructure(original);
        }

        if (!JsonUtils.isComplexType(obj)) {
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
    public static TableStructure extractData(@Nonnull Object object, @Nonnull JSONTree tree) {
        List<List<Object>> dataset = new LinkedList<>();
        LinkedHashSet<NodePath<String>> extracted = new LinkedHashSet<>();
        Map<NodePath<String>, JSONTree> config = tree.toFlatMap();
        extractData(dataset, tree.getPath(), object, config, extracted);

        Set<String> duplicate = extracted.stream().map(
            path -> path.get(path.size() - 1)
        ).collect(
            Collectors.groupingBy(Function.identity(), Collectors.counting())
        ).entrySet().stream().filter(
            e -> (e.getValue() > 1)
        ).map(
            Entry::getKey
        ).collect(
            Collectors.toSet()
        );

        DataColumn[] columns = new DataColumn[extracted.size()];
        int i = 0;
        for (NodePath<String> path : extracted) {
            JSONTree node = config.get(path);
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
    private static void extractSchema(List<JSONId> ids, JSONId parent, Object object, AtomicInteger count) {
        if (object instanceof Map) { // JSONObject
            for (Entry<String, Object> entry : ((Map<String, Object>) object).entrySet()) {
                DataType dataType = detectDataType(entry.getValue());
                JSONId id = new JSONId(parent, entry.getKey(), dataType, count.getAndIncrement());
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
                    ids.add(new JSONId(parent, ARRAY_EMPTY, null, count.getAndIncrement()));
                    break;
                case ARRAY: // 二维数组
                    ids.add(parent = new JSONId(parent, ARRAY_ARRAY, null, count.getAndIncrement()));
                    buildArrayColumns(findFirstArray((List<List<Object>>) list), ids, parent, count);
                    break;
                case OBJECT: // 数组对象
                    ids.add(parent = new JSONId(parent, ARRAY_OBJECT, null, count.getAndIncrement()));
                    extractSchema(ids, parent, findFirstObject((List<Map<String, Object>>) list), count); // 继续下钻解析
                    break;
                case BASIC: // 基本类型（一行）
                    ids.add(parent = new JSONId(parent, ARRAY_BASIC, null, count.getAndIncrement()));
                    buildArrayColumns(list, ids, parent, count);
                    break;
                default:
                    throw new RuntimeException("Unknown data type: " + Jsons.toJson(list)); // cannot happened
            }
        } else {
            // If others json type then to skip
        }
    }

    private static TreeNode<JSONId, Null> buildTree(List<JSONId> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }

        TreeNode<JSONId, Null> root = TreeNodeBuilder.<JSONId, Null> newBuilder(
            new JSONId(null, ROOT, null, 0)
        ).build();

        try {
            root.mount(ids.stream().map(JSONExtractUtils::toNode).collect(Collectors.toList()));
            return root;
        } catch (Exception e) {
            throw new IllegalStateException("Parsed json schema occur error: " + e.getMessage(), e);
        }
    }

    private static BaseNode<JSONId, Null> toNode(JSONId id) {
        return new BaseNode<>(id, id.getParent(), null);
    }

    @SuppressWarnings("unchecked")
    private static void extractData(List<List<Object>> dataset, NodePath<String> parent,
                                    Object object, Map<NodePath<String>, JSONTree> config,
                                    LinkedHashSet<NodePath<String>> extracted) {
        JSONTree tree = config.get(parent);
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
            List<JSONTree> checkedNodes = new LinkedList<>();
            for (String name : map.keySet()) {
                JSONTree node = config.get(new NodePath<>(parent, name));
                if (node != null && node.isChecked()) {
                    checkedNodes.add(node);
                }
            }
            // adjust orders
            checkedNodes.sort(Comparator.comparing(JSONTree::getOrders));

            List<List<Object>> subset = new LinkedList<>();
            for (JSONTree node : checkedNodes) {
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
            JSONTree node;
            switch (detectArrayType(list)) {
                case EMPTY: // 空的数组
                    // Nothing to do
                    break;
                case ARRAY: // 二维数组
                    node = config.get(new NodePath<>(parent, ARRAY_ARRAY));
                    if (node != null && node.isChecked()) {
                        List<Pair<Integer, JSONTree>> checkedNodes = getCheckedChildren(node, config, extracted);
                        List<List<Object>> subset = new LinkedList<>();
                        for (List<Object> array : ((List<List<Object>>) list)) {
                            List<Object> row = new LinkedList<>();
                            int size = array.size();
                            for (Pair<Integer, JSONTree> pair : checkedNodes) {
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
                        List<Pair<Integer, JSONTree>> checkedNodes = getCheckedChildren(node, config, extracted);
                        int size = list.size();
                        List<Object> row = new LinkedList<>();
                        for (Pair<Integer, JSONTree> pair : checkedNodes) {
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
        return JsonUtils.isComplexType(value) ? null : DataType.STRING;
    }

    private static Object getValue(Object value, DataType dataType) {
        return dataType == DataType.STRING ? Objects.toString(value, null) : value;
    }

    private static void buildArrayColumns(List<?> list, List<JSONId> ids,
                                          JSONId parent, AtomicInteger count) {
        for (Object element : list) {
            int index = count.getAndIncrement();
            // 二维数组和基本数组不再下钻解析，如果为复合类型（二维数组），则直接判决其为STRING
            DataType dataType = detectDataType(element, DataType.STRING);
            ids.add(new JSONId(parent, String.format(ARRAY_INDEX, index), dataType, index));
        }
    }

    private static List<Pair<Integer, JSONTree>> getCheckedChildren(
            JSONTree node, Map<NodePath<String>, JSONTree> config, LinkedHashSet<NodePath<String>> columns) {
        int startIndex = node.getOrders() + 1;
        List<Pair<Integer, JSONTree>> checkedNodes = new LinkedList<>();
        for (int i = 0, n = node.getChildren().size(); i < n; i++) {
            String name = String.format(ARRAY_INDEX, startIndex + i);
            JSONTree child = config.get(new NodePath<>(node.getPath(), name));
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
