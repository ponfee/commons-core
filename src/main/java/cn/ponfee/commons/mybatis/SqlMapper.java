package cn.ponfee.commons.mybatis;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static cn.ponfee.commons.util.ObjectUtils.typeOf;

/**
 * <pre>
 * MyBatis执行sql工具，在写SQL的时候建议使用参数形式的可以是${}或#{}
 * 不建议将参数直接拼到字符串中，当大量这么使用的时候由于缓存MappedStatement而占用更多的内存
 * https://gitee.com/free/Mybatis_Utils/tree/master/SqlMapper
 *
 * Mybatis-generator、通用Mapper、Mybatis-Plus对比：
 *   https://www.jianshu.com/p/7be6da536f8f
 *   https://blog.csdn.net/m0_37524586/article/details/88351833
 *
 * </pre>
 *
 * @author liuzh
 * @since 2015-03-10
 */
public class SqlMapper {

    private final SqlSession sqlSession;
    private final MSUtils msUtils;

    /**
     * 构造方法，默认缓存MappedStatement
     *
     * SqlSessionTemplate implements SqlSession
     * 
     * @param sqlSession
     */
    public SqlMapper(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
        this.msUtils = new MSUtils(sqlSession.getConfiguration());
    }

    /**
     * 查询返回一个结果，多个结果时抛出异常
     *
     * @param sql 执行的sql
     * @return
     */
    public Map<String, Object> selectOne(String sql) {
        return asSingleItem(selectList(sql));
    }

    /**
     * 查询返回一个结果，多个结果时抛出异常
     *
     * @param sql   执行的sql
     * @param param 参数
     * @return
     */
    public Map<String, Object> selectOne(String sql, Object param) {
        return asSingleItem(selectList(sql, param));
    }

    /**
     * 查询返回一个结果，多个结果时抛出异常
     *
     * @param sql        执行的sql
     * @param resultType 返回的结果类型
     * @return
     */
    public <T> T selectOne(String sql, Class<T> resultType) {
        return asSingleItem(selectList(sql, resultType));
    }

    /**
     * 查询返回一个结果，多个结果时抛出异常
     *
     * @param sql        执行的sql
     * @param param      参数
     * @param resultType 返回的结果类型
     * @return
     */
    public <T> T selectOne(String sql, Object param, Class<T> resultType) {
        return asSingleItem(selectList(sql, param, resultType));
    }

    /**
     * 查询返回List<Map<String, Object>>
     *
     * @param sql 执行的sql
     * @return
     */
    public List<Map<String, Object>> selectList(String sql) {
        return sqlSession.selectList(msUtils.select(sql));
    }

    /**
     * 查询返回List<Map<String, Object>>
     *
     * @param sql   执行的sql
     * @param param 参数
     * @return
     */
    public List<Map<String, Object>> selectList(String sql, Object param) {
        return sqlSession.selectList(
            msUtils.selectDynamic(sql, typeOf(param)), param
        );
    }

    /**
     * 查询返回指定的结果类型
     *
     * @param sql        执行的sql
     * @param resultType 返回的结果类型
     * @return
     */
    public <T> List<T> selectList(String sql, Class<T> resultType) {
        String msId = resultType == null 
                    ? msUtils.select(sql) 
                    : msUtils.select(sql, resultType);
        return sqlSession.selectList(msId);
    }

    /**
     * 查询返回指定的结果类型
     *
     * @param sql        执行的sql
     * @param param      参数
     * @param resultType 返回的结果类型
     * @return
     */
    public <T> List<T> selectList(String sql, Object param, Class<T> resultType) {
        String msId = resultType == null
                    ? msUtils.selectDynamic(sql, typeOf(param))
                    : msUtils.selectDynamic(sql, typeOf(param), resultType);
        return sqlSession.selectList(msId, param);
    }

    /**
     * <pre>
     *  Query full scroll data
     *  
     *  &lt;script&gt;
     *    SELECT id, name FROM t_test 
     *    WHERE name IS NOT NULL &lt;if test='id!=null'&gt;AND id&gt;#{id}&lt;/if&gt;
     *    ORDER BY id ASC 
     *    LIMIT 5000
     *  &lt;/script&gt;
     * </pre>
     * 
     * @param sql        the mybatis sql script
     * @param param      the param
     * @param resultType the result type
     * @param action     the action
     */
    public <P, R> void selectScroll(String sql, P param, Class<R> resultType, 
                                    BiFunction<P, List<R>, P> action) {
        List<R> list; boolean hasNext;
        do {
            list = selectList(sql, param, resultType);
            hasNext = CollectionUtils.isNotEmpty(list);
            if (hasNext) {
                param = action.apply(param, list);
            }
        } while (hasNext);
    }

    /**
     * 插入数据
     *
     * @param sql 执行的sql
     * @return
     */
    public int insert(String sql) {
        return sqlSession.insert(msUtils.insert(sql));
    }

    /**
     * 插入数据
     *
     * @param sql   执行的sql
     * @param param 参数
     * @return
     */
    public int insert(String sql, Object param) {
        return sqlSession.insert(
            msUtils.insertDynamic(sql, typeOf(param)), param
        );
    }

    /**
     * 更新数据
     *
     * @param sql 执行的sql
     * @return
     */
    public int update(String sql) {
        return sqlSession.update(msUtils.update(sql));
    }

    /**
     * 更新数据
     *
     * @param sql   执行的sql
     * @param param 参数
     * @return
     */
    public int update(String sql, Object param) {
        return sqlSession.update(
            msUtils.updateDynamic(sql, typeOf(param)), param
        );
    }

    /**
     * 删除数据
     *
     * @param sql 执行的sql
     * @return
     */
    public int delete(String sql) {
        return sqlSession.delete(msUtils.delete(sql));
    }

    /**
     * 删除数据
     *
     * @param sql   执行的sql
     * @param param 参数
     * @return
     */
    public int delete(String sql, Object param) {
        return sqlSession.delete(
            msUtils.deleteDynamic(sql, typeOf(param)), param
        );
    }

    /**
     * 获取List中最多只有一个的数据
     *
     * @param list List结果
     * @param <T>  泛型类型
     * @return
     */
    private <T> T asSingleItem(List<T> list) {
        int rowSize = list == null ? 0 : list.size();
        if (rowSize > 1) {
            throw new TooManyResultsException("Expected one row (or null) to be returned by selectOne(), but found: " + list.size());
        }
        return rowSize == 1 ? list.get(0) : null;
    }

    // ---------------------------------------------------------------------private class
    private static class MSUtils {
        private final Configuration configuration;
        private final LanguageDriver languageDriver;

        private MSUtils(Configuration configuration) {
            this.configuration = configuration;
            this.languageDriver = configuration.getDefaultScriptingLanguageInstance();
        }

        /**
         * 创建MSID
         *
         * @param sql 执行的sql
         * @param sql 执行的sqlCommandType
         * @return
         */
        private String newMsId(String sql, SqlCommandType sqlCommandType) {
            return new StringBuilder(sqlCommandType.toString())
                .append(".").append(sql.hashCode()).toString();
        }

        /**
         * 是否已经存在该ID
         *
         * @param msId
         * @return
         */
        private boolean hasMappedStatement(String msId) {
            return configuration.hasStatement(msId, false);
        }

        /**
         * 创建一个查询的MS
         *
         * @param msId
         * @param sqlSource  执行的sqlSource
         * @param resultType 返回的结果类型
         */
        private void newSelectMappedStatement(String msId, SqlSource sqlSource, final Class<?> resultType) {
            List<ResultMap> list = Collections.singletonList(
                new ResultMap.Builder(
                    configuration, "defaultResultMap", resultType, new ArrayList<>(0)
                ).build()
            );
            MappedStatement ms = new MappedStatement.Builder(
                configuration, msId, sqlSource, SqlCommandType.SELECT
            ).resultMaps(list).build();
            //缓存
            configuration.addMappedStatement(ms);
        }

        /**
         * 创建一个简单的MS
         *
         * @param msId
         * @param sqlSource      执行的sqlSource
         * @param sqlCommandType 执行的sqlCommandType
         */
        private void newUpdateMappedStatement(String msId, SqlSource sqlSource, 
                                              SqlCommandType sqlCommandType) {
            List<ResultMap> list = Collections.singletonList(
                new ResultMap.Builder(configuration, "defaultResultMap", int.class, new ArrayList<>(0)).build()
            );
            MappedStatement ms = new MappedStatement.Builder(
                configuration, msId, sqlSource, sqlCommandType
            ).resultMaps(list).build();
            configuration.addMappedStatement(ms);
        }

        private String select(String sql) {
            String msId = newMsId(sql, SqlCommandType.SELECT);
            if (hasMappedStatement(msId)) {
                return msId;
            }
            StaticSqlSource sqlSource = new StaticSqlSource(configuration, sql);
            newSelectMappedStatement(msId, sqlSource, Map.class);
            return msId;
        }

        private String selectDynamic(String sql, Class<?> parameterType) {
            String msId = newMsId(sql + parameterType, SqlCommandType.SELECT);
            if (hasMappedStatement(msId)) {
                return msId;
            }
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, parameterType);
            newSelectMappedStatement(msId, sqlSource, Map.class);
            return msId;
        }

        private String select(String sql, Class<?> resultType) {
            String msId = newMsId(resultType + sql, SqlCommandType.SELECT);
            if (hasMappedStatement(msId)) {
                return msId;
            }
            StaticSqlSource sqlSource = new StaticSqlSource(configuration, sql);
            newSelectMappedStatement(msId, sqlSource, resultType);
            return msId;
        }

        private String selectDynamic(String sql, Class<?> parameterType, Class<?> resultType) {
            String msId = newMsId(resultType + sql + parameterType, SqlCommandType.SELECT);
            if (hasMappedStatement(msId)) {
                return msId;
            }
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, parameterType);
            newSelectMappedStatement(msId, sqlSource, resultType);
            return msId;
        }

        private String insert(String sql) {
            String msId = newMsId(sql, SqlCommandType.INSERT);
            if (hasMappedStatement(msId)) {
                return msId;
            }
            StaticSqlSource sqlSource = new StaticSqlSource(configuration, sql);
            newUpdateMappedStatement(msId, sqlSource, SqlCommandType.INSERT);
            return msId;
        }

        private String insertDynamic(String sql, Class<?> parameterType) {
            String msId = newMsId(sql + parameterType, SqlCommandType.INSERT);
            if (hasMappedStatement(msId)) {
                return msId;
            }
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, parameterType);
            newUpdateMappedStatement(msId, sqlSource, SqlCommandType.INSERT);
            return msId;
        }

        private String update(String sql) {
            String msId = newMsId(sql, SqlCommandType.UPDATE);
            if (hasMappedStatement(msId)) {
                return msId;
            }
            StaticSqlSource sqlSource = new StaticSqlSource(configuration, sql);
            newUpdateMappedStatement(msId, sqlSource, SqlCommandType.UPDATE);
            return msId;
        }

        private String updateDynamic(String sql, Class<?> parameterType) {
            String msId = newMsId(sql + parameterType, SqlCommandType.UPDATE);
            if (hasMappedStatement(msId)) {
                return msId;
            }
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, parameterType);
            newUpdateMappedStatement(msId, sqlSource, SqlCommandType.UPDATE);
            return msId;
        }

        private String delete(String sql) {
            String msId = newMsId(sql, SqlCommandType.DELETE);
            if (hasMappedStatement(msId)) {
                return msId;
            }
            StaticSqlSource sqlSource = new StaticSqlSource(configuration, sql);
            newUpdateMappedStatement(msId, sqlSource, SqlCommandType.DELETE);
            return msId;
        }

        private String deleteDynamic(String sql, Class<?> parameterType) {
            String msId = newMsId(sql + parameterType, SqlCommandType.DELETE);
            if (hasMappedStatement(msId)) {
                return msId;
            }
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, parameterType);
            newUpdateMappedStatement(msId, sqlSource, SqlCommandType.DELETE);
            return msId;
        }
    }

}
