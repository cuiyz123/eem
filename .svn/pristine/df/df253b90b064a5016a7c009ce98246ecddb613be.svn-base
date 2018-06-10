package com.metarnet.eomeem.model;


import javax.persistence.*;
import java.sql.Timestamp;

/**
 * 1.未审核
 * 2.已审核
 * 3.审核驳回
 */

@Entity
@Table(name = "t_eem_report_apply")
@AttributeOverrides({
        @AttributeOverride(name = "objectId", column = @Column(name = "report_apply_id", nullable = false))})
public class EemApply extends BaseEntity {


    private String tpInputName;

    private Long tpInputID;//模板id
    private String pageName;//上报的页面的名称
    private String reportUserName;//上报人的用户名
    private String reportUserTrueName;//上报人的用户名
    private String reportOrgCode;//上报的部门
    private String reportOrgName;//上报的部门
    private String reportDate;//上报的时间   年度 、半年、季度（第几季度）
    private String reportYear;//上报年份
    private String reason;
    private Long pageId;


    private String auditUserName;//上报人的用户名
    private String auditUserTrueName;//上报人的用户名
    private String auditOrgCode;//上报的部门
    private String auditOrgName;//上报的部门
    private String auditReason;
    private String result;
    private Boolean auditResult;
    private Timestamp autidTime;



    public String getTpInputName() {
        return tpInputName;
    }

    public void setTpInputName(String tpInputName) {
        this.tpInputName = tpInputName;
    }

    public Long getTpInputID() {
        return tpInputID;
    }

    public void setTpInputID(Long tpInputID) {
        this.tpInputID = tpInputID;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    public Timestamp getAutidTime() {
        return autidTime;
    }

    public void setAutidTime(Timestamp autidTime) {
        this.autidTime = autidTime;
    }


    public String getReportOrgName() {
        return reportOrgName;
    }

    public void setReportOrgName(String reportOrgName) {
        this.reportOrgName = reportOrgName;
    }

    public String getReportUserTrueName() {
        return reportUserTrueName;
    }

    public void setReportUserTrueName(String reportUserTrueName) {
        this.reportUserTrueName = reportUserTrueName;
    }

    public String getAuditUserName() {
        return auditUserName;
    }

    public void setAuditUserName(String auditUserName) {
        this.auditUserName = auditUserName;
    }

    public String getAuditUserTrueName() {
        return auditUserTrueName;
    }

    public void setAuditUserTrueName(String auditUserTrueName) {
        this.auditUserTrueName = auditUserTrueName;
    }

    public String getAuditOrgCode() {
        return auditOrgCode;
    }

    public void setAuditOrgCode(String auditOrgCode) {
        this.auditOrgCode = auditOrgCode;
    }

    public String getAuditOrgName() {
        return auditOrgName;
    }

    public void setAuditOrgName(String auditOrgName) {
        this.auditOrgName = auditOrgName;
    }

    public String getAuditReason() {
        return auditReason;
    }

    public void setAuditReason(String auditReason) {
        this.auditReason = auditReason;
    }


    public Boolean getAuditResult() {
        return auditResult;
    }

    public void setAuditResult(Boolean auditResult) {
        this.auditResult = auditResult;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
