package com.metarnet.eomeem.model;

import javax.persistence.*;

/**
 * Created by Administrator on 2016/7/19.
 */
@Entity
@AttributeOverrides({
        @AttributeOverride(name = "objectId", column = @Column(name = "VENDOR_ID", unique = true, nullable = false))
})
@Table(name = "t_eem_vendor_info")
public class VendorEntity extends BaseEntity{
    private String vendorCode;
    private String vendorName;
    private String shortName;//原有的英文简称
    private String remark;
    private String operOrgCode;
    private String category;//厂家所用设备的类型 甲乙丙
    private String shortName1;//厂商中文简称


    @Column(name = "SHORT_NAME1")
    public String getShortName1() {
        return shortName1;
    }

    public void setShortName1(String shortName1) {
        this.shortName1 = shortName1;
    }

    @Column(name = "CATEGORY")
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Column(name = "VENDOR_CODE")
    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    @Column(name = "VENDOR_NAME")
    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    @Column(name = "SHORT_NAME")
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Column(name = "REMARK")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Column(name = "OPER_ORG_CODE")
    public String getOperOrgCode() {
        return operOrgCode;
    }

    public void setOperOrgCode(String operOrgCode) {
        this.operOrgCode = operOrgCode;
    }
}
