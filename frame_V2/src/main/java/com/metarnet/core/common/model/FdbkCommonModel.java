package com.metarnet.core.common.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.sql.Timestamp;

/**
 * Created with IntelliJ IDEA.
 * User: wangzwty
 * Date: 16-3-31
 * Time: 下午4:26
 * 反馈单
 */
@MappedSuperclass
@AttributeOverride(name = "objectId", column = @Column(name = "FEEDBACK_FORM_ID"))
public abstract class FdbkCommonModel extends BaseForm {

    private Boolean isTimeout;

    private String disAssignObjectId;

    private String disAssignObjectName;

    private String disAssignObjectType;

    @Column(name = "TIME_OUT")
    public Boolean getIsTimeout() {
        return isTimeout;
    }

    public void setIsTimeout(Boolean isTimeout) {
        this.isTimeout = isTimeout;
    }

    @Column(name = "DIS_ASSIGN_OBJECT_ID")
    public String getDisAssignObjectId() {
        return disAssignObjectId;
    }

    public void setDisAssignObjectId(String disAssignObjectId) {
        this.disAssignObjectId = disAssignObjectId;
    }

    @Column(name = "DIS_ASSIGN_OBJECT_NAME")
    public String getDisAssignObjectName() {
        return disAssignObjectName;
    }

    public void setDisAssignObjectName(String disAssignObjectName) {
        this.disAssignObjectName = disAssignObjectName;
    }

    @Column(name = "DIS_ASSIGN_OBJECT_TYPE")
    public String getDisAssignObjectType() {
        return disAssignObjectType;
    }

    public void setDisAssignObjectType(String disAssignObjectType) {
        this.disAssignObjectType = disAssignObjectType;
    }
}
