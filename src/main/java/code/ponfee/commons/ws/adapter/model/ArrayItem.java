package code.ponfee.commons.ws.adapter.model;

/**
 * 封装数组对象
 * @author fupf
 * @param <T>
 */
public class ArrayItem<T> {
    private T[] item;

    public ArrayItem() {}

    public ArrayItem(T[] item) {
        this.item = item;
    }

    public T[] getItem() {
        return item;
    }

    public void setItem(T[] item) {
        this.item = item;
    }
}
