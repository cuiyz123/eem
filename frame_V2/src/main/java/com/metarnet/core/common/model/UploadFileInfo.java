package com.metarnet.core.common.model;

/**
 * Created by Administrator on 2015/7/15.
 */
public class UploadFileInfo extends AbstractFileInfo {
    private String keywords;
    private String desc;
    private String operator;
    private Long fileSize;

    public UploadFileInfo() {
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}
