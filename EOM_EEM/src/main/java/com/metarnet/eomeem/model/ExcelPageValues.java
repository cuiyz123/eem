package com.metarnet.eomeem.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


@Entity
@Table(name = "t_eem_excel_page_values")
public class ExcelPageValues implements Serializable {


    private Long objectId;/*业务主键*/

    private long tpID;//对应的模板id

    private long pageID;//上报的Excel 的id

    private long rowIndex;//横轴

    private long colIndex;//纵轴

    private String dataType;//数据类型


    private String txtValue;//对应位置的值

    @Id
    @Column(name = "OBJECT_ID", nullable = false, updatable = false)
    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    @Column(name = "template_id")
    public long getTpID() {
        return tpID;
    }

    public void setTpID(long tpID) {
        this.tpID = tpID;
    }

    @Column(name = "pageid")
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

    public String getTxtValue() {
        return txtValue;
    }

    public void setTxtValue(String txtValue) {
        this.txtValue = txtValue;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }


}
