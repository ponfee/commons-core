/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.ws.adapter.model;

/**
 * cannot with generic like as <code>MapItem<K,V></code>
 * ParameterizedTypeImpl cannot be cast to TypeVariable
 * 
 * @author Ponfee
 */
@SuppressWarnings("rawtypes")
public class MapItem {
    private MapEntry[] item;

    public MapItem() {}

    public MapItem(MapEntry[] item) {
        this.item = item;
    }

    public MapEntry[] getItem() {
        return item;
    }

    public void setItem(MapEntry[] item) {
        this.item = item;
    }
}
