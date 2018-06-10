package com.metarnet.core.common.service.impl;

import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.model.EnumItem;
import com.metarnet.core.common.model.EnumType;
import com.metarnet.core.common.model.EnumValue;
import com.metarnet.core.common.service.IEnumConfigService;
import com.metarnet.core.common.utils.HttpRequestUtil;
import com.ucloud.paas.agent.PaasException;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/7/13.
 */
@Service
public class EnumConfigServiceImpl implements IEnumConfigService {
    @Resource
    private IBaseDAO baseDAO;

    @Override
    public EnumValue getEnumValueById(Integer enumValueId) throws PaasException {
        return  HttpRequestUtil.getEnumValueById(enumValueId);
    }

    @Override
    public EnumType getEnumType(String enumItemCode, String orgId, Integer status) throws PaasException {
        EnumType enumType = new EnumType();
        if(null!=enumItemCode){
            enumType = HttpRequestUtil.getEnumType("enumItemCode",enumItemCode);
        }else{
            enumType = HttpRequestUtil.getEnumType("enumItemId",orgId);
        }
        return enumType;
    }

    @Override
    public List<EnumType> getFilterChildEnumType(String parentEnumItemCode, Integer parentEnumValueId, String orgId, Integer status) throws PaasException {
        List<EnumType> enumTypes = new ArrayList<EnumType>();
        EnumType enumType = new EnumType();
        try {
            List<EnumItem> enumItems = baseDAO.find("from EnumItem m where m.enumItemCode=? and m.lifeCircleStatus=?",new Object[]{parentEnumItemCode,status});
            List<EnumValue> enumValueList = baseDAO.find("from EnumValue e where e.parentEnumValueId=? and e.lifeCircleStatus=?",new Object[]{parentEnumValueId,status});
            if(enumItems != null && enumItems.size() > 0){
                enumType.setEnumItem(enumItems.get(0));
            }
            if(enumValueList != null && enumValueList.size() > 0){
                enumType.setEnumValues(enumValueList);
            }
            enumTypes.add(enumType);
        } catch (Exception e){
            e.printStackTrace();
        }
        return enumTypes;
    }

    @Override
    public List<EnumType> getChildEnumType(String parentEnumItemCode, String orgId, Integer status) throws PaasException {
        return null;
    }

    @Override
    public EnumValue getEnumValue(String enumItemCode, String enumValueCode, Integer enumValueNum, Integer parentEnumValueId, String orgId) throws PaasException {
        EnumValue enumValue = new EnumValue();
        EnumType enumType = HttpRequestUtil.getEnumType("enumItemCode",enumItemCode);
        if(enumType != null){
            List<EnumValue> list = enumType.getEnumValues();
            for(EnumValue value : list){
                if(enumValueCode.equals(value.getEnumValueCode())){
                    return value;
                }
            }
        }
        return enumValue;
    }
}
