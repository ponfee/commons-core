package code.ponfee.commons.export;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import code.ponfee.commons.reflect.Fields;
import code.ponfee.commons.tree.FlatNode;

/**
 * Exports abstract class
 * 
 * @author fupf
 */
public abstract class AbstractDataExporter<T> implements DataExporter<T> {

    public static final int AWAIT_TIME_MILLIS = 7;

    private boolean empty = true;
    private String name; // report name: non thread safe

    @Override
    public boolean isEmpty() {
        return empty;
    }

    public final void nonEmpty() {
        this.empty = false;
    }

    public final AbstractDataExporter<T> setName(String name) {
        this.name = name;
        return this;
    }

    public final String getName() {
        return name;
    }

    protected final <E> void rollingTbody(Table<E> table,
        BiConsumer<Object[], Integer> action) {
        try {
            E data; Function<E, Object[]> converter;
            if ((converter = table.getConverter()) != null) {
                for (int i = 0; table.isNotEnd();) {
                    if ((data = table.poll()) != null) {
                        action.accept(converter.apply(data), i++);
                    } else {
                        Thread.sleep(AWAIT_TIME_MILLIS);
                    }
                }
            } else {
                String[] fields = getLeafThead(
                    table.getThead()
                ).stream().map(
                    Thead::getField
                ).toArray(
                    String[]::new
                );
                Object[] array;
                for (int i = 0; table.isNotEnd();) {
                    if ((data = table.poll()) != null) {
                        if (data instanceof Object[]) {
                            array = (Object[]) data;
                        } else if (data.getClass().isArray()) {
                            array = array2array(data);
                        } else if (data instanceof Map<?, ?>) {
                            array = map2array((Map<?, ?>) data);
                        } else {
                            array = bean2array(data, fields);
                        }
                        action.accept(array, i++);
                    } else {
                        Thread.sleep(AWAIT_TIME_MILLIS);
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected final List<Thead> getLeafThead(
        List<FlatNode<Integer, Thead>> thead) {
        return thead.stream()
                    .filter(FlatNode::isLeaf)
                    .map(FlatNode::getAttach)
                    .collect(Collectors.toList());
    }

    private static Object[] array2array(Object array0) {
        int size = Array.getLength(array0);
        Object[] array = new Object[size];
        for (int i = 0; i < size; i++) {
            array[i] = Array.get(array0, i);
        }
        return array;
    }

    private static Object[] map2array(Map<?, ?> map) {
        return map.entrySet().stream().map(Entry::getValue).toArray();
    }

    private static Object[] bean2array(Object bean, String[] fields) {
        int size = fields.length;
        Object[] array = new Object[size];
        for (int i = 0; i < size; i++) {
            // must be setting field
            array[i] = Fields.get(bean, fields[i]);
        }
        return array;
    }

}
