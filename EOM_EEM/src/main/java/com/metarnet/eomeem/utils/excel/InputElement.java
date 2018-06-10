package com.metarnet.eomeem.utils.excel;

import com.metarnet.eomeem.utils.InputElementDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.util.HtmlUtils;

import java.util.*;


public abstract class InputElement {
	protected final Log logger = LogFactory.getLog(getClass());
    
    InputElementDefinition elementDefinition;
    String inputType;
    Properties keyValueProps;
    List valueProps;
    
    public Enumeration getKeyValuePropKeys() {
        return keyValueProps.keys();
    }
    public Iterator getValuePropsIterator() {
        return valueProps.iterator();
    }
    /**
     * 
     */
    public InputElement() {
        super();
//        this.keyValueProps = new Properties();
        this.valueProps = new ArrayList(2);
//        this.keyValueProps =  new Properties();
    }
    /**
     * @param inputType
     */
    public InputElement(String inputType) {
        super();
        this.inputType = inputType;
    }
    
    public boolean hasFilter(){
    	if(keyValueProps.containsKey("filter")) {
    		String filterValue = keyValueProps.getProperty("filter");
    		if(filterValue != null && !filterValue.trim().equals(""))
    			return true;
    	}
    	return false;
    }
    
    public String getFilter(){
    	if(this.hasFilter()) {
    		if(!"".equals(keyValueProps.getProperty("filter")) && keyValueProps.getProperty("filter")!=null){
    			return keyValueProps.getProperty("filter").trim();
    		}
    		//return keyValueProps.getProperty("filter").trim();
    	}
    	return null;
    }

    /**
     * @return Returns the inputType.
     */
    public String getInputType() {
        return inputType;
    }
    /**
     * @param inputType The inputType to set.
     */
    public void setInputType(String inputType) {
        this.inputType = inputType;
    }
    
    public void setElementDefinition(InputElementDefinition elementDefinition) {
        this.elementDefinition = elementDefinition;
    }

    public void setKeyValueProps(Properties keyValueProps) {
        this.keyValueProps = keyValueProps;
    }
    
    public Properties getKeyValueProps() {
        return this.keyValueProps;
    }
    /**
     */
    public void addKeyValueProp(String key, String value) {
        if(key != null && this.elementDefinition.containsKeyValueProp(key)) {
           
            String nk = key.trim().toLowerCase();
            Object oldValue = this.keyValueProps.remove(nk);
          
            this.keyValueProps.setProperty(nk, value);
          
        }
        if(key!=null&&("id".equals(key.trim())||"name".equals(key.trim()))){
        	  this.keyValueProps.setProperty(key, value);
        }
        if(key!=null&&"value".equals(key.trim())){
      	  this.keyValueProps.setProperty(key, value);
      }
    }

    /**
     */
    public void addValueProp(String value) {
        if(this.elementDefinition.containsValueProp(value))
            this.valueProps.add(value.trim().toLowerCase());
    }

    public String toString(){
        StringBuffer sb = new StringBuffer(this.getElementBegin());

        Iterator itr = this.getValuePropsIterator();
        while(itr!=null && itr.hasNext()) { 
            sb.append(" ");
            sb.append(itr.next().toString());
        }
        Enumeration enu = this.getKeyValuePropKeys();

        while(enu!=null && enu.hasMoreElements()) {            
            sb.append(" ");
            String key = (String)enu.nextElement();
            sb.append(key);
            sb.append("=");
            String tempValue = this.keyValueProps.getProperty(key, null);
            if(key.equals("value"))
            	if(tempValue!=null && tempValue.indexOf("\"")>=0)
            		tempValue = HtmlUtils.htmlEscape(tempValue);
            	
            tempValue = "\"" + tempValue + "\"";
            sb.append(tempValue);
        }
        
        sb.append(this.getElementEnd());
        return sb.toString();
    }
    /**
     * @return
     */
    abstract public String getElementEnd();
    /**
     * @return
     */
    abstract String getElementBegin();
    
    
    public String toString(String name){
        this.keyValueProps.put("name", name);
        return this.toString();
    }
    
    public String toString(String name, String attachFileHref){
    	this.keyValueProps.put("name", name);
    	
        StringBuffer sb = new StringBuffer(this.getElementBegin());

        Iterator itr = this.getValuePropsIterator();
        while(itr!=null && itr.hasNext()) { 
            sb.append(" ");
            sb.append(itr.next().toString());
        }
        Enumeration enu = this.getKeyValuePropKeys();

        while(enu!=null && enu.hasMoreElements()) {            
            sb.append(" ");
            String key = (String)enu.nextElement();
            sb.append(key);
            sb.append("=");
            String tempValue = this.keyValueProps.getProperty(key, null);
            if(key.equals("value"))
            	if(tempValue!=null && tempValue.indexOf("\"")>=0)
            		tempValue = HtmlUtils.htmlEscape(tempValue);
            	
            tempValue = "\"" + tempValue + "\"";
            sb.append(tempValue);
        }
        
        sb.append(this.getElementEnd());
        
        sb.append(attachFileHref);
        return sb.toString();
    }
}
