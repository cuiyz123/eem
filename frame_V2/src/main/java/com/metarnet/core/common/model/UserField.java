package com.metarnet.core.common.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: liujun
 * Date: 13-4-2
 * Time: 下午3:08
 * 说明: 用于注解字段为用户,从而根据用户id查询出name
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UserField {
    //目标属性
    String name();
    //是否使用session中的信息,初始化表单会使用,如申请人
    boolean whetherUseSession() default false;
}
