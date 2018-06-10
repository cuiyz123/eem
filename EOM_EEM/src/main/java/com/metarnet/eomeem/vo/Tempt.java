package com.metarnet.eomeem.vo;

import java.util.List;
import java.util.Set;

/**
 * Created by dong on 2017/5/17.
 */
public class Tempt {
    private Long objectId;
    private String tempName;
    private String shortName;

    private List<Specialty> nodeVoSet;

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getTempName() {
        return tempName;
    }

    public void setTempName(String tempName) {
        this.tempName = tempName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public List<Specialty> getNodeVoSet() {
        return nodeVoSet;
    }

    public void setNodeVoSet(List<Specialty> nodeVoSet) {
        this.nodeVoSet = nodeVoSet;
    }
}
