package com.metarnet.eomeem.model;

import javax.persistence.*;
import java.util.Date;

//上报表
@Entity
@Table(name = "t_eem_excel_file_data")
public class EvaluationFileData {
    private long fileID;
    private String fileName;
    private Date uploadDate = new Date();
    private String filePath;
    private String reportOrgCode;
    private String reportPersonName;
    private Long reportPersonID;
    private String reportPersonTel;

    @Id
    @Column(name = "FILE_DATA_ID", unique = true, nullable = false)
    public long getFileID() {
        return fileID;
    }

    public void setFileID(long fileID) {
        this.fileID = fileID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getReportOrgCode() {
        return reportOrgCode;
    }

    public void setReportOrgCode(String reportOrgCode) {
        this.reportOrgCode = reportOrgCode;
    }

    public String getReportPersonName() {
        return reportPersonName;
    }

    public void setReportPersonName(String reportPersonName) {
        this.reportPersonName = reportPersonName;
    }

    public Long getReportPersonID() {
        return reportPersonID;
    }

    public void setReportPersonID(Long reportPersonID) {
        this.reportPersonID = reportPersonID;
    }

    public String getReportPersonTel() {
        return reportPersonTel;
    }

    public void setReportPersonTel(String reportPersonTel) {
        this.reportPersonTel = reportPersonTel;
    }
}