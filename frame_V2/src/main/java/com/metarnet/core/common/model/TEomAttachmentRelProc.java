package com.metarnet.core.common.model;

import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * Company: Metarnet
 * User: kans
 * Date: 13-4-23
 * Time: 上午10:03
 * Description: 流程类-附件关联表.
 */
@Entity
@Table(name = "t_eom_attachment_rel_proc")
@Where(clause = "DELETED_FLAG=0")
@AttributeOverrides({
        @AttributeOverride(name = "objectId", column = @Column(name = "ATT_REL_PROC_ID", nullable = false))
})
public class TEomAttachmentRelProc extends BaseForm {

    private String flowingObjectTable;/*流转对象表名-对应申请单、调度单、反馈信息、电路或者产品等的表名或者实体名*/
    private Long flowingObjectId;/*流转对象ID-对应申请单ID、调度单ID、反馈信息ID、电路ID或者产品ID*/
    private String activityInstanceId;/*活动(环节)实例ID*/
    private String taskInstanceId;/*任务实例ID*/
    private String attachmentId;/*附件ID*/
    private String attachmentName;/*附件名称*/
    private Integer attachmentTypeEnumId;/*附件类型*/
    private String attachmentPurpose;/*附件用途*/
    private Integer attachmentFormatEnumId;/*附件格式*/
    private Integer uploadedByPersonId;/*上传人ID*/
    private String uploadedByPersonName;/*上传人名称*/
    private Integer uploadedByOrgId;/*上传人所在组织ID*/
    private String uploadedByOrgName;/*上传人所在组织名称*/
    private String remarks;/*备注*/
//    private Integer shardingId;/*附件关联表分片ID*/
    private Integer flowingObjectShardingId;/*流转对象分片ID*/


    public TEomAttachmentRelProc() {
    }

    @Column(name = "FLOWING_OBJECT_TABLE", nullable = false, length = 30)
    public String getFlowingObjectTable() {
        return this.flowingObjectTable;
    }

    public void setFlowingObjectTable(String flowingObjectTable) {
        this.flowingObjectTable = flowingObjectTable;
    }

    @Column(name = "FLOWING_OBJECT_ID", nullable = false)
    public Long getFlowingObjectId() {
        return this.flowingObjectId;
    }

    public void setFlowingObjectId(Long flowingObjectId) {
        this.flowingObjectId = flowingObjectId;
    }

    @Column(name = "ACTIVITY_INSTANCE_ID", nullable = false, length = 100)
    public String getActivityInstanceId() {
        return this.activityInstanceId;
    }

    public void setActivityInstanceId(String activityInstanceId) {
        this.activityInstanceId = activityInstanceId;
    }

    @Column(name = "TASK_INSTANCE_ID", nullable = false, length = 100)
    public String getTaskInstanceId() {
        return this.taskInstanceId;
    }

    public void setTaskInstanceId(String taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    @Column(name = "ATTACHMENT_ID", nullable = false)
    public String getAttachmentId() {
        return this.attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    @Column(name = "ATTACHMENT_NAME", nullable = false, length = 200)
    public String getAttachmentName() {
        return this.attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    @Column(name = "ATTACHMENT_TYPE_ENUM_ID", nullable = false)
    public Integer getAttachmentTypeEnumId() {
        return this.attachmentTypeEnumId;
    }

    public void setAttachmentTypeEnumId(Integer attachmentTypeEnumId) {
        this.attachmentTypeEnumId = attachmentTypeEnumId;
    }

    @Column(name = "ATTACHMENT_PURPOSE", length = 240)
    public String getAttachmentPurpose() {
        return this.attachmentPurpose;
    }

    public void setAttachmentPurpose(String attachmentPurpose) {
        this.attachmentPurpose = attachmentPurpose;
    }

    @Column(name = "ATTACHMENT_FORMAT_ENUM_ID", nullable = false)
    public Integer getAttachmentFormatEnumId() {
        return this.attachmentFormatEnumId;
    }

    public void setAttachmentFormatEnumId(Integer attachmentFormatEnumId) {
        this.attachmentFormatEnumId = attachmentFormatEnumId;
    }

    @Column(name = "UPLOADED_BY_PERSON_ID", nullable = false)
    public Integer getUploadedByPersonId() {
        return this.uploadedByPersonId;
    }

    public void setUploadedByPersonId(Integer uploadedByPersonId) {
        this.uploadedByPersonId = uploadedByPersonId;
    }

    @Column(name = "UPLOADED_BY_PERSON_NAME", nullable = false, length = 120)
    public String getUploadedByPersonName() {
        return this.uploadedByPersonName;
    }

    public void setUploadedByPersonName(String uploadedByPersonName) {
        this.uploadedByPersonName = uploadedByPersonName;
    }

    @Column(name = "UPLOADED_BY_ORG_ID", nullable = false)
    public Integer getUploadedByOrgId() {
        return this.uploadedByOrgId;
    }

    public void setUploadedByOrgId(Integer uploadedByOrgId) {
        this.uploadedByOrgId = uploadedByOrgId;
    }

    @Column(name = "UPLOADED_BY_ORG_NAME", nullable = false, length = 120)
    public String getUploadedByOrgName() {
        return this.uploadedByOrgName;
    }

    public void setUploadedByOrgName(String uploadedByOrgName) {
        this.uploadedByOrgName = uploadedByOrgName;
    }

    @Column(name = "REMARKS", length = 500)
    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

//    @Transient
//    public Integer getShardingId() {
//        return this.shardingId;
//    }
//
//    public void setShardingId(Integer shardingId) {
//        this.shardingId = shardingId;
//    }

    @Transient
    public Integer getFlowingObjectShardingId() {
        return this.flowingObjectShardingId;
    }

    public void setFlowingObjectShardingId(Integer flowingObjectShardingId) {
        this.flowingObjectShardingId = flowingObjectShardingId;
    }



    @Override
    public String toString() {
        return "TEomAttachmentRelProc{" +
                "attRelProcId=" + super.getObjectId() +
                ", flowingObjectTable='" + flowingObjectTable + '\'' +
                ", flowingObjectId=" + flowingObjectId +
                ", activityInstanceId='" + activityInstanceId + '\'' +
                ", taskInstanceId='" + taskInstanceId + '\'' +
                ", attachmentId='" + attachmentId + '\'' +
                ", attachmentName='" + attachmentName + '\'' +
                ", attachmentTypeEnumId=" + attachmentTypeEnumId +
                ", attachmentPurpose='" + attachmentPurpose + '\'' +
                ", attachmentFormatEnumId=" + attachmentFormatEnumId +
                ", uploadedByPersonId=" + uploadedByPersonId +
                ", uploadedByPersonName='" + uploadedByPersonName + '\'' +
                ", uploadedByOrgId=" + uploadedByOrgId +
                ", uploadedByOrgName='" + uploadedByOrgName + '\'' +
                ", remarks='" + remarks + '\'' +
                ", shardingId=" + getShardingId() +
                ", flowingObjectShardingId=" + flowingObjectShardingId +
                '}';
    }
}