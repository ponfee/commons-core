/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.schema.json;

import cn.ponfee.commons.collect.Collects;
import cn.ponfee.commons.json.Jsons;
import cn.ponfee.commons.model.Null;
import cn.ponfee.commons.model.ToJsonString;
import cn.ponfee.commons.schema.DataType;
import cn.ponfee.commons.tree.NodePath;
import cn.ponfee.commons.tree.TreeNode;
import cn.ponfee.commons.tree.TreeTrait;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Json data tree structure
 * 
 * @author Ponfee
 */
public class JsonTree extends ToJsonString implements Serializable, Comparable<JsonTree>, TreeTrait<JsonId, Null, JsonTree> {

    private static final long serialVersionUID = 2185766536906561848L;

    // 解决NodePath泛型参数为具体类型时，FastJson反序列化的报错问题
    @JSONField(deserializeUsing = NodePath.FastjsonDeserializer.class)
    private NodePath<String>   path; // 路径
    private String             name; // 节点
    private int              orders; // 次序
    private boolean         checked; // 是否选中
    private DataType           type; // 数据类型
    private List<JsonTree> children; // 子节点列表

    @Override
    public int compareTo(JsonTree o) {
        return this.path.compareTo(o.path);
    }

    @Override
    public int hashCode() {
        return this.path.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JsonTree)) {
            return false;
        }

        JsonTree other = (JsonTree) obj;
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

        try {
            this.sortByName();
            other.sortByName();

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
    public void setChildren(List<JsonTree> children) {
        if (CollectionUtils.isNotEmpty(children)) {
            List<String> duplicated = Collects.duplicate(children, JsonTree::getName);
            if (CollectionUtils.isNotEmpty(duplicated)) {
                throw new IllegalStateException("Duplicated child name " + duplicated);
            }
        }

        this.children = children;
    }

    // --------------------------------------------------------------------------sort
    public void sortByOrders() {
        this.sortChildren(Comparator.comparing(JsonTree::getOrders));
    }

    public void sortByName() {
        this.sortChildren(Comparator.comparing(JsonTree::getName));
    }

    public void sortChildren(Comparator<JsonTree> comparator) {
        if (CollectionUtils.isNotEmpty(this.children)) {
            this.children.sort(comparator);
            for (JsonTree node : this.children) {
                node.sortChildren(comparator);
            }
        }
    }

    public Map<NodePath<String>, JsonTree> toFlatMap() {
        Map<NodePath<String>, JsonTree> map = new HashMap<>();
        this.toFlatMap(map);
        return map;
    }

    // --------------------------------------------------------------------------static methods
    public static JsonTree convert(TreeNode<JsonId, Null> tree) {
        JsonId id = tree.getNid();
        JsonTree jt = new JsonTree();

        jt.setName(id.getName());
        jt.setOrders(id.getOrders());
        jt.setChecked(false);
        jt.setType(id.getType());
        jt.setPath(new NodePath<>(tree.getPath().stream().map(JsonId::getName).collect(Collectors.toList())));

        return jt;
    }

    public static boolean hasChoose(JsonTree root) {
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

    @Override
    public List<JsonTree> getChildren() {
        return children;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    // --------------------------------------------------------------------------private methods
    private static boolean checkChoose(JsonTree node) {
        if (CollectionUtils.isEmpty(node.children)) {
            return node.checked;
        }

        boolean hasLeafChildChoose = false;
        for (JsonTree child : node.children) {
            if (child.checked && !node.checked) {
                throw new IllegalStateException(
                    "Child is checked but parent is unchecked: " + child.path.toString()
                );
            }
            if (CollectionUtils.isEmpty(child.children)) {
                // leaf node
                if (child.checked && JsonExtractUtils.ARRAY_EMPTY.equals(child.name)) {
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

    private void toFlatMap(Map<NodePath<String>, JsonTree> map) {
        map.put(this.getPath(), this);
        if (CollectionUtils.isNotEmpty(this.children)) {
            for (JsonTree node : this.children) {
                node.toFlatMap(map);
            }
        }
    }

}
