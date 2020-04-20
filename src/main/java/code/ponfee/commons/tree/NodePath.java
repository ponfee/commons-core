package code.ponfee.commons.tree;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotEmpty;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import code.ponfee.commons.tree.NodePath.NodePathFastjsonDeserializeMarker;
import code.ponfee.commons.tree.NodePath.NodePathJacksonDeserializer;

/**
 * Representing immutable node path array
 * 
 * @author Ponfee
 * @param <T> the node id type
 */
// NodePath is extends ArrayList, so must be use mappingTo in fastjson
// if not do it then deserialized json as a collection type(java.util.ArrayList)
@JSONType(mappingTo = NodePathFastjsonDeserializeMarker.class)
@JsonDeserialize(using = NodePathJacksonDeserializer.class)
public final class NodePath<T extends Serializable & Comparable<? super T>> 
    extends ArrayList<T> implements Serializable, Comparable<NodePath<T>>, Cloneable {

    private static final long serialVersionUID = 9090552044337950223L;

    @SuppressWarnings("unchecked")
    public NodePath(@NotEmpty T... path) {
        if (path == null || path.length == 0) {
            throw new NullPointerException();
        }
        Stream.of(path).forEach(super::add);
    }

    public NodePath(@NotEmpty List<T> path) {
        if (path == null || path.isEmpty()) {
            throw new NullPointerException();
        }
        super.addAll(path);
    }

    public NodePath(@NotEmpty NodePath<T> parent, T child) {
        if (parent == null || parent.isEmpty()) {
            throw new NullPointerException();
        }
        super.addAll(parent);
        super.add(child);
    }

    @Override
    public int compareTo(NodePath<T> o) {
        int min = Math.min(super.size(), o.size());
        for (int i = 0; i < min; i++) {
            int c = super.get(i).compareTo(o.get(i));
            if (c != 0) {
                return c;
            }
        }

        return super.size() - o.size();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof NodePath)
            && ListUtils.isEqualList(this, (NodePath<?>) obj);
    }

    @Override
    public NodePath<T> clone() {
        return SerializationUtils.clone(this);
    }

    // --------------------------------------------------------------------------override list methods
    @Override
    public final List<T> subList(int fromIndex, int toIndex) {
        return new UnmodifiableList<>(super.subList(fromIndex, toIndex));
    }

    @Override
    public final Iterator<T> iterator() {
        return listIterator(0);
    }

    @Override
    public final ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @Override
    public final ListIterator<T> listIterator(int index) {
        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        return new UnmodifiableListIterator(index);
    }

    // --------------------------------------------------------------------------inner class
    private class UnmodifiableListIterator implements ListIterator<T> {
        int cursor;

        UnmodifiableListIterator(int cursor) {
            this.cursor = cursor;
        }

        @Override
        public boolean hasNext() {
            return size() > 0 && cursor < size();
        }

        @Override
        public T next() {
            return get(cursor++);
        }

        @Override
        public boolean hasPrevious() {
            return cursor > 0 && cursor <= size();
        }

        @Override
        public T previous() {
            return get(--cursor);
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(T e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(T e) {
            throw new UnsupportedOperationException();
        }
    }

    // --------------------------------------------------------------------------unsupported operation
    @Override @Deprecated
    public boolean add(T e) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    protected void removeRange(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void replaceAll(UnaryOperator<T> operator) {
        throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public void sort(Comparator<? super T> c) {
        throw new UnsupportedOperationException();
    }

    // -----------------------------------------------------custom fastjson deserialize
    @JSONType(deserializer = NodePathFastjsonDeserializer.class)
    public static class NodePathFastjsonDeserializeMarker {}

    public static class NodePathFastjsonDeserializer<T extends Serializable & Comparable<? super T>> 
        implements ObjectDeserializer {
        @Override @SuppressWarnings("unchecked")
        public NodePath<T> deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            if ((type = getActualType(type)) != NodePath.class) {
                throw new UnsupportedOperationException(
                    "Only supported deserialize NodePath, cannot supported: " + type
                );
            }

            String value = parser.parseObject(String.class);
            if (StringUtils.isEmpty(value) ) {
                return null;
            }

            JSONArray list = JSON.parseArray(value);
            if (list.isEmpty()) {
                return null;
            }
            return new NodePath<>(
                list.stream().map(o -> (T) o).collect(Collectors.toList())
            );
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

    /*public static class IntegerNodePath implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        // 当字段有泛型参数时的类型信息type为ParameterizedType，
        // 所以必须用JSONField注解，否则当成Collection来解析
        @JSONField(deserializeUsing = NodePathFastjsonDeserializer.class)
        private NodePath<Integer> path;

        public NodePath<Integer> getPath() {
            return path;
        }

        public void setPath(NodePath<Integer> path) {
            this.path = path;
        }
    }*/

    // -----------------------------------------------------custom jackson deserialize
    public static class NodePathJacksonDeserializer<T extends Serializable & Comparable<? super T>>
        extends JsonDeserializer<NodePath<T>> {
        @Override @SuppressWarnings("unchecked")
        public NodePath<T> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            List<T> list = p.readValueAs(List.class);
            return CollectionUtils.isEmpty(list) ? null : new NodePath<>(list);
        }
    }

}
