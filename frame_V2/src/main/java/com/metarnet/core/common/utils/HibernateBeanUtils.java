package com.metarnet.core.common.utils;

import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.EnumConfigAdapter;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.model.*;
import com.sun.xml.internal.ws.util.UtilException;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.HibernateException;

import javax.persistence.*;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA. User: metarnet Date: 13-3-26 Time: 下午8:53
 * hibernate实体bean的工具类
 */
public class HibernateBeanUtils {
    private static final Class[] AnnotationClasses = new Class[]{
            ManyToOne.class, OneToMany.class, OneToOne.class, ManyToMany.class};

    /**
     * 递归赋值
     *
     * @param source        被赋值对象
     * @param propertyName  属性名
     * @param propertyValue 属性值
     */
    public static void recursionEvaluate(Object source, String propertyName, Object propertyValue)
            throws UtilException {
        recursionEvaluate(source, propertyName, propertyValue, null);
    }

    /**
     * 判断该属性是否具有javax.persistence的注解
     *
     * @param pd
     */
    private static Boolean isHavePersistenceAnnotation(PropertyDescriptor pd) throws IntrospectionException {
        for (Class clazz : AnnotationClasses) {
            if (null != pd.getReadMethod().getAnnotation(clazz)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 递归赋值
     *
     * @param source        被赋值对象
     * @param propertyName  属性名
     * @param propertyValue 属性值
     * @param handleList    已解析的对象列表
     */
    private static void recursionEvaluate(Object source, String propertyName, Object propertyValue, List handleList)
            throws UtilException {
        if (null == handleList) {
            handleList = new ArrayList();
        }
        // 得到key为属性名，value为属性对应的PropertyDescriptor的HashMap
        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(source.getClass()).getPropertyDescriptors()) {
                if (pd.getName().equals(propertyName)) {
                    pd.getWriteMethod().invoke(source, propertyValue);
                }
                if (isHavePersistenceAnnotation(pd) && !handleList.contains(pd.getReadMethod().invoke(source,
                        null))) {
                    handleList.add(pd.getReadMethod().invoke(source, null));
                    recursionEvaluate(source, propertyName, propertyValue, handleList);
                }
            }
        } catch (Exception e) {
            throw new UtilException(e);
        }
    }

    /**
     * 反射调用get方法
     *
     * @param source       对象
     * @param propertyName 属性名
     * @return
     * @throws java.beans.IntrospectionException
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object getValue(Object source, String propertyName) throws IntrospectionException,
            InvocationTargetException, IllegalAccessException {
        return new PropertyDescriptor(propertyName, source.getClass()).getReadMethod().invoke(source, null);
    }


    /**
     * 设置删除标记
     *
     * @param entity      删除实体
     * @param deletedFlag
     * @throws com.sun.xml.internal.ws.util.UtilException
     */
    public static void setDeletedFlag(Object entity, Boolean deletedFlag) throws UtilException {
        setDeletedFlag(entity, null, deletedFlag);
    }

    /**
     * 设置删除标记
     *
     * @param entity      删除实体
     * @param userId      当前用户ID
     * @param deletedFlag
     * @throws com.sun.xml.internal.ws.util.UtilException
     */
    public static void setDeletedFlag(Object entity, Integer userId, Boolean deletedFlag) throws UtilException {
        setValue(entity, Constants.DELETED_FLAG, deletedFlag);
        setDateValue(entity, Constants.DELETION_TIME);
        if (null != userId) {
            setValue(entity, Constants.DELETED_BY, userId);
        }
    }

    /**
     * 设置属性值
     *
     * @param entity   实例名
     * @param property 属性名
     * @param value    属性值
     * @throws com.sun.xml.internal.ws.util.UtilException
     */
    public static void setValue(Object entity, String property, Object value) throws UtilException {
        try {
            new PropertyDescriptor(property, entity.getClass()).getWriteMethod().invoke(entity, value);
        } catch (Exception e) {
            return;
        }
    }

    /**
     * 设置日期类型
     *
     * @param entity   实体名
     * @param property 属性名
     * @throws com.sun.xml.internal.ws.util.UtilException
     */
    private static void setDateValue(Object entity, String property) throws UtilException {
        setDateValue(entity, property, null);
    }

    /**
     * 设置日期类型
     *
     * @param entity   实体名
     * @param property 属性名
     * @throws com.sun.xml.internal.ws.util.UtilException
     */
    private static void setDateValue(Object entity, String property, Long value) throws UtilException {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(property, entity.getClass());
            if (entity instanceof GeneralInfoModel && Constants.CREATION_TIME.equals(property)) {
                if (((GeneralInfoModel) entity).getCreationTime() != null)
                    return;
            }
            if (pd.getPropertyType().isAssignableFrom(Timestamp.class)) {
                setValue(entity, property, new Timestamp(value == null ? new Date().getTime() : value));
                return;
            }
            if (pd.getPropertyType().isAssignableFrom(Date.class)) {
                setValue(entity, property, new Date(value == null ? new Date().getTime() : value));
                return;
            }
            if (pd.getPropertyType().isAssignableFrom(java.sql.Date.class)) {
                setValue(entity, property, new java.sql.Date(value == null ? new Date().getTime() : value));
            }
        } catch (Exception e) {
            throw new UtilException(e);
        }
    }

    /**
     * 设置归档日期类型
     *
     * @param entity 实体名
     * @throws com.sun.xml.internal.ws.util.UtilException
     */
    public static void setArchiveBaseDate(Object entity) throws UtilException {
        try {
            entity.getClass().getDeclaredField(Constants.ARCHIVE_BASE_DATE);
        } catch (NoSuchFieldException e) {
            return;
        }
        try {
            setDateValue(entity, Constants.ARCHIVE_BASE_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse
                    (Constants.ARCHIVE_BASE_DATE_VALUE).getTime());
        } catch (Exception e) {
            throw new UtilException(e);
        }
    }

    /**
     * 设置创建相关信息（创建人，创建时间）
     *
     * @param entity     业务实体
     * @param userEntity 当前用户
     * @throws com.sun.xml.internal.ws.util.UtilException
     */
    public static void setCreatedInfo(Object entity, UserEntity userEntity) throws UtilException {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(Constants.CREATED_BY, entity.getClass());
            if (null != pd.getReadMethod().invoke(entity, null) && !new Integer(0).equals(pd.getReadMethod().invoke
                    (entity, null))) {
                return;
            }
        } catch (Exception e) {
            throw new UtilException(e);
        }
        setValue(entity, Constants.CREATED_BY, userEntity.getUserId());
        setDateValue(entity, Constants.CREATION_TIME);
        setArchiveBaseDate(entity);
    }

    /**
     * 设置创建相关信息（创建人，创建时间）
     *
     * @param entity 业务实体
     * @param userId 当前用户ID
     * @throws com.sun.xml.internal.ws.util.UtilException
     */
    public static void setCreatedInfo(Object entity, Integer userId) throws UtilException {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(Constants.CREATED_BY, entity.getClass());
            if (null != pd.getReadMethod().invoke(entity, null)) {
                return;
            }
        } catch (Exception e) {
            throw new UtilException(e);
        }
        setValue(entity, Constants.CREATED_BY, userId);
        setValue(entity, Constants.RECORD_VERSION, 1);
        setDateValue(entity, Constants.CREATION_TIME);
        setArchiveBaseDate(entity);
    }

    /**
     * 设置更新相关信息（创建人，创建时间）
     *
     * @param entity 业务实体
     * @param userId 当前用户ID
     * @throws com.sun.xml.internal.ws.util.UtilException
     */
    public static void setLastUpdateInfo(Object entity, Long userId) throws UtilException {
        setValue(entity, Constants.LAST_UPDATED_BY, userId);
        setDateValue(entity, Constants.LAST_UPDATE_TIME);
    }

    /**
     * 获取Hibernate实体对应的表名
     *
     * @param entityClass 实体类
     */
    public static String getTableName(Class entityClass) {
        if (entityClass.isAssignableFrom(Serializable.class)) {
            return null;
        }
        if (!entityClass.isAnnotationPresent(Table.class)) {
            return getTableName(entityClass.getSuperclass());
        }
        return ((Table) entityClass.getAnnotation(Table.class)).name();
    }

    /**
     * 设置id值
     *
     * @param entity     实体
     * @param userEntity 当前用户
     */
    public static void setId(Object entity, UserEntity userEntity) throws UtilException {
        setId(entity, entity.getClass(), userEntity);
    }

    /**
     * 设置id值
     *
     * @param entity     实体
     * @param clazz      要解析的class（可能是实体的父类）
     * @param userEntity 当前用户
     */
    public static void setId(Object entity, Class clazz, UserEntity userEntity) throws UtilException {
        EntityBinder entityBinder = AnnotationReader.readEntity(clazz);
        for (IdBinder idBinder : entityBinder.getIdBinderList()) {
            try {
                /**
                 * 临时注释 by zwwang 2015-9-15
                 */
//                if (Constants.ADB_SHARDING_ID.equals(idBinder.getFieldName()) && null == idBinder.getPd().getReadMethod().invoke(entity,
//                        null)) {
//                    idBinder.getPd().getWriteMethod().invoke(entity, DataRouteAdapter.getInstance().findShardInfoByOrgId(userEntity
//                            .getOrgID().intValue()).getShardingId());
//                    continue;
//                }
                if (null != idBinder.getPd().getReadMethod().invoke(entity, null)) {
                    return;
                }
                if (idBinder.getAnnotationClass().equals(EmbeddedId.class)) {
                    Object id = Class.forName(idBinder.getPd().getPropertyType().getName()).newInstance();
                    setId(id, userEntity);
                    idBinder.getPd().getWriteMethod().invoke(entity, id);
                    return;
                }

                idBinder.getPd().getWriteMethod().invoke(entity, ((IBaseDAO) SpringContextUtils.getBean("baseDAO")).getSequenceNextValue
                        (entity.getClass()));
                return;
            } catch (Exception e) {
                throw new UtilException(e);
            }
        }
        setId(entity, clazz.getSuperclass(), userEntity);
    }


    /**
     * 修改hibernate源码使用，替换shardingId
     *
     * @param id  联合主键
     * @param sql
     * @return
     */
    public static String reqlaceShardingId(Serializable id, String sql) {
        Field[] fields = id.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), id.getClass());
                if (!field.getName().contains("shardingId")) {
                    continue;
                }
                return sql.replace(":shardingId", String.valueOf(pd.getReadMethod().invoke(id, null)));
            } catch (Exception e) {
                throw new HibernateException(e);
            }
        }
        return sql;
    }


    /**
     * 将实体中带有Exclude注解的属性的属性值设为null
     *
     * @param entity 1业务实体
     * @param list
     * @throws com.sun.xml.internal.ws.util.UtilException
     */
    public static void setNullByExcludeAnnotation(Object entity, List list) throws UtilException {
        try {
            if (entity.getClass().getName().contains("$$")) {
                return;
            }
            if (null == list) {
                list = new ArrayList();
            }
            if (list.contains(entity)) {
                return;
            }
            list.add(entity);

            for (PropertyDescriptor pd : Introspector.getBeanInfo(entity.getClass()).getPropertyDescriptors()) {
                Object value = pd.getReadMethod().invoke(entity, null);
                if (null == value) {
                    continue;
                }
                if (pd.getReadMethod().isAnnotationPresent(Exclude.class) || (!pd.getName().equals("class") && getField
                        (entity.getClass(), pd.getName()).isAnnotationPresent(Exclude.class))) {
                    pd.getWriteMethod().invoke(entity, new Object[]{null});
                    continue;
                }
                //主键也设置为空
                if ((pd.getReadMethod().isAnnotationPresent(Id.class)) && !Constants.ADB_SHARDING_ID.equals(pd.getName())) {
                    pd.getWriteMethod().invoke(entity, new Object[]{null});
                    continue;
                }
                if (Collection.class.isAssignableFrom(pd.getPropertyType()) && null != pd.getReadMethod().invoke(entity, null)) {
                    for (Iterator it = ((Collection) pd.getReadMethod().invoke(entity, null)).iterator(); it.hasNext(); ) {
                        setNullByExcludeAnnotation(it.next(), list);
                    }
                    continue;
                }
                if (value != null && pd.getPropertyType().isAnnotationPresent(Entity.class)) {
                    setNullByExcludeAnnotation(value, list);
                }
            }
        } catch (Exception e) {
            throw new UtilException(e);
        }
    }

    /**
     * 递归获取类的某个属性
     *
     * @param clazz     类
     * @param fieldName 属性名
     * @return
     * @throws NoSuchFieldException
     */
    public static Field getField(Class clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (null != clazz.getSuperclass()) {
                return getField(clazz.getSuperclass(), fieldName);
            }
            throw e;
        }
    }


    /**
     * 将实体中带有Exclude注解的属性的属性值设为null
     *
     * @param entity 业务实体
     * @throws com.sun.xml.internal.ws.util.UtilException
     */
    public static void setNullByExcludeAnnotation(Object entity) throws UtilException {
        setNullByExcludeAnnotation(entity, new ArrayList());
    }

    /**
     * 基于注解初始化entity一些默认值，目前支持人员，部门，枚举值，日期的初始化
     *
     * @param entity 业务实体
     * @throws com.sun.xml.internal.ws.util.UtilException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws com.ucloud.paas.agent.PaasException
     */
    public static void interpretField(Object entity) throws UtilException {
        interpretField(entity, null);
    }

    /**
     * 基于注解初始化entity一些默认值，目前支持人员，部门，枚举值，日期的初始化
     *
     * @param entity     业务实体
     * @param userEntity 当前登录用户
     * @ throws UtilException
     */
    public static void interpretField(Object entity, UserEntity userEntity) throws UtilException {
        interpretField(entity, userEntity, new ArrayList());
    }

    /**
     * 基于注解初始化entity一些默认值，目前支持人员，部门，枚举值，日期的初始化
     *
     * @param entity     业务实体
     * @param userEntity 当前登录用户
     * @ throws UtilException
     */

    private static void interpretField(Object entity, UserEntity userEntity, List<Class> list) throws UtilException {
        try {
            if (list.contains(entity.getClass())) {
                return;
            }
            list.add(entity.getClass());
            EntityBinder entityBinder = AnnotationReader.readEntity(entity.getClass());
            //解析UserField
            for (Field field : entityBinder.getUserFields()) {
                //原和目标数据为空时不做处理
                PropertyDescriptor sourcePD = new PropertyDescriptor(field.getName(), entity.getClass());
                PropertyDescriptor tagerPD = new PropertyDescriptor(field.getAnnotation(UserField.class).name(), entity.getClass());
                Object fieldValue = sourcePD.getReadMethod().invoke(entity, null);
                if (null != tagerPD.getReadMethod().invoke(entity, null)) {
                    continue;
                }
                /**
                 * 是否过滤  fieldValue	userEntity	whetherUseSession
                 *    是          是	        否	            否
                 *    是          是	        是	            否
                 *    是          是	        是	            是
                 *    是          是	        否	            是
                 *    否          否	        否	            否
                 *    否          否	        是	            否
                 *    是          否	        是	            是
                 *    否          否	        否	            是
                 *
                 */
                if (fieldValue == null && (userEntity == null || !field.getAnnotation(UserField.class).whetherUseSession())) {
                    continue;
                }
                //初始化时使用当前session信息
                if (fieldValue == null) {
                    tagerPD.getWriteMethod().invoke(entity, userEntity.getTrueName());
                    sourcePD.getWriteMethod().invoke(entity, userEntity.getUserId());
                    continue;
                }
                if (field.getType().isAssignableFrom(String.class)) {
                    StringBuffer empNames = new StringBuffer();
                    for (String userId : ((String) fieldValue).split(",")) {
                        empNames.append(AAAAAdapter.getInstence().findUserbyUserID(Integer.valueOf(userId)).getTrueName()).append(",");
                    }
                    if (StringUtils.isNotEmpty(empNames)) {
                        empNames.deleteCharAt(empNames.length() - 1);
                        tagerPD.getWriteMethod().invoke(entity, empNames);
                    }
                    continue;
                }

                UserEntity user = AAAAAdapter.getInstence().findUserbyUserID((Integer) fieldValue);
                tagerPD.getWriteMethod().invoke(entity, user.getTrueName());
            }
            //解析DeptField
            for (Field field : entityBinder.getDeptFields()) {
                //原和目标数据为空时不做处理
                PropertyDescriptor sourcePD = new PropertyDescriptor(field.getName(), entity.getClass());
                PropertyDescriptor tagerPD = new PropertyDescriptor(field.getAnnotation(DeptField.class).name(), entity.getClass());
                Object fieldValue = sourcePD.getReadMethod().invoke(entity, null);
                if (null != tagerPD.getReadMethod().invoke(entity, null)) {
                    continue;
                }
                /**
                 * 是否过滤  fieldValue	userEntity	whetherUseSession
                 *    是          是	        否	            否
                 *    是          是	        是	            否
                 *    是          是	        是	            是
                 *    是          是	        否	            是
                 *    否          否	        否	            否
                 *    否          否	        是	            否
                 *    是          否	        是	            是
                 *    否          否	        否	            是
                 *
                 */
                if (fieldValue == null && (userEntity == null || !field.getAnnotation(DeptField.class).whetherUseSession())) {
                    continue;
                }
                //初始化时使用当前session信息
                if (fieldValue == null) {
                    tagerPD.getWriteMethod().invoke(entity, AAAAAdapter.getInstence().findOrgByOrgID(userEntity.getOrgID()).getOrgName());
                    sourcePD.getWriteMethod().invoke(entity, userEntity.getOrgID());
                    continue;
                }
                if (field.getType().isAssignableFrom(String.class)) {
                    StringBuffer orgNames = new StringBuffer();
                    for (String userId : ((String) fieldValue).split(",")) {
                        orgNames.append(AAAAAdapter.getInstence().findOrgByOrgID(Long.valueOf(userId)).getOrgName()).append(",");
                    }
                    if (StringUtils.isNotEmpty(orgNames)) {
                        orgNames.deleteCharAt(orgNames.length() - 1);
                        tagerPD.getWriteMethod().invoke(entity, orgNames);
                    }
                    continue;
                }
                OrgEntity org = AAAAAdapter.getInstence().findOrgByOrgID((Long) fieldValue);
                tagerPD.getWriteMethod().invoke(entity, org.getOrgName());
            }
            //解析DeptField
            for (Field field : entityBinder.getEnumFields()) {
                //原和目标数据为空时不做处理
                PropertyDescriptor sourcePD = new PropertyDescriptor(field.getName(), entity.getClass());
                Object fieldValue = sourcePD.getReadMethod().invoke(entity, null);
                if (fieldValue == null) {
                    continue;
                }
                PropertyDescriptor tagerPD = new PropertyDescriptor(field.getAnnotation(EnumField.class).name(), entity.getClass());
                if (null != tagerPD.getReadMethod().invoke(entity, null)) {
                    continue;
                }
                EnumValue ev = EnumConfigAdapter.getInstence().getEnumValueById((Integer) sourcePD.getReadMethod().invoke(entity, null));
                tagerPD.getWriteMethod().invoke(entity, ev.getEnumValueName());
            }
            //解析DateField
            for (Field field : entityBinder.getDateFields()) {
                PropertyDescriptor sourcePD = new PropertyDescriptor(field.getName(), entity.getClass());
                Object fieldValue = sourcePD.getReadMethod().invoke(entity, null);
                if (fieldValue != null) {
                    continue;
                }
                Object value = null;
                if (field.getType().isAssignableFrom(Date.class)) {
                   /*field.set(entity, new Date(System.currentTimeMillis()));
                    continue;*/
                    value = new Date(System.currentTimeMillis());
                }
                if (field.getType().isAssignableFrom(java.sql.Date.class)) {
                   /* field.set(entity, new java.sql.Date(System.currentTimeMillis()));
                    continue;*/
                    value = new java.sql.Date(System.currentTimeMillis());
                }
                if (field.getType().isAssignableFrom(Timestamp.class)) {
                   /* field.set(entity, new Timestamp(System.currentTimeMillis()));*/
                    value = new Timestamp(System.currentTimeMillis());
                }
                sourcePD.getWriteMethod().invoke(entity, value);

            }
            //递归赋值关联实体
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (field.getDeclaringClass().isAnnotationPresent(Entity.class)) {
                    interpretField(entity, userEntity, list);
                }
            }
        } catch (Exception e) {
            throw new UtilException(e);
        }
    }

    /**
     * 给属性设置默认值
     *
     * @param entity 实体
     * @param pd     属性对应的PropertyDescriptor
     * @throws IllegalAccessException
     * @throws java.lang.reflect.InvocationTargetException
     * @throws java.text.ParseException
     */
    public static void setDefaultValue(Object entity, PropertyDescriptor pd) throws IllegalAccessException, InvocationTargetException,
            ParseException {
        if (pd.getReadMethod().invoke(entity, null) == null && pd.getPropertyType().equals(String.class)) {
            pd.getWriteMethod().invoke(entity, StringUtils.EMPTY);
            return;
        }
        if (pd.getReadMethod().invoke(entity, null) == null && pd.getPropertyType().equals(Integer.class)) {
            pd.getWriteMethod().invoke(entity, NumberUtils.INTEGER_ZERO);
            return;
        }
        if (pd.getReadMethod().invoke(entity, null) == null && pd.getPropertyType().equals(Long.class)) {
            pd.getWriteMethod().invoke(entity, NumberUtils.LONG_ZERO);
            return;
        }
        if (pd.getReadMethod().invoke(entity, null) == null && pd.getPropertyType().equals(Date.class)) {
            pd.getWriteMethod().invoke(entity, new SimpleDateFormat("yyyy-MM-dd").parse("0000-00-00"));
            return;
        }
        if (pd.getReadMethod().invoke(entity, null) == null && pd.getPropertyType().equals(Boolean.class)) {
            pd.getWriteMethod().invoke(entity, false);
            return;
        }
        if (pd.getReadMethod().invoke(entity, null) == null && pd.getPropertyType().equals(Timestamp.class)) {
            pd.getWriteMethod().invoke(entity, new Timestamp(NumberUtils.INTEGER_ZERO));
        }
    }

    /**
     * 保存前处理
     *
     * @param entity     业务实体
     * @param userEntity 用户信息
     * @throws com.sun.xml.internal.ws.util.UtilException
     */
    public static void beforeSaveHandle(Object entity, UserEntity userEntity) throws UtilException {
        try {
            beforeSaveHandle(entity, userEntity, null);
        } catch (Exception e) {
            throw new UtilException(e);
        }
    }

    /**
     * 保存前处理
     *
     * @param entity     业务实体
     * @param userEntity 用户信息
     * @throws com.sun.xml.internal.ws.util.UtilException
     * @throws java.beans.IntrospectionException
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    private static void beforeSaveHandle(Object entity, UserEntity userEntity, List list) throws UtilException,
            IntrospectionException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (entity.getClass().getName().contains("$$") || entity == null) {
            return;
        }
        if (null == list) {
            list = new ArrayList();
        }
        if (list.contains(entity)) {
            return;
        }
        list.add(entity);
        try {
            HibernateBeanUtils.setId(entity, userEntity);
            HibernateBeanUtils.setCreatedInfo(entity, userEntity);
            HibernateBeanUtils.setLastUpdateInfo(entity, userEntity.getUserId());
            HibernateBeanUtils.setDeletedFlag(entity, false);
            initNotNullDefaultValue(entity);
        } catch (Exception e) {
            throw new UtilException(e);
        }
        PropertyDescriptor targetPds[] = Introspector.getBeanInfo(entity.getClass()).getPropertyDescriptors();
        for (PropertyDescriptor pd : targetPds) {
            if (Collection.class.isAssignableFrom(pd.getPropertyType()) && null != pd.getReadMethod().invoke(entity,
                    null) && !pd.getReadMethod().isAnnotationPresent(Transient.class)) {
                for (Iterator it = ((Collection) pd.getReadMethod().invoke(entity, null)).iterator(); it.hasNext(); ) {
                    beforeSaveHandle(it.next(), userEntity, list);
                }
                continue;
            }
            Object value = pd.getReadMethod().invoke(entity, null);
            if (value != null && pd.getPropertyType().isAnnotationPresent(Entity.class)) {
                beforeSaveHandle(value, userEntity, list);
            }
        }
    }

    /**
     * 将非空字段设置默认值
     *
     * @param entity
     * @throws java.beans.IntrospectionException
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalAccessException
     * @throws java.text.ParseException
     */
    public static void initNotNullDefaultValue(Object entity) throws IntrospectionException, InvocationTargetException,
            IllegalAccessException, ParseException {
        initNotNullDefaultValue(entity, entity.getClass());
    }

    /**
     * 将非空字段设置默认值
     *
     * @param entity
     * @param clazz
     * @throws java.beans.IntrospectionException
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalAccessException
     * @throws java.text.ParseException
     */
    private static void initNotNullDefaultValue(Object entity, Class clazz) throws IntrospectionException,
            IllegalAccessException, InvocationTargetException, ParseException {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
            if (pd.getReadMethod().isAnnotationPresent(Column.class) && !pd.getReadMethod().getAnnotation(Column
                    .class).nullable()) {
                HibernateBeanUtils.setDefaultValue(entity, pd);
            }
        }
        if (clazz.getSuperclass() != null) {
            initNotNullDefaultValue(entity, clazz.getSuperclass());
        }
    }

    /**
     * 保存前处理
     *
     * @param entity     业务实体
     * @param userEntity 用户信息
     * @throws com.sun.xml.internal.ws.util.UtilException
     */
    public static void beforeSaveOrUpdateHandle(Object entity, UserEntity userEntity) throws UtilException,
            IntrospectionException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        beforeSaveOrUpdateHandle(entity, userEntity, null);
    }

    /**
     * 保存前处理
     *
     * @param entity     业务实体
     * @param userEntity 用户信息
     */
    private static void beforeSaveOrUpdateHandle(Object entity, UserEntity userEntity, List list) throws UtilException,
            IntrospectionException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (entity == null) {
            return;
        }
        if (null == list) {
            list = new ArrayList();
        }
        if (list.contains(entity)) {
            return;
        }
        list.add(entity);
        try {

            PropertyDescriptor createByPD = new PropertyDescriptor(Constants.CREATED_BY, entity.getClass());
            if (null == createByPD.getReadMethod().invoke(entity, null)) {
                HibernateBeanUtils.beforeSaveHandle(entity, userEntity);
                return;
            }
            for (PropertyDescriptor pd : Introspector.getBeanInfo(entity.getClass()).getPropertyDescriptors()) {
                if (pd.getPropertyType().isAnnotationPresent(Entity.class)) {
                    beforeSaveOrUpdateHandle(pd.getReadMethod().invoke(entity, null), userEntity, list);
                    continue;
                }


                if (Collection.class.isAssignableFrom(pd.getPropertyType()) && !pd.getReadMethod().isAnnotationPresent(Transient.class)) {
                    Collection collection = (Collection) pd.getReadMethod().invoke(entity, null);
                    if (collection != null) {
                        for (Iterator it = collection.iterator(); it.hasNext(); ) {
                            beforeSaveOrUpdateHandle(it.next(), userEntity, list);
                        }
                    }

                }
            }
            HibernateBeanUtils.setLastUpdateInfo(entity, userEntity.getUserId());
            initNotNullDefaultValue(entity);
        } catch (Exception e) {
            throw new UtilException(e);
        }
    }

}
