package com.metarnet.eomeem.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Administrator on 2016/8/29.
 */
@Entity
@Table(name = "t_eem_org_notice")
public class EemOrgNoticeEntity {
    private Long objectID;
    private String orgCode;
    private Long noticeID;

    @Id
    @Column(name = "OBJECT_ID", nullable = false, updatable = false)
    public Long getObjectID() {
        return objectID;
    }

    public void setObjectID(Long objectID) {
        this.objectID = objectID;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public Long getNoticeID() {
        return noticeID;
    }

    public void setNoticeID(Long noticeID) {
        this.noticeID = noticeID;
    }
}
