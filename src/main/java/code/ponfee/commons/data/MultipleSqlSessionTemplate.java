package code.ponfee.commons.data;

import static org.apache.ibatis.reflection.ExceptionUtil.unwrapThrowable;
import static org.mybatis.spring.SqlSessionUtils.closeSqlSession;
import static org.mybatis.spring.SqlSessionUtils.getSqlSession;
import static org.mybatis.spring.SqlSessionUtils.isSqlSessionTransactional;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.dao.support.PersistenceExceptionTranslator;

import code.ponfee.commons.data.lookup.MultipleDataSourceContext;

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

        Objects.requireNonNull(targetSqlSessionFactories);
        this.targetSqlSessionFactories = targetSqlSessionFactories;

        this.defaultTargetSqlSessionFactory = defaultTargetSqlSessionFactory;
        this.defaultTargetExecutorType = defaultTargetSqlSessionFactory.getConfiguration().getDefaultExecutorType();
        this.defaultTargetExceptionTranslator = new MyBatisExceptionTranslator(
            defaultTargetSqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), true
        );
        this.sqlSessionProxy = (SqlSession) Proxy.newProxyInstance(
            SqlSessionFactory.class.getClassLoader(), new Class[] {SqlSession.class }, 
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

    public ExecutorType getExecutorType() {
        return this.defaultTargetExecutorType;
    }

    public PersistenceExceptionTranslator getPersistenceExceptionTranslator() {
        return this.defaultTargetExceptionTranslator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T selectOne(String statement) {
        return this.sqlSessionProxy.selectOne(statement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T selectOne(String statement, Object parameter) {
        return this.sqlSessionProxy.selectOne(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
        return this.sqlSessionProxy.selectMap(statement, mapKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        return this.sqlSessionProxy.selectMap(statement, parameter, mapKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectMap(statement, parameter, mapKey, rowBounds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Cursor<T> selectCursor(String statement) {
        return this.sqlSessionProxy.selectCursor(statement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter) {
        return this.sqlSessionProxy.selectCursor(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectCursor(statement, parameter, rowBounds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> List<E> selectList(String statement) {
        return this.sqlSessionProxy.selectList(statement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        return this.sqlSessionProxy.selectList(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectList(statement, parameter, rowBounds);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void select(String statement, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, handler);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void select(String statement, Object parameter, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, parameter, handler);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, parameter, rowBounds, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int insert(String statement) {
        return this.sqlSessionProxy.insert(statement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int insert(String statement, Object parameter) {
        return this.sqlSessionProxy.insert(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(String statement) {
        return this.sqlSessionProxy.update(statement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(String statement, Object parameter) {
        return this.sqlSessionProxy.update(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(String statement) {
        return this.sqlSessionProxy.delete(statement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(String statement, Object parameter) {
        return this.sqlSessionProxy.delete(statement, parameter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getMapper(Class<T> type) {
        return getConfiguration().getMapper(type, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() {
        throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit(boolean force) {
        throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback() {
        throw new UnsupportedOperationException("Manual rollback is not allowed over a Spring managed SqlSession");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback(boolean force) {
        throw new UnsupportedOperationException("Manual rollback is not allowed over a Spring managed SqlSession");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        throw new UnsupportedOperationException("Manual close is not allowed over a Spring managed SqlSession");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearCache() {
        this.sqlSessionProxy.clearCache();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration() {
        return this.defaultTargetSqlSessionFactory.getConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() {
        return this.sqlSessionProxy.getConnection();
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0.2
     */
    @Override
    public List<BatchResult> flushStatements() {
        return this.sqlSessionProxy.flushStatements();
    }

}
