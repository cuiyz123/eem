package com.metarnet.core.common.utils;

import com.alibaba.fastjson.JSONArray;
import com.metarnet.core.common.model.EnumType;
import com.metarnet.core.common.model.EnumValue;
import com.ucloud.paas.agent.PaasException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 15-9-25
 * Time: 上午10:55
 * To change this template use File | Settings | File Templates.
 */
public class HttpRequestUtil {

//    private static final String URL = "http://10.249.6.32:8088/unicom_enum/eomEnumServlet.do?";

    private static List<EnumValue> getEnumValueListFromUrl(String type, String enumItemCodeOrId) {

        String url = Constants.ENUMURL + "type=" + type + "&enumItemCodeOrId=" + enumItemCodeOrId;
        String return_str = "";
        List<EnumValue> enumValueList = new ArrayList<EnumValue>();
        try {
            return_str = HttpClientUtil.sendGetRequest(url, "utf-8").trim();
            enumValueList = JSONArray.parseArray(return_str, EnumValue.class);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return enumValueList;
    }


    public static EnumValue getEnumValueById(Integer enumValueId) {

        EnumValue enumValue = new EnumValue();
        try {
            List<EnumValue> valueList = getEnumValueListFromUrl("enumValueId", enumValueId + "");
            if (null != valueList && valueList.size() > 0) {
                enumValue = valueList.get(0);
            }
        } catch (Exception ea) {
            ea.printStackTrace();
        }
        return enumValue;
    }

    public static EnumType getEnumType(String type, String orgId) {

        EnumType enumType = new EnumType();
        try {
            List<EnumValue> enumValueList = getEnumValueListFromUrl(type, orgId);
            enumType.setEnumValues(enumValueList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return enumType;
    }


    public List<EnumType> getFilterChildEnumType(Integer parentEnumValueId) throws PaasException {
        List<EnumType> enumTypes = new ArrayList<EnumType>();
//        enumTypes = this.getEnumValueListFromUrl("parentEnumValueId",parentEnumValueId+"");
        return enumTypes;
    }

    public static void main(String[] arg) {

        int num1 = 4 & 42;
        String str = "test";
        String str1 = str.replace("t", "0");
        String str2 = new String("test");
        int int1 = 130;
        Integer int2 = new Integer(130);
        Integer int3 = new Integer(130);
        System.out.println("num1 = " + num1);
        System.out.println("str.hashCode() = " + str.hashCode());
        System.out.println("str1.hashCode() = " + str1.hashCode());
        System.out.println("str1 == str2 = " + (str1 == str2));
        System.out.println("int1 == int2 = " + (int1 == int2));
        System.out.println("int2 == 130 = " + (int2 == 130));
        System.out.println("int2 == int3 = " + (int2 == int3));

        StringBuffer sb = new StringBuffer();
        sb.append("test");

        //把JSON字符串转换为JAVA 对象数组
//        String personstr = "[{enumValueId:'204619',enumValueName:'作废'},{enumValueId:'203135',enumValueName:'加急'},{enumValueId:'203136',enumValueName:'变更追加'},{enumValueId:'203137',enumValueName:'挂起'},{enumValueId:'203138',enumValueName:'撤单'},{enumValueId:'203931',enumValueName:'延期'},{enumValueId:'203932',enumValueName:'解挂'}]";
//        JSONArray json = JSONArray.fromObject(personstr);
//        List<EnumValue> persons = (List<EnumValue>)JSONArray.toCollection(json, EnumValue.class);

        //
//        String str = HttpClientUtil.sendGetRequest("http://127.0.0.1:7000/unicom_zidian/eomEnumServlet.do?type=enumItemId&enumItemCodeOrId=200357","utf-8");
//        JSONArray json = JSONArray.fromObject(str);
//        List<EnumValue> persons = (List<EnumValue>)JSONArray.toCollection(json, EnumValue.class);

//         HttpRequestUtil httpRequestUtil = new HttpRequestUtil();
//        EnumType enumType = httpRequestUtil.getEnumType("enumItemId","200357");
//        for(EnumValue enumValue_n: enumType.getEnumValues()){
//            System.out.print("getEnumValueId:"+enumValue_n.getEnumValueId());
//            System.out.println("\tgetEnumValueName:"+enumValue_n.getEnumValueName());
//        }

//
//        System.out.println(httpRequestUtil.getEnumValueById(2303).getEnumValueName());
    }

}
