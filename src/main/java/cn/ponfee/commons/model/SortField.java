/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.model;

import cn.ponfee.commons.model.SortOrder.NullHandling;

/**
 * SortOrder and Field
 * 
 * @author Ponfee
 */
public class SortField implements java.io.Serializable {

    private static final long serialVersionUID = -2400506091734529951L;

    private final String field;
    private final SortOrder sortOrder;
    private final boolean ignoreCase;
    private final NullHandling nullHandling;

    public SortField(String field, SortOrder sortOrder) {
        this(field, sortOrder, false, null);
    }

    public SortField(String field, SortOrder sortOrder, 
                     boolean ignoreCase, NullHandling nullHandling) {
        this.field = field;
        this.sortOrder = sortOrder;
        this.ignoreCase = ignoreCase;
        this.nullHandling = nullHandling;
    }

    public String getField() {
        return field;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public NullHandling getNullHandling() {
        return nullHandling;
    }

}
