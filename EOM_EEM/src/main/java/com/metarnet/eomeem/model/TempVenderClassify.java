package com.metarnet.eomeem.model;

import javax.persistence.*;
import java.io.Serializable;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Created by Administrator on 2017/3/16.
 */
@Entity
@Table(name = "t_eem_temp_ven_classify")
public class TempVenderClassify implements Serializable {

    private int objectId;

    private String tempType;

    private int tpInputId;

    private String shortName;

    private String tempName;

    public TempVenderClassify() {
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "object_id")
    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    @Column(name = "temp_type")
    public String getTempType() {
        return tempType;
    }

    public void setTempType(String tempType) {
        this.tempType = tempType;
    }

    @Column(name = "tpInputId")
    public int getTpInputId() {
        return tpInputId;
    }

    public void setTpInputId(int tpInputId) {
        this.tpInputId = tpInputId;
    }

    @Column(name = "short_name")
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Column(name = "temp_name")
    public String getTempName() {
        return tempName;
    }

    public void setTempName(String tempName) {
        this.tempName = tempName;
    }
}
