package com.metarnet.core.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: metarnet
 * Date: 13-3-19
 * Time: 下午9:22
 * 页面组件模型
 */
public class ComponentModel {
    public final static String SHOW = "show";
    public final static String EDIT = "edit";

    private String component;
    private String type;
    private Boolean draft;
    //用于构建页面时传递必要的参数
//    private TaskInstance taskInstance = new TaskInstance();
    private String processInstID;
    //自定义参数，目前只有处理过程组件使用，因为处理过程是基于activeInsId查询数据。
    private String activityDefID;
    //组件扩展信息
    public List<ComponentExpand> expands = new ArrayList<ComponentExpand>();

    public ComponentModel() {
    }

    public ComponentModel(String component, String type, Boolean draft) {
        this.component = component;
        this.type = type;
        this.draft = draft;
    }

    public ComponentModel(String component, String type, String processInstID, String activityDefID) {
        this.component = component;
        this.type = type;
        this.processInstID = processInstID;
        this.activityDefID = activityDefID;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getDraft() {
        return draft;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    public String getProcessInstID() {
        return processInstID;
    }

    public void setProcessInstID(String processInstID) {
        this.processInstID = processInstID;
    }

    public List<ComponentExpand> getExpands() {
        return expands;
    }

    public void setExpands(List<ComponentExpand> expands) {
        this.expands = expands;
    }

    public String getActivityDefID() {
        return activityDefID;
    }

    public void setActivityDefID(String activityDefID) {
        this.activityDefID = activityDefID;
    }

    public boolean equalsComponent(Object o){
        ComponentModel that = (ComponentModel) o;
        return component.equals(that.component);
    }

    /**
     * equals 通过组件名称(component)及流程实例ID(taskInstance.getProcessInstID)判断
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o){
        	return true;
        }
        if (o == null || getClass() != o.getClass()){
        	return false;
        }

        ComponentModel that = (ComponentModel) o;

        if (!component.equals(that.component)){
        	return false;
        }
        if (!processInstID.equals(that.processInstID)){
        	return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = component.hashCode();
        if (null != processInstID) {
            result = 31 * result + processInstID.hashCode();
        }
        return result;
    }

}
