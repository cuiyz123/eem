package com.metarnet.eomeem.service.impl;

import com.alibaba.fastjson.JSON;
import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.FileAdapter;
import com.metarnet.core.common.adapter.SendAdapter;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.utils.Constants;
import com.metarnet.core.common.utils.HttpClientUtil;
import com.metarnet.eomeem.model.*;
import com.metarnet.eomeem.service.*;
import com.metarnet.eomeem.utils.*;
import com.metarnet.eomeem.vo.NameValue;
import com.ucloud.paas.agent.PaasException;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import com.unicom.ucloud.workflow.objects.Participant;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.hibernate.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2015/11/17.
 */
@Service
public class EemGatherServiceImpl implements IEemGatherService {

    private static final String GUANG_LAN = "光缆";
    private static final String DIE_XING_GUANG_LAN = "蝶形光缆";
    private static final String GUANG_LAN_CE_SHI = "光 缆 测 试";
    private static final String DIE_XING_GUANG_LAN_CE_SHI = "蝶 形 光 缆 测 试";

    private static final String GUANG_LAN_TOTAL = "光缆测试数据半年报表";
    private static final String DIE_XING_GUANG_LAN_TOTA = "蝶形光缆测试数据半年报表";
    private static final String GUANG_LAN_ZH_TOTAL = "光缆产品供货和综合评价季度表";
    private static final String DIE_XING_GUANG_LAN_ZH_TOTAL = "蝶形光缆产品供货和综合评价季度表";

    private static final String STAT_TYPE_COL = "col";//标识 统计表格是行还是列
    private static final String STAT_TYPE_ROW = "row";

    private int pix = 16;//光缆和蝶形光缆的 系数 综合的是  16

    private String init = "";

    private String initYear = "";

    public static List<EemTempEntity> gatherTempletList = null; // 汇总模板

    Logger logger = LogManager.getLogger(EemGatherServiceImpl.class);

    @Resource
    private IBaseDAO baseDAO;

    @Resource
    private IEemReportService eemReportService;

    @Resource
    private IEemTemplateService eemTemplateService;

    @Resource
    private IEemReportService reportService;

    @Resource
    private IEemCommonService eemCommonService;
    @Resource
    private IEemSummaryService eemSummaryService;

    public List<String> getPageByDepAndFormidAndReportDate(String dep, String reportDate, String year, long formId) {
        List<String> aa = new ArrayList<String>();
        aa = Arrays.asList(dep);
        String hql = "select  distinct tpInputName from ExcelPage a where a.tpInputID = '" + formId + "' and a.reportOrgCode like '219%' and a.reportDate='" + reportDate + "' and a.reportYear='" + year + "'";
        List<String> list = null;
        try {
            list = baseDAO.find(hql);
//            list = baseDAO.find(hql,aa.toArray());
        } catch (DAOException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public HSSFWorkbook downGatherData(String repotYear, String reportDate, EemTempEntity tempEntity, EemTempEntity repotEntity, String deptIds, UserEntity userEntity) {

        try {
            switch (Integer.parseInt(reportDate)) {
                case 1:
                    reportDate = "第一季度";
                    break;
                case 2:
                    reportDate = "第二季度";
                    break;
                case 3:
                    reportDate = "第三季度";
                    break;
                case 4:
                    reportDate = "第四季度";
                    break;
                case 6:
                    reportDate = "上半年";
                    break;
                case 12:
                    reportDate = "下半年";
                    break;
                case 13:
                    reportDate = "全年";
                    break;
                default:
                    reportDate = "第一季度";

            }
            List<OrgEntity> orgList1 = new ArrayList<OrgEntity>();
            if (StringUtils.isBlank(deptIds)) {
                OrgEntity orgEntity = AAAAAdapter.getCompany(userEntity.getOrgID().intValue());
                orgList1 = AAAAAdapter.getInstence().findOrgListByParentID(orgEntity.getOrgId());
            } else {
                for (String orgId : deptIds.split(",")) {
                    orgList1.add(AAAAAdapter.getInstence().findOrgByOrgID(Long.parseLong(orgId)));
                }
            }
//            EemTempEntity reportTempEntity =tempEntity.getEemTempEntity();// eemTemplateService.findTempByID(tempEntity.getEemTempEntity());
            HSSFWorkbook gatherTemp = new HSSFWorkbook(new ByteArrayInputStream(tempEntity.getTemplateExcelByteData().getUploadFileData()));
            HSSFWorkbook inputTemp = new HSSFWorkbook(new ByteArrayInputStream(repotEntity.getTemplateExcelByteData().getUploadFileData()));
            int sheetCount = gatherTemp.getNumberOfSheets();
            for (int i = 0; i < sheetCount; ) {//gatherTemp.getSheetAt(i)
                HSSFSheet sheet = null;
                if (sheetCount == 1) {
                    sheet = gatherTemp.getSheetAt(0);
                } else {
                    sheet = gatherTemp.getSheetAt(i);
                }
                String sheetName = sheet.getSheetName();
                if (sheetName.equals(inputTemp.getSheetName(0))) {
//                    List<OrgEntity> orgList = AAAAAdapter.getInstence().findOrgListByParentID(120);
                    dealSheetForEvaluationPoi(false, sheet, orgList1, reportDate, false, repotYear, repotEntity.getObjectId(), "");
//                    dealSheetForProGather(sheet, orgList1, repotYear, reportDate, reportTempEntity.getObjectId());
                    i++;
                } else {
                    int reSheetCount = gatherTemp.getNumberOfSheets();
                    for (int j = 0; j < reSheetCount; j++) {
                        HSSFSheet reSheet = gatherTemp.getSheetAt(j);
                        String reSheetName = reSheet.getSheetName();
                        if (reSheetName.equals(sheetName)) {
                            gatherTemp.removeSheetAt(j);
                            sheetCount--;
                            break;


                        }
                    }
                }
            }

            return gatherTemp;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public EemTempEntity getTemEntity(Long formId) throws DAOException {
        List<EemTempEntity> list = baseDAO.find("from EemTempEntity a where a.objectId=" + formId);
        EemTempEntity nt = null;
        if (list != null && list.size() > 0) {
            nt = list.get(0);
        }
        return nt;
    }


    /**
     * 总部汇总和省份汇总都用到，总部汇总如果按年汇总，那么不考虑data也就是上报频率，而省份汇总时会考虑到频率
     * 总部汇总传data值为空，省份为该有的频率值（季度或半年） zxx
     */

    private String fromTempContentToSqlResForGather(String content, String orgForInQuery,
                                                    String reportYearStr, String reportDateStr, long tpInputID) {
        String sql = "";
        String res = "";
        try {
            if (content != null) {
                content = content.trim();
                if (content.trim().startsWith("##SQL:")) {
                    sql = content.substring(6);
                    StringBuilder addCondition = new StringBuilder(" and txtvalue!=\"\" ");
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(reportYearStr)) {
                        addCondition.append(" and p.reportYear ='").append(reportYearStr).append("'");
                    }
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(reportDateStr)) {
                        addCondition.append(" and p.reportDate ='").append(reportDateStr).append("'");
                    }

                    if (org.apache.commons.lang3.StringUtils.isNotBlank(orgForInQuery)) {
                        addCondition.append(" and p.reportOrgCode like '").append(Long.parseLong(orgForInQuery)).append("%'");
                    }
//                    if (org.apache.commons.lang3.StringUtils.isNotBlank(reportPageID)) {
//                        addCondition.append(" and p.page_id in (").append(reportPageID).append(")");
//                    }

                    if (sql != null && !"".equals(sql)) {

                        if (sql.contains("$$condition")) {
                            sql = sql.replace("$$condition", addCondition.toString());
                        }
                        if (sql.contains("$$tpInputID")) {
                            sql = sql.replace("$$tpInputID", tpInputID + "");
                        }
                        if (sql.contains("$$date")) {
                            sql = sql.replace("$$date", reportDateStr + "");
                        }
                        if (sql.contains("$$reportYear")) {
                            sql = sql.replace("$$reportYear", reportYearStr + "");
                        }

                        Query query = baseDAO.getSessionFactory().getCurrentSession().createSQLQuery(sql);
                        List list = query.list();

                        if (list != null && list.size() > 0 && list.get(0) != null) {
                            Object ob = list.get(0);
                            res = ob.toString();
//                            try {//处理无限循环小数 如果出现问题立即捕获 不影响数据显示
//                                if (res.indexOf(".") != -1) {
//                                    if (res.length() - res.indexOf(".") > 3) {
//                                        res = res.substring(0, res.indexOf(".") + 3);
//                                    }
//                                }
//                            } catch (Exception ex) {
//                                ex.printStackTrace();
//                            }
                        }
                    }
                }
                if (content.trim().startsWith("##PV")) {
                    res = "##PV";
                }
            }
        } catch (Exception ex) {
            System.out.println(sql);
            ex.printStackTrace();
        }
        return res;
    }


    //根据模版解析sql
    @Override
    public WritableWorkbook fromDBByteArrayToTable(boolean aa, byte[] array, OutputStream os, String reportDateStr, List orgList, String reportYear, String formId) throws Exception {
//        formId="24";
        System.out.println("reportDateStr=" + reportDateStr);
        System.out.println("reportYear=" + reportYear);
        boolean allReplace = false;
//        String conditions = " isprovince='1'  and ywdepart!='YW01060138' and  ywdepart!='YW01060137' ";
        if (aa) {
            if ("0".equals(reportDateStr)) {
                reportDateStr = " in('第一季度','第二季度','第三季度','第四季度')";
            } else if ("-1".equals(reportDateStr)) {
                reportDateStr = "";
                allReplace = true;
            }
            /*switch (Integer.parseInt(reportDateStr)) {
                case -1:
                    reportDateStr = "";
                    allReplace = true;
                    break;
                case 0:
                    reportDateStr = " in('第一季度','第二季度','第三季度','第四季度')";
                    allReplace = true;
                    break;
                case 1:
                    reportDateStr = "第一季度";
                    break;
                case 2:
                    reportDateStr = "第二季度";
                    break;
                case 3:
                    reportDateStr = "第三季度";
                    break;
                case 4:
                    reportDateStr = "第四季度";
                    break;
                case 6:
                    reportDateStr = "上半年";
                    break;
                case 12:
                    reportDateStr = "下半年";
                    break;
                case 13:
                    reportDateStr = "全年";
                    break;
                default:
                    reportDateStr = "第一季度";

            }*/
        }


        boolean isSearchHQData = false;//标示需要统计全国汇总数据
        //要汇总的省份信息保存到 searchPVList
//        List<OrgEntity> orgList = new ArrayList<OrgEntity>();
        /*if (deps != null) {
            for (String orgId : deps.split(",")) {
                OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgID(Long.parseLong(orgId));
                orgList.add(orgEntity);
            }
        }*/

        InputStream is = new ByteArrayInputStream(array);
        WorkbookSettings wbs = new WorkbookSettings();
        wbs.setWriteAccess(null);
        //解决乱码问题
        wbs.setEncoding("GB2312");
        Workbook wb = Workbook.getWorkbook(is);
        //打开一个文件副本，并将指定数据写回到源文件
        WritableWorkbook wwb = Workbook.createWorkbook(os, wb, wbs);
        // WritableSheet   sheet=wwb.getSheet(0);
        WritableSheet[] sheetArray = wwb.getSheets();
        for (WritableSheet sheet : sheetArray) {
            sheet.getSettings().setSelected(true);
            if (sheet.getName().equals("分省后评价得分")) {
                continue;
            }
            //sheet name现在这三个sheet页都能读到
            System.out.println("99999999" + sheet.getName());
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
                            System.out.println("表格系数为" + coefficient);
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
                dealSheetForHQ2new(wwb, sheet, orgList, reportDateStr, isSearchHQData, "common", reportYear, coefficient, equipType, formId);
            } else if (content.contains("甲") || content.contains("乙")) {
                dealSheetForHQ(allReplace, sheet, orgList, reportDateStr, isSearchHQData, "common", reportYear, coefficient, equipType, Long.parseLong(formId));
            } else if (content.contains(GUANG_LAN_TOTAL) || content.contains(DIE_XING_GUANG_LAN_TOTA) || content.contains(GUANG_LAN_ZH_TOTAL) || content.contains(DIE_XING_GUANG_LAN_ZH_TOTAL)) {
                wwb.removeSheet(1);
            } else if (content.contains(GUANG_LAN) || content.contains(DIE_XING_GUANG_LAN) || content.contains(DIE_XING_GUANG_LAN_CE_SHI) || content.contains(GUANG_LAN_CE_SHI)) {
                //continue;
                //dealSheetForGUANGLAN(sheet,searchPVList,HQList,reportDateStr,isSearchHQData,"common",reportYear,coefficient,equipType,content,formId);
                dealSheetForGUANGLANnew(wwb, sheet, orgList, reportDateStr, isSearchHQData, "common", reportYear, coefficient, equipType, content, formId);
            } else {
                if (sheet.getName().contains("原始")) {
                    dealSheetForHQ1(allReplace, sheet, orgList, reportDateStr, isSearchHQData, "common", reportYear, Long.parseLong(formId));
                } else {
                    dealSheetForEvaluation(allReplace, sheet, orgList, reportDateStr, isSearchHQData, "common", reportYear, Long.parseLong(formId));
                }
            }
        }
//        wwb.write();
//        wwb.close();
        return wwb;
    }


    private void delExcelPage(ExcelPage ep) {

        try {
            ExcelPage excelPage = new ExcelPage();
            excelPage.setReportOrgCode(ep.getReportOrgCode());
            excelPage.setReportYear(ep.getReportYear());
            excelPage.setReportDate(ep.getReportDate());
            excelPage.setTpInputID(ep.getTpInputID());

            List<ExcelPage> list = baseDAO.findByExample(excelPage);
            String ids = "";
            if (list != null && list.size() > 0) {
                for (ExcelPage page : list) {
                    ids += excelPage.getObjectId() + ",";
                }
                if (ids.length() > 0) {
                    ids = (String) ids.subSequence(0, ids.length() - 1);
                    int delNum1 = baseDAO.executeSql("delete from t_cost_excel_page_values where pageid in(" + ids + ")");
                    int delNum2 = baseDAO.executeSql("delete from t_cost_excel_page where pageid in(" + ids + ")");
                    logger.info("删除" + ep.getObjectId() + "对应的" + delNum1 + "条模板数据的" + delNum2 + "条数据");
                }
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }


    //	处理后评价汇总 对各种设备进行汇总
    //dealType   current  across
    private void dealSheetForHQ(boolean allrepache, WritableSheet sheet, List<OrgEntity> orgList, String reportDateStr, boolean isSearchHQData, String dealType, String reportYear, double coefficient, String equipType, long repotTemId) throws Exception {
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

                    String sql = fromTempletContentToSql(allrepache, content, "", reportDateStr, reportYear, repotTemId);

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
        System.out.println("sheet=" + sheet.getName());
        List<List<String>> dataList = getDataList(isSearchHQData, dealType, sheet, orgList, spaceList, spaceRow, STAT_TYPE_ROW);


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
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
        if ("甲".equals(equipType)) {//这里处理甲类汇总模板
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
                        String qStr = dataList.get(j).get(i);
                        double value = Double.valueOf(qStr);
                        if (value > 0) {
                            //int coefficientInt=(int)coefficient;
                            //int index=i/coefficientInt-1;
                            verNumArray[i]++;
                        }
                    }
                }
            }
            double maxNum = 0;
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
                        sortList.add(Double.parseDouble(resList.get(i)));
                    }
                } catch (Exception ex) {

                }
            }
            Collections.sort(sortList);
            int a = (int) (sortList.size() * 0.7);
            double aMin = sortList.get(a) / orgList.size();
            WritableCell rCell = null;

            for (int j = 0; j < resList.size(); j++) {
                try {
                    double tt = Double.parseDouble(resList.get(j));
                    tt = tt / orgList.size();

                    if (j % coefficient == 0) {//总分项在这里处理
                        if (tt < 60 && tt >= 40) {
                            rCell = new Label(j - 2, orgList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.RED, 12));
                        } else if (tt < 40) {
                            rCell = new Label(j - 2, orgList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            if (tt >= aMin) {
                                rCell = new Label(j - 2, orgList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                            } else {
                                rCell = new Label(j - 2, orgList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                            }
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(j - 2, orgList.size() + spaceRow, j, orgList.size() + spaceRow);

                    } else {
                        //	rCell = new Number(j,orgList.size()+spaceRow,tt);
                    }
                } catch (Exception ex) {
                    rCell = new Label(j, orgList.size() + spaceRow, resList.get(j), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
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
                        sortList.add(Double.parseDouble(resList.get(i)));
                    }
                } catch (Exception ex) {

                }
            }
            Collections.sort(sortList);
            a = (int) (sortList.size() * 0.7);

            aMin = sortList.get(a) / orgList.size();
            rCell = null;
            for (int j = 0; j < resList.size(); j++) {
                try {

                    double tt = Double.parseDouble(resList.get(j));
                    tt = tt / orgList.size();

                    if (j % coefficient == 0) {//总分项在这里处理
                        if (tt < 60 && tt >= 40) {
                            rCell = new Label(j - 2, orgList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.RED, 12));
                        } else if (tt < 40) {
                            rCell = new Label(j - 2, orgList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {
                            if (tt >= aMin) {
                                rCell = new Label(j - 2, orgList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                            } else {
                                rCell = new Label(j - 2, orgList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                            }
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(j - 2, orgList.size() + spaceRow + 1, j, orgList.size() + spaceRow);
                    } else {
                        //rCell = new Number(j,orgList.size()+spaceRow+1,tt);
                    }


                } catch (Exception ex) {

                    rCell = new Label(j, orgList.size() + spaceRow + 1, resList.get(j), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                    sheet.addCell(rCell);//具体数字
                }
                //sheet.addCell(rCell);//具体数字
            }
///////////////////////add
            resList = new ArrayList<String>();//
            resList.add("后评价得分");
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

            aMin = sortList.get(a) / orgList.size();
            rCell = null;
            for (int j = 0; j < resList.size(); j++) {
                try {
                    double tt = 0;
                    if (j % coefficient == 0) {
                        tt = Double.parseDouble(resList.get(j));
                        if (verNumArray[j] != 0.0) {
                            tt = tt / verNumArray[j];
                        }

                    }

                    if (j % coefficient == 0) {//总分项在这里处理
                        int coefficientInt = (int) coefficient;

                        if (tt >= aMin) {
                            if (tt - markValue[j] < 0) {
                                rCell = new Label(j - 2, orgList.size() + spaceRow + 2, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                            } else {
                                rCell = new Label(j - 2, orgList.size() + spaceRow + 2, df.format(tt - markValue[j]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                            }
                        } else {
                            if (tt - markValue[j] < 0) {
                                rCell = new Label(j - 2, orgList.size() + spaceRow + 2, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12)); //后70%，标记成白色

                            } else {
                                rCell = new Label(j - 2, orgList.size() + spaceRow + 2, df.format(tt - markValue[j]), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12)); //后70%，标记成白色
                            }
                        }

                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(j - 2, orgList.size() + spaceRow + 2, j, orgList.size() + spaceRow);
                    } else {
                        //rCell = new Number(j,orgList.size()+spaceRow+1,tt);
                    }


                } catch (Exception ex) {

                    rCell = new Label(j, orgList.size() + spaceRow + 2, resList.get(j), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                    sheet.addCell(rCell);//具体数字
                }
            }

            ///////////////////////////

        } else if (!sheet.getName().equals("各省得分") && ("乙".equals(equipType) || "丙".equals(equipType))) {


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

            int verNum = (int) ((dataList.get(0).size() - 1) / coefficient); //厂家数量；
            double[] verNumArray = new double[dataList.get(0).size()]; //保存各个厂家省份覆盖数量
            double[] scaleMerit = new double[dataList.get(0).size()];// 规模效益分
            for (int i = 1; i < dataList.get(0).size(); i++) {
                //double markY=0;
                if (i % coefficient == 0) { //取总分
                    for (int j = 0; j < dataList.size(); j++) {
                        String qStr = dataList.get(j).get(i);
                        double value = Double.valueOf(qStr);
                        if (value > 0) {
                            //int coefficientInt=(int)coefficient;
                            //int index=i/coefficientInt-1;
                            verNumArray[i]++;
                        }
                    }
                }
            }
            double maxNum = 0;
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
            List<String> resList = new ArrayList<String>();//保存合计行的数据
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
                        if (resDouble < 0.01) {
                            resDouble = minArray[j];
                        }
                        resList.add(resDouble + "");
                    }
                }
            }


            List<Double> sortList = new ArrayList<Double>();
            for (int i = 0; i < resList.size(); i++) {
                try {
                    if (i % coefficient == 0) {
                        double tt = Double.parseDouble(resList.get(i));
                        tt = (tt / orgList.size()) * 0.8 + mark[i] * 0.2;
                        sortList.add(tt);
                    }
                } catch (Exception ex) {

                }
            }
            Collections.sort(sortList);
            int a = 0;
            if (sortList.size() > 0) {
                a = (int) (sortList.size() * 0.7);
            }


            //double aMin = sortList.get(a)/orgList.size();
            double aMin = 0d;
            if (sortList.size() > 0) {
                aMin = sortList.get(a);
            }

            WritableCell rCell = null;
            for (int j = 0; j < resList.size(); j++) {
                try {
                    double tt = Double.parseDouble(resList.get(j));
                    tt = (tt / orgList.size()) * 0.8 + mark[j] * 0.2;
                    if (j % coefficient == 0) {//总分项在这里处理
                        if (tt < 60) {
                            rCell = new Label(j - 2, orgList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.RED, 12));
                        } else {
                            if (tt >= aMin) {
                                rCell = new Label(j - 2, orgList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12));
                            } else {
                                rCell = new Label(j - 2, orgList.size() + spaceRow, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                            }
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(j - 2, orgList.size() + spaceRow, j, orgList.size() + spaceRow);
                    } else {
                        //rCell = new Number(j,orgList.size()+spaceRow,tt);
                        //sheet.addCell(rCell);//具体数字
                    }
                } catch (Exception ex) {
                    rCell = new Label(j, orgList.size() + spaceRow, resList.get(j), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                    sheet.addCell(rCell);//具体数字
                }


            }


            resList.set(0, "建议暂缓下期集采");


            rCell = null;
            for (int j = 0; j < resList.size(); j++) {
                try {
                    double tt = Double.parseDouble(resList.get(j));
                    tt = tt / orgList.size();
                    if (j % coefficient == 0) {//总分项在这里处理
                        if (tt < 40) {
                            rCell = new Label(j - 2, orgList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.YELLOW, 12));
                        } else {

                            rCell = new Label(j - 2, orgList.size() + spaceRow + 1, df.format(tt), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                        }
                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(j - 2, orgList.size() + spaceRow + 1, j, orgList.size() + spaceRow);
                    } else {
                        //rCell = new Number(j,orgList.size()+spaceRow,tt);
                        //sheet.addCell(rCell);//具体数字
                    }
                } catch (Exception ex) {
                    rCell = new Label(j, orgList.size() + spaceRow + 1, resList.get(j), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                    sheet.addCell(rCell);//具体数字
                }


            }


            /////////////////////////add
            resList = new ArrayList<String>();//
            resList.add("后评价得分");
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
            if (sortList.size() > 0) {
                a = (int) (sortList.size() * 0.3);
                aMin = sortList.get(a) / orgList.size();
            }
            rCell = null;
            for (int j = 0; j < resList.size(); j++) {
                try {
                    double tt = 0;
                    if (j % coefficient == 0 && verNumArray[j] > 0) {
                        tt = Double.parseDouble(resList.get(j));
                        tt = tt / verNumArray[j];
                    }

                    if (j % coefficient == 0) {//总分项在这里处理
                        int coefficientInt = (int) coefficient;

                        if (tt >= aMin) {
                            if (tt - markValue[j] < 0) {
                                rCell = new Label(j - 2, orgList.size() + spaceRow + 2, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                            } else {
                                rCell = new Label(j - 2, orgList.size() + spaceRow + 2, df.format(tt - markValue[j]), ExportUtil.getBackfround(jxl.format.Colour.GREEN, 12)); //前30% 标记成绿色
                            }
                        } else {
                            if (tt - markValue[j] < 0) {
                                rCell = new Label(j - 2, orgList.size() + spaceRow + 2, df.format(0), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                            } else {
                                rCell = new Label(j - 2, orgList.size() + spaceRow + 2, df.format(tt - markValue[j]), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12));
                            }

                        }

                        sheet.addCell(rCell);//具体数字
                        sheet.mergeCells(j - 2, orgList.size() + spaceRow + 2, j, orgList.size() + spaceRow);
                    } else {
                        //rCell = new Number(j,orgList.size()+spaceRow+1,tt);
                    }


                } catch (Exception ex) {

                    rCell = new Label(j, orgList.size() + spaceRow + 2, resList.get(j), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                    sheet.addCell(rCell);//具体数字
                }
            }

            ///////////////////////////


        }//else if("乙".equals(equipType)||"丙".equals(equipType)){
        else if ("乙1".equals(equipType)) {


        }
    }

    //  根据模版表格中特殊格式的sql 生成查询结果  后评价 总部汇总用到
    private String fromTempletContentToSql(boolean allReplace, String content, String hqDepsForInQuery, String date, String reportYear, long reportTmpId) {
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
                        String asql = " and txtvalue!=\"\"  and p.tpInputID=" + reportTmpId;
                        sql = sql.replace("$$condition", asql + " and ( willCollect=1 or willCollect=0 )");
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

                        //System.out.println("000"+sql);
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

                        Date newdate = new Date();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        String dateStrMon = df.format(newdate);
                        String monthStr = dateStrMon.substring(5, 7);
                        String yearStr = dateStrMon.substring(0, 4);
                        String lastYearStr = "";
                        String currentYearStr = "";
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
                        sql = sql.replace("$$condition", " and  (willCollect=1 or willCollect=1)" + lastYearStr + currentYearStr);
                        // zxx end
                        //System.out.println("000"+sql);
                    } else { //季度半年汇总
                        if (allReplace) {
                            sql = sql.replace("='$$date'", date);
                        } else {
                            sql = sql.replace("$$date", date);
                        }

                        sql = sql.replace("$$reportYear", reportYear);
                        String asql = " and txtvalue!=\"\"  and p.tpInputID=" + reportTmpId;
                        sql = sql.replace("$$condition", asql + " and ( willCollect=1 or willCollect=0)");
                    }
                    sql = sql.replace("$$formid", String.valueOf(reportTmpId));
                    return sql;
                }
            }
        } catch (Exception ex) {
            System.out.println(sql);
            ex.printStackTrace();
        }
        return res;
    }


    //	  省份和 汇总项 颠倒
    private void dealSheetForHQ2new(WritableWorkbook wwb, WritableSheet sheet, List<OrgEntity> orgList, String reportDateStr, boolean isSearchHQData, String dealType, String reportYear, double coefficient, String equipType, String formId) throws Exception {
        int rowCount = sheet.getRows();
        int colCount = sheet.getColumns();
        List<NameValue> spaceList = new ArrayList<NameValue>();
        int spaceRow = 0;//填充数据到的具体的行数5

        spaceRow = getSpaceList(sheet, reportDateStr, dealType, reportYear, spaceList, Long.parseLong(formId));

        List<List<String>> dataList = getDataList(isSearchHQData, dealType, sheet, orgList, spaceList, spaceRow, STAT_TYPE_COL);

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

            if (wwb.getNumberOfSheets() > 2) {
                getArraysNew2(orgList, wwb, dataList, sumArray, percentArray, mark, percentMark, realArray, finalArray, coefficient,
                        resArray, faultRateArray, serveArray, evaluateArray, equipType, formId, vendorfaultRateArray,
                        hqEvaluateArray, hqGroupEvaluateArray, groupEvaluateArray);
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
            Collections.sort(sortList);
            int a = (int) (sortList.size() * 0.7);

            //double aMin = sortList.get(a)/orgList.size();
            double aMin = sortList.get(a);

            int col = orgList.size() + spaceRow;
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
            col = orgList.size() + spaceRow + 2;
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
                        //rCell = new Number(j,orgList.size()+spaceRow,tt);
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
            col = orgList.size() + spaceRow + 3;
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
                        //rCell = new Number(j,orgList.size()+spaceRow,tt);
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
            col = orgList.size() + spaceRow + 5;
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
            col = orgList.size() + spaceRow + 6;
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
            col = orgList.size() + spaceRow + 7;
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
            col = orgList.size() + spaceRow + 8;
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
            col = orgList.size() + spaceRow + 9;
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
        }


    }


    //根据模版表格中特殊格式的sql 生成查询结果
    private String fromTempletContentToSqlRes(String content, String date) {
        String sql = "";
        String res = "";
        try {
            if (content != null) {
                content = content.trim();
                if (content.trim().startsWith("##SQL:")) {
                    sql = content.substring(6);
                    sql = sql.replace("$$date", date);
                    sql = sql.replace("$$condition", " and txtvalue!=\"\"  and ( willCollect=1 or willCollect=0)");

                    if (sql != null && !"".equals(sql)) {

                        Object obj = baseDAO.executeSql(sql);
                        if (obj != null) {
                            List list = (List) obj;
                            if (list != null && list.size() > 0 && list.get(0) != null) {
                                Map resMap = (Map) list.get(0);
                                Object ob = resMap.get("value");
                                if (ob != null) {
                                    res = ob.toString();

                                    try {//处理无限循环小数 如果出现问题立即捕获 不影响数据显示
                                        if (res.indexOf(".") != -1) {
                                            if (res.length() - res.indexOf(".") > 3) {

                                                res = res.substring(0, res.indexOf(".") + 3);
                                            }
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(sql);
            ex.printStackTrace();
        }
        return res;
    }

    //处理后评价
    private void dealSheetForEvaluation(boolean all, WritableSheet sheet, List<OrgEntity> orgList, String reportDateStr, boolean isSearchHQData, String dealType, String reportYear, long reportTmpId) throws Exception {

        int rowCount = sheet.getRows();
        int colCount = sheet.getColumns();
        for (int i = 0; i < rowCount; i++) {  //行 循环

            for (int j = 0; j < colCount; j++) {//列 循环
                WritableCell cell = sheet.getWritableCell(j, i);
                String content = cell.getContents();
                if (content != null && content.trim().startsWith("##SQL:")) {
                    String res = "";
                    String depsForInQuery = "";
                    for (OrgEntity gr : orgList) {
                        depsForInQuery += "'" + gr.getOrgCode() + "',";
                    }
                    if (depsForInQuery.endsWith(",")) {
                        depsForInQuery = depsForInQuery.substring(1, depsForInQuery.length() - 2);
                    }
                    res = fromTempletContentToSqlResForEvaluation(all, content, depsForInQuery, reportDateStr, reportYear, reportTmpId);

                    double resDouble = 0;
                    try {
                        resDouble = Double.parseDouble(res);
                    } catch (Exception ex) {

                    }
                    Number n = new Number(j, i, resDouble, ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));//加边框
                    sheet.addCell(n);//具体数字


                    //	}

                    int pvAndHqColSize = 0;//经计算后 大于 这个值的列 都清空
                    if (isSearchHQData == true) {
                        pvAndHqColSize = 1;
                    }

                }
            }
        }
    }

    //  根据模版表格中特殊格式的sql 生成查询结果  后评价 总部汇总用到

    /*  总部汇总和省份汇总都用到，总部汇总如果按年汇总，那么不考虑data也就是上报频率，而省份汇总时会考虑到频率
      总部汇总传data值为空，省份为该有的频率值（季度或半年） zxx

*/
    private String fromTempletContentToSqlResForEvaluation(boolean allReplace, String content, String orgCods, String date, String reportYear, long reportTmpId) {
        String sql = "";
        String res = "";
        try {
            if (content != null) {
                content = content.trim();
                if (content.trim().startsWith("##SQL:")) {
                    sql = content.substring(6);
                    //zxx
                    System.out.println("".equals(date));
                    if ("".equals(date) == false) {//省份汇总
                        System.out.println("date不为空");
                        if (allReplace) {
                            sql = sql.replace("='$$date'", date);
                        } else {
                            sql = sql.replace("$$date", date);
                        }


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

                        sql = sql.replace("$$condition", " and txtvalue!=\"\"  and ( willCollect=1 or willCollect=0 )" + lastYearStr + currentYearStr);


                    } else if (sql.contains("$$depart") && sql.contains("$$formid")) {
                        sql = sql.replace("$$reportYear", reportYear);
                        sql = sql.replace("$$formid", String.valueOf(reportTmpId));
                        sql = sql.replace("$$depart", orgCods);
                        sql = sql.replace("$$condition", " and txtvalue!=\"\"  ");
                    } else {
                        sql = sql.replace("$$reportYear", reportYear);
                        String asql = " and txtvalue!=\"\"  and p.tpInputID=" + reportTmpId;
                        sql = sql.replace("$$condition", asql + " and p.reportOrgCode in(" + orgCods + ")");
                    }

                    //sql=sql.replace("$$reportYear", reportYear);
                    String tmpCondition = orgCods;
                    sql = sql.replace("'$$depart'", tmpCondition);
                    //sql=sql.replace("$$condition", " (p.iswithdraw!='Y' or p.iswithdraw is null)");


                    if (sql != null && !"".equals(sql)) {
                        Object obj = baseDAO.findNativeSQL(sql, null);
                        if (obj != null) {
                            List list = (List) obj;
                            if (list != null && list.size() > 0 && list.get(0) != null) {
                                Map resMap = (Map) list.get(0);
                                Object ob = resMap.get("value");
                                if (ob != null) {
                                    res = ob.toString();

                                    System.out.println("--------查詢結果：" + res + "--------");
                                    System.out.println(sql);


                                    try {//处理无限循环小数 如果出现问题立即捕获 不影响数据显示
                                        if (res.indexOf(".") != -1) {
                                            if (res.length() - res.indexOf(".") > 3) {

                                                res = res.substring(0, res.indexOf(".") + 3);
                                            }
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }


                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(sql);
            ex.printStackTrace();
        }
        return res;
    }


    private void dealSheetForGUANGLANnew(WritableWorkbook wwb, WritableSheet sheet, List<OrgEntity> orgList, String reportDateStr, boolean isSearchHQData, String dealType, String reportYear, double coefficient, String equipType, String content, String formId) throws Exception {

        java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");

        WritableSheet ws = null;
        for (WritableSheet wws : wwb.getSheets()) {
            if (wws.getName().equals("分省后评价得分")) {
                ws = wws;
            }
        }
        ws.setColumnView(0, 50);


        int rowCount = sheet.getRows();
        int colCount = sheet.getColumns();
        List<NameValue> spaceList = new ArrayList<NameValue>();
        int spaceRow = 0;//填充数据到的具体的行数5

        String companyListSQL = "";


        if ("".equals(reportYear)) {

            if (content.equals(DIE_XING_GUANG_LAN)) {
                companyListSQL = "select distinct(tpInputName) from t_eem_excel_page where tpInputID='67266'  and  (reportyear='2013' or reportyear='2014')";
                pix = 16;
            } else if (content.contains(GUANG_LAN)) {
                companyListSQL = "select distinct(tpInputName) from t_eem_excel_page where tpInputID='67264'  and  (reportyear='2013' or reportyear='2014')";
                pix = 16;
            } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {
                pix = 12;
                companyListSQL = "select distinct(tpInputName) from t_eem_excel_page where tpInputID='67218'  and  (reportyear='2013' or reportyear='2014')";
            } else if (content.contains(GUANG_LAN_CE_SHI)) {
                pix = 6;
                companyListSQL = "select distinct(tpInputName) from t_eem_excel_page where tpInputID='67220'   and  (reportyear='2013' or reportyear='2014')";
            }


        } else {
            if (content.equals(DIE_XING_GUANG_LAN)) {
                companyListSQL = "select distinct(tpInputName) from t_eem_excel_page where tpInputID='67266' and   and  reportyear='" + reportYear + "'";
                pix = 16;
            } else if (content.contains(GUANG_LAN)) {
                companyListSQL = "select distinct(tpInputName) from t_eem_excel_page where tpInputID='67264'  and  reportyear='" + reportYear + "'";
                pix = 16;
            } else if (content.contains(DIE_XING_GUANG_LAN_CE_SHI)) {
                pix = 12;
                companyListSQL = "select distinct(tpInputName) from t_eem_excel_page where tpInputID='67218' and  reportyear='" + reportYear + "'";
            } else if (content.contains(GUANG_LAN_CE_SHI)) {
                pix = 6;
                companyListSQL = "select distinct(tpInputName) from t_eem_excel_page where tpInputID=5  and  reportyear='" + reportYear + "'";
            }

        }


        List<Map> companyList = baseDAO.findNativeSQL(companyListSQL, null);

        //新增加start
        //将厂家写到新的sheet页中
        for (int k = 0; k < companyList.size(); k++) {
            String name = companyList.get(k).get("tpinputname").toString();
            Label label = new Label(0, k + 1, name, ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            ws.addCell(label);
        }
        for (int k = 0; k < orgList.size(); k++) {
            String name = orgList.get(k).getOrgName();
            Label label = new Label(k + 1, 0, name, ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
            ws.addCell(label);
        }
        //end


        spaceRow = getSpaceList(sheet, reportDateStr, dealType, reportYear, spaceList, Long.parseLong(formId));
        int verdorNum = companyList.size();  // 现网供应商数量，取sheet数量
        double[] pvNum = new double[verdorNum];  //存放各个厂家提供服务的省份数量

        //double[][] allFenshu = new double[companyList.size()][5];
//		double[][] allFenshu = new double[companyList.size()][7];
        double[][] allFenshu = new double[companyList.size()][9];//新增加两列2014版后评价平均分，2014版后评价得分  2014.10.10 btliu
        double[] sumGroupFenshu = new double[companyList.size()];//存放每个厂家的分省后评价得分和
        double[] minGroupFenshu = new double[companyList.size()];//存放每个厂家覆盖省份中最小分省后评价得分
        for (int z = 0; z < companyList.size(); z++) {

            String company = companyList.get(z).get("tpinputname").toString();
            System.out.println("~~~~~~~~~`" + z + "!!!" + company);
            List<List<String>> dataList = getDataListGUANGLAN(sheet, orgList, spaceList, spaceRow, STAT_TYPE_COL, company, z, content);
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
                    }/*else{
                            pvSumFenshuArray[i][index++] =0;
							pvSumFenshuArray[i][index++] = 0;
							pvSumFenshuArray[i][index++] = 0;
						}
						*/

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


//							新增加的df.format(tt),ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12)
/*	 //cgliu start
                         Label label = new Label(i,z,pvReal[i]+"",ExportUtil.getBackfround(jxl.format.Colour.WHITE, 8));
							try {
								ws.addCell(label);
							} catch (RowsExceededException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (WriteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							//把值写入到Excel中
							//end

							 */

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

                        //pvSumFenshuArray[i][index] = resArray[i][index++];
                        //pvSumFenshuArray[i][index] = resArray[i][index++];
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
                            /*pvSumFenshuArray[i][index] = resArray[i][index++];
                            pvSumFenshuArray[i][index] = resArray[i][index++];
							pvSumFenshuArray[i][index] = resArray[i][index++];
							pvSumFenshuArray[i][index] = resArray[i][index++];*/
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
                    }/*else{
                            pvSumFenshuArray[i][index++] = 0;   //tindex=5
							pvSumFenshuArray[i][index++] = 0;
							pvSumFenshuArray[i][index++] = 0;
							continue;
						}

						*/

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

 /*   //cgliu							新增加的df.format(tt),ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12)
                            Label label = new Label(i,z,pvReal[i]+"",ExportUtil.getBackfround(jxl.format.Colour.WHITE, 8));
							try {
								ws.addCell(label);
							} catch (RowsExceededException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (WriteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							//把值写入到Excel中
							//end

							*/

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
                    /*for(int i = 0 ; i<resArray[0].length ; i++){
                        for(int j = 0 ; j<resArray.length ; j++){
							if(i >=0 && i <=4&&resArray[j][i]!=0){
								tmpSet.add(j+"");
							}

						}
					}*/


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
                            /*if(resArray[j][i]<0.180&&resArray[j][i]!=0){
                                sumArray[i] = sumArray[i]+0.26;
							} else if(resArray[j][i]>0.26&&resArray[j][i]!=0){
								sumArray[i] = sumArray[i]+0.26;
							} else if(resArray[j][i]==0){
								sumArray[i] = sumArray[i]+0.26;
							}else{
								sumArray[i] = sumArray[i]+resArray[j][i];
							}*/

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
                    System.out.println("company:" + company);
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

                    length = sumArray[tindex++] + sumArray[tindex++] + sumArray[tindex++];
                } catch (Exception ex) {
                    System.out.println("company:" + company);
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
                    /*double t1 = sumArray[1]/orgList.size();
                    double t2 = sumArray[2]/orgList.size();
					double t3 = sumArray[3]/orgList.size();
					double t4 = sumArray[4]/orgList.size();
					double t5 = sumArray[5]/orgList.size();

					double tmp = (t1+t2+t3+t4+t5)/5;*/


                sumFenshuArray[tindex] = (40 * (0.26 - sumArray[0])) / (0.26 - 0.18);


					/*sumFenshuArray[tindex] = 4 - 4*(sumArray[tindex++]/length);
                    sumFenshuArray[tindex] = 4 - 4*(sumArray[tindex++]/length);
					sumFenshuArray[tindex] = 4 - 4*(sumArray[tindex++]/length);
					sumFenshuArray[tindex] = sumArray[tindex++];
					sumFenshuArray[tindex] = sumArray[tindex++];

					double tmp = sumArray[tindex]/sumArray[tindex+1];//故障率

					faultPercent = tmp;
					if(tmp>0.1){
						tmp = 0.1;
					}


					sumFenshuArray[tindex] = 20 - 200*(tmp);
					tindex=tindex+2;


					sumFenshuArray[tindex] = sumArray[tindex++];
					sumFenshuArray[tindex] = sumArray[tindex++];
					sumFenshuArray[tindex] = sumArray[tindex++];
					sumFenshuArray[tindex] = sumArray[tindex++];
					tmp = sumArray[tindex]/sumArray[tindex+1];//故障率

					if(tmp>0.1){
						tmp = 0.1;
					}
					sumFenshuArray[tindex] = 10 - 100*(tmp);*/
            }


            for (int i = 0; i < sumFenshuArray.length; i++) {
                double tFenshu = sumFenshuArray[i];
                if (Double.isNaN(tFenshu)) {
                    tFenshu = 0;
                }
                real = tFenshu + real;
            }

            //resList.set(0, "建议暂缓下期集采");


            //double aMin = sortList.get(a)/orgList.size();


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
//					for(int j = 0; j < tempPvReal.size() -1; j ++){
//						if(tempPvReal.get(j) > tempPvReal.get(j + 1)){
//							double temp = tempPvReal.get(j);
//							tempPvReal.set(j, tempPvReal.get(j + 1));
//							tempPvReal.set(j + 1, temp);
//						}
//					}
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

//	  新增加的

/*	 //cgliu
      try {
			wwb.write();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			wwb.close();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//end
	    */
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
            allFenshu[i][7] = (sumGroupFenshu[i] + (orgList.size() - pvNum[i]) * minGroupFenshu[i]) / orgList.size() * 0.7;
            //2014版后评价得分
            allFenshu[i][8] = allFenshu[i][7] + scaleMark;
        }
        //add

        int col = orgList.size() + spaceRow;
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
        System.out.print("a~~~~~~~~~" + a + "!!!!!!!!!allFenshu" + allFenshu.length);
        //  System.out.print("a~~~~~~~~~"+a+"!!!!!!!!!sortList"+sortList.size());
        //double aMin = sortList.get(a)/orgList.size();
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
            rCell = null;
            col = orgList.size() + spaceRow;
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
                }

            } catch (Exception ex) {
                rCell = new Label(col, 2 + (z * pix), "hhh", ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 12));
                sheet.addCell(rCell);//具体数字
            }
        }

    }

    private List<List<String>> getDataList(boolean isSearchHQData, String dealType, WritableSheet sheet, List<OrgEntity> orglist, List<NameValue> spaceList, int spaceRow, String statType) {
        List<List<String>> dataList = new ArrayList<List<String>>();
        String res = "";
        WritableCell n = null;
        //zxx start
        String sql1 = "";
        String sql2 = "";
        String sql2_p = "";
        int count = 0;
        double ss = 0.0;
        //zxx end

        for (int i = 0; i < orglist.size(); i++) {//获得省份 zxx 应该获取每个省份的总的季度值或总的半年值
            //zxx start
			/*if(!STAT_TYPE_COL.equals(statType)){
				count=1;//只拼一次
				sql1="select distinct p.reportdate from t_eem_excel_page  p where ";
				ss=0.0;
			}*/
            count = 1;//只拼一次
            logger.info("++++++++++++" + orglist.get(i).getOrgName());
            sql1 = "select distinct p.reportdate from t_eem_excel_page  p where ";
//			sql2="select distinct p.reportdate from t_eem_excel_page  p where ";
            ss = 0.0;
            //zxx end
            List<String> tmpList = new ArrayList<String>();
            for (int j = 0; j < spaceList.size(); j++) {
                NameValue value = spaceList.get(j);
                int va = Integer.parseInt(value.getValue());
                int na = Integer.parseInt(value.getName());
                if (value.getRemark() != null && value.getRemark().startsWith("PV")) {
                    tmpList.add(orglist.get(i).getOrgName());
                    if (STAT_TYPE_COL.equals(statType)) {
                        n = new Label(i + spaceRow, va, orglist.get(i).getOrgName(), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                    } else {
                        n = new Label(va, i + spaceRow, orglist.get(i).getOrgName(), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));
                    }
                } else {
                    String sql = value.getRemark().replace("$$depart", orglist.get(i).getOrgCode());
//                    sql = sql.replace("$$formid",String.valueOf(reportTempId));
                    Object obj = null;

                    if (sql.indexOf("reportdate") == -1) {
                        //zxx start
                        if (count == 1) {
                            Object obj1 = null;
                            String a[] = sql.split("and");

                            for (int z = 0; z < a.length; z++) {
                                if (a[z].indexOf("tpInputID") != -1) {
                                    sql1 = sql1 + a[z] + " and ";
                                }
                                if (a[z].indexOf("createdep") != -1) {
                                    sql1 = sql1 + a[z] + " and ";
                                }
                                if (a[z].indexOf("Year") != -1) {
									/*sql1=sql1+a[z];
									int first=sql1.indexOf("Year");//年份从20开始
									sql1=sql1.substring(0, first+11);
									break;
								}*/
                                    //考虑sql语句的多种拼写方式zxx
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

                            //sql1=sql1+a[3]+" "+"and ";
                            //sql1=sql1+a[6]+" "+"and";
                            //sql1=sql1+a[7];
                            //int first=sql1.indexOf("Year");//年份从20开始
                            //sql1=sql1.substring(0, first+11);
                            //System.out.println("88888888"+sql1+"99999999999999999999999");
                            try {
                                obj1 = baseDAO.findNativeSQL(sql1, null);
                            } catch (Exception ex) {
                                logger.info("报错SQL5：" + sql1);
                            }
                            if (obj1 != null) {
                                List list1 = (List) obj1;
                                if (list1.size() > 0) {
                                    ss = list1.size();
                                }
                                //System.out.println("ss="+ss);
                            }
                            count = 0;
                        }
                        //zxx end
                    }


                    try {
                        if (sql.indexOf("strtonumber") != -1) {
                            sql = sql.replace("strtonumber", "");
                        }
                        obj = baseDAO.findNativeSQL(sql, null);
                    } catch (Exception ex) {
                        logger.info("报错SQL1：" + sql);
                    }
                    if (obj != null) {
                        List list = (List) obj;
                        if (list != null && list.size() > 0 && list.get(0) != null) {
                            Map resMap = (Map) list.get(0);
                            Object ob = resMap.get("value");
                            if (ob != null) {

                                //新增加的处理近四个季度汇总
                                sql2 = "select distinct p.reportdate from t_eem_excel_page  p where ";
                                sql2_p = "select distinct p.reportdate from t_eem_excel_page  p where p.DELETED_FLAG=0 and p.reportOrgCode=" + orglist.get(i).getOrgCode();
                                Object obj2 = null;
                                List list2 = null;
                                if (isSearchHQData) {
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

                                        lastYearStr = " and ((p.reportYear='" + allReportYear + "' and p.reportdate in('第一季度','第二季度','第三季度','第四季度'))";
                                        currentYearStr = ")";
                                        //bn="上半年";
                                    } else if (monthInt >= 4 && monthInt < 7) {
                                        //reportDateStr = "第一季度";
                                        lastYearStr = " and ((p.reportYear='" + allReportYear + "' and p.reportdate in('第二季度','第三季度','第四季度'))";
                                        currentYearStr = " or (p.reportYear='" + yearStr + "' and p.reportdate in('第一季度'))" + ")";
                                        //bn="上半年";

                                    } else if (monthInt >= 7 && monthInt < 10) {
                                        //reportDateStr = "第二季度";
                                        lastYearStr = " and ((p.reportYear='" + allReportYear + "' and p.reportdate in('第三季度','第四季度'))";
                                        currentYearStr = " or (p.reportYear='" + yearStr + "' and p.reportdate in('第一季度','第二季度'))" + ")";
                                        //bn="下半年";

                                    } else {
                                        //reportDateStr = "第三季度";
                                        lastYearStr = " and ((p.reportYear='" + allReportYear + "' and p.reportdate in('第四季度'))";
                                        currentYearStr = " or (p.reportYear='" + yearStr + "' and p.reportdate in('第一季度','第二季度','第三季度'))" + ")";
                                        //bn="下半年";
                                    }
                                    //sql=sql+lastYearStr+currentYearStr;
                                    //拼sql
                                    switch (Integer.parseInt(dealType)) {
                                        case 1:
                                            break;
                                        case 2:
                                            break;
                                    }
                                    String zz[] = sql.split("and");
                                    for (int z = 0; z < zz.length; z++) {
                                        if (zz[z].indexOf("tpInputID") != -1) {
                                            if (sql2.indexOf("tpInputID") == -1)
                                                sql2 = sql2 + zz[z] + " and ";
                                        }
                                        if (zz[z].indexOf("createdep") != -1) {
                                            if (sql2.indexOf("createdep") == -1) {
                                                int begianIndex = zz[z].indexOf("(") - 1;
                                                int lastIndex = zz[z].indexOf(")") + 1;
                                                String dep = zz[z].substring(begianIndex, lastIndex);
                                                dep = "createdep in" + dep;
                                                sql2 = sql2 + dep;
                                            }

                                        }
                                    }

                                    String reportDateStr = "";


                                    sql2 = sql2 + lastYearStr + currentYearStr;
                                    if (sql2.indexOf("value") != -1) {
                                        sql2 = sql2.replace("value", "");
                                        if (sql2.indexOf("from dual") != -1) {
                                            sql2 = sql2.replace("from dual", "");
                                        }
                                    }

                                    try {
                                        sql2_p += lastYearStr + currentYearStr;
                                        obj2 = baseDAO.executeSql(sql2_p);
                                    } catch (Exception ex) {
                                        logger.info("报错SQL2：" + sql2_p);
                                    }
                                    if (obj2 != null) {
                                        list2 = (List) obj2;
                                        if (list2.size() > 0) {
                                            ss = list2.size();
                                        }
                                        //System.out.println("ss="+ss);
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

    //新加的汇总
    //为了移动核心网11个模板，在每一个新增加的sheet页，原始数据进行的操作


    private void dealSheetForHQ1(boolean all, WritableSheet sheet, List<OrgEntity> orglist, String reportDateStr, boolean isSearchHQData, String dealType, String reportYear, long reportTempId) throws Exception {
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

                    String sql = fromTempletContentToSql(all, content, "", reportDateStr, reportYear, reportTempId);

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
        System.out.println("sheet=" + sheet.getName());
        List<List<String>> dataList = getDataList(isSearchHQData, dealType, sheet, orglist, spaceList, spaceRow, STAT_TYPE_ROW);


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


    private int getSpaceList(WritableSheet sheet, String reportDateStr, String dealType, String reportYear, List<NameValue> spaceList, long reportTemId) {
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

                    String sql = fromTempletContentToSql(false, content, "", reportDateStr, reportYear, reportTemId);

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

    private List<List<String>> getDataListGUANGLAN(WritableSheet sheet, List<OrgEntity> orgList, List<NameValue> spaceList, int spaceRow, String statType, String company, int k, String content) {
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
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (WriteException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        if (company.equals("江苏亨通光电股份有限公司")) {
            String a = "asdf";
        }
        for (int i = 0; i < orgList.size(); i++) {//每个省份的上报情况
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
                    tmpList.add(orgList.get(i).getOrgName());

                    n = new Label(i + spaceRow, va, orgList.get(i).getOrgName(), ExportUtil.getBackfround(jxl.format.Colour.GRAY_25, 8));

                } else {

                    String sql = value.getRemark().replace("$$depart", orgList.get(i).getOrgCode());
                    sql = sql.replace("$$tpInputName", company);
                    //System.out.println("11111sql="+sql);
                    Object obj = null;

                    //						zxx start
                    if (sql.indexOf("reportdate") == -1) {
                        if (count == 1) {
                            Object obj1 = null;
                            String a[] = sql.split("and");

                            for (int z = 0; z < a.length; z++) {
                                if (a[z].indexOf("tpinputid") != -1) {
                                    if (sql1.indexOf("tpinputid") == -1)
                                        sql1 = sql1 + a[z] + " and ";
                                }
                                if (a[z].indexOf("tpinputname") != -1) {
                                    if (sql1.indexOf("tpinputname") == -1)
                                        sql1 = sql1 + a[z] + " and ";
                                }
                                if (a[z].indexOf("createdep") != -1) {
                                    if (sql1.indexOf("createdep") == -1)
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

                            //System.out.println("88888888"+sql1+"99999999999999999999999");
                            try {
                                obj1 = baseDAO.executeSql(sql1);
                            } catch (Exception ex) {
                                logger.info("报错SQL3：" + sql1);
                            }
                            if (obj1 != null) {
                                List list1 = (List) obj1;
                                if (list1.size() > 0) {
                                    ss = list1.size();
                                }
                                //System.out.println("ss="+ss);
                            }
                            count = 0;
                        }
                        //zxx end
                    }


                    try {
                        obj = baseDAO.findNativeSQL(sql, null);

                    } catch (Exception ex) {
                        logger.info("报错SQL4：" + sql);
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
                                            //System.out.println("res="+res);
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
    private void getArraysNew(List<List<String>> dataList, double[] sumArray, double[] percentArray, double[] mark, double[] percentMark, double[] realArray, double[] finalArray, double coefficient, double[][] resArray, double[][] faultRateArray, double[][] serveArray, double[] evaluateArray, String equipType, String formId, double vendorfaultRateArray[], double[] hqEvaluateArray) {

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
                    System.out.println("########" + resArray[j][i + 1]);
                    System.out.println("########" + resArray[j][i]);
                    double percentValue = resArray[j][i + 1] / resArray[j][i];//各个省份每个厂家的故障率
                    System.out.println("~~~~~~~~~~~~~~~~````````" + percentValue);
                    EvaluationReference evaluationReference = findEvaluationReferenceById(Long.parseLong(formId));
                    System.out.println("MaxFaultrate=" + evaluationReference.getMaxFaultrate());
                    System.out.println("MinFaultrate=" + evaluationReference.getMinFaultrate());
                    if (!Double.isNaN(percentValue)) {
                        if (percentValue >= evaluationReference.getMaxFaultrate()) {
                            faultRateArray[j][i] = 0;
                        } else if (percentValue <= evaluationReference.getMinFaultrate()) {
                            faultRateArray[j][i] = 100;
                        } else {
                            faultRateArray[j][i] = 100 - 100 * (percentValue - evaluationReference.getMinFaultrate()) / (evaluationReference.getMaxFaultrate() - evaluationReference.getMinFaultrate());

                        }
                        System.out.println("@@@@@@@@@@@@@" + faultRateArray[j][i]);
                        serveArray[j][i] = resArray[j][i + 2]; // 每个省份的服务得分
                        if (equipType.contains("乙")) {
                            pvEvaluateArray[j][i] = faultRateArray[j][i] * 0.8 + serveArray[j][i] * 0.2;
                        }
                        if (equipType.contains("丙")) {//哈哈
                            pvEvaluateArray[j][i] = faultRateArray[j][i] * 0.95 + serveArray[j][i] * 0.05;

							/* cgliu//新增加的df.format(tt),ExportUtil.getBackfround(jxl.format.Colour.WHITE, 12)
							Label label = new Label(j,i,pvEvaluateArray[j][i]+"",ExportUtil.getBackfround(jxl.format.Colour.WHITE, 8));
							try {
								ws.addCell(label);
							} catch (RowsExceededException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (WriteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}*/
                            //把值写入到Excel中


                        }
                    }
                    hqEvaluateArray[i] += pvEvaluateArray[j][i];
                    vendorfaultRateArray[i] += faultRateArray[j][i];
                }
                /////////////////////////
            }
            if (t > 10) {
                mark[i] = 100;
            } else {
                mark[i] = t * 10;
            }
        }
		/* cgliu//新增加的
		try {
			wwb.write();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			wwb.close();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
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


    // 新增集采得分
    private void getArraysNew2(List<OrgEntity> orgList, WritableWorkbook wwb,
                               List<List<String>> dataList,
                               double[] sumArray, double[] percentArray, double[] mark, double[] percentMark,
                               double[] realArray, double[] finalArray, double coefficient, double[][] resArray,
                               double[][] faultRateArray, double[][] serveArray, double[] evaluateArray,
                               String equipType, String formId, double vendorfaultRateArray[], double[] hqEvaluateArray,
                               double[] hqGroupEvaluateArray, double[] groupEvaluateArray) {

        java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");


        //往第三个sheet页添加信息
        WritableSheet ws = wwb.getSheet(2);
        for (int k = 0; k < orgList.size(); k++) {
            Label label = new Label(k + 2, 0, orgList.get(k).getOrgName(), ExportUtil.getBackfround(jxl.format.Colour.WHITE, 8));
            try {
                ws.addCell(label);
            } catch (RowsExceededException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (WriteException e) {
                // TODO Auto-generated catch block
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
                ///////////////////////
                if ((i + 2) % coefficient == 0) {
                    if (Double.valueOf(dataList.get(j).get(i)) > 0) {  //判断省份是否使用该设备，根据设备数量
                        verNumArray[i]++;
                    }
                    System.out.println("########" + resArray[j][i + 1]);
                    System.out.println("########" + resArray[j][i]);
                    double percentValue = resArray[j][i + 1] / resArray[j][i];//各个省份每个厂家的故障率
                    System.out.println("~~~~~~~~~~~~~~~~````````" + percentValue);
                    EvaluationReference evaluationReference = findEvaluationReferenceById(Long.parseLong(formId));
                    System.out.println("MaxFaultrate=" + evaluationReference.getMaxFaultrate());
                    System.out.println("MinFaultrate=" + evaluationReference.getMinFaultrate());
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
                        System.out.println("@@@@@@@@@@@@@" + faultRateArray[j][i]);
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


                            if (pvEvaluateArray[j][i] > 1) {
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
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (WriteException e) {
                                // TODO Auto-generated catch block
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
            if (t > 10) {
                mark[i] = 100;
            } else {
                mark[i] = t * 10;
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
                        (orgList.size() - verNumArray[i]) * minGroupEvaluate[i]) / orgList.size() * 0.7;
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

                    evaluateArray[i] = hqEvaluateArray[i] - markValue[i];
                    if (evaluateArray[i] < 0) {
                        evaluateArray[i] = 0;
                    }
                }
            }
        }
    }


    public EvaluationReference findEvaluationReferenceById(long formId) {
        List<EvaluationReference> list = null;
        try {
            list = baseDAO.find("from EvaluationReference where formId=" + formId);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        EvaluationReference evaluationReference = new EvaluationReference();
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%list" + list.size());
        if (list != null && list.size() > 0) {
            evaluationReference = list.get(0);
        }
        return evaluationReference;
    }

    //poi 处理wwb
    private void dealSheetForEvaluationPoi(boolean allReplace, HSSFSheet sheet, List<OrgEntity> orgList, String reportDateStr, boolean isSearchHQData, String reportYear, long reportTmpId, String fileType) throws Exception {

        //强制执行sheet中的公式
        String pagename = sheet.getSheetName();//sheet 的name 对应pagename
        int rowCount = sheet.getPhysicalNumberOfRows();//行数
        for (int i = 0; i < rowCount; i++) {
            //行 循环
            System.out.print("~~~~~~~~~~~~~i" + i + "~~~~~~~~~~~~~~~~rowCount" + rowCount);
            HSSFRow row = sheet.getRow(i);
            int colCount = 0;
            if (row != null) {
                colCount = row.getLastCellNum(); //列数
            }

            for (int j = 0; j < colCount; j++) {//列 循环
                sheet.setForceFormulaRecalculation(true);
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~j=" + j);
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
                    String orgCodes = "";
                    for (OrgEntity org : orgList) {
                        orgCodes += "'" + org.getOrgCode() + "',";
                    }
                    if (orgCodes.endsWith(",")) {
                        orgCodes = orgCodes.substring(1, orgCodes.length() - 2);
                    }
                    if (!"".equals(fileType) && fileType != null && fileType.equals("2")) {//光缆
                        String depsForInQuery = "";
                        res = fromTempletContentToSqlResForEvaluationCable(content, orgCodes, reportDateStr, reportYear, pagename);
                    } else {
                        res = fromTempletContentToSqlResForEvaluation(allReplace, content, orgCodes, reportDateStr, reportYear, reportTmpId);
                    }
                    System.out.println("-------------" + res + "-查詢結果----------------------------------");

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


    @Override
    public void saveAutoGather() {
        logger.info("+++++++++++定时器开始执行！！！");
        String reportDateStr = "";
        /***********************************************************************
         * 我们会根据当前的月份判断汇总的季度值
         */
        // 时间处理 begin
        /*Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dateStrMon = df.format(date);
        String monthStr = dateStrMon.substring(5, 7);//当前月
        String yearStr = dateStrMon.substring(0, 4);//当前年
        String reportYear = yearStr;*/
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;//当前月
        int year = Calendar.getInstance().get(Calendar.YEAR);//当前年
        // 全年汇总的年份
        try {
            List<EvaluationReportTime> ertList = baseDAO.find("from EvaluationReportTime where deletedFlag=0");
            for (int i=0;i<ertList.size();i++) {
                // 总部季度汇总
                EvaluationReportTime ert = ertList.get(i);
                if (ert.getYwdepart().equals("120")) {
                   /* if (ert.getIsReport().equals("当前年度")) {//当前年度
                        bsaveExportExcelYear(year + "", "");
                    } else if (ert.getIsReport().equals("近四个季度")) {//近四个季度  todo
                        saveExportExcelKn(year + "", "-1");
                    } else if (ert.getIsReport().equals("当前季度")) {
                        if (month == 1 || month == 2 || month == 3) {//上年第四季度
                            reportDateStr = "第四季度";
                            year = year - 1;
                        }
                        if (month == 4 || month == 5 || month == 6) {//当年第一季度
                            reportDateStr = "第一季度";
                        }
                        if (month == 7 || month == 8 || month == 9) {//当年第二季度
                            reportDateStr = "第二季度";
                        }
                        if (month == 10 || month == 11 || month == 12) {//当年第三季度
                            reportDateStr = "第三季度";
                        }
                        bsaveExportExcelYear(year + "", reportDateStr);
                    } else {
                        String reportYear = ert.getIsReport().substring(0, 4);
                        reportDateStr = ert.getIsReport().substring(4, ert.getIsReport().length());
                        bsaveExportExcel(reportYear, reportDateStr);
                    }*/
                    if(month<7){
                        reportDateStr="下半年";
                        year =year-1;
                    }else {
                        reportDateStr="上半年";
                    }

                  /*  if (ert.getIsReport().equals("年度汇总")) {//当前年度
                        if(month<7){
                            year=Calendar.getInstance().get(Calendar.YEAR)-1;
                        }
                        bsaveExportExcelYear2(year + "", "全年");
                    } else if (ert.getIsReport().equals("上一周期")) {//近四个季度  todo
                        if("上半年".equals(reportDateStr)){
                            reportDateStr="下半年";
                            year=year-1;
                        }else {
                            reportDateStr="上半年";
                        }
                        bsaveExporthalfYear(year + "", reportDateStr);
                    }else if (ert.getIsReport().equals("前推第二周期")) {//近四个季度  todo
                        year=year-1;
                        bsaveExporthalfYear(year + "", reportDateStr);
                    } else if (ert.getIsReport().equals("当前周期")) {

                       bsaveExporthalfYear(year + "", reportDateStr);*/

                    //jw  替换 contains  ----  equals

                    if (ert.getIsReport().contains("年度汇总")) {//当前年度
                        if(month<7){
                            year=Calendar.getInstance().get(Calendar.YEAR)-1;
                        }
                        bsaveExportExcelYear2(year + "", "全年");
                    } else if (ert.getIsReport().contains("上一周期")) {//近四个季度  todo
                        if("上半年".equals(reportDateStr)){
                            reportDateStr="下半年";
                            year=year-1;
                        }else {
                            reportDateStr="上半年";
                        }
                        bsaveExporthalfYear(year + "", reportDateStr);
                    }else if (ert.getIsReport().contains("前推第二周期")) {//近四个季度  todo
                        year=year-1;
                        bsaveExporthalfYear(year + "", reportDateStr);
                    } else if (ert.getIsReport().contains("当前周期")) {

                        bsaveExporthalfYear(year + "", reportDateStr);
                    }/*else if (ert.getIsReport().contains("上一年度汇总")) {                               //===增加上一年度
                        if(month<7){
                            year=Calendar.getInstance().get(Calendar.YEAR)-2;
                        }else {
                            year=Calendar.getInstance().get(Calendar.YEAR)-1;
                        }
                        bsaveExportExcelYear2(year + "", "上一年度");
                        //jw  替换 contains  ----  equals
                    }*/ else {
                        String reportYear = ert.getIsReport().substring(0, 4);
                        reportDateStr = ert.getIsReport().substring(4, ert.getIsReport().length());
                        bsaveExportExcel(reportYear, reportDateStr);
                    }


                    ert.setDeletedFlag(true);
                    logger.info("EvaluationReportTime"+ert.getObjectID());
                    baseDAO.saveOrUpdate(ert);
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void timeoutAlert() {
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        month = 1;
        //int month = 1;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        List<EemTempEntity> eemTempEntityList = null;
        String hql = "";
        try {
            if (month == 1) {//上一年的第四季度
                year = year - 1;
                eemTempEntityList = baseDAO.find("select new EemTempEntity(objectId,tempName) from EemTempEntity where deletedFlag=0 and tempType=1");
                hql = "from ExcelPage where deletedFlag=0 and reportYear='" + year + "' and (reportDate='第四季度' or reportDate='下半年')";
            }
            if (month == 4) {//当年的第一季度
                eemTempEntityList = baseDAO.find("select new EemTempEntity(objectId,tempName) from EemTempEntity where deletedFlag=0 and tempType=1 and reportedFrequency=1");
                hql = "from ExcelPage where deletedFlag=0 and reportYear='" + year + "' and reportDate='第一季度'";
            }
            if (month == 7) {//当年的第二季度
                eemTempEntityList = baseDAO.find("select new EemTempEntity(objectId,tempName) from EemTempEntity where deletedFlag=0 and tempType=1");

                hql = "from ExcelPage where deletedFlag=0 and reportYear='" + year + "' and (reportDate='第二季度' or reportDate='上半年')";
            }
            if (month == 10) {//当年的第三季度
                eemTempEntityList = baseDAO.find("select new EemTempEntity(objectId,tempName) from EemTempEntity where deletedFlag=0 and tempType=1 and reportedFrequency=1");
                hql = "from ExcelPage where deletedFlag=0 and reportYear='" + year + "' and reportDate='第三季度'";
            }


            List<OrgEntity> orgList = new ArrayList<OrgEntity>();
            List<OrgEntity> orgEntityList = AAAAAdapter.getInstence().findOrgListAndSelfByParentID(1L, false);
            for (OrgEntity orgEntity : orgEntityList) {
                if (orgEntity.getOrgCode().startsWith("1") || orgEntity.getOrgCode().startsWith("81") || orgEntity.getOrgCode().startsWith("51")) {
                    continue;
                } else {
                    orgList.add(orgEntity);
                    List<OrgEntity> cityEntityList = AAAAAdapter.getInstence().findOrgListAndSelfByParentID(orgEntity.getOrgId(), false);
                    for (OrgEntity cityEntity : cityEntityList) {
                        if ("BB".equals(cityEntity.getShortName())) {
                            continue;
                        } else {
                            orgList.add(cityEntity);
                        }
                    }
                }
            }
            Map<String, ExcelPage> orgEntityMap = new HashMap<String, ExcelPage>();
            List<ExcelPage> excelPageList = baseDAO.find(hql.toString());
            for (ExcelPage excelPage : excelPageList) {
                orgEntityMap.put(excelPage.getReportOrgCode() + "$" + excelPage.getTpInputID(), excelPage);

            }
            List<String> dataList = new ArrayList<String>();
            for (EemTempEntity eemTempEntity : eemTempEntityList) {
                for (OrgEntity orgEntity : orgList) {
                    if (orgEntityMap.get(orgEntity.getOrgCode() + "$" + eemTempEntity.getObjectId()) != null) {
                        logger.info("数据已经上报");
                    } else {
                        dataList.add(orgEntity.getOrgCode() + "$" + orgEntity.getOrgName() + "$" + eemTempEntity.getTempName());
                    }
                }
            }
            for (String data : dataList) {
                Map<String, String> paramsMap = new HashMap<String, String>();
                paramsMap.put("specialty", "ALL");
                paramsMap.put("process", Constants.PROCESS_MODEL_NAME);
                String[] s = data.split("\\$");
               // paramsMap.put("orgID", AAAAAdapter.getInstence().findOrgByOrgCode(data.split("$")[0]).getOrgId().toString());
                paramsMap.put("orgID", AAAAAdapter.getInstence().findOrgByOrgCode(s[0]).getOrgId().toString());
                paramsMap.put("node", "reportData");
                paramsMap.put("flag", "true");
                String result = HttpClientUtil.sendPostRequestByJava(Constants.POWERURL + "/powerController.do?method=findNextParticipant4", paramsMap);
                List participants = JSON.parseArray(result);
                List<Participant> participantList = new ArrayList<Participant>();
                for (Object object : participants) {
                    Participant p = new Participant();
                    p.setParticipantID(object.toString());
                    p.setParticipantName("");
                    p.setParticipantType("1");
                    participantList.add(p);
                    SendAdapter.sentMessageToDo(Constants.MODEL_NAME, data.split("\\$")[1] + data.split("\\$")[2], data.split("\\$")[1] + data.split("\\$")[2], participantList, -1L, -1L);
                }
            }
        } catch (DAOException e) {
            e.printStackTrace();
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveAppoint() {
        List<EvaluationCollectTime> collectTimeList = null;
        try {
            collectTimeList = baseDAO.find("from EvaluationCollectTime where deletedFlag=0 order by createDate");
            for(int i=0;i<(collectTimeList.size()>2?2:collectTimeList.size());i++){

               final  EvaluationCollectTime collectTime = collectTimeList.get(i);
                collectTime.setDeletedFlag(true);
                baseDAO.saveOrUpdate(collectTime);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            EemTempEntity summaryTemp = eemTemplateService.findTempByID(collectTime.getTempID());
                            UserEntity userEntity = AAAAAdapter.findUserByPortalAccountId(collectTime.getCreatedUserName());
                            EemTempEntity repotTemp = eemTemplateService.findTempByID(collectTime.getReportTempID());
                            OutputStream os = null;
                            String filePathName = "";
                            long fileDataTid = 1L;
                            String fileName = summaryTemp.getTempName();
                            String name = "";

                            String filePath = "";
                            if (repotTemp.getLevel() == 5) {//光缆
                                fileDataTid = baseDAO.getSequenceNextValue(EvaluationFileData.class);
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
                            String excelFilePath = eemSummaryService.saveZBCollectData(collectTime.getReportYear(), collectTime.getReportDate(), summaryTemp,userEntity , collectTime.getProvinceCodes(), collectTime.getReportTempID().toString(), collectTime.getProvinceNames());

                            if (os != null) {
                                os.flush();
                                os.close();
                            }
//                            if (repotTemp.getLevel() == 2) {
//                                byte[] byteArray = null;
//                                File fl = new File(filePathName);
//                                FileInputStream fis = new FileInputStream(fl);
//                                ByteArrayOutputStream bos = new ByteArrayOutputStream(10000);
//                                byte[] b = new byte[1024];
//                                int n;
//                                while ((n = fis.read(b)) != -1) {
//                                    bos.write(b, 0, n);
//                                }
//                                fis.close();
//                                bos.close();
//                                byteArray = bos.toByteArray();
//                                eemSummaryService.saveEvaluationFileData(fileDataTid, filePath, fileName, "120", userEntity);
//
//                                eemReportService.importEvaluationFileDataForCable(fileDataTid,
//                                        byteArray, repotTemp, userEntity, "120", collectTime.getReportDate(), collectTime.getReportYear());
//                            }

                        } catch (ServiceException e1) {
                            e1.printStackTrace();
                        } catch (PaasAAAAException e1) {
                            e1.printStackTrace();
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (DAOException e1) {
                            e1.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();


            }
        } catch (DAOException e) {
            e.printStackTrace();
        }

    }


    //省份汇总
    public void saveExportExcelForAll(String ywdepart, String name,
                                      String reportYear, String reportDateStr, int monthInt, UserEntity user) throws PaasAAAAException, DAOException {

        System.out.println("开始省份汇总地市数据");

        // String conditions = " isprovince='1' and ywdepart!='YW01060138' and
        // ywdepart!='YW01060137' ";

        // 获得省份的信息
        // List<SecWebGroupinfo> PList = ibatisDao.query("getGroupListWithType",
        // conditions);


        // EvaluationService evaluationService = (EvaluationService) this
        // .getBean("evaluationService");
        // SecWebUserinfo user = (SecWebUserinfo) request.getSession()
        // .getAttribute(Constants.USERINFO);
        // String ywdepart=user.getYwDepart();
        // String withdraw=request.getParameter("withdraw");
        // 之前是页面传入的formid

        // String path="G:"+File.separator+ywdepart+File.separator;;

//        String path = File.separator + "202NFSShare" + File.separator
//                + "eomsData" + File.separator + "attachment" + File.separator
//                + "meoms" + File.separator + "HPJDownload" + File.separator
//                + ywdepart + File.separator;
        String path = EemConstants.GATHER_DATA_PATH + user.getOrgEntity().getOrgName() + "/";
        // 删除当前季度的服务器文件
        deleteAllFileJd(path, name, reportYear,
                reportDateStr);

        String year = String.valueOf((Integer.parseInt(reportYear) - 1));
        // 删除上一年该季度的值
        deleteAllFileJd(path, name, year,
                reportDateStr);

        // 新增加的方法
        // 删除当前季度和上一年该季度的值
        deleteDqjd(name, reportYear, reportDateStr);
        // 删除上一年当前季度的值
        deleteDqjd(name, year, reportDateStr);

        String formid = "";
        String formName = "";
        String formIdStr = "";


        String allName = "";
        // 路径 /202NFSShare/eomsData/attachment/meoms/HPJDownload

        String pathname = "";

        List<OrgEntity> depList = AAAAAdapter.findOrgListByParentID(Integer.parseInt(AAAAAdapter.getInstence().findOrgByOrgCode(ywdepart).getOrgId().toString()));
        String citys = "";
        for (OrgEntity org : depList) {
            citys += org.getOrgId() + ",";
        }
//        citys = citys.substring(0, citys.lastIndexOf(","));
        if (citys.endsWith(",")) {
            citys = citys.substring(0, citys.length() - 1);
        }


        String fileid = "";
        int count = 0;
        for (int i = 0; i < gatherTempletList.size(); i++) {

            EemTempEntity repotEntity = findReportEemTempEntity(gatherTempletList.get(0).getObjectId());
            if (repotEntity == null) {
                continue;
            }
            EvaluationReportExcel ere = new EvaluationReportExcel();// 保存各个表的信息
//            reportDateStr = init;
            count++;
            Date newUpdate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
            String fileDate = sdf.format(newUpdate);


            // 光缆测试和蝶形光缆测试时间显示
            if (formid.equals("6")) {
                if (monthInt >= 1 && monthInt <= 6) {
                    reportDateStr = "上半年";
                }
                if (monthInt >= 7 && monthInt <= 12) {
                    reportDateStr = "下半年";
                }

            }
            // 模板名称
            String excelName = reportYear + reportDateStr + gatherTempletList.get(i).getTempName()
                    + ".xls";
            // 文件的保存路径
            pathname = path + excelName;


//            File file = new File(pathname);
            logger.info("++++++++++++++path:" + path);
            // 向数据库插入信息 start
            long obj = baseDAO.getSequenceNextValue(EvaluationReportExcel.class);
            ere.setObjectId(obj);
            ere.setDep(name);
            ere.setExcelName(excelName);
            ere.setExcelPath(path);
            ere.setFormId(gatherTempletList.get(i).getObjectId());
            ere.setCreationTime(new Timestamp(newUpdate.getTime()));
            ere.setReportYear(reportYear);
            ere.setReportDate(reportDateStr);
            ere.setFileName(excelName);// 设置新由时间生成的名字
            ere.setDeletedFlag(false);
            try {
                logger.info("预约汇总excle:"+ere.getObjectId()+"日期："+ere.getReportYear()+"--------"+ere.getReportDate());
                baseDAO.saveOrUpdate(ere);
            } catch (DAOException e) {
                e.printStackTrace();
            }
            // end

            HSSFWorkbook wwb = downGatherData(reportYear, reportYear, gatherTempletList.get(i), repotEntity, citys, user);
            HSSFFormulaEvaluator.evaluateAllFormulaCells(wwb);


            OutputStream os = null;
            try {
                os = new FileOutputStream(pathname);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                wwb.write(os);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                os.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                os.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    // 总部汇总省份（季度）
    public void bsaveExportExcel(String reportYear, String reportDateStr) throws Exception {

        /*List<OrgEntity> orgList = new ArrayList<OrgEntity>();
        List<OrgEntity> orgEntityList = AAAAAdapter.getInstence().findOrgListAndSelfByParentID(1L, false);
        for (OrgEntity orgEntity : orgEntityList) {
            if (orgEntity.getOrgCode().startsWith("1") || orgEntity.getOrgCode().startsWith("81") || orgEntity.getOrgCode().startsWith("51")) {
                continue;
            } else {
                orgList.add(orgEntity);
            }
        }*/
        String path = EemConstants.ORDER_DATA_PATH + File.separator + "120" + File.separator;
        File dirFile = new File(path);
        if(!dirFile.exists()){
            dirFile.mkdirs();
        }
        // 删除指定路径下的所有文件
        // excelService.delAllFile(path);
        // 删除当前季度的服务器文件，jide fangkai
        deleteAllFileJd(path, "总部", reportYear,
                reportDateStr);

        // 删除上一年的该季度的值
        String year = String.valueOf((Integer.parseInt(reportYear) - 1));
        // 记得放开
        deleteAllFileJd(path, "总部", year,
                reportDateStr);

        // 删除当前季度数据库的值
        deleteDqjd("总部", reportYear, reportDateStr);
        // 删除上一年当前季度的值
//        deleteDqjd("总部", year, reportDateStr);

        // 获得所有的汇总模板
        gatherTempletList = eemCommonService.findTempList2("sum", null);


        OutputStream os = null;
        WritableWorkbook wwb = null;
        String pathname = "";
        // 循环所有的汇总模板
        for (EemTempEntity et : gatherTempletList) {
            if (et.getReportedFrequency() == 2) {
                continue;
            }
            EvaluationReportExcel ere = new EvaluationReportExcel();// 保存各个表的信息
            logger.info(reportYear + "++++++" + reportDateStr + "++++++" + et.getTempName());
            String index = "";
            // 模板名称
            String excelName = reportYear + reportDateStr + et.getTempName()
                    + ".xls";
            // 文件的保存路径
            pathname = path + excelName;
            ere.setDep("总部");
            long objectId = baseDAO.getSequenceNextValue(EvaluationReportExcel.class);
            logger.info(EvaluationReportExcel.class + "+++==+++" + objectId);
            ere.setObjectId(objectId);
            ere.setExcelName(excelName);
            ere.setExcelPath(path);
            ere.setFormId(et.getObjectId());
            ere.setCreationTime(new Timestamp(System.currentTimeMillis()));
            ere.setFileName(excelName);
            ere.setReportYear(reportYear);
            ere.setReportDate(reportDateStr);
            ere.setDeptsWithDraw(index);
            ere.setOperUserTrueName("系统自动");
            ere.setOperUserPhone("无");
            ere.setTpInputName(et.getTempName());
            ere.setDeletedFlag(false);
            os = new FileOutputStream(pathname);
            EemTempEntity rTempEntity = findReportEemTempEntity(et.getObjectId());
            wwb = eemSummaryService.fromDBByteArrayToTable(et.getObjectId(),et.getTemplateExcelByteData().getUploadFileData(), os, reportDateStr, "", reportYear, rTempEntity.getObjectId().toString(),false);
            wwb.write();
            wwb.close();
            os.flush();
            os.close();
            File file  = new File(pathname);
            FileAdapter fileAdapter = FileAdapter.getInstance();
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
            String attachmentId = fileAdapter.upload("",null, dataInputStream);
            dataInputStream.close();
            ere.setAttachmentId(attachmentId);
            baseDAO.saveOrUpdate(ere);
            file.delete();
        }

    }


    // 总部汇总省份全年
    public void bsaveExportExcelYear(String reportYear, String reportDateStr) throws Exception {


        // 获得所有的汇总模板
        gatherTempletList = eemCommonService.findTempList2("sum", null);


        // 汇总模板formid
        String formIdStr = "";
        OutputStream os = null;
        WritableWorkbook wwb = null;
        /*List<OrgEntity> orgList = new ArrayList<OrgEntity>();
        List<OrgEntity> orgEntityList = AAAAAdapter.getInstence().findOrgListAndSelfByParentID(1L, false);
        for (OrgEntity orgEntity : orgEntityList) {
            if (orgEntity.getOrgCode().startsWith("1") || orgEntity.getOrgCode().startsWith("81") || orgEntity.getOrgCode().startsWith("51")) {
                continue;
            } else {
                orgList.add(orgEntity);
            }
        }*/
        String path = EemConstants.ORDER_DATA_PATH +File.separator+ "120" + File.separator;
        String pathname = "";
        // 删除指定路径下的所有文件
        // excelService.delAllFile(path);

        // 删除指定路径下的所有文件
        // excelService.delAllFile(path);
        // 删除当前季度的服务器文件,jidefangkai
        deleteAllFileJd(path, "总部", reportYear,
                "全年");

		/*
		 * List<EvaluationReportExcel> list =null;
		 * list=evaluationReportFormService.findAllEvaluationReportExcelHq();
		 * if(list.size()>0){ for(EvaluationReportExcel ere:list){ //删除表中上一季度的信息
		 * try{ evaluationReportFormService.delete(ere); } catch(Exception e){
		 * e.printStackTrace(); } } }
		 */
        // 删除当前季度数据库的值
        deleteDqjd("总部", reportYear, "全年");

//        List<EvaluationTemplet> listEt = null;
//        String formId = "";
//        List<EvaluationWithDrawForReport> listEwdfr = null;
        String reportDate = "";
        for (EemTempEntity et : gatherTempletList) {
            EemTempEntity reportTemmp = findReportEemTempEntity(et.getObjectId());
            if (reportTemmp == null) {//如果汇总模板未关联上报模板则不进行汇总计算
                continue;
            }
            if(org.apache.commons.lang3.StringUtils.isBlank(reportDateStr)){//全年
                reportDate = reportDateStr;
            }else{
                if (et.getTempPattern() == 2) {
                    if("第一季度".equals(reportDateStr)||("第二季度").equals(reportDateStr)){
                        reportDate = "上半年";
                    }else{
                        reportDate = "下半年";
                    }
                }else{
                    reportDate = reportDateStr;
                }
            }

            EvaluationReportExcel ere = new EvaluationReportExcel();// 保存各个表的信息
            String index = "";

            // 模板名称
            String excelName = reportYear + "_全年" + et.getTempName()
                    + ".xls";
            // 文件的保存路径
            pathname = path + excelName;
            ere.setDep("总部");
            ere.setExcelName(excelName);
            ere.setExcelPath(path);
            ere.setFormId(et.getObjectId());
            ere.setCreationTime(new Timestamp(System.currentTimeMillis()));
            ere.setReportYear(reportYear);
            ere.setReportDate("全年");
            ere.setFileName(excelName);
            ere.setDeptsWithDraw(index);
            ere.setOperUserTrueName("系统自动");
            ere.setOperUserPhone("无");
            ere.setTpInputName(et.getTempName());
            Long reportExcelId = baseDAO.getSequenceNextValue(EvaluationReportExcel.class);
            ere.setObjectId(reportExcelId);
            os = new FileOutputStream(pathname);
            wwb = eemSummaryService.fromDBByteArrayToTable(et.getObjectId(),et.getTemplateExcelByteData().getUploadFileData(), os, reportDate, "", reportYear, et.getEemTempEntity().getObjectId().toString(),false);
            wwb.write();
            wwb.close();
            os.flush();
            os.close();
            File file  = new File(pathname);
            FileAdapter fileAdapter = FileAdapter.getInstance();
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
            String attachmentId = fileAdapter.upload("",null, dataInputStream);
            dataInputStream.close();
            ere.setAttachmentId(attachmentId);
            baseDAO.saveOrUpdate(ere);
            file.delete();
        }

    }


    // 总部汇总省份上一年度
    public void bsaveExportExcelLastYear(String reportYear,String reportDateStr) throws Exception{

        // 获得所有的汇总模板
        List<EemTempEntity>  gatherTempletList = eemCommonService.findTempList2("sum", null);


        // 汇总模板formid
        String formIdStr = "";
        OutputStream os = null;
        WritableWorkbook wwb = null;
        /*List<OrgEntity> orgList = new ArrayList<OrgEntity>();
        List<OrgEntity> orgEntityList = AAAAAdapter.getInstence().findOrgListAndSelfByParentID(1L, false);
        for (OrgEntity orgEntity : orgEntityList) {
            if (orgEntity.getOrgCode().startsWith("1") || orgEntity.getOrgCode().startsWith("81") || orgEntity.getOrgCode().startsWith("51")) {
                continue;
            } else {
                orgList.add(orgEntity);
            }
        }*/
        String path = EemConstants.ORDER_DATA_PATH +File.separator+ "120" + File.separator;
        String pathname = "";
        // 删除指定路径下的所有文件
        // excelService.delAllFile(path);

        // 删除指定路径下的所有文件
        // excelService.delAllFile(path);
        // 删除当前季度的服务器文件,jidefangkai
        deleteAllFileJd(path, "总部", reportYear,
                "全年");

        /*
         * List<EvaluationReportExcel> list =null;
         * list=evaluationReportFormService.findAllEvaluationReportExcelHq();
         * if(list.size()>0){ for(EvaluationReportExcel ere:list){ //删除表中上一季度的信息
         * try{ evaluationReportFormService.delete(ere); } catch(Exception e){
         * e.printStackTrace(); } } }
         */
        // 删除当前季度数据库的值
        deleteDqjd("总部", reportYear, "全年");

//        List<EvaluationTemplet> listEt = null;
//        String formId = "";
//        List<EvaluationWithDrawForReport> listEwdfr = null;
        String reportDate = "";
        for (EemTempEntity et : gatherTempletList) {
            try{
                EemTempEntity reportTemmp = findReportEemTempEntity(et.getObjectId());
                if (reportTemmp == null) {//如果汇总模板未关联上报模板则不进行汇总计算
                    continue;
                }
//            if(org.apache.commons.lang3.StringUtils.isBlank(reportDateStr)){//全年
//                reportDate = reportDateStr;
//            }else{
//                if (et.getTempPattern() == 2) {
//                    if("第一季度".equals(reportDateStr)||("第二季度").equals(reportDateStr)){
//                        reportDate = "上半年";
//                    }else{
//                        reportDate = "下半年";
//                    }
//                }else{
                reportDate = reportDateStr;
//                }
//            }

                EvaluationReportExcel ere = new EvaluationReportExcel();// 保存各个表的信息
                String index = "";
                // 模板名称
                //jw   判断是否为跨年模板====================================
            /*  String excelName="";
              int month = Calendar.getInstance().get(Calendar.MONTH) + 1;

              if(reportDateStr.contains("上年")){
                  if(month>7){

                      excelName = reportYear+""+ "_全年" + et.getTempName()
                              + ".xls";
                  }else{
                      int LYear2=Integer.parseInt(reportYear)+1;

                      excelName = reportYear+ "下半年" +LYear2+""+ "上半年"+ et.getTempName()
                              + ".xls";
                  }
              }else{
                  if(month>7){
                      int  year2=Calendar.getInstance().get(Calendar.YEAR)-1;
                      int  year3=year2+1;
                      excelName = year2+ "下半年" +year3+""+ "上半年"+ et.getTempName()
                              + ".xls";

                  }else{
                      int  year1=Calendar.getInstance().get(Calendar.YEAR)-1;
                      excelName = year1 + "_全年" + et.getTempName()
                              + ".xls";

                  }

              }*/
                //////////////////jw3.20=================================
                // 模板名称
                String excelName = reportYear + "_全年" + et.getTempName()
                        + ".xls";
                // 文件的保存路径
                pathname = path + excelName;
                ere.setDep("总部");
                ere.setExcelName(excelName);
                ere.setExcelPath(path);
                ere.setFormId(et.getObjectId());
                ere.setCreationTime(new Timestamp(System.currentTimeMillis()));
                ere.setReportYear(reportYear);
                ere.setReportDate("全年");
                ere.setFileName(excelName);
                ere.setDeptsWithDraw(index);
                ere.setOperUserTrueName("系统自动");
                ere.setOperUserPhone("无");
                ere.setTpInputName(et.getTempName());
                Long reportExcelId = baseDAO.getSequenceNextValue(EvaluationReportExcel.class);
                ere.setObjectId(reportExcelId);
                os = new FileOutputStream(pathname);
                wwb = eemSummaryService.fromDBByteArrayToTable(et.getObjectId(),et.getTemplateExcelByteData().getUploadFileData(), os, reportDate, "", reportYear, reportTemmp.getObjectId().toString(),true);
                wwb.write();
                wwb.close();
                os.flush();
                os.close();
                File file  = new File(pathname);
                FileAdapter fileAdapter = FileAdapter.getInstance();
                DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
                String attachmentId = fileAdapter.upload("",null, dataInputStream);
                dataInputStream.close();
                ere.setAttachmentId(attachmentId);
                logger.info("预约汇总excle:"+ere.getObjectId()+"日期："+ere.getReportYear()+"--------"+ere.getReportDate());
                baseDAO.saveOrUpdate(ere);
                file.delete();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    // 总部汇总省份全年
    public void bsaveExportExcelYear2(String reportYear, String reportDateStr) throws Exception {


        // 获得所有的汇总模板
        List<EemTempEntity>  gatherTempletList = eemCommonService.findTempList2("sum", null);


        // 汇总模板formid
        String formIdStr = "";
        OutputStream os = null;
        WritableWorkbook wwb = null;
        /*List<OrgEntity> orgList = new ArrayList<OrgEntity>();
        List<OrgEntity> orgEntityList = AAAAAdapter.getInstence().findOrgListAndSelfByParentID(1L, false);
        for (OrgEntity orgEntity : orgEntityList) {
            if (orgEntity.getOrgCode().startsWith("1") || orgEntity.getOrgCode().startsWith("81") || orgEntity.getOrgCode().startsWith("51")) {
                continue;
            } else {
                orgList.add(orgEntity);
            }
        }*/
        String path = EemConstants.ORDER_DATA_PATH +File.separator+ "120" + File.separator;
        String pathname = "";
        // 删除指定路径下的所有文件
        // excelService.delAllFile(path);

        // 删除指定路径下的所有文件
        // excelService.delAllFile(path);
        // 删除当前季度的服务器文件,jidefangkai
        deleteAllFileJd(path, "总部", reportYear,
                "全年");

		/*
		 * List<EvaluationReportExcel> list =null;
		 * list=evaluationReportFormService.findAllEvaluationReportExcelHq();
		 * if(list.size()>0){ for(EvaluationReportExcel ere:list){ //删除表中上一季度的信息
		 * try{ evaluationReportFormService.delete(ere); } catch(Exception e){
		 * e.printStackTrace(); } } }
		 */
        // 删除当前季度数据库的值
        deleteDqjd("总部", reportYear, "全年");

//        List<EvaluationTemplet> listEt = null;
//        String formId = "";
//        List<EvaluationWithDrawForReport> listEwdfr = null;
        String reportDate = "";
        for (EemTempEntity et : gatherTempletList) {
          try{
              EemTempEntity reportTemmp = findReportEemTempEntity(et.getObjectId());
              if (reportTemmp == null) {//如果汇总模板未关联上报模板则不进行汇总计算
                  continue;
              }
//            if(org.apache.commons.lang3.StringUtils.isBlank(reportDateStr)){//全年
//                reportDate = reportDateStr;
//            }else{
//                if (et.getTempPattern() == 2) {
//                    if("第一季度".equals(reportDateStr)||("第二季度").equals(reportDateStr)){
//                        reportDate = "上半年";
//                    }else{
//                        reportDate = "下半年";
//                    }
//                }else{
              reportDate = reportDateStr;
//                }
//            }

              EvaluationReportExcel ere = new EvaluationReportExcel();// 保存各个表的信息
              String index = "";
              // 模板名称
              //jw   判断是否为跨年模板====================================
            /*  String excelName="";
              int month = Calendar.getInstance().get(Calendar.MONTH) + 1;

              if(reportDateStr.contains("上年")){
                  if(month>7){

                      excelName = reportYear+""+ "_全年" + et.getTempName()
                              + ".xls";
                  }else{
                      int LYear2=Integer.parseInt(reportYear)+1;

                      excelName = reportYear+ "下半年" +LYear2+""+ "上半年"+ et.getTempName()
                              + ".xls";
                  }
              }else{
                  if(month>7){
                      int  year2=Calendar.getInstance().get(Calendar.YEAR)-1;
                      int  year3=year2+1;
                      excelName = year2+ "下半年" +year3+""+ "上半年"+ et.getTempName()
                              + ".xls";

                  }else{
                      int  year1=Calendar.getInstance().get(Calendar.YEAR)-1;
                      excelName = year1 + "_全年" + et.getTempName()
                              + ".xls";

                  }

              }*/
              //////////////////jw3.20=================================
              // 模板名称
            String excelName = reportYear + "_全年" + et.getTempName()
                      + ".xls";
              // 文件的保存路径
              pathname = path + excelName;
              ere.setDep("总部");
              ere.setExcelName(excelName);
              ere.setExcelPath(path);
              ere.setFormId(et.getObjectId());
              ere.setCreationTime(new Timestamp(System.currentTimeMillis()));
              ere.setReportYear(reportYear);
              ere.setReportDate("全年");
              ere.setFileName(excelName);
              ere.setDeptsWithDraw(index);
              ere.setOperUserTrueName("系统自动");
              ere.setOperUserPhone("无");
              ere.setTpInputName(et.getTempName());
              Long reportExcelId = baseDAO.getSequenceNextValue(EvaluationReportExcel.class);
              ere.setObjectId(reportExcelId);
              os = new FileOutputStream(pathname);
              wwb = eemSummaryService.fromDBByteArrayToTable(et.getObjectId(),et.getTemplateExcelByteData().getUploadFileData(), os, reportDate, "", reportYear, reportTemmp.getObjectId().toString(),true);
              wwb.write();
              wwb.close();
              os.flush();
              os.close();
              File file  = new File(pathname);
              FileAdapter fileAdapter = FileAdapter.getInstance();
              DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
              String attachmentId = fileAdapter.upload("",null, dataInputStream);
              dataInputStream.close();
              ere.setAttachmentId(attachmentId);
              logger.info("预约汇总excle:"+ere.getObjectId()+"日期："+ere.getReportYear()+"--------"+ere.getReportDate());
              baseDAO.saveOrUpdate(ere);
              file.delete();
          }catch (Exception e){
              e.printStackTrace();
          }

        }

    }

    // 总部汇总省份全年
    public void bsaveExporthalfYear(String reportYear, String reportDateStr)  {


        // 获得所有的汇总模板
        try {
            List<EemTempEntity> gatherTempletList = eemCommonService.findTempList2("sum", null);
            // 汇总模板formid
            String formIdStr = "";
            OutputStream os = null;
            WritableWorkbook wwb = null;
        /*List<OrgEntity> orgList = new ArrayList<OrgEntity>();
        List<OrgEntity> orgEntityList = AAAAAdapter.getInstence().findOrgListAndSelfByParentID(1L, false);
        for (OrgEntity orgEntity : orgEntityList) {
            if (orgEntity.getOrgCode().startsWith("1") || orgEntity.getOrgCode().startsWith("81") || orgEntity.getOrgCode().startsWith("51")) {
                continue;
            } else {
                orgList.add(orgEntity);
            }
        }*/
            String path = EemConstants.ORDER_DATA_PATH +File.separator+ "120" + File.separator;
            String pathname = "";
            // 删除指定路径下的所有文件
            // excelService.delAllFile(path);

            // 删除指定路径下的所有文件
            // excelService.delAllFile(path);
            // 删除当前季度的服务器文件,jidefangkai
            deleteAllFileJd(path, "总部", reportYear,
                    reportDateStr);

		/*
		 * List<EvaluationReportExcel> list =null;
		 * list=evaluationReportFormService.findAllEvaluationReportExcelHq();
		 * if(list.size()>0){ for(EvaluationReportExcel ere:list){ //删除表中上一季度的信息
		 * try{ evaluationReportFormService.delete(ere); } catch(Exception e){
		 * e.printStackTrace(); } } }
		 */
            // 删除当前季度数据库的值
            deleteDqjd("总部", reportYear, reportDateStr);

//        List<EvaluationTemplet> listEt = null;
//        String formId = "";
//        List<EvaluationWithDrawForReport> listEwdfr = null;
            String reportDate = "";
            int num=0;
            for (int i =0;i<gatherTempletList.size();i++) {
              try {
                  EemTempEntity reportTemmp = findReportEemTempEntity(gatherTempletList.get(i).getObjectId());
                  if (reportTemmp == null) {//如果汇总模板未关联上报模板则不进行汇总计算
                      continue;
                  }
                  logger.info(i+"+++++++++++++++++++++++++"+num+"----------------------"+gatherTempletList.get(i).getShortName()+"------"+gatherTempletList.get(i).getObjectId());
                  if(org.apache.commons.lang3.StringUtils.isBlank(reportDateStr)){//全年
                      reportDate = reportDateStr;
                  }else{
//                if (et.getTempPattern() == 2) {
//                    if("第一季度".equals(reportDateStr)||("第二季度").equals(reportDateStr)){
//                        reportDate = "上半年";
//                    }else{
//                        reportDate = "下半年";
//                    }
//                }else{
                      reportDate = reportDateStr;
//                }
                  }

                  EvaluationReportExcel ere = new EvaluationReportExcel();// 保存各个表的信息
                  String index = "";

                  // 模板名称
                  String excelName = reportYear + "_"+reportDate + gatherTempletList.get(i).getTempName()
                          + ".xls";
                  // 文件的保存路径
                  pathname = path + excelName;
                  ere.setDep("总部");
                  ere.setExcelName(excelName);
                  ere.setExcelPath(path);
                  ere.setFormId(gatherTempletList.get(i).getObjectId());
                  ere.setCreationTime(new Timestamp(System.currentTimeMillis()));
                  ere.setReportYear(reportYear);
                  ere.setReportDate(reportDate);
                  ere.setFileName(excelName);
                  ere.setDeptsWithDraw(index);
                  ere.setOperUserTrueName("系统自动");
                  ere.setOperUserPhone("无");
                  ere.setTpInputName(gatherTempletList.get(i).getTempName());
                  Long reportExcelId = baseDAO.getSequenceNextValue(EvaluationReportExcel.class);
                  ere.setObjectId(reportExcelId);
                  os = new FileOutputStream(pathname);
                  String ss="";
                  if(gatherTempletList.get(i).getEemTempEntity()!=null&&gatherTempletList.get(i).getEemTempEntity().getObjectId()!=null){
                      ss=gatherTempletList.get(i).getEemTempEntity().getObjectId().toString();
                  }
                  wwb = eemSummaryService.fromDBByteArrayToTable(gatherTempletList.get(i).getObjectId(),gatherTempletList.get(i).getTemplateExcelByteData().getUploadFileData(), os, reportDate, "", reportYear, ss,true);
                  wwb.write();
                  logger.info("+++++++++++++++++++++++++" + i + "------" + (++num) + "----------------------" + gatherTempletList.get(i).getShortName() + "------" + gatherTempletList.get(i).getObjectId());
                  wwb.close();
                  os.flush();
                  os.close();
                  File file  = new File(pathname);
                  FileAdapter fileAdapter = FileAdapter.getInstance();
                  DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
                  String attachmentId = fileAdapter.upload("",null, dataInputStream);
                  dataInputStream.close();
                  ere.setAttachmentId(attachmentId);
                  logger.info(attachmentId+"预约汇总excle:"+ere.getObjectId()+"日期："+ere.getReportYear()+"--------"+ere.getReportDate());
                  baseDAO.saveOrUpdate(ere);
                  file.delete();
              }catch (Exception e){
                  e.printStackTrace();
              }
//            file.delete();
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    // 总部汇总省份近四个季度（跨年）
    public void saveExportExcelKn(String reportYear, String reportDateStr) throws Exception {

        // 删除数据库中的信息
		/*
		 * List<EvaluationReportExcel> list =null;
		 * list=evaluationReportFormService.findAllEvaluationReportExcelHqYearKn();
		 * if(list.size()>0){ for(EvaluationReportExcel ere:list){ try{
		 * evaluationReportFormService.delete(ere); } catch(Exception e){
		 * e.printStackTrace(); } }
		 *  }
		 */
        // 获得所有的汇总模板
        gatherTempletList = eemCommonService.findTempList2("sum", null);

        // EvaluationReportFormService
        // evaluationReportFormService=(EvaluationReportFormService)this.getBean("evaluationReportFormService");

        // 汇总模板formid
//        String formIdStr = "";
//        EvaluationReportFormTemplet erft = null;
//        EvaluationReportFormTemplet model = null;
//        EReportFormInputTemplet erfit = null;
        OutputStream os = null;
        WritableWorkbook wwb = null;


        // 测试为了提高速度只测试北京和安徽
        /*List<OrgEntity> orgList = new ArrayList<OrgEntity>();
        List<OrgEntity> orgEntityList = AAAAAdapter.getInstence().findOrgListAndSelfByParentID(1L, false);
        for (OrgEntity orgEntity : orgEntityList) {
            if (orgEntity.getOrgCode().startsWith("1") || orgEntity.getOrgCode().startsWith("81") || orgEntity.getOrgCode().startsWith("51")) {
                continue;
            } else {
                orgList.add(orgEntity);
            }
        }*/
        String path = EemConstants.ORDER_DATA_PATH+File.separator + "120" + File.separator;

        String pathname = "";
        // 删除指定路径下的所有文件
        // excelService.delAllFile(path);

        // 测试完取消注释
        deleteAllFileKn(path, "总部");

		/*
		 * List<EvaluationReportExcel> list =null;
		 * list=evaluationReportFormService.findAllEvaluationReportExcelHq();
		 * if(list.size()>0){ for(EvaluationReportExcel ere:list){ //删除表中上一季度的信息
		 * try{ evaluationReportFormService.delete(ere); } catch(Exception e){
		 * e.printStackTrace(); } } }
		 */
        // 删除当前季度数据库的值
        // 测试完取消注释
        deleteKn("总部");

//        List<EvaluationTemplet> listEt = null;
//        String formId = "";
//        List<EvaluationWithDrawForReport> listEwdfr = null;

        for (EemTempEntity et : gatherTempletList) {
            EemTempEntity reportTemp = findReportEemTempEntity(et.getObjectId());
            if (reportTemp == null) {
                continue;
            }
            // 测试完需注释 btliu 2015.7.2
			/*
			 * if(et.getFormId()!=153246636){ continue; }
			 */
            String index = "";
            String depsWithdraw = "";
            Date newUpdate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");


           /* formIdStr = et.getFormId() + "";
            // 根据汇总表的id 查找对应的导入模板
            listEt = excelService
                    .findEvaluationTempletByFormidforstat(formIdStr);
            if (listEt.size() == 0) {
                continue;
            }

            if (et.getFormName().contains("EPON")) {
                listEwdfr = excelService.findWithDrawForReport("89635",
                        reportYear, reportDateStr);
                if (listEwdfr.size() > 0) {
                    if (listEwdfr.size() != 0) {
                        for (int z = 0; z < listEwdfr.size(); z++) {
                            // 不重复输出地市的名字
                            if (depsWithdraw.contains(listEwdfr.get(z)
                                    .getCreateDepZH())) {
                                continue;
                            } else {
                                // 如果模板有被退回的那么在后加（）注释
                                depsWithdraw += listEwdfr.get(z)
                                        .getCreateDepZH();
                                index = depsWithdraw;
                            }

                        }
                    }
                }
            }
            // 如果为EPON类型那么，也要判断一下EPON服务模板是否退回，如果退回则不进行汇总
            if (et.getFormName().contains("GPON")) {
                listEwdfr = excelService.findWithDrawForReport("89659",
                        reportYear, reportDateStr);
                if (listEwdfr.size() > 0) {
                    for (int z = 0; z < listEwdfr.size(); z++) {
                        // 不重复输出地市的名字
                        if (depsWithdraw.contains(listEwdfr.get(z)
                                .getCreateDepZH())) {
                            continue;
                        } else {
                            // 如果模板有被退回的那么在后加（）注释
                            depsWithdraw += listEwdfr.get(z).getCreateDepZH();
                            index = depsWithdraw;
                        }

                    }
                }
            }

            // 看该模板，指定的日期是否有省份数据被退回，如果有则该表不进行汇总
            for (int i = 0; i < listEt.size(); i++) {
                formId = listEt.get(i).getFormId() + "";
                listEwdfr = excelService.findWithDrawForReport(formId,
                        reportYear, reportDateStr);
                if (listEwdfr.size() != 0 && !formId.equals("89659")
                        && !formId.equals("89635")) {
                    for (int z = 0; z < listEwdfr.size(); z++) {
                        // 不重复输出地市的名字
                        if (depsWithdraw.contains(listEwdfr.get(z)
                                .getCreateDepZH())) {
                            continue;
                        } else {
                            // 如果模板有被退回的那么在后加（）注释
                            depsWithdraw += listEwdfr.get(z).getCreateDepZH();
                            index = depsWithdraw;
                        }

                    }
                }
                // 如果为EPON类型那么，也要判断一下EPON服务模板是否退回，如果退回则不进行汇总

            }
            if (listEwdfr.size() > 0) {
                depsWithdraw = "(" + depsWithdraw + "被退回)";
                index = index + "被退回";
            }

            erft = evaluationReportFormService
                    .findExcelReportFormById(formIdStr);
            if (erft == null) {
                continue;
            }*/
            // 模板名称
            // String
            // excelName=reportYear+"近四个季度"+et.getFormName()+depsWithdraw+".xls";
            String excelName = reportYear + "近四个季度" + et.getTempName() + depsWithdraw
                    + ".xls";
            // 文件的保存路径
            pathname = path + excelName;
            EvaluationReportExcel ere = new EvaluationReportExcel();// 保存各个表的信息

            ere.setDep("总部");
            ere.setExcelName(excelName);
            ere.setExcelPath(path);
            ere.setFormId(et.getObjectId());
            ere.setCreationTime(new Timestamp(newUpdate.getTime()));
            ere.setReportYear(reportYear);
            ere.setReportDate("跨年");
            ere.setFileName(excelName);
            ere.setDeptsWithDraw(index);
            ere.setOperUserTrueName("系统自动");
            ere.setOperUserPhone("无");
            ere.setTpInputName(et.getTempName());
            ere.setDeletedFlag(false);
//            model = evaluationReportFormService
//                    .findExcelReportFormById(formIdStr);
//            erfit = evaluationReportFormService
//                    .findExcelReportFormInputTempletById(formIdStr);
            os = new FileOutputStream(pathname);
            // 近四个季度汇总年份传空值
            wwb = eemSummaryService.fromDBByteArrayToTable(et.getObjectId(),et.getTemplateExcelByteData().getUploadFileData(), os, reportDateStr, "", reportYear, et.getEemTempEntity().getObjectId().toString(),false);
            wwb.write();
            wwb.close();
            os.flush();
            os.close();
            File file  = new File(pathname);
            FileAdapter fileAdapter = FileAdapter.getInstance();
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
            String attachmentId = fileAdapter.upload("",null, dataInputStream);
            dataInputStream.close();
            ere.setAttachmentId(attachmentId);
            baseDAO.saveOrUpdate(ere);
            file.delete();
        }

    }

    // 删除当前季度
    public void deleteDqjd(String name, String year, String month) {
        Map<String, String> map = new HashMap<String, String>();
        map = getDqdata();
        // String year=String.valueOf(map.get("year"));
        // String month=String.valueOf(map.get("month"));
        List<EvaluationReportExcel> list = null;
        list = findAllEvaluationReportByName(name,
                year, month);
        if (list.size() != 0) {
            for (EvaluationReportExcel ere : list) {
                // 删除表中上一季度的信息
                try {
                    baseDAO.delete(ere);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 删除当前季度
    public void deleteKn(String name) {
        Map<String, String> map = new HashMap<String, String>();
        map = getDqdata();
        // String year=String.valueOf(map.get("year"));
        // String month=String.valueOf(map.get("month"));
        List<EvaluationReportExcel> list = null;
        list = findAllEvaluationReportExcelHqYearKn();
        if (list.size() != 0) {
            for (EvaluationReportExcel ere : list) {
                // 删除表中上一季度的信息
                try {
                    baseDAO.delete(ere);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 删除去年当前季度
    public void deleteLastyearjd(String name) {
        Map<String, String> map = new HashMap<String, String>();
        map = getDqdata();
        String year = String.valueOf((Integer.parseInt(String.valueOf(map
                .get("year"))) - 1));
        String month = String.valueOf(map.get("month"));
        List<EvaluationReportExcel> list = null;
        list = findAllEvaluationReportByName(name,
                year, month);
        if (list.size() != 0) {
            for (EvaluationReportExcel ere : list) {
                // 删除表中上一季度的信息
                try {
                    baseDAO.delete(ere);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Map<String, String> getDqdata() {
        String reportDateStr = "";
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dateStrMon = df.format(date);
        String monthStr = dateStrMon.substring(5, 7);
        String yearStr = dateStrMon.substring(0, 4);
        String reportYear = yearStr;

        String bn = "";
        // 全年汇总的年份
        String allReportYear = String.valueOf((Integer.parseInt(yearStr) - 1));
        if (monthStr.startsWith("0")) {
            monthStr = monthStr.replace("0", "").trim();
        }
        int monthInt = Integer.parseInt(monthStr);
        // 月份
        if (monthInt <= 3) {
            reportDateStr = "第四季度";
            reportYear = String.valueOf((Integer.parseInt(yearStr) - 1));
            bn = "上半年";
        } else if (monthInt >= 4 && monthInt < 7) {
            reportDateStr = "第一季度";
            bn = "上半年";

        } else if (monthInt >= 7 && monthInt < 10) {
            reportDateStr = "第二季度";
            bn = "下半年";

        } else {
            reportDateStr = "第三季度";
            bn = "下半年";
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("year", reportYear);
        map.put("month", reportDateStr);

        return map;

    }

    /**
     * ************************************************************************
     * 汇总模板
     *
     * @param reportYear    上报年
     * @param reportDateStr 上报季度
     * @param formid        汇总模板Id
     * @throws Exception
     */
    public void saveExportExcelNewUpdate(String dep, String reportYear,
                                         String reportDateStr, String formid) throws Exception {


        // 获得所有的汇总模板
        gatherTempletList = eemCommonService.findTempList2("sum", null);
        OutputStream os = null;
        WritableWorkbook wwb = null;
        EvaluationReportExcel ere = new EvaluationReportExcel();// 保存各个表的信息

        List<OrgEntity> orgEntityList = AAAAAdapter.getInstence().findOrgListAndSelfByParentID(1L, false);
        List<OrgEntity> proList = new ArrayList<OrgEntity>();
        for (OrgEntity orgEntity : orgEntityList) {
            if (orgEntity.getOrgCode().startsWith("1") || orgEntity.getOrgCode().startsWith("81") || orgEntity.getOrgCode().startsWith("51")) {
                continue;
            } else {
                proList.add(orgEntity);
            }
        }
        /*String deps = "";
        for (OrgEntity org : orgList) {
            deps += org.getOrgId() + ",";
        }
        if (deps.endsWith(",")) {
            deps = deps.substring(0, deps.length() - 1);
        }*/

        // String path="G:"+File.separator+"result"+File.separator;;
        String path = EemConstants.RELATIVE_PATH + File.separator + "202NFSShare" + File.separator
                + "eomsData" + File.separator + "attachment" + File.separator
                + "meoms" + File.separator + "HPJDownload" + File.separator
                + "country" + File.separator;
        String pathname = "";
        // 删除指定路径下的所有文件
        // excelService.delAllFile(path);

//        List<EvaluationTemplet> listEt = null;
//        String formId = "";
//        List<EvaluationWithDrawForReport> listEwdfr = null;

        for (EemTempEntity et : gatherTempletList) {
            String index = "";
            String depsWithdraw = "";
            Date newUpdate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
            String fileName = sdf.format(newUpdate);
            fileName += ".xls";

            // formIdStr=et.getFormId()+"";
            // 根据汇总表的id 查找对应的导入模板
//            listEt = excelService.findEvaluationTempletByFormidforstat(formid);
//            if (listEt.size() == 0) {
//                continue;
//            }
//
//            if (et.getFormName().contains("EPON")) {
//                listEwdfr = excelService.findWithDrawForReport("89635",
//                        reportYear, reportDateStr);
//                if (listEwdfr.size() > 0) {
//                    if (listEwdfr.size() != 0) {
//                        for (int z = 0; z < listEwdfr.size(); z++) {
//                            // 不重复输出地市的名字
//                            if (depsWithdraw.contains(listEwdfr.get(z)
//                                    .getCreateDepZH())) {
//                                continue;
//                            } else {
//                                // 如果模板有被退回的那么在后加（）注释
//                                depsWithdraw += listEwdfr.get(z)
//                                        .getCreateDepZH();
//                                index = depsWithdraw;
//                            }
//
//                        }
//                    }
//                }
//            }
//            // 如果为EPON类型那么，也要判断一下EPON服务模板是否退回，如果退回则不进行汇总
//            if (et.getFormName().contains("GPON")) {
//                listEwdfr = excelService.findWithDrawForReport("89659",
//                        reportYear, reportDateStr);
//                if (listEwdfr.size() > 0) {
//                    for (int z = 0; z < listEwdfr.size(); z++) {
//                        // 不重复输出地市的名字
//                        if (depsWithdraw.contains(listEwdfr.get(z)
//                                .getCreateDepZH())) {
//                            continue;
//                        } else {
//                            // 如果模板有被退回的那么在后加（）注释
//                            depsWithdraw += listEwdfr.get(z).getCreateDepZH();
//                            index = depsWithdraw;
//                        }
//
//                    }
//                }
//            }
//
//            // 看该模板，指定的日期是否有省份数据被退回，如果有则该表不进行汇总
//            for (int i = 0; i < listEt.size(); i++) {
//                formId = listEt.get(i).getFormId() + "";
//                listEwdfr = excelService.findWithDrawForReport(formId,
//                        reportYear, reportDateStr);
//                if (listEwdfr.size() != 0 && !formId.equals("89659")
//                        && !formId.equals("89635")) {
//                    for (int z = 0; z < listEwdfr.size(); z++) {
//                        // 不重复输出地市的名字
//                        if (depsWithdraw.contains(listEwdfr.get(z)
//                                .getCreateDepZH())) {
//                            continue;
//                        } else {
//                            // 如果模板有被退回的那么在后加（）注释
//                            depsWithdraw += listEwdfr.get(z).getCreateDepZH();
//                            index = depsWithdraw;
//                        }
//
//                    }
//                }
//                // 如果为EPON类型那么，也要判断一下EPON服务模板是否退回，如果退回则不进行汇总
//
//            }
//            if (listEwdfr.size() > 0) {
//                depsWithdraw = "(" + depsWithdraw + "被退回)";
//                index = index + "被退回";
//            }
//
//            erft = evaluationReportFormService.findExcelReportFormById(formid);
//            if (erft == null) {
//                continue;
//            }
            // 模板名称
            // String
            // excelName=reportYear+"近四个季度"+et.getFormName()+depsWithdraw+".xls";
            String startName = "";
            if ("全年".equals(reportDateStr)) {
                startName = reportYear + "全年";
            } else if ("跨年".equals(reportDateStr)) {
                startName = "近四个季度";
            } else {
                startName = reportYear + reportDateStr;
            }
            String excelName = startName + et.getTempName() + depsWithdraw
                    + ".xls";
            // 文件的保存路径
            pathname = path + fileName;

            ere.setDep(dep);
            ere.setExcelName(excelName);
            ere.setExcelPath(path);
            ere.setFormId(et.getObjectId());
            ere.setCreationTime(new Timestamp(newUpdate.getTime()));
            ere.setReportYear(reportYear);
            ere.setReportDate(reportDateStr);
            ere.setFileName(fileName);
            ere.setReportDate("跨年");
            ere.setFileName(excelName);
            ere.setDeptsWithDraw(index);
            ere.setOperUserTrueName("系统自动");
            ere.setOperUserPhone("无");
            ere.setTpInputName(et.getTempName());
            ere.setDeletedFlag(false);
            baseDAO.saveOrUpdate(ere);

//            model = evaluationReportFormService.findExcelReportFormById(formid);
//            erfit = evaluationReportFormService
//                    .findExcelReportFormInputTempletById(formid);
            os = new FileOutputStream(pathname);
            // 近四个季度汇总年份传空值
            if ("全年".equals(reportDateStr)) {
                wwb = fromDBByteArrayToTable(false, et.getTemplateExcelByteData().getUploadFileData(), os, reportDateStr, proList, reportYear, et.getEemTempEntity().getObjectId().toString());
            } else if ("跨年".equals(reportDateStr)) {
                wwb = fromDBByteArrayToTable(false, et.getTemplateExcelByteData().getUploadFileData(), os, reportDateStr, proList, reportYear, et.getEemTempEntity().getObjectId().toString());
            } else {
                wwb = fromDBByteArrayToTable(false, et.getTemplateExcelByteData().getUploadFileData(), os, reportDateStr, proList, reportYear, et.getEemTempEntity().getObjectId().toString());
            }
            wwb.write();
            wwb.close();
            os.flush();
            os.close();
        }

    }

    //删除指定路径下的指定文件（按季度删除相关文件）服务器路径下的文件
    public void deleteAllFileJd(String path, String dep, String year, String month) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
            System.out.println("文件路径不存在");
        } else {
            List<EvaluationReportExcel> list = null;
            list = findAllEvaluationReportByName(dep, year, month);
            String[] tem = file.list();
            File temp = null;
            for (int i = 0; i < tem.length; i++) {
                for (EvaluationReportExcel ere : list) {
                    String name = ere.getFileName();
                    if (path.endsWith(File.separator)) {
                        temp = new File(path + name);
                    } else {
                        temp = new File(path + File.separator + name);
                    }
                    if (temp.isFile()) {
                        temp.delete();
                    }

                }
            }
        }
    }

    public void deleteAllFileKn(String path, String dep) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
            System.out.println("文件路径不存在");
        } else {
            List<EvaluationReportExcel> list = null;
            list = findAllEvaluationReportExcelHqYearKn();
            String[] tem = file.list();
            File temp = null;
            for (int i = 0; i < tem.length; i++) {
                for (EvaluationReportExcel ere : list) {
                    String name = ere.getFileName();
                    if (path.endsWith(File.separator)) {
                        temp = new File(path + name);
                    } else {
                        temp = new File(path + File.separator + name);
                    }
                    if (temp.isFile()) {
                        temp.delete();
                    }

                }
            }
        }
    }

    public List<EvaluationReportExcel> findAllEvaluationReportExcelHqYearKn() {
        List<EvaluationReportExcel> list = null;
        try {
            list = baseDAO.find("from EvaluationReportExcel a where a.dep='总部' and a.reportDate='跨年' order by creationTime desc");
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return list;

    }

    public List<EvaluationReportExcel> findAllEvaluationReportByName(String name, String year, String month) {
        List<EvaluationReportExcel> list = null;
        try {
            list = baseDAO.find("from EvaluationReportExcel a where a.dep='" + name + "' and a.reportYear='" + year + "' and a.reportDate='" + month + "'  order by creationTime desc");
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return list;

    }


    public EemTempEntity findReportEemTempEntity(Long objectID) {
        try {
//            String hql = "from EemTempEntity where deletedFlag=0 and eemTempEntity.objectId=" + objectID;REL_TEMP_ID
            String hql = "from EemTempEntity where deletedFlag=0 and REL_TEMP_ID=" + objectID;
            List<EemTempEntity> data = baseDAO.find(hql);
            if (data != null && data.size() > 0) {
                return data.get(0);
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //根据模版表格中特殊格式的sql 生成查询结果  后评价--光缆 省份汇总用到zxx
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
                    sql = sql.replace("$$tpInputName", pagename);
                    sql = sql.replace("$$condition", " and txtvalue!=\"\"  and (p.iswithdraw!='Y' or  p.iswithdraw is null)");


                    if (sql != null && !"".equals(sql)) {
                        Object obj = baseDAO.findNativeSQL(sql, null);
                        if (obj != null) {
                            List list = (List) obj;
                            if (list != null && list.size() > 0 && list.get(0) != null) {
                                Map resMap = (Map) list.get(0);
                                Object ob = resMap.get("value");
                                if (ob != null) {
                                    res = ob.toString();


                                    try {//处理无限循环小数 如果出现问题立即捕获 不影响数据显示
                                        if (res.indexOf(".") != -1) {
                                            if (res.length() - res.indexOf(".") > 3) {

                                                res = res.substring(0, res.indexOf(".") + 3);
                                            }
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }


                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(sql);
            ex.printStackTrace();
        }
        return res;
    }

}
