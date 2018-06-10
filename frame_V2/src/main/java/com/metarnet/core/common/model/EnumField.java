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
 * 说明: 用于注解字段为枚举,从而根据枚举id、枚举类型查询出name
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EnumField {
    String name();
    String type();
}
