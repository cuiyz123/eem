package com.metarnet.core.common.model;

import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Administrator on 2015/7/13.
 */
public class EnumItem {
    private String enumItemCode;
    private String enumItemName;
    private Integer enumItemId;
    private String enumItemDesc;
    private Boolean allowCustomizationFlag;
    private Boolean setValueByOrgFlag;
    private String appId;
    private String moduleId;
    private Integer parentEnumItemId;
    private Integer lifeCircleStatus;
    private Date startActiveDate;
    private Date endActiveDate;
    private String attribute1;
    private String attribute2;
    private String attribute3;
    private String attribute4;
    private String attribute5;
    private Integer createdBy;
    private Timestamp creationDate;
    private Integer lastUpdatedBy;
    private Timestamp lastUpdateDate;
    private Integer recordVersion;
    private Integer deletedBy;
    private Timestamp deletionDate;

    public String getEnumItemCode() {
        return enumItemCode;
    }

    public void setEnumItemCode(String enumItemCode) {
        this.enumItemCode = enumItemCode;
    }

    public String getEnumItemName() {
        return enumItemName;
    }

    public void setEnumItemName(String enumItemName) {
        this.enumItemName = enumItemName;
    }

    @Id
    public Integer getEnumItemId() {
        return enumItemId;
    }

    public void setEnumItemId(Integer enumItemId) {
        this.enumItemId = enumItemId;
    }

    public String getEnumItemDesc() {
        return enumItemDesc;
    }

    public void setEnumItemDesc(String enumItemDesc) {
        this.enumItemDesc = enumItemDesc;
    }

    public Boolean getAllowCustomizationFlag() {
        return allowCustomizationFlag;
    }

    public void setAllowCustomizationFlag(Boolean allowCustomizationFlag) {
        this.allowCustomizationFlag = allowCustomizationFlag;
    }

    public Boolean getSetValueByOrgFlag() {
        return setValueByOrgFlag;
    }

    public void setSetValueByOrgFlag(Boolean setValueByOrgFlag) {
        this.setValueByOrgFlag = setValueByOrgFlag;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public Integer getParentEnumItemId() {
        return parentEnumItemId;
    }

    public void setParentEnumItemId(Integer parentEnumItemId) {
        this.parentEnumItemId = parentEnumItemId;
    }

    public Integer getLifeCircleStatus() {
        return lifeCircleStatus;
    }

    public void setLifeCircleStatus(Integer lifeCircleStatus) {
        this.lifeCircleStatus = lifeCircleStatus;
    }

    public Date getStartActiveDate() {
        return startActiveDate;
    }

    public void setStartActiveDate(Date startActiveDate) {
        this.startActiveDate = startActiveDate;
    }

    public Date getEndActiveDate() {
        return endActiveDate;
    }

    public void setEndActiveDate(Date endActiveDate) {
        this.endActiveDate = endActiveDate;
    }

    public String getAttribute1() {
        return attribute1;
    }

    public void setAttribute1(String attribute1) {
        this.attribute1 = attribute1;
    }

    public String getAttribute2() {
        return attribute2;
    }

    public void setAttribute2(String attribute2) {
        this.attribute2 = attribute2;
    }

    public String getAttribute3() {
        return attribute3;
    }

    public void setAttribute3(String attribute3) {
        this.attribute3 = attribute3;
    }

    public String getAttribute4() {
        return attribute4;
    }

    public void setAttribute4(String attribute4) {
        this.attribute4 = attribute4;
    }

    public String getAttribute5() {
        return attribute5;
    }

    public void setAttribute5(String attribute5) {
        this.attribute5 = attribute5;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public Integer getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Integer lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Timestamp getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Timestamp lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Integer getRecordVersion() {
        return recordVersion;
    }

    public void setRecordVersion(Integer recordVersion) {
        this.recordVersion = recordVersion;
    }

    public Integer getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(Integer deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Timestamp getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(Timestamp deletionDate) {
        this.deletionDate = deletionDate;
    }
}
