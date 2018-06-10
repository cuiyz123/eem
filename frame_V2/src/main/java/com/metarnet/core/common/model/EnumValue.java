package com.metarnet.core.common.model;

import javax.persistence.Id;
import java.util.Date;

/**
 * Created by Administrator on 2015/7/13.
 */
public class EnumValue {
    private Integer enumValueId;
    private Integer enumItemId;
    private String enumItemCode;
    private String enumValueCode;
    private Integer enumValueNum;
    private String enumValueName;
    private String enumValueDesc;
    private Integer parentEnumValueId;
    private Integer sortNum;
    private Integer lifeCircleStatus;
    private Date startActiveDate;
    private Date endActiveDate;
    private Integer orgId;
    private String attribute1;
    private String attribute2;
    private String attribute3;
    private String attribute4;
    private String attribute5;
    private Integer createdBy;
    private Date creationDate;
    private Integer lastUpdatedBy;
    private Date lastUpdateDate;
    private Integer recordVersion;
    private Integer deletedBy;
    private Date deletionDate;
    private String key;
    private String value;

    @Id
    public Integer getEnumValueId() {
        return enumValueId;
    }

    public void setEnumValueId(Integer enumValueId) {
        this.enumValueId = enumValueId;
    }

    public Integer getEnumItemId() {
        return enumItemId;
    }

    public void setEnumItemId(Integer enumItemId) {
        this.enumItemId = enumItemId;
    }

    public String getEnumItemCode() {
        return enumItemCode;
    }

    public void setEnumItemCode(String enumItemCode) {
        this.enumItemCode = enumItemCode;
    }

    public String getEnumValueCode() {
        return enumValueCode;
    }

    public void setEnumValueCode(String enumValueCode) {
        this.enumValueCode = enumValueCode;
    }

    public Integer getEnumValueNum() {
        return enumValueNum;
    }

    public void setEnumValueNum(Integer enumValueNum) {
        this.enumValueNum = enumValueNum;
    }

    public String getEnumValueName() {
        return enumValueName;
    }

    public void setEnumValueName(String enumValueName) {
        this.enumValueName = enumValueName;
    }

    public String getEnumValueDesc() {
        return enumValueDesc;
    }

    public void setEnumValueDesc(String enumValueDesc) {
        this.enumValueDesc = enumValueDesc;
    }

    public Integer getParentEnumValueId() {
        return parentEnumValueId;
    }

    public void setParentEnumValueId(Integer parentEnumValueId) {
        this.parentEnumValueId = parentEnumValueId;
    }

    public Integer getSortNum() {
        return sortNum;
    }

    public void setSortNum(Integer sortNum) {
        this.sortNum = sortNum;
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

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Integer getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Integer lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
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

    public Date getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(Date deletionDate) {
        this.deletionDate = deletionDate;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
