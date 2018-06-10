package com.metarnet.core.common.model;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jietianwu
 * Date: 13-7-5
 * Time: 下午2:29
 * 组件的扩张信息,如转办、转派、会签
 */
public class ComponentExpand {
    public static final String COMINFOLIST = "ComInfoList";  //处理过程组件
    private String compName;
    private Map params;

    public ComponentExpand() {

    }

    public ComponentExpand(String compName, Map params) {
        this.compName = compName;
        this.params = params;
    }

    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }

    public Map getParams() {
        return params;
    }

    public void setParams(Map params) {
        this.params = params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
        	return true;
        }
        if (o == null || getClass() != o.getClass()){
        	return false;
        }

        ComponentExpand that = (ComponentExpand) o;

        if (!compName.equals(that.compName)){
        	return false;
        }
        if (!params.equals(that.params)){
        	return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = compName.hashCode();
        result = 31 * result + params.hashCode();
        return result;
    }
}
