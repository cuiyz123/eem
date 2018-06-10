package com.metarnet.core.common.adapter;

import com.metarnet.core.common.model.EnumType;
import com.metarnet.core.common.model.EnumValue;
import com.metarnet.core.common.service.IEnumConfigService;
import com.metarnet.core.common.utils.Constants;
import com.ucloud.paas.agent.PaasException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: metarnet-yhhu
 * Date: 13-4-5
 * Time: 上午11:44
 * 配置管理服务适配器
 */
@Service
public class EnumConfigAdapter {
    private static  EnumConfigAdapter enumConfigAdapter = new EnumConfigAdapter();

    @Autowired
    private IEnumConfigService enumConfigService;


    @PostConstruct
    public void init() {
        enumConfigAdapter = this;
        enumConfigAdapter.enumConfigService = this.enumConfigService;
    }

    private EnumConfigAdapter() {
    }

    public static EnumConfigAdapter getInstence() {
        return enumConfigAdapter;
    }

//    @Override
//    public EnumValue getEnumValue(String enumItemCode, String enumValueCode, Integer enumValueNum,
//                                  Integer parentEnumValueId, String orgId) throws PaasException {
//        EnumValue enumValue = super.getEnumValue(enumItemCode, enumValueCode, enumValueNum, parentEnumValueId, orgId);
//        EnumValue enumValueDef = new EnumValue();
//        enumValueDef.setEnumValueName("没查到");
//        enumValueDef.setEnumValueId(1);
//        enumValueDef.setEnumValueCode("没查到");
//        enumValueDef.setEnumItemCode("没找到");
//        enumValueDef.setEnumItemId(1);
//        return enumValue == null ? enumValueDef : enumValue;
//    }

    public EnumValue getEnumValueById(Integer enumValueId) throws PaasException {
        EnumValue enumValue = enumConfigAdapter.enumConfigService.getEnumValueById(enumValueId);
        EnumValue enumValueDef = new EnumValue();
        enumValueDef.setEnumValueName("没查到");
        enumValueDef.setEnumValueId(1);
        enumValueDef.setEnumValueCode("没查到");
        enumValueDef.setEnumItemCode("没找到");
        enumValueDef.setEnumItemId(1);
        return enumValue == null ? enumValueDef : enumValue;
    }

    public EnumType getEnumType(String enumItemCode, String orgId, Integer status) throws PaasException {
        EnumType enumType = enumConfigAdapter.enumConfigService.getEnumType(enumItemCode, orgId, status);
        for (EnumValue enumValue : enumType.getEnumValues()) {
            if (StringUtils.isNotEmpty(enumValue.getAttribute4()) && enumValue.getAttribute4().contains(Constants.MODEL_CODE)) {
                enumType.getEnumValues().remove(enumValue);
            }
        }
        return enumType;
    }

    public List<EnumType> getFilterChildEnumType(String parentEnumItemCode, Integer parentEnumValueId, String orgId, Integer status) throws PaasException {
        return enumConfigAdapter.enumConfigService.getFilterChildEnumType(parentEnumItemCode, parentEnumValueId, orgId, status);
    }

    public List<EnumType> getChildEnumType(String parentEnumItemCode, String orgId, Integer status) throws PaasException {
        return enumConfigAdapter.enumConfigService.getChildEnumType(parentEnumItemCode, orgId, status);
    }

    public EnumValue getEnumValue(String enumItemCode, String enumValueCode, Integer enumValueNum, Integer parentEnumValueId, String orgId) throws PaasException {
        return enumConfigAdapter.enumConfigService.getEnumValue(enumItemCode,enumValueCode,enumValueNum,parentEnumValueId,orgId);
    }
}
