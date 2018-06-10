package com.metarnet.core.common.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangzwty
 * Date: 16-04-05
 * Time: 上午9:25
 * 业务实体的注解信息
 */
public class EntityBinder {
    private List<IdBinder> idBinderList = new ArrayList<IdBinder>();
    private List<Field> userFields = new ArrayList<Field>();
    private List<Field> deptFields = new ArrayList<Field>();
    private List<Field> enumFields = new ArrayList<Field>();
    private List<Field> dateFields = new ArrayList<Field>();
    private List<Field> attachmentFields = new ArrayList<Field>();
    private Field mainTransferField;
    private Field copyField;
    private Field participantField;

    public List<IdBinder> getIdBinderList() {
        return idBinderList;
    }

    public void setIdBinderList(List<IdBinder> idBinderList) {
        this.idBinderList = idBinderList;
    }

    public List<Field> getUserFields() {
        return userFields;
    }

    public void setUserFields(List<Field> userFields) {
        this.userFields = userFields;
    }

    public List<Field> getDeptFields() {
        return deptFields;
    }

    public void setDeptFields(List<Field> deptFields) {
        this.deptFields = deptFields;
    }

    public List<Field> getEnumFields() {
        return enumFields;
    }

    public void setEnumFields(List<Field> enumFields) {
        this.enumFields = enumFields;
    }

    public List<Field> getDateFields() {
        return dateFields;
    }

    public void setDateFields(List<Field> dateFields) {
        this.dateFields = dateFields;
    }

    public List<Field> getAttachmentFields() {
        return attachmentFields;
    }

    public void setAttachmentFields(List<Field> attachmentFields) {
        this.attachmentFields = attachmentFields;
    }

    public Field getMainTransferField() {
        return mainTransferField;
    }

    public void setMainTransferField(Field mainTransferField) {
        this.mainTransferField = mainTransferField;
    }

    public Field getCopyField() {
        return copyField;
    }

    public void setCopyField(Field copyField) {
        this.copyField = copyField;
    }

    public Field getParticipantField() {
        return participantField;
    }

    public void setParticipantField(Field participantField) {
        this.participantField = participantField;
    }
}
