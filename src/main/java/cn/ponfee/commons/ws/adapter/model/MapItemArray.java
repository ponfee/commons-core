/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.ws.adapter.model;

/**
 * 封装MapItem数组
 * 
 * @author Ponfee
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
