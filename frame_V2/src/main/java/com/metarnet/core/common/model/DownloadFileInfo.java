package com.metarnet.core.common.model;

import java.io.ByteArrayOutputStream;
import java.util.Date;

/**
 * Created by Administrator on 2015/7/15.
 */
public class DownloadFileInfo extends AbstractFileInfo {
    private String keywords;
    private String desc;
    private Long fileSize;
    private Date createTime;
    private ByteArrayOutputStream byteArrayOutputStream;

    public DownloadFileInfo() {
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

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return byteArrayOutputStream;
    }

    public void setByteArrayOutputStream(ByteArrayOutputStream byteArrayOutputStream) {
        this.byteArrayOutputStream = byteArrayOutputStream;
    }
}
