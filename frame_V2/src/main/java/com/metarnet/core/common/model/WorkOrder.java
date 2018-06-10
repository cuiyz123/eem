package com.metarnet.core.common.model;

/**
 * Created by z on 2015/3/3.
 */
public class WorkOrder {
    private Long sequenceValue;
    private String workOrder;

    public Long getSequenceValue() {
        return sequenceValue;
    }

    public void setSequenceValue(Long sequenceValue) {
        this.sequenceValue = sequenceValue;
    }

    public String getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(String workOrder) {
        this.workOrder = workOrder;
    }
}
