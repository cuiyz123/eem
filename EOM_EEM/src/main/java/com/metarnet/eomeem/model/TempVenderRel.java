package com.metarnet.eomeem.model;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Created by Administrator on 2017/3/17.
 */
@Table(name = "t_eem_temp_vender")
@Entity
public class TempVenderRel {

    private int id;

    private int tempId;

    private int venderId;

    private int relValue;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "object_id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "temp_id")
    public int getTempId() {
        return tempId;
    }

    public void setTempId(int tempId) {
        this.tempId = tempId;
    }

    @Column(name = "vender_id")
    public int getVenderId() {
        return venderId;
    }

    public void setVenderId(int venderId) {
        this.venderId = venderId;
    }

    @Column(name = "rel_value")
    public int getRelValue() {
        return relValue;
    }

    public void setRelValue(int relValue) {
        this.relValue = relValue;
    }
}
