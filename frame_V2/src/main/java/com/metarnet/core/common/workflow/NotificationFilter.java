package com.metarnet.core.common.workflow;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: hadoop
 * Date: 15-5-13
 * Time: 下午5:47
 * To change this template use File | Settings | File Templates.
 */
public class NotificationFilter {

    private String processModelID;
    private String processModelName;
    private String activityID;
    private String activityName;
    private String appID;
    private String notifyType;
    private String notificationInstID;
    private String processInstID;
    private String jobTitle;
    private String jobID;
    private String senderType;
    private String senderID;
    private Date beginDeliveryDate;
    private Date endDeliveryDate;
    private PageCondition pageCondition;
    private String Taskinstid;

    public Date getBeginDeliveryDate() {
        return beginDeliveryDate;
    }

    public void setBeginDeliveryDate(Date beginDeliveryDate) {
        this.beginDeliveryDate = beginDeliveryDate;
    }

    public String getProcessModelID() {
        return processModelID;
    }

    public void setProcessModelID(String processModelID) {
        this.processModelID = processModelID;
    }

    public String getProcessModelName() {
        return processModelName;
    }

    public void setProcessModelName(String processModelName) {
        this.processModelName = processModelName;
    }

    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(String notifyType) {
        this.notifyType = notifyType;
    }

    public String getNotificationInstID() {
        return notificationInstID;
    }

    public void setNotificationInstID(String notificationInstID) {
        this.notificationInstID = notificationInstID;
    }

    public String getProcessInstID() {
        return processInstID;
    }

    public void setProcessInstID(String processInstID) {
        this.processInstID = processInstID;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public Date getEndDeliveryDate() {
        return endDeliveryDate;
    }

    public void setEndDeliveryDate(Date endDeliveryDate) {
        this.endDeliveryDate = endDeliveryDate;
    }

    public PageCondition getPageCondition() {
        return pageCondition;
    }

    public void setPageCondition(PageCondition pageCondition) {
        this.pageCondition = pageCondition;
    }

    public String getTaskinstid() {
        return Taskinstid;
    }

    public void setTaskinstid(String taskinstid) {
        Taskinstid = taskinstid;
    }
}
