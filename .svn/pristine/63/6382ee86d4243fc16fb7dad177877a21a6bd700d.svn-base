package com.metarnet.eomeem.model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Administrator on 2016/7/6.
 * 公告实体
 */
@Entity
@AttributeOverrides({
        @AttributeOverride(name = "objectId", column = @Column(name = "NOTICE_ID", unique = true, nullable = false))
})
@Table(name = "t_eem_notice_info")
public class EemNoticeEntity extends BaseEntity{
    private Date startDate;//开始时间
    private Date endDate;//结束时间
    private Boolean top;//是否置顶
    private Boolean overdue;//是否过期
    private String operOrgCode;
    private String deptNames;
    private String deptCodes;
    private String editHtml;

    @Column(name = "START_DATE")
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Column(name = "END_DATE")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Column(name = "TOP")
    public Boolean getTop() {
        return top;
    }

    public void setTop(Boolean top) {
        this.top = top;
    }

    @Column(name = "OVERDUE")
    public Boolean getOverdue() {
        return overdue;
    }

    public void setOverdue(Boolean overdue) {
        this.overdue = overdue;
    }

    @Column(name = "OPER_ORG_CODE")
    public String getOperOrgCode() {
        return operOrgCode;
    }

    public void setOperOrgCode(String operOrgCode) {
        this.operOrgCode = operOrgCode;
    }

    @Column(name = "DEPT_NAMES")
    public String getDeptNames() {
        return deptNames;
    }

    public void setDeptNames(String deptNames) {
        this.deptNames = deptNames;
    }

    @Column(name = "DEPT_CODES")
    public String getDeptCodes() {
        return deptCodes;
    }

    public void setDeptCodes(String deptCodes) {
        this.deptCodes = deptCodes;
    }

    @Transient
    public String getEditHtml() {
        return editHtml;
    }

    public void setEditHtml(String editHtml) {
        this.editHtml = editHtml;
    }
}
