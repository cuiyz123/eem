package com.metarnet.core.common.model;


import java.util.List;

/**
 * Created by Administrator on 2015/7/13.
 */
public class EnumType {
    private EnumItem enumItem;
    private List<EnumValue> enumValues;

    public EnumItem getEnumItem() {
        return enumItem;
    }

    public void setEnumItem(EnumItem enumItem) {
        this.enumItem = enumItem;
    }

    public List<EnumValue> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<EnumValue> enumValues) {
        this.enumValues = enumValues;
    }
}
