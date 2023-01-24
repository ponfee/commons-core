/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.schema;

import cn.ponfee.commons.exception.ServerException;
import cn.ponfee.commons.json.Jsons;
import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Collections;
import java.util.Optional;

/**
 * 数据格式处理
 * 
 * @author Ponfee
 */
public enum DataStructures {

    /** 标准 */
    NORMAL(NormalStructure.class) {
        @Override
        public NormalStructure empty() {
            return new NormalStructure(0);
        }
    }, // 

    /** 表格 */
    TABLE(TableStructure.class) {
        private final DataColumn[] empty = new DataColumn[0];

        @Override
        public DataStructure empty() {
            return new TableStructure(empty, Collections.emptyList());
        }

        @Override
        public DataStructure parse(String text) {
            TableStructure table = (TableStructure) Jsons.fromJson(text, this.type());
            if (table.getColumns() == null && table.getDataset() == null) {
                throw new IllegalArgumentException("Invalid table structure: " + text);
            }
            return table;
        }
    }, //

    /** 原文 */
    PLAIN(PlainStructure.class) {
        private final PlainStructure empty = new PlainStructure("");

        @Override
        public DataStructure empty() {
            return empty;
        }
    }, //

    ;

    private static final DataStructures DEFAULT_STRUCTURE = NORMAL;

    private final Class<? extends DataStructure> type;

    DataStructures(Class<? extends DataStructure> type) {
        this.type = type;
    }

    public DataStructure parse(String text) {
        return Jsons.fromJson(text, this.type());
    }

    public abstract DataStructure empty();

    public Class<? extends DataStructure> type() {
        return this.type;
    }

    public static DataStructures ofType(Class<? extends DataStructure> type) {
        if (type == null) {
            return DEFAULT_STRUCTURE;
        }

        for (DataStructures ds : DataStructures.values()) {
            if (ds.type == type) {
                return ds;
            }
        }

        throw new UnsupportedOperationException("Unknown structure type: " + type);
    }

    public static DataStructures ofName(String name) {
        if (StringUtils.isBlank(name)) {
            return DEFAULT_STRUCTURE;
        }

        for (DataStructures ds : DataStructures.values()) {
            if (ds.name().equalsIgnoreCase(name)) {
                return ds;
            }
        }

        throw new UnsupportedOperationException("Unknown structure type: " + name);
    }

    public static DataStructure empty(String name) {
        return ofName(name).empty();
    }

    // ---------------------------------------------------------------------------detect text to data structure

    public static DataStructure detect(String text, boolean strict) throws ParseException {
        for (DataStructures ds : DataStructures.values()) {
            try {
                return ds.parse(text);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }

        if (!strict) {
            return new PlainStructure(text);
        }

        throw new ParseException("Unresolvable text data: " + text, 0);
    }

    // ---------------------------------------------------------------------------convert source structure to target structure

    public static <S extends DataStructure, T extends DataStructure> T convert(S source, Class<T> targetType) {
        return convert(source, ofType(targetType).name());
    }

    public static <S extends DataStructure, T extends DataStructure> T convert(S source, DataStructures targetType) {
        return convert(source, (targetType == null ? DEFAULT_STRUCTURE : targetType).name());
    }

    @SuppressWarnings("unchecked")
    public static <S extends DataStructure, T extends DataStructure> T convert(S source, String structure) {
        if (source == null) {
            return null;
        }

        DataStructures sourceType = ofType(source.getClass());
        String structure0 = Optional.ofNullable(structure).filter(StringUtils::isNotBlank)
                                    .map(String::toUpperCase).orElse(DEFAULT_STRUCTURE.name());
        if (structure0.equals(sourceType.name())) {
            return (T) source;
        }

        // toNormal(), toTable(), toPlain()
        String methodName = "to" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, structure0);

        Method method;
        try {
            method = source.getClass().getDeclaredMethod(methodName);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Unknown structure type: " + structure, e);
        }

        try {
            return (T) method.invoke(source);
        } catch (Exception e) {
            if (StringUtils.isBlank(structure)) {
                return (T) source.toPlain();
            }
            throw new ServerException("Structure type convert failed, expect: " + structure + ", actual: " + sourceType.name(), e);
        }
    }

}
