package com.metarnet.eomeem.service;

import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.GeneralInfoModel;
import com.metarnet.core.common.model.Pager;
import com.metarnet.eomeem.model.*;
import com.metarnet.eomeem.vo.Tempt;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/6/2.
 */
public interface IEemCommonService {
    Pager queryDataList(Pager pager, String type, UserEntity userEntity, Long sign, String tempIds, int year) throws ServiceException;

    Pager queryDataList2(Pager pager, String type, UserEntity userEntity, Long sign, int year, String reportDate) throws ServiceException;

    Pager sumDataList(Pager pager, UserEntity userEntity) throws ServiceException;

    Pager queryCancelList(Pager pager, UserEntity userEntity) throws ServiceException;
    Pager queryDeductList(Pager pager, UserEntity userEntity) throws ServiceException;

    Pager countAnalysis(Pager pager, UserEntity userEntity) throws ServiceException;

    String saveAudit(GeneralInfoModel generalInfoModel, UserEntity userEntity) throws ServiceException;

    List<EemTempEntity> findTempList(String type, UserEntity userEntity, String tempIds) throws ServiceException;
    //厂商报表
    List<ReportEntity> reportTempList(String type, UserEntity userEntity) throws ServiceException;

    EvaluationReportExcel findEvaluationReportExcelByEid(String ObjectId) throws ServiceException, DAOException;

    EvaluationCollectExcel findEvaluationCollectExcelById(Long id) throws ServiceException, DAOException;

    void saveOrderReportDate(String reportData, UserEntity userEntity, boolean isOrder) throws ServiceException;

    Pager queryReportExcel(Pager pager, UserEntity userEntity) throws ServiceException;

    List<EemTempEntity> findTempList2(String type, UserEntity userEntity) throws ServiceException;

    boolean isButton(String isOrder, UserEntity userEntity) throws ServiceException;

    List<EemTempEntity> getAllUseExcelTempletList(Integer type);

    List<ExcelPage> findPageList(String dateStr, String year, OrgEntity orgEntity, UserEntity userEntity);

    List<EvaluationReportExcel> findEvaluationReportExcelByYearAndDate(String reportYear, String reportDate);

    List<EvaluationReportExcel> findEvaluationReportExcelByNew4Q();

    Pager findTimelyRateInfo(Pager pager, String provinceCode, String reportDate);

    Pager findAccuracyRateInfo(Pager pager, String provinceCode, String reportDate);

    List<ExcelPage> findExcelPages(Long tempID, String deptCode, String reportYear, String reportDate);

    String saveOrg(HttpServletRequest request, RepotOrg repotOrg, UserEntity userEntity);

    String getMangerOrgNams(UserEntity userEntity);

    List<EemTempEntity> findTempListToDevice(String type, UserEntity userEntity) throws ServiceException;
//查找厂商列表
    List queryDevice(Integer vendorId, String vendorName, String type) throws ServiceException;

    //查找报表列表
   // List querySheet(Long id, String sheetName, String type) throws ServiceException;

    String saveApply(String operdesc, String id, UserEntity userEntity);

    Pager queryApplyList(Pager pager, Integer type, UserEntity userEntity, long sign ,String tempIds);//  jw  已经修改  加参数

    EemApply getEemApplyById(long l);

    void saveApply(EemApply apply, UserEntity userEntity);

    String findVenders(String type, UserEntity userEntity);

    Pager queryVengers(Pager pager, String type, UserEntity userEntity);

    List<Map> initManagerTemplate(String temp, UserEntity userEntity);
    List<Tempt> initManagerTemplate2(String temp, UserEntity userEntity);

    List<Map> findSpecialtys(UserEntity userEntity);

    void saveTemptSpecInfo(UserEntity userEntity, String nodeSet);

    String findTempIdsByNodeId(String userName, String nodeID);
}
