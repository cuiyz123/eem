package com.metarnet.eomeem.model;

import javax.persistence.*;


@Entity
@Table(name = "evalution_report_excel")
public class EvaluationReportExcel extends BaseEntity {
    private long formId;                    //formid
    private String dep;                        //省份
    private String excelName;                //Excel名
    private String excelPath;                //excel路径
    private String tpInputName;
    private String fileName;                //由数字组成的名字，防止在下载是出现编码问题
    private String reportYear;              //汇总年份
    private String reportDate;              //季度或半年
    private String deptsWithDraw;            //退回的所有地市或省份
    private String drawMessage;
    private String dateGrading;
    private String isUpdate;
    private String attachmentId;


    public String getIsUpdate() {
        return isUpdate;
    }

    public void setIsUpdate(String isUpdate) {
        this.isUpdate = isUpdate;
    }

    public String getDeptsWithDraw() {
        return deptsWithDraw;
    }

    public void setDeptsWithDraw(String deptsWithDraw) {
        this.deptsWithDraw = deptsWithDraw;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public String getDep() {
        return dep;
    }

    public void setDep(String dep) {
        this.dep = dep;
    }


    public String getExcelName() {
        return excelName;
    }

    public void setExcelName(String excelName) {
        this.excelName = excelName;
    }

    public String getExcelPath() {
        return excelPath;
    }

    public void setExcelPath(String excelPath) {
        this.excelPath = excelPath;
    }

    public long getFormId() {
        return formId;
    }

    public void setFormId(long formId) {
        this.formId = formId;
    }

    public String getDrawMessage() {
        return drawMessage;
    }

    public void setDrawMessage(String drawMessage) {
        this.drawMessage = drawMessage;
    }

    public String getTpInputName() {
        return tpInputName;
    }

    public void setTpInputName(String tpInputName) {
        this.tpInputName = tpInputName;
    }

    @Transient
    public String getDateGrading() {
        return dateGrading;
    }

    public void setDateGrading(String dateGrading) {
        this.dateGrading = dateGrading;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }
}
