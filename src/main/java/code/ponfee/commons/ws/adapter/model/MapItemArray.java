package code.ponfee.commons.ws.adapter.model;

/**
 * 封装MapItem数组
 * @author fupf
 */
public class MapItemArray {
    private MapItem[] items;

    public MapItemArray() {}

    public MapItemArray(MapItem[] items) {
        this.items = items;
    }

    public MapItem[] getItems() {
        return items;
    }

    public void setItems(MapItem[] items) {
        this.items = items;
    }
}
