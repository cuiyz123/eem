package com.metarnet.core.common.model;

/**
 * Created with IntelliJ IDEA.
 * User: jietianwu
 * Date: 13-7-5
 * Time: 下午5:03
 * 前、后处理模型
 */
public class ProcessorModel {
    private String name;
    private String params;

    public ProcessorModel(String name, String params) {
        this.name = name;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }
}
