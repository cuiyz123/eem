package com.metarnet.core.common.model;
/**
 * Created with IntelliJ IDEA.
 * User: metarnet
 * Date: 13-3-13
 * Time: 下午2:59
 * <p/>
 * 流程引擎扩展字段的extendNode
 */
public class ExtendNode {
    private String key;
    private String value;
    private String desc;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
