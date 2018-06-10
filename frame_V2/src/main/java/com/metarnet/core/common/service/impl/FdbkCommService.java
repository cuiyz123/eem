package com.metarnet.core.common.service.impl;

import com.metarnet.core.common.adapter.WorkflowAdapter;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.BaseForm;
import com.metarnet.core.common.model.DisCommonModel;
import com.metarnet.core.common.model.FdbkCommonModel;
import com.metarnet.core.common.service.ICommFdbkService;
import com.metarnet.core.common.utils.BeanUtils;
import com.metarnet.core.common.utils.Constants;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.unicom.ucloud.workflow.objects.Participant;
import com.unicom.ucloud.workflow.objects.ProcessInstance;
import com.unicom.ucloud.workflow.objects.TaskInstance;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by wangzwty on 2016/4/10/0010.
 */
@Service
public class FdbkCommService extends GeneOperateService implements ICommFdbkService {

    @Resource
    private IBaseDAO baseDAO;

    @Override
    public String geneworkOrderNumber(String processCodeOrBusinessType, BaseForm baseForm, UserEntity userEntity) throws ServiceException {
        return null;
    }

    @Override
    protected String getProcessExtendAttribute(BaseForm baseForm) {
        return null;
    }

    @Override
    public void processPostStart(BaseForm baseForm, UserEntity userEntity) throws ServiceException {

    }

    @Override
    public void beforeAddSubProcess(TaskInstance taskInstance, List<Participant> participants, Object entity, UserEntity userEntity, String params, LinkedHashMap<String, Object> ps) throws ServiceException {

    }

    public void initForm(FdbkCommonModel fdbkCommonModel, UserEntity userEntity, TaskInstance taskInstance) throws ServiceException {
        try {
            /*Map<String, Object> relativeData = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(),
                    Arrays.asList(Constants.ORG_CODE), userEntity.getUserName());
            ProcessInstance processInstance = WorkflowAdapter.getProcessInstance(userEntity.getUserName(), taskInstance.getProcessInstID());
            DisCommonModel dataDispatchEntity = (DisCommonModel) baseDAO.findEntityByProcessInstId(Constants.DIS_MODEL, processInstance.getParentProcessInstID());
            List<FdbkCommonModel> dataFeedBackEntityList = baseDAO.find("from " + Constants.FDBK_MODEL + " where dispatchId=?", new Object[]{dataDispatchEntity.getObjectId()});
            for (FdbkCommonModel feedBackEntity : dataFeedBackEntityList) {
                if (Constants.DISPATCH_OBJECT_TYPE_MEMBER.equals(feedBackEntity.getDisAssignObjectType()) && feedBackEntity.getDisAssignObjectId().equals(userEntity.getUserId().toString())) {
                    BeanUtils.copyProperties(feedBackEntity, fdbkCommonModel);
                    break;
                }
                if (Constants.DISPATCH_OBJECT_TYPE_ORG.equals(feedBackEntity.getDisAssignObjectType()) && feedBackEntity.getDisAssignObjectId().equals(relativeData.get(Constants.ORG_CODE))) {
                    BeanUtils.copyProperties(feedBackEntity, fdbkCommonModel);
                    break;
                }
            }*/
            Map<String, Object> relativeData = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(),
                    Arrays.asList(Constants.FDBK_ID), userEntity.getUserName());
            if (relativeData.size() > 0) {
                List<FdbkCommonModel> dataFeedBackEntityList = baseDAO.find("from " + Constants.FDBK_MODEL + " where objectId=?", new Object[]{Long.valueOf(relativeData.get(Constants.FDBK_ID).toString())});
                BeanUtils.copyProperties(dataFeedBackEntityList.get(0), fdbkCommonModel);
            }
            initForm("", fdbkCommonModel, userEntity);
        } catch (AdapterException e) {
            e.printStackTrace();
        } catch (DAOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initForm(String processCode, BaseForm baseForm, UserEntity userEntity) throws ServiceException {
        super.initForm(baseForm, userEntity);
    }

    @Override
    public List<FdbkCommonModel> findFdbkListByTaskInst(TaskInstance taskInstance, UserEntity userEntity) throws ServiceException {

        try {
            ProcessInstance processInstance = WorkflowAdapter.getProcessInstance(userEntity.getUserName(), taskInstance.getProcessInstID());
            String parentProInstID = processInstance.getParentProcessInstID();
            if ("0".equals(parentProInstID)) {
                parentProInstID = processInstance.getProcessInstID();
            }
            String hql = "from " + Constants.FDBK_MODEL + " where parentProInstId = ?";
            return baseDAO.find(hql, new Object[]{parentProInstID});
        } catch (AdapterException e) {
            e.printStackTrace();
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<FdbkCommonModel> findFdbkListByParentProInstId(String processInstID, UserEntity userEntity) throws ServiceException {
        try {
            String hql = "from " + Constants.FDBK_MODEL + " where parentProInstId = ?";
            return baseDAO.find(hql, new Object[]{processInstID});
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void submitFdbk(FdbkCommonModel fdbkCommonModel, UserEntity userEntity, TaskInstance taskInstance) {
        Map<String, Object> relativeData = new HashMap<String, Object>();
        relativeData.put("operationAction", "fdbk");
        relativeData.put("processInstID", taskInstance.getProcessInstID());
        relativeData.put("nextStep", "sumAudit");
        try {
            WorkflowAdapter.setRelativeData(taskInstance.getProcessInstID(), relativeData, userEntity.getUserName());
            fdbkCommonModel.setWorkOrderStatus("已反馈");
            submit(taskInstance, fdbkCommonModel, "", Constants.ROUTE_START_SUBMIT, userEntity, 40050254);
        } catch (AdapterException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    public void submitFdbk(FdbkCommonModel fdbkCommonModel, UserEntity userEntity, TaskInstance taskInstance, Map<String, Object> relativeData) {
        relativeData.put("operationAction", "fdbk");
        relativeData.put("processInstID", taskInstance.getProcessInstID());
        try {
            WorkflowAdapter.setRelativeData(taskInstance.getProcessInstID(), relativeData, userEntity.getUserName());
            fdbkCommonModel.setWorkOrderStatus("已反馈");
            submit(taskInstance, fdbkCommonModel, "", Constants.ROUTE_START_SUBMIT, userEntity, 40050254);
        } catch (AdapterException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }
}
