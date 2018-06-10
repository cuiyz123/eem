package com.metarnet.core.common.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eos.workflow.omservice.WFParticipant;
import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.SendAdapter;
import com.metarnet.core.common.adapter.WorkflowAdapter;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.*;
import com.metarnet.core.common.service.IWorkflowBaseService;
import com.metarnet.core.common.service.IWorkflowProcessor;
import com.metarnet.core.common.utils.Constants;
import com.metarnet.core.common.utils.HibernateBeanUtils;
import com.metarnet.core.common.utils.PubFun;
import com.metarnet.core.common.utils.SpringContextUtils;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import com.unicom.ucloud.workflow.filters.TaskFilter;
import com.unicom.ucloud.workflow.objects.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@SuppressWarnings("unchecked")
@Service("workflowBaseService")
public class WorkflowBaseServiceImpl implements IWorkflowBaseService {

    private Logger logger = LogManager.getLogger(WorkflowBaseServiceImpl.class);

    private String participant = "'participant':[{'participantID':'#participantID','participantName':'','participantType':'1'}]";
    private String participantID = "#participantID";

    final private String PROINSTID = "proinstid";
    final private String PARPROINSTID = "parproinstid";
    final private String PARTICIPANTS = "participants";
    final private String ACTINSTNAME = "actinstname";

    @Autowired
    private IBaseDAO baseDAO;

    @Override
    public List<TaskInstance> getMyWaitingTasks(TaskFilter taskFilter, String accountId) throws ServiceException {
        try {

            taskFilter.setProcessModelName(Constants.PROCESS_MODELS);
//            taskFilter.setJobCode("ZB-E131-201508-000055");
//            taskFilter.setProcessModelName("com.unicom.ucloud.eom.eomkgm.workflow.taskApply");
            return WorkflowAdapter.getMyWaitingTasks(taskFilter, accountId);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public String startProcess(UserEntity userEntity, Object entity, String processModelID, Participant participant, Map<String, Object> bizModleParams, ProcessModelParams processModelParams) throws ServiceException {
        String processInstID = null;
        try {
            String accounId = userEntity.getUserName();
            processInstID = WorkflowAdapter.startProcess(accounId, processModelID, participant, bizModleParams, processModelParams);
            /*try {
                //放置工单归档参数
                //注:测试使用阶段，防止entity实体不包含下面属性而产生异常影响使用,此处先不抛异常
                Map relateMap = new HashMap();
                relateMap.put(Global.BIZ_ROOTVCCOLUMN1, org.apache.commons.beanutils.BeanUtils.getProperty(entity, "startObjTabName"));
                relateMap.put(Global.BIZ_ROOTVCCOLUMN2, org.apache.commons.beanutils.BeanUtils.getProperty(entity, "startObjectId"));
                //根流程需设置分片id
                relateMap.put(Constants.SHARDING_ID, String.valueOf(DataRouteAdapter.getInstence().findShardInfoByOrgId(userEntity.getOrgID().intValue())));
                WorkflowAdapter.setRelativeData(processInstID, relateMap, accounId);
            } catch (Exception e) {

            }
            TaskInstance taskInstance = new TaskInstance();
            taskInstance.setProcessInstID(processInstID);
            taskInstance.setRootProcessInstId(processInstID);
            List<ActivityInstance> acts = WorkflowAdapter.getActivityInstances(accounId, processInstID);
            taskInstance.setActivityInstName(acts.get(0).getActivityInstName());
            taskInstance.setCreateDate(acts.get(0).getCreateTime());
            if (Constants.IS_SHARDING) {
                taskInstance.setShard(String.valueOf(DataRouteAdapter.getInstence().findShardInfoByOrgId(userEntity.getOrgID().intValue())));
            }*/
        } catch (AdapterException e) {
            e.printStackTrace();
            throw new ServiceException(e);
        }
        return processInstID;
    }

    @Override
    public void handleJobTitle(TaskInstance taskInstance, UserEntity userEntity, boolean rejectTag) throws AdapterException {

    }

    @Override
    public String addSubProcess(String businessCode, String productCode, String majorCode, String orgCode, UserEntity userEntity, TaskInstance taskInstance, String activityDefID, LinkedHashMap params) throws ServiceException {
        return addSubProcess(businessCode, productCode, majorCode, orgCode, null, userEntity, taskInstance,
                activityDefID, params);
    }

    @Override
    public String addSubProcess(String businessCode, String productCode, String majorCode, String orgCode, String attribute1, UserEntity userEntity, TaskInstance taskInstance, String activityDefID, LinkedHashMap params) throws ServiceException {
        ProcessDefInfo processDefInfo = AAAAAdapter.getInstence().getProcessDefName(Constants.BUSINESS_CODE, activityDefID);
        logger.error("流程挂接接口返回值：processDefName=" + processDefInfo.getProcessDefName() + " 具体信息如下 businessCode=" + businessCode + " productCode=" + productCode + "" +
                " majorCode=" + majorCode + "  orgCode=" + orgCode + "  attribute1=" + attribute1 + " 用户信息=" + userEntity.getUserId());
        try {
            return WorkflowAdapter.addAndStartProcessWithParentActivityInstID(processDefInfo.getProcessDefName(), "", taskInstance
                    .getProcessInstID(), WorkflowAdapter.findActivityInstByActivityDefID(
                    taskInstance.getProcessInstID(), activityDefID, userEntity.getUserName())
                    .getActivityInstID(), params, userEntity.getUserName());


        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public List<Map> getTransverseInfo(String activityInstId, String processInstID, String shard) throws ServiceException {
        return null;
    }

    @Override
    public void submitTask(TaskInstance taskInstance, List<Participant> participants, Object entity, UserEntity userEntity, Integer operTypeEnumId) throws ServiceException {
        submitTask(taskInstance, participants, entity, userEntity, "", operTypeEnumId);
    }

    @Override
    public void submitTask(TaskInstance taskInstance, List<Participant> participants, Object entity, UserEntity userEntity, List<String> list, Integer operTypeEnumId) throws ServiceException {
        try {
            String accountId = userEntity.getUserName();
            if (StringUtils.isEmpty(taskInstance.getTaskInstID())) {
                taskInstance = getTaskInstance(taskInstance.getProcessInstID(), accountId);
            }
            // 前处理
            ActivityModel activityModel = ExtendNodeCofnig.parseActivity(userEntity.getUserName(),
                    taskInstance.getProcessModelName(), taskInstance.getActivityDefID());
            if (null != activityModel && activityModel.getPreProcessorList().size() > 0) {
                for (ProcessorModel preProcessor : activityModel.getPreProcessorList()) {
                    ((IWorkflowProcessor) SpringContextUtils.getBean(preProcessor.getName())).execute(taskInstance,
                            participants, entity, userEntity, preProcessor.getParams());
                }
            }
            List<ActivityDef> activityDefs = WorkflowAdapter.getNextActivitiesMaybeArrived(taskInstance.getActivityInstID(),
                    accountId);
            //子流程和结束环节下一步处理人设为null
            for (ActivityDef activityDef : activityDefs) {
                if (!Constants.ACT_TYPE_MANUAL.equals(activityDef.getActivitytype())) {
                    participants = null;
                }
            }
//            this.setNextParticipant(taskInstance.getProcessInstID(), participants, userEntity,list);
            WorkflowAdapter.submitTask(accountId, taskInstance, null);
            //保存通用处理信息，操作日志
            saveGeneralInfo((BaseForm) entity, taskInstance, userEntity, operTypeEnumId);
            // 后处理
            if (null != activityModel && activityModel.getPostProcessorList().size() > 0) {
                for (ProcessorModel postProcessor : activityModel.getPostProcessorList()) {
                    ((IWorkflowProcessor) SpringContextUtils.getBean(postProcessor.getName())).execute(taskInstance,
                            participants, entity, userEntity, postProcessor.getParams());
                }
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void submitTask(TaskInstance taskInstance, List<Participant> participants, Object entity, UserEntity userEntity, String route, Integer operTypeEnumId) throws ServiceException {
        try {
            String accountId = userEntity.getUserName();
            if (StringUtils.isEmpty(taskInstance.getTaskInstID())) {
                taskInstance = getTaskInstance(taskInstance.getProcessInstID(), accountId);
            }
            // 前处理
            ActivityModel activityModel = ExtendNodeCofnig.parseActivity(userEntity.getUserName(),
                    taskInstance.getProcessModelName(), taskInstance.getActivityDefID());
            if (null != activityModel && activityModel.getPreProcessorList().size() > 0) {
                for (ProcessorModel preProcessor : activityModel.getPreProcessorList()) {
                    ((IWorkflowProcessor) SpringContextUtils.getBean(preProcessor.getName())).execute(taskInstance,
                            participants, entity, userEntity, preProcessor.getParams());
                }
            }
            List<ActivityDef> activityDefs = WorkflowAdapter.getNextActivitiesMaybeArrived(taskInstance.getActivityInstID(),
                    accountId);
            //子流程和结束环节下一步处理人设为null
            for (ActivityDef activityDef : activityDefs) {
                if (!Constants.ACT_TYPE_MANUAL.equals(activityDef.getActivitytype())) {
                    participants = null;
                }
            }
            if (null != route && !"".equals(route) && Constants.ROUTE_START_SUBMIT.equals(route)) {
                this.setNextParticipant(taskInstance.getProcessInstID(), participants, userEntity, taskInstance);
            }
            WorkflowAdapter.submitTask(accountId, taskInstance, null);
            //保存通用处理信息，操作日志
            saveGeneralInfo((BaseForm) entity, taskInstance, userEntity, operTypeEnumId);
            // 后处理
            if (null != activityModel && activityModel.getPostProcessorList().size() > 0) {
                for (ProcessorModel postProcessor : activityModel.getPostProcessorList()) {
                    ((IWorkflowProcessor) SpringContextUtils.getBean(postProcessor.getName())).execute(taskInstance,
                            participants, entity, userEntity, postProcessor.getParams());
                }
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void finishActivityInstance(TaskInstance taskInstance, List<Participant> participants, Object entity, UserEntity userEntity) throws ServiceException {
        try {
            ActivityModel activityModel = ExtendNodeCofnig.parseActivity(userEntity.getUserName(),
                    taskInstance.getProcessModelName(), taskInstance.getActivityDefID());
            // 前处理
            if (null != activityModel && activityModel.getPreProcessorList().size() > 0) {
                for (ProcessorModel preProcessor : activityModel.getPreProcessorList()) {
                    ((IWorkflowProcessor) SpringContextUtils.getBean(preProcessor.getName())).execute(taskInstance,
                            participants, entity, userEntity, preProcessor.getParams());
                }
            }
            WorkflowAdapter.finishActivityInstance(taskInstance.getActivityInstID(), userEntity.getUserName());
            // 后处理
            if (null != activityModel && activityModel.getPostProcessorList().size() > 0) {
                for (ProcessorModel postProcessor : activityModel.getPostProcessorList()) {
                    ((IWorkflowProcessor) SpringContextUtils.getBean(postProcessor.getName())).execute(taskInstance,
                            participants, entity, userEntity, postProcessor.getParams());
                }
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void setNextPersonMap(String areacode, String orgcode, String majorcode, String productcode, String roleclass, String processInstID, String accountId) throws ServiceException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.DIMENSION_AREA_CODE, StringUtils.isEmpty(areacode) ? "" : areacode);
        map.put(Constants.DIMENSION_ORG_CODE, StringUtils.isEmpty(orgcode) ? "" : orgcode);
        map.put(Constants.DIMENSION_MAJOR_CODE, StringUtils.isEmpty(majorcode) ? "" : majorcode);
        map.put(Constants.DIMENSION_PRODUCT_CODE, StringUtils.isEmpty(productcode) ? "" : productcode);
        map.put(Constants.DIMENSION_ROLE_CLASS, StringUtils.isEmpty(roleclass) ? "" : roleclass);
        try {
            WorkflowAdapter.setRelativeData(processInstID, map, accountId);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public TaskInstance getTaskInstance(String processId, String accountId) throws ServiceException {
        //  TaskInstance taskInstance = new TaskInstance();
        try {
            // TaskFilter taskFilter = new TaskFilter();
            // taskFilter.setProcessInstID(processId);
            // List<TaskInstance> list = WorkflowAdapter.getMyWaitingTasks(taskFilter, accountId);
            // List<TaskInstance> list = WorkflowAdapter.queryNextWorkItemsByProcessInstID(processId,accountId);
            List<ActivityInstance> acts = WorkflowAdapter.getActivityInstances(accountId, processId);
            if (acts != null && acts.size() > 0) {
                String activityInstID = acts.get(acts.size() - 1).getActivityInstID();//由于getActivityInstances()已按时间排序，直接取acts.get(1)即可。
                List<TaskInstance> list = WorkflowAdapter.getTaskInstancesByActivityID(accountId, activityInstID);
                if (list != null && list.size() > 0) {
                    return list.get(0);
                }
/*                for (TaskInstance instance : list) {
                    if (instance.getProcessInstID().equals(processId)) {
                        taskInstance = instance;
                   }
                }*/
            }
        } catch (Exception e) {
//            throw new ServiceException("", new UserEntity(), "", e);
        }
        return new TaskInstance();
    }

    @Override
    public void restartSubProcess(Map<String, String> subProcessInstIDMap, Map<String, Object> relativeMap, TaskInstance taskInstance, String accountId, List<Participant> participants) throws ServiceException {
        if (subProcessInstIDMap == null || subProcessInstIDMap.isEmpty()) {
            return;
        }
        try {
            //重启子流程
            for (String subProcessInstID : subProcessInstIDMap.keySet()) {
                String activityDefName = subProcessInstIDMap.get(subProcessInstID);
                WorkflowAdapter.restartFinishedProcessInst(accountId, Long.parseLong(subProcessInstID), activityDefName,
                        participants);
            }
            //提交当前流程
            WorkflowAdapter.setRelativeData(taskInstance.getProcessInstID(), relativeMap, accountId);
            WorkflowAdapter.submitTask(accountId, taskInstance, null);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void setNextParticipant(String ProcessInstId, List<Participant> participants, UserEntity userEntity, TaskInstance taskInstance) throws ServiceException {
        try {
//            Map mapTemp = WorkflowAdapter.getRelativeData(ProcessInstId, Arrays.asList(Constants.FIRST_STEP_USER, Constants.FIRST_ACCOUNTID, Constants.FIRST_ORG_CODE), userEntity.getUserName());
            Map map = new HashMap();
            /*
            如果相关数据区里的firstStepUser包含participant，则为直接起的流程或者是上级流程选的指定的人
            若不包含，则为上级流程没有指定人
            如果为指定人的那种情况， 并且相关数据区里firstAccountId为空，则赋值当前人accountId
            如果不是人的情况，并且相关数据区里firstOrgCode为空，则赋值当前人cloudOrgId
             */
            /*if (null != mapTemp && mapTemp.size() > 0) {
                if (!"".equals(mapTemp.get(Constants.FIRST_STEP_USER)) && mapTemp.get(Constants.FIRST_STEP_USER).toString().contains("participant")) {
                    if (null == mapTemp.get(Constants.FIRST_ACCOUNTID) || "".equals(mapTemp.get(Constants.FIRST_ACCOUNTID))) {
                        map.put(Constants.FIRST_ACCOUNTID, userEntity.getUserName());
                    }
                } else if (null == mapTemp.get(Constants.FIRST_ORG_CODE) || "".equals(mapTemp.get(Constants.FIRST_ORG_CODE))) {
                    String orgCode = "[" + mapTemp.get(Constants.FIRST_STEP_USER).toString() + "]";
                    JSONArray json = JSONArray.parseArray(orgCode);
                    Object[] objs = json.toArray();
                    String org = ((net.sf.json.JSONObject) objs[0]).get("orgcode").toString();
                    String org1 = org.replaceAll("\\[", "");
                    String org2 = org1.replaceAll("\\]", "");
                    map.put(Constants.FIRST_ORG_CODE, org2.replaceAll("\"", ""));
                }
            }*/
            if (null != participants && participants.size() > 0) {
                map.put(Constants.FIRST_STEP_USER,
                        "{" +
                                "'areacode':[]," +
                                "'majorcode':[]," +
                                "'orgcode':[]," +
                                "'productcode':[]," +
                                "'participant':" + com.alibaba.fastjson.JSONArray.toJSONString(participants) +
                                "}"
                );
            } /*else {
                Map maps = WorkflowAdapter.getRelativeData(ProcessInstId, Arrays.asList("flow"), userEntity.getUserName());
                *//*
                局数据有一个执行接口人的处理，不需要重新配置firstStepUser
                 *//*
                if (null != maps && maps.size() > 0 && maps.get("flow").equals("execute")) {
                    return;
                } else {
                    List<UserEntity> userEntityList = AAAAAdapter.findUserListByOrgID(userEntity.getOrgID().intValue());
                    Map<String, Object> relativeData = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(),
                            Arrays.asList(Constants.DIMENSION_MAJOR_CODE), userEntity.getUserName());
                    String specialty = (String) relativeData.get(Constants.DIMENSION_MAJOR_CODE);
                    if (specialty == null || "".equals(specialty)) {
                        *//*String   taskInstance2=WorkflowAdapter.getProcessInstance(userEntity.getUserName(),taskInstance.getProcessInstID()).getParentProcessInstID() ;
                        Map<String, Object> relativeData2 = WorkflowAdapter.getRelativeData(taskInstance2,
                                Arrays.asList(Constants.DIMENSION_MAJOR_CODE), userEntity.getUserName());
                        specialty =(String) relativeData2.get(Constants.DIMENSION_MAJOR_CODE);*//*
                        LinkedHashMap<String, Object> relaDatas = new LinkedHashMap<String, Object>();
                        relaDatas.put(Constants.DIMENSION_MAJOR_CODE, specialty);
                        WorkflowAdapter.setRelativeData(taskInstance.getProcessInstID(), relaDatas, userEntity.getUserName());

                    }
                    participants = AAAAAdapter.findNextParticipants(taskInstance, userEntity, specialty, userEntity.getOrgID().toString());
                    if (participants == null) {
                        participants = new ArrayList<Participant>();
                        for (UserEntity u : userEntityList) {
                            Participant p = new Participant();
                            p.setParticipantID(u.getUserName());
                            p.setParticipantName("");
                            p.setParticipantType("1");
                            participants.add(p);
                        }
                    }

                    map.put(Constants.FIRST_STEP_USER,
                            "{" +
                                    "'areacode':[]," +
                                    "'majorcode':[]," +
                                    "'orgcode':[]," +
                                    "'productcode':[]," +
                                    "'participant':" + com.alibaba.fastjson.JSONArray.toJSONString(participants) +
                                    "}");
                    *//*map.put(Constants.FIRST_STEP_USER,
                            "{" +
                                    "'areacode':[]," +
                                    "'majorcode':[]," +
                                    "'orgcode':[" + userEntity.getOrgID().intValue() + "]," +
                                    "'productcode':[]" +
                                    "}"
                    );*//*

                }
            }*/
            WorkflowAdapter.setRelativeData(ProcessInstId, map, userEntity.getUserName());
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void setNextParticipant(String ProcessInstId, List<Participant> participants, UserEntity userEntity, List<String> list, TaskInstance taskInstance) throws ServiceException {
        try {
//            Map mapTemp = WorkflowAdapter.getRelativeData(ProcessInstId, Arrays.asList(Constants.FIRST_STEP_USER, Constants.FIRST_ACCOUNTID, Constants.FIRST_ORG_CODE), userEntity.getUserName());
            Map map = new HashMap();
            /*
            如果相关数据区里的firstStepUser包含participant，则为直接起的流程或者是上级流程选的指定的人
            若不包含，则为上级流程没有指定人
            如果为指定人的那种情况， 并且相关数据区里firstAccountId为空，则赋值当前人accountId
            如果不是人的情况，并且相关数据区里firstOrgCode为空，则赋值当前人cloudOrgId
             */
            /*if (null != mapTemp && mapTemp.size() > 0) {
                if (!"".equals(mapTemp.get(Constants.FIRST_STEP_USER)) && mapTemp.get(Constants.FIRST_STEP_USER).toString().contains("participant")) {
                    if (null == mapTemp.get(Constants.FIRST_ACCOUNTID) || "".equals(mapTemp.get(Constants.FIRST_ACCOUNTID))) {
                        map.put(Constants.FIRST_ACCOUNTID, userEntity.getUserName());
                    }
                } else if (null == mapTemp.get(Constants.FIRST_ORG_CODE) || "".equals(mapTemp.get(Constants.FIRST_ORG_CODE))) {
                    String orgCode = "[" + mapTemp.get(Constants.FIRST_STEP_USER).toString() + "]";
                    JSONArray json = JSONArray.parseArray(orgCode);
                    Object[] objs = json.toArray();
                    String org = ((net.sf.json.JSONObject) objs[0]).get("orgcode").toString();
                    String org1 = org.replaceAll("\\[", "");
                    String org2 = org1.replaceAll("\\]", "");
                    map.put(Constants.FIRST_ORG_CODE, org2.replaceAll("\"", ""));
                }
            }
            String speciality = "";
            for (String s : list) {
                speciality += s + ",";
            }
            speciality = speciality.substring(0, speciality.length() - 1);*/
            if (null != participants && participants.size() > 0) {
                map.put(Constants.FIRST_STEP_USER,
                        "{" +
                                "'areacode':[]," +
                                "'majorcode':[]," +
                                "'orgcode':[]," +
                                "'productcode':[]," +
                                "'participant':" + com.alibaba.fastjson.JSONArray.toJSONString(participants) +
                                "}"
                );
            } /*else {
                Map maps = WorkflowAdapter.getRelativeData(ProcessInstId, Arrays.asList("flowNo"), userEntity.getUserName());
                *//*
                局数据有一个执行接口人的处理，不需要重新配置firstStepUser
                 *//*
                if (null != maps && maps.size() > 0 && maps.get("flowNo").equals("execute")) {
                    WorkflowAdapter.setRelativeData(ProcessInstId, map, userEntity.getUserName());
                    return;
                } else {

                    *//*map.put(Constants.FIRST_STEP_USER,
                            "{" +
                                    "'areacode':[]," +
                                    "'majorcode':[" + speciality + "]," +
                                    "'orgcode':[" + userEntity.getOrgID().intValue() + "]," +
                                    "'productcode':[]" +
                                    "}"
                    );*//*
                    List<Participant> participantList = new ArrayList<Participant>();
                    if (participants == null || participants.size() == 0) {
                        participantList = AAAAAdapter.findNextParticipants(taskInstance, userEntity, speciality, userEntity.getOrgID().toString());
                        *//*Participant p = new Participant();
                        p.setParticipantID("caokj");
                        p.setParticipantName("");
                        p.setParticipantType("1");
                        participantList.add(p);*//*
                    } else {
                        participantList = participants;
                    }
                    map.put(Constants.FIRST_STEP_USER,
                            "{" +
                                    "'areacode':[]," +
                                    "'majorcode':[]," +
                                    "'orgcode':[]," +
                                    "'productcode':[]," +
                                    "'participant':" + com.alibaba.fastjson.JSONArray.toJSONString(participantList) +
                                    "}");
                    //map.put(Constants.DIMENSION_MAJOR_CODE, speciality);
                }
            }*/
            String speciality = "";
            for (String s : list) {
                speciality += s + ",";
            }
            speciality = speciality.substring(0, speciality.length() - 1);
            map.put(Constants.DIMENSION_MAJOR_CODE, speciality);
            WorkflowAdapter.setRelativeData(ProcessInstId, map, userEntity.getUserName());
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public List<GeneralInfoModel> getGeneralInfoByRootProcessId(String rootProcessId) throws ServiceException {
        try {
            return baseDAO.find("from GeneralInfoModel where rootProInstId = ?", new Object[]{rootProcessId});
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获得根流程下所有当前处理环节信息，处理人信息 与工单日志公用一个model
     *
     * @param rootProcessId 根流程实例ID
     * @return
     */
    @Override
    public List<GeneralInfoModel> getAllActivityInstanceInfos(String rootProcessId, String jobID, final UserEntity user) throws ServiceException {
        logger.info("INTO getAllActivityInstanceInfos...");
        final List<GeneralInfoModel> nowProcessPersonInfos = new ArrayList<GeneralInfoModel>();
        try {

            int currActInstCount = 0;              //当前活动节点数量

            Date start_find_doing = new Date();
            logger.info("Start to findDoingActivitysByJobID...");
            String activityInstJSONStr = WorkflowAdapter.findDoingActivitysByJobID(user.getTrueName(), jobID);
            logger.info("Return findDoingActivitysByJobID.../COST = " + (new Date().getTime() - start_find_doing.getTime()));
            start_find_doing = new Date();
            com.alibaba.fastjson.JSONArray activityInstances = com.alibaba.fastjson.JSON.parseArray(activityInstJSONStr);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            for (int j = 0; j < activityInstances.size(); j++) {
                JSONObject act = (JSONObject) activityInstances.get(j);

                final String processInstID = String.valueOf(act.get(PROINSTID));
                final String parentProcessInstID = String.valueOf(act.get(PARPROINSTID));
                final String activityInstName = String.valueOf(act.get(ACTINSTNAME));
                final com.alibaba.fastjson.JSONArray participants = act.getJSONArray(PARTICIPANTS);
                String createTimeStr = act.getString("creattime");
                final Timestamp createTime = new Timestamp(sdf.parse(createTimeStr).getTime());
                Timestamp lastOperTime = null;
                List<GeneralInfoModel> generalInfoModelList = baseDAO.find("from GeneralInfoModel where processInstId = ? order by creationTime desc ,operTime desc", new Object[]{processInstID});
                if (generalInfoModelList != null && generalInfoModelList.size() > 0 && "40050464".equals(String.valueOf(generalInfoModelList.get(0).getOperTypeEnumId()))) {
                    lastOperTime = generalInfoModelList.get(0).getOperTime();
                }
                final Timestamp turnTocreateTime = lastOperTime;
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        Date start_processing_logModel = new Date();
                        GeneralInfoModel logModeinfo = new GeneralInfoModel();
                        logModeinfo.setActivityInstName(activityInstName);
                        logModeinfo.setCreationTime(createTime);
                        logModeinfo.setProcessInstId(processInstID);
                        logModeinfo.setParentProInstId(parentProcessInstID);
                        if (turnTocreateTime != null) {
                            logModeinfo.setCreationTime(turnTocreateTime);
                        }
                        List<String> pIds = new ArrayList<String>();
                        for (int k = 0; k < participants.size(); k++) {
                            String participantID = String.valueOf(((JSONObject) participants.get(k)).get("id"));
                            if (pIds.contains(participantID)) {
                                continue;
                            }
                            pIds.add(participantID);
                        }

                        List<UserEntity> currentUserList = null;
                        Date start_find_users = new Date();
                        try {
                            currentUserList = AAAAAdapter.getInstence().findUserListByUserNames(pIds);
                        } catch (PaasAAAAException e) {
                            logger.info("ERROR : findUserListByUserNames Failed....../TRY AGAIN.../COST " + (new Date().getTime() - start_find_users.getTime()));
                            Date re_start_find_users = new Date();
                            try {
                                currentUserList = AAAAAdapter.getInstence().findUserListByUserNames(pIds);
                            } catch (PaasAAAAException e1) {
                                logger.info("ERROR : findUserListByUserNames Failed AGAIN.../COST " + (new Date().getTime() - re_start_find_users.getTime()));
                            }
                        }
                        StringBuffer userName = new StringBuffer();
                        String participantName = "";
                        if (currentUserList != null && currentUserList.size() > 0) {
                            logger.info("SUCCESSS : findUserListByUserNames /COST " + (new Date().getTime() - start_find_users.getTime()));
                            UserEntity currentUser = null;
                            if (currentUserList != null && currentUserList.size() > 0) {
                                for (int i = 0; i < currentUserList.size(); i++) {
                                    currentUser = currentUserList.get(i);
                                    try {
                                        setBelongInfo(logModeinfo, currentUser);
                                    } catch (ServiceException e) {
                                        e.printStackTrace();
                                    }
                                    userName.append(currentUser.getTrueName() + "||" + currentUser.getUserName());
                                    if (i < currentUserList.size() - 1) {
                                        userName.append(",");
                                    }
                                }
                                participantName = currentUser.getUserName();
                                OrgEntity orgEntity = currentUser.getOrgEntity();
                                logModeinfo.setOperOrgName(orgEntity.getOrgName());
                            }
                        }
                        try {
                            Map<String, Object> relativeData = null;
                            if ("汇总审核".equals(activityInstName)) {
                                relativeData = WorkflowAdapter.getRelativeData(parentProcessInstID,
                                        Arrays.asList(Constants.ORG_CODE), participantName);
                            } else {
                                relativeData = WorkflowAdapter.getRelativeData(processInstID,
                                        Arrays.asList(Constants.ORG_CODE), participantName);
                            }

                            if (relativeData.size() > 0 && relativeData != null) {
                                if (!StringUtils.isEmpty(String.valueOf(relativeData.get(Constants.ORG_CODE))) && !"null".equals(String.valueOf(relativeData.get(Constants.ORG_CODE))))
                                    logModeinfo.setOperOrgName(AAAAAdapter.getInstence().findOrgByOrgCode(String.valueOf(relativeData.get(Constants.ORG_CODE))).getOrgName());
                            }
                        } catch (AdapterException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (PaasAAAAException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }

                        logModeinfo.setOperUserTrueName(userName.toString());


                        synchronized (nowProcessPersonInfos) {
                            nowProcessPersonInfos.add(logModeinfo);
                        }
                        logger.info("Adding logModeinfo into nowProcessPersonInfos/Size of nowProcessPersonInfos is " + nowProcessPersonInfos.size() + "/COST " + (new Date().getTime() - start_processing_logModel.getTime()));
                    }
                }).start();

                currActInstCount++;
            }

            while (nowProcessPersonInfos.size() < currActInstCount) {
                Thread.sleep(10);
            }
            logger.info("Return processOrderLogModel.../COST = " + (new Date().getTime() - start_find_doing.getTime()));
        } catch (Exception e) {
            throw new ServiceException(e);
        }

        return nowProcessPersonInfos;
    }


    @Override
    public String generateDisWorkOrderCode(UserEntity userEntity, String speciality, String businessType, Long sequenceValue) throws ServiceException {
        try {
            // 工单类型
            String woType = (StringUtils.isEmpty(speciality) ? "综合" : speciality)
                    + (StringUtils.isEmpty(businessType) ? Constants.BUSINESS_TYPE : businessType);
            // 组织标识
            String orgIdentifier = "";

            OrgShortNameEntity orgShortNameEntity = AAAAAdapter.getInstence().getOrgShortNameEntity(
                    userEntity.getOrgID().intValue());
            orgIdentifier = orgShortNameEntity == null ? "XXX" : orgShortNameEntity.getOrgShortName();
            // 日期
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
            String periodIdentifier = "【" + simpleDateFormat.format(new Date()) + "】";

            return orgIdentifier + "网调" + periodIdentifier + woType
                    + new DecimalFormat("00000").format(sequenceValue);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void saveGeneralProcess(GeneralInfoModel generalInfoModel, TaskInstance taskInstance, UserEntity userEntity) throws ServiceException {
        saveGeneralProcess(generalInfoModel, taskInstance, userEntity, null);
    }

    @Override
    public void saveGeneralProcess(GeneralInfoModel generalInfoModel, TaskInstance taskInstance, UserEntity userEntity, String participant) throws ServiceException {
        try {
            if (generalInfoModel.getOperTypeEnumId() == 40050228) {//签发
                if (Constants.Y.equals(generalInfoModel.getProcessingStatus())) {
                    addSubPreProcess(taskInstance, null, userEntity, Constants.BUSINESS_CODE);
                }
            }
            Map<String, Object> relativeData = null;
            try {
                if (generalInfoModel.getOperTypeEnumId() == 40050227) {//审核
                    Map<String, Object> relative = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(), Arrays.asList("processInstID"), userEntity.getUserName());
                    if (relative.size() > 0) {
                        relativeData = WorkflowAdapter.getRelativeData(relative.get("processInstID").toString(), Arrays.asList(Constants.APPLY_ID, Constants.ROOT_DISPATCH_ID, Constants.PARENT_DISPATCH_ID, Constants.DISPATCH_ID), userEntity.getUserName());
                    } else {
                        relativeData = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(), Arrays.asList(Constants.APPLY_ID, Constants.ROOT_DISPATCH_ID, Constants.PARENT_DISPATCH_ID, Constants.DISPATCH_ID), userEntity.getUserName());
                    }
                } else {
                    relativeData = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(), Arrays.asList(Constants.APPLY_ID, Constants.ROOT_DISPATCH_ID, Constants.PARENT_DISPATCH_ID, Constants.DISPATCH_ID), userEntity.getUserName());
                }
            } catch (AdapterException e) {
                e.printStackTrace();
            }
            generalInfoModel.setApplyId(relativeData.get(Constants.APPLY_ID) == null ? -1L : Long.valueOf(relativeData.get(Constants.APPLY_ID).toString()));
            generalInfoModel.setRootDisId(relativeData.get(Constants.ROOT_DISPATCH_ID) == null ? -1L : Long.valueOf(relativeData.get(Constants.ROOT_DISPATCH_ID).toString()));
            generalInfoModel.setParentDisId(relativeData.get(Constants.PARENT_DISPATCH_ID) == null ? -1L : Long.valueOf(relativeData.get(Constants.PARENT_DISPATCH_ID).toString()));
            generalInfoModel.setDispatchId(relativeData.get(Constants.DISPATCH_ID) == null ? -1L : Long.valueOf(relativeData.get(Constants.DISPATCH_ID).toString()));

            if (StringUtils.isEmpty(taskInstance.getTaskInstID())) {
                taskInstance = getTaskInstance(taskInstance.getProcessInstID(), userEntity.getUserName());
            }
//            submitTask(taskInstance, null, generalInfoModel, userEntity, null);

            if (generalInfoModel.getOperTypeEnumId() == 40050464) {//转办
                List<Participant> participants = null;
                ParticipantsUtilModel pum = JSON.parseObject(participant, ParticipantsUtilModel.class);
                participants = pum.getParticipantList();
                Map map = null;
                try {
                    map = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(), Arrays.asList(new String[]{
                            Constants.ROOT_ACTIVITY_DEF_ID, Constants.ROOT_PROCESS_INST_ID}), userEntity.getUserName());
                    if (null == map.get(Constants.ROOT_ACTIVITY_DEF_ID)) {
                        map.put(Constants.ROOT_ACTIVITY_DEF_ID, taskInstance.getActivityDefID());
                        map.put(Constants.ROOT_PROCESS_INST_ID, taskInstance.getProcessInstID());
                    }
                    WorkflowAdapter.setRelativeData(taskInstance.getProcessInstID(), map, userEntity.getUserName());
                } catch (AdapterException e) {
                    e.printStackTrace();
                }

                try {
                    WorkflowAdapter.forwardTask(userEntity.getUserName(), taskInstance.getTaskInstID(), participants);
                } catch (AdapterException e) {
                    e.printStackTrace();
                }
                saveGeneralInfo(generalInfoModel, taskInstance, userEntity, generalInfoModel.getOperTypeEnumId());
                Map<String, Object> relative = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(), Arrays.asList(Constants.JOB_TITLE), userEntity.getUserName());
                String jobTitle = "";
                if (relative.size() > 0 && relative.get(Constants.JOB_TITLE) != null) {
                    jobTitle = relative.get(Constants.JOB_TITLE).toString();
                }
                SendAdapter.sentMessageToDo(Constants.MODEL_NAME, taskInstance.getJobCode(), jobTitle, participants, Long.parseLong(taskInstance.getProcessInstID()), Long.parseLong(taskInstance.getTaskInstID()));
            } else if (generalInfoModel.getOperTypeEnumId() == 40050465) {//撤单
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("PROCESS_STATUS", Constants.PROCESSING_TYPE_CANCEL);
                WorkflowAdapter.setRelativeData(taskInstance.getProcessInstID(), map, userEntity.getUserName());
                WorkflowAdapter.terminateProcessInstance(taskInstance.getProcessInstID(), userEntity.getUserName());
                saveGeneralInfo(generalInfoModel, taskInstance, userEntity, generalInfoModel.getOperTypeEnumId());
            } else {
                submitTask(taskInstance, null, generalInfoModel, userEntity, null);
            }
            if (generalInfoModel.getOperTypeEnumId() == 40050228) {//签发
                if (Constants.Y.equals(generalInfoModel.getProcessingStatus())) {
                    //查找主送
                    List<FdbkCommonModel> fdbkList = baseDAO.findEntityListByParentProInstId(Constants.FDBK_MODEL, taskInstance.getProcessInstID());
                    addSubPostProcess(taskInstance, null, userEntity, Constants.BUSINESS_CODE, fdbkList);
                    updateDispatchModelIssue(taskInstance, userEntity);
                    List<DisCommonModel> dispatchList = baseDAO.find("from " + Constants.DIS_MODEL + " where processInstId = ?", new Object[]{taskInstance.getProcessInstID()});
                    if (dispatchList != null && dispatchList.size() > 0) {
                        ccTask(taskInstance, userEntity, dispatchList.get(0));
                    }
                    try {
                        Map<String, Object> relativeDataNew = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(), Arrays.asList(Constants.DIMENSION_MAJOR_CODE, Constants.ORG_CODE), userEntity.getUserName());
                        OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(relativeDataNew.get(Constants.ORG_CODE).toString());
                        List<Participant> participantList = AAAAAdapter.getInstence().findNextParticipants(Constants.PROCESS_MODEL_NAME, Constants.DISPATCH_LINK_NAME, relativeDataNew.get(Constants.DIMENSION_MAJOR_CODE) + "", orgEntity.getOrgId().toString());
                        relativeDataNew.put(Constants.FIRST_STEP_USER,
                                "{" +
                                        "'areacode':[]," +
                                        "'majorcode':[]," +
                                        "'orgcode':[]," +
                                        "'productcode':[]," +
                                        "'participant':" + com.alibaba.fastjson.JSONArray.toJSONString(participantList) +
                                        "}"
                        );
                        WorkflowAdapter.setRelativeData(taskInstance.getProcessInstID(), relativeDataNew, userEntity.getUserName());
                    } catch (AdapterException e) {
                        e.printStackTrace();
                    } catch (PaasAAAAException e) {
                        e.printStackTrace();
                    }
                }
            } else if (generalInfoModel.getOperTypeEnumId() == 40050229) {//签收
                Map<String, Object> relativeDataNew = new HashMap<String, Object>();
                try {
                    relativeDataNew.put(Constants.CREATE_USER, "{'" +
                            "areacode':[]," +
                            "'majorcode':[]," +
                            "'orgcode':[]," +
                            "'productcode':[]," +
                            "'participant':[{'participantID':'" + userEntity.getUserName() + "','participantName':'','participantType':'1'}]}");
                    WorkflowAdapter.setRelativeData(taskInstance.getProcessInstID(), relativeDataNew, userEntity.getUserName());
                    updateFdbkModelSignTime(taskInstance, userEntity);
                } catch (AdapterException e) {
                    e.printStackTrace();
                }
            }

        } catch (DAOException e) {
            e.printStackTrace();
        } catch (AdapterException e) {
            e.printStackTrace();
        }
    }

    /**
     * 抄送工作项
     *
     * @param taskInstance
     * @param userEntity
     * @param dispatch
     */
    private void ccTask(TaskInstance taskInstance, UserEntity userEntity, DisCommonModel dispatch) {

        List<Map<String, String>> copyList = new ArrayList<Map<String, String>>();

        List<Participant> copyDeptList = new ArrayList<Participant>();

        if (dispatch.getCopyTransfer() != null && StringUtils.isNotBlank(dispatch.getCopyTransfer())) {
            String orgOrPersonStr[] = dispatch.getCopyTransfer().split(",");
            for (int i = 0; i < orgOrPersonStr.length; i++) {
                Map<String, String> map = new HashMap<String, String>();
                String orgOrPerson = orgOrPersonStr[i];
                String info[] = orgOrPerson.split(":");
                //派发对象Id
                String id = info[0];
                //派发对象类型
                String type = info[1];
                //派发到组织
                if (Constants.DISPATCH_OBJECT_TYPE_ORG.equals(type)) {
                    List<Participant> participantList = new ArrayList<Participant>();
                    String orgCode = null;
                    try {
                        orgCode = AAAAAdapter.getInstence().getCompany(Integer.valueOf(id)).getOrgId().toString();
//                        orgCode = AAAAAdapter.getInstence().getCompany(AAAAAdapter.getInstence().findOrgByOrgCode(id).getOrgId().intValue()).getOrgId().toString();
                    } catch (PaasAAAAException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
//                    taskInstance.setActivityDefID("subTurnToSend");
//                    taskInstance.setProcessModelName("com.metarnet.fastprocess.main");
                    Map<String, Object> relativeData = null;
                    try {
                        relativeData = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(), Arrays.asList(Constants.DIMENSION_MAJOR_CODE), userEntity.getUserName());
                    } catch (AdapterException e) {
                        e.printStackTrace();
                    }
                    participantList = AAAAAdapter.getInstence().findNextParticipants(Constants.PROCESS_MODEL_NAME, "s_subTurnToSend", relativeData.get(Constants.DIMENSION_MAJOR_CODE).toString(), orgCode);
                    if (participantList != null && participantList.size() > 0) {
                        copyDeptList.addAll(participantList);
                    }

                } else {
                    /**
                     * 派发到人
                     */
                    try {
                        UserEntity copyUser = AAAAAdapter.getInstence().findUserByUserID(Long.valueOf(id));

                        Participant participant = new Participant();
                        participant.setParticipantID(copyUser.getUserName());
                        participant.setParticipantName(copyUser.getTrueName());
                        participant.setParticipantType("1");

                        copyDeptList.add(participant);
                    } catch (PaasAAAAException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        if (copyDeptList.size() > 0) {
            try {
                WorkflowAdapter.ccTask(taskInstance.getTaskInstID(), copyDeptList, userEntity.getUserName());
            } catch (AdapterException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private void updateDispatchModelIssue(TaskInstance taskInstance, UserEntity userEntity) {
        String hql = "from " + Constants.DIS_MODEL + " where processInstId = ?";

        try {
            List list = baseDAO.find(hql, new Object[]{taskInstance.getProcessInstID()});
            if (list != null && list.size() > 0) {
                DisCommonModel disCommonModel = (DisCommonModel) list.get(0);
                disCommonModel.setIssueTime(new Timestamp(System.currentTimeMillis()));
                disCommonModel.setIssueUserId(userEntity.getUserId());
                disCommonModel.setIssueUserTrueName(userEntity.getTrueName());
                baseDAO.update(disCommonModel);
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    private void updateFdbkModelSignTime(TaskInstance taskInstance, UserEntity userEntity) {
        String hql = "from " + Constants.FDBK_MODEL + " where objectId = ?";
        Map map = null;
        List list = null;
        try {
            map = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(), Arrays.asList(new String[]{
                    Constants.FDBK_ID}), userEntity.getUserName());
            if (map != null && map.get(Constants.FDBK_ID) != null)
                list = baseDAO.find(hql, new Object[]{map.get(Constants.FDBK_ID)});
            if (list != null && list.size() > 0) {
                FdbkCommonModel fdbkCommonModel = (FdbkCommonModel) list.get(0);
                fdbkCommonModel.setTaskStartTime(new Timestamp(System.currentTimeMillis()));    //接单时间
                fdbkCommonModel.setWorkOrderStatus("已接单");
                fdbkCommonModel.setProcessInstId(taskInstance.getProcessInstID());
                baseDAO.update(fdbkCommonModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setGeneralInfo(BaseForm baseForm, TaskInstance taskInstance, UserEntity userEntity) throws ServiceException {
        ProcessInstance processInstance = null;
        try {
            processInstance = WorkflowAdapter.getProcessInstance(userEntity.getUserName(), taskInstance.getProcessInstID());
        } catch (AdapterException e) {
            e.printStackTrace();
        }

        baseForm.setTaskStartTime(new Timestamp(taskInstance.getCreateDate().getTime()));
        baseForm.setOperUserId(userEntity.getUserId());
        baseForm.setOperUserTrueName(userEntity.getTrueName());
        baseForm.setOperOrgId(userEntity.getOrgID());
        baseForm.setOperOrgName(userEntity.getOrgEntity().getOrgName());
        baseForm.setOperFullOrgName(userEntity.getOrgEntity().getFullOrgName());
        baseForm.setOperTime(new Timestamp(new Date().getTime()));
        baseForm.setTaskInstId(taskInstance.getTaskInstID());
        baseForm.setProcessInstId(taskInstance.getProcessInstID());
        baseForm.setParentProInstId(processInstance.getParentProcessInstID());
        baseForm.setRootProInstId(taskInstance.getRootProcessInstId());
        baseForm.setActivityInstName(taskInstance.getActivityInstName());

        setBelongInfo(baseForm, userEntity);
    }

    @Override
    public void setBelongInfo(BaseForm baseForm, UserEntity userEntity) throws ServiceException {
        try {
            Map orgInfo = PubFun.getOrgInfoByOrgID(Integer.parseInt(AAAAAdapter.getCompany(userEntity.getOrgID().intValue()).getOrgId().toString()));
            // 所属省分
            baseForm.setBelongProvinceCode(orgInfo.get(PubFun.BELONGEDPROVINCE) == null ? null : ((Long) orgInfo.get(PubFun.BELONGEDPROVINCE)).intValue());
            //所属省分中文
            baseForm.setBelongProvinceName(orgInfo.get(PubFun.BELONGEDPROVINCENAME) == null ? null : ((String) orgInfo.get(PubFun.BELONGEDPROVINCENAME)));
            // 所属地市
            baseForm.setBelongCityCode(orgInfo.get(PubFun.BELONGEDCITY) == null ? null : ((Long) orgInfo.get(PubFun.BELONGEDCITY)).intValue());
            //所属地市中文
            baseForm.setBelongCityName(orgInfo.get(PubFun.BELONGEDCITYNAME) == null ? null : ((String) orgInfo.get(PubFun.BELONGEDCITYNAME)));
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addSubPreProcess(TaskInstance taskInstance, List<Participant> participants, UserEntity userEntity, String params) throws ServiceException {
        LinkedHashMap<String, Object> relaDatas = new LinkedHashMap<String, Object>();
        String accountId = userEntity.getUserName();
        String majorcoden = null;
        String[] departments = new String[0];
        try {
            departments = WorkflowAdapter.resolveProcsParameter(StringUtils.EMPTY, Constants.PASS_CODE, majorcoden, StringUtils.EMPTY);
        } catch (AdapterException e) {
            e.printStackTrace();
        }
        relaDatas.put(Constants.DEPARTMENTS, departments);
        try {
            WorkflowAdapter.setRelativeData(taskInstance.getProcessInstID(), relaDatas, accountId);
        } catch (AdapterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addSubPostProcess(TaskInstance taskInstance, List<Participant> participants, UserEntity userEntity, String params, List<FdbkCommonModel> fdbkList) throws ServiceException {
        try {
            String errorName = "";
            //查询当前人组织
            LinkedHashMap<String, Object> ps = new LinkedHashMap<String, Object>();
            Map<String, Object> relativeData = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(),
                    Arrays.asList(Constants.DIMENSION_MAJOR_CODE, Constants.DIMENSION_AREA_CODE, Constants.DIMENSION_PRODUCT_CODE,
                            Constants.OBJECT_TABLE, Constants.OBJECT_ID, Constants.FIRST_STEP_USER, Constants.SUBPARENTDISPATCHER,
                            Constants.SUB_RELATEDDISPATCHID, Constants.BIZ_STRCOLUMN1, Constants.JOB_TITLE, Constants.JOB_CODE,
                            Constants.APPLY_ID, Constants.ROOT_DISPATCH_ID, Constants.ACTION_TYPE, Constants.PARENT_DISPATCH_ID, Constants.DISPATCH_ID, "subDataMake"), userEntity.getUserName());
            //上级部门审核人参与者
            WFParticipant dispatcher = new WFParticipant();

            try {
                //获取流程实例的子流程
                List<ProcessInstance> processInstances = WorkflowAdapter.getSubProcessInstance(userEntity.getUserName(), taskInstance.getProcessInstID());
                for (ProcessInstance pie : processInstances) {
                    if (Constants.HANGING.equals(pie.getProcessModelName()) && "1".equals(pie.getProcessInstStatus())) {//如果子流程是通用挂接子流程且处于运行状态
                        WorkflowAdapter.terminateProcessInstance(pie.getProcessInstID(), userEntity.getUserName());//撤销流程
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String activityID = "";


            List<ActivityDef> activityDefs = WorkflowAdapter.getNextActivitiesMaybeArrived(taskInstance.getActivityInstID(), userEntity.getUserName());
            for (ActivityDef activityDef : activityDefs) {
                if (Constants.ACT_TYPE_SUBFLOW.equals(activityDef.getActivitytype())) {
                    activityID = activityDef.getActivityID();
                }
            }

            for (FdbkCommonModel feedback : fdbkList) {
                //派发到组织
                if (Constants.DISPATCH_OBJECT_TYPE_ORG.equals(feedback.getDisAssignObjectType())) {
                    String[] firstStepUser = new String[0];
                    try {
                        firstStepUser = WorkflowAdapter.resolveProcsParameter((String) relativeData.get(Constants.DIMENSION_AREA_CODE),
                                feedback.getDisAssignObjectId().toString(), (String) relativeData.get(Constants.DIMENSION_MAJOR_CODE),
                                (String) relativeData.get(Constants.DIMENSION_PRODUCT_CODE));
                    } catch (AdapterException e1) {
                        e1.printStackTrace();
                    }
                    String dispatchName = feedback.getDisAssignObjectName();
                    // 派发到部门
                    OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(feedback.getDisAssignObjectId());
                    if (orgEntity.getOrgType().equals(Constants.PROCESS_START_DEPT)) {
                        ps.put(Constants.SOURCE, Constants.PROCESS_START_DEPT);
                    } else if (orgEntity.getOrgType().equals(Constants.PROCESS_START_ORG)) {
                        //派发到分公司
                        ps.put(Constants.SOURCE, Constants.PROCESS_START_ORG);
                    } else if (orgEntity.getOrgType().equals("PRO")) {
                        //派发到分公司
                        ps.put(Constants.SOURCE, Constants.PROCESS_START_ORG);
                    } else if (orgEntity.getOrgType().equals("CITY")) {
                        //派发到分公司
                        ps.put(Constants.SOURCE, Constants.PROCESS_START_ORG);
                    } else {
                        ps.put(Constants.SOURCE, Constants.PROCESS_START_DEPT);
                    }
                    ps.put(Constants.IS_ROOT, Constants.N);
                    ps.put(Constants.FIRST_STEP_USER, firstStepUser[0]);
                    ps.put(Constants.ORG_NAME, dispatchName);
                    ps.put(Constants.ORG_CODE, feedback.getDisAssignObjectId());
                    ps.put(Constants.SUBPARENTDISPATCHER, dispatcher);
                    ps.put(Constants.SUB_RELATEDDISPATCHID, relativeData.get(Constants.SUB_RELATEDDISPATCHID) == null ? "" : relativeData.get(Constants.SUB_RELATEDDISPATCHID));

                    ps.put(Constants.BIZ_DATCOLUMN1, feedback.getReqFdbkTime().getTime() + "");

                    if (Constants.IS_SHOW_MAJOR) {
                        ps.put(Constants.BIZ_STRCOLUMN1, relativeData.get(Constants.BIZ_STRCOLUMN1));
                    }
                    //增加jobtitle jobcode
                    ps.put(Constants.JOB_TITLE, relativeData.get(Constants.JOB_TITLE) == null ? "" : relativeData.get(Constants.JOB_TITLE));
                    ps.put(Constants.JOB_CODE, relativeData.get(Constants.JOB_CODE) == null ? "" : relativeData.get(Constants.JOB_CODE));
                    ps.put(Constants.ACTION_TYPE, relativeData.get(Constants.ACTION_TYPE));
                    //虽然派发到组织但是取得是组织的所属分公司
                    Object majorCode = relativeData.get(Constants.DIMENSION_MAJOR_CODE);
//                    beforeAddSubProcess(taskInstance, participants, entity, userEntity, params, ps);
                    List<Participant> participantList = new ArrayList<Participant>();
                    if (participants == null || participants.size() == 0) {
                        participantList = AAAAAdapter.getInstence().findNextParticipants(Constants.PROCESS_MODEL_NAME, taskInstance, userEntity, majorCode.toString(), orgEntity.getOrgId().toString());
                        if (participantList == null || participantList.size() == 0) {
                            errorName += dispatchName + "，";
                            logger.error(dispatchName + "没有配置角色");
                            continue;
                        }
                    } else {
                        participantList = participants;
                    }
                    ps.put(Constants.FIRST_STEP_USER,
                            "{" +
                                    "'areacode':[]," +
                                    "'majorcode':[]," +
                                    "'orgcode':[]," +
                                    "'productcode':[]," +
                                    "'participant':" + com.alibaba.fastjson.JSONArray.toJSONString(participantList) +
                                    "}");
                    //子流程驳回父流程需要的参数
                    ps.put(Constants.approvalStatusSub, Constants.Y);

                    try {
                        String subPrs = addSubProcess(params, null, majorCode == null ? "" : majorCode.toString(), orgEntity.getOrgCode(), Constants.PROCESS_EXTEND_ATTRIBUTE_DIS, userEntity,
                                taskInstance, activityID, ps);
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put(Constants.DIMENSION_MAJOR_CODE, majorCode.toString());
                        map.put(Constants.APPLY_ID, relativeData.get(Constants.APPLY_ID));
                        map.put(Constants.DISPATCH_ID, feedback.getDispatchId());
                        map.put(Constants.PARENT_DISPATCH_ID, relativeData.get(Constants.DISPATCH_ID));
                        map.put(Constants.ROOT_DISPATCH_ID, relativeData.get(Constants.ROOT_DISPATCH_ID));
                        map.put(Constants.FDBK_ID, feedback.getObjectId());
                        if (relativeData.get("subDataMake") != null) {
                            map.put("subDataMake", relativeData.get("subDataMake").toString());
                        }
                        WorkflowAdapter.setRelativeData(subPrs, map, userEntity.getUserName());
                    } catch (Exception e) {
                        errorName += dispatchName + "，";
                        logger.error(dispatchName + "没有配置角色");
                        continue;
                    }
                }
                //派发到人
                if (Constants.DISPATCH_OBJECT_TYPE_MEMBER.equals(feedback.getDisAssignObjectType())) {
                    String parentFirstStepUser = (String) relativeData.get(Constants.FIRST_STEP_USER);// 取当前流程相关数据区的firstStepUser
                    String firstStepUser = StringUtils.EMPTY;
                    if (parentFirstStepUser.contains("'participant':")) {
                        parentFirstStepUser = parentFirstStepUser.substring(0, parentFirstStepUser.indexOf("'participant':"));
                        firstStepUser = parentFirstStepUser + participant + "}";
                    } else {
                        firstStepUser = parentFirstStepUser.replace("}", "," + participant + "}");
                    }
                    UserEntity user = AAAAAdapter.getInstence().findUserbyUserID(Integer.valueOf(feedback.getDisAssignObjectId()));
                    // String firstStepUser = parentFirstStepUser.replace("}", "," + participant + "}");
                    firstStepUser = StringUtils.replace(firstStepUser, participantID, user.getUserName());
                    ps.put(Constants.SOURCE, Constants.PROCESS_START_NISSUE);
                    ps.put(Constants.IS_ROOT, Constants.N);
                    ps.put(Constants.FIRST_STEP_USER, firstStepUser);
                    ps.put(Constants.ORG_NAME, user.getTrueName());
                    ps.put(Constants.ORG_CODE, user.getOrgCode());
                    ps.put(Constants.SUBPARENTDISPATCHER, dispatcher);
                    ps.put(Constants.SUB_RELATEDDISPATCHID, relativeData.get(Constants.SUB_RELATEDDISPATCHID) == null ? "" : relativeData.get(Constants.SUB_RELATEDDISPATCHID));

                    ps.put(Constants.BIZ_DATCOLUMN1, (feedback.getReqFdbkTime() == null ? new Date().getTime() : feedback.getReqFdbkTime().getTime()) + "");
                    if (Constants.IS_SHOW_MAJOR) {
                        ps.put(Constants.BIZ_STRCOLUMN1, relativeData.get(Constants.BIZ_STRCOLUMN1));
                    }
                    //增加jobtitle jobcode
                    ps.put(Constants.JOB_TITLE, relativeData.get(Constants.JOB_TITLE) == null ? "" : relativeData.get(Constants.JOB_TITLE));
                    ps.put(Constants.JOB_CODE, relativeData.get(Constants.JOB_CODE) == null ? "" : relativeData.get(Constants.JOB_CODE));
                    ps.put(Constants.ACTION_TYPE, relativeData.get(Constants.ACTION_TYPE));
                    //orgCode取得是人所属分公司
                    String orgCode = AAAAAdapter.getInstence().getCompany(user.getOrgID().intValue()).getOrgId().toString();
//                    beforeAddSubProcess(taskInstance, participants, entity, userEntity, params, ps);
                    //子流程驳回父流程需要的参数
                    ps.put(Constants.approvalStatusSub, Constants.Y);
                    Object majorCode = relativeData.get(Constants.DIMENSION_MAJOR_CODE);
                    String subPrs = addSubProcess(params, null, majorCode == null ? "" : majorCode.toString(), orgCode, Constants.PROCESS_EXTEND_ATTRIBUTE_DIS, userEntity,
                            taskInstance, activityID, ps);
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(Constants.DIMENSION_MAJOR_CODE, majorCode.toString());
                    map.put(Constants.APPLY_ID, relativeData.get(Constants.APPLY_ID));
                    map.put(Constants.DISPATCH_ID, feedback.getDispatchId());
                    map.put(Constants.PARENT_DISPATCH_ID, relativeData.get(Constants.DISPATCH_ID));
                    map.put(Constants.ROOT_DISPATCH_ID, relativeData.get(Constants.ROOT_DISPATCH_ID));
                    map.put(Constants.FDBK_ID, feedback.getObjectId());
                    if (relativeData.get("subDataMake") != null) {
                        map.put("subDataMake", relativeData.get("subDataMake").toString());
                    }
                    WorkflowAdapter.setRelativeData(subPrs, map, userEntity.getUserName());
                }

                logger.warn("派发对象错误,类型为：" + feedback.getDisAssignObjectType());
            }

            if (!errorName.equals("")) {
                //查询当前的活动实例
                ActivityInstance activityInstance = WorkflowAdapter.getWorkflowService(userEntity.getUserName()).
                        findLastActivityInstByActivityID(taskInstance.getProcessInstID(), Constants.PROCESS_EXTEND_ATTRIBUTE_DIS);
                //流程回退
                WorkflowAdapter.backActivity(userEntity.getUserName()
                        , activityInstance.getActivityInstID(), taskInstance.getActivityInstID());
                errorName = errorName + "以上组织没有配置相关的角色，请配置相应角色，或者驳回重派";
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(Constants.ERROR_NAME_FOR_DISPATCH, errorName);
                WorkflowAdapter.setRelativeData(taskInstance.getProcessInstID(), map, userEntity.getUserName());
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void saveGeneralInfo(GeneralInfoModel generalInfoModel, TaskInstance taskInstance, UserEntity userEntity) throws ServiceException {
        setGeneralInfo(generalInfoModel, taskInstance, userEntity);
        generalInfoModel.setRootProInstId(taskInstance.getRootProcessInstId());
        generalInfoModel.setProcessInstId(taskInstance.getProcessInstID());
        generalInfoModel.setTaskInstId(taskInstance.getTaskInstID());
        generalInfoModel.setActivityInstName(taskInstance.getActivityInstName());
        try {
            baseDAO.saveOrUpdate(generalInfoModel, userEntity);
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveGeneralInfo(BaseForm baseForm, TaskInstance taskInstance, UserEntity userEntity, Integer operTypeEnumId) throws ServiceException {
        if (operTypeEnumId == null) {
            saveGeneralInfo((GeneralInfoModel) baseForm, taskInstance, userEntity);
            return;
        }

        GeneralInfoModel generalInfoModel = new GeneralInfoModel();
        Map<String, Object> relativeData = null;
        try {
            if (operTypeEnumId == 40050227) {
                Map<String, Object> relative = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(), Arrays.asList("processInstID"), userEntity.getUserName());
                relativeData = WorkflowAdapter.getRelativeData(relative.get("processInstID").toString(), Arrays.asList(Constants.APPLY_ID, Constants.ROOT_DISPATCH_ID, Constants.PARENT_DISPATCH_ID, Constants.DISPATCH_ID), userEntity.getUserName());
            } else {
                if (operTypeEnumId == 40050464) {
                    List<GeneralInfoModel> generalInfoModelList = baseDAO.find("from GeneralInfoModel where processInstId = ? order by creationTime desc ,operTime desc", new Object[]{taskInstance.getProcessInstID()});
                    if (generalInfoModelList != null && generalInfoModelList.size() > 0 && "40050464".equals(String.valueOf(generalInfoModelList.get(0).getOperTypeEnumId()))) {
                        generalInfoModel.setCreationTime(generalInfoModelList.get(0).getOperTime());
                    }
                }
                relativeData = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(), Arrays.asList(Constants.APPLY_ID, Constants.ROOT_DISPATCH_ID, Constants.PARENT_DISPATCH_ID, Constants.DISPATCH_ID), userEntity.getUserName());
            }
        } catch (AdapterException e) {
            e.printStackTrace();
        } catch (DAOException e) {
            e.printStackTrace();
        }

        generalInfoModel.setApplyId(relativeData.get(Constants.APPLY_ID) == null ? -1L : Long.valueOf(relativeData.get(Constants.APPLY_ID).toString()));
        generalInfoModel.setRootDisId(relativeData.get(Constants.ROOT_DISPATCH_ID) == null ? -1L : Long.valueOf(relativeData.get(Constants.ROOT_DISPATCH_ID).toString()));
        generalInfoModel.setParentDisId(relativeData.get(Constants.PARENT_DISPATCH_ID) == null ? -1L : Long.valueOf(relativeData.get(Constants.PARENT_DISPATCH_ID).toString()));
        generalInfoModel.setDispatchId(relativeData.get(Constants.DISPATCH_ID) == null ? -1L : Long.valueOf(relativeData.get(Constants.DISPATCH_ID).toString()));
        Long objectId = baseForm.getObjectId();
        String objectTable = HibernateBeanUtils.getTableName(baseForm.getClass());

        generalInfoModel.setProcessingObjectID(objectId);
        generalInfoModel.setProcessingObjectTable(objectTable);
        generalInfoModel.setOperTypeEnumId(operTypeEnumId);
        generalInfoModel.setOperDesc(baseForm.getOperDesc());
        saveGeneralInfo(generalInfoModel, taskInstance, userEntity);

    }

    @Override
    public Pager queryWorkOrderList(Pager pager, UserEntity userEntity) throws ServiceException {

        Map orgInfo = null;
        try {
            orgInfo = PubFun.getOrgInfoByOrgID(Integer.parseInt(AAAAAdapter.getCompany(userEntity.getOrgID().intValue()).getOrgId().toString()));
        } catch (Exception e) {
            e.printStackTrace();
            orgInfo = PubFun.getOrgInfoByUserEntity(userEntity);
        }
        Integer belongedProvince = orgInfo.get(PubFun.BELONGEDPROVINCE) == null ? null : Integer.valueOf(orgInfo.get(PubFun.BELONGEDPROVINCE).toString());
        Integer belongedCity = orgInfo.get(PubFun.BELONGEDCITY) == null ? null : Integer.valueOf(orgInfo.get(PubFun.BELONGEDCITY).toString());
        /*if (workOrderQuery.getOrgFilter() != null) {
            orgInfo = PubFun.getOrgInfoByOrgID(workOrderQuery.getOrgFilter());
        } else {
            orgInfo = PubFun.getOrgInfoByUserEntity(userEntity);
        }*/

        Map parameters = new HashMap();


        Map<String, Object> params = pager.getFastQueryParameters();
        String theme = StringUtils.EMPTY;
        String orgId = StringUtils.EMPTY;
        String workordernumber = StringUtils.EMPTY;
        if (params.size() > 0) {
            theme = params.get("lk_theme").toString();
            orgId = params.get("lk_flowing_object_id").toString();
            workordernumber = params.get("lk_workordernumber").toString();
        }

        StringBuffer hql = new StringBuffer();
        hql.append("select * from (");
        if (!"".equals(Constants.APP_SQL)) {
            hql.append(Constants.APP_SQL);
            hql.append("select distinct apply_id from t_eom_general_info where belong_Province_Code = " + belongedProvince);
            parameters.put("belongProvinceCode", belongedProvince);
            if (belongedCity != null) {
                hql.append(" and BELONG_CITY_CODE = " + belongedCity);
                parameters.put("belongCityCode", belongedCity);
            }
            hql.append(")");
            if (StringUtils.isNotBlank(workordernumber)) {
                hql.append(" and dis_Order_Number like '%" + workordernumber + "%'");
            }
            if (StringUtils.isNotBlank(theme)) {
                hql.append(" and theme like '%" + theme + "%'");
            }
            if (StringUtils.isNotBlank(orgId)) {
                hql.append(" and oper_org_id = " + orgId);
            }
//            hql.append(")");
            hql.append("union ");
        }
        if (!"".equals(Constants.DIS_SQL)) {
            hql.append(Constants.DIS_SQL);
//            hql.append("select PRO_ID as objectId, theme , dis_Order_Number as workordernumber , '申请单' as tabletype , req_Fdbk_Time as reqFdbkTime, creation_Time as creationTime , oper_Org_Name as operOrgName , root_pro_Inst_Id as rootProInstId , ISSUE_TIME as issueTime , ISSUE_USER_TRUE_NAME as issueUserTrueName from ");
//            hql.append(Constants.DIS_TABLE);
//            hql.append(" where PRO_ID in(");

            hql.append(" select distinct DISPATCH_ID from t_eom_general_info where belong_Province_Code = " + belongedProvince);
            parameters.put("belongProvinceCode", belongedProvince);
            if (belongedCity != null) {
                hql.append(" and BELONG_CITY_CODE = " + belongedCity);
                parameters.put("belongCityCode", belongedCity);
            }
            hql.append(")");
            if (StringUtils.isNotBlank(workordernumber)) {
                hql.append(" and dis_Order_Number like '%" + workordernumber + "%'");
            }
            if (StringUtils.isNotBlank(theme)) {
                hql.append(" and theme like '%" + theme + "%'");
            }
            if (StringUtils.isNotBlank(orgId)) {
                hql.append(" and oper_org_id = " + orgId);
            }
        }
        hql.append(")tmp order by creationTime desc");
        try {
            //            pager = baseDAO.findByParametersPage(pager , hql.toString() , parameters);
            pager = baseDAO.findNativeSQL(hql.toString().replace("#appTable#", "申请单").replace("#disTable#", "调度单"), null, pager);
            /*List finanList = new ArrayList();
            List list = pager.getExhibitDatas();
            for(Object obj : list){
                Map map = new HashMap();
                Object[] objArray = (Object[]) obj;
                map.put("theme" , objArray[0]);
                map.put("workordernumber" , objArray[1]);
                map.put("tabletype" , objArray[2]);
                map.put("reqFdbkTime" , objArray[3]);
                map.put("creationTime" , objArray[4]);
                map.put("operOrgName" , objArray[5]);
                finanList.add(map);
            }
            pager.setExhibitDatas(finanList);*/
            pager.setIsSuccess(true);
            return pager;
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return pager;
    }


    public TaskInstance getTaskInstance2(String processId, String accountId) throws ServiceException {
        try {
            List<ActivityInstance> acts = WorkflowAdapter.getActivityInstances(accountId, processId);
            if (acts != null && acts.size() > 0) {
                String activityInstID = "";
                if (acts.size() >= 2) {
                    boolean isGo = true;
                    int i = 1;
                    while (isGo && i < acts.size()) {
                        if (acts.get(i).getActivityType().equals("manual")) {
                            activityInstID = acts.get(i).getActivityInstID();//由于getActivityInstances()已按时间排序，直接取acts.get(1)即可。
                            isGo = false;
                        } else {
                            i++;
                        }
                    }

                } else {
                    activityInstID = acts.get(acts.size() - 1).getActivityInstID();//由于getActivityInstances()已按时间排序，直接取acts.get(1)即可。
                }

                List<TaskInstance> list = WorkflowAdapter.getTaskInstancesByActivityID(accountId, activityInstID);
                if (list != null && list.size() > 0) {
                    return list.get(0);
                }

            }
        } catch (Exception e) {

        }
        return new TaskInstance();
    }


    public void queryAppAndDisByRootProcess(HttpServletRequest request, String rootProcessInstID) throws ServiceException, DAOException {
        AppCommonModel appModel = null;
        DisCommonModel disModel = null;
        //查询申请单
        String sql_app = " from " + Constants.APP_MODEL + " A where A.processInstId=? ";
        List<AppCommonModel> appCommonModels = baseDAO.find(sql_app, new Object[]{rootProcessInstID});
        if (appCommonModels != null && appCommonModels.size() > 0)
            appModel = appCommonModels.get(0);
        //调度单
        String sql_dis = " from " + Constants.DIS_MODEL + " A where A.processInstId=? ";
        List<DisCommonModel> disCommonModels = baseDAO.find(sql_dis, new Object[]{rootProcessInstID});
        if (disCommonModels != null && disCommonModels.size() > 0)
            disModel = disCommonModels.get(0);
        request.setAttribute("taskAppForm", appModel);
        request.setAttribute("tEomTaskDisForm", disModel);
    }
}