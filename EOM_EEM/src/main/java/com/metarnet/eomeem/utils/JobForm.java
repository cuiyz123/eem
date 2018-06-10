package com.metarnet.eomeem.utils;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author ChenBo
 */
public class JobForm {
	//String formBegin ="<?xml version=\"1.0\" encoding=\"gb2312\"?><table verson=\"1.0\" cellPadding=\"3\" cellSpacing=\"1\"  width=\"95%\" align=\"center\" class=\"form_position2\">";
	String formBegin ="<?xml version=\"1.0\" encoding=\"GB2312\"?><table verson=\"1.0\"  cellPadding=\"3\" cellSpacing=\"1\"  id=\"datatable\"  width=\"95%\" align=\"center\" class=\"form_position2\">";
      //  jw  4.10添加 id="datatable"
	String formEnd ="</table>";
    LinkedList rowList = new LinkedList();

    public void createAndAppendFormRow(int rowId) {
        this.rowList.addLast(new JobFormRow(rowId));
    }
    
    public void appendFormCell(int rowId, JobFormCell cell){
        ((JobFormRow) this.rowList.get(rowId)).appendFormCell(cell);
    }
    
    /**
     * @return Returns the formBegin.
     */
    public String getFormBegin() {
        return formBegin;
    }
    /**
     * @return Returns the formEnd.
     */
    public String getFormEnd() {
        return formEnd;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer("");
        sb.append(this.getFormBegin());
        Iterator itr = this.rowList.iterator();
        while(itr.hasNext()) {
            JobFormRow row = (JobFormRow) itr.next();
            sb.append(row.toString());
        }
        sb.append(this.getFormEnd());
        return sb.toString();
    }
}