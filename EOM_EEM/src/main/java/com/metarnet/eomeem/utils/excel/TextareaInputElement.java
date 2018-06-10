package com.metarnet.eomeem.utils.excel;

public class TextareaInputElement extends InputElement {
    public static final String elementBegin = "<textarea ";
    public static final String elementEnd = "></textarea>";
    
    public String elementEndWithValue= elementEnd; 

    public String getElementBegin() {
        return elementBegin;
    }
    public String getElementEnd() {
        return elementEndWithValue;
    }

    public String toString() {
        if(this.keyValueProps.containsKey("value")) {
            String val = this.keyValueProps.getProperty("value");
            if(val == null)
                val = "";
            /*else {
                String[] temp = val.split("\"");
                if(temp.length>1)
                    val = temp[1].split("\"")[0];

            }*/
            this.elementEndWithValue = ">" + val + "</textarea>";
        }
        return super.toString();
    }
}
