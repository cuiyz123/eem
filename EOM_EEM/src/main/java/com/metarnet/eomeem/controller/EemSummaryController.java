package com.metarnet.eomeem.controller;

import com.alibaba.fastjson.JSONObject;
import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.FileAdapter;
import com.metarnet.core.common.controller.BaseController;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.eomeem.model.EemTempEntity;
import com.metarnet.eomeem.model.EvaluationCollectTime;
import com.metarnet.eomeem.model.EvaluationFileData;
import com.metarnet.eomeem.model.ExcelPage;
import com.metarnet.eomeem.service.IEemReportService;
import com.metarnet.eomeem.service.IEemSummaryService;
import com.metarnet.eomeem.service.IEemTemplateService;
import com.metarnet.eomeem.utils.DateUtils;
import com.metarnet.eomeem.utils.EemConstants;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by Administrator on 2016/8/14.
 */
@Controller
public class EemSummaryController extends BaseController {

    private static final String CHARACTER_GB2312 = "gb2312";

    private static final String CHARACTER_ISO8859 = "ISO8859-1";

    private Logger logger = LogManager.getLogger(EemSummaryController.class);
    @Resource
    private IEemSummaryService eemSummaryService;
    @Resource
    private IEemTemplateService eemTemplateService;
    @Resource
    private IBaseDAO baseDAO;
    @Resource
    private IEemReportService eemReportService;

    @RequestMapping(value = "/eemSummaryController.do", params = "method=collectData")
    public void collectData(HttpServletResponse response, HttpServletRequest request, String type, Long temptId, String reportYear, String reportDate, String deptIds) throws UIException {
        /**
         * 1.判断是单纯汇总还是汇总并上报
         * 2.获取模板
         * 4.获取地市已经审核通过的上报记录 ExcelPage
         * 5.获取地市信息
         */
        try {
            reportDate = URLDecoder.decode(reportDate, "UTF-8");
            UserEntity userEntity = getUserEntity(request);
            OutputStream os = null;
            HSSFWorkbook wwb = new HSSFWorkbook();
            EemTempEntity eemTempEntity = eemTemplateService.findTempByID(temptId);
            EemTempEntity collectTemp = eemTempEntity.getEemTempEntity();
            HSSFWorkbook inputTemp = new HSSFWorkbook(new ByteArrayInputStream(eemTempEntity.getTemplateExcelByteData().getUploadFileData()));
            Long fileDataTid = baseDAO.getSequenceNextValue(EvaluationFileData.class);
            String fileName = collectTemp.getShortName();
            String name = "";
            String filePath = "";
            String filePathName = "";
            OrgEntity orgEntity = AAAAAdapter.getCompany(Integer.parseInt(userEntity.getOrgEntity().getOrgId().toString()));
            String reportCode = orgEntity.getOrgCode();
            File file = null;
            if (type.equals("collectAndReport")) {//汇总并上报
                long mark = System.currentTimeMillis();
                name = mark + "_" + fileName + ".xls";
                filePath = EemConstants.EVALUATION_FILE_ABSOLUTE_PATH + File.separator + reportCode + File.separator
                        + DateUtils.getStrFromDateYYYYMM(new Date());
                filePathName = filePath + File.separator + fileDataTid + ".xls";
                logger.info("~~~~~11~~~~~~~" + filePathName);
                file = new File(filePathName);
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                file.createNewFile();
                os = new FileOutputStream(file);
            } else {//汇总
                os = response.getOutputStream();
            }
            List<OrgEntity> orgEntityList = new ArrayList<OrgEntity>();
            if (StringUtils.isNotBlank(deptIds)) {
                String[] deptArr = deptIds.split(",");
                for (String dept : deptArr) {
                    OrgEntity cityOrgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(dept);
                    orgEntityList.add(cityOrgEntity);
                }
            } else {
                List<OrgEntity> cityOrgEntity = AAAAAdapter.getInstence().findOrgListAndSelfByParentID(orgEntity.getOrgId());
                for (OrgEntity org : cityOrgEntity) {
                    if (org.getOrgCode().length() == 3 ||(org.getShortName()!=null&&org.getShortName().equals("BB"))) {
                        continue;
                    } else {
                        orgEntityList.add(org);
                    }
                }
                if(userEntity.getCategory().equals("PRO")){
                    String sql = "SELECT CITY_CODE from t_eem_repot_org where PROVINCE_CODE="+userEntity.getOrgEntity().getProCode()+";";
                    StringBuffer   buffer = new StringBuffer();
                        List<Map> list = baseDAO.findNativeSQL(sql,null);
                        for(Map map:list){
                           OrgEntity city = AAAAAdapter.getInstence().findOrgByOrgCode(map.get("city_code").toString());
                           if(city!=null){
                               orgEntityList.add(city);

                           }
                        }
                }
            }
            if(reportDate.contains("%")){
                reportDate =URLDecoder.decode(reportDate,"UTF-8");
            }
            if (eemTempEntity.getLevel() == 2) {//光缆
                HashSet<String> pageNameList = new HashSet<String>();
                String orgIds = "";
                for(int i = 0;i < orgEntityList.size();i++){
                    if(i == orgEntityList.size() - 1){
                        orgIds =orgIds+ orgEntityList.get(i).getOrgCode();
                        break;
                    }
                    orgIds =orgIds+ orgEntityList.get(i).getOrgCode() + ",";
                }
                //厂家个数
                String tempSql2 = "select t.pageName from t_eem_excel_page t WHERE t.DELETED_FLAG = 0 and" +
                        " t.reportYear="+reportYear+" and t.reportOrgCode in ("+orgIds+") and t.tpInputID = " + temptId ;

                System.out.println(tempSql2);
                List<Map> lists = (List)baseDAO.findNativeSQL(tempSql2,null);

                for(int i = 0;i < lists.size();i++){
                    Map map = lists.get(i);
                    //厂家类型个数
                    pageNameList.add((String)map.get("pagename"));
                }
                wwb = eemSummaryService.fromDBByteArrayToTableForEvaCablePoi(collectTemp.getTemplateExcelByteData().getUploadFileData(), os, reportDate, orgEntityList, reportYear, inputTemp.getSheetName(0), pageNameList);
            } else {
                wwb = eemSummaryService.fromDBByteArrayToTableForEvaPoi(collectTemp.getTemplateExcelByteData().getUploadFileData(), os, reportDate, orgEntityList, reportYear, inputTemp.getSheetName(0));
            }
            HSSFFormulaEvaluator.evaluateAllFormulaCells(wwb);
            if (type.equals("collectAndReport")) {
                wwb.write(os);
                byte[] byteArray = null;
                File fl = new File(filePathName);
                FileInputStream fis = new FileInputStream(fl);
                ByteArrayOutputStream bos = new ByteArrayOutputStream(10000);
                byte[] b = new byte[1024];
                int n;
                while ((n = fis.read(b)) != -1) {
                    bos.write(b, 0, n);
                }
                fis.close();
                bos.close();
                os.flush();
                os.close();
                byteArray = bos.toByteArray();
                FileAdapter fileAdapter = FileAdapter.getInstance();
                DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
                String attachmentId = fileAdapter.upload("",null, dataInputStream);
                dataInputStream.close();
                ExcelPage excelPage = new ExcelPage();
                excelPage.setTpInputID(temptId);
                excelPage.setFileName(eemTempEntity.getShortName());
                excelPage.setTpInputName(eemTempEntity.getTempName());
                excelPage.setReportOrgCode(reportCode);
                if(reportDate.contains("%")){
                    reportDate =URLDecoder.decode(reportDate,"UTF-8");
                }
                excelPage.setReportDate(reportDate);
                excelPage.setApplyId(1L);
                excelPage.setReportYear(reportYear);
                excelPage.setOperUserPhone(userEntity.getMobilePhone());
                excelPage.setReportType("collect");
                excelPage.setAttachmentId(attachmentId);
                String result = eemReportService.saveReportData(byteArray, excelPage, "", "", userEntity);
                JSONObject jsonObject = new JSONObject();
                if (StringUtils.isBlank(result)) {
                    jsonObject.put("success", true);
                } else {
                    jsonObject.put("success", false);
                    jsonObject.put("msg", result);
                }
                endHandle(request, response, jsonObject, "省分统计并上报数据");
            } else {
                name = orgEntity.getOrgName()+eemTempEntity.getShortName()+".xls";
//                name = com.metarnet.eomeem.utils.StringUtils.toUtf8String(request.getHeader("User-Agent"), name);
                response.setContentType("application/x-msdownload");
//                response.addHeader("Content-Disposition", "attachment; filename="
//                        + name + ".xls");
                name = new String(name.getBytes(CHARACTER_GB2312), CHARACTER_ISO8859);
                response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
                wwb.write(os);
                wwb.close();
                os.flush();
                os.close();
            }
            if(file!=null&&file.exists()){
                file.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @RequestMapping(value = "/eemSummaryController.do", params = "method=ZBCollectData")
    public void ZBCollectData(HttpServletResponse response, HttpServletRequest request) {
        /**
         * 1.根据汇总模板获得上报模板
         * 2.获得汇总模板
         * 3.判断是否是光缆
         */
        final String formIdStr = request.getParameter("formId");
        final String reportDate = request.getParameter("reportDate");
        final String reportYear = request.getParameter("reportYear");
        final String deps = request.getParameter("deptIds");
        final String deptNames = request.getParameter("deptNames");
        String result = "";
        try {
            final UserEntity userEntity = getUserEntity(request);
            String reportCode = AAAAAdapter.getCompany(Integer.parseInt(userEntity.getOrgEntity().getOrgId().toString())).getOrgCode();
            //汇总模板
            final EemTempEntity summaryTemp = eemTemplateService.findTempByID(Long.valueOf(formIdStr));
            List<EvaluationCollectTime> collectTimeList = baseDAO.find("from EvaluationCollectTime where deletedFlag=0 and createdBy="+userEntity.getUserId()+" and tempID="+summaryTemp.getObjectId()+" and reportYear='"+reportYear+"' and reportDate='"+reportDate+"' and provinceCodes='"+deps+"'");
            if(collectTimeList.size() > 0){
                result = "正在汇总数据，请等待";
            }else{
                result = "已经开始汇总数据，在此期间您可以处理其他事物";
                EvaluationCollectTime evaluationCollectTime = new EvaluationCollectTime();
                evaluationCollectTime.setCreatedBy(userEntity.getUserId());
                evaluationCollectTime.setCreatedTrueName(userEntity.getTrueName());
                evaluationCollectTime.setCreatedUserName(userEntity.getUserName());
                evaluationCollectTime.setDeletedFlag(false);
                evaluationCollectTime.setTempID(summaryTemp.getObjectId());
                evaluationCollectTime.setTempName(summaryTemp.getTempName());
                evaluationCollectTime.setReportYear(reportYear);
                evaluationCollectTime.setReportDate(reportDate);
                evaluationCollectTime.setProvinceCodes(deps);
                evaluationCollectTime.setProvinceNames(deptNames);
                evaluationCollectTime.setCreateDate(new Date());
                evaluationCollectTime.setObjectID(baseDAO.getSequenceNextValue(EvaluationCollectTime.class));
                evaluationCollectTime = eemSummaryService.saveEvaluationCollectTime(evaluationCollectTime);
                EemTempEntity reportTemp = null;
                List<EemTempEntity> eemTempEntityList = baseDAO.find("from EemTempEntity where deletedFlag=0 and eemTempEntity.objectId=" + summaryTemp.getObjectId());
                if (eemTempEntityList != null && eemTempEntityList.size() > 0) {
                    reportTemp = eemTempEntityList.get(0);
                }
                String fileName = summaryTemp.getTempName();
                String filePathName = "";
                String filePath = "";
                String name = "";
                long fileDataTid = baseDAO.getSequenceNextValue(EvaluationFileData.class);
                OutputStream os = null;
                if (reportTemp.getLevel() == 5) {//光缆
                    long mark = System.currentTimeMillis();
                    name = mark + "_" + fileName + ".xls";
                    filePath = EemConstants.EVALUATION_FILE_ABSOLUTE_PATH
                            + File.separator
                            + DateUtils.getStrFromDateYYYYMM(new Date());
                    filePathName = filePath + File.separator + fileDataTid + ".xls";
                    File file = new File(filePathName);
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    file.createNewFile();
                    os = new FileOutputStream(file);
                }
                /*WritableWorkbook wwb = eemSummaryService
                        .fromDBByteArrayToTable(summaryTemp.getTemplateExcelByteData().getUploadFileData(), os,
                                reportDate, deps, reportYear, formIdStr,true);*/
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String excelFilePath = eemSummaryService.saveZBCollectData(reportYear, reportDate, summaryTemp, userEntity, deps, formIdStr, deptNames);
                    }
                }).start();
                if (os != null) {
                    os.flush();
                    os.close();
                }
                if (reportTemp.getLevel() == 5) {
                    byte[] byteArray = null;
                    File fl = new File(filePathName);
                    FileInputStream fis = new FileInputStream(fl);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream(10000);
                    byte[] b = new byte[1024];
                    int n;
                    while ((n = fis.read(b)) != -1) {
                        bos.write(b, 0, n);
                    }
                    fis.close();
                    bos.close();
                    byteArray = bos.toByteArray();
                    eemSummaryService.saveEvaluationFileData(fileDataTid, filePath, fileName, reportCode, userEntity);
                    try {
                        eemReportService.importEvaluationFileDataForCable(fileDataTid,
                                byteArray, reportTemp, userEntity, reportCode, reportDate, reportYear);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out
                                .println("++++++++++++++导入时发生异常++++++++++++++++++++++++++++++++++++");
                    }
                }
//            download(request,response,excelFilePath,"application/octet-stream",fileName+".xls");
                evaluationCollectTime.setLastUpdateDate(new Date());
                evaluationCollectTime.setDeletedFlag(true);
                eemSummaryService.saveEvaluationCollectTime(evaluationCollectTime);
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("success",true);
            jsonObject.put("msg",result);
            endHandle(request,response,jsonObject,"总部汇总");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @RequestMapping(value = "/eemSummaryController.do", params = "method=ZBCollectData2")
    public void ZBCollectData2(HttpServletResponse response, HttpServletRequest request) {
        /**
         * 1.根据汇总模板获得上报模板
         * 2.获得汇总模板
         * 3.判断是否是光缆
         */
        String formIdStr = request.getParameter("formId");
        String reportDate = request.getParameter("reportDate");
        String reportYear = request.getParameter("reportYear");
        String deps = request.getParameter("deptIds");
        String deptNames = request.getParameter("deptNames");
        String result = "";
        try {
            UserEntity userEntity = getUserEntity(request);
            String reportCode = AAAAAdapter.getCompany(Integer.parseInt(userEntity.getOrgEntity().getOrgId().toString())).getOrgCode();
            //汇总模板
            EemTempEntity summaryTemp = eemTemplateService.findTempByID(Long.valueOf(formIdStr));
            List<EvaluationCollectTime> collectTimeList = baseDAO.find("from EvaluationCollectTime where deletedFlag=0 and createdBy="+userEntity.getUserId()+" and tempID="+summaryTemp.getObjectId()+" and reportYear='"+reportYear+"' and reportDate='"+reportDate+"' and provinceCodes='"+deps+"'");
            if(collectTimeList.size() > 0){
                result = "正在汇总数据，请等待";
            }else{
                result = "已经开始汇总数据，在此期间您可以处理其他事物";
                EvaluationCollectTime evaluationCollectTime = new EvaluationCollectTime();
                evaluationCollectTime.setCreatedBy(userEntity.getUserId());
                evaluationCollectTime.setCreatedTrueName(userEntity.getTrueName());
                evaluationCollectTime.setCreatedUserName(userEntity.getUserName());
                evaluationCollectTime.setDeletedFlag(false);
                evaluationCollectTime.setTempID(summaryTemp.getObjectId());
                evaluationCollectTime.setTempName(summaryTemp.getTempName());
                evaluationCollectTime.setReportYear(reportYear);
                evaluationCollectTime.setReportDate(reportDate);
                evaluationCollectTime.setProvinceCodes(deps);
                evaluationCollectTime.setProvinceNames(deptNames);
                evaluationCollectTime.setCreateDate(new Date());
                evaluationCollectTime.setObjectID(baseDAO.getSequenceNextValue(EvaluationCollectTime.class));
//                evaluationCollectTime = eemSummaryService.saveEvaluationCollectTime(evaluationCollectTime);
                EemTempEntity reportTemp = null;
                List<EemTempEntity> eemTempEntityList = baseDAO.find("from EemTempEntity where deletedFlag=0 and eemTempEntity.objectId=" + summaryTemp.getObjectId());
                if (eemTempEntityList != null && eemTempEntityList.size() > 0) {
                    reportTemp = eemTempEntityList.get(0);
                }
                evaluationCollectTime.setReportTempID(reportTemp.getObjectId());
                evaluationCollectTime.setLastUpdateDate(new Date());
//                evaluationCollectTime.setDeletedFlag(true);
                eemSummaryService.saveEvaluationCollectTime(evaluationCollectTime);
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("success", true);
            jsonObject.put("msg",result);
            endHandle(request, response, jsonObject, "总部汇总");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @RequestMapping(value = "/eemSummaryController.do", params = "method=checkReportPro")
    public void checkReportPro(HttpServletResponse response, HttpServletRequest request,Long formId,String reportYear,String reportDate,String deptIds) throws UIException {
        String result = eemSummaryService.checkReportPro(formId,reportYear,reportDate,deptIds);
        JSONObject jsonObject = new JSONObject();
        if(StringUtils.isBlank(result)){
            jsonObject.put("success",true);
        }else{
            if(result.indexOf("未上报数据")>-1){
                jsonObject.put("success",false);
                jsonObject.put("msg",result);
            }else{
                jsonObject.put("success",true);
                jsonObject.put("msg",result);
            }
        }
        endHandle(request,response,jsonObject,"checkReportPro");
    }

    protected  void download(HttpServletRequest request,HttpServletResponse response,
                             String downLoadPath, String contentType,String fileName) throws Exception {

        request.setCharacterEncoding("UTF-8");
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        //获取文件的长度
        long fileLength = new File(downLoadPath).length();

        String userAgent = request.getHeader("User-Agent");
        //针对IE或者以IE为内核的浏览器：
        if (userAgent.contains("MSIE")||userAgent.contains("Trident")) {
            fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
        } else {
            //非IE浏览器的处理：
            fileName = new String(fileName.getBytes("UTF-8"),"ISO-8859-1");
        }

        //设置文件输出类型
        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition", "attachment; filename="
                + fileName);

        //设置输出长度
        response.setHeader("Content-Length", String.valueOf(fileLength));
        //获取输入流
        bis = new BufferedInputStream(new FileInputStream(downLoadPath));
        //输出流
        bos = new BufferedOutputStream(response.getOutputStream());
        byte[] buff = new byte[2048];
        int bytesRead;
        while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
            bos.write(buff, 0, bytesRead);
        }
        //关闭流
        bis.close();
        bos.close();
    }
}
