package com.metarnet.eomeem.model;

import com.metarnet.core.common.model.ParticipantField;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.sql.Timestamp;

/**
 * Created with IntelliJ IDEA.
 * User: wangzwty
 * Date: 16-3-31
 * Time: 上午9:57
 * 申请调度、反馈、通用处理信息的父类
 */
@MappedSuperclass
public class BaseEntity implements java.io.Serializable {

    private Long objectId;/*业务主键*/
    private Long createdBy;/*创建人*/
    private Timestamp creationTime;/*创建时间*/
    private Long lastUpdatedBy;/*最后修改人*/
    private Timestamp lastUpdateTime;/*最后修改时间*/
    private Boolean deletedFlag;/*删除标记*/
    private Long deletedBy;/*删除人*/
    private Timestamp deletionTime;/*删除时间*/

    private Integer shardingId; //分片

    private String workOrderStatus;    //工单状态
    private Integer specialty;    //专业
    private String specialtyName;    //专业中文

    //操作信息
    private Long operUserId;
    private String operUserPhone;
    private String operUserTrueName;
    private Integer operTypeEnumId;
    private Long operOrgId;
    private String operOrgName;
    private String operFullOrgName;
    private Timestamp operTime;
    private String operDesc;
    private Timestamp taskStartTime;


    //流程信息
    private String processInstId;
    private String parentProInstId;
    private String rootProInstId;
    private String taskInstId;
    private String activityInstName;

    //表单信息
    private Long applyId;//applyld 1 表示省份  2表示地市
    private Long dispatchId;
    private Long parentDisId;
    private Long rootDisId;
    private String appOrderNumber;
    private String disOrderNumber;
    private String theme;
    private Timestamp reqFdbkTime;


    private Integer belongProvinceCode;
    private String belongProvinceName;
    private Integer belongCityCode;
    private String belongCityName;


    private Boolean draftFlag;      //是否为草稿

    //下一步操作人
    @ParticipantField
    private String participantID;
    private String participantTrueName;

    //扩展字段
    private String attribute1;
    private String attribute2;
    private String attribute3;
    private String attribute4;
    private String attribute5;
    private Timestamp archiveBaseTime; //归档基准日期

    public BaseEntity() {
    }

    public BaseEntity(Long objectId) {
        this.objectId = objectId;
    }

    public BaseEntity(Long objectId,String attribute1) {
        this.objectId = objectId;
        this.attribute1 = attribute1;
    }

    public BaseEntity(Long objectId,Long createdBy, Timestamp creationTime) {
        this.objectId = objectId;
        this.createdBy = createdBy;
        this.creationTime = creationTime;
    }

    @Id
    @Column(name = "OBJECT_ID", nullable = false, updatable = false)
    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    @Column(name = "CREATED_BY")
    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name = "CREATION_TIME")
    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    @Column(name = "LAST_UPDATED_BY")
    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @Column(name = "LAST_UPDATE_TIME")
    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Column(name = "DELETED_FLAG")
    public Boolean getDeletedFlag() {
        return deletedFlag;
    }

    public void setDeletedFlag(Boolean deletedFlag) {
        this.deletedFlag = deletedFlag;
    }

    @Column(name = "DELETED_BY")
    public Long getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(Long deletedBy) {
        this.deletedBy = deletedBy;
    }

    @Column(name = "DELETION_TIME")
    public Timestamp getDeletionTime() {
        return deletionTime;
    }

    public void setDeletionTime(Timestamp deletionTime) {
        this.deletionTime = deletionTime;
    }

    @Column(name = "SHARDING_ID")
    public Integer getShardingId() {
        return shardingId;
    }

    public void setShardingId(Integer shardingId) {
        this.shardingId = shardingId;
    }

    @Column(name = "WORK_ORDER_STATUS")
    public String getWorkOrderStatus() {
        return workOrderStatus;
    }

    public void setWorkOrderStatus(String workOrderStatus) {
        this.workOrderStatus = workOrderStatus;
    }

    @Column(name = "SPECIALTY")
    public Integer getSpecialty() {
        return specialty;
    }

    public void setSpecialty(Integer specialty) {
        this.specialty = specialty;
    }

    @Column(name = "OPER_USER_ID")
    public Long getOperUserId() {
        return operUserId;
    }

    public void setOperUserId(Long operUserId) {
        this.operUserId = operUserId;
    }

    @Column(name = "OPER_TYPE_ENUM_ID")
    public Integer getOperTypeEnumId() {
        return operTypeEnumId;
    }

    public void setOperTypeEnumId(Integer operTypeEnumId) {
        this.operTypeEnumId = operTypeEnumId;
    }

    @Column(name = "OPER_ORG_ID")
    public Long getOperOrgId() {
        return operOrgId;
    }

    public void setOperOrgId(Long operOrgId) {
        this.operOrgId = operOrgId;
    }

    @Column(name = "OPER_TIME")
    public Timestamp getOperTime() {
        return operTime;
    }

    public void setOperTime(Timestamp operTime) {
        this.operTime = operTime;
    }

    @Column(name = "OPER_DESC")
    public String getOperDesc() {
        return operDesc;
    }

    public void setOperDesc(String operDesc) {
        this.operDesc = operDesc;
    }

    @Column(name = "PROCESS_INST_ID")
    public String getProcessInstId() {
        return processInstId;
    }

    public void setProcessInstId(String processInstId) {
        this.processInstId = processInstId;
    }

    @Column(name = "PARENT_PRO_INST_ID")
    public String getParentProInstId() {
        return parentProInstId;
    }

    public void setParentProInstId(String parentProInstId) {
        this.parentProInstId = parentProInstId;
    }

    @Column(name = "ROOT_PRO_INST_ID")
    public String getRootProInstId() {
        return rootProInstId;
    }

    public void setRootProInstId(String rootProInstId) {
        this.rootProInstId = rootProInstId;
    }

    @Column(name = "TASK_INST_ID")
    public String getTaskInstId() {
        return taskInstId;
    }

    public void setTaskInstId(String taskInstId) {
        this.taskInstId = taskInstId;
    }

    @Column(name = "ACTIVITY_INST_NAME")
    public String getActivityInstName() {
        return activityInstName;
    }

    public void setActivityInstName(String activityInstName) {
        this.activityInstName = activityInstName;
    }

    @Column(name = "APPLY_ID")
    public Long getApplyId() {
        return applyId;
    }

    public void setApplyId(Long applyId) {
        this.applyId = applyId;
    }

    @Column(name = "DISPATCH_ID")
    public Long getDispatchId() {
        return dispatchId;
    }

    public void setDispatchId(Long dispatchId) {
        this.dispatchId = dispatchId;
    }

    @Column(name = "PARENT_DIS_ID")
    public Long getParentDisId() {
        return parentDisId;
    }

    public void setParentDisId(Long parentDisId) {
        this.parentDisId = parentDisId;
    }

    @Column(name = "ROOT_DIS_ID")
    public Long getRootDisId() {
        return rootDisId;
    }

    public void setRootDisId(Long rootDisId) {
        this.rootDisId = rootDisId;
    }

    @Column(name = "APP_ORDER_NUMBER")
    public String getAppOrderNumber() {
        return appOrderNumber;
    }

    public void setAppOrderNumber(String appOrderNumber) {
        this.appOrderNumber = appOrderNumber;
    }

    @Column(name = "DIS_ORDER_NUMBER")
    public String getDisOrderNumber() {
        return disOrderNumber;
    }

    public void setDisOrderNumber(String disOrderNumber) {
        this.disOrderNumber = disOrderNumber;
    }

    @Column(name = "BELONG_PROVINCE_CODE")
    public Integer getBelongProvinceCode() {
        return belongProvinceCode;
    }

    public void setBelongProvinceCode(Integer belongProvinceCode) {
        this.belongProvinceCode = belongProvinceCode;
    }

    @Column(name = "BELONG_CITY_CODE")
    public Integer getBelongCityCode() {
        return belongCityCode;
    }

    public void setBelongCityCode(Integer belongCityCode) {
        this.belongCityCode = belongCityCode;
    }

    @Column(name = "DRAFT_FLAG")
    public Boolean getDraftFlag() {
        return draftFlag;
    }

    public void setDraftFlag(Boolean draftFlag) {
        this.draftFlag = draftFlag;
    }

    @Column(name = "PARTICIPANT_ID")
    public String getParticipantID() {
        return participantID;
    }

    public void setParticipantID(String participantID) {
        this.participantID = participantID;
    }

    @Column(name = "PARTICIPANT_TRUENAME")
    public String getParticipantTrueName() {
        return participantTrueName;
    }

    public void setParticipantTrueName(String participantTrueName) {
        this.participantTrueName = participantTrueName;
    }

    @Column(name = "ATTRIBUTE1")
    public String getAttribute1() {
        return attribute1;
    }

    public void setAttribute1(String attribute1) {
        this.attribute1 = attribute1;
    }

    @Column(name = "ATTRIBUTE2")
    public String getAttribute2() {
        return attribute2;
    }

    public void setAttribute2(String attribute2) {
        this.attribute2 = attribute2;
    }

    @Column(name = "ATTRIBUTE3")
    public String getAttribute3() {
        return attribute3;
    }

    public void setAttribute3(String attribute3) {
        this.attribute3 = attribute3;
    }

    @Column(name = "ATTRIBUTE4")
    public String getAttribute4() {
        return attribute4;
    }

    public void setAttribute4(String attribute4) {
        this.attribute4 = attribute4;
    }

    @Column(name = "ATTRIBUTE5")
    public String getAttribute5() {
        return attribute5;
    }

    public void setAttribute5(String attribute5) {
        this.attribute5 = attribute5;
    }

    @Column(name = "ARCHIVE_BASE_TIME")
    public Timestamp getArchiveBaseTime() {
        return archiveBaseTime;
    }

    public void setArchiveBaseTime(Timestamp archiveBaseTime) {
        this.archiveBaseTime = archiveBaseTime;
    }

    @Column(name = "THEME")
    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    @Column(name = "REQ_FDBK_TIME")
    public Timestamp getReqFdbkTime() {
        return reqFdbkTime;
    }

    public void setReqFdbkTime(Timestamp reqFdbkTime) {
        this.reqFdbkTime = reqFdbkTime;
    }

    @Column(name = "OPER_USER_PHONE")
    public String getOperUserPhone() {
        return operUserPhone;
    }

    public void setOperUserPhone(String operUserPhone) {
        this.operUserPhone = operUserPhone;
    }

    @Column(name = "OPER_USER_TRUE_NAME")
    public String getOperUserTrueName() {
        return operUserTrueName;
    }

    public void setOperUserTrueName(String operUserTrueName) {
        this.operUserTrueName = operUserTrueName;
    }

    @Column(name = "OPER_ORG_NAME")
    public String getOperOrgName() {
        return operOrgName;
    }

    public void setOperOrgName(String operOrgName) {
        this.operOrgName = operOrgName;
    }

    @Column(name = "TASK_START_TIME")
    public Timestamp getTaskStartTime() {
        return taskStartTime;
    }

    public void setTaskStartTime(Timestamp taskStartTime) {
        this.taskStartTime = taskStartTime;
    }

    @Column(name = "OPER_FULL_ORG_NAME")
    public String getOperFullOrgName() {
        return operFullOrgName;
    }

    public void setOperFullOrgName(String operFullOrgName) {
        this.operFullOrgName = operFullOrgName;
    }

    @Column(name = "BELONG_CITY_NAME")
    public String getBelongCityName() {
        return belongCityName;
    }

    public void setBelongCityName(String belongCityName) {
        this.belongCityName = belongCityName;
    }
    @Column(name = "BELONG_PROVINCE_NAME")
    public String getBelongProvinceName() {
        return belongProvinceName;
    }

    public void setBelongProvinceName(String belongProvinceName) {
        this.belongProvinceName = belongProvinceName;
    }

    @Column(name = "SPECIALTY_NAME")
    public String getSpecialtyName() {
        return specialtyName;
    }

    public void setSpecialtyName(String specialtyName) {
        this.specialtyName = specialtyName;
    }
}
