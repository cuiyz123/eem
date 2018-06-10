package com.metarnet.core.common.model;

/**
 * Created by Administrator on 2015/7/15.
 */
public abstract class AbstractFileInfo {
    private String fileName;

    public AbstractFileInfo() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
