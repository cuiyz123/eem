package com.metarnet.eomeem.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


@Entity
@Table(name = "t_eem_deduct_page_values")
public class DedcutPageValues implements Serializable {


    private Long objectId;/*业务主键*/

    private long deductTpID;//对应的扣分模板

    private long reportTpID;//对应的汇总模板id

    private long pageID;//上报的Excel 的id

    private long rowIndex;//横轴

    private long colIndex;//纵轴

    private String dataType;//数据类型

    private String venderName;

    private String txtValue;//对应位置的值

    @Id
    @Column(name = "OBJECT_ID", nullable = false, updatable = false)
    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public long getDeductTpID() {
        return deductTpID;
    }

    public void setDeductTpID(long deductTpID) {
        this.deductTpID = deductTpID;
    }

    public long getReportTpID() {
        return reportTpID;
    }

    public void setReportTpID(long reportTpID) {
        this.reportTpID = reportTpID;
    }

    public long getPageID() {
        return pageID;
    }

    public void setPageID(long pageID) {
        this.pageID = pageID;
    }

    public long getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(long rowIndex) {
        this.rowIndex = rowIndex;
    }

    public long getColIndex() {
        return colIndex;
    }

    public void setColIndex(long colIndex) {
        this.colIndex = colIndex;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getVenderName() {
        return venderName;
    }

    public void setVenderName(String venderName) {
        this.venderName = venderName;
    }

    public String getTxtValue() {
        return txtValue;
    }

    public void setTxtValue(String txtValue) {
        this.txtValue = txtValue;
    }
}
