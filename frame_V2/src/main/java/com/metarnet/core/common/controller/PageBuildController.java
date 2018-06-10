package com.metarnet.core.common.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.metarnet.core.common.adapter.WorkflowAdapter;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.core.common.model.*;
import com.metarnet.core.common.service.IGenerateTabService;
import com.metarnet.core.common.service.IWorkflowBaseService;
import com.metarnet.core.common.utils.BeanUtils;
import com.metarnet.core.common.utils.Constants;
import com.metarnet.core.common.utils.SpringContextUtils;
import com.sun.xml.internal.ws.util.UtilException;
import com.ucloud.paas.agent.PaasException;
import com.unicom.ucloud.workflow.filters.TaskFilter;
import com.unicom.ucloud.workflow.objects.ActivityInstance;
import com.unicom.ucloud.workflow.objects.PageCondition;
import com.unicom.ucloud.workflow.objects.ProcessInstance;
import com.unicom.ucloud.workflow.objects.TaskInstance;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: metarnet
 * Date: 13-2-5
 * Time: 下午4:25
 * 页面生成Controller,
 */
@Controller("pageBuildController")
public class PageBuildController extends BaseController {
    Logger logger = LogManager.getLogger(PageBuildController.class);
    private final static String WAITING = "waiting";
    private final static String AREAS = "areas";
    private final static String TABS = "tabs";
    private final static String LINKS = "links";
    private final static String MANUAL = "manual";
    private final static String CountFlag = "+";
    private final static String UNREAD = "unread";
    private final static String READED = "readed";
    @Resource
    private IWorkflowBaseService workflowBaseService;


    //递归方式
    public static enum RecursionType {
        UP,//向上递归
        DOWN,//向下递归
        ALL, //上下都递归
        NEVER,//不递归
        FORCE_DOWN  //强制递归
    }

    private enum JavaProcessControl {
        RETURN,
        CONTINUE
    }


    /**
     * 根据流程任务实例信息，动态生成页卡及操作按钮
     *
     * @param taskInstance 任务实例
     * @param type         类型 在办:waiting ,已办:  completed
     * @param response
     * @param request
     * @throws UIException
     */
    @RequestMapping(value = "/pageBuild.do", params = "method=buildTabs")
    public void buildTabs(TaskInstance taskInstance, String type, HttpServletResponse response,
                          HttpServletRequest request) throws UIException {
        JSONObject json = new JSONObject();
        try {
            LinkedHashMap<String, AreaModel> areaModels = new LinkedHashMap();
            parseProcess(request, areaModels, taskInstance.getProcessInstID(), RecursionType.ALL, new AreaNameCount(),
                    WorkflowAdapter.getProcessInstance(getAccountId(request), taskInstance.getProcessInstID()),
                    Constants.SHOW_FIRST, new HashMap(),taskInstance.getShard());
//            if (StringUtils.isNotEmpty(taskInstance.getTaskInstID())) {
            parseCurrentTaskInstance(taskInstance, type, request, json, areaModels);
//            }
            LinkedHashMap tabs = new LinkedHashMap();
//            LinkedHashMap tabItem = new LinkedHashMap();
//            tabItem.put("path","/frame/formFrame.jsp");
//
            //          tabs.put(areaModels.keySet().toArray()[0], "frame/formFrame.jsp?type=waiting&method=build");
            Integer shard = StringUtils.isEmpty(taskInstance.getShard()) ? null : Integer.valueOf(taskInstance
                    .getShard());
            List<Tab> tabList = ((IGenerateTabService) SpringContextUtils.getBean("tabsService")).generateTabs
                    (getUserEntity(request),
                            taskInstance.getRootProcessInstId(), shard);
            for (Tab tab : tabList) {
                tabs.put(tab.getName(), tab.getUrl());
            }
            json.put(TABS, tabs);
            endHandle(request, response, json, taskInstance.getTaskInstID());
        } catch (Exception e) {
            throw new UIException(e);
        }
    }

    /**
     * 新建工单
     *
     * @param response
     * @param request
     */
    @RequestMapping(value = "/pageBuild.do", params = "method=create")
    public void create(String processModelName, HttpServletResponse response,
                       HttpServletRequest request) throws UIException {
        try {
            ActivityModel startActivityModel = ExtendNodeCofnig.parseActivity(getAccountId(request),
                    processModelName, Constants.START_ACTIVITY);
            JSONObject json = new JSONObject();
            LinkedHashMap<String, AreaModel> areaModels = new LinkedHashMap();
            AreaModel areaModel = new AreaModel(startActivityModel.getAreaName());
            areaModel.setAreaName(parseExp(request, areaModel.getAreaName(), null));
            areaModel.getComponentModels().add(getComp(request, startActivityModel.getComponent(),
                    ComponentModel.EDIT, null, null, new HashMap(),null));
            areaModels.put(startActivityModel.getAreaName(), areaModel);
//            parseComponent(areaModels, areaModel.getComponentModels().get(0), request);
            json.put(LINKS, startActivityModel.getEditLinkList());
            json.put(AREAS, areaModels);
            endHandle(request, response, json, processModelName);
        } catch (Exception e) {
            throw new UIException(e);
        }
    }

    /**
     * 获取可编辑的组件，为了不改变缓存，返回一个克隆对象
     *
     * @param request
     * @param componentModel
     * @param type
     * @param processInstID
     * @param activityDefID  当前活动定义ID
     * @return
     */
    private ComponentModel getComp(HttpServletRequest request, ComponentModel componentModel, String type,
                                   String processInstID, String activityDefID, Map<String,
            ArrayList<String>> customActivityMap,String shardingId) throws AdapterException,
            UIException, ServiceException, PaasException, UtilException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        if (componentModel == null) {
            return null;
        }
        ComponentModel newComponentMode = new ComponentModel();
        BeanUtils.copyProperties(componentModel, newComponentMode, null, new String[]{"expands"});
        newComponentMode.setType(type);
        newComponentMode.setProcessInstID(processInstID);
        newComponentMode.setActivityDefID(activityDefID);
        String component = parseExp(request, componentModel.getComponent(), processInstID);

        if (StringUtils.isEmpty(component)) {
            return null;
        }
        newComponentMode.setComponent(component);
        //屏蔽此功能，在流程监控中可以看到转办、转派等信息，此功能涉及到审核时会有问题，因为审核是一个组件显示多个审核
        //步骤，而下面代码只能加载第一个审核的转办等信息，如有需要，后期需完善。

        //查询当前环节是否有转派，转办，签发等。
//        if (null != activityDefID) {
//            Set<ComponentExpand> componentExpands = queryCustomActivity(processInstID, activityDefID,
//                    customActivityMap,shardingId);
//            if (componentExpands != null && !ComponentExpand.COMINFOLIST.equals(newComponentMode.getComponent())) {
//                newComponentMode.getExpands().addAll(componentExpands);
//            }
//        }

        return newComponentMode;
    }

    /**
     * 获取自定义环节，如转派 转办 会签
     *
     * @param processInstID
     * @param activityDefID
     * @return
     * @throws ServiceException
     * @throws com.ucloud.paas.agent.PaasException
     */
    private Set<ComponentExpand> queryCustomActivity(String processInstID, String activityDefID, Map<String,
            ArrayList<String>> customActivityMap,String shardingId) throws AdapterException, ServiceException,
            PaasException, IllegalAccessException, IntrospectionException, InvocationTargetException, UtilException {
        return queryCustomActivity(processInstID, activityDefID, true, customActivityMap,shardingId);
    }

    /**
     * 获取自定义环节，如转派 转办 会签
     *
     * @param processInstID
     * @param activityDefID
     * @param recursionTag
     * @return
     * @throws ServiceException
     * @throws com.ucloud.paas.agent.PaasException
     */
    private Set<ComponentExpand> queryCustomActivity(String processInstID, String activityDefID, boolean recursionTag,
                                                     Map<String, ArrayList<String>> customActivityMap,String shardingId)
            throws ServiceException, PaasException, AdapterException, IllegalAccessException, IntrospectionException, InvocationTargetException, UtilException {
        /*Set<ComponentExpand> componentExpandSet = new HashSet<ComponentExpand>();
        //
        if (customActivityMap.containsKey(processInstID)) {
            if (!customActivityMap.get(processInstID).contains(activityDefID)) {
                return null;
            }
            Map expMap = new HashMap();
            expMap.put("activityInstID", activityDefID);
            expMap.put("processInstID", Long.valueOf(processInstID));
            componentExpandSet.add(new ComponentExpand(ComponentExpand.COMINFOLIST, expMap));
            return componentExpandSet;
        }
        TEomGenProcessingInfoRec tEomGenProcessingInfoRec = new TEomGenProcessingInfoRec();
        tEomGenProcessingInfoRec.setProcessingObjectId(Long.valueOf(processInstID));
        if(Constants.IS_SHARDING&&StringUtils.isNotEmpty(shardingId)){
            HibernateBeanUtils.recursionEvaluate(tEomGenProcessingInfoRec, Constants.ADB_SHARDING_ID,
                    Integer.valueOf(shardingId));
        }
        List<TEomGenProcessingInfoRec> tEomFlowingObjProcInsRels = workflowBaseService
                .getGeneralProcList(tEomGenProcessingInfoRec, null);
        if (tEomFlowingObjProcInsRels == null && tEomFlowingObjProcInsRels.size() == 0) {
            customActivityMap.put(processInstID, new ArrayList());
        }
        for (TEomGenProcessingInfoRec tegpir : tEomFlowingObjProcInsRels) {
            if (!customActivityMap.containsKey(tegpir.getProcessingObjectId())) {
                customActivityMap.put(String.valueOf(tegpir.getProcessingObjectId()), new ArrayList<String>());
            }
            if (!customActivityMap.get(String.valueOf(tegpir.getProcessingObjectId())).contains(tegpir
                    .getProcessingObjectTable())) {
                customActivityMap.get(String.valueOf(tegpir.getProcessingObjectId())).add(tegpir
                        .getProcessingObjectTable());
            }
        }*/
        if (!recursionTag) {
            return null;
        }
        return queryCustomActivity(processInstID, activityDefID, false, customActivityMap,shardingId);
    }

    /**
     * 获取单个component
     *
     * @param taskInstId 任务实例
     * @param component  页面组件
     * @param response
     * @param request
     * @throws UIException
     */
    @RequestMapping(value = "/pageBuild.do", params = "method=buildComponent")
    public void buildComponent(String taskInstId, String processInstanceId, String component,
                               HttpServletResponse response, HttpServletRequest request) throws UIException {

        TaskInstance taskInstance = new TaskInstance();
        try {
            ComponentModel componentModel = new ComponentModel();
            //页面组件获取有三种方式，1是开始环节通过流程引擎配置获取，2是保存日志时有可能没有保存component，通常这种
            // 情况，3是直接读取taskInstance中的扩展字段
            if (StringUtils.isNotEmpty(component)) {
                componentModel = new ComponentModel(component, ComponentModel.SHOW, processInstanceId, null);
                endHandle(request, response, JSON.toJSONString(componentModel), null , false);
                return;
            }
            if (StringUtils.isEmpty(taskInstId)) {
                taskInstance.setActivityDefID(Constants.START_ACTIVITY);
                taskInstance.setProcessModelName(String.valueOf(WorkflowAdapter.getProcessInstance(getAccountId
                        (request), processInstanceId).getProcessModelName()));
                taskInstance.setShard("1");
            } else if (StringUtils.isEmpty(component)) {
                taskInstance = WorkflowAdapter.getTaskInstanceObject(getAccountId(request), taskInstId);
            }
            ActivityModel activityModel = getActivityAttribute(taskInstance, request);
            componentModel = getComp(request, activityModel.getComponent(), ComponentModel.SHOW,
                    taskInstance.getProcessInstID(), null, new HashMap(),taskInstance.getShard());
            componentModel.setDraft(false);
            endHandle(request, response, JSON.toJSONString(componentModel), null , false);
        } catch (Exception e) {
            if (null == taskInstance) {
                throw new UIException("", e);
            }
            throw new UIException("获取页面组件失败 ProcessModelName=" + taskInstance.getProcessModelId() + " " +
                    "activityDefID=" + taskInstance.getActivityDefID(), e);
        }
    }

    /**
     * 根据流程任务实例信息，动态获取页面信息
     *
     * @param taskInstance 任务实例
     * @param type         类型 在办:waiting ,已办:  completed
     * @param response
     * @param request
     * @throws UIException
     */
    @RequestMapping(value = "/pageBuild.do", params = "method=build")
    public void build(TaskInstance taskInstance, String type, HttpServletResponse response,
                      HttpServletRequest request) throws UIException {
        try {
            String activityInstName = taskInstance.getActivityInstName();
            String jobTitle = taskInstance.getJobTitle();
            if(activityInstName != null && !"".equals(activityInstName)){
                taskInstance.setActivityInstName(java.net.URLDecoder.decode(activityInstName , "UTF-8"));
            }
            if(jobTitle != null && !"".equals(jobTitle)){
                taskInstance.setJobTitle(java.net.URLDecoder.decode(jobTitle , "UTF-8"));
            }
            TaskFilter taskFilter = new TaskFilter();
            taskFilter.setProcessInstID(taskInstance.getProcessInstID());
            PageCondition page = new PageCondition();
            page.setBegin(0);
            page.setLength(1);
            page.setIsCount(true);
            taskFilter.setPageCondition(page);
            List<TaskInstance> list = null;
            if(taskInstance.getTaskInstID().startsWith("-")){
                if("waiting".equals(type)) {
                    long startTime = System.currentTimeMillis();
                    list  = WorkflowAdapter.getMyWaitingTasks(taskFilter , getUserEntity(request).getUserName());
                    logger.info("getMyWaitingTasks cost : " + (System.currentTimeMillis() - startTime));
                } else {
                    list =  WorkflowAdapter.getMyCompletedTasks(taskFilter , getUserEntity(request).getUserName());
                }
                if(list != null && list.size() > 0){
                    taskInstance = list.get(0);
                }
            }


//            endHandle(request, response, build(taskInstance,type,request), taskInstance.getTaskInstID());
            request.setAttribute("__buildJSON" , build(taskInstance,type,request));
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            request.setAttribute("__taskInstance" , JSON.toJSONString(taskInstance, ser, SerializerFeature.WriteNullListAsEmpty));
            request.getRequestDispatcher("base/frame/frame.jsp").forward(request , response);
        } catch (Exception e) {
            throw new UIException(e);
        }
    }

    public JSONObject build(TaskInstance taskInstance,String type,HttpServletRequest request) throws UIException{
        JSONObject json = new JSONObject();
        try {
            Date start = new Date();
            String  accountId = getAccountId(request);
            if (StringUtils.isEmpty(taskInstance.getActivityInstID())) {
                taskInstance = WorkflowAdapter.getTaskInstanceObject(accountId,
                        taskInstance.getTaskInstID());
            }
            LinkedHashMap<String, AreaModel> areaModels = new LinkedHashMap();
            parseProcess(request, areaModels, taskInstance.getProcessInstID(), RecursionType.ALL, new AreaNameCount(),
                    WorkflowAdapter.getProcessInstance(accountId, taskInstance.getProcessInstID()),
                    Constants.SHOW_FIRST, new HashMap(),taskInstance.getShard());
            if (StringUtils.isNotEmpty(taskInstance.getTaskInstID())) {
                parseCurrentTaskInstance(taskInstance, type, request, json, areaModels);
            }
            json.put(AREAS, areaModels);
            logger.info("Finish build - cost : " + (new Date().getTime() - start.getTime()));
            return json;
        } catch (Exception e) {
            throw new UIException(e);
        }

    }

    /**
     * 工单查询显示工单详情
     *
     * @param taskInstance
     * @param response
     * @param request
     * @throws UIException
     */
    @RequestMapping(value = "/pageBuild.do", params = "method=buildQueryDetail")
    public void buildQueryDetail(TaskInstance taskInstance, HttpServletResponse response,
                                 HttpServletRequest request) throws UIException {
        JSONObject json = new JSONObject();
        try {
            ProcessInstance processInstance = WorkflowAdapter.getProcessInstance(getAccountId(request), taskInstance.getProcessInstID());
            LinkedHashMap<String, AreaModel> areaModels = new LinkedHashMap();
            parseProcess(request, areaModels, taskInstance.getProcessInstID(), RecursionType.UP,
                    new AreaNameCount(), processInstance, Boolean.FALSE, new HashMap(),taskInstance.getShard());
            AreaModel areaModel = ExtendNodeCofnig.parseProcess(WorkflowAdapter.getActivityExtendAttributes
                    (getAccountId(request), processInstance.getProcessModelName(), null));
            json.put(LINKS, areaModel.getShowLinkList());
            json.put(AREAS, areaModels);
            request.setAttribute("__buildJSON" , json);
            String rootProcessInstId = WorkflowAdapter.getRootProcessInstance(getAccountId(request) , taskInstance.getProcessInstID()).getProcessInstID();
            taskInstance.setRootProcessInstId(rootProcessInstId);
            request.setAttribute("__taskInstance" , JSON.toJSONString(taskInstance));
            request.getRequestDispatcher("base/frame/frame.jsp").forward(request , response);
//            endHandle(request, response, json, taskInstance.getTaskInstID());
        } catch (Exception e) {
            throw new UIException(e);
        }
    }

    /**
     * 解析component，存在一个环节配置多个组件的情况 TaskAppForm:processStart=appSave,TaskDispatchForm:processStart=appSave
     * <p/>
     * 当整个页面只有一个component时，设置该页面为只读。
     *
     * @param areaModels       区域列表
     * @param currentComponent 当前步骤的组件
     * @param request
     * @throws AdapterException
     * @throws UIException
     */
    private void parseComponent(LinkedHashMap<String, AreaModel> areaModels, ComponentModel currentComponent,
                                HttpServletRequest request) throws AdapterException, UIException {
        int count = 0;
        for (AreaModel areaModel : areaModels.values()) {
            for (ComponentModel componentModel : areaModel.getComponentModels()) {
                count++;
            }
        }
        //只有一个页面组件，并且是当前环节的组件时，显示为edit
        if (count == 1 && ((((AreaModel) areaModels.values().toArray()[0]).getComponentModels().get(0).equals
                (currentComponent)))) {
            ((AreaModel) areaModels.values().toArray()[0]).getComponentModels().get(0).setType(ComponentModel.EDIT);
        }
    }


    /**
     * 解析流程建模定义的表达式，areaName、component都可以定义。表达式如下：
     * TaskAppForm:processStart=[appSave||appSubmit],TaskDispatchForm:processStart=[submit||null]
     *
     * @param request
     * @param obj           表达式
     * @param processInstID 流程实例ID
     * @return
     * @throws AdapterException
     * @throws UIException
     */
    private String parseExp(HttpServletRequest request, String obj, String processInstID) throws AdapterException,
            UIException {
        if(obj!=null){
            if (obj.contains(",")) {
                String[] comps = obj.split(",");
                for (String comp : comps) {
                    String[] exps = comp.split(":");
                    String[] params = exps[1].split("=");
                    //String[] value = params[1].replace("[", "").replace("]", "").split("\\|\\|");
                    String[] value = params[1].replace("[", "").replace("]", "").split("\\|\\|",-1);
                    Map<String, Object> map = new HashMap();
                    if (StringUtils.isEmpty(processInstID)) {
                        map.put(params[0], request.getParameter(params[0]));
                    } else {
                        map = WorkflowAdapter.getRelativeData(processInstID, Arrays.asList(new String[]{params[0]}),
                                getAccountId(request));
                    }
                    if ((params[1].equals("null") && StringUtils.isEmpty((String) map.get(params[0]))) || Arrays.asList
                            (value).contains(map.get(params[0])) || (!map.containsKey(params[0]) && Arrays.asList(value)
                            .contains("null"))) {
                        if (exps[0].contains("{")) {
                            return parseRelExp(exps[0], processInstID, getAccountId(request));
                        }
                        return "null".equals(exps[0]) ? null : exps[0];
                    }
                }
            }
        }
        if (StringUtils.isNotEmpty(obj) && obj.contains("{")) {
            return parseRelExp(obj, processInstID, getAccountId(request));
        }
        return obj;
    }

    /**
     * 解析相关数据区表达式
     *
     * @param accountId
     * @param exp
     * @return
     */
    public String parseRelExp(String exp, String processInstID, String accountId) throws AdapterException {
        String areaName =StringUtils.EMPTY;
        int leftIndex = exp.indexOf("{");
        int rightIndex = exp.indexOf("}");
        String relativeData = exp.substring(leftIndex+1,rightIndex);
        if(StringUtils.isNotEmpty(processInstID)){
        Map<String, Object> map = new HashMap();
        map = WorkflowAdapter.getRelativeData(processInstID, Arrays.asList(new String[]{relativeData}), accountId);
        if(leftIndex!= NumberUtils.INTEGER_ZERO){
            areaName=exp.substring(0,leftIndex-1);
        }
        if(map.get(relativeData)!=null&&StringUtils.isNotEmpty(map.get(relativeData).toString())){
            areaName+=map.get(relativeData);
        }
        }
        if(rightIndex!=exp.length()){
            areaName+=exp.substring(rightIndex+1,exp.length());
        }
        return areaName;
    }


    /**
     * 解析当前任务
     *
     * @param taskInstance 当前任务
     * @param type         待办或一版
     * @param request
     * @param json         返回结果集
     * @param areaModels   生成的页面区域结果集
     * @throws ServiceException
     * @throws AdapterException
     * @throws UIException
     */
    private void parseCurrentTaskInstance(TaskInstance taskInstance, String type, HttpServletRequest request,
                                          JSONObject json, LinkedHashMap<String,
            AreaModel> areaModels) throws ServiceException, AdapterException, UIException, PaasException,
            UtilException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        ActivityModel currentActivityModel = getActivityAttribute(taskInstance, request);
        // 存在有的环节没有component的情况,比如当前只是修改之前某一环节的数据.
//        if (null != currentActivityModel.getComponent() && WAITING.equals(type)) {
//            currentActivityModel.getComponent().setProcessInstID(taskInstance.getProcessInstID());
////            /**草稿类型组件处理逻辑
////             1.历史步骤已经有该组件了，设置组件类型为edit
////             2.没有该组件时，追加到尾部
////             */
////
//            if (null != currentActivityModel.getComponent() && currentActivityModel.getComponent().getDraft() &&
//                    areaModels.containsKey(currentActivityModel.getAreaName()) && areaModels.size() == 1 && (
//                    (AreaModel) areaModels.values().toArray()[0]).getComponentModels().size() == 1) {
//
//                AreaModel areaModel = getAreaModel(currentActivityModel.getAreaName(), new AreaNameCount(),
//                        taskInstance.getProcessInstID());
////                if (!areaModels.containsKey(currentActivityModel.getAreaName())) {
////                    areaModels.put(areaModel.getAreaName(), new AreaModel(areaModel.getAreaName()));
////                }
//                LinkedList<ComponentModel> componentModels = areaModels.get(areaModel.getAreaName())
//                        .getComponentModels();
//                ComponentModel componentModel = currentActivityModel.getComponent();
//                //draft类型为true的设为EDIT
//                if (componentModels.contains(componentModel)) {
////                    if (componentModels.contains(componentModel)) {
//                    componentModels.add(componentModels.lastIndexOf(componentModel), getComp(request, componentModel,
//                            ComponentModel.EDIT, taskInstance.getProcessInstID(), null, new HashMap()));
//                    componentModels.removeLastOccurrence(componentModel);
////                    } else {
////                        componentModels.add(getComp(componentModel, ComponentModel.EDIT));
////                    }
//                }
//            }
//        }

        Boolean expandTag = false;

        ComponentModel currentComponentModel = getComp(request, currentActivityModel.getComponent(), type,
                taskInstance.getProcessInstID(), null, new HashMap(),taskInstance.getShard());
        //当前component已经被解析过了就不需要新建处理过程的component。
        if (!areaModels.containsKey(currentActivityModel.getAreaName()) || !areaModels.get(currentActivityModel
                .getAreaName()).getComponentModels().contains(currentComponentModel)) {

            //屏蔽此功能，在流程监控中可以看到转办、转派等信息，此功能涉及到审核时会有问题，因为审核是一个组件显示多个审核
            //步骤，而下面代码只能加载第一个审核的转办等信息，如有需要，后期需完善。

            //加载转派等信息
//            Set<ComponentExpand> componentExpands = queryCustomActivity(taskInstance.getProcessInstID(),
//                    taskInstance.getActivityDefID(), new HashMap<String, ArrayList<String>>(),taskInstance.getShard());
//            if (componentExpands != null) {
//                for (ComponentExpand componentExpand : componentExpands) {
//                    ((AreaModel) (areaModels.values().toArray()[areaModels.size() - 1])).getComponentModels().add(
//                            new ComponentModel(componentExpand.getCompName(), ComponentModel.SHOW,
//                                    taskInstance.getProcessInstID(), taskInstance.getActivityDefID()));
////                areaModels.get(currentActivityModel.getAreaName()).getComponentModels().add(new ComponentModel
////                        (componentExpand.getCompName(), ComponentModel.SHOW, false, taskInstance.getProcessInstID(),
////                                taskInstance.getActivityDefID()));
//                }
//            }
        }
//        parseComponent(areaModels, currentActivityModel.getComponent(), request);

        //待办 在办的连接不用
        if (WAITING.equals(type)) {
            if (null == currentActivityModel.getEditLinkList()) {
                logger.info("当前环节没有配置edit类型的link");
            }
            //待办、当前只有一个组件、且是当前组件时显示为edit

            LinkedList<ComponentModel> componentModels = areaModels.values().toArray(
                    new AreaModel[areaModels.size()])[0].getComponentModels();
            //编辑状态显示show的link
            if (areaModels.size() == 1 && componentModels.size() == 1 && componentModels.get(0).equals
                    (currentComponentModel)) {
                componentModels.get(0).setType(ComponentModel.EDIT);
                json.put(LINKS, getLinks(request, currentActivityModel.getShowLinkList(), taskInstance));
            } else {
                json.put(LINKS, getLinks(request, currentActivityModel.getEditLinkList(), taskInstance));
            }
        } else  {
            json.put(LINKS, getLinks(request, currentActivityModel.getShowLinkList(), taskInstance));
        }

    }

    /**
     * 创建link
     *
     * @param request
     * @param linkList
     * @param taskInstance
     * @return
     * @throws AdapterException
     * @throws UIException
     */
    private List<String> getLinks(HttpServletRequest request, List<String> linkList,
                                  TaskInstance taskInstance) throws AdapterException, UIException {
        if (linkList == null) {
            return null;
        }
        List<String> newLinkList = new ArrayList();
        for (String link : linkList) {
            if (!link.contains(":")) {
                if(!newLinkList.contains(link)){
                    newLinkList.add(link);
                }
                continue;
            }
            String[] exps = link.split(":");
            String[] params = exps[1].split("=");
            //String[] value = params[1].replace("[", "").replace("]", "").split("\\|\\|");
            String[] value = params[1].replace("[", "").replace("]", "").split("\\|\\|",-1);
            Map<String, Object> map = new HashMap();
            if (StringUtils.isEmpty(taskInstance.getProcessInstID())) {
                map.put(params[0], request.getParameter(params[0]));
            } else {
                map = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(),
                        Arrays.asList(new String[]{params[0]}), getAccountId(request));
            }
            if ((params[1].equals("null") && StringUtils.isEmpty((String) map.get(params[0]))) || Arrays.asList
                    (value).contains(map.get(params[0])) || (!map.containsKey(params[0]) && Arrays.asList(value)
                    .contains("null"))) {
                String newLink = exps[0];
                if (StringUtils.isNotEmpty(newLink) && !"null".equals(newLink)&&!newLinkList.contains(newLink)) {
                    newLinkList.add(newLink);
                }
            }
        }
        return newLinkList;
    }

    public List<String> getLinks(HttpServletRequest request,TaskInstance taskInstance,String showOrEdit)throws Exception{
        ActivityModel currentActivityModel = getActivityAttribute(taskInstance, request);
        if("show".equals(showOrEdit)){
           return getLinks(request,currentActivityModel.getShowLinkList(),taskInstance);
        }else{
            return getLinks(request,currentActivityModel.getEditLinkList(),taskInstance);
        }
    }
    /**
     * 根据流程模型增加配置信息
     *
     * @param request
     * @param areaModelMap           区域列表
     * @param processInstID          流程模型
     * @param recursionType          是否向上、下递归
     * @param areaCounter            区域计数器
     * @param currentProcessInstance 当前流程实例
     * @param showFirst              是否只显示第一个area
     * @throws AdapterException
     * @throws ServiceException
     */

    private void parseProcess(HttpServletRequest request, LinkedHashMap<String, AreaModel> areaModelMap,
                              String processInstID, RecursionType recursionType, AreaNameCount areaCounter,
                              ProcessInstance currentProcessInstance, Boolean showFirst, Map<String,
            ArrayList<String>> customActivityMap,String shardingId) throws AdapterException, ServiceException, UIException,
            PaasException, UtilException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        parseProcess(request, areaModelMap, processInstID, recursionType, areaCounter, currentProcessInstance, showFirst,
                customActivityMap, new ArrayList(),shardingId);
    }

    /**
     * 根据流程模型增加配置信息
     *
     * @param request
     * @param areaModelMap           区域列表
     * @param processInstID          流程模型
     * @param recursionType          是否向上、下递归
     * @param areaCounter            区域计数器
     * @param currentProcessInstance 当前流程实例
     * @param showFirst              是否只显示第一个area
     * @param processInstIdList      用于存储哪些需要
     * @throws AdapterException
     * @throws ServiceException
     */

    private void parseProcess(HttpServletRequest request, LinkedHashMap<String, AreaModel> areaModelMap,
                              String processInstID, RecursionType recursionType, AreaNameCount areaCounter,
                              ProcessInstance currentProcessInstance, Boolean showFirst, Map<String,
            ArrayList<String>> customActivityMap, List<String> processInstIdList,String shardingId) throws
            AdapterException, ServiceException, UIException, PaasException, UtilException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        if (null == processInstIdList) {
            processInstIdList = new ArrayList();
        }
        if (processInstIdList.contains(processInstID)) {
            return;
        }
        Date start_get_process = new Date();
        logger.info("Start to getProcessInstance...");
        ProcessInstance processInstance = WorkflowAdapter.getProcessInstance(getAccountId(request), processInstID);
        logger.info("Complete getProcessInstance.../COST = " + (new Date().getTime() - start_get_process.getTime()));
        if (StringUtils.isNotEmpty(processInstance.getParentProcessInstID()) && !"0".equals(processInstance
                .getParentProcessInstID()) && (RecursionType.ALL.equals(recursionType) || RecursionType.UP.equals
                (recursionType))) {
            parseProcess(request, areaModelMap, processInstance.getParentProcessInstID(), recursionType, areaCounter,
                    currentProcessInstance, showFirst, customActivityMap, processInstIdList,shardingId);
        }
        //获取历史步骤
        Date start_get_act = new Date();
        logger.info("Start to getActivityInstances...");
        List<ActivityInstance> activityInstances = WorkflowAdapter.getActivityInstances(getAccountId(request), processInstID);
        logger.info("Complete getActivityInstances.../COST = " + (new Date().getTime() - start_get_act.getTime()));
        if (null != activityInstances) {
            for (ActivityInstance activityInstance : activityInstances) {
//                //过滤当前环节，工单查询是子流程环节不过滤，目的是可以递归子流程
//                /**
//                 * 当前环节处理逻辑
//                 *
//                 * 是子流程环节	是工单查询	结果
//                 *  否	            否	    过滤
//                 *  是	            否	    过滤
//                 *  否	            是	    过滤
//                 *  是	            是	    不过滤
//                 *
//                 */
                if (Constants.ACT_RUN.equals(activityInstance.getCurrentState()) && !(Constants.ACT_TYPE_SUBFLOW.equals
                        (activityInstance.getActivityType()) && RecursionType.FORCE_DOWN.equals(recursionType))) {
                    continue;
                }
                //过滤停止环节
                if (Constants.ACT_STOP.equals(activityInstance.getCurrentState())) {
                    continue;
                }
                JavaProcessControl javaProcessControl = parseActivity(request, areaModelMap, processInstID,
                        recursionType, areaCounter, currentProcessInstance, showFirst, activityInstance,
                        customActivityMap,processInstIdList,shardingId);
                if (JavaProcessControl.RETURN.equals(javaProcessControl)) {
                    return;
                }
            }
        }
        //当程序是强制向下递归时，需要判断当前环节是否是子流程环节，如果是，需要递归
        if (RecursionType.FORCE_DOWN.equals(recursionType)) {

        }
        //5表示撤销流程，需要显示撤销信息
        if (Constants.PIS_STOP.equals(processInstance.getProcessInstStatus())) {
            ((AreaModel) (areaModelMap.values().toArray()[areaModelMap.size() - 1])).getComponentModels().add(new
                    ComponentModel("RecallForm", ComponentModel.SHOW, processInstID, null));
        }
    }

    /**
     * @param request
     * @param areaModelMap           区域列表
     * @param processInstID          流程模型
     * @param recursionType          是否向上、下递归
     * @param areaCounter            区域计数器
     * @param currentProcessInstance 当前流程实例
     * @param showFirst              是否只显示第一个area
     * @param activityInstance       流程定义id
     * @return
     * @throws UIException
     * @throws AdapterException
     * @throws ServiceException
     * @throws com.ucloud.paas.agent.PaasException
     * @throws com.sun.xml.internal.ws.util.UtilException
     */

    private JavaProcessControl parseActivity(HttpServletRequest request, LinkedHashMap<String, AreaModel> areaModelMap,
                                             String processInstID, RecursionType recursionType,
                                             AreaNameCount areaCounter, ProcessInstance currentProcessInstance,
                                             Boolean showFirst, ActivityInstance activityInstance, Map<String,
            ArrayList<String>> customActivityMap, List processInstIdList,String shardingId) throws UIException,
            AdapterException, ServiceException, PaasException, UtilException, IllegalAccessException, IntrospectionException, InvocationTargetException {

        /**
         *  非人工环节排除掉,两种情况除外
         *  1.开始环节不排除
         *  2.该环节是子流程,并且扩展属性中定义了showSubProcess的显示子流程
         *  3.如果当前环节正处在定义了showSubProcess的子流程中,则逻辑2失效,因为子流程间是并列关系,不应看见彼此.
         *  4.普元返回当前环节，需除掉
         */
        if ((!MANUAL.equals(activityInstance.getActivityType()) && !Constants.ACT_TYPE_START.equals(activityInstance
                .getActivityType()) && !Constants.ACT_TYPE_SUBFLOW.equals(activityInstance.getActivityType()))) {
            return JavaProcessControl.CONTINUE;
        }
        ActivityModel activityAttribute = getActivityAttribute(activityInstance, request);
        if (Constants.ACT_TYPE_SUBFLOW.equals(activityInstance.getActivityType())) {
            Date start_get_subPro = new Date();
            logger.info("Start to getSubProcessInstance...");
            List<ProcessInstance> subProcessInstances = WorkflowAdapter.getSubProcessInstance(getAccountId(request), processInstID);
            logger.info("Complete getSubProcessInstance.../COST = " + (new Date().getTime() - start_get_subPro.getTime()));
            if (!subProcessInstances.contains(currentProcessInstance)) {
                for (ProcessInstance subProcessInstance : subProcessInstances) {
                    if (RecursionType.ALL.equals(recursionType) && activityAttribute.getShowSubflow() != null
                            && subProcessInstance.getProcessModelName().contains(activityAttribute.getShowSubflow())) {
                        parseProcess(request, areaModelMap, subProcessInstance.getProcessInstID(),
                                RecursionType.NEVER, areaCounter, currentProcessInstance, showFirst,
                                customActivityMap, processInstIdList,shardingId);
                    }
                    //如果是RecursionType.FORCE_DOWN强制递归，通常发生在工单查询中使用，工单查询提供根流程的流程实例ID
                    if (RecursionType.FORCE_DOWN.equals(recursionType)) {
                        parseProcess(request, areaModelMap, subProcessInstance.getProcessInstID(),
                                RecursionType.FORCE_DOWN, areaCounter, currentProcessInstance, showFirst,
                                customActivityMap, processInstIdList,shardingId);
                    }
                }
            }
        }
        //这种情况只出现在工单查询时，主流程当前环节是子流程环节，此时不显示该环节的component
        if (Constants.ACT_TYPE_SUBFLOW.equals(activityInstance.getActivityType()) && Constants.ACT_RUN.equals
                (activityInstance.getActivityType())) {
            return JavaProcessControl.CONTINUE;
        }       //todo
//                List<TaskInstance> taskInstances = WorkflowAdapter.getTaskInstancesByActivityID(getUserId(request),
//                        activityInstance.getActivityInstID());
//                //目前任务状态为“终止”的，查询不出来
//                if (null == taskInstances || taskInstances.isEmpty()) {
//                    continue;
//                }
        ComponentModel componentModel = getComp(request, activityAttribute.getComponent(),
                ComponentModel.SHOW, processInstID, activityInstance.getActivityDefID(), customActivityMap,shardingId);
        //环节有可能没有配置component，但这是错误的
        if (null == componentModel) {
            return JavaProcessControl.CONTINUE;
        }

        AreaModel areaModel = getAreaModel(activityAttribute.getAreaName(), areaCounter, processInstID);
        String areaName = areaModel.getAreaName();
        if (areaName == null) {
            Object[] areaArray = areaModelMap.values().toArray();
            if(areaArray.length>0){
                areaName = ((AreaModel) areaArray[areaArray.length - 1]).getAreaName();
            }
        }
        areaName = parseExp(request, areaName, processInstID);
        if (null == areaName) {
            return JavaProcessControl.CONTINUE;
        }
        areaModel.setAreaName(areaName);

        //只显示第一个area的信息
        if (areaModelMap.size() > 0 && !areaModelMap.containsKey(areaModel.getAreaName()) && showFirst) {
            return JavaProcessControl.RETURN;
        }
        if (!areaModelMap.containsKey(areaModel.getAreaName())) {
            areaModelMap.put(areaModel.getAreaName(), areaModel);
        }
        LinkedList<ComponentModel> componentModels = areaModelMap.get(areaModel.getAreaName())
                .getComponentModels();
        //组件不会  加载两次
//        if (!componentModels.isEmpty() && componentModels.contains(componentModel)) {
//            return JavaProcessControl.CONTINUE;
//        }
        /**
         * 由于要排除特殊情况，如存在多个反馈审核列表（对应流程实例ID不通），这种情况要去重
         */
        if (!componentModels.isEmpty()){
            for(int i = 0 ; i < componentModels.size() ; i++){
                ComponentModel tmpCom = componentModels.get(i);
                if(tmpCom.equalsComponent(componentModel)){
                    tmpCom.setProcessInstID(componentModel.getProcessInstID());
                    return JavaProcessControl.CONTINUE;
                }
            }
        }

        componentModels.add(componentModel);
//                //加载转派组件
//                if (activityAttribute.getEditLinkList().contains(Links.FORWARDFORMLINK)) {
//                    componentModels.add(new ComponentModel(ComponentModel.FORWARDFORM, ComponentModel.SHOW, false,
//                            processInstID));
//                }
        return JavaProcessControl.CONTINUE;
    }

    /**
     * @param areaName
     * @return
     */
    private AreaModel getAreaModel(String areaName, AreaNameCount areaNameCount, String processInstID) {
        if (StringUtils.isEmpty(areaName) || !areaName.contains(CountFlag)) {
            return new AreaModel(areaName);
        }
        if (!areaNameCount.getAreaNameMap().containsKey(areaName + processInstID)) {
            Integer count = areaNameCount.getAreaCounter().get(areaName) == null ? 1 : areaNameCount.getAreaCounter()
                    .get(areaName) + 1;
            areaNameCount.getAreaCounter().put(areaName, count);
            String newAreaName = areaName.replace("+", areaNameCount.getAreaCounter().get(areaName) + "");
            areaNameCount.getAreaNameMap().put(areaName + processInstID, newAreaName);
        }
        return new AreaModel(areaNameCount.getAreaNameMap().get(areaName + processInstID));
    }


    /**
     * 根据待办信息获取配置信息
     *
     * @param taskInstance
     * @param request
     * @return
     * @throws ServiceException
     * @throws AdapterException
     */
    private ActivityModel getActivityAttribute(TaskInstance taskInstance, HttpServletRequest request) throws
            ServiceException, AdapterException, UIException {
        return ExtendNodeCofnig.parseActivity(getAccountId(request), taskInstance.getProcessModelName(),
                taskInstance.getActivityDefID());
    }

    /**
     * 根据待办信息获取配置信息
     *
     * @param activityInstance
     * @param request
     * @return
     * @throws ServiceException
     * @throws AdapterException
     */
    private ActivityModel getActivityAttribute(ActivityInstance activityInstance, HttpServletRequest request) throws
            ServiceException, AdapterException, UIException {
        ProcessInstance processInstance = WorkflowAdapter.getProcessInstance(getAccountId(request),
                activityInstance.getProcessInstID());
        return ExtendNodeCofnig.parseActivity(getAccountId(request), processInstance.getProcessModelName(),
                activityInstance.getActivityDefID());
    }


}

class AreaNameCount {
    private Map<String, Integer> areaCounter = new HashMap();
    /**
     * key=areaName+proccessInsId,value=areaName
     */
    private Map<String, String> areaNameMap = new HashMap();

    public Map<String, Integer> getAreaCounter() {
        return areaCounter;
    }

    public void setAreaCounter(Map<String, Integer> areaCounter) {
        this.areaCounter = areaCounter;
    }

    public Map<String, String> getAreaNameMap() {
        return areaNameMap;
    }

    public void setAreaNameMap(Map<String, String> areaNameMap) {
        this.areaNameMap = areaNameMap;
    }
}