package com.metarnet.eomeem.model;

import javax.persistence.*;

/**
 * Created by Administrator on 2017/5/17.
 */
@Entity
@Table(name="t_eem_device")
public class DeviceEntity {
    private Long id;
    private Long tpInputID;//报表id
    private String sheetName;//报表名称
    private String deviceName;//设备名称
    private String type;//分甲乙丙三类

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false, updatable = false )
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTpInputID() {
        return tpInputID;
    }

    public void setTpInputID(Long tpInputID) {
        this.tpInputID = tpInputID;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
