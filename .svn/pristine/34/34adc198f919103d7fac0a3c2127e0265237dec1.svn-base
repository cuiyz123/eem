package com.metarnet.eomeem.utils;

import com.metarnet.eomeem.utils.excel.InputElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 * @author ChenBo
 */
public class InputElementDefinition {
	protected final Log logger = LogFactory.getLog(getClass());

	String inputType;
    String className;
    Properties keyValueProps;
    List valueProps;
    
    /**
     * 
     */
    public InputElementDefinition() {
        super();
        this.keyValueProps = new Properties();
        this.valueProps = new ArrayList(2);
    }
    /**
     * @param inputType
     * @param keyValueProps
     * @param valueProps
     */
    public InputElementDefinition(String inputType) {
        super();
        this.inputType = inputType;
    }
//    /**
//     * @param inputType
//     * @param keyValueProps
//     * @param valueProps
//     */
//    public InputElementDefinition(String inputType, Properties keyValueProps,
//            List valueProps) {
//        super();
//        this.inputType = inputType;
//        this.keyValueProps = keyValueProps;
//        this.valueProps = valueProps;
//    }
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

    /**
     * @return Returns the keyValueProps.
     */
    public Properties getKeyValueProps() {
        return keyValueProps;
    }
//    /**
//     * @param keyValueProps The keyValueProps to set.
//     */
//    public void setKeyValueProps(Properties keyValueProps) {
//        this.keyValueProps = keyValueProps;
//    }
    /**
     * @param keyValueProps The keyValueProps to set.
     */
    public void addKeyValueProp(String key, String value) {
        if(key != null)
            this.keyValueProps.put(key.trim().toLowerCase(), value);
    }
    /**
     * @param valueProps The valueProps to set.
     */
    public boolean containsKeyValueProp(String key) {
        if(key == null)
            return false;
        return (this.keyValueProps.containsKey(key.trim().toLowerCase()));
    }

    /**
     * @return Returns the valueProps.
     */
    public List getValueProps() {
        return valueProps;
    }
//    /**
//     * @param valueProps The valueProps to set.
//     */
//    public void setValueProps(List valueProps) {
//        this.valueProps = valueProps;
//    }
    /**
     * @param valueProps The valueProps to set.
     */
    public void addValueProp(String value) {
        this.valueProps.add(value.trim().toLowerCase());
    }
    /**
     * @param valueProps The valueProps to set.
     */
    public boolean containsValueProp(String value) {
        if(value == null)
            return false;
        return (this.valueProps.contains(value.trim().toLowerCase()));
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public InputElement newInputElement() {
        if(this.className == null)
            return null;
        try {
            Object obj = Class.forName(this.className).newInstance();
            if(obj instanceof InputElement) {
                InputElement ie = (InputElement)obj;
                ie.setInputType(this.getInputType());
                ie.setElementDefinition(this);
                Properties np = new Properties();
                np.putAll((Hashtable) this.keyValueProps.clone());
                ie.setKeyValueProps(np);
                return ie;
            } else {
                this.logger.error(this.className + "����com.wayout.oms.job.domain.form.element.InputElement��");
                this.logger.error("���η���null");
                return null;
            }
        } catch (InstantiationException e) {
            this.logger.error("ʵ��" + this.className + "��?" + e.getMessage());
            this.logger.error("���������ļ������η���null");
            return null;
        } catch (IllegalAccessException e) {
            this.logger.error("����" + this.className + "ʵ��ʱ��?" + e.getMessage());
            this.logger.error("��ȷ�ϸ����Ƿ��й������޲����췽�������η���null");
            return null;
        } catch (ClassNotFoundException e) {
            this.logger.error("����" + this.className + "ʵ��ʱ��?" + e.getMessage());
            this.logger.error("�޷��ҵ����࣡���η���null");
            return null;
        }
        
    }
}
