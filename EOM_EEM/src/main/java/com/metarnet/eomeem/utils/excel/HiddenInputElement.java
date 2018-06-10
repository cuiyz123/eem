package com.metarnet.eomeem.utils.excel;
public class HiddenInputElement extends InputElement {
    public static final String elementBegin = "<input type=\"hidden\"";
    public static final String elementEnd = ">";

    public String getElementBegin() {
        return elementBegin;
    }
    public String getElementEnd() {
        return elementEnd;
    }

}
