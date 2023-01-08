/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.mybatis;

import cn.ponfee.commons.data.lookup.MultipleDataSourceContext;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.*;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.dao.support.PersistenceExceptionTranslator;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.apache.ibatis.reflection.ExceptionUtil.unwrapThrowable;
import static org.mybatis.spring.SqlSessionUtils.*;

/**
 * Mutiple datasource for SqlSessionTemplate
 * 
 * @author Ponfee
 */
public class MultipleSqlSessionTemplate extends SqlSessionTemplate {

    private final SqlSessionFactory defaultTargetSqlSessionFactory;
    private final ExecutorType defaultTargetExecutorType;
    private final PersistenceExceptionTranslator defaultTargetExceptionTranslator;

    private final Map<Object, SqlSessionFactory> targetSqlSessionFactories;
 
    private final SqlSession sqlSessionProxy;

    public MultipleSqlSessionTemplate(SqlSessionFactory defaultTargetSqlSessionFactory, 
                                      Map<Object, SqlSessionFactory> targetSqlSessionFactories) {
        super(defaultTargetSqlSessionFactory);

        this.targetSqlSessionFactories = Objects.requireNonNull(targetSqlSessionFactories);

        this.defaultTargetSqlSessionFactory = defaultTargetSqlSessionFactory;
        this.defaultTargetExecutorType = defaultTargetSqlSessionFactory.getConfiguration().getDefaultExecutorType();
        this.defaultTargetExceptionTranslator = new MyBatisExceptionTranslator(
            defaultTargetSqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), true
        );
        this.sqlSessionProxy = (SqlSession) Proxy.newProxyInstance(
            SqlSessionFactory.class.getClassLoader(), 
            new Class[] {SqlSession.class }, 
            (proxy, method, args) -> {
                SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
                SqlSession sqlSession = getSqlSession(
                    sqlSessionFactory, defaultTargetExecutorType, defaultTargetExceptionTranslator
                );
                try {
                    Object result = method.invoke(sqlSession, args);
                    if (!isSqlSessionTransactional(sqlSession, sqlSessionFactory)) {
                        // force commit even on non-dirty sessions because some databases require
                        // a commit/rollback before calling close()
                        sqlSession.commit(true);
                    }
                    return result;
                } catch (Throwable t) {
                    Throwable unwrapped = unwrapThrowable(t);
                    if (defaultTargetExceptionTranslator != null && unwrapped instanceof PersistenceException) {
                        // release the connection to avoid a deadlock if the translator is no loaded. See issue #22
                        closeSqlSession(sqlSession, sqlSessionFactory);
                        sqlSession = null;
                        Throwable translated = defaultTargetExceptionTranslator.translateExceptionIfPossible(
                            (PersistenceException) unwrapped
                        );
                        if (translated != null) {
                            unwrapped = translated;
                        }
                    }
                    throw unwrapped;
                } finally {
                    if (sqlSession != null) {
                        closeSqlSession(sqlSession, sqlSessionFactory);
                    }
                }
            }
        );
    }

    @Override
    public SqlSessionFactory getSqlSessionFactory() {
        return Optional.ofNullable(
            targetSqlSessionFactories.get(MultipleDataSourceContext.get())
        ).orElse(
            defaultTargetSqlSessionFactory
        );
    }

    @Override
    public ExecutorType getExecutorType() {
        return this.defaultTargetExecutorType;
    }

    @Override
    public PersistenceExceptionTranslator getPersistenceExceptionTranslator() {
        return this.defaultTargetExceptionTranslator;
    }

    @Override
    public <T> T selectOne(String statement) {
        return this.sqlSessionProxy.selectOne(statement);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        return this.sqlSessionProxy.selectOne(statement, parameter);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
        return this.sqlSessionProxy.selectMap(statement, mapKey);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        return this.sqlSessionProxy.selectMap(statement, parameter, mapKey);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectMap(statement, parameter, mapKey, rowBounds);
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement) {
        return this.sqlSessionProxy.selectCursor(statement);
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter) {
        return this.sqlSessionProxy.selectCursor(statement, parameter);
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectCursor(statement, parameter, rowBounds);
    }

    @Override
    public <E> List<E> selectList(String statement) {
        return this.sqlSessionProxy.selectList(statement);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        return this.sqlSessionProxy.selectList(statement, parameter);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectList(statement, parameter, rowBounds);
    }

    @Override
    public void select(String statement, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, handler);
    }

    @Override
    public void select(String statement, Object parameter, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, parameter, handler);
    }

    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, parameter, rowBounds, handler);
    }

    @Override
    public int insert(String statement) {
        return this.sqlSessionProxy.insert(statement);
    }

    @Override
    public int insert(String statement, Object parameter) {
        return this.sqlSessionProxy.insert(statement, parameter);
    }

    @Override
    public int update(String statement) {
        return this.sqlSessionProxy.update(statement);
    }

    @Override
    public int update(String statement, Object parameter) {
        return this.sqlSessionProxy.update(statement, parameter);
    }

    @Override
    public int delete(String statement) {
        return this.sqlSessionProxy.delete(statement);
    }

    @Override
    public int delete(String statement, Object parameter) {
        return this.sqlSessionProxy.delete(statement, parameter);
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return getConfiguration().getMapper(type, this);
    }

    @Override
    public void commit() {
        throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
    }

    @Override
    public void commit(boolean force) {
        throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
    }

    @Override
    public void rollback() {
        throw new UnsupportedOperationException("Manual rollback is not allowed over a Spring managed SqlSession");
    }

    @Override
    public void rollback(boolean force) {
        throw new UnsupportedOperationException("Manual rollback is not allowed over a Spring managed SqlSession");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Manual close is not allowed over a Spring managed SqlSession");
    }

    @Override
    public void clearCache() {
        this.sqlSessionProxy.clearCache();
    }

    @Override
    public Configuration getConfiguration() {
        return this.defaultTargetSqlSessionFactory.getConfiguration();
    }

    @Override
    public Connection getConnection() {
        return this.sqlSessionProxy.getConnection();
    }

    @Override
    public List<BatchResult> flushStatements() {
        return this.sqlSessionProxy.flushStatements();
    }

}
