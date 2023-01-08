/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed {@link LinkedHashMap} with pre-defined get methods
 * 
 * @author Ponfee
 */
public class TypedLinkedHashMap<K, V> extends LinkedHashMap<K, V> implements TypedMap<K, V> {

    private static final long serialVersionUID = -4207327688392334942L;

    public TypedLinkedHashMap() {
        super();
    }

    public TypedLinkedHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public TypedLinkedHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

}
