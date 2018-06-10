package com.metarnet.eomeem.utils;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author ChenBo
 */
public class JobFormRow {
    int id;
//    String rowBegin ="<row id=\"?\">";
//    String rowEnd ="</row>";
    String rowBegin ="<tr id=\"@\">";
    String rowEnd ="</tr>";
    LinkedList cellList = new LinkedList();
    
    /**
     * @param id
     */
    public JobFormRow(int id) {
        super();
        this.id = id;
    }
    
    public void appendFormCell(JobFormCell cell) {
        this.cellList.addLast(cell);
    }
    
    /**
     * @return Returns the rowBegin.
     */
    public String getRowBegin() {
        return rowBegin.replaceFirst("@", "" + this.id);
    }
    /**
     * @return Returns the rowEnd.
     */
    public String getRowEnd() {
        return rowEnd;
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer("");
        sb.append(this.getRowBegin());
        Iterator itr = this.cellList.iterator();
        while(itr.hasNext()) {
            JobFormCell cell = (JobFormCell) itr.next();
            sb.append(cell.toString());
        }
        sb.append(this.getRowEnd());
        return sb.toString();
    }
}
