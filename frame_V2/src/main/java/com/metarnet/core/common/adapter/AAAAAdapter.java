package com.metarnet.core.common.adapter;

import com.alibaba.fastjson.JSON;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.model.OrgShortNameEntity;
import com.metarnet.core.common.model.OrgShortNameStructure;
import com.metarnet.core.common.model.OrgStructure;
import com.metarnet.core.common.model.ProcessDefInfo;
import com.metarnet.core.common.utils.Constants;
import com.metarnet.core.common.utils.HttpClientUtil;
import com.metarnet.core.common.utils.SpringContextUtils;
import com.ucloud.paas.proxy.aaaa.AAAAService;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import com.unicom.ucloud.workflow.objects.ActivityDef;
import com.unicom.ucloud.workflow.objects.Participant;
import com.unicom.ucloud.workflow.objects.TaskInstance;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: jtwu
 * Date: 12-11-28
 * Time: 下午4:47
 * 4A适配器，负责封装Paas平台提供的4A服务。
 */
public class AAAAAdapter extends AAAAService {
    private static final AAAAAdapter _AAAAdapter = new AAAAAdapter();
    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(AAAAAdapter.class);

    private AAAAAdapter() {
    }

    public static AAAAAdapter getInstence() {
        return _AAAAdapter;
    }

    /**
     * 根据用户返回营销区域ID
     *
     * @param userEntity 用户信息
     * @return
     */
    public static Integer getMarketingAreaId(UserEntity userEntity) {
        return 0;
    }

    /**
     * 根据用户返回维护区域ID
     *
     * @param userEntity 用户信息
     * @return
     */
    public static Integer getMaintenanceAreaId(UserEntity userEntity) {
        return 0;
    }

    /**
     * 根据组织编号返回上一级分公司,集团分公司没有父分公司，返回null
     * 比如济南分公司对应山东分公司、济南分公司运维部对应山东分公司。
     *
     * @param cloudOrgId 组织ID
     * @return
     */
    public static OrgEntity getParentCompany(Integer cloudOrgId) throws PaasAAAAException {
        OrgStructure orgStructure = findCompanysByOrgId(cloudOrgId);
        if (null != orgStructure.getCountyCompany()) {
            return orgStructure.getCityCompany();
        }
        if (null != orgStructure.getCityCompany()) {
            return orgStructure.getProvinceCompany();
        }
        if (null != orgStructure.getProvinceCompany()) {
            List<OrgEntity> orgEntityList = _AAAAdapter.findOrgListByParentID(Long.valueOf(Constants.GROUP_COMPANY_ORG_ID));
            OrgEntity orgEntity = null;
            for (OrgEntity org : orgEntityList) {
                if (org.getOrgType().equals("UNI")) {
                    orgEntity = org;
                    break;
                }
            }
            return orgEntity;
        }
        return null;
    }

    /**
     * 根据组织ID返回分公司OrgEntity
     *
     * @param cloudOrgId 组织ID
     * @return
     */
    public static OrgEntity getCompany(Integer cloudOrgId) throws PaasAAAAException {
        OrgStructure orgStructure = findCompanysByOrgId(cloudOrgId);
        if (null != orgStructure.getCountyCompany()) {
            return orgStructure.getCountyCompany();
        }
        if (null != orgStructure.getCityCompany()) {
            return orgStructure.getCityCompany();
        }
        if (null != orgStructure.getProvinceCompany()) {
            return orgStructure.getProvinceCompany();
        }
        if (null != orgStructure.getGroupCompany()) {
            return orgStructure.getGroupCompany();
        }
        /*if (null != orgStructure.getGroupCompany()) {
            return aaaaAdapter.aaaaService.findOrgByOrgID(Constants.GROUP_COMPANY_ORG_ID);
        }*/
        return null;
    }

    /**
     * 根据组织编码获得所在分公司部门简称
     *
     * @param cloudOrgId 组织编码
     * @return
     * @throws com.ucloud.paas.proxy.aaaa.util.PaasAAAAException
     */
    public static OrgShortNameEntity getOrgShortNameEntity(Integer cloudOrgId) throws PaasAAAAException {
        OrgShortNameStructure orgShortNameStructure = findCompanysBrevityByOrgId(cloudOrgId);
        if (null != orgShortNameStructure.getCountyCompany()) {
            return orgShortNameStructure.getCountyCompany();
        }
        if (null != orgShortNameStructure.getCityCompany()) {
            return orgShortNameStructure.getCityCompany();
        }
        if (null != orgShortNameStructure.getProvinceCompany()) {
            return orgShortNameStructure.getProvinceCompany();
        }
        return orgShortNameStructure.getGroupCompany();
    }


    /**
     * 根据组织编号返回上一级分公司,集团分公司没有父分公司，返回null
     * 比如济南分公司对应山东分公司、济南分公司运维部对应山东分公司。
     *
     * @param cloudOrgId 组织ID
     * @return
     */
    public static OrgShortNameEntity getParentOrgShortNameEntity(Integer cloudOrgId) throws PaasAAAAException {
        OrgShortNameStructure orgShortNameStructure = findCompanysBrevityByOrgId(cloudOrgId);
        if (null != orgShortNameStructure.getCountyCompany()) {
            return orgShortNameStructure.getCityCompany();
        }
        if (null != orgShortNameStructure.getCityCompany()) {
            return orgShortNameStructure.getProvinceCompany();
        }
        if (null != orgShortNameStructure.getProvinceCompany()) {
            return getOrgShortNameEntity(Constants.GROUP_COMPANY_ORG_ID);
        }
        return null;
    }

    /**
     * 获取组织所属分公司类型(总部、省分、市分、区县)
     *
     * @param cloudOrgId
     * @return
     * @throws AdapterException
     */
    public static CompanyType getCompanyType(Integer cloudOrgId) throws AdapterException {
        try {
            try {
                return CompanyType.County.getCompanyType(findCompanysByOrgId(cloudOrgId));
            } catch (PaasAAAAException e) {
                e.printStackTrace();
            }
        } catch (AdapterException e) {
            throw new AdapterException("orgStructure is null, cloudOrgId is " + cloudOrgId);
        }
        return null;
    }

    /**
     * 根据orgId递归获取下级组织
     *
     * @param orgId
     * @return
     */
    public static List<OrgEntity> findOrgRecursionListByOrgID(Integer orgId) throws PaasAAAAException {
        List list = new ArrayList();
        List<OrgEntity> childOrgList = AAAAAdapter.getInstence().findOrgListByParentID(orgId);
        if (childOrgList == null) {
            return null;
        }
        list.addAll(childOrgList);
        for (OrgEntity orgEntity : childOrgList) {
            List<OrgEntity> tempList = findOrgRecursionListByOrgID(Integer.valueOf(orgEntity.getOrgId().toString()));
            if (null != tempList) {
                list.addAll(tempList);
            }
        }
        return list;
    }

    /**
     * 根据orgId递归获取下级orgId列表
     *
     * @param orgId
     * @return
     */
    public static Integer[] findOrgIdListByParentID(Integer orgId) throws PaasAAAAException {
        List<Integer> list = new ArrayList();
        for (OrgEntity orgEntity : findOrgRecursionListByOrgID(orgId)) {
            list.add(Integer.valueOf(orgEntity.getOrgId().toString()));
        }
        return list.toArray(new Integer[]{list.size()});
    }

    //所属分公司类型
    public static enum CompanyType {
        County {//区县

            @Override
            public CompanyType getCompanyType(OrgStructure orgStructure) throws AdapterException {
                if (orgStructure.getCountyCompany() == null) {
                    return City.getCompanyType(orgStructure);
                }
                return County;
            }

            @Override
            public Integer getCompanyTypeCode() throws AdapterException {
                return 2;
            }
        }, City {//市分

            @Override
            public CompanyType getCompanyType(OrgStructure orgStructure) throws AdapterException {
                if (orgStructure.getCityCompany() == null) {
                    return Province.getCompanyType(orgStructure);
                }
                return City;
            }

            @Override
            public Integer getCompanyTypeCode() throws AdapterException {
                return 2;
            }
        }, Province {//省分

            @Override
            public CompanyType getCompanyType(OrgStructure orgStructure) throws AdapterException {
                if (orgStructure.getProvinceCompany() == null) {
                    return Group.getCompanyType(orgStructure);
                }
                return Province;
            }

            @Override
            public Integer getCompanyTypeCode() throws AdapterException {
                return 1;
            }
        }, Group {//总部

            @Override
            public CompanyType getCompanyType(OrgStructure orgStructure) throws AdapterException {
                if (orgStructure.getGroupCompany() == null) {
                    throw new AdapterException("");
                }
                return Group;
            }

            @Override
            public Integer getCompanyTypeCode() throws AdapterException {
                return 0;
            }
        };

        //获取部门所属的分公司类型
        public abstract CompanyType getCompanyType(OrgStructure orgStructure) throws AdapterException;

        public abstract Integer getCompanyTypeCode() throws AdapterException;
    }


    public static UserEntity findUserByPortalAccountId(String userName) throws PaasAAAAException {
        return _AAAAdapter.findUserByUserName(userName);
    }

    public static OrgShortNameStructure findCompanysBrevityByOrgId(int orgID) throws PaasAAAAException {
        OrgShortNameStructure orgShortNameStructure = new OrgShortNameStructure();
        OrgEntity orgEntity = _AAAAdapter.findOrgByOrgID(Long.valueOf(orgID));
        String orgCode = orgEntity.getOrgCode();
        if (orgEntity != null) {
            if ("120".equals(orgEntity.getProCode())) {//中国联通总部
                OrgShortNameEntity orgShortNameEntityP = new OrgShortNameEntity();
//                orgShortNameEntityP.setOrgShortName(_AAAAdapter.findOrgByOrgCode(orgEntity.getProCode()).getShortName());
                orgShortNameEntityP.setOrgShortName("ZGLT");
                orgShortNameStructure.setProvinceCompany(orgShortNameEntityP);
            } else {
                if (orgCode.length() >= 5) {//部门或地市
                    OrgShortNameEntity orgShortNameEntityP = new OrgShortNameEntity();
                    OrgShortNameEntity orgShortNameEntityC = new OrgShortNameEntity();
                    orgShortNameEntityP.setOrgShortName(_AAAAdapter.findOrgByOrgCode(orgEntity.getProCode()).getShortName());
                    orgShortNameEntityC.setOrgShortName(_AAAAdapter.findOrgByOrgCode(orgCode.substring(0, 5)).getShortName());
                    orgShortNameStructure.setProvinceCompany(orgShortNameEntityP);
                    orgShortNameStructure.setCityCompany(orgShortNameEntityC);
                } else {//省分
                    OrgShortNameEntity orgShortNameEntityP = new OrgShortNameEntity();
                    orgShortNameEntityP.setOrgShortName(_AAAAdapter.findOrgByOrgCode(orgEntity.getProCode()).getShortName());
                    orgShortNameStructure.setProvinceCompany(orgShortNameEntityP);
                }
            }
        }
        return orgShortNameStructure;
    }

    public static UserEntity findUserbyUserID(int cloudUserId) throws PaasAAAAException {
        return _AAAAdapter.findUserByUserID(Long.valueOf(cloudUserId));
    }

    public static OrgStructure findCompanysByOrgId(int cloudOrgId) throws PaasAAAAException {
        OrgStructure orgStructure = new OrgStructure();
        OrgEntity orgEntity = _AAAAdapter.findOrgByOrgID(Long.valueOf(cloudOrgId));
        if (orgEntity != null) {
            String orgLevel = orgEntity.getOrgType();
            if ("CITY".equals(orgLevel)) {
                OrgEntity pOrgEntity = _AAAAdapter.findOrgByOrgID(orgEntity.getParentOrgId());
                if ("120".equals(orgEntity.getProCode())) {
                    OrgEntity pOrg = _AAAAdapter.findOrgByOrgCode(orgEntity.getProCode());
                    orgStructure.setProvinceCompany(pOrg);
                } else if ("BB".equals(orgEntity.getShortName())) {
                    orgStructure.setCityCompany(pOrgEntity);
                    orgStructure.setProvinceCompany(pOrgEntity);
                } else {
                    orgStructure.setCityCompany(orgEntity);
                    orgStructure.setProvinceCompany(pOrgEntity);
                }
            } else if ("PRO".equals(orgLevel)) {
                orgStructure.setProvinceCompany(orgEntity);
            } else if ("UNI".equals(orgLevel)) {
                orgStructure.setGroupCompany(orgEntity);
            } else if ("DEP".equals(orgLevel)) {
                OrgEntity pOrg = _AAAAdapter.findOrgByOrgCode(orgEntity.getProCode());
                orgStructure.setProvinceCompany(pOrg);
                if (!"UNI".equals(pOrg.getOrgType())) {
                    OrgEntity org = _AAAAdapter.findOrgByOrgCode(orgEntity.getOrgCode().substring(0, 5));
                    if ("BB".equals(org.getShortName())) {
                        OrgEntity pG = _AAAAdapter.findOrgByOrgID(org.getParentOrgId());
                        orgStructure.setCityCompany(pG);
                    } else {
                        if (orgEntity.getOrgCode().length() > 5) {
                            orgStructure.setCityCompany(org);
                           /* if(orgEntity.getOrgCode().length()==7){
                                orgStructure.setCountyCompany(orgEntity);
                            }else{
                                orgStructure.setCountyCompany(AAAAAdapter.getInstence().findOrgByOrgCode(orgEntity.getOrgCode().substring(0,7)));
                            }*/
                        }
                    }
                }
            }
        }
        return orgStructure;
    }

    public static List<UserEntity> findUserListByOrgID(int orgID) throws PaasAAAAException {
        return _AAAAdapter.findUserListByOrgID(Long.valueOf(orgID));
    }

    public static List<OrgEntity> findOrgListByParentID(int orgID) throws PaasAAAAException {
        return _AAAAdapter.findOrgListByParentID(Long.valueOf(orgID));
    }


    public static List<OrgEntity> getAsboluteOrgHierarchy(int cloudOrgId) throws PaasAAAAException {
        List<OrgEntity> orgEntityList = new ArrayList<OrgEntity>();
        OrgEntity orgEntity = _AAAAdapter.findOrgByOrgID(Long.valueOf(cloudOrgId));
        orgEntityList.add(orgEntity);
        return orgEntityList;
    }

    public static List<OrgEntity> findOrgsByOrgIdAndName(Long orgId, String orgName) throws PaasAAAAException {
        List<OrgEntity> orgEntityList = new ArrayList<OrgEntity>();
        orgEntityList.add(_AAAAdapter.findOrgByOrgID(orgId));
        return orgEntityList;
    }

    public static List findUserEntityByCloudOrgIdAndEmpName(Long orgId, String empName) throws PaasAAAAException {
        return _AAAAdapter.findUserListByUserName(orgId, empName);
    }

    public static List<Participant> findNextParticipants(TaskInstance taskInstance, UserEntity userEntity, String specialty, String orgID) {
        List<ActivityDef> list = null;
        List<Participant> participantList = new ArrayList<Participant>();
        try {
            list = WorkflowAdapter.getNextActivitiesMaybeArrived(taskInstance.getActivityInstID(), userEntity.getUserName());
            Map<String, String> paramsMap = new HashMap<String, String>();
            if (StringUtils.isNotBlank(specialty)) {
                paramsMap.put("specialty", specialty);
            } else {
                paramsMap.put("specialty", "ALL");
            }
            try {
                paramsMap.put("orgID", AAAAAdapter.getCompany(Integer.parseInt(orgID)).getOrgId().toString());
            } catch (PaasAAAAException e) {
                e.printStackTrace();
            }
            paramsMap.put("process", taskInstance.getProcessModelName());
            paramsMap.put("flag", "true");
            if (list != null && list.size() > 0) {
                if (list.size() == 1) {
                    paramsMap.put("node", list.get(0).getActivityID());
                } else {
                    for (ActivityDef activityDef : list) {
                        if (activityDef.getActivitytype() != null && "manual".equals(activityDef.getActivitytype())) {
                            paramsMap.put("node", activityDef.getActivityID());
                            break;
                        }
                    }
                }
            }
            String result = HttpClientUtil.sendPostRequestByJava(Constants.POWERURL + "/powerController.do?method=findNextParticipant4", paramsMap);
            List participants = JSON.parseArray(result);
            for (Object object : participants) {
                Participant p = new Participant();
                p.setParticipantID(object.toString());
                p.setParticipantName("");
                p.setParticipantType("1");
                participantList.add(p);
            }
        } catch (AdapterException e) {
            e.printStackTrace();
        }
        return participantList;
    }

    public static List<Participant> findNextParticipants(String processModelName, TaskInstance taskInstance, UserEntity userEntity, String specialty, String orgID) {
        List<ActivityDef> list = null;
        List<Participant> participantList = new ArrayList<Participant>();
        try {
            list = WorkflowAdapter.getNextActivitiesMaybeArrived(taskInstance.getActivityInstID(), userEntity.getUserName());
            Map<String, String> paramsMap = new HashMap<String, String>();
            if (StringUtils.isNotBlank(specialty)) {
                paramsMap.put("specialty", specialty);
            } else {
                paramsMap.put("specialty", "ALL");
            }
            paramsMap.put("orgID", orgID);
            paramsMap.put("process", processModelName);
            paramsMap.put("flag", "true");
            if (list != null && list.size() > 0) {
                if (list.size() == 1) {
                    paramsMap.put("node", list.get(0).getActivityID());
                } else {
                    for (ActivityDef activityDef : list) {
                        if (activityDef.getActivitytype() != null && "manual".equals(activityDef.getActivitytype())) {
                            paramsMap.put("node", activityDef.getActivityID());
                            break;
                        }
                    }
                }
            }
            String result = HttpClientUtil.sendPostRequestByJava(Constants.POWERURL + "/powerController.do?method=findNextParticipant4", paramsMap);
            List participants = JSON.parseArray(result);
            for (Object object : participants) {
                Participant p = new Participant();
                p.setParticipantID(object.toString());
                p.setParticipantName("");
                p.setParticipantType("1");
                participantList.add(p);
            }
        } catch (AdapterException e) {
            e.printStackTrace();
        }
        return participantList;
    }

    public static List<Participant> findNextParticipants(TaskInstance taskInstance, String specialty, String orgID) {
        List<Participant> participantList = new ArrayList<Participant>();
        try {
            Map<String, String> paramsMap = new HashMap<String, String>();
            if (StringUtils.isNotBlank(specialty)) {
                paramsMap.put("specialty", specialty);
            } else {
                paramsMap.put("specialty", "ALL");
            }
            paramsMap.put("orgID", AAAAAdapter.getCompany(Integer.parseInt(orgID)).getOrgId().toString());
            paramsMap.put("process", taskInstance.getProcessModelName());
            paramsMap.put("node", taskInstance.getActivityDefID());
            paramsMap.put("flag", "true");
            logger.info("查询人员参数：" + paramsMap);
            String result = HttpClientUtil.sendPostRequestByJava(Constants.POWERURL + "/powerController.do?method=findNextParticipant4", paramsMap);
            logger.info("人员查询结果result:" + result);
            List participants = JSON.parseArray(result);
            for (Object object : participants) {
                Participant p = new Participant();
                p.setParticipantID(object.toString());
                p.setParticipantName("");
                p.setParticipantType("1");
                participantList.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return participantList;
    }

    public static List<Participant> findNextParticipants(String processModelName, String nodeCode, String specialty, String orgID) {
        List<Participant> participantList = new ArrayList<Participant>();
        try {
            Map<String, String> paramsMap = new HashMap<String, String>();
            if (StringUtils.isNotBlank(specialty)) {
                paramsMap.put("specialty", specialty);
            } else {
                paramsMap.put("specialty", "ALL");
            }
            paramsMap.put("orgID", orgID);
            paramsMap.put("process", processModelName);
            paramsMap.put("node", nodeCode);
            logger.info("查询人员参数：" + paramsMap);
            String result = HttpClientUtil.sendPostRequestByJava(Constants.POWERURL + "/powerController.do?method=findNextParticipant4", paramsMap);
            logger.info("人员查询结果result:" + result);
            List participants = JSON.parseArray(result);
            for (Object object : participants) {
                Participant p = new Participant();
                p.setParticipantID(object.toString());
                p.setParticipantName("");
                p.setParticipantType("1");
                participantList.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return participantList;
    }

    public static ProcessDefInfo getProcessDefName(String businessCode, String type) {
        try {
            IBaseDAO iBaseDAO = (IBaseDAO) SpringContextUtils.getBean("baseDAO");
            List<ProcessDefInfo> processDefInfoList = iBaseDAO.find("from ProcessDefInfo p where p.businessCode=? and p.type=?", new Object[]{businessCode, type});
            if (processDefInfoList != null && processDefInfoList.size() > 0) {
                return processDefInfoList.get(0);
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
