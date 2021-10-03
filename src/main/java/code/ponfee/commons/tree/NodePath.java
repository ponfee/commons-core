package code.ponfee.commons.tree;

import code.ponfee.commons.collect.ImmutableArrayList;
import code.ponfee.commons.tree.NodePath.NodePathFastjsonDeserializeMarker;
import code.ponfee.commons.tree.NodePath.NodePathJacksonDeserializer;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static code.ponfee.commons.collect.Collects.requireNonEmpty;
import static java.util.Objects.requireNonNull;

/**
 * Representing immutable node path array
 *
 * @param <T> the node id type
 * @author Ponfee
 */
// NodePath is extends ArrayList, so must be use mappingTo in fastjson
// if not do it then deserialized json as a collection type(java.util.ArrayList)
// hashCode()/equals() extends ImmutableArrayList
@JSONType(mappingTo = NodePathFastjsonDeserializeMarker.class)
@JsonDeserialize(using = NodePathJacksonDeserializer.class)
public final class NodePath<T extends Serializable & Comparable<? super T>>
    extends ImmutableArrayList<T> implements Comparable<NodePath<T>> {

    private static final long serialVersionUID = 9090552044337950223L;

    @SuppressWarnings("unchecked")
    public NodePath(@NotEmpty T... path) {
        super(requireNonEmpty(path));
    }

    public NodePath(@NotEmpty T[] parent, T child) {
        super(requireNonEmpty(parent), requireNonNull(child));
    }

    public NodePath(@NotEmpty List<T> path) {
        super(requireNonEmpty(path));
    }

    public NodePath(@NotEmpty NodePath<T> parent, T child) {
        super(requireNonEmpty(parent), requireNonNull(child));
    }

    @Override
    public int compareTo(NodePath<T> o) {
        int c;
        for (Iterator<T> a = this.iterator(), b = o.iterator(); a.hasNext() && b.hasNext(); ) {
            if ((c = a.next().compareTo(b.next())) != 0) {
                return c;
            }
        }
        return super.size() - o.size();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof NodePath) && super.equals(obj);
    }

    @Override
    public NodePath<T> clone() {
        return new NodePath<>(this);
    }

    // -----------------------------------------------------custom fastjson deserialize
    @JSONType(deserializer = NodePathFastjsonDeserializer.class)
    public static class NodePathFastjsonDeserializeMarker {
    }

    /**
     * <pre> {@code
     *   public static class IntegerNodePath {
     *     // 当定义的NodePath字段其泛型参数为具体类型时，必须用JSONField注解，否则报错
     *     @JSONField(deserializeUsing = NodePathFastjsonDeserializer.class)
     *     private NodePath<Integer> path; // ** NodePath<Integer> **
     *   }
     * }</pre>
     *
     * @param <T>
     */
    public static class NodePathFastjsonDeserializer<T extends Serializable & Comparable<? super T>> implements ObjectDeserializer {
        @Override @SuppressWarnings("unchecked")
        public NodePath<T> deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            if ((type = getActualType(type)) != NodePath.class) {
                throw new UnsupportedOperationException("Only supported deserialize NodePath, cannot supported: " + type);
            }

            String value = parser.parseObject(String.class);
            if (StringUtils.isEmpty(value)) {
                return null;
            }

            JSONArray list = JSON.parseArray(value);
            if (list.isEmpty()) {
                return null;
            }
            return new NodePath<>(list.stream().map(o -> (T) o).collect(Collectors.toList()));
        }

        @Override
        public int getFastMatchToken() {
            return 0 /*JSONToken.RBRACKET*/;
        }

        private static Class<?> getActualType(Type type) {
            if (type instanceof Class<?>) {
                return (Class<?>) type;
            } else if (type instanceof ParameterizedType) {
                // code.ponfee.commons.tree.NodePath<java.lang.Integer>
                return (Class<?>) ((ParameterizedType) type).getRawType();
            } else {
                throw new UnsupportedOperationException("Unsupported type: " + type);
            }
        }
    }

    // -----------------------------------------------------custom jackson deserialize
    public static class NodePathJacksonDeserializer<T extends Serializable & Comparable<? super T>> extends JsonDeserializer<NodePath<T>> {
        @Override @SuppressWarnings("unchecked")
        public NodePath<T> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            List<T> list = p.readValueAs(List.class);
            return CollectionUtils.isEmpty(list) ? null : new NodePath<>(list);
        }
    }

}
