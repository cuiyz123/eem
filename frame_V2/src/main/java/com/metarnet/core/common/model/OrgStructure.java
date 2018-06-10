package com.metarnet.core.common.model;

import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;

/**
 * Created by Administrator on 2015/7/10.
 */
public class OrgStructure {
    private OrgEntity groupCompany;
    private OrgEntity provinceCompany;
    private OrgEntity cityCompany;
    private OrgEntity countyCompany;

    public OrgEntity getGroupCompany() {
        return groupCompany;
    }

    public void setGroupCompany(OrgEntity groupCompany) {
        this.groupCompany = groupCompany;
    }

    public OrgEntity getProvinceCompany() {
        return provinceCompany;
    }

    public void setProvinceCompany(OrgEntity provinceCompany) {
        this.provinceCompany = provinceCompany;
    }

    public OrgEntity getCityCompany() {
        return cityCompany;
    }

    public void setCityCompany(OrgEntity cityCompany) {
        this.cityCompany = cityCompany;
    }

    public OrgEntity getCountyCompany() {
        return countyCompany;
    }

    public void setCountyCompany(OrgEntity countyCompany) {
        this.countyCompany = countyCompany;
    }
}
