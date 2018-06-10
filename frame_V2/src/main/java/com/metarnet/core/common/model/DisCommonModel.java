package com.metarnet.core.common.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: wangzwty
 * Date: 16-3-31
 * Time: 下午3:17
 * 调度单
 */
@AttributeOverride(name = "objectId", column = @Column(name = "DISPATCH_ID"))
@MappedSuperclass
public class DisCommonModel extends BaseForm {

    private String mainTransfer;//主送对象 前台传入,支持人和组织，格式 137000：User,138:Org
    private String mainTransferLabel;//主送对象 前台传入,支持人和组织，格式 137000：User,138:Org

    private String copyTransfer;//抄送对象  规则与主送相同
    private String copyTransferLabel;//抄送对象  规则与主送相同

    private Long issueUserId;    //签发人
    private String issueUserTrueName;    //签发人

    private Timestamp issueTime;    //签发时间

    @Column(name = "MAIN_TRANSFER")
    public String getMainTransfer() {
        return mainTransfer;
    }

    public void setMainTransfer(String mainTransfer) {
        this.mainTransfer = mainTransfer;
    }

    @Column(name = "COPY_TRANSFER")
    public String getCopyTransfer() {
        return copyTransfer;
    }

    public void setCopyTransfer(String copyTransfer) {
        this.copyTransfer = copyTransfer;
    }

    @Column(name = "ISSUE_USER_ID")
    public Long getIssueUserId() {
        return issueUserId;
    }

    public void setIssueUserId(Long issueUserId) {
        this.issueUserId = issueUserId;
    }

    @Column(name = "ISSUE_TIME")
    public Timestamp getIssueTime() {
        return issueTime;
    }

    public void setIssueTime(Timestamp issueTime) {
        this.issueTime = issueTime;
    }

    @Column(name = "MAIN_TRANSFER_LABEL")
    public String getMainTransferLabel() {
        return mainTransferLabel;
    }

    public void setMainTransferLabel(String mainTransferLabel) {
        this.mainTransferLabel = mainTransferLabel;
    }

    @Column(name = "COPY_TRANSFER_LABEL")
    public String getCopyTransferLabel() {
        return copyTransferLabel;
    }

    public void setCopyTransferLabel(String copyTransferLabel) {
        this.copyTransferLabel = copyTransferLabel;
    }

    @Column(name = "ISSUE_USER_TRUE_NAME")
    public String getIssueUserTrueName() {
        return issueUserTrueName;
    }

    public void setIssueUserTrueName(String issueUserTrueName) {
        this.issueUserTrueName = issueUserTrueName;
    }
}
