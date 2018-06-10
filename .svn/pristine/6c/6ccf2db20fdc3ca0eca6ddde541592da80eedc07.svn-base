package com.metarnet.eomeem.service.impl;

import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.Pager;
import com.metarnet.eomeem.model.EemNoticeEntity;
import com.metarnet.eomeem.model.EemOrgNoticeEntity;
import com.metarnet.eomeem.service.IEemNoticeService;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by Administrator on 2016/7/6.
 */
@Service
public class EemNoticeServiceImpl implements IEemNoticeService {
    @Resource
    private IBaseDAO baseDAO;

    @Override
    public Pager queryNoticeList(Pager pager, UserEntity userEntity) throws ServiceException {
//        StringBuffer hql = new StringBuffer("from EemNoticeEntity ene left join EemOrgNoticeEntity eon on ene.objectId=eon.noticeID where ene.deletedFlag=0 and ene.overdue=0");
        StringBuffer sql = new StringBuffer("SELECT DISTINCT" +
                " ene.NOTICE_ID, " +
                " ene.THEME, " +
                " ene.OPER_USER_TRUE_NAME, " +
                " ene.OPER_FULL_ORG_NAME, " +
                " ene.START_DATE, " +
                " ene.END_DATE, " +
                " ene.TOP, " +
                " ene.CREATION_TIME, " +
                " ene.DEPT_NAMES, " +
                " ene.OPER_USER_ID,"+
                " ene.OPER_ORG_CODE, "+
                " ene.OPER_DESC "+
                "FROM " +
                " t_eem_notice_info ene  " +
                "LEFT JOIN t_eem_org_notice eon ON ene.NOTICE_ID = eon.noticeID where ene.DELETED_FLAG=0 and ene.OVERDUE=0 ");
//        Map<String, Object> queryMap = new HashMap<String, Object>();
        try {
            Map<String, Object> params = pager.getParameters();
            if (params.size() > 0) {
                if (params.get("noticeTheme") != null && StringUtils.isNotBlank(params.get("noticeTheme").toString())) {
                    sql.append(" and ene.THEME like '%" + params.get("noticeTheme") + "%'");
                }
                if (params.get("noticeCreatePerson") != null && StringUtils.isNotBlank(params.get("noticeCreatePerson").toString())) {
                    sql.append(" and ene.OPER_USER_TRUE_NAME like '%" + params.get("noticeCreatePerson") + "%'");
                }
                if (params.get("noticeCreateDate") != null && StringUtils.isNotBlank(params.get("noticeCreateDate").toString())) {
                    sql.append(" and (ene.CREATION_TIME between '" + params.get("noticeCreateDate") + " 00:00:00'");
                    sql.append(" and '" + params.get("noticeCreateDate") + " 23:59:59')");
                }
            }
            if(userEntity.getCategory().equals("PRO")||userEntity.getCategory().equals("CITY")){
                String orgCode = userEntity.getOrgEntity().getOrgCode();
                sql.append(" and eon.orgCode='"+orgCode.substring(0,3)+"'");
            }
            sql.append(" order by ene.TOP desc,ene.LAST_UPDATE_TIME desc");
            pager = baseDAO.findNativeSQL(sql.toString(), null, pager);
            for (Map map : (List<Map>)pager.getExhibitDatas()) {
                StringBuffer editHtml = new StringBuffer("<a href=javascript:; onclick='showNotice(" + map.get("notice_id") + ")'>查看</a>");
                if (userEntity.getAdmin()) {//是管理员
                    if ("root".equals(userEntity.getUserName()) || map.get("oper_org_code").toString().startsWith(userEntity.getOrgCode())) {
                        editHtml.append("&nbsp;&nbsp;<a href=javascript:; onclick='updateNotice(" + map.get("notice_id") + ")'>修改</a>");
                        editHtml.append("&nbsp;&nbsp;<a href=javascript:; onclick='delNotice(" + map.get("notice_id") + ")'>删除</a>");
                        if (map.get("top").toString().equals("true")) {
                            editHtml.append("&nbsp;&nbsp;<a href=javascript:; onclick='topNotice(" + map.get("notice_id") + ")'>取消置顶</a>");
                        } else {
                            editHtml.append("&nbsp;&nbsp;<a href=javascript:; onclick='topNotice(" + map.get("notice_id") + ")'>置顶</a>");
                        }
                    } else {
                        if (map.get("oper_org_code").toString().startsWith(userEntity.getOrgCode())) {
                            editHtml.append("&nbsp;&nbsp;<a href=javascript:; onclick='updateNotice(" + map.get("notice_id") + ")'>修改</a>");
                            if (map.get("oper_user_id").toString().equals(userEntity.getUserId())) {
                                editHtml.append("&nbsp;&nbsp;<a href=javascript:; onclick='delNotice(" + map.get("notice_id") + ")'>删除</a>");
                            }
                            if (map.get("top").toString().equals("true")) {
                                editHtml.append("&nbsp;&nbsp;<a href=javascript:; onclick='topNotice(" + map.get("notice_id") + ")'>取消置顶</a>");
                            } else {
                                editHtml.append("&nbsp;&nbsp;<a href=javascript:; onclick='topNotice(" + map.get("notice_id") + ")'>置顶</a>");
                            }
                        }
                    }
                }
                map.put("editHtml", editHtml.toString());
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
    public List<EemNoticeEntity> findNoticeList(UserEntity userEntity) throws ServiceException {
        List<EemNoticeEntity>  eemNoticeEntityList = new ArrayList<EemNoticeEntity>();
        StringBuffer hql = new StringBuffer("from EemNoticeEntity where deletedFlag=0 and overdue=0 order by lastUpdateTime,top desc");
        try {
            eemNoticeEntityList = baseDAO.find(hql.toString());
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return eemNoticeEntityList;
    }

    @Override
    public String saveNotice(EemNoticeEntity eemNoticeEntity,String deptCodes, UserEntity userEntity) throws ServiceException {
        try {
            eemNoticeEntity.setOperUserId(userEntity.getUserId());
            eemNoticeEntity.setOperUserPhone(userEntity.getMobilePhone());
            eemNoticeEntity.setOperUserTrueName(userEntity.getTrueName());
            eemNoticeEntity.setOperOrgId(userEntity.getOrgID());
            eemNoticeEntity.setOperOrgCode(userEntity.getOrgCode());
            eemNoticeEntity.setOperOrgName(userEntity.getOrgEntity().getOrgName());
            eemNoticeEntity.setOperFullOrgName(userEntity.getOrgEntity().getFullOrgName());
            eemNoticeEntity.setOverdue(false);
            Long objectId = 0L;
            String[] deptArr = deptCodes.split(",");
            if(eemNoticeEntity.getObjectId()!=null){//修改
                objectId = eemNoticeEntity.getObjectId();
                baseDAO.executeSql("delete from t_eem_org_notice where noticeID="+objectId);
            }else{
                objectId = baseDAO.getSequenceNextValue(EemNoticeEntity.class);
                eemNoticeEntity.setObjectId(objectId);
            }
            List<EemOrgNoticeEntity> orgNoticeEntityList = new ArrayList<EemOrgNoticeEntity>();
            for(String dept : deptArr){
                EemOrgNoticeEntity orgNoticeEntity = new EemOrgNoticeEntity();
                orgNoticeEntity.setObjectID(baseDAO.getSequenceNextValue(EemOrgNoticeEntity.class));
                orgNoticeEntity.setNoticeID(objectId);
                orgNoticeEntity.setOrgCode(dept);
                orgNoticeEntityList.add(orgNoticeEntity);
            }
            baseDAO.saveOrUpdate(eemNoticeEntity, userEntity);
            baseDAO.saveOrUpdateAll(orgNoticeEntityList);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public EemNoticeEntity showNotice(Long objectID, UserEntity userEntity) throws ServiceException {
        try {
            return (EemNoticeEntity) baseDAO.get(EemNoticeEntity.class, objectID);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteNotice(Long objectID, UserEntity userEntity) throws ServiceException {
        try {
            EemNoticeEntity eemNoticeEntity = (EemNoticeEntity) baseDAO.get(EemNoticeEntity.class, objectID);
            eemNoticeEntity.setDeletedFlag(true);
            eemNoticeEntity.setDeletedBy(userEntity.getUserId());
            eemNoticeEntity.setDeletionTime(new Timestamp(System.currentTimeMillis()));
            baseDAO.executeSql("delete from t_eem_org_notice where noticeID="+objectID);
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTopNotice(Long objectID, UserEntity userEntity) throws ServiceException {
        try {
            EemNoticeEntity eemNoticeEntity = (EemNoticeEntity) baseDAO.get(EemNoticeEntity.class, objectID);
            if(eemNoticeEntity.getTop()){
                eemNoticeEntity.setTop(false);
            }else{
                eemNoticeEntity.setTop(true);
            }
            eemNoticeEntity.setLastUpdateTime(new Timestamp(System.currentTimeMillis()));
            eemNoticeEntity.setLastUpdatedBy(userEntity.getUserId());
            baseDAO.saveOrUpdate(eemNoticeEntity,userEntity);
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 定时任务 判断通知是否到期
     * @throws ServiceException
     */
    @Override
    public void updateNoticeStatus() throws ServiceException {
        try {
            List<EemNoticeEntity> updateList = new ArrayList<EemNoticeEntity>();
            List<EemNoticeEntity> eemNoticeEntityList = baseDAO.find("from EemNoticeEntity where deletedFlag=0");
            Date currentDate = new Date();
            for(EemNoticeEntity eemNoticeEntity : eemNoticeEntityList){
                if(eemNoticeEntity.getOverdue()){//已经失效的
                    //取当前时间 如果当前时间大约开始时间 启用
                    if(currentDate.getTime()>=eemNoticeEntity.getStartDate().getTime()){
                        eemNoticeEntity.setOverdue(false);
                        updateList.add(eemNoticeEntity);
                    }
                }else{
                    //取当前时间 如果当前时间大约结束时间 禁用
                    if(currentDate.getTime()>=eemNoticeEntity.getEndDate().getTime()){
                        eemNoticeEntity.setOverdue(true);
                        updateList.add(eemNoticeEntity);
                    }
                }
            }
            baseDAO.saveOrUpdateAll(updateList);
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }
}
