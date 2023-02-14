/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.model;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;
import java.util.Map;

/**
 * Typed {@link LinkedMultiValueMap} with pre-defined get methods
 * 
 * @author Ponfee
 */
public class TypedLinkedMultiValueMap<K, V> extends LinkedMultiValueMap<K, V> implements RemovableTypedKeyValue<K, V> {

    private static final long serialVersionUID = 4369022038293264189L;

    public TypedLinkedMultiValueMap() {
        super();
    }

    public TypedLinkedMultiValueMap(int initialCapacity) {
        super(initialCapacity);
    }

    public TypedLinkedMultiValueMap(Map<K, List<V>> otherMap) {
        super(otherMap);
    }

    @Override
    public V getValue(K key) {
        return getFirst(key);
    }

    @Override
    public V removeKey(K key) {
        List<V> values = remove(key);
        return CollectionUtils.isEmpty(values) ? null : values.get(0);
    }

}
