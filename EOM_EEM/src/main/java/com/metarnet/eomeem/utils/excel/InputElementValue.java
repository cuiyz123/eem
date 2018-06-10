package com.metarnet.eomeem.utils.excel;

import java.io.Serializable;

/**
 * @author ChenBo
 */
public class InputElementValue implements Serializable {

     int rowId;
     int columnId;
     String value;
    public int getColumnId() {
        return columnId;
    }
    public void setColumnId(int columnId) {
        this.columnId = columnId;
    }
    public int getRowId() {
        return rowId;
    }
    public void setRowId(int rowId) {
        this.rowId = rowId;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
