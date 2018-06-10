package com.metarnet.core.common.workflow;

import com.eos.workflow.api.BPSServiceClientFactory;
import com.eos.workflow.api.IBPSServiceClient;
import com.eos.workflow.data.*;
import com.eos.workflow.omservice.WFParticipant;
import com.eos.workflow.omservice.WIParticipantInfo;
import com.metarnet.core.common.exception.AdapterException;
import com.primeton.workflow.api.WFServiceException;
import com.primeton.workflow.model.definition.Participant;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hadoop
 * Date: 15-5-13
 * Time: 下午3:51
 * To change this template use File | Settings | File Templates.
 */
public class WorkFlowDataConvertor {

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");


    public static com.primeton.workflow.api.PageCond convert2PageCond(com.unicom.ucloud.workflow.objects.PageCondition pc)
    {
        if (pc == null) {
            return null;
        }
        com.primeton.workflow.api.PageCond newPC = new com.primeton.workflow.api.PageCond();
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
        return newPC;
    }

    public static com.unicom.ucloud.workflow.objects.PageCondition convert2PageCond(com.primeton.workflow.api.PageCond pc)
    {
        if (pc == null) {
            return null;
        }
        com.unicom.ucloud.workflow.objects.PageCondition newPC = new com.unicom.ucloud.workflow.objects.PageCondition();
        return convert2PageCond(pc, newPC);
    }

    public static com.unicom.ucloud.workflow.objects.PageCondition convert2PageCond(com.primeton.workflow.api.PageCond pc, com.unicom.ucloud.workflow.objects.PageCondition newPC)
    {
        if (pc == null) {
            return null;
        }
        newPC.setBegin(pc.getBegin());
        newPC.setLength(pc.getLength());
        newPC.setIsCount(Boolean.valueOf(pc.getIsCount()));
        newPC.setCount(pc.getCount());
        newPC.setCurrentPage(pc.getCurrentPage());
        newPC.setIsFirst(Boolean.valueOf(pc.isFirst()));
        newPC.setIsLast(Boolean.valueOf(pc.isLast()));
        newPC.setTotalPage(pc.getTotalPage());
        return newPC;
    }

    public static com.unicom.ucloud.workflow.objects.PageCondition convert2EosPageCond(com.eos.foundation.PageCond pc)
    {
        if (pc == null) {
            return null;
        }
        com.unicom.ucloud.workflow.objects.PageCondition newPC = new com.unicom.ucloud.workflow.objects.PageCondition();
        return convert2EosPageCond(pc, newPC);
    }

    public static com.unicom.ucloud.workflow.objects.PageCondition convert2EosPageCond(com.eos.foundation.PageCond pc, com.unicom.ucloud.workflow.objects.PageCondition newPC)
    {
        if (pc == null) {
            return null;
        }
        newPC.setBegin(pc.getBegin());
        newPC.setLength(pc.getLength());
        newPC.setIsCount(Boolean.valueOf(pc.getIsCount()));
        newPC.setCount(pc.getCount());
        newPC.setCurrentPage(pc.getCurrentPage());
        newPC.setIsFirst(Boolean.valueOf(pc.getIsFirst()));
        newPC.setIsLast(Boolean.valueOf(pc.getIsLast()));
        newPC.setTotalPage(pc.getTotalPage());
        return newPC;
    }

    public static List<TaskInstance> convert2TaskInstList(List<WFWorkItem> list)
            throws AdapterException
    {
        if (list == null) {
            return new ArrayList(0);
        }
        int size = list.size();
        List result = new ArrayList(size);
        TaskInstance task = null;
        for (int i = 0; i < size; i++)
        {
            task = convert2TaskInstance((WFWorkItem)list.get(i));
            result.add(task);
        }
        return result;
    }

    public static TaskInstance convert2TaskInstance(WFWorkItem wi)
            throws AdapterException
    {
        if (wi == null) {
            return null;
        }
        try
        {
            TaskInstance task = convert2TaskInstanceNoList(wi);
            if (wi.getWorkItemID() > 0L) {
                try {
                    IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();

                    long WorkItemID = wi.getWorkItemID();
                    List PartInfo = client.getWorkItemManager().queryWorkItemParticipantInfo(WorkItemID);
                    List<WFParticipant> ps = new ArrayList();
                    for (int i = 0; i < PartInfo.size(); i++) {
                        WFParticipant part = new WFParticipant();
                        part.setId(((WIParticipantInfo)PartInfo.get(i)).getId());
                        part.setName(((WIParticipantInfo)PartInfo.get(i)).getName());
                        part.setTypeCode(((WIParticipantInfo)PartInfo.get(i)).getTypeCode());
                        String Permission = ((WIParticipantInfo)PartInfo.get(i)).getPermission();
                        long CurrentState = wi.getCurrentState();

                        if (((Permission.equals("EXE")) && (CurrentState == 10L)) || ((Permission.equals("GET")) && (CurrentState == 4L)))
                            part.setAttribute("Permission", "0");
                        else {
                            part.setAttribute("Permission", "1");
                        }
                        ps.add(part);
                    }

                    if (PartInfo != null) {
                        List<Participant> taskps = new ArrayList();
                        for (WFParticipant wii : ps) {
                            taskps.add(convert2Participant(wii));
                        }
                        task.setParticipants(taskps);
                    }
                } catch (WFServiceException e1) {
                    throw new AdapterException(e1);
                } catch (AdapterException e) {
                    throw new AdapterException(e);
                }

            }

            return task;
        } catch (NumberFormatException e) {
            throw new AdapterException(e); } catch (ParseException e) {
        }
        throw new AdapterException("");
    }

    public static TaskInstance convert2TaskInstanceNoList(WFWorkItem wi)
            throws ParseException
    {


        TaskInstance task = new TaskInstance();
        task.setActivityDefID(wi.getActivityDefID());
        task.setActivityInstID(String.valueOf(wi.getActivityInstID()));
        task.setActivityInstName(String.valueOf(wi.getActivityInstName()));

        task.setWarningDate(sdf.parse(wi.getRemindTime()));
        task.setCompletionDate(sdf.parse(wi.getEndTime()));
        task.setCreateDate(sdf.parse(wi.getCreateTime()));
        task.setEndDate(sdf.parse(wi.getFinalTime()));
        task.setFormURL(wi.getActionURL());
        task.setRootProcessInstId(String.valueOf(wi.getRootProcInstID()));

        task.setProcessModelCNName(wi.getProcessChName());

        if (String.valueOf(wi.getCurrentState()).equals("10"))
            task.setCurrentState("1");
        else if (String.valueOf(wi.getCurrentState()).equals("12"))
            task.setCurrentState("2");
        else if (String.valueOf(wi.getCurrentState()).equals("8"))
            task.setCurrentState("3");
        else if (String.valueOf(wi.getCurrentState()).equals("4")) {
            task.setCurrentState("4");
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("jobID") != null))
        {
            task.setJobID(wi.getBizObject().get("jobID").toString());
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("jobTitle") != null))
        {
            task.setJobTitle(wi.getBizObject().get("jobTitle").toString());
        }

        if ((wi.getBizObject() != null) && (wi.getBizObject().get("shard") != null))
        {
            task.setShard(wi.getBizObject().get("shard").toString());
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("businessId") != null))
        {
            task.setBusinessId(wi.getBizObject().get("businessId").toString());
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("productcode") != null))
        {
            task.setPRODUCT_ID(wi.getBizObject().get("productcode").toString());
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("majorcode") != null))
        {
            task.setMAJOR_ID(wi.getBizObject().get("majorcode").toString());
        }

        if ((wi.getBizObject() != null) && (wi.getBizObject().get("jobType") != null))
        {
            task.setJobtype(wi.getBizObject().get("jobType").toString());
        }

        if ((wi.getBizObject() != null) && (wi.getBizObject().get("jobCode") != null))
        {
            task.setJobCode(wi.getBizObject().get("jobCode").toString());
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("jobStarttime") != null))
        {
            task.setJobStarttime(sdf.parse(wi.getBizObject().get("jobStarttime").toString()));
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("jobEndtime") != null))
        {
            task.setJobEndtime(sdf.parse(wi.getBizObject().get("jobEndtime").toString()));
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("rootvcColumn1") != null))
        {
            task.setRootvcColumn1(wi.getBizObject().get("rootvcColumn1").toString());
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("rootvcColumn2") != null))
        {
            task.setRootvcColumn2(wi.getBizObject().get("rootvcColumn2").toString());
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("rootnmColumn1") != null))
        {
            task.setRootnmColumn1(Integer.parseInt(wi.getBizObject().get("rootnmColumn1").toString()));
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("rootnmColumn2") != null))
        {
            task.setRootnmColumn2(Integer.parseInt(wi.getBizObject().get("rootnmColumn2").toString()));
        }

        if ((wi.getBizObject() != null) && (wi.getBizObject().get("SenderID") != null))
        {
            task.setSenderID(wi.getBizObject().get("SenderID").toString());
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("ReceiverID") != null))
        {
            task.setReceiverID(wi.getBizObject().get("ReceiverID").toString());
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("strColumn1") != null))
        {
            task.setStrColumn1(wi.getBizObject().get("strColumn1").toString());
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("strColumn2") != null))
        {
            task.setStrColumn2(wi.getBizObject().get("strColumn2").toString());
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("strColumn3") != null))
        {
            task.setStrColumn3(wi.getBizObject().get("strColumn3").toString());
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("strColumn4") != null))
        {
            task.setStrColumn4(wi.getBizObject().get("strColumn4").toString());
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("strColumn5") != null))
        {
            task.setStrColumn5(wi.getBizObject().get("strColumn5").toString());
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("strColumn6") != null))
        {
            task.setStrColumn6(wi.getBizObject().get("strColumn6").toString());
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("strColumn7") != null))
        {
            task.setStrColumn7(wi.getBizObject().get("strColumn7").toString());
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("datColumn1") != null))
        {
            task.setDatColumn1(sdf.parse(wi.getBizObject().get("datColumn1").toString()));
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("datColumn2") != null))
        {
            task.setDatColumn2(sdf.parse(wi.getBizObject().get("datColumn2").toString()));
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("reBacktime") != null))
        {
            task.setReBacktime(sdf.parse(wi.getBizObject().get("reBacktime").toString()));
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("numColumn1") != null))
        {
            task.setNumColumn1(Integer.parseInt(wi.getBizObject().get("numColumn1").toString()));
        }
        if ((wi.getBizObject() != null) && (wi.getBizObject().get("numColumn2") != null))
        {
            task.setNumColumn2(Integer.parseInt(wi.getBizObject().get("numColumn2").toString()));
        }

        task.setProcessInstID(String.valueOf(wi.getProcessInstID()));

        task.setProcessModelId(String.valueOf(wi.getProcessDefID()));
        task.setProcessModelName(wi.getProcessDefName());

        task.setTaskInstID(String.valueOf(wi.getWorkItemID()));

        if (wi.getIsTimeOut() != null) {
            boolean isTimeOut = wi.getIsTimeOut().equals("Y");
            if (isTimeOut)
                task.setTaskWarning("1");
            else {
                task.setTaskWarning("0");
            }
        }
        return task;
    }

    public static List<ProcessInstance> convert2ProcessInstList(List<WFProcessInst> list)
            throws ParseException
    {
        if (list == null) {
            return new ArrayList(0);
        }

        int size = list.size();
        List result = new ArrayList(size);

        ProcessInstance prs = null;
        for (int i = 0; i < size; i++) {
            prs = new ProcessInstance();

            WFProcessInst pi = (WFProcessInst)list.get(i);
            prs.setParentActID(String.valueOf(pi.getParentActID()));

            prs.setParentProcessInstID(String.valueOf(pi.getParentProcID()));

            prs.setProcessInstID(String.valueOf(pi.getProcessInstID()));

            String CurrentState = String.valueOf(pi.getCurrentState());

            if (CurrentState.equals("2"))
                prs.setProcessInstStatus("1");
            else if (CurrentState.equals("3"))
                prs.setProcessInstStatus("3");
            else if (CurrentState.equals("7"))
                prs.setProcessInstStatus("4");
            else if (CurrentState.equals("8")) {
                prs.setProcessInstStatus("5");
            }
            prs.setProcessModelID(String.valueOf(pi.getProcessDefID()));

            prs.setProcessModelName(pi.getProcessDefName());

            prs.setStartAccountID(pi.getCreator());
            prs.setStartDate(sdf.parse(pi.getStartTime()));

            result.add(prs);
        }

        return result;
    }

    public static List<ActivityInstance> convert2ActivityInstList(List<WFActivityInst> list)
            throws ParseException, WFServiceException
    {
        if (list == null) {
            return new ArrayList(0);
        }

        int size = list.size();
        List result = new ArrayList(size);

        for (int i = 0; i < size; i++) {
            WFActivityInst wfActivityInst = (WFActivityInst)list.get(i);
            ActivityInstance act = convert2ActivityInst(wfActivityInst);

            result.add(act);
        }

        return result;
    }

    public static ActivityInstance convert2ActivityInst(WFActivityInst wfActivityInst)
            throws ParseException, WFServiceException
    {
        ActivityInstance act = null;
        act = new ActivityInstance();

        act.setActivityDefID(wfActivityInst.getActivityDefID());
        act.setActivityInstDesc(wfActivityInst.getActivityInstDesc());
        act.setActivityInstID(String.valueOf(wfActivityInst.getActivityInstID()));

        act.setActivityInstName(wfActivityInst.getActivityInstName());
        act.setActivityType(wfActivityInst.getActivityType());
        act.setCreateTime(sdf.parse(wfActivityInst.getCreateTime().toString()));

        String CurrentState = String.valueOf(wfActivityInst.getCurrentState());

        if (CurrentState.equals("2"))
            act.setCurrentState("1");
        else if (CurrentState.equals("3"))
            act.setCurrentState("3");
        else if (CurrentState.equals("7"))
            act.setCurrentState("4");
        else if (CurrentState.equals("8")) {
            act.setCurrentState("5");
        }
        act.setEndTime(sdf.parse(wfActivityInst.getEndTime()));

        act.setProcessInstID(String.valueOf(wfActivityInst.getProcessInstID()));

        act.setRollbackFlag(wfActivityInst.getRollbackFlag());
        act.setStartTime(sdf.parse(wfActivityInst.getStartTime()));

        IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();

        if (wfActivityInst.getActivityType().equals("subflow")) {
            long[] a = client.getProcessInstManager().querySubProcessInstIDsByActivityInstID(wfActivityInst.getActivityInstID());

            List subProcessID = new ArrayList();
            for (long aa : a) {
                subProcessID.add(aa + "");
            }
            act.setSubProcessID(subProcessID);
        }
        return act;
    }

    public static Participant convert2Participant(WFParticipant wfparticipant)
            throws AdapterException
    {
        if (wfparticipant == null) {
            return null;
        }
        Participant participant = new Participant();
        if (wfparticipant.getTypeCode().equalsIgnoreCase("person"))
        {
            participant.setParticipantType("1");
        } else if (wfparticipant.getTypeCode().equalsIgnoreCase("role"))
        {
            participant.setParticipantType("2");
        } else if (wfparticipant.getTypeCode().equalsIgnoreCase("organization"))
        {
            participant.setParticipantType("3");
        }
        else {
            throw new AdapterException(wfparticipant.getTypeCode() + "为不支持的类型");
        }
        participant.setParticipantID(wfparticipant.getId());
        if (wfparticipant.getName() != null) {
            participant.setParticipantName(wfparticipant.getName());
        }
//        if (wfparticipant.getAttribute("Permission") != null) {
//            participant.setParticipantStatus(wfparticipant.getAttribute("Permission").toString());
//        }
        return participant;
    }

    public static List<Participant> convert2PartticipantList(List<WFParticipant> wfparticipants)
            throws AdapterException
    {
        List parts = new ArrayList();
        if ((wfparticipants == null) || (wfparticipants.size() == 0)) {
            return parts;
        }
        for (int i = 0; i < wfparticipants.size(); i++) {
            parts.add(convert2Participant((WFParticipant)wfparticipants.get(i)));
        }
        return parts;
    }

    public static List<WFParticipant> convert2WFParticipantList(List<Participant> participants)
            throws AdapterException
    {
        List wfparts = new ArrayList();
        if ((participants == null) || (participants.size() == 0)) {
            return wfparts;
        }
        for (int i = 0; i < participants.size(); i++) {
            wfparts.add(convert2WFParticipant((Participant)participants.get(i)));
        }
        return wfparts;
    }

    public static WFParticipant convert2WFParticipant(Participant participant)
            throws AdapterException
    {
        if (participant == null) {
            return null;
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
        return wfparticipant;
    }

    public static ActivityDef convert2WFActivityDefine(WFActivityDefine activityDef)
    {
        ActivityDef wfactividyDefine = new ActivityDef();
        wfactividyDefine.setActivityID(activityDef.getId());
        wfactividyDefine.setActivityName(activityDef.getName());

        wfactividyDefine.setActivitytype(activityDef.getType());
        return wfactividyDefine;
    }

    public static ProcessModel convert2ProcessModel(WFProcessDefine processDef)
    {
        ProcessModel processModel = new ProcessModel();
        processModel.setProcessModelID(processDef.getProcessDefID().toString());
        processModel.setProcessModelName(processDef.getProcessDefName());
        processModel.setProcessModelDes(processDef.getProcessChName());
        return processModel;
    }

    public static NotificationInstance convert2Notification(WFNotificationInst wfnoti)
            throws ParseException
    {
        if (wfnoti == null) {
            return null;
        }
        NotificationInstance noti = new NotificationInstance();
        noti.setActivitiInstID("" + wfnoti.getActInstID());
        noti.setActivityInstName(wfnoti.getActInstName());

        noti.setDeliveryDate(sdf.parse(wfnoti.getCreateTime()));
        if(wfnoti.getConfirmTime() != null){
            noti.setReadDate(sdf.parse(wfnoti.getConfirmTime()));
        }

        noti.setFormURL(wfnoti.getActionURL());
        noti.setJobID(wfnoti.getMessage());
        noti.setJobTitle(wfnoti.getTitle());
        noti.setNotificationInstID("" + wfnoti.getNotificationID());
        noti.setProcessInstID("" + wfnoti.getProcInstID());

        noti.setProcessModelName(wfnoti.getProcDefName());
        noti.setSenderID(wfnoti.getSender());

        noti.setTaskinstid("" + wfnoti.getWorkItemID());
        return noti;
    }

    public static List<NotificationInstance> convert2NotificationList(List<WFNotificationInst> wfnotis)
            throws ParseException
    {
        List notis = new ArrayList();
        if ((wfnotis != null) && (wfnotis.size() > 0)) {
            for (int i = 0; i < wfnotis.size(); i++) {
                notis.add(convert2Notification((WFNotificationInst)wfnotis.get(i)));
            }
        }
        return notis;
    }
}
