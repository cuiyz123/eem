package com.metarnet.core.common.utils;

import com.metarnet.core.common.model.ParticipantField;
import com.sun.xml.internal.ws.util.UtilException;

import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jietianwu
 * Date: 13-11-22
 * Time: 上午9:24
 * 业务实体注解读取解析类
 */
public class AnnotationReader {
    private static final ConcurrentHashMap<Class, EntityBinder> entityBinders = new ConcurrentHashMap();

    /**
     * 读取并解析业务实体中的注解信息
     *
     * @param entityClass 业务实体
     * @return
     * @throws com.sun.xml.internal.ws.util.UtilException
     */
    public static EntityBinder readEntity(Class entityClass) throws UtilException {
        if (entityBinders.contains(entityClass)) {
            return entityBinders.get(entityClass);
        }
        EntityBinder entityBinder = new EntityBinder();
        // 得到key为属性名，value为属性对应的PropertyDescriptor的HashMap
        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(entityClass).getPropertyDescriptors()) {
                if (pd.getName().equals("class")) {
                    continue;
                }
                Field field = getField(entityClass, pd.getName());
                //解析主键
                if (pd.getReadMethod().isAnnotationPresent(Id.class) || pd.getReadMethod().isAnnotationPresent(EmbeddedId.class)) {
                    entityBinder.getIdBinderList().add(new IdBinder(pd.getName(), pd,
                            pd.getReadMethod().getAnnotation(EmbeddedId.class) == null ? Id.class : EmbeddedId.class));
                    continue;
                }
                //解析ParticipantField注解
                else if (field.isAnnotationPresent(ParticipantField.class)) {
                    entityBinder.setParticipantField(field);
                }
            }
        } catch (Exception e) {
            throw new UtilException(e);
        }
        entityBinders.put(entityClass, entityBinder);
        return entityBinder;
    }

    private static Field getField(Class aClass, String fieldName) throws NoSuchFieldException {

        try {
            return aClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            try {
                return getField(aClass.getSuperclass(), fieldName);
            } catch (NullPointerException e1) {
                System.out.println(aClass.getName() + "**********" + fieldName);
                throw new NoSuchFieldError();
            }
        }
    }
}
