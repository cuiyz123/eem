package com.metarnet.eomeem.model;


import javax.persistence.*;
import java.sql.Timestamp;

/**
 * 1.未审核
 * 2.已审核
 * 3.审核驳回
 */

@Entity
@Table(name = "t_eem_excel_page")
@AttributeOverrides({
        @AttributeOverride(name = "objectId", column = @Column(name = "pageid", nullable = false))})
public class ExcelPage extends BaseEntity {

    private String tpInputName;
    private String fileName;
    private Long tpInputID;//模板id
    private String pageName;//上报的页面的名称
    private String reportUserName;//上报人的用户名
    private String reportOrgCode;//上报的部门
    private String reportDate;//上报的时间   年度 、半年、季度（第几季度）
    private String reportYear;//上报年份
    private String reportPath;//上报的附件存放地址
    private String pageType;//加一个pageType  必要时用于区分自维和代维
    private String reportDataTime;
    private String dateGrading;
    private Integer rejectNum;//驳回次数
    private String reportType;//上报类型 report表示直接上报  collect表示汇总上报
    private String filePath;
    private String attachmentId;

    private String   iswithdraw;//退回后重新上报
    private Timestamp withdrawtime;
    private int willCollect;//0,1是汇总

    private long disID;//标识有没有做申请退回操作

    private Long fileDataId;//
    //新增
    private Boolean auditSameLevel;//本级审核 1已审核true 0未审核false
    private Boolean auperiorAudit;//上级审核  1已审核 0未审核

    @Column(name = "audit_Same_Level")
    public Boolean getAuditSameLevel() {
        return auditSameLevel;
    }

    public void setAuditSameLevel(Boolean auditSameLevel) {
        this.auditSameLevel = auditSameLevel;
    }

    @Column(name = "auperior_Audit")
    public Boolean getAuperiorAudit() {
        return auperiorAudit;
    }

    public void setAuperiorAudit(Boolean auperiorAudit) {
        this.auperiorAudit = auperiorAudit;
    }

    @Column(name = "EVALUATE_FILEDATA_ID", nullable = true)
    public Long getFileDataId() {
        return fileDataId;
    }

    public void setFileDataId(Long fileDataId) {
        this.fileDataId = fileDataId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public String getReportYear() {
        return reportYear;
    }

    public void setReportYear(String reportYear) {
        this.reportYear = reportYear;
    }

    public String getReportPath() {
        return reportPath;
    }

    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

    public long getTpInputID() {
        return tpInputID;
    }

    public void setTpInputID(long tpInputID) {
        this.tpInputID = tpInputID;
    }

    public String getReportUserName() {
        return reportUserName;
    }

    public void setReportUserName(String reportUserName) {
        this.reportUserName = reportUserName;
    }

    public String getReportOrgCode() {
        return reportOrgCode;
    }

    public void setReportOrgCode(String reportOrgCode) {
        this.reportOrgCode = reportOrgCode;
    }

    public String getPageType() {
        return pageType;
    }

    public void setPageType(String pageType) {
        this.pageType = pageType;
    }

    public long getDisID() {
        return disID;
    }

    public void setDisID(long disID) {
        this.disID = disID;
    }


    public String getReportDataTime() {
        return reportDataTime;
    }

    public void setReportDataTime(String reportDataTime) {
        this.reportDataTime = reportDataTime;
    }

    public String getTpInputName() {
        return tpInputName;
    }

    public void setTpInputName(String tpInputName) {
        this.tpInputName = tpInputName;
    }

    public String getIswithdraw() {
        return iswithdraw;
    }

    public void setIswithdraw(String iswithdraw) {
        this.iswithdraw = iswithdraw;
    }

    public Timestamp getWithdrawtime() {
        return withdrawtime;
    }

    public void setWithdrawtime(Timestamp withdrawtime) {
        this.withdrawtime = withdrawtime;
    }

    @Column(name = "willCollect", nullable = false ,columnDefinition = "INT default 0", updatable = false)
    public int getWillCollect() {
        return willCollect;
    }

    public void setWillCollect(int willCollect) {
        this.willCollect = willCollect;
    }

    public String getDateGrading() {
        return dateGrading;
    }

    public void setDateGrading(String dateGrading) {
        this.dateGrading = dateGrading;
    }

    @Column(columnDefinition = "INT default 0")
    public Integer getRejectNum() {
        return rejectNum;
    }

    public void setRejectNum(Integer rejectNum) {
        this.rejectNum = rejectNum;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }
}
