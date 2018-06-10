package com.metarnet.eomeem.model;

import com.metarnet.core.common.model.BaseForm;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * Created by xjwang on 2016/3/25.
 */
@Entity
@Table(name = "t_eem_template_data")
@AttributeOverrides({
        @AttributeOverride(name = "objectId", column = @Column(name = "temp_data_id", nullable = false))})
public class TemplateExcelByteData extends BaseForm {

    private byte[] uploadFileData;//上传的流的二进制
    private byte[] xmlFileData;//解析成xml后的二进制

    public byte[] getUploadFileData() {
        return uploadFileData;
    }

    public void setUploadFileData(byte[] uploadFileData) {
        this.uploadFileData = uploadFileData;
    }

    public byte[] getXmlFileData() {
        return xmlFileData;
    }

    public void setXmlFileData(byte[] xmlFileData) {
        this.xmlFileData = xmlFileData;
    }
}
