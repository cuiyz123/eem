package com.metarnet.core.common.model;

/**
 * Created by Administrator on 2015/7/10.
 */
public class OrgShortNameStructure {

    private OrgShortNameEntity groupCompany;
    private OrgShortNameEntity provinceCompany;
    private OrgShortNameEntity cityCompany;
    private OrgShortNameEntity countyCompany;

    public OrgShortNameEntity getGroupCompany() {
        return groupCompany;
    }

    public void setGroupCompany(OrgShortNameEntity groupCompany) {
        this.groupCompany = groupCompany;
    }

    public OrgShortNameEntity getProvinceCompany() {
        return provinceCompany;
    }

    public void setProvinceCompany(OrgShortNameEntity provinceCompany) {
        this.provinceCompany = provinceCompany;
    }

    public OrgShortNameEntity getCityCompany() {
        return cityCompany;
    }

    public void setCityCompany(OrgShortNameEntity cityCompany) {
        this.cityCompany = cityCompany;
    }

    public OrgShortNameEntity getCountyCompany() {
        return countyCompany;
    }

    public void setCountyCompany(OrgShortNameEntity countyCompany) {
        this.countyCompany = countyCompany;
    }
}
