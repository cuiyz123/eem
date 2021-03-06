package com.metarnet.eomeem.service.impl;

import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.Pager;
import com.metarnet.eomeem.model.DeviceEntity;
import com.metarnet.eomeem.model.ExcelPage;
import com.metarnet.eomeem.service.IEemQueryService;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/14.
 */
@Service
public class EemQueryServiceImpl implements IEemQueryService {
    @Resource
    private IBaseDAO baseDAO;

    @Override
    public Pager queryDataList(Pager pager, String type, UserEntity userEntity,String tempIds) throws ServiceException {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        String monStr="";
        if(month<=9){
            monStr="上半年";
        }else{
            monStr="下半年";
        }

        try {
            OrgEntity orgEntity = AAAAAdapter.getCompany(userEntity.getOrgID().intValue());
            //默认查看当前年份当前时间的数据
//            StringBuffer hql = new StringBuffer("from ExcelPage where deletedFlag=0 and reportYear="+year+" and reportDate="+"'"+monStr+"'");
            StringBuffer hql = new StringBuffer("from ExcelPage where deletedFlag=0 ");

            Map<String, Object> params = pager.getParameters();
            if (params.size() > 0) {
                if (params.get("tempId") != null && StringUtils.isNotBlank(params.get("tempId").toString())) {
                    hql.append(" and tpInputID = " + params.get("tempId"));
                }else if(!tempIds.equals("0")){
                    hql.append(" and tpInputID in(" +tempIds+")");
                }
                if (params.get("year") != null && StringUtils.isNotBlank(params.get("year").toString())) {
                    hql.append(" and reportYear = '" + params.get("year") + "'");
                }
                if (params.get("reportDate") != null && StringUtils.isNotBlank(params.get("reportDate").toString())) {
                    String reportDate = params.get("reportDate").toString();
                    hql.append(" and reportDate ='" + reportDate + "'");
                }
                String queryType = String.valueOf(params.get("type"));
                if (!queryType.equals("null") && StringUtils.isNotBlank(queryType)) {
                    if (queryType.equals("city")) {//查询地市上报数据
                        if (userEntity.getCategory().equals("PRO")) {
//                          hql.append(" and reportOrgCode like '" + orgEntity.getOrgCode() + "%' and reportOrgCode!='" + orgEntity.getOrgCode() + "'");
                            hql.append("  and  reportOrgCode like '" + orgEntity.getOrgCode() + "%'and workOrderStatus!='审核退回' and APPLY_ID=2");
                        } else if (userEntity.getCategory().equals("CITY")) {
                            hql.append("  and reportOrgCode like '" + orgEntity.getOrgCode() + "%'");
                        }
                    }
                    if (queryType.equals("pro")) {//查询省分上报数据
                        if (userEntity.getCategory().equals("UNI")) {
                            hql.append(" and applyId=1 and  length(reportOrgCode)=3 and workOrderStatus!='审核退回'");
                            if (params.get("deptIds") != null && StringUtils.isNotBlank(params.get("deptIds").toString())) {
                                hql.append(" and reportOrgCode in(" + params.get("deptIds").toString() + ")");
                            }
                        } else {
                            hql.append(" and applyId=1 and reportOrgCode='" + orgEntity.getOrgCode() + "'");
                        }
                    }/*else if (queryType.equals("city")){
                        hql.append(" and applyId=2 ");
                    }*/
                }
                pager = baseDAO.getPageByHql(hql.toString(), pager, null);
                for (ExcelPage excelPage : (List<ExcelPage>) pager.getExhibitDatas()) {
                    if(excelPage.getAuditSameLevel()==true&&excelPage.getOperUserId().equals(userEntity.getUserId())&&excelPage.getDisID()==0){
                        if(excelPage.getWorkOrderStatus()=="审核退回"&&excelPage.getRejectNum()!=null){
                            excelPage.setTheme("3");
                        }else if(excelPage.getRejectNum()==null){
                            excelPage.setTheme("1");
                        }
                    }else if(excelPage.getAuditSameLevel()==true&&excelPage.getOperUserId().equals(userEntity.getUserId())&&excelPage.getDisID()==1){
                        excelPage.setTheme("2");
                    }
                    excelPage.setDateGrading(excelPage.getReportYear() + "-" + excelPage.getReportDate());
                }
            }
        } catch (Exception e) {
            throw new ServiceException(e);
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
    public Pager queryDataListAll(Pager pager, String type, UserEntity userEntity) throws ServiceException {
        List<ExcelPage> excelPageList = new ArrayList<ExcelPage>();
        try {
            OrgEntity orgEntity = AAAAAdapter.getCompany(userEntity.getOrgID().intValue());
            StringBuffer hql = new StringBuffer("from ExcelPage where deletedFlag=0 ");
            Map<String, Object> params = pager.getParameters();
            if (params.size() > 0) {
                if (params.get("tempId") != null && StringUtils.isNotBlank(params.get("tempId").toString())) {
                    hql.append(" and tpInputID = " + params.get("tempId"));
                }
                if (params.get("year") != null && StringUtils.isNotBlank(params.get("year").toString())) {
                    hql.append(" and reportYear = '" + params.get("year") + "'");
                }
                if (params.get("reportDate") != null && StringUtils.isNotBlank(params.get("reportDate").toString())) {
                    String reportDate = params.get("reportDate").toString();
                    hql.append(" and reportDate ='" + reportDate + "'");
                }
                String queryType = String.valueOf(params.get("type"));
                if (!queryType.equals("null") && StringUtils.isNotBlank(queryType)) {
                    if (queryType.equals("city")) {//查询地市上报数据
                        if (userEntity.getCategory().equals("PRO")) {
                            hql.append(" and reportOrgCode like '" + orgEntity.getOrgCode() + "%' and reportOrgCode!='" + orgEntity.getOrgCode() + "'");
                        } else if (userEntity.getCategory().equals("CITY")) {
                            hql.append(" and reportOrgCode like '" + orgEntity.getOrgCode() + "%'");
                        }
                    }
                    if (queryType.equals("pro")) {//查询省分上报数据
                        if (userEntity.getCategory().equals("UNI")) {
                            hql.append(" and length(reportOrgCode)=3 ");
                            if (params.get("deptIds") != null && StringUtils.isNotBlank(params.get("deptIds").toString())) {
                                hql.append(" and reportOrgCode in(" + params.get("deptIds").toString() + ")");
                            }
                        } else {
                            hql.append(" and reportOrgCode='" + orgEntity.getOrgCode() + "'");
                        }
                    }
                }
                excelPageList = baseDAO.find(hql.toString());
                for (ExcelPage excelPage : excelPageList) {
                    excelPage.setDateGrading(excelPage.getReportYear() + "-" + excelPage.getReportDate());
                }
                pager.setExhibitDatas(excelPageList);
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        } finally {
            pager.setIsSuccess(true);
            pager.setPageCount(excelPageList.size() > 0 ? 1 : 0);
            pager.setRecordCount(excelPageList.size());
            pager.setPageSize(excelPageList.size());
            return pager;
        }
    }

    //整体打包下载
    @Override
    public List<ExcelPage> downAllProReportExcel(String reportYear, String reportData,String tempId) {
        List<ExcelPage> excelPageList = new ArrayList<ExcelPage>();
        try {
            if(StringUtils.isNotBlank(tempId)){
                excelPageList = baseDAO.find("from ExcelPage where deletedFlag=0 and length(reportOrgCode)=3 and reportYear='"+reportYear+"'and reportDate='"+reportData+"' and tpInputID="+Long.parseLong(tempId));
            }else
                excelPageList = baseDAO.find("from ExcelPage where deletedFlag=0 and length(reportOrgCode)=3 and reportYear='"+reportYear+"'and reportDate='"+reportData+"'");
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return excelPageList;
    }

    @Override
    public Pager querySheetList(Pager pager, UserEntity userEntity,Long id) throws ServiceException {
        try {
          String sql =  "SELECT t.deviceName,t.sheetName,t.tpInputID from t_eem_device t";
            if(pager.getParameters().size()>0){
                if(pager.getParameters().get("id")!=null&&pager.getParameters().get("id")!=""){
                    id = Long.parseLong(pager.getParameters().get("id").toString());
                    sql +=" where t.id="+id;
                }
            }
            int nowNumber = pager.getNowPage();
            pager = baseDAO.findNativeSQL( sql, null,pager);
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


    //数据打包下载
    @Override
    public List<ExcelPage> downAllProReportExcel2(String reportYear, String reportData,String tempId,String reportOrgCode) {
        List<ExcelPage> excelPageList = new ArrayList<ExcelPage>();
        try {
            if(StringUtils.isNotBlank(tempId)){
                if(StringUtils.isNotBlank(reportOrgCode)){
                    excelPageList = baseDAO.find("from ExcelPage where deletedFlag=0 and length(reportOrgCode)=3 and reportYear='"+reportYear+"'and reportDate='"+reportData+"' and tpInputID="+Long.parseLong(tempId)+ " and reportOrgCode in (" + reportOrgCode + ")" );
                }else{
                    excelPageList = baseDAO.find("from ExcelPage where deletedFlag=0 and length(reportOrgCode)=3 and reportYear='"+reportYear+"'and reportDate='"+reportData+"' and tpInputID="+Long.parseLong(tempId) );
                }

            }else
                excelPageList = baseDAO.find("from ExcelPage where deletedFlag=0 and length(reportOrgCode)=3 and reportYear='"+reportYear+"'and reportDate='"+reportData+"'");
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return excelPageList;
    }

    @Override
    public List<DeviceEntity> findTempListToDevice1(Long id, UserEntity userEntity) throws ServiceException {
        try {
            String hql = "from DeviceEntity ";
            return baseDAO.find(hql);

        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //查询设备对应的厂家
    @Override
    public List<Map> queryVenderByDevice(long l) {

        try {
            String sql="select r.venderName from t_eem_report r where r.tpInputID="+l;
            List<Map> list = baseDAO.findNativeSQL(sql, null);
            return list ;

        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
