package com.metarnet.core.common.model;

/**
 * Created with IntelliJ IDEA.
 * User: metarnet
 * Date: 13-4-13
 * Time: 上午11:35
 * To change this template use File | Settings | File Templates.
 */
public class Links {
    public final static String SHOW = "show";
    public final static String EDIT = "edit";
    //转派组件名称
    public final static String FORWARDFORMLINK = "ForwardFormLink";
    private String name;
    private String type;

    public Links(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
