package code.ponfee.commons.mybatis;
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mybatis - 获取Mybatis查询sql工具
 *
 * https://gitee.com/free/Mybatis_Utils/tree/master/SqlHelper
 * 
 * <code>
 * SqlHelper.getNamespaceSql(
 *   sqlSession, 
 *   "com.github.pagehelper.mapper.CountryMapper.listAll"
 * )
 *             ||
 *             \/
 * SELECT * FROM t_sched_job
 * </code>
 *
 *
 * <code>
 * sqlHelper.getMapperSql(
 *   sqlSession,
 *   "code.ponfee.dao.mapper.SchedJobMapper.query4page",
 *   Arrays.asList(1, 2),
 *   Arrays.asList(3, 4),
 *   "id"
 * )
 *             ||
 *             \/
 * SELECT * FROM t_sched_log 
 * WHERE status IN (1,2) AND id NOT IN (3,4)
 * ORDER BY id
 * </code>
 * 
 * @author liuzh/abel533/isea533
 */
public class SqlHelper {

    /**
     * 通过接口获取sql
     *
     * @param mapper
     * @param methodName
     * @param args
     * @return
     */
    public static String getMapperSql(Object mapper, String methodName, Object... args) {
        MetaObject metaObject = SystemMetaObject.forObject(mapper);
        SqlSession session = (SqlSession) metaObject.getValue("h.sqlSession");
        Class<?> mapperInterface = (Class<?>) metaObject.getValue("h.mapperInterface");
        String fullMethodName = mapperInterface.getCanonicalName() + "." + methodName;
        if (args == null || args.length == 0) {
            return getNamespaceSql(session, fullMethodName, null);
        } else {
            return getMapperSql(session, mapperInterface, methodName, args);
        }
    }

    /**
     * 通过Mapper方法名获取sql
     *
     * @param session
     * @param fullMapperMethodName
     * @param args
     * @return
     */
    public static String getMapperSql(SqlSession session, String fullMapperMethodName, Object... args) {
        if (args == null || args.length == 0) {
            return getNamespaceSql(session, fullMapperMethodName, null);
        }
        String methodName = fullMapperMethodName.substring(fullMapperMethodName.lastIndexOf('.') + 1);
        Class<?> mapperInterface;
        try {
            mapperInterface = Class.forName(fullMapperMethodName.substring(0, fullMapperMethodName.lastIndexOf('.')));
            return getMapperSql(session, mapperInterface, methodName, args);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("参数" + fullMapperMethodName + "无效！");
        }
    }

    /**
     * 通过Mapper接口和方法名
     *
     * @param session
     * @param mapperInterface
     * @param methodName
     * @param args
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String getMapperSql(SqlSession session, Class<?> mapperInterface, 
                                      String methodName, Object... args) {
        String fullMapperMethodName = mapperInterface.getCanonicalName() + "." + methodName;
        if (args == null || args.length == 0) {
            return getNamespaceSql(session, fullMapperMethodName, null);
        }
        Method method = getDeclaredMethods(mapperInterface, methodName);
        Map<String, Object> params = new HashMap<>();
        Class<?>[] argTypes = method.getParameterTypes();
        for (int i = 0; i < argTypes.length; i++) {
            if (   !RowBounds.class.isAssignableFrom(argTypes[i]) 
                && !ResultHandler.class.isAssignableFrom(argTypes[i])) {
                String paramName = "param" + (params.size() + 1);
                paramName = getParamNameFromAnnotation(method, i, paramName);
                params.put(paramName, i >= args.length ? null : args[i]);
            }
        }
        if (args != null && args.length == 1) {
            Object params0 = wrapCollection(args[0]);
            if (params0 instanceof Map) {
                params.putAll((Map<String, ?>) params0);
            }
        }
        return getNamespaceSql(session, fullMapperMethodName, params);
    }

    /**
     * 通过命名空间方式获取sql
     *
     * @param session
     * @param namespace
     * @return
     */
    public static String getNamespaceSql(SqlSession session, String namespace) {
        return getNamespaceSql(session, namespace, null);
    }

    /**
     * 通过命名空间方式获取sql
     *
     * @param session
     * @param namespace
     * @param params
     * @return
     */
    public static String getNamespaceSql(SqlSession session, String namespace, Object params) {
        params = wrapCollection(params);
        Configuration configuration = session.getConfiguration();
        MappedStatement mappedStatement = configuration.getMappedStatement(namespace);
        TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
        BoundSql boundSql = mappedStatement.getBoundSql(params);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql();
        if (parameterMappings != null) {
            for (ParameterMapping parameterMapping : parameterMappings) {
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (params == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(params.getClass())) {
                        value = params;
                    } else {
                        MetaObject metaObject = configuration.newMetaObject(params);
                        value = metaObject.getValue(propertyName);
                    }
                    JdbcType jdbcType = parameterMapping.getJdbcType();
                    if (value == null && jdbcType == null) {
                        jdbcType = configuration.getJdbcTypeForNull();
                    }
                    sql = replaceParameter(sql, value, jdbcType, parameterMapping.getJavaType());
                }
            }
        }
        return sql;
    }

    /**
     * 根据类型替换参数
     * 仅作为数字和字符串两种类型进行处理，需要特殊处理的可以继续完善这里
     *
     * @param sql
     * @param value
     * @param jdbcType
     * @param javaType
     * @return
     */
    private static String replaceParameter(String sql, Object value, 
                                           JdbcType jdbcType, Class<?> javaType) {
        String strValue = String.valueOf(value);
        if (jdbcType != null) {
            switch (jdbcType) {
                //数字
                case BIT:
                case TINYINT:
                case SMALLINT:
                case INTEGER:
                case BIGINT:
                case FLOAT:
                case REAL:
                case DOUBLE:
                case NUMERIC:
                case DECIMAL:
                    break;
                //日期
                case DATE:
                case TIME:
                case TIMESTAMP:
                    //其他，包含字符串和其他特殊类型
                default:
                    strValue = "'" + strValue + "'";

            }
        } else if (Number.class.isAssignableFrom(javaType)) {
            //不加单引号
        } else {
            strValue = "'" + strValue + "'";
        }
        return sql.replaceFirst("\\?", strValue);
    }

    /**
     * 获取指定的方法
     *
     * @param clazz
     * @param methodName
     * @return
     */
    private static Method getDeclaredMethods(Class<?> clazz, String methodName) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new IllegalArgumentException("方法" + methodName + "不存在！");
    }

    /**
     * 获取参数注解名
     *
     * @param method
     * @param i
     * @param paramName
     * @return
     */
    private static String getParamNameFromAnnotation(Method method, int i, String paramName) {
        final Object[] paramAnnos = method.getParameterAnnotations()[i];
        for (Object paramAnno : paramAnnos) {
            if (paramAnno instanceof Param) {
                paramName = ((Param) paramAnno).value();
            }
        }
        return paramName;
    }

    /**
     * 简单包装参数
     *
     * @param object
     * @return
     */
    private static Object wrapCollection(final Object object) {
        if (object instanceof List) {
            Map<String, Object> map = new HashMap<>();
            map.put("list", object);
            return map;
        } else if (object != null && object.getClass().isArray()) {
            Map<String, Object> map = new HashMap<>();
            map.put("array", object);
            return map;
        }
        return object;
    }
}
