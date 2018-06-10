package com.metarnet.eomeem.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.FileAdapter;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.core.common.model.DownloadFileInfo;
import com.metarnet.core.common.model.Pager;
import com.metarnet.core.common.model.TEomAttachmentRelProc;
import com.metarnet.core.common.service.IAttachmentRelProcService;
import com.metarnet.core.common.utils.Constants;
import com.metarnet.core.common.utils.SpringContextUtils;
import com.metarnet.eomeem.model.*;
import com.metarnet.eomeem.service.IEemCommonService;
import com.metarnet.eomeem.service.IEemDeductService;
import com.metarnet.eomeem.service.IEemTemplateService;
import com.metarnet.eomeem.utils.*;
import com.metarnet.eomeem.utils.excel.InputElement;
import com.metarnet.eomeem.utils.excel.SelectInputElement;
import com.metarnet.eomeem.utils.excel.SelectOptionItem;
import com.metarnet.eomeem.vo.AnalysisDeduct;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import com.unicom.ucloud.workflow.objects.TaskInstance;
import jxl.Cell;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.dom4j.Element;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2015/11/17.
 */
@Service
public class EemDeductServiceImpl implements IEemDeductService {
    private int rowBegin = 4;

    private int colBegin = 4;

    private static final String KEY_WORDS = "keywords";

    private static final String OPERATOR = "operator";

    Logger logger = LogManager.getLogger(EemDeductServiceImpl.class);

    @Resource
    private IBaseDAO baseDAO;
    @Resource
    private IEemTemplateService templateService;

    @Resource
    private IEemCommonService eemCommonService;

    @Resource
    private IAttachmentRelProcService attachmentRelProcService;

    @Override
    public boolean checkReport(ExcelPage excelPage, UserEntity userEntity) {

        boolean re = true;
        ExcelPage ex = new ExcelPage();
        try {
            ex.setReportOrgCode(AAAAAdapter.getCompany(Integer.parseInt(userEntity.getOrgEntity().getOrgId().toString())).getOrgCode().toString());
            ex.setReportYear(excelPage.getReportYear());
            ex.setReportDate(excelPage.getReportDate());
            ex.setTpInputID(excelPage.getTpInputID());
            ex.setDisID(0);
            ex.setWillCollect(excelPage.getWillCollect());
            //
//            ex.setTpInputName(excelPage.getTpInputName());
            List<ExcelPage> excelPageList = baseDAO.findByExample(ex);
            if (excelPageList != null && excelPageList.size() > 0) {
                re = false;
            }
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return re;

    }

    @Override
    public DeductExcelPage initReport(UserEntity user, HttpServletRequest request, TaskInstance taskInstance) {
        DeductExcelPage excelPage = new DeductExcelPage();
        try {
            excelPage.setOperOrgName(user.getOrgEntity().getOrgName());
            excelPage.setOperUserPhone(user.getMobilePhone());
            excelPage.setOperUserTrueName(user.getTrueName());
            return excelPage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String excelToPage(MultipartFile file) {
        String result = null;
        try {
//            result = new ExcelConverter2().fromDBByteArrayToHTMLTableEvaluation(file.getBytes(), null, "");
            result = new ExcelToHtml().parseExcelToHtml(file.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String excelToHtmlByID(String objectId) {
        String result = null;
        try {
            DeductExcelPage excelPage = getExcelPage(Long.parseLong(objectId));
            List<ExcelPageValues2> pageValuesList = findExcelPageValueByPageID(Arrays.asList(excelPage.getObjectId()));
            EemTempEntity eemTempEntity = getTemEntity(excelPage.getTpInputID());
            ExcelConverter2 converter2 = new ExcelConverter2();
            result = converter2.fromDBByteArrayToHTMLTableEvaluation2(eemTempEntity.getTemplateExcelByteData().getXmlFileData(), pageValuesList, "");
        } catch (DAOException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public HSSFWorkbook downReportData(String objectId) {
        try {
            DeductExcelPage excelPage = getExcelPage(Long.parseLong(objectId));
            List<ExcelPageValues> pageValuesList = findExcelPageValueByPageID(Arrays.asList(excelPage.getObjectId()));
            EemTempEntity eemTempEntity = getTemEntity(excelPage.getTpInputID());
            HSSFWorkbook inputTemp = new HSSFWorkbook(new ByteArrayInputStream(eemTempEntity.getTemplateExcelByteData().getUploadFileData()));
            int sheetCount = inputTemp.getNumberOfSheets();
            for (int i = 0; i < sheetCount; i++) {
                HSSFSheet sheet = inputTemp.getSheetAt(i);
                String sheetName = sheet.getSheetName();
                int rowCount = sheet.getPhysicalNumberOfRows();//行数
                int sqlStart = 0;
                for (int ii = 0; ii < rowCount; ii++) {
                    //行 循环
                    HSSFRow row = sheet.getRow(ii);
                    int colCount = 0;
                    if (row != null) {
                        colCount = row.getLastCellNum(); //列数
                    }
                    for (int j = 0; j < colCount; j++) {//列 循环
                        sheet.setForceFormulaRecalculation(true);
                        HSSFCell cell = row.getCell(j);
                        String content = "";
                        if (!"".equals(cell) && cell != null) {
                            if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                content = cell.getStringCellValue();
                            } else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                                content = cell.getNumericCellValue() + "";//没有日期格式的，所以没判断
                            } else if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
                                content = cell.getCellFormula();
                            }
                        }
                        if (content.contains("xxxx")) {
                            Timestamp reportDate = new Timestamp(new Date().getTime());
                            int year = reportDate.getYear() + 1900;
                            int month = reportDate.getMonth() + 1;
                            content = content.substring(content.lastIndexOf("x") + 2, content.length() - 1);
                            content = year + "年" + month + "月" + content;
                            cell.setCellValue(content);
                        }
                        for (int jj = 0; jj < pageValuesList.size(); jj++) {
                            if (String.valueOf(ii).equals(String.valueOf(pageValuesList.get(jj).getRowIndex())) && String.valueOf(j).equals(String.valueOf(pageValuesList.get(jj).getColIndex()))) {
                                cell.setCellValue(pageValuesList.get(jj).getTxtValue());
                            }
                        }
                    }
                }
            }
            return inputTemp;
        } catch (Exception e) {
        }
        return null;
    }

    public Set analysisJobContentDonePoi(byte[] byteArray, HSSFSheet jobContentExcelSheet, long formId, long pageId) throws Exception {
        InputStream stream = (new ByteArrayInputStream(byteArray));
        org.dom4j.Document doc;
        try {
            List errors = new ArrayList();
            doc = XMLHelper.createSAXReader("JobFormHtmlPreview", errors).read(new InputSource(stream));
            if (errors.size() != 0)
                throw new Exception("");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        HashSet set = new HashSet();
        if (doc == null) {
            return set;
        }
        // 获取所有TR
        Iterator trList = doc.getRootElement().elementIterator("tr");
        int rowCount = 0;
        int colCount = 0;

        while (trList != null && trList.hasNext()) {
            Element tr = (Element) trList.next();
            //获取该TR的ID
            String trId = tr.attributeValue("id");
            rowCount = Integer.parseInt(trId);
            //获取该TR所有TD
            Iterator tdList = tr.elementIterator("td");
            int currentColCount = 0;
            while (tdList != null && tdList.hasNext()) {
                Element td = (Element) tdList.next();
                //获取该TD的ID
                String tdId = td.attributeValue("id");
                currentColCount = Integer.parseInt(tdId);
                String text = td.getText();
                Element span = null;
                if (text == null || "".equals(text)) {
                    Iterator spanList = td.elementIterator("SPAN");
                    while (spanList != null && spanList.hasNext()) {
                        span = (Element) spanList.next();
                        String spanText = span.getText();
                        if (spanText != null && "text:value=".equals(spanText.trim())) {
                            text = "text:value=";
                            break;
                        }
                    }
                }
                if (text != null && !"".equals(text)) {
                    InputElement ie = this.createInputElement(text);
                    if (ie != null) {
                        // 从EXCEL中，获取值
                        jobContentExcelSheet.setForceFormulaRecalculation(true);
                        logger.info("~~~~~row=" + rowCount + "!!!currentColCount=" + currentColCount);
                        HSSFRow row = jobContentExcelSheet.getRow(rowCount);
                        HSSFCell cell = row.getCell(currentColCount);
                        if (cell == null) {
                            continue;
                        }
                        String cellText = "";
                        if (!"".equals(cell.getCellType()) && cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                            cellText = cell.getStringCellValue();
                        } else if (!"".equals(cell.getCellType()) && cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {//日期和数值都是CELL_TYPE_NUMERIC
                            if (HSSFDateUtil.isCellDateFormatted(cell)) {//判断是否是日期格式
                                DateCell dc = (DateCell) cell;
                                if (dc != null && dc.getDateFormat() != null && dc.getDate() != null) {
                                    Date comparedDate = new Date();
                                    try {
                                        comparedDate = new SimpleDateFormat("yyyy-MM-dd").parse("1949-01-01");
                                    } catch (Exception e) {
                                        comparedDate = new Date(1901, 1, 1);
                                    }
                                    if (dc.getDate().before(comparedDate))
                                        cellText = dc.getDateFormat().format(dc.getDate());
                                    else
                                        cellText = new SimpleDateFormat("yyyy-MM-dd").format(dc.getDate());
                                }
                            } else {//数值格式
                                double intcellText = cell.getNumericCellValue();
                                cellText = intcellText + "";
                            }
                        } else if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
                            try {
                                cellText = cell.getNumericCellValue() + "";
                            } catch (Exception e) {
                                cellText = String.valueOf(0);
                            }

                        } else if (cell.getCellStyle().equals(HSSFCell.CELL_TYPE_ERROR)) {
                            cellText = String.valueOf(cell.getErrorCellValue());
                        }
                        //判断是否有一些必填字段需要检查
                        Properties prop = ie.getKeyValueProps();
                        if (prop != null) {
                            String ismust = prop.getProperty("ismust");

                            if (ismust != null && ismust.equals("true") && (cellText == null || cellText.equals("")))
                                throw new Exception("有一些必填字段没有填写，请填写后再提交!");
                        }
                        ExcelPageValues epv = new ExcelPageValues();
                        epv.setObjectId(baseDAO.getSequenceNextValue(ExcelPageValues.class));
                        epv.setRowIndex(rowCount);
                        epv.setColIndex(currentColCount);
                        if (cellText != null) {
                            cellText = cellText.replace(",", "");
                        }
                        epv.setTxtValue(cellText);
                        epv.setPageID(pageId);
                        epv.setTpID(formId);
                        set.add(epv);
                    }
                }
                colCount = (currentColCount > colCount) ? currentColCount : colCount;
            }
        }
        return set;
    }

    public InputElement createInputElement(String txt) {

        if (txt == null)
            return null;

        // 风格类型
        String[] temp = txt.split(":", 2);

        JobConfiguration jobConfiguration = (JobConfiguration) SpringContextUtils
                .getBean("jobConfiguration");
        InputElementDefinition def = jobConfiguration
                .getInputElementDef(temp[0]);

        // 根据配置文件培植创建一个新的 InputElement 实例
        InputElement ie = def.newInputElement();
        if (ie != null && temp.length > 1) {

            // 设置配置的通用属性g
            String[] props = temp[1].split(";");
            for (int i = 0; i < props.length; i++) {
                String[] kv = props[i].split("=", 2);
                if (kv.length > 1) {

                    if ("value".equalsIgnoreCase(kv[0]) && kv[1].length() >= 2) {
                        if ((kv[1].indexOf("\"") == 0 && kv[1]
                                .lastIndexOf("\"") == (kv[1].length() - 1))
                                || (kv[1].indexOf("'") == 0 && kv[1]
                                .lastIndexOf("'") == (kv[1].length() - 1)))
                            kv[1] = kv[1].substring(1, kv[1].length() - 1);
                    }

                    ie.addKeyValueProp(kv[0], kv[1]);
                } else {

                    ie.addValueProp(props[i]);
                }
            }

            // 设置特殊属性
            if (ie instanceof SelectInputElement) {
                SelectInputElement sie = (SelectInputElement) ie;
                List list = new ArrayList();
                try {
                    Map<String, String> map = jobConfiguration
                            .getProps("option");
                    if (map != null && map.size() > 0) {
                        for (String key : map.keySet()) {
                            String value = map.get(key);
                            SelectOptionItem so = new SelectOptionItem(key,
                                    value);
                            list.add(so);
                        }
                    }
                    sie.setOptions(list);
                } catch (Exception ex) {

                }
            }
        }

        return ie;
    }


    public EemTempEntity getTemEntity(Long formId) throws DAOException {
        List<EemTempEntity> list = baseDAO.find("from EemTempEntity a where a.objectId=" + formId);
        EemTempEntity nt = null;
        if (list != null && list.size() > 0) {
            nt = list.get(0);
        }
        return nt;
    }

    @Override
    public DeductExcelPage getExcelPage(Long formId) throws DAOException {
        return (DeductExcelPage) baseDAO.get(DeductExcelPage.class, formId);
    }

    public List findExcelPageValueByPageID(List PageIds) throws ServiceException {
        try {
            return baseDAO.find("from ExcelPageValues2 where pageID in (?)", PageIds.toArray());
        } catch (DAOException e) {
            e.printStackTrace();
            throw new ServiceException(e);
        }
    }

    protected Object initParams( UserEntity userEntity,DeductExcelPage workFeedBackOrder) throws UIException {
        try {
            OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgID(userEntity.getOrgID());
            Integer uploadedByPersonId = userEntity.getUserId().intValue();
            String uploadedByPersonName = userEntity.getTrueName();
            Integer uploadedByOrgId = orgEntity.getOrgId().intValue();
            String uploadedByOrgName = orgEntity.getOrgName();

            String flowingFlag ="N";// 流程标识(流程类附件为Y|非流程类附件为N)
            String attachmentTypeEnumId = "1";// 附件类型
            String attachmentFormatEnumId = "1";// 附件格式
            String shardingId ="1";// 附件关联表分片ID
            String attribute1 = null;// 附加条件

            String flowingObjectTable = "t_eem_deduct_excel_page";// 流转对象表名
            String flowingObjectId = workFeedBackOrder.getObjectId().toString();// 流转对象ID-对应申请单ID、调度单ID、反馈信息ID、电路ID或者产品ID
            String flowingObjectShardingId = "1";// 流转对象分片ID
            String activityInstanceId ="1";// 活动(环节)实例ID
            String taskInstanceId = "1";// 任务实例ID
            String rootProcessInstanceId = "1";// 根流程实例ID

            TEomAttachmentRelProc attachmentRelProc = new TEomAttachmentRelProc();
            attachmentRelProc.setFlowingObjectTable(flowingObjectTable);
            attachmentRelProc.setFlowingObjectId(Long.parseLong(flowingObjectId));
            if (org.apache.commons.lang.StringUtils.isNotEmpty(flowingObjectShardingId)) {
                attachmentRelProc.setFlowingObjectShardingId(Integer.parseInt(flowingObjectShardingId));
            }
            attachmentRelProc.setActivityInstanceId(activityInstanceId);
            attachmentRelProc.setTaskInstanceId(taskInstanceId);
            attachmentRelProc.setAttachmentTypeEnumId(Integer.parseInt(attachmentTypeEnumId));
            attachmentRelProc.setAttachmentFormatEnumId(Integer.parseInt(attachmentFormatEnumId));
            if (Constants.IS_SHARDING) {
                attachmentRelProc.setShardingId(Integer.parseInt(shardingId));
            }
            if (org.apache.commons.lang.StringUtils.isNotEmpty(rootProcessInstanceId)) {
                attachmentRelProc.setAttribute1(rootProcessInstanceId); // 根流程实例ID保存到attribute1
            }
            attachmentRelProc.setUploadedByPersonId(uploadedByPersonId);
            attachmentRelProc.setUploadedByPersonName(uploadedByPersonName);
            attachmentRelProc.setUploadedByOrgId(uploadedByOrgId);
            attachmentRelProc.setUploadedByOrgName(uploadedByOrgName);
            attachmentRelProc.setAttribute1("");
            return attachmentRelProc;
        } catch (Exception e) {
            e.printStackTrace();
//	    throw new UIException(this.getClass().getName(), e.getMessage());
        }
        return null;
    }

    //----------------------------------------------------------------------华丽丽的分割线xlzhang------------------------------------------------------------------------------------------

    /**
     * @param excelPage
     * @param sheetName  暂且未使用
     * @param withdraw
     * @param userEntity
     * @return
     */
    @Override
    public String saveReportData(HttpServletRequest request,MultipartFile file, DeductExcelPage excelPage, String sheetName, String withdraw, UserEntity userEntity) {
        /**
         * 1.判断是普通模板还是光缆模板
         * 2.保存上报的excel文件
         * 3.光缆产品供货和综合评价季度表、蝶形光缆综合评价表、蝶形光缆、光缆测试需要特殊处理（即光缆模板需要特殊处理）
         * 4.OLT设备（含EMS）互通商用、FTTH ONU设备互通商用后评价表  特殊处理
         */
        String result = "";
        try {
            Long fileDataID = baseDAO.getSequenceNextValue(EvaluationFileData.class);
            OrgEntity orgEntity = AAAAAdapter.getCompany(Integer.parseInt(userEntity.getOrgEntity().getOrgId().toString()));
            String orgCode = "1";
            if(orgEntity!=null){
                orgCode =orgEntity.getOrgCode();
            }
            String filePath = EemConstants.EVALUATION_FILE_ABSOLUTE_PATH + File.separator +orgCode
                    + File.separator + DateUtils.getStrFromDateYYYYMM(new Date());//要保存的路径
            String fileName = filePath + File.separator + fileDataID + ".xls";//要保存的excel的路径+名字


//            FileAdapter fileAdapter = FileAdapter.getInstance();
//            DataInputStream dataInputStream = new DataInputStream(file.getInputStream());
            Long pageId = baseDAO.getSequenceNextValue(DeductExcelPage.class);
            excelPage.setObjectId(pageId);

            Object object = this.initParams(userEntity,excelPage);

            TEomAttachmentRelProc tEomAttachmentRelProc = attachmentRelProcService.saveFileAndUploadToPass(
                    object, request, Constants.STORAGE_NAME, "keywords", "operator", userEntity);
//            String attachmentId = fileAdapter.upload("",null, dataInputStream);
//            dataInputStream.close();
            EemTempEntity eemTempEntity = templateService.findTempByID(excelPage.getTpInputID());
            Workbook wb = null;
            InputStream is = new ByteArrayInputStream(file.getBytes());
            wb = Workbook.getWorkbook(is);
            Sheet[] sheetArray = wb.getSheets();
            if(sheetArray.length>0&&!sheetArray[0].getName().equals(eemTempEntity.getShortName())){
                result= "请选择对应的模板！";
            }
            excelPage.setFileName(eemTempEntity.getTempName());
            excelPage.setFilePath(fileName);

            excelPage.setAttachmentId(tEomAttachmentRelProc.getAttachmentId());
            if(org.apache.commons.lang3.StringUtils.isBlank(excelPage.getReportType())){
                excelPage.setReportType("deduct");
            }
//            String withdraw = "";//退回后重新上报 yes 表示退回后 重新上报
            if(org.apache.commons.lang3.StringUtils.isBlank(result)){
                result = this.importEvaluationFileDataByWithDraw(orgCode,
                        fileDataID, file.getBytes(), eemTempEntity, userEntity,
                        excelPage, withdraw);// 保存具体数据

            }

//            if (org.apache.commons.lang3.StringUtils.isBlank(result)) {
//                EvaluationFileData efd = new EvaluationFileData();
//                efd.setFileID(fileDataID);
//                efd.setFilePath(filePath);
//                efd.setFileName(file.getName());
//                efd.setUploadDate(new Date());
//                efd.setReportOrgCode(orgEntity.getOrgCode());
//                efd.setReportPersonName(userEntity.getTrueName());
//                efd.setReportPersonID(userEntity.getUserId());
//                efd.setReportPersonTel(excelPage.getOperUserPhone());
//                baseDAO.saveOrUpdate(efd);
//            }
        } catch (Exception e) {
            result = "系统内部运行异常";
            e.printStackTrace();
        } finally {
//            if(file!=null){
//                file.delete();
//            }
            return result;
        }
    }

    public String importEvaluationFileDataByWithDraw(String reportCode, Long fileDataTid, byte[] fileByteArray, EemTempEntity tempEntity, UserEntity user, DeductExcelPage excelPage, String withdraw) throws Exception {
        String result = "";
        Workbook wb = null;
        InputStream is = new ByteArrayInputStream(fileByteArray);
        wb = Workbook.getWorkbook(is);
        if (!withdraw.equals("yes")) {
            List<DeductExcelPage> currentList = baseDAO.find("from DeductExcelPage where reportOrgCode='" + reportCode + "' and reportYear='" + excelPage.getReportYear() + "' and reportDate='" + excelPage.getReportDate() + "' and tpInputID=" + excelPage.getTpInputID());
            for (DeductExcelPage page : currentList) {
               List values = baseDAO.find("from DedcutPageValues where pageID="+page.getObjectId());
                if(values!=null&&values.size()>0){
                    baseDAO.deleteAll(values);
                }
                List values2 = baseDAO.find("from ExcelPageValues2 where pageID="+page.getObjectId());
                if(values2!=null&&values2.size()>0){
                    baseDAO.deleteAll(values2);
                }
            }

            baseDAO.deleteAll(currentList);
        }
        Sheet[] sheetArray = wb.getSheets();
        OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(reportCode);
        for (Sheet sheet : sheetArray) {
            String sheetName = sheet.getName();
            if(sheetName.equals("填表说明")||sheetName.contains("问题说明表")||!sheetName.equals(tempEntity.getShortName())){
                return "请选择对应模板";
//                continue;
            }
            DeductExcelPage ep = new DeductExcelPage();
            ep.setFileName(excelPage.getFileName());
            ep.setTpInputName(tempEntity.getTempName());
            ep.setCreatedBy(user.getUserId());
            ep.setCreationTime(new Timestamp(System.currentTimeMillis()));
            ep.setReportOrgCode(reportCode);
            ep.setSummaryId(excelPage.getSummaryId());

            ep.setObjectId(excelPage.getObjectId());
            ep.setPageName(sheetName);
            ep.setOperUserId(user.getUserId());
            ep.setOperUserTrueName(user.getTrueName());
            ep.setOperOrgName(orgEntity.getOrgName());
            ep.setOperFullOrgName(orgEntity.getFullOrgName());
            ep.setOperUserPhone(excelPage.getOperUserPhone());
            ep.setTpInputID(tempEntity.getObjectId());
            ep.setReportDate(excelPage.getReportDate());
            ep.setReportYear(excelPage.getReportYear());
            ep.setFileDataId(fileDataTid);
            ep.setDeletedFlag(false);
            ep.setFilePath(excelPage.getFilePath());
            ep.setReportType(excelPage.getReportType());
            ep.setWorkOrderStatus("未审核");
            ep.setAttachmentId(excelPage.getAttachmentId());
            if (withdraw.equals("yes")) {
                ep.setIswithdraw("yes");
                ep.setLastUpdateTime(new Timestamp(System.currentTimeMillis()));
                List<DeductExcelPage> currentList = baseDAO.find("from DeductExcelPage where reportOrgCode='" + reportCode + "' and reportYear='" + excelPage.getReportYear() + "' and reportDate='" + excelPage.getReportDate() + "' and tpInputID=" + excelPage.getTpInputID() + " and tpInputName='" + ep.getTpInputName() + "'");
                for (DeductExcelPage page : currentList) {
                    deletePageValuse(page);
                    page.setDeletedFlag(true);
                }
                baseDAO.deleteAll(currentList, user);
            }
            result+= importEvaluationFileDataForEachSheet(sheet, tempEntity, ep, "1");
        }
        return result;
    }

    public String importEvaluationFileDataForType2Withdraw(String reportCode, Long fileDataTid, byte[] fileByteArray, EemTempEntity tempEntity, UserEntity user, DeductExcelPage excelPage, String withdraw, List<String> withDrawPageName, String isWithDrawStr, List<DeductExcelPage> excelPageList)
            throws Exception {
        String result = "";
        Workbook wb = null;
        InputStream is = new ByteArrayInputStream(fileByteArray);
        wb = Workbook.getWorkbook(is);
        Sheet[] sheetArray = wb.getSheets();
        if (!withdraw.equals("yes")) {
            if (excelPageList != null && excelPageList.size() > 0) {
                for (DeductExcelPage deletePage : excelPageList) {
                    deletePageValuse(deletePage);
                    deletePage.setDeletedFlag(true);
                }
                baseDAO.deleteAll(excelPageList, user);
            }
        }
        OrgEntity orgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(reportCode);
        for (Sheet sheet : sheetArray) {
            String pageName = sheet.getCell(2, 1).getContents();
            if (!"".equals(pageName) && pageName != null) {
                DeductExcelPage ep = new DeductExcelPage();
                ep.setFileName(excelPage.getFileName());
                ep.setCreatedBy(user.getUserId());
                ep.setCreationTime(new Timestamp(System.currentTimeMillis()));
                ep.setReportOrgCode(reportCode);
                Long pageId = baseDAO.getSequenceNextValue(DeductExcelPage.class);
                ep.setTpInputName(tempEntity.getTempName() + "-" + pageName);
                ep.setObjectId(pageId);
                ep.setPageName(pageName);
                ep.setOperUserId(user.getUserId());
                ep.setOperUserTrueName(user.getTrueName());
                ep.setOperOrgName(orgEntity.getOrgName());
                ep.setOperFullOrgName(orgEntity.getFullOrgName());
                ep.setOperUserPhone(excelPage.getOperUserPhone());
                ep.setTpInputID(tempEntity.getObjectId());
                ep.setReportDate(excelPage.getReportDate());
                ep.setReportYear(excelPage.getReportYear());
                ep.setFileDataId(fileDataTid);
                ep.setWorkOrderStatus("未审核");
                ep.setFilePath(excelPage.getFilePath());
                ep.setReportType(excelPage.getReportType());
                ep.setDeletedFlag(false);
                ep.setAttachmentId(excelPage.getAttachmentId());
                if (withdraw.equals("yes")) {
                    ep.setIswithdraw("yes");
                    ep.setLastUpdateTime(new Timestamp(System.currentTimeMillis()));
                    List<DeductExcelPage> currentList = baseDAO.find("from DeductExcelPage where reportOrgCode='" + reportCode + "' and reportYear='" + excelPage.getReportYear() + "' and reportDate='" + excelPage.getReportDate() + "' and tpInputID=" + excelPage.getTpInputID() + " and tpInputName='" + ep.getTpInputName() + "'");
                    for (DeductExcelPage page : currentList) {
                        deletePageValuse(page);
                        page.setDeletedFlag(true);
                    }
                    baseDAO.deleteAll(currentList, user);
                }
                result+= importEvaluationFileDataForEachSheet(sheet, tempEntity, ep, "2");
            } else {
                throw new Exception();
            }
        }
        return result;
    }

    public String importEvaluationFileDataForEachSheet(Sheet sheet, EemTempEntity tempEntity, DeductExcelPage ep, String formType)
            throws Exception {
        logger.info("##########################" + sheet.getName());
        long pageId = 0;// 获得导入ExcelPage的formid 以及pageid
        pageId = ep.getObjectId();
        if(tempEntity.getLevel()!=3){
            boolean isLicit = this.checkFileIsLicit(sheet, tempEntity.getTemplateExcelByteData().getUploadFileData());
            if (!isLicit) {
                return "与系统提供模板不一致，请下载最新模板上报数据！";
            }
        }
        // 在保存数据之前先删掉 本部门 该月 改formid的page数据end
        baseDAO.saveOrUpdate(ep);
        ExcelConverter2 ec = new ExcelConverter2();
        List<List<String>> vauleList = null;
        Workbook wb = null;

        vauleList = ec.analysisJobContentDone2(tempEntity.getTemplateExcelByteData().getXmlFileData(), sheet, tempEntity.getObjectId(), pageId);// 根据导入excel的sheet获得ExcelPageValues的集合

        if (vauleList != null && vauleList.size() > 1) {
            List list = new ArrayList();
            for (int i=0;i<vauleList.get(0).size();i++) {
                DedcutPageValues deductValue = new DedcutPageValues();
                deductValue.setPageID(ep.getObjectId());
                deductValue.setDeductTpID(ep.getTpInputID());
                deductValue.setReportTpID(ep.getSummaryId());
                deductValue.setTxtValue(vauleList.get(1).get(i));
                deductValue.setVenderName(vauleList.get(0).get(i));
                long pId = baseDAO.getSequenceNextValue(DedcutPageValues.class);
                deductValue.setObjectId(pId);
                list.add(deductValue);

            }
            baseDAO.saveOrUpdateAll(list);
        }

        Set<ExcelPageValues2> set = null;

        set = ec.analysisJobContentDone3(tempEntity.getTemplateExcelByteData().getXmlFileData(), sheet, tempEntity.getObjectId(), pageId);// 根据导入excel的sheet获得ExcelPageValues的集合

        if (set != null && set.size() > 0) {
            for (ExcelPageValues2 excelPageValues : set) {
                excelPageValues.setObjectId(baseDAO.getSequenceNextValue(ExcelPageValues2.class));
            }
            baseDAO.saveOrUpdateAll(set);
        }

        if (wb != null) {
            wb.close();
        }
        return "";
    }

    private void deletePageValuse(DeductExcelPage excelPage) {
        try {
            baseDAO.executeSql("delete from t_eem_deduct_excel_page where pageID=" + excelPage.getObjectId());
            baseDAO.executeSql("delete from t_eem_excel_page_values2 where pageID=" + excelPage.getObjectId());
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param wb
     * @return
     * @throws Exception
     */
    private String moreSheetsCheck(Workbook wb) throws Exception {
        String result = "";
        Sheet[] sheets = wb.getSheets();
        // int colCount=0;
        int rowCount = 0;
        // 有多少个sheet页
        int sheetsCount = sheets.length;
        for (Sheet sheet : sheets) {
            // 填表说明的sheet页不做校验
            if (sheet.getName().equals("填表说明")) {
                continue;
            } else if (sheet.getName().contains("互通商用后评价表")) {
                // colCount=sheet.getColumns();
                // -4是减去4个备注的信息
                rowCount = sheet.getRows() - 4;
                int col = colBegin;
                // 刨去前两个sheet页不算
                for (int j = 2; j < sheetsCount; j++) {
                    for (int i = rowBegin, z = 0; i < rowCount; z++) {
                        Cell cell = sheet.getCell(col, i);
                        String value = cell.getContents();
                        if (!"".equals(value) && value != null) {
                            Sheet tosheet = wb.getSheet(j);
                            if (!sheet.getCell(colBegin - 1, i).getContents()
                                    .trim().equals("总分")) {
                                result += compareWithOtherSheet(tosheet, value,
                                        z);
                            }

                            i = i + 2;
                        } else {
                            i = i + 2;
                        }
                    }
                    col++;

                }

            } else {
                continue;
            }
        }
        return result;
    }

    private String compareWithOtherSheet(Sheet toSheet, String value, int questionNo) throws Exception {
        String result = "";
        // 说明表开始解析的行数
        int rowStart = 4 + 12 * questionNo;
        // 说明表开始解析的列数
        int colStart = 2;
        // 应该填写的问题个数
        int rowCounts = Integer.parseInt(value);

        // 获得厂家的名字
        // String company = tosheet.getCell(0, rowStart).getContents();

        for (int i = rowStart, j = 0; j < rowCounts; j++, i++) {
            String cellValue = toSheet.getCell(colStart, i).getContents();
            if ("".equals(cellValue) || cellValue == null) {
                result += "请在" + toSheet.getName() + "中填写" + rowCounts
                        + "条问题说明从第" + (rowStart + 1) + "行开始填写;";
            }
        }
        return result;

    }

    // 判断上传的excel是不是我们总部提供的 是否是合法的
    private boolean checkFileIsLicit(Sheet uploadSheet, byte[] sysFile)
            throws IOException, BiffException {
        InputStream sysFileIs = new ByteArrayInputStream(sysFile);
        Workbook sysFilewb = Workbook.getWorkbook(sysFileIs);
        Sheet sysSheet = sysFilewb.getSheet(0);
        // 这里是针对邵岩增加的两个导入模板，因为之前的模板均是一个sheet页，所以程序上直接获取第一个sheet页
        // 现在的上报数据在第二个模板所以如果第一个sheet页 的名为填表说明，则继续向后取第二个sheet页
        if (sysSheet.getName().equals("填表说明")) {
            sysSheet = sysFilewb.getSheet(1);
        }
        String[] testArray = {"0,0", "0,5", "0,10", "0,15", "0,20", "0,25",
                "0,30", "0,35", "0,40", "1,0", "1,5", "1,10", "1,15", "1,20",
                "1,25", "1,30", "1,35", "1,40", "3,0", "7,0", "15,0", "20,0",
                "25,0", "30,0", "35,0"};
        boolean isLicit = true;
        for (String checkPosition : testArray) {
            if (checkPosition != null && !"".equals(checkPosition)) {
                String[] checkPoint = checkPosition.split(",");
                int x = StringUtils.fromStringToInt(checkPoint[0]);
                int y = StringUtils.fromStringToInt(checkPoint[1]);
                String uploadFileContents = "";
                String sysFileContents = "";
                try {
                    Cell uploadFilec = uploadSheet.getCell(x, y);
                    Cell sysFilec = sysSheet.getCell(x, y);
                    uploadFileContents = uploadFilec.getContents();
                    sysFileContents = sysFilec.getContents();
                } catch (ArrayIndexOutOfBoundsException e) {
                    // 检测时出现异常 可以忽略此异常 有可能给的测试值 有问题
                    continue;
                }

                if (sysFileContents != null && !"".equals(sysFileContents)) {
                    if (!uploadFileContents.trim().equals(sysFileContents.trim())&&!sysFileContents.equals("##text:value=#")) {
                        isLicit = false;
                        logger.info("与系统提供模板不一致，请下载最新模板上报数据！");
                        if (sysFilewb != null) {
                            sysFilewb.close();
                        }

                        return isLicit;
                    }
                }
            }
        }
        if (sysFilewb != null) {
            sysFilewb.close();
        }
        return isLicit;
    }

    /**
     * 检查该模板某年某季度/半年 数据是否已经上报
     *
     * @param excelPage
     * @param userEntity
     * @return
     */
    @Override
    public String hasPowerToSave(DeductExcelPage excelPage, UserEntity userEntity) {
        try {
            String reportCode = AAAAAdapter.getCompany(Integer.parseInt(userEntity.getOrgEntity().getOrgId().toString())).getOrgCode();
            List<DeductExcelPage> currentList = baseDAO.find("from DeductExcelPage where reportOrgCode='" + reportCode + "' and reportYear='" + excelPage.getReportYear() + "' and reportDate='" + excelPage.getReportDate() + "' and tpInputID=" + excelPage.getTpInputID());
            if (currentList != null && currentList.size() > 0) {//已经上报过或退回了
                String isWithDraw = "";
                for (DeductExcelPage page : currentList) {
                    if ("Y".equals(page.getIswithdraw())) {
                        isWithDraw = "Y";
                        break;
                    }
                }
                if (org.apache.commons.lang3.StringUtils.isBlank(isWithDraw)) {
                    if (EemConstants.REPORT_TIME_LOCK) {//开启时间锁
                        if (hasDatePower()) {
                            return "hasReportedShouldOverride";
                        } else {
                            return "reportDateError";//不在上报日期内
                        }
                    } else {
                        return "hasReportedShouldOverride";
                    }
                } else {
                    return "withdraw";
                }
            } else {//未上报过
                if (EemConstants.REPORT_TIME_LOCK) {//开启时间锁
                    if (hasDatePower()) {
                        return "ok";
                    } else {
                        return "reportDateError";//不在上报日期内
                    }
                } else {
                    return "ok";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ok";
    }

    @Override
    public String hasPowerToSave2(ExcelPage excelPage, UserEntity userEntity) {
        String result = "ok";
        try {
            String reportCode = AAAAAdapter.getCompany(Integer.parseInt(userEntity.getOrgEntity().getOrgId().toString())).getOrgCode();
            List<ExcelPage> currentList = baseDAO.find("from ExcelPage where deletedFlag=0 and reportOrgCode like '" + reportCode + "%' and reportYear='" + excelPage.getReportYear() + "' and reportDate='" + excelPage.getReportDate() + "' and tpInputID=" + excelPage.getTpInputID());
            List<ExcelPage> cityList = new ArrayList<ExcelPage>();
            List<ExcelPage> proList = new ArrayList<ExcelPage>();
            for (ExcelPage page : currentList) {
                if (page.getReportOrgCode().length() == 3) {
                    proList.add(page);
                } else {
                    cityList.add(page);
                }
            }
            if (proList != null && proList.size() > 0) {//已经上报过或退回了
                String isWithDraw = "";
                for (ExcelPage page : proList) {
                    if ("Y".equals(page.getIswithdraw())) {
                        isWithDraw = "Y";
                        break;
                    }
                }
                if (org.apache.commons.lang3.StringUtils.isBlank(isWithDraw)) {
                    if (EemConstants.REPORT_TIME_LOCK) {//开启时间锁
                        if (hasDatePower()) {
                            result = "hasReportedShouldOverride";
                        } else {
                            result = "reportDateError";//不在上报日期内
                        }
                    } else {
                        result = "hasReportedShouldOverride";
                    }
                } else {
                    result = "withdraw";
                }
            } else {//未上报过
                if (EemConstants.REPORT_TIME_LOCK) {//开启时间锁
                    if (hasDatePower()) {
                        result = "ok";
                    } else {
                        result = "reportDateError";//不在上报日期内
                    }
                } else {
                    result = "ok";
                }
            }
            for (ExcelPage page : cityList) {
                if (!page.getDeletedFlag() && !page.getWorkOrderStatus().equals("已审核")) {
                    result = "有地市数据未审核";
                    break;
                }
            }
            if(cityList.size()==0){
                result = "所有地市都未上报数据，不能进行汇总并上报";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean hasDatePower() {
        int currentDate = Integer.parseInt(DateUtils.getDateString(new Date(),
                "dd"));
        int currentMonth = Integer.parseInt(DateUtils.getDateString(new Date(),
                "MM"));

        boolean isReportDate = false;

        if (1 == currentMonth) {
            if (currentDate <= 20 && currentDate >= 1) {
                isReportDate = true;
            }
        } else if (4 == currentMonth) {
            if (currentDate <= 20) {
                isReportDate = true;
            }
        } else if (7 == currentMonth) {
            if (currentDate <= 20 && currentDate >= 1) {
                isReportDate = true;
            }
        } else if (10 == currentMonth) {
            if (currentDate <= 20 && currentDate >= 1) {
                isReportDate = true;

            }
        }
        return isReportDate;
    }

    // 光缆类型总部汇总导入
    public void importEvaluationFileDataForCable(long fileDataTid,
                                                 byte[] fileByteArray, EemTempEntity eemTempEntity,
                                                 UserEntity user, String reportCode, String reportDate, String reportYear) throws Exception {

        HSSFWorkbook wb = null;
        InputStream is = new ByteArrayInputStream(fileByteArray);
        wb = new HSSFWorkbook(is);
        int sheetCount = wb.getNumberOfSheets();
        //todo  删除上次该季度上报记录
        /*List<EvaluationPage> list = evaluationDao
                .getPageByReportDateAndFormIdAndWillCollect(reportDateStr,
                        reportYear, formidstr, "");
        if (list != null && list.size() > 0) {
            for (EvaluationPage deletePage : list) {
                evaluationDao.deletePageAndValues((int) deletePage.getPageId());
            }
        }*/

        for (int i = 0; i < sheetCount; i++) {
            HSSFSheet sheet = wb.getSheetAt(i);
            String pageName = sheet.getSheetName();
            ExcelPage ep = new ExcelPage();
            ep.setOperUserTrueName(user.getTrueName());
            ep.setReportOrgCode(reportCode);
            ep.setCreationTime(new Timestamp(System.currentTimeMillis()));
            ep.setObjectId(baseDAO.getSequenceNextValue(ExcelPage.class));
            ep.setPageName(pageName);
            ep.setOperUserPhone(user.getMobilePhone());
            ep.setTpInputID(eemTempEntity.getObjectId());
            if (reportDate == null || reportDate.equals("")) {
                ep.setReportDate("全年");
            } else {
                ep.setReportDate(reportDate);
            }
            ep.setReportYear(reportYear);
            ep.setFileDataId(fileDataTid);
            // 总部汇总光缆时设置Iswithdraw，为了在正常汇总时，pageName不包括总部汇总的
            ep.setIswithdraw("static");
            baseDAO.saveOrUpdate(ep);
        }

    }

    @Override
    public String excelToPageWeight(String objId) {
        String result = null;
        try {
            FileAdapter fileAdapter = FileAdapter.getInstance();
            DownloadFileInfo info = fileAdapter.download(objId);
            InputStream stream1 = new ByteArrayInputStream(info.getByteArrayOutputStream().toByteArray());

            result = new ExcelToHtml().parseExcelToHtml(stream1,null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public HSSFWorkbook getHssFWorkbook(String id) {
        return null;
    }

    @Override
    public Pager countAnalysis(Pager pager, UserEntity userEntity) {

        try {
            String monthParam="下半年";
            String yearParam="2016";
        int month =  Calendar.getInstance().get(Calendar.MONTH)+1;
        int year =  Calendar.getInstance().get(Calendar.YEAR);
            Map<String, Object> params = pager.getParameters();
            if (params.size() > 0) {
                if (params.get("reportDate") != null && org.apache.commons.lang3.StringUtils.isNotBlank(params.get("reportDate").toString())) {
                  monthParam =params.get("reportDate").toString();
                }
                if (params.get("year") != null && org.apache.commons.lang3.StringUtils.isNotBlank(params.get("year").toString())) {
                    yearParam = params.get("year").toString();
                }
            }else{
                if(month>8||month<3){
                    monthParam="下半年";
                }else{
                    monthParam="上半年";
                }
                if(month<3){
                    year = year-1;
                    yearParam = String.valueOf(year);
                }
            }


            List<EemTempEntity> tempEntityList = baseDAO.find("select new EemTempEntity(objectId,shortName) from EemTempEntity where deletedFlag=0 and tempType=3");;
            List<AnalysisDeduct> dataList = new ArrayList<AnalysisDeduct>();





        for(int i=0;i<tempEntityList.size();i++){
            AnalysisDeduct deduct = new AnalysisDeduct();
            deduct.setEquipmentName(tempEntityList.get(i).getTempName());
        //    List<DedcutPageValues> dedcutPageValueses = baseDAO.find(" from DedcutPageValues where deductTpID="+tempEntityList.get(i).getObjectId());

            List<Map> listVaules = baseDAO.findNativeSQL("SELECT v.* FROM t_eem_deduct_page_values v,t_eem_deduct_excel_page p where p.pageid=v.pageID and v.deductTpID="+tempEntityList.get(i).getObjectId()+" and p.reportDate=? and p.reportYear=?",new Object[]{monthParam,yearParam});
            deduct.setAilixin("-");
            deduct.setAlang("-");
            deduct.setBeier("-");
            deduct.setCisco("-");
            deduct.setDatang("-");
            deduct.setFenghuo("-");
            deduct.setHuasan("-");
            deduct.setHuawei("-");
            deduct.setJuniper("-");
            deduct.setNuoxi("-");
            deduct.setPutian("-");
            deduct.setZhongxing("-");
            deduct.setXinyoutong("-");
            deduct.setTefa("-");
            if(listVaules!=null&&listVaules.size()>0){
                    for(Map map:listVaules){
                      if("爱立信".equals(map.get("vendername")))  deduct.setAilixin(map.get("txtvalue").toString());
                        if("阿郎".equals(map.get("vendername")))   deduct.setAlang(map.get("txtvalue").toString());
                        if("贝尔".equals(map.get("vendername"))) deduct.setBeier(map.get("txtvalue").toString());
                        if("CISCO".equals(map.get("vendername")))   deduct.setCisco(map.get("txtvalue").toString());
                        if("大唐".equals(map.get("vendername")))    deduct.setDatang(map.get("txtvalue").toString());
                        if("烽火".equals(map.get("vendername")))   deduct.setFenghuo(map.get("txtvalue").toString());
                        if("华三".equals(map.get("vendername")))   deduct.setHuasan(map.get("txtvalue").toString());
                        if("华为".equals(map.get("vendername")))   deduct.setHuawei(map.get("txtvalue").toString());
                        if("JUNIPER".equals(map.get("vendername")))    deduct.setJuniper(map.get("txtvalue").toString());
                        if("诺西".equals(map.get("vendername")))     deduct.setNuoxi(map.get("txtvalue").toString());
                        if("普天".equals(map.get("vendername")))   deduct.setPutian(map.get("txtvalue").toString());
                        if("中兴".equals(map.get("vendername")))   deduct.setZhongxing(map.get("txtvalue").toString());
                        if("新邮通".equals(map.get("vendername")))   deduct.setXinyoutong(map.get("txtvalue").toString());
                        if("特发".equals(map.get("vendername")))   deduct.setTefa(map.get("txtvalue").toString());
                    }
            }else {
                deduct.setAilixin("");
                deduct.setAlang("");
                deduct.setBeier("");
                deduct.setCisco("");
                deduct.setDatang("");
                deduct.setFenghuo("");
                deduct.setHuasan("");
                deduct.setHuawei("");
                deduct.setJuniper("");
                deduct.setNuoxi("");
                deduct.setPutian("");
                deduct.setZhongxing("");
                deduct.setXinyoutong("");
                deduct.setTefa("");
            }

            dataList.add(deduct);
       }
            pager.setExhibitDatas(dataList);
        pager.setIsSuccess(true);
        pager.setPageCount(dataList.size() > 0 ? 1 : 0);
        pager.setRecordCount(dataList.size());
        pager.setPageSize(dataList.size());
        return pager;
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public String findAttIds(String reportYear, String decode) {
        String attIds="";
        try {
            String sql =" select * from t_eem_deduct_excel_page where pageid>0 and reportYear ='"+reportYear+"' and reportDate='"+decode+"'";
            List<Map> list =baseDAO.findNativeSQL(sql, null);
            for(int i =0;i<list.size();i++){
                attIds+=list.get(i).get("attachmentid")+",";
            }
            if(list.size()>0){
                attIds= attIds.substring(0,attIds.length()-1);
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return attIds;
    }

    @Override
    public boolean deleteReportData(String pageid, UserEntity userEntity) {
        try {
            DeductExcelPage excelPage = (DeductExcelPage)baseDAO.get(DeductExcelPage.class,Long.parseLong(pageid));
                List values = baseDAO.find("from DedcutPageValues where pageID="+excelPage.getObjectId());
                if(values!=null&&values.size()>0){
                    baseDAO.deleteAll(values);
                }

            baseDAO.delete(excelPage);
            return  true;
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public String findAllVender(){
        String sql = "select * from t_eem_vender;";
        try {
            List<Map> list = baseDAO.findNativeSQL(sql, null);
            return JSONArray.toJSONString(list);
        } catch (DAOException e) {
            e.printStackTrace();
            return "";
        }
    }




}