package com.metarnet.eomeem.utils;


/**
 * @author ChenBo
 */
public class JobFormCell {
    public static final String VALIGN_TOP = "top"; 
    public static final String VALIGN_CENTER = "middle"; 
    public static final String VALIGN_BOTTOM = "bottom"; 
    public static final String ALIGN_LEFT = "left"; 
    public static final String ALIGN_CENTER = "center"; 
    public static final String ALIGN_RIGHT = "right"; 
    

    String cellBegin = "<td style=\"text-align: center;\" nowrap=\"nowrap\"   id=\"@\" colspan=\"@\" rowspan=\"@\" valign=\"@\" align=\"@\" width=\"@\" height=\"@\" bgcolor=\"@\" class=\"@\">";
    String cellEnd = "</td>";

    int id;
    int colspan=1;
    int rowspan=1;
    String valign = "center";
    String align = "left";
    int width=200;
    int height=80;
    String styleClass="excel_table_name";
    

    String beforeInputText;
    String input;
    String afterInputText;
    
  
    String font = null;
    String color = null;
    String bgcolor = "#FFFFFF";
    String size = null;
    boolean isBold = false;
    boolean isItalic = false;
    
    /**
     */
    public void setColor(int r, int g, int b) {
     //   this.color = FormatUtils.getColorFormat(r, g, b);
    }
    /**
     */
    public void setBackgroundColor(int r, int g, int b) {
    //    this.bgcolor = FormatUtils.getColorFormat(r, g, b);
    }
    
    /**
     * @param font The font to set.
     */
    public void setFont(String font) {
        this.font = font;
    }
    /**
     * @param isBold The isBold to set.
     */
    public void setBold(boolean isBold) {
        this.isBold = isBold;
    }
    /**
     * @param size The size to set.
     */
    public void setSize(int size) {
        this.size = "" + size + "pt";
    }
    /**
     * @param id
     */
    public JobFormCell(int id) {
        super();
        this.id = id;
    }
    /**
     * @param afterInputText The afterInputText to set.
     */
    public void setAfterInputText(String afterInputText) {
        this.afterInputText = afterInputText;
    }
    /**
     * @param beforeInputText The beforeInputText to set.
     */
    public void setBeforeInputText(String beforeInputText) {
        this.beforeInputText = beforeInputText;
    }
    
    
    /**
     * @return Returns the afterInputText.
     */
    public String getAfterInputText() {
        return getTextWithStyle(afterInputText);
    }
    /**
     * @return Returns the beforeInputText.
     */
    public String getBeforeInputText() {
        return getTextWithStyle(beforeInputText);
    }
    
    private String getTextWithStyle(String text) {
        if(text == null)
            return "";
        String ret = text;
        
        if(this.font != null || this.color != null || this.size != null){
	          String temp = "<SPAN style=\"";
	          if(font != null)
	              temp += "font-family:" + this.font + ";";
	          if(color != null)
	              temp += "color:" + this.color + ";";
	          if(size != null)
	              temp += "font-size=" + this.size;
	          ret = temp + "\">" + text + "</SPAN>";
            
        }
        
        if(isBold)
            return "<B>" + ret + "</B>";
        
        if(isItalic)
            return "<I>" + ret + "</I>";
        
        return ret;
            
    }
    /**
     * @param align The align to set.
     */
    public void setAlign(String align) {
        this.align = align;
    }
    /**
     * @param colspan The colspan to set.
     */
    public void setColspan(int colspan) {
        this.colspan = colspan;
    }
    /**
     * @param height The height to set.
     */
    public void setHeight(int height) {
        this.height = height;
    }
    /**
     * @param input The input to set.
     */
    public void setInput(String input) {
        this.input = input;
    }
    /**
     * @param rowspan The rowspan to set.
     */
    public void setRowspan(int rowspan) {
        this.rowspan = rowspan;
    }
    /**
     * @param valign The valign to set.
     */
    public void setValign(String valign) {
        this.valign = valign;
    }
    /**
     * @param width The width to set.
     */
    public void setWidth(int width) {
        this.width = width;
    }
    /**
     * @return
     */
    private String getCellEnd() {
        return this.cellEnd;
    }
    /**
     * @return
     */
    private String getCellBegin() {
        String begin = this.cellBegin.replaceFirst("@", "" + this.id);
        begin = begin.replaceFirst("@", "" + this.colspan);
        begin = begin.replaceFirst("@", "" + this.rowspan);
        begin = begin.replaceFirst("@", this.valign);
        begin = begin.replaceFirst("@", this.align);
        begin = begin.replaceFirst("@", "" + this.width);
        begin = begin.replaceFirst("@", "" + this.height);
        begin = begin.replaceFirst("@", "" + this.bgcolor);
        begin = begin.replaceFirst("@", "" + this.styleClass);
        return begin;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer("");
        sb.append(this.getCellBegin());
        sb.append(this.getBeforeInputText());
        sb.append(this.input==null?"":this.input);
        sb.append(this.getAfterInputText());
        sb.append(this.getCellEnd());
        return sb.toString();
    }
    /**
     * @return Returns the colspan.
     */
    public int getColspan() {
        return colspan;
    }
    /**
     * @return Returns the rowspan.
     */
    public int getRowspan() {
        return rowspan;
    }
    public void setItalic(boolean isItalic) {
        this.isItalic = isItalic;
    }
	public String getStyleClass() {
		return styleClass;
	}
	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}
}
