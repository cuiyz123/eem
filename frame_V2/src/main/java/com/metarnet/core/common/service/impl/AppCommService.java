package com.metarnet.core.common.service.impl;

import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.BaseForm;
import com.metarnet.core.common.model.OrgShortNameEntity;
import com.metarnet.core.common.model.OrgShortNameStructure;
import com.metarnet.core.common.utils.Constants;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.unicom.ucloud.workflow.objects.Participant;
import com.unicom.ucloud.workflow.objects.TaskInstance;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by wangzwty on 2016/4/10/0010.
 */
public class AppCommService extends GeneOperateService {
    @Resource
    private IBaseDAO baseDAO;

    @Override
    public String geneworkOrderNumber(String processCodeOrBusinessType, BaseForm baseForm, UserEntity userEntity) throws ServiceException {
        try {
            OrgShortNameStructure orgBrevityStructure = AAAAAdapter.getInstence().findCompanysBrevityByOrgId(
                    userEntity.getOrgID().intValue());
            // 工单类型
            String woType = Constants.MODEL_CODE + processCodeOrBusinessType;
            // 组织标识
            String orgIdentifier = "";
            if (orgBrevityStructure.getCityCompany() != null) {
                orgIdentifier = orgBrevityStructure.getProvinceCompany().getOrgShortName() + "/"
                        + orgBrevityStructure.getCityCompany().getOrgShortName();
            } else {
                OrgShortNameEntity orgShortNameEntity = AAAAAdapter.getInstence().getOrgShortNameEntity(
                        userEntity.getOrgID().intValue());
                orgIdentifier = ((orgShortNameEntity == null) ? "XXX" : orgShortNameEntity.getOrgShortName());
            }

            // 日期
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
            String periodIdentifier = simpleDateFormat.format(new Date());
            Long sequenceNextValue = findTEomSequence("W", woType, orgIdentifier, periodIdentifier);

            if ("".equals(woType) || woType == null || "null".equals(woType)) {
                woType = "XXX";
            }
            String num = orgIdentifier + "-" + woType + "-" + periodIdentifier + "-" + new DecimalFormat("000000").format(sequenceNextValue - 1);
            return num;

        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    protected String getProcessExtendAttribute(BaseForm baseForm) {
        return Constants.PROCESS_EXTEND_ATTRIBUTE_APP;
    }

    @Override
    public void processPostStart(BaseForm baseForm, UserEntity userEntity) throws ServiceException {

    }

    @Override
    public void beforeAddSubProcess(TaskInstance taskInstance, List<Participant> participants, Object entity, UserEntity userEntity, String params, LinkedHashMap<String, Object> ps) throws ServiceException {

    }

    @Override
    public void initForm(String processCode, BaseForm baseForm, UserEntity userEntity) throws ServiceException {
        super.initForm(baseForm, userEntity);
        String workOrderNumber = geneworkOrderNumber(processCode, baseForm, userEntity);
        baseForm.setAppOrderNumber(workOrderNumber);
        try {
            baseForm.setObjectId(baseDAO.getSequenceNextValue(baseForm.getClass()));
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

}
