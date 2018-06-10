package com.metarnet.eomeem.service.impl;

import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.FileAdapter;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.eomeem.model.*;
import com.metarnet.eomeem.service.IEemSummaryService;
import com.metarnet.eomeem.service.IEemTemplateService;
import com.metarnet.eomeem.utils.EemConstants;
import com.metarnet.eomeem.utils.ExportUtil;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import jxl.Cell;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.lang.Boolean;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2016/8/14.
 */
@Service
public class EemSummaryServiceImpl implements IEemSummaryService {
    private Logger logger = LogManager.getLogger(EemSummaryServiceImpl.class);
    private static final String GUANG_LAN = "光缆";
    private static final String DIE_XING_GUANG_LAN = "蝶形光缆";
    private static final String GUANG_LAN_CE_SHI = "光 缆 测 试";
    private static final String DIE_XING_GUANG_LAN_CE_SHI = "蝶 形 光 缆 测 试";

    private static final String GUANG_LAN_TOTAL = "光缆测试数据半年报表";
    private static final String DIE_XING_GUANG_LAN_TOTA = "蝶形光缆测试数据半年报表";
    private static final String GUANG_LAN_ZH_TOTAL = "光缆产品供货和综合评价季度表";
    private static final String DIE_XING_GUANG_LAN_ZH_TOTAL = "蝶形光缆产品供货和综合评价季度表";
    private static final List<Double> rankScoreList = new ArrayList<Double>();
    private static final String STAT_TYPE_COL = "col";//标识 统计表格是行还是列
    private static final String STAT_TYPE_ROW = "row";
    private int pix = 16;//光缆和蝶形光缆的 系数 综合的是  16
    double[] pvLength_ = new double[31];//存放各个省份的光缆长度  最多有31个省份

    @Resource
    private IBaseDAO baseDAO;
    @Resource
    private IEemTemplateService eemTemplateService;

    {
        rankScoreList.add(100d);
        rankScoreList.add(90d);
        rankScoreList.add(85d);
        rankScoreList.add(80d);
        rankScoreList.add(75d);
        rankScoreList.add(70d);
        rankScoreList.add(65d);
        rankScoreList.add(60d);
        rankScoreList.add(55d);
        rankScoreList.add(50d);
        rankScoreList.add(45d);
        rankScoreList.add(40d);
        rankScoreList.add(35d);
        rankScoreList.add(30d);
        rankScoreList.add(25d);
        rankScoreList.add(20d);
        rankScoreList.add(15d);
        rankScoreList.add(10d);
        rankScoreList.add(5d);
        rankScoreList.add(0d);
    }

    public HSSFWorkbook fromDBByteArrayToTableForEvaPoi(byte[] inputArray, OutputStream os, String reportDateStr, List<OrgEntity> deps, String reportYear, String formName) throws Exception {
        boolean isSearchHQData = false;//表示是否未审核数据也可汇总
        InputStream is = new ByteArrayInputStream(inputArray);
        WorkbookSettings wbs = new WorkbookSettings();
        wbs.setEncoding("GB2312");
        HSSFWorkbook wwb = new HSSFWorkbook(is);
        int sheetCount = wwb.getNumberOfSheets();
        for (int i = 0; i < sheetCount; ) {
            HSSFSheet sheet = null;
            if (sheetCount == 1) {
                sheet = wwb.getSheetAt(0);
            } else {
                sheet = wwb.getSheetAt(i);
            }
            String sheetName = sheet.getSheetName();
            if (sheetName.equals(formName)) {
                dealSheetForEvaluationPoi(sheet, deps, reportDateStr, isSearchHQData, "common", reportYear, "");
                i++;
            } else {
                int reSheetCount = wwb.getNumberOfSheets();
                for (int j = 0; j < reSheetCount; j++) {
                    HSSFSheet reSheet = wwb.getSheetAt(j);
                    String reSheetName = reSheet.getSheetName();
                    if (reSheetName.equals(sheetName)) {
                        wwb.removeSheetAt(j);
                        sheetCount--;
                        break;
                    }
                }
            }
        }
        return wwb;
    }

    @Override
    public HSSFWorkbook fromDBByteArrayToTableForEvaCablePoi(byte[] inputArray, OutputStream os, String reportDateStr, List<OrgEntity> deps, String reportYear, String formName, HashSet<String> pageNameList) throws Exception {
        boolean isSearchHQData = false;//表示是否未审核数据也可汇总
        InputStream is = new ByteArrayInputStream(inputArray);
        WorkbookSettings wbs = new WorkbookSettings();
        wbs.setEncoding("GB2312");
        HSSFWorkbook wwb = new HSSFWorkbook(is);
        int sheetCount1 = wwb.getNumberOfSheets();
        for (int i = 0; i < sheetCount1; i++) {
            HSSFSheet sheet = null;
            sheet = wwb.getSheetAt(i);
            if ("分省后评价得分".equals(sheet.getSheetName())) {
                wwb.removeSheetAt(i);
            }
        }
        int sheetCount = wwb.getNumberOfSheets();
        for (int i = 0; i < sheetCount; ) {
            HSSFSheet sheet = null;
            if (sheetCount == 1) {
                sheet = wwb.getSheetAt(0);
            } else {
                sheet = wwb.getSheetAt(i);
            }

            String sheetName = sheet.getSheetName();
            if (sheetName.equals(formName)) {
                int k=0;
                for (String str:pageNameList) {
                    wwb.cloneSheet(0);  //第一个sheet作为模板，每个厂家复制一个sheet,然后修改sheetname
                    wwb.setSheetName(k + 1, str);
                    HSSFSheet newSheet = wwb.getSheetAt(k + 1);
                    HSSFRow row = newSheet.getRow(1);
                    HSSFCell cell = row.getCell(2);
                    cell.setCellValue(str);
                    dealSheetForEvaluationPoi(newSheet, deps, reportDateStr, isSearchHQData, "common", reportYear, "2");
                    k++;
                }
                wwb.removeSheetAt(0); //删除第一个sheet
                break;
            } else {
                int reSheetCount = wwb.getNumberOfSheets();
                for (int j = 0; j < reSheetCount; j++) {
                    HSSFSheet reSheet = wwb.getSheetAt(j);
                    String reSheetName = reSheet.getSheetName();
                    if (reSheetName.equals(sheetName)) {
                        wwb.removeSheetAt(j);
                        sheetCount--;
                        break;
                    }
                }
            }
        }
        HSSFFormulaEvaluator.evaluateAllFormulaCells(wwb);
        return wwb;
    }

    //poi 处理wwb
    private void dealSheetForEvaluationPoi(HSSFSheet sheet, List<OrgEntity> PList, String reportDateStr, boolean isSearchHQData, String dealType, String reportYear, String fileType) throws Exception {
        //强制执行sheet中的公式
        String pagename = sheet.getSheetName();//sheet 的name 对应pagename
        int rowCount = sheet.getPhysicalNumberOfRows();//行数
        for (int i = 0; i < rowCount; i++) {
            //行 循环
            logger.info("~~~~~~~~~~~~~i" + i + "~~~~~~~~~~~~~~~~rowCount" + rowCount);
            HSSFRow row = sheet.getRow(i);
            int colCount = 0;
            if (row != null) {
                colCount = row.getLastCellNum(); //列数
            }

            for (int j = 0; j < colCount; j++) {//列 循环
                sheet.setForceFormulaRecalculation(true);
                logger.info("~~~~~~~~~~~~~~~~~~~~~~j=" + j);
                HSSFCell cell = row.getCell(j);

                //String content = cell.getContents();
                String content = "";
                if (!"".equals(cell) && cell != null) {
                    if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                        content = cell.getStringCellValue();
                    } else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                        content = cell.getNumericCellValue() + "";//没有日期格式的，所以没判断
                    } else if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {

                        content = cell.getCellFormula();
                        // cell.setCellFormula(content);
                        /* try{
                             content=String.valueOf(cell.getNumericCellValue());
						 }catch(IllegalStateException e){
							 content=String.valueOf(cell.getStringCellValue());
							// content=String.valueOf(cell.getRichStringCellValue());
						 }*/


                    }
                }
                //content=cell.getStringCellValue();
                if (content != null && content.trim().startsWith("##SQL:")) {
                    String res = "";
                    String depsForInQuery = "'";
                    for (OrgEntity gr : PList) {
                        depsForInQuery += gr.getOrgCode() + "','";
                    }
                    if (depsForInQuery.endsWith(",'")) {
                        depsForInQuery = depsForInQuery.substring(0, depsForInQuery.length() - 2);
                    }
                    if (!"".equals(fileType) && fileType != null && fileType.equals("2")) {//光缆
                        res = fromTempletContentToSqlResForEvaluationCable(content, depsForInQuery, reportDateStr, reportYear, pagename);
                    } else {
                        res = fromTempletContentToSqlResForEvaluation(content, depsForInQuery, reportDateStr, reportYear, isSearchHQData);
                    }
                    logger.info("-------------" + res + "-查詢結果----------------------------------");

                    double resDouble = 0;
                    try {

                        resDouble = Double.parseDouble(res);
                    } catch (Exception ex) {

                    }
                    //Number n=new Number(j,i,resDouble,ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));//加边框
                    //sheet.addCell(n);//具体数字
                    if (!"".equals(res) && res != null && (res.equals("是") || res.equals("否"))) {
                        cell.setCellValue(res);
                    } else {
                        cell.setCellValue(resDouble);
                    }
                    //cell.setCellValue(resDouble);  //增加"是"和"否" 两种情况


                    //	}

                    int pvAndHqColSize = 0;//经计算后 大于 这个值的列 都清空
                    if (isSearchHQData == true) {
                        pvAndHqColSize = 1;
                    }

                }
            }
        }
        sheet.setForceFormulaRecalculation(true);//强制执行sheet中的公式
    }

    /**
     * 总部汇总和省份汇总都用到，总部汇总如果按年汇总，那么不考虑data也就是上报频率，而省份汇总时会考虑到频率
     * 总部汇总传data值为空，省份为该有的频率值（季度或半年） zxx
     */
    private String fromTempletContentToSqlResForEvaluation(String content, String hqDepsForInQuery, String date, String reportYear, Boolean flag) {
        String sql = "";
        String res = "";
        try {
            if (content != null) {
                content = content.trim();
                if (content.trim().startsWith("##SQL:")) {
                    sql = content.substring(6);
                    //zxx
                    logger.info("".equals(date));
                    if ("".equals(date) == false) {//省份汇总
                        logger.info("date不为空");
                        if ("全年".equals(date)) {
                            sql = sql.replace("and p.reportdate='$$date'", "");
                        } else
                            sql = sql.replace("$$date", date);
                    } else {
                        if (sql.indexOf("and p.reportdate='$$date'") != -1) {
                            sql = sql.replace("and p.reportdate='$$date'", "");
                        }
                        if (sql.indexOf("and p.reportdate ='$$date'") != -1) {
                            sql = sql.replace("and p.reportdate ='$$date'", "");
                        }
                        if (sql.indexOf("and p.reportdate = '$$date'") != -1) {
                            sql = sql.replace("and p.reportdate = '$$date'", "");
                        }
                        //sql=sql.replace("and p.reportdate='$$date'", "");

                    }
                    //sql=sql.replace("$$date", date);

                    if ("".equals(reportYear) || reportYear == null) {
                        if (sql.indexOf("and p.reportYear='$$reportYear'") != -1) {
                            sql = sql.replace("and p.reportYear='$$reportYear'", "");
                        }
                        if (sql.indexOf("and p.reportYear ='$$reportYear'") != -1) {
                            sql = sql.replace("and p.reportYear ='$$reportYear'", "");
                        }
                        if (sql.indexOf("and p.reportYear = '$$reportYear'") != -1) {
                            sql = sql.replace("and p.reportYear = '$$reportYear'", "");
                        }


                        String reportDateStr = "";
                        Date newdate = new Date();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        String dateStrMon = df.format(newdate);
                        String monthStr = dateStrMon.substring(5, 7);
                        String yearStr = dateStrMon.substring(0, 4);
                        //String year=yearStr;


                        String lastYearStr = "";
                        String currentYearStr = "";

                        String bn = "";
                        //全年汇总的年份
                        String allReportYear = String.valueOf((Integer.parseInt(yearStr) - 1));
                        if (monthStr.startsWith("0")) {
                            monthStr = monthStr.replace("0", "").trim();
                        }
                        int monthInt = Integer.parseInt(monthStr);
                        //月份
                        if (monthInt <= 3) {
                            //reportDateStr = "第四季度";
                            //reportYear=String.valueOf((Integer.parseInt(yearStr)-1));

                            lastYearStr = " and ((p.reportyear='" + allReportYear + "' and p.reportdate in('第一季度','第二季度','第三季度','第四季度'))";
                            currentYearStr = ")";
                            //bn="上半年";
                        } else if (monthInt >= 4 && monthInt < 7) {
                            //reportDateStr = "第一季度";
                            lastYearStr = " and ((p.reportyear='" + allReportYear + "' and p.reportdate in('第二季度','第三季度','第四季度'))";
                            currentYearStr = " or (p.reportyear='" + yearStr + "' and p.reportdate in('第一季度'))" + ")";
                            //bn="上半年";

                        } else if (monthInt >= 7 && monthInt < 10) {
                            //reportDateStr = "第二季度";
                            lastYearStr = " and ((p.reportyear='" + allReportYear + "' and p.reportdate in('第三季度','第四季度'))";
                            currentYearStr = " or (p.reportyear='" + yearStr + "' and p.reportdate in('第一季度','第二季度'))" + ")";
                            //bn="下半年";

                        } else {
                            //reportDateStr = "第三季度";
                            lastYearStr = " and ((p.reportyear='" + allReportYear + "' and p.reportdate in('第四季度'))";
                            currentYearStr = " or (p.reportyear='" + yearStr + "' and p.reportdate in('第一季度','第二季度','第三季度'))" + ")";
                            //bn="下半年";
                        }
                        //sql=sql+lastYearStr+currentYearStr;

                        sql = sql.replace("$$condition", " and txtvalue!=\"\"  " + lastYearStr + currentYearStr);


                    } else {
                        sql = sql.replace("$$reportYear", reportYear);
                        sql = sql.replace("$$condition", " and p.DELETED_FLAG=0 and txtvalue!=\"\"");                    }

                    //sql=sql.replace("$$reportYear", reportYear);
                    String tmpCondition = hqDepsForInQuery;
                    sql = sql.replace("'$$depart'", tmpCondition);

                    //sql=sql.replace("$$condition", " (p.iswithdraw!='Y' or p.iswithdraw is null)");


                    if (StringUtils.isNotBlank(sql)) {
                        sql = sql.replaceAll("strtonumber", "");
                        if (flag) {
                            sql = sql.replaceAll("and p.WORK_ORDER_STATUS=\"已审核\"", "");
                            sql = sql.replaceAll("and  p.WORK_ORDER_STATUS=\"已审核\"", "");
                        }
                        System.out.println("--------------***"+sql);
                        Object obj = baseDAO.findNativeSQL(sql, null);
                        if (obj != null) {
                            List list = (List) obj;
                            if (list != null && list.size() > 0 && list.get(0) != null) {
                                Map resMap = (Map) list.get(0);
                                Object ob = resMap.get("value");
                                if (ob != null) {
                                    res = ob.toString();

                                    logger.info("--------查詢結果：" + res + "--------");
                                    logger.info(sql);
                                    try {//处理无限循环小数 如果出现问题立即捕获 不影响数据显示
                                        if (res.indexOf(".") != -1) {
                                            if (res.length() - res.indexOf(".") > 3) {
                                                res = res.substring(0, res.indexOf(".") + 3);
                                            }
                                        }
                                    } catch (Exception ex) {
                                        logger.info("err:" + sql);
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.info("err:" + sql);
            ex.printStackTrace();
        }
        return res;
    }

    /**
     * 根据模版表格中特殊格式的sql 生成查询结果  后评价--光缆 省份汇总用到
     */
    private String fromTempletContentToSqlResForEvaluationCable(String content, String hqDepsForInQuery, String date, String reportYear, String pagename) {
        String sql = "";
        String res = "";
        try {
            if (content != null) {
                content = content.trim();
                if (content.trim().startsWith("##SQL:")) {
                    sql = content.substring(6);

                    sql = sql.replace("$$date", date);
                    sql = sql.replace("$$reportYear", reportYear);
                    String tmpCondition = hqDepsForInQuery;
                    sql = sql.replace("'$$depart'", tmpCondition);
                    sql = sql.replace("$$pagename", pagename);
                    sql = sql.replace("$$condition", " and (p.iswithdraw!='Y' or  p.iswithdraw is null)");


                    if (sql != null && !"".equals(sql)) {
                        sql = sql.replaceAll("strtonumber", "");
                        System.out.println("--------------***"+sql);
                        Object obj = baseDAO.findNativeSQL(sql, null);
                        if (obj != null) {
                            List list = (List) obj;
                            if (list != null && list.size() > 0 && list.get(0) != null) {
                                Map resMap = (Map) list.get(0);
                                Object ob = resMap.get("value");
                                if (ob != null) {
                                    //四舍五入保留两位小数
                                    double f = Double.parseDouble(ob.toString());
                                    BigDecimal b = new BigDecimal(f);
                                    double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                    res = Double.toString(f1);

                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.info(sql);
            ex.printStackTrace();
        }
        return res;
    }

    //根据模版解析sql
    //zxx 注释
    public WritableWorkbook fromDBByteArrayToTable(long sumId, byte[] inputArray, OutputStream os, String reportDateStr, String deps, String reportYear, String formId, Boolean flag) throws Exception {
        //获得省份的信息
        boolean isSearchHQData = false;//表示是否未审核汇总 ，默认为否
        if (flag) {
            isSearchHQData = flag;
        }
        List<OrgEntity> orgEntityList = new ArrayList<OrgEntity>();
        if (StringUtils.isNotBlank(deps)) {
            String[] depts = deps.split(",");
            for (String dept : depts) {
                OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(dept);
                orgEntityList.add(orgEntity);
            }
        } else {
            List<OrgEntity> orgList = AAAAAdapter.getInstence().findOrgListAndSelfByParentID(1L, false);
            for (OrgEntity orgEntity : orgList) {
                if (orgEntity.getOrgCode().startsWith("1") || orgEntity.getOrgCode().startsWith("81") || orgEntity.getOrgCode().startsWith("51")) {
                    continue;
             //   } else {    jw 4.26  去除汇总中的多余子公司
                }  else if( orgEntity.getOrgCode().startsWith("2"))  {
                    orgEntityList.add(orgEntity);
                }
            }
        }
        //inputArray 其中为汇总模板byte[]数组
        InputStream is = new ByteArrayInputStream(inputArray);
        WorkbookSettings wbs = new WorkbookSettings();
        //解决乱码问题
        wbs.setEncoding("GB2312");
        Workbook wb = Workbook.getWorkbook(is);
        //打开一个文件副本，并将指定数据写回到源文件
        WorkbookSettings settings = new WorkbookSettings();
        settings.setWriteAccess(null);
        //wwb为汇总模板
        WritableWorkbook wwb = Workbook.createWorkbook(os, wb, settings);
        // WritableSheet   sheet=wwb.getSheet(0);获取汇总模板中的sheet页信息
        WritableSheet[] sheetArray = wwb.getSheets();
        ArrayList<ArrayList> arrayList = new ArrayList<ArrayList>();//同一个sheet可能存放多行list,存放多个省分，多个厂家的明细设备数,为了计算省分是否为有效覆盖
//        for (WritableSheet sheet : sheetArray) {
        for(int i=0;i<sheetArray.length;i++){
            WritableSheet sheet = sheetArray[i];
            if (sheet.getName().equals("分省后评价得分")) {
                continue;
            }
            //sheet name现在这三个sheet页都能读到
            logger.info("99999999" + sheet.getName());
            WritableCell cell = sheet.getWritableCell(0, 0);
            double coefficient = 0; //表格中存 总分的列数的系数 例如3  则 3 6 9 12 列都为总分 用于排名
            String equipType = "";
            String content = cell.getContents();
            if (content != null && !"".equals(content)) {
                String[] coefficientArray = content.split(",");
                if (coefficientArray != null && coefficientArray.length > 1) {
                    String t = coefficientArray[0];
                    try {
                        if (t != null) {
                            coefficient = Double.parseDouble(t);
                            logger.info("表格系数为" + coefficient);
                        }
                    } catch (Exception ex) {

                    }
                    if (coefficientArray.length >= 2) {
                        equipType = coefficientArray[1].trim();
                    }
                }
            }

            if (content.contains("col")) {
                //dealSheetForHQ2(sheet,searchPVList,HQList,reportDateStr,isSearchHQData,"common",reportYear,coefficient,equipType,formId);
                dealSheetForHQ2new(wwb, sheet, orgEntityList, reportDateStr, isSearchHQData, "common", reportYear, coefficient, equipType, String.valueOf(sumId));
            } else if (content.contains("甲") || content.contains("乙")) {
                dealSheetForHQ(sumId, sheet, orgEntityList, reportDateStr, isSearchHQData, "common", reportYear, coefficient, equipType,arrayList);
            } else if (content.contains(GUANG_LAN_TOTAL) || content.contains(DIE_XING_GUANG_LAN_TOTA) || content.contains(GUANG_LAN_ZH_TOTAL) || content.contains(DIE_XING_GUANG_LAN_ZH_TOTAL)) {
                wwb.removeSheet(1);
            } else if (((content.contains(GUANG_LAN)  || content.contains(GUANG_LAN_CE_SHI)) && (!content.contains(DIE_XING_GUANG_LAN)) &&  (!content.contains(DIE_XING_GUANG_LAN_CE_SHI)))) {
                dealSheetForGUANGLANnew3(wwb, sheet, orgEntityList, reportDateStr, isSearchHQData, "common", reportYear, coefficient, equipType, content, formId);
            }else if(content.contains(DIE_XING_GUANG_LAN)  || content.contains(DIE_XING_GUANG_LAN_CE_SHI)){
//                dealSheetForGUANGLANnew2(wwb, sheet, orgEntityList, reportDateStr, isSearchHQData, "common", reportYear, coefficient, equipType, content, formId);
                dealSheetForGUANGLANnew(wwb, sheet, orgEntityList, reportDateStr, isSearchHQData, "common", reportYear, coefficient, equipType, content, formId);
            }
            else {
                if (sheet.getName().contains("原始")) {
                    dealSheetForHQ1(sheet, orgEntityList, reportDateStr, isSearchHQData, "common", reportYear);
                }
//                else if(sheet.getName().contains("PON-OLT")){
//                    dealSheetForEvaluation(sheet, orgEntityList, reportDateStr, isSearchHQData, "common", reportYear,i,arrayList);
//                }
                else {
                    dealSheetForEvaluation(sheet, orgEntityList, reportDateStr, isSearchHQData, "common", reportYear,i,arrayList);
                }
            }
        }
        return wwb;
    }

    @Override
    public String saveZBCollectData(String reportYear, String reportDate, EemTempEntity summaryTemp, UserEntity userEntity, String deps, String formIdStr, String deptNames) {
        try {
            String excelName = reportYear + reportDate + summaryTemp.getTempName() + ".xls";
            String path = EemConstants.GATHER_DATA_PATH + File.separator + "120";
           String pathname = path + File.separator + excelName;

            OutputStream os =   os = new FileOutputStream(pathname);

            WritableWorkbook wwb = fromDBByteArrayToTable(summaryTemp.getObjectId(), summaryTemp.getTemplateExcelByteData().getUploadFileData(), os,
                    reportDate, deps, reportYear, formIdStr, true);
            EvaluationCollectExcel ere = null;
            System.out.println("***********************"+"from EvaluationCollectExcel a where a.formId=" + summaryTemp.getObjectId() + " and a.reportYear='" + reportYear + "' and a.reportDate='" + reportDate + "'  order by creationTime desc");
//            List<EvaluationCollectExcel> list = baseDAO.find("from EvaluationCollectExcel a where a.formId=" + summaryTemp.getObjectId() + " and a.reportYear='" + reportYear + "' and a.reportDate='" + reportDate + "'  order by creationTime desc");
            System.out.println("-------------------------------" + wwb.getNumberOfSheets());
//            System.out.println("-------------------------------" + list.size());
//            if (list != null && list.size() > 0 && deptNames.equals(list.get(0).getReportDeptNames())) {//需要判断上报省分是否一致
//                ere = list.get(0);
//            } else {
                ere = new EvaluationCollectExcel();// 保存各个表的信息
                ere.setObjectId(baseDAO.getSequenceNextValue(EvaluationReportExcel.class));
//                ere.setObjectId(baseDAO.getSequenceNextValue(EvaluationCollectExcel.class));
//            }

            wwb.write();
            wwb.close();
            os.flush();
            os.close();
            File file  = new File(pathname);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            FileAdapter fileAdapter = FileAdapter.getInstance();
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
            String attachmentId = fileAdapter.upload("", null, dataInputStream);
            dataInputStream.close();
            ere.setDep("总部");
            ere.setExcelName(excelName);
            ere.setExcelPath(path);
            ere.setFormId(summaryTemp.getObjectId());
            ere.setCreationTime(new Timestamp(System.currentTimeMillis()));
            ere.setReportYear(reportYear);
            ere.setReportDate(reportDate);
            ere.setFileName(excelName);
            ere.setDeptsWithDraw("");
            ere.setOperUserTrueName(userEntity.getTrueName());
            ere.setOperUserPhone(userEntity.getMobilePhone());
            ere.setOperUserId(userEntity.getUserId());
            ere.setOperOrgId(userEntity.getOrgID());
            ere.setOperFullOrgName(userEntity.getOrgEntity().getFullOrgName());
            ere.setTpInputName(summaryTemp.getTempName());
            ere.setDeletedFlag(false);
            ere.setReportDeptNames(deptNames);
            ere.setReportDeptCodes(deps);
            ere.setAttachmentId(attachmentId);
            baseDAO.save(ere);
            file.delete();
            return path + File.separator + excelName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void saveEvaluationFileData(Long objectID, String filePath, String fileName, String reportCode, UserEntity userEntity) {
        EvaluationFileData efd = new EvaluationFileData();
        efd.setFileID(objectID);
        efd.setFilePath(filePath);
        efd.setFileName(fileName);
        efd.setUploadDate(new Date());
        efd.setReportOrgCode(reportCode);
        efd.setReportPersonName(userEntity.getTrueName());
        efd.setReportPersonID(userEntity.getUserId());
        efd.setReportPersonTel(userEntity.getMobilePhone());
        try {
            baseDAO.saveOrUpdate(efd);
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String checkReportPro(Long formId, String reportYear, String reportDate, String deptIds) {
        StringBuffer result = new StringBuffer();
        try {
            Map<String, ExcelPage> excelPageMap = new HashMap<String, ExcelPage>();
            List<EemTempEntity> eemTempEntityList = eemTemplateService.findEportTempByCollectID(formId);
            String ids = "";
            for (EemTempEntity eemTempEntity : eemTempEntityList) {
                ids += eemTempEntity.getObjectId() + ",";
            }
            if (StringUtils.isNotBlank(ids)) {
                ids = ids.substring(0, ids.length() - 1);
            }
            List<ExcelPage> excelPageList = baseDAO.find("from ExcelPage where deletedFlag=0 and reportYear='" + reportYear + "' and reportDate='" + reportDate + "'and tpInputID in(" + ids + ")");
            for (ExcelPage excelPage : excelPageList) {
                excelPageMap.put(excelPage.getReportOrgCode(), excelPage);
            }
            for (String dept : deptIds.split(",")) {
                if (StringUtils.isNotBlank(dept) && excelPageMap.get(dept) == null) {
                    OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(dept);
                    result.append("【" + orgEntity.getOrgName() + "未上报数据】");
                } else if (StringUtils.isNotBlank(dept) && excelPageMap.get(dept) != null) {
                    OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(dept);
                    if (excelPageMap.get(dept).getWorkOrderStatus().equals("未审核")) {
                        result.append("【" + orgEntity.getOrgName() + "上报数据未审核】");
                    }
                }
            }
        } catch (DAOException e) {
            e.printStackTrace();
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    @Override
    public EvaluationCollectTime saveEvaluationCollectTime(EvaluationCollectTime evaluationCollectTime) {
        try {
            baseDAO.saveOrUpdate(evaluationCollectTime);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return evaluationCollectTime;
    }

    //	  省份和 汇总项 颠倒
    private void dealSheetForHQ2new(WritableWorkbook wwb, WritableSheet sheet, List<OrgEntity> PList, String reportDateStr, boolean isSearchHQData, String dealType, String reportYear, double coefficient, String equipType, String formId) throws Exception {
        int rowCount = sheet.getRows();
        int colCount = sheet.getColumns();
        List<NameValue> spaceList = new ArrayList<NameValue>();
        int spaceRow = 0;//填充数据到的具体的行数5

        spaceRow = getSpaceList(sheet, reportDateStr, dealType, reportYear, spaceList, isSearchHQData);

        List<List<String>> dataList = getDataList(sheet, PList, spaceList, spaceRow, STAT_TYPE_COL,isSearchHQData);

        double[][] resArray = getResArray(dataList);

        if ("乙1".equals(equipType) || "丙1".equals(equipType)) {//哈哈丙

            double[] sumArray = new double[dataList.get(0).size()];//设备数和故障数 为求和， 服务得分为最高分
            double[] percentArray = new double[dataList.get(0).size()];
            double[] mark = new double[dataList.get(0).size()];//保存最后的规模效益得分

            double[] percentMark = new double[dataList.get(0).size()];
            double[] realArray = new double[dataList.get(0).size()];
            double[] finalArray = new double[dataList.get(0).size()];

            double[][] serveArray = new double[dataList.size()][dataList.get(0).size()];  //每个省份的服务得分
            double[][] faultRateArray = new double[dataList.size()][dataList.get(0).size()];   //每个省份的故障率得分
            double[] evaluateArray = new double[dataList.get(0).size()];  //新增集采服务后评价得分
            double vendorfaultRateArray[] = new double[dataList.get(0).size()]; //各个厂家故障率得分
            double[] hqEvaluateArray = new double[dataList.get(0).size()];  // 后评价平均分
            double[] hqGroupEvaluateArray = new double[dataList.get(0).size()];//2014版集采后评价平均分
            double[] groupEvaluateArray = new double[dataList.get(0).size()];//2014版集采后评价得分
            //getArrays(dataList,sumArray,percentArray,mark,percentMark,realArray,finalArray,coefficient,resArray,equipType);
            double[] groupEvaluateArray_2016 = new double[dataList.get(0).size()];//2016版后评价得分

            //2017年版后评价得分
            double[] hqGroupEvaluateArray_2017 = new double[dataList.get(0).size()];
            //2017年版有效规模效益分
            double[] hqGroupEvaluateArray_2017xy = new double[dataList.get(0).size()];




            if (wwb.getNumberOfSheets() > 2) {
                getArraysNew2(PList, wwb, dataList, sumArray, percentArray, mark, percentMark, realArray, finalArray, coefficient,
                        resArray, faultRateArray, serveArray, evaluateArray, equipType, formId, vendorfaultRateArray,
                        hqEvaluateArray, hqGroupEvaluateArray, groupEvaluateArray,hqGroupEvaluateArray_2017,hqGroupEvaluateArray_2017xy);
            } else {
                getArraysNew(dataList, sumArray, percentArray, mark, percentMark, realArray, finalArray, coefficient,
                        resArray, faultRateArray, serveArray, evaluateArray, equipType, formId, vendorfaultRateArray, hqEvaluateArray);
            }


            List<Double> sortList = new ArrayList<Double>();
            for (int i = 0; i < finalArray.length; i++) {
                try {
                    if (i % coefficient == 0) {
                        double tt = finalArray[i];
                        sortList.add(tt);
                    }
                } catch (Exception ex) {
                }
            }
            Collections.sort(sortList); //排序
            int a = (int) (sortList.size() * 0.7);

            //double aMin = sortList.get(a)/PList.size();
            double aMin = sortList.get(a);

            int col = PList.size() + spaceRow;
            WritableCell rCell = null;
            java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");

            for (int j = 0; j < finalArray.length; j++) {
                if (j - 2 < 0) {
                    continue;
                }
                double tt = percentMark[j - 2];

                double ttt = percentArray[j - 2];
                String tttStr = "";
                if (ttt > 998 && ttt < 1000) {
                    tttStr = "-";
                } else {
                    //	tttStr=ttt+"";
                    tttStr = df.format(ttt * 100) + "%";
                }

                try {
                    if (j % coefficient == 0) {//总分项在这里处理
                        if (tt < 60) {
                            rCell = new Label(col + 1, j - 2, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.RED, 12));
                        } else {
                            if (tt >= aMin) {
                                rCell = new Label(col + 1, j - 2, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                            } else {
                                rCell = new Label(col + 1, j - 2, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                            }
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 1, j - 2, col + 1, j);

                        rCell = new Label(col, j - 2, tttStr, ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col, j - 2, col, j);
                    } else {
                        //rCell = new Number(j,col+1,tt);
                        //sheet.addCell(rCell);//具体数字
                    }
                } catch (Exception ex) {
                    rCell = new Label(col + 1, j, tt + "", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                    sheet.addCell(rCell);//具体数字
                }
            }
            rCell = new Label(col + 1, 0, "故障率得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字
            rCell = new Label(col, 0, "故障率", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字


            rCell = null;
            col = PList.size() + spaceRow + 2;
            for (int j = 0; j < finalArray.length; j++) {
                if (j - 2 < 0) {
                    continue;
                }
                double tt = finalArray[j - 2];
                try {
                    if (j % coefficient == 0) {//总分项在这里处理
                        if (tt < 60) {
                            rCell = new Label(col, j - 2, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.RED, 12));
                        } else {
                            if (tt >= aMin) {
                                rCell = new Label(col, j - 2, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                            } else {
                                rCell = new Label(col, j - 2, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                            }
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col, j - 2, col, j);
                    } else {
                        //rCell = new Number(j,PList.size()+spaceRow,tt);
                        //sheet.addCell(rCell);//具体数字
                    }
                } catch (Exception ex) {
                    rCell = new Label(col, j, tt + "", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                    sheet.addCell(rCell);//具体数字
                }
            }
            rCell = new Label(col, 0, "总部项目集采得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字

            rCell = null;
            col = PList.size() + spaceRow + 3;
            for (int j = 0; j < realArray.length; j++) {
                if (j - 2 < 0) {
                    continue;
                }
                double tt = realArray[j - 2];
                double ttt = mark[j - 2];
                try {
                    if (j % coefficient == 0) {//总分项在这里处理
                        if (tt < 40) {
                            rCell = new Label(col + 1, j - 2, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 1, j - 2, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 1, j - 2, col + 1, j);

                        rCell = new Label(col, j - 2, df.format(ttt), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col, j - 2, col, j);
                    } else {
                        //rCell = new Number(j,PList.size()+spaceRow,tt);
                        //sheet.addCell(rCell);//具体数字
                    }
                } catch (Exception ex) {
                    rCell = new Label(col + 1, j, tt + "", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                    sheet.addCell(rCell);//具体数字
                }
            }
            rCell = new Label(col, 0, "规模效益得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字
            rCell = new Label(col + 1, 0, "建议暂缓下期集采", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字
            //add
            List<Double> rateList = new ArrayList<Double>();
            for (int j = 1; j < vendorfaultRateArray.length; j++) {
                if ((j + 2) % coefficient == 0) {
                    rateList.add(vendorfaultRateArray[j]);
                }
            }
            Collections.sort(rateList);
            int bb = (int) (rateList.size() * 0.3);
            double bbMin = sortList.get(bb);
            rCell = null;
            col = PList.size() + spaceRow + 5;
            for (int j = 1; j < vendorfaultRateArray.length; j++) {
                if ((j + 2) % coefficient == 0) {
                    double value = vendorfaultRateArray[j];
                    if (value >= bbMin) {
                        rCell = new Label(col, j, df.format(value), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                    } else {
                        rCell = new Label(col, j, df.format(value), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                    }
                    //rCell = new Label(col,j,df.format(value),ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                    sheet.addCell(rCell);//具体数字
                    sheet.mergeCells(col, j, col, j + 2);
                }
            }
            rCell = new Label(col, 0, "新故障率得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字
            //后评价平均分
            List<Double> listAvg = new ArrayList<Double>();
            for (int j = 1; j < hqEvaluateArray.length; j++) {
                if ((j + 2) % coefficient == 0) {
                    listAvg.add(hqEvaluateArray[j]);
                }
            }
            Collections.sort(listAvg);
            int bavg = (int) (listAvg.size() * 0.3);
            double bMinAvg = sortList.get(bavg);
            rCell = null;
            col = PList.size() + spaceRow + 6;
            for (int j = 1; j < hqEvaluateArray.length; j++) {
                if ((j + 2) % coefficient == 0) {

                    double value = hqEvaluateArray[j];
                    if (value >= bMinAvg) {
                        rCell = new Label(col, j, df.format(value), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                    } else {
                        rCell = new Label(col, j, df.format(value), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                    }
                    //rCell = new Label(col,j,df.format(value),ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                    sheet.addCell(rCell);//具体数字
                    sheet.mergeCells(col, j, col, j + 2);
                }

            }
            rCell = new Label(col, 0, "后评价平均分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字

            //

            List<Double> list = new ArrayList<Double>();
            for (int j = 1; j < evaluateArray.length; j++) {
                if ((j + 2) % coefficient == 0) {
                    list.add(evaluateArray[j]);
                }
            }
            Collections.sort(list);
            int b = (int) (list.size() * 0.3);
            double bMin = sortList.get(b);
            rCell = null;
            col = PList.size() + spaceRow + 7;
            for (int j = 1; j < evaluateArray.length; j++) {
                if ((j + 2) % coefficient == 0) {
                    double value = evaluateArray[j];
                    if (value >= bMin) {
                        rCell = new Label(col, j, df.format(value), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                    } else {
                        rCell = new Label(col, j, df.format(value), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                    }
                    //rCell = new Label(col,j,df.format(value),ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                    sheet.addCell(rCell);//具体数字
                    sheet.mergeCells(col, j, col, j + 2);
                }

            }
            rCell = new Label(col, 0, "后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字
            //add by btliu start
            List<Double> hqGroupList = new ArrayList<Double>();
            for (int j = 1; j < hqGroupEvaluateArray.length; j++) {
                if ((j + 2) % coefficient == 0) {
                    hqGroupList.add(hqGroupEvaluateArray[j]);
                }
            }
            Collections.sort(hqGroupList);
            int c = (int) (hqGroupList.size() * 0.3);
            double cMin = sortList.get(c);
            rCell = null;
            col = PList.size() + spaceRow + 8;
            for (int j = 1; j < hqGroupEvaluateArray.length; j++) {
                if ((j + 2) % coefficient == 0) {
                    double value = hqGroupEvaluateArray[j];
                    if (value >= cMin) {
                        rCell = new Label(col, j, df.format(value), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                    } else {
                        rCell = new Label(col, j, df.format(value), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                    }
                    sheet.addCell(rCell);//具体数字
                    sheet.mergeCells(col, j, col, j + 2);
                }
            }

            rCell = new Label(col, 0, "2014版后评价平均分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字
            List<Double> groupList = new ArrayList<Double>();
            for (int j = 1; j < groupEvaluateArray.length; j++) {
                if ((j + 2) % coefficient == 0) {
                    groupList.add(groupEvaluateArray[j]);
                }
            }
            Collections.sort(groupList);
            int d = (int) (groupList.size() * 0.3);
            double dMin = sortList.get(d);
            rCell = null;
            col = PList.size() + spaceRow + 9;
            for (int j = 1; j < groupEvaluateArray.length; j++) {
                if ((j + 2) % coefficient == 0) {
                    double value = groupEvaluateArray[j];
                    if (value >= dMin) {
                        rCell = new Label(col, j, df.format(value), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                    } else {
                        rCell = new Label(col, j, df.format(value), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                    }
                    sheet.addCell(rCell);//具体数字
                    sheet.mergeCells(col, j, col, j + 2);
                }
            }
            rCell = new Label(col, 0, "2014版后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字
            //btliu end

            col = PList.size() + spaceRow + 10;
            for (int j = 1; j < hqGroupEvaluateArray_2017.length; j++) {
                if ((j + 2) % coefficient == 0) {
                    double value = hqGroupEvaluateArray_2017[j];
                    if (value >= dMin) {
                        rCell = new Label(col, j, df.format(value), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                    } else {
                        rCell = new Label(col, j,df.format(value), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                    }
                    sheet.addCell(rCell);//具体数字
                    sheet.mergeCells(col, j, col, j + 2);
                }
            }
            rCell = new Label(col, 0, "2017版后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字


            col = PList.size() + spaceRow + 11;
            for (int j = 1; j < hqGroupEvaluateArray_2017xy.length; j++) {
                if ((j + 2) % coefficient == 0) {
                    double value = hqGroupEvaluateArray_2017xy[j];
                    if(value < 0){
                        value = 0;
                    }
                    if (value >= dMin) {
                        rCell = new Label(col, j, df.format(value), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                    } else {
                        rCell = new Label(col, j, df.format(value), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                    }
                    sheet.addCell(rCell);//具体数字
                    sheet.mergeCells(col, j, col, j + 2);
                }
            }
            rCell = new Label(col, 0, "2017有效规模效益分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字


        }
    }

    //	处理后评价汇总 对各种设备进行汇总
    private void dealSheetForHQ(long sumId, WritableSheet sheet, List<OrgEntity> PList, String reportDateStr, boolean isSearchHQData,
                                String dealType, String reportYear, double coefficient, String equipType,ArrayList<ArrayList> arrayList) throws Exception {
        int rowCount = sheet.getRows();
        int colCount = sheet.getColumns();
        List<NameValue> spaceList = null;
        int spaceRow = 0;//填充数据到的具体的行数

        for (int i = 0; i < rowCount; i++) {  //行 循环
            if (spaceList != null && spaceList.size() > 0) {
                break;
            }

            spaceList = new ArrayList<NameValue>();
            for (int j = 0; j < colCount; j++) {//列 循环

                WritableCell cell = sheet.getWritableCell(j, i);
                String content = cell.getContents();

                if (content != null && content.trim().startsWith("##SQL:")) {

                    String sql = fromTempletContentToSql(content, "", reportDateStr, reportYear);
                    if (isSearchHQData) {
                        sql = sql.replaceAll("and p.WORK_ORDER_STATUS=\"已审核\"", "");
                    }
                    NameValue nv = new NameValue();
                    nv.setName(i + "");
                    nv.setValue(j + "");
                    nv.setRemark(sql);

                    spaceList.add(nv);
                } else if (content != null && content.trim().startsWith("##PV")) {
                    NameValue nv = new NameValue();
                    nv.setName(i + "");
                    nv.setValue(j + "");
                    nv.setRemark("PV");
                    spaceList.add(nv);
                }
            }
            spaceRow++;
        }
        spaceRow--;
        String res = "";
        WritableCell n = null;
        logger.info("sheet=" + sheet.getName());
        int nowNumber = spaceRow;
        List<List<String>> dataList = getDataList(sheet, PList, spaceList, spaceRow, STAT_TYPE_ROW,isSearchHQData);


        double[][] resArray = new double[dataList.size()][dataList.get(0).size()];
        if(!"甲".equals(equipType)){
            for (int i = 0; i < dataList.size(); i++) {
                List<String> tmp = dataList.get(i);//每个省的数据
                for (int j = 1; j < tmp.size(); j++) {//从1开始因被第一个是省份名字的单元格

                    double aa = 0;
                    try {
                        String a = tmp.get(j).toString();
                        aa = Double.parseDouble(a);
                    } catch (Exception ex) {

                    }
                    resArray[i][j] = aa;
                }
            }
        }
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
        if ("甲".equals(equipType)) {//这里处理甲类汇总模板

            //集团扣分项
//            List<DedcutPageValues> dedcutPageValueses = baseDAO.find(" from DedcutPageValues where deductTpID="+tempEntityList.get(i).getObjectId());
            List<Map> dedcutList = baseDAO.findNativeSQL("select txtvalue from t_eem_deduct_page_values where reportTpID='" + sumId + "'", null);
            List<Double> decutSum = new ArrayList<Double>();
            for (int m = 0; m < dedcutList.size(); m++) {
                decutSum.add(Double.parseDouble(dedcutList.get(m).get("txtvalue").toString()));
            }


            //重新计算后评价得分及
            for (int i = 0; i < dataList.size(); i++) {
                List<Double> postScot = new ArrayList<Double>();
                List<Double> postScot2 = new ArrayList<Double>();
                List<Double> rankList = new ArrayList<Double>();
                int num = 0;
                for (int j = 1; j < dataList.get(i).size(); j++) {
                    if (j % coefficient == 0) {
                        postScot.add(Double.parseDouble(dataList.get(i).get(j - 1)));
                    }
                }
                Collections.sort(postScot);
                Map<Double, Integer> rangking = new HashMap<Double, Integer>();
                int cnum = postScot.size();
                int ss = 0;
                for (int k = 0; k < cnum; k++) {
                    if (rangking.size() > 0 && postScot.get(postScot.size() - 1 - k).equals(postScot.get(postScot.size() - k))) {
                        //ss = ss - 1;
                        continue;
                    } else {
                        rangking.put(postScot.get(postScot.size() - 1 - k), ss);
                        ss++;
                    }

                }
                for (int j = 1; j < dataList.get(i).size(); j++) {
                    if (j % coefficient == 0) {
                        if (dataList.get(i).get(j - 1).equals(0.0)) {
                            continue;
                        }
//                        if(Double.parseDouble(dataList.get(i).get(j - 1))>=postScot.get(postScot.size()-num-1)){
                        logger.info("-----------------------" + dataList.get(i).get(0));
                        System.out.println("-----------------------" + dataList.get(i).get(0));
                        if ((Double.parseDouble(dataList.get(i).get(j - 1)) != 0) && rankScoreList.get(rangking.get(Double.valueOf(dataList.get(i).get(j - 1)))) < Double.parseDouble(dataList.get(i).get(j - 1))) {
                            dataList.get(i).set(j - 1, Double.toString(rankScoreList.get(rangking.get(Double.valueOf(dataList.get(i).get(j - 1))))));
                        }
                        num++;
                        // }
                    }
                }

            }
            for (int i = 0; i < dataList.size(); i++) {
                List<String> tmp = dataList.get(i);//每个省的数据
                for (int j = 1; j < tmp.size(); j++) {//从1开始因被第一个是省份名字的单元格

                    double aa = 0;
                    try {
                        String a = tmp.get(j).toString();
                        aa = Double.parseDouble(a);
                    } catch (Exception ex) {
                    }
                    resArray[i][j] = aa;
                }
            }

            for (int r = 0; r < dataList.size(); r++) {
                for (int t = 1; t < dataList.get(r).size(); t++) {
                    if (t % coefficient == 0) {
                        Cell cell = sheet.getRow(nowNumber)[t];
                        try {
                            Label lable1 = new Label(t, nowNumber, dataList.get(r).get(t - 1), cell.getCellFormat());
                            sheet.addCell(lable1);
                        } catch (Exception e) {
                            logger.info(sheet.getName() + "后评价得分：" + r + "=======" + (t - 1));
                            e.printStackTrace();
                        }
                    }
                }
                nowNumber++;
            }


            //全国合计行
            List<String> resList = new ArrayList<String>();//保存合计行的数据
            resList.add("省内项目集采得分");
            for (int i = 0; i < dataList.size(); i++) {
                List<String> tmp = dataList.get(i);//每个省的数据
                for (int j = 1; j < tmp.size(); j++) {//从1开始因被第一个是省份名字的单元格
                    if (resList.size() > j) {
                        String str = resList.get(j);
                        double resDouble = Double.parseDouble(str);
                        String tmpStr = tmp.get(j);
                        double tmpDouble = Double.parseDouble(tmpStr);
                        resDouble = resDouble + tmpDouble;
                        resList.set(j, resDouble + "");

                    } else if (resList.size() == j) {
                        String str = tmp.get(j);

                        resList.add(str);
                    }
                }
            }

////////////////////////////////////////add

            int verNum = (int) ((dataList.get(0).size() - 1) / coefficient); //厂家数量；
            double[] verNumArray = new double[dataList.get(0).size()]; //保存各个厂家省份覆盖数量
            for (int i = 1; i < dataList.get(0).size(); i++) {
                if (i % coefficient == 0) { //取总分
                    for (int j = 0; j < dataList.size(); j++) {
                        String qStr = dataList.get(j).get(i - 1);
                        double value = Double.valueOf(qStr);
                        if (value > 0) {
                            //int coefficientInt=(int)coefficient;
                            //int index=i/coefficientInt-1;
                            verNumArray[i - 1]++;
                        }
                    }
                }
            }
            double maxNum = 0;//取省份覆盖最多的数量
            double[] markValue = new double[dataList.get(0).size()];
            for (int i = 0; i < verNumArray.length; i++) {

                if (verNumArray[i] > maxNum) {
                    maxNum = verNumArray[i];  //取省份覆盖最多的数量
                }
            }
            if (verNum > 4 && verNum <= 10) {  //供应商数量4至10家的情况
                for (int i = 0; i < verNumArray.length; i++) {
                    if (verNumArray[i] > 0) {
                        if (verNumArray[i] == maxNum) {
                            markValue[i] = 0;                            //供应商设备应用最多省份不扣分
                        } else {
                            markValue[i] = (1 - verNumArray[i] / maxNum) * 20;   //(供应商提供省份数量/供应商设备应用最多省份数量)*20
                        }
                    }

                }
            }
            if (verNum > 10) {  //供应商数量大于10家的情况
                for (int i = 0; i < verNumArray.length; i++) {
                    if (verNumArray[i] > 0) {
                        if (verNumArray[i] > 15) {
                            markValue[i] = 0;
                        } else {
                            markValue[i] = (15 - verNumArray[i]) * 1.5;
                        }
                    }

                }
            }

            ////////////////////////////////////////////////////add


            List<Double> sortList = new ArrayList<Double>();
            for (int i = 0; i < resList.size(); i++) {
                try {
                    if (i % coefficient == 0) {
                        sortList.add(Double.parseDouble(resList.get(i - 1)));
                    }
                } catch (Exception ex) {

                }
            }
            Collections.sort(sortList);
            int a = (int) (sortList.size() * 0.7);
            double aMin = sortList.get(a) / PList.size();
            WritableCell rCell = null;

            for (int j = 0; j < resList.size(); j++) {
                try {
                    double tt = Double.parseDouble(resList.get(j - 1));
                    tt = tt / PList.size();

                    if (j % coefficient == 0) {//总分项在这里处理
                       if(coefficient==4){
                           if (tt < 60 && tt >= 40) {
                               rCell = new Label(j - 3, PList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.RED, 12));
                           } else if (tt < 40) {
                               rCell = new Label(j - 3, PList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                           } else {
                               if (tt >= aMin) {
                                   rCell = new Label(j - 3, PList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                               } else {
                                   rCell = new Label(j - 3, PList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                               }
                           }
                           sheet.addCell(rCell);//具体数字
                           sheet.mergeCells(j - 3, PList.size() + spaceRow, j, PList.size() + spaceRow);

                       }else if(coefficient==5){
                           if (tt < 60 && tt >= 40) {
                               rCell = new Label(j - 4, PList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.RED, 12));
                           } else if (tt < 40) {
                               rCell = new Label(j - 4, PList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                           } else {
                               if (tt >= aMin) {
                                   rCell = new Label(j - 4, PList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                               } else {
                                   rCell = new Label(j - 4, PList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                               }
                           }
                           sheet.addCell(rCell);//具体数字
                           sheet.mergeCells(j - 4, PList.size() + spaceRow, j, PList.size() + spaceRow);

                       }

                    } else {
                        //	rCell = new Number(j,PList.size()+spaceRow,tt);
                    }
                } catch (Exception ex) {
                    rCell = new Label(j, PList.size() + spaceRow, resList.get(j), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                    sheet.addCell(rCell);//具体数字
                }
            }


            ///甲类第二种
            //获得每个厂家的最小得分
            double[] minArray = new double[dataList.get(0).size()];
            double t = 999999;
            for (int i = 1; i < dataList.get(0).size(); i++) {
                t = 999999;
                for (int j = 0; j < dataList.size(); j++) {
                    double each = resArray[j][i];
                    if (each < t && each > 0) {
                        t = each;
                    }
                }
                if (t > 999990) {
                    minArray[i] = 0;
                } else {
                    minArray[i] = t;
                }
            }

            //获得每个厂家的最小得分
            resList = new ArrayList<String>();//保存合计行的数据
            resList.add("总部项目集采得分");
            for (int i = 0; i < dataList.size(); i++) {
                List<String> tmp = dataList.get(i);//每个省的数据
                for (int j = 1; j < tmp.size(); j++) {//从1开始因被第一个是省份名字的单元格
                    if (resList.size() > j) {
                        String str = resList.get(j);
                        double resDouble = Double.parseDouble(str);
                        String tmpStr = tmp.get(j);
                        double tmpDouble = Double.parseDouble(tmpStr);

                        if (tmpDouble < 1) {
                            tmpDouble = minArray[j];
                        }
                        resDouble = resDouble + tmpDouble;

                        resList.set(j, resDouble + "");

                    } else if (resList.size() == j) {
                        String str = tmp.get(j);
                        double resDouble = Double.parseDouble(str);
                        if (resDouble < 1) {
                            resDouble = minArray[j];
                        }
                        resList.add(resDouble + "");
                    }
                }
            }


            sortList = new ArrayList<Double>();
            for (int i = 0; i < resList.size(); i++) {
                try {
                    if (i % coefficient == 0) {
                        sortList.add(Double.parseDouble(resList.get(i - 1)));
                    }
                } catch (Exception ex) {

                }
            }
            Collections.sort(sortList);
            a = (int) (sortList.size() * 0.7);

            aMin = sortList.get(a) / PList.size();
            rCell = null;
            for (int j = 0; j < resList.size(); j++) {
                try {
                    double tt = Double.parseDouble(resList.get(j - 1));
                    tt = tt / PList.size();
                    if (j % coefficient == 0) {//总分项在这里处理
                        if(coefficient==4){
                            if (tt < 60 && tt >= 40) {
                                rCell = new Label(j - 3, PList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.RED, 12));
                            } else if (tt < 40) {
                                rCell = new Label(j - 3, PList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                            } else {
                                if (tt >= aMin) {
                                    rCell = new Label(j - 3, PList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                                } else {
                                    rCell = new Label(j - 3, PList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                                }
                            }
                            sheet.addCell(rCell);//具体数字
                            sheet.mergeCells(j - 3, PList.size() + spaceRow + 1, j, PList.size() + spaceRow);
                        }else if(coefficient==5) {

                            if (tt < 60 && tt >= 40) {
                                rCell = new Label(j - 4, PList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.RED, 12));
                            } else if (tt < 40) {
                                rCell = new Label(j - 4, PList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                            } else {
                                if (tt >= aMin) {
                                    rCell = new Label(j - 4, PList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                                } else {
                                    rCell = new Label(j - 4, PList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                                }
                            }
                            sheet.addCell(rCell);//具体数字
                            sheet.mergeCells(j - 4, PList.size() + spaceRow + 1, j, PList.size() + spaceRow);
                        }
                    } else {
                        //rCell = new Number(j,PList.size()+spaceRow+1,tt);
                    }


                } catch (Exception ex) {

                    rCell = new Label(j, PList.size() + spaceRow + 1, resList.get(j), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                    sheet.addCell(rCell);//具体数字
                }
                //sheet.addCell(rCell);//具体数字
            }
            //总部扣分 stat
            resList = new ArrayList<String>();//保存合计行的数据
            resList.add("总部扣分");
            for (int i = 0; i < dataList.size(); i++) {
                List<String> tmp = dataList.get(i);//每个省的数据
                for (int j = 1; j < tmp.size(); j++) {//从1开始因被第一个是省份名字的单元格
                    if (resList.size() > j) {
                        String str = resList.get(j);
                        double resDouble = Double.parseDouble(str);
                        String tmpStr = tmp.get(j);
                        double tmpDouble = Double.parseDouble(tmpStr);

                        if (tmpDouble < 1) {
                            tmpDouble = minArray[j];
                        }
                        resDouble = resDouble + tmpDouble;

                        resList.set(j, resDouble + "");

                    } else if (resList.size() == j) {
                        String str = tmp.get(j);
                        double resDouble = Double.parseDouble(str);
                        if (resDouble < 1) {
                            resDouble = minArray[j];
                        }
                        resList.add(resDouble + "");
                    }
                }
            }


            sortList = new ArrayList<Double>();
            for (int i = 0; i < resList.size(); i++) {
                try {
                    if (i % coefficient == 0) {
                        sortList.add(Double.parseDouble(resList.get(i - 1)));
                    }
                } catch (Exception ex) {

                }
            }
            Collections.sort(sortList);
            a = (int) (sortList.size() * 0.7);

            aMin = sortList.get(a) / PList.size();
            rCell = null;
            int num = 0;
            for (int j = 0; j < resList.size(); j++) {
                try {

                    double tt = Double.parseDouble(resList.get(j));
                    tt = tt / PList.size();

                    if (j % coefficient == 0) {//总分项在这里处理
                            if(coefficient==4){
                                if (num < decutSum.size()) {
                                    rCell = new Label(j - 3, PList.size() + spaceRow + 2, df.format(decutSum.get(num)), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                                } else
                                    rCell = new Label(j - 3, PList.size() + spaceRow + 2, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                                num++;
                                sheet.addCell(rCell);//具体数字
                                sheet.mergeCells(j - 3, PList.size() + spaceRow + 2, j, PList.size() + spaceRow);
                            }else if(coefficient==5) {
                                if (num < decutSum.size()) {
                                    rCell = new Label(j - 4, PList.size() + spaceRow + 2, df.format(decutSum.get(num)), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                                } else
                                    rCell = new Label(j - 4, PList.size() + spaceRow + 2, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                                num++;
                                sheet.addCell(rCell);//具体数字
                                sheet.mergeCells(j - 4, PList.size() + spaceRow + 2, j, PList.size() + spaceRow);
                            }
                    } else {
                        //rCell = new Number(j,PList.size()+spaceRow+1,tt);
                    }


                } catch (Exception ex) {

                    rCell = new Label(j, PList.size() + spaceRow + 2, resList.get(j), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                    sheet.addCell(rCell);//具体数字
                }
                //sheet.addCell(rCell);//具体数字
            }
            //总部扣分  end

///////////////////////add
            resList = new ArrayList<String>();//
            resList.add("2017年后评价得分");
            for (int i = 0; i < dataList.size(); i++) {
                List<String> tmp = dataList.get(i);//每个省的数据
                for (int j = 1; j < tmp.size(); j++) {//从1开始因被第一个是省份名字的单元格
                    if (resList.size() > j) {
                        String str = resList.get(j);
                        double resDouble = Double.parseDouble(str);
                        String tmpStr = tmp.get(j);
                        double tmpDouble = Double.parseDouble(tmpStr);
                        if (tmpDouble < 1) {
                            tmpDouble = minArray[j];
                        }
                        resDouble = resDouble + tmpDouble;
                        resList.set(j, resDouble + "");

                    } else if (resList.size() == j) {
                        String str = tmp.get(j);
                        double resDouble = Double.parseDouble(str);
                        if (resDouble < 1) {
                            resDouble = minArray[j];
                            resList.add(resDouble+"");
                        }else{
                            resList.add(str);
                        }
                    }
                }
            }
            sortList = new ArrayList<Double>();
            for (int i = 0; i < resList.size(); i++) {
                try {
                    if (i % coefficient == 0) {
                        sortList.add(Double.parseDouble(resList.get(i - 1)));
                    }
                } catch (Exception ex) {

                }
            }
            Collections.sort(sortList);
            a = (int) (sortList.size() * 0.3);

            aMin = sortList.get(a) / PList.size();
            rCell = null;
            num = 0;
            for (int j = 0; j < resList.size(); j++) {
                try {
                    double tt = 0;
                    if (j % coefficient == 0) {
                        tt = Double.parseDouble(resList.get(j - 1));
                        if (verNumArray[j - 1] != 0.0) {
                            tt = tt / PList.size();
                        }

                    }

                    if (j % coefficient == 0) {//总分项在这里处理
                        double dedcut = 0.0;
                        if (num < decutSum.size()) {
                            dedcut = decutSum.get(num);
                        }
                        num++;
                        if(coefficient==4){
                            if (tt >= aMin) {
                                if (tt - dedcut < 0) {
                                    rCell = new Label(j - 3, PList.size() + spaceRow + 3, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                                } else {
                                    rCell = new Label(j - 3, PList.size() + spaceRow + 3, df.format(tt - dedcut), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                                }
                            } else {
                                if (tt - dedcut < 0) {
                                    rCell = new Label(j - 3, PList.size() + spaceRow + 3, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12)); //后70%，标记成白色

                                } else {
                                    rCell = new Label(j - 3, PList.size() + spaceRow + 3, df.format(tt - dedcut), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12)); //后70%，标记成白色
                                }
                            }

                            sheet.addCell(rCell);//具体数字
                            sheet.mergeCells(j - 3, PList.size() + spaceRow + 3, j, PList.size() + spaceRow);
                        }else if(coefficient==5) {
                            if (tt >= aMin) {
                                if (tt - dedcut < 0) {
                                    rCell = new Label(j - 4, PList.size() + spaceRow + 3, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                                } else {
                                    rCell = new Label(j - 4, PList.size() + spaceRow + 3, df.format(tt - dedcut), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                                }
                            } else {
                                if (tt - dedcut < 0) {
                                    rCell = new Label(j - 4, PList.size() + spaceRow + 3, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12)); //后70%，标记成白色

                                } else {
                                    rCell = new Label(j - 4, PList.size() + spaceRow + 3, df.format(tt - dedcut), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12)); //后70%，标记成白色
                                }
                            }

                            sheet.addCell(rCell);//具体数字
                            sheet.mergeCells(j - 4, PList.size() + spaceRow + 3, j, PList.size() + spaceRow);
                        }
                    } else {
                        //rCell = new Number(j,PList.size()+spaceRow+1,tt);
                    }
                } catch (Exception ex) {
                    rCell = new Label(j, PList.size() + spaceRow + 3, resList.get(j), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                    sheet.addCell(rCell);//具体数字
                }
            }

            ///////////////////////////
            //新增2017年后评价得分
        } else {
            if (!sheet.getName().equals("各省得分") && ("乙".equals(equipType) || "丙".equals(equipType))) {
                double[] minArray = new double[dataList.get(0).size()];
                double[] mark = new double[dataList.get(0).size()];//保存最后的规模效益得分
                double tmin = 999999;
                for (int i = 1; i < dataList.get(0).size(); i++) {
                    tmin = 999999;
                    int t = 0;//保存有几个省有得分
                    for (int j = 0; j < dataList.size(); j++) {
                        double each = resArray[j][i];
                        if (each > 0) {
                            t++;
                        }
                        if (each < tmin && each > 0) {
                            tmin = each;
                        }
                    }
                    if (t > 10) {
                        mark[i] = 100;
                    } else {
                        mark[i] = t * 10;
                    }

                    if (tmin > 999990) {
                        minArray[i] = 0;
                    } else {
                        minArray[i] = tmin;
                    }
                }

                ////////////////////////////////////////add
//            double minGroupFenshu;
                int verNum = (int) ((dataList.get(0).size() - 1) / coefficient); //厂家数量；
//                double[] verNumArray = new double[dataList.get(0).size()]; //保存各个厂家省份覆盖数量
//                int verNum = arrayList.get(0).size(); //厂家数量；
                double[] verNumArray = new double[dataList.get(0).size()]; //保存各个厂家省份覆盖数量
                //省分使用该厂家的设备占该省所有设备的百分比大于5%，计算为有效覆盖
                for(int i=0;i<verNum;i++){
                    for(OrgEntity org:PList){
                        String orgCode = org.getOrgCode();
                        double sum = 0;
                        double fenSum = 0;
                        for(ArrayList<HashMap> list:arrayList){
                            for(HashMap<String,Double> map:list){
                                sum += map.get(orgCode);
                            }
                            HashMap<String,Double> map1 = list.get(i);
                            fenSum += map1.get(orgCode);
                        }
                        if((fenSum/sum)>=0.05){
                            verNumArray[((int) ((i + 1) * coefficient))]++;
                        }
                    }

                }


//                for (int i = 1; i < dataList.get(0).size(); i++) {
//                    //double markY=0;
//                    if (i % coefficient == 0) { //取总分
//                        //循环一次得到一次总分，并且得到的是各个省份的
//                        for (int j = 0; j < dataList.size(); j++) {
//                            String qStr = dataList.get(j).get(i);
//                            double value = Double.valueOf(qStr);
//                            //比较总分，比较各个省份总分最小的
//                            //List<Double> groupList = new ArrayList<Double>();
//
//                            if (value > 0) {
//                                verNumArray[i]++;
//
//                            }
//                        }
//                    }
//                }
                double maxNum = 0;
                double[] markValue = new double[dataList.get(0).size()];
                double[] markValue2 = new double[dataList.get(0).size()];//规模效益分  最大20
                for (int i = 0; i < verNumArray.length; i++) {

                    if (verNumArray[i] > maxNum) {
                        maxNum = verNumArray[i];  //取省份覆盖最多的数量
                    }
                }
                if (verNum > 4 && verNum <= 10) {  //供应商数量4至10家的情况
                    for (int i = 0; i < verNumArray.length; i++) {
                        if (verNumArray[i] > 0) {
                            if (verNumArray[i] == maxNum) {
                                markValue[i] = 0;                            //供应商设备应用最多省份不扣分
                            } else {
                                markValue[i] = (1 - verNumArray[i] / maxNum) * 20;   //(供应商提供省份数量/供应商设备应用最多省份数量)*20
                            }
                        }

                    }
                }
                if (verNum > 10) {  //供应商数量大于10家的情况
                    for (int i = 0; i < verNumArray.length; i++) {
                        if (verNumArray[i] > 0) {
                            if (verNumArray[i] > 15) {
                                markValue[i] = 0;
                            } else {
                                markValue[i] = (15 - verNumArray[i]) * 1.5;
                            }
                        }

                    }
                }

                ////////////////////////////////////////////////////add
                List<String> resList = new ArrayList<String>();//保存合计行的数据
                resList.add("总部项目集采得分");
                for (int i = 0; i < dataList.size(); i++) {
                    List<String> tmp = dataList.get(i);//每个省的数据
                    for (int j = 1; j < tmp.size(); j++) {//从1开始因被第一个是省份名字的单元格
                        if (resList.size() > j) {
                            String str = verNumArray[j - 1] + "";
                            double resDouble = Double.parseDouble(str);
                            String tmpStr = tmp.get(j);
                            double tmpDouble = Double.parseDouble(tmpStr);
                            if (tmpDouble < 1) {
                                tmpDouble = minArray[j];
                            }
                            resDouble = resDouble + tmpDouble;
                            resList.set(j, resDouble + "");
                        } else if (resList.size() == j) {
                            String str = tmp.get(j);
                            double resDouble = Double.parseDouble(str);
                            if (resDouble < 0.01) {
                                resDouble = minArray[j];
                            }
                            resList.add(resDouble + "");
                        }
                    }
                }
                //总部项目集采得分分数列表
                List<Double> sortList = new ArrayList<Double>();
                for (int i = 0; i < resList.size(); i++) {
                    try {
                        if (i % coefficient == 0) {
                            double tt = Double.parseDouble(resList.get(i));
                            tt = (tt / PList.size()) * 0.8 + mark[i] * 0.2;
                            sortList.add(tt);
                        }
                    } catch (Exception ex) {
                    }
                }
                Collections.sort(sortList);
                int a = (int) (sortList.size() * 0.7);

                //double aMin = sortList.get(a)/PList.size();
                double aMin = sortList.get(a);


                WritableCell rCell = null;
                for (int j = 0; j < resList.size(); j++) {
                    try {
                        double tt = Double.parseDouble(resList.get(j));
                        tt = (tt / PList.size()) * 0.8 + mark[j] * 0.2;
                        if (j % coefficient == 0) {//总分项在这里处理
                            if (tt < 60) {
//                                rCell = new Label(j - 2, PList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.RED, 12));
                                rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.RED, 12));
                            } else {
                                if (tt >= aMin) {
//                                    rCell = new Label(j - 2, PList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                                    rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.RED, 12));
                                } else {
//                                    rCell = new Label(j - 2, PList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                                    rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.RED, 12));
                                }
                            }
                            sheet.addCell(rCell);//具体数字
//                            sheet.mergeCells(j - 2, PList.size() + spaceRow, j, PList.size() + spaceRow);
                            sheet.mergeCells(j - (int)coefficient+1, PList.size() + spaceRow, j, PList.size() + spaceRow);
                        } else {
                            //rCell = new Number(j,PList.size()+spaceRow,tt);
                            //sheet.addCell(rCell);//具体数字
                        }
                    } catch (Exception ex) {
                        rCell = new Label(j, PList.size() + spaceRow, resList.get(j), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                        sheet.addCell(rCell);//具体数字
                    }


                }
                resList.set(0, "建议暂缓下期集采");
                rCell = null;
                for (int j = 0; j < resList.size(); j++) {
                    try {
                        double tt = Double.parseDouble(resList.get(j));
                        tt = tt / PList.size();
                        if (j % coefficient == 0) {//总分项在这里处理
                            if (tt < 40) {
//                                rCell = new Label(j - 2, PList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                                rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                            } else {

//                                rCell = new Label(j - 2, PList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                                rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                            }
                            sheet.addCell(rCell);//具体数字
//                            sheet.mergeCells(j - 2, PList.size() + spaceRow + 1, j, PList.size() + spaceRow);
                            sheet.mergeCells(j - (int)coefficient+1, PList.size() + spaceRow + 1, j, PList.size() + spaceRow);
                        } else {
                            //rCell = new Number(j,PList.size()+spaceRow,tt);
                            //sheet.addCell(rCell);//具体数字
                        }
                    } catch (Exception ex) {
                        rCell = new Label(j, PList.size() + spaceRow + 1, resList.get(j), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                        sheet.addCell(rCell);//具体数字
                    }
                }


                /////////////////////////add

                resList = new ArrayList<String>();//
                List<Double> minList = new ArrayList<Double>();
                List<Double> minList1 = new ArrayList<Double>();

                resList.add("后评价得分");
                //Apple----
//                for (int i = 0; i < dataList.size(); i++) {
//                    List<String> tmp = dataList.get(i);//每个省的数据
//
//                    for (int j = 1; j < tmp.size(); j++) {//从1开始因为第一个是省份名字的单元格
//                        if (i % coefficient == 0 && i > 0) {
//                            minList.add(Double.parseDouble(resList.get(i)));
//
//                        }
//                    }
//                }

                for (int i = 0; i < dataList.size(); i++) {
                    List<String> tmp = dataList.get(i);//每个省的数据
                    for (int j = 1; j < tmp.size(); j++) {//从1开始因被第一个是省份名字的单元格
                        if (resList.size() > j) {
                            String str = resList.get(j);
                            double resDouble = Double.parseDouble(str);
                            String tmpStr = tmp.get(j);
                            double tmpDouble = Double.parseDouble(tmpStr);
                            resDouble = resDouble + tmpDouble;
                            resList.set(j, resDouble + "");

                        } else if (resList.size() == j) {
                            String str = tmp.get(j);
                            resList.add(str);
                        }
                    }
                }
                sortList = new ArrayList<Double>();
                for (int i = 0; i < resList.size(); i++) {
                    try {
                        //Apple------
                        if (i % coefficient == 0) {
                            sortList.add(Double.parseDouble(resList.get(i)));
                        }
                    } catch (Exception ex) {

                    }
                }
                Collections.sort(sortList);
                a = (int) (sortList.size() * 0.3);

                aMin = sortList.get(a) / PList.size();
                rCell = null;
                for (int j = 0; j < resList.size(); j++) {
                    try {
                        double tt = 0;
                        double minGroupFenshu = 0;
                        List<Double> groupMinList = new ArrayList<Double>();
                        if (j % coefficient == 0 && verNumArray[j] > 0) {
                            //Apple-----
                            for (int i = 1; i < dataList.get(0).size(); i++) {//lie
                                //double markY=0;
                                List<Double> groupList = new ArrayList<Double>();
                                //循环一次得到一次总分，并且得到的是各个省份的
                                for (int m = 0; m < dataList.size(); m++) {
                                    String qStr = dataList.get(m).get(j);
                                    double value = Double.valueOf(qStr);
                                    if (value > 0) {
                                        groupList.add(value);
                                    }
                                    Collections.sort(groupList);
                                }
                                //取出各个省份总分最小的同一厂家最小总分
                                groupMinList.add(groupList.get(0));
                            }
                            minGroupFenshu =(groupMinList.get(0));
                            System.out.println("---------minGroupFenshu------------"+minGroupFenshu);
                            tt = Double.parseDouble(resList.get(j));

                            tt = tt + minGroupFenshu * (PList.size() - verNumArray[j]);
                            tt = tt/PList.size();
//                        tt = Double.parseDouble(resList.get(j));
//                        tt = tt / verNumArray[j];
                        }

                        if (j % coefficient == 0) {//总分项在这里处理
                            int coefficientInt = (int) coefficient;
                            double verNumVaule = verNumArray[j];
                            if (verNumArray[j] > 20) {
                                verNumVaule = 20;
                            }

                            if (tt >= aMin) {
                                if (tt - markValue[j] < 0) {
//                                    rCell = new Label(j - 2, PList.size() + spaceRow + 2, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                                    rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow + 2, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                                } else {
//                                    rCell = new Label(j - 2, PList.size() + spaceRow + 2, df.format(tt * 0.8 + verNumVaule), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                                    rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow + 2, df.format(tt * 0.8 + verNumVaule), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                                }
                            } else {
                                if (tt - markValue[j] < 0) {
//                                    rCell = new Label(j - 2, PList.size() + spaceRow + 2, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                                    rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow + 2, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                                } else {
//                                    rCell = new Label(j - 2, PList.size() + spaceRow + 2, df.format(tt * 0.8 + verNumVaule), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                                    rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow + 2, df.format(tt * 0.8 + verNumVaule), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                                }

                            }
                            sheet.addCell(rCell);//具体数字
//                            sheet.mergeCells(j - 2, PList.size() + spaceRow + 2, j, PList.size() + spaceRow);
                            sheet.mergeCells(j - (int)coefficient+1, PList.size() + spaceRow + 2, j, PList.size() + spaceRow);
                        } else {
                            //rCell = new Number(j,PList.size()+spaceRow+1,tt);
                        }
                    } catch (Exception ex) {
                        rCell = new Label(j, PList.size() + spaceRow + 2, resList.get(j), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                        sheet.addCell(rCell);//具体数字
                    }
                }
                ///////////////////////////
                //2017年后评价得分
                resList = new ArrayList<String>();//
                if (reportYear.equals("2015")) {
                    resList.add("2015年后评价得分");

                } else if (reportYear.equals("2016")) {
                    resList.add("2016年后评价得分");

                } else {
                    resList.add("2017年后评价得分");
                }
                for (int i = 0; i < dataList.size(); i++) {
                    List<String> tmp = dataList.get(i);//每个省的数据
                    for (int j = 1; j < tmp.size(); j++) {//从1开始因被第一个是省份名字的单元格
                        if (resList.size() > j) {
                            String str = resList.get(j);
                            double resDouble = Double.parseDouble(str);
                            String tmpStr = tmp.get(j);
                            double tmpDouble = Double.parseDouble(tmpStr);
                            resDouble = resDouble + tmpDouble;
                            resList.set(j, resDouble + "");
                        } else if (resList.size() == j) {
                            String str = tmp.get(j);
                            resList.add(str);
                        }
                    }
                }
                sortList = new ArrayList<Double>();
                for (int i = 0; i < resList.size(); i++) {
                    try {
                        if (i % coefficient == 0) {
                            sortList.add(Double.parseDouble(resList.get(i)));
                        }
                    } catch (Exception ex) {
                    }
                }
                Collections.sort(sortList);
                a = (int) (sortList.size() * 0.3);

                aMin = sortList.get(a) / PList.size();
                rCell = null;
                for (int j = 0; j < resList.size(); j++) {
                    try {
                        double tt = 0;
                        double minGroupFenshu = 0;
                        List<Double> groupMinList = new ArrayList<Double>();
                        if (j % coefficient == 0 && verNumArray[j] > 0) {
                            //Apple-----
                            for (int i = 1; i < dataList.get(0).size(); i++) {//lie
                                //double markY=0;
                                List<Double> groupList = new ArrayList<Double>();
                                //循环一次得到一次总分，并且得到的是各个省份的
                                for (int m = 0; m < dataList.size(); m++) {
                                    String qStr = dataList.get(m).get(j);
                                    double value = Double.valueOf(qStr);
                                    if (value > 0) {
                                        groupList.add(value);
                                    }
                                    Collections.sort(groupList);
                                }
                                //取出各个省份总分最小的同一厂家最小总分
                                groupMinList.add(groupList.get(0));
                            }
                            minGroupFenshu =(groupMinList.get(0));
                            System.out.println("---------minGroupFenshu------------"+minGroupFenshu);
                            tt = Double.parseDouble(resList.get(j));

                            tt = tt + minGroupFenshu * (PList.size() - verNumArray[j]);
                            tt = tt/PList.size();
//                            tt = Double.parseDouble(resList.get(j));
//                            tt = tt / verNumArray[j];
                        }

                        if (j % coefficient == 0) {//总分项在这里处理
                            int coefficientInt = (int) coefficient;
                            double verNumVaule = verNumArray[j];
                            if (verNumArray[j] > 20) {
                                verNumVaule = 20;
                            }

                            if (tt >= aMin) {
                                if (tt - markValue[j] < 0) {
//                                    rCell = new Label(j - 2, PList.size() + spaceRow + 3, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                                    rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow + 3, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                                } else {
//                                    rCell = new Label(j - 2, PList.size() + spaceRow + 3, df.format((tt * 0.8 + verNumVaule)), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                                    rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow + 3, df.format((tt * 0.8 + verNumVaule)), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                                }
                            } else {
                                if (tt - markValue[j] < 0) {
//                                    rCell = new Label(j - 2, PList.size() + spaceRow + 3, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                                    rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow + 3, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                                } else {
//                                    rCell = new Label(j - 2, PList.size() + spaceRow + 3, df.format((tt * 0.8 + verNumVaule)), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                                    rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow + 3, df.format((tt * 0.8 + verNumVaule)), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                                }
                            }
                            sheet.addCell(rCell);//具体数字
//                            sheet.mergeCells(j - 2, PList.size() + spaceRow + 3, j, PList.size() + spaceRow);
                            sheet.mergeCells(j - (int)coefficient+1, PList.size() + spaceRow + 3, j, PList.size() + spaceRow);
                        } else {
                        }
                    } catch (Exception ex) {
                        rCell = new Label(j, PList.size() + spaceRow + 3, resList.get(j), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                        sheet.addCell(rCell);//具体数字
                    }
                }

                //2017年有效规模效益分
                resList = new ArrayList<String>();//
                if (reportYear.equals("2015")) {
                    resList.add("2015年有效规模效益分");
                } else if (reportYear.equals("2016")) {
                    resList.add("2016年有效规模效益分");
                } else {
                    resList.add("2017年有效规模效益分");

                }

                for (int i = 0; i < dataList.size(); i++) {
                    List<String> tmp = dataList.get(i);//每个省的数据
                    for (int j = 1; j < tmp.size(); j++) {//从1开始因被第一个是省份名字的单元格
                        if (resList.size() > j) {
                            String str = resList.get(j);
                            double resDouble = Double.parseDouble(str);
                            String tmpStr = tmp.get(j);
                            double tmpDouble = Double.parseDouble(tmpStr);
                            resDouble = resDouble + tmpDouble;
                            resList.set(j, resDouble + "");
                        } else if (resList.size() == j) {
                            String str = tmp.get(j);
                            resList.add(str);
                        }
                    }
                }
                sortList = new ArrayList<Double>();
                for (int i = 0; i < resList.size(); i++) {
                    try {
                        if (i % coefficient == 0) {
                            sortList.add(Double.parseDouble(resList.get(i)));
                        }
                    } catch (Exception ex) {
                    }
                }
                Collections.sort(sortList);
                a = (int) (sortList.size() * 0.3);

                aMin = sortList.get(a) / PList.size();
                rCell = null;
                int p = 0;
                for (int j = 0; j < resList.size(); j++) {
                    try {
                        double tt = 0;

                        if (j % coefficient == 0 && verNumArray[j] > 0) {
                            tt = Double.parseDouble(resList.get(j));
                            tt = tt / verNumArray[j];
                        }

                        if (j % coefficient == 0) {//总分项在这里处理
                            int coefficientInt = (int) coefficient;
                            double verNumVaule = verNumArray[j];
                            if (verNumArray[j] > 20) {
                                verNumArray[j] = 20;
                            }

                            if (tt >= aMin) {
                                if (tt - markValue[j] < 0) {
//                                    rCell = new Label(j - 2, PList.size() + spaceRow + 4, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                                    rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow + 4, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                                } else {
//                                    rCell = new Label(j - 2, PList.size() + spaceRow + 4, df.format(verNumArray[j]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                                    rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow + 4, df.format(verNumArray[j]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                                }
                            } else {
                                if (tt - markValue[j] < 0) {
//                                    rCell = new Label(j - 2, PList.size() + spaceRow + 4, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                                    rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow + 4, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                                } else {
//                                    rCell = new Label(j - 2, PList.size() + spaceRow + 4, df.format(verNumArray[j]), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                                    rCell = new Label(j - (int)coefficient+1, PList.size() + spaceRow + 4, df.format(verNumArray[j]), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                                }
                            }
                            sheet.addCell(rCell);//具体数字
//                            sheet.mergeCells(j - 2, PList.size() + spaceRow + 4, j, PList.size() + spaceRow);
                            sheet.mergeCells(j - (int)coefficient+1, PList.size() + spaceRow + 4, j, PList.size() + spaceRow);
                        } else {
                        }
                    } catch (Exception ex) {
                        rCell = new Label(j, PList.size() + spaceRow + 4, resList.get(j), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                        sheet.addCell(rCell);//具体数字
                    }
                }



/*
            resList = new ArrayList<String>();//
            resList.add("2017年有效规模效益分");
            for (int i = 0; i < dataList.size(); i++) {
                List<String> tmp = dataList.get(i);//每个省的数据
                for (int j = 1; j < tmp.size(); j++) {//从1开始
                    if (resList.size() > j) {
                        String str = resList.get(j);
                        double resDouble = Double.parseDouble(str);
                        String tmpStr = tmp.get(j);
                        double tmpDouble = Double.parseDouble(tmpStr);
                        resDouble = resDouble + tmpDouble;
                        resList.set(j, resDouble + "");
                    } else if (resList.size() == j) {
                        String str = tmp.get(j);
                        resList.add(str);
                    }
                }
            }

            for (int j = 1; j < resList.size(); j++) {
                try {
                    double tt = 0;
                    if (j % coefficient == 0 && verNumArray[j] > 0) {
                        tt = Double.parseDouble(resList.get(j));
                        tt = tt / verNumArray[j];
                    }

                    if (j % coefficient == 0) {//总分项在这里处理
                        if (tt >= aMin) {
                            if (tt - markValue[j] < 0) {
                                rCell = new Label(j - 2, PList.size() + spaceRow + 4, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                            } else {
                                rCell = new Label(j - 2, PList.size() + spaceRow + 4, df.format(verNumArray[j]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                            }
                        } else {
                            if (tt - markValue[j] < 0) {
                                rCell = new Label(j - 2, PList.size() + spaceRow + 4, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                            } else {
                                rCell = new Label(j - 2, PList.size() + spaceRow + 4, df.format(verNumArray[j]), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                            }
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(j - 2, PList.size() + spaceRow + 4, j, PList.size() + spaceRow);
                    } else {
                    }
                } catch (Exception ex) {
                    rCell = new Label(j, PList.size() + spaceRow + 4,df.format(verNumArray[j]), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                    sheet.addCell(rCell);//具体数字
                }

            }*/
            } else if ("乙1".equals(equipType)) {


            }
        }
    }

    private void dealSheetForGUANGLANnew2(WritableWorkbook wwb, WritableSheet sheet, List<OrgEntity> PList, String reportDateStr, boolean isSearchHQData, String dealType, String reportYear, double coefficient, String equipType, String content, String formId) throws Exception{
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
        WritableSheet ws = null;
        for (WritableSheet wws : wwb.getSheets()) {
            if (wws.getName().equals("分省后评价得分")) {//获取汇总模板中分省后评价得分的sheet页
                ws = wws;
            }
        }
        ws.setColumnView(0, 50);
        List<NameValue> spaceList = new ArrayList<NameValue>();
        int spaceRow = 0;//填充数据到的具体的行数5

        String companyListSQL = "";


        if ("".equals(reportYear)) {
            if (content.equals(DIE_XING_GUANG_LAN)) {//碟形光缆
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='72' and  (iswithdraw is  null or iswithdraw<>'static') and  (reportyear='2015' or reportyear='2016')";
                pix = 16;
            } else if (content.contains(GUANG_LAN)) {//光缆
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='47' and (iswithdraw is  null or iswithdraw<>'static') and  (reportyear='2015' or reportyear='2016')";
                pix = 16;
            } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {//碟形光缆测试
                pix = 12;
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='71' and (iswithdraw is  null or iswithdraw<>'static') and  (reportyear='2015' or reportyear='2016')";
            } else if (content.contains(GUANG_LAN_CE_SHI)) {//光缆测试
                pix = 6;
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='46' and (iswithdraw is  null or iswithdraw<>'static') and  (reportyear='2015' or reportyear='2016')";
            }
        } else {
            if (content.equals(DIE_XING_GUANG_LAN)) {
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='72' and  (iswithdraw is  null or iswithdraw<>'static') and  reportyear='" + reportYear + "'";
                pix = 16;
            } else if (content.contains(GUANG_LAN)) {
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='47' and (iswithdraw is  null or iswithdraw<>'static') and  reportyear='" + reportYear + "'";
                pix = 16;
            } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {
                pix = 12;
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='71' and (iswithdraw is  null or iswithdraw<>'static') and  reportyear='" + reportYear + "'";
            } else if (content.contains(GUANG_LAN_CE_SHI)) {
                pix = 6;
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='46' and (iswithdraw is  null or iswithdraw<>'static') and  reportyear='" + reportYear + "'";
            }
        }


//        List<Map> companyList = hibDao.queryBySql(companyListSQL);
        List<Map> companyList = baseDAO.findNativeSQL(companyListSQL, null);
        if (companyList.size() > 0) {
            //新增加start
            //将厂家写到新的sheet页中
            for (int k = 0; k < companyList.size(); k++) {
                String name = companyList.get(k).get("pagename").toString();
                Label label = new Label(0, k + 1, name, ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                ws.addCell(label);
            }
            //将省份写入到sheet页中
            for (int k = 0; k < PList.size(); k++) {
                String name = PList.get(k).getOrgName();
                Label label = new Label(k + 1, 0, name, ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                ws.addCell(label);
            }
            //end


            spaceRow = getSpaceList(sheet, reportDateStr, dealType, reportYear, spaceList, isSearchHQData);
            int verdorNum = companyList.size();  // 现网供应商数量，取sheet数量
            double[] pvNum = new double[verdorNum];  //存放各个厂家提供服务的省份数量

            //double[][] allFenshu = new double[companyList.size()][9];//新增加两列2014版后评价平均分，2014版后评价得分  2014.10.10 btliu
            //蝶形光缆测试    蝶形光缆综合   添加2017年版后评价得分   2017年版有效规模效益分
            double[][] allFenshu = new double[companyList.size()][9 + 2];
            double[] sumGroupFenshu = new double[companyList.size()];//存放每个厂家的分省后评价得分和
            double[] minGroupFenshu = new double[companyList.size()];//存放每个厂家覆盖省份中最小分省后评价得分
            //存放各个厂商光缆总长度
            double[] lengths = new double[companyList.size()];

            //double[] pvLength_ = new double[31];//存放各个省份的光缆长度  最多有31个省份

            for (int z = 0; z < companyList.size(); z++) {

                String company = companyList.get(z).get("pagename").toString();
                logger.info("~~~~~~~~~`" + z + "!!!" + company);
                List<List<String>> dataList = getDataListGUANGLAN(sheet, PList, spaceList, spaceRow, STAT_TYPE_COL, company, z, content);
                if (dataList.get(0).size() == 0) {
                    continue;
                }
                double[][] resArray = getResArray(dataList);
                double[] sumArray = new double[dataList.get(0).size()];//各种指标的和
                double[][] pvSumArray = new double[dataList.size()][dataList.get(0).size()];
                double[] sumFenshuArray = new double[dataList.get(0).size()];//各种指标的分数的和
                double[] mark = new double[dataList.get(0).size()];//保存最后的规模效益得分
                double real = 0;
                double[] pvReal = new double[dataList.size()];
                double PvFenshu = 0;
                double[] finalArray = new double[dataList.get(0).size()];
                double[][] pvSumFenshuArray = new double[dataList.size()][dataList.get(0).size()];
                double[] pvLength = new double[dataList.size()]; //各个省份的光缆总长度
                int markCount = 0;
                int pvTindex = 1;
                Set<String> tmpSet = new HashSet<String>();
                List<Double> tempPvReal = new ArrayList<Double>();//用于计算min(ai) 2014.10.10 btliu
                if (content.contains(DIE_XING_GUANG_LAN)) {

                    for (int i = 0; i < resArray[0].length; i++) {
                        for (int j = 0; j < resArray.length; j++) {
                            if (i >= 0 && i <= 5 && resArray[j][i] != 0) {
                                tmpSet.add(j + "");
                            }

                            if (i >= 10 && i <= 14) {
                                if (sumArray[i] < resArray[j][i]) {
                                    sumArray[i] = resArray[j][i];
                                }
                            } else {
                                sumArray[i] = sumArray[i] + resArray[j][i];
                            }
                        }
                    }

                    ////add
                    for (int i = 0; i < resArray.length; i++) {
                        int index = 1;
                        pvLength[i] = resArray[i][index++] + resArray[i][index++] + resArray[i][index++] + resArray[i][index++];
                        pvLength_[i] =  pvLength[i];
                        if (pvLength[i] > 0) {
                            pvSumFenshuArray[i][index] = 15 - 15 * (resArray[i][index++] / pvLength[i]);
                            pvSumFenshuArray[i][index] = 5 - 5 * (resArray[i][index++] / pvLength[i]);
                            pvSumFenshuArray[i][index] = 15 - 15 * (resArray[i][index++] / pvLength[i]);

                            double tmp = 0.0;
                            if (resArray[i][index + 1] == 0.0) {
                                tmp = 0;
                            } else {
                                tmp = resArray[i][index] / resArray[i][index + 1];//故障率  tindex=9   维护2新增接头的衰减（双向平均0.04dB以上的芯数）(注意：是光纤芯数,不是光缆接头个数！！！)/接头总芯数
                            }

                            double pvfaultPercent = tmp;   //0.026898130484547884
                            if (tmp > 0.1) {
                                tmp = 0.1;
                            }
                            pvSumFenshuArray[i][index] = 20 - 200 * (tmp);

                            index = index + 2;
                            pvSumFenshuArray[i][index] = resArray[i][index++];
                            pvSumFenshuArray[i][index] = resArray[i][index++];
                            pvSumFenshuArray[i][index] = resArray[i][index++];
                            pvSumFenshuArray[i][index] = resArray[i][index++];
                            pvSumFenshuArray[i][index] = resArray[i][index++];

                            if (resArray[i][index + 1] == 0.0) {
                                tmp = 0;
                            } else {
                                tmp = resArray[i][index] / resArray[i][index + 1];//故障率
                            }


                            if (tmp > 0.1) {
                                tmp = 0.1;
                            }

                            pvSumFenshuArray[i][index] = 10 - 100 * (tmp);
                        }
                    }
                    for (int i = 0; i < pvSumFenshuArray.length; i++) {
                        for (int j = 0; j < pvSumFenshuArray[0].length; j++) {
                            double tFenshu = pvSumFenshuArray[i][j];
                            if (Double.isNaN(tFenshu)) {
                                tFenshu = 0;
                            }
                            pvReal[i] = tFenshu + pvReal[i];
                        }
                    }

                    for (int i = 0; i < pvReal.length; i++) {
                        pvReal[i] = pvReal[i] * 1.25;

                        Label label = new Label(i + 1, z + 1, df.format(pvReal[i]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 8));
                        ws.addCell(label);
                        PvFenshu += pvReal[i];

                        if (pvReal[i] > 0) {
                            tempPvReal.add(pvReal[i]);
                        }
                    }
                    if (tmpSet.size() > 0) {
                        sumGroupFenshu[z] = PvFenshu;

                        PvFenshu = PvFenshu / tmpSet.size();
                    } else {
                        PvFenshu = 0;
                    }
                    ////add
                } else if (content.contains(GUANG_LAN)) {
                    for (int i = 0; i < resArray[0].length; i++) {
                        for (int j = 0; j < resArray.length; j++) {
                            if (i >= 0 && i <= 3 && resArray[j][i] != 0) {
                                tmpSet.add(j + "");
                            }

                            if (i >= 7 && i <= 8 || i >= 11 && i <= 14) {
                                if (sumArray[i] < resArray[j][i]) {
                                    if (resArray[j][i] > 3) {
                                        sumArray[i] = 3;
                                    } else {
                                        sumArray[i] = resArray[j][i];
                                    }
                                }
                            } else {
                                sumArray[i] = sumArray[i] + resArray[j][i];
                            }
                        }
                    }
                    ////add
                    for (int i = 0; i < resArray.length; i++) {
                        int index = 1;
                        pvLength[i] = resArray[i][index++] + resArray[i][index++] + resArray[i][index++];
                        if (pvLength[i] > 0) {
                            pvSumFenshuArray[i][index] = 4 - 4 * (resArray[i][index++] / pvLength[i]);   //tindex=4
                            pvSumFenshuArray[i][index] = 4 - 4 * (resArray[i][index++] / pvLength[i]);
                            pvSumFenshuArray[i][index] = 4 - 4 * (resArray[i][index++] / pvLength[i]);

                            if (resArray[i][index] > 3) {  //index=7
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }


                            if (resArray[i][index] > 3) {  //index=8
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }

                            double tmp = 0.0;
                            if (resArray[i][index + 1] == 0.0) {
                                tmp = 0;
                            } else {
                                tmp = resArray[i][index] / resArray[i][index + 1];//故障率  tindex=9   维护2新增接头的衰减（双向平均0.04dB以上的芯数）(注意：是光纤芯数,不是光缆接头个数！！！)/接头总芯数
                            }


                            double pvfaultPercent = tmp;

                            if (tmp > 0.1) {
                                tmp = 0.1;
                            }
                            if (!Double.isNaN(tmp)) {
                                pvSumFenshuArray[i][index] = 20 - 200 * (tmp);
                            } else {
                                pvSumFenshuArray[i][index] = 0;
                            }


                            index = index + 2;   //tindex=11
                            if (resArray[i][index] > 3) {  //index=11
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }

                            if (resArray[i][index] > 3) {  //index=12
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }
                            if (resArray[i][index] > 3) {  //index=13
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }
                            if (resArray[i][index] > 3) {  //index=14
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }

                            if (resArray[i][index + 1] == 0.0) {
                                tmp = 0;
                            } else {
                                tmp = resArray[i][index] / resArray[i][index + 1];//故障率   //tindex=15    因为光缆自身质量问题所造成的光缆故障次数/故障总次数
                            }


                            if (tmp > 0.1) {
                                tmp = 0.1;
                            }
                            if (!Double.isNaN(tmp)) {
                                pvSumFenshuArray[i][index] = 10 - 100 * (tmp);
                            } else {
                                pvSumFenshuArray[i][index] = 0;
                            }
                        }
                    }

                    for (int i = 0; i < pvSumFenshuArray.length; i++) {
                        for (int j = 0; j < pvSumFenshuArray[0].length; j++) {
                            double tFenshu = pvSumFenshuArray[i][j];
                            if (Double.isNaN(tFenshu)) {
                                tFenshu = 0;
                            }
                            pvReal[i] = tFenshu + pvReal[i];
                        }
                    }
                    //int count=0;
                    for (int i = 0; i < pvReal.length; i++) {//哈哈99999999999999999
                        pvReal[i] = pvReal[i] * 1.6666667;

                        //start
                        Label label = new Label(i + 1, z + 1, df.format(pvReal[i]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 8));
                        ws.addCell(label);
                        //end

                        PvFenshu += pvReal[i];

                        if (pvReal[i] > 0) {
                            tempPvReal.add(pvReal[i]);
                        }
                    }

                    if (tmpSet.size() > 0) {
                        sumGroupFenshu[z] = PvFenshu;
                        PvFenshu = PvFenshu / tmpSet.size();
                    } else {
                        PvFenshu = 0;
                    }
                    /////add
                } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {
                    int ttt = 0;
                    double tttt = 0;
                    for (int i = 0; i < resArray.length; i++) {
                        int pvt = 0;
                        double pvtt = 0;
                        for (int j = 0; j < resArray[0].length; j++) {

                            if (j >= 0 && j <= 4 && resArray[i][j] != 0) {
                                tmpSet.add(i + "");

                            }
                            if (resArray[i][j] < 0.18 && resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + 0.18;
                                pvt++;
                                pvtt = pvtt + 0.18;
                            }
                            else if (resArray[i][j] > 0.27 && resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + 0.27;
                                pvt++;
                                pvtt = pvtt + 0.27;
                            } else if (resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + resArray[i][j];
                                //add
                                pvt++;
                                pvtt = pvtt + resArray[i][j];
                            }

                        }
                        //add
                        int ix = 1;
                        pvSumArray[i][0] = pvtt / pvt;  //记录每个省份的平均值
                        pvSumFenshuArray[i][ix] = (10 * (0.27 - pvSumArray[i][0])) / (0.27 - 0.18);
                    }
                    sumArray[0] = tttt / ttt;

                    //add
                    for (int i = 0; i < pvSumFenshuArray.length; i++) {
                        for (int j = 0; j < pvSumFenshuArray[0].length; j++) {
                            double tFenshu = pvSumFenshuArray[i][j];
                            if (Double.isNaN(tFenshu)) {
                                tFenshu = 0;
                            }
                            pvReal[i] = tFenshu + pvReal[i];
                        }
                    }

                    for (int i = 0; i < pvReal.length; i++) {
                        pvReal[i] = pvReal[i] * 10;

                        Label label = new Label(i + 1, z + 1, df.format(pvReal[i]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 8));
                        ws.addCell(label);
                        PvFenshu += pvReal[i];

                        if (pvReal[i] > 0) {
                            tempPvReal.add(pvReal[i]);
                        }
                    }
                    if (tmpSet.size() > 0) {
                        sumGroupFenshu[z] = PvFenshu;
                        PvFenshu = PvFenshu / tmpSet.size();
                    } else {
                        PvFenshu = 0;
                    }
                    //add
                } else if (content.contains(GUANG_LAN_CE_SHI)) {
                    for (int i = 0; i < resArray[0].length; i++) {
                        for (int j = 0; j < resArray.length; j++) {
                            if (i >= 0 && i <= 6 && resArray[j][i] != 0) {
                                tmpSet.add(j + "");
                            }
                        }
                    }


                    int ttt = 0;
                    double tttt = 0;
                    for (int i = 0; i < resArray.length; i++) {
                        int pvt = 0;
                        double pvtt = 0;
                        for (int j = 0; j < resArray[0].length; j++) {
                            if (resArray[i][j] < 0.180 && resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + 0.18;
                                // add
                                pvtt = pvtt + 0.18;
                                pvt++;
                            } else if (resArray[i][j] > 0.26 && resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + 0.26;
//								add
                                pvtt = pvtt + 0.26;
                                pvt++;
                            } else if (resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + resArray[i][j];
                                pvtt = pvtt + resArray[i][j];
                                pvt++;
                            }

                        }
                        //add
                        int ix = 1;

                        pvSumArray[i][0] = pvtt / pvt;  //记录每个省份的平均值
                        if (!Double.isNaN(pvSumArray[i][0])) {
                            pvSumFenshuArray[i][ix] = (40 * (0.26 - pvSumArray[i][0])) / (0.26 - 0.18);
                        } else {
                            pvSumFenshuArray[i][ix] = 0;
                        }

                        //add
                    }
                    sumArray[0] = tttt / ttt;
                    //add
                    for (int i = 0; i < pvSumFenshuArray.length; i++) {
                        for (int j = 0; j < pvSumFenshuArray[0].length; j++) {
                            double tFenshu = pvSumFenshuArray[i][j];
                            if (Double.isNaN(tFenshu)) {
                                tFenshu = 0;
                            }
                            pvReal[i] = tFenshu + pvReal[i];
                        }
                    }

                    for (int i = 0; i < pvReal.length; i++) {
                        pvReal[i] = pvReal[i] * 2.5;
                        //start
                        Label label = new Label(i + 1, z + 1, df.format(pvReal[i]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 8));
                        ws.addCell(label);
                        //end
                        PvFenshu += pvReal[i];

                        if (pvReal[i] > 0) {
                            tempPvReal.add(pvReal[i]);
                        }
                    }
                    if (tmpSet.size() > 0) {
                        sumGroupFenshu[z] = PvFenshu;

                        PvFenshu = PvFenshu / tmpSet.size();
                    } else {
                        PvFenshu = 0;
                    }
                    //add
                }

                markCount = tmpSet.size();
                pvNum[z] = markCount;   //存放各个供应商覆盖省份的数量

                double length = 0;//光缆的总长度
                int tindex = 1;
                double faultPercent = 0;//故障率
                if (content.contains(DIE_XING_GUANG_LAN)) {
                    try {
                        length = sumArray[tindex++] + sumArray[tindex++] + sumArray[tindex++] + sumArray[tindex++];
                    } catch (Exception ex) {
                        logger.info("company:" + company);
                    }
                    sumFenshuArray[tindex] = 15 - 15 * (sumArray[tindex++] / length);
                    sumFenshuArray[tindex] = 5 - 5 * (sumArray[tindex++] / length);
                    sumFenshuArray[tindex] = 15 - 15 * (sumArray[tindex++] / length);
                    double tmp = 0.0;
                    if (sumArray[tindex + 1] == 0.0) {
                        tmp = 0;
                    } else {
                        tmp = sumArray[tindex] / sumArray[tindex + 1];//故障率
                    }


                    faultPercent = tmp;
                    if (tmp > 0.1) {
                        tmp = 0.1;
                    }


                    sumFenshuArray[tindex] = 20 - 200 * (tmp);
                    tindex = tindex + 2;

                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    if (sumArray[tindex + 1] == 0.0) {
                        tmp = 0;
                    } else {
                        tmp = sumArray[tindex] / sumArray[tindex + 1];//故障率
                    }

                    if (tmp > 0.1) {
                        tmp = 0.1;
                    }

                    sumFenshuArray[tindex] = 10 - 100 * (tmp);
                } else if (content.contains(GUANG_LAN)) {//哈哈11111111111
                    try {
                        //光缆总长度
                        length = sumArray[tindex++] + sumArray[tindex++] + sumArray[tindex++];
                        //记录各个厂商光缆长度
                        lengths[z] = length;
                    } catch (Exception ex) {
                        logger.info("company:" + company);
                    }
                    sumFenshuArray[tindex] = 4 - 4 * (sumArray[tindex++] / length);
                    sumFenshuArray[tindex] = 4 - 4 * (sumArray[tindex++] / length);
                    sumFenshuArray[tindex] = 4 - 4 * (sumArray[tindex++] / length);
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    double tmp = 0.0;
                    if (sumArray[tindex + 1] == 0.0) {
                        tmp = 0;
                    } else {
                        tmp = sumArray[tindex] / sumArray[tindex + 1];//故障率
                    }

                    faultPercent = tmp;
                    if (tmp > 0.1) {
                        tmp = 0.1;
                    }

                    sumFenshuArray[tindex] = 20 - 200 * (tmp);
                    tindex = tindex + 2;


                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    if (sumArray[tindex + 1] == 0.0) {
                        tmp = 0;
                    } else {
                        tmp = sumArray[tindex] / sumArray[tindex + 1];//故障率
                    }
                    if (tmp > 0.1) {
                        tmp = 0.1;
                    }
                    sumFenshuArray[tindex] = 10 - 100 * (tmp);
                } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {
                    sumFenshuArray[tindex] = (10 * (0.27 - sumArray[0])) / (0.27 - 0.18);
                } else if (content.contains(GUANG_LAN_CE_SHI)) {
                    sumFenshuArray[tindex] = (40 * (0.26 - sumArray[0])) / (0.26 - 0.18);
                }

                for (int i = 0; i < sumFenshuArray.length; i++) {
                    double tFenshu = sumFenshuArray[i];
                    if (Double.isNaN(tFenshu)) {
                        tFenshu = 0;
                    }
                    real = tFenshu + real;
                }
                if (content.contains(DIE_XING_GUANG_LAN)) {
                    real = real * 1.25;
                } else if (content.contains(GUANG_LAN)) {
                    real = real * 1.6666667;
                } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {
                    real = real * 10;
                } else if (content.contains(GUANG_LAN_CE_SHI)) {
                    real = real * 2.5;
                }
                int markFensu = markCount * 10;
                if (markFensu > 100) {
                    markFensu = 100;
                }
                allFenshu[z][0] = real;
                allFenshu[z][1] = markFensu;
                allFenshu[z][2] = real * 0.8 + markFensu * 0.2;
                allFenshu[z][3] = markCount;
                allFenshu[z][4] = faultPercent;
                allFenshu[z][5] = PvFenshu;

                //用冒泡计算每个厂家的最小分省后评价得分
                if (tempPvReal.size() > 1) {
                    for (int j = 0; j < tempPvReal.size() - 1; j++) {
                        for (int k = 0; k < tempPvReal.size() - 1 - j; k++) {
                            if (tempPvReal.get(k) > tempPvReal.get(k + 1)) {
                                double temp = tempPvReal.get(k);
                                tempPvReal.set(k, tempPvReal.get(k + 1));
                                tempPvReal.set(k + 1, temp);
                            }
                        }

                    }
                    minGroupFenshu[z] = tempPvReal.get(0);
                } else if (tempPvReal.size() > 0) {
                    minGroupFenshu[z] = tempPvReal.get(0);
                }
            }

            //add
            double maxPvNum = 0;
            double[] meritArray = new double[verdorNum];
            for (int i = 0; i < pvNum.length; i++) {

                double num = pvNum[i];
                if (num > maxPvNum) {
                    maxPvNum = num;
                }
            }

            for (int i = 0; i < allFenshu.length; i++) {
                if (verdorNum > 4 && verdorNum < 10) {
                    if (pvNum[i] > 0) {
                        if (pvNum[i] == maxPvNum) {
                            meritArray[i] = 0;
                        } else {
                            meritArray[i] = (pvNum[i] / maxPvNum) * 20;
                        }
                    }

                } else if (verdorNum > 10) {
                    if (pvNum[i] > 0) {
                        if (pvNum[i] > 15) {
                            meritArray[i] = 0;
                        } else {
                            meritArray[i] = (15 - pvNum[i]) * 1.5;//哈哈24444
                            if (meritArray[i] > 21) {
                                meritArray[i] = 21;
                            }
                        }
                    }
                }
                allFenshu[i][6] = allFenshu[i][5]; //后评价平均分（刘红新增）
                allFenshu[i][5] = allFenshu[i][5] - meritArray[i]; //后评价得分（刘红新增）

                //规模效益得分 btliu
                double scaleMark = 0;
                if (verdorNum <= 3) {
                    scaleMark = 0;
                } else if (verdorNum >= 4 && verdorNum <= 10) {
                    scaleMark = (pvNum[i] / maxPvNum) * 30;
                } else if (verdorNum >= 11) {
                    scaleMark = pvNum[i] * 2;
                }
                if (scaleMark > 30) {
                    scaleMark = 30;//30分封顶
                }
                //2014版后评价平均分
                allFenshu[i][7] = (sumGroupFenshu[i] + (PList.size() - pvNum[i]) * minGroupFenshu[i]) / PList.size() * 0.7;
                //2014版后评价得分
                allFenshu[i][8] = allFenshu[i][7] + scaleMark;

                int n = 0; //n 为模板覆盖省份数量
                String sql = "select count(DISTINCT(reportOrgCode)) from  t_eem_excel_page \n" +
                        "where tpInputID =  (select object_id from t_eom_temp_info where REL_TEMP_ID = "+ formId +")";
                List<Map> l = baseDAO.findNativeSQL(sql,null);
                if(l.size() > 0){
                    n = Integer.parseInt(l.get(0).get("count(distinct(reportorgcode))") + "");
                    //n = (int)pvNum[i];
                }

/*
                //2016年版后评价得分
                allFenshu[i][9] = (sumGroupFenshu[i] + (31 - n) * minGroupFenshu[i])/31 * 0.8;
                //2016年规模效益分  如大于20分  则 为 20 分
                allFenshu[i][10] = n;
                if(allFenshu[i][10] > 20){
                    allFenshu[i][10] = 20;
                }
    */
                //2017年版后评价得分
                // Apple------------20170927
                allFenshu[i][9] = (sumGroupFenshu[i] + (31 - n) * minGroupFenshu[i])/31 * 0.8;

                //2017年规模效益分
                allFenshu[i][10] = 0;

                //if(lengths[i]/pvLength_[i] > 0.05){
                allFenshu[i][10] = n;
                if(allFenshu[i][10] > 20){
                    allFenshu[i][10] = 20;
                }
                if( allFenshu[i][9] == 0){
                    allFenshu[i][10] = 0;
                }
            }
            //add

            int col = PList.size() + spaceRow;
            WritableCell rCell = null;
            //java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00");


            rCell = new Label(col, 0, "总部项目集采得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字
            rCell = new Label(col + 1, 0, "规模效益得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字
            rCell = new Label(col + 2, 0, "建议暂缓下期集采", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字

            if (!content.contains(GUANG_LAN_CE_SHI)) {
                rCell = new Label(col + 3, 0, "故障率", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字
//		 add

                rCell = new Label(col + 4, 0, "后评价平均分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 5, 0, "后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                //add by btliu
                rCell = new Label(col + 6, 0, "2014版后评价平均分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 7, 0, "后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字


                rCell = new Label(col + 8, 0, "2017后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 9, 0, "2017年有效规模效益分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                //end
            } else {
                rCell = new Label(col + 3, 0, "后评价平均分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 4, 0, "后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字
//		add by btliu
                rCell = new Label(col + 5, 0, "2014版后评价平均分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 6, 0, "后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字
                //end
                //add
                rCell = new Label(col + 7, 0, "2017年后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);

                rCell = new Label(col + 8, 0, "2017年有效规模效益分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);

            }


            List<Double> sortList = new ArrayList<Double>();
            for (int i = 0; i < allFenshu.length; i++) {
                try {

                    double tt = allFenshu[i][2];
                    sortList.add(tt);
                } catch (Exception ex) {

                }
            }
            Collections.sort(sortList);
            int a = (int) (sortList.size() * 0.7);
            logger.info("a~~~~~~~~~" + a + "!!!!!!!!!allFenshu" + allFenshu.length);
            //  logger.info("a~~~~~~~~~"+a+"!!!!!!!!!sortList"+sortList.size());
            //double aMin = sortList.get(a)/PList.size();
            double aMin = sortList.get(a);


            for (int z = 0; z < allFenshu.length; z++) {
                double shengFenshu = allFenshu[z][0];
                double markFensu = allFenshu[z][1];
                double real = allFenshu[z][2];
                double markCount = allFenshu[z][3];
                double percent = allFenshu[z][4];//故障率
                double PvFenshu5 = allFenshu[z][5];  //新增
                double pvFenshuAvg = allFenshu[z][6];
                double groupFenshuAvg = allFenshu[z][7];
                double groupFenshu = allFenshu[z][8];
                /*
                //2016年版后评价得分
                double groupFenshu_2016 = allFenshu[z][9];
                //2016年版后评价效益分
                double groupFenshu_2016xy = allFenshu[z][10];
                */
                //2017年版后评价得分
                double groupFenshu_2017 = allFenshu[z][9];
                //2017年规模效益分
                double groupFenshu_2017xy = allFenshu[z][10];


                rCell = null;
                col = PList.size() + spaceRow;
                try {
                    if (markCount > 0) {
                        if (real < 60) {
                            rCell = new Label(col, 1 + (z * pix), df.format(real), ExportUtil.getBackfround(jxl.format.Colour.RED, 12));

                        } else {
                            if (real >= aMin) {
                                rCell = new Label(col, 1 + (z * pix), df.format(real), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                            } else {
                                rCell = new Label(col, 1 + (z * pix), df.format(real), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                            }
                        }
                    } else {
                        rCell = new Label(col, 1 + (z * pix), "-", ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                    }
                    sheet.addCell(rCell);//具体数字
                    sheet.mergeCells(col, 1 + (z * pix), col, pix + (z * pix));


                    rCell = new Label(col + 1, 1 + (z * pix), df.format(markFensu), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                    sheet.addCell(rCell);//具体数字
                    sheet.mergeCells(col + 1, 1 + (z * pix), col + 1, pix + (z * pix));

                    if (markCount > 0) {
                        if (shengFenshu < 40) {
                            rCell = new Label(col + 2, 1 + (z * pix), df.format(shengFenshu), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {

                            rCell = new Label(col + 2, 1 + (z * pix), df.format(shengFenshu), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));

                        }
                    } else {
                        rCell = new Label(col + 2, 1 + (z * pix), "-", ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                    }

                    sheet.addCell(rCell);//具体数字
                    sheet.mergeCells(col + 2, 1 + (z * pix), col + 2, pix + (z * pix));

                    if (!content.contains(GUANG_LAN_CE_SHI)) {
                        if (percent > 0) {
                            rCell = new Label(col + 3, 1 + (z * pix), df.format(percent * 100) + "%", ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));

                        } else {
                            rCell = new Label(col + 3, 1 + (z * pix), "-", ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                        }

                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 3, 1 + (z * pix), col + 3, pix + (z * pix));
                    }


//				/ add
                    if (!content.contains(GUANG_LAN_CE_SHI)) {//pvFenshuAvg
                        //后评价平均分
                        rCell = new Label(col + 4, 1 + (z * pix), df.format(pvFenshuAvg), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 4, 1 + (z * pix), col + 4, pix + (z * pix));

                        // 后评价的得分
                        if (PvFenshu5 <= 0) {
                            rCell = new Label(col + 5, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 5, 1 + (z * pix), df.format(PvFenshu5), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 5, 1 + (z * pix), col + 5, pix + (z * pix));

                        //2014版后评价平均分
                        rCell = new Label(col + 6, 1 + (z * pix), df.format(groupFenshuAvg), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));

                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 6, 1 + (z * pix), col + 6, pix + (z * pix));

                        // 后评价的得分
                        if (groupFenshu <= 0) {
                            rCell = new Label(col + 7, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 7, 1 + (z * pix), df.format(groupFenshu), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 7, 1 + (z * pix), col + 7, pix + (z * pix));

                        //2017年后评价得分
                        if (groupFenshu_2017 <= 0) {
                            rCell = new Label(col + 8, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 8, 1 + (z * pix), df.format(groupFenshu_2017), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 8, 1 + (z * pix), col + 7, pix + (z * pix));

                        //2017年有效规模效益分
                        if (groupFenshu_2017xy <= 0) {
                            rCell = new Label(col + 9, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 9, 1 + (z * pix), df.format(groupFenshu_2017xy), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 9, 1 + (z * pix), col + 7, pix + (z * pix));


                    } else {
                        //后评价平均分
                        rCell = new Label(col + 3, 1 + (z * pix), df.format(pvFenshuAvg), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 3, 1 + (z * pix), col + 3, pix + (z * pix));

                        //后评价得分
                        if (PvFenshu5 <= 0) {
                            rCell = new Label(col + 4, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 4, 1 + (z * pix), df.format(PvFenshu5), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));

                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 4, 1 + (z * pix), col + 4, pix + (z * pix));
                        //2014版后评价平均分
                        rCell = new Label(col + 5, 1 + (z * pix), df.format(groupFenshuAvg), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));

                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 5, 1 + (z * pix), col + 5, pix + (z * pix));

                        // 后评价的得分
                        if (groupFenshu <= 0) {
                            rCell = new Label(col + 6, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 6, 1 + (z * pix), df.format(groupFenshu), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 6, 1 + (z * pix), col + 6, pix + (z * pix));

                        //2017年后评价得分
                        if (groupFenshu_2017 <= 0) {
                            rCell = new Label(col + 7, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 7, 1 + (z * pix), df.format(groupFenshu_2017), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 7, 1 + (z * pix), col + 7, pix + (z * pix));

                        //2017年有效规模效益分
                        if (groupFenshu_2017xy <= 0) {
                            rCell = new Label(col + 8, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 8, 1 + (z * pix), df.format(groupFenshu_2017xy), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 8, 1 + (z * pix), col + 7, pix + (z * pix));

                    }
                } catch (Exception ex) {
                    rCell = new Label(col, 2 + (z * pix), "hhh", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                    sheet.addCell(rCell);//具体数字
                }
            }
        }
    }



    private void dealSheetForGUANGLANnew(WritableWorkbook wwb, WritableSheet sheet, List<OrgEntity> PList, String reportDateStr, boolean isSearchHQData, String dealType, String reportYear, double coefficient, String equipType, String content, String formId) throws Exception {

        java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
        WritableSheet ws = null;
        for (WritableSheet wws : wwb.getSheets()) {
            if (wws.getName().equals("分省后评价得分")) {//获取汇总模板中分省后评价得分的sheet页
                ws = wws;
            }
        }
        ws.setColumnView(0, 50);
        List<NameValue> spaceList = new ArrayList<NameValue>();
        int spaceRow = 0;//填充数据到的具体的行数5

        String companyListSQL = "";


        if ("".equals(reportYear)) {
            if (content.equals(DIE_XING_GUANG_LAN)) {//碟形光缆
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='72' and  (iswithdraw is  null or iswithdraw<>'static') and  (reportyear='2015' or reportyear='2016') and  DELETED_FLAG=FALSE";
                pix = 16;
            } else if (content.contains(GUANG_LAN)) {//光缆
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='47' and (iswithdraw is  null or iswithdraw<>'static') and  (reportyear='2015' or reportyear='2016') and  DELETED_FLAG=FALSE";
                pix = 16;
            } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {//碟形光缆测试
                pix = 12;
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='71' and (iswithdraw is  null or iswithdraw<>'static') and  (reportyear='2015' or reportyear='2016') and  DELETED_FLAG=FALSE";
            } else if (content.contains(GUANG_LAN_CE_SHI)) {//光缆测试
                pix = 6;
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='46' and (iswithdraw is  null or iswithdraw<>'static') and  (reportyear='2015' or reportyear='2016') and  DELETED_FLAG=FALSE";
            }
        } else {
            if (content.equals(DIE_XING_GUANG_LAN)) {
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='72'  and DELETED_FLAG=FALSE  and  (iswithdraw is  null or iswithdraw<>'static') and  reportyear='" + reportYear + "'";
                pix = 16;
            } else if (content.contains(GUANG_LAN)) {
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='47'   and DELETED_FLAG=FALSE and (iswithdraw is  null or iswithdraw<>'static') and  reportyear='" + reportYear + "'";
                pix = 16;
            } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {
                pix = 12;
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='71'  and DELETED_FLAG=FALSE  and (iswithdraw is  null or iswithdraw<>'static') and  reportyear='" + reportYear + "'";
            } else if (content.contains(GUANG_LAN_CE_SHI)) {
                pix = 6;
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='46'   and DELETED_FLAG=FALSE and (iswithdraw is  null or iswithdraw<>'static') and  reportyear='" + reportYear + "'";
            }//todo 这里有写死的id 及表明需要修改

        }


//        List<Map> companyList = hibDao.queryBySql(companyListSQL);
        List<Map> companyList = baseDAO.findNativeSQL(companyListSQL, null);
        if (companyList.size() > 0) {

            //新增加start
            //将厂家写到新的sheet页中
            for (int k = 0; k < companyList.size(); k++) {
                String name = companyList.get(k).get("pagename").toString();
                Label label = new Label(0, k + 1, name, ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                ws.addCell(label);
            }
            //将省份写入到sheet页中
            for (int k = 0; k < PList.size(); k++) {
                String name = PList.get(k).getOrgName();
                Label label = new Label(k + 1, 0, name, ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                ws.addCell(label);
            }
            //end


            spaceRow = getSpaceList(sheet, reportDateStr, dealType, reportYear, spaceList, isSearchHQData);
            int verdorNum = companyList.size();  // 现网供应商数量，取sheet数量
            double[] pvNum = new double[verdorNum];  //存放各个厂家提供服务的省份数量

            //double[][] allFenshu = new double[companyList.size()][9];//新增加两列2014版后评价平均分，2014版后评价得分  2014.10.10 btliu
            //光缆测试汇总 新增加  2016年版后评价得分    2016年规模效益分        2017年版后评价得分   三列
            //光缆综合汇总 新增加  2016年版后评价得分    2016年规模效益分        2017年版后评价得分  2017年规模效益分  四列
            double[][] allFenshu = new double[companyList.size()][9 + 3 + 1];
            double[] sumGroupFenshu = new double[companyList.size()];//存放每个厂家的分省后评价得分和
            double[] minGroupFenshu = new double[companyList.size()];//存放每个厂家覆盖省份中最小分省后评价得分
            //存放各个厂商光缆总长度
            double[] lengths = new double[companyList.size()];
            double[] OrgLengths = new double[PList.size()];


            //double[] pvLength_ = new double[allFenshu.length];
            if (content.contains(DIE_XING_GUANG_LAN)) {
            String sb="SELECT (( SELECT sum(txtvalue) AS VALUE FROM t_eem_excel_page_values v, t_eem_excel_page p WHERE v.pageid = p.pageid AND ( v.rowindex IN ( '4', '5', '6', '7', '8', '9', '10', '11' )) AND v.colindex IN ('3', '4', '5', '6') AND p.tpInputID = 72 AND txtvalue IS NOT NULL AND p.pagename = '$$pageName' AND p.reportdate = '$$date' AND p.reportOrgCode IN ('$$depart') AND p.reportYear = '$$reportYear' )) VALUE FROM DUAL";
            sb= sb.replace("$$reportYear",reportYear);
            sb =  sb.replace("$$date",reportDateStr);
            for(int q=0;q<PList.size();q++){
                OrgLengths[q]=0;

                String sb2 =  sb.replace("$$depart", PList.get(q).getOrgCode());
                for(int z = 0; z < companyList.size(); z++){
                    String sb3 = sb2.replace("$$pageName", companyList.get(z).get("pagename").toString());
                    Object obj = null;
                    System.out.println(sb3);
                    obj = baseDAO.findNativeSQL(sb3, null);
                    if (obj != null) {
                        List list = (List) obj;
                        if (list != null && list.size() > 0 && list.get(0) != null) {
                            Map resMap = (Map) list.get(0);
                            Object ob = resMap.get("value");
                            if (ob != null) {
                                OrgLengths[q]+=  new Double(ob.toString()).doubleValue();;

                            }
                        }
                    }
                        }

                }
            }


            for (int z = 0; z < companyList.size(); z++) {

                String company = companyList.get(z).get("pagename").toString();
                logger.info("~~~~~~~~~`" + z + "!!!" + company);
                List<List<String>> dataList = getDataListGUANGLAN(sheet, PList, spaceList, spaceRow, STAT_TYPE_COL, company, z, content);
                if (dataList.get(0).size() == 0) {
                    continue;
                }
                double[][] resArray = getResArray(dataList);
                double[] sumArray = new double[dataList.get(0).size()];//各种指标的和
                double[][] pvSumArray = new double[dataList.size()][dataList.get(0).size()];
                double[] sumFenshuArray = new double[dataList.get(0).size()];//各种指标的分数的和
                double[] mark = new double[dataList.get(0).size()];//保存最后的规模效益得分
                double real = 0;
                double[] pvReal = new double[dataList.size()];
                double PvFenshu = 0;
                double[] finalArray = new double[dataList.get(0).size()];
                double[][] pvSumFenshuArray = new double[dataList.size()][dataList.get(0).size()];
                double[] pvLength = new double[dataList.size()]; //各个省份的光缆总长度
                //pvLength_ =  new double[pvLength.length];
                int markCount = 0;
                int pvTindex = 1;
                Set<String> tmpSet = new HashSet<String>();
                List<Double> tempPvReal = new ArrayList<Double>();//用于计算min(ai) 2014.10.10 btliu
                if (content.contains(DIE_XING_GUANG_LAN)) {
                    allFenshu[z][12]=0;
                    for(int h=0;h<resArray.length;h++){
                        double ss=0L;
                        ss = resArray[h][1]+resArray[h][2]+resArray[h][3]+resArray[h][4];
                        if(ss>0&&(ss/OrgLengths[h])>0.05){
                            allFenshu[z][12]++;
                        }
                    }
                    if(  allFenshu[z][12]>20){
                        allFenshu[z][12]=20;
                    }


                    for (int i = 0; i < resArray[0].length; i++) {
                        for (int j = 0; j < resArray.length; j++) {
                            if (i >= 0 && i <= 5 && resArray[j][i] != 0) {
                                tmpSet.add(j + "");
                            }

                            if (i >= 10 && i <= 14) {
                                if (sumArray[i] < resArray[j][i]) {
                                    sumArray[i] = resArray[j][i];
                                }
                            } else {
                                sumArray[i] = sumArray[i] + resArray[j][i];
                            }
                        }
                    }

                    ////add
                    for (int i = 0; i < resArray.length; i++) {
                        int index = 1;
                        pvLength[i] = resArray[i][index++] + resArray[i][index++] + resArray[i][index++] + resArray[i][index++];
                        //pvLength_[i] =  pvLength[i];
                        if (pvLength[i] > 0) {
                            pvSumFenshuArray[i][index] = 15 - 15 * (resArray[i][index++] / pvLength[i]);
                            pvSumFenshuArray[i][index] = 5 - 5 * (resArray[i][index++] / pvLength[i]);
                            pvSumFenshuArray[i][index] = 15 - 15 * (resArray[i][index++] / pvLength[i]);

                            double tmp = 0.0;
                            if (resArray[i][index + 1] == 0.0) {
                                tmp = 0;
                            } else {
                                tmp = resArray[i][index] / resArray[i][index + 1];//故障率  tindex=9   维护2新增接头的衰减（双向平均0.04dB以上的芯数）(注意：是光纤芯数,不是光缆接头个数！！！)/接头总芯数
                            }

                            double pvfaultPercent = tmp;   //0.026898130484547884
                            if (tmp > 0.1) {
                                tmp = 0.1;
                            }
                            pvSumFenshuArray[i][index] = 20 - 200 * (tmp);

                            index = index + 2;
                            pvSumFenshuArray[i][index] = resArray[i][index++];
                            pvSumFenshuArray[i][index] = resArray[i][index++];
                            pvSumFenshuArray[i][index] = resArray[i][index++];
                            pvSumFenshuArray[i][index] = resArray[i][index++];
                            pvSumFenshuArray[i][index] = resArray[i][index++];

                            if (resArray[i][index + 1] == 0.0) {
                                tmp = 0;
                            } else {
                                tmp = resArray[i][index] / resArray[i][index + 1];//故障率
                            }


                            if (tmp > 0.1) {
                                tmp = 0.1;
                            }

                            pvSumFenshuArray[i][index] = 10 - 100 * (tmp);
                        }
                    }
                    for (int i = 0; i < pvSumFenshuArray.length; i++) {
                        for (int j = 0; j < pvSumFenshuArray[0].length; j++) {
                            double tFenshu = pvSumFenshuArray[i][j];
                            if (Double.isNaN(tFenshu)) {
                                tFenshu = 0;
                            }
                            pvReal[i] = tFenshu + pvReal[i];
                        }
                    }

                    for (int i = 0; i < pvReal.length; i++) {
                        pvReal[i] = pvReal[i] * 1.25;

                        Label label = new Label(i + 1, z + 1, df.format(pvReal[i]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 8));
                        ws.addCell(label);
                        PvFenshu += pvReal[i];

                        if (pvReal[i] > 0) {
                            tempPvReal.add(pvReal[i]);
                        }
                    }
                    if (tmpSet.size() > 0) {
                        sumGroupFenshu[z] = PvFenshu;

                        PvFenshu = PvFenshu / tmpSet.size();
                    } else {
                        PvFenshu = 0;
                    }
                    ////add
                } else if (content.contains(GUANG_LAN)) {
                    for (int i = 0; i < resArray[0].length; i++) {
                        for (int j = 0; j < resArray.length; j++) {
                            if (i >= 0 && i <= 3 && resArray[j][i] != 0) {
                                tmpSet.add(j + "");
                            }

                            if (i >= 7 && i <= 8 || i >= 11 && i <= 14) {
                                if (sumArray[i] < resArray[j][i]) {
                                    if (resArray[j][i] > 3) {
                                        sumArray[i] = 3;
                                    } else {
                                        sumArray[i] = resArray[j][i];
                                    }
                                }
                            } else {
                                sumArray[i] = sumArray[i] + resArray[j][i];
                            }
                        }
                    }

                    ////add
                    for (int i = 0; i < resArray.length; i++) {
                        int index = 1;
                        pvLength[i] = resArray[i][index++] + resArray[i][index++] + resArray[i][index++];
                        //pvLength_[i] =  pvLength[i];
                        if (pvLength[i] > 0) {
                            pvSumFenshuArray[i][index] = 4 - 4 * (resArray[i][index++] / pvLength[i]);   //tindex=4
                            pvSumFenshuArray[i][index] = 4 - 4 * (resArray[i][index++] / pvLength[i]);
                            pvSumFenshuArray[i][index] = 4 - 4 * (resArray[i][index++] / pvLength[i]);

                            if (resArray[i][index] > 3) {  //index=7
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }


                            if (resArray[i][index] > 3) {  //index=8
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }

                            double tmp = 0.0;
                            if (resArray[i][index + 1] == 0.0) {
                                tmp = 0;
                            } else {
                                tmp = resArray[i][index] / resArray[i][index + 1];//故障率  tindex=9   维护2新增接头的衰减（双向平均0.04dB以上的芯数）(注意：是光纤芯数,不是光缆接头个数！！！)/接头总芯数
                            }


                            double pvfaultPercent = tmp;

                            if (tmp > 0.1) {
                                tmp = 0.1;
                            }
                            if (!Double.isNaN(tmp)) {
                                pvSumFenshuArray[i][index] = 20 - 200 * (tmp);
                            } else {
                                pvSumFenshuArray[i][index] = 0;
                            }


                            index = index + 2;   //tindex=11
                            if (resArray[i][index] > 3) {  //index=11
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }

                            if (resArray[i][index] > 3) {  //index=12
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }
                            if (resArray[i][index] > 3) {  //index=13
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }
                            if (resArray[i][index] > 3) {  //index=14
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }

                            if (resArray[i][index + 1] == 0.0) {
                                tmp = 0;
                            } else {
                                tmp = resArray[i][index] / resArray[i][index + 1];//故障率   //tindex=15    因为光缆自身质量问题所造成的光缆故障次数/故障总次数
                            }


                            if (tmp > 0.1) {
                                tmp = 0.1;
                            }
                            if (!Double.isNaN(tmp)) {
                                pvSumFenshuArray[i][index] = 10 - 100 * (tmp);
                            } else {
                                pvSumFenshuArray[i][index] = 0;
                            }
                        }
                    }

                    for (int i = 0; i < pvSumFenshuArray.length; i++) {
                        for (int j = 0; j < pvSumFenshuArray[0].length; j++) {
                            double tFenshu = pvSumFenshuArray[i][j];
                            if (Double.isNaN(tFenshu)) {
                                tFenshu = 0;
                            }
                            pvReal[i] = tFenshu + pvReal[i];
                        }
                    }
                    //int count=0;
                    for (int i = 0; i < pvReal.length; i++) {//哈哈99999999999999999
                        pvReal[i] = pvReal[i] * 1.6666667;

                        //start
                        Label label = new Label(i + 1, z + 1, df.format(pvReal[i]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 8));
                        ws.addCell(label);
                        //end

                        PvFenshu += pvReal[i];

                        if (pvReal[i] > 0) {
                            tempPvReal.add(pvReal[i]);
                        }
                    }

                    if (tmpSet.size() > 0) {
                        sumGroupFenshu[z] = PvFenshu;

                        PvFenshu = PvFenshu / tmpSet.size();
                    } else {
                        PvFenshu = 0;
                    }

                    /////add
                } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {
                    int ttt = 0;
                    double tttt = 0;
                    for (int i = 0; i < resArray.length; i++) {
                        int pvt = 0;
                        double pvtt = 0;
                        for (int j = 0; j < resArray[0].length; j++) {

                            if (j >= 0 && j <= 4 && resArray[i][j] != 0) {
                                tmpSet.add(i + "");

                            }
                            //if(resArray[i][j]<0.2&&resArray[i][j]!=0){
                            if (resArray[i][j] < 0.18 && resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + 0.18;
                                //tttt = tttt+0.2;
                                //add
                                pvt++;
                                pvtt = pvtt + 0.18;
                                //pvtt=pvtt+0.2;
                            } //else if(resArray[i][j]>0.28&&resArray[i][j]!=0){
                            else if (resArray[i][j] > 0.27 && resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + 0.27;
                                //tttt = tttt+0.28;
                                //add
                                pvt++;
                                pvtt = pvtt + 0.27;
                                //pvtt=pvtt+0.28;
                            } else if (resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + resArray[i][j];
                                //add
                                pvt++;
                                pvtt = pvtt + resArray[i][j];
                            }

                        }
                        //add
                        int ix = 1;
                        pvSumArray[i][0] = pvtt / pvt;  //记录每个省份的平均值
                        pvSumFenshuArray[i][ix] = (10 * (0.27 - pvSumArray[i][0])) / (0.27 - 0.18);
                        //pvSumFenshuArray[i][ix]= (10*(0.28 - pvSumArray[i][0]))/(0.28-0.2);
                        //add
                    }
                    sumArray[0] = tttt / ttt;

                    //add
                    for (int i = 0; i < pvSumFenshuArray.length; i++) {
                        for (int j = 0; j < pvSumFenshuArray[0].length; j++) {
                            double tFenshu = pvSumFenshuArray[i][j];
                            if (Double.isNaN(tFenshu)) {
                                tFenshu = 0;
                            }
                            pvReal[i] = tFenshu + pvReal[i];
                        }
                    }

                    for (int i = 0; i < pvReal.length; i++) {
                        pvReal[i] = pvReal[i] * 10;

                        Label label = new Label(i + 1, z + 1, df.format(pvReal[i]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 8));
                        ws.addCell(label);
                        PvFenshu += pvReal[i];

                        if (pvReal[i] > 0) {
                            tempPvReal.add(pvReal[i]);
                        }
                    }
                    if (tmpSet.size() > 0) {
                        sumGroupFenshu[z] = PvFenshu;

                        PvFenshu = PvFenshu / tmpSet.size();
                    } else {
                        PvFenshu = 0;
                    }
                    //add
                } else if (content.contains(GUANG_LAN_CE_SHI)) {
                    for (int i = 0; i < resArray[0].length; i++) {
                        for (int j = 0; j < resArray.length; j++) {
                            if (i >= 0 && i <= 6 && resArray[j][i] != 0) {
                                tmpSet.add(j + "");
                            }
                        }
                    }


                    int ttt = 0;
                    double tttt = 0;
                    for (int i = 0; i < resArray.length; i++) {
                        int pvt = 0;
                        double pvtt = 0;
                        for (int j = 0; j < resArray[0].length; j++) {
                            if (resArray[i][j] < 0.180 && resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + 0.18;
                                // add
                                pvtt = pvtt + 0.18;
                                pvt++;
                            } else if (resArray[i][j] > 0.26 && resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + 0.26;
//								add
                                pvtt = pvtt + 0.26;
                                pvt++;
                            } else if (resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + resArray[i][j];
                                pvtt = pvtt + resArray[i][j];
                                pvt++;
                            }

                        }
                        //add
                        int ix = 1;

                        pvSumArray[i][0] = pvtt / pvt;  //记录每个省份的平均值
                        if (!Double.isNaN(pvSumArray[i][0])) {
                            pvSumFenshuArray[i][ix] = (40 * (0.26 - pvSumArray[i][0])) / (0.26 - 0.18);
                        } else {
                            pvSumFenshuArray[i][ix] = 0;
                        }

                        //add
                    }
                    sumArray[0] = tttt / ttt;
                    //add
                    for (int i = 0; i < pvSumFenshuArray.length; i++) {
                        for (int j = 0; j < pvSumFenshuArray[0].length; j++) {
                            double tFenshu = pvSumFenshuArray[i][j];
                            if (Double.isNaN(tFenshu)) {
                                tFenshu = 0;
                            }
                            pvReal[i] = tFenshu + pvReal[i];
                        }
                    }

                    for (int i = 0; i < pvReal.length; i++) {
                        pvReal[i] = pvReal[i] * 2.5;
                        //start
                        Label label = new Label(i + 1, z + 1, df.format(pvReal[i]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 8));
                        ws.addCell(label);
                        //end
                        PvFenshu += pvReal[i];

                        if (pvReal[i] > 0) {
                            tempPvReal.add(pvReal[i]);
                        }
                    }
                    if (tmpSet.size() > 0) {
                        sumGroupFenshu[z] = PvFenshu;
                        PvFenshu = PvFenshu / tmpSet.size();
                    } else {
                        PvFenshu = 0;
                    }

                    //add
                }

                markCount = tmpSet.size();
                pvNum[z] = markCount;   //存放各个供应商覆盖省份的数量

                double length = 0;//光缆的总长度
                int tindex = 1;
                double faultPercent = 0;//故障率
                if (content.contains(DIE_XING_GUANG_LAN)) {
                    try {
                        length = sumArray[tindex++] + sumArray[tindex++] + sumArray[tindex++] + sumArray[tindex++];
                    } catch (Exception ex) {
                        logger.info("company:" + company);
                    }
                    sumFenshuArray[tindex] = 15 - 15 * (sumArray[tindex++] / length);
                    sumFenshuArray[tindex] = 5 - 5 * (sumArray[tindex++] / length);
                    sumFenshuArray[tindex] = 15 - 15 * (sumArray[tindex++] / length);
                    double tmp = 0.0;
                    if (sumArray[tindex + 1] == 0.0) {
                        tmp = 0;
                    } else {
                        tmp = sumArray[tindex] / sumArray[tindex + 1];//故障率
                    }


                    faultPercent = tmp;
                    if (tmp > 0.1) {
                        tmp = 0.1;
                    }


                    sumFenshuArray[tindex] = 20 - 200 * (tmp);
                    tindex = tindex + 2;

                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    if (sumArray[tindex + 1] == 0.0) {
                        tmp = 0;
                    } else {
                        tmp = sumArray[tindex] / sumArray[tindex + 1];//故障率
                    }

                    if (tmp > 0.1) {
                        tmp = 0.1;
                    }

                    sumFenshuArray[tindex] = 10 - 100 * (tmp);
                } else if (content.contains(GUANG_LAN)) {//哈哈11111111111
                    try {
                        //光缆总长度
                        length = sumArray[tindex++] + sumArray[tindex++] + sumArray[tindex++];
                        //记录各个厂商光缆长度
                        lengths[z] = length;
                    } catch (Exception ex) {
                        logger.info("company:" + company);
                    }
                    sumFenshuArray[tindex] = 4 - 4 * (sumArray[tindex++] / length);
                    sumFenshuArray[tindex] = 4 - 4 * (sumArray[tindex++] / length);
                    sumFenshuArray[tindex] = 4 - 4 * (sumArray[tindex++] / length);
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    double tmp = 0.0;
                    if (sumArray[tindex + 1] == 0.0) {
                        tmp = 0;
                    } else {
                        tmp = sumArray[tindex] / sumArray[tindex + 1];//故障率
                    }

                    faultPercent = tmp;
                    if (tmp > 0.1) {
                        tmp = 0.1;
                    }

                    sumFenshuArray[tindex] = 20 - 200 * (tmp);
                    tindex = tindex + 2;


                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    if (sumArray[tindex + 1] == 0.0) {
                        tmp = 0;
                    } else {
                        tmp = sumArray[tindex] / sumArray[tindex + 1];//故障率
                    }
                    if (tmp > 0.1) {
                        tmp = 0.1;
                    }
                    sumFenshuArray[tindex] = 10 - 100 * (tmp);
                } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {
                    //sumFenshuArray[tindex] = (10*(0.28 - sumArray[0]))/(0.28-0.2);
                    sumFenshuArray[tindex] = (10 * (0.27 - sumArray[0])) / (0.27 - 0.18);

                } else if (content.contains(GUANG_LAN_CE_SHI)) {
                    sumFenshuArray[tindex] = (40 * (0.26 - sumArray[0])) / (0.26 - 0.18);
                }

                for (int i = 0; i < sumFenshuArray.length; i++) {
                    double tFenshu = sumFenshuArray[i];
                    if (Double.isNaN(tFenshu)) {
                        tFenshu = 0;
                    }
                    real = tFenshu + real;
                }


                if (content.contains(DIE_XING_GUANG_LAN)) {
                    real = real * 1.25;
                } else if (content.contains(GUANG_LAN)) {
                    real = real * 1.6666667;
                } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {
                    real = real * 10;
                } else if (content.contains(GUANG_LAN_CE_SHI)) {
                    real = real * 2.5;
                }
                int markFensu = markCount * 10;
                if (markFensu > 100) {
                    markFensu = 100;
                }
                allFenshu[z][0] = real;
                allFenshu[z][1] = markFensu;
                allFenshu[z][2] = real * 0.8 + markFensu * 0.2;
                allFenshu[z][3] = markCount;
                allFenshu[z][4] = faultPercent;
                allFenshu[z][5] = PvFenshu;

                //用冒泡计算每个厂家的最小分省后评价得分
                if (tempPvReal.size() > 1) {
                    for (int j = 0; j < tempPvReal.size() - 1; j++) {
                        for (int k = 0; k < tempPvReal.size() - 1 - j; k++) {
                            if (tempPvReal.get(k) > tempPvReal.get(k + 1)) {
                                double temp = tempPvReal.get(k);
                                tempPvReal.set(k, tempPvReal.get(k + 1));
                                tempPvReal.set(k + 1, temp);
                            }
                        }

                    }
                    minGroupFenshu[z] = tempPvReal.get(0);
                } else if (tempPvReal.size() > 0) {
                    minGroupFenshu[z] = tempPvReal.get(0);
                }
            }

            //add
            double maxPvNum = 0;
            double[] meritArray = new double[verdorNum];
            for (int i = 0; i < pvNum.length; i++) {

                double num = pvNum[i];
                if (num > maxPvNum) {
                    maxPvNum = num;
                }
            }

            for (int i = 0; i < allFenshu.length; i++) {
                if (verdorNum > 4 && verdorNum < 10) {
                    if (pvNum[i] > 0) {
                        if (pvNum[i] == maxPvNum) {
                            meritArray[i] = 0;
                        } else {
                            meritArray[i] = (pvNum[i] / maxPvNum) * 20;
                        }
                    }

                } else if (verdorNum > 10) {
                    if (pvNum[i] > 0) {
                        if (pvNum[i] > 15) {
                            meritArray[i] = 0;
                        } else {
                            meritArray[i] = (15 - pvNum[i]) * 1.5;//哈哈24444
                            if (meritArray[i] > 21) {
                                meritArray[i] = 21;
                            }
                        }
                    }
                }
                allFenshu[i][6] = allFenshu[i][5]; //后评价平均分（刘红新增）
                allFenshu[i][5] = allFenshu[i][5] - meritArray[i]; //后评价得分（刘红新增）

                //规模效益得分 btliu
                double scaleMark = 0;
                if (verdorNum <= 3) {
                    scaleMark = 0;
                } else if (verdorNum >= 4 && verdorNum <= 10) {
                    scaleMark = (pvNum[i] / maxPvNum) * 30;
                } else if (verdorNum >= 11) {
                    scaleMark = pvNum[i] * 2;
                }
                if (scaleMark > 30) {
                    scaleMark = 30;//30分封顶
                }
                //2014版后评价平均分
                allFenshu[i][7] = (sumGroupFenshu[i] + (PList.size() - pvNum[i]) * minGroupFenshu[i]) / PList.size() * 0.7;
                //2014版后评价得分
                allFenshu[i][8] = allFenshu[i][7] + scaleMark;

                int n = 0; //n 为模板覆盖省份数量
                String sql = "select count(DISTINCT(reportOrgCode)) from  t_eem_excel_page \n" +
                        "where tpInputID =  (select object_id from t_eom_temp_info where REL_TEMP_ID = "+ formId +")";
                List<Map> l = baseDAO.findNativeSQL(sql,null);
                if(l.size() > 0){
                    n = Integer.parseInt(l.get(0).get("count(distinct(reportorgcode))") + "");
                    //n = (int)pvNum[i];
                }
                n = (int)pvNum[i];

                //2016年版后评价得分
                //Apple-----20170927
                allFenshu[i][9] = (sumGroupFenshu[i] + (31 - n) * minGroupFenshu[i])/31 * 0.8;
                //2016年规模效益分  如大于20分  则 为 20 分

                //allFenshu[i][10] = n;
                allFenshu[i][10] = scaleMark;
                if(allFenshu[i][10] > 20){
                    allFenshu[i][10] = 20;
                }
                //2017年版后评价得分
//                allFenshu[i][11] = (sumGroupFenshu[i] + (31 - n) * minGroupFenshu[i])/31 * 0.8 +n;
                allFenshu[i][11] = (sumGroupFenshu[i] + (31 - n) * minGroupFenshu[i])/31 * 0.8 ;



                if(!content.contains(DIE_XING_GUANG_LAN)){
                    //2017年规模效益分
                    allFenshu[i][12] = 0;

                    allFenshu[i][12] = scaleMark;
                    if(allFenshu[i][12] > 20){
                        allFenshu[i][12] = 20;
                    }
                }
                /*
                if(pvLength_[i] != 0){
                  //  if(lengths[i]/pvLength_[i] > 0.05){
                    if(true){
                        allFenshu[i][12] = n;
                        if(allFenshu[i][12] > 20){
                            allFenshu[i][12] = 20;
                        }
                    }
                }else{
                    allFenshu[i][12] = 0;
                }*/

            }
            //add

            int col = PList.size() + spaceRow;
            WritableCell rCell = null;
            //java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00");


            rCell = new Label(col, 0, "总部项目集采得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字
            rCell = new Label(col + 1, 0, "规模效益得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字
            rCell = new Label(col + 2, 0, "建议暂缓下期集采", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字

            //向excel表格中增加新的列
            if (!content.contains(GUANG_LAN_CE_SHI)) {
                rCell = new Label(col + 3, 0, "故障率", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字
//		 add

                rCell = new Label(col + 4, 0, "后评价平均分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 5, 0, "后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                //add by btliu
                rCell = new Label(col + 6, 0, "2014版后评价平均分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 7, 0, "后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                //光缆综合模板新增四列   2016年版后评价得分  2016年规模效益分   2017年版后评价得分  2017版有效规模效益分
                rCell = new Label(col + 8, 0, "2016年版后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 9, 0, "2016年规模效益分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 10, 0, "2017年版后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 11, 0, "2017年版规模效益分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                //end
            } else {
                rCell = new Label(col + 3, 0, "后评价平均分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 4, 0, "后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字
//		add by btliu
                rCell = new Label(col + 5, 0, "2014版后评价平均分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 6, 0, "后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字
                //end

                //光缆测试模板新增三列   2016年版后评价得分  2016年规模效益分   2017年版后评价得分
                rCell = new Label(col + 7, 0, "2016年版后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 8, 0, "2016年规模效益分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 9, 0, "2017年版后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字
            }


            List<Double> sortList = new ArrayList<Double>();
            for (int i = 0; i < allFenshu.length; i++) {
                try {

                    double tt = allFenshu[i][2];
                    sortList.add(tt);
                } catch (Exception ex) {

                }
            }
            Collections.sort(sortList);
            int a = (int) (sortList.size() * 0.7);
            logger.info("a~~~~~~~~~" + a + "!!!!!!!!!allFenshu" + allFenshu.length);
            //  logger.info("a~~~~~~~~~"+a+"!!!!!!!!!sortList"+sortList.size());
            //double aMin = sortList.get(a)/PList.size();
            double aMin = sortList.get(a);


            for (int z = 0; z < allFenshu.length; z++) {
                double shengFenshu = allFenshu[z][0];
                double markFensu = allFenshu[z][1];
                double real = allFenshu[z][2];
                double markCount = allFenshu[z][3];
                double percent = allFenshu[z][4];//故障率
                double PvFenshu5 = allFenshu[z][5];  //新增
                double pvFenshuAvg = allFenshu[z][6];
                double groupFenshuAvg = allFenshu[z][7];
                double groupFenshu = allFenshu[z][8];
                //2016年版后评价得分
                double groupFenshu_2016 = allFenshu[z][9];
                //2016年版后评价效益分
                double groupFenshu_2016xy = allFenshu[z][10];
                //2017年版后评价得分
                double groupFenshu_2017 = allFenshu[z][11];
                //2017年规模效益分
                double groupFenshu_2017xy = allFenshu[z][12];


                rCell = null;
                col = PList.size() + spaceRow;
                try {
                    if (markCount > 0) {
                        if (real < 60) {
                            rCell = new Label(col, 1 + (z * pix), df.format(real), ExportUtil.getBackfround(jxl.format.Colour.RED, 12));

                        } else {
                            if (real >= aMin) {
                                rCell = new Label(col, 1 + (z * pix), df.format(real), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                            } else {
                                rCell = new Label(col, 1 + (z * pix), df.format(real), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                            }
                        }
                    } else {
                        rCell = new Label(col, 1 + (z * pix), "-", ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                    }
                    sheet.addCell(rCell);//具体数字
                    sheet.mergeCells(col, 1 + (z * pix), col, pix + (z * pix));


                    rCell = new Label(col + 1, 1 + (z * pix), df.format(markFensu), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                    sheet.addCell(rCell);//具体数字
                    sheet.mergeCells(col + 1, 1 + (z * pix), col + 1, pix + (z * pix));

                    if (markCount > 0) {
                        if (shengFenshu < 40) {
                            rCell = new Label(col + 2, 1 + (z * pix), df.format(shengFenshu), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {

                            rCell = new Label(col + 2, 1 + (z * pix), df.format(shengFenshu), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));

                        }
                    } else {
                        rCell = new Label(col + 2, 1 + (z * pix), "-", ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                    }

                    sheet.addCell(rCell);//具体数字
                    sheet.mergeCells(col + 2, 1 + (z * pix), col + 2, pix + (z * pix));

                    if (!content.contains(GUANG_LAN_CE_SHI)) {
                        if (percent > 0) {
                            rCell = new Label(col + 3, 1 + (z * pix), df.format(percent * 100) + "%", ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));

                        } else {
                            rCell = new Label(col + 3, 1 + (z * pix), "-", ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                        }

                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 3, 1 + (z * pix), col + 3, pix + (z * pix));
                    }


//				/ add
                    if (!content.contains(GUANG_LAN_CE_SHI)) {//pvFenshuAvg
                        //后评价平均分
                        rCell = new Label(col + 4, 1 + (z * pix), df.format(pvFenshuAvg), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));

                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 4, 1 + (z * pix), col + 4, pix + (z * pix));

                        // 后评价的得分
                        if (PvFenshu5 <= 0) {
                            rCell = new Label(col + 5, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 5, 1 + (z * pix), df.format(PvFenshu5), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 5, 1 + (z * pix), col + 5, pix + (z * pix));

                        //2014版后评价平均分
                        rCell = new Label(col + 6, 1 + (z * pix), df.format(groupFenshuAvg), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));

                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 6, 1 + (z * pix), col + 6, pix + (z * pix));

                        // 后评价的得分
                        if (groupFenshu <= 0) {
                            rCell = new Label(col + 7, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 7, 1 + (z * pix), df.format(groupFenshu), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 7, 1 + (z * pix), col + 7, pix + (z * pix));

                        //2016年版后评价得分
                        if (groupFenshu_2016 <= 0) {
                            rCell = new Label(col + 8, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 8, 1 + (z * pix), df.format(groupFenshu_2016), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 8, 1 + (z * pix), col + 7, pix + (z * pix));

                        //2016年规模效益分
                        if (groupFenshu_2016xy <= 0) {
                            rCell = new Label(col + 9, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 9, 1 + (z * pix), df.format(groupFenshu_2016xy), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 9, 1 + (z * pix), col + 7, pix + (z * pix));

                        //2017年后评价得分
                        if (groupFenshu_2017 <= 0) {
                            rCell = new Label(col + 10, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 10, 1 + (z * pix), df.format(groupFenshu_2017), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 10, 1 + (z * pix), col + 7, pix + (z * pix));


                        //2017年后评价规模效益得分
                        if (groupFenshu_2017xy <= 0) {
                            rCell = new Label(col + 11, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 11, 1 + (z * pix), df.format(groupFenshu_2017xy), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 11, 1 + (z * pix), col + 7, pix + (z * pix));


                    } else {
                        //后评价平均分
                        rCell = new Label(col + 3, 1 + (z * pix), df.format(pvFenshuAvg), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 3, 1 + (z * pix), col + 3, pix + (z * pix));

                        //后评价得分
                        if (PvFenshu5 <= 0) {
                            rCell = new Label(col + 4, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 4, 1 + (z * pix), df.format(PvFenshu5), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));

                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 4, 1 + (z * pix), col + 4, pix + (z * pix));
                        //2014版后评价平均分
                        rCell = new Label(col + 5, 1 + (z * pix), df.format(groupFenshuAvg), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));

                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 5, 1 + (z * pix), col + 5, pix + (z * pix));

                        // 后评价的得分
                        if (groupFenshu <= 0) {
                            rCell = new Label(col + 6, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 6, 1 + (z * pix), df.format(groupFenshu), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 6, 1 + (z * pix), col + 6, pix + (z * pix));


                        //2016年版后评价得分
                        if (groupFenshu <= 0) {
                            rCell = new Label(col + 7, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 7, 1 + (z * pix), df.format(groupFenshu_2016), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 7, 1 + (z * pix), col + 7, pix + (z * pix));

                        //2016年规模效益分
                        if (groupFenshu <= 0) {
                            rCell = new Label(col + 8, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 8, 1 + (z * pix), df.format(groupFenshu_2016xy), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 8, 1 + (z * pix), col + 7, pix + (z * pix));

                        //2017年后评价得分
                        if (groupFenshu <= 0) {
                            rCell = new Label(col + 9, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 9, 1 + (z * pix), df.format(groupFenshu_2017), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 9, 1 + (z * pix), col + 7, pix + (z * pix));


                        //2017年规模效益分
                        /*
                        if (groupFenshu <= 0) {
                            rCell = new Label(col + 10, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 10, 1 + (z * pix), df.format(groupFenshu_2017xy), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 10, 1 + (z * pix), col + 7, pix + (z * pix));
                        */
                    }

                } catch (Exception ex) {
                    rCell = new Label(col, 2 + (z * pix), "hhh", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                    sheet.addCell(rCell);//具体数字
                }
            }
        }
    }

    private void dealSheetForGUANGLANnew3(WritableWorkbook wwb, WritableSheet sheet, List<OrgEntity> PList, String reportDateStr, boolean isSearchHQData, String dealType, String reportYear, double coefficient, String equipType, String content, String formId) throws Exception {

        java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
        WritableSheet ws = null;
        for (WritableSheet wws : wwb.getSheets()) {
            if (wws.getName().equals("分省后评价得分")) {//获取汇总模板中分省后评价得分的sheet页
                ws = wws;
            }
        }
        ws.setColumnView(0, 50);
        List<NameValue> spaceList = new ArrayList<NameValue>();
        int spaceRow = 0;//填充数据到的具体的行数5

        String companyListSQL = "";


        if ("".equals(reportYear)) {
            if (content.equals(DIE_XING_GUANG_LAN)) {//碟形光缆
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='72' and  (iswithdraw is  null or iswithdraw<>'static') and  (reportyear='2015' or reportyear='2016') and  DELETED_FLAG=FALSE";
                pix = 16;
            } else if (content.contains(GUANG_LAN)) {//光缆
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='47' and (iswithdraw is  null or iswithdraw<>'static') and  (reportyear='2015' or reportyear='2016') and  DELETED_FLAG=FALSE";
                pix = 16;
            } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {//碟形光缆测试
                pix = 12;
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='71' and (iswithdraw is  null or iswithdraw<>'static') and  (reportyear='2015' or reportyear='2016') and  DELETED_FLAG=FALSE";
            } else if (content.contains(GUANG_LAN_CE_SHI)) {//光缆测试
                pix = 6;
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='46' and (iswithdraw is  null or iswithdraw<>'static') and  (reportyear='2015' or reportyear='2016') and  DELETED_FLAG=FALSE";
            }
        } else {
            if (content.equals(DIE_XING_GUANG_LAN)) {
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='72'  and DELETED_FLAG=FALSE  and  (iswithdraw is  null or iswithdraw<>'static') and  reportyear='" + reportYear + "'";
                pix = 16;
            } else if (content.contains(GUANG_LAN)) {
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='47'   and DELETED_FLAG=FALSE and (iswithdraw is  null or iswithdraw<>'static') and  reportyear='" + reportYear + "'";
                pix = 16;
            } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {
                pix = 12;
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='71'  and DELETED_FLAG=FALSE  and (iswithdraw is  null or iswithdraw<>'static') and  reportyear='" + reportYear + "'";
            } else if (content.contains(GUANG_LAN_CE_SHI)) {
                pix = 6;
                companyListSQL = "select distinct(pagename) from t_eem_excel_page where tpInputID='46'   and DELETED_FLAG=FALSE and (iswithdraw is  null or iswithdraw<>'static') and  reportyear='" + reportYear + "'";
            }//todo 这里有写死的id 及表明需要修改

        }


//        List<Map> companyList = hibDao.queryBySql(companyListSQL);
        List<Map> companyList = baseDAO.findNativeSQL(companyListSQL, null);
        if (companyList.size() > 0) {

            //新增加start
            //将厂家写到新的sheet页中
            for (int k = 0; k < companyList.size(); k++) {
                String name = companyList.get(k).get("pagename").toString();
                Label label = new Label(0, k + 1, name, ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                ws.addCell(label);
            }
            //将省份写入到sheet页中
            for (int k = 0; k < PList.size(); k++) {
                String name = PList.get(k).getOrgName();
                Label label = new Label(k + 1, 0, name, ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                ws.addCell(label);
            }
            //end


            spaceRow = getSpaceList(sheet, reportDateStr, dealType, reportYear, spaceList, isSearchHQData);
            int verdorNum = companyList.size();  // 现网供应商数量，取sheet数量
            double[] pvNum = new double[verdorNum];  //存放各个厂家提供服务的省份数量

            //double[][] allFenshu = new double[companyList.size()][9];//新增加两列2014版后评价平均分，2014版后评价得分  2014.10.10 btliu
            //光缆测试汇总 新增加  2016年版后评价得分    2016年规模效益分        2017年版后评价得分   三列
            //光缆综合汇总 新增加  2016年版后评价得分    2016年规模效益分        2017年版后评价得分  2017年规模效益分  四列
            double[][] allFenshu = new double[companyList.size()][9 + 3 + 1];
            double[] sumGroupFenshu = new double[companyList.size()];//存放每个厂家的分省后评价得分和
            double[] minGroupFenshu = new double[companyList.size()];//存放每个厂家覆盖省份中最小分省后评价得分
            //存放各个厂商光缆总长度
            double[] lengths = new double[companyList.size()];
            double[] OrgLengths = new double[PList.size()];


            //double[] pvLength_ = new double[allFenshu.length];
            if (content.contains(DIE_XING_GUANG_LAN)) {
                String sb="SELECT (( SELECT sum(txtvalue) AS VALUE FROM t_eem_excel_page_values v, t_eem_excel_page p WHERE v.pageid = p.pageid AND ( v.rowindex IN ( '4', '5', '6', '7', '8', '9', '10', '11' )) AND v.colindex IN ('3', '4', '5', '6') AND p.tpInputID = 72 AND txtvalue IS NOT NULL AND p.pagename = '$$pageName' AND p.reportdate = '$$date' AND p.reportOrgCode IN ('$$depart') AND p.reportYear = '$$reportYear' )) VALUE FROM DUAL";
                sb= sb.replace("$$reportYear",reportYear);
                sb =  sb.replace("$$date",reportDateStr);
                for(int q=0;q<PList.size();q++){
                    OrgLengths[q]=0;

                    String sb2 =  sb.replace("$$depart", PList.get(q).getOrgCode());
                    for(int z = 0; z < companyList.size(); z++){
                        String sb3 = sb2.replace("$$pageName", companyList.get(0).get("pagename").toString());
                        Object obj = null;
                        obj = baseDAO.findNativeSQL(sb3, null);
                        if (obj != null) {
                            List list = (List) obj;
                            if (list != null && list.size() > 0 && list.get(0) != null) {
                                Map resMap = (Map) list.get(0);
                                Object ob = resMap.get("value");
                                if (ob != null) {
                                    OrgLengths[q]+=  new Double(ob.toString()).doubleValue();;

                                }
                            }
                        }
                    }

                }
            }


            for (int z = 0; z < companyList.size(); z++) {

                String company = companyList.get(z).get("pagename").toString();
                logger.info("~~~~~~~~~`" + z + "!!!" + company);
                List<List<String>> dataList = getDataListGUANGLAN(sheet, PList, spaceList, spaceRow, STAT_TYPE_COL, company, z, content);
                if (dataList.get(0).size() == 0) {
                    continue;
                }
                double[][] resArray = getResArray(dataList);
                double[] sumArray = new double[dataList.get(0).size()];//各种指标的和
                double[][] pvSumArray = new double[dataList.size()][dataList.get(0).size()];
                double[] sumFenshuArray = new double[dataList.get(0).size()];//各种指标的分数的和
                double[] mark = new double[dataList.get(0).size()];//保存最后的规模效益得分
                double real = 0;
                double[] pvReal = new double[dataList.size()];
                double PvFenshu = 0;
                double[] finalArray = new double[dataList.get(0).size()];
                double[][] pvSumFenshuArray = new double[dataList.size()][dataList.get(0).size()];
                double[] pvLength = new double[dataList.size()]; //各个省份的光缆总长度
                //pvLength_ =  new double[pvLength.length];
                int markCount = 0;
                int pvTindex = 1;
                Set<String> tmpSet = new HashSet<String>();
                List<Double> tempPvReal = new ArrayList<Double>();//用于计算min(ai) 2014.10.10 btliu
                if (content.contains(GUANG_LAN)) {
                    allFenshu[z][12] = 0;
                    for (int h = 0; h < resArray.length; h++) {
                        double ss = 0L;
                        ss = resArray[h][1] + resArray[h][2] + resArray[h][3] + resArray[h][4];
                        if (ss > 0 && (ss / OrgLengths[h]) > 0.05) {
                            allFenshu[z][12]++;
                        }
                    }
                    if (allFenshu[z][12] > 20) {
                        allFenshu[z][12] = 20;
                    }
                }
                if (content.contains(DIE_XING_GUANG_LAN)) {
                    for (int i = 0; i < resArray[0].length; i++) {
                        for (int j = 0; j < resArray.length; j++) {
                            if (i >= 0 && i <= 5 && resArray[j][i] != 0) {
                                tmpSet.add(j + "");
                            }

                            if (i >= 10 && i <= 14) {
                                if (sumArray[i] < resArray[j][i]) {
                                    sumArray[i] = resArray[j][i];
                                }
                            } else {
                                sumArray[i] = sumArray[i] + resArray[j][i];
                            }
                        }
                    }

                    ////add
                    for (int i = 0; i < resArray.length; i++) {
                        int index = 1;
                        pvLength[i] = resArray[i][index++] + resArray[i][index++] + resArray[i][index++] + resArray[i][index++];
                        //pvLength_[i] =  pvLength[i];
                        if (pvLength[i] > 0) {
                            pvSumFenshuArray[i][index] = 15 - 15 * (resArray[i][index++] / pvLength[i]);
                            pvSumFenshuArray[i][index] = 5 - 5 * (resArray[i][index++] / pvLength[i]);
                            pvSumFenshuArray[i][index] = 15 - 15 * (resArray[i][index++] / pvLength[i]);

                            double tmp = 0.0;
                            if (resArray[i][index + 1] == 0.0) {
                                tmp = 0;
                            } else {
                                tmp = resArray[i][index] / resArray[i][index + 1];//故障率  tindex=9   维护2新增接头的衰减（双向平均0.04dB以上的芯数）(注意：是光纤芯数,不是光缆接头个数！！！)/接头总芯数
                            }

                            double pvfaultPercent = tmp;   //0.026898130484547884
                            if (tmp > 0.1) {
                                tmp = 0.1;
                            }
                            pvSumFenshuArray[i][index] = 20 - 200 * (tmp);

                            index = index + 2;
                            pvSumFenshuArray[i][index] = resArray[i][index++];
                            pvSumFenshuArray[i][index] = resArray[i][index++];
                            pvSumFenshuArray[i][index] = resArray[i][index++];
                            pvSumFenshuArray[i][index] = resArray[i][index++];
                            pvSumFenshuArray[i][index] = resArray[i][index++];

                            if (resArray[i][index + 1] == 0.0) {
                                tmp = 0;
                            } else {
                                tmp = resArray[i][index] / resArray[i][index + 1];//故障率
                            }


                            if (tmp > 0.1) {
                                tmp = 0.1;
                            }

                            pvSumFenshuArray[i][index] = 10 - 100 * (tmp);
                        }
                    }
                    for (int i = 0; i < pvSumFenshuArray.length; i++) {
                        for (int j = 0; j < pvSumFenshuArray[0].length; j++) {
                            double tFenshu = pvSumFenshuArray[i][j];
                            if (Double.isNaN(tFenshu)) {
                                tFenshu = 0;
                            }
                            pvReal[i] = tFenshu + pvReal[i];
                        }
                    }

                    for (int i = 0; i < pvReal.length; i++) {
                        pvReal[i] = pvReal[i] * 1.25;

                        Label label = new Label(i + 1, z + 1, df.format(pvReal[i]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 8));
                        ws.addCell(label);
                        PvFenshu += pvReal[i];

                        if (pvReal[i] > 0) {
                            tempPvReal.add(pvReal[i]);
                        }
                    }
                    if (tmpSet.size() > 0) {
                        sumGroupFenshu[z] = PvFenshu;

                        PvFenshu = PvFenshu / tmpSet.size();
                    } else {
                        PvFenshu = 0;
                    }
                    ////add
                } else if (content.contains(GUANG_LAN)) {
                    for (int i = 0; i < resArray[0].length; i++) {
                        for (int j = 0; j < resArray.length; j++) {
                            if (i >= 0 && i <= 3 && resArray[j][i] != 0) {
                                tmpSet.add(j + "");
                            }

                            if (i >= 7 && i <= 8 || i >= 11 && i <= 14) {
                                if (sumArray[i] < resArray[j][i]) {
                                    if (resArray[j][i] > 3) {
                                        sumArray[i] = 3;
                                    } else {
                                        sumArray[i] = resArray[j][i];
                                    }
                                }
                            } else {
                                sumArray[i] = sumArray[i] + resArray[j][i];
                            }
                        }
                    }

                    ////add
                    for (int i = 0; i < resArray.length; i++) {
                        int index = 1;
                        pvLength[i] = resArray[i][index++] + resArray[i][index++] + resArray[i][index++];
                        //pvLength_[i] =  pvLength[i];
                        if (pvLength[i] > 0) {
                            pvSumFenshuArray[i][index] = 4 - 4 * (resArray[i][index++] / pvLength[i]);   //tindex=4
                            pvSumFenshuArray[i][index] = 4 - 4 * (resArray[i][index++] / pvLength[i]);
                            pvSumFenshuArray[i][index] = 4 - 4 * (resArray[i][index++] / pvLength[i]);

                            if (resArray[i][index] > 3) {  //index=7
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }


                            if (resArray[i][index] > 3) {  //index=8
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }

                            double tmp = 0.0;
                            if (resArray[i][index + 1] == 0.0) {
                                tmp = 0;
                            } else {
                                tmp = resArray[i][index] / resArray[i][index + 1];//故障率  tindex=9   维护2新增接头的衰减（双向平均0.04dB以上的芯数）(注意：是光纤芯数,不是光缆接头个数！！！)/接头总芯数
                            }


                            double pvfaultPercent = tmp;

                            if (tmp > 0.1) {
                                tmp = 0.1;
                            }
                            if (!Double.isNaN(tmp)) {
                                pvSumFenshuArray[i][index] = 20 - 200 * (tmp);
                            } else {
                                pvSumFenshuArray[i][index] = 0;
                            }


                            index = index + 2;   //tindex=11
                            if (resArray[i][index] > 3) {  //index=11
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }

                            if (resArray[i][index] > 3) {  //index=12
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }
                            if (resArray[i][index] > 3) {  //index=13
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }
                            if (resArray[i][index] > 3) {  //index=14
                                pvSumFenshuArray[i][index] = 3;
                                index++;
                            } else {
                                pvSumFenshuArray[i][index] = resArray[i][index++];
                            }

                            if (resArray[i][index + 1] == 0.0) {
                                tmp = 0;
                            } else {
                                tmp = resArray[i][index] / resArray[i][index + 1];//故障率   //tindex=15    因为光缆自身质量问题所造成的光缆故障次数/故障总次数
                            }


                            if (tmp > 0.1) {
                                tmp = 0.1;
                            }
                            if (!Double.isNaN(tmp)) {
                                pvSumFenshuArray[i][index] = 10 - 100 * (tmp);
                            } else {
                                pvSumFenshuArray[i][index] = 0;
                            }
                        }
                    }

                    for (int i = 0; i < pvSumFenshuArray.length; i++) {
                        for (int j = 0; j < pvSumFenshuArray[0].length; j++) {
                            double tFenshu = pvSumFenshuArray[i][j];
                            if (Double.isNaN(tFenshu)) {
                                tFenshu = 0;
                            }
                            pvReal[i] = tFenshu + pvReal[i];
                        }
                    }
                    //int count=0;
                    for (int i = 0; i < pvReal.length; i++) {//哈哈99999999999999999
                        pvReal[i] = pvReal[i] * 1.6666667;

                        //start
                        Label label = new Label(i + 1, z + 1, df.format(pvReal[i]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 8));
                        ws.addCell(label);
                        //end

                        PvFenshu += pvReal[i];

                        if (pvReal[i] > 0) {
                            tempPvReal.add(pvReal[i]);
                        }
                    }

                    if (tmpSet.size() > 0) {
                        sumGroupFenshu[z] = PvFenshu;

                        PvFenshu = PvFenshu / tmpSet.size();
                    } else {
                        PvFenshu = 0;
                    }

                    /////add
                } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {
                    int ttt = 0;
                    double tttt = 0;
                    for (int i = 0; i < resArray.length; i++) {
                        int pvt = 0;
                        double pvtt = 0;
                        for (int j = 0; j < resArray[0].length; j++) {

                            if (j >= 0 && j <= 4 && resArray[i][j] != 0) {
                                tmpSet.add(i + "");

                            }
                            //if(resArray[i][j]<0.2&&resArray[i][j]!=0){
                            if (resArray[i][j] < 0.18 && resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + 0.18;
                                //tttt = tttt+0.2;
                                //add
                                pvt++;
                                pvtt = pvtt + 0.18;
                                //pvtt=pvtt+0.2;
                            } //else if(resArray[i][j]>0.28&&resArray[i][j]!=0){
                            else if (resArray[i][j] > 0.27 && resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + 0.27;
                                //tttt = tttt+0.28;
                                //add
                                pvt++;
                                pvtt = pvtt + 0.27;
                                //pvtt=pvtt+0.28;
                            } else if (resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + resArray[i][j];
                                //add
                                pvt++;
                                pvtt = pvtt + resArray[i][j];
                            }

                        }
                        //add
                        int ix = 1;
                        pvSumArray[i][0] = pvtt / pvt;  //记录每个省份的平均值
                        pvSumFenshuArray[i][ix] = (10 * (0.27 - pvSumArray[i][0])) / (0.27 - 0.18);
                        //pvSumFenshuArray[i][ix]= (10*(0.28 - pvSumArray[i][0]))/(0.28-0.2);
                        //add
                    }
                    sumArray[0] = tttt / ttt;

                    //add
                    for (int i = 0; i < pvSumFenshuArray.length; i++) {
                        for (int j = 0; j < pvSumFenshuArray[0].length; j++) {
                            double tFenshu = pvSumFenshuArray[i][j];
                            if (Double.isNaN(tFenshu)) {
                                tFenshu = 0;
                            }
                            pvReal[i] = tFenshu + pvReal[i];
                        }
                    }

                    for (int i = 0; i < pvReal.length; i++) {
                        pvReal[i] = pvReal[i] * 10;

                        Label label = new Label(i + 1, z + 1, df.format(pvReal[i]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 8));
                        ws.addCell(label);
                        PvFenshu += pvReal[i];

                        if (pvReal[i] > 0) {
                            tempPvReal.add(pvReal[i]);
                        }
                    }
                    if (tmpSet.size() > 0) {
                        sumGroupFenshu[z] = PvFenshu;

                        PvFenshu = PvFenshu / tmpSet.size();
                    } else {
                        PvFenshu = 0;
                    }
                    //add
                } else if (content.contains(GUANG_LAN_CE_SHI)) {
                    for (int i = 0; i < resArray[0].length; i++) {
                        for (int j = 0; j < resArray.length; j++) {
                            if (i >= 0 && i <= 6 && resArray[j][i] != 0) {
                                tmpSet.add(j + "");
                            }
                        }
                    }


                    int ttt = 0;
                    double tttt = 0;
                    for (int i = 0; i < resArray.length; i++) {
                        int pvt = 0;
                        double pvtt = 0;
                        for (int j = 0; j < resArray[0].length; j++) {
                            if (resArray[i][j] < 0.180 && resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + 0.18;
                                // add
                                pvtt = pvtt + 0.18;
                                pvt++;
                            } else if (resArray[i][j] > 0.26 && resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + 0.26;
//								add
                                pvtt = pvtt + 0.26;
                                pvt++;
                            } else if (resArray[i][j] != 0) {
                                ttt++;
                                tttt = tttt + resArray[i][j];
                                pvtt = pvtt + resArray[i][j];
                                pvt++;
                            }

                        }
                        //add
                        int ix = 1;

                        pvSumArray[i][0] = pvtt / pvt;  //记录每个省份的平均值
                        if (!Double.isNaN(pvSumArray[i][0])) {
                            pvSumFenshuArray[i][ix] = (40 * (0.26 - pvSumArray[i][0])) / (0.26 - 0.18);
                        } else {
                            pvSumFenshuArray[i][ix] = 0;
                        }

                        //add
                    }
                    sumArray[0] = tttt / ttt;
                    //add
                    for (int i = 0; i < pvSumFenshuArray.length; i++) {
                        for (int j = 0; j < pvSumFenshuArray[0].length; j++) {
                            double tFenshu = pvSumFenshuArray[i][j];
                            if (Double.isNaN(tFenshu)) {
                                tFenshu = 0;
                            }
                            pvReal[i] = tFenshu + pvReal[i];
                        }
                    }

                    for (int i = 0; i < pvReal.length; i++) {
                        pvReal[i] = pvReal[i] * 2.5;
                        //start
                        Label label = new Label(i + 1, z + 1, df.format(pvReal[i]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 8));
                        ws.addCell(label);
                        //end
                        PvFenshu += pvReal[i];

                        if (pvReal[i] > 0) {
                            tempPvReal.add(pvReal[i]);
                        }
                    }
                    if (tmpSet.size() > 0) {
                        sumGroupFenshu[z] = PvFenshu;
                        PvFenshu = PvFenshu / tmpSet.size();
                    } else {
                        PvFenshu = 0;
                    }

                    //add
                }

                markCount = tmpSet.size();
                pvNum[z] = markCount;   //存放各个供应商覆盖省份的数量

                double length = 0;//光缆的总长度
                int tindex = 1;
                double faultPercent = 0;//故障率
                if (content.contains(DIE_XING_GUANG_LAN)) {
                    try {
                        length = sumArray[tindex++] + sumArray[tindex++] + sumArray[tindex++] + sumArray[tindex++];
                    } catch (Exception ex) {
                        logger.info("company:" + company);
                    }
                    sumFenshuArray[tindex] = 15 - 15 * (sumArray[tindex++] / length);
                    sumFenshuArray[tindex] = 5 - 5 * (sumArray[tindex++] / length);
                    sumFenshuArray[tindex] = 15 - 15 * (sumArray[tindex++] / length);
                    double tmp = 0.0;
                    if (sumArray[tindex + 1] == 0.0) {
                        tmp = 0;
                    } else {
                        tmp = sumArray[tindex] / sumArray[tindex + 1];//故障率
                    }


                    faultPercent = tmp;
                    if (tmp > 0.1) {
                        tmp = 0.1;
                    }


                    sumFenshuArray[tindex] = 20 - 200 * (tmp);
                    tindex = tindex + 2;

                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    if (sumArray[tindex + 1] == 0.0) {
                        tmp = 0;
                    } else {
                        tmp = sumArray[tindex] / sumArray[tindex + 1];//故障率
                    }

                    if (tmp > 0.1) {
                        tmp = 0.1;
                    }

                    sumFenshuArray[tindex] = 10 - 100 * (tmp);
                } else if (content.contains(GUANG_LAN)) {//哈哈11111111111
                    try {
                        //光缆总长度
                        length = sumArray[tindex++] + sumArray[tindex++] + sumArray[tindex++];
                        //记录各个厂商光缆长度
                        lengths[z] = length;
                    } catch (Exception ex) {
                        logger.info("company:" + company);
                    }
                    sumFenshuArray[tindex] = 4 - 4 * (sumArray[tindex++] / length);
                    sumFenshuArray[tindex] = 4 - 4 * (sumArray[tindex++] / length);
                    sumFenshuArray[tindex] = 4 - 4 * (sumArray[tindex++] / length);
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    double tmp = 0.0;
                    if (sumArray[tindex + 1] == 0.0) {
                        tmp = 0;
                    } else {
                        tmp = sumArray[tindex] / sumArray[tindex + 1];//故障率
                    }

                    faultPercent = tmp;
                    if (tmp > 0.1) {
                        tmp = 0.1;
                    }

                    sumFenshuArray[tindex] = 20 - 200 * (tmp);
                    tindex = tindex + 2;


                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    sumFenshuArray[tindex] = sumArray[tindex++];
                    if (sumArray[tindex + 1] == 0.0) {
                        tmp = 0;
                    } else {
                        tmp = sumArray[tindex] / sumArray[tindex + 1];//故障率
                    }
                    if (tmp > 0.1) {
                        tmp = 0.1;
                    }
                    sumFenshuArray[tindex] = 10 - 100 * (tmp);
                } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {
                    //sumFenshuArray[tindex] = (10*(0.28 - sumArray[0]))/(0.28-0.2);
                    sumFenshuArray[tindex] = (10 * (0.27 - sumArray[0])) / (0.27 - 0.18);

                } else if (content.contains(GUANG_LAN_CE_SHI)) {
                    sumFenshuArray[tindex] = (40 * (0.26 - sumArray[0])) / (0.26 - 0.18);
                }

                for (int i = 0; i < sumFenshuArray.length; i++) {
                    double tFenshu = sumFenshuArray[i];
                    if (Double.isNaN(tFenshu)) {
                        tFenshu = 0;
                    }
                    real = tFenshu + real;
                }


                if (content.contains(DIE_XING_GUANG_LAN)) {
                    real = real * 1.25;
                } else if (content.contains(GUANG_LAN)) {
                    real = real * 1.6666667;
                } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {
                    real = real * 10;
                } else if (content.contains(GUANG_LAN_CE_SHI)) {
                    real = real * 2.5;
                }
                int markFensu = markCount * 10;
                if (markFensu > 100) {
                    markFensu = 100;
                }
                allFenshu[z][0] = real;
                allFenshu[z][1] = markFensu;
                allFenshu[z][2] = real * 0.8 + markFensu * 0.2;
                allFenshu[z][3] = markCount;
                allFenshu[z][4] = faultPercent;
                allFenshu[z][5] = PvFenshu;

                //用冒泡计算每个厂家的最小分省后评价得分
                if (tempPvReal.size() > 1) {
                    for (int j = 0; j < tempPvReal.size() - 1; j++) {
                        for (int k = 0; k < tempPvReal.size() - 1 - j; k++) {
                            if (tempPvReal.get(k) > tempPvReal.get(k + 1)) {
                                double temp = tempPvReal.get(k);
                                tempPvReal.set(k, tempPvReal.get(k + 1));
                                tempPvReal.set(k + 1, temp);
                            }
                        }

                    }
                    minGroupFenshu[z] = tempPvReal.get(0);
                } else if (tempPvReal.size() > 0) {
                    minGroupFenshu[z] = tempPvReal.get(0);
                }
            }

            //add
            double maxPvNum = 0;
            double[] meritArray = new double[verdorNum];
            for (int i = 0; i < pvNum.length; i++) {

                double num = pvNum[i];
                if (num > maxPvNum) {
                    maxPvNum = num;
                }
            }

            for (int i = 0; i < allFenshu.length; i++) {
                if (verdorNum > 4 && verdorNum < 10) {
                    if (pvNum[i] > 0) {
                        if (pvNum[i] == maxPvNum) {
                            meritArray[i] = 0;
                        } else {
                            meritArray[i] = (pvNum[i] / maxPvNum) * 20;
                        }
                    }

                } else if (verdorNum > 10) {
                    if (pvNum[i] > 0) {
                        if (pvNum[i] > 15) {
                            meritArray[i] = 0;
                        } else {
                            meritArray[i] = (15 - pvNum[i]) * 1.5;//哈哈24444
                            if (meritArray[i] > 21) {
                                meritArray[i] = 21;
                            }
                        }
                    }
                }
                allFenshu[i][6] = allFenshu[i][5]; //后评价平均分（刘红新增）
                allFenshu[i][5] = allFenshu[i][5] - meritArray[i]; //后评价得分（刘红新增）

                //规模效益得分 btliu
                double scaleMark = 0;
                if (verdorNum <= 3) {
                    scaleMark = 0;
                } else if (verdorNum >= 4 && verdorNum <= 10) {
                    scaleMark = (pvNum[i] / maxPvNum) * 30;
                } else if (verdorNum >= 11) {
                    scaleMark = pvNum[i] * 2;
                }
                if (scaleMark > 30) {
                    scaleMark = 30;//30分封顶
                }
                //2014版后评价平均分
                allFenshu[i][7] = (sumGroupFenshu[i] + (PList.size() - pvNum[i]) * minGroupFenshu[i]) / PList.size() * 0.7;
                //2014版后评价得分
                allFenshu[i][8] = allFenshu[i][7] + scaleMark;

                int n = 0; //n 为模板覆盖省份数量
                String sql = "select count(DISTINCT(reportOrgCode)) from  t_eem_excel_page \n" +
                        "where tpInputID =  (select object_id from t_eom_temp_info where REL_TEMP_ID = "+ formId +")";
                List<Map> l = baseDAO.findNativeSQL(sql,null);
                if(l.size() > 0){
                    n = Integer.parseInt(l.get(0).get("count(distinct(reportorgcode))") + "");
                    //n = (int)pvNum[i];
                }
                n = (int)pvNum[i];

                //2016年版后评价得分
                allFenshu[i][9] = (sumGroupFenshu[i] + (31 - n) * minGroupFenshu[i])/31 * 0.8;
                //2016年规模效益分  如大于20分  则 为 20 分

                //allFenshu[i][10] = n;
                allFenshu[i][10] = scaleMark;
                if(allFenshu[i][10] > 20){
                    allFenshu[i][10] = 20;
                }
                //2017年版后评价得分
//                allFenshu[i][11] = (sumGroupFenshu[i] + (31 - n) * minGroupFenshu[i])/31 * 0.8 + n;
                allFenshu[i][11] = (sumGroupFenshu[i] + (31 - n) * minGroupFenshu[i])/31 * 0.8 ;



                if(!content.contains(GUANG_LAN)){
                    //2017年规模效益分
                    allFenshu[i][12] = 0;

                    allFenshu[i][12] = scaleMark;
                    if(allFenshu[i][12] > 20){
                        allFenshu[i][12] = 20;
                    }
                }
                /*
                if(pvLength_[i] != 0){
                  //  if(lengths[i]/pvLength_[i] > 0.05){
                    if(true){
                        allFenshu[i][12] = n;
                        if(allFenshu[i][12] > 20){
                            allFenshu[i][12] = 20;
                        }
                    }
                }else{
                    allFenshu[i][12] = 0;
                }*/

            }
            //add

            int col = PList.size() + spaceRow;
            WritableCell rCell = null;
            //java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00");


            rCell = new Label(col, 0, "总部项目集采得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字
            rCell = new Label(col + 1, 0, "规模效益得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字
            rCell = new Label(col + 2, 0, "建议暂缓下期集采", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            sheet.addCell(rCell);//具体数字

            //向excel表格中增加新的列
            if (!content.contains(GUANG_LAN_CE_SHI)) {
                rCell = new Label(col + 3, 0, "故障率", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字
//		 add

                rCell = new Label(col + 4, 0, "后评价平均分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 5, 0, "后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                //add by btliu
                rCell = new Label(col + 6, 0, "2014版后评价平均分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 7, 0, "后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                //光缆综合模板新增四列   2016年版后评价得分  2016年规模效益分   2017年版后评价得分  2017版有效规模效益分
                rCell = new Label(col + 8, 0, "2016年版后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 9, 0, "2016年规模效益分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 10, 0, "2017年版后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 11, 0, "2017年版规模效益分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                //end
            } else {
                rCell = new Label(col + 3, 0, "后评价平均分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 4, 0, "后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字
//		add by btliu
                rCell = new Label(col + 5, 0, "2014版后评价平均分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 6, 0, "后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字
                //end

                //光缆测试模板新增三列   2016年版后评价得分  2016年规模效益分   2017年版后评价得分
                rCell = new Label(col + 7, 0, "2016年版后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 8, 0, "2016年规模效益分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字

                rCell = new Label(col + 9, 0, "2017年版后评价得分", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字
            }


            List<Double> sortList = new ArrayList<Double>();
            for (int i = 0; i < allFenshu.length; i++) {
                try {

                    double tt = allFenshu[i][2];
                    sortList.add(tt);
                } catch (Exception ex) {

                }
            }
            Collections.sort(sortList);
            int a = (int) (sortList.size() * 0.7);
            logger.info("a~~~~~~~~~" + a + "!!!!!!!!!allFenshu" + allFenshu.length);
            //  logger.info("a~~~~~~~~~"+a+"!!!!!!!!!sortList"+sortList.size());
            //double aMin = sortList.get(a)/PList.size();
            double aMin = sortList.get(a);


            for (int z = 0; z < allFenshu.length; z++) {
                double shengFenshu = allFenshu[z][0];
                double markFensu = allFenshu[z][1];
                double real = allFenshu[z][2];
                double markCount = allFenshu[z][3];
                double percent = allFenshu[z][4];//故障率
                double PvFenshu5 = allFenshu[z][5];  //新增
                double pvFenshuAvg = allFenshu[z][6];
                double groupFenshuAvg = allFenshu[z][7];
                double groupFenshu = allFenshu[z][8];
                //2016年版后评价得分
                double groupFenshu_2016 = allFenshu[z][9];
                //2016年版后评价效益分
                double groupFenshu_2016xy = allFenshu[z][10];
                //2017年版后评价得分
                double groupFenshu_2017 = allFenshu[z][11];
                //2017年规模效益分
                double groupFenshu_2017xy = allFenshu[z][12];


                rCell = null;
                col = PList.size() + spaceRow;
                try {
                    if (markCount > 0) {
                        if (real < 60) {
                            rCell = new Label(col, 1 + (z * pix), df.format(real), ExportUtil.getBackfround(jxl.format.Colour.RED, 12));

                        } else {
                            if (real >= aMin) {
                                rCell = new Label(col, 1 + (z * pix), df.format(real), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                            } else {
                                rCell = new Label(col, 1 + (z * pix), df.format(real), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                            }
                        }
                    } else {
                        rCell = new Label(col, 1 + (z * pix), "-", ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                    }
                    sheet.addCell(rCell);//具体数字
                    sheet.mergeCells(col, 1 + (z * pix), col, pix + (z * pix));


                    rCell = new Label(col + 1, 1 + (z * pix), df.format(markFensu), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                    sheet.addCell(rCell);//具体数字
                    sheet.mergeCells(col + 1, 1 + (z * pix), col + 1, pix + (z * pix));

                    if (markCount > 0) {
                        if (shengFenshu < 40) {
                            rCell = new Label(col + 2, 1 + (z * pix), df.format(shengFenshu), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {

                            rCell = new Label(col + 2, 1 + (z * pix), df.format(shengFenshu), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));

                        }
                    } else {
                        rCell = new Label(col + 2, 1 + (z * pix), "-", ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                    }

                    sheet.addCell(rCell);//具体数字
                    sheet.mergeCells(col + 2, 1 + (z * pix), col + 2, pix + (z * pix));

                    if (!content.contains(GUANG_LAN_CE_SHI)) {
                        if (percent > 0) {
                            rCell = new Label(col + 3, 1 + (z * pix), df.format(percent * 100) + "%", ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));

                        } else {
                            rCell = new Label(col + 3, 1 + (z * pix), "-", ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                        }

                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 3, 1 + (z * pix), col + 3, pix + (z * pix));
                    }


//				/ add
                    if (!content.contains(GUANG_LAN_CE_SHI)) {//pvFenshuAvg
                        //后评价平均分
                        rCell = new Label(col + 4, 1 + (z * pix), df.format(pvFenshuAvg), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));

                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 4, 1 + (z * pix), col + 4, pix + (z * pix));

                        // 后评价的得分
                        if (PvFenshu5 <= 0) {
                            rCell = new Label(col + 5, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 5, 1 + (z * pix), df.format(PvFenshu5), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 5, 1 + (z * pix), col + 5, pix + (z * pix));

                        //2014版后评价平均分
                        rCell = new Label(col + 6, 1 + (z * pix), df.format(groupFenshuAvg), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));

                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 6, 1 + (z * pix), col + 6, pix + (z * pix));

                        // 后评价的得分
                        if (groupFenshu <= 0) {
                            rCell = new Label(col + 7, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 7, 1 + (z * pix), df.format(groupFenshu), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 7, 1 + (z * pix), col + 7, pix + (z * pix));

                        //2016年版后评价得分
                        if (groupFenshu_2016 <= 0) {
                            rCell = new Label(col + 8, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 8, 1 + (z * pix), df.format(groupFenshu_2016), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 8, 1 + (z * pix), col + 7, pix + (z * pix));

                        //2016年规模效益分
                        if (groupFenshu_2016xy <= 0) {
                            rCell = new Label(col + 9, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 9, 1 + (z * pix), df.format(groupFenshu_2016xy), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 9, 1 + (z * pix), col + 7, pix + (z * pix));

                        //2017年后评价得分
                        if (groupFenshu_2017 <= 0) {
                            rCell = new Label(col + 10, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 10, 1 + (z * pix), df.format(groupFenshu_2017), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 10, 1 + (z * pix), col + 7, pix + (z * pix));


                        //2017年后评价规模效益得分
                        if (groupFenshu_2017xy <= 0) {
                            rCell = new Label(col + 11, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 11, 1 + (z * pix), df.format(groupFenshu_2017xy), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 11, 1 + (z * pix), col + 7, pix + (z * pix));


                    } else {
                        //后评价平均分
                        rCell = new Label(col + 3, 1 + (z * pix), df.format(pvFenshuAvg), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 3, 1 + (z * pix), col + 3, pix + (z * pix));

                        //后评价得分
                        if (PvFenshu5 <= 0) {
                            rCell = new Label(col + 4, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 4, 1 + (z * pix), df.format(PvFenshu5), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));

                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 4, 1 + (z * pix), col + 4, pix + (z * pix));
                        //2014版后评价平均分
                        rCell = new Label(col + 5, 1 + (z * pix), df.format(groupFenshuAvg), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));

                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 5, 1 + (z * pix), col + 5, pix + (z * pix));

                        // 后评价的得分
                        if (groupFenshu <= 0) {
                            rCell = new Label(col + 6, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 6, 1 + (z * pix), df.format(groupFenshu), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 6, 1 + (z * pix), col + 6, pix + (z * pix));


                        //2016年版后评价得分
                        if (groupFenshu <= 0) {
                            rCell = new Label(col + 7, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 7, 1 + (z * pix), df.format(groupFenshu_2016), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 7, 1 + (z * pix), col + 7, pix + (z * pix));

                        //2016年规模效益分
                        if (groupFenshu <= 0) {
                            rCell = new Label(col + 8, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 8, 1 + (z * pix), df.format(groupFenshu_2016xy), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 8, 1 + (z * pix), col + 7, pix + (z * pix));

                        //2017年后评价得分
                        if (groupFenshu <= 0) {
                            rCell = new Label(col + 9, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 9, 1 + (z * pix), df.format(groupFenshu_2017), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 9, 1 + (z * pix), col + 7, pix + (z * pix));


                        //2017年规模效益分
                        /*
                        if (groupFenshu <= 0) {
                            rCell = new Label(col + 10, 1 + (z * pix), df.format(0), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            rCell = new Label(col + 10, 1 + (z * pix), df.format(groupFenshu_2017xy), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(col + 10, 1 + (z * pix), col + 7, pix + (z * pix));
                        */
                    }

                } catch (Exception ex) {
                    rCell = new Label(col, 2 + (z * pix), "hhh", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                    sheet.addCell(rCell);//具体数字
                }
            }
        }
    }


    //新加的汇总
    //为了移动核心网11个模板，在每一个新增加的sheet页，原始数据进行的操作
    private void dealSheetForHQ1(WritableSheet sheet, List<OrgEntity> PList, String reportDateStr, boolean isSearchHQData, String dealType, String reportYear) throws Exception {
        int rowCount = sheet.getRows();
        int colCount = sheet.getColumns();
        List<NameValue> spaceList = null;
        int spaceRow = 0;//填充数据到的具体的行数

        for (int i = 0; i < rowCount; i++) {  //行 循环
            if (spaceList != null && spaceList.size() > 0) {
                break;
            }

            spaceList = new ArrayList<NameValue>();
            for (int j = 0; j < colCount; j++) {//列 循环

                WritableCell cell = sheet.getWritableCell(j, i);
                String content = cell.getContents();

                if (content != null && content.trim().startsWith("##SQL:")) {

                    String sql = fromTempletContentToSql(content, "", reportDateStr, reportYear);
                    if (isSearchHQData) {
                        sql = sql.replaceAll("and p.WORK_ORDER_STATUS=\"已审核\"", "");
                    }
                    NameValue nv = new NameValue();
                    nv.setName(i + "");
                    nv.setValue(j + "");
                    nv.setRemark(sql);

                    spaceList.add(nv);
                } else if (content != null && content.trim().startsWith("##PV")) {
                    NameValue nv = new NameValue();
                    nv.setName(i + "");
                    nv.setValue(j + "");
                    nv.setRemark("PV");
                    spaceList.add(nv);
                }
            }
            spaceRow++;
        }
        spaceRow--;
        String res = "";
        WritableCell n = null;
        logger.info("sheet=" + sheet.getName());
        List<List<String>> dataList = getDataList(sheet, PList, spaceList, spaceRow, STAT_TYPE_ROW,isSearchHQData);


        double[][] resArray = new double[dataList.size()][dataList.get(0).size()];
        for (int i = 0; i < dataList.size(); i++) {
            List<String> tmp = dataList.get(i);//每个省的数据
            for (int j = 1; j < tmp.size(); j++) {//从1开始因被第一个是省份名字的单元格

                double aa = 0;
                try {
                    String a = tmp.get(j).toString();
                    aa = Double.parseDouble(a);
                } catch (Exception ex) {

                }
                resArray[i][j] = aa;
            }
        }
    }

    //处理后评价
    private void dealSheetForEvaluation(WritableSheet sheet, List<OrgEntity> PList, String reportDateStr, boolean isSearchHQData,
                                        String dealType, String reportYear,int sheetCountNow,ArrayList<ArrayList> arrayList) throws Exception {
        int rowCount = sheet.getRows();
        int colCount = sheet.getColumns();
       /* if(sheetCountNow == 0){//计算汇总表的第一个页签，并且获取每个省分的各设备数量以便汇总页签计算有效规模效益分
            Map<String,Double> map = null;//存放不同省分的同一个表格的值
//            ArrayList<ArrayList> arrayList = new ArrayList<ArrayList>();//同一个sheet可能存放多行list
            ArrayList<Map> list = null;//存放同一行的map
            boolean flag = false;//标识该行是否存的是设备数量
            for (int i = 0; i < rowCount; i++) {  //行 循环
                flag = false;
                for (int j = 0; j < colCount; j++) {//列 循环
                    WritableCell cell = sheet.getWritableCell(j, i);
                    String content = cell.getContents();
                    if(content != null && content.contains("数量") && !content.contains("故障")){
                        list = new ArrayList<Map>();
                        flag = true;
                    }
                    if (content != null && content.trim().startsWith("##SQL:")) {
                        if(flag){
                            map = new HashMap<String,Double>();
                        }
                        String res = "";
                        String depsForInQuery = "'";
                        for (OrgEntity gr : PList) {
                            depsForInQuery += gr.getOrgCode() + "','";
                            if(flag){//如果改行存的是设备数量，那么计算并且存放每个省分的该值
                                String depForInQuery = "'" + gr.getOrgCode() + "'";
                                res = fromTempletContentToSqlResForEvaluation(content, depForInQuery, reportDateStr, reportYear, isSearchHQData);
//                                map.put(gr.getOrgCode(),Double.parseDouble(res));
                                if(res.equals("")){
                                    map.put(gr.getOrgCode(),Double.parseDouble("0"));
                                }else{
                                    map.put(gr.getOrgCode(),Double.parseDouble(res));
                                }

                            }
                        }
                        if (depsForInQuery.endsWith(",'")) {
                            depsForInQuery = depsForInQuery.substring(0, depsForInQuery.length() - 2);
                        }
                        res = fromTempletContentToSqlResForEvaluation(content, depsForInQuery, reportDateStr, reportYear, isSearchHQData);

                        double resDouble = 0;
                        try {
                            resDouble = Double.parseDouble(res);
                        } catch (Exception ex) {

                        }
                        Number n = new Number(j, i, resDouble, ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));//加边框
                        sheet.addCell(n);//具体数字


                        int pvAndHqColSize = 0;//经计算后 大于 这个值的列 都清空
                        if (isSearchHQData == true) {
                            pvAndHqColSize = 1;
                        }

                    }
                    if(flag && map != null && map.size() != 0){
                        list.add(map);
                        map = new HashMap<String,Double>();
                    }
                }
                if(flag){
                    arrayList.add(list);
                }
            }
        }*/
        if (sheetCountNow == 0) {//计算汇总表的第一个页签，并且获取每个省分的各设备数量以便汇总页签计算有效规模效益分
            Map<String, Double> map = null;//存放不同省分的同一个表格的值
//            ArrayList<ArrayList> arrayList = new ArrayList<ArrayList>();//同一个sheet可能存放多行list
            ArrayList<Map> list = null;//存放同一行的map
            boolean flag = false;//标识该行是否存的是设备数量
            String content_OLT = null;
            for (int i = 0; i < rowCount; i++) {  //行 循环
                flag = false;
                for (int j = 0; j < colCount; j++) {//列 循环
                    WritableCell cell = sheet.getWritableCell(j, i);
                    String content = cell.getContents();
                    WritableCell cell_OLT = sheet.getWritableCell(2, i);//EPON-OLT和GPON-OLT的有效覆盖率计算
                    if (cell_OLT.getContents() != null && !cell_OLT.getContents().equals("")) {
                        content_OLT = cell_OLT.getContents();
                    }
                    if (sheet.getName().contains("PON-OLT") && content_OLT.contains("PON业务板故障")
                            && content != null && content.contains("数量") && !content.contains("故障")) {
                        list = new ArrayList<Map>();
                        flag = true;
                    } else if (!sheet.getName().contains("PON-OLT") && content != null && content.contains("数量") && !content.contains("故障")) {
                        list = new ArrayList<Map>();
                        flag = true;
                    }
                    if (content != null && content.trim().startsWith("##SQL:")) {
                        if (flag) {
                            map = new HashMap<String, Double>();
                        }
                        String res = "";
                        String depsForInQuery = "'";
                        for (OrgEntity gr : PList) {
                            depsForInQuery += gr.getOrgCode() + "','";
                            if (flag) {//如果改行存的是设备数量，那么计算并且存放每个省分的该值
                                String depForInQuery = "'" + gr.getOrgCode() + "'";
                                res = fromTempletContentToSqlResForEvaluation(content, depForInQuery, reportDateStr, reportYear, isSearchHQData);
//                                map.put(gr.getOrgCode(),Double.parseDouble(res));
                                if (res.equals("")) {
                                    map.put(gr.getOrgCode(), Double.parseDouble("0"));
                                } else {
                                    map.put(gr.getOrgCode(), Double.parseDouble(res));
                                }

                            }
                        }
                        if (depsForInQuery.endsWith(",'")) {
                            depsForInQuery = depsForInQuery.substring(0, depsForInQuery.length() - 2);
                        }
                        res = fromTempletContentToSqlResForEvaluation(content, depsForInQuery, reportDateStr, reportYear, isSearchHQData);

                        double resDouble = 0;
                        try {
                            resDouble = Double.parseDouble(res);
                        } catch (Exception ex) {

                        }
                        Number n = new Number(j, i, resDouble, ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));//加边框
                        sheet.addCell(n);//具体数字


                        int pvAndHqColSize = 0;//经计算后 大于 这个值的列 都清空
                        if (isSearchHQData == true) {
                            pvAndHqColSize = 1;
                        }

                    }
                    if (flag && map != null && map.size() != 0) {
                        list.add(map);
                        map = new HashMap<String, Double>();
                    }
                }
                if (flag) {
                    arrayList.add(list);
                }
            }
        }else{
            for (int i = 0; i < rowCount; i++) {  //行 循环

                for (int j = 0; j < colCount; j++) {//列 循环
                    WritableCell cell = sheet.getWritableCell(j, i);
                    String content = cell.getContents();
                    if (content != null && content.trim().startsWith("##SQL:")) {
                        String res = "";
                        String depsForInQuery = "'";
                        for (OrgEntity gr : PList) {
                            depsForInQuery += gr.getOrgCode() + "','";
                        }
                        if (depsForInQuery.endsWith(",'")) {
                            depsForInQuery = depsForInQuery.substring(0, depsForInQuery.length() - 2);
                        }
                        res = fromTempletContentToSqlResForEvaluation(content, depsForInQuery, reportDateStr, reportYear, isSearchHQData);

                        double resDouble = 0;
                        try {
                            resDouble = Double.parseDouble(res);
                        } catch (Exception ex) {

                        }
                        Number n = new Number(j, i, resDouble, ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));//加边框
                        sheet.addCell(n);//具体数字


                        int pvAndHqColSize = 0;//经计算后 大于 这个值的列 都清空
                        if (isSearchHQData == true) {
                            pvAndHqColSize = 1;
                        }

                    }
                }
            }
        }

    }

    //  根据模版表格中特殊格式的sql 生成查询结果  后评价 总部汇总用到
    private String fromTempletContentToSql(String content, String hqDepsForInQuery, String date, String reportYear) {
        String sql = "";
        String res = "";
        try {
            if (content != null) {
                content = content.trim();
                //zxx 季度汇总和全年汇总做出判断，季度汇总中data有值而全年汇总data为空

                if (content.trim().startsWith("##SQL:")) {
                    sql = content.substring(6);
                    //全年汇总
                    if (("".equals(date) || date == null) && !("".equals(reportYear) || reportYear == null)) {
                        //sql=sql.replace("$$date", date);
                        sql = sql.replace("$$reportYear", reportYear);
                        sql = sql.replace("$$condition", " and txtvalue!=\"\" ");
                        //zxx start 处理excel文件中的语句格式
                        if (sql.indexOf("and p.reportdate='$$date'") != -1) {
                            sql = sql.replace("and p.reportdate='$$date'", "");
                        }
                        if (sql.indexOf("and p.reportdate ='$$date'") != -1) {
                            sql = sql.replace("and p.reportdate ='$$date'", "");
                        }
                        if (sql.indexOf("and p.reportdate = '$$date'") != -1) {
                            sql = sql.replace("and p.reportdate = '$$date'", "");
                        }
                        // zxx end

                        //logger.info("000"+sql);
                    }

                    //新增加的跨年汇总代码20140219
                    if (("".equals(date) || date == null) && ("".equals(reportYear) || reportYear == null)) {
                        //sql=sql.replace("$$date", date);
                        if (sql.indexOf("and p.reportYear='$$reportYear'") != -1) {
                            sql = sql.replace("and p.reportYear='$$reportYear'", "");
                        }
                        if (sql.indexOf("and p.reportYear ='$$reportYear'") != -1) {
                            sql = sql.replace("and p.reportYear ='$$reportYear'", "");
                        }
                        if (sql.indexOf("and p.reportYear = '$$reportYear'") != -1) {
                            sql = sql.replace("and p.reportYear = '$$reportYear'", "");
                        }
                        //sql=sql.replace("$$reportYear", reportYear);

                        //zxx start 处理excel文件中的语句格式
                        if (sql.indexOf("and p.reportdate='$$date'") != -1) {
                            sql = sql.replace("and p.reportdate='$$date'", "");
                        }
                        if (sql.indexOf("and p.reportdate ='$$date'") != -1) {
                            sql = sql.replace("and p.reportdate ='$$date'", "");
                        }
                        if (sql.indexOf("and p.reportdate = '$$date'") != -1) {
                            sql = sql.replace("and p.reportdate = '$$date'", "");
                        }

                        String reportDateStr = "";
                        Date newdate = new Date();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        String dateStrMon = df.format(newdate);
                        String monthStr = dateStrMon.substring(5, 7);
                        String yearStr = dateStrMon.substring(0, 4);
                        //String year=yearStr;


                        String lastYearStr = "";
                        String currentYearStr = "";

                        String bn = "";
                        //全年汇总的年份
                        String allReportYear = String.valueOf((Integer.parseInt(yearStr) - 1));
                        if (monthStr.startsWith("0")) {
                            monthStr = monthStr.replace("0", "").trim();
                        }
                        int monthInt = Integer.parseInt(monthStr);
                        //月份
                        if (monthInt <= 3) {
                            //reportDateStr = "第四季度";
                            //reportYear=String.valueOf((Integer.parseInt(yearStr)-1));

                            lastYearStr = " and ((p.reportyear='" + allReportYear + "' and p.reportdate in('第一季度','第二季度','第三季度','第四季度'))";
                            currentYearStr = ")";
                            //bn="上半年";
                        } else if (monthInt >= 4 && monthInt < 7) {
                            //reportDateStr = "第一季度";
                            lastYearStr = " and ((p.reportyear='" + allReportYear + "' and p.reportdate in('第二季度','第三季度','第四季度'))";
                            currentYearStr = " or (p.reportyear='" + yearStr + "' and p.reportdate in('第一季度'))" + ")";
                            //bn="上半年";

                        } else if (monthInt >= 7 && monthInt < 10) {
                            //reportDateStr = "第二季度";
                            lastYearStr = " and ((p.reportyear='" + allReportYear + "' and p.reportdate in('第三季度','第四季度'))";
                            currentYearStr = " or (p.reportyear='" + yearStr + "' and p.reportdate in('第一季度','第二季度'))" + ")";
                            //bn="下半年";

                        } else {
                            //reportDateStr = "第三季度";
                            lastYearStr = " and ((p.reportyear='" + allReportYear + "' and p.reportdate in('第四季度'))";
                            currentYearStr = " or (p.reportyear='" + yearStr + "' and p.reportdate in('第一季度','第二季度','第三季度'))" + ")";
                            //bn="下半年";
                        }
                        //sql=sql+lastYearStr+currentYearStr;

                        sql = sql.replace("$$condition", " and txtvalue!=\"\" " + lastYearStr + currentYearStr);

                        // zxx end

                        //logger.info("000"+sql);
                    }

                    //季度半年汇总
                    else {
                     if ("全年".equals(date)) {
                            sql = sql.replace("and p.reportdate='$$date'", "");
                        } else {
                            sql = sql.replace("$$date", date);
                        }

                        sql = sql.replace("$$reportYear", reportYear);
                        sql = sql.replace("$$condition", " and txtvalue!=\"\" ");
                        //  ===================先判断是否属于年度汇总 判断当前是否大于7月   大于且属于年度汇总则进行跨年汇总    jw 3.21
                        //分别对全年和上年进行不同汇总
                       /* int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
                        String lastYear = "";
                        String currentYear = "";

                        if ("全年".equals(date)) {
                            if (month < 7) {
                                sql = sql.replace("and p.reportdate='$$date'", "");
                                sql = sql.replace("$$reportYear", reportYear);
                                sql = sql.replace("$$condition", " and txtvalue!=\"\" ");

                            } else {
                                //int LYear=Integer.parseInt(reportYear)-1;
                                String LYear = String.valueOf(Integer.parseInt(reportYear) - 1);
                                lastYear = " and ((p.reportyear='" + LYear + "' and p.reportdate ='下半年')";
                                currentYear = " or (p.reportyear='" + reportYear + "' and p.reportdate ='上半年')" + ")";

                                // sql = sql.replace("$$condition", " and txtvalue!=\"\" " + lastYear + currentYear);

                                //  "and p.reportYear ='$$reportYear'"     sql = sql.replace("and p.reportdate = '$$date'", "");
                                sql = sql.replace("and p.reportYear ='$$reportYear'", lastYear + currentYear);
                                sql = sql.replace("and p.reportdate = '$$date'", "");
                                sql = sql.replace("$$condition", " and txtvalue!=\"\" ");

                            }
                        } else if ("上年".equals(date)) {
                            if (month < 7) {
                                int year2 = Calendar.getInstance().get(Calendar.YEAR) - 2;
                                int year3 = Calendar.getInstance().get(Calendar.YEAR) - 1;
                                String aaa = year2 + "";
                                String bbb = year3 + "";
                                // String LYear=String.valueOf(Integer.parseInt(reportYear)-1);
                                lastYear = " and ((p.reportyear='" + aaa + "' and p.reportdate ='下半年')";
                                currentYear = " or (p.reportyear='" + bbb + "' and p.reportdate ='上半年')" + ")";
                                sql = sql.replace("and p.reportYear ='$$reportYear'", lastYear + currentYear);
                                sql = sql.replace("and p.reportdate = '$$date'", "");
                                sql = sql.replace("$$condition", " and txtvalue!=\"\" ");


                            } else {
                                int year4 = Calendar.getInstance().get(Calendar.YEAR) - 1;
                                String ccc = year4 + "";
                                sql = sql.replace("and p.reportdate='$$date'", "");
                                sql = sql.replace("$$reportYear", ccc);
                                sql = sql.replace("$$condition", " and txtvalue!=\"\" ");


                            }
                        } else {
                            sql = sql.replace("$$date", date);
                            sql = sql.replace("$$reportYear", reportYear);
                            sql = sql.replace("$$condition", " and txtvalue!=\"\" ");

                        }*/
                        //=====================================================
                    }
                    return sql;
                }
            }
        } catch (Exception ex) {
            logger.info(sql);
            ex.printStackTrace();
        }
        return res;
    }

    private int getSpaceList(WritableSheet sheet, String reportDateStr, String dealType, String reportYear, List<NameValue> spaceList, Boolean flag) {
        int rowCount = sheet.getRows();
        int colCount = sheet.getColumns();
        int spaceRow = 0;//填充数据到的具体的行数

        for (int i = 0; i < colCount; i++) {  //列 循环
            if (spaceList != null && spaceList.size() > 0) {
                break;
            }

            //	spaceList = new ArrayList<NameValue>();
            for (int j = 0; j < rowCount; j++) {//行 循环

                WritableCell cell = sheet.getWritableCell(i, j);
                String content = cell.getContents();

                if (content != null && content.trim().startsWith("##SQL:")) {

                    String sql = fromTempletContentToSql(content, "", reportDateStr, reportYear);
                    if (flag) {
                        sql = sql.replaceAll("and p.WORK_ORDER_STATUS=\"已审核\"", "");
                        sql = sql.replaceAll("and  p.WORK_ORDER_STATUS=\"已审核\"", "");
                    }
                    NameValue nv = new NameValue();
                    nv.setName(i + "");
                    nv.setValue(j + "");
                    nv.setRemark(sql);

                    spaceList.add(nv);
                } else if (content != null && content.trim().startsWith("##PV")) {
                    NameValue nv = new NameValue();
                    nv.setName(i + "");
                    nv.setValue(j + "");
                    nv.setRemark("PV");
                    spaceList.add(nv);
                }
            }
            spaceRow++;
        }
        spaceRow--;
        return spaceRow;
    }

    private List<List<String>> getDataList(WritableSheet sheet, List<OrgEntity> PList, List<NameValue> spaceList, int spaceRow, String statType,boolean flag) {
        List<List<String>> dataList = new ArrayList<List<String>>();
        String res = "";
        WritableCell n = null;
        //zxx start
        String sql1 = "";
        String sql2 = "";
        int count = 0;
        double ss = 0.0;
        //zxx end

        for (int i = 0; i < PList.size(); i++) {//获得省份 zxx 应该获取每个省份的总的季度值或总的半年值
            count = 1;//只拼一次
            sql1 = "select distinct p.reportdate from t_eem_excel_page  p where 1=1  ";
            ss = 0.0;
            List<String> tmpList = new ArrayList<String>();
            for (int j = 0; j < spaceList.size(); j++) {
                NameValue value = spaceList.get(j);
                int va = Integer.parseInt(value.getValue());
                int na = Integer.parseInt(value.getName());
                if (value.getRemark() != null && value.getRemark().startsWith("PV")) {
                    tmpList.add(PList.get(i).getOrgName());
                    if (STAT_TYPE_COL.equals(statType)) {
                        n = new Label(i + spaceRow, va, PList.get(i).getOrgName(), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                    } else {
                        n = new Label(va, i + spaceRow, PList.get(i).getOrgName(), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                    }
                } else {
                    String sql = value.getRemark().replace("$$depart", PList.get(i).getOrgCode());
                    if (flag) {
                        sql = sql.replaceAll("and p.WORK_ORDER_STATUS=\"已审核\"", "");
                        sql = sql.replaceAll("and  p.WORK_ORDER_STATUS=\"已审核\"", "");
                    }
                    Object obj = null;

                    if (sql.indexOf("reportdate") == -1) {
                        //zxx start
                        if (count == 1) {
                            Object obj1 = null;
                            String a[] = sql.split("and");

                            for (int z = 0; z < a.length; z++) {
                                if (a[z].indexOf("tpInputID") != -1) {
                                    sql1 = sql1  + " and "+ a[z];
                                }
                                if (a[z].indexOf("reportOrgCode") != -1) {
                                    sql1 = sql1  + " and "+ a[z];
                                }
                                if (a[z].indexOf("Year") != -1) {
                                    sql1 = sql1  + " and "+ a[z];
                                    int first = 0;
                                    if (sql1.indexOf("Year='") != -1) {
                                        first = sql1.indexOf("Year='");
                                        sql1 = sql1.substring(0, first + 11);
                                    }
                                    if (sql1.indexOf("Year ='") != -1) {
                                        first = sql1.indexOf("Year ='");
                                        sql1 = sql1.substring(0, first + 12);
                                    }
                                    if (sql1.indexOf("Year = '") != -1) {
                                        first = sql1.indexOf("Year = '");
                                        sql1 = sql1.substring(0, first + 13);
                                    }
                                    break;
                                }
                            }

                            try {
                                obj1 = baseDAO.findNativeSQL(sql1, null);
                            } catch (Exception ex) {
                                logger.info("报错SQL：" + sql1);
                            }
                            if (obj1 != null) {
                                List list1 = (List) obj1;
                                if (list1.size() > 0) {
                                    ss = list1.size();
                                }
                            }
                            count = 0;
                        }
                        //zxx end
                    }


                    try {
                        sql = sql.replaceAll("strtonumber", "");
                        System.out.println(sql);
                        obj = baseDAO.findNativeSQL(sql, null);
                    } catch (Exception ex) {
                        logger.info("报错SQL：" + sql);
                    }
                    if (obj != null) {
                        List list = (List) obj;
                        if (list != null && list.size() > 0 && list.get(0) != null) {
                            Map resMap = (Map) list.get(0);
                            Object ob = resMap.get("value");
                            if (ob != null) {

                                //新增加的处理近四个季度汇总
                                sql2 = "select distinct p.reportdate from t_eem_excel_page  p where ";
                                Object obj2 = null;
                                List list2 = null;
                                if (sql.indexOf("reportdate") != -1 && sql.indexOf("第一") != -1 && sql.indexOf("第二") != -1) {
                                    //拼sql
                                    String zz[] = sql.split("and");
                                    for (int z = 0; z < zz.length; z++) {
                                        if (zz[z].indexOf("tpInputID") != -1) {
                                            if (sql2.indexOf("tpInputID") == -1)
                                                sql2 = sql2 + zz[z] + " and ";
                                        }
                                        if (zz[z].indexOf("reportOrgCode") != -1) {
                                            if (sql2.indexOf("reportOrgCode") == -1) {
                                                int begianIndex = zz[z].indexOf("(") - 1;
                                                int lastIndex = zz[z].indexOf(")") + 1;
                                                String dep = zz[z].substring(begianIndex, lastIndex);
                                                dep = "reportOrgCode in" + dep;
                                                sql2 = sql2 + dep;
                                            }

                                        }
                                    }

                                    String reportDateStr = "";
                                    Date newdate = new Date();
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                    String dateStrMon = df.format(newdate);
                                    String monthStr = dateStrMon.substring(5, 7);
                                    String yearStr = dateStrMon.substring(0, 4);
                                    //String year=yearStr;


                                    String lastYearStr = "";
                                    String currentYearStr = "";

                                    String bn = "";
                                    //全年汇总的年份
                                    String allReportYear = String.valueOf((Integer.parseInt(yearStr) - 1));
                                    if (monthStr.startsWith("0")) {
                                        monthStr = monthStr.replace("0", "").trim();
                                    }
                                    int monthInt = Integer.parseInt(monthStr);
                                    //月份
                                    if (monthInt <= 3) {
                                        //reportDateStr = "第四季度";
                                        //reportYear=String.valueOf((Integer.parseInt(yearStr)-1));

                                        lastYearStr = " and ((p.reportyear='" + allReportYear + "' and p.reportdate in('第一季度','第二季度','第三季度','第四季度'))";
                                        currentYearStr = ")";
                                        //bn="上半年";
                                    } else if (monthInt >= 4 && monthInt < 7) {
                                        //reportDateStr = "第一季度";
                                        lastYearStr = " and ((p.reportyear='" + allReportYear + "' and p.reportdate in('第二季度','第三季度','第四季度'))";
                                        currentYearStr = " or (p.reportyear='" + yearStr + "' and p.reportdate in('第一季度'))" + ")";
                                        //bn="上半年";

                                    } else if (monthInt >= 7 && monthInt < 10) {
                                        //reportDateStr = "第二季度";
                                        lastYearStr = " and ((p.reportyear='" + allReportYear + "' and p.reportdate in('第三季度','第四季度'))";
                                        currentYearStr = " or (p.reportyear='" + yearStr + "' and p.reportdate in('第一季度','第二季度'))" + ")";
                                        //bn="下半年";

                                    } else {
                                        //reportDateStr = "第三季度";
                                        lastYearStr = " and ((p.reportyear='" + allReportYear + "' and p.reportdate in('第四季度'))";
                                        currentYearStr = " or (p.reportyear='" + yearStr + "' and p.reportdate in('第一季度','第二季度','第三季度'))" + ")";
                                        //bn="下半年";
                                    }
                                    //sql=sql+lastYearStr+currentYearStr;

                                    sql2 = sql2 + lastYearStr + currentYearStr;
                                    if (sql2.indexOf("value") != -1) {
                                        sql2 = sql2.replace("value", "");
                                        if (sql2.indexOf("from dual") != -1) {
                                            sql2 = sql2.replace("from dual", "");
                                        }
                                    }

                                    try {
//                                        obj2 = hibDao.queryBySql(sql2);
                                        sql2 = sql2.replaceAll("strtonumber", "");
                                        obj2 = baseDAO.findNativeSQL(sql2, null);
                                    } catch (Exception ex) {
                                        logger.info("报错SQL：" + sql2);
                                    }
                                    if (obj2 != null) {
                                        list2 = (List) obj2;
                                        if (list2.size() > 0) {
                                            ss = list2.size();
                                        }
                                        //logger.info("ss="+ss);
                                    }
                                    res = ob.toString();
                                    double ss1;

                                    ss1 = new Double(ob.toString()).doubleValue();
                                    if (ss != 0.0) {

                                        res = ss1 / ss + "";
                                    } else {
                                        res = "0";
                                    }

                                }


                                //zxx start

                                //非全年汇总
                                else if (sql.indexOf("reportdate") != -1) {
                                    //else if(sql.indexOf("reportdate")!=-1&&sql.indexOf("第一")==-1){
                                    res = ob.toString();
                                } else {
                                    double ss1;

                                    ss1 = new Double(ob.toString()).doubleValue();
                                    //ss=1.0;

                                    if (ss != 0.0) {
                                        if (ss1 == -111) {
                                            res = ss1 / 1 + "";
                                        }else
                                        res = ss1 / ss + "";
                                    } else {
                                        res = "0";
                                    }
                                }
                                //}
                                //zxx end


                                //res = ob.toString();

                                try {//处理无限循环小数 如果出现问题立即捕获 不影响数据显示
                                    if (res.indexOf(".") != -1) {
                                        if (res.length() - res.indexOf(".") > 3) {
                                            res = res.substring(0, res.indexOf(".") + 3);
                                        }
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                res = "0";
                            }
                        } else {
                            res = "0";
                        }
                    } else {
                        res = "0";
                    }
                    double resDouble = 0;
                    try {
                        resDouble = Double.parseDouble(res);
                    } catch (Exception ex) {

                    }
                    tmpList.add(resDouble + "");

                    if (STAT_TYPE_COL.equals(statType)) {
                        n = new Number(i + spaceRow, va, resDouble, ExportUtil.getBackfround(jxl.format.Colour.WHITE, 8));
                    } else {
                        n = new Number(va, i + spaceRow, resDouble, ExportUtil.getBackfround(jxl.format.Colour.WHITE, 8));
                    }

                }

                try {
                    sheet.addCell(n);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            dataList.add(tmpList);
        }
        return dataList;
    }

    private double[][] getResArray(List<List<String>> dataList) {
        double[][] resArray = new double[dataList.size()][dataList.get(0).size()];
        for (int i = 0; i < dataList.size(); i++) {
            List<String> tmp = dataList.get(i);//每个省的数据
            for (int j = 1; j < tmp.size(); j++) {//从1开始因被第一个是省份名字的单元格

                double aa = 0;
                try {
                    String a = tmp.get(j).toString();
                    aa = Double.parseDouble(a);
                } catch (Exception ex) {

                }
                resArray[i][j] = aa;

            }
        }
        return resArray;
    }

    // 新增集采得分
    //新增2017年版后评价得分   2017年版有效规模效益分
    private void getArraysNew2(List<OrgEntity> PList, WritableWorkbook wwb,
                               List<List<String>> dataList,
                               double[] sumArray, double[] percentArray, double[] mark, double[] percentMark,
                               double[] realArray, double[] finalArray, double coefficient, double[][] resArray,
                               double[][] faultRateArray, double[][] serveArray, double[] evaluateArray,
                               String equipType, String formId, double vendorfaultRateArray[], double[] hqEvaluateArray,
                               double[] hqGroupEvaluateArray, double[] groupEvaluateArray,double[] hqGroupEvaluateArray_2017,double[] hqGroupEvaluateArray_2017xy) {

        java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");


        //往第三个sheet页添加信息
        WritableSheet ws = wwb.getSheet(2);
        for (int k = 0; k < PList.size(); k++) {
            Label label = new Label(k + 2, 0, PList.get(k).getOrgName(), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 8));
            try {
                ws.addCell(label);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /*cgliu //新增加文件处理
        java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00");
		File file = new File("G:/test.xls");
		OutputStream os =null;
		WritableWorkbook wwb = null;
		WritableSheet ws=null;
		try{
			file.createNewFile();
			//创建输出流
			//outputstream是所有字节输出流的根不能直接实例
			//可由其他子类输出流生成
			os = new FileOutputStream(file);
			//利用workbook工厂类创建可写入workbook
			//注意：writeableWorkbook的构造方法是protected，必须由工厂类来创建
			//这里我们直接将workbook直接写入输出流里
			wwb = Workbook.createWorkbook(os);
			//创建其中的表单对象，其中的参数代表sheet的名称和位置索引类似集合的概念
			 ws = wwb.createSheet(formId, 0);
			//接着要向里面写入数据
			//很形象的说 excel中的每一行都是由一个一个的Label组成的
			//我们就可以创建Label对象
			//解释一下其中的参数就是行 列 值
		}
		catch(Exception e){
			e.printStackTrace();
		}
		//end
*/
        //double hqEvaluateArray[]=new double[dataList.get(0).size()];
        double[][] pvEvaluateArray = new double[dataList.size()][dataList.get(0).size()]; //各个省份集采服务后评价得分
        int verNum = (int) ((dataList.get(0).size() - 1) / coefficient); //厂家数量；
        double[] verNumArray = new double[dataList.get(0).size()]; //保存各个厂家省份覆盖数量
        double[] sumGroupEvaluate = new double[dataList.get(0).size()];//∑ai
        double[] minGroupEvaluate = new double[dataList.get(0).size()];//min（ai）非0
        //保存每个厂商的光缆总长度
        double[] sumArrays = new double[dataList.get(0).size()];
        //有效覆盖率的分母
        double[] sumArrays2 = new double[dataList.size()];

        for (int i = 1; i < dataList.get(0).size(); i++) {//行循环

            List<Double> tempMinGroupEvaluate = new ArrayList<Double>();//用于计算最小分省得分
            int t = 0;//保存有几个省有得分
            for (int j = 0; j < dataList.size(); j++) {//省份循环
                double each = resArray[j][i];
                if (i % coefficient == 0) {
                    if (each > sumArray[i]) {
                        sumArray[i] = each;
                    }
                } else {
                    if (each > 0) {
                        t++;
                    }
                    sumArray[i] = sumArray[i] + each;
                }
                sumArrays[i] = sumArray[i];
                ///////////////////////
                if ((i + 2) % coefficient == 0) {
                    if (Double.valueOf(dataList.get(j).get(i)) > 0) {  //判断省份是否使用该设备，根据设备数量
                        verNumArray[i]++;

                    }
                    logger.info("########" + resArray[j][i + 1]);
                    logger.info("########" + resArray[j][i]);
                    double percentValue = resArray[j][i + 1] / resArray[j][i];//各个省份每个厂家的故障率
                    logger.info("~~~~~~~~~~~~~~~~````````" + percentValue);
                    EvaluationReference evaluationReference = findEvaluationReferenceById(Long.parseLong(formId));
                    logger.info("MaxFaultrate=" + evaluationReference.getMaxFaultrate());
                    logger.info("MinFaultrate=" + evaluationReference.getMinFaultrate());
                    if (!Double.isNaN(percentValue)) {
                        if (percentValue >= evaluationReference.getMaxFaultrate()) {
                            faultRateArray[j][i] = 0;
                        } else if (percentValue <= evaluationReference.getMinFaultrate()) {
                            faultRateArray[j][i] = 100;
                        } else {
                            faultRateArray[j][i] = 100 - 100 * (
                                    percentValue - evaluationReference.getMinFaultrate()) /
                                    (evaluationReference.getMaxFaultrate() - evaluationReference.getMinFaultrate());

                        }
                        logger.info("@@@@@@@@@@@@@" + faultRateArray[j][i]);
                        serveArray[j][i] = resArray[j][i + 2]; // 每个省份的服务得分
                        if (equipType.contains("乙")) {
                            pvEvaluateArray[j][i] = faultRateArray[j][i] * 0.8 + serveArray[j][i] * 10 * 0.2;

//							add by btliu start
                            if (pvEvaluateArray[j][i] > 1) {
                                //if(pvEvaluateArray[j][i] > 0){
                                tempMinGroupEvaluate.add(Double.valueOf(pvEvaluateArray[j][i]));
                                sumGroupEvaluate[i] += pvEvaluateArray[j][i];
                            }
                            //btliu end

                            //将数据放到sheet页的指定单元格中
                            Label label = new Label(j + 2, i, df.format(pvEvaluateArray[j][i]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                            try {
                                ws.addCell(label);
                                ws.mergeCells(j + 2, i, j + 2, i + 2);
                            } catch (RowsExceededException e) {
                                e.printStackTrace();
                            } catch (WriteException e) {
                                e.printStackTrace();
                            }

                        }
                        if (equipType.contains("丙")) {//哈哈
                            pvEvaluateArray[j][i] = faultRateArray[j][i] * 0.95 + serveArray[j][i] * 10 * 0.05;

                            if (pvEvaluateArray[j][i] >=0) {
                                tempMinGroupEvaluate.add(Double.valueOf(pvEvaluateArray[j][i]));
                                sumGroupEvaluate[i] += pvEvaluateArray[j][i];
                            }
                            //btliu end

                            //将数据放到sheet页的指定单元格中
                            Label label = new Label(j + 2, i, df.format(pvEvaluateArray[j][i]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                            try {
                                ws.addCell(label);
                                ws.mergeCells(j + 2, i, j + 2, i + 2);
                            } catch (RowsExceededException e) {
                                e.printStackTrace();
                            } catch (WriteException e) {
                                e.printStackTrace();
                            }


                        }
                    }
                    hqEvaluateArray[i] += pvEvaluateArray[j][i];
                    vendorfaultRateArray[i] += faultRateArray[j][i];
                }
                /////////////////////////
            }




            if (tempMinGroupEvaluate.size() > 1) {
                for (int j = 0; j < tempMinGroupEvaluate.size() - 1; j++) {
                    for (int k = 0; k < tempMinGroupEvaluate.size() - 1 - j; k++) {
                        //最小值不能小于1
                        if (tempMinGroupEvaluate.get(k) > tempMinGroupEvaluate.get(k + 1)) {
                            double temp = tempMinGroupEvaluate.get(k);
                            tempMinGroupEvaluate.set(k, tempMinGroupEvaluate.get(k + 1));
                            tempMinGroupEvaluate.set(k + 1, temp);
                        }
                    }

                }
                minGroupEvaluate[i] = tempMinGroupEvaluate.get(0);
            } else if (tempMinGroupEvaluate.size() > 0) {
                minGroupEvaluate[i] = tempMinGroupEvaluate.get(0);
            }
            //btliu end
            if (t > 20) {
                mark[i] = 100;
            } else {
                mark[i] = t * 5;
            }
        }


        for (int j = 0; j < dataList.size(); j++) {//省份循环

            sumArrays2[j] = 0;
            int t = 0;//保存有几个省有得分
            for (int i = 1; i < dataList.get(0).size(); i++) {//行循环
                    if((i + 2) % coefficient==0){
                        sumArrays2[j]+=Double.valueOf(dataList.get(j).get(i));
                }
            }
        }





        for (int i = 1; i < dataList.get(0).size(); i++) {//行循环
            int t = 0;//保存有几个省有得分
            for (int j = 0; j < dataList.size(); j++) {//省份循环
                if ((i + 2) % coefficient == 0) {
                    if (Double.valueOf(dataList.get(j).get(i)) > 0&&Double.valueOf(dataList.get(j).get(i))/sumArrays2[j]>0.05) {  //判断省份是否使用该设备，根据设备数量
                        hqGroupEvaluateArray_2017xy[i]++;
                    }
                }
            }
        }
        //输出
        /////////////////////////////// add
        for (int i = 0; i < hqEvaluateArray.length; i++) {//行循环
            if (verNumArray[i] == 0) {
                hqEvaluateArray[i] = 0;
            } else {
                hqEvaluateArray[i] = hqEvaluateArray[i] / verNumArray[i];//服务集采平均值
            }

        }
        // 新故障率
        for (int i = 0; i < vendorfaultRateArray.length; i++) {
            if (verNumArray[i] == 0) {
                vendorfaultRateArray[i] = 0;
            } else {
                vendorfaultRateArray[i] = vendorfaultRateArray[i] / verNumArray[i];  //新故障率=各省故障率和/省份个数
            }

        }
        //
        double maxNum = 0;
        double[] markValue = new double[dataList.get(0).size()];
        //规模效益扣分项得分
        for (int i = 0; i < verNumArray.length; i++) {
            if (verNumArray[i] > maxNum) {
                maxNum = verNumArray[i];  //取省份覆盖最多的数量
            }
        }

        if (verNum > 4 && verNum < 10) {  //供应商数量4至10家的情况
            for (int i = 0; i < verNumArray.length; i++) {
                if (verNumArray[i] > 0) {
                    if (verNumArray[i] == maxNum) {
                        markValue[i] = 0;                            //供应商设备应用最多省份不扣分
                    } else {
                        markValue[i] = (1 - verNumArray[i] / maxNum) * 20;   //(供应商提供省份数量/供应商设备应用最多省份数量)*20
                    }
                } else {
                    markValue[i] = 0;
                }
            }

        }
        if (verNum > 10) {  //供应商数量大于10家的情况
            for (int i = 0; i < verNumArray.length; i++) {
                if (verNumArray[i] > 0) {
                    if (verNumArray[i] > 15) {
                        markValue[i] = 0;
                    } else {
                        markValue[i] = (15 - verNumArray[i]) * 1.5;
                        if (markValue[i] > 21) {
                            markValue[i] = 21;
                        }
                    }
                } else {
                    markValue[i] = 0;
                }

            }
        }
        //add by btliu start
        for (int i = 1; i < groupEvaluateArray.length; i++) {//2014集采后评价得分
            //规模效益分
            int count = dataList.get(0).size();
            double scaleMark = 0;
            if (count <= 3) {
                scaleMark = 0;
            } else if (count >= 4 && count <= 10) {
                scaleMark = (verNumArray[i] / maxNum) * 30;
            } else if (count >= 11) {
                scaleMark = verNumArray[i] * 2;
            }
            if (scaleMark > 30) {
                scaleMark = 30;//30分封顶
            }
            if (verNumArray[i] == 0) {
            } else {
                hqGroupEvaluateArray[i] = (sumGroupEvaluate[i] +
                        (PList.size() - verNumArray[i]) * minGroupEvaluate[i]) / PList.size() * 0.7;
                groupEvaluateArray[i] = hqGroupEvaluateArray[i] + scaleMark;
            }
        }
        //btliu end
        /////////////////////////////////add
        for (int i = 0; i < sumArray.length; i++) {
            if ((i + 2) % coefficient == 0) {
                if (sumArray[i] > 0) {
                    percentArray[i] = sumArray[i + 1] / sumArray[i];  //故障率
                } else {
                    percentArray[i] = 999;
                }
            }
        }
        double max = 0;
        double min = 9999999;
        for (int i = 0; i < percentArray.length; i++) {
            if ((i + 2) % coefficient == 0) {
                if (percentArray[i] > max && percentArray[i] <= 100) {
                    max = percentArray[i];
                }
                if (percentArray[i] < min && sumArray[i] > 0) {
                    min = percentArray[i];
                }
            }
        }


        for (int i = 0; i < percentArray.length; i++) {
            if ((i + 2) % coefficient == 0) {
                if (percentArray[i] <= 100) {
                    double tmp = 0;
                    if (percentArray[i] > 1) {
                        tmp = percentArray[i];
                    } else {
                        tmp = percentArray[i];
                    }
                    percentMark[i] = 100 * (max - tmp) / (max - min);//故障率得分
                    if (i > 35 && i < 45) {
                        String a = "test";
                    }
                    if (equipType.contains("乙")) {
                        realArray[i] = (percentMark[i] * 0.8 + sumArray[i + 2] * 10 * 0.2);  //建议暂缓下期集采
                        finalArray[i] = (percentMark[i] * 0.8 + sumArray[i + 2] * 10 * 0.2) * 0.8 + mark[i] * 0.2; //总部项目集采得分
                    } else if (equipType.contains("丙")) {
                        realArray[i] = (percentMark[i] * 0.95 + sumArray[i + 2] * 10 * 0.05);
                        finalArray[i] = (percentMark[i] * 0.95 + sumArray[i + 2] * 10 * 0.05) * 0.8 + mark[i] * 0.2;
                    }

                    evaluateArray[i] = hqEvaluateArray[i] - markValue[i];//老系统的算法
               //     evaluateArray[i] = hqEvaluateArray[i] * 0.8 + mark[i];//新系统  jp
                    if (evaluateArray[i] < 0) {
                        evaluateArray[i] = 0;
                    }
                }
            }
        }

        //2017年后评价得分
        for (int i = 1; i < hqGroupEvaluateArray_2017.length; i++) {//2017集采后评价得分
            if((i+2)%coefficient==0){

            if(sumGroupEvaluate[i] == 0){
                hqGroupEvaluateArray_2017[i] = 0;
                continue;
            }
            double ss = hqGroupEvaluateArray_2017xy[i];
            if(ss>20){
                ss=20;
            }

            hqGroupEvaluateArray_2017[i] =(sumGroupEvaluate[i] + (31 - verNumArray[i])*minGroupEvaluate[i]) /31 * 0.8 + ss;
        }
        }
        //2017年有效规模效益分
        for (int i = 1; i < hqGroupEvaluateArray_2017xy.length; i++) {//2017集采后评价得分
//            hqGroupEvaluateArray_2017xy[i] = verNumArray[i];
            if(hqGroupEvaluateArray_2017xy[i] > 20){
                hqGroupEvaluateArray_2017xy[i] = 20;
            }
        }
    }

    // 新增集采得分
    private void getArraysNew(List<List<String>> dataList, double[] sumArray, double[] percentArray, double[] mark, double[] percentMark, double[] realArray, double[] finalArray, double coefficient, double[][] resArray, double[][] faultRateArray, double[][] serveArray, double[] evaluateArray, String equipType, String formId, double vendorfaultRateArray[], double[] hqEvaluateArray) {
        double[][] pvEvaluateArray = new double[dataList.size()][dataList.get(0).size()]; //各个省份集采服务后评价得分
        int verNum = (int) ((dataList.get(0).size() - 1) / coefficient); //厂家数量；
        double[] verNumArray = new double[dataList.get(0).size()]; //保存各个厂家省份覆盖数量

        for (int i = 1; i < dataList.get(0).size(); i++) {//行循环
            int t = 0;//保存有几个省有得分
            for (int j = 0; j < dataList.size(); j++) {//省份循环
                double each = resArray[j][i];
                if (i % coefficient == 0) {
                    if (each > sumArray[i]) {
                        sumArray[i] = each;
                    }
                } else {
                    if (each > 0) {
                        t++;
                    }
                    sumArray[i] = sumArray[i] + each;
                }
                ///////////////////////
                if ((i + 2) % coefficient == 0) {
                    if (Double.valueOf(dataList.get(j).get(i)) > 0) {  //判断省份是否使用该设备，根据设备数量
                        verNumArray[i]++;
                    }
                    logger.info("########" + resArray[j][i + 1]);
                    logger.info("########" + resArray[j][i]);
                    double percentValue = resArray[j][i + 1] / resArray[j][i];//各个省份每个厂家的故障率
                    logger.info("~~~~~~~~~~~~~~~~````````" + percentValue);
                    EvaluationReference evaluationReference = findEvaluationReferenceById(Long.parseLong(formId));
                    logger.info("MaxFaultrate=" + evaluationReference.getMaxFaultrate());
                    logger.info("MinFaultrate=" + evaluationReference.getMinFaultrate());
                    if (!Double.isNaN(percentValue)) {
                        if (percentValue >= evaluationReference.getMaxFaultrate()) {
                            faultRateArray[j][i] = 0;
                        } else if (percentValue <= evaluationReference.getMinFaultrate()) {
                            faultRateArray[j][i] = 100;
                        } else {
                            faultRateArray[j][i] = 100 - 100 * (percentValue - evaluationReference.getMinFaultrate()) / (evaluationReference.getMaxFaultrate() - evaluationReference.getMinFaultrate());

                        }
                        logger.info("@@@@@@@@@@@@@" + faultRateArray[j][i]);
                        serveArray[j][i] = resArray[j][i + 2]; // 每个省份的服务得分
                        if (equipType.contains("乙")) {
                            pvEvaluateArray[j][i] = faultRateArray[j][i] * 0.8 + serveArray[j][i] * 0.2;
                        }
                        if (equipType.contains("丙")) {//哈哈
                            pvEvaluateArray[j][i] = faultRateArray[j][i] * 0.95 + serveArray[j][i] * 0.05;
                        }
                    }
                    hqEvaluateArray[i] += pvEvaluateArray[j][i];
                    vendorfaultRateArray[i] += faultRateArray[j][i];
                }
            }
            if (t > 20) {
                mark[i] = 100;
            } else {
                mark[i] = t * 5;
            }
        }
        for (int i = 0; i < hqEvaluateArray.length; i++) {//行循环
            if (verNumArray[i] == 0) {
                hqEvaluateArray[i] = 0;
            } else {
                hqEvaluateArray[i] = hqEvaluateArray[i] / verNumArray[i];//服务集采平均值
            }

        }
        // 新故障率
        for (int i = 0; i < vendorfaultRateArray.length; i++) {
            if (verNumArray[i] == 0) {
                vendorfaultRateArray[i] = 0;
            } else {
                vendorfaultRateArray[i] = vendorfaultRateArray[i] / verNumArray[i];  //新故障率=各省故障率和/省份个数
            }

        }
        //
        double maxNum = 0;
        double[] markValue = new double[dataList.get(0).size()];
        //规模效益扣分项得分
        for (int i = 0; i < verNumArray.length; i++) {
            if (verNumArray[i] > maxNum) {
                maxNum = verNumArray[i];  //取省份覆盖最多的数量
            }
        }

        if (verNum > 4 && verNum < 10) {  //供应商数量4至10家的情况
            for (int i = 0; i < verNumArray.length; i++) {
                if (verNumArray[i] > 0) {
                    if (verNumArray[i] == maxNum) {
                        markValue[i] = 0;                            //供应商设备应用最多省份不扣分
                    } else {
                        markValue[i] = (1 - verNumArray[i] / maxNum) * 20;   //(供应商提供省份数量/供应商设备应用最多省份数量)*20
                    }
                } else {
                    markValue[i] = 0;
                }
            }

        }
        if (verNum > 10) {  //供应商数量大于10家的情况
            for (int i = 0; i < verNumArray.length; i++) {
                if (verNumArray[i] > 0) {
                    if (verNumArray[i] > 15) {
                        markValue[i] = 0;
                    } else {
                        markValue[i] = (15 - verNumArray[i]) * 1.5;
                        if (markValue[i] > 21) {
                            markValue[i] = 21;
                        }
                    }
                } else {
                    markValue[i] = 0;
                }

            }
        }


        /////////////////////////////////add
        for (int i = 0; i < sumArray.length; i++) {
            if ((i + 2) % coefficient == 0) {
                if (sumArray[i] > 0) {
                    percentArray[i] = sumArray[i + 1] / sumArray[i];  //故障率
                } else {
                    percentArray[i] = 999;
                }
            }
        }
        double max = 0;
        double min = 9999999;
        for (int i = 0; i < percentArray.length; i++) {
            if ((i + 2) % coefficient == 0) {
                if (percentArray[i] > max && percentArray[i] <= 100) {
                    max = percentArray[i];
                }
                if (percentArray[i] < min && sumArray[i] > 0) {
                    min = percentArray[i];
                }
            }
        }


        for (int i = 0; i < percentArray.length; i++) {
            if ((i + 2) % coefficient == 0) {
                if (percentArray[i] <= 100) {
                    double tmp = 0;
                    if (percentArray[i] > 1) {
                        tmp = percentArray[i];
                    } else {
                        tmp = percentArray[i];
                    }
                    percentMark[i] = 100 * (max - tmp) / (max - min);//故障率得分
                    if (i > 35 && i < 45) {
                        String a = "test";
                    }
                    if (equipType.contains("乙")) {
                        realArray[i] = (percentMark[i] * 0.8 + sumArray[i + 2] * 10 * 0.2);  //建议暂缓下期集采
                        finalArray[i] = (percentMark[i] * 0.8 + sumArray[i + 2] * 10 * 0.2) * 0.8 + mark[i] * 0.2; //总部项目集采得分
                    } else if (equipType.contains("丙")) {
                        realArray[i] = (percentMark[i] * 0.95 + sumArray[i + 2] * 10 * 0.05);
                        finalArray[i] = (percentMark[i] * 0.95 + sumArray[i + 2] * 10 * 0.05) * 0.8 + mark[i] * 0.2;
                    }

                    evaluateArray[i] = hqEvaluateArray[i] - markValue[i];
                    if (evaluateArray[i] < 0) {
                        evaluateArray[i] = 0;
                    }
                }
            }
        }
    }

    private List<List<String>> getDataListGUANGLAN(WritableSheet sheet, List<OrgEntity> PList, List<NameValue> spaceList, int spaceRow, String statType, String company, int k, String content) {
        List<List<String>> dataList = new ArrayList<List<String>>();

        String res = "";
        WritableCell n = null;
        //		zxx start
        String sql1 = "";
        int count = 0;
        double ss;
        ;
        //zxx end


        try {
            n = new Label(0, (k * pix) + 1, company, ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
            sheet.addCell(n);
            sheet.mergeCells(0, (k * pix) + 1, 0, (k * pix) + pix);

            if (content.contains(DIE_XING_GUANG_LAN)) {
                n = new Label(1, (k * pix) + 1, "1芯", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 2, "2芯", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 3, "3芯", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 4, "4芯", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 5, "发现外护套开裂次数", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 6, "发现光缆变形次数", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 7, "发现加强芯脱出现象的次数", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 8, "使用2发现的光纤断裂现象（光纤断裂次数）", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 9, "蝶形光缆段的总数量", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 10, "小半径弯曲", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 11, "极端高低温（40度以上，和零下20度以下地区填写）", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 12, "日光直射", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 13, "腐蚀性环境（如酸性土壤，污染地区，管道长期受污水浸泡等）", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 14, "钉固受力", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 15, "因为光缆自身质量问题所造成的光缆故障次数", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 16, "故障总次数", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
            } else if (content.contains(GUANG_LAN)) {

                n = new Label(1, (k * pix) + 1, "一级干线", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 2, "省内干线", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 3, "本地网", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 4, "发现外护套开裂次数", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 5, "发现光缆变形次数", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 6, "发现油膏干裂、涂覆层脱落现象的次数", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 7, "光缆接头是否容易（主观评定,分为3、2、0三个等级）", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 8, "光缆接续速度（主观评定,分为3、2、0三个等级）", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 9, "维护2新增接头的衰减（双向平均0.04dB以上的芯数）(注意：是光纤芯数,不是光缆接头个数！！！)", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 10, "接头总芯数", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 11, "雷电高发地段", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 12, "极端高低温（40度以上,和零下20度以下地区填写）", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 13, "腐蚀性环境（如酸性土壤,污染地区,管道长期受污水浸泡等）", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 14, "冻土地带（三北地区和高原地区填写）", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 15, "因为光缆自身质量问题所造成的光缆故障次数", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 16, "故障总次数", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);

            } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {
                n = new Label(1, (k * pix) + 1, "样品1实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 2, "样品2实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 3, "样品3实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 4, "样品4实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 5, "样品5实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 6, "样品6实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 7, "样品7实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 8, "样品8实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 9, "样品9实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 10, "样品10实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 11, "样品11实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 12, "样品12实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);

            } else if (content.contains(GUANG_LAN_CE_SHI)) {
                n = new Label(1, (k * pix) + 1, "样品1实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 2, "样品2实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 3, "样品3实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 4, "样品4实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 5, "样品5实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
                n = new Label(1, (k * pix) + 6, "样品6实测数据", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                sheet.addCell(n);
            }


        } catch (RowsExceededException e1) {
            e1.printStackTrace();
        } catch (WriteException e1) {
            e1.printStackTrace();
        }
        if (company.equals("江苏亨通光电股份有限公司")) {
            String a = "asdf";
        }
        for (int i = 0; i < PList.size(); i++) {//每个省份的上报情况
            //zxx start
            count = 1;//只拼一次字符串即可
            sql1 = "select distinct p.reportdate from t_eem_excel_page  p where ";
            ss = 0.0;
            //zxx end
            List<String> tmpList = new ArrayList<String>();
            for (int j = 0; j < spaceList.size(); j++) {
                NameValue value = spaceList.get(j);
                int va = Integer.parseInt(value.getValue());
                int na = Integer.parseInt(value.getName());
                if (value.getRemark() != null && value.getRemark().startsWith("PV")) {
                    tmpList.add(PList.get(i).getOrgName());

                    n = new Label(i + spaceRow, va, PList.get(i).getOrgName(), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));

                } else {

                    String sql = value.getRemark().replace("$$depart", PList.get(i).getOrgCode());
                    sql = sql.replace("$$pageName", company);
                    //logger.info("11111sql="+sql);
                    Object obj = null;

                    //						zxx start
                    if (sql.indexOf("reportdate") == -1) {
                        if (count == 1) {
                            Object obj1 = null;
                            String a[] = sql.split("and");

                            for (int z = 0; z < a.length; z++) {
                                if (a[z].indexOf("tpInputID") != -1) {
                                    if (sql1.indexOf("tpInputID") == -1)
                                        sql1 = sql1 + a[z] + " and ";
                                }
                                if (a[z].indexOf("pagename") != -1) {
                                    if (sql1.indexOf("pagename") == -1)
                                        sql1 = sql1 + a[z] + " and ";
                                }
                                if (a[z].indexOf("reportOrgCode") != -1) {
                                    if (sql1.indexOf("reportOrgCode") == -1)
                                        sql1 = sql1 + a[z] + " and ";
                                }
                                if (a[z].indexOf("Year") != -1) {
                                    sql1 = sql1 + a[z];
                                    int first = 0;
                                    if (sql1.indexOf("Year='") != -1) {
                                        first = sql1.indexOf("Year='");
                                        sql1 = sql1.substring(0, first + 11);
                                    }
                                    if (sql1.indexOf("Year ='") != -1) {
                                        first = sql1.indexOf("Year ='");
                                        sql1 = sql1.substring(0, first + 12);
                                    }
                                    if (sql1.indexOf("Year = '") != -1) {
                                        first = sql1.indexOf("Year = '");
                                        sql1 = sql1.substring(0, first + 13);
                                    }
                                    break;
                                }
                            }

                            //logger.info("88888888"+sql1+"99999999999999999999999");
                            try {
                                sql1 = sql1.replaceAll("strtonumber", "");
                                System.out.println(sql1);
                                obj = baseDAO.findNativeSQL(sql1, null);
                            } catch (Exception ex) {
                                logger.info("报错SQL：" + sql1);
                            }
                            if (obj1 != null) {
                                List list1 = (List) obj1;
                                if (list1.size() > 0) {
                                    ss = list1.size();
                                }
                                //logger.info("ss="+ss);
                            }
                            count = 0;
                        }
                        //zxx end
                    }


                    try {
                        sql = sql.replaceAll("strtonumber", "");
                        System.out.println(sql);
                        obj = baseDAO.findNativeSQL(sql, null);

                    } catch (Exception ex) {
                        System.out.println(ex);
                        logger.info("报错SQL：" + sql);
                    }
                    if (obj != null) {
                        List list = (List) obj;
                        if (list != null && list.size() > 0 && list.get(0) != null) {
                            Map resMap = (Map) list.get(0);
                            Object ob = resMap.get("value");
                            if (ob != null) {

                                //zxx start
                                if (sql.indexOf("reportdate") != -1) {
                                    res = ob.toString();
                                } else {
                                    double ss1;
                                    ss1 = new Double(ob.toString()).doubleValue();
                                    if (sql.indexOf("sum") != -1) {
                                        if (ss != 0.0) {
                                            res = ss1 / ss + "";
                                            //logger.info("res="+res);
                                        } else {
                                            res = "0";
                                        }
                                    } else {
                                        res = ss1 + "";
                                    }
                                }

                                //zxx end
                                //res = ob.toString();

                                try {//处理无限循环小数 如果出现问题立即捕获 不影响数据显示
                                    if (res.indexOf(".") != -1) {
                                        if (res.length() - res.indexOf(".") > 3) {
                                            res = res.substring(0, res.indexOf(".") + 3);
                                        }
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                res = "0";
                            }
                        } else {
                            res = "0";
                        }
                    } else {
                        res = "0";
                    }
                    double resDouble = 0;
                    try {
                        resDouble = Double.parseDouble(res);
                    } catch (Exception ex) {

                    }
                    tmpList.add(resDouble + "");


                    n = new Number(i + spaceRow, (k * pix) + va, resDouble, ExportUtil.getBackfround(jxl.format.Colour.WHITE, 8));


                }

                try {
                    sheet.addCell(n);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            dataList.add(tmpList);
        }

        return dataList;
    }

    public EvaluationReference findEvaluationReferenceById(long formId) {
        try {
            EemTempEntity eemTempEntity = (EemTempEntity) baseDAO.get(EemTempEntity.class, formId);
            EvaluationReference evaluationReference = (EvaluationReference) baseDAO.get(EvaluationReference.class, eemTempEntity.getShortName());
            if (evaluationReference == null) {
                evaluationReference = new EvaluationReference();
                evaluationReference.setMaxFaultrate(0);
                evaluationReference.setMinFaultrate(0);
            }

            return evaluationReference;
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }
}