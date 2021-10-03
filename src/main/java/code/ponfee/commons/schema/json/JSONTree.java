package code.ponfee.commons.schema.json;

import code.ponfee.commons.schema.DataType;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.model.Null;
import code.ponfee.commons.tree.NodePath;
import code.ponfee.commons.tree.TreeNode;
import code.ponfee.commons.tree.TreeTrait;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Json data tree structure
 * 
 * @author Ponfee
 */
public class JSONTree implements Serializable, Comparable<JSONTree>, TreeTrait<JSONId, Null, JSONTree> {

    private static final long serialVersionUID = 2185766536906561848L;

    // 解决NodePath泛型参数为具体类型时，FastJson反序列化的报错问题
    @JSONField(deserializeUsing = NodePath.NodePathFastjsonDeserializer.class)
    private NodePath<String>   path; // 路径
    private String             name; // 节点
    private int              orders; // 次序
    private boolean         checked; // 是否选中
    private DataType           type; // 数据类型
    private List<JSONTree> children; // 子节点列表

    @Override
    public int compareTo(JSONTree o) {
        return this.path.compareTo(o.path);
    }

    @Override
    public int hashCode() {
        return this.path.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JSONTree)) {
            return false;
        }

        JSONTree other = (JSONTree) obj;
        try {
            this.sortByName();
            other.sortByName();

            if (!this.path.equals(other.path)) {
                return false;
            }

            if (   CollectionUtils.isEmpty(this.children)
                && CollectionUtils.isEmpty(other.children)
            ) {
                return true;
            }

            if (   CollectionUtils.isEmpty(this.children)
                || CollectionUtils.isEmpty(other.children)
                || this.children.size() != other.children.size()
            ) {
                return false;
            }

            for (int i = 0, n = this.children.size(); i < n; i++) {
                if (!this.children.get(i).equals(other.children.get(i))) {
                    return false;
                }
            }

            return true;
        } finally {
            this.sortByOrders();
            other.sortByOrders();
        }
    }

    @Override
    public String toString() {
        return Jsons.toJson(this);
        //return JSON.toJSONString(this, FastjsonPropertyFilter.exclude("children"));
    }

    @Override
    public void setChildren(List<JSONTree> children) {
        if (CollectionUtils.isNotEmpty(children)) {
            Set<String> duplicated = children.stream().map(
                JSONTree::getName
            ).collect(
                Collectors.groupingBy(Function.identity(), Collectors.counting())
            ).entrySet().stream().filter(
                 e -> e.getValue() > 1
             ).map(
                 Entry::getKey
             ).collect(
                 Collectors.toSet()
             );

            if (CollectionUtils.isNotEmpty(duplicated)) {
                throw new IllegalStateException("Duplicated json id name: " + duplicated);
            }
        }

        this.children = children;
    }

    // --------------------------------------------------------------------------sort
    public void sortByOrders() {
        this.sortChildren(Comparator.comparing(JSONTree::getOrders));
    }

    public void sortByName() {
        this.sortChildren(Comparator.comparing(JSONTree::getName));
    }

    public void sortChildren(Comparator<JSONTree> comparator) {
        if (CollectionUtils.isNotEmpty(this.children)) {
            this.children.sort(comparator);
            for (JSONTree node : this.children) {
                node.sortChildren(comparator);
            }
        }
    }

    public Map<NodePath<String>, JSONTree> toFlatMap() {
        Map<NodePath<String>, JSONTree> map = new HashMap<>();
        this.toFlatMap(map);
        return map;
    }

    // --------------------------------------------------------------------------static methods
    public static JSONTree convert(TreeNode<JSONId, Null> tree) {
        JSONId id = tree.getNid();
        JSONTree jt = new JSONTree();

        jt.setName(id.getName());
        jt.setOrders(id.getOrders());
        jt.setChecked(false);
        jt.setType(id.getType());
        jt.setPath(new NodePath<>(
            tree.getPath().stream().map(JSONId::getName).collect(Collectors.toList())
        ));

        return jt;
    }

    public static boolean hasChoose(JSONTree root) {
        if (root == null) {
            return false;
        }
        checkChoose(root);
        return root.checked;
    }

    // --------------------------------------------------------------------------getter/setter
    public NodePath<String> getPath() {
        return path;
    }

    public void setPath(NodePath<String> path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public List<JSONTree> getChildren() {
        return children;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    // --------------------------------------------------------------------------private methods
    private static boolean checkChoose(JSONTree node) {
        if (CollectionUtils.isEmpty(node.children)) {
            return node.checked;
        }

        boolean hasLeafChildChoose = false;
        for (JSONTree child : node.children) {
            if (child.checked && !node.checked) {
                throw new IllegalStateException(
                    "Child is checked but parent is unchecked: " + child.path.toString()
                );
            }
            if (CollectionUtils.isEmpty(child.children)) {
                // leaf node
                if (child.checked && JSONExtractUtils.ARRAY_EMPTY.equals(child.name)) {
                    throw new IllegalStateException(
                        "Empty array cannot be checked: " + child.path.toString()
                    );
                }
                hasLeafChildChoose |= child.checked;
            } else {
                hasLeafChildChoose |= checkChoose(child);
            }
        }

        if (node.checked && !hasLeafChildChoose) {
            throw new IllegalStateException(
                "Parent is checked but not checked children: " + node.path.toString()
            );
        }
        return hasLeafChildChoose;
    }

    private void toFlatMap(Map<NodePath<String>, JSONTree> map) {
        map.put(this.getPath(), this);
        if (CollectionUtils.isNotEmpty(this.children)) {
            for (JSONTree node : this.children) {
                node.toFlatMap(map);
            }
        }
    }

}
