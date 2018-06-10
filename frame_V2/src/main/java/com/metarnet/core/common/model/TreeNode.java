package com.metarnet.core.common.model;

import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: jietianwu
 * Date: 13-4-18
 * Time: 下午5:38
 * ucloud树模型
 */
public class TreeNode {
    private String id;
    private String text;
    private String hasChild;
    private String xmlSource;
    private String defaultOpen;
    private String logoImagePath;
    private String statusFlag;
    private String title;
    private String hrefPath;
    private String target;
    private String dbClick;
    private String orderStr;
    private String returnValue;
    private String isSelected;
    private String indeterminate;
    private String thisType;
    private String detailedType;
    private String isSubmit;
    private String parentId;
    private String childIds;
    private LinkedList<TreeNode> treeNode = new LinkedList<TreeNode>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHasChild() {
        return hasChild;
    }

    public void setHasChild(String hasChild) {
        this.hasChild = hasChild;
    }

    public String getXmlSource() {
        return xmlSource;
    }

    public void setXmlSource(String xmlSource) {
        this.xmlSource = xmlSource;
    }

    public String getDefaultOpen() {
        return defaultOpen;
    }

    public void setDefaultOpen(String defaultOpen) {
        this.defaultOpen = defaultOpen;
    }

    public String getLogoImagePath() {
        return logoImagePath;
    }

    public void setLogoImagePath(String logoImagePath) {
        this.logoImagePath = logoImagePath;
    }

    public String getStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(String statusFlag) {
        this.statusFlag = statusFlag;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHrefPath() {
        return hrefPath;
    }

    public void setHrefPath(String hrefPath) {
        this.hrefPath = hrefPath;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getDbClick() {
        return dbClick;
    }

    public void setDbClick(String dbClick) {
        this.dbClick = dbClick;
    }

    public String getOrderStr() {
        return orderStr;
    }

    public void setOrderStr(String orderStr) {
        this.orderStr = orderStr;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    public String getIndeterminate() {
        return indeterminate;
    }

    public void setIndeterminate(String indeterminate) {
        this.indeterminate = indeterminate;
    }

    public String getThisType() {
        return thisType;
    }

    public void setThisType(String thisType) {
        this.thisType = thisType;
    }

    public String getDetailedType() {
        return detailedType;
    }

    public void setDetailedType(String detailedType) {
        this.detailedType = detailedType;
    }

    public String getSubmit() {
        return isSubmit;
    }

    public void setSubmit(String submit) {
        isSubmit = submit;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getChildIds() {
        return childIds;
    }

    public void setChildIds(String childIds) {
        this.childIds = childIds;
    }

    public LinkedList<TreeNode> getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(LinkedList<TreeNode> treeNode) {
        this.treeNode = treeNode;
    }

	public String getIsSelected() {
		return isSelected;
	}

	public void setIsSelected(String isSelected) {
		this.isSelected = isSelected;
	}

	public String getIsSubmit() {
		return isSubmit;
	}

	public void setIsSubmit(String isSubmit) {
		this.isSubmit = isSubmit;
	}
}
