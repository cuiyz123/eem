package com.metarnet.core.common.service;


import com.metarnet.core.common.model.EnumType;
import com.metarnet.core.common.model.EnumValue;
import com.ucloud.paas.agent.PaasException;

import java.util.List;

/**
 * Created by Administrator on 2015/7/13.
 */
public interface IEnumConfigService {

    public EnumValue getEnumValueById(Integer enumValueId) throws PaasException;

    public EnumType getEnumType(String enumItemCode, String orgId, Integer status) throws PaasException;

    public List<EnumType> getFilterChildEnumType(String parentEnumItemCode, Integer parentEnumValueId, String orgId, Integer status) throws PaasException;

    public List<EnumType> getChildEnumType(String parentEnumItemCode, String orgId, Integer status) throws PaasException;

    public EnumValue getEnumValue(String enumItemCode, String enumValueCode, Integer enumValueNum, Integer parentEnumValueId, String orgId) throws PaasException;
}
