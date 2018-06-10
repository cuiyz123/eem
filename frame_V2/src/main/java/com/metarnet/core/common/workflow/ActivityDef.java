package com.metarnet.core.common.workflow;

/**
 * Created with IntelliJ IDEA.
 * User: hadoop
 * Date: 15-5-13
 * Time: 下午4:18
 * To change this template use File | Settings | File Templates.
 */
public class ActivityDef {

    private String activityID;
    private String activityName;
    private String activitytype;

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

    public String getActivitytype() {
        return activitytype;
    }

    public void setActivitytype(String activitytype) {
        this.activitytype = activitytype;
    }
}
