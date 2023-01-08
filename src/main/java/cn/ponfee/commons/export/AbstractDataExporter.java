/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.export;

import cn.ponfee.commons.reflect.Fields;
import cn.ponfee.commons.tree.FlatNode;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Exports abstract class
 * 
 * @author Ponfee
 */
public abstract class AbstractDataExporter<T> implements DataExporter<T> {

    public static final int AWAIT_TIME_MILLIS = 47;

    private boolean empty = true;
    private String name; // report name: non thread safe

    @Override
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public void close() {
        // nothing to do
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

    protected final <E> void rollingTbody(Table<E> table, BiConsumer<Object[], Integer> action) {
        try {
            E data; Function<E, Object[]> converter;
            if ((converter = table.getConverter()) != null) {
                for (int i = 0; table.isNotEnd();) {
                    if ((data = table.getRow(AWAIT_TIME_MILLIS)) != null) {
                        action.accept(converter.apply(data), i++);
                    }
                }
            } else {
                String[] fields = table.getThead()
                                       .stream()
                                       .filter(FlatNode::isLeaf)
                                       .map(f -> f.getAttach().getField())
                                       .toArray(String[]::new);
                Object[] array;
                for (int i = 0; table.isNotEnd();) {
                    if ((data = table.getRow(AWAIT_TIME_MILLIS)) != null) {
                        if (data instanceof Object[]) {
                            array = (Object[]) data;
                        } else if (data.getClass().isArray()) {
                            array = covariantArray(data);
                        } else if (data instanceof Collection<?>) {
                            array = collection2array((Collection<?>) data);
                        } else if (data instanceof Iterable<?>) {
                            array = iterable2array((Iterable<?>) data);
                        } else if (data instanceof Iterator<?>) {
                            array = iterator2array((Iterator<?>) data);
                        } else if (data instanceof Map<?, ?>) {
                            array = map2array((Map<?, ?>) data);
                        } else if (data instanceof Dictionary<?, ?>) {
                            array = dictionary2array((Dictionary<?, ?>) data);
                        } else {
                            array = bean2array(data, fields);
                        }
                        action.accept(array, i++);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    protected final List<Thead> getLeafThead(List<FlatNode<Integer, Thead>> thead) {
        return thead.stream()
                    .filter(FlatNode::isLeaf)
                    .map(FlatNode::getAttach)
                    .collect(Collectors.toList());
    }

    private static Object[] collection2array(Collection<?> coll) {
        Object[] array = new Object[coll.size()];
        int i = 0;
        for (Object obj : coll) {
            array[i++] = obj;
        }
        return array;
    }

    private static Object[] iterable2array(Iterable<?> iterable) {
        List<Object> list = new LinkedList<>();
        iterable.forEach(list::add);
        return list.toArray();
    }

    private static Object[] iterator2array(Iterator<?> iter) {
        List<Object> list = new LinkedList<>();
        while (iter.hasNext()) {
            list.add(iter.next());
        }
        return list.toArray();
    }

    private static Object[] covariantArray(Object array0) {
        int size = Array.getLength(array0);
        Object[] array = new Object[size];
        for (int i = 0; i < size; i++) {
            array[i] = Array.get(array0, i);
        }
        return array;
    }

    private static Object[] map2array(Map<?, ?> map) {
        List<Object> list = new LinkedList<>();
        map.forEach((k, v) -> list.add(v));
        return list.toArray();
    }

    private static Object[] dictionary2array(Dictionary<?, ?> dic) {
        List<Object> list = new LinkedList<>();
        Enumeration<?> enu = dic.elements();
        while (enu.hasMoreElements()) {
            list.add(enu.nextElement());
        }
        return list.toArray();
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
