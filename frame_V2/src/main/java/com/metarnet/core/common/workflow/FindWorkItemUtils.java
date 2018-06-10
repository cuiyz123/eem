package com.metarnet.core.common.workflow;

import com.eos.das.entity.DASManager;
import com.eos.das.entity.ExpressionHelper;
import com.eos.das.entity.IDASCriteria;
import com.eos.das.entity.criteria.CriteriaType;
import com.eos.das.entity.criteria.ExprType;
import com.eos.das.entity.criteria.LogicType;
import com.eos.workflow.api.BPSServiceClientFactory;
import com.eos.workflow.api.IBPSServiceClient;
import com.eos.workflow.api.IWFQueryManager;
import com.eos.workflow.data.WFWorkItem;
import com.eos.workflow.helper.ResultList;
import com.eos.workflow.omservice.WFParticipant;
import com.metarnet.core.common.exception.AdapterException;
import com.primeton.workflow.api.WFServiceException;
import commonj.sdo.DataObject;
import commonj.sdo.helper.DataFactory;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: hadoop
 * Date: 15-5-13
 * Time: 下午3:17
 * To change this template use File | Settings | File Templates.
 */
public class FindWorkItemUtils {
    private static Logger logger = Logger.getLogger(FindWorkItemUtils.class);

    static boolean pending = false;

    public static List<TaskInstance> getMyTasks(TaskFilter taskfilter, String userId)
            throws AdapterException
    {
        if (taskfilter.getPageCondition() == null) {
            throw new AdapterException("没有分页参数，会产生严重的性能问题。");
        }

        pending = System.getProperty("PENDING", "true").equals("true");
        println("查询挂起 " + pending);

        boolean sdo = System.getProperty("SDO", "true").equals("true");
        if (sdo) {
            println("SDO for getMyTasks ");
            return getMyTasks4SDO(taskfilter, userId);
        }
        println("SQL for getMyTasks ");
        return getMyTasks4SQL(taskfilter, userId);
    }

    public static List<TaskInstance> getMyTasks4SDO(TaskFilter taskfilter, String userId)
            throws AdapterException
    {
        List tasks = new ArrayList();
        try
        {
            IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();

            CriteriaType wf = (CriteriaType) CriteriaType.FACTORY.create();
            wf.set_entity("com.eos.workflow.data.WFWorkItem");
            CriteriaType bf = (CriteriaType) CriteriaType.FACTORY.create();
            bf.set_entity("com.eos.workflow.data.Wfbizinfo");
            bf.set_entity("com.eos.workflow.api.ext.bizdataset.WFBusiinfo");

            List tel = processWorkItemExpr(taskfilter);
            List bel = processBizExpr(taskfilter);

            processWorkItemFifter(taskfilter, tel, wf);
            processBizFifter(taskfilter, bel, bf);

            DataObject pageCond = processPageCondition(taskfilter);
            DataObject[] results = null;

            String taskType = taskfilter.getTaskType();
            if (taskType == null) {
                throw new AdapterException("taskType属性不能为空");
            }
            if (taskType.equals("2")) {
                wf.set("_orderby[1]/_property", "endTime");
                wf.set("_orderby[1]/_sort", "desc");
            }
            else if (taskType.equals("3")) {
                wf.set("_orderby[1]/_property", "workItemID");
                wf.set("_orderby[1]/_sort", "desc");
            } else if (taskType.equals("1")) {
                wf.set("_orderby[1]/_property", "workItemID");
                wf.set("_orderby[1]/_sort", "desc");
            }
            else if (taskType.equals("1_ORDERFINALTIME"))
            {
                String TypeName = "finalTime";

                ExprType ll = (ExprType) ExprType.FACTORY.create();
                ll.set_opEnum(ExprType.OP.NOT_NULL);
                ll.set_property(TypeName);

                wf.get_expr().add(ll);

                wf.set("_orderby[1]/_property", TypeName);
                wf.set("_orderby[1]/_sort", "asc");
            } else {
                throw new AdapterException("taskType属性 不支持" + taskType);
            }

            results = query4SDO(taskfilter, userId, client, wf, bf, pageCond, results);
            if ((results == null) || (results.length == 0))
                return tasks;
            for (DataObject data : results) {
                DataObject _pageCond = data.getDataObject("_pageCond");
                if (_pageCond != null)
                {
                    com.eos.foundation.PageCond pc = (com.eos.foundation.PageCond)_pageCond;
                    taskfilter.setPageCondition(WorkFlowDataConvertor.convert2EosPageCond(pc));
                }

                TaskInstance ti = dataObject2TaskInstace(data);

                tasks.add(ti);
            }

            println("结果集：" + results.length);
        } catch (WFServiceException e) {
            throw new AdapterException(e);
        }
        return tasks;
    }

    private static DataObject[] query4SDO(TaskFilter taskfilter, String userId, IBPSServiceClient client, CriteriaType wf, CriteriaType bf, DataObject pageCond, DataObject[] results)
            throws AdapterException, WFServiceException
    {
        String taskType = taskfilter.getTaskType();
        if (taskType == null) {
            throw new AdapterException("taskType属性不能为空");
        }
        String scope = "ALL";
        String permission = "ALL";

        String joinID = "processinstid";
        joinID = "workItemID,workItemID";
        if (taskType.equals("2"))
        {
            results = client.getWorklistQueryManager().queryPersonFinishedBizEntities4SDO(userId, scope, bf, wf, joinID, true, pageCond);
        }
        else if (taskType.equals("3"))
        {
            List<WFParticipant> list = client.getOMService().getParticipantScope("person", userId);
            String roles = null;
            if ((list != null) && (!list.isEmpty())) {
                StringBuffer sb = new StringBuffer();

                for (WFParticipant p : list)
                {
                    sb.append(",");
                    sb.append(p.getId());
                }
                roles = sb.toString();
                roles = roles.substring(1);
            }

            List and = wf.get_and();
            int index = 1;
            if ((and != null) && (!and.isEmpty()))
                index = and.size() + 1;
            if (roles != null) {
                wf.set("_and[" + index + "]/_expr[1]/participant", roles);
                wf.set("_and[" + index + "]/_expr[1]/_op", "in");
            }

            BPSServiceClientFactory.getLoginManager().setCurrentUser(userId, userId, "", null);
//            IBPSServiceManagerExt service = (IBPSServiceManagerExt)BPSServiceClientFactoryExt.getDefaultClient().getService(IBPSServiceManagerExt.class);
//            results = service.queryPersonFinishedBizEntities4SDO(userId, scope, bf, wf, joinID, false, pageCond);
        }
        else if ((taskType.equals("1")) || (taskType.equals("1_ORDERFINALTIME"))) {
            results = client.getWorklistQueryManager().queryPersonBizEntities4SDO(userId, permission, scope, bf, wf, joinID, pageCond);
        }
        else
        {
            throw new AdapterException("taskType属性 不支持" + taskType);
        }
        return results;
    }

    private static DataObject processPageCondition(TaskFilter taskfilter)
    {
        com.unicom.ucloud.workflow.objects.PageCondition pct = taskfilter.getPageCondition();
        DataObject pageCond;
        if (pct != null) {
            pageCond = DataFactory.INSTANCE.create("com.eos.foundation", "PageCond");

            pageCond.set("begin", Integer.valueOf(pct.getBegin()));
            pageCond.set("length", Integer.valueOf(pct.getLength()));
            pageCond.set("isCount", pct.getIsCount());
        } else {
            pageCond = DataFactory.INSTANCE.create("com.eos.foundation", "PageCond");

            pageCond.set("begin", Integer.valueOf(0));
            pageCond.set("length", Integer.valueOf(30));
            pageCond.set("isCount", Boolean.valueOf(true));
        }
        return pageCond;
    }

    private static List<ExprType> processBizExpr(TaskFilter taskfilter) throws AdapterException
    {
        List bel = new ArrayList();
        List bed = taskfilter.getBizExprDataObject();
        if ((bed != null) && (bed.size() != 0)) {
            try {
                for (Iterator i  = bed.iterator(); i.hasNext(); ) {
                    Object te = i.next();
                    bel.add((ExprType)te);
                }
            }
            catch (Exception e)
            {
                Iterator i$;
                throw new AdapterException("BizExprDataObject的类型转换异常", e);
            }
        }
        return bel;
    }

    private static List<ExprType> processWorkItemExpr(TaskFilter taskfilter) throws AdapterException
    {
        List tel = new ArrayList();
        List ted = taskfilter.getTaskExprDataObject();
        if ((ted != null) && (ted.size() != 0)) {
            try {
                for (Iterator i = ted.iterator(); i.hasNext(); ) {
                    Object te = i.next();
                    tel.add((ExprType)te);
                }
            }
            catch (Exception e)
            {
                Iterator i$;
                throw new AdapterException("TaskExprDataObject的类型转换异常", e);
            }
        }
        return tel;
    }

    private static boolean hasExprProperty(List<ExprType> l, String property) {
        if ((l == null) || (l.size() == 0))
            return false;
        for (ExprType w : l) {
            if (w.get_property().equals(property))
                return true;
        }
        return false;
    }

    private static void processBizFifter(TaskFilter taskfilter, List<ExprType> l, CriteriaType ct)
            throws AdapterException
    {
        String jobID = taskfilter.getJobID();
        if ((jobID != null) && (jobID.length() != 0))
        {
            String property = "jobID".toLowerCase();
            if (hasExprProperty(l, property))
                throw new AdapterException(conflictMessage(property));
            ExprType ll = (ExprType) ExprType.FACTORY.create();
            ll.set_opEnum(ExprType.OP.EQ);
            ll.set_value(jobID);
            ll.set_property(property);
            l.add(ll);
        }

        String jobCode = taskfilter.getJobCode();
        if ((jobCode != null) && (jobCode.length() != 0))
        {
            String property = "jobCode".toLowerCase();
            if (hasExprProperty(l, property))
                throw new AdapterException(conflictMessage(property));
            ExprType ll = (ExprType) ExprType.FACTORY.create();
            ll.set_opEnum(ExprType.OP.LIKE);
            ll.set_likeRuleEnum(ExprType.LIKERULE.ALL);
            ll.set_value(jobCode);
            ll.set_property(property);
            l.add(ll);
        }

        String jobTitle = taskfilter.getJobTitle();
        if ((jobTitle != null) && (jobTitle.length() != 0))
        {
            String property = "jobTitle".toLowerCase();
            if (hasExprProperty(l, property))
                throw new AdapterException(conflictMessage(property));
            ExprType ll = (ExprType) ExprType.FACTORY.create();
            ll.set_opEnum(ExprType.OP.LIKE);
            ll.set_likeRuleEnum(ExprType.LIKERULE.ALL);
            ll.set_value(jobTitle);
            ll.set_property(property);
            l.add(ll);
        }

        String productID = taskfilter.getProductID();
        if ((productID != null) && (productID.length() != 0))
        {
            String property = "productcode".toLowerCase();
            if (hasExprProperty(l, property))
                throw new AdapterException(conflictMessage(property));
            if (productID.indexOf(",") > 0) {
                ExprType ll = (ExprType) ExprType.FACTORY.create();
                ll.set_opEnum(ExprType.OP.IN);
                ll.set_value(productID);
                ll.set_property(property);
                l.add(ll);
            } else {
                ExprType ll = (ExprType) ExprType.FACTORY.create();
                ll.set_opEnum(ExprType.OP.EQ);
                ll.set_value(jobID);
                ll.set_property(property);
                l.add(ll);
            }
        }
        ct.set_expr(l);
    }

    private static void processWorkItemFifter(TaskFilter taskfilter, List<ExprType> l, CriteriaType ct)
            throws AdapterException
    {
        String processInstID = taskfilter.getProcessInstID();
        if ((processInstID != null) && (processInstID.length() != 0)) {
            String property = "processInstID";
            if (hasExprProperty(l, property))
                throw new AdapterException(conflictMessage(property));
            ExprType ll = (ExprType) ExprType.FACTORY.create();
            ll.set_opEnum(ExprType.OP.EQ);
            ll.set_value(processInstID);
            ll.set_property(property);
            l.add(ll);
        }

        String processModelName = taskfilter.getProcessModelName();
        if ((processModelName != null) && (processModelName.length() != 0)) {
            String property = "processDefName";
            if (hasExprProperty(l, property)) {
                throw new AdapterException(conflictMessage(property));
            }
            if (processModelName.indexOf(",") > 0) {
                ExprType ll = (ExprType) ExprType.FACTORY.create();
                ll.set_opEnum(ExprType.OP.IN);
                ll.set_value(processModelName);
                ll.set_property(property);
                l.add(ll);
            } else {
                ExprType ll = (ExprType) ExprType.FACTORY.create();
                ll.set_opEnum(ExprType.OP.EQ);
                ll.set_value(processModelName);
                ll.set_property(property);
                l.add(ll);
            }

        }

        String activityDefId_op = taskfilter.getActivityDefID_op();

        String activityDefId = taskfilter.getActivityDefId();
        if ((activityDefId != null) && (activityDefId.length() != 0)) {
            String property = "activityDefID";
            if (hasExprProperty(l, property)) {
                throw new AdapterException(conflictMessage(property));
            }
            if ((activityDefId_op != null) && (activityDefId_op.length() != 0) && (activityDefId_op.equals("-1")))
            {
                if (activityDefId.indexOf(",") > 0) {
                    List al = new ArrayList();
                    ExprType ll = (ExprType) ExprType.FACTORY.create();
                    ll.set_opEnum(ExprType.OP.IN);
                    ll.set_value(activityDefId);
                    ll.set_property(property);
                    al.add(ll);
                    List nl = new ArrayList();
                    LogicType lt = (LogicType) LogicType.FACTORY.create();
                    lt.set_expr(al);
                    nl.add(lt);
                    ct.set_not(nl);
                } else {
                    ExprType ll = (ExprType) ExprType.FACTORY.create();
                    ll.set_opEnum(ExprType.OP.NOT_EQ);
                    ll.set_value(activityDefId);
                    ll.set_property(property);
                    l.add(ll);
                }

            }
            else if (activityDefId.indexOf(",") > 0) {
                ExprType ll = (ExprType) ExprType.FACTORY.create();
                ll.set_opEnum(ExprType.OP.IN);
                ll.set_value(activityDefId);
                ll.set_property(property);
                l.add(ll);
            } else {
                ExprType ll = (ExprType) ExprType.FACTORY.create();
                ll.set_opEnum(ExprType.OP.EQ);
                ll.set_value(activityDefId);
                ll.set_property(property);
                l.add(ll);
            }

        }

        String activityID = taskfilter.getActivityID();
        if ((activityID != null) && (activityID.length() != 0)) {
            String property = "activityInstID";
            if (hasExprProperty(l, property))
                throw new AdapterException(conflictMessage(property));
            ExprType ll = (ExprType) ExprType.FACTORY.create();
            ll.set_opEnum(ExprType.OP.EQ);
            ll.set_value(activityID);
            ll.set_property(property);
            l.add(ll);
        }

        String activityName = taskfilter.getActivityName();
        if ((activityName != null) && (activityName.length() != 0)) {
            String property = "activityInstName";
            if (hasExprProperty(l, property))
                throw new AdapterException(conflictMessage(property));
            ExprType ll = (ExprType) ExprType.FACTORY.create();
            ll.set_opEnum(ExprType.OP.LIKE);
            ll.set_likeRuleEnum(ExprType.LIKERULE.ALL);
            ll.set_value(activityName);
            ll.set_property(property);
            l.add(ll);
        }

        if (taskfilter.getAppID() != null) {
            throw new AdapterException("appID属性不支持查询");
        }

        String patter = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(patter);

        Date beginStartDate = taskfilter.getBeginStartDate();

        Date endStartDate = taskfilter.getEndStartDate();
        if ((beginStartDate != null) && (endStartDate != null)) {
            String property = "createTime";
            if (hasExprProperty(l, property))
                throw new AdapterException(conflictMessage(property));
            ExprType ll = (ExprType) ExprType.FACTORY.create();
            ll.set_opEnum(ExprType.OP.BETWEEN);
            ll.set_property(property);
            ll.set_pattern(patter);
            ll.set_max(sdf.format(endStartDate));
            ll.set_min(sdf.format(beginStartDate));
            l.add(ll);
        }
        if (((beginStartDate == null) && (endStartDate != null)) || ((beginStartDate != null) && (endStartDate == null)))
        {
            throw new AdapterException("创建时间必须成对出现，不支持单一条件查询");
        }
        Date beginEndDate = taskfilter.getBeginEndDate();

        Date endEndDate = taskfilter.getEndEndDate();
        if ((beginEndDate != null) && (endEndDate != null)) {
            String property = "finalTime";
            if (hasExprProperty(l, property))
                throw new AdapterException(conflictMessage(property));
            ExprType ll = (ExprType) ExprType.FACTORY.create();
            ll.set_opEnum(ExprType.OP.BETWEEN);
            ll.set_property(property);
            ll.set_pattern(patter);
            ll.set_max(sdf.format(endEndDate));
            ll.set_min(sdf.format(beginEndDate));
            l.add(ll);
        }
        if (((beginEndDate == null) && (endEndDate != null)) || ((beginEndDate != null) && (endEndDate == null)))
        {
            throw new AdapterException("期望完成时间必须成对出现，不支持单一条件查询");
        }
        String parentTaskInstID = taskfilter.getParentTaskInstID();
        if ((parentTaskInstID != null) && (parentTaskInstID.length() != 0)) {
            throw new AdapterException("parentTaskInstID属性不支持查询");
        }

        String processModelID = taskfilter.getProcessModelID();
        if ((processModelID != null) && (processModelID.length() != 0)) {
            String property = "processDefID";
            if (hasExprProperty(l, property))
                throw new AdapterException(conflictMessage(property));
            ExprType ll = (ExprType) ExprType.FACTORY.create();
            ll.set_opEnum(ExprType.OP.EQ);
            ll.set_value(processModelID);
            ll.set_property(property);
            l.add(ll);
        }

//        if (taskfilter.getProcessParams() != null) {
//            throw new AdapterException("processParams属性不支持查询");
//        }

        if (taskfilter.getSenderID() != null) {
            throw new AdapterException("sender属性不支持查询");
        }

        if (taskfilter.getSenderType() != null) {
            throw new AdapterException("senderType属性不支持查询");
        }

        String taskInstID = taskfilter.getTaskInstID();
        if ((taskInstID != null) && (taskInstID.length() != 0)) {
            String property = "workItemID";
            if (hasExprProperty(l, property))
                throw new AdapterException(conflictMessage(property));
            ExprType ll = (ExprType) ExprType.FACTORY.create();
            ll.set_opEnum(ExprType.OP.EQ);
            ll.set_value(taskInstID);
            ll.set_property(property);
            l.add(ll);
        }

        String taskWarning = taskfilter.getTaskWarning();
        if ((taskWarning != null) && (taskWarning.length() != 0)) {
            if ((taskWarning.equals("0")) || (taskWarning.equals("1"))) {
                String property = "isTimeOut";
                if (hasExprProperty(l, property))
                    throw new AdapterException(conflictMessage(property));
                ExprType ll = (ExprType) ExprType.FACTORY.create();
                ll.set_opEnum(ExprType.OP.EQ);
                ll.set_value(taskWarning.equals("1") ? "Y" : "N");
                ll.set_property(property);
                l.add(ll);
            } else {
                throw new AdapterException("非法的查询条件taskWarning=" + taskWarning);
            }

        }

        String senderID = taskfilter.getSenderID();

        String receiverID = taskfilter.getReceiverID();
        String property = "statesList";
        if ((senderID != null) && (senderID.length() != 0)) {
            if (hasExprProperty(l, property))
                throw new AdapterException(conflictMessage(property));
            if ((receiverID != null) && (receiverID.length() != 0))
            {
                ExprType ll = (ExprType) ExprType.FACTORY.create();
                ll.set_opEnum(ExprType.OP.EQ);
                ll.set_value("SenderID[" + senderID + "]ReceiverID[" + receiverID + "]");
                ll.set_property(property);
                l.add(ll);
            }
            else {
                ExprType ll = (ExprType) ExprType.FACTORY.create();
                ll.set_opEnum(ExprType.OP.LIKE);
                ll.set_likeRuleEnum(ExprType.LIKERULE.START);
                ll.set_value("SenderID[" + senderID + "]");
                ll.set_property(property);
                l.add(ll);
            }
        } else {
            if (hasExprProperty(l, property))
                throw new AdapterException(conflictMessage(property));
            if ((receiverID != null) && (receiverID.length() != 0))
            {
                ExprType ll = (ExprType) ExprType.FACTORY.create();
                ll.set_opEnum(ExprType.OP.LIKE);
                ll.set_likeRuleEnum(ExprType.LIKERULE.END);
                ll.set_value("ReceiverID[" + receiverID + "]");
                ll.set_property(property);
                l.add(ll);
            }

        }

        ct.set_expr(l);
    }

    private static String conflictMessage(String property) {
        return "查询条件" + property + "与自定义查询条件有冲突，请使用单一方式查询";
    }

    private static TaskInstance dataObject2TaskInstace(DataObject data)
            throws AdapterException
    {
        WFWorkItem wi = dataObject2WorkItem(data);
        return WorkFlowDataConvertor.convert2TaskInstance(wi);
    }

    private static WFWorkItem dataObject2WorkItem(DataObject data) {
        WFWorkItem wi = new WFWorkItem();
        Map bizMap = new HashMap();
        DataObject bizObject = data.getDataObject("bizObject");
        if (bizObject != null) {
            bizMap.put("jobID", bizObject.getString("jobID".toLowerCase()));
            bizMap.put("jobCode", bizObject.getString("jobCode".toLowerCase()));
            bizMap.put("jobTitle", bizObject.getString("jobTitle".toLowerCase()));
            bizMap.put("jobType", bizObject.getString("jobType".toLowerCase()));
            bizMap.put("jobEndtime", bizObject.getString("jobEndtime".toLowerCase()));
            bizMap.put("jobStarttime", bizObject.getString("jobStarttime".toLowerCase()));
            bizMap.put("reBacktime", bizObject.getString("reBacktime".toLowerCase()));
            bizMap.put("shard", bizObject.getString("shard".toLowerCase()));
            bizMap.put("businessId", bizObject.getString("businessId".toLowerCase()));
            bizMap.put("productcode", bizObject.getString("productcode".toLowerCase()));
            bizMap.put("majorcode", bizObject.getString("majorcode".toLowerCase()));

            bizMap.put("datColumn1", bizObject.getString("datColumn1".toLowerCase()));
            bizMap.put("datColumn2", bizObject.getString("datColumn2".toLowerCase()));
            bizMap.put("numColumn1", Long.valueOf(bizObject.getLong("numColumn1".toLowerCase())));
            bizMap.put("numColumn2", Long.valueOf(bizObject.getLong("numColumn2".toLowerCase())));
            bizMap.put("rootnmColumn1", Long.valueOf(bizObject.getLong("rootnmColumn1".toLowerCase())));
            bizMap.put("rootnmColumn2", Long.valueOf(bizObject.getLong("rootnmColumn2".toLowerCase())));
            bizMap.put("rootvcColumn1", bizObject.getString("rootvcColumn1".toLowerCase()));
            bizMap.put("rootvcColumn2", bizObject.getString("rootvcColumn2".toLowerCase()));
            bizMap.put("strColumn1", bizObject.getString("strColumn1".toLowerCase()));
            bizMap.put("strColumn2", bizObject.getString("strColumn2".toLowerCase()));
            bizMap.put("strColumn3", bizObject.getString("strColumn3".toLowerCase()));
            bizMap.put("strColumn4", bizObject.getString("strColumn4".toLowerCase()));
            bizMap.put("strColumn5", bizObject.getString("strColumn5".toLowerCase()));
            bizMap.put("strColumn6", bizObject.getString("strColumn6".toLowerCase()));
            bizMap.put("strColumn7", bizObject.getString("strColumn7".toLowerCase()));
            bizMap.put("SenderID", bizObject.getString("SenderID".toLowerCase()));
            bizMap.put("ReceiverID", bizObject.getString("ReceiverID".toLowerCase()));
        }
        wi.setActionMask(data.getString("actionMask"));
        wi.setActionURL(data.getString("actionURL"));
        wi.setActivityDefID(data.getString("activityDefID"));
        wi.setActivityInstID(data.getLong("activityInstID"));
        wi.setActivityInstName(data.getString("activityInstName"));
        wi.setAllowAgent(data.getString("allowAgent"));
        wi.setAssistant(data.getString("assistantID"));
        wi.setAssistantName(data.getString("assistant"));
        wi.setBizObject(bizMap);
        wi.setBizState(data.getInt("bizState"));
        wi.setCatalogName(data.getString("catalogName"));
        wi.setCatalogUUID(data.getString("catalogUUID"));
        wi.setCreateTime(data.getString("createTime"));
        wi.setCurrentState(data.getInt("currentState"));
        wi.setDealOpinion(data.getString("dealOpinion"));
        wi.setDealResult(data.getString("dealResult"));
        wi.setEndTime(data.getString("endTime"));
        wi.setFinalTime(data.getString("finalTime"));
        wi.setIsTimeOut(data.getString("isTimeOut"));
        wi.setLimitNum(data.getInt("limitNum"));
        wi.setLimitNumDesc(data.getString("limitNumDesc"));
        wi.setParticipant(data.getString("participantID"));

        wi.setPartiName(data.getString("participant"));
        wi.setPriority(data.getInt("priority"));
        wi.setProcessChName(data.getString("processChName"));
        wi.setProcessDefID(data.getLong("processDefID"));
        wi.setProcessInstID(data.getLong("processInstID"));
        wi.setProcessInstName(data.getString("processInstName"));
        wi.setProcessDefName(data.getString("processDefName"));
        wi.setRemindTime(data.getString("remindTime"));
        wi.setRootProcInstID(data.getLong("rootProcInstID"));
        wi.setStartTime(data.getString("startTime"));
        wi.setStatesList(data.getString("statesList"));
        wi.setTimeOutNum(data.getInt("timeOutNum"));
        wi.setTimeOutNumDesc(data.getString("timeOutNumDesc"));
        wi.setUrlType(data.getString("urlType"));
        wi.setWorkItemDesc(data.getString("workItemDesc"));
        wi.setWorkItemID(data.getLong("workItemID"));
        wi.setWorkItemName(data.getString("workItemName"));
        wi.setWorkItemType(data.getString("workItemType"));
        return wi;
    }

    public static List<TaskInstance> getMyTasks4SDO2(TaskFilter taskfilter, String userId)
            throws AdapterException
    {
        List tasks = new ArrayList();
        try
        {
            IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();

            DataObject workItemFilter = DataFactory.INSTANCE.create("com.primeton.das.criteria", "criteriaType");

            DataObject bizEntityFilter = DataFactory.INSTANCE.create("com.primeton.das.criteria", "criteriaType");

            DataObject pageCond = null;

            com.unicom.ucloud.workflow.objects.PageCondition pct = taskfilter.getPageCondition();
            if (pct != null) {
                pageCond = DataFactory.INSTANCE.create("com.eos.foundation", "PageCond");

                pageCond.set("begin", Integer.valueOf(pct.getBegin()));
                pageCond.set("length", Integer.valueOf(pct.getLength()));
                pageCond.set("isCount", pct.getIsCount());
            } else {
                pageCond = DataFactory.INSTANCE.create("com.eos.foundation", "PageCond");

                pageCond.set("begin", Integer.valueOf(0));
                pageCond.set("length", Integer.valueOf(11));
                pageCond.set("isCount", Boolean.valueOf(true));
            }
            workItemFilter.set("_entity", "com.eos.workflow.data.WFWorkItem");

            bizEntityFilter.set("_entity", "com.eos.workflow.data.Wfbizinfo");

            DataObject[] results = null;

            String taskType = taskfilter.getTaskType();
            if (taskType == null) {
                throw new AdapterException("taskType属性不能为空");
            }
            if (taskType.equals("2"))
            {
                results = client.getWorklistQueryManager().queryPersonFinishedBizEntities4SDO(userId, "ALL", bizEntityFilter, workItemFilter, "processinstid", true, pageCond);
            }
            else if (taskType.equals("1")) {
                results = client.getWorklistQueryManager().queryPersonBizEntities4SDO(userId, "ALL", "ALL", bizEntityFilter, workItemFilter, "processinstid", pageCond);
            }
            else
            {
                throw new AdapterException("taskType属性 不支持" + taskType);
            }

            for (DataObject data : results) {
                TaskInstance ti = new TaskInstance();
                DataObject bizObject = data.getDataObject("bizObject");
                DataObject _pageCond = data.getDataObject("_pageCond");
                if (_pageCond != null)
                {
                    com.eos.foundation.PageCond pc = (com.eos.foundation.PageCond)_pageCond;
                    taskfilter.setPageCondition(WorkFlowDataConvertor.convert2EosPageCond(pc));
                }

                ti.setJobID(bizObject.getString("vccolumn2"));
                ti.setJobTitle(bizObject.getString("vccolumn1"));
                ti.setShard(data.getString("vcColumn4"));
                ti.setShard(data.getString("vcColumn5"));
                ti.setShard(data.getString("vcColumn6"));
                ti.setShard(data.getString("vcColumn7"));

                ti.setActivityDefID(data.getString("activityDefID"));
                ti.setActivityInstID(data.getString("activityInstID"));
                ti.setActivityInstName(data.getString("activityInstName"));
                ti.setAppID("");

                ti.setCompletionDate(data.getDate("endTime"));
                ti.setCreateDate(data.getDate("createTime"));
                ti.setEndDate(data.getDate("finalTime"));
                ti.setWarningDate(data.getDate("remindTime"));

                ti.setFormURL(data.getString("actionURL"));

                ti.setProcessInstID(data.getLong("processInstID") + "");

                ti.setProcessModelId(data.getString("processDefID"));
                ti.setProcessModelName(data.getString("processDefName"));

                ti.setTaskInstID(data.getString("workItemID"));
                ti.setRootProcessInstId(data.getString("rootProcessInstId"));

                if ((data.getString("currentState").equals("4")) || (data.getString("currentState").equals("10")))
                    ti.setCurrentState("1");
                else if (data.getString("currentState").equals("12"))
                    ti.setCurrentState("2");
                else if (data.getString("currentState").equals("8")) {
                    ti.setCurrentState("3");
                }

                tasks.add(ti);
            }
        } catch (WFServiceException e) {
            throw new AdapterException(e);
        } catch (Exception e) {
            throw new AdapterException(e);
        }
        return tasks;
    }

    public static List<TaskInstance> getMyTasks4SQL(TaskFilter taskfilter, String userId)
            throws AdapterException
    {
        List tasks = null;
        try {
            IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();

            List wiBindList = new ArrayList();
            List bizBindList = new ArrayList();
            StringBuffer wiSqlBuff = new StringBuffer();
            StringBuffer bizSqlBuff = new StringBuffer();
            wiSqlBuff.append("");
            bizSqlBuff.append("");
            appendSqlWithParams(taskfilter, wiSqlBuff, wiBindList, bizSqlBuff, bizBindList);

            com.primeton.workflow.api.PageCond cond = WorkFlowDataConvertor.convert2PageCond(taskfilter.getPageCondition());

            List workItems = null;

            String taskType = taskfilter.getTaskType();
            if (taskType == null) {
                throw new AdapterException("taskType属性不能为空");
            }

            if (taskType.equals("2"))
            {
                wiSqlBuff.append(" 1=1 order by endTime desc");
                workItems = client.getWorklistQueryManager().queryPersonFinishedWorkItemsWithBizInfo(userId, "ALL", false, "Job", wiSqlBuff.toString(), bizSqlBuff.toString(), wiBindList, bizBindList, cond);
            }
            else if (taskType.equals("1"))
            {
                wiSqlBuff.append(" 1=1 order by createTime desc");

                if (pending) {
                    workItems = client.getWorklistQueryManager().queryPersonWorkItemsWithBizInfo(userId, "ALL", "ALL", "Job", wiSqlBuff.toString(), bizSqlBuff.toString(), wiBindList, bizBindList, true, cond);
                }
                else
                {
                    workItems = client.getWorklistQueryManager().queryPersonWorkItemsWithBizInfo(userId, "ALL", "ALL", "Job", wiSqlBuff.toString(), bizSqlBuff.toString(), wiBindList, bizBindList, cond);
                }

            }
            else
            {
                throw new AdapterException("taskType属性 不支持" + taskType);
            }

            if (workItems == null) {
                if (taskfilter.getPageCondition() != null)
                    taskfilter.getPageCondition().setCount(0);
                return null;
            }
            try
            {
                ResultList list = (ResultList)workItems;

                com.primeton.workflow.api.PageCond pageCond = list.getPageCond();
                com.unicom.ucloud.workflow.objects.PageCondition pageCondition = taskfilter.getPageCondition();
                if (pageCondition != null) {
                    WorkFlowDataConvertor.convert2PageCond(pageCond, pageCondition);
                }
                tasks = WorkFlowDataConvertor.convert2TaskInstList(workItems);
            }
            catch (Exception e)
            {
                throw new AdapterException(e);
            }
        }
        catch (WFServiceException e) {
            throw new AdapterException(e);
        }
        return tasks;
    }

    public static String getRootProcInstID(String processInstID)
    {
        if ((processInstID == null) || (processInstID.length() == 0))
            return null;
        try {
            IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();

            IWFQueryManager queryManager = client.getCommonQueryManage();
            IDASCriteria criteria = DASManager.createCriteria("com.eos.workflow.data.WFWorkItem");
            criteria.add(ExpressionHelper.eq("processInstID", processInstID));
            com.primeton.workflow.api.PageCond pageCond = new com.primeton.workflow.api.PageCond(1);
            List queryWorkItems = queryManager.queryWorkItemsCriteria(criteria, pageCond);
            if ((queryWorkItems == null) || (queryWorkItems.isEmpty()))
                return null;
            WFWorkItem wf = (WFWorkItem)queryWorkItems.get(0);
            return wf.getRootProcInstID() + "";
        } catch (WFServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void appendSqlWithParams(TaskFilter taskfilter, StringBuffer wiSqlBuff, List<String> wiBindList, StringBuffer bizSqlBuff, List<String> bizBindList)
            throws AdapterException
    {
        String processInstID = taskfilter.getProcessInstID();
        if ((processInstID != null) && (processInstID.length() != 0)) {
            autoWithAnd(wiSqlBuff);
            wiSqlBuff.append("bizinfo.processInstID = ? ");

            String rootProcInstID = getRootProcInstID(processInstID);

            wiBindList.add(rootProcInstID);
        }

        String processModelName = taskfilter.getProcessModelName();
        if ((processModelName != null) && (processModelName.length() != 0)) {
            autoWithAnd(wiSqlBuff);
            if (processModelName.indexOf(",") > 0) {
                String[] processModelNames = processModelName.split(",");
                StringBuffer modelBuff = new StringBuffer();
                modelBuff.append("?");
                wiBindList.add(processModelNames[0]);
                for (int i = 1; i < processModelNames.length; i++) {
                    modelBuff.append(",");
                    modelBuff.append("?");
                    wiBindList.add(processModelNames[i]);
                }
                wiSqlBuff.append("processDefName in(" + modelBuff.toString() + ") ");
            }
            else {
                wiSqlBuff.append("processDefName = ? ");
                wiBindList.add(processModelName);
            }
        }

        String activityDefId_op = taskfilter.getActivityDefID_op();

        String activityDefId = taskfilter.getActivityDefId();
        if ((activityDefId != null) && (activityDefId.length() != 0))
        {
            if ((activityDefId_op != null) && (activityDefId_op.length() != 0) && (activityDefId_op.equals("-1")))
            {
                if (activityDefId.indexOf(",") > 0) {
                    String[] activityDefIds = activityDefId.split(",");
                    for (String a : activityDefIds) {
                        autoWithOr(wiSqlBuff);
                        wiSqlBuff.append("activityDefID <> ?  ");
                        wiBindList.add(a);
                    }
                } else {
                    autoWithOr(wiSqlBuff);
                    wiSqlBuff.append("activityDefID <> ? ");
                    wiBindList.add(activityDefId);
                }
            }
            else {
                autoWithAnd(wiSqlBuff);

                if (activityDefId.indexOf(",") > 0) {
                    String[] processModelNames = activityDefId.split(",");
                    StringBuffer modelBuff = new StringBuffer();
                    modelBuff.append("?");
                    wiBindList.add(processModelNames[0]);
                    for (int i = 1; i < processModelNames.length; i++) {
                        modelBuff.append(",");
                        modelBuff.append("?");
                        wiBindList.add(processModelNames[i]);
                    }
                    wiSqlBuff.append("activityDefID in(" + modelBuff.toString() + ") ");
                }
                else {
                    wiSqlBuff.append("activityDefID = ? ");
                    wiBindList.add(activityDefId);
                }
            }
        }

        String activityID = taskfilter.getActivityID();
        if ((activityID != null) && (activityID.length() != 0))
        {
            autoWithAnd(wiSqlBuff);
            wiSqlBuff.append("activityInstID = ? ");
            wiBindList.add(activityID);
        }

        String activityName = taskfilter.getActivityName();
        if ((activityName != null) && (activityName.length() != 0))
        {
            autoWithAnd(wiSqlBuff);
            wiSqlBuff.append("activityInstName like ? ");
            wiBindList.add("%" + activityName + "%");
        }

        if (taskfilter.getAppID() != null) {
            throw new AdapterException("appID属性不支持查询");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date beginStartDate = taskfilter.getBeginStartDate();

        Date endStartDate = taskfilter.getEndStartDate();
        if ((beginStartDate != null) && (endStartDate != null))
        {
            autoWithAnd(wiSqlBuff);
            wiSqlBuff.append("createTime>? and createTime<=? ");
            wiBindList.add(sdf.format(beginStartDate));
            wiBindList.add(sdf.format(endStartDate));
        }
        if (((beginStartDate == null) && (endStartDate != null)) || ((beginStartDate != null) && (endStartDate == null)))
        {
            throw new AdapterException("创建时间必须成对出现，不支持单一条件查询");
        }
        Date beginEndDate = taskfilter.getBeginEndDate();

        Date endEndDate = taskfilter.getEndEndDate();
        if ((beginEndDate != null) && (endEndDate != null))
        {
            autoWithAnd(wiSqlBuff);
            wiSqlBuff.append("finalTime>? and finalTime<=? ");
            wiBindList.add(sdf.format(beginEndDate));
            wiBindList.add(sdf.format(endEndDate));
        }
        if (((beginEndDate == null) && (endEndDate != null)) || ((beginEndDate != null) && (endEndDate == null)))
        {
            throw new AdapterException("期望完成时间必须成对出现，不支持单一条件查询");
        }
        String parentTaskInstID = taskfilter.getParentTaskInstID();
        if ((parentTaskInstID != null) && (parentTaskInstID.length() != 0)) {
            throw new AdapterException("parentTaskInstID属性不支持查询");
        }

        String processModelID = taskfilter.getProcessModelID();
        if ((processModelID != null) && (processModelID.length() != 0))
        {
            autoWithAnd(wiSqlBuff);
            wiSqlBuff.append("processDefID = ? ");
            wiBindList.add(processModelID);
        }

//        if (taskfilter.getProcessParams() != null) {
//            throw new AdapterException("processParams属性不支持查询");
//        }

        if (taskfilter.getSenderID() != null) {
            throw new AdapterException("sender属性不支持查询");
        }

        if (taskfilter.getSenderType() != null) {
            throw new AdapterException("senderType属性不支持查询");
        }

        String taskInstID = taskfilter.getTaskInstID();
        if ((taskInstID != null) && (taskInstID.length() != 0))
        {
            autoWithAnd(wiSqlBuff);
            wiSqlBuff.append("workItemID = ? ");
            wiBindList.add(taskInstID);
        }

        String taskWarning = taskfilter.getTaskWarning();
        if ((taskWarning != null) && (taskWarning.length() != 0))
        {
            if ((taskWarning.equals("0")) || (taskWarning.equals("1"))) {
                autoWithAnd(wiSqlBuff);
                wiSqlBuff.append("isTimeOut = ? ");
                wiBindList.add(taskWarning.equals("1") ? "Y" : "N");
            }

        }

        String senderID = taskfilter.getSenderID();

        String receiverID = taskfilter.getReceiverID();
        if ((senderID != null) && (senderID.length() != 0))
        {
            if ((receiverID != null) && (receiverID.length() != 0))
            {
                autoWithAnd(wiSqlBuff);
                wiSqlBuff.append("statesList = ? ");
                wiBindList.add("SenderID[" + senderID + "]ReceiverID[" + receiverID + "]");
            }
            else {
                autoWithAnd(wiSqlBuff);
                wiSqlBuff.append("statesList like ? ");
                wiBindList.add("SenderID[" + senderID + "]%");
            }
        }
        else if ((receiverID != null) && (receiverID.length() != 0))
        {
            autoWithAnd(wiSqlBuff);
            wiSqlBuff.append("statesList like ? ");
            wiBindList.add("%ReceiverID[" + receiverID + "]");
        }

        String jobID = taskfilter.getJobID();
        if ((jobID != null) && (jobID.length() != 0))
        {
            autoWithAnd(bizSqlBuff);
            bizSqlBuff.append("jobID = ? ");
            bizBindList.add(jobID);
        }

        String jobCode = taskfilter.getJobCode();
        if ((jobCode != null) && (jobCode.length() != 0))
        {
            autoWithAnd(bizSqlBuff);

            bizSqlBuff.append("jobCode like ? ");
            bizBindList.add("%" + jobCode + "%");
        }

        String jobTitle = taskfilter.getJobTitle();
        if ((jobTitle != null) && (jobTitle.length() != 0))
        {
            autoWithAnd(bizSqlBuff);
            bizSqlBuff.append("jobTitle like ? ");
            bizBindList.add("%" + jobTitle + "%");
        }

        String productID = taskfilter.getProductID();
        if ((productID != null) && (productID.length() != 0))
        {
            autoWithAnd(bizSqlBuff);

            if (productID.indexOf(",") > 0) {
                String[] processModelNames = productID.split(",");
                StringBuffer modelBuff = new StringBuffer();
                modelBuff.append("?");
                bizBindList.add(processModelNames[0]);
                for (int i = 1; i < processModelNames.length; i++) {
                    modelBuff.append(",");
                    modelBuff.append("?");
                    bizBindList.add(processModelNames[i]);
                }
                bizSqlBuff.append("productcode in(" + modelBuff.toString() + ") ");
            }
            else {
                bizSqlBuff.append("productcode = ? ");
                bizBindList.add(productID);
            }
        }

        autoWithAnd(wiSqlBuff);

        println("wiSqlBuff:" + wiSqlBuff);
        println("bizSqlBuff:" + bizSqlBuff);
    }

    private static void autoWithAnd(StringBuffer sb) {
        if (sb.toString().length() != 0)
            sb.append("and ");
    }

    private static void autoWithOr(StringBuffer sb)
    {
        if (sb.toString().length() != 0)
            sb.append("or ");
    }

    static void println(Object o)
    {
        logger.debug(o.toString());
    }

    @Deprecated
    public static void appendSqlWithParams2(TaskFilter taskfilter, StringBuffer wiSqlBuff, List<String> wiBindList, StringBuffer bizSqlBuff, List<String> bizBindList)
            throws AdapterException
    {
        if (taskfilter == null) {
            return;
        }
        if (taskfilter.getProcessModelID() != null) {
            if (wiSqlBuff.toString().length() != 0) {
                wiSqlBuff.append("and ");
            }
            if (taskfilter.getProcessModelID().indexOf(",") <= 0)
            {
                wiSqlBuff.append("processDefName = ? ");
                wiBindList.add(taskfilter.getProcessModelID());
            }
        }
        if (taskfilter.getTaskInstID() != null) {
            if (wiSqlBuff.toString().length() != 0) {
                wiSqlBuff.append("and ");
            }
            wiSqlBuff.append("workItemID = ? ");
            wiBindList.add(taskfilter.getTaskInstID());
        }
        if (taskfilter.getProcessInstID() != null) {
            if (wiSqlBuff.toString().length() != 0) {
                wiSqlBuff.append("and ");
            }
            wiSqlBuff.append("bizinfo.processInstID = ? ");
            wiBindList.add(taskfilter.getProcessInstID());
        }
        if (taskfilter.getActivityID() != null) {
            if (wiSqlBuff.toString().length() != 0) {
                wiSqlBuff.append("and ");
            }
            wiSqlBuff.append("activityDefID = ? ");
            wiBindList.add(taskfilter.getActivityID());
        }
        if (taskfilter.getActivityName() != null)
        {
            if (wiSqlBuff.toString().length() != 0) {
                wiSqlBuff.append("and ");
            }
            wiSqlBuff.append("activityInstName like ? ");
            wiBindList.add("%" + taskfilter.getActivityName() + "%");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        if ((taskfilter.getBeginStartDate() == null) || (taskfilter.getBeginEndDate() == null))
        {
            if (taskfilter.getBeginStartDate() != null) {
                if (wiSqlBuff.toString().length() != 0) {
                    wiSqlBuff.append("and ");
                }

                wiSqlBuff.append(" date_format(createTime,'%Y%m%d%H%i%s')>=? ");
                wiBindList.add(sdf.format(taskfilter.getBeginStartDate()));
                println(wiSqlBuff.toString());
                println(sdf.format(taskfilter.getBeginStartDate()));
            }
            else if (taskfilter.getBeginEndDate() != null) {
                if (wiSqlBuff.toString().length() != 0) {
                    wiSqlBuff.append("and ");
                }
                wiSqlBuff.append("wfworkitem.createTime <= ? ");
                wiBindList.add("to_date('" + sdf.format(taskfilter.getBeginEndDate()) + "','yyyy-MM-dd hh:mi:ss')");
            }
        }

        if ((taskfilter.getEndStartDate() != null) && (taskfilter.getEndEndDate() != null))
        {
            if (wiSqlBuff.toString().length() != 0) {
                wiSqlBuff.append("and ");
            }
            wiSqlBuff.append("(wfworkitem.endTime between ? and ?) ");
            wiBindList.add("to_date('" + sdf.format(taskfilter.getEndStartDate()) + "','yyyy-MM-dd hh:mi:ss')");

            wiBindList.add("to_date('" + sdf.format(taskfilter.getEndEndDate()) + "','yyyy-MM-dd hh:mi:ss')");
        }
        else if (taskfilter.getEndStartDate() != null) {
            if (wiSqlBuff.toString().length() != 0) {
                wiSqlBuff.append("and ");
            }
            wiSqlBuff.append("wfworkitem.endTime >= ? ");
            wiBindList.add("to_date('" + sdf.format(taskfilter.getEndStartDate()) + "','yyyy-MM-dd hh:mi:ss')");
        }
        else if (taskfilter.getEndEndDate() != null) {
            if (wiSqlBuff.toString().length() != 0) {
                wiSqlBuff.append("and ");
            }
            wiSqlBuff.append("wfworkitem.endTime <= ? ");
            wiBindList.add("to_date('" + sdf.format(taskfilter.getEndEndDate()) + "','yyyy-MM-dd hh:mi:ss')");
        }

        if (taskfilter.getJobTitle() != null) {
            if (bizSqlBuff.toString().length() != 0) {
                bizSqlBuff.append("and ");
            }
            bizSqlBuff.append("jobTitle like ? ");
            bizBindList.add("%" + taskfilter.getJobTitle() + "%");
        }
        if (taskfilter.getJobID() != null) {
            if (bizSqlBuff.toString().length() != 0) {
                bizSqlBuff.append("and ");
            }
            bizSqlBuff.append("jobID =? ");
            bizBindList.add(taskfilter.getJobID());
        }
    }

    @Deprecated
    public int getTaskStatistics2(TaskFilter taskfilter, String userId)
            throws AdapterException
    {
        List workItems = null;
        try {
            IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();

            List wiBindList = new ArrayList();
            List bizBindList = new ArrayList();
            StringBuffer wiSqlBuff = new StringBuffer();
            StringBuffer bizSqlBuff = new StringBuffer();
            wiSqlBuff.append("");
            bizSqlBuff.append("");
            appendSqlWithParams(taskfilter, wiSqlBuff, wiBindList, bizSqlBuff, bizBindList);

            com.primeton.workflow.api.PageCond cond = new com.primeton.workflow.api.PageCond();
            cond.setBegin(0);
            cond.setIsCount(true);
            cond.setLength(2147483647);
            if (taskfilter.getTaskType() != null) {
                if (taskfilter.getTaskType().equals("2"))
                {
                    workItems = client.getWorklistQueryManager().queryPersonFinishedWorkItemsWithBizInfo(userId, "ALL", false, "Job", wiSqlBuff.toString(), bizSqlBuff.toString(), wiBindList, bizBindList, cond);
                }
                else if (taskfilter.getTaskType().equals("1"))
                {
                    workItems = client.getWorklistQueryManager().queryPersonWorkItemsWithBizInfo(userId, "ALL", "ALL", "Job", wiSqlBuff.toString(), bizSqlBuff.toString(), wiBindList, bizBindList, cond);
                }

            }
            else
            {
                throw new AdapterException("任务类型：1. 待办、2.已办、3.待阅、4. 已阅 中的一项,必填");
            }
        }
        catch (WFServiceException e) {
            throw new AdapterException(e);
        }
        return workItems == null ? 0 : workItems.size();
    }
}
