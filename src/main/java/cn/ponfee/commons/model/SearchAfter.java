/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.model;

/**
 * Search after
 * 
 * @author Ponfee
 */
public class SearchAfter<T> implements java.io.Serializable {

    private static final long serialVersionUID = 4870755106055211046L;

    private final SortField sortField;
    private final T value;

    public SearchAfter(SortField sortField, T value) {
        this.sortField = sortField;
        this.value = value;
    }

    public SortField getSortField() {
        return sortField;
    }

    public T getValue() {
        return value;
    }

    public SearchAfter<T> copy(T value) {
        return new SearchAfter<>(this.sortField, value);
    }

}
