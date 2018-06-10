package com.metarnet.eomeem.utils;


import org.dom4j.Document;
import org.dom4j.Element;

import java.util.*;

public class JobConfiguration extends Configuration {
	private Properties inputs;
    private String inputPromptBegin;
    private String inputPromptEnd;
    private Map<String,Map<String, String>> promptMap= new HashMap<String, Map<String,String>>();// key 属性类型 里面的map代表具体属性

  
    public Configuration doConfigure(Document doc) {
    	Element infoNode = doc.getRootElement().element("info");
        Element jobDefNode = doc.getRootElement().element("job-definition");
        this.inputPromptBegin = jobDefNode.element("input-prompt").attributeValue("begin");
        this.inputPromptEnd = jobDefNode.element("input-prompt").attributeValue("end");
     
        Iterator inputTypes = jobDefNode.element("input-types").elementIterator("input");
        this.inputs = new Properties();
        while (inputs != null && inputTypes.hasNext()) {
            Element input = (Element)inputTypes.next();
          
            String type = input.attributeValue("type");
            InputElementDefinition inputDef = new InputElementDefinition();
            inputDef.setInputType(type);
          
            
            String className = input.attributeValue("class");
            inputDef.setClassName(className);
          

          
            Iterator propItr = input.element("props").elementIterator("prop");
            while(propItr != null && propItr.hasNext()) {
                Element prop = (Element)propItr.next();
                String key = prop.attributeValue("key");
                if(key != null){
                   
                    inputDef.addKeyValueProp(key, prop.getTextTrim());
                } else {
                  
                    inputDef.addValueProp(prop.getTextTrim());
                }
            }
            this.inputs.put(type.trim().toLowerCase(), inputDef);
        }
       
        
        Iterator propsIt = jobDefNode.element("props").elementIterator("prop");
        while (propsIt.hasNext()) {
        	 Element el = (Element)propsIt.next();
        	 String name = el.attributeValue("name");
        	 Iterator propItr = el.elementIterator("option");
        	 Map<String,String> tmpMap = new TreeMap<String, String>();
        	 while(propItr != null && propItr.hasNext()) {
        		 Element option = (Element) propItr.next();
        		 String key = option.attributeValue("key");
        		 String value = option.getTextTrim();
        		 tmpMap.put(key, value);
        	 }
        	 promptMap.put(name, tmpMap);
        }
        
        
        return this;
    }
    
    public Map<String,String>  getProps(String name){
    	return promptMap.get(name);
    }
    
    public InputElementDefinition getInputElementDef(String type) {
        return (InputElementDefinition) this.inputs.get(type.trim().toLowerCase());
    }

    /* (non-Javadoc)
     * @see com.wayout.webframe.config.Configuration#supports()
     */
    public boolean supports() {
        return true;
    }

//    /**
//     * @return Returns the inputPrompt.
//     */
//    public String getInputPrompt() {
//        return inputPrompt;
//    }
	/**
	 * @return Returns the inputPromptBegin.
	 */
	public String getInputPromptBegin() {
	    return inputPromptBegin;
	}
    /**
     * @return Returns the inputPromptEnd.
     */
    public String getInputPromptEnd() {
        return inputPromptEnd;
    }
}
