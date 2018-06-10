package com.metarnet.eomeem.model;

import javax.persistence.*;



/**
 * Created by Administrator on 2017/5/5.
 */
@Entity
@Table(name="t_eem_report")
public class ReportEntity {
    private Long id;
    private Long tpInputID;
    private String sheetName;
    private String shortName;//报表简介
    private String venderName;//厂商名称
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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getVenderName() {
        return venderName;
    }

    public void setVenderName(String venderName) {
        this.venderName = venderName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
