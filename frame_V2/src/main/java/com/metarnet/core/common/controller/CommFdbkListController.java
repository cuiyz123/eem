package com.metarnet.core.common.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.metarnet.core.common.adapter.WorkflowAdapter;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.core.common.model.FdbkCommonModel;
import com.metarnet.core.common.model.GeneralInfoModel;
import com.metarnet.core.common.service.ICommFdbkService;
import com.metarnet.core.common.service.IDraftService;
import com.metarnet.core.common.service.IWorkflowBaseService;
import com.metarnet.core.common.utils.Constants;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.unicom.ucloud.workflow.filters.TaskFilter;

import com.unicom.ucloud.workflow.objects.ProcessInstance;
import com.unicom.ucloud.workflow.objects.ActivityInstance;
import com.unicom.ucloud.workflow.objects.ProcessInstance;
import com.unicom.ucloud.workflow.objects.TaskInstance;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 公共反馈列表审核
 *
 * @author zwwang
 */
@Controller
public class CommFdbkListController extends BaseController {


    @Resource(name = "fdbkCommService")
    private ICommFdbkService commFdbkService;
    @Resource
    private IBaseDAO dao;
    @Resource
    private IWorkflowBaseService iWorkflowBaseService;

    @RequestMapping(value = "/commFdbkListController.do")
    private ModelAndView init(HttpServletResponse response, HttpServletRequest request, TaskInstance taskInstance) throws UIException {
        /*List<FdbkCommonModel> fdbkList = null;
        try {
            fdbkList = commFdbkService.findFdbkListByTaskInst(taskInstance , getUserEntity(request));
            if(fdbkList != null && fdbkList.size() > 0){
                TaskFilter taskFilter = new TaskFilter();
//                taskFilter.setTaskType(null);
                taskFilter.setActivityDefID_op(taskInstance.getActivityDefID());
                for(int i = 0 ; i < fdbkList.size() ; i++){
                    FdbkCommonModel fdbkCommonModel = fdbkList.get(i);
                    taskFilter.setProcessInstID(fdbkCommonModel.getProcessInstId());
                    List list = null;
                    try {
                        list = WorkflowAdapter.getMyWaitingTasks(taskFilter, getUserEntity(request).getUserName());
                    } catch (AdapterException e) {
                        e.printStackTrace();
                    }
                    if(list == null || list.size() == 0){
                        fdbkCommonModel.setWorkOrderStatus("未反馈");
                    }
                }
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        }*/
        return new ModelAndView(new InternalResourceView("base/page/feedbackList.jsp"));
    }

    @RequestMapping(value = "/commFdbkListController.do", params = "method=list")
    private void list(HttpServletResponse response, HttpServletRequest request, TaskInstance taskInstance) throws UIException {
        UserEntity userEntity = getUserEntity(request);
        List<FdbkCommonModel> fdbkList = null;
        try {
            fdbkList = commFdbkService.findFdbkListByTaskInst(taskInstance, getUserEntity(request));
            if (fdbkList != null && fdbkList.size() > 0) {
                TaskFilter taskFilter = new TaskFilter();
//                taskFilter.setTaskType(null);
                taskFilter.setActivityDefID_op(taskInstance.getActivityDefID());
                Comparator<GeneralInfoModel> comparator = new Comparator<GeneralInfoModel>() {
                    public int compare(GeneralInfoModel g1, GeneralInfoModel g2) {
                        return g1.getObjectId() > g2.getObjectId() ? -1 : 1;
                    }
                };
                for (int i = 0; i < fdbkList.size(); i++) {
                    FdbkCommonModel fdbkCommonModel = fdbkList.get(i);
                    taskFilter.setProcessInstID(fdbkCommonModel.getProcessInstId());
                    try {
                        List<TaskInstance> list = null;
                        try {
                            list = WorkflowAdapter.getMyWaitingTasks(taskFilter, getUserEntity(request).getUserName());
                        } catch (AdapterException e) {
                            e.printStackTrace();
                        }
                        if (list.size() > 0 && list.get(0).getActivityDefID().equals("sumAudit")) {
                            fdbkCommonModel.setAttribute1("Y");
                        } else {
                            List<GeneralInfoModel> generalInfoModelList = dao.find("from GeneralInfoModel gm where gm.activityInstName='汇总审核' and gm.processInstId='" + fdbkCommonModel.getProcessInstId() + "'");
                            Collections.sort(generalInfoModelList, comparator);
                            if (generalInfoModelList.size() > 0) {
                                if (Constants.Y.equals(generalInfoModelList.get(0).getProcessingStatus())) {
                                    fdbkCommonModel.setWorkOrderStatus("已汇总审核通过");
                                } else {
                                    fdbkCommonModel.setWorkOrderStatus("已汇总审核驳回");
                                }
                                fdbkCommonModel.setAttribute1(generalInfoModelList.get(0).getProcessingStatus());
                                fdbkCommonModel.setAttribute2(generalInfoModelList.get(0).getOperDesc());
                            }else{
                                fdbkCommonModel.setWorkOrderStatus("未反馈");
                            }
                        }
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        SerializeConfig ser = new SerializeConfig();
        ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
        endHandle(request, response, JSON.toJSONString(fdbkList, ser, SerializerFeature.WriteNullListAsEmpty), "", false);
    }

    @RequestMapping(value = "/commFdbkListController.do", params = "method=getFeedbackListByDispatch")
    public void getFeedbackListByDispatch(HttpServletResponse response, HttpServletRequest request, TaskInstance taskInstance) throws UIException {
        List<FdbkCommonModel> fdbkList = null;
        try {
//            List<ProcessInstance> subProcessInstances = WorkflowAdapter.getSubProcessInstance(getUserEntity(request).getUserName(), taskInstance.getProcessInstID());
//            if (subProcessInstances != null && subProcessInstances.size() > 0) {
//                Boolean isFDBK = false;
//                for (int i = 0; i < subProcessInstances.size(); i++) {
//                    ProcessInstance processInstance = subProcessInstances.get(i);
//                    //只保留反馈流程
//                    if (processInstance.getProcessModelName().equals(Constants.FDBK_PROCESS_MODEL)) {
//                        isFDBK = true;
//                    }
//                }
//                if (isFDBK) {
                    fdbkList = commFdbkService.findFdbkListByParentProInstId(taskInstance.getProcessInstID(), getUserEntity(request));
                    if (fdbkList != null && fdbkList.size() > 0) {
                        TaskFilter taskFilter = new TaskFilter();
                        taskFilter.setActivityDefID_op("sumAudit");
                        Comparator<GeneralInfoModel> comparator = new Comparator<GeneralInfoModel>() {
                            public int compare(GeneralInfoModel g1, GeneralInfoModel g2) {
                                return g1.getObjectId() > g2.getObjectId() ? -1 : 1;
                            }
                        };
                        for (int i = 0; i < fdbkList.size(); i++) {
                            FdbkCommonModel fdbkCommonModel = fdbkList.get(i);
                            if (!fdbkCommonModel.getProcessInstId().equals(taskInstance.getProcessInstID())) {
                                List<FdbkCommonModel> fdbkLists = commFdbkService.findFdbkListByParentProInstId(fdbkCommonModel.getProcessInstId(), getUserEntity(request));
                                if (fdbkLists != null && fdbkLists.size() > 0) {
                                    fdbkCommonModel.setAttribute5("Y");
                                }
                            }
                            /*taskFilter.setProcessInstID(fdbkCommonModel.getProcessInstId());
                            try {
                                List<GeneralInfoModel> generalInfoModelList = dao.find("from GeneralInfoModel gm where gm.activityInstName='汇总审核' and gm.processInstId='" + fdbkCommonModel.getProcessInstId() + "'");
                                Collections.sort(generalInfoModelList, comparator);
                                if (generalInfoModelList.size() > 0) {
                                    if (Constants.Y.equals(generalInfoModelList.get(0).getProcessingStatus())) {
                                        fdbkCommonModel.setWorkOrderStatus("已汇总审核通过");
                                    } else {
                                        fdbkCommonModel.setWorkOrderStatus("已汇总审核驳回");
                                    }
                                    fdbkCommonModel.setAttribute1(generalInfoModelList.get(0).getProcessingStatus());
                                    fdbkCommonModel.setAttribute2(generalInfoModelList.get(0).getOperDesc());
                                }else{
                                    fdbkCommonModel.setWorkOrderStatus("未反馈");
                                }
                            } catch (DAOException e) {
                                e.printStackTrace();
                            }*/
                        }
                    }
//                }
//            }
        } catch (ServiceException e) {
            e.printStackTrace();
        }
//        catch (AdapterException e) {
//            e.printStackTrace();
//        }
        SerializeConfig ser = new SerializeConfig();
        ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
        endHandle(request, response, JSON.toJSONString(fdbkList, ser, SerializerFeature.WriteNullListAsEmpty), "", false);
    }
}
