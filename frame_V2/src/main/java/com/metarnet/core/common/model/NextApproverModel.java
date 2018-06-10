package com.metarnet.core.common.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: jietianwu
 * Date: 13-9-25
 * Time: 上午9:23
 * 下一步审核人
 */
public class NextApproverModel {
    //下一步审核人的部门取值类型,本部门,本公司,上级公司
    public enum Type {Dept, ParentDept, Company, ParentCompany}

    /**
     * flag
     * 0—needApproval扩展项没有填值
     * 1—直接给出下一步环节id，格式【1：下一步执行环节id】，如【1:manualActivity1】
     * 2—跨越父子流程，且下一步环节不确定，格式【2:下一步执行环节角色/id，判断条件；。。。】，如【2:101020,isProvinceApply=0;101019,isProvinceApply=1】
     * 3—跨越父子流程，且下一步环节的参与者是从相关数据区中得倒，格式 【3:下一步执行环节角色/id，判断条件；。。。】描述处格式【相关数据区key值】，如【feedbackDeptList】
     */
    private int flag;
    private String paramInfo;
    private String type;

    public NextApproverModel(String info, String type) {
        this.type = type;
        if (StringUtils.containsOnly(info, "null")){
            this.flag = 0;
            this.paramInfo = info;
        }else{
            String _str[] = info.split(":");
            this.flag = Integer.parseInt(_str[0]);
            this.paramInfo = _str[1];
        }
    }

    public int getFlag() {
        return flag;
    }

    public String getType() {
        return type;
    }

    public String getParamInfo() {
        return paramInfo;
    }
}
