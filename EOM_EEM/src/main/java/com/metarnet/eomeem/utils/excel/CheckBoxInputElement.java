package com.metarnet.eomeem.utils.excel;

/**
 * @author ChenBo
 */
public class CheckBoxInputElement extends InputElement {
    public static final String elementBegin = "<input type=\"checkbox\"";
    public static final String elementEnd = ">";

    public String getElementBegin() {
        return elementBegin;
    }
    public String getElementEnd() {
        return elementEnd;
    }

}
