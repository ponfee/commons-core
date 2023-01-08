/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.ws.adapter.model;

/**
 * 封装数组对象
 * 
 * @author Ponfee
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
