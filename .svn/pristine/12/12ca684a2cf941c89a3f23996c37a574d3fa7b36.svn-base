package com.metarnet.eomeem.utils.excel;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * @author ChenBo
 */
public class SelectOptionItem implements Serializable {
	String key;
	String value;
	boolean selected = false;
	/**
	 * @return Returns the selected.
	 */
	public boolean isSelected() {
		return selected;
	}
	/**
	 * @param selected The selected to set.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	/**
	 * 
	 */
	public SelectOptionItem(String key) {
		this.key = key;
		this.value = key;
	}
	
	/**
	 * 
	 */
	public SelectOptionItem(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	/**
	 * 
	 * @param keyAndValue
	 * @param spliter �ָ��
	 * @return
	 */
	public static SelectOptionItem createSelectOptionItem(String keyAndValue, String spliter) {
		if(keyAndValue == null)
			return null;
		String[] temp = keyAndValue.split(spliter, 2);
		SelectOptionItem item = null;
		if(temp.length>1) 
			item = new SelectOptionItem(temp[0], temp[1]);
		else
			item = new SelectOptionItem(temp[0]);
		return item;
	}
	
	public String toString(){
		StringBuffer ret = new StringBuffer("<option value='");  
        ret.append(this.getKey());
        ret.append("' ");
    	if(this.selected) {
    		ret.append("selected");
    	}
        ret.append(" >");
        ret.append(this.getValue());
        ret.append("</option>");
		return ret.toString();
	}
	
	public void checkIfSelected(String[] selectedValueList) {
		if(selectedValueList != null) {
            for(int j=0; j<selectedValueList.length; j++) {
            	if(StringUtils.equals(selectedValueList[j], this.getKey())) {
            		this.setSelected(true);
            	}
            }
		}
	}
	/**
	 * @return Returns the key.
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @param key The key to set.
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value The value to set.
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
