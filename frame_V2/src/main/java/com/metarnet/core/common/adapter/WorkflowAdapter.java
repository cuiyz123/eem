package com.metarnet.core.common.adapter;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.eos.das.entity.DASManager;
import com.eos.das.entity.ExpressionHelper;
import com.eos.das.entity.IDASCriteria;
import com.eos.workflow.api.BPSServiceClientFactory;
import com.eos.workflow.api.IBPSServiceClient;
import com.eos.workflow.data.NotificationOption;
import com.eos.workflow.data.WFNotificationInst;
import com.eos.workflow.data.WFWorkItem;
import com.eos.workflow.helper.ResultList;
import com.eos.workflow.omservice.WFParticipant;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.model.ProcessParameterModel;
import com.metarnet.core.common.utils.Constants;
import com.metarnet.core.common.utils.HttpClientUtil;
import com.metarnet.core.common.workflow.WorkFlowDataConvertor;
import com.primeton.ucloud.workflow.factory.BPMServiceFactory;
import com.primeton.workflow.api.PageCond;
import com.primeton.workflow.api.WFServiceException;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import com.unicom.ucloud.workflow.exceptions.WFException;
import com.unicom.ucloud.workflow.filters.NotificationFilter;
import com.unicom.ucloud.workflow.filters.TaskFilter;
import com.unicom.ucloud.workflow.interfaces.WorkflowObjectInterface;
import com.unicom.ucloud.workflow.objects.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jtwu
 * Date: 12-11-28
 * Time: 下午4:47
 * 工作流引擎代理，负责封装工作流引擎提供的接口。
 */
public class WorkflowAdapter {
    private static Logger logger = Logger.getLogger(WorkflowAdapter.class);
    private static BPMServiceFactory factory = BPMServiceFactory.getInstance();
//    public static ThreadLocal<Map<String,Map<String,Object>>> relativeDataMap=new ThreadLocal<Map<String,Map<String,Object>>>();
    public static Map<String,Map<String,Object>> relativeDataMap;

    /**
     * 启动流程
     *
     * @param accountId          当前登录ID
     * @param processModelID     流程模型ID
     * @param participant        参与者
     * @param bizModleParams     业务参数
     * @param processModelParams 流程模型对象参数
     * @return
     * @throws AdapterException
     *
     */
    public static String startProcess(String accountId, String processModelID, Participant participant, Map<String,
            Object> bizModleParams, ProcessModelParams processModelParams) throws AdapterException {
        try {
            processModelParams.setProcessInstName(processModelID);
            processModelParams.setProcessModelName(processModelID);
            return getWorkflowService(accountId).startProcess(processModelID, processModelParams, bizModleParams,
                    participant, null);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    public static void main(String args[]){
       // BPMServiceFactory.getInstance();
        Participant participant = new   Participant();
        participant.setParticipantType("1");
        participant.setParticipantID("caokj");
        Map<String,Object> bizModleParams = new HashMap<String,Object>();
        ProcessModelParams processModelParams = new ProcessModelParams();
      //  bizModleParams
        Map<String, Object> bizMap = new HashMap<String, Object>();
        bizMap.put("bizTableName", "Job");// bizTableName 存 表名
        bizMap.put("jobTitle", "123");// 存long类型属性值
        bizMap.put("jobID", "123");// 存字符串类型属性值
        bizMap.put("jobCode", "345");

        bizMap.put("shard",1);// 存字符串类型属性值
        bizMap.put("businessId", "");// 存字符串类型属性值
        try {
            startProcess("caokj","com.metarnet.eomcm.deploy",null,bizMap,processModelParams);
        } catch (AdapterException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * 查询待办
     *
     * @param taskFilter 待办查询实体
     * @param userName  当前登录ID
     * @return 待办列表
     * @throws AdapterException
     *
     */

    public static List<TaskInstance> getMyWaitingTasks(TaskFilter taskFilter, String userName) throws AdapterException {
        logger.info(userName);
        try {
            if (taskFilter.getPageCondition() == null) {
                PageCondition pageCon = new PageCondition();
                pageCon.setLength(1);
                pageCon.setBegin(0); // 注意，这个是记录的起始记录号， 第一次查询默认为0
                pageCon.setIsCount(false);
                taskFilter.setPageCondition(pageCon);
            }
            List<TaskInstance> list = getWorkflowService(userName).getMyWaitingTasks(taskFilter);
            return list;
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 通过流程实例获取代办，用于刚起流程时查询代办
     *
     * @param processInstID
     * @param accountId
     * @return
     * @throws AdapterException
     *
     */
    public static List<TaskInstance> queryNextWorkItemsByProcessInstID(String processInstID, String accountId) throws AdapterException {
        try {
            logger.info("queryNextWorkItemsByProcessInstID===" + processInstID);
            List<TaskInstance> list = getWorkflowService(accountId).queryNextWorkItemsByProcessInstID(Long.valueOf(processInstID));
            return list;
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }


    /**
     * 查询已办
     *
     * @param taskFilter 待办查询实体
     * @param accountId  当前登录ID
     * @return 待办列表
     * @throws AdapterException
     *
     */

    public static List<TaskInstance> getMyCompletedTasks(TaskFilter taskFilter,
                                                         String accountId) throws AdapterException {
        try {
            return getWorkflowService(accountId).getMyCompletedTasks(taskFilter);
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 查询已办--基于同一工单流水号、活动环节的合并
     *
     * @param accountId
     * @param taskFilter
     * @throws AdapterException
     *
     */
    public static List<TaskInstance> getMyCompletedTasksDistinctJobId(TaskFilter taskFilter, String accountId) throws AdapterException {
        try {
            return getWorkflowService(accountId).getMyCompletedTasksDistinctJobId(taskFilter);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 查询已办--基于同于流程实例中相同处理人的已办合并
     *
     * @param accountId
     * @param taskFilter
     * @throws AdapterException
     *
     */
    public static List<TaskInstance> getMyCompletedTasksDistinctProinstanceId(TaskFilter taskFilter, String accountId)
            throws AdapterException {
        try {
            return getWorkflowService(accountId).getMyCompletedTasksDistinctProinstanceId(taskFilter);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 根据活动实例ID获取任务实例ID
     *
     * @param accountId      用户ID
     * @param activityInstID 活动实例ID
     * @return
     * @throws AdapterException
     *
     */
    public static List<TaskInstance> getTaskInstancesByActivityID(String accountId,
                                                                  String activityInstID) throws AdapterException {
        try {
            return getWorkflowService(accountId).getTaskInstancesByActivityID(activityInstID);
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 提交待办
     *
     * @param accountId    当前登录ID
     * @param taskInstance 任务实例信息
     * @param participants 下一步执行人列表
     * @throws com.unicom.ucloud.workflow.exceptions.WFException
     *
     */
    public static void submitTask(String accountId, TaskInstance taskInstance, List<Participant> participants) throws
            AdapterException {
        try {
            if (participants != null && participants.size() == 0) {
                participants = null;
            }
            getWorkflowService(accountId).submitTask(taskInstance, participants);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 传递数据
     *
     * @param processInstID 流程实例ID
     * @param relaDatas     设置更新内容
     */
    public static void setRelativeData(String processInstID, Map<String, Object> relaDatas, String accountId) throws AdapterException {
        try {
            getWorkflowService(accountId).setRelativeData(processInstID, relaDatas);
            if(relativeDataMap==null){
                relativeDataMap = new HashMap<String, Map<String, Object>>();
            }
            if(relativeDataMap.get(processInstID) == null)
                relativeDataMap.put(processInstID,new HashMap<String, Object>());

            relativeDataMap.get(processInstID).putAll(relaDatas);
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 获取数据
     *
     * @param processInstID 流程实例ID
     * @param keys          关键字
     */
    public static Map<String, Object> getRelativeData(String processInstID, List<String> keys,
                                                      String accountId) throws AdapterException {
        try {
            Map<String, Object> rlds = null;
            if (relativeDataMap == null) {
                relativeDataMap = new HashMap<String, Map<String, Object>>();
            }
            /*if (relativeDataMap.containsKey(processInstID)) {
                rlds = new HashMap<String, Object>();
                //先从缓存中取；
                Map<String, Object> cds = relativeDataMap.get(processInstID);
                List<String> ks = new ArrayList<String>();
                for (String k : keys) {
                    if (cds.containsKey(k)&&cds.get(k)!=null) {//空值不返回
                        rlds.put(k, cds.get(k));
                    } else {
                        ks.add(k);
                    }
                }
                if (ks.size() > 0) {
                    Map<String, Object> rs = getWorkflowService(accountId).getRelativeData(processInstID, ks);
                    for (String s : ks) {
                        //目的是将为null的值也缓存
                        cds.put(s, rs.get(s));
                    }
                    rlds.putAll(rs);
                }
            } else {*/
                rlds = getWorkflowService(accountId).getRelativeData(processInstID, keys);
                Map hashMap=relativeDataMap.get(processInstID);
                if (hashMap == null){
                    hashMap =new HashMap<String, Object>();
                    relativeDataMap.put(processInstID, hashMap);
                }
                for (String s : keys) {
                    hashMap.put(s, rlds.get(s));
                }
//            }
            return rlds;
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 查询当前流程模板对应的所有流程环节信息；
     *
     * @param processModelID
     * @param accountId
     * @return
     * @throws AdapterException
     *
     */
    public static List<ActivityDef> getActivitDefLists(String processModelID,
                                                       String accountId) throws AdapterException {
        try {
            return getWorkflowService(accountId).getActivitDefLists(processModelID);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 获取流程实例流转过的活动
     *
     * @param accountId     当前用户
     * @param processInstID 流程实例ID
     * @return
     * @throws AdapterException
     *
     */
    public static List<ActivityInstance> getActivityInstances(String accountId, String processInstID)
            throws AdapterException {
        try {
            return getWorkflowService(accountId).getActivityInstances(processInstID);
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 转办
     *
     * @param accountId      当前登录ID
     * @param taskInstanceId 任务实例ID
     * @param participants   下一步执行人列表
     */
    public static void forwardTask(String accountId, String taskInstanceId,
                                   List<Participant> participants) throws AdapterException {
        try {
            getWorkflowService(accountId).forwardTask(taskInstanceId, participants);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }


    /**
     * 协办
     *
     * @param accountId      当前登录ID
     * @param taskInstanceId 任务实例ID
     * @param participants   下一步执行人列表
     */
    public static void delegateTask(String accountId, String taskInstanceId,
                                    List<Participant> participants) throws AdapterException {
        try {
            getWorkflowService(accountId).delegateTask(taskInstanceId, participants);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 获取流程api接口
     *
     * @param accountId 当前用户
     * @return
     * @throws com.unicom.ucloud.workflow.exceptions.WFException
     *
     */
    public static WorkflowObjectInterface getWorkflowService(String accountId) throws WFException {
        return factory.getWorkflowService(accountId, Constants.APP_ID, null);
    }

    /**
     * 业务描述：撤回任务，在对方没有提交之前进行撤回
     *
     * @param currentActivityInstId 当前活动实例ID
     * @param targetActivityInstId  需要回退/取回的目标活动实例ID
     */
    public static void backActivity(String accountId, String currentActivityInstId,
                                    String targetActivityInstId) throws AdapterException {
        try {
            getWorkflowService(accountId).backActivity(currentActivityInstId, targetActivityInstId);
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 更新工单编号
     *
     * @param accountId
     * @throws AdapterException
     *
     */
    public static void updateJobCodeInfo(String accountId, String processInstID,
                                         String jobCode) throws AdapterException {
        try {
            getWorkflowService(accountId).updateJobCodeInfo(processInstID, jobCode);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }


    /**
     * 根据任务实例ID获取当前实例扩展信息
     *
     * @param accountId      当前登录ID
     * @param processModelID 流程定义ID
     * @param activityDefID  环节定义ID
     * @return
     * @throws AdapterException
     *
     */
    public static String getActivityExtendAttributes(String accountId, String processModelID,
                                                     String activityDefID) throws AdapterException {
        try {
            logger.debug("getActivityExtendAttributes parameters: accountId = " + accountId + " processModelID=" +
                    processModelID + " activityDefID=" + activityDefID);
            return getWorkflowService(accountId).getActivityExtendAttributes(processModelID, activityDefID);
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 根据流程实例ID获取流程对象
     *
     * @param processInstId 流程实例ID
     * @param accountId     当前登录ID
     * @return
     * @throws AdapterException
     *
     */
    public static ProcessInstance getProcessInstance(String accountId, String processInstId) throws AdapterException {
        try {
            ProcessInstance processInstance = getWorkflowService(accountId).getProcessInstance(processInstId);
            return processInstance;
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }


//

    /**
     * 根据流程模型ID获取第一个流程环节ID
     *
     * @param processModelID 流程模型ID
     * @param accountId      当前登录ID
     * @return
     */
    public static ActivityDef getStartActivity(String accountId, String processModelID) throws AdapterException {
        throw new AdapterException("");
//        try {
//            return getWorkflowService(accountId).getStartActivity(processModelID);
//        } catch (WFException e) {
//            throw new AdapterException(e);
//        }
    }

    /**
     * 获取流程实例的子流程
     *
     * @param accountId     当前登录ID
     * @param processInstId 流程实例ID
     * @return
     * @throws AdapterException
     *
     */
    public static List<ProcessInstance> getSubProcessInstance(String accountId,
                                                              String processInstId) throws AdapterException {
        try {
            return getWorkflowService(accountId).getSubProcessInstance(processInstId);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    public static TaskInstance getTaskInstanceObject(String accountId, String taskInstId) throws AdapterException {
        try {
            return getWorkflowService(accountId).getTaskInstanceObject(taskInstId);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 查询流程模板
     *
     * @param accountId
     * @return
     * @throws AdapterException
     *
     */
    public static List<ProcessModel> getProcessModeLists(String accountId) throws AdapterException {
        try {
            return getWorkflowService(accountId).getProcessModeLists(null);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 根据传入的processParameter 分解组装processParameter对象集合
     *
     * @param areacode    区域名称
     * @param orgcode     组织名称
     * @param majorcode   专业名称
     * @param productcode 产品名称
     * @return
     */
    public static String[] resolveProcsParameter(String areacode, String orgcode, String majorcode,
                                                 String productcode) throws AdapterException {

        List<ProcessParameterModel> processParameterModelList = new ArrayList<ProcessParameterModel>();
        try {
            if (null == areacode) {
                areacode = "";
            }
            if (null == orgcode) {
                orgcode = "";
            }
            if (null == majorcode) {
                majorcode = "";
            }
            if (null == productcode) {
                productcode = "";
            }

            String[] areacode_ = areacode.split(",");
            String[] orgcode_ = orgcode.split(",");
            String[] majorcode_ = majorcode.split(",");
            String[] productcode_ = productcode.split(",");

            for (int a = 0; a < areacode_.length; a++) {
                for (int o = 0; o < orgcode_.length; o++) {
                    if (StringUtils.isBlank(orgcode_[o])) {
                        continue;
                    }
                    for (int m = 0; m < majorcode_.length; m++) {
                        for (int p = 0; p < productcode_.length; p++) {
                            ProcessParameterModel processParameterModel = new ProcessParameterModel();
                            Set<String> orgcodeSet = new HashSet<String>();//
                            orgcodeSet.add(orgcode_[o]);
                            processParameterModel.setOrgcode(orgcodeSet); //组织名称
                            Set<String> areacodeSet = new HashSet<String>();//
                            areacodeSet.add(areacode_[a]);
                            processParameterModel.setAreacode(areacodeSet);
                            Set<String> majorcodeSet = new HashSet<String>();//
                            majorcodeSet.add(majorcode_[m]);
                            processParameterModel.setMajorcode(majorcodeSet);
                            Set<String> productcodeSet = new HashSet<String>();//
                            productcodeSet.add(productcode_[p]);
                            processParameterModel.setProductcode(productcodeSet);
                            processParameterModelList.add(processParameterModel);
                        }
                    }
                }
            }

            String[] processParameterJson = new String[processParameterModelList.size()];
            for (int pa = 0; pa < processParameterModelList.size(); pa++) {
                processParameterJson[pa] = JSONArray.toJSONString(processParameterModelList.get(pa));
            }
            return processParameterJson;
        } catch (Exception e) {
            throw new AdapterException(e);
        }
        //return JSONArray.toJSONString(processParameterModelList);
    }

    /**
     * 查询待阅
     *
     * @param notificationFilter 通知查询过滤条件
     * @param accountId          当前登录ID
     * @return 待阅信息列表
     * @throws AdapterException
     *
     */
    public static List<com.metarnet.core.common.workflow.NotificationInstance> getMyUnreadNotifications(NotificationFilter notificationFilter,
                                                                      String accountId) throws AdapterException {
        try {
//            return getWorkflowService(accountId).getMyUnreadNotifications(notificationFilter);
            WFNotificationInst.State state = WFNotificationInst.State.UNVIEWED;
            return getNotifications(notificationFilter, state , accountId);
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 查询已阅
     *
     * @param notificationFilter 通知查询过滤条件
     * @param accountId          当前登录ID
     * @return 已阅信息列表
     * @throws AdapterException
     *
     */
    public static List<com.metarnet.core.common.workflow.NotificationInstance> getMyReadNotifications(NotificationFilter notificationFilter,
                                                                    String accountId) throws AdapterException {
        try {
//            return getWorkflowService(accountId).getMyReadNotifications(notificationFilter);
            WFNotificationInst.State state = WFNotificationInst.State.VIEWED;
            return getNotifications(notificationFilter, state , accountId);
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 更新待阅为已阅读
     *
     * @param notificationInstId 通知对象实例IDt
     * @param accountId          当前登录ID
     * @return 已阅信息列表
     * @throws AdapterException
     *
     */
    public static void setNotificationToRead(String notificationInstId, String accountId) throws AdapterException {
//        try {
//            getWorkflowService(accountId).setNotificationToRead(notificationInstId);
//        } catch (Exception e) {
//            throw new AdapterException(e);
//        }
        IBPSServiceClient client = null;
        try {
            client = BPSServiceClientFactory.getDefaultClient();
            client.getNotificationManager().confirmNotification(Long.parseLong(notificationInstId));
        } catch (WFServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private static List<com.metarnet.core.common.workflow.NotificationInstance> getNotifications(NotificationFilter notificationfilter, WFNotificationInst.State state , String accountId) throws AdapterException
    {
        List notis = null;
        try {
            IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();
            PageCond cond = null;
            if(notificationfilter != null){
                PageCondition pc = notificationfilter.getPageCondition();
                if(pc != null){
                    PageCond newPC = new PageCond();
                    newPC.setBegin(pc.getBegin());
                    newPC.setBeginIndex(pc.getBegin());
                    newPC.setLength(pc.getLength());
                    newPC.setIsCount(pc.getIsCount() == null ? false : pc.getIsCount().booleanValue());
                    if (pc.getCount() != 0)
                        newPC.setCount(pc.getCount());
                    newPC.setCurrentPage(pc.getCurrentPage());
                    newPC.setFirst(pc.getIsFirst() == null ? false : pc.getIsFirst().booleanValue());
                    newPC.setLast(pc.getIsLast() == null ? false : pc.getIsLast().booleanValue());
                    newPC.setTotalPage(pc.getTotalPage());

                    cond = newPC;
                } else {
                    cond = new PageCond(2147483647);
                }
            } else {
                cond = new PageCond(2147483647);
            }

            IDASCriteria AccountCriteria = DASManager.createCriteria("com.eos.workflow.data.WFNotificationInst");

            String ProcessModelID = notificationfilter.getProcessModelID();
            if ((ProcessModelID != null) && (ProcessModelID.length() != 0)) {
                AccountCriteria.add(ExpressionHelper.eq("procDefID", ProcessModelID));
            }
            String processModelName = notificationfilter.getProcessModelName();

            if ((processModelName != null) && (processModelName.length() != 0)) {
                if (processModelName.contains(","))
                    AccountCriteria.add(ExpressionHelper.in("procDefName", processModelName.split(",")));
                else {
                    AccountCriteria.add(ExpressionHelper.eq("procDefName", processModelName));
                }

            }

            String appID = notificationfilter.getAppID();
            if ((appID != null) && (appID.length() != 0)) {
                throw new AdapterException("appID属性不支持查询");
            }
            String notificationInstID = notificationfilter.getNotificationInstID();
            if ((notificationInstID != null) && (notificationInstID.length() != 0)) {
                AccountCriteria.add(ExpressionHelper.eq("notificationID", notificationInstID));
            }
            String processInstID = notificationfilter.getProcessInstID();
            if ((processInstID != null) && (processInstID.length() != 0)) {
                AccountCriteria.add(ExpressionHelper.eq("procInstID", processInstID));
            }

            String activityID = notificationfilter.getActivityID();
            if ((activityID != null) && (activityID.length() != 0)) {
                AccountCriteria.add(ExpressionHelper.eq("actInstID", activityID));
            }
            String ActivityName = notificationfilter.getActivityName();
            if ((ActivityName != null) && (ActivityName.length() != 0)) {
                AccountCriteria.add(ExpressionHelper.like("actInstName", "%" + ActivityName + "%"));
            }
            String JobTitle = notificationfilter.getJobTitle();
            if ((JobTitle != null) && (JobTitle.length() != 0)) {
                AccountCriteria.add(ExpressionHelper.like("title", "%" + JobTitle + "%"));
            }
            String JobID = notificationfilter.getJobID();
            if ((JobID != null) && (JobID.length() != 0)) {
                AccountCriteria.add(ExpressionHelper.like("message", "%" + JobID + "%"));
                AccountCriteria.desc("message");
            }
            else {
                AccountCriteria.desc("createTime");
            }
            String SenderType = notificationfilter.getSenderType();
            if ((SenderType != null) && (SenderType.length() != 0)) {
                throw new AdapterException("SenderType属性不支持查询");
            }
            String Sender = notificationfilter.getSenderID();
            if ((Sender != null) && (Sender.length() != 0)) {
                AccountCriteria.add(ExpressionHelper.eq("sender", Sender));
            }

//            String currentUserID = DataContextManager.current().getMUODataContext().getUserObject().getUserId();
//            if ((currentUserID != null) && (!"".equals(currentUserID)))
                AccountCriteria.add(ExpressionHelper.eq("recipient", "P{" + accountId + "}"));
//            else {
//                throw new AdapterException("未获取到消息处理人");
//            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date beginStartDate = notificationfilter.getBeginDeliveryDate();

            Date endStartDate = notificationfilter.getEndDeliveryDate();
            if ((beginStartDate != null) && (endStartDate != null)) {
                AccountCriteria.add(ExpressionHelper.between("createTime", sdf.format(beginStartDate), sdf.format(endStartDate)));
            }
            AccountCriteria.add(ExpressionHelper.eq("state", state));
            List wfnotifis = client.getNotificationManager().queryNotificationsCriteria(AccountCriteria, cond);
//            notis = wfnotifis;
            notis = WorkFlowDataConvertor.convert2NotificationList(wfnotifis);
            try
            {
                ResultList list = (ResultList)wfnotifis;
                PageCond pageCond = list.getPageCond();
                PageCondition pageCondition = notificationfilter.getPageCondition();
                if (pageCondition != null) {
                    WorkFlowDataConvertor.convert2PageCond(pageCond, pageCondition);
                }
            } catch (Exception e) {
                throw new AdapterException("通知的分页信息转换异常", e);
            }
        }
        catch (WFServiceException e)
        {
            throw new AdapterException(e);
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return notis;
    }

    /**
     * 任务/通知抄送
     *
     * @param taskInstId   任务id
     * @param participants 用户对象列表
     * @param userId    当前登录ID
     */
    public static void ccTask(String taskInstId, List<Participant> participants,
                              String userId) throws AdapterException {
//        try {
//            getWorkflowService(accountId).ccTask(taskInstId, participants);
//        } catch (WFException e) {
//            throw new AdapterException(e);
//        }
        try {
            if ((participants == null) || (participants.size() == 0)) {
                throw new AdapterException("被抄送的参数者为空");
            }
            WFParticipant[] pars = new WFParticipant[participants.size()];
            for (int i = 0; i < participants.size(); i++) {
//                pars[i] = WorkFlowDataConvertor.convert2WFParticipant((com.primeton.workflow.model.definition.Participant)participants.get(i));
                Participant participant = participants.get(i);
                if (participant == null) {
                    continue;
                }
                WFParticipant wfparticipant = new WFParticipant();
                if ("1".equals(String.valueOf(participant.getParticipantType())))
                {
                    wfparticipant.setId(participant.getParticipantID());
                    wfparticipant.setTypeCode("person");
                    wfparticipant.setName(participant.getParticipantName());
                } else if ("2".equals(String.valueOf(participant.getParticipantType())))
                {
                    wfparticipant.setId(participant.getParticipantID());
                    wfparticipant.setTypeCode("role");
                    wfparticipant.setName(participant.getParticipantName());
                } else if ("3".equals(String.valueOf(participant.getParticipantType())))
                {
                    wfparticipant.setId(participant.getParticipantID());
                    wfparticipant.setTypeCode("organization");
                    wfparticipant.setName(participant.getParticipantName());
                } else {
                    throw new AdapterException(participant.getParticipantType() + "为不支持的类型");
                }
                pars[i] = wfparticipant;
            }
            IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();
            WFWorkItem workItem = client.getWorkItemManager().queryWorkItemDetail(Long.parseLong(taskInstId));

            NotificationOption option = new NotificationOption();
            option.setExpandParticipant(true);

//            String jobCode = null;
//            String jobTitle = null;
            TaskInstance taskInstance = WorkflowAdapter.getTaskInstanceObject(userId,taskInstId);
//            com.metarnet.common.workflow.TaskFilter taskfilter = new com.metarnet.common.workflow.TaskFilter();
//            taskfilter.setTaskInstID(taskInstId);
//            taskfilter.setTaskType("1");
//            com.metarnet.common.workflow.PageCondition pageCon = new com.metarnet.common.workflow.PageCondition();
//            pageCon.setLength(1);
//            pageCon.setBegin(0);
//            pageCon.setIsCount(Boolean.valueOf(false));
//            taskfilter.setPageCondition(pageCon);
//            List s = FindWorkItemUtils.getMyTasks(taskfilter, userId);
//            String jobID;
//            String jobTitle;
//            if ((s == null) || (s.size() != 1))
//            {
//                jobID = jobTitle = null;
//            } else {
//                TaskInstance ss = (TaskInstance)s.get(0);
//                jobID = ss.getJobID();
//                jobTitle = ss.getJobTitle();
//            }
//
//            if (jobID == null) {
//                taskfilter.setTaskInstID(taskInstId);
//                taskfilter.setTaskType("2");
//                s = FindWorkItemUtils.getMyTasks(taskfilter, userId);
//                if ((s == null) || (s.size() != 1)) {
//                    jobID = jobTitle = null;
//                } else {
//                    TaskInstance ss = (TaskInstance)s.get(0);
//                    jobID = ss.getJobID();
//                    jobTitle = ss.getJobTitle();
//                }
//            }
            client.getNotificationManager().sendTaskNotification(userId, pars, taskInstance.getJobTitle(), taskInstance.getJobCode(), workItem, option);
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 任务认领
     *
     * @param taskInstId 任务id
     * @param accountId  当前登录ID
     * @throws AdapterException
     *
     */
    public static void claimTask(String taskInstId, String accountId) throws AdapterException {
        try {
            getWorkflowService(accountId).claimTask(taskInstId);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 动态启动并挂接子流程到父流程
     *
     * @param processDefName      子流程模版名称
     * @param processInstName     子流程实例名称
     * @param parentProcessInstID 父流程实例id
     * @param actInstID           父流程环节实例id
     * @param params              子流程启动参数
     * @throws AdapterException
     *
     */
    public static String addAndStartProcessWithParentActivityInstID(String processDefName, String processInstName,
                                                                    String parentProcessInstID, String actInstID,
                                                                    LinkedHashMap<String, Object> params,
                                                                    String accountId) throws AdapterException {
        try {
            return getWorkflowService(accountId).addAndStartProcessWithParentActivityInstID(processDefName,
                    processInstName, parentProcessInstID, actInstID, params);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 根据流程实例id和环节定义id查找正在运行的环节实例id
     *
     * @param processInstID 流程实例ID
     * @param activityDefID 环节定义ID
     * @return
     */
    public static ActivityInstance findActivityInstByActivityDefID(String processInstID, String activityDefID,
                                                                   String accountId) throws AdapterException {
        try {
            return getWorkflowService(accountId).findActivityInstByActivityDefID(processInstID, activityDefID);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 18.1	通过当前环节实例ID，找到后继可能到达的活动的角色
     *
     * @param processInstID  流程实例ID
     * @param activityInstID 活动实例ID
     * @param accountId
     * @param parameters     可选 所需的维度参数值 应用可以指定值，如果某个维度没有指定，则流程引擎使用流程模板中的配置值
     * @return
     */
    public static List<Participant> getNextActivitiesMaybeRoles(String processInstID, String activityInstID,
                                                                String accountId, LinkedHashMap parameters) throws
            AdapterException {
        try {
            List<ActivityDef> activityDefList = getNextActivitiesMaybeArrived(activityInstID, accountId);
            List<Participant> participants = new ArrayList<Participant>();
            if (activityDefList != null && activityDefList.size() > 0) {
                for (ActivityDef activityDef : activityDefList) {
                    participants.addAll(getWorkflowService(accountId).getProbableParticipants
                            (processInstID, activityInstID, activityDef.getActivityID(), parameters));
                }
            }
            return participants;
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 42  查询出当前环节的后续环节可能的参与者
     *
     * @param processInstID         流程实例ID
     * @param currentActivityInstID 当前环节实例 id
     * @param activityID            要查询的流程环 节定义 id（一般指 下环节的环节 id）
     * @param accountId
     * @param parameters            可选 所需的维度参数值 应用可以指定值，如果某个维度没有指定，则流程引擎使用流程模板中的配置值
     * @return
     */
    public static List<Participant> getProbableParticipants(String processInstID, String currentActivityInstID,
                                                            String activityID, String accountId,
                                                            LinkedHashMap parameters) throws AdapterException {
        try {
            return getWorkflowService(accountId).getProbableParticipants(processInstID, currentActivityInstID,
                    activityID, parameters);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 43.	获取指定环节的参与者
     *
     * @param processModelID 流程定义ID
     * @param activityID     环节定义 id
     * @param accountId
     * @param parameters     可选 所需的维度参数值 应用可以指定值，如果某个维度没有指定，则流程引擎使用流程模板中的配置值
     * @return
     */
    public static List<Participant> getActivityParticipants(String processModelID, String activityID,
                                                            LinkedHashMap parameters,
                                                            String accountId) throws AdapterException {
        try {
            return getWorkflowService(accountId).getActivityParticipants(processModelID, activityID, parameters);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 通过当前环节实例ID，找到后继可能到达的活动定义
     *
     * @param activityInstID 活动实例ID
     */
    public static List<ActivityDef> getNextActivitiesMaybeArrived(String activityInstID,
                                                                  String accountId) throws AdapterException {
        try {
            return getWorkflowService(accountId).getNextActivitiesMaybeArrived(activityInstID);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 回退到指定的步骤
     *
     * @param accountId
     * @param processId   当前流程id
     * @param currentActivityInstId 当前活动实例id
     * @param activityDefID  要回退环节的活动定义id
     * @throws AdapterException
     *
     */

    public static void backTargetActivity(String accountId, String processId, String currentActivityInstId, String activityDefID
    ) throws AdapterException {
        try {
            String targetActivityInstId = "";
            //获得当前流程流转过的所有活动
            List<ActivityInstance> activityInstances = getActivityInstances(accountId, processId);
            //遍历所有活动如果有与传过来的定义id相同的才执行回退
            for (ActivityInstance activityInstance : activityInstances) {
                String defId = activityInstance.getActivityDefID();
                if ((activityDefID).equals(defId)) {
                    targetActivityInstId = activityInstance.getActivityInstID();
                }
            }
            if (!"".equals(targetActivityInstId)) {
                backActivity(accountId, currentActivityInstId, targetActivityInstId);
            }
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }
    /**
     * 撤销流程
     *
     * @param processInstID 流程实例ID
     */
    public static void terminateProcessInstance(String processInstID, String accountId) throws AdapterException {
        try {
            getWorkflowService(accountId).terminateProcessInstance(processInstID);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 更新工单主题
     *
     * @param processInstID 流程实例
     * @param jobTilt       工单主题
     * @param accountId
     * @throws AdapterException
     *
     */
    public static void updateJobTitleInfo(String processInstID, String jobTilt,
                                          String accountId) throws AdapterException {
        try {
            getWorkflowService(accountId).updateJobTitleInfo(processInstID, jobTilt);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 工作项实例ID
     *
     * @param taskInstanceId 任务实例Id
     * @param accountId
     * @throws AdapterException
     *
     */
    public static void drawbackWorkItem(long taskInstanceId, String accountId) throws AdapterException {
        try {
            getWorkflowService(accountId).drawbackWorkItem(taskInstanceId, true, false);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 根据工作项ID判断该工作项是否可以拽回
     *
     * @param taskInstanceId 任务实例Id
     * @param accountId
     * @return
     * @throws AdapterException
     *
     */
    public static boolean isDrawbackEnable(long taskInstanceId, String accountId) throws AdapterException {
        try {
            return getWorkflowService(accountId).isDrawbackEnable(taskInstanceId);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 根据工作项ID，活动定义ID删除工作项被指定的参与者。
     *
     * @param taskInstanceId 任务实例Id
     * @param activityDefID  环节定义ID
     * @param accountId
     * @throws AdapterException
     *
     */
    public static void clearAppointedActivityParticipants(long taskInstanceId, String activityDefID,
                                                          String accountId) throws AdapterException {
        try {
            getWorkflowService(accountId).clearAppointedActivityParticipants(taskInstanceId, activityDefID);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 48.	根据环节实列结束指定环节
     *
     * @param activityInstID 流程环节实例ID
     */

    public static void finishActivityInstance(String activityInstID, String accountId) throws AdapterException {
        try {
            getWorkflowService(accountId).finishActivityInstance(activityInstID);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 根据流程实例ID获取当前流程正在处理的工作项参与
     *
     * @param processInstID
     * @param accountId
     * @return
     * @throws AdapterException
     *
     */
    public static List<Participant> findDoingParticipant(String processInstID,
                                                         String accountId) throws AdapterException {
        try {
            return getWorkflowService(accountId).findDoingParticipant(processInstID);
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 重启已完成的流程实例（未转历史），激活指定活动并指派参与者。
     *
     * @param processInstID 流程实例ID
     * @param activityDefID 活动定义ID
     * @param participants  参与值列表
     * @return
     * @throws AdapterException
     *
     */

    public static String restartFinishedProcessInst(String accountId, long processInstID, String activityDefID,
                                                    List<Participant> participants) throws AdapterException {
        try {
            return getWorkflowService(accountId).restartFinishedProcessInst(String.valueOf(processInstID),
                    activityDefID, participants);
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 挂起流程
     *
     * @param processInstID 流程实例ID
     * @throws AdapterException
     *
     */
    public static void suspendProcessInstance(long processInstID, String accountId) throws AdapterException {
        try {
            getWorkflowService(accountId).suspendProcessInstance(String.valueOf(processInstID));
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }


    /**
     * 激活流程
     *
     * @param processInstID 流程实例ID
     * @throws AdapterException
     *
     */
    public static void resumeProcessInstance(long processInstID, String accountId) throws AdapterException {
        try {
            getWorkflowService(accountId).resumeProcessInstance(String.valueOf(processInstID));
        } catch (WFException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 获取根流程实例id
     *
     * @param processInstanceId 流程实例ID
     */
    public static ProcessInstance getRootProcessInstance(String accountId, String processInstanceId) throws AdapterException {
        try {
            ProcessInstance processInstance = getProcessInstance(accountId, processInstanceId);
            if ("0".equals(processInstance.getParentProcessInstID())) {
                return processInstance;
            }
            return getRootProcessInstance(accountId, processInstance.getParentProcessInstID());
        } catch (AdapterException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 激活活动实例
     *
     * @param accountId
     * @param activityInstID
     * @throws AdapterException
     *
     */
    public static void restartActivityInstance(String accountId, String activityInstID) throws AdapterException {
        try {
            getWorkflowService(accountId).restartActivityInstance(activityInstID);
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 启动一个未开始的环节
     *
     * @param accountId
     * @param processInstID
     * @param activityDefID
     * @throws AdapterException
     */
    public static void createAndStartActivityInstance(String accountId, String processInstID, String activityDefID) throws AdapterException {
        try {
            getWorkflowService(accountId).createAndStartActivityInstance(processInstID, activityDefID);
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 终止活动实例
     *
     * @param accountId
     * @param activityInstID
     * @throws AdapterException
     *
     */
    public static void terminateActivityInstance(String accountId, String activityInstID) throws AdapterException {
        try {
            getWorkflowService(accountId).terminateActivityInstance(activityInstID);
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     *
      * @param specialty 专业    z
     * @param orgID 组织id         z
     * @param process           流程定义id
     * @param node  流程活动定义id         l
     * @return
     */
    public static String findNextParticipant(String specialty, String orgID,String process, String node){
         return findNextParticipant(specialty , orgID , process , node , false);
    }

    public static String findNextParticipant(String specialty, String orgID, String orgName , String process, String node , Boolean flag){
        Map<String , String> paramsMap = new HashMap<String , String>();
        List<Participant> participants = new ArrayList<Participant>();
        paramsMap.put("specialty" , specialty);
        try {
            paramsMap.put("orgID" , AAAAAdapter.getCompany(Integer.parseInt(orgID)).getOrgId().toString());
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        }
        paramsMap.put("process" , process);
        paramsMap.put("node" , node);
        paramsMap.put("flag" , String.valueOf(flag));
        String userAccountIds = HttpClientUtil.sendPostRequestByJava(Constants.POWERURL + "/powerController.do?method=findNextParticipant2", paramsMap);

        if(userAccountIds != null && !"".equals(userAccountIds)){
            List list = JSON.parseArray(userAccountIds);
            for(Object userAccountId : list){
                Participant participant = new Participant();
                participant.setParticipantID(String.valueOf(userAccountId));
                participant.setParticipantName(orgID + ":" + orgName);
                participant.setParticipantType("1");
                participants.add(participant);
            }
        }
        return JSON.toJSONString(participants);
    }
    public static String findNextParticipant(String specialty, String orgID,String process, String node , Boolean flag){
        return findNextParticipant(specialty , orgID , "" , process , node , flag);
    }

    /**
     *
     * @param specialty 专业    z
     * @param orgID 组织id         z
     * @param process           流程定义id
     * @param node  流程活动定义id         l
     * @return
     */
    public static List<String>  findNextParticipantToChildInstance(String specialty, String orgID,String process, String node){
        return findNextParticipantToChildInstance(specialty, orgID, "" , process, node);
    }
    public static List<String>  findNextParticipantToChildInstance(String specialty, String orgID, String orgName , String process, String node){
        Map<String , String> paramsMap = new HashMap<String , String>();
        List<Participant> participants = new ArrayList<Participant>();
        paramsMap.put("specialty" , specialty);
        try {
            paramsMap.put("orgID" , AAAAAdapter.getCompany(Integer.parseInt(orgID)).getOrgId().toString());
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        }
        paramsMap.put("process" , process);
        paramsMap.put("node" , node);
        String userAccountIds = HttpClientUtil.sendPostRequestByJava(Constants.POWERURL + "/powerController.do?method=findNextParticipant2", paramsMap);;
         List<String> listInfo = new ArrayList<String>();
        if(userAccountIds != null && !"".equals(userAccountIds)){
            List list = JSON.parseArray(userAccountIds);
            for(Object userAccountId : list){
                Participant participant = new Participant();
                participant.setParticipantID(String.valueOf(userAccountId));
                participant.setParticipantName(orgID + ":" + orgName);
                participant.setParticipantType("1");
                participants.add(participant);
                listInfo.add(JSON.toJSONString(participants));
                participants = new ArrayList<Participant>();
            }
        }
        return listInfo;
    }

    public static String findDoingActivitysByJobID(String accountId , String jobId){
        try {
            return getWorkflowService(accountId).findDoingActivitys(jobId);
        } catch (WFException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }


}