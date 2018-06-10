package com.metarnet.core.common.controller;

import com.alibaba.fastjson.JSON;
import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.EnumConfigAdapter;
import com.metarnet.core.common.adapter.WorkflowAdapter;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.core.common.model.EnumValue;
import com.metarnet.core.common.model.GeneralInfoModel;
import com.metarnet.core.common.model.TreeNode;
import com.metarnet.core.common.model.WorkFlowMonitorTreeNode;
import com.metarnet.core.common.service.IWorkflowBaseService;
import com.metarnet.core.common.utils.Constants;
import com.ucloud.paas.agent.PaasException;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 流程监控日志
 *
 * @author zwwang
 */
@Controller
public class CommWorkFlowMonitorController extends BaseController {


    @Resource
    private IWorkflowBaseService workflowBaseService;

    @RequestMapping(value = "/commWorkFlowMonitorController.do", params = "method=getOrderLog")
    public void getOrderLog(HttpServletRequest request, HttpServletResponse response, String rootProcessInstId, String jobID) throws UIException {
        JSONObject tree = new JSONObject();
        String accountId = getAccountId(request);

        List<GeneralInfoModel> orderLogModels = null;
        List<GeneralInfoModel> nowActivityList = null;
        Map<String, String> processInstID2treeNodeIdMap = null;
        Map<String, WorkFlowMonitorTreeNode> processInstID2treeNodeMap = null;

        Map<Integer, WorkFlowMonitorTreeNode> group2treeNodeMap = new HashMap<Integer, WorkFlowMonitorTreeNode>();
        ;
        int currentTreeNodeId = 0;
        String parentTreeNodeId = null;
        WorkFlowMonitorTreeNode parentTreeNode = null;

        try {
            orderLogModels = workflowBaseService.getGeneralInfoByRootProcessId(rootProcessInstId);
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        try {
            nowActivityList = workflowBaseService.getAllActivityInstanceInfos(rootProcessInstId, jobID, getUserEntity(request));
        } catch (ServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        List<WorkFlowMonitorTreeNode> trees = new ArrayList();
        if (orderLogModels != null) {
            if (nowActivityList != null) {
                orderLogModels.addAll(nowActivityList);
            }

            /*Collections.sort(orderLogModels, new Comparator<GeneralInfoModel>() {

                @Override
                public int compare(GeneralInfoModel o1, GeneralInfoModel o2) {
                    if (o1.getCreationTime().after(o2.getCreationTime())) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });*/

            processInstID2treeNodeIdMap = new HashMap<String, String>();
            processInstID2treeNodeMap = new HashMap<String, WorkFlowMonitorTreeNode>();

            for (int i = 0; i < orderLogModels.size(); i++) {
                WorkFlowMonitorTreeNode treeNode = new WorkFlowMonitorTreeNode();

                GeneralInfoModel logModel = orderLogModels.get(i);

//                processInstID2treeNodeIdMap.put(logModel.getProcessInstanceId() , String.valueOf(currentTreeNodeId));
                treeNode.setId(String.valueOf(currentTreeNodeId));
                treeNode.setOperateOrg(logModel.getOperOrgName());
                if (logModel.getCreationTime() != null) {
                    String creationDateStr = logModel.getCreationTime().toString();
                    creationDateStr = creationDateStr.substring(0, 19);
                    treeNode.setArriveDateTime(creationDateStr);
                }

                treeNode.setOperator(logModel.getOperUserTrueName());

                treeNode.setActivityName(logModel.getActivityInstName());
                if (logModel.getOperTime() != null) {
                    String operateTimeStr = logModel.getOperTime().toString();
                    operateTimeStr = operateTimeStr.substring(0, 19);
                    treeNode.setCompleteDateTime(operateTimeStr);
                } else {
                    treeNode.setNowActivity(true);
                }
                if (logModel.getOperTypeEnumId() != null) {
                    try {
                        EnumValue enumValue = EnumConfigAdapter.getInstence().getEnumValueById(logModel.getOperTypeEnumId());
                        if (enumValue != null) {
                            if (40050227 == logModel.getOperTypeEnumId() || 40050228 == logModel.getOperTypeEnumId()) {
                                if (logModel.getProcessingStatus().equals(Constants.Y)) {
                                    if(40050227 == logModel.getOperTypeEnumId()&&StringUtils.isNotBlank(logModel.getAttribute1())){
                                        if("up".equals(logModel.getAttribute1())){
                                            treeNode.setProcessType(enumValue.getEnumValueName() + "-上报");
                                        }else if("peer".equals(logModel.getAttribute1())){
                                            treeNode.setProcessType(enumValue.getEnumValueName() + "-同级审核");
                                        }
                                    }else{
                                        treeNode.setProcessType(enumValue.getEnumValueName() + "-通过");
                                    }
                                } else {
                                    treeNode.setProcessType(enumValue.getEnumValueName() + "-驳回");
                                }
                            } else {
                                treeNode.setProcessType(enumValue.getEnumValueName());
                            }
                        }

                    } catch (PaasException e) {
                        e.printStackTrace();
                    }
                }

                treeNode.setProcessOpinion(logModel.getOperDesc());

                /*String parentProcessInstID = logModel.getParentProInstId();
                if(parentProcessInstID == null || "".equals(parentProcessInstID)){
                    try {
                        parentProcessInstID = WorkflowAdapter.getProcessInstance(accountId, orderLogModels.get(i).getProcessInstId()).getParentProcessInstID();
                    } catch (AdapterException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }*/

                treeNode.setProcessingObjectTable(logModel.getProcessingObjectTable());

//                processInstID2treeNodeMap.put(logModel.getProcessInstId() , treeNode);

                if (logModel.getBelongCityCode() == null) {
                    if ((parentTreeNode = group2treeNodeMap.get(logModel.getBelongProvinceCode())) == null) {
                        parentTreeNode = new WorkFlowMonitorTreeNode();
                        parentTreeNode.setId(currentTreeNodeId + "-parent");
                        parentTreeNode.setOperateOrg(logModel.getBelongProvinceName());
                        trees.add(parentTreeNode);
                        group2treeNodeMap.put(logModel.getBelongProvinceCode(), parentTreeNode);
                    }
                } else {
                    WorkFlowMonitorTreeNode grandParTreeNode = group2treeNodeMap.get(logModel.getBelongProvinceCode());

                    if ((parentTreeNode = group2treeNodeMap.get(logModel.getBelongCityCode())) == null) {
                        parentTreeNode = new WorkFlowMonitorTreeNode();
                        parentTreeNode.setId(currentTreeNodeId + "-parent");
                        parentTreeNode.setOperateOrg(logModel.getBelongCityName());
                        group2treeNodeMap.put(logModel.getBelongCityCode(), parentTreeNode);
                        if (grandParTreeNode != null) {
                            parentTreeNode.setParentId(grandParTreeNode.getId());
                            grandParTreeNode.getChildren().add(parentTreeNode);
                        } else {
                            parentTreeNode.setOperateOrg(logModel.getBelongProvinceName());
                            trees.add(parentTreeNode);
                        }
                    }


                }

                treeNode.setParentId(parentTreeNode.getId());
                parentTreeNode.getChildren().add(treeNode);


//                trees.add(treeNode);

                currentTreeNodeId++;
            }
        }
        List newTree = transTrees(trees);
        tree.put("data", newTree);
        endHandle(request, response, JSON.toJSONString(tree), "", false);
    }

    private List transTrees(List<WorkFlowMonitorTreeNode> trees) {
        List<WorkFlowMonitorTreeNode> newTrees = new ArrayList();
        for (int i = 0; i < trees.size(); i++) {
            WorkFlowMonitorTreeNode treeNode = trees.get(i);
            newTrees.add(treeNode);
            List list = getChildren(treeNode);
            if (list != null && list.size() > 0) {
                newTrees.addAll(list);
            }
        }
        return newTrees;
    }

    private List<WorkFlowMonitorTreeNode> getChildren(WorkFlowMonitorTreeNode treeNode) {
        List<WorkFlowMonitorTreeNode> finalChildren = new ArrayList<WorkFlowMonitorTreeNode>();
        List<WorkFlowMonitorTreeNode> children = treeNode.getChildren();
        if (children.size() != 0) {
            for (int i = 0; i < children.size(); i++) {
                WorkFlowMonitorTreeNode node = children.get(i);
                finalChildren.add(node);
                List list = getChildren(node);
                finalChildren.addAll(list);
            }
        } else {
            return children;
        }
        return finalChildren;
    }

    private void addTreeNode(List<TreeNode> trees, WorkFlowMonitorTreeNode treeNode) {
        if ("".equals(treeNode.getParentId()) || treeNode.getParentId() == null) {
            trees.add(treeNode);
        } else {
            String parNodeId = treeNode.getParentId();
            for (int i = Integer.valueOf(parNodeId); i < trees.size(); i++) {
                WorkFlowMonitorTreeNode parTreeNode = (WorkFlowMonitorTreeNode) trees.get(i);
                if (parTreeNode.getId().equals(parNodeId)) {
                    trees.add(i + 1, treeNode);
                }
            }
        }
    }

    @RequestMapping(value = "/commWorkFlowMonitorController.do", params = "method=getUserEntityByUserNames")
    public void getUserEntityByUserNames(HttpServletRequest request, HttpServletResponse response, String userNames) throws UIException {

        List<String> userNameList = new ArrayList<String>();
        String[] userNamesArray = userNames.split(",");
        for (int i = 0; i < userNamesArray.length; i++) {
            userNameList.add(userNamesArray[i]);
        }

        List<UserEntity> list = null;
        try {
            list = AAAAAdapter.getInstence().findUserListByUserNames(userNameList);
            for (UserEntity userEntity : list) {
                userEntity.setOrgName(userEntity.getOrgEntity().getFullOrgName());
            }
        } catch (PaasAAAAException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        endHandle(request, response, JSON.toJSONString(list), "", false);
    }

    @RequestMapping(value = "/commWorkFlowMonitorController.do", params = "method=getUserEntityByIds")
    public void getUserEntityByIds(HttpServletRequest request, HttpServletResponse response, String ids, String type) throws UIException {

        List<UserEntity> list = null;
        try {
            if ("name".equals(type)) {
                List<String> names = Arrays.asList(ids.split(","));
                list = AAAAAdapter.getInstence().findUserListByUserNames(names);
            } else {
                list = AAAAAdapter.getInstence().findUserListByUserIDs(ids);
                for (UserEntity userEntity : list) {
                    userEntity.setOrgName(userEntity.getOrgEntity().getOrgName());
                }
            }
        } catch (PaasAAAAException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        endHandle(request, response, JSON.toJSONString(list), "", false);
    }

}
