package com.metarnet.eomeem.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "evaluation_collect_time")
public class EvaluationCollectTime {
    private Long objectID;
    private Long createdBy;/*创建人*/
    private String createdTrueName;
    private String createdUserName;
    private Long tempID;//模板ID
    private Long reportTempID;//模板ID
    private String tempName;//模板名称
    private String reportYear;//上报年份
    private String reportDate;//上报时间
    private String provinceCodes;//汇总省分编码
    private String provinceNames;//汇总省分名称
    private Boolean deletedFlag;
    private Date createDate;
    private Date lastUpdateDate;

    @Id
    @Column(name = "COLLECT_TIME_ID", unique = true, nullable = false)
    public Long getObjectID() {
        return objectID;
    }

    public void setObjectID(Long objectID) {
        this.objectID = objectID;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedTrueName() {
        return createdTrueName;
    }

    public void setCreatedTrueName(String createdTrueName) {
        this.createdTrueName = createdTrueName;
    }

    public String getCreatedUserName() {
        return createdUserName;
    }

    public void setCreatedUserName(String createdUserName) {
        this.createdUserName = createdUserName;
    }

    public Long getTempID() {
        return tempID;
    }

    public void setTempID(Long tempID) {
        this.tempID = tempID;
    }

    public String getTempName() {
        return tempName;
    }

    public void setTempName(String tempName) {
        this.tempName = tempName;
    }

    public String getReportYear() {
        return reportYear;
    }

    public void setReportYear(String reportYear) {
        this.reportYear = reportYear;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public String getProvinceCodes() {
        return provinceCodes;
    }

    public void setProvinceCodes(String provinceCodes) {
        this.provinceCodes = provinceCodes;
    }

    public String getProvinceNames() {
        return provinceNames;
    }

    public void setProvinceNames(String provinceNames) {
        this.provinceNames = provinceNames;
    }

    public Boolean getDeletedFlag() {
        return deletedFlag;
    }

    public void setDeletedFlag(Boolean deletedFlag) {
        this.deletedFlag = deletedFlag;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getReportTempID() {
        return reportTempID;
    }

    public void setReportTempID(Long reportTempID) {
        this.reportTempID = reportTempID;
    }
}
