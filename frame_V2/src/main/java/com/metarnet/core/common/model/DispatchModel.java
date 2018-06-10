package com.metarnet.core.common.model;

/**
 * Created with IntelliJ IDEA.
 * User: jietianwu
 * Date: 13-4-18
 * Time: 下午7:55
 * 派发树模型
 */
public class DispatchModel {
    //派发枚举值
    public static final String DISPATCHTYPEDEPT = "org";
    public static final String DISPATCHTYPEPERSON = "user";
    //派发树类型
    private String dispatchType;
    //组织id
        private Integer cloudOrgId;
    //角色id
    private Integer cloudRoleId;
    //产品id
    private String productcode;

    public String getDispatchType() {
        return dispatchType;
    }

    public void setDispatchType(String dispatchType) {
        this.dispatchType = dispatchType;
    }

    public Integer getCloudOrgId() {
        return cloudOrgId;
    }

    public void setCloudOrgId(Integer cloudOrgId) {
        this.cloudOrgId = cloudOrgId;
    }

    public Integer getCloudRoleId() {
        return cloudRoleId;
    }

    public void setCloudRoleId(Integer cloudRoleId) {
        this.cloudRoleId = cloudRoleId;
    }

    public String getProductcode() {
        return productcode;
    }

    public void setProductcode(String productcode) {
        this.productcode = productcode;
    }
}
