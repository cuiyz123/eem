package com.metarnet.core.common.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jietianwu on 13-12-27.
 * 标识实体中哪个字段是下一步处理人
 * 格式逗号分割
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ParticipantField {
}
