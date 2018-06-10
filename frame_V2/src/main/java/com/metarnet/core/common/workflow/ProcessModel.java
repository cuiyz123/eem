package com.metarnet.core.common.workflow;

/**
 * Created with IntelliJ IDEA.
 * User: hadoop
 * Date: 15-5-13
 * Time: 下午4:18
 * To change this template use File | Settings | File Templates.
 */
public class ProcessModel {

    private String ProcessModelID;
    private String processModelName;
    private String ProcessModelDes;

    public String getProcessModelID() {
        return ProcessModelID;
    }

    public void setProcessModelID(String processModelID) {
        ProcessModelID = processModelID;
    }

    public String getProcessModelName() {
        return processModelName;
    }

    public void setProcessModelName(String processModelName) {
        this.processModelName = processModelName;
    }

    public String getProcessModelDes() {
        return ProcessModelDes;
    }

    public void setProcessModelDes(String processModelDes) {
        ProcessModelDes = processModelDes;
    }
}
