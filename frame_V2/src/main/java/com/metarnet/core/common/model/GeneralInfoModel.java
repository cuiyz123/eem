package com.metarnet.core.common.model;

import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * Created by Administrator on 2016/4/14.
 */
@Entity
@Table(name = "t_eom_general_info")
@Where(clause = "DELETED_FLAG=0")
@AttributeOverrides({
        @AttributeOverride(name = "objectId", column = @Column(name = "GENERAL_ID", nullable = false))})
public class GeneralInfoModel extends BaseForm {
    private Long processingObjectID;
    private String processingObjectTable;
    private String processingStatus;
    private String report;

    @Column(name = "PROCESSING_OBJECT_ID")
    public Long getProcessingObjectID() {
        return processingObjectID;
    }

    public void setProcessingObjectID(Long processingObjectID) {
        this.processingObjectID = processingObjectID;
    }

    @Column(name = "PROCESSING_OBJECT_TABLE")
    public String getProcessingObjectTable() {
        return processingObjectTable;
    }

    public void setProcessingObjectTable(String processingObjectTable) {
        this.processingObjectTable = processingObjectTable;
    }

    @Column(name = "PROCESSING_STATUS")
    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    @Column(name = "REPORT")
    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }
}
