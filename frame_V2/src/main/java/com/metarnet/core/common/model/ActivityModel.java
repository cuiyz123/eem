package com.metarnet.core.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: metarnet
 * Date: 13-2-20
 * Time: 下午3:06
 * To change this template use File | Settings | File Templates.
 */
public class ActivityModel {
    private String id;
    private List<String> showLinkList = new ArrayList();
    private List<String> editLinkList = new ArrayList();
    private ComponentModel component;
    private String areaName;
    private String feedbackAbstractRoleId;
    private List<ProcessorModel> postProcessorList = new ArrayList();
    private List<ProcessorModel> preProcessorList = new ArrayList();
    private String showSubflow;
    private String needApproval;
    private NextApproverModel nextApproverModel;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ComponentModel getComponent() {
        return component;
    }

    public void setComponent(ComponentModel component) {
        this.component = component;
    }

    public List<ProcessorModel> getPostProcessorList() {
        return postProcessorList;
    }

    public void setPostProcessorList(List<ProcessorModel> postProcessorList) {
        this.postProcessorList = postProcessorList;
    }

    public NextApproverModel getNextApproverModel() {
        return nextApproverModel;
    }

    public void setNextApproverModel(NextApproverModel nextApproverModel) {
        this.nextApproverModel = nextApproverModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
        	return true;
        }
        if (o == null || getClass() != o.getClass()){
        	return false;
        }

        ActivityModel activityModel = (ActivityModel) o;

        if (!component.equals(activityModel.component)) {
        	return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return component.hashCode();
    }

    public List<ProcessorModel> getPreProcessorList() {
        return preProcessorList;
    }

    public void setPreProcessorList(List<ProcessorModel> preProcessorList) {
        this.preProcessorList = preProcessorList;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public List<String> getShowLinkList() {
        return showLinkList;
    }

    public void setShowLinkList(List<String> showLinkList) {
        this.showLinkList = showLinkList;
    }

    public List<String> getEditLinkList() {
        return editLinkList;
    }

    public void setEditLinkList(List<String> editLinkList) {
        this.editLinkList = editLinkList;
    }

    public String getShowSubflow() {
        return showSubflow;
    }

    public void setShowSubflow(String showSubflow) {
        this.showSubflow = showSubflow;
    }

    public void addLink(Links links) {
        if (null == links) {
            return;
        }
        if (Links.SHOW.equals(links.getType())) {
            addLink(showLinkList, links.getName());
            return;
        }
        if (Links.EDIT.equals(links.getType())) {
            addLink(editLinkList, links.getName());
        }
    }

    private void addLink(List list, String links) {
        String[] linkList = links.split(",");
        for (String link : linkList) {
            list.add(link);
        }
    }

    public String getFeedbackAbstractRoleId() {
        return feedbackAbstractRoleId;
    }

    public void setFeedbackAbstractRoleId(String feedbackAbstractRoleId) {
        this.feedbackAbstractRoleId = feedbackAbstractRoleId;
    }

    public String getNeedApproval() {
        return needApproval;
    }

    public void setNeedApproval(String needApproval) {
        this.needApproval = needApproval;
    }
}
