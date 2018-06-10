package com.metarnet.core.common.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: jietianwu
 * Date: 13-4-24
 * Time: 下午8:48
 * 表示工单编号索引编号通过该字段建立的
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OrderNum {

}
