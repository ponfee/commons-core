package code.ponfee.commons.export;

import java.lang.reflect.Array;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import code.ponfee.commons.reflect.Fields;
import code.ponfee.commons.tree.FlatNode;

/**
 * 导出
 * @author fupf
 */
public abstract class AbstractExporter<T> implements DataExporter<T> {

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

    public final AbstractExporter<T> setName(String name) {
        this.name = name;
        return this;
    }

    public final String getName() {
        return name;
    }

    protected final void rollingTbody(Table table,
        BiConsumer<Object[], Integer> action) {
        try {
            Object data; Function<Object, Object[]> converter;
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
                            array = toArray(data);
                        } else {
                            array = toArray(data, fields);
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

    private static Object[] toArray(Object array0) {
        int size = Array.getLength(array0);
        Object[] array = new Object[size];
        for (int i = 0; i < size; i++) {
            array[i] = Array.get(array0, i);
        }
        return array;
    }

    private static Object[] toArray(Object bean, String[] fields) {
        int size = fields.length;
        Object[] array = new Object[size];
        for (int i = 0; i < size; i++) {
            // must be setting field
            array[i] = Fields.get(bean, fields[i]);
        }
        return array;
    }

}
