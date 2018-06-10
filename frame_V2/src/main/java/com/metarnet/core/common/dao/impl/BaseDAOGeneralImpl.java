package com.metarnet.core.common.dao.impl;

import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.model.OrderNum;
import com.metarnet.core.common.model.Pager;
import com.metarnet.core.common.utils.*;
import com.sun.xml.internal.ws.util.UtilException;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.*;
import org.hibernate.Query;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Example;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: hadoop
 * Date: 15-9-16
 * Time: 下午2:44
 * To change this template use File | Settings | File Templates.
 */

/**
 * Hibernate 通用处理类
 */
@Service(value = "baseDAO")
public class BaseDAOGeneralImpl implements IBaseDAO {

    Logger logger = LogManager.getLogger("BaseDAOGeneralImpl");

    /**
     * 存储已加载过的sequence
     */
    private static final Map<Class, String> sequenceMap = new ConcurrentHashMap<Class, String>();

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public int bulkUpdate(String queryString) throws DAOException {
        return sessionFactory.getCurrentSession().createQuery(queryString).executeUpdate();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int bulkUpdate(String queryString, Object[] values) throws DAOException {
        Query query = sessionFactory.getCurrentSession().createQuery(queryString);
        for (int i = 0; i < values.length; i++) {
            query.setParameter(i, values[i]);
        }
        return query.executeUpdate();
    }

    @Override
    public void clear() throws DAOException {
        sessionFactory.getCurrentSession().clear();
    }

    @Override
    public boolean contains(Object entity) throws DAOException {
        return sessionFactory.getCurrentSession().contains(entity);
    }

    @Override
    public void delete(Object entity) throws DAOException {
        sessionFactory.getCurrentSession().delete(entity);
    }

    @Override
    public void delete(Object entity, Integer userId) throws DAOException {
        try {
            beforeDeleteHandle(entity, userId);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new DAOException(e);
        }
        delete(entity);
    }

    @Override
    public void deleteAll(Collection entities) throws DAOException {
//        sessionFactory.getCurrentSession().beginTransaction();
        int i=1;
        for (Object entity : entities) {
            sessionFactory.getCurrentSession().delete(entity);
            logger.info("删除数据开始"+(i++));
        }
//        sessionFactory.getCurrentSession().getTransaction().commit();
    }

    @Override
    public void deleteAll(Collection entities, Integer userId) throws DAOException {
        deleteAll(entities);
    }
    @Override
    public void deleteAll(Collection entities, UserEntity userEntity) throws DAOException {
        for (Object entity : entities) {
            HibernateBeanUtils.setId(entity, userEntity);
            HibernateBeanUtils.setCreatedInfo(entity, userEntity);
            HibernateBeanUtils.setLastUpdateInfo(entity, userEntity.getUserId());
            HibernateBeanUtils.setDeletedFlag(entity, true);
            sessionFactory.getCurrentSession().saveOrUpdate(entity);
        }
    }
    @Override
    public Filter enableFilter(String filterName) throws DAOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void evict(Object entity) throws DAOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List find(String queryString) throws DAOException {
        return sessionFactory.getCurrentSession().createQuery(queryString).list();
    }

    @Override
    public List find(String queryString, Object value) throws DAOException {
        return sessionFactory.getCurrentSession().createQuery(queryString).setParameter(1, value).list();
    }

    @Override
    public List find(String queryString, Object[] values) throws DAOException {
        Query query = sessionFactory.getCurrentSession().createQuery(queryString);
        for (int i = 0; i < values.length; i++) {
            query.setParameter(i, values[i]);
        }
        return query.list();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List findByCriteria(DetachedCriteria criteria) throws DAOException {
//        return sessionFactory.getCurrentSession().createQuery("from com.metarnet.eomtm.model.TEomTaskDisForm where objectId=174").list();
        List list = null;
        try{
            list = criteria.getExecutableCriteria(sessionFactory.getCurrentSession()).list();
        } catch (Exception e){
            list = criteria.getExecutableCriteria(sessionFactory.getCurrentSession()).list();
        }
        return list;
    }

    @Override
    public List findByCriteria(DetachedCriteria criteria, int firstResult, int maxResults) throws DAOException {
        return criteria.getExecutableCriteria(sessionFactory.getCurrentSession()).setFirstResult(firstResult).setMaxResults(maxResults).list();
    }

    @Override
    public List findByExample(Object exampleEntity) throws DAOException {
        return sessionFactory.getCurrentSession().createCriteria(exampleEntity.getClass()).add(Example.create(exampleEntity)).list();
    }

    @Override
    public List findByExample(Object exampleEntity, int firstResult, int maxResults) throws DAOException {
        return sessionFactory.getCurrentSession().createCriteria(exampleEntity.getClass()).add(Example.create(exampleEntity)).setFirstResult(firstResult).setMaxResults(maxResults).list();
    }

    @Override
    public Serializable findById(Serializable entity) throws DAOException {
//        DetachedCriteria criteria = DetachedCriteria.forClass(entity.getClass());
//        try {
//            for (IdBinder idBinder : AnnotationReader.readEntity(entity.getClass()).getIdBinderList()) {
//                criteria.add(Restrictions.eq(idBinder.getFieldName(), idBinder.getPd().getReadMethod().invoke(entity, null)));
//            }
//        } catch (Exception e) {
//            throw new DAOException(e);
//        }

//        List list = findByCriteria(criteria);
//        if (list == null || list.size() == 0) {
//            return null;
//        }
//        return (Serializable) list.get(0);
        Object objectId = 0L;
        for (IdBinder idBinder : AnnotationReader.readEntity(entity.getClass()).getIdBinderList()) {
            try {
                objectId = idBinder.getPd().getReadMethod().invoke(entity, null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvocationTargetException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return (Serializable) sessionFactory.getCurrentSession().get(entity.getClass() , (Serializable) objectId);
    }


    @Override
    public void flush() throws DAOException {
        sessionFactory.getCurrentSession().flush();
    }

    @Override
    public Object get(Class entityClass, Serializable id) throws DAOException {
        return sessionFactory.getCurrentSession().get(entityClass.getName(), id);
    }

    @Override
    public Object get(String entityName, Serializable id) throws DAOException {
        return sessionFactory.getCurrentSession().get(entityName, id);
    }

    @Override
    public Object get(Class entityClass, Serializable id, LockMode lockMode) throws DAOException {
        return sessionFactory.getCurrentSession().get(entityClass, id, lockMode);
    }

    @Override
    public Object get(String entityName, Serializable id, LockMode lockMode) throws DAOException {
        return sessionFactory.getCurrentSession().get(entityName, id, lockMode);
    }

    @Override
    public Iterator iterate(String queryString) throws DAOException {
        return sessionFactory.getCurrentSession().createQuery(queryString).iterate();
    }

    @Override
    public Iterator iterate(String queryString, Object value) throws DAOException {
        Query query = sessionFactory.getCurrentSession().createQuery(queryString);
        query.setParameter(1, value);
        return query.iterate();
    }

    @Override
    public Iterator iterate(String queryString, Object[] values) throws DAOException {
        Query query = sessionFactory.getCurrentSession().createQuery(queryString);
        for (int i = 0; i < values.length; i++) {
            query.setParameter(i, values[i]);
        }
        return query.iterate();
    }

    @Override
    public void lock(Object entity, LockMode lockMode) throws DAOException {
        sessionFactory.getCurrentSession().lock(entity, lockMode);
    }

    @Override
    public void lock(String entityName, Object entity, LockMode lockMode) throws DAOException {
        sessionFactory.getCurrentSession().lock(entityName, entity, lockMode);
    }

    @Override
    public Object merge(Object entity) throws DAOException {
        return sessionFactory.getCurrentSession().merge(entity);
    }

    @Override
    public Object merge(String entityName, Object entity) throws DAOException {
        return sessionFactory.getCurrentSession().merge(entityName, entity);
    }

    @Override
    public void persist(Object entity) throws DAOException {
        sessionFactory.getCurrentSession().persist(entity);
    }

    @Override
    public void persist(String entityName, Object entity) throws DAOException {
        sessionFactory.getCurrentSession().persist(entityName, entity);
    }

    @Override
    public void refresh(Object entity) throws DAOException {
        sessionFactory.getCurrentSession().refresh(entity);
    }

    @Override
    public void refresh(Object entity, LockMode lockMode) throws DAOException {
        sessionFactory.getCurrentSession().refresh(entity, lockMode);
    }

    @Override
    public void replicate(Object entity, ReplicationMode replicationMode) throws DAOException {
        sessionFactory.getCurrentSession().replicate(entity, replicationMode);
    }

    @Override
    public void replicate(String entityName, Object entity, ReplicationMode replicationMode) throws DAOException {
        sessionFactory.getCurrentSession().replicate(entityName, entity, replicationMode);
    }

    @Override
    public Serializable save(Object entity) throws DAOException {
        return sessionFactory.getCurrentSession().save(entity);
    }

    @Override
    public Serializable save(Object entity, UserEntity userEntity) throws DAOException {
        HibernateBeanUtils.beforeSaveHandle(entity, userEntity);
        return save(entity);
    }

    @Override
    public Serializable save(String entityName, Object entity) throws DAOException {
        return sessionFactory.getCurrentSession().save(entityName, entity);
    }

    @Override
    public Serializable save(String entityName, Object entity, UserEntity userEntity) throws DAOException {
        HibernateBeanUtils.beforeSaveHandle(entity, userEntity);
        return save(entityName, entity);
    }

    @Override
    public void saveOrUpdate(Object entity) throws DAOException {
        sessionFactory.getCurrentSession().saveOrUpdate(entity);
    }

    @Override
    public void saveOrUpdate(Object entity, UserEntity userEntity) throws DAOException {
        HibernateBeanUtils.beforeSaveHandle(entity, userEntity);
        saveOrUpdate(entity);
    }

    @Override
    public void saveOrUpdate(String entityName, Object entity) throws DAOException {
        sessionFactory.getCurrentSession().saveOrUpdate(entityName, entity);
    }

    @Override
    public void saveOrUpdate(String entityName, Object entity, UserEntity userEntity) throws DAOException {
        HibernateBeanUtils.beforeSaveHandle(entity, userEntity);
        sessionFactory.getCurrentSession().saveOrUpdate(entityName, entity);
    }

    @Override
    public void saveOrUpdateAll(Collection entities) throws DAOException {
//        sessionFactory.getCurrentSession().beginTransaction();
        for (Object entity : entities) {
            sessionFactory.getCurrentSession().saveOrUpdate(entity);
        }
//        sessionFactory.getCurrentSession().getTransaction().commit();
    }

    @Override
    public void saveOrUpdateAll(Collection entities, UserEntity userEntity) throws DAOException {
        for (Object entity : entities) {
            HibernateBeanUtils.beforeSaveHandle(entity, userEntity);
            sessionFactory.getCurrentSession().saveOrUpdate(entity);
        }
//        HibernateBeanUtils.beforeSaveHandle(entities, userEntity);
//        saveOrUpdateAll(entities);
    }

    @Override
    public Serializable save(Object entity, Integer userId) throws DAOException {
        try {
            return save(entity, AAAAAdapter.getInstence().findUserbyUserID(userId));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new DAOException(e);
        }
    }

    @Override
    public Serializable save(String entityName, Object entity, Integer userId) throws DAOException {
        try {
            return save(entityName, entity, AAAAAdapter.getInstence().findUserbyUserID(userId));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new DAOException(e);
        }
    }

    @Override
    public void saveOrUpdate(Object entity, Integer userId) throws DAOException {
        try {
            saveOrUpdate(entity, AAAAAdapter.getInstence().findUserbyUserID(userId));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new DAOException(e);
        }
    }

    @Override
    public void saveOrUpdate(String entityName, Object entity, Integer userId) throws DAOException {
        try {
            saveOrUpdate(entityName, entity, AAAAAdapter.getInstence().findUserbyUserID(userId));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new DAOException(e);
        }
    }

    @Override
    public void saveOrUpdateAll(Collection entities, Integer userId) throws DAOException {
        try {
            beforeDeleteHandle(entities, userId);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new DAOException(e);
        }
        saveOrUpdateAll(entities);
    }

    @Override
    public void update(Object entity) throws DAOException {
        sessionFactory.getCurrentSession().update(entity);
    }

    @Override
    public void update(Object entity, Integer userId) throws DAOException {
//        try {
//            beforeDeleteHandle(entity, userId);
//        } catch (Exception e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            throw new DAOException(e);
//        }
        update(entity);
    }

    @Override
    public void update(Object entity, LockMode lockMode) throws DAOException {
        update(entity);
    }

    @Override
    public void update(Object entity, LockMode lockMode, Integer userId) throws DAOException {
        try {
            beforeDeleteHandle(entity, userId);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new DAOException(e);
        }
        update(entity, lockMode);
    }

    @Override
    public void update(String entityName, Object entity) throws DAOException {
        sessionFactory.getCurrentSession().update(entityName, entity);
    }

    @Override
    public void update(String entityName, Object entity, Integer userId) throws DAOException {
        try {
            beforeDeleteHandle(entity, userId);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new DAOException(e);
        }
        update(entityName, entity);
    }

    @Override
    public void update(String entityName, Object entity, LockMode lockMode) throws DAOException {
        update(entityName, entity);
    }

    @Override
    public void update(String entityName, Object entity, LockMode lockMode, Integer userId) throws DAOException {
        update(entityName, entity, userId);
    }

    @Override
    public List findByExample(String entityName, Object exampleEntity) throws DAOException {
        return sessionFactory.getCurrentSession().createCriteria(entityName).add(Example.create(exampleEntity)).list();
    }

    @Override
    public List findByExample(String entityName, Object exampleEntity, int firstResult, int maxResults) throws DAOException {
        return sessionFactory.getCurrentSession().createCriteria(entityName).add(Example.create(exampleEntity)).setFirstResult(firstResult).setMaxResults(maxResults).list();
    }

    @Override
    public Pager getPageByHql(String queryString, Pager page) throws DAOException {
        String countQueryString;
        int totalCount = 0;

        if (queryString.contains("distinct")) {
            countQueryString = queryString.replaceFirst("distinct",
                    "distinct count(*) ");
        } else if (queryString.trim().startsWith("select")) {
            countQueryString = queryString.replaceFirst("select",
                    "select count(*) ");
        } else {
            countQueryString = " select count(*) " + queryString;
        }
        countQueryString = countQueryString.replace("from", "FROM");
        countQueryString = StringUtils.append(countQueryString.substring(0,
                countQueryString.indexOf("count(*)") + 9), countQueryString
                .substring(countQueryString.indexOf(" FROM "), countQueryString
                        .length()));
        List countlist = find(countQueryString);
        if (countlist.size() > 0) {
            if (countlist.get(0) instanceof Long) {
                totalCount = (Integer) countlist.get(0);
            } else if (countlist.get(0) instanceof Object) {
                Object[] objects = (Object[]) countlist.get(0);
                totalCount = ((Integer) objects[0]);
            }
        }

        // 返回分页对象
        if (totalCount < 1) {
            return new Pager();
        }
        if (page == null) {
            page = new Pager();
        } else {
            if (null != page.getSort()) {
                queryString = queryString + " order by " + page.getSort() + " "
                        + page.getDirection();
            }
        }

        Query query = sessionFactory.getCurrentSession().createQuery(queryString);
        int startIndex = 0;
        if (page.getPageSize() > 0) {
            startIndex = Pager.getStartOfPage(page.getNowPage(), page
                    .getPageSize());
        }
        query = query.setFirstResult(startIndex);
        if (page.getPageSize() > 0) {
            query = query.setMaxResults(page.getPageSize());
        }
        List list = query.list();
        if (page.getPageSize() == 0) {
            page.setPageSize(list.size());
        }
        page = new Pager(startIndex, totalCount, page.getPageSize(), list);
        page.setNowPage(page.getNowPage());
        return page;
    }

    @Override
    public Pager getPageByHql(String queryString, Pager page, Map<String, Object> queryMap) throws DAOException {
        String countQueryString;
        int totalCount = 0;

        if (queryString.contains("distinct")) {
            countQueryString = queryString.replaceFirst("distinct",
                    "distinct count(*) ");
        } else if (queryString.trim().startsWith("select")) {
            countQueryString = queryString.replaceFirst("select",
                    "select count(*) ");
        } else {
            countQueryString = " select count(*) " + queryString;
        }
        countQueryString = countQueryString.replace("from", "FROM");
        countQueryString = StringUtils.append(countQueryString.substring(0,
                countQueryString.indexOf("count(*)") + 9), countQueryString
                .substring(countQueryString.indexOf(" FROM "), countQueryString
                        .length()));
        Query query1 = sessionFactory.getCurrentSession().createQuery(countQueryString);
        if (queryMap != null) {
            Set<Map.Entry<String, Object>> mapKey = queryMap.entrySet();
            for (Map.Entry<String, Object> entry : mapKey) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof String) {
                    query1.setString(key, (String) value);
                } else if (value instanceof Long) {
                    query1.setLong(key, (Long) value);
                } else if (value instanceof Integer) {
                    query1.setInteger(key, (Integer) value);
                } else if (value instanceof Boolean) {
                    query1.setBoolean(key, (Boolean) value);
                } else if (value instanceof Timestamp) {
                    query1.setTimestamp(key, (Timestamp) value);
                } else if (value instanceof Date) {
                    query1.setDate(key, (Date) value);
                }
            }
        }
        List countlist = query1.list();
        if (countlist.size() > 0) {
            if (countlist.get(0) instanceof Long) {
                totalCount = ((Long) countlist.get(0)).intValue();
            } else if (countlist.get(0) instanceof Object) {
                Object[] objects = (Object[]) countlist.get(0);
                totalCount = (Integer) objects[0];
            }
        }

        // 返回分页对象
        if (totalCount < 1) {
            return new Pager();
        }
        if (page == null) {
            page = new Pager();
        } else {
            if (null != page.getSort()&&!page.getSort().contains(",0")) {
                queryString = queryString + " order by " + page.getSort() + " "
                        + page.getDirection();
            }
        }

        Query query = sessionFactory.getCurrentSession().createQuery(queryString);
        if (queryMap != null) {
            Set<Map.Entry<String, Object>> mapKey = queryMap.entrySet();
            for (Map.Entry<String, Object> entry : mapKey) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof String) {
                    query.setString(key, (String) value);
                } else if (value instanceof Long) {
                    query.setLong(key, (Long) value);
                } else if (value instanceof Integer) {
                    query.setInteger(key, (Integer) value);
                } else if (value instanceof Boolean) {
                    query.setBoolean(key, (Boolean) value);
                } else if (value instanceof Timestamp) {
                    query.setTimestamp(key, (Timestamp) value);
                } else if (value instanceof Date) {
                    query.setDate(key, (Date) value);
                }
            }
        }
        int startIndex = 0;
        if (page.getPageSize() > 0) {
            startIndex = Pager.getStartOfPage(page.getNowPage(), page
                    .getPageSize());
        }
        query = query.setFirstResult(startIndex);
        if (page.getPageSize() > 0) {
            query = query.setMaxResults(page.getPageSize());
        }
        List list = query.list();
        if (page.getPageSize() == 0) {
            page.setPageSize(list.size());
        }
        page = new Pager(startIndex, totalCount, page.getPageSize(), list,page.getNowPage());
       // page.setNowPage(page.getPageCount());
        return page;
    }

    @Override
    public Pager getPageByEntity(String entityName, Pager page) throws DAOException {
        return getPageByHql("FROM " + entityName, page);
    }

    /*@Override
    public Pager getPageByCriteria(DetachedCriteria detachedCriteria, Pager page) throws DAOException {
        return null;
    }*/

    /*@Override
    public Pager getPageByExample(Object entity, Pager page) throws DAOException {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(entity
                .getClass());
        Object u = PubFun.convertEmptyValueToNull(entity);
        detachedCriteria.add(Example.create(u));
        return getPageByCriteria(detachedCriteria, page);
    }*/

    @Override
    public List<Map> findNativeSQL(String sql, Object[] obj) throws DAOException {
        List resultList = new ArrayList();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = SessionFactoryUtils.getDataSource(sessionFactory).getConnection();
            ps = connection.prepareStatement(sql);
            if (obj != null) {
                for (int i = 0; i < obj.length; i++) {
                    ps.setObject(i + 1, obj[i]);
                }
            }
            rs = ps.executeQuery();
            ResultSetMetaData rsm = rs.getMetaData();
            while (rs.next()) {
                Map map = new HashMap();
                for (int col = 0; col < rsm
                        .getColumnCount(); col++) {
                    map.put(rsm.getColumnLabel(col + 1)
                            .toLowerCase(), rs
                            .getObject(col + 1));
                }
                resultList.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if(rs != null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            if(ps != null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            if(connection != null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        return resultList;


//        Query query = sessionFactory.getCurrentSession().createSQLQuery(sql);
//        if (obj != null) {
//            for (int i = 0; i < obj.length; i++) {
//                query.setParameter(i, obj[i]);
//            }
//        }
//
//        return query.list();
    }

    @Override
    public List<Map> findNativeSQL(String sql, Object[] obj, boolean addDeletedFlag) throws DAOException {
        if (addDeletedFlag) {
            return findNativeSQL(addDeletedFlag(sql, "DELETED_FLAG"), obj);
        }
        return findNativeSQL(sql, obj);
    }

    @Override
    public Pager findNativeSQL(String sql, Object[] obj, Pager page) throws DAOException {
        String countQueryString;
        int totalCount = 0;

        /*if (sql.contains("distinct")) {
            countQueryString = sql.replaceFirst("distinct",
                    "distinct count(*) countnum");
        } else */
        if (sql.trim().startsWith("select *")) {
            countQueryString = sql.replaceFirst("select",
                    "select count(*) countnum");
        } else if (sql.trim().startsWith("select")) {
            countQueryString = sql.replaceFirst("select",
                    "select count(*) countnum");
        } else {
            countQueryString = " select count(*) countnum from " + "(" + sql + ") as  view";
        }
        countQueryString = countQueryString.replace("from", "FROM");
        countQueryString = StringUtils.append(countQueryString.substring(0,
                countQueryString.indexOf("countnum") + 9), countQueryString
                .substring(countQueryString.indexOf(" FROM "), countQueryString
                        .length()));
        List countlist = findNativeSQL(countQueryString, obj);
        if (countlist.size() > 0) {
            if (countlist.get(0) instanceof Long) {
                totalCount = (Integer) countlist.get(0);
            } else if(countlist.get(0) instanceof BigInteger){
                totalCount = Integer.valueOf(countlist.get(0).toString());
            }else if (countlist.get(0) instanceof Object) {
                Map objects = (Map) countlist.get(0);
                try{
                    totalCount = (Integer) objects.get("countnum");
                } catch (Exception e){
                    totalCount = ((Long) objects.get("countnum")).intValue();
                }

            }
        }

        // 返回分页对象
        if (totalCount < 1) {
            return new Pager();
        }
        if (page == null) {
            page = new Pager();
        } else {
            if (null != page.getSort()&&!page.getSort().contains(",0")) {
                sql = sql + " order by " + page.getSort() + " "
                        + page.getDirection();
            }
        }

        int startIndex = 0;
        if (page.getPageSize() > 0) {
//			startIndex = Page.getStartOfPage(page.getNewPageNo(), page
//					.getPageSize());
            startIndex = page.getStartRecord();
        }
        sql += " limit " + startIndex + "," + page.getPageSize();
        List list = findNativeSQL(sql, obj);
        if (page.getPageSize() == 0) {
            page.setPageSize(list.size());
        }
//        page = new Pager(startIndex, totalCount, page.getPageSize(), list);
//        page.setNowPage(page.getPageCount());
        page.setRecordCount(totalCount);
       // page.setRecordCount(list.size());

        page.setExhibitDatas(list);
        page.setPageCount(page.getRecordCount() % page.getPageSize() == 0 ? page.getRecordCount()/page.getPageSize() : page.getRecordCount()/page.getPageSize() + 1);
        return page;
    }


    @Override
    public int executeSql(String sql) {
        return sessionFactory.getCurrentSession().createSQLQuery(sql).executeUpdate();
    }

    @Override
    public SessionFactory getSessionFactory() {
        return sessionFactory;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Long getSequenceNextValue(Class entityClass) throws DAOException {
        if (sequenceMap.containsKey(entityClass)) {
            return getSequenceNextValue(sequenceMap.get(entityClass).toUpperCase());
        }
        try {
            if (entityClass.isAnnotationPresent(DiscriminatorValue.class) && entityClass.isAnnotationPresent(SecondaryTable.class)) {
                return getSequenceNextValue(entityClass.getSuperclass());
            }
            if (entityClass.isAnnotationPresent(Table.class)) {
                String tableName = ((Table) entityClass.getAnnotation(Table.class)).name();
                String colnmName = getColnmName(entityClass);
                String sequenceName = getSequenceName(tableName, colnmName);
                sequenceMap.put(entityClass, sequenceName);
                return getSequenceNextValue(sequenceName.toUpperCase());
            }
        } catch (IntrospectionException e) {
            throw new DAOException(e);
        }
        throw new DAOException("无法获取sequence，对应的PO为" + entityClass.getName());
    }

    @Override
    public Long getSequenceNextValue(String sequenceName) throws DAOException {
        /*Date start = new Date();
        logger.info("Start to getSequenceNextValue..." + sequenceName + "===" + start);
        List<Map> map = findNativeSQL("select current_value from unisequence where name='" + sequenceName + "'", new Object[]{});
        if (map.size() > 0 && map.get(0) != null) {
            *//*Long value = Long.valueOf(String.valueOf(map.get(0).get("current_value")));
            executeSql("update unisequence set current_value=" + (value + 1) + " where name='" + sequenceName + "'");
            return value;*//*
            Sequence sequence = (Sequence) sessionFactory.getCurrentSession().get(Sequence.class, sequenceName);
            sequence.setCurrentValue(sequence.getCurrentValue() + sequence.getIncrement());
            sessionFactory.getCurrentSession().saveOrUpdate(sequence);
            logger.info("Return getSequenceNextValue..." + sequenceName + "===" + start + "/COST = " + (new Date().getTime() - start.getTime()));
            return sequence.getCurrentValue();
        } else {
            try {
                createSequence(sequenceName);
            } catch (Exception e) {
                return null;
            }
            return 1L;
        }*/
        Date start = new Date();
        logger.info("Start to getSequenceNextValue..." + sequenceName + "===" + start);
        List<Map> map = findNativeSQL(" select nextval('" + sequenceName + "')", null);
        if (map.size() > 0 && map.get(0).values().toArray()[0] != null&&Integer.valueOf(map.get(0).values().toArray()[0].toString())>0) {
            Long value = Long.parseLong(map.get(0).values().toArray()[0].toString());
            logger.info("Return getSequenceNextValue..." + sequenceName + "===" + start + "/COST = " + (new Date().getTime() - start.getTime()));
            return value;
        }else{
            createSequence(sequenceName);
            return 1L;
        }
    }

    @Override
    public Long getOrderNumSequenceNextValue(Class entityClass) throws DAOException {
        try {
            if (entityClass.isAnnotationPresent(Table.class)) {
                String tableName = ((Table) entityClass.getAnnotation(Table.class)).name();
                Field[] fields = entityClass.getDeclaredFields();
                for (Field field : fields) {
                    PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entityClass);
                    if (pd.getReadMethod().isAnnotationPresent(OrderNum.class)) {
                        String sequenceName = getSequenceName(tableName, (pd.getReadMethod().getAnnotation(Column
                                .class)).name());
                        return getSequenceNextValue(sequenceName);
                    }
                }
            }
        } catch (Exception e) {
            throw new DAOException(e);
        }
        return null;
    }

    @Override
    public void createSequence(String sequence) throws DAOException {
//        executeSql("INSERT INTO unisequence(NAME,CURRENT_VALUE,INCREMENT) VALUES('" + sequence + "', 1,1)");
//        executeSql("call insertval('" + sequence + "')");
        logger.info("创建序列:"+sequence+"开始");
        Session session =sessionFactory.getCurrentSession(); //获取hibernate会话
        String procName="{Call insertval(?) }";
        SQLQuery query = session.createSQLQuery(procName);
        query.setString(0, sequence);
        query.executeUpdate();
        logger.info("创建序列:"+sequence+"结束");
    }

    private void beforeDeleteHandle(Object entity, Integer userId) throws DAOException, IntrospectionException,
            InvocationTargetException, IllegalAccessException, NoSuchMethodException, UtilException {
        HibernateBeanUtils.setDeletedFlag(entity, userId, true);
        PropertyDescriptor targetPds[] = Introspector.getBeanInfo(entity.getClass()).getPropertyDescriptors();
        for (PropertyDescriptor pd : targetPds) {
            if (Collection.class.isAssignableFrom(pd.getPropertyType()) && null != pd.getReadMethod().invoke(entity,
                    null)) {
                for (Iterator it = ((Collection) pd.getReadMethod().invoke(entity, null)).iterator(); it.hasNext(); ) {
                    beforeDeleteHandle(it.next(), userId);
                }
            }
        }
    }

    private String addDeletedFlag(String queryString, String deletedFlag) throws DAOException {
        if (queryString == null || "".equals(queryString)) {
            throw new DAOException("查询语句为空");
        }
        String where = " WHERE ";
        queryString = queryString.replace(" from ", " FROM ");
        queryString = queryString.replace(" where ", where);
        if (!queryString.contains(deletedFlag)) {
            String deletedFlagWhere = where + deletedFlag + " = 0 ";
            if (!queryString.contains(where)) {
                queryString = StringUtils.append(queryString, deletedFlagWhere);
            } else { // 判断当前是否是多表联查，如果是需要所有表都加上deletedFlag = 0的条件
                String from = queryString.substring(queryString.indexOf(" FROM ") + 6, queryString.indexOf(where));
                String[] tables = from.split(",");
                List<String> tableAliases = new ArrayList<String>();
                for (String table : tables) {
                    tableAliases.add(table.substring(table.indexOf(" ") + 1));
                }
                String whereSql = where;
                for (String tableAlias : tableAliases) {
                    whereSql += tableAlias + "." + deletedFlag + "=0 AND ";
                }
                queryString = queryString.replace(where, whereSql);
            }
        }
        return queryString;
    }

    public String getColnmName(Class entityClass) throws DAOException, IntrospectionException {
        SetMap<String, String> setMap = new SetMap<String, String>();
        //处理使用@AttributeOverride情况
        catchAttributeOverrideColumnName(entityClass, setMap);
        //混合继承模式情况，使用父类的AttributeOverride
        if (entityClass.isAnnotationPresent(DiscriminatorValue.class) && entityClass.isAnnotationPresent(SecondaryTable.class)) {
            catchAttributeOverrideColumnName(entityClass.getSuperclass(), setMap);
        }
        for (PropertyDescriptor pd : Introspector.getBeanInfo(entityClass).getPropertyDescriptors()) {
            // 基本情况及使用@AttributeOverride情况
            if (pd.getReadMethod().isAnnotationPresent(Id.class) && !pd.getName().contains(Constants.ADB_SHARDING_ID)) {
                // 使用@AttributeOverride情况
                return setMap.get(pd.getName()) != null ? setMap.get(pd.getName()) : (pd.getReadMethod()
                        .getAnnotation(Column.class)).name();
            }
            // 复合组件情况
            if (pd.getReadMethod().isAnnotationPresent(EmbeddedId.class)) {
                Class returnType = pd.getReadMethod().getReturnType();
                Field[] returnTypeFields = returnType.getDeclaredFields();
                for (Field returnTypeField : returnTypeFields) {
                    PropertyDescriptor returnTypeFieldPd = new PropertyDescriptor(returnTypeField.getName(),
                            returnType);

                    if (!returnTypeFieldPd.getReadMethod().getAnnotation(Column.class).name().equals
                            (Constants.ARCHIVE_BASE_DATE_COLUMN_NAME)) {
                        return (returnTypeFieldPd.getReadMethod().getAnnotation(Column.class)).name();
                    }
                }
            }
        }
        return null;
    }

    private void catchAttributeOverrideColumnName(Class entityClass, SetMap setMap) {
        if (entityClass.isAnnotationPresent(AttributeOverrides.class)) {
            AttributeOverrides attributeOverrides = (AttributeOverrides) entityClass.getAnnotation(AttributeOverrides
                    .class);
            for (AttributeOverride attributeOverride : attributeOverrides.value()) {
                setMap.put(attributeOverride.name(), attributeOverride.column().name());
            }
            return;
        }
        if (entityClass.isAnnotationPresent(AttributeOverride.class)) {
            AttributeOverride attributeOverride = (AttributeOverride) entityClass.getAnnotation(AttributeOverride
                    .class);
            setMap.put(attributeOverride.name(), (attributeOverride.column().name()));
        }
    }

    private String getSequenceName(String tableName, String colnmName) {
        return "SEQ_" + Constants.MODEL_NAME + "_ADB_" + tableName + "_" + colnmName;
    }


    public Object findEntityByProcessInstId(String entityName , String processInstID){
        try {
            List entityList = findEntityListByProcessInstId(entityName , processInstID);
            if(entityList.size()>0){
                return entityList.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List findEntityListByProcessInstId(String entityName , String processInstID){
        try {
            List entityList = find("from " + entityName + " where processInstId=?", new Object[]{processInstID});
            return entityList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List findEntityListByParentProInstId(String entityName , String parentProInstId){
        try {
            List entityList = find("from " + entityName + " where parentProInstId=?", new Object[]{parentProInstId});
            return entityList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object findEntityByRootProInstId(String entityName , String rootProInstId){
        try {
            List entityList = find("from " + entityName + " where rootProInstId=?", new Object[]{rootProInstId});
            if(entityList.size()>0){
                return entityList.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Pager findByParametersPage(Pager page , String hql, Map<String, Object> parameters) throws DAOException {
        String countHql = "select count(*) " + hql.substring(hql.indexOf("from"));
        Query query = sessionFactory.getCurrentSession().createQuery(countHql);
        if (parameters != null) {
            for (String string : parameters.keySet()) {
                Object obj = parameters.get(string);
                if(obj instanceof Collection<?>){
                    query.setParameterList(string, (Collection<?>)obj);
                }else if(obj instanceof Object[]){
                    query.setParameterList(string, (Object[])obj);
                }else{
                    query.setParameter(string, obj);
                }
            }
        }
        List<Long> list = query.list();
        if(list != null && list.size() > 0){
            page.setRecordCount(list.get(0).intValue());
        }

        if(page.getRecordCount() > 0){
            query = sessionFactory.getCurrentSession().createQuery(hql);
            if (parameters != null) {
                for (String string : parameters.keySet()) {
                    Object obj = parameters.get(string);
                    if(obj instanceof Collection<?>){
                        query.setParameterList(string, (Collection<?>)obj);
                    }else if(obj instanceof Object[]){
                        query.setParameterList(string, (Object[])obj);
                    }else{
                        query.setParameter(string, obj);
                    }
                }
            }
            query.setFirstResult(page.getStartRecord()).setMaxResults(page.getPageSize());
            list = query.list();

        } else {
            list = new ArrayList();
        }

        page.setExhibitDatas(list);
        page.setPageCount(page.getRecordCount() % page.getPageSize() == 0 ? page.getRecordCount()/page.getPageSize() : page.getRecordCount()/page.getPageSize() + 1);
//        query.setFirstResult(page.getStart()).setMaxResults(page.getPageSize());
//        List list = query.list();
        return page;
    }
}
