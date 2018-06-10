package com.metarnet.eomeem.vo;

/**
 * Created by dong on 2017/5/17.
 */
public class Specialty  implements Cloneable {

    private Long objectId;
    private String scode;
    private String sname;
    private Boolean checked = false;



    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getScode() {
        return scode;
    }

    public void setScode(String scode) {
        this.scode = scode;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }


    public Object clone() throws CloneNotSupportedException {
        Specialty clone = null;
        try{
            clone = (Specialty) super.clone();

        }catch(CloneNotSupportedException e){
            throw new RuntimeException(e); // won't happen
        }

        return clone;
    }
}
