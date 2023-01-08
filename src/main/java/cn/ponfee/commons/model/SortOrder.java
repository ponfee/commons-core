/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.model;

/**
 * Query Order by
 * 
 * @author Ponfee
 */
public enum SortOrder {

    ASC, DESC;

    public static SortOrder of(String name) {
        return "ASC".equalsIgnoreCase(name) ? ASC : DESC;
    }

    public enum NullHandling {
        NATIVE, NULLS_FIRST, NULLS_LAST
    }

}
