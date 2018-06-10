package com.metarnet.core.common.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: metarnet
 * Date: 13-4-3
 * Time: 下午7:54
 * To change this template use File | Settings | File Templates.
 */
public class AreaModel {
    private String areaName;
    private List<String> showLinkList = new ArrayList();
    private List<String> editLinkList = new ArrayList();
    private LinkedList<ComponentModel> componentModels = new LinkedList<ComponentModel>();

    public AreaModel() {
    }

    public AreaModel(String areaName) {
        this.areaName = areaName;
    }

    public LinkedList<ComponentModel> getComponentModels() {
        return componentModels;
    }

    public void setComponentModels(LinkedList<ComponentModel> componentModels) {
        this.componentModels = componentModels;
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

    @Override
    public boolean equals(Object o) {
        if (this == o){
        	return true;
        }
        if (o == null || getClass() != o.getClass()){
        	return false;
        }

        AreaModel areaModel = (AreaModel) o;

        if (areaName != null ? !areaName.equals(areaModel.areaName) : areaModel.areaName != null){
        	return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return areaName != null ? areaName.hashCode() : 0;
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
}
