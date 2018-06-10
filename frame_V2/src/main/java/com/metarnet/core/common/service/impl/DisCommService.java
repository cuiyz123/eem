package com.metarnet.core.common.service.impl;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.EnumConfigAdapter;
import com.metarnet.core.common.adapter.WorkflowAdapter;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.*;
import com.metarnet.core.common.service.ICommDispatchService;
import com.metarnet.core.common.utils.BeanUtils;
import com.metarnet.core.common.utils.Constants;
import com.metarnet.core.common.utils.PubFun;
import com.ucloud.paas.agent.PaasException;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import com.unicom.ucloud.workflow.objects.Participant;
import com.unicom.ucloud.workflow.objects.ProcessInstance;
import com.unicom.ucloud.workflow.objects.TaskInstance;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by wangzwty on 2016/4/10/0010.
 */
@Service
public class DisCommService extends GeneOperateService implements ICommDispatchService {

    private Logger logger = LogManager.getLogger();

    @Resource
    private IBaseDAO baseDAO;

    @Override
    public String geneworkOrderNumber(String processCodeOrBusinessType, BaseForm baseForm, UserEntity userEntity) throws ServiceException {
        String speciality = null;
        try {
            if (baseForm != null && baseForm.getSpecialty() != null && !"".equals(baseForm.getSpecialty())) {
                speciality = EnumConfigAdapter.getInstence().getEnumValueById(baseForm.getSpecialty()).getEnumValueName();
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        // 工单类型
        String woType = (StringUtils.isEmpty(speciality) ? "综合" : speciality)
                + (StringUtils.isEmpty(processCodeOrBusinessType) ? Constants.BUSINESS_TYPE : processCodeOrBusinessType);
        // 组织标识
        String orgIdentifier = "";

        OrgShortNameEntity orgShortNameEntity = null;
        try {
            orgShortNameEntity = AAAAAdapter.getInstence().getOrgShortNameEntity(
                    userEntity.getOrgID().intValue());
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        }
        orgIdentifier = orgShortNameEntity == null ? "XXX" : orgShortNameEntity.getOrgShortName();
        // 日期
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
        String periodIdentifier = "【" + simpleDateFormat.format(new Date()) + "】";

        Long sequenceNextValue = findTEomSequence("W", Constants.BUSINESS_TYPE, orgIdentifier,
                periodIdentifier);
        return orgIdentifier + "网调" + periodIdentifier + woType
                + new DecimalFormat("000000").format(sequenceNextValue - 1);
    }

    @Override
    protected String getProcessExtendAttribute(BaseForm baseForm) {
        return Constants.PROCESS_EXTEND_ATTRIBUTE_DIS;
    }

    @Override
    public void beforeAddSubProcess(TaskInstance taskInstance, List<Participant> participants, Object entity, UserEntity userEntity, String params, LinkedHashMap<String, Object> ps) throws ServiceException {

    }

    @Override
    public void initForm(String processCode, BaseForm baseForm, UserEntity userEntity) throws ServiceException {
        super.initForm(baseForm, userEntity);
        String workOrderNumber = geneworkOrderNumber(processCode, baseForm, userEntity);
        baseForm.setDisOrderNumber(workOrderNumber);
        try {
            baseForm.setObjectId(baseDAO.getSequenceNextValue(baseForm.getClass()));
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    public void saveOrUpdateForm(BaseForm baseForm, UserEntity userEntity) throws ServiceException {
        super.saveOrUpdateForm(baseForm, userEntity);
    }

    @Override
    public void processPostStart(BaseForm baseForm, UserEntity userEntity) throws ServiceException {
        deleteFeedbackList(baseForm);
        saveFeedbackList(baseForm, Constants.FDBK_MODEL, userEntity);
    }

    private List<FdbkCommonModel> saveFeedbackList(BaseForm baseForm, String fdbkClassName, UserEntity userEntity) throws ServiceException {

        String mainTransfers = ((DisCommonModel) baseForm).getMainTransfer();
        if (StringUtils.isEmpty(mainTransfers)) {
            logger.warn("没有获取到派发对象");
            return null;
        }

        List<FdbkCommonModel> fdbkList = new ArrayList<FdbkCommonModel>();

        for (String str : mainTransfers.split(",")) {
            FdbkCommonModel feedbackModel = null;
            try {
                feedbackModel = (FdbkCommonModel) Class.forName(fdbkClassName).newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (feedbackModel == null) {
                break;
            }

            feedbackModel.setParentProInstId(baseForm.getProcessInstId());
            feedbackModel.setRootProInstId(baseForm.getRootProInstId());
            feedbackModel.setRootDisId(baseForm.getRootDisId());
            feedbackModel.setProcessInstId(baseForm.getProcessInstId());
            feedbackModel.setDispatchId(baseForm.getObjectId());
            feedbackModel.setParentDisId(baseForm.getParentDisId());
            feedbackModel.setAppOrderNumber(baseForm.getAppOrderNumber());
            feedbackModel.setDisOrderNumber(baseForm.getDisOrderNumber());
            feedbackModel.setApplyId(baseForm.getApplyId());
            feedbackModel.setTheme(baseForm.getTheme());
            feedbackModel.setSpecialty(baseForm.getSpecialty());
            feedbackModel.setReqFdbkTime(baseForm.getReqFdbkTime());
            feedbackModel.setParentDisId(baseForm.getParentDisId());
            feedbackModel.setDisAssignObjectId(str.split(":")[0]);


            String type = str.split(":")[1];
            Map orgInfo = null;
            if (Constants.DISPATCH_OBJECT_TYPE_ORG.equals(type)) {
                //派发到组织
                OrgEntity orgEntity = null;
                try {
                    orgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(str.split(":")[0]);
                } catch (PaasAAAAException e) {
                    e.printStackTrace();
                }
                try {
                    orgInfo = PubFun.getOrgInfoByOrgID(Integer.parseInt(AAAAAdapter.getCompany(Integer.parseInt(orgEntity.getOrgId().toString())).getOrgId().toString()));
                } catch (PaasAAAAException e) {
                    e.printStackTrace();
                }

                // 所属省分
                feedbackModel.setBelongProvinceCode(orgInfo.get(PubFun.BELONGEDPROVINCE) == null ? null : ((Long) orgInfo.get(PubFun.BELONGEDPROVINCE)).intValue());
                //所属省分中文
                feedbackModel.setBelongProvinceName(orgInfo.get(PubFun.BELONGEDPROVINCENAME) == null ? null : ((String) orgInfo.get(PubFun.BELONGEDPROVINCENAME)));
                // 所属地市
                feedbackModel.setBelongCityCode(orgInfo.get(PubFun.BELONGEDCITY) == null ? null : ((Long) orgInfo.get(PubFun.BELONGEDCITY)).intValue());
                // 所属地市中文
                feedbackModel.setBelongCityName(orgInfo.get(PubFun.BELONGEDCITYNAME) == null ? null : ((String) orgInfo.get(PubFun.BELONGEDCITYNAME)));

                feedbackModel.setDisAssignObjectName(orgEntity.getFullOrgName());
            } else if (Constants.DISPATCH_OBJECT_TYPE_MEMBER.equals(type)) {
                UserEntity _userEntity = null;
                try {
                    _userEntity = AAAAAdapter.getInstence().findUserbyUserID(Integer.parseInt(str.split(":")[0]));
                } catch (PaasAAAAException e) {
                    e.printStackTrace();
                }
                try {
                    orgInfo = PubFun.getOrgInfoByOrgID(Integer.parseInt(AAAAAdapter.getCompany(Integer.parseInt(_userEntity.getOrgEntity().getOrgId().toString())).getOrgId().toString()));
                } catch (PaasAAAAException e) {
                    e.printStackTrace();
                }

                // 所属省分
                feedbackModel.setBelongProvinceCode(orgInfo.get(PubFun.BELONGEDPROVINCE) == null ? null : ((Long) orgInfo.get(PubFun.BELONGEDPROVINCE)).intValue());
                //所属省份中文
                feedbackModel.setBelongProvinceName(orgInfo.get(PubFun.BELONGEDPROVINCENAME) == null ? null : ((String) orgInfo.get(PubFun.BELONGEDPROVINCENAME)));
                // 所属地市
                feedbackModel.setBelongCityCode(orgInfo.get(PubFun.BELONGEDCITY) == null ? null : ((Long) orgInfo.get(PubFun.BELONGEDCITY)).intValue());
                //所属地市中文
                feedbackModel.setBelongCityName(orgInfo.get(PubFun.BELONGEDCITYNAME) == null ? null : ((String) orgInfo.get(PubFun.BELONGEDCITYNAME)));

                feedbackModel.setDisAssignObjectName(_userEntity.getTrueName());
            }
            feedbackModel.setWorkOrderStatus("未签收");
            feedbackModel.setDisAssignObjectType(type);
            fdbkList.add(feedbackModel);
        }
        try {
            baseDAO.saveOrUpdateAll(fdbkList, userEntity);
        } catch (DAOException e) {
            e.printStackTrace();
        }

        return fdbkList;
    }

    private void deleteFeedbackList(BaseForm baseForm) {
        try {
            String sql = "delete from "+ Constants.FDBK_TABLE +
                    " where DISPATCH_ID =" + baseForm.getObjectId();
            int del = baseDAO.executeSql(sql);
            logger.info("删除，调度单id：" + baseForm.getObjectId() + "对应的" + del + "条反馈信息");
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String submitTurnToDispatch(DisCommonModel disCommonModel, TaskInstance taskInstance, UserEntity userEntity, String dispatchType) throws ServiceException {
        String result = "";
        DisCommonModel dispatchModel = null;
        if (StringUtils.isBlank(dispatchType)) {
            dispatchType = "s_subTurnToSend";
        }
        try {
            Map<String, Object> relativeData = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(), Arrays.asList(Constants.DIMENSION_MAJOR_CODE), userEntity.getUserName());
            if (StringUtils.isNotBlank(disCommonModel.getMainTransfer())) {
                for (String str : disCommonModel.getMainTransfer().split(",")) {
                    if (StringUtils.isNotBlank(str.split(":")[0]) && Constants.DISPATCH_OBJECT_TYPE_ORG.equals(str.split(":")[1])) {
                        try {
                            OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(str.split(":")[0]);
                            List<Participant> participantList = AAAAAdapter.getInstence().findNextParticipants(Constants.PROCESS_MODEL_NAME, dispatchType, relativeData.get(Constants.DIMENSION_MAJOR_CODE).toString(), orgEntity.getOrgId().toString());
                            if (participantList == null || participantList.size() == 0) {
                                result += "【" + orgEntity.getOrgName() + "，未配置接单人】";
                            }
                        } catch (PaasAAAAException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    List list = baseDAO.find("from " + Constants.DIS_MODEL + " where objectId = ?", new Object[]{disCommonModel.getObjectId()});
                    if (list != null && list.size() > 0) {
                        dispatchModel = (DisCommonModel) list.get(0);
                    } else {
                        try {
                            dispatchModel = (DisCommonModel) Class.forName(Constants.DIS_MODEL).newInstance();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        DisCommonModel tmpDispatch = (DisCommonModel) baseDAO.findEntityByRootProInstId(Constants.DIS_MODEL, taskInstance.getRootProcessInstId());
                        BeanUtils.copyProperties(tmpDispatch, dispatchModel);
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                dispatchModel.setTaskInstId(taskInstance.getTaskInstID());
                dispatchModel.setProcessInstId(taskInstance.getProcessInstID());
                dispatchModel.setMainTransfer(disCommonModel.getMainTransfer());
                dispatchModel.setMainTransferLabel(disCommonModel.getMainTransferLabel());
                dispatchModel.setOperDesc(disCommonModel.getOperDesc());
                dispatchModel.setReqFdbkTime(disCommonModel.getReqFdbkTime());
                dispatchModel.setObjectId(disCommonModel.getObjectId());
                dispatchModel.setOperUserId(userEntity.getUserId());
                dispatchModel.setOperUserPhone(userEntity.getMobilePhone());
                dispatchModel.setOperUserTrueName(userEntity.getTrueName());
                dispatchModel.setOperOrgId(userEntity.getOrgID());
                dispatchModel.setOperOrgName(userEntity.getOrgEntity().getOrgName());
//        BeanUtils.copyProperties(disCommonModel , dispatchModel);
                if (StringUtils.isNotBlank(dispatchType) && StringUtils.isBlank(result)) {
                    try {
                        Map<String, Object> relaDatas = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(), Arrays.asList(Constants.DIMENSION_MAJOR_CODE, Constants.ORG_CODE,Constants.DISPATCH_ID), userEntity.getUserName());
                        relaDatas.put(Constants.DISPATCH_TYPE, dispatchType);
                        relaDatas.put(dispatchType, dispatchType);
                        OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(relaDatas.get(Constants.ORG_CODE).toString());
                        List<Participant> participantList = AAAAAdapter.getInstence().findNextParticipants(Constants.PROCESS_MODEL_NAME, dispatchType, relaDatas.get(Constants.DIMENSION_MAJOR_CODE) + "", orgEntity.getOrgId().toString());
                        if(participantList==null||participantList.size()==0){
                            Participant participant = new Participant();
                            participant.setParticipantID(userEntity.getUserName());
                            participant.setParticipantName("");
                            participant.setParticipantType("1");
                            participantList.add(participant);
                        }
                        relaDatas.put(Constants.FIRST_STEP_USER,
                                "{" +
                                        "'areacode':[]," +
                                        "'majorcode':[]," +
                                        "'orgcode':[]," +
                                        "'productcode':[]," +
                                        "'participant':" + com.alibaba.fastjson.JSONArray.toJSONString(participantList) +
                                        "}"
                        );
                        dispatchModel.setParentDisId(relaDatas.get(Constants.DISPATCH_ID)==null?-1L:Long.valueOf(relaDatas.get(Constants.DISPATCH_ID).toString()));
                        WorkflowAdapter.setRelativeData(taskInstance.getProcessInstID(), relaDatas, userEntity.getUserName());
                    } catch (AdapterException e) {
                        e.printStackTrace();
                    } catch (PaasAAAAException e) {
                        e.printStackTrace();
                    }
                }
                if (StringUtils.isBlank(result)) {
//                    workflowBaseService.setGeneralInfo(dispatchModel, taskInstance, userEntity);
                    saveOrUpdateForm(dispatchModel, userEntity);
                    List<FdbkCommonModel> fdbkList = saveFeedbackList(dispatchModel, Constants.FDBK_MODEL, userEntity);
                    workflowBaseService.addSubPreProcess(taskInstance, null, userEntity, Constants.BUSINESS_CODE);
                    workflowBaseService.submitTask(taskInstance, null, dispatchModel, userEntity, 40050257);
                    workflowBaseService.addSubPostProcess(taskInstance, null, userEntity, Constants.BUSINESS_CODE, fdbkList);
                }
            } else {
                result = "请选择要派发的单位";
            }
        } catch (AdapterException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public DisCommonModel initTurnToDispatch(TaskInstance taskInstance, UserEntity userEntity) throws ServiceException {
        try {
            DisCommonModel disCommonModel = (DisCommonModel) baseDAO.findEntityByProcessInstId(Constants.DIS_MODEL, taskInstance.getProcessInstID());
            if (disCommonModel != null) {
                return disCommonModel;
            } else {
                try {
                    DisCommonModel dispatchModel = (DisCommonModel) Class.forName(Constants.DIS_MODEL).newInstance();
                    disCommonModel = (DisCommonModel) baseDAO.findEntityByRootProInstId(Constants.DIS_MODEL, taskInstance.getRootProcessInstId());
                    BeanUtils.copyProperties(disCommonModel, dispatchModel);
                    dispatchModel.setProcessInstId(taskInstance.getProcessInstID());
                    try {
                        dispatchModel.setObjectId(baseDAO.getSequenceNextValue(dispatchModel.getClass()));
                        saveDraft(dispatchModel, userEntity);
                        return dispatchModel;
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public DisCommonModel showTurnToDispatch(TaskInstance taskInstance, UserEntity userEntity) throws ServiceException {
        try {
            DisCommonModel disCommonModel = (DisCommonModel) baseDAO.findEntityByProcessInstId(Constants.DIS_MODEL, taskInstance.getProcessInstID());
            if (disCommonModel != null) {
                return disCommonModel;
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
