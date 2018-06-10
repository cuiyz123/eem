package com.metarnet.eomeem.service.impl;

import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.GeneralInfoModel;
import com.metarnet.core.common.model.Pager;
import com.metarnet.core.common.utils.Constants;
import com.metarnet.core.common.utils.PubFun;
import com.metarnet.eomeem.model.*;
import com.metarnet.eomeem.service.IEemCommonService;
import com.metarnet.eomeem.utils.PowerUtil;
import com.metarnet.eomeem.vo.AnalysisVo;
import com.metarnet.eomeem.vo.Specialty;
import com.metarnet.eomeem.vo.Tempt;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by Administrator on 2016/6/2.
 */
@Service
public class EemCommonServiceImpl implements IEemCommonService {

    private Logger logger = LogManager.getLogger(this.getClass());

    @Resource
    private IBaseDAO baseDAO;

    @Override
    public Pager queryDataList(Pager pager, String type, UserEntity userEntity, Long sign,String tempIds, int year) throws ServiceException {
//        boolean auditSameLevel = false;
//        boolean auperiorAudit = false;
//        Calendar cal = Calendar.getInstance();
//        int month = cal.get(Calendar.MONTH) + 1;
//        int year = cal.get(Calendar.YEAR);
        try {
            OrgEntity orgEntity = AAAAAdapter.getCompany(userEntity.getOrgID().intValue());
            StringBuffer hql = new StringBuffer("from ExcelPage where deletedFlag=0 and reportYear='"+year+"'");
            Map<String, Object> params = pager.getParameters();// jw
            if ("UNI".equals(userEntity.getCategory())) {//省份数据同级审核过
                if ("todo".equals(type)) {
                   // hql.append(" and auditSameLevel=true and auperiorAudit=false and length(reportOrgCode)=3 and applyId=1");
                    //hql.append(" and applyId=1 ");
                    //jw 3.7  //////////////省份查询框
                    if (params.get("deptIds") != null && StringUtils.isNotBlank(params.get("deptIds").toString())) {
                        hql.append("and auditSameLevel=true and auperiorAudit=false and applyId=1 and reportOrgCode in(" + params.get("deptIds").toString() + ")");
                    } else {
                        hql.append(" and auditSameLevel=true and auperiorAudit=false and length(reportOrgCode)=3 and applyId=1");
                    }
                    //  ///////////////////////
                } else {

                }
            } else if ("PRO".equals(userEntity.getCategory())) {//地市上级未审核，省份同级未审核，怎样判断地市的已经同级审核过？

                if ("todo".equals(type)) {
                    hql.append(" and ((length(reportOrgCode)=3 and applyId=1 and workOrderStatus='未审核') or (auditSameLevel=true and length(reportOrgCode)>3 and applyId=2 and auperiorAudit=false and workOrderStatus='已审核'))");//同级通过，上级未通过
                    hql.append(" and reportOrgCode like '" + orgEntity.getOrgCode() + "%'");
                } else {

                }
            } else if ("CITY".equals(userEntity.getCategory())) {//地市数据同级未审核
                if ("todo".equals(type)) {
                    hql.append(" and auditSameLevel=false and workOrderStatus='未审核' ");
                    hql.append(" and applyId=2  ");
                    hql.append(" and reportOrgCode like '" + orgEntity.getOrgCode() + "%' ");

                } else {

                }

            }


                if ("todo".equals(type)) {
                    hql.append(" and (iswithdraw='yes' or iswithdraw is null or iswithdraw = '') ");


            }

            Map<String, Object> queryMap = new HashMap<String, Object>();
          //  Map<String, Object> params = pager.getParameters();
            if (params.size() > 0) {
                if (params.get("tempId") != null && StringUtils.isNotBlank(params.get("tempId").toString())) {
                    hql.append(" and tpInputID = " + params.get("tempId"));
                }else if(!tempIds.equals("0")){
                    hql.append(" and tpInputID in ("+tempIds+")");
                }
                if (params.get("year") != null && StringUtils.isNotBlank(params.get("year").toString())) {
                    hql.append(" and reportYear = '" + params.get("year") + "'");
                }
                /*if (params.get("deptIds") != null && StringUtils.isNotBlank(params.get("deptIds").toString())) {
                    String orgCodes = "";
                    for (String orgId : params.get("deptIds").toString().split(",")) {
                        OrgEntity org = AAAAAdapter.getInstence().findOrgByOrgID(Long.parseLong(orgId));
                        orgCodes += "'" + org.getOrgCode() + "',";
                    }
                    hql.append(" and reportOrgCode in(:aaaa)");
                    queryMap.put("aaaa", orgCodes.substring(1, orgCodes.length() - 2));
                }*/   //jw3.8暂时去掉
                if (params.get("reportDate") != null && StringUtils.isNotBlank(params.get("reportDate").toString()) && !params.get("reportDate").equals("全年")) {
                    String reportDate = params.get("reportDate").toString();
                    hql.append(" and reportDate ='" + reportDate + "'");
                }
            }else if(!tempIds.equals("0")){

                hql.append(" and tpInputID in ("+tempIds+")");
            }
            int nowNumber = pager.getNowPage();
            pager = baseDAO.getPageByHql(hql.toString(), pager, queryMap);
            pager.setNowPage(nowNumber);
        } catch (DAOException e) {
            e.printStackTrace();
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        }
        for (Object excelPage : pager.getExhibitDatas()) {
            ExcelPage excelPage1 = (ExcelPage) excelPage;
//            excelPage1.setDateGrading(excelPage1.getReportYear()+">"+excelPage1.getReportDate());
            ((ExcelPage) excelPage).setDateGrading(excelPage1.getReportYear() + "-" + excelPage1.getReportDate());
        }
        pager.setIsSuccess(true);
        int pageSize = pager.getPageSize();
        int recordCount = pager.getRecordCount();
        int pageCount = pager.getPageCount();
        pageCount = recordCount / pageSize + (recordCount % pageSize > 0 ? 1 : 0);
        pager.setPageCount(pageCount);
        return pager;
    }


    @Override
    public Pager queryDataList2(Pager pager, String type, UserEntity userEntity, Long sign,int year, String reportDate) throws ServiceException {
//        Calendar cal = Calendar.getInstance();
//        int month = cal.get(Calendar.MONTH) + 1;
//        int year = cal.get(Calendar.YEAR);
//        String monthStr="";
//        if(month<=9){
//            monthStr="上半年";
//        }else{
//            monthStr="下半年";
//        }
        //也可把条件中审核状态都改为ep.WORK_ORDER_STATUS<>'未审核'

        StringBuffer sql = new StringBuffer("select distinct ep.pageid as objectId,ep.tpInputName,ep.OPER_USER_TRUE_NAME as operUserTrueName,ep.OPER_ORG_NAME as operOrgName,ep.OPER_USER_PHONE as operUserPhone,ep.reportDate ,ep.reportYear,ep.CREATION_TIME as creationTime,gim.PROCESSING_STATUS,gim.OPER_DESC as audit_info from t_eem_excel_page ep left join t_eom_general_info gim on ep.pageid=gim.PROCESSING_OBJECT_ID where (ep.WORK_ORDER_STATUS='已审核' or ep.WORK_ORDER_STATUS='审核退回') and gim.OPER_USER_ID=" + userEntity.getUserId()+" and reportYear="+year+" and reportDate="+"'"+reportDate+"'");

        StringBuffer countSql = new StringBuffer("select count(distinct ep.pageid) as num from t_eem_excel_page ep left join t_eom_general_info gim on ep.pageid=gim.PROCESSING_OBJECT_ID where (ep.WORK_ORDER_STATUS='已审核' or ep.WORK_ORDER_STATUS='审核退回') and gim.OPER_USER_ID=" + userEntity.getUserId() + " and ep.APPLY_ID=" + sign);
        if (userEntity.getCategory().equals("PRO")) {
            countSql = new StringBuffer("select count(distinct ep.pageid) as num from t_eem_excel_page ep left join t_eom_general_info gim on ep.pageid=gim.PROCESSING_OBJECT_ID where (ep.WORK_ORDER_STATUS = '已审核'OR ep.WORK_ORDER_STATUS = '审核退回')AND((length(reportOrgCode)= 3 AND ep.APPLY_ID = 1 AND ep.audit_Same_Level = TRUE)OR(ep.audit_Same_Level = TRUE AND ep.auperior_Audit = TRUE AND length(reportOrgCode)> 3 AND ep.APPLY_ID = 2))");
        }
        try {
            Map<String, Object> params = pager.getParameters();
            if ("UNI".equals(userEntity.getCategory())) {//集团审核过省份，上级审核为true
                //sql.append(" and ep.audit_Same_Level=TRUE and ep.APPLY_ID=1");
                //jw 3.7  //////////////省份查询框
                if (params.get("deptIds") != null && StringUtils.isNotBlank(params.get("deptIds").toString())) {
                    sql.append("and ep.audit_Same_Level=TRUE and ep.APPLY_ID=1 and reportOrgCode in(" + params.get("deptIds").toString() + ")");
                } else {
                    sql.append(" and ep.audit_Same_Level=TRUE and ep.APPLY_ID=1");
                }
                //  ///////////////////////
            } else if ("PRO".equals(userEntity.getCategory())) {//地市上级审核过，省份同级审核过
                sql.append(" and ((length(reportOrgCode)=3 and ep.APPLY_ID=1 and ep.audit_Same_Level=TRUE) or (ep.audit_Same_Level=TRUE and ep.auperior_Audit=true and length(reportOrgCode)>3 and ep.APPLY_ID=2))");

            } else if ("CITY".equals(userEntity.getCategory())) {//地市同级审核过
                sql.append(" and ep.audit_Same_Level=TRUE and ep.APPLY_ID=2 ");
            }

           // Map<String, Object> params = pager.getParameters();
            if (params.size() > 0) {
                if (params.get("tempId") != null && StringUtils.isNotBlank(params.get("tempId").toString())) {
                    sql.append(" and ep.tpInputID = " + params.get("tempId"));
                }
                if (params.get("year") != null && StringUtils.isNotBlank(params.get("year").toString())) {
                    sql.append(" and ep.reportYear = " + params.get("year"));
                }
                if (params.get("reportDate") != null && StringUtils.isNotBlank(params.get("reportDate").toString()) && !params.get("reportDate").equals("全年")) {
                    sql.append(" and ep.reportDate ='" + params.get("reportDate") + "'");
                }
            }
            int nowNumber = pager.getNowPage();
            pager = baseDAO.findNativeSQL(sql.toString(), null, pager);
            pager.setNowPage(nowNumber);
            List<Map> list = baseDAO.findNativeSQL(countSql.toString(), null);
            if (list.size() > 0) {
                if (list.get(0).get("num") != null) {
                    pager.setRecordCount(Integer.parseInt(list.get(0).get("num").toString()));
                }
            }
            for (Object excelPage : pager.getExhibitDatas()) {
                Map<String, Object> objectMap = (Map<String, Object>) excelPage;
                objectMap.put("dateGrading", objectMap.get("reportyear") + "-" + objectMap.get("reportdate"));
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        pager.setIsSuccess(true);
        int pageSize = pager.getPageSize();
        int recordCount = pager.getRecordCount();
        int pageCount = pager.getPageCount();
        pageCount = recordCount / pageSize + (recordCount % pageSize > 0 ? 1 : 0);
        pager.setPageCount(pageCount);
        return pager;
    }

    @Override
    public Pager sumDataList(Pager pager, UserEntity userEntity) throws ServiceException {
        StringBuffer hql = new StringBuffer("from EvaluationCollectExcel ere where ere.deletedFlag=0 ");
        Map<String, Object> queryMap = new HashMap<String, Object>();
        try {
            Map<String, Object> params = pager.getParameters();
            if (params.size() > 0) {
                if (params.get("tempId") != null && StringUtils.isNotBlank(params.get("tempId").toString())) {
                    hql.append(" and ere.formId = " + params.get("tempId"));
                }
                if (params.get("year") != null && StringUtils.isNotBlank(params.get("year").toString())) {
                    hql.append(" and ere.reportYear = " + params.get("year"));
                }
                if (params.get("reportDate") != null && StringUtils.isNotBlank(params.get("reportDate").toString())) {
                    hql.append(" and ere.reportDate ='" + params.get("reportDate") + "'");
                }
            }
            hql.append(" order by ere.creationTime desc");
            pager = baseDAO.getPageByHql(hql.toString(), pager, queryMap);
            for (EvaluationCollectExcel evaluationCollectExcel : (List<EvaluationCollectExcel>) pager.getExhibitDatas()) {
                evaluationCollectExcel.setDateGrading(evaluationCollectExcel.getReportYear() + "-" + evaluationCollectExcel.getReportDate());
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        pager.setIsSuccess(true);
        int pageSize = pager.getPageSize();
        int recordCount = pager.getRecordCount();
        int pageCount = pager.getPageCount();
        pageCount = recordCount / pageSize + (recordCount % pageSize > 0 ? 1 : 0);
        pager.setPageCount(pageCount);
        return pager;
    }

    @Override
    public Pager queryCancelList(Pager pager, UserEntity userEntity) throws ServiceException {
        try {
            String orgCode = userEntity.getOrgCode();
            if (userEntity.getCategory().equals("CITY")) {
                orgCode = orgCode.substring(0, 5);
            } else {
                orgCode = orgCode.substring(0, 3);
            }
            String sql = "";
            if ("PRO".equals(userEntity.getCategory())) {
                sql = "SELECT teep.OPER_ORG_NAME reportDept,teep.reportYear reportYear,teep.reportDate reportDate,teep.tpInputName tempName,tegi.OPER_USER_TRUE_NAME cancelPerson,tegi.OPER_TIME cancelDate,tegi.OPER_DESC cancelDesc from t_eom_general_info tegi LEFT JOIN t_eem_excel_page  teep ON tegi.PROCESSING_OBJECT_ID=teep.pageid and tegi.PROCESSING_STATUS ='N' where length(reportOrgCode)=3 and teep.reportOrgCode like '" + orgCode + "'";
            } else {
                sql = "SELECT teep.OPER_ORG_NAME reportDept,teep.reportYear reportYear,teep.reportDate reportDate,teep.tpInputName tempName,tegi.OPER_USER_TRUE_NAME cancelPerson,tegi.OPER_TIME cancelDate,tegi.OPER_DESC cancelDesc from t_eom_general_info tegi LEFT JOIN t_eem_excel_page  teep ON tegi.PROCESSING_OBJECT_ID=teep.pageid and tegi.PROCESSING_STATUS ='N' where teep.reportOrgCode like '" + orgCode + "%'";

            }
            pager = baseDAO.findNativeSQL(sql, null, pager);
        } catch (DAOException e) {
            e.printStackTrace();
        }

        pager.setIsSuccess(true);
        int pageSize = pager.getPageSize();
        int recordCount = pager.getRecordCount();
        int pageCount = pager.getPageCount();
        pageCount = recordCount / pageSize + (recordCount % pageSize > 0 ? 1 : 0);
        pager.setPageCount(pageCount);
        return pager;
    }

    @Override
    public Pager queryDeductList(Pager pager, UserEntity userEntity) throws ServiceException {
        try {
//            String orgCode = userEntity.getOrgCode();
            StringBuffer hql = new StringBuffer(" select * from t_eem_deduct_excel_page where pageid>0 ");
            Map<String, Object> params = pager.getParameters();
            if (params.size() > 0) {
                if (params.get("year") != null && StringUtils.isNotBlank(params.get("year").toString())) {
                    hql.append(" and reportYear = '" + params.get("year") + "'");
                }
                if (params.get("reportDate") != null && StringUtils.isNotBlank(params.get("reportDate").toString())) {
                    String reportDate = params.get("reportDate").toString();
                    hql.append(" and reportDate ='" + reportDate + "'");
                }

                if (params.get("tempId") != null && StringUtils.isNotBlank(params.get("tempId").toString())) {
                    String tempId = params.get("tempId").toString();
                    hql.append(" and tpInputID ='" + tempId + "'");
                }
            }

            pager = baseDAO.findNativeSQL(hql.append(" order by pageid desc").toString(), null, pager);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        pager.setIsSuccess(true);
        int pageSize = pager.getPageSize();
        int recordCount = pager.getRecordCount();
        int pageCount = pager.getPageCount();
        pageCount = recordCount / pageSize + (recordCount % pageSize > 0 ? 1 : 0);
        pager.setPageCount(pageCount);
        return pager;
    }

    /**
     * 统计分析
     *
     * @param pager
     * @param userEntity
     * @return
     * @throws ServiceException
     */
    @Override
    public Pager countAnalysis(Pager pager, UserEntity userEntity) throws ServiceException {
        /**
         * 1)	及时率：任务拟稿中规定任务上报周期，一次任务需要上报N张报表，任务结束时间内上报报表M张，及时率算法为：M/N*100；
         2)	准确率：一次任务中规定上报N张报表，在填报过程中被上级单位驳回M次（同一张报表被驳回多次，累加）；准确率算法为：（N-M）/N*100%；
         3)	及时率环比：环比=（本周期及时率－上周期及时率）/本周期及时率×100%；
         4)	准确率环比：环比=（本周期准确率－上周期准确率）/本周期准确率×100%；
         5)	统计分析模块菜单权限前期只对集团用户开放；
         将符合条件的数据都查出来，然后进行过滤计算
         */
        List<AnalysisVo> analysisVoList = new ArrayList<AnalysisVo>();
        try {
            Map<String, Object> params = pager.getParameters();
            int year = Calendar.getInstance().get(Calendar.YEAR);
            int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
            String reportDate = "上半年";
            String reportYear = String.valueOf(year);
            if (month < 7) {
                reportYear = String.valueOf(year - 1);
                reportDate = "下半年";
            }

            String preReportYear = "";
            String preReportDate = "";
            String endTime = "";
            String preEndTime = "";
            String curReportYear = null;
            if (params.size() > 0) {
                String param = params.get("reportDate").toString();
                reportYear = param.split("-")[0];
                reportDate = param.split("-")[1];
            }
            int yearNum = Integer.parseInt(reportYear);
            if (reportDate.equals("上半年")) {
//                preReportYear = reportYear + "";
//                preReportDate = "第四季度";

                preReportYear = String.valueOf(yearNum-1);
                preReportDate = "下半年";
                endTime = reportYear + "-07-21";
                preEndTime = reportYear + "-01-21";
            } else if (reportDate.equals("下半年")) {
                preReportYear = reportYear;
                curReportYear = String.valueOf(yearNum+1);
//                preReportDate = "第一季度";
                preReportDate = "上半年";
                endTime = curReportYear + "-01-21";
                preEndTime = reportYear + "-07-21";
            }
//            else if (reportDate.equals("第三季度")) {
//                preReportYear = reportYear;
//                preReportDate = "第二季度";
//                endTime = reportYear + "-10-29";
//                preEndTime = reportYear + "-07-29";
//            } else if (reportDate.equals("第四季度")) {
//                preReportYear = reportYear;
//               preReportDate = "第三季度";
//                endTime = (Integer.parseInt(reportYear) + 1) + "-01-29";
//                preEndTime = reportYear + "-10-29";
//            }
            String timelyRateSql = "";
            String accuracyRateSql = "";
            String preTimelyRateSql = "";
            String preAccuracyRateSql = "";
            if (reportDate.equals("全年")) {
                timelyRateSql = "SELECT t2.OPER_ORG_NAME AS province, t2.OPER_USER_TRUE_NAME AS reportPerson, t2.OPER_USER_PHONE AS reportTel, t2.reportOrgCode, CASE WHEN t2.CREATION_TIME < '" + endTime + " 00:00:00' THEN 1 ELSE 0 END AS num FROM t_eem_excel_page t2 JOIN ( SELECT min(t1.pageid) pageid FROM t_eem_excel_page t1 WHERE t1.reportYear = '2017' AND t1.reportDate = '上半年' and t1.APPLY_ID=1 GROUP BY t1.tpInputID, t1.reportOrgCode ) t3 ON t2.pageid = t3.pageid " +
                        "UNION " +
                        "SELECT t2.OPER_ORG_NAME AS province, t2.OPER_USER_TRUE_NAME AS reportPerson, t2.OPER_USER_PHONE AS reportTel, t2.reportOrgCode, CASE WHEN t2.CREATION_TIME < '" + endTime + " 00:00:00' THEN 1 ELSE 0 END AS num FROM t_eem_excel_page t2 JOIN ( SELECT min(t1.pageid) pageid FROM t_eem_excel_page t1 WHERE t1.reportYear = '2017' AND t1.reportDate = '下半年' and t1.APPLY_ID=1  GROUP BY t1.tpInputID, t1.reportOrgCode ) t3 ON t2.pageid = t3.pageid ";
//                        "UNION " +
//                        "SELECT t2.OPER_ORG_NAME AS province, t2.OPER_USER_TRUE_NAME AS reportPerson, t2.OPER_USER_PHONE AS reportTel, t2.reportOrgCode, CASE WHEN t2.CREATION_TIME < '"+endTime+" 00:00:00' THEN 1 ELSE 0 END AS num FROM t_eem_excel_page t2 JOIN ( SELECT min(t1.pageid) pageid FROM t_eem_excel_page t1 WHERE t1.reportYear = '2016' AND t1.reportDate = '第三季度' GROUP BY t1.tpInputID, t1.reportOrgCode ) t3 ON t2.pageid = t3.pageid " +
//                        "UNION " +
//                        "SELECT t2.OPER_ORG_NAME AS province, t2.OPER_USER_TRUE_NAME AS reportPerson, t2.OPER_USER_PHONE AS reportTel, t2.reportOrgCode, CASE WHEN t2.CREATION_TIME < '"+endTime+" 00:00:00' THEN 1 ELSE 0 END AS num FROM t_eem_excel_page t2 JOIN ( SELECT min(t1.pageid) pageid FROM t_eem_excel_page t1 WHERE t1.reportYear = '2016' AND t1.reportDate = '第四季度' GROUP BY t1.tpInputID, t1.reportOrgCode ) t3 ON t2.pageid = t3.pageid ";
                //本周期准确率数据查询sql
                accuracyRateSql = "SELECT sum(rejectNum) num, t1.reportOrgCode FROM t_eem_excel_page t1 WHERE t1.reportYear = '" + reportYear + "' AND t1.reportDate = '上半年' and t1.APPLY_ID=1  GROUP BY t1.reportOrgCode " +
                        "UNION " +
                        "SELECT sum(rejectNum) num, t1.reportOrgCode FROM t_eem_excel_page t1 WHERE t1.reportYear = '" + reportYear + "' AND t1.reportDate = '下半年' and t1.APPLY_ID=1  GROUP BY t1.reportOrgCode ";
//                        "UNION " +
//                        "SELECT sum(rejectNum) num, t1.reportOrgCode FROM t_eem_excel_page t1 WHERE t1.reportYear = '"+reportYear+"' AND t1.reportDate = '第三季度' GROUP BY t1.reportOrgCode " +
//                        "UNION " +
//                        "SELECT sum(rejectNum) num, t1.reportOrgCode FROM t_eem_excel_page t1 WHERE t1.reportYear = '"+reportYear+"' AND t1.reportDate = '第四季度' GROUP BY t1.reportOrgCode";
                //上周期及时率数据查询sql
                preTimelyRateSql = "SELECT t2.OPER_ORG_NAME AS province, t2.OPER_USER_TRUE_NAME AS reportPerson, t2.OPER_USER_PHONE AS reportTel, t2.reportOrgCode, CASE WHEN t2.CREATION_TIME < '" + preEndTime + " 00:00:00' THEN 1 ELSE 0 END AS num FROM t_eem_excel_page t2 JOIN ( SELECT min(t1.pageid) pageid FROM t_eem_excel_page t1 WHERE t1.reportYear = '2017' AND t1.reportDate = '上半年' and t1.APPLY_ID=1  GROUP BY t1.tpInputID, t1.reportOrgCode ) t3 ON t2.pageid = t3.pageid " +
                        "UNION " +
                        "SELECT t2.OPER_ORG_NAME AS province, t2.OPER_USER_TRUE_NAME AS reportPerson, t2.OPER_USER_PHONE AS reportTel, t2.reportOrgCode, CASE WHEN t2.CREATION_TIME < '" + preEndTime + " 00:00:00' THEN 1 ELSE 0 END AS num FROM t_eem_excel_page t2 JOIN ( SELECT min(t1.pageid) pageid FROM t_eem_excel_page t1 WHERE t1.reportYear = '2017' AND t1.reportDate = '下半年' and t1.APPLY_ID=1 GROUP BY t1.tpInputID, t1.reportOrgCode ) t3 ON t2.pageid = t3.pageid ";
//                        "UNION " +
//                        "SELECT t2.OPER_ORG_NAME AS province, t2.OPER_USER_TRUE_NAME AS reportPerson, t2.OPER_USER_PHONE AS reportTel, t2.reportOrgCode, CASE WHEN t2.CREATION_TIME < '"+preEndTime+" 00:00:00' THEN 1 ELSE 0 END AS num FROM t_eem_excel_page t2 JOIN ( SELECT min(t1.pageid) pageid FROM t_eem_excel_page t1 WHERE t1.reportYear = '2016' AND t1.reportDate = '第三季度' GROUP BY t1.tpInputID, t1.reportOrgCode ) t3 ON t2.pageid = t3.pageid " +
//                        "UNION " +
//                        "SELECT t2.OPER_ORG_NAME AS province, t2.OPER_USER_TRUE_NAME AS reportPerson, t2.OPER_USER_PHONE AS reportTel, t2.reportOrgCode, CASE WHEN t2.CREATION_TIME < '"+preEndTime+" 00:00:00' THEN 1 ELSE 0 END AS num FROM t_eem_excel_page t2 JOIN ( SELECT min(t1.pageid) pageid FROM t_eem_excel_page t1 WHERE t1.reportYear = '2016' AND t1.reportDate = '第四季度' GROUP BY t1.tpInputID, t1.reportOrgCode ) t3 ON t2.pageid = t3.pageid ";
                //上周期准确率数据查询sql
                preAccuracyRateSql = "SELECT sum(rejectNum) num, t1.reportOrgCode FROM t_eem_excel_page t1 WHERE t1.reportYear = '" + preReportYear + "' and t1.APPLY_ID=1  AND t1.reportDate = '上半年' GROUP BY t1.reportOrgCode " +
                        "UNION " +
                        "SELECT sum(rejectNum) num, t1.reportOrgCode FROM t_eem_excel_page t1 WHERE t1.reportYear = '" + preReportYear + "' and t1.APPLY_ID=1  AND t1.reportDate = '下半年' GROUP BY t1.reportOrgCode ";
//                         "UNION " +
//                         "SELECT sum(rejectNum) num, t1.reportOrgCode FROM t_eem_excel_page t1 WHERE t1.reportYear = '"+preReportYear+"' AND t1.reportDate = '第三季度' GROUP BY t1.reportOrgCode " +
//                         "UNION " +
//                         "SELECT sum(rejectNum) num, t1.reportOrgCode FROM t_eem_excel_page t1 WHERE t1.reportYear = '"+preReportYear+"' AND t1.reportDate = '第四季度' GROUP BY t1.reportOrgCode";
            } else {
                //本周期及时率数据查询sql
                timelyRateSql = "SELECT " +
                        "t2.OPER_ORG_NAME AS province, " +
                        "t2.OPER_USER_TRUE_NAME AS reportPerson, " +
                        "t2.OPER_USER_PHONE AS reportTel, " +
                        "t2.reportOrgCode " +
//                        "case when t2.CREATION_TIME < '" + endTime + " 00:00:00' then 1 else 0 end as num " +
                        "FROM " +
                        "t_eem_excel_page t2 " +
                        "JOIN ( " +
                        "SELECT " +
                        "min(t1.pageid) pageid " +
                        "FROM " +
                        "t_eem_excel_page t1 " +
                        "WHERE " +
                        "t1.reportYear = '" + reportYear + "' " +
                        "AND t1.WORK_ORDER_STATUS = '已审核' "+                            //  jw  3.7 tianjia 及时率已上报的计算
                        "  and t1.APPLY_ID=1 AND t1.reportDate = '" + reportDate + "' " +
                        "GROUP BY " +
                        "t1.tpInputID,t1.reportOrgCode " +
                        ") t3 ON t2.pageid = t3.pageid " +
                        "where t2.CREATION_TIME < '" + endTime + " 00:00:00'";
                //本周期准确率数据查询sql
                accuracyRateSql = "SELECT " +
                        "sum(rejectNum) num, " +
                        "t1.reportOrgCode " +
                        "FROM " +
                        "t_eem_excel_page t1 " +
                        "WHERE " +
                        "t1.reportYear = '" + reportYear + "' " +
                        "  and t1.APPLY_ID=1 AND t1.reportDate = '" + reportDate + "' " +
                        "GROUP BY " +
                        "t1.reportOrgCode";
                //上周期及时率数据查询sql
                preTimelyRateSql = "SELECT " +
                        "t2.OPER_ORG_NAME AS province, " +
                        "t2.OPER_USER_TRUE_NAME AS reportPerson, " +
                        "t2.OPER_USER_PHONE AS reportTel, " +
                        "t2.reportOrgCode " +
//                        "case when t2.CREATION_TIME < '" + preEndTime + " 00:00:00' then 1 else 0 end as num " +
                        "FROM " +
                        "t_eem_excel_page t2 " +
                        "JOIN ( " +
                        "SELECT " +
                        "min(t1.pageid) pageid " +
                        "FROM " +
                        "t_eem_excel_page t1 " +
                        "WHERE " +
                        "t1.reportYear = '" + preReportYear + "' " +
                        "AND t1.WORK_ORDER_STATUS = '已审核' "+                            //  jw  3.7 tianjia 及时率已上报的计算
                        "and t1.APPLY_ID=1 AND t1.reportDate = '" + preReportDate + "' " +
                        "GROUP BY " +
                        "t1.tpInputID,t1.reportOrgCode " +
                        ") t3 ON t2.pageid = t3.pageid " +
                        "where t2.CREATION_TIME < '" + preEndTime + " 00:00:00'";
                //上周期准确率数据查询sql
                preAccuracyRateSql = "SELECT " +
                        "sum(rejectNum) num, " +
                        "t1.reportOrgCode " +
                        "FROM " +
                        "t_eem_excel_page t1 " +
                        "WHERE " +
                        "t1.reportYear = '" + preReportYear + "' " +
                        "and t1.APPLY_ID=1 AND t1.reportDate = '" + preReportDate + "' " +
                        "GROUP BY " +
                        "t1.reportOrgCode";
            }
            List list = baseDAO.find("select new EemTempEntity(objectId,tempName) from EemTempEntity where deletedFlag=0 and tempType=1 ");

            List<Map> currentTimelyRateList = baseDAO.findNativeSQL(timelyRateSql, null);
            List<Map> currentAccuracyRateList = baseDAO.findNativeSQL(accuracyRateSql, null);
            List<Map> preTimelyRateList = baseDAO.findNativeSQL(preTimelyRateSql, null);
            List<Map> preAccuracyRateList = baseDAO.findNativeSQL(preAccuracyRateSql, null);
            List<OrgEntity> orgEntityList = null;
            try {
                orgEntityList = AAAAAdapter.getInstence().findOrgListAndSelfByParentID(1L, false);
            } catch (PaasAAAAException e) {
                e.printStackTrace();
            }
            for (OrgEntity orgEntity : orgEntityList) {
                if (orgEntity.getOrgCode().startsWith("1") || orgEntity.getOrgCode().startsWith("81") || orgEntity.getOrgCode().startsWith("51")) {
                    continue;
                }/* else {
                    AnalysisVo vo = new AnalysisVo();
                    vo.setProvinceName(orgEntity.getOrgName());
                    vo.setProvinceCode(orgEntity.getOrgCode());
                    analysisVoList.add(vo);
                }*/
                else if( orgEntity.getOrgCode().startsWith("2"))  {
                    AnalysisVo vo = new AnalysisVo();
                    vo.setProvinceName(orgEntity.getOrgName());
                    vo.setProvinceCode(orgEntity.getOrgCode());
                    analysisVoList.add(vo);}
            }
            //本期及时率及准确率
            for (AnalysisVo vo : analysisVoList) {
                Double reportNum = 0d;
                Double rejectNum = 0d;
                Set<String> nameSet = new HashSet<String>();
                for (Map map : currentTimelyRateList) {
                    if (map.get("reportorgcode") != null && map.get("reportorgcode").equals(vo.getProvinceCode())) {
                        reportNum++;
                        nameSet.add(map.get("reportperson") + "(" + map.get("reporttel") + ")");
                    }
                }
                vo.setReportPerson(nameSet.toString().substring(1, nameSet.toString().length() - 1));
                for (Map map : currentAccuracyRateList) {
                    if (map.get("reportorgcode") != null && map.get("reportorgcode").equals(vo.getProvinceCode()) && map.get("num") != null) {
                        rejectNum += Double.parseDouble(map.get("num").toString());
                    }
                }
                if (StringUtils.isBlank(vo.getReportPerson())) {
                    vo.setTimelyRate(0d);
                    vo.setAccuracyRate(0d);
                } else {
                    Double timelyRate = (reportNum / list.size()) * 100;//本期及时率;
                    Double accuracyRate = (list.size() - rejectNum) / list.size() * 100;//本期准确率
                    vo.setTimelyRate(new BigDecimal(timelyRate).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    vo.setAccuracyRate(new BigDecimal(accuracyRate).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
            //上期及时率及准确率
            for (AnalysisVo vo : analysisVoList) {
                Double reportNum = 0d;
                Double rejectNum = 0d;
                for (Map map : preTimelyRateList) {
                    if (map.get("reportorgcode") != null && map.get("reportorgcode").equals(vo.getProvinceCode())) {
                        reportNum++;
                    }
                }
                for (Map map : preAccuracyRateList) {
                    if (map.get("reportorgcode") != null && map.get("reportorgcode").equals(vo.getProvinceCode()) && map.get("num") != null) {
                        rejectNum += Double.parseDouble(map.get("num").toString());
                    }
                }
                Double preAccuracyRate = 0d;
                Double preTimelyRate = (reportNum / list.size()) * 100;//上期及时率;
                if (rejectNum > 0) {
                    preAccuracyRate = (list.size() - rejectNum) / list.size() * 100;//上期准确率
                }
                if (vo.getTimelyRate() <= 0) {
                    vo.setMomTimelyRate(0d);
                } else {
                    vo.setMomTimelyRate(new BigDecimal((vo.getTimelyRate() - preTimelyRate) / vo.getTimelyRate() * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
                if (vo.getAccuracyRate() <= 0) {
                    vo.setMomAccuracyRate(0d);
                } else {
                    vo.setMomAccuracyRate(new BigDecimal((vo.getAccuracyRate() - preAccuracyRate) / vo.getAccuracyRate() * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
            pager.setExhibitDatas(analysisVoList);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        pager.setIsSuccess(true);
        pager.setPageCount(analysisVoList.size() > 0 ? 1 : 0);
        pager.setRecordCount(analysisVoList.size());
        pager.setPageSize(analysisVoList.size());
        return pager;
    }

    @Override
    public String saveAudit(GeneralInfoModel generalInfoModel, UserEntity userEntity) throws ServiceException {
        try {
            generalInfoModel.setOperUserId(userEntity.getUserId());
            generalInfoModel.setOperUserTrueName(userEntity.getTrueName());
            generalInfoModel.setOperOrgId(userEntity.getOrgID());
            generalInfoModel.setOperOrgName(userEntity.getOrgEntity().getOrgName());
            generalInfoModel.setOperFullOrgName(userEntity.getOrgEntity().getFullOrgName());
            generalInfoModel.setOperTime(new Timestamp(new Date().getTime()));

            Map orgInfo = null;

            orgInfo = PubFun.getOrgInfoByOrgID(Integer.parseInt(AAAAAdapter.getCompany(userEntity.getOrgID().intValue()).getOrgId().toString()));

            // 所属省分
            generalInfoModel.setBelongProvinceCode(orgInfo.get(PubFun.BELONGEDPROVINCE) == null ? null : ((Long) orgInfo.get(PubFun.BELONGEDPROVINCE)).intValue());
            //所属省分中文
            generalInfoModel.setBelongProvinceName(orgInfo.get(PubFun.BELONGEDPROVINCENAME) == null ? null : ((String) orgInfo.get(PubFun.BELONGEDPROVINCENAME)));
            // 所属地市
            generalInfoModel.setBelongCityCode(orgInfo.get(PubFun.BELONGEDCITY) == null ? null : ((Long) orgInfo.get(PubFun.BELONGEDCITY)).intValue());
            //所属地市中文
            generalInfoModel.setBelongCityName(orgInfo.get(PubFun.BELONGEDCITYNAME) == null ? null : ((String) orgInfo.get(PubFun.BELONGEDCITYNAME)));
            generalInfoModel.setObjectId(baseDAO.getSequenceNextValue(generalInfoModel.getClass()));
            ExcelPage excelPage = (ExcelPage) baseDAO.get(ExcelPage.class, generalInfoModel.getProcessingObjectID());

            if (Constants.Y.equals(generalInfoModel.getProcessingStatus())) {
                //同级审核，审核状态不变还是未审;上级审核，审核状态改变
                if ("UNI".equals(userEntity.getCategory())) {//集团，上级审核
                    excelPage.setWorkOrderStatus("已审核");
                    excelPage.setAuperiorAudit(true);
                } else if ("PRO".equals(userEntity.getCategory())) {//省份
                    if (excelPage.getReportOrgCode().length() == 3) {
                        excelPage.setWorkOrderStatus("已审核");
                        excelPage.setAuditSameLevel(true);
                    } else {
                        excelPage.setWorkOrderStatus("已审核");
                        excelPage.setAuperiorAudit(true);
                    }
                } else if ("CITY".equals(userEntity.getCategory())) {//地市
                    excelPage.setWorkOrderStatus("已审核");
                    excelPage.setAuditSameLevel(true);
                }

            } else {
                if ("UNI".equals(userEntity.getCategory())) {//集团，上级退回
                    excelPage.setWorkOrderStatus("审核退回");
                    excelPage.setAuperiorAudit(true);
                    excelPage.setIswithdraw("Y");
                  //  excelPage.setDeletedFlag(true);

                } else if ("PRO".equals(userEntity.getCategory())) {//省份
                    if (excelPage.getReportOrgCode().length() == 3) {//同级退回
                        excelPage.setWorkOrderStatus("审核退回");//省分退回省分上报的数据
                        excelPage.setAuditSameLevel(true);
                        excelPage.setIswithdraw("Y");
                      //  excelPage.setDeletedFlag(true);

                    } else {
                        excelPage.setWorkOrderStatus("审核退回");
                        excelPage.setAuperiorAudit(true);
                        excelPage.setIswithdraw("Y");
                       // excelPage.setDeletedFlag(true);



                    }
                } else if ("CITY".equals(userEntity.getCategory())) {//地市
                    excelPage.setWorkOrderStatus("审核退回");//地市退回地市上报的数据
                    excelPage.setAuditSameLevel(true);
                    excelPage.setIswithdraw("Y");
                   // excelPage.setDeletedFlag(true);


                }

                Integer num = excelPage.getRejectNum();
                if (num == null) {
                    num = 1;
                } else {
                    num++;
                }
                excelPage.setRejectNum(num);
            }

            baseDAO.saveOrUpdate(generalInfoModel);
            baseDAO.saveOrUpdate(excelPage);
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ReportEntity> reportTempList(String type, UserEntity userEntity) throws ServiceException {
        try {
            if (type.equals("甲")) {
                String hql = "select new ReportEntity(id,sheetName,shortName,tpInputID,type,venderName) from ReportEntity where deletedFlag=0 and Type=甲";
                return baseDAO.find(hql);
            } else if (type.equals("乙")) {
                return baseDAO.find("select new ReportEntity(id,sheetName,shortName,tpInputID,type,venderName) from ReportEntity where deletedFlag=0 and Type=乙");
            } else if (type.equals("丙")) {
                return baseDAO.find("select new ReportEntity(id,sheetName,shortName,tpInputID,type,venderName) from ReportEntity where deletedFlag=0 and Type=丙");
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<EemTempEntity> findTempList(String type, UserEntity userEntity,String tempIds) throws ServiceException {
        try {
            if(StringUtils.isNotBlank(tempIds)){
                if (type.equals("report")) {
                    String hql = "select new EemTempEntity(objectId,tempName,reportedFrequency,tempType,tempPattern,attribute1,level) from EemTempEntity where deletedFlag=0 and tempType=1 ";
                    if(!tempIds.equals("all")&&!tempIds.equals("0")){
                        hql+="and objectId in("+tempIds+") ";
                    }

                    if (userEntity != null && userEntity.getCategory().equals("CITY")) {
                        hql += " and level!=3";
                    }
                    return baseDAO.find(hql);
                } else if (type.equals("sum")) {
                    return baseDAO.find("select new EemTempEntity(objectId,tempName,reportedFrequency,tempType,tempPattern,attribute1,level) from EemTempEntity where deletedFlag=0 and tempType=2");
                } else if (type.equals("deduct")) {
                    return baseDAO.find("select new EemTempEntity(objectId,tempName,shortName,reportedFrequency,tempType,tempPattern,attribute1,level,applyId) from EemTempEntity where deletedFlag=0 and tempType=3");
                }
            }else {
                return null;
            }

        } catch (DAOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public EvaluationReportExcel findEvaluationReportExcelByEid(String ObjectId) throws ServiceException, DAOException {
        List<EvaluationReportExcel> list = baseDAO.find("from EvaluationReportExcel a where a.objectId=" + Long.parseLong(ObjectId));
        EvaluationReportExcel nt = null;
        if (list != null && list.size() > 0) {
            nt = list.get(0);
        }
        return nt;


    }

    @Override
    public EvaluationCollectExcel findEvaluationCollectExcelById(Long id) throws ServiceException, DAOException {
        EvaluationCollectExcel collectExcel = (EvaluationCollectExcel) baseDAO.get(EvaluationCollectExcel.class, id);
        return collectExcel;
    }

    @Override
    public void saveOrderReportDate(String reportData, UserEntity userEntity, boolean isOrder) throws ServiceException {
        try {
            OrgEntity company = AAAAAdapter.getCompany(Integer.parseInt(userEntity.getOrgEntity().getOrgId().toString()));
            EvaluationReportTime reportTime = new EvaluationReportTime();
            List<EvaluationReportTime> reportTimeList = baseDAO.find("from EvaluationReportTime where ywdepart='" + company.getOrgCode() + "' and isReport='" + reportData + "'");
            if (reportTimeList.size() > 0) {
                reportTime = reportTimeList.get(0);
                if (isOrder) {
                    reportTime.setDeletedFlag(false);
                } else {
                    reportTime.setDeletedFlag(true);
                }
                baseDAO.saveOrUpdate(reportTime);
            } else {
                reportTime.setYwdepart(company.getOrgCode());
                reportTime.setName(company.getOrgName());
                reportTime.setIsReport(reportData);
                reportTime.setCreatedBy(userEntity.getUserId());
                reportTime.setCreaterTrueName(userEntity.getTrueName());
                reportTime.setDeletedFlag(true);
                reportTime.setObjectID(baseDAO.getSequenceNextValue(EvaluationReportTime.class));
            }
            baseDAO.saveOrUpdate(reportTime);
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Pager queryReportExcel(Pager pager, UserEntity userEntity) throws ServiceException {
        StringBuffer hql = new StringBuffer("from EvaluationReportExcel where 1=1 ");
        Map<String, Object> queryMap = new HashMap<String, Object>();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        try {
            Map<String, Object> params = pager.getParameters();
            if (params.get("reportData") != null) {
                String reportDateStr = "下半年";
//                String param = URLDecoder.decode(params.get("reportData").toString(),"UTF-8");
                String param = params.get("reportData").toString();
                if (month < 7) {
                    reportDateStr = "下半年";
                    year = year - 1;
                } else {
                    reportDateStr = "上半年";
                }
                /*if (param.equals("上一周期")) {
                    if ("上半年".equals(reportDateStr)) {
                        reportDateStr = "下半年";
                        year = year - 1;
                    } else {
                        reportDateStr = "上半年";
                    }
                }

                if ("年度汇总".equals(param)) {
                    reportDateStr = "全年";
                }
                if ("前推第二周期".equals(param)) {
                    year = year - 1;
                }*/
                // ============jw  3.20
                if (param.contains("上一周期")) {
                    if ("上半年".equals(reportDateStr)) {
                        reportDateStr = "下半年";
                        year = year - 1;
                    } else {
                        reportDateStr = "上半年";
                    }
                }
                if (param.contains("年度汇总")) {
                    reportDateStr = "全年";
                }
                if (param.contains("前推第二周期")) {
                    year = year - 1;
                }
                if (param.contains("上一年度汇总")) {
                    year = year - 1;
                    reportDateStr = "上一年度";
                }

                hql.append(" and reportYear='" + year + "'");
                hql.append(" and reportDate='" + reportDateStr + "'");
            } else {


                if (month < 7) {
                    hql.append(" and reportYear='" + (year - 1) + "'");
                    hql.append(" and reportDate='下半年'");
                }
                if (month > 6) {//当年第一季度
                    hql.append(" and reportYear='" + year + "'");
                    hql.append(" and reportDate='上半年'");
                }
               /* if (month <3) {//当年第二季度
                    hql.append(" and reportYear='" + (year - 1) + "'");
                    hql.append(" and reportDate='上半年'");
                }*/
//                if (month == 10 || month == 11 || month == 12) {//当年第三季度
//                    hql.append(" and reportYear='" + year + "'");
//                    hql.append(" and reportDate='第三季度'");
//                }
            }
            int nowNumber = pager.getNowPage();

            pager = baseDAO.getPageByHql(hql.toString(), pager, queryMap);
            pager.setNowPage(nowNumber);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        pager.setIsSuccess(true);
        int pageSize = pager.getPageSize();
        int recordCount = pager.getRecordCount();
        int pageCount = pager.getPageCount();
        pageCount = recordCount / pageSize + (recordCount % pageSize > 0 ? 1 : 0);
        pager.setPageCount(pageCount);
        return pager;
    }

    @Override
    public List<EemTempEntity> findTempList2(String type, UserEntity userEntity) throws ServiceException {
        try {
            if (type.equals("report")) {
                return baseDAO.find(" from EemTempEntity where deletedFlag=0 and tempType=1");
            } else if (type.equals("sum")) {
                return baseDAO.find(" from EemTempEntity where deletedFlag=0 and tempType=2");
//                return baseDAO.find(" from EemTempEntity where deletedFlag=0 and tempType=2 and objectId=137");
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isButton(String isOrder, UserEntity userEntity) throws ServiceException {
        try {
            if (!"root".equals(userEntity.getUserName())) {
                List<EvaluationReportTime> reportTimeList = baseDAO.find("from EvaluationReportTime where deletedFlag=0 and isReport='" + isOrder + "'");
                if (reportTimeList.size() > 0) {
                    return false;
                } else {
                    return true;
                }
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<EemTempEntity> getAllUseExcelTempletList(Integer type) {
        List<EemTempEntity> eemTempEntityList = null;
        try {
            eemTempEntityList = baseDAO.find("select new EemTempEntity(objectId,tempName,shortName) from EemTempEntity where deletedFlag=0 and tempType=1 and reportedFrequency=" + type);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return eemTempEntityList;
    }

    @Override
    public List findPageList(String dateStr, String year, OrgEntity orgEntity, UserEntity userEntity) {
        List excelPageList = new ArrayList();
        StringBuffer hql = new StringBuffer("from ExcelPage where deletedFlag=0");
        String sql = "";
        if (userEntity.getCategory().equals("UNI") || "root".equals(userEntity.getUserName())) {
//            sql = "SELECT " +
//                    "t.reportOrgCode, " +
//                    "t.tpInputID, " +
//                    "t.tpInputName, " +
//                    "COUNT(t.pageid) num " +
//                    "FROM " +
//                    "t_eem_excel_page t " +
//                    "GROUP BY " +
//                    "t.tpInputID, " +
//                    "t.reportOrgCode, " +
//                    "t.reportYear, " +
//                    "t.reportDate " +
//                    "HAVING " +
//                    "length(t.reportOrgCode) =3 " +
//                    "AND reportYear = '" + year + "' " +
//                    "AND reportDate = '" + dateStr + "'";
            sql = "SELECT \n" +
                    "  a.reportOrgCode,\n" +
                    "\ta.tpInputID,\n" +
                    "\ta.tpInputName,\n" +
                    "a.WORK_ORDER_STATUS WORKORDERSTATUS, (CASE a.WORK_ORDER_STATUS\n" +
                    "WHEN '已审核' THEN 1\n" +
                    "WHEN '未审核' THEN 0\n" +
                    "ELSE NULL END) num\n" +
                    "FROM(\n" +
                    "SELECT\n" +
                    "\tt.reportOrgCode,\n" +
                    "\tt.tpInputID,\n" +
                    "\tt.tpInputName,\n" +
                    "  t.CREATION_TIME,\n" +
                    "  t.WORK_ORDER_STATUS\n" +
                    "FROM\n" +
                    "\tt_eem_excel_page t\n" +
                    "where\n" +
                    "\tlength(t.reportOrgCode) = 3\n" +
                    "AND reportYear = '" + year + "'\n" +
                    "AND reportDate = '" + dateStr + "'\n" +
                    "ORDER BY t.tpInputID,t.reportOrgCode) a ,\n" +
                    "(SELECT\n" +
                    "\tt.reportOrgCode,\n" +
                    "\tt.tpInputID,\n" +
                    "  MAX(t.CREATION_TIME) sj\n" +
                    "\n" +
                    "FROM\n" +
                    "\tt_eem_excel_page t\n" +
                    "where\n" +
                    "\tlength(t.reportOrgCode) = 3\n" +
                    "AND reportYear = '" + year + "'\n" +
                    "AND reportDate = '" + dateStr + "'\n" +
                    "GROUP BY t.reportOrgCode,t.tpInputID\n" +
                    "ORDER BY t.tpInputID,t.reportOrgCode) b\n" +
                    "where a.tpInputID = b.tpInputID\n" +
                    "and a.reportOrgCode=b.reportOrgCode\n" +
                    "and a.CREATION_TIME = b.sj";

        } else if (userEntity.getCategory().equals("PRO")) {
//            sql = "SELECT " +
//                    "t.reportOrgCode, " +
//                    "t.tpInputID, " +
//                    "t.tpInputName, " +
//                    "COUNT(t.pageid) num " +
//                    "FROM " +
//                    "t_eem_excel_page t " +
//                    "GROUP BY " +
//                    "t.tpInputID, " +
//                    "t.reportOrgCode, " +
//                    "t.reportYear, " +
//                    "t.reportDate " +
//                    "HAVING " +
//                    "t.reportOrgCode like '" + orgEntity.getOrgCode() + "%'" +
//                    "AND reportYear = '" + year + "' " +
//                    "AND reportDate = '" + dateStr + "'";
            sql="SELECT \n" +
                    "  a.reportOrgCode,\n" +
                    "\ta.tpInputID,\n" +
                    "\ta.tpInputName,\n" +
                    "a.WORK_ORDER_STATUS WORKORDERSTATUS, " +
                    "(CASE a.WORK_ORDER_STATUS\n" +
                    "WHEN '已审核' THEN 1\n" +
                    "WHEN '未审核' THEN 0\n" +
                    "ELSE NULL END) num\n" +
                    "FROM(\n" +
                    "SELECT\n" +
                    "\tt.reportOrgCode,\n" +
                    "\tt.tpInputID,\n" +
                    "\tt.tpInputName,\n" +
                    "  t.CREATION_TIME,\n" +
                    "  t.WORK_ORDER_STATUS\n" +
                    "FROM\n" +
                    "\tt_eem_excel_page t\n" +
                    "where\n" +
                    "\tt.reportOrgCode LIKE '" + orgEntity.getOrgCode() + "%'\n" +
                    "AND reportYear = '" + year + "'\n" +
                    "AND reportDate = '" + dateStr + "'\n" +
                    "ORDER BY t.tpInputID,t.reportOrgCode) a ,\n" +
                    "(SELECT\n" +
                    "\tt.reportOrgCode,\n" +
                    "\tt.tpInputID,\n" +
                    "  MAX(t.CREATION_TIME) sj\n" +
                    "\n" +
                    "FROM\n" +
                    "\tt_eem_excel_page t\n" +
                    "where\n" +
                    "\tt.reportOrgCode LIKE '" + orgEntity.getOrgCode() + "%'\n" +
                    "AND reportYear = '" + year + "' \n" +
                    "AND reportDate = '" + dateStr + "'\n" +
                    "GROUP BY t.reportOrgCode,t.tpInputID\n" +
                    "ORDER BY t.tpInputID,t.reportOrgCode) b\n" +
                    "where a.tpInputID = b.tpInputID\n" +
                    "and a.reportOrgCode=b.reportOrgCode\n" +
                    "and\n" +
                    "a.CREATION_TIME = b.sj";
        } else {
//            sql = "SELECT " +
//                    "t.reportOrgCode, " +
//                    "t.tpInputID, " +
//                    "t.tpInputName, " +
//                    "COUNT(t.pageid) num " +
//                    "FROM " +
//                    "t_eem_excel_page t " +
//                    "GROUP BY " +
//                    "t.tpInputID, " +
//                    "t.reportOrgCode, " +
//                    "t.reportYear, " +
//                    "t.reportDate " +
//                    "HAVING " +
//                    "t.reportOrgCode = '" + orgEntity.getOrgCode() + "' " +
//                    "AND reportYear = '" + year + "' " +
//                    "AND reportDate = '" + dateStr + "'";
            sql = "SELECT \n" +
                    "  a.reportOrgCode,\n" +
                    "\ta.tpInputID,\n" +
                    "\ta.tpInputName,\n" +
                    "a.WORK_ORDER_STATUS WORKORDERSTATUS, (CASE a.WORK_ORDER_STATUS\n" +
                    "WHEN '已审核' THEN 1\n" +
                    "WHEN '未审核' THEN 0\n" +
                    "ELSE NULL END) num\n" +
                    "FROM(\n" +
                    "SELECT\n" +
                    "\tt.reportOrgCode,\n" +
                    "\tt.tpInputID,\n" +
                    "\tt.tpInputName,\n" +
                    "  t.CREATION_TIME,\n" +
                    "  t.WORK_ORDER_STATUS\n" +
                    "FROM\n" +
                    "\tt_eem_excel_page t\n" +
                    "where\n" +
                    "\tt.reportOrgCode = '" + orgEntity.getOrgCode() + "%'\n" +
                    "AND reportYear = '" + year + "' \n" +
                    "AND reportDate = '" + dateStr + "'\n" +
                    "ORDER BY t.tpInputID,t.reportOrgCode) a ,\n" +
                    "(SELECT\n" +
                    "\tt.reportOrgCode,\n" +
                    "\tt.tpInputID,\n" +
                    "  MAX(t.CREATION_TIME) sj\n" +
                    "\n" +
                    "FROM\n" +
                    "\tt_eem_excel_page t\n" +
                    "where\n" +
                    "\tt.reportOrgCode LIKE '" + orgEntity.getOrgCode() + "%'\n" +
                    "AND reportYear = '" + year + "' \n" +
                    "AND reportDate = '" + dateStr + "'\n" +
                    "GROUP BY t.reportOrgCode,t.tpInputID\n" +
                    "ORDER BY t.tpInputID,t.reportOrgCode) b\n" +
                    "where a.tpInputID = b.tpInputID\n" +
                    "and a.reportOrgCode=b.reportOrgCode\n" +
                    "anda.CREATION_TIME = b.sj";
        }
       /* if (StringUtils.isNotBlank(dateStr)) {
            hql.append(" and reportDate='" + dateStr + "'");
        }
        if (StringUtils.isNotBlank(year)) {
            hql.append(" and reportYear='" + year + "'");
        }*/
        try {
            excelPageList = baseDAO.findNativeSQL(sql, null);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return excelPageList;
    }

    @Override
    public List<EvaluationReportExcel> findEvaluationReportExcelByYearAndDate(String reportYear, String reportDate) {
        StringBuffer hql = new StringBuffer("from EvaluationReportExcel where reportYear='" + reportYear + "'");
        try {
            if (StringUtils.isNotBlank(reportDate)) {
                hql.append(" and reportDate='" + reportDate + "'");
            }
            return baseDAO.find(hql.toString());
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<EvaluationReportExcel> findEvaluationReportExcelByNew4Q() {
        List<EvaluationReportExcel> reportExcelList = new ArrayList<EvaluationReportExcel>();
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String hql = "";
        if (month == 1 || month == 2 || month == 3) {//上年第四季度
            hql = "from EvaluationReportExcel where (reportYear='" + (year - 1) + "' and reportDate='第四季度') or (reportYear='" + (year - 1) + "' and reportDate='第三季度') or (reportYear='" + (year - 1) + "' and reportDate='第二季度') or (reportYear='" + (year - 1) + "' and reportDate='第一季度')";
        }
        if (month == 4 || month == 5 || month == 6) {//当年第一季度
            hql = "from EvaluationReportExcel where (reportYear='" + year + "' and reportDate='第一季度') or (reportYear='" + (year - 1) + "' and reportDate='第四季度') or (reportYear='" + (year - 1) + "' and reportDate='第三季度') or (reportYear='" + (year - 1) + "' and reportDate='第二季度')";
        }
        if (month == 7 || month == 8 || month == 9) {//当年第二季度
            hql = "from EvaluationReportExcel where (reportYear='" + year + "' and reportDate='第二季度') or (reportYear='" + year + "' and reportDate='第一季度') or (reportYear='" + (year - 1) + "' and reportDate='第四季度') or (reportYear='" + (year - 1) + "' and reportDate='第三季度')";
        }
        if (month == 10 || month == 11 || month == 12) {//当年第三季度
            hql = "from EvaluationReportExcel where (reportYear='" + year + "' and reportDate='第三季度') or (reportYear='" + year + "' and reportDate='第二季度') or (reportYear='" + year + "' and reportDate='第一季度') or (reportYear='" + (year - 1) + "' and reportDate='第四季度')";
        }
        try {
            reportExcelList = baseDAO.find(hql);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return reportExcelList;
    }

    @Override
    public Pager findTimelyRateInfo(Pager pager, String provinceCode, String reportDate) {
        int year = Calendar.getInstance().get(Calendar.YEAR);


        try {
            String[] arr = reportDate.split("-");
            String time = "";
            String hql = "";


            System.out.println("arr[1]="+arr[1]+"-----------------------------");

            if (StringUtils.isNotBlank(reportDate)) {
//                if(reportDate.indexOf("+")!=-1) {
                if (arr[1].equals("全年")) {
                }
                else {
                    time = arr[0] + "-01-20 00:00:00";
                    hql = "from ExcelPage where reportOrgCode='" + provinceCode + "' and reportYear='" + arr[0] + "' and reportDate='" + arr[1] + "' and creationTime>='" + time + "'";
                        System.out.println("hql===="+hql);

//                    } else if (arr[1].equals("下半年")) {
//                    time = arr[0] + "-07-20 00:00:00";
//                    hql = "from ExcelPage where reportOrgCode='" + provinceCode + "' and reportYear='" + arr[0] + "' and reportDate='" + arr[1] + "' and creationTime>='" + time + "'";
                } /*else if (arr[1].equals("第三季度")) {
                time = arr[0] + "-11-01 00:00:00";
                hql = "from ExcelPage where reportOrgCode='" + provinceCode + "' and reportYear='" + arr[0] + "' and reportDate='" + arr[1] + "' and creationTime>='" + time + "'";
            } else if (arr[1].equals("第四季度")) {
                time = (Integer.valueOf(arr[0]) + 1) + "-02-01 00:00:00";
                hql ="from ExcelPage where reportOrgCode='" + provinceCode + "' and reportYear='" + arr[0] + "' and reportDate='" + arr[1] + "' and creationTime>='" + time + "'";
            } else if (arr[1].equals("全年")) {
                String time1 = arr[0] + "-05-01 00:00:00";
                String time2 = arr[0] + "-05-01 00:00:00";
                String time3 = arr[0] + "-05-01 00:00:00";
                String time4 = (Integer.valueOf(arr[0]) + 1) + "-02-01 00:00:00";
                hql = "from ExcelPage where reportOrgCode='" + provinceCode + "' and reportYear='" + arr[0] + "' and reportDate='第一季度' and creationTime>='" + time1 + "' UNION " +
                        "from ExcelPage where reportOrgCode='" + provinceCode + "' and reportYear='" + arr[0] + "' and reportDate='第二季度' and creationTime>='" + time2 +"' UNION " +
                        "from ExcelPage where reportOrgCode='" + provinceCode + "' and reportYear='" + arr[0] + "' and reportDate='第三季度' and creationTime>='" + time3 + "' UNION " +
                        "from ExcelPage where reportOrgCode='" + provinceCode + "' and reportYear='" + arr[0] + "' and reportDate='第四季度' and creationTime>='" + time4 + "'";
            }*/
            } else {
//                int year = Calendar.getInstance().get(Calendar.YEAR);
                int month = Calendar.getInstance().get(Calendar.MONTH);
                String monthParam = "下半年";
                if (month > 8 || month < 3) {
                    monthParam = "上半年";
                    if (month < 3) {
                        year = year - 1;
                    }
                } else {
                    monthParam = "下半年";
                }
                time = year + "-05-01 00:00:00";
                hql = "from ExcelPage where reportOrgCode='" + provinceCode + "' and reportYear='" + year + "' and reportDate='" + monthParam + "'";
            }

            System.out.println("hql===="+hql);
            pager = baseDAO.getPageByHql(hql, pager, null);
            for (ExcelPage excelPage : (List<ExcelPage>) pager.getExhibitDatas()) {
                excelPage.setDateGrading(excelPage.getReportYear() + "-" + excelPage.getReportDate());
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        pager.setIsSuccess(true);
        int pageSize = pager.getPageSize();
        int recordCount = pager.getRecordCount();
        int pageCount = pager.getPageCount();
        pageCount = recordCount / pageSize + (recordCount % pageSize > 0 ? 1 : 0);
        pager.setPageCount(pageCount);
        return pager;
    }

    @Override
    public Pager findAccuracyRateInfo(Pager pager, String provinceCode, String reportDate) {
        try {
            if (pager.getParameters().size() > 0) {
                reportDate = pager.getParameters().get("reportDate").toString();
                provinceCode = pager.getParameters().get("provinceCode").toString();
            }
            String[] arr = reportDate.split("-");
            String sql = "";
            if (arr[1].equals("全年")) {
                sql = "SELECT " +
                        "teep.pageid objectId, " +
                        "teep.tpInputName, " +
                        "teep.fileName, " +
                        "teep.OPER_USER_TRUE_NAME operUserTrueName, " +
                        "teep.OPER_ORG_NAME operOrgName, " +
                        "teep.OPER_USER_PHONE operUserPhone, " +
                        "teep.reportYear, " +
                        "teep.reportDate, " +
                        "teep.CREATION_TIME creationTime, " +
                        "tegj.OPER_TIME rejectTime, " +
                        "teep.rejectNum, " +
                        "tegj.OPER_DESC signInfo " +
                        "FROM " +
                        "t_eem_excel_page teep " +
                        "LEFT JOIN t_eom_general_info tegj ON teep.pageid = tegj.PROCESSING_OBJECT_ID " +
                        "AND tegj.PROCESSING_OBJECT_TABLE = 't_eem_excel_page' " +
                        "WHERE " +
                        "teep.reportOrgCode='" + provinceCode + "' and teep.reportYear='" + arr[0] + "' and teep.rejectNum>0 ";
            } else {
                sql = "SELECT " +
                        "teep.pageid objectId, " +
                        "teep.tpInputName, " +
                        "teep.fileName, " +
                        "teep.OPER_USER_TRUE_NAME operUserTrueName, " +
                        "teep.OPER_ORG_NAME operOrgName, " +
                        "teep.OPER_USER_PHONE operUserPhone, " +
                        "teep.reportYear, " +
                        "teep.reportDate, " +
                        "teep.CREATION_TIME creationTime, " +
                        "tegj.OPER_TIME rejectTime, " +
                        "teep.rejectNum, " +
                        "tegj.OPER_DESC signInfo " +
                        "FROM " +
                        "t_eem_excel_page teep " +
                        "LEFT JOIN t_eom_general_info tegj ON teep.pageid = tegj.PROCESSING_OBJECT_ID " +
                        "AND tegj.PROCESSING_OBJECT_TABLE = 't_eem_excel_page' " +
                        "WHERE " +
                        "teep.reportOrgCode='" + provinceCode + "' and teep.reportDate='" + arr[1] + "' and teep.reportYear='" + arr[0] + "' and teep.rejectNum>0 ";
            }
            pager = baseDAO.findNativeSQL(sql, null, pager);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        pager.setIsSuccess(true);
        int pageSize = pager.getPageSize();
        int recordCount = pager.getRecordCount();
        int pageCount = pager.getPageCount();
        pageCount = recordCount / pageSize + (recordCount % pageSize > 0 ? 1 : 0);
        pager.setPageCount(pageCount);
        return pager;
    }

    @Override
    public List<ExcelPage> findExcelPages(Long tempID, String deptCode, String reportYear, String reportDate) {
        try {
            List<ExcelPage> excelPageList = baseDAO.find("from ExcelPage where tpInputID=" + tempID + " and reportOrgCode='" + deptCode + "' and reportYear='" + reportYear + "' and reportDate='" + reportDate + "'");
            return excelPageList;
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String saveOrg(HttpServletRequest request, RepotOrg repotOrg, UserEntity userEntity) {
        String orgIds = repotOrg.getAttribute1();
        try {
            String[] arrOrgs = orgIds.split(",");
            Set set = new HashSet();
            for (String org : arrOrgs) {
                RepotOrg og = new RepotOrg();
                OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgID(Long.parseLong(org.split(":")[0]));

                StringBuilder sql = new StringBuilder("delete from t_eem_repot_org where 1=1 ");
                int num = 0;
                sql.append(" and orgId='" + orgEntity.getOrgId() + "'");
                num = baseDAO.executeSql(sql.toString());
                logger.info("删除账号" + orgEntity.getOrgId() + "," + num + "条数据");
                og.setCityCode(orgEntity.getOrgCode());
                og.setOrgId(orgEntity.getOrgId().toString());
                og.setFullOrgName(orgEntity.getFullOrgName());
                og.setCityName(orgEntity.getOrgName());
                og.setProvinceCode(orgEntity.getProCode());
                og.setProvinceName(AAAAAdapter.getInstence().findOrgByOrgCode(orgEntity.getProCode()).getOrgName());
                og.setType(orgEntity.getOrgType());

                og.setCreatedBy(userEntity.getUserId());
                og.setCreateTrueName(userEntity.getTrueName());
                og.setCreateUserName(userEntity.getUserName());
                og.setCreationTime(new Timestamp(new Date().getTime()));
                long pId = baseDAO.getSequenceNextValue(RepotOrg.class);
                og.setObjectId(pId);
                set.add(og);
            }
            baseDAO.saveOrUpdateAll(set);
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getMangerOrgNams(UserEntity userEntity) {
        String sql = "SELECT fullOrgName from t_eem_repot_org where PROVINCE_CODE=" + userEntity.getOrgEntity().getProCode() + ";";
        StringBuffer buffer = new StringBuffer();
        try {
            List<Map> list = baseDAO.findNativeSQL(sql, null);
            for (Map map : list) {
                buffer.append(map.get("fullorgname") + ",");
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        if ("".equals(buffer.toString())) {
            return "";
        }
        return buffer.toString().substring(0, buffer.toString().length() - 1);
    }

    @Override
    public List<EemTempEntity> findTempListToDevice(String type, UserEntity userEntity) throws ServiceException {
        try {
            if (type.equals("report")) {
                String hql = "select new EemTempEntity(objectId,tempName,  shortName,  reportedFrequency,  tempType,tempPattern, attribute1,level, applyId) from EemTempEntity where deletedFlag=0 and tempType=1";
                return baseDAO.find(hql);
            }

        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //查找厂商列表
    @Override
    public List queryDevice(Integer vendorId, String vendorName, String type) throws ServiceException {
        try {

            String sql = "";
            if (type.equals("甲")) {
                sql = "select VENDOR_ID,VENDOR_NAME from t_eem_vendor_info info,t_eem_report re where info.CATEGORY=re.type and info.CATEGORY=\"甲\";";

            } else if (type.equals("乙")) {
                sql = "select VENDOR_ID,VENDOR_NAME from t_eem_vendor_info info,t_eem_report re where info.CATEGORY=re.type and info.CATEGORY=\"乙\";";
            } else {
                sql = "select VENDOR_ID,VENDOR_NAME from t_eem_vendor_info info,t_eem_report re where info.CATEGORY=re.type and info.CATEGORY=\"丙\";";

            }
            List<Map> vendorEntityList = baseDAO.findNativeSQL(sql, null);
//           if(vendorEntityList.size()>0){
//               String vendorNames="";
//               for(Map map : vendorEntityList){
//                   vendorNames+="'"+map.get("vendor_name")+"',";
//               }
//           }
            return vendorEntityList;
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;

    }

    //查找报表列表
  /*  @Override
    public List querySheet(Long id, String sheetName, String type) throws ServiceException {
        try {
            *//*String sql="";
            if(type.equals("甲")){
                 sql="select id,sheetName from t_eem_report where type='甲';";

            }else if(type.equals("乙")){
                 sql="select id,sheetName from t_eem_report where type='乙';";
            }else{
                sql="select id,sheetName from t_eem_report where type='丙';";

            }*//*
            String sql = "select id,sheetName from t_eem_report where type='甲';";
            List<Map> sheetEntityList = baseDAO.findNativeSQL(sql, null);
            return sheetEntityList;
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;

    }*/

    @Override
    public String findVenders(String type, UserEntity userEntity) {

        if (type.equals("2")) {
            type = "乙";
        } else if (type.equals("3")) {
            type = "丙";
        } else {
            type = "甲";
        }
        String sql = "SELECT t.shortName from t_eem_report t where  t.type='" + type + "'";
        String str = "";
        try {
            List<Map> venders = baseDAO.findNativeSQL(sql, null);
            if (venders.size() > 0) {
                for (Map map : venders) {
                    str += map.get("shortname").toString() + ",";
                }
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        if (StringUtils.isNotBlank(str)) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    @Override
    public void saveApply(EemApply apply, UserEntity userEntity) {
        EemApply apply1 = null;
        try {
            apply1 = (EemApply) baseDAO.get(EemApply.class, apply.getObjectId());
            apply1.setAuditReason(apply.getAuditReason());
            apply1.setResult(apply.getResult());
            apply1.setAuditUserTrueName(userEntity.getTrueName());
            apply1.setAuditOrgName(userEntity.getOrgName());
            apply1.setAuditUserName(userEntity.getUserName());
            apply1.setAutidTime(new Timestamp(new Date().getTime()));
            if (apply1.getResult().equals("同意")) {
                ExcelPage excelPage = (ExcelPage) baseDAO.get(ExcelPage.class, apply1.getPageId());
                excelPage.setIswithdraw("Y");
                baseDAO.saveOrUpdate(excelPage);
            }
            baseDAO.saveOrUpdate(apply1);
        } catch (DAOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Pager queryVengers(Pager pager, String type, UserEntity userEntity) {
        try {
            if (type.equals("2")) {
                type = "乙";
            } else if (type.equals("3")) {
                type = "丙";
            } else {
                type = "甲";
            }
            List<Map> mlist = baseDAO.findNativeSQL("SELECT t.SHORT_NAME1,t.VENDOR_NAME from t_eem_vendor_info t where t.CATEGORY='" + type + "'", null);
            List<Map> vlist2 = baseDAO.findNativeSQL("SELECT t.shortName,t.venderName from t_eem_report t  where t.type='" + type + "'", null);

            for (Map map : mlist) {
                int i = 0;
                for (Map map2 : vlist2) {

                    if (map2.get("vendername").toString().contains(map.get("short_name1").toString())) {
                        map.put("a" + i, "√");
                    } else {
                        map.put("a" + i, " ");
                    }
                    i = i + 1;
                }
                map.get("shortname");

            }
//        pager = baseDAO.getPageByHql(hql.toString(), pager, queryMap);

//        for (Object excelPage : pager.getExhibitDatas()) {
//            ExcelPage excelPage1 = (ExcelPage) excelPage;
////            excelPage1.setDateGrading(excelPage1.getReportYear()+">"+excelPage1.getReportDate());
//            ((ExcelPage) excelPage).setDateGrading(excelPage1.getReportYear() + "-" + excelPage1.getReportDate());
//        }
            pager.setExhibitDatas(mlist);
            pager.setIsSuccess(true);
            int pageSize = pager.getPageSize();
            int recordCount = pager.getRecordCount();
            int pageCount = pager.getPageCount();
            pageCount = recordCount / pageSize + (recordCount % pageSize > 0 ? 1 : 0);
            pager.setPageCount(pageCount);
            return pager;
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Map> initManagerTemplate(String temp, UserEntity userEntity) {
        try {
            String sql = "SELECT t.OBJECT_ID objectid,t.TEMP_NAME tempname,t.SHORT_NAME shortname from t_eom_temp_info t where  t.DELETED_FLAG=FALSE and t.TEMP_TYPE=1";
            if (StringUtils.isNotBlank(temp)) {
                sql += " and OBJECT_ID=" + temp;
            }
            List<Map> list = baseDAO.findNativeSQL(sql, null);
            return list;
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public List<Tempt> initManagerTemplate2(String temp, UserEntity userEntity) {
        try {
            List<Tempt> tList = new ArrayList<Tempt>();
            String sql = "SELECT t.OBJECT_ID objectid,t.TEMP_NAME tempname,t.SHORT_NAME shortname from t_eom_temp_info t where  t.DELETED_FLAG=FALSE and t.TEMP_TYPE=1";
            if (StringUtils.isNotBlank(temp)) {
                sql += " and OBJECT_ID=" + temp;
            }
            List<TemptSpec> temptSpecList = baseDAO.find("from TemptSpec ");
            Map<String,Integer> temptSpecMap = new HashMap<String, Integer>();
            Iterator<TemptSpec> specIterator = temptSpecList.iterator();
            while (specIterator.hasNext()){
                TemptSpec ob = specIterator.next();
                temptSpecMap.put(ob.getTempId()+"-"+ob.getSpecialtyId(),1);
            }
            List<Map> slist = baseDAO.findNativeSQL("SELECT t.specialtyid objectid,t.SPECIALTYCODE scode,t.SPECIALTYNAME sname  from metar_specialtyinfo t  where t.deleteflag=FALSE", null);
            List<Specialty> specialtySet = new ArrayList<Specialty>();
            for(Map map:slist){
                Specialty specialty = new Specialty();
                specialty.setObjectId(Long.parseLong(map.get("objectid").toString()));
                specialty.setScode(map.get("scode").toString());
                specialty.setSname(map.get("sname").toString());
                specialtySet.add(specialty);
            }
            List<Map> list = baseDAO.findNativeSQL(sql, null);
            for(Map map:list){
                List<Specialty> copy = new ArrayList<Specialty>(specialtySet.size());


                Tempt tempt = new Tempt();
                tempt.setObjectId(Long.parseLong(map.get("objectid").toString()));
                tempt.setTempName(map.get("tempname").toString());
                tempt.setShortName(map.get("shortname").toString());

                Iterator<Specialty> iterator = specialtySet.iterator();
                while(iterator.hasNext()){
                    Specialty specialty = (Specialty)iterator.next().clone();
                    if(temptSpecMap.containsKey(tempt.getObjectId()+"-"+specialty.getObjectId())){
                        specialty.setChecked(true);
                    }
                    copy.add(specialty);
                }
                tempt.setNodeVoSet(copy);
                tList.add(tempt);

            }
          return   tList;
        } catch (DAOException e) {
            e.printStackTrace();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public List<Map> findSpecialtys(UserEntity userEntity) {
        try {
            List<Map> list = baseDAO.findNativeSQL("SELECT t.specialtyid objectid,t.SPECIALTYCODE scode,t.SPECIALTYNAME sname  from metar_specialtyinfo t  where t.deleteflag=FALSE", null);
            return list;
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void saveTemptSpecInfo(UserEntity userEntity, String nodeSet) {
        try {
            int num=0;
            if (StringUtils.isNotBlank(nodeSet)) {
                String[] entitys = nodeSet.split(",");
                Timestamp timestamp = new Timestamp(new Date().getTime());
                List<TemptSpec> list = new ArrayList<TemptSpec>();
                Map<Long,String> temptMap = new HashMap<Long, String>();

                for (String str : entitys) {
                    String[] propertys = str.split(":");
                    TemptSpec temptSpec = new TemptSpec();
                    temptSpec.setObjectId(baseDAO.getSequenceNextValue(TemptSpec.class));
                    temptSpec.setTempId(Long.parseLong(propertys[0]));//2:5:PF0101:传输:BSC
                    temptMap.put(temptSpec.getTempId(),"");
                    temptSpec.setTempName(propertys[4]);

                    temptSpec.setSpecialtyName(propertys[3]);
                    temptSpec.setSpecialtyCode(propertys[2]);
                    temptSpec.setSpecialtyId(Long.parseLong(propertys[1]));

                    temptSpec.setCreationTime(timestamp);
                    temptSpec.setCreatedBy(userEntity.getUserId());
                    temptSpec.setCreateTrueName(userEntity.getTrueName());
                    temptSpec.setCreateUserName(userEntity.getUserName());
                    list.add(temptSpec);
                }
                if(list.size()>0){
                    StringBuilder detelSql = new StringBuilder("delete from t_eem_temp_specialty where 1=1 ");
                    StringBuilder tempIds = new StringBuilder();
                    for(Long tempId:temptMap.keySet()){
                        tempIds.append(tempId+",");
                    }
                    detelSql.append(" and tempId in("+tempIds.toString().substring(0, tempIds.length()-1)+")");
                   num= baseDAO.executeSql(detelSql.toString());
                    logger.info(userEntity.getUserName() + "删除账号2"  + "," + num + "条流数据");
                    baseDAO.saveOrUpdateAll(list);
                }

            }else {
                num= baseDAO.executeSql("delete from t_eem_temp_specialty where 1=1 ");
                logger.info(userEntity.getUserName() + "删除账号2"  + "," + num + "条流数据");
            }


    } catch (DAOException e) {
            e.printStackTrace();
        }
        System.out.println(nodeSet);
    }

    @Override
    public String findTempIdsByNodeId(String userName, String nodeID) {
        String specitys = PowerUtil.getInstence().findSpecialtyByNodeId(userName, nodeID);
        String tempIds="";
        if(StringUtils.isNotBlank(specitys)){
            List<Map> list = null;
            try {
                list = baseDAO.findNativeSQL("SELECT DISTINCT(t.tempId) from t_eem_temp_specialty t where t.specialtyCode  in (" + specitys + ")", null);
            } catch (DAOException e) {
                e.printStackTrace();
            }
            for(Map map:list){
                tempIds+=map.get("tempid")+",";
            }
            if(StringUtils.isNotBlank(tempIds)){
                tempIds = tempIds.substring(0,tempIds.length()-1);
            }else{
                tempIds="-1";//-1 其专业代表没有模板
            }
            if(specitys.contains("ALL")){
               tempIds="0";//0 代表所有模板
            }

        }
        return tempIds;
    }

    @Override
    public String saveApply(String operdesc, String id, UserEntity userEntity) {
        EemApply apply = new EemApply();
        try {
            ExcelPage page = (ExcelPage) baseDAO.get(ExcelPage.class, Long.parseLong(id));
            apply.setObjectId(baseDAO.getSequenceNextValue(EemApply.class));
            apply.setReportUserName(userEntity.getUserName());
            apply.setReportUserTrueName(userEntity.getTrueName());
            apply.setReason(operdesc);
            apply.setPageId(page.getObjectId());

            apply.setReportYear(page.getReportYear());
            apply.setPageName(page.getPageName());
            apply.setReportDate(page.getReportDate());

            apply.setTpInputID(page.getTpInputID());
            apply.setTpInputName(page.getTpInputName());
            apply.setCreationTime(new Timestamp(new Date().getTime()));

            apply.setReportOrgCode(page.getReportOrgCode());
            apply.setReportOrgName(page.getOperOrgName());
            apply.setApplyId(page.getApplyId());

            baseDAO.saveOrUpdate(apply);
            page.setDisID(1);
            page.setDeletedFlag(true);
            baseDAO.saveOrUpdate(page);


        } catch (DAOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Pager queryApplyList(Pager pager, Integer type, UserEntity userEntity, long sign ,String tempIds) {

        try {//type 1:待办申请  2：已办申请  3：已申请重新上报
            OrgEntity orgEntity = AAAAAdapter.getCompany(userEntity.getOrgID().intValue());
            String userType = userEntity.getCategory();
            String yearCon = null;
            String reportDateCon = null;
            Map<String, Object> params = pager.getParameters();
            if(!pager.getParameters().isEmpty()){
                yearCon = pager.getParameters().get("year").toString();
                reportDateCon = pager.getParameters().get("reportDate").toString();
            }
            StringBuffer hql = new StringBuffer("from EemApply where objectId>0");
           // if(yearCon != null){
            if(StringUtils.isNotBlank(yearCon)){

                hql.append(" and reportYear='" + yearCon + "'");
            }
           // if(reportDateCon != null){
            if(StringUtils.isNotBlank(reportDateCon)){
                hql.append(" and reportDate='" + reportDateCon + "'");
            }
            if (type == 3) {
                hql.append(" and reportUserName='" + userEntity.getUserName() + "'");
            }
            if (type == 2) {
                hql.append(" and auditUserName='" + userEntity.getUserName() + "'");
                hql.append(" and result!=null");
                //
                if (params.get("deptIds") != null && StringUtils.isNotBlank(params.get("deptIds").toString())) {
                    hql.append(" and reportOrgCode in(" + params.get("deptIds").toString() + ")");
                }     //jw3.7省份查询框
            }
            if (type == 1) {
                hql.append(" and result=null");
                if (userType.equals("UNI")) {
                   // hql.append(" and applyId=1");
                    //jw 3.7  //////////////省份查询框
                    if (params.get("deptIds") != null && StringUtils.isNotBlank(params.get("deptIds").toString())) {
                        hql.append(" and applyId=1 and reportOrgCode in(" + params.get("deptIds").toString() + ")");
                    } else {
                        hql.append(" and applyId=1");
                    }
                    //  ///////////////////////
                } else {
                    hql.append(" and applyId=2");
                    hql.append(" and reportOrgCode like '" + orgEntity.getOrgCode() + "%' ");


                }
            }
            //  jw  3.2
          //  Map<String, Object> params = pager.getParameters();
            if (params.size() > 0) {
                if (params.get("tempId") != null && StringUtils.isNotBlank(params.get("tempId").toString())) {
                    hql.append(" and tpInputID = " + params.get("tempId"));
                } else if (!tempIds.equals("0")) {
                    hql.append(" and tpInputID in (" + tempIds + ")");
                }
            }/*else if(!tempIds.equals("0")){
                hql.append(" and tpInputID in ("+tempIds+")");
            }*/
            //    用于选项框限定模板
            Map<String, Object> queryMap = new HashMap<String, Object>();


            int nowNumber = pager.getNowPage();
            pager = baseDAO.getPageByHql(hql.toString(), pager, queryMap);
            pager.setNowPage(nowNumber);
        } catch (DAOException e) {
            e.printStackTrace();
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        }
        pager.setIsSuccess(true);
        int pageSize = pager.getPageSize();
        int recordCount = pager.getRecordCount();
        int pageCount = pager.getPageCount();
        pageCount = recordCount / pageSize + (recordCount % pageSize > 0 ? 1 : 0);
        pager.setPageCount(pageCount);
        return pager;
    }

    @Override
    public EemApply getEemApplyById(long l) {
        try {
            return (EemApply) baseDAO.get(EemApply.class, l);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }

}