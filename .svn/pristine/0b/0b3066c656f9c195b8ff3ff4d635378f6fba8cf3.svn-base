package com.metarnet.eomeem.utils;

import com.alibaba.fastjson.JSON;
import com.metarnet.core.common.utils.Constants;
import com.metarnet.core.common.utils.HttpClientUtil;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dong on 2017/5/22.
 */
public class PowerUtil {

    public static final PowerUtil _powerUtil = new PowerUtil();
    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(PowerUtil.class);

    public static PowerUtil getInstence(){
        return  _powerUtil;
    }

    private PowerUtil(){

    }

    public String findSpecialtyByNodeId(String userName,String nodeId){
        Map<String,String> paramsMap = new HashMap();
        paramsMap.put("userName",userName);
        paramsMap.put("nodeID",nodeId);
        paramsMap.put("processCode","eom_eem");
        String result = HttpClientUtil.sendPostRequestByJava(Constants.POWERURL + "/powerController.do?method=findSpecialtyByNodeID", paramsMap);
        List<String> specialtys = JSON.parseArray(result, String.class);
       String resutls="";
        for(String str:specialtys){
            resutls+="'"+str+"',";
        }
        if(org.apache.commons.lang3.StringUtils.isNotBlank(resutls)){
            resutls = resutls.substring(0,resutls.length()-1);
        }
        return resutls;
    }

}
