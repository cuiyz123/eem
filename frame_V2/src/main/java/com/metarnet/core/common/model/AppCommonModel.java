package com.metarnet.core.common.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Created with IntelliJ IDEA.
 * User: wangzwty
 * Date: 16-3-31
 * Time: 下午4:27
 * 申请单基类
 */
@MappedSuperclass
@AttributeOverride(name = "objectId", column = @Column(name = "APP_ID"))
public class AppCommonModel extends BaseForm {


}
