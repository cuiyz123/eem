package com.metarnet.core.common.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.WorkflowAdapter;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.core.common.model.DispatchModel;
import com.metarnet.core.common.model.TreeNode;
import com.metarnet.core.common.model.TreeNodeVo;
import com.metarnet.core.common.utils.Constants;
import com.metarnet.core.common.utils.PubFun;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import com.unicom.ucloud.workflow.objects.Participant;
import com.unicom.ucloud.workflow.objects.TaskInstance;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created with IntelliJ IDEA. User: jietianwu Date: 13-4-18 Time: 下午5:23 公共的树组件
 */
@Controller
public class CommTreeController extends BaseController {
    Logger logger = Logger.getLogger("CommTreeController");

    /**
     * 部门树
     *
     * @param dispatchModel 派发树数据规则
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/commTreeController.do", params = "method=createOrgTree")
    @ResponseBody
    public void createOrgTree(DispatchModel dispatchModel, HttpServletRequest request, HttpServletResponse response,
                              boolean onlyCompany) throws UIException {
        List<TreeNode> trees = new ArrayList<TreeNode>();
        // 派发部门
        try {
            OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgID(dispatchModel.getCloudOrgId().longValue());
            trees.add(createOrgTreeNode(orgEntity, onlyCompany, true, Boolean.parseBoolean(request.getParameter("returnNodeType"))));
        } catch (PaasAAAAException e) {
//            throw new UIException("查询部门列表失败  orgId=" + dispatchModel.getCloudOrgId(), "派发树查询出错", dispatchModel
//                    .getCloudOrgId()
//                    + "");
        }
        endHandleTree(request, response, trees, "查询部门人员树失败  orgId=" + dispatchModel.getCloudOrgId(), true);
    }

    /**
     * 人员树
     *
     * @param dispatchModel 派发树数据规则
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/commTreeController.do", params = "method=createUserTree")
    @ResponseBody
    public void createUserTree(DispatchModel dispatchModel, HttpServletRequest request, HttpServletResponse response,
                               boolean expand) throws UIException {

//        LinkedList<TreeNode> trees = new LinkedList<TreeNode>();
//        try {
//            TesTime t = new TesTime();
//            List<UserEntity> userEntityList = AAAAAdapter.getInstence().findUserListByOrgID(
//                    dispatchModel.getCloudOrgId());
//            OrgEntity thisOrgEntity = AAAAAdapter.getInstence().findOrgByOrgID(dispatchModel.getCloudOrgId().longValue());
//
//            List<OrgEntity> orgList = AAAAAdapter.getInstence().findOrgListByParentID(dispatchModel.getCloudOrgId());
//            String oragName = thisOrgEntity.getOrgName();
//            Boolean returnNodeType = Boolean.parseBoolean(request.getParameter("returnNodeType"));
//            if (orgList != null) {
//                for (OrgEntity orgEntity : orgList) {
//                    if (orgEntity != null) {
//                        trees.add(createUserTreeNode(orgEntity, returnNodeType));
//                    }
//                }
//            }
//            if (userEntityList != null) {
//                for (UserEntity userEntity : userEntityList) {
//                    TreeNode chiTreeNode = new TreeNode();
//                    String nodeId = userEntity.getUserId().toString();
//                    if (returnNodeType) {
//                        nodeId += ":" + Constants.DISPATCH_OBJECT_TYPE_MEMBER;
//                    }
//                    chiTreeNode.setId(nodeId);
//                    chiTreeNode.setText(userEntity.getTrueName());
//                    Map m = new HashMap<String, String>();
//                    m.put("cloudOrgId", userEntity.getOrgID());
//                    m.put("orgName", oragName);
//                    m.put("mobTel", userEntity.getMobilePhone());
//                    m.put("officeTel", userEntity.getTelephone());
////                    m.put("faxTel", userEntity.getNoteInfo() != null ? userEntity.getNoteInfo().getFaxTel() : null);
//                    m.put("emailAddress", userEntity.getAddress());
//                    chiTreeNode.setReturnValue(JSONObject.fromObject(m).toString());
//                    chiTreeNode.setHasChild("0");
//                    trees.add(chiTreeNode);
//                }
//            }
//
//            Boolean showParent = Boolean.parseBoolean(request.getParameter("showParent"));
//            t.printInfo("+++createUserTree+++");
//            if (showParent) {
//                List<TreeNode> rootNode = new ArrayList<TreeNode>();
//                TreeNode treeNode = new TreeNode();
//                treeNode.setId(thisOrgEntity.getOrgId() + ":" + Constants.DISPATCH_OBJECT_TYPE_ORG);
//                treeNode.setText(thisOrgEntity.getOrgName());
//                treeNode.setHasChild("1");
//                treeNode.setDefaultOpen("1");
//                treeNode.setTreeNode(trees);
//                rootNode.add(treeNode);
//                JSONObject jsonObject = new JSONObject();
//                JSONArray treeNodes = JSONArray.fromObject(rootNode);
//                super.removeEmptyTreeNodes(jsonObject, treeNodes);
//                endHandle(request, response, DFXmlJsonConverter.dfJson2XmlFormat(jsonObject), "", false);
//            } else {
//                endHandleTree(request, response, trees, "查询部门人员树失败  orgId=" + dispatchModel.getCloudOrgId(), false);
//            }
//        } catch (PaasAAAAException e) {
////            throw new UIException("查询人员列表失败  orgId=" + dispatchModel.getCloudOrgId(), "派发树查询出错", dispatchModel
////                    .getCloudOrgId()
////                    + "");
//        }

    }

    /**
     * 派发树
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/commTreeController.do", params = "method=createDisPatchTree2")
    @ResponseBody
    public void createDisPatchTree2(HttpServletRequest request, HttpServletResponse response, int type) throws UIException, PaasAAAAException, AdapterException {
        List<TreeNodeVo> list2 = new ArrayList<TreeNodeVo>();
        UserEntity user = getUserEntity(request);
        List<OrgEntity> orgList;
//        OrgEntity thisOrgEntity = AAAAAdapter.getInstence().findOrgByOrgID(user.getOrgID());
        OrgEntity thisOrgEntity = user.getOrgEntity();
        String topOrgId = request.getParameter("rootOrgId");
        if (topOrgId != null && !"".equals(topOrgId)) {
            thisOrgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(topOrgId);
        }
        String rootAsCompany = request.getParameter("rootAsCompany");
        if (!"1".equals(rootAsCompany)) {
//            thisOrgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(thisOrgEntity.getProCode());
//            thisOrgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(AAAAAdapter.getCompany(thisOrgEntity.getOrgId().intValue()).getOrgCode());
            thisOrgEntity = AAAAAdapter.getCompany(thisOrgEntity.getOrgId().intValue());
        }


        //点击节点展开时，传回的被展开节点ID
        String orgId2 = request.getParameter("id");

        OrgEntity clickOrgEntity = null;
        Comparator<TreeNodeVo> comparator = new Comparator<TreeNodeVo>() {
            public int compare(TreeNodeVo o1, TreeNodeVo o2) {
                if(o1.getSortNum()!=null&&o2.getSortNum()!=null){
                    return o1.getSortNum() == o2.getSortNum() ? 0 :
                            (o1.getSortNum() > o2.getSortNum() ? 1 : -1);
                }else{
                    return 0;
                }
            }
        };
        if (type == 1) {

            if (orgId2 != null && !"".equals(orgId2)) {
                clickOrgEntity = AAAAAdapter.getCompany(Integer.valueOf(orgId2));
                orgList = AAAAAdapter.getInstence().findOrgListByParentID(Integer.parseInt(orgId2));
            } else {
                if ("UNI".equals(thisOrgEntity.getOrgType())) {
                    if (!"1".equals(rootAsCompany)) {
                        orgList = AAAAAdapter.getInstence().findOrgListByParentID(1);
                        /*TreeNodeVo uniNode = new TreeNodeVo();
                        uniNode.setNodeId("UNI");
                        uniNode.setId("UNI");
                        uniNode.setParentId("0");
                        uniNode.setLabel("集团");
                        uniNode.setFullName("集团");
                        uniNode.setOpen(false);
                        uniNode.setType(1);
                        list2.add(uniNode);
                        TreeNodeVo poiNode = new TreeNodeVo();
                        poiNode.setNodeId("PRO");
                        poiNode.setId("PRO");
                        poiNode.setParentId("0");
                        poiNode.setLabel("省分公司");
                        poiNode.setFullName("省分公司");
                        poiNode.setOpen(false);
                        poiNode.setType(1);
                        list2.add(poiNode);
                        TreeNodeVo popNode = new TreeNodeVo();
                        popNode.setNodeId("POP");
                        popNode.setId("POP");
                        popNode.setParentId("0");
                        popNode.setLabel("其他");
                        popNode.setFullName("其他");
                        popNode.setType(1);
                        popNode.setOpen(false);
                        list2.add(popNode);*/
                        TreeNodeVo poiNode = new TreeNodeVo();
                        poiNode.setNodeId("PRO");
                        poiNode.setId("PRO");
                        poiNode.setParentId("0");
                        poiNode.setLabel("省分公司");
                        poiNode.setFullName("省分公司");
                        poiNode.setOpen(true);
                        poiNode.setType(1);
                        list2.add(poiNode);
                    } else {
                        clickOrgEntity = AAAAAdapter.getCompany(thisOrgEntity.getOrgId().intValue());
                        clickOrgEntity.setOrgType("DEP");
                        orgList = AAAAAdapter.getInstence().findOrgListByParentID(thisOrgEntity.getOrgId());
                    }
                } else {
                    TreeNodeVo pNode = new TreeNodeVo();
                    pNode.setNodeId("org-" + thisOrgEntity.getOrgId());
                    pNode.setId(thisOrgEntity.getOrgId().toString());
                    pNode.setParentId("org-" + thisOrgEntity.getParentOrgId().toString());
                    pNode.setLabel(thisOrgEntity.getOrgName().replaceAll("分公司","").replaceAll("省", "").replaceAll("市",""));
                    pNode.setFullName(thisOrgEntity.getFullOrgName());
                    pNode.setOpen(false);
                    pNode.setType(1);
                    pNode.setCode(thisOrgEntity.getOrgCode());
                    pNode.setSortNum(thisOrgEntity.getSortNum());
                    list2.add(pNode);
//                List<UserEntity> userEntityList1 = AAAAAdapter.getInstence().findUserListByOrgID(thisOrgEntity.getOrgId());
                    orgList = AAAAAdapter.getInstence().findOrgListByParentID(thisOrgEntity.getOrgId());
                }
            }
//            List<TreeNode> trees = new ArrayList<TreeNode>();

            for (int i = 0; i < orgList.size(); i++) {
                TreeNodeVo treeNode = new TreeNodeVo();
                if (orgList.get(i).getOrgType().equals("POP")) {
                    continue;
//                    treeNode.setParentId("POP");
                } else if (orgList.get(i).getOrgType().equals("UNI")) {
                    continue;
                    /*treeNode.setParentId("UNI");
//                    List<OrgEntity> uorList=AAAAAdapter.findOrgRecursionListByOrgID(orgList.get(i).getOrgId().intValue());
                    List<OrgEntity> uorList = AAAAAdapter.findOrgListByParentID(orgList.get(i).getOrgId().intValue());
//                    List<UserEntity> userEntityList = AAAAAdapter.getInstence().findUserListByOrgID(orgList.get(i).getOrgId());
                    for (OrgEntity org : uorList) {
                        TreeNodeVo oNode = new TreeNodeVo();
                        oNode.setIsParent(!org.getLeaf());
                        oNode.setNodeId("org-" + org.getOrgId());
                        oNode.setId(org.getOrgId().toString());
                        oNode.setLabel(org.getOrgName());
                        oNode.setFullName(org.getFullOrgName());
                        oNode.setType(1);
                        oNode.setParentId("org-" + org.getParentOrgId().toString());
                        oNode.setOpen(true);
                        oNode.setCode(org.getOrgCode());
                        oNode.setSortNum(org.getSortNum());
                        list2.add(oNode);

                    }*/
                } else if (orgList.get(i).getOrgType().equals("PRO")) {
                    treeNode.setParentId("PRO");
                } else {
                    continue;
//                    treeNode.setParentId("org-" + orgList.get(i).getParentOrgId().toString());
                }
                treeNode.setNodeId("org-" + orgList.get(i).getOrgId());
                treeNode.setId(orgList.get(i).getOrgId().toString());
                treeNode.setLabel(orgList.get(i).getOrgName().replaceAll("分公司","").replaceAll("省","").replaceAll("市",""));
                treeNode.setFullName(orgList.get(i).getFullOrgName());
                treeNode.setType(1);
                treeNode.setCode(orgList.get(i).getOrgCode());
//                if (clickOrgEntity != null && "DEP".equals(clickOrgEntity.getOrgType())) {
                if (clickOrgEntity != null && "DEP".equals(orgList.get(i).getOrgType())) {
                    treeNode.setIsParent(!orgList.get(i).getLeaf());
                } else if("BB".equals(orgList.get(i).getShortName())){
                    treeNode.setIsParent(true);
                    treeNode.setParentId("org-" + orgList.get(i).getParentOrgId().toString());
                } else {
                    treeNode.setIsParent(false);
                }
//                treeNode.setIsParent(!orgList.get(i).getLeaf());
                treeNode.setOpen(false);
                treeNode.setSortNum(orgList.get(i).getSortNum());
                list2.add(treeNode);
            }
            Collections.sort(list2,comparator);
            endHandle(request, response, JSON.toJSONString(list2, true).toString(), "", false);

        } else if(type == 4 || type == 5){
            if(orgId2 != null && !"".equals(orgId2)) {
                List<UserEntity> userEntityList = AAAAAdapter.getInstence().findUserListByOrgID(Integer.parseInt(orgId2));
                if (userEntityList != null) {
                    for (UserEntity userEntity : userEntityList) {
                        TreeNodeVo userNode = new TreeNodeVo();
                        userNode.setNodeId("person-" + userEntity.getUserId());
                        userNode.setId(userEntity.getUserId().toString());
                        userNode.setUserName(userEntity.getUserName());
                        userNode.setParentId("org-" + orgId2);
                        userNode.setLabel(userEntity.getTrueName());
                        userNode.setType(2);
                        userNode.setOpen(false);
                        userNode.setMobilePhone(userEntity.getMobilePhone());
                        userNode.setSortNum(1);
                        list2.add(userNode);
                    }
                }
            } else {
                orgList = AAAAAdapter.getInstence().findProfessionalOfficeByOrgID(user.getOrgID());
                for(int i  = 0 ; i < orgList.size() ; i++){
                    TreeNodeVo oNode = new TreeNodeVo();
                    oNode.setIsParent(true);
                    oNode.setNodeId("org-" + orgList.get(i).getOrgId());
                    oNode.setId(orgList.get(i).getOrgId().toString());
                    oNode.setLabel(orgList.get(i).getOrgName());
                    oNode.setType(1);
                    oNode.setCode(orgList.get(i).getOrgCode());
                    oNode.setParentId("org-" + orgList.get(i).getParentOrgId().toString());
                    if(type == 4){
                        oNode.setIsParent(false);
                        oNode.setOpen(true);
                    } else {
                        oNode.setIsParent(true);
                    }
                    oNode.setSortNum(orgList.get(i).getSortNum());
                    list2.add(oNode);
                }
            }
            Collections.sort(list2,comparator);
            endHandle(request, response, JSON.toJSONString(list2, true).toString(), "", false);
        }  else {
            int cloudOrgId = user.getOrgID().intValue();
//            AAAAAdapter.CompanyType ctype = AAAAAdapter.getCompanyType(cloudOrgId);
            List<TreeNodeVo> trees = new ArrayList();
            Long orgId = user.getOrgID();

            if (orgId2 != null) {
//                orgList = AAAAAdapter.findOrgRecursionListByOrgID(Integer.parseInt(orgId2));
                orgList = AAAAAdapter.findOrgListByParentID(Integer.parseInt(orgId2));
                /*com.metarnet.common.model.TreeNode treeNodePv = new com.metarnet.common.model.TreeNode();
                treeNodePv.setId(thisOrgEntity.getOrgId().toString());
                treeNodePv.setParentId(thisOrgEntity.getParentOrgId().toString());
                treeNodePv.setLabel(thisOrgEntity.getOrgName());
                treeNodePv.setOpen(false);
                trees.add(treeNodePv);*/
                List<UserEntity> userEntityList = AAAAAdapter.getInstence().findUserListByOrgID(Integer.parseInt(orgId2));

                //移除无权限的数据
                removeNoAuthority(userEntityList, orgList, request.getParameter("specialty"), orgId2, request.getParameter("process"), request.getParameter("node"));

                if (userEntityList != null) {

                    for (UserEntity userEntity : userEntityList) {
                        TreeNodeVo userNode = new TreeNodeVo();
                        userNode.setNodeId("person-" + userEntity.getUserId());
                        userNode.setId(userEntity.getUserId().toString());
                        userNode.setUserName(userEntity.getUserName());
                        userNode.setParentId("org-" + userEntity.getOrgID().toString());
                        userNode.setLabel(userEntity.getTrueName());
                        userNode.setType(2);
                        userNode.setOpen(false);
                        userNode.setMobilePhone(userEntity.getMobilePhone());
                        userNode.setSortNum(1);
                        trees.add(userNode);

                    }
                }
                if (orgList != null) {
                    for (OrgEntity org : orgList) {
                        TreeNodeVo orgNode = new TreeNodeVo();
                        orgNode.setParentId("org-" + org.getParentOrgId().toString());
                        orgNode.setNodeId("org-" + org.getOrgId());
                        orgNode.setId(org.getOrgId().toString());
                        orgNode.setIsParent(true);
                        orgNode.setLabel(org.getOrgName());
                        orgNode.setType(1);
                        orgNode.setOpen(false);
                        orgNode.setNocheck(true);
                        orgNode.setCode(org.getOrgCode());
                        orgNode.setSortNum(org.getSortNum());
                        trees.add(orgNode);
                    }
                }
            } else {
                if (orgId != null) {
                    if ("UNI".equals(thisOrgEntity.getOrgType())) {
//                        orgList = AAAAAdapter.findOrgRecursionListByOrgID(1);
                        orgList = AAAAAdapter.findOrgListByParentID(1);
                        TreeNodeVo uniNode = new TreeNodeVo();
                        uniNode.setNodeId("UNI");
                        uniNode.setId("UNI");
                        uniNode.setParentId("0");
                        uniNode.setLabel("集团");
                        uniNode.setType(1);
                        uniNode.setOpen(false);
                        uniNode.setNocheck(true);
                        trees.add(uniNode);
                        TreeNodeVo poiNode = new TreeNodeVo();
                        poiNode.setNodeId("PRO");
                        poiNode.setId("PRO");
                        poiNode.setParentId("0");
                        poiNode.setLabel("省分公司");
                        poiNode.setType(1);
                        poiNode.setOpen(false);
                        poiNode.setNocheck(true);
                        trees.add(poiNode);
                        TreeNodeVo popNode = new TreeNodeVo();
                        popNode.setNodeId("POP");
                        popNode.setId("POP");
                        popNode.setParentId("0");
                        popNode.setLabel("其他");
                        popNode.setType(1);
                        popNode.setOpen(false);
                        popNode.setNocheck(true);
                        trees.add(popNode);
                    } else {
//                        orgList = AAAAAdapter.findOrgRecursionListByOrgID(orgId.intValue());
                        orgList = AAAAAdapter.findOrgListByParentID(thisOrgEntity.getOrgId().intValue());
                        TreeNodeVo treeNodePv = new TreeNodeVo();
                        treeNodePv.setNodeId("org-" + thisOrgEntity.getOrgId());
                        treeNodePv.setId(thisOrgEntity.getOrgId().toString());
                        treeNodePv.setParentId("org-" + thisOrgEntity.getParentOrgId().toString());
                        treeNodePv.setLabel(thisOrgEntity.getOrgName());
                        treeNodePv.setOpen(false);
                        treeNodePv.setNocheck(true);
                        treeNodePv.setCode(thisOrgEntity.getOrgCode());
                        treeNodePv.setSortNum(thisOrgEntity.getSortNum());
                        trees.add(treeNodePv);
                        List<UserEntity> userEntityList = AAAAAdapter.getInstence().findUserListByOrgID(thisOrgEntity.getOrgId());

                        //移除无权限的数据
                        removeNoAuthority(userEntityList, orgList, request.getParameter("specialty"), thisOrgEntity.getOrgId().toString(), request.getParameter("process"), request.getParameter("node"));

                        if (userEntityList != null) {

                            for (UserEntity userEntity : userEntityList) {
                                TreeNodeVo userNode = new TreeNodeVo();
                                userNode.setNodeId("person-" + userEntity.getUserId());
                                userNode.setId(userEntity.getUserId().toString());
                                userNode.setUserName(userEntity.getUserName());
                                userNode.setParentId("org-" + userEntity.getOrgID().toString());
                                userNode.setLabel(userEntity.getTrueName());
                                userNode.setType(2);
                                userNode.setMobilePhone(userEntity.getMobilePhone());
                                userNode.setOpen(false);
                                userNode.setSortNum(1);
                                trees.add(userNode);

                            }
                        }
                    }

                    for (OrgEntity orgEntity : orgList) {
                        if (orgEntity != null) {
                            TreeNodeVo treeNode = new TreeNodeVo();
                            if ("UNI".equals(thisOrgEntity.getOrgType())) {
                                if (orgEntity.getOrgType().equals("UNI")) {
                                    treeNode.setParentId("UNI");
                                    treeNode.setNodeId("org-" + orgEntity.getOrgId());
                                    treeNode.setId(orgEntity.getOrgId().toString());
                                    treeNode.setIsParent(true);
                                    treeNode.setLabel(orgEntity.getOrgName());
                                    treeNode.setType(1);
                                    treeNode.setOpen(false);
                                    treeNode.setNocheck(true);
                                    treeNode.setCode(orgEntity.getOrgCode());
                                    treeNode.setSortNum(orgEntity.getSortNum());
                                    trees.add(treeNode);
                                } else if (orgEntity.getOrgType().equals("POP")) {
                                    treeNode.setParentId("POP");
                                    treeNode.setNodeId("org-" + orgEntity.getOrgId());
                                    treeNode.setId(orgEntity.getOrgId().toString());
                                    treeNode.setIsParent(true);
                                    treeNode.setLabel(orgEntity.getOrgName());
                                    treeNode.setType(1);
                                    treeNode.setOpen(false);
                                    treeNode.setNocheck(true);
                                    treeNode.setCode(orgEntity.getOrgCode());
                                    treeNode.setSortNum(orgEntity.getSortNum());
                                    trees.add(treeNode);
                                } else if (orgEntity.getOrgType().equals("PRO")) {
                                    treeNode.setParentId("PRO");
                                    treeNode.setNodeId("org-" + orgEntity.getOrgId());
                                    treeNode.setId(orgEntity.getOrgId().toString());
                                    treeNode.setIsParent(true);
                                    treeNode.setLabel(orgEntity.getOrgName());
                                    treeNode.setType(1);
                                    treeNode.setOpen(false);
                                    treeNode.setNocheck(true);
                                    treeNode.setCode(orgEntity.getOrgCode());
                                    treeNode.setSortNum(orgEntity.getSortNum());
                                    trees.add(treeNode);
                                } else {
                                    //treeNode.setParentId(orgEntity.getParentOrgId().toString());
                                }
                            } else {
                                treeNode.setParentId("org-" + orgEntity.getParentOrgId().toString());
                                treeNode.setNodeId("org-" + orgEntity.getOrgId());
                                treeNode.setId(orgEntity.getOrgId().toString());
                                treeNode.setIsParent(true);
                                treeNode.setLabel(orgEntity.getOrgName());
                                treeNode.setType(1);
                                treeNode.setOpen(false);
                                treeNode.setNocheck(true);
                                treeNode.setCode(orgEntity.getOrgCode());
                                treeNode.setSortNum(orgEntity.getSortNum());
                                trees.add(treeNode);
                            }
                        }

                    }


                }

            }
            Collections.sort(trees,comparator);
            endHandle(request, response, JSON.toJSONString(trees, true).toString(), "", false);
        }
    }


    // endHandleTree(request, response, trees, "查询派发树失败  userId=" + getUserEntity(request).getUserId(), false);

    /**
     * 模糊查询人员
     *
     * @param request
     * @param name
     * @param response
     * @throws com.metarnet.core.common.exception
     */
    @RequestMapping(value = "/commTreeController.do", params = "method=queryPerson")
    @ResponseBody
    public void queryPerson(HttpServletRequest request, String name, HttpServletResponse response) throws UIException {
        try {
            List arrayList = new ArrayList();
            UserEntity user = getUserEntity(request);
            OrgEntity orgEntity = AAAAAdapter.getCompany(user.getOrgID().intValue());
            Long orgId = orgEntity.getOrgId();
            if ("UNI".equals(orgEntity.getOrgType())) {
                orgId = orgEntity.getParentOrgId();
            }
            //arrayList=AAAAAdapter.findOrgsByOrgIdAndName(orgId,name);
            arrayList = AAAAAdapter.findUserEntityByCloudOrgIdAndEmpName(orgId, name);
            endHandle(request, response, JSON.toJSONString(arrayList, SerializerFeature.DisableCircularReferenceDetect).toString(), "", false);

        } catch (PaasAAAAException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private List<TreeNode> getDisTree(UserEntity user, int treeType, TaskInstance taskInstance,
                                      HttpServletRequest request) throws Exception {
        /*List<AssignTreeEntity> assignList = null;
        List<TreeNode> trees = new ArrayList<TreeNode>();
        LinkedList<Integer> roleLink = new LinkedList<Integer>();
        boolean returnNodeType = Boolean.parseBoolean(request.getParameter("returnNodeType"));
        boolean ifOnlyPerson = Boolean.parseBoolean(request.getParameter("ifOnlyPerson"));
        ActivityModel activityModel = new ActivityModel();
        List<RoleEntity> roleEntities = new ArrayList();
        String abstractRoleId = request.getParameter("abstractRoleId");
        activityModel = ExtendNodeCofnig.parseActivity(user.getAccount().getAccountId(), taskInstance
                .getProcessModelName(), StringUtils.isEmpty(taskInstance.getActivityDefID()) ? Constants.START_ACTIVITY
                : taskInstance.getActivityDefID());
        if (activityModel.getFeedbackAbstractRoleId() != null) {
            Map<String, Set<String>> map = new HashMap();
            if (request.getParameter("areacode") != null) {
                map.put("AREA_ID", new HashSet(Arrays.asList(new String[]{request.getParameter("areacode")})));
            }
            if (request.getParameter("majorcode") != null) {
                map.put("MAJOR_ID", new HashSet(Arrays.asList(new String[]{request.getParameter("majorcode")})));
            }
            if (request.getParameter("productcode") != null) {
                map.put("PRODUCT_ID", new HashSet(Arrays.asList(new String[]{request.getParameter("productcode")})));
            }
            // map.put("ORG_ID", new HashSet(Arrays.asList(new
            // String[]{String.valueOf(AAAAAdapter.getCompany(user
            // .getCloudOrgId()).getCloudOrgId())})));
            try {
                map.put("ABSTRACT_ROLE_ID", new HashSet(Arrays.asList(
                        new String[]{
                                StringUtils.isNotEmpty(abstractRoleId) ? abstractRoleId : activityModel.getFeedbackAbstractRoleId()
                        })));
            } catch (Exception e) {
                logger.info("使用派发树时发现当前环节没有配置feedbackAbstractRoleId,该情况只出现在不派给同级部门时!");
            }
            roleEntities = AAAAAdapter.getInstence().findRoleListByDimensions(map);
            for (RoleEntity roleEntity : roleEntities) {
                roleLink.add(roleEntity.getCloudRoleId());
                // roleLink.add(Integer.parseInt(part.getParticipantID()));
            }
        }
        Integer assignTreeType = treeType;
        if (treeType == 11) {
            assignTreeType = 1;
        } else if (treeType == 21) { //省分派发树添加总部可选
            assignTreeType = 2;
        } else if (treeType == 31) { //地市派发树添加省分公司可选
            assignTreeType = 3;
        }

        assignList = AAAAAdapter.getInstence().getAssignTree(user.getCloudUserId(), (Integer[]) roleLink.toArray(new Integer[0]),
                assignTreeType, AAAAAdapter.getInstence().getCompany(user.getCloudOrgId()).getCloudOrgId());
        if (assignList != null && assignList.size() > 0) {
            Map<Integer, TreeNode> assignMap = new HashMap();
            for (AssignTreeEntity ate : assignList) {
                //是否过滤人
                if (Constants.filterMember && treeType != 3 && treeType != 31 && ate.getNoteType() == 2) {
                    continue;
                }
                if (treeType == 11 && ate.getNoteType() == 1) {
                    OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgID(ate.getNodeId());
                    if (!"Dept".equals(orgEntity.getCuncOrgClass())) {
                        continue;
                    }
                }
                Integer parentOrgId = AAAAAdapter.getInstence().getCompany(user.getCloudOrgId()).getCloudOrgId(); //当前人所在公司的orgId
                if (ifOnlyPerson) {
                    //去掉多余的公司 若 parentOrgId == orgEntity.getParentCloudOrgId() 即省派发树去掉地市公司 总部派发树去掉省公司
                    OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgID(ate.getNodeId()); //4A方法中返回的OrgEntity
                    if (null != orgEntity && parentOrgId.intValue() == orgEntity.getParentCloudOrgId().intValue()) {
                        continue;
                    }
                }
                TreeNode treeNode = new TreeNode();
                String nodeId = ate.getNodeId().toString();
                if (returnNodeType) {
                    String nodeType = Constants.DISPATCH_OBJECT_TYPE_ORG;
                    if (ate.getNoteType() == 2) {
                        nodeType = Constants.DISPATCH_OBJECT_TYPE_MEMBER;
                    }
                    nodeId += ":" + nodeType;
                }
                treeNode.setId(nodeId);
                treeNode.setText(ate.getName());
                treeNode.setHasChild("0");
                assignMap.put(ate.getNodeId(), treeNode);
            }
            if (ifOnlyPerson && assignMap.size() == 1) {
                //assignMap == 1 即没有配置角色，只有一个公司Id的情况。不符合业务要求，clear掉
                assignMap.clear();
            }

            if (assignMap.size() > 0) {
                for (AssignTreeEntity ate : assignList) {
                    if (!assignMap.containsKey(ate.getNodeId())) {
                        continue;
                    }
                    if (assignMap.containsKey(ate.getParentNodeId())) {
                        assignMap.get(ate.getParentNodeId()).setHasChild("1");
                        assignMap.get(ate.getParentNodeId()).setDefaultOpen("1");
                        assignMap.get(ate.getParentNodeId()).getTreeNode().add(assignMap.get(ate.getNodeId()));
                    } else {
                        trees.add(assignMap.get(ate.getNodeId()));
                    }
                }
            }
        }
        OrgEntity orgEntity = null;
        if (treeType == 21) {
            orgEntity = AAAAAdapter.getInstence().findOrgByOrgID(Constants.GROUP_COMPANY_ORG_ID);
            TreeNode tn = createTreeNodeByEntity(orgEntity, Constants.DISPATCH_OBJECT_TYPE_ORG, returnNodeType);
            trees.add(tn);
        } else if (treeType == 31) {
            //   List<Integer> companyIdList = new ArrayList<Integer>();
            Integer userCloudOrgId = getUserEntity(request).getCloudOrgId();
            orgEntity = AAAAAdapter.getInstence().getParentCompany(userCloudOrgId);
            trees.add(createTreeNodeByEntity(orgEntity, Constants.DISPATCH_OBJECT_TYPE_ORG, returnNodeType));
            //   companyIdList.add(orgEntity.getCloudOrgId());
*//*            for (TreeNode tn : trees) {
                Integer parentCloudOrgId = AAAAAdapter.getInstence().findOrgByOrgID(Integer.parseInt(tn.getId())).getParentCloudOrgId();
                if (!companyIdList.contains(parentCloudOrgId)) {
                    orgEntity = AAAAAdapter.getInstence().findOrgByOrgID(parentCloudOrgId);
                    TreeNode provinceNode = createTreeNodeByEntity(orgEntity, "org");
                    trees.add(provinceNode);
                    companyIdList.add(parentCloudOrgId);
                }
            }*//*
        }*/
        //treeType  1集团   2省分    3地市
        List<TreeNode> trees = new ArrayList<TreeNode>();
        List<OrgEntity> orgEntityList = new ArrayList<OrgEntity>();
        OrgEntity orgEntity = user.getOrgEntity();
        if ("UNI".equals(orgEntity.getOrgType())) {
            orgEntityList = AAAAAdapter.getInstence().findOrgListByParentID(1);
        } else {
            orgEntityList = AAAAAdapter.getInstence().findOrgListByParentID(user.getOrgID());
        }
        for (OrgEntity org : orgEntityList) {
            trees.add(createTreeNodeByEntity(org, Constants.DISPATCH_OBJECT_TYPE_ORG, true));
        }
        return trees;
    }

    /**
     * 同级部门人员树
     *
     * @param dispatchModel 派发树数据规则
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/commTreeController.do", params = "method=createParentOrgUserTree")
    @ResponseBody
    public void createParentOrgUserTree(DispatchModel dispatchModel, HttpServletRequest request,
                                        HttpServletResponse response) throws UIException {
        List<TreeNode> trees = new ArrayList<TreeNode>();
        try {
            OrgEntity org = AAAAAdapter.getInstence().findOrgByOrgID(dispatchModel.getCloudOrgId().longValue());
            if (org.getParentOrgId() == null || org.getParentOrgId() == 0) {
                org.setParentOrgId(Constants.GROUP_COMPANY_ORG_ID.longValue());
            }
            List<OrgEntity> orgList = AAAAAdapter.getInstence().findOrgListByParentID(org.getParentOrgId().intValue());
            List<UserEntity> userEntityList = AAAAAdapter.getInstence().findUserListByOrgID(org.getParentOrgId().intValue());
            String oragName = AAAAAdapter.getInstence().findOrgByOrgID(org.getParentOrgId()).getOrgName();
            Boolean returnNodeType = Boolean.parseBoolean(request.getParameter("returnNodeType"));
            OrgEntity rootOrg = AAAAAdapter.getInstence().findOrgByOrgID(org.getParentOrgId());
            trees.add(createUserTreeNode(rootOrg, returnNodeType));
            trees.get(0).setDefaultOpen("1");
            if (orgList != null) {
                for (OrgEntity orgEntity : orgList) {
                    if (orgEntity != null) {
                        trees.get(0).getTreeNode().add(createUserTreeNode(orgEntity, returnNodeType));
                    }
                }
            }
            if (userEntityList != null) {
                for (UserEntity userEntity : userEntityList) {
                    TreeNode chiTreeNode = new TreeNode();
                    String nodeId = userEntity.getUserId().toString();
                    if (returnNodeType) {
                        nodeId += ":" + Constants.DISPATCH_OBJECT_TYPE_MEMBER;
                    }
                    chiTreeNode.setId(nodeId);
                    chiTreeNode.setText(userEntity.getTrueName());
                    Map m = new HashMap<String, String>();
                    m.put("cloudOrgId", userEntity.getOrgID());
                    m.put("orgName", oragName);
                    m.put("mobTel", userEntity.getMobilePhone());
                    m.put("officeTel", userEntity.getTelephone());
//                    m.put("faxTel", userEntity.getNoteInfo() != null ? userEntity.getNoteInfo().getFaxTel() : null);
                    m.put("emailAddress", userEntity.getAddress());
                    chiTreeNode.setReturnValue(JSON.toJSONString(m));
                    chiTreeNode.setHasChild("0");
                    trees.get(0).getTreeNode().add(chiTreeNode);
                }
            }
        } catch (PaasAAAAException e) {
//            throw new UIException("查询同级部门人员树失败  orgId=" + dispatchModel.getCloudOrgId(), "派发树查询出错", dispatchModel
//                    .getCloudOrgId()
//                    + "");
        }
        endHandleTree(request, response, trees, "查询同级部门人员树失败  orgId=" + dispatchModel.getCloudOrgId(), false);
    }

    /**
     * 创建部门节点
     *
     * @param orgEntity 部门实体
     */
    private TreeNode createOrgTreeNode(OrgEntity orgEntity, boolean onlyCompany, boolean expand, boolean returnNodeType) {
        TreeNode treeNode = new TreeNode();
        String nodeId = orgEntity.getOrgId().toString();
        if (returnNodeType) {
            nodeId += ":" + Constants.DISPATCH_OBJECT_TYPE_ORG;
        }
        treeNode.setId(nodeId);
        treeNode.setText(orgEntity.getOrgName());
        treeNode.setHasChild("1");
        treeNode.setDefaultOpen(expand ? "1" : "0");
        treeNode.setXmlSource("../../commTreeController.do?method=getSubOrg&cloudOrgId=" + orgEntity.getOrgId()
                + "&onlyCompany=" + onlyCompany + "&expand=" + !expand + "&returnNodeType=" + returnNodeType);
        return treeNode;
    }

    /**
     * 创建人员节点
     *
     * @param orgEntity 部门实体
     */
    private TreeNode createUserTreeNode(OrgEntity orgEntity, boolean returnNodeType) {
        TreeNode treeNode = new TreeNode();
        String nodeId = orgEntity.getOrgId().toString();
        if (returnNodeType) {
            nodeId += ":" + Constants.DISPATCH_OBJECT_TYPE_ORG;
        }
        treeNode.setId(nodeId);
        treeNode.setText(orgEntity.getOrgName());
        treeNode.setHasChild("1");
        treeNode.setXmlSource("../../commTreeController.do?method=createUserTree&cloudOrgId=" + orgEntity.getOrgId() + "&returnNodeType=" + returnNodeType);
        return treeNode;
    }

    /**
     * 创建没有直接点的NODE
     *
     * @param obj
     * @param nodeType
     * @return
     * @throws Exception
     */
    private TreeNode createTreeNodeByEntity(Object obj, String nodeType, boolean returnNodeType) throws Exception {
        TreeNode treeNode = new TreeNode();
        try {
            if (nodeType.equals(Constants.DISPATCH_OBJECT_TYPE_MEMBER)) {
                UserEntity userEntity = (UserEntity) obj;
                String nodeId = userEntity.getUserId().toString();
                if (returnNodeType) {
                    nodeId += ":" + Constants.DISPATCH_OBJECT_TYPE_MEMBER;
                }
                treeNode.setId(nodeId);
                treeNode.setText(userEntity.getTrueName());
                Map m = new HashMap<String, String>();
                m.put("cloudOrgId", userEntity.getOrgID());
                m.put("orgName", AAAAAdapter.getInstence().findOrgByOrgID(userEntity.getOrgID()).getOrgName());
                m.put("mobTel", userEntity.getMobilePhone());
                m.put("officeTel", userEntity.getTelephone());
//                m.put("faxTel", userEntity.getNoteInfo() != null ? userEntity.getNoteInfo().getFaxTel() : null);
                m.put("emailAddress", userEntity.getAddress());
                treeNode.setReturnValue(JSON.toJSONString(m));
            } else {
                OrgEntity orgEntity = (OrgEntity) obj;
                String nodeId = orgEntity.getOrgId().toString();
                if (returnNodeType) {
                    nodeId += ":" + Constants.DISPATCH_OBJECT_TYPE_ORG;
                }
                treeNode.setId(nodeId);
                treeNode.setText(orgEntity.getOrgName());
            }
        } catch (Exception e) {
//            throw new UIException("", e.getMessage());
        }
        treeNode.setHasChild("0");
        return treeNode;

    }


    private void endHandleTree(HttpServletRequest request, HttpServletResponse response, List trees, String errorMsg,
                               boolean write) throws UIException {
        if (trees.size() > 0) {
            Map map = new HashMap();
            map.put("TreeNode", trees);
            JSONObject jsonObject = (JSONObject)JSON.toJSON(map);
            JSONArray treeNodes = (JSONArray) jsonObject.get("TreeNode");
            removeEmptyTreeNodes(jsonObject, treeNodes);
//            String xmlString = DFXmlJsonConverter.dfJson2XmlFormat(jsonObject);
//            endHandle(request, response, xmlString, errorMsg, write);
        } else {
//            endHandle(request, response, DFXmlJsonConverter.dfJson2XmlFormat(null), errorMsg, write);
        }
    }

    /**
     * 获取子部门节点
     *
     * @param dispatchModel
     * @param request
     * @param response
     * @throws com.metarnet.core.common.exception.UIException
     */
    @RequestMapping(value = "/commTreeController.do", params = "method=getSubOrg")
    @ResponseBody
    public void createSubOrgTreeNode(DispatchModel dispatchModel, HttpServletRequest request,
                                     HttpServletResponse response, boolean onlyCompany) throws UIException {
        List<TreeNode> trees = new ArrayList<TreeNode>();

        try {
            List<OrgEntity> orgList = AAAAAdapter.getInstence().findOrgListByParentID(dispatchModel.getCloudOrgId());
            boolean returnNodeType = Boolean.parseBoolean(request.getParameter("returnNodeType"));
            if (null != orgList && orgList.size() != 0) {
                for (OrgEntity orgEntity : orgList) {
                    if (onlyCompany) {
                        if (orgEntity.getOrgType().equals("ORG")) {
                            trees.add(createOrgTreeNode(orgEntity, onlyCompany, false, returnNodeType));
                        }
                    } else
                        trees.add(createOrgTreeNode(orgEntity, onlyCompany, false, returnNodeType));
                }
            }
        } catch (PaasAAAAException e) {
//            throw new UIException("查询部门列表失败  orgId=" + dispatchModel.getCloudOrgId(), "派发树查询出错", dispatchModel
//                    .getCloudOrgId()
//                    + "");
        }
        endHandleTree(request, response, trees, "查询部门人员树失败  orgId=" + dispatchModel.getCloudOrgId(), false);
    }

    /**
     * 获取显示信息
     *
     * @param request
     * @param response
     * @throws com.metarnet.core.common.exception.UIException
     */
    @RequestMapping(value = "/commTreeController.do", params = "method=getDisplayById")
    @ResponseBody
    public void getOrgOrUserNames(HttpServletRequest request, HttpServletResponse response) throws UIException {
        String names = "";
        String fieldType = request.getParameter("fieldType");
        String fieldId = request.getParameter("fieldId");
        try {
            fieldId = java.net.URLDecoder.decode(fieldId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String valueId = request.getParameter("valueId");
        Boolean fullName = Boolean.valueOf(request.getParameter("fullName"));
        Map<String, Object> mapPerOrg = new HashMap<String, Object>();
        try {
            String[] idArray = fieldId.split(",");
            for (String id : idArray) {
                String name = "";
                try {
                    id = id.split(":")[0];
                    if (!PubFun.isNumeric(id)) {
                        names += id + ',';
                        continue;
                    }
                    if ("org".equals(fieldType)) {
                        OrgEntity o = AAAAAdapter.getInstence().findOrgByOrgID(Long.parseLong(id));
                        if (fullName) {
                            List<OrgEntity> orgEntityList = AAAAAdapter.getInstence().getAsboluteOrgHierarchy(Integer.parseInt(id));
                            if (orgEntityList.size() > 1) {
                                for (int a = 1; a < orgEntityList.size(); a++) {
                                    name += orgEntityList.get(a).getOrgName();
                                    if (a != orgEntityList.size() - 1) {
                                        name += "-";
                                    }
                                }
                            } else {
                                name = o.getOrgName();
                            }
                        } else {
                            name = o.getOrgName();
                        }
                    } else {
                        UserEntity u = AAAAAdapter.getInstence().findUserbyUserID(Integer.parseInt(id));
                        if (u != null) {
                            name = u.getTrueName();
                            Map m = new HashMap<String, String>();
                            m.put("cloudOrgId", u.getOrgID());
                            m.put("orgName", AAAAAdapter.getInstence().findOrgByOrgID(u.getOrgID())
                                    .getOrgName());
                            m.put("mobTel", u.getMobilePhone());
                            m.put("officeTel", u.getTelephone());
//                            m.put("faxTel", u.getNoteInfo() != null ? u.getNoteInfo().getFaxTel() : null);
                            m.put("emailAddress", u.getAddress());
                            mapPerOrg.put(id, m);
                        } else {
                            name = null;
                        }
                    }
                    //   }
                    if (name == null) {
                        name = id;
                    }
                } catch (NumberFormatException e) {
                    name = id;
                }
                names += name + ',';
            }
            names = names.substring(0, names.length() - 1);
        /*
         * if (isOrg) endHandle(request, response, names, "获取部门或人员名称出错：" +
	     * fieldId, true); else { mapPerOrg.put("name", names);
	     * mapPerOrg.put("valueId",valueId); endHandle(request, response,
	     * JSONObject.fromObject(mapPerOrg) .toString(), "获取部门或人员名称出错：" +
	     * fieldId, true); }
	     */
            mapPerOrg.put("name", names);
            mapPerOrg.put("valueId", valueId);
            endHandle(request, response, JSON.toJSONString(mapPerOrg), "获取部门或人员名称出错：" + fieldId, true);
        } catch (PaasAAAAException e) {
//            throw new UIException("获取部门或人员名称出错：" + fieldId, "获取部门或人员名称出错", fieldId + "--" + fieldType);
        }

    }

    /**
     * 判断下一步是否是审核
     *
     * @param request
     * @param response
     * @param taskInstance 待办信息，如果是新建工单，需提供流程定义名称
     * @throws com.metarnet.core.common.exception.UIException
     */
    @RequestMapping(value = "/commTreeController.do", params = "method=judgeNextActivityIsApproval")
    @ResponseBody
    public void JudgeNextActivityIsApproval(HttpServletRequest request, HttpServletResponse response,
                                            TaskInstance taskInstance) throws UIException {
        try {
            // Boolean result =
            // workflowBaseService.judgeNextActivityIsApproval(taskInstance,
            // getAccountId(request));
            // 暂时不做校验，默认为true
            Boolean result = true;
            endHandle(request, response, result.toString(), "judgeNextActivityIsApproval", false/* taskInstance.getActivityInstID() */);
        } catch (Exception e) {
//            throw new UIException("", "", "判断下一步是否是审核出错: activityInstID=" + taskInstance.getActivityInstID());
        }
    }

    /**
     * 根据流程环节id获取有权限操作该环节的人员列表
     *
     * @param request
     * @param response
     * @param taskInstance 待办
     * @throws com.metarnet.core.common.exception.UIException
     */
    @RequestMapping(value = "/commTreeController.do", params = "method=getApproverList")
    @ResponseBody
    public void getApproverList(HttpServletRequest request, HttpServletResponse response, TaskInstance taskInstance,
                                String productcode, String areacode, String majorcode, String orgcode, String roleclass) throws UIException {
        try {
            LinkedHashMap parameters = new LinkedHashMap();
            if (StringUtils.isNotEmpty(productcode)) {
                parameters.put(Constants.DIMENSION_PRODUCT_CODE, productcode);
            }
            if (StringUtils.isNotEmpty(majorcode)) {
                parameters.put(Constants.DIMENSION_MAJOR_CODE, majorcode);
            }
            if (StringUtils.isNotEmpty(orgcode)) {
                parameters.put(Constants.DIMENSION_ORG_CODE, orgcode);
            }
            if (StringUtils.isNotEmpty(roleclass)) {
                parameters.put(Constants.DIMENSION_ROLE_CLASS, roleclass);
            }
        /*
         * parameters.put(NextApproverModel.Parameter.areacode.name(),
	     * StringUtils.isNotEmpty(areacode) ? areacode :
	     * getUserEntity(request).getCloudOrgId());
	     */
            /*List l = workflowBaseService.getApproverList(taskInstance, getAccountId(request), parameters,
                    getUserEntity(request));*/
            List participantList = new ArrayList();
            List<UserEntity> userEntityList = AAAAAdapter.findUserListByOrgID(getUserEntity(request).getOrgID().intValue());
            for (UserEntity u : userEntityList) {
                Map map = new HashMap();
                map.put("cloudAccountId", u.getUserName());
                map.put("displayName", u.getTrueName());
                participantList.add(map);
            }
            endHandle(request, response, JSON.toJSONString(participantList), "getApproverList");
        } catch (Exception e) {
//            throw new UIException("", "", "获取下一步可能的参与者出错: activityInstID=" + taskInstance.getActivityInstID());
        }
    }

    /**
     * 催办人员树
     *
     * @param dispatchModel 派发树数据规则
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/commTreeController.do", params = "method=createRemindUserTree")
    @ResponseBody
    public void createRemindUserTree(DispatchModel dispatchModel, HttpServletRequest request,
                                     HttpServletResponse response, String processInstID) throws UIException {
        List<TreeNode> trees = new ArrayList<TreeNode>();
        try {
            List<Participant> participants = WorkflowAdapter.findDoingParticipant(processInstID, getAccountId(request));
            if (participants != null && participants.size() != 0) {
                for (Participant participant : participants) {
                    UserEntity userEntity = AAAAAdapter.getInstence().findUserByPortalAccountId(
                            participant.getParticipantID());
                    TreeNode chiTreeNode = new TreeNode();
                    chiTreeNode.setId(userEntity.getUserId().toString());
                    chiTreeNode.setText(userEntity.getTrueName());
                    trees.add(chiTreeNode);
                }
                endHandleTree(request, response, trees, "查询部门人员树失败  orgId=" + dispatchModel.getCloudOrgId(), false);
            } else {
                JSONObject json = new JSONObject();
                json.put("success", true);
                endHandle(request, response, json, "催办列表为空");
            }
        } catch (PaasAAAAException e) {
//            throw new UIException("获取催办列表错误  orgId=" + dispatchModel.getCloudOrgId(), "获取催办列表错误", dispatchModel
//                    .getCloudOrgId()
//                    + "");
        } catch (Exception e) {
//            throw new UIException("获取催办列表错误  orgId=" + dispatchModel.getCloudOrgId(), "获取催办列表错误", dispatchModel
//                    .getCloudOrgId()
//                    + "");
        }
    }

    @RequestMapping(value = "/commTreeController.do", params = "method=doSearch")
    @ResponseBody
    public void doSearch(HttpServletResponse response, HttpServletRequest request, int searchDataType,
                         String searchScope, String searchText) throws UIException {
        // 1为组织 2为人员
        Map searchResult = new HashMap();
        int success = 0;
        Map dfData = new HashMap();
        String message = "查询失败";
        List dataList = new ArrayList();
        int dataCount = 0;
        searchScope = searchScope.split(":")[0];
        try {
            if (searchDataType == 1) {
                List<OrgEntity> orgList = AAAAAdapter.getInstence().findOrgsByOrgIdAndName(
                        Long.parseLong(searchScope), searchText);
                for (OrgEntity orgEntity : orgList) {
                    dataList.add(createSearchNode(null, "", orgEntity.getOrgId().intValue(), orgEntity.getOrgName()));
                }
            } else {
                List<Object[]> userList = AAAAAdapter.getInstence().findUserEntityByCloudOrgIdAndEmpName(
                        Long.parseLong(searchScope), searchText);
                for (Object[] oarray : userList) {
                    UserEntity u = (UserEntity) oarray[0];
                    String orgName = (String) oarray[1];
                    dataList.add(createSearchNode(u.getUserId().intValue(), u.getTrueName(), u.getOrgID().intValue(), orgName));
                }

            }
            success = 1;
            dataCount = dataList.size();
            message = "查询成功";
        } catch (Exception e) {
//            throw new UIException("模糊查询部门/人员出错", getUserId(request), e);
        } finally {
            searchResult.put("success", success);
            dfData.put("dataList", dataList);
            dfData.put("dataCount", dataCount);
            searchResult.put("dfData", dfData);
            searchResult.put("message", message);
            endHandle(request, response, JSON.toJSONString(searchResult), "模糊查询部门/人员searchScope:" + searchScope
                    + ";searchText" + searchText);
        }
    }

    /**
     * 模糊搜索组织创建节点
     *
     * @param trees
     * @param oList
     * @param i
     * @param userId
     * @throws com.metarnet.core.common.exception.UIException
     */
    public void getORGTreeNode(List<TreeNode> trees, List<OrgEntity> oList, int i, int userId, boolean returnNodeType) throws UIException {
        try {
            if (i == oList.size() - 1) {
                return;
            }
            List<OrgEntity> subOrgList = AAAAAdapter.getInstence().findOrgListByParentID(oList.get(i).getOrgId().intValue());
            for (OrgEntity o : subOrgList) {
                TreeNode t = createOrgTreeNode(o, false, false, returnNodeType);
                if (o.getOrgId().equals(oList.get(i + 1).getOrgId())) {

                    if (i == oList.size() - 2) {
                        t.setIsSelected("1");
                    } else {
                        LinkedList<TreeNode> ts = new LinkedList<TreeNode>();
                        getORGTreeNode(ts, oList, i + 1, userId, returnNodeType);
                        t.setTreeNode(ts);
                        t.setHasChild("1");
                        t.setDefaultOpen("1");
                    }
                }
                trees.add(t);
            }

        } catch (Exception e) {
//            throw new UIException("组装节点失败", userId, e);
        }
    }

    /**
     * 模糊搜索人员创建节点
     *
     * @param trees
     * @param oList
     * @param i
     * @param userId
     * @param selectUserId
     * @throws com.metarnet.core.common.exception.UIException
     */
    public void getUserTreeNode(List<TreeNode> trees, List<OrgEntity> oList, int i, int userId, Integer selectUserId, boolean returnNodeType)
            throws UIException {
        try {
            if (i >= oList.size()) {
                return;
            }
            Integer orgId = oList.get(i).getOrgId().intValue();
            List<OrgEntity> subOrgList = AAAAAdapter.getInstence().findOrgListByParentID(orgId);
            if (subOrgList != null) {
                for (OrgEntity o : subOrgList) {
                    if (o != null) {
                        TreeNode t = createUserTreeNode(o, returnNodeType);
                        if (i + 1 < oList.size() && o.getOrgId().equals(oList.get(i + 1).getOrgId())) {
                            LinkedList<TreeNode> ts = new LinkedList<TreeNode>();
                            getUserTreeNode(ts, oList, i + 1, userId, selectUserId, returnNodeType);
                            t.setTreeNode(ts);
                            t.setHasChild("1");
                            t.setDefaultOpen("1");
                        }
                        trees.add(t);
                    }
                }
            }
            List<UserEntity> userEntityList = AAAAAdapter.getInstence().findUserListByOrgID(orgId);
            if (userEntityList != null) {
                for (UserEntity userEntity : userEntityList) {
                    TreeNode chiTreeNode = new TreeNode();
                    String nodeId = userEntity.getUserId().toString();
                    if (returnNodeType) {
                        nodeId += ":" + Constants.DISPATCH_OBJECT_TYPE_MEMBER;
                    }
                    chiTreeNode.setId(nodeId);
                    chiTreeNode.setText(userEntity.getTrueName());
                    if (userEntity.getUserId().equals(selectUserId)) {
                        chiTreeNode.setIsSelected("1");
                    }
                    Map m = new HashMap<String, String>();
                    m.put("cloudOrgId", userEntity.getOrgID());
                    m.put("orgName", oList.get(i).getOrgName());
                    m.put("mobTel", userEntity.getMobilePhone());
                    m.put("officeTel", userEntity.getTelephone());
//                    m.put("faxTel", userEntity.getNoteInfo() != null ? userEntity.getNoteInfo().getFaxTel() : null);
                    m.put("emailAddress", userEntity.getAddress());
                    chiTreeNode.setReturnValue(JSON.toJSONString(m));
                    chiTreeNode.setHasChild("0");
                    trees.add(chiTreeNode);
                }
            }

        } catch (Exception e) {
//            throw new UIException("组装节点失败", userId, e);
        }
    }

    @RequestMapping(value = "/commTreeController.do", params = "method=dfDeeptreeSearch")
    @ResponseBody
    public void dfDeeptreeSearch(HttpServletResponse response, HttpServletRequest request, int cloudOrgId,
                                 int fuzzySearchDataType, String searchScope) throws UIException {
        List<TreeNode> trees = new ArrayList<TreeNode>();
        try {
            List<OrgEntity> orgList = AAAAAdapter.getInstence().getAsboluteOrgHierarchy(cloudOrgId);
            boolean returnNodeType = Boolean.parseBoolean(request.getParameter("returnNodeType"));
            List<OrgEntity> subResult = null;
            int scope = Integer.valueOf(searchScope.split(":")[0]);
            for (int i = 0; i < orgList.size(); i++) {
                if (orgList.get(i).getOrgId() == scope) {
                    subResult = orgList.subList(i, orgList.size());
                    break;
                }
            }
            // 1为组织 2为人员
            if (fuzzySearchDataType == 1) {
                getORGTreeNode(trees, subResult, 0, getUserId(request), returnNodeType);
            } else {
                getUserTreeNode(trees, subResult, 0, getUserId(request), Integer.parseInt(request
                        .getParameter("cloudUserId")), returnNodeType);
            }
            JSONObject jsonObject = new JSONObject();
//            JSONArray treeNodes = JSONArray.parseArray(trees);
//            super.removeEmptyTreeNodes(jsonObject, treeNodes);
//            endHandle(request, response, DFXmlJsonConverter.dfJson2XmlFormat(jsonObject), "", true);
        } catch (Exception e) {
//            throw new UIException("模糊查询部门/人员出错", getUserId(request), e);
        }

    }

    public Map createSearchNode(Integer cloudUserId, String empName, Integer cloudOrgId, String orgName) {
        Map nodeMap = new HashMap();
        nodeMap.put("cloudUserId", cloudUserId);
        nodeMap.put("empName", empName);
        nodeMap.put("cloudOrgId", cloudOrgId);
        nodeMap.put("orgName", orgName);
        return nodeMap;
    }

    private void removeNoAuthority(List<UserEntity> userEntityList, List<OrgEntity> orgEntityList, String specialty, String orgID, String process, String node) {
        if (StringUtils.isBlank(specialty) || StringUtils.isBlank(process) || StringUtils.isBlank(node))
            return;
        String participants = WorkflowAdapter.findNextParticipant(specialty, orgID, process, node, true);
        JSONArray json = JSON.parseArray(participants);
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < json.size(); i++) {
            list.add(json.getJSONObject(i).getString("participantID"));
        }
        Iterator<UserEntity> userEntityIterator = userEntityList.iterator();
        while (userEntityIterator.hasNext()) {
            UserEntity userEntity = userEntityIterator.next();
            if (!list.contains(userEntity.getUserName()))
                userEntityIterator.remove();
        }

    }


    /**
     * 派发树
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/commTreeController.do", params = "method=createDisPatchTree3")
    @ResponseBody
    public void createDisPatchTree3(HttpServletRequest request, HttpServletResponse response, int type) throws UIException, PaasAAAAException, AdapterException {
        List<TreeNodeVo> list2 = new ArrayList<TreeNodeVo>();
        UserEntity user = getUserEntity(request);
        List<OrgEntity> orgList;
//        OrgEntity thisOrgEntity = AAAAAdapter.getInstence().findOrgByOrgID(user.getOrgID());
        OrgEntity thisOrgEntity = user.getOrgEntity();
        String topOrgId = request.getParameter("rootOrgId");
        if (topOrgId != null && !"".equals(topOrgId)) {
            thisOrgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(topOrgId);
        }
        String rootAsCompany = request.getParameter("rootAsCompany");
        if (!"1".equals(rootAsCompany)) {
//            thisOrgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(thisOrgEntity.getProCode());
//            thisOrgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(AAAAAdapter.getCompany(thisOrgEntity.getOrgId().intValue()).getOrgCode());
            thisOrgEntity = AAAAAdapter.getCompany(thisOrgEntity.getOrgId().intValue());
        }


        //点击节点展开时，传回的被展开节点ID
        String orgId2 = request.getParameter("id");

        OrgEntity clickOrgEntity = null;
        if (type == 1) {

            if (orgId2 != null && !"".equals(orgId2)) {
                clickOrgEntity = AAAAAdapter.getCompany(Integer.valueOf(orgId2));
                orgList = AAAAAdapter.getInstence().findOrgListByParentID(Integer.parseInt(orgId2));
            } else {
                if ("UNI".equals(thisOrgEntity.getOrgType())) {
                    if (!"1".equals(rootAsCompany)) {
                        orgList = AAAAAdapter.getInstence().findOrgListByParentID(1);
                        TreeNodeVo uniNode = new TreeNodeVo();
                        uniNode.setNodeId("UNI");
                        uniNode.setId("UNI");
                        uniNode.setParentId("0");
                        uniNode.setLabel("集团");
                        uniNode.setFullName("集团");
                        uniNode.setOpen(false);
                        uniNode.setType(1);
                        list2.add(uniNode);
                        TreeNodeVo poiNode = new TreeNodeVo();
                        poiNode.setNodeId("PRO");
                        poiNode.setId("PRO");
                        poiNode.setParentId("0");
                        poiNode.setLabel("省分公司");
                        poiNode.setFullName("省分公司");
                        poiNode.setOpen(false);
                        poiNode.setType(1);
                        list2.add(poiNode);
                        TreeNodeVo popNode = new TreeNodeVo();
                        popNode.setNodeId("POP");
                        popNode.setId("POP");
                        popNode.setParentId("0");
                        popNode.setLabel("其他");
                        popNode.setFullName("其他");
                        popNode.setType(1);
                        popNode.setOpen(false);
                        list2.add(popNode);
                    } else {
                        clickOrgEntity = AAAAAdapter.getCompany(thisOrgEntity.getOrgId().intValue());
                        clickOrgEntity.setOrgType("DEP");
                        orgList = AAAAAdapter.getInstence().findOrgListByParentID(thisOrgEntity.getOrgId());
                    }
                } else {
                    TreeNodeVo pNode = new TreeNodeVo();
                    pNode.setNodeId("org-" + thisOrgEntity.getOrgId());
                    pNode.setId(thisOrgEntity.getOrgId().toString());
                    pNode.setParentId("org-" + thisOrgEntity.getParentOrgId().toString());
                    pNode.setLabel(thisOrgEntity.getOrgName());
                    pNode.setFullName(thisOrgEntity.getFullOrgName());
                    pNode.setOpen(false);
                    pNode.setType(1);
                    list2.add(pNode);
//                List<UserEntity> userEntityList1 = AAAAAdapter.getInstence().findUserListByOrgID(thisOrgEntity.getOrgId());
                    orgList = AAAAAdapter.getInstence().findOrgListByParentID(thisOrgEntity.getOrgId());
                }
            }
//            List<TreeNode> trees = new ArrayList<TreeNode>();

            for (int i = 0; i < orgList.size(); i++) {
                TreeNodeVo treeNode = new TreeNodeVo();
                if (orgList.get(i).getOrgType().equals("POP")) {
                    treeNode.setParentId("POP");
                } else if (orgList.get(i).getOrgType().equals("UNI")) {
                    treeNode.setParentId("UNI");
//                    List<OrgEntity> uorList=AAAAAdapter.findOrgRecursionListByOrgID(orgList.get(i).getOrgId().intValue());
                    List<OrgEntity> uorList = AAAAAdapter.findOrgListByParentID(orgList.get(i).getOrgId().intValue());
//                    List<UserEntity> userEntityList = AAAAAdapter.getInstence().findUserListByOrgID(orgList.get(i).getOrgId());
                    for (OrgEntity org : uorList) {
                        TreeNodeVo oNode = new TreeNodeVo();
                        oNode.setIsParent(true);
                        oNode.setNodeId("org-" + org.getOrgId());
                        oNode.setId(org.getOrgId().toString());
                        oNode.setLabel(org.getOrgName());
                        oNode.setFullName(org.getFullOrgName());
                        oNode.setType(1);
                        oNode.setParentId("org-" + org.getParentOrgId().toString());
                        oNode.setOpen(true);
                        list2.add(oNode);

                    }
                } else if (orgList.get(i).getOrgType().equals("PRO")) {
                    treeNode.setParentId("PRO");
                } else {
                    treeNode.setParentId("org-" + orgList.get(i).getParentOrgId().toString());
                }
                treeNode.setNodeId("org-" + orgList.get(i).getOrgId());
                treeNode.setId(orgList.get(i).getOrgId().toString());
                treeNode.setLabel(orgList.get(i).getOrgName());
                treeNode.setFullName(orgList.get(i).getFullOrgName());
                treeNode.setType(1);
//                if (clickOrgEntity != null && "DEP".equals(clickOrgEntity.getOrgType())) {
                if (clickOrgEntity != null && "DEP".equals(orgList.get(i).getOrgType())) {
                    treeNode.setIsParent(true);
                } else if("BB".equals(orgList.get(i).getShortName())){
                    treeNode.setIsParent(true);
                    treeNode.setParentId("org-" + orgList.get(i).getParentOrgId().toString());
                } else if("CITY".equals(orgList.get(i).getOrgType())){
                    continue;
                }else {
                    treeNode.setIsParent(false);
                }
                treeNode.setOpen(false);
                list2.add(treeNode);
            }

            endHandle(request, response, JSON.toJSONString(list2, true).toString(), "", false);

        } else if(type == 4 || type == 5){
            if(orgId2 != null && !"".equals(orgId2)) {
                List<UserEntity> userEntityList = AAAAAdapter.getInstence().findUserListByOrgID(Integer.parseInt(orgId2));
                if (userEntityList != null) {
                    for (UserEntity userEntity : userEntityList) {
                        TreeNodeVo userNode = new TreeNodeVo();
                        userNode.setNodeId("person-" + userEntity.getUserId());
                        userNode.setId(userEntity.getUserId().toString());
                        userNode.setUserName(userEntity.getUserName());
                        userNode.setParentId("org-" + orgId2);
                        userNode.setLabel(userEntity.getTrueName());
                        userNode.setType(2);
                        userNode.setOpen(false);
                        userNode.setMobilePhone(userEntity.getMobilePhone());
                        list2.add(userNode);
                    }
                }
            } else {
                orgList = AAAAAdapter.getInstence().findProfessionalOfficeByOrgID(user.getOrgID());
                for(int i  = 0 ; i < orgList.size() ; i++){
                    TreeNodeVo oNode = new TreeNodeVo();
                    oNode.setIsParent(true);
                    oNode.setNodeId("org-" + orgList.get(i).getOrgId());
                    oNode.setId(orgList.get(i).getOrgId().toString());
                    oNode.setLabel(orgList.get(i).getOrgName());
                    oNode.setType(1);
                    oNode.setParentId("org-" + orgList.get(i).getParentOrgId().toString());
                    if(type == 4){
                        oNode.setIsParent(false);
                        oNode.setOpen(true);
                    } else {
                        oNode.setIsParent(true);
                    }
                    list2.add(oNode);
                }
            }

            endHandle(request, response, JSON.toJSONString(list2, true).toString(), "", false);
        }  else {
            int cloudOrgId = user.getOrgID().intValue();
//            AAAAAdapter.CompanyType ctype = AAAAAdapter.getCompanyType(cloudOrgId);
            List<TreeNodeVo> trees = new ArrayList();
            Long orgId = user.getOrgID();

            if (orgId2 != null) {
//                orgList = AAAAAdapter.findOrgRecursionListByOrgID(Integer.parseInt(orgId2));
                orgList = AAAAAdapter.findOrgListByParentID(Integer.parseInt(orgId2));
                /*com.metarnet.common.model.TreeNode treeNodePv = new com.metarnet.common.model.TreeNode();
                treeNodePv.setId(thisOrgEntity.getOrgId().toString());
                treeNodePv.setParentId(thisOrgEntity.getParentOrgId().toString());
                treeNodePv.setLabel(thisOrgEntity.getOrgName());
                treeNodePv.setOpen(false);
                trees.add(treeNodePv);*/
                List<UserEntity> userEntityList = AAAAAdapter.getInstence().findUserListByOrgID(Integer.parseInt(orgId2));

                //移除无权限的数据
                removeNoAuthority(userEntityList, orgList, request.getParameter("specialty"), orgId2, request.getParameter("process"), request.getParameter("node"));

                if (userEntityList != null) {

                    for (UserEntity userEntity : userEntityList) {
                        TreeNodeVo userNode = new TreeNodeVo();
                        userNode.setNodeId("person-" + userEntity.getUserId());
                        userNode.setId(userEntity.getUserId().toString());
                        userNode.setUserName(userEntity.getUserName());
                        userNode.setParentId("org-" + userEntity.getOrgID().toString());
                        userNode.setLabel(userEntity.getTrueName());
                        userNode.setType(2);
                        userNode.setOpen(false);
                        userNode.setMobilePhone(userEntity.getMobilePhone());
                        trees.add(userNode);

                    }
                }
                if (orgList != null) {
                    for (OrgEntity org : orgList) {
                        TreeNodeVo orgNode = new TreeNodeVo();
                        orgNode.setParentId("org-" + org.getParentOrgId().toString());
                        orgNode.setNodeId("org-" + org.getOrgId());
                        orgNode.setId(org.getOrgId().toString());
                        orgNode.setIsParent(true);
                        orgNode.setLabel(org.getOrgName());
                        orgNode.setType(1);
                        orgNode.setOpen(false);
                        orgNode.setNocheck(true);
                        trees.add(orgNode);
                    }
                }
            } else {
                if (orgId != null) {
                    if ("UNI".equals(thisOrgEntity.getOrgType())) {
//                        orgList = AAAAAdapter.findOrgRecursionListByOrgID(1);
                        orgList = AAAAAdapter.findOrgListByParentID(1);
                        TreeNodeVo uniNode = new TreeNodeVo();
                        uniNode.setNodeId("UNI");
                        uniNode.setId("UNI");
                        uniNode.setParentId("0");
                        uniNode.setLabel("集团");
                        uniNode.setType(1);
                        uniNode.setOpen(false);
                        uniNode.setNocheck(true);
                        trees.add(uniNode);
                        TreeNodeVo poiNode = new TreeNodeVo();
                        poiNode.setNodeId("PRO");
                        poiNode.setId("PRO");
                        poiNode.setParentId("0");
                        poiNode.setLabel("省分公司");
                        poiNode.setType(1);
                        poiNode.setOpen(false);
                        poiNode.setNocheck(true);
                        trees.add(poiNode);
                        TreeNodeVo popNode = new TreeNodeVo();
                        popNode.setNodeId("POP");
                        popNode.setId("POP");
                        popNode.setParentId("0");
                        popNode.setLabel("其他");
                        popNode.setType(1);
                        popNode.setOpen(false);
                        popNode.setNocheck(true);
                        trees.add(popNode);
                    } else {
//                        orgList = AAAAAdapter.findOrgRecursionListByOrgID(orgId.intValue());
                        orgList = AAAAAdapter.findOrgListByParentID(thisOrgEntity.getOrgId().intValue());
                        TreeNodeVo treeNodePv = new TreeNodeVo();
                        treeNodePv.setNodeId("org-" + thisOrgEntity.getOrgId());
                        treeNodePv.setId(thisOrgEntity.getOrgId().toString());
                        treeNodePv.setParentId("org-" + thisOrgEntity.getParentOrgId().toString());
                        treeNodePv.setLabel(thisOrgEntity.getOrgName());
                        treeNodePv.setOpen(false);
                        treeNodePv.setNocheck(true);
                        trees.add(treeNodePv);
                        List<UserEntity> userEntityList = AAAAAdapter.getInstence().findUserListByOrgID(thisOrgEntity.getOrgId());

                        //移除无权限的数据
                        removeNoAuthority(userEntityList, orgList, request.getParameter("specialty"), thisOrgEntity.getOrgId().toString(), request.getParameter("process"), request.getParameter("node"));

                        if (userEntityList != null) {

                            for (UserEntity userEntity : userEntityList) {
                                TreeNodeVo userNode = new TreeNodeVo();
                                userNode.setNodeId("person-" + userEntity.getUserId());
                                userNode.setId(userEntity.getUserId().toString());
                                userNode.setUserName(userEntity.getUserName());
                                userNode.setParentId("org-" + userEntity.getOrgID().toString());
                                userNode.setLabel(userEntity.getTrueName());
                                userNode.setType(2);
                                userNode.setMobilePhone(userEntity.getMobilePhone());
                                userNode.setOpen(false);
                                trees.add(userNode);

                            }
                        }
                    }

                    for (OrgEntity orgEntity : orgList) {
                        if (orgEntity != null) {
                            TreeNodeVo treeNode = new TreeNodeVo();
                            if ("UNI".equals(thisOrgEntity.getOrgType())) {
                                if (orgEntity.getOrgType().equals("UNI")) {
                                    treeNode.setParentId("UNI");
                                    treeNode.setNodeId("org-" + orgEntity.getOrgId());
                                    treeNode.setId(orgEntity.getOrgId().toString());
                                    treeNode.setIsParent(true);
                                    treeNode.setLabel(orgEntity.getOrgName());
                                    treeNode.setType(1);
                                    treeNode.setOpen(false);
                                    treeNode.setNocheck(true);
                                    trees.add(treeNode);
                                } else if (orgEntity.getOrgType().equals("POP")) {
                                    treeNode.setParentId("POP");
                                    treeNode.setNodeId("org-" + orgEntity.getOrgId());
                                    treeNode.setId(orgEntity.getOrgId().toString());
                                    treeNode.setIsParent(true);
                                    treeNode.setLabel(orgEntity.getOrgName());
                                    treeNode.setType(1);
                                    treeNode.setOpen(false);
                                    treeNode.setNocheck(true);
                                    trees.add(treeNode);
                                } else if (orgEntity.getOrgType().equals("PRO")) {
                                    treeNode.setParentId("PRO");
                                    treeNode.setNodeId("org-" + orgEntity.getOrgId());
                                    treeNode.setId(orgEntity.getOrgId().toString());
                                    treeNode.setIsParent(true);
                                    treeNode.setLabel(orgEntity.getOrgName());
                                    treeNode.setType(1);
                                    treeNode.setOpen(false);
                                    treeNode.setNocheck(true);
                                    trees.add(treeNode);
                                } else {
                                    //treeNode.setParentId(orgEntity.getParentOrgId().toString());
                                }
                            } else {
                                treeNode.setParentId("org-" + orgEntity.getParentOrgId().toString());
                                treeNode.setNodeId("org-" + orgEntity.getOrgId());
                                treeNode.setId(orgEntity.getOrgId().toString());
                                treeNode.setIsParent(true);
                                treeNode.setLabel(orgEntity.getOrgName());
                                treeNode.setType(1);
                                treeNode.setOpen(false);
                                treeNode.setNocheck(true);
                                trees.add(treeNode);
                            }
                        }

                    }


                }

            }

            endHandle(request, response, JSON.toJSONString(trees, true).toString(), "", false);
        }
    }

}
