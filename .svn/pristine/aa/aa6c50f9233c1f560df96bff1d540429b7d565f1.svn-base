package com.metarnet.eomeem.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "EVALUATION_REPORT_TIME")
public class EvaluationReportTime {
    private Long objectID;
    private Long createdBy;/*创建人*/
    private String createrTrueName;
    private String ywdepart;      //省份编码
    private String name;          //省份名字
    private String isReport;      //是否进行汇总
    private Boolean deletedFlag;

    public String getIsReport() {
        return isReport;
    }

    public void setIsReport(String isReport) {
        this.isReport = isReport;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Id
    @Column(name = "objectID", nullable = false, updatable = false)
    public Long getObjectID() {
        return objectID;
    }

    public void setObjectID(Long objectID) {
        this.objectID = objectID;
    }

    public String getYwdepart() {
        return ywdepart;
    }

    public void setYwdepart(String ywdepart) {
        this.ywdepart = ywdepart;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreaterTrueName() {
        return createrTrueName;
    }

    public void setCreaterTrueName(String createrTrueName) {
        this.createrTrueName = createrTrueName;
    }

    public Boolean getDeletedFlag() {
        return deletedFlag;
    }

    public void setDeletedFlag(Boolean deletedFlag) {
        this.deletedFlag = deletedFlag;
    }
}
