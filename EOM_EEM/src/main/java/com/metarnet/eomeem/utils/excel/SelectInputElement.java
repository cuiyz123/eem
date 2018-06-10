package com.metarnet.eomeem.utils.excel;

import org.apache.commons.lang.StringUtils;

import java.util.List;


//import com.wayout.webframe.entry.domain.EntryItem;

/**
 * @author ChenBo
 */
public class SelectInputElement extends InputElement{
    public static final String elementBegin = "<select ";
    public static final String elementEnd = ">";
    public static final String selectEnd = "</select>";
    public static final String optionPropName = "options";
    public static final String valueSpliter = ",";
    public static final String keyValueSpliter = "-";

    protected List options = null;
    
    public String getElementBegin() {
        return elementBegin;
    }
    public String getElementEnd() {
        return elementEnd;
    }
    public String getOptionValue() {
        return (String) this.keyValueProps.get(optionPropName);
    }
    public String toString() {
    	//���ø��෽�����õ�һ��ֻ�����Բ���(ѡ��ֵ�Ĳ��ֵ�SELECT�ؼ��ı�
        StringBuffer ret = new StringBuffer(super.toString());
        
        //��ȡ��
//        String selectedValues = this.keyValueProps.getProperty("selected");
        String selectedValues = this.keyValueProps.getProperty("value");
        String[] selectedValueList = selectedValues.split(valueSpliter);
        
        if(options != null && options.size()>0) {
            //���ͨ������ֵ��ȡֵ
            for(int i=0; i<options.size(); i++) {
            	SelectOptionItem item = (SelectOptionItem) options.get(i);
                ret.append("<option value='");
                ret.append(item.getKey());
                ret.append("' ");
                for(int j=0; j<selectedValueList.length; j++) {
                	if(StringUtils.equals(selectedValueList[j], item.getKey())) {
                		ret.append("selected");
                		break;
                	}
                }
                ret.append(" >");
                ret.append(item.getValue());
                ret.append("</option>");
            }
        } else {
            //���ͨ��ҳ�����û�ȡֵ
            String values = this.keyValueProps.getProperty("values");
            String[] valueList = values.split(valueSpliter);
            //����ÿһ��option
            for(int i=0; i<valueList.length; i++){
            	SelectOptionItem item = SelectOptionItem.createSelectOptionItem(valueList[i], keyValueSpliter);
            	item.checkIfSelected(selectedValueList);
                ret.append(item.toString());
            }
        }
        ret.append(selectEnd);
        return ret.toString();
    }
    public void setOptions(List options) {
        this.options = options;
    }
}
