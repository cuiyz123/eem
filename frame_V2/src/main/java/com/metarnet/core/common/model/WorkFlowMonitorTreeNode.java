package com.metarnet.core.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hadoop
 * Date: 15-11-10
 * Time: 下午4:56
 * To change this template use File | Settings | File Templates.
 */
public class WorkFlowMonitorTreeNode extends TreeNode {

    /**
     * 到达时间
     */
    private String arriveDateTime;

    /**
     * 处理人
     */
    private String operator;

    /**
     * 处理人ID
     */
    private String operatorId;

    /**
     * 处理部门
     */
    private String operateOrg;

    /**
     * 完成时间
     */
    private String completeDateTime;

    /**
     * 处理类型
     */
    private String processType;

    /**
     * 处理意见
     */
    private String processOpinion;

    /**
     * 环节名称
     */
    private String activityName;

    /**
     * 环节名称
     */
    private Boolean nowActivity;

    /**
     * 处理对象ID
     */
    private String processingObjectId;

    /**
     * 处理对象表名
     */
    private String processingObjectTable;

    private List<WorkFlowMonitorTreeNode> children;


    public String getArriveDateTime() {
        return arriveDateTime;
    }

    public void setArriveDateTime(String arriveDateTime) {
        this.arriveDateTime = arriveDateTime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOperateOrg() {
        return operateOrg;
    }

    public void setOperateOrg(String operateOrg) {
        this.operateOrg = operateOrg;
    }

    public String getCompleteDateTime() {
        return completeDateTime;
    }

    public void setCompleteDateTime(String completeDateTime) {
        this.completeDateTime = completeDateTime;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public String getProcessOpinion() {
        return processOpinion;
    }

    public void setProcessOpinion(String processOpinion) {
        this.processOpinion = processOpinion;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }
    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public Boolean getNowActivity() {
        return nowActivity;
    }

    public void setNowActivity(Boolean nowActivity) {
        this.nowActivity = nowActivity;
    }

    public String getProcessingObjectId() {
        return processingObjectId;
    }

    public void setProcessingObjectId(String processingObjectId) {
        this.processingObjectId = processingObjectId;
    }

    public String getProcessingObjectTable() {
        return processingObjectTable;
    }

    public void setProcessingObjectTable(String processingObjectTable) {
        this.processingObjectTable = processingObjectTable;
    }

    public List<WorkFlowMonitorTreeNode> getChildren() {
        if(children == null){
            children = new ArrayList<WorkFlowMonitorTreeNode>();
        }
        return children;
    }
}
