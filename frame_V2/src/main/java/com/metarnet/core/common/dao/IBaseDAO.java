package com.metarnet.core.common.dao;

import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.model.Pager;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.hibernate.Filter;
import org.hibernate.LockMode;
import org.hibernate.ReplicationMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface IBaseDAO {


    /**
     * 功能: 通过调用hinernate4的executeUpdate实现批量删除
     *
     * @param queryString 需要更新的语句
     * @return 更新的记录数
     * @throws DAOException 抛出数据访问异常
     */
    public int bulkUpdate(String queryString) throws DAOException;

    public int bulkUpdate(String queryString, Object[] values) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的clear方法清空模板 实现流程： 1. 调用HibernateTemplate的clear方法
     *
     * @throws DAOException 抛出数据访问异常
     */
    public void clear() throws DAOException;


    /**
     * 功能: 通过调用HibernateTemplate的contains方法实现 实现流程： 1. 传入entity数据对象 2.
     * 调用HibernateTemplate的contains方法 3. 看是否包含该对象
     *
     * @param entity 实体对象
     * @return boolean 是否包含对象标识
     * @throws DAOException 抛出数据访问异常
     */
    public boolean contains(Object entity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的delete方法实现 实现流程： 1. 传入entity数据对象 2. 调用HibernateTemplate的delete方法 3.
     * 对传入的对象进行删除
     *
     * @param entity 实体对象
     * @throws DAOException 抛出数据访问异常
     */
    public void delete(Object entity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的delete方法实现 实现流程： 1. 传入entity数据对象 2. 调用HibernateTemplate的delete方法 3.
     * 对传入的对象进行删除
     *
     * @param entity 实体对象
     * @param userId 当前用户ID
     * @throws DAOException 抛出数据访问异常
     */
    public void delete(Object entity, Integer userId) throws DAOException;


    /**
     * 功能: 通过调用HibernateTemplate的deleteAll方法实现 实现流程： 1. 传入entities数据对象集合 2.
     * 调用HibernateTemplate的delete方法 3. 对传入的对象集合进行删除
     *
     * @param entities 实体集合
     * @throws DAOException 抛出数据访问异常
     */
    public void deleteAll(Collection entities) throws DAOException;


    /**
     * 功能: 通过调用HibernateTemplate的deleteAll方法实现 实现流程： 1. 传入entities数据对象集合 2.
     * 调用HibernateTemplate的delete方法 3. 对传入的对象集合进行删除
     *
     * @param entities 实体集合
     * @param userId   当前用户ID
     * @throws DAOException 抛出数据访问异常
     */
    public void deleteAll(Collection entities, Integer userId) throws DAOException;
    public void deleteAll(Collection entities, UserEntity userEntity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的enableFilter方法实现 实现流程： 1. 传入filterName字符串 2.
     * 调用HibernateTemplate的enableFilter方法 3. 对enableFilter进行过滤
     *
     * @param filterName 字符串名称
     * @return 过滤器对象
     * @throws IllegalStateException 抛出数据访问异常
     */
    public Filter enableFilter(String filterName) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的evict方法实现 实现流程： 1. 传入实体对象 2. 调用HibernateTemplate的evict方法
     *
     * @param entity 实体对象
     * @throws DAOException 抛出数据访问异常
     */
    public void evict(Object entity) throws DAOException;


    /**
     * 功能: 通过调用HibernateTemplate的find方法实现 实现流程： 1. 传入一个需要执行的查询语句 2. 调用HibernateTemplate的find方法 3.
     * 返回的List
     *
     * @param queryString 查询语句
     * @return 返回的List集合
     * @throws DAOException 抛出数据访问异常
     */
    public List find(String queryString) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的find方法实现 实现流程： 1. 传入一个需要执行的查询语句和对象的值 2. 调用HibernateTemplate的find方法
     * 3. 返回的List
     *
     * @param queryString 查询语句
     * @param value       对象的值
     * @return 返回的List集合
     * @throws DAOException 抛出数据访问异常
     */
    public List find(String queryString, Object value) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的find方法实现 实现流程： 1. 传入一个需要执行的查询语句和对象集合 2. 调用HibernateTemplate的find方法
     * 3. 返回的List
     *
     * @param queryString 查询语句
     * @param values      对象的数组
     * @return 返回的List集合
     * @throws DAOException 抛出数据访问异常
     */
    public List find(String queryString, Object[] values) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的findByCriteria方法实现 实现流程： 1. 传入一个DetachedCriteria对象 2.
     * 调用HibernateTemplate的findByCriteria方法 3. 返回的List
     *
     * @param criteria DetachedCriteria对象
     * @return 返回的List集合
     * @throws DAOException 抛出数据访问异常
     */
    public List findByCriteria(DetachedCriteria criteria) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的findByCriteria方法实现 实现流程： 1. 传入一个DetachedCriteria对象，第一条记录数，最大的记录数 2.
     * 调用HibernateTemplate的findByCriteria方法 3. 返回的List
     *
     * @param criteria    DetachedCriteria对象
     * @param firstResult 第一条记录数
     * @param maxResults  最大的数
     * @return 返回的List集合
     * @throws DAOException 抛出数据访问异常
     */
    public List findByCriteria(DetachedCriteria criteria, int firstResult, int maxResults)
            throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的findByExample方法实现 实现流程： 1. 传入一个exampleEntity对象 2.
     * 调用HibernateTemplate的findByExample方法 3. 返回的List
     *
     * @param exampleEntity 对象
     * @return List 返回的List集合
     * @throws DAOException 抛出数据访问异常
     */
    public List findByExample(Object exampleEntity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的findByExample方法实现 实现流程： 1. 传入一个DetachedCriteria对象，第一条记录数，最大的记录数 2.
     * 调用HibernateTemplate的findByExample方法 3. 返回的List
     *
     * @param exampleEntity 对象
     * @param firstResult   第一条记录数
     * @param maxResults    最大的记录数
     * @return List 返回的List集合
     * @throws DAOException 抛出数据访问异常
     */
    public List findByExample(Object exampleEntity, int firstResult, int maxResults) throws DAOException;

    /**
     * 根据实体中的id进行查询
     *
     * @param entity 业务实体
     * @return 查询出的结果，没有时返回null
     * @throws DAOException
     */
    public Serializable findById(Serializable entity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的flush方法实现 实现流程： 1. 调用HibernateTemplate的flush方法
     *
     * @throws DAOException 抛出数据访问异常
     */
    public void flush() throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的get方法实现 实现流程： 1. 传入entityClass类与ID 2. 调用HibernateTemplate的get方法 3.
     * 返回对象
     *
     * @param entityClass 实体类名
     * @param id          ID值
     * @return Object对象
     * @throws DAOException 抛出数据访问异常
     */
    public Object get(Class entityClass, Serializable id) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的get方法实现 实现流程： 1. 参入参数调用HibernateTemplate的get方法实现
     *
     * @param entityName 实体对象
     * @param id         对象关键标识
     * @return Object对象
     * @throws DAOException 抛出数据访问异常
     */
    public Object get(String entityName, Serializable id) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的get方法实现 实现流程： 1. 参入参数调用HibernateTemplate的get方法实现
     *
     * @param entityClass 实体对象
     * @param id          对象关键标识
     * @param lockMode    LockMode对象
     * @return Object对象
     * @throws DAOException 抛出数据访问异常
     */
    public Object get(Class entityClass, Serializable id, LockMode lockMode)
            throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的get方法实现 实现流程： 1. 参入参数调用HibernateTemplate的get方法实现
     *
     * @param entityName 实体对象
     * @param id         对象关键标识
     * @param lockMode   LockMode对象
     * @return Object对象
     * @throws DAOException 抛出数据访问异常
     */
    public Object get(String entityName, Serializable id, LockMode lockMode)
            throws DAOException;


    /**
     * 功能: 通过调用HibernateTemplate的iterate方法实现 实现流程： 1. 参入参数调用HibernateTemplate的iterate方法实现
     *
     * @param queryString 字符串
     * @return Iterator对象
     * @throws DAOException 抛出数据访问异常
     */
    public Iterator iterate(String queryString) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的iterate方法实现 实现流程： 1. 参入参数调用HibernateTemplate的iterate方法实现
     *
     * @param queryString 字符串
     * @param value       对象
     * @return Iterator对象
     * @throws DAOException 抛出数据访问异常
     */
    public Iterator iterate(String queryString, Object value) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的iterate方法实现 实现流程： 1. 参入参数调用HibernateTemplate的iterate方法实现
     *
     * @param queryString 字符串
     * @param values      对象集合
     * @return Iterator对象
     * @throws DAOException 抛出数据访问异常
     */
    public Iterator iterate(String queryString, Object[] values) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的load方法实现 实现流程： 1. 参入参数调用HibernateTemplate的load方法实现
     *
     * @param entityClass Class对象
     * @param id          ID识别
     * @return Object对象
     * @throws DAOException
     *          抛出数据访问异常
     */
//    public Object load(Class entityClass, Serializable id) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的load方法实现 实现流程： 1. 参入参数调用HibernateTemplate的load方法实现
     *
     * @param entityName 实体对象名称
     * @param id         ID识别
     * @return Object对象
     * @throws DAOException
     *          抛出数据访问异常
     */
//    public Object load(String entityName, Serializable id) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的load方法实现 实现流程： 1. 参入参数调用HibernateTemplate的load方法实现
     *
     * @param entity 实体对象
     * @param id     ID识别
     * @throws DAOException
     *          抛出数据访问异常
     */
//    public void load(Object entity, Serializable id) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的load方法实现 实现流程： 1. 参入参数调用HibernateTemplate的load方法实现
     *
     * @param entityClass 实体对象名称类
     * @param id          ID识别
     * @param lockMode    LockMode对象
     * @return Object对象
     * @throws DAOException
     *          抛出数据访问异常
     */
//    public Object load(Class entityClass, Serializable id, LockMode lockMode) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的load方法实现 实现流程： 1. 参入参数调用HibernateTemplate的load方法实现
     *
     * @param entityName 实体对象名称
     * @param id         ID识别
     * @param lockMode   LockMode对象
     * @return Object对象
     * @throws DAOException
     *          抛出数据访问异常
     */
//    public Object load(String entityName, Serializable id, LockMode lockMode) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的loadAll方法实现 实现流程： 1. 参入参数调用HibernateTemplate的loadAll方法实现
     *
     * @param entityClass 实体对象名称
     * @return List对象
     * @throws DAOException
     *          抛出数据访问异常
     */
//    public List loadAll(Class entityClass) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的lock方法实现 实现流程： 1. 参入参数调用HibernateTemplate的lock方法实现
     *
     * @param entity   实体对象
     * @param lockMode LockMode对象
     * @throws DAOException 抛出数据访问异常
     */
    public void lock(Object entity, LockMode lockMode) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的lock方法实现 实现流程： 1. 参入参数调用HibernateTemplate的lock方法实现
     *
     * @param entityName 实体名称
     * @param entity     实体对象
     * @param lockMode   LockMode对象
     * @throws DAOException 抛出数据访问异常
     */
    public void lock(String entityName, Object entity, LockMode lockMode) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的merge方法实现 实现流程： 1. 参入参数调用HibernateTemplate的merge方法实现
     *
     * @param entity 实体对象
     * @return 对象
     * @throws DAOException 抛出数据访问异常
     */
    public Object merge(Object entity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的merge方法实现 实现流程： 1. 参入参数调用HibernateTemplate的merge方法实现
     *
     * @param entityName 实体对象名称
     * @param entity     实体对象
     * @return Object对象
     * @throws DAOException 抛出数据访问异常
     */
    public Object merge(String entityName, Object entity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的persist方法实现 实现流程： 1. 参入参数调用HibernateTemplate的persist方法实现
     *
     * @param entity 实体对象
     * @throws DAOException 抛出数据访问异常
     */
    public void persist(Object entity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的persist方法实现 实现流程： 1. 参入参数调用HibernateTemplate的persist方法实现
     *
     * @param entityName 实体名称
     * @param entity     实体对象
     * @throws DAOException 抛出数据访问异常
     */
    public void persist(String entityName, Object entity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的refresh方法实现 实现流程： 1. 参入参数调用HibernateTemplate的refresh方法实现
     *
     * @param entity 实体对象
     * @throws DAOException 抛出数据访问异常
     */
    public void refresh(Object entity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的refresh方法实现 实现流程： 1. 参入参数调用HibernateTemplate的refresh方法实现
     *
     * @param entity   实体对象
     * @param lockMode LockMode对象
     * @throws DAOException 抛出数据访问异常
     */
    public void refresh(Object entity, LockMode lockMode) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的replicate方法实现 实现流程： 1. 参入参数调用HibernateTemplate的replicate方法实现
     *
     * @param entity          实体对象
     * @param replicationMode ReplicationMode对象
     * @throws DAOException 抛出数据访问异常
     */
    public void replicate(Object entity, ReplicationMode replicationMode)
            throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的replicate方法实现 实现流程： 1. 参入参数调用HibernateTemplate的replicate方法实现
     *
     * @param entityName      实体对象名称
     * @param entity          实体对象
     * @param replicationMode ReplicationMode对象
     * @throws DAOException 抛出数据访问异常
     */
    public void replicate(String entityName, Object entity, ReplicationMode replicationMode)
            throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的save方法实现 实现流程： 1. 参入参数调用HibernateTemplate的save方法实现
     *
     * @param entity 实体对象
     * @return Serializable 对象
     * @throws DAOException 抛出数据访问异常
     */
    public Serializable save(Object entity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的save方法实现 实现流程： 1. 参入参数调用HibernateTemplate的save方法实现
     *
     * @param entity     实体对象
     * @param userEntity 当前用户
     * @return Serializable 对象
     * @throws DAOException 抛出数据访问异常
     */
    public Serializable save(Object entity, UserEntity userEntity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的save方法实现 实现流程： 1. 参入参数调用HibernateTemplate的save方法实现
     *
     * @param entityName 实体对象名称
     * @param entity     实体对象
     * @throws DAOException 抛出数据访问异常
     */
    public Serializable save(String entityName, Object entity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的save方法实现 实现流程： 1. 参入参数调用HibernateTemplate的save方法实现
     *
     * @param entityName 实体对象名称
     * @param entity     实体对象
     * @param userEntity 当前用户ID
     * @throws DAOException 抛出数据访问异常
     */
    public Serializable save(String entityName, Object entity, UserEntity userEntity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的saveOrUpdate方法实现 实现流程： 1. 参入参数调用HibernateTemplate的saveOrUpdate方法实现
     *
     * @param entity 实体对象
     * @throws DAOException 抛出数据访问异常
     */
    public void saveOrUpdate(Object entity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的saveOrUpdate方法实现 实现流程： 1. 参入参数调用HibernateTemplate的saveOrUpdate方法实现
     *
     * @param entity     实体对象
     * @param userEntity 当前用户
     * @throws DAOException 抛出数据访问异常
     */
    public void saveOrUpdate(Object entity, UserEntity userEntity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的saveOrUpdate方法实现 实现流程： 1. 参入参数调用HibernateTemplate的saveOrUpdate方法实现
     *
     * @param entityName 实体对象名称
     * @param entity     实体对象
     * @throws DAOException 抛出数据访问异常
     */
    public void saveOrUpdate(String entityName, Object entity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的saveOrUpdate方法实现 实现流程： 1. 参入参数调用HibernateTemplate的saveOrUpdate方法实现
     *
     * @param entityName 实体对象名称
     * @param entity     实体对象
     * @param userEntity 当前用户
     * @throws DAOException 抛出数据访问异常
     */
    public void saveOrUpdate(String entityName, Object entity, UserEntity userEntity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的saveOrUpdate方法实现 实现流程： 1. 参入参数调用HibernateTemplate的saveOrUpdate方法实现
     *
     * @param entities 实体结合
     * @throws DAOException 抛出数据访问异常
     */
    public void saveOrUpdateAll(Collection entities) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的saveOrUpdate方法实现 实现流程： 1. 参入参数调用HibernateTemplate的saveOrUpdate方法实现
     *
     * @param entities   实体结合
     * @param userEntity 当前用户
     * @throws DAOException 抛出数据访问异常
     */
    public void saveOrUpdateAll(Collection entities, UserEntity userEntity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的save方法实现 实现流程： 1. 参入参数调用HibernateTemplate的save方法实现
     *
     * @param entity 实体对象
     * @param userId 当前用户ID
     * @return Serializable 对象
     * @throws DAOException 抛出数据访问异常
     */
    public Serializable save(Object entity, Integer userId) throws DAOException;


    /**
     * 功能: 通过调用HibernateTemplate的save方法实现 实现流程： 1. 参入参数调用HibernateTemplate的save方法实现
     *
     * @param entityName 实体对象名称
     * @param entity     实体对象
     * @param userId     当前用户ID
     * @throws DAOException 抛出数据访问异常
     */
    public Serializable save(String entityName, Object entity, Integer userId) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的saveOrUpdate方法实现 实现流程： 1. 参入参数调用HibernateTemplate的saveOrUpdate方法实现
     *
     * @param entity 实体对象
     * @param userId 当前用户ID
     * @throws DAOException 抛出数据访问异常
     */
    public void saveOrUpdate(Object entity, Integer userId) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的saveOrUpdate方法实现 实现流程： 1. 参入参数调用HibernateTemplate的saveOrUpdate方法实现
     *
     * @param entityName 实体对象名称
     * @param entity     实体对象
     * @param userId     当前用户ID
     * @throws DAOException 抛出数据访问异常
     */
    public void saveOrUpdate(String entityName, Object entity, Integer userId) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的saveOrUpdate方法实现 实现流程： 1. 参入参数调用HibernateTemplate的saveOrUpdate方法实现
     *
     * @param entities 实体结合
     * @param userId   当前用户ID
     * @throws DAOException 抛出数据访问异常
     */
    public void saveOrUpdateAll(Collection entities, Integer userId) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的update方法实现 实现流程： 1. 参入参数调用HibernateTemplate的update方法实现
     *
     * @param entity 实体对象
     * @throws DAOException 抛出数据访问异常
     */
    public void update(Object entity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的update方法实现 实现流程： 1. 参入参数调用HibernateTemplate的update方法实现
     *
     * @param entity 实体对象
     * @param userId 当前用户ID
     * @throws DAOException 抛出数据访问异常
     */
    public void update(Object entity, Integer userId) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的update方法实现 实现流程： 1. 参入参数调用HibernateTemplate的update方法实现
     *
     * @param entity   实体对象
     * @param lockMode LockMode对象
     * @throws DAOException 抛出数据访问异常
     */
    public void update(Object entity, LockMode lockMode) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的update方法实现 实现流程： 1. 参入参数调用HibernateTemplate的update方法实现
     *
     * @param entity   实体对象
     * @param lockMode LockMode对象
     * @param userId   当前用户ID
     * @throws DAOException 抛出数据访问异常
     */
    public void update(Object entity, LockMode lockMode, Integer userId) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的update方法实现 实现流程： 1. 参入参数调用HibernateTemplate的update方法实现
     *
     * @param entityName 实体对象名称
     * @param entity     实体对象
     * @throws DAOException 抛出数据访问异常
     */
    public void update(String entityName, Object entity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的update方法实现 实现流程： 1. 参入参数调用HibernateTemplate的update方法实现
     *
     * @param entityName 实体对象名称
     * @param entity     实体对象
     * @param userId     当前用户ID
     * @throws DAOException 抛出数据访问异常
     */
    public void update(String entityName, Object entity, Integer userId) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的update方法实现 实现流程： 1. 参入参数调用HibernateTemplate的update方法实现
     *
     * @param entityName 实体对象名称
     * @param entity     实体对象
     * @param lockMode   LockMode对象
     * @throws DAOException 抛出数据访问异常
     */
    public void update(String entityName, Object entity, LockMode lockMode)
            throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的update方法实现 实现流程： 1. 参入参数调用HibernateTemplate的update方法实现
     *
     * @param entityName 实体对象名称
     * @param entity     实体对象
     * @param lockMode   LockMode对象
     * @param userId     当前用户ID
     * @throws DAOException 抛出数据访问异常
     */
    public void update(String entityName, Object entity, LockMode lockMode, Integer userId)
            throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的findByExample方法实现 实现流程： 1.
     * 参入参数调用HibernateTemplate的findByExample方法实现
     *
     * @param entityName    实体对象的名称
     * @param exampleEntity 对象
     * @return List数组
     * @throws DAOException 抛出数据访问异常
     */
    public List findByExample(String entityName, Object exampleEntity) throws DAOException;

    /**
     * 功能: 通过调用HibernateTemplate的findByExample方法实现
     * 实现流程： 1.传入参数调用HibernateTemplate的findByExample方法实现
     *
     * @param entityName    实体对象的名称
     * @param exampleEntity 对象
     * @param firstResult   第一条记录数
     * @param maxResults    最大的记录数
     * @return List数组
     * @throws DAOException 抛出数据访问异常
     */
    public List findByExample(String entityName, Object exampleEntity, int firstResult,
                              int maxResults) throws DAOException;


    /**
     * 功能: 根据传入的HQL语句和page对象返回查询数据页对象
     *
     * @param queryString HQL语句
     * @param page        查询页对象
     * @return page 数据结果集页对象
     * @throws DAOException 抛出数据访问异常
     */
    public Pager getPageByHql(String queryString, Pager page) throws DAOException;

    /**
     * 功能: 根据传入的HQL语句和page对象返回查询数据页对象
     * @param queryString
     * @param page
     * @param queryMap
     * @return
     * @throws DAOException
     */
    public Pager getPageByHql(String queryString, Pager page, Map<String, Object> queryMap) throws DAOException;
    /**
     * 功能: 根据传入的HQL语句和page对象返回查询数据页对象
     *
     * @param page 查询页对象
     * @return page 数据结果集页对象
     * @throws DAOException 抛出数据访问异常
     */
    public Pager getPageByEntity(String entityName, Pager page) throws DAOException;

    /**
     * 功能: 根据传入的DetachedCriteria对象和page对象返回查询数据页对象
     *
     * @param detachedCriteria 查询对象
     * @param page             查询页对象
     * @return page 数据结果集页对象
     * @throws DAOException 抛出数据访问异常
     */
//    public Pager getPageByCriteria(final DetachedCriteria detachedCriteria, final Pager page)
//            throws DAOException;

    /**
     * 功能: 根据传入的HQL语句和page对象返回查询数据页对象
     *
     * @param entity 查询的Example对象
     * @param page   查询页对象
     * @return page 数据结果集页对象
     * @throws DAOException 抛出数据访问异常
     */
//    public Pager getPageByExample(Object entity, final Pager page) throws DAOException;


    /**
     * 通用的调用原生SQL
     *
     * @param sql sql语句
     * @param obj 所对应的参数
     * @return List
     */
    public List<Map> findNativeSQL(final String sql, final Object[] obj) throws DAOException;

    /**
     * 通用的调用原生SQL
     *
     * @param sql sql语句
     * @param obj 所对应的参数
     * @param addDeletedFlag 是否添加deleteFlag
     * @return List
     */
    public List<Map> findNativeSQL(final String sql, final Object[] obj, boolean addDeletedFlag) throws DAOException;

    /**
     * 通用的调用原生SQL
     *
     * @param sql  sql语句
     * @param obj  所对应的参数
     * @param page 分页信息
     * @return List
     */
    public Pager findNativeSQL(final String sql, final Object[] obj, Pager page) throws DAOException;


    /**
     * 通用的调用原生SQL
     *
     * @param sql sql语句
     * @return List
     * @throws java.sql.SQLException
     * @throws IllegalStateException
     * @throws org.hibernate.HibernateException
     * @throws org.springframework.dao.DataAccessResourceFailureException
     */
    public int executeSql(String sql) throws DAOException;

    /**
     * 获取SessionFactory
     *
     * @return SessionFactory
     */
    public SessionFactory getSessionFactory();

    /**
     * 获取PO对应sequence的nextValue
     *
     * @param entityClass PO类型
     * @return
     */
    public Long getSequenceNextValue(Class entityClass) throws DAOException;

    /**
     * 获取sequence的nextvalue
     *
     * @param sequenceName 序列名
     * @return
     * @throws DAOException
     */
    public Long getSequenceNextValue(String sequenceName) throws DAOException;

    /**
     * 获取工单编号sequence的nextvalue
     *
     * @param entityClass PO
     * @return
     * @throws DAOException
     */
    public Long getOrderNumSequenceNextValue(Class entityClass) throws DAOException;

    /**
     * 创建sequence
     *
     * @param sequence
     * @throws DAOException
     */
    public void createSequence(String sequence) throws DAOException;

    public Object findEntityByProcessInstId(String entityName , String processInstID) throws DAOException;

    public List findEntityListByProcessInstId(String entityName , String processInstID) throws DAOException;

    public List findEntityListByParentProInstId(String entityName , String parentProInstId) throws DAOException;

    public Object findEntityByRootProInstId(String entityName , String parentProInstId) throws DAOException;

    public Pager findByParametersPage(Pager pager , String hql , Map<String , Object> parameters) throws DAOException;

}